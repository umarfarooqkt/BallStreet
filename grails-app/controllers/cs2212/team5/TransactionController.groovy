package cs2212.team5

import grails.plugin.springsecurity.annotation.Secured
import grails.rest.RestfulController

@Secured(['ROLE_USER'])
class TransactionController extends RestfulController {

    static allowedMethods = [getPastTransactions: 'POST', getPendingTransactions: 'POST', createTransaction: 'POST']
    static responseFormats = ['json']

    def springSecurityService

    TransactionController() {
        super(Transaction)
    }

    def getPastTransactions() {
        def userName = springSecurityService.currentUser.username
        def user = UserAccount.find{username == userName}
        if (user != null) { //if user exists reutrn their transactions sorted by id
            respond user.transactions.findAll{it.tStatus == "closed" || it.tStatus == "failed"}.sort{it.transactionID}
        }
        else {
            response.status = 501 //user does not exist
        }
    }

    def getPendingTransactions() {
        def userName = springSecurityService.currentUser.username
        def user = UserAccount.find{username == userName}
        if (user != null) { //if user exists reutrn their transactions sorted by id
            respond user.transactions.findAll{it.tStatus == "open"}.sort{it.transactionID}
        }
        else {
            response.status = 501 //user does not exist
        }
    }

    def createTransaction() {
        def fName = params.firstName
        def lName = params.lastName
        def quantity
        try {
            quantity = Integer.parseInt(params.quantity)
        }
        catch (NumberFormatException e) {
            quantity = -1
        }
        def price = Double.parseDouble(params.price)
        def tType = params.tType
        def userName = springSecurityService.currentUser.username
        def user = UserAccount.find{username == userName}
        if (quantity <= 0) {
            response.status = 506 //invalid quantity
        }
        else if (user == null) {
            response.status = 501 //user does not exist
        }
        else {
            if (tType == "buy") {
                if (user.balance < price*quantity) {
                    response.status = 502 //insufficient funds
                }
                else { //sufficient funds
                    def currentDate = new Date()
                    user.transactionCount = user.transactionCount + 1
                    //create buy transaction
                    def newTransaction = new Transaction(tType: "buy", tStatus: "open", transactionOpened: currentDate, stockFirstName: fName, stockLastName: lName, stockPrice: price, stockQuantity: quantity, transactionID: user.transactionCount, balanceBefore: user.balance, creator: user).save()
                    user.balance = user.balance - quantity*price
                    user.addToTransactions(newTransaction).save(flush: true)
                    addToPortfolio(user, fName, lName, quantity)
                }
            }
            else if (tType == "sell") {
                def stock = user.portfolio.find{it.stockFirstName == fName && it.stockLastName == lName}
                if (stock != null) {
                    if (quantity > stock.quantityOwned) {
                        response.status = 503 //insufficient quantity
                    }
                    else { //sufficient quantity
                        def currentDate = new Date()
                        user.transactionCount = user.transactionCount + 1
                        //create sell transaction
                        def newTransaction = new Transaction(tType: "sell", tStatus: "open", transactionOpened: currentDate, stockFirstName: fName, stockLastName: lName, stockPrice: price, stockQuantity: quantity, transactionID: user.transactionCount, balanceBefore: user.balance, creator: user).save()
                        user.balance = user.balance + quantity*price
                        user.addToTransactions(newTransaction).save(flush: true)
                        removeFromPortfolio(user, fName, lName, quantity)
                    }
                }
                else {
                    response.status = 504 //do not own stock
                }
            }
            else {
                response.status = 505 //incorrect type
            }
        }
    }

    def addToPortfolio(UserAccount user, String fName, String lName, int quantity) {
        //adds stock to user's portfolio
        def stock = user.portfolio.find{it.stockFirstName == fName && it.stockLastName == lName}
        //System.out.println("add " + stock)
        if (stock != null) {
            stock.quantityOwned = stock.quantityOwned + quantity
            stock.save(flush: true)
        }
        else {
            def newStock = new Stock(stockFirstName: fName, stockLastName: lName, quantityOwned: quantity, quantityBefore: 0, owner: user).save()
            user.addToPortfolio(newStock).save(flush: true)
        }
    }

    def removeFromPortfolio(UserAccount user, String fName, String lName, int quantity) {
        //removes stock from user's portfolio
        def stock = user.portfolio.find{it.stockFirstName == fName && it.stockLastName == lName}
        //System.out.println("remove " + stock)
        stock.quantityOwned = stock.quantityOwned - quantity
        stock.save(flush: true)
    }

    def index() { }
}
