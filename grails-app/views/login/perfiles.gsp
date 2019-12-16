<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <link href="${resource(dir: 'bootstrap-3.1.1/css', file: 'bootstrap-theme-spacelab.css')}" rel="stylesheet">

        <meta name="layout" content="login">
        <title>Perfil</title>
    </head>

    <body>

%{--    <div id="Canvas" style="background-image:url('${g.assetPath(src: 'apli/bitacora.png')}');height: 250px;background-repeat: no-repeat;background-position: right;width:1000px;">--}%
%{--    <div style="background-image:url('${g.assetPath(src: 'apli/compras.png')}');height: 250px;background-repeat: no-repeat;background-position: right; text-align: center; margin-top: 20px; height: ${(flash.message) ? '650' : '580'}px;" class="well">--}%
    <div style="background-repeat: no-repeat;background-position: right; text-align: center; margin-top: 20px; height: ${(flash.message) ? '650' : '580'}px;" class="well">
%{--        <h1 class="titl" style="font-size: 24px; color: #06a">...Ingreso al Sistema</h1>--}%
        <elm:flashMessage tipo="${flash.tipo}" icon="${flash.icon}"
                          clase="${flash.clase}">${flash.message}</elm:flashMessage>

        <h3>Seleccione su perfil de trabajo</h3>
        <div>
            <i class="fa fa-4x fa-users text-success"></i>
        </div>

        <div style="text-align: center; width: 100%; margin-left: 35%; margin-top: 5%">
            <elm:flashMessage tipo="${flash.tipo}" icon="${flash.icon}" clase="${flash.clase}">${flash.message}</elm:flashMessage>

            <g:form name="frmLogin" action="savePer" class="form-signin well" role="form" style="width: 300px;">
                <h3 class="text-center">Perfil</h3>
                <g:select name="prfl" class="form-control" from="${perfilesUsr}" optionKey="id" optionValue="perfil"/>
                <div class="divBtn" style="margin-top: 40px">
                    <a href="#" class="btn btn-primary btn-lg btn-block btn-login" id="btnPerfiles" style="width: 140px; margin: auto">
                        <i class="fa fa-lock"></i> Entrar
                    </a>
                </div>
            </g:form>
        </div>
    <div id="cargando" class="text-center hidden">
        <img src="${resource(dir: 'images', file: 'spinner32.gif')}" alt='Cargando...' width="32px" height="32px"/>
    </div>
    </div>

        <script type="text/javascript">
            var $frm = $("#frmLogin");
            function doLogin() {
                if ($frm.valid()) {
                    // $("#cargando").removeClass('hidden');
                    var dialog = cargarLoader("Cargando...");
                    // $(".btn-login").replaceWith($("#cargando"));
                    $("#btnPerfiles").replaceWith($("#cargando"));
                    $("#frmLogin").submit();
                }
            }
            $(function () {
                $frm.validate();
                // $(".btn-login").click(function () {
                $("#btnPerfiles").click(function () {
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