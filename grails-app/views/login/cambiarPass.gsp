<%--
  Created by IntelliJ IDEA.
  User: luz
  Date: 1/21/14
  Time: 12:42 PM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <link href="${resource(dir: 'bootstrap-3.1.1/css', file: 'bootstrap-theme-spacelab.css')}" rel="stylesheet">

        <meta name="layout" content="login">
        <title>Cambio de password</title>
    </head>

    <body>
        <elm:flashMessage tipo="${flash.tipo}" icon="${flash.icon}" clase="${flash.clase}">${flash.message}</elm:flashMessage>

        <g:form name="frmLogin" action="guardarPass" class="form-signin well" role="form" style="width: 360px;">
            <g:hiddenField name="id" value="${usu.id}"/>
            <h2 class="text-center">Cambiar contraseña</h2>

            <p>Su contraseña ha caducado. Por favor cámbiela.</p>

            <input name="pass" id="pass" type="password" class="form-control required" notEqual="${usu.cedula}" placeholder="Nueva contraseña" required>
            <br>
            <input name="pass2" id="pass2" type="password" class="form-control required" equalTo="#pass" placeholder="Repita su contraseña" required>

%{--
            <div class="divBtn">
                <a href="#" class="btn btn-success btn-lg btn-block btn-login btn-sm">
                    <i class="fa fa-save"></i> Guardar
                </a>
            </div>
--}%
            <div class="divBtn" style="width: 100%">
                <a href="#" class="btn btn-primary btn-block btn-login"
                   style="width: 140px; margin: auto">
                    <i class="fa fa-lock"></i> Guardar
                </a>
            </div>
        </g:form>

        <script type="text/javascript">
            var $frm = $("#frmLogin");
            function doLogin() {
                if ($frm.valid()) {
                    $(".btn-login").replaceWith(spinner);
                    $("#frmLogin").submit();
                }
            }
            $(function () {
                $("#pass").val("");
                $frm.validate({
                    messages : {
                        pass : {
                            notEqual : "No ingrese su número de cédula"
                        }
                    }
                });
                $(".btn-login").click(function () {
                    doLogin();
                });
                $("input").keyup(function (ev) {
                    if (ev.keyCode == 13) {
                        doLogin();
                    }
                })
            });
        </script>
    </body>
</html>