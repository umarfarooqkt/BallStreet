import React from 'react';

const checkStatus = (response) => {
    console.log(response.status);
    if(response.status >= 200 && response.status < 300) {
        return response.json()
    } else {
        var error = new Error(response.status == 401 ? "Invalid username and/or password" : response.statusText);
        error.response = response;
        throw error;
    }
};

var UserAccountCreator = React.createClass({
    getInitialState()
    {
        return {
            userName : "",
            message : "",
            password: ""
        }
    },

    componentDidMount() {
        //get score data and store in browser storage
        fetch("http://localhost:8080/score/getScore", {method: 'POST'}).then(response => {
            if(response.ok) {
                response.json().then(json => {
                    //creates table heading
                    let result = "";
                    for (let i = 0; i < json.length; i++) {
                        result = result + json[i];
                    }
                    if (json.length == 0) {
                        result = "None..."
                    }
                    sessionStorage.setItem("marquee", result);
                });
            }
            else {
                console.log("Error: " + response.status);
                sessionStorage.setItem("marquee", "Error retrieving scores...");
            }
        });
    },

    handleNameChange(e) {
        e.preventDefault();
        this.setState({userName : e.target.value});
    },

    handlePasswordChange(e) {
        e.preventDefault();
        this.setState({password : e.target.value});
    },

    handleSignup(e) {
        e.preventDefault();
        let name = this.state.userName;
        let password = this.state.password;
        //make call to controller method attempting to create a user and display message based on success or failure status
        fetch("http://localhost:8080/userAccount/createUser?userName=" + name + "&password=" + password, {method: 'POST', headers: {"Content-Type": "application/json"}}).then(response => {
            console.log(response.status);
            if (response.ok) {
                this.setState({message : name + " was created successfully!"});
            }
            else {
                let msg = "Error: " + response.status;
                switch(response.status) {
                    case 401: msg = "Unauthorized"; break;
                    case 501: msg = "Missing username field..."; break;
                    case 502: msg = "Missing password field..."; break;
                    case 503: msg = name + " is taken"; break;
                }
                this.setState({message : msg});
            }
        });
    },

    handleLogin(e) {
        e.preventDefault();
        let name = this.state.userName;
        let password = this.state.password;
        fetch("/api/login", {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({username: name, password: password})
        })
            .then(checkStatus)
            .then(this.success.bind(this))
            .catch(this.fail.bind(this));
    },

    success(authObject) {
        console.log("Signed in", authObject);
        let name = this.state.userName;
        if(authObject) {
            localStorage.authObject = JSON.stringify(authObject);
        }
        let token = JSON.parse(localStorage.authObject).access_token;
        //make call to controller method attempting to login and display message if failure, otherwise link to home page
        fetch("http://localhost:8080/userAccount/getUser?userName=" + name, {method: 'POST', headers: {'Authorization': 'Bearer ' + token}}).then(response => {
            console.log(response.status);
            if (response.ok) {
                response.json().then(json => {
                    //if successful then store name, balance and netWorth (to be accessed by other pages) and link to home page
                    sessionStorage.setItem("balance", json.balance.toFixed(2));
                    sessionStorage.setItem("netWorth", json.netWorth.toFixed(2));
                    sessionStorage.setItem("username", name);
                    window.location.href='/home';
                    this.setState({message : name + " successfully logged in!"});
                });
            }
            else {
                let msg = "Error: " + response.status;
                switch(response.status) {
                    case 401: msg = "Unauthorized"; break;
                    case 501: msg = "User does not exist..."; break;
                }
                this.setState({message : msg});
            }
        });
    },

    fail(res) {
        console.log("Failed to sign in" + res);
        this.setState({message: res.message});
    },

    render() {
        //sets up a form for login data input
        return (
            <div>
                    <form onSubmit={this.handleSubmit}>
                        <p className="control is-small">
                            <input className = "input" type="text" placeholder="Username" defaultValue={this.state.userName} onChange={this.handleNameChange}/>
                        </p>
                        <p className="control is-small">
                            <input className = "input" type="password" placeholder = "Password" defaultValue={this.state.password} onChange={this.handlePasswordChange}/>
                            {this.state.message}
                        </p>
                    </form>

                <p className="control is-grouped">
                    <a className="button is-outlined is-danger" onClick={this.handleSignup}>Sign Up</a>
                    <a className="button is-outlined is-success" onClick={this.handleLogin}>Login</a>
                </p>

            </div>
        );
    }
});

export class LoginPage extends React.Component {

    constructor() {
        super();
    }

    render() {
        return(
            <div>
            <UserAccountCreator/>
            </div>
        );
    }
}