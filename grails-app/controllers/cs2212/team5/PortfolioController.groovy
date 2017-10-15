package cs2212.team5

import grails.plugin.springsecurity.annotation.Secured

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.ParseException
import java.text.SimpleDateFormat

@Secured(['ROLE_USER'])
class PortfolioController {

    static allowedMethods = [getNetWorthHistory: 'POST', getBalanceHistory: 'POST', getPortfolio: 'POST']
    static responseFormats = ['json']

    def springSecurityService

    def getNetWorthHistory() {
        def name = springSecurityService.currentUser.username
        //testing if user with given username exists
        def user = UserAccount.find{username == name}
        if (user != null) { //if user exists
            respond user.netWorthHistory
        }
        else {
            response.status = 501 //user does not exist (failure)
        }
    }

    def getBalanceHistory() {
        def name = springSecurityService.currentUser.username
        //testing if user with given username exists
        def user = UserAccount.find{username == name}
        if (user != null) { //if user exists
            respond user.balanceHistory
        }
        else {
            response.status = 501 //user does not exist (failure)
        }
    }

    def getPortfolio() {
        //method which returns user's portfolio with player data from SQL DB
        Connection connection = null;
        try {
            def name = springSecurityService.currentUser.username
            //testing if user with given username exists
            def user = UserAccount.find{username == name}
            if (user != null) { //if user exists
                String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
                connection = DriverManager.getConnection(url, "Zain", "password")
                Statement statement = connection.createStatement()
                ResultSet result
                PlayerSummary [] rtrn = new PlayerSummary[user.portfolio.size()]
                int i = 0
                int days = numDays()
                for (s in user.portfolio) {
                    //SQL Query
                    result = statement.executeQuery("SELECT * FROM INITIALSTOCKPRICES WHERE `#LastName`='" + s.stockLastName + "' AND `#FirstName`='" + s.stockFirstName + "'");
                    while (result.next()) {
                        rtrn[i] = new PlayerSummary(firstName: result.getString("#FirstName"), lastName: result.getString("#LastName"), team: result.getString("#Team Abbr."), previousDayPrice: result.getDouble(days+23), currentPrice: result.getDouble("#CurrentPrice"), quantityOwned: s.quantityOwned)
                        i = i + 1
                    }
                }
                respond rtrn
            }
            else {
                response.status = 502 //user does not exist (failure)
            }
        }
        catch (SQLException e) {
            System.out.println(e.getMessage())
            response.status = 501 //connection or query issue
        }
        finally {
            if (connection != null) {
                try {
                    connection.close()
                }
                catch (SQLException e) {
                    System.out.println(e.getMessage())
                    response.status = 501 //connection or query issue
                }
            }
        }
    }

    def numDays() {
        //method that calculates days since DB was created - in order to figure out column of current date in DB
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Date f = df.parse("2017-03-19");
            Date t = new Date();
            return ((t.getTime() - f.getTime()) / (1000 * 60 * 60 * 24))
        }
        catch (ParseException e) {
            System.out.println(e.getMessage())
            return -1
        }
    }

    def index() { }
}
