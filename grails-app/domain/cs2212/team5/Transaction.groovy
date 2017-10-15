package cs2212.team5

class Transaction {

    Date transactionOpened
    Date transactionClosed
    String tType
    String tStatus
    String stockFirstName
    String stockLastName
    double stockPrice
    int stockQuantity
    int transactionID

    double balanceBefore

    static belongsTo = [creator : UserAccount]

    static constraints = {
        transactionClosed nullable: true
    }
}
