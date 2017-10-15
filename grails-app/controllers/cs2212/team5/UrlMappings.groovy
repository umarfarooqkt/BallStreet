package cs2212.team5

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "/login"(view:"/login")
        "/home"(view:"/home")
        "/transactions"(view:"/transactions")
        "/settings"(view:"/settings")
        "/stocks"(view:"/stocks")
        "/leagues"(view:"/leagues")
        "/players"(view:"/players")
        "/market"(view:"/market")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
