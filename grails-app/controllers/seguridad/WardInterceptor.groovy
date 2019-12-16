package seguridad

class WardInterceptor {

    WardInterceptor () {
//        matchAll().excludes(controller: 'login')
        matchAll().excludes(controller:'login')
                .excludes(controller:'shield')
                .excludes(controller:'prfl')  /** todo: poner acciones base para incluir prfl **/
    }

    boolean before() {
        println "acción: " + actionName + " controlador: " + controllerName + " params: $params"
//        println "shield sesión: " + session
//        println "usuario: " + session.usuario
        session.an = actionName
        session.cn = controllerName
        session.pr = params
        def usro
        if(session) {
            usro = session.usuario
        }

        if(session.an == 'saveTramite' && session.cn == 'tramite'){
            println("entro")
            return true
        } else {
            if (!session?.usuario && !session?.perfil) {
                if(controllerName != "inicio" && actionName != "index") {
                    flash.message = "Usted ha superado el tiempo de inactividad máximo de la sesión"
                }
//                render grailsLinkGenerator.link(controller: 'login', action: 'login', absolute: true)
//                redirect(controller: 'login', action: 'login')
                render "<script type='text/javascript'> window.location.href = '/' </script>"
                session.finalize()
                return false
            } else {
//                def usu = Persona.get(session.usuario.id)
                println "session válida: ${usro}"
                    if (!isAllowed()) {
                        println "no permitido"
                        redirect(controller: 'shield', action: 'ataques')
                        return false
                    }

            }
        }

        true
    }

    boolean after() {
//        println "+++++después"
        true
    }

    void afterView() {
//        println "+++++afterview"
        // no-op
    }


    boolean isAllowed() {
//        println "--> ${session.permisos[controllerName.toLowerCase()]} --> ${actionName}"
//
//        try {
//            if((request.method == "POST") || (actionName.toLowerCase() =~ 'ajax')) {
//                println "es post no audit"
//                return true
//            }
////            println "is allowed Accion: ${actionName.toLowerCase()} ---  Controlador: ${controllerName.toLowerCase()} --- Permisos de ese controlador: "+session.permisos[controllerName.toLowerCase()]
//            if (!session.permisos[controllerName.toLowerCase()]) {
//                return false
//            } else {
//                if (session.permisos[controllerName.toLowerCase()].contains(actionName.toLowerCase())) {
//                    return true
//                } else {
//                    return false
//                }
//            }
//
//        } catch (e) {
//            println "Shield execption e: " + e
//            return false
//        }

        return true

    }

}
