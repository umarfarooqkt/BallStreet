package cs2212.team5

class UpdateController{

    static allowedMethods = [updateTransactions: 'POST']
    static responseFormats = ['json']

    def TransactionService

    def updateTransactions() {
        System.out.println("Updating Transactions...")
        TransactionService.serviceMethod()
        response.status = 200
    }

    def index() { }
}
