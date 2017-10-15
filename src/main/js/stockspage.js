import React from 'react';

var PlayerEntry = React.createClass({
    getInitialState() {
        return {
            change: 0,
            quantity: 0
        }
    },

    componentDidMount() {
        let change = this.props.price-this.props.pPrice;
        this.setState({change: change});
    },

    qtyChange(e) {
        e.preventDefault();
        this.setState({quantity: e.target.value});
    },

    handleSubmit(e) {
        e.preventDefault();
        let token = JSON.parse(localStorage.authObject).access_token;
        fetch('http://localhost:8080/transaction/createTransaction?firstName=' + this.props.firstName + '&lastName=' + this.props.lastName + '&price=' + this.props.price + '&quantity=' + this.state.quantity + '&tType=sell', {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                console.log(response.status);
                if (response.ok) {
                    alert("Successfully sold");
                }
                else {
                    let msg = "Error: " + response.status;
                    switch(response.status) {
                        case 401: msg = "Unauthorized"; break;
                        case 501: msg = "Invalid User..."; break;
                        case 502: msg = "Insufficient balance..."; break;
                        case 503: msg = "Insufficient quantity..."; break;
                        case 504: msg = "Stock is no longer owned..."; break;
                        case 505: msg = "Incorrect transaction type..."; break;
                        case 506: msg = "Invalid quantity..."; break;
                    }
                    alert(msg);
                }
            });
    },

    render() {
        return (
            <tbody>
            <tr>
                <td>{this.props.firstName}</td>
                <td>{this.props.lastName}</td>
                <td>{this.props.team}</td>
                <td>{this.props.price.toFixed(2)}</td>
                <td>{this.state.change.toFixed(2)}</td>
                <td>{this.props.quantity}</td>
                <td>
                    <form onSubmit={this.handleSubmit}>
                        <input type="text" placeholder="Quantity" onChange={this.qtyChange}/>
                        <input className = "sellButton" type="submit" defaultValue="Sell!"/>
                    </form>
                </td>
            </tr>
            </tbody>
        );
    }

});

var Stocks = React.createClass({
    getInitialState() {
        return {
            refresh: 1,
            playerData: []
        }
    },

    refreshData() {
        let ref = this.state.refresh + 1;
        this.setState({refresh: ref});
    },

    componentDidMount() {
        let token = JSON.parse(localStorage.authObject).access_token;
        fetch('http://localhost:8080/portfolio/getPortfolio', {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                if (response.ok) {
                    response.json().then(json => {
                        let result = [];
                        result.push(<thead><tr><th>First Name:</th><th>Last Name:</th><th>Team:</th><th>Price:</th><th>Change:</th><th>Qty:</th><th>Sell!</th></tr></thead>);
                        for (let i = 0; i < json.length; i++) {
                            result.push(<PlayerEntry firstName={json[i].firstName} lastName={json[i].lastName} pPrice={json[i].previousDayPrice} price={json[i].currentPrice} team={json[i].team} quantity={json[i].quantityOwned}/>);
                        }
                        this.setState({playerData: result});
                    })
                }
            });
    },


    render() {
        //puts together all the different components
        return(
            <div>
                <br/>
                <table className="stocksList">
                    {this.state.playerData}
                </table>
            </div>
        );
    }
});

export class StockPage extends React.Component {

    constructor() {
        super();
    }

    render() {
        return(
            <div>
                <Stocks/>
            </div>
        );
    }
}