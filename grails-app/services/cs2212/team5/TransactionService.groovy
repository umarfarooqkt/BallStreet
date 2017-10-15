package cs2212.team5

import grails.transaction.Transactional

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

@Transactional
class TransactionService {

    def addToPortfolio(UserAccount user, String fName, String lName, int quantity) {
        //adds stock to user's portfolio
        def stock = user.portfolio.find{it.stockFirstName == fName && it.stockLastName == lName}
        if (stock != null) {
            stock.quantityOwned = stock.quantityOwned + quantity
            System.out.println(stock.quantityOwned)
            stock.save(flush: true)
        }
        else {
            def newStock = new Stock(stockFirstName: fName, stockLastName: lName, quantityOwned: quantity, quantityBefore: 0, owner: user).save()
            user.addToPortfolio(newStock).save(flush: true)
        }
    }

    def removeFromPortfolio(UserAccount user, String fName, String lName, int quantity) {
        //removes stock from user's portfolio and deletes if 0 quantity
        def stock = user.portfolio.find{it.stockFirstName == fName && it.stockLastName == lName}
        if (stock.quantityOwned == quantity) {
            user.removeFromPortfolio(stock)
            stock.delete(flush: true)
            user.save(flush: true)
        }
        else {
            stock.quantityOwned = stock.quantityOwned - quantity
            stock.save(flush: true)
        }
    }

    def calculateNetWorth(UserAccount user, Statement statement, ResultSet result) {
        user.netWorth = user.balance
        for (s in user.portfolio) {
            def price = 0
            String lastName = s.stockLastName
            String firstName = s.stockFirstName
            try {
                result = statement.executeQuery("SELECT `#FirstName`,`#LastName`,`#CurrentPrice` FROM INITIALSTOCKPRICES WHERE `#LastName`='" + lastName + "' AND `#FirstName`='" + firstName + "'");
                while ( result.next() ) {
                    price = result.getDouble(3)
                }
            }
            catch (SQLException e) {
                System.out.println(e.getMessage())
            }
            user.netWorth = user.netWorth + price*s.quantityOwned
        }
    }

    def serviceMethod() {
        Connection connection;
        Statement statement
        ResultSet result;
        try {
            String url = "jdbc:mysql://team5-compsci2212.cgndepqzlosf.us-east-1.rds.amazonaws.com/Initialized_Players";
            connection = DriverManager.getConnection(url, "Zain", "password");
            statement = connection.createStatement();
        }
        catch (SQLException e) {
            System.out.println("Cannot connect to SQL database...")
            System.out.println(e.getMessage())
            return
        }

        def allUsers = UserAccount.findAll()
        def currentDate = new Date()
        def hours = currentDate.getHours()
        def minutes = currentDate.getMinutes()
        for (user in allUsers) { //for each user
            def pendingTransactions = user.transactions.findAll{it.tStatus == "open"}.sort{it.transactionID}
            if (pendingTransactions.size() > 0) { //if they have pending transactions
                double previousBalance = pendingTransactions.get(0).balanceBefore //adjust balance to before first enqueue
                System.out.println(user.username)
                for (stock in  user.portfolio) {
                    stock.quantityOwned = stock.quantityBefore //set quantity to quantity before transaction enqueued
                    stock.save(flush: true)
                }
                for (transaction in pendingTransactions) { //for each transaction
                    def price = 0
                    String lastName = transaction.stockLastName
                    String firstName = transaction.stockFirstName
                    try { //execute query and get price
                        result = statement.executeQuery("SELECT `#FirstName`,`#LastName`,`#CurrentPrice` FROM INITIALSTOCKPRICES WHERE `#LastName`='" + lastName + "' AND `#FirstName`='" + firstName + "'");
                        while ( result.next() ) {
                            price = result.getDouble(3)
                        }
                    }
                    catch (SQLException e) {
                        System.out.println("Cannot retrieve price for " + firstName + " " + lastName)
                        System.out.println(e.getMessage())
                        transaction.balanceBefore = previousBalance
                        transaction.tStatus = "failed"
                        transaction.save(flush: true)
                        continue
                    }

                    System.out.println("The price of " + firstName + " " + lastName + " is "+ price)
                    if (transaction.tType == "sell" && transaction.tStatus != "failed") { //if sell type
                        transaction.stockPrice = price
                        transaction.transactionClosed = currentDate
                        user.balance = previousBalance + transaction.stockPrice*transaction.stockQuantity
                        def stock = user.portfolio.find{it.stockFirstName == transaction.stockFirstName && it.stockLastName == transaction.stockLastName}
                        if (stock == null) { //if stock does not exist
                            transaction.balanceBefore = previousBalance
                            transaction.tStatus = "failed"
                            transaction.save(flush: true)
                            user.balance = previousBalance
                            user.save(flush: true)
                        }
                        else if (stock.quantityOwned >= transaction.stockQuantity) { //if sufficient quantity
                            user.save(flush: true)
                            transaction.balanceBefore = previousBalance
                            transaction.tStatus = "closed"
                            transaction.save(flush: true)
                            previousBalance = user.balance
                            removeFromPortfolio(user, transaction.stockFirstName, transaction.stockLastName, transaction.stockQuantity)
                        }
                        else { //otherwise transaction failed
                            transaction.balanceBefore = previousBalance
                            transaction.tStatus = "failed"
                            transaction.save(flush: true)
                            user.balance = previousBalance
                            user.save(flush: true)
                        }
                    }
                    else if (transaction.tType == "buy" && transaction.tStatus != "failed") { //if buy type
                        transaction.stockPrice = price
                        transaction.transactionClosed = currentDate
                        user.balance = previousBalance - transaction.stockPrice*transaction.stockQuantity
                        if (user.balance >= 0) { //if sufficient balance
                            user.save(flush: true)
                            transaction.balanceBefore = previousBalance
                            transaction.tStatus = "closed"
                            transaction.save(flush: true)
                            previousBalance = user.balance
                            addToPortfolio(user, transaction.stockFirstName, transaction.stockLastName, transaction.stockQuantity)
                        }
                        else { //otherwise transaction failed
                            transaction.balanceBefore = previousBalance
                            transaction.tStatus = "failed"
                            transaction.save(flush: true)
                            user.balance = previousBalance
                            user.save(flush: true)
                        }
                    }
                } //end transaction loop
                for (stock in  user.portfolio) { //set stock quantityOwned to quantityBefore enqueueing
                    stock.quantityBefore = stock.quantityOwned
                    stock.save(flush: true)
                }
                user.save(flush: true)
            } //end if (checking if user has pending transactions)
            calculateNetWorth(user, statement, result)
            user.save(flush: true)
            if (minutes %2 == 0) { //store networth and balance in history mechanisms, every two minutes
                for (int i = 9; i > 0; i --) {
                    user.netWorthHistory[i] = user.netWorthHistory[i-1]
                    user.balanceHistory[i] = user.balanceHistory[i-1]
                }
                user.netWorthHistory[0] = user.netWorth
                user.balanceHistory[0] = user.balance
            }
        } //end user loop

        connection.close();
    }
}

