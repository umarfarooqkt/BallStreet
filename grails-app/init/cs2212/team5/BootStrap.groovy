package cs2212.team5

import grails3.example.Role

class BootStrap {

    def init = { servletContext ->
        def global = League.find{name == "Global Leaderboard"}
        if (global == null) {
            new League(numMembers: 0, maxMembers: -1, name: "Global Leaderboard").save()
        }
        def role = Role.find{authority: 'ROLE_USER'}
        if (role == null) {
            new Role(authority: 'ROLE_USER').save()
        }
    }
    def destroy = {
    }
}
