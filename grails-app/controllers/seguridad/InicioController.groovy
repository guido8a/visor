package seguridad

class InicioController {

    def index() {
    }

    def parametros = {

        if (session.usuario.puedeAdmin) {
            return []
        } else {
            flash.message = "Está tratando de ingresar a un pantalla restringida para su perfil. Está acción será registrada."
            response.sendError(403)
        }
    }
}
