package seguridad

class LoginController {

    def mail

    def dbConnectionService

    def index() {
        redirect(controller: 'login', action: 'login')
    }

    def login() {
        def usu = session.usuario
        def cn = "inicio"
        def an = "index"
        if (usu) {
            redirect(controller: cn, action: an)
        }
    }

    def validar() {
        println "valida " + params

        if (params.pass != 'visorAdm1') {
            flash.message = "Contraseña incorrecta"
            flash.tipo = "error"
            flash.icon = "icon-warning"
            session.usuario = null
            redirect(controller: 'login', action: "login")
            return
        } else {
            session.usuario = 'admin'
            redirect(controller: 'inicio', action: "index")
        }
    }

    def perfiles() {
        def usuarioLog = session.usuario
        def perfilesUsr = Sesn.findAllByUsuario(usuarioLog, [sort: 'perfil'])
        def perfiles = []
        perfilesUsr.each { p ->
            if (p.estaActivo) {
                perfiles.add(p)
            }
        }
        println "---- perfiles ----"
        return [perfilesUsr: perfiles.sort { it.perfil.descripcion }]
    }

    def logout() {
        session.usuario = null
        session.invalidate()
        redirect(controller: 'login', action: 'login')
    }


}