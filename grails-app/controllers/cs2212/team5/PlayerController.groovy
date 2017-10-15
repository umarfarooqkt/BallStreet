package cs2212.team5

import grails.plugin.springsecurity.annotation.Secured

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Secured(['ROLE_USER'])
class PlayerController {

    static allowedMethods = [getAllPlayers: 'POST', getPlayersByKeyword: 'POST', getPlayer: 'POST', getSuggestedPlayers: 'POST', getPlayerPriceHistory: 'POST']
    static responseFormats = ['json']

    def getPlayersByKeyword() {
        //methods returns players which contain keyword from SQL DB
        Connection connection = null;
        try {
            String keyword = params.keyword
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
            connection = DriverManager.getConnection(url, "Zain", "password")
            Statement statement = connection.createStatement()
            ResultSet result
            //SQL Query
            result = statement.executeQuery("SELECT * FROM INITIALSTOCKPRICES WHERE `#LastName` LIKE '%" + keyword + "%' OR `#FirstName` LIKE '%" + keyword + "%'");
            int rows = 0
            if (result.last()) {
                rows = result.getRow()
                // Move to beginning
                result.beforeFirst()
            }
            System.out.println("The numbers of rows is " + rows + " with keyword " + keyword)
            int days = numDays()
            PlayerSummary[] rtrn = new PlayerSummary[rows]
            int i = 0
            while (result.next()) {
                rtrn[i] = new PlayerSummary(firstName: result.getString("#FirstName"), lastName: result.getString("#LastName"), team: result.getString("#Team Abbr."), previousDayPrice: result.getDouble(days+23), currentPrice: result.getDouble("#CurrentPrice"))
                i = i + 1
            }
            respond rtrn
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

    def getAllPlayers() {
        //method returns all players from SQL DB
        Connection connection = null;
        try {
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
            connection = DriverManager.getConnection(url, "Zain", "password")
            Statement statement = connection.createStatement()
            ResultSet result
            //SQL Query
            result = statement.executeQuery("SELECT * FROM INITIALSTOCKPRICES")
            int rows = 0
            if (result.last()) {
                rows = result.getRow()
                // Move to beginning
                result.beforeFirst()
            }
            System.out.println("The numbers of rows is " + rows)
            int days = numDays()
            PlayerSummary [] rtrn = new PlayerSummary[rows]
            int i = 0
            while (result.next()) {
                rtrn[i] = new PlayerSummary(firstName: result.getString("#FirstName"), lastName: result.getString("#LastName"), team: result.getString("#Team Abbr."), previousDayPrice: result.getDouble(days+23), currentPrice: result.getDouble("#CurrentPrice"))
                i = i + 1
            }
            respond rtrn
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

    def getPlayer() {
        //method returns play with given first and last name from SQL DB
        Connection connection = null;
        try {
            String firstName = params.firstName
            String lastName = params.lastName
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
            connection = DriverManager.getConnection(url, "Zain", "password")
            Statement statement = connection.createStatement()
            ResultSet result
            //SQL Query
            result = statement.executeQuery("SELECT * FROM INITIALSTOCKPRICES WHERE `#LastName`='" + lastName + "' AND `#FirstName`='" + firstName + "'");
            int rows = 0
            if (result.last()) {
                rows = result.getRow()
                // Move to beginning
                result.beforeFirst()
            }
            System.out.println("The numbers of rows is " + rows + " so...")
            int days = numDays()
            if (rows == 1) {
                PlayerProfile rtrn
                while (result.next()) {
                    rtrn = new PlayerProfile(firstName: result.getString("#FirstName"), lastName: result.getString("#LastName"), jerseyNumber: result.getString("#Jersey Num"), position: result.getString("#Position"), height: result.getString("#Height"), weight: result.getString("#Weight"), age: result.getInt("#Age"), teamCity: result.getString("#Team City"), teamName: result.getString("#Team Name"), gp: result.getInt("#GamesPlayed"), reb: result.getDouble("#RebPerGame"), ast: result.getDouble("#AstPerGame"), pts: result.getDouble("#PtsPerGame"), previousDayPrice: result.getDouble(days+23), currentPrice: result.getDouble("#CurrentPrice"))
                }
                respond rtrn
            }
            else {
                response.status = 502 //player does not exist
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

    def getSuggestedPlayers() {
        //method to return suggested players from SQL DB
        Connection connection = null;
        try {
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
            connection = DriverManager.getConnection(url, "Zain", "password")
            Statement statement = connection.createStatement()
            ResultSet result
            //SQL Query
            result = statement.executeQuery("SELECT * FROM topfiveplayers")
            PlayerSummary [] rtrn = new PlayerSummary[5]
            int i = 0
            while (result.next() && i < 5) {
                rtrn[i] = new PlayerSummary(firstName: result.getString("#FirstName"), lastName: result.getString("#LastName"), team: result.getString("#Team Abbr."), currentPrice: result.getInt("#FANTASYPOINTS")) //user current price to store daily fantasy points
                i = i + 1
            }
            respond rtrn
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

    def getPlayerPriceHistory() {
        //method to return players price history from SQL DB
        Connection connection = null;
        try {
            String firstName = params.firstName
            String lastName = params.lastName
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players"
            connection = DriverManager.getConnection(url, "Zain", "password")
            Statement statement = connection.createStatement()
            ResultSet result
            //SQL Query
            result = statement.executeQuery("SELECT * FROM INITIALSTOCKPRICES WHERE `#LastName`='" + lastName + "' AND `#FirstName`='" + firstName + "'");
            int rows = 0
            if (result.last()) {
                rows = result.getRow()
                // Move to beginning
                result.beforeFirst()
            }
            int days = numDays()
            System.out.println("The numbers of rows is " + rows + " so...")
            if (rows == 1) {
                double [] rtrn = new double[10];
                int i
                while (result.next()) {
                    for (i=0; i<9 && (23-i+days > 23); i++) {
                        double price = result.getDouble(23+days-i)
                        if (price == null) {
                            rtrn[i+1] = 0
                        }
                        else {
                            rtrn[i+1] = price
                        }
                    }
                    for (int j = i; j < 9; j++) {
                        rtrn[j+1] = 0
                    }
                    rtrn[0] = result.getDouble(23)
                }
                respond rtrn
            }
            else {
                response.status = 502 //player does not exist
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
