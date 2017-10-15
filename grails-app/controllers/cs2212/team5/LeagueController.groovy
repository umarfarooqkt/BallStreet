package cs2212.team5

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured(['ROLE_USER'])
class LeagueController extends RestfulController {

    static allowedMethods = [getLeagues: 'POST', getMyLeagues: 'POST', joinLeague: 'POST', leaveLeague: 'POST', getMembers: 'POST', createLeague: 'POST']
    static responseFormats = ['json']

    def springSecurityService

    LeagueController() {
        super(League)
    }

    /**
     * Controller method which returns leagues
     * @return all leagues
     */
    def getLeagues() {
        respond League.findAll().sort{it.id}
    }

    /**
     * Controller method which returns the leagues belonging to a given username
     * @return leagues belonging to username
     */
    def getMyLeagues() {
        def name = springSecurityService.currentUser.username
        //testing if user with given username exists
        def user = UserAccount.find{username == name}
        if (user != null) { //if user exists
            respond user.leagues.sort{it.id}
        }
        else {
            response.status = 501 //user does not exist (failure)
        }
    }

    /**
     * Controller method which, given a league name, username, and password, attempts to add user to the league
     * @return success or failure status
     */
    def joinLeague() {
        def leagueName = params.leagueName
        def pass = params.password
        def userName = springSecurityService.currentUser.username
        //testing if user and league exist
        def league = League.find{name == leagueName}
        def user = UserAccount.find{username == userName}

        //if both user and league exist and the league is not full (or maxMembers is -1 implies infinite size league)
        if (user != null && league != null && (league.numMembers < league.maxMembers || league.maxMembers == -1)) {
            if (league in user.leagues) {
                response.status = 501 //user is already in the league (failure)
            }
            else {
                if (league.password == "" || league.password == null) { //if league had no password, add user (success)
                    league.numMembers = league.numMembers + 1;
                    league.addToMembers(user).save(flush: true)
                    user.addToLeagues(league).save(flush: true)
                    response.status = 200
                }
                else { //else league has password
                    if (league.password == pass) { //is password is correct, add user (success)
                        league.numMembers = league.numMembers + 1;
                        league.addToMembers(user).save(flush: true)
                        user.addToLeagues(league).save(flush: true)
                        response.status = 200
                    }
                    else {
                        response.status = 502 //league password is incorrect (failure)
                    }
                }
            }
        }
        else {
            response.status = 503 //league/user do not exist or full league (failure)
        }
    }

    /**
     * Controller method which, given a league name and username, attempts to remove the user from the league
     * @return success or failure status
     */
    def leaveLeague() {
        def leagueName = params.leagueName
        def userName = springSecurityService.currentUser.username
        //testing if user and league exist
        def league = League.find{name == leagueName}
        def user = UserAccount.find{username == userName}

        if (user != null && league != null) { //if user and league exist
            if (league in user.leagues) { //if user is in leagues, remove user (success)
                user.removeFromLeagues(league).save(flush: true)
                if (league.numMembers == 1) { //if the user is the last user in the league, then delete league
                    league.delete(flush: true)
                }
                else { //user is not last user in league
                    league.numMembers = league.numMembers - 1
                    league.removeFromMembers(user).save(flush: true)
                }
                response.status = 200
            }
            else {
                response.status = 502 //user is not in the league (failure)
            }
        }
        else {
            response.status = 501 //league/user do not exist (failure)
        }
    }

    /**
     * Controller method which returns the members of a given league
     * @return members of league
     */
    def getMembers() {
        def leagueName = params.leagueName
        //testing if league exists
        def league = League.find{name == leagueName}
        if (league != null) {//if league exists
            respond league.members.sort{it.netWorth}
        }
        else
            response.status = 501 //league does not exist (failure)
    }

    /**
     * Controller method which, given a league name, owner username, and password, attempts to create a league with the given data
     * @return success or failure status
     */
    def createLeague() {
        def leagueName = params.leagueName
        def creatorName = springSecurityService.currentUser.username
        def pass = params.password
        //testing if league does not exist and owner does exist
        def league = League.find{name == leagueName}
        def leagueCreator = UserAccount.find{username == creatorName}

        if (leagueName == "") {
            response.status = 501
        }
        else if (leagueCreator == null) {
            response.status = 502 //user does not exist (failure)
        }
        else if (league == null) { //if league does not exist and owner exists, create new league (success)
            def newLeague = new League(numMembers: 1, maxMembers: 25, name: leagueName, password : pass).save()
            newLeague.addToMembers(leagueCreator).save(flush: true)
            leagueCreator.addToLeagues(newLeague).save(flush: true)
            response.status = 200
        }
        else {
            response.status = 503 //league already exists (failure)
        }
    }

    def index() { }
}
