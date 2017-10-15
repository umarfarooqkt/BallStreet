package cs2212.team5

class League {

    String name
    String password
    int numMembers
    int maxMembers

    static hasMany = [members : UserAccount]
    static belongsTo = UserAccount

    static constraints = {
        members nullable: true
        password nullable: true
    }
}
