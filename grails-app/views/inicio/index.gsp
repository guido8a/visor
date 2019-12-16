<%@ page contentType="text/html;charset=UTF-8" %>

<html xmlns="http://www.w3.org/1999/html">
<head>
%{--    <title>Datos de Visor</title>--}%
    <meta name="layout" content="main"/>
    <style type="text/css">
    @page {
        size: 8.5in 11in;  /* width height */
        margin: 0.25in;
    }

    .item {
        width: 320px;
        height: 180px;
        float: left;
        margin: 4px;
        font-family: 'open sans condensed';
        background-color: #eceeff;
        border: 1px;
        border-color: #5c6e80;
        border-style: solid;
    }
    .item2 {
        width: 660px;
        height: 110px;
        float: left;
        margin: 4px;
        font-family: 'open sans condensed';
        background-color: #eceeff;
        border: 1px;
        border-color: #5c6e80;
        border-style: solid;
    }

    .imagen {
        width: 200px;
        height: 120px;
        margin: auto;
        margin-top: 10px;
    }
    .imagen2 {
        width: 100px;
        height: 80px;
        margin: auto;
        margin-top: 10px;
        margin-right: 40px;
        float: right;
    }

    .texto {
        width: 90%;
        height: 50px;
        padding-top: 0px;
        margin: auto;
        margin: 8px;
        font-size: 16px;
        font-style: normal;
    }

    .fuera {
        margin-left: 15px;
        margin-top: 20px;
        /*background-color: #317fbf; */
        background-color: rgba(114, 131, 147, 0.9);
        border: none;
    }

    .titl {
        font-family: 'open sans condensed';
        font-weight: bold;
        text-shadow: -2px 2px 1px rgba(0, 0, 0, 0.25);
        color: #0070B0;
        margin-top: 20px;
    }
    </style>
</head>

<body>
<div class="dialog">
    <div style="text-align: center;"><h2 class="titl">
            <p class="text-warning">Red Metropolitana de Monitoreo de la Calidad del Aire</p>
%{--            <p class="text-warning">Visor</p>--}%
        </h2>
    </div>

    <div class="body ui-corner-all" style="width: 680px;position: relative;margin: auto;margin-top: 20px;height: 280px; ">


        <a href= "${createLink(controller:'datos', action: 'cargarDatos')}" alt="Buscar en la Base de Conocimiento"
           style="text-decoration: none">
        <div class="ui-corner-all item fuera">
            <div class="ui-corner-all item">
                <div class="imagen" >
                    %{--<img src="${resource(dir: 'images', file: 'conocimiento.png')}" width="100%" height="100%"/>--}%
                    <asset:image src="apli/corpaire.png" width="100%" height="100%"/>
                </div>

                <div class="texto">
                    <span class="text-success"><strong>Cargar datos</strong></span></div>
            </div>
        </div>
        </a>

        <a href= "${createLink(controller:'mantenimientoItems', action: 'precios')}" style="text-decoration: none">
        <div class="ui-corner-all item fuera">
            <div class="ui-corner-all item">
                <div class="imagen">
                    <asset:image src="apli/radiación.png" alt="Buscar en la Base de Conocimiento" width="100%" height="100%"/>
%{--                    <img src="${resource(dir: 'images', file: 'agenda.png')}" width="100%" height="100%"/>--}%
                </div>

                <div class="texto">
                    <span class="text-success"><strong>Cargar datos de Radiación</strong></span></div>
            </div>
        </div>
        </a>

        <a href= "${createLink(controller:'mantenimientoItems', action: 'registro')}" alt="Buscar en la Base de Conocimiento"
           style="text-decoration: none">
            <div class="ui-corner-all item fuera">
                <div class="ui-corner-all item">
                    <div class="imagen" >
                        %{--<img src="${resource(dir: 'images', file: 'conocimiento.png')}" width="100%" height="100%"/>--}%
                        <asset:image src="apli/metereologico.png" width="100%" height="100%"/>
                    </div>

                    <div class="texto">
                        <span class="text-success"><strong>Procesar datos calculados</strong></span></div>
                </div>
            </div>
        </a>

        <a href= "${createLink(controller:'mantenimientoItems', action: 'precios')}" style="text-decoration: none">
            <div class="ui-corner-all item fuera">
                <div class="ui-corner-all item">
                    <div class="imagen">
                        <asset:image src="apli/direccion.png" alt="Buscar en la Base de Conocimiento" width="100%" height="100%"/>
                        %{--                    <img src="${resource(dir: 'images', file: 'agenda.png')}" width="100%" height="100%"/>--}%
                    </div>

                    <div class="texto">
                        <span class="text-success"><strong>Procesar datos de Dirección del viento</strong></span></div>
                </div>
            </div>
        </a>

        <a href= "${createLink(controller:'login', action: 'logout')}" style="text-decoration: none">
            <div class="ui-corner-all item2 fuera">
                <div class="ui-corner-all item2">
                    <div class="imagen2">
                        <asset:image src="apli/apagar.png" alt="Salir del sistema"  width="100%" height="100%"/>
                    </div>

                    <div style="margin-top: 50px; margin-left: 140px;">
                        <span class="text-success" style="font-size: large"><strong>Salir del sistema</strong></span></div>
                </div>
            </div>
        </a>



    </div>


</div>
    <script type="text/javascript">
        $(".fuera").hover(function () {
            var d = $(this).find(".imagen,.imagen2")
            d.width(d.width() + 10)
            d.height(d.height() + 10)

        }, function () {
            var d = $(this).find(".imagen, .imagen2")
            d.width(d.width() - 10)
            d.height(d.height() - 10)
        })


        $(function () {
            $(".openImagenDir").click(function () {
                openLoader();
            });

            $(".openImagen").click(function () {
                openLoader();
            });
        });



    </script>
</body>
</html>
