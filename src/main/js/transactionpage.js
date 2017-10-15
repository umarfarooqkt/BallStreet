import React from 'react';
import Select from 'react-select';

var TransactionEntry = React.createClass({
    getInitialState () {
        return {
            balanceBefore : 0,
            balanceChange : 0,
            balanceAfter : 0,
            date : null
        }
    },

    componentDidMount() {
        let tType= this.props.tType;
        let price = this.props.stockPrice;
        let quantity = this.props.stockQuantity;
        let balanceBefore = this.props.balanceBefore;
        let status = this.props.tStatus;
        let date = this.props.tDate;
        date = date.substring(0,10);
        if (status != "failed") {
            let tChange = 0;
            if (tType == "sell") {
                tChange = 1 * price * quantity;
            }
            else if (tType == "buy") {
                tChange = -1 * price * quantity;
            }
            let balanceAfter = balanceBefore + tChange;

            this.setState({balanceChange: tChange.toFixed(2), balanceAfter: balanceAfter.toFixed(2), balanceBefore: balanceBefore.toFixed(2), date: date});
        }
        else {
            this.setState({balanceChange: "N/A", balanceAfter: "N/A", balanceBefore: balanceBefore.toFixed(2), date: date});
        }
    },

    render () {
        return (
            <tbody>
            <tr>
                <td>{this.props.id}</td>
                <td>{this.state.date}</td>
                <td>{this.props.tType}</td>
                <td>{this.props.tStatus}</td>
                <td>{this.props.stockName}</td>
                <td>{this.props.stockPrice.toFixed(2)}</td>
                <td>{this.props.stockQuantity}</td>
                <td>{this.state.balanceBefore}</td>
                <td>{this.state.balanceChange}</td>
                <td>{this.state.balanceAfter}</td>
            </tr>
            </tbody>
        );
    }
});

var TransactionList = React.createClass({
    getInitialState() {
        return {
            transactions : [],
        }
    },

    fetchFromAPI() {
        let token = JSON.parse(localStorage.authObject).access_token;
        let url = this.props.url;
        console.log(token);
        //calls controller method attempting to get transactions and builds transaction list
        fetch('http://localhost:8080/transaction/'+url, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                console.log(url + response.status);
                if(response.ok) {
                    response.json().then(json => {
                        let results = [];
                        //creates table heading
                        results.push(<thead>
                        <tr>
                            <th rowSpan="2">ID:</th>
                            <th rowSpan="2">Date {this.props.type}:</th>
                            <th rowSpan="2">Type:</th>
                            <th rowSpan="2">Status:</th>
                            <th rowSpan="2">Stock Name:</th>
                            <th rowSpan="2">Price:</th>
                            <th rowSpan="2">Quantity:</th>
                            <th colSpan="3" className="centerAlign">Balance:</th>
                        </tr>
                        <tr>
                            <th>Before:</th>
                            <th>Change:</th>
                            <th>After:</th>
                        </tr>
                        </thead>);
                        if (url == "getPendingTransactions") {
                            for (let i = json.length-1; i > -1; i--) {
                                results.push(<TransactionEntry id={json[i].transactionID} tDate={json[i].transactionOpened} tType={json[i].tType} tStatus={json[i].tStatus} stockName={json[i].stockFirstName + " " + json[i].stockLastName} stockPrice={json[i].stockPrice} stockQuantity={json[i].stockQuantity} balanceBefore={json[i].balanceBefore}/>);
                            }
                        }
                        else {
                            for (let i = json.length-1; i > -1; i--) {
                                results.push(<TransactionEntry id={json[i].transactionID} tDate={json[i].transactionClosed} tType={json[i].tType} tStatus={json[i].tStatus} stockName={json[i].stockFirstName + " " + json[i].stockLastName} stockPrice={json[i].stockPrice} stockQuantity={json[i].stockQuantity} balanceBefore={json[i].balanceBefore}/>);
                            }
                        }
                        if (json.length == 0) {
                            let results = [];
                            results.push(<tr><td>No Transactions...</td></tr>);
                            this.setState({transactions: results});
                        }
                        else {
                            this.setState({transactions: []});
                            this.setState({transactions: results});
                        }
                    });
                }
                else{
                    this.setState({transactions: []});
                }
            });
    },

    componentDidMount() {
        this.fetchFromAPI();
    },

    /*componentWillReceiveProps() {
        setTimeout(this.fetchFromAPI, 500);
        console.log("here");
    },*/

    render() {
        //creates table of league entries
        return(
            <div>
                <table className="transactionList">
                    {this.state.transactions}
                </table>
            </div>
        )
    }
});

var Transaction = React.createClass({
    getInitialState() {
        return {
            refresh: 1
        }
    },

    refreshData() {
        let ref = this.state.refresh + 1;
        this.setState({refresh: ref});
    },

    render() {
        //puts together all the different components
        return(
            <div>
                <h1>Pending Transactions</h1>
                <TransactionList type="opened" url="getPendingTransactions" refresh={this.state.refresh} callback={this.refreshData}/><br/><br/>
                <h1>Transaction History</h1>
                <TransactionList type="closed" url="getPastTransactions" refresh={this.state.refresh} callback={this.refreshData}/>
            </div>
        );
    }
});

export class TransactionPage extends React.Component {

    constructor() {
        super();
    }

    render() {
        return(
            <div>
                <Transaction/>
            </div>
        );
    }
}