

import React from 'react';
import { LineChart, Line , CartesianGrid, XAxis, YAxis, Tooltip} from 'recharts';
import Select from 'react-select';

var LineGraph = React.createClass({
    getInitialState() {
        return {
            refresh: 1,
            playerData2: [],
            options: null,
            selected: "",
            graphData: null,
            text: "",
            name: ""
        }
    },

    componentDidMount() {
        let token = JSON.parse(localStorage.authObject).access_token;
        let options = [];
        fetch('http://localhost:8080/player/getAllPlayers', {
            method: 'POST',
            headers: {'Authorization': 'Bearer ' + token}
        })
            .then(response => {
                if (response.ok) {
                    response.json().then(json => {
                        for (let i = 0; i < json.length; i++) {
                            options.push({
                                label: json[i].firstName + " " + json[i].lastName,
                                value: json[i].firstName + " " + json[i].lastName,
                                firstName: json[i].firstName,
                                lastName: json[i].lastName
                            });
                        }
                        this.setState({options: options});
                    })
                }
            });
    },
    callAPI2(firstName, lastName) {
        let token = JSON.parse(localStorage.authObject).access_token;
        fetch('http://localhost:8080/player/getPlayer?lastName='+lastName+"&firstName="+firstName, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                if (response.ok) {
                    response.json().then(json => {
                        let result = [];
                        //creating table headings

                        result.push(<tr>
                            <th className="number">#</th>
                            <th className="position">POS</th>
                            <th className="height">Height</th>
                            <th className="weight">Weight</th>
                            <th className="age">Age</th>
                            <th className="city">City</th>
                            <th className="team">Team</th>
                            <th className="gp">GP</th>
                            <th className="reb">REB/GP</th>
                            <th className="ast">AST/GP</th>
                            <th className="pts">PTS/GP</th>

                        </tr>);
                        let change = json.currentPrice-json.previousDayPrice;
                        result.push(<tr>
                            <td>{json.jerseyNumber}</td>
                            <td>{json.position}</td>
                            <td>{json.height}</td>
                            <td>{json.weight}</td>
                            <td>{json.age}</td>
                            <td>{json.teamCity}</td>
                            <td>{json.teamName}</td>
                            <td>{json.gp}</td>
                            <td>{json.reb}</td>
                            <td>{json.ast}</td>
                            <td>{json.pts}</td>

                        </tr>);
                        let name = json.firstName + " " + json.lastName
                        this.setState({playerData2: []});
                        this.setState({playerData2: result, text: "Player Stock Price over Past 10 Days", name: name});
                    })
                }
            });
        fetch("http://localhost:8080/player/getPlayerPriceHistory?lastName="+lastName+"&firstName="+firstName, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
            if(response.ok) {
                response.json().then(json => {
                    //creates table heading
                    let testData = [
                        {Date: 'Day 1', price: json[9]},
                        {Date: 'Day 2', price: json[8]},
                        {Date: 'Day 3', price: json[7]},
                        {Date: 'Day 4', price: json[6]},
                        {Date: 'Day 5', price: json[5]},
                        {Date: 'Day 6', price: json[4]},
                        {Date: 'Day 7', price: json[3]},
                        {Date: 'Day 8', price: json[2]},
                        {Date: 'Day 9', price: json[1]},
                        {Date: 'Day 10', price:json[0]},
                    ];
                    this.setState({graphData:testData});
                });
            }
            else {
                let msg = "Error: " + response.status;
            }
        });
    },


    logChange(val) {
        let x = val.value;
        console.log("Selected: " + x);
        this.setState({selected: x});
        this.callAPI2(val.firstName, val.lastName);
    },

    render () {
        return (
            <div id="info">

                <Select className= "selectBar" value={this.state.selected} options={this.state.options} onChange={this.logChange}/>
                  <br/><br/>
                <table className = "playerTable">
                    {this.state.playerData2}
                </table>
                <br/><br/>
                <LineChart width={800} height={200} data={this.state.graphData} className="line">
                    <XAxis dataKey= "Date" />
                    <YAxis />
                    <Tooltip />
                    <CartesianGrid stroke='#f5f5f5'/>
                    <Line type='monotone' dataKey='price' stroke='red'/>
                </LineChart>
                <h3 id="chartTitle">{this.state.text} </h3>

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