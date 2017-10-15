package cs2212.team5

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController
import grails3.example.Role
import grails3.example.User
import grails3.example.UserRole

class UserAccountController extends RestfulController {

    static allowedMethods = [createUser: 'POST', getUser: 'POST']
    static responseFormats = ['json']

    def springSecurityService

    UserAccountController() {
        super(UserAccount)
    }

    /**
     * Controller method which, given a username and password, attempts to create a valid user
     * @return success or failure status
     */
    def createUser(){
        def userName = params.userName
        def password = params.password
        //testing if user with userName exists
        def userAccount = UserAccount.find{username == userName}

        //if user with userName doesn't exist and the user supplied a password and userName
        if(userAccount == null && password != "" && userName != ""){
            def role = Role.find{authority: 'ROLE_USER'}
            def user = new User(username: userName, password: password).save()
            UserRole.create(user, role, true)

            def newAccount = new UserAccount(userName, 1000, 1000).save()
            def global = League.find{name == "Global Leaderboard"}
            newAccount.addToLeagues(global)
            global.numMembers = global.numMembers + 1 //increment members in global leaderboard
            global.addToMembers(newAccount).save(flush: true) //add user to global leaderboard
            response.status = 200; //success
        }
        else if (userName == "")
            response.status = 501
        else if (password == "")
            response.status = 502
        else
            response.status = 503 //username is already taken (failure)
    }

    /**
     * Controller method which, given a username and password, attempts to login the user
     * @return user or failure status
     */
    @Secured(['ROLE_USER'])
    def getUser(){
        if (springSecurityService.currentUser != null) {
            def userName = springSecurityService.currentUser.username
            //System.out.println(userName + " logged in!")
            def user = UserAccount.find{username == userName} //attempts to find user with userName
            if(user != null){ //if user found, respond with user data
                respond user
            }
            else {
                response.status = 501
            }
        }
        else
            response.status = 501 //invalid user (failure)

    }

    def index() { }
}
