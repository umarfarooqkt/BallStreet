
import React from 'react';
import { LineChart, Line , CartesianGrid, XAxis, YAxis, Tooltip} from 'recharts';

var LineGraph = React.createClass({
    getInitialState () {
        return {
            graphData: null,
            graphData2: null
        }
    },

    componentDidMount(){
        let token = JSON.parse(localStorage.authObject).access_token;
        fetch("http://localhost:8080/portfolio/getNetWorthHistory", {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
            if (response.ok) {
                response.json().then(json => {
                    //creates table heading
                    let testData = [
                        {Date: '20', price: json[9]},
                        {Date: '18', price: json[8]},
                        {Date: '16', price: json[7]},
                        {Date: '14', price: json[6]},
                        {Date: '12', price: json[5]},
                        {Date: '10', price: json[4]},
                        {Date: '8', price: json[3]},
                        {Date: '6', price: json[2]},
                        {Date: '4', price: json[1]},
                        {Date: '2', price: json[0]},
                    ];
                    this.setState({graphData: testData});
                });
            }
            else {
                let msg = "Error: " + response.status;
            }
        });
        fetch("http://localhost:8080/portfolio/getBalanceHistory", {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
            if (response.ok) {
                response.json().then(json => {
                    //creates table heading
                    let testData = [
                        {Date: '20', price: json[9]},
                        {Date: '18', price: json[8]},
                        {Date: '16', price: json[7]},
                        {Date: '14', price: json[6]},
                        {Date: '12', price: json[5]},
                        {Date: '10', price: json[4]},
                        {Date: '8', price: json[3]},
                        {Date: '6', price: json[2]},
                        {Date: '4', price: json[1]},
                        {Date: '2', price: json[0]},
                    ];
                    this.setState({graphData2: testData});
                });
            }
            else {
                let msg = "Error: " + response.status;
            }
        });
    },

    render () {
        return (
        <div>
            <div id="chart1">
                <LineChart width={300} height={300} data={this.state.graphData} className="worthChart">
                    <XAxis dataKey= "Date" />
                    <YAxis />
                    <Tooltip />
                    <CartesianGrid stroke='#f5f5f5'/>
                    <Line type='monotone' dataKey='price' stroke='red'/>
                </LineChart>
                <h3 id="networthTitle"> Networth over the <br/>past 20 minutes</h3>
            </div>

            <div id="chart2">
                <LineChart width={300} height={300} data={this.state.graphData2} className="balanceChart">
                    <XAxis dataKey= "Date" />
                    <YAxis />
                    <Tooltip />
                    <CartesianGrid stroke='#f5f5f5'/>
                    <Line type='monotone' dataKey='price' stroke='red'/>
                </LineChart>
                <h3 id="balanceTitle"> Balance over the <br/>past 20 minutes</h3>
            </div>
        </div>
        );
    }
});

export class Graph extends React.Component {
    constructor() {
        super();
    }

    render() {
        return(
            <div>
                <LineGraph/>
            </div>
        );
    }
}