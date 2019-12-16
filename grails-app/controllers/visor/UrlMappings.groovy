package visor

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: "login", view:"/login")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
