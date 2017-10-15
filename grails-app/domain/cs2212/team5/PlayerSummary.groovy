package cs2212.team5

class PlayerSummary {

    String firstName
    String lastName
    double currentPrice
    double previousDayPrice
    String team
    int quantityOwned

    static constraints = {
        quantityOwned nullable: true
        previousDayPrice nullable: true
    }
}