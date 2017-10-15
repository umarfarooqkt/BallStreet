import React from 'react';

var UserEntry = React.createClass({
    getInitialState () {
        return {
        }
    },
    render () {
        //renders a row of the standings table
        return (
            <tbody>
                <tr className="standingsRow">
                    <td>{this.props.rank}</td>
                    <td>{this.props.userName}</td>
                    <td>{this.props.balance.toFixed(2)}</td>
                    <td>{this.props.netWorth.toFixed(2)}</td>
                </tr>
            </tbody>
        );
    }
});

var LeaveField = React.createClass({
    getInitialState () {
        return {
        }
    },

    handleSubmit(e) {
        e.preventDefault();
        let leagueName = this.props.name;
        let token = JSON.parse(localStorage.authObject).access_token;
        //make call to controller method attempting to league league, alert user with result
        fetch('http://localhost:8080/league/leaveLeague?leagueName=' + leagueName, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                if (response.ok) {
                    //alert("Successfully left " + leagueName);
                    this.props.callback();
                }
                else {
                    let msg = "Error: " + response.status;
                    switch(response.status) {
                        case 401: msg = "Unauthorized"; break;
                        case 501: msg = "League does not exist..."; break;
                        case 502: msg = "Already left league!"; break;
                    }
                    alert(msg);
                }
            });
    },

    render () {
        return (
            <td>
                <form onSubmit={this.handleSubmit}>
                    <input type="submit" className = "joinLeagueButton" defaultValue="Leave League!"/>
                </form>
            </td>
        );
    }
});

var JoinField = React.createClass({
    getInitialState () {
        return {
            input: "",
            inputPassword: ""
        }
    },

    handlePasswordChange(e) {
        e.preventDefault();
        this.setState({inputPassword: e.target.value});
    },

    componentDidMount() {
        let password = this.props.password;
        if (password != null) { //if there is a password, display password field
            this.setState({input:<input type="password" placeholder="Enter Password..." onChange={this.handlePasswordChange}/>});
        }
    },

    handleSubmit(e) {
        e.preventDefault();
        let password = this.state.inputPassword;
        let leagueName = this.props.name;
        let token = JSON.parse(localStorage.authObject).access_token;
        //make call to controller method attempting to join league, alert user of result
        fetch('http://localhost:8080/league/joinLeague?leagueName=' + leagueName + '&password=' + password, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
               if (response.ok) {
                   //alert("Successfully joined " + leagueName);
                   this.props.callback();
               }
               else {
                   let msg = "Error: " + response.status;
                   switch(response.status) {
                       case 401: msg = "Unauthorized"; break;
                       case 501: msg = "Already joined league!"; break;
                       case 502: msg = "Invalid password..."; break;
                       case 503: msg = "League is full or doesn't exist..."; break;
                   }
                   alert(msg);
               }
            });
    },

    render () {
        return (
            <td>
                <form onSubmit={this.handleSubmit}>
                    {this.state.input}
                    <input type="submit" className = "joinLeagueButton" defaultValue="Join League!"/>
                </form>
            </td>
        );
    }
});

var LeagueEntry = React.createClass({
    getInitialState () {
        return {
            userEntries : [],
            buttonStatus : "+",
            standings : "",
            maxMembers: "",
            joinField: ""
        }
    },

    toggleStandings(e) {
        e.preventDefault();
        let name = this.props.name;
        let status = this.state.buttonStatus;
        let token = JSON.parse(localStorage.authObject).access_token;
        if (status == "+") { //if button state is expand
            //calls controller method attempting to get members of a league, builds standings based on result
            fetch('http://localhost:8080/league/getMembers?leagueName='+ name, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
                if(response.ok) {
                    response.json().then(json => {
                        let results = [];
                        //creates table heading
                        results.push(<tr><th className="rank">Rank:</th><th>Username:</th><th>Balance:</th><th>Net Worth:</th></tr>);
                        let x = json.length;
                        for (let i = x-1; i >= 0; i--) {
                            results.push(<UserEntry rank={x-i} userName={json[i].username} balance={json[i].balance} netWorth={json[i].netWorth}/>);
                        }
                        this.setState({userEntries: results, buttonStatus: "-"});
                        //makes standings a table inside of a row of the league list
                        this.setState({standings:
                        <tr>
                            <td colSpan="4">
                                <table className="userList">
                                    {this.state.userEntries}
                                </table>
                            </td>
                        </tr>});
                    });
                }
                else{ //if response not ok, set everything to default value (collapse standings)
                    this.setState({userEntries: [], buttonStatus: "+"});
                    this.setState({standings: ""});
                }
            });
        }
        else { //if button state is collapse, then set everything to default value (collapse standings)
            this.setState({userEntries: [], buttonStatus: "+"});
            this.setState({standings: ""});
        }

    },

    componentDidMount() {
        let maxMembers = this.props.maxMembers;
        let join =  this.props.join;
        //different configurations depending on whether it is a default league or if join/leave should be displayed
        if (maxMembers == -1) {
            this.setState({maxMembers: "", joinField: <td>Default League...</td>});
        }
        else if (join == "false") {
            this.setState({maxMembers: "/" + maxMembers, joinField: <LeaveField name={this.props.name} callback={this.props.callback}/>});
        }
        else {
            this.setState({maxMembers: "/" + maxMembers, joinField: <JoinField password={this.props.password} name={this.props.name}  callback={this.props.callback}/>});
        }
    },

    render () {
        return (
            //renders a row of league list
            <tbody>
                <tr>
                    <td className="name">{this.props.name}</td>
                    <td className="members">{this.props.members}{this.state.maxMembers}</td>
                    {this.state.joinField}
                    <td className="expand"><button type="submit" className="viewButton" onClick={this.toggleStandings}>{this.state.buttonStatus}</button></td>
                </tr>
                {this.state.standings}
            </tbody>
        );
    }
});

var LeagueList = React.createClass({
    getInitialState() {
        return {
            leagueEntries : [],
        }
    },

    fetchFromAPI() {
        let urlExtension = this.props.url; //extension will determine if source of list is for user's leagues or all leagues
        let join = "true";
        let token = JSON.parse(localStorage.authObject).access_token;
        console.log(token);
        if (urlExtension != "getLeagues") { //if extension  is for user's leagues, get name, and set join to false so "Leave" buttons show up
            join = "false";
        }
        //calls controller method attempting to get leagues, and builds league list based on result
        fetch('http://localhost:8080/league/' + urlExtension, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}})
            .then(response => {
                console.log(response.status);
                if(response.ok) {
                    response.json().then(json => {
                        let results = [];
                        //creates table heading
                        results.push(<tr><th className="name">League Name:</th><th className="members">Status:</th><th>Join the League:</th><th className="expand">View Standings</th></tr>);
                        for (let i = 0; i < json.length; i++) {
                            results.push(<LeagueEntry name={json[i].name} members={json[i].numMembers} maxMembers={json[i].maxMembers} join={join} password={json[i].password} callback={this.props.callback}/>);
                        }
                        this.setState({leagueEntries: []});
                        this.setState({leagueEntries: results});
                    });
                }
                else{
                    this.setState({leagueEntries: []});
                }
            });
    },

    componentDidMount() {
        this.fetchFromAPI();
    },

    componentWillReceiveProps() {
        setTimeout(this.fetchFromAPI, 500);
        console.log("here");
    },

    render() {
        //creates table of league entries
        return(
            <div>
                <table className="leagueList">
                    {this.state.leagueEntries}
                </table>
            </div>
        )
    }
});


var LeagueCreator = React.createClass({
    getInitialState()
    {
        return {
            leagueName : "",
            message : "",
            password: ""
        }
    },

    handleNameChange(e) {
        e.preventDefault();
        this.setState({leagueName : e.target.value});
    },

    handlePasswordChange(e) {
        e.preventDefault();
        this.setState({password : e.target.value});
    },

    handleSubmit(e) {
        e.preventDefault();
        let name = this.state.leagueName;
        let password = this.state.password;
        let token = JSON.parse(localStorage.authObject).access_token;
        //calls controller method attempting to create a league, and displays message regarding the result
        fetch("http://localhost:8080/league/createLeague?leagueName=" + name +"&password=" + password, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
            if (response.ok) {
                this.setState({message : name + " was created successfully!"});
            }
            else {
                let msg = "Error: " + response.status;
                switch(response.status) {
                    case 401: msg = "Unauthorized"; break;
                    case 501: msg = "Missing league name field..."; break;
                    case 502: msg = "Invalid user..."; break;
                    case 503: msg = name + " is taken"; break;
                }
                this.setState({message: msg});
            }
        });
        this.props.callback();
    },

    render() {
        //sets up a form for league creation data input
        return (
            <div>
                <form onSubmit={this.handleSubmit}>

                    <p>Enter a League Name:</p>
                    <input type="text" defaultValue={this.state.leagueName} onChange={this.handleNameChange}/>

                    <p>Create League Password (blank for public league):</p>
                    <input type="password" defaultValue={this.state.password} onChange={this.handlePasswordChange}/>
                    <p>
                        {this.state.message}
                    </p>
                    <p><button  className = "leagueCreateButton">Create League!</button></p>
                </form>
            </div>
        );
    }
});

var League = React.createClass({
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
                <h1>Create League:</h1>
                <LeagueCreator callback={this.refreshData}/>
                <h1>My Leagues:</h1>
                <LeagueList url="getMyLeagues" refresh={this.state.refresh} callback={this.refreshData}/>
                <h1>All Leagues:</h1>
                <LeagueList url="getLeagues" refresh={this.state.refresh} callback={this.refreshData}/>
            </div>
        );
    }
});

export class LeaguePage extends React.Component {

    constructor() {
        super();
    }

    render() {
        return(
            <div>
                <League/>
            </div>
        );
    }
}