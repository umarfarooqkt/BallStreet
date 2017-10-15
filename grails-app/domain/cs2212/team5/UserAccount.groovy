package cs2212.team5

class UserAccount {

    String username
    double balance
    double netWorth
    double [] balanceHistory
    double [] netWorthHistory
    int transactionCount

    static hasMany = [leagues: League, transactions: Transaction, portfolio: Stock]

    UserAccount(String username, double balance, double netWorth) {
        this.username = username
        this.balance = balance
        this.netWorth = netWorth
        transactionCount = 0
        balanceHistory = new double[10]
        netWorthHistory = new double[10]
        for (int i = 0; i < 10; i++) {
            balanceHistory[i]=1000
            netWorthHistory[i]=1000
        }
        System.out.println(this.username + " was created!")
    }

    static constraints = {
        leagues nullable: true
        transactions nullable: true
        portfolio nullable: true
    }
}
