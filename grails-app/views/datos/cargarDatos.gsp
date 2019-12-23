<%--
  Created by IntelliJ IDEA.
  User: fabricio
  Date: 10/10/19
  Time: 13:12
--%>


<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <title>Cargar Datos</title>

%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js/vendor', file: 'jquery.ui.widget.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js/imgResize', file: 'load-image.min.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js/imgResize', file: 'canvas-to-blob.min.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js', file: 'jquery.iframe-transport.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js', file: 'jquery.fileupload.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js', file: 'jquery.fileupload-process.js')}"></script>--}%
%{--    <script src="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/js', file: 'jquery.fileupload-image.js')}"></script>--}%
%{--    <link href="${resource(dir: 'js/plugins/jQuery-File-Upload-9.5.6/css', file: 'jquery.fileupload.css')}"--}%
%{--          rel="stylesheet">--}%

    <asset:javascript src="/jQuery-File-Upload-9.5.6/js/vendor/jquery.ui.widget.js"/>
    <asset:javascript src="/jQuery-File-Upload-9.5.6/js/jquery.iframe-transport.js"/>
    <asset:javascript src="/jQuery-File-Upload-9.5.6/js/jquery.fileupload.js"/>
    <asset:javascript src="/jQuery-File-Upload-9.5.6/js/jquery.fileupload-process.js"/>



    <style type="text/css">

    .alinear {
        text-align: center !important;
    }

    #buscar {
        width: 400px;
        border-color: #0c6cc2;
    }
    </style>

</head>

<body>

<elm:flashMessage tipo="${flash.tipo}" clase="${flash.clase}">${flash.message}</elm:flashMessage>

<!-- botones -->
<div class="btn-toolbar toolbar">
    <div class="btn-group">
        <g:link controller="inicio" action="index" class="btn btn-info">
            <i class="fa fa-arrow-left"></i> Inicio
        </g:link>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="datosMinutos"><i class="fa fa-file-excel"></i>
            Cargar datos Minutos
        </a>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="prueba"><i class="fa fa-file-excel"></i>
            Prueba Minutos
        </a>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="calcular"><i class="fa fa-pen"></i>
            Calcular datos derivados
        </a>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="calculaDir"><i class="fa fa-pen"></i>
            Calcular dirección
        </a>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="cargaIUV"><i class="fa fa-pen"></i>
            Cargar IUV
        </a>
    </div>
    <div class="btn-group">
        <a href="#" class="btn col-md-12 btn-success" id="mueveArch"><i class="fa fa-file"></i>
            Cargar archivos
        </a>
    </div>
</div>

<div class="col-md-12" style="margin-top: 10px">
%{--
    <g:uploadForm action="leeCSV" method="post" name="frmaArchivo">
        <div class="panel panel-primary">
            <div class="panel-heading">Seleccionar el archivo a cargar</div>

            <div class="panel-body">

                <div class="panel-body col-md-2">
                    <label>Archivo (Excel xlsx)</label>
                </div>

                <span class="btn btn-info fileinput-button col-md-6" style="position: relative">
                    <input type="file" name="file" multiple="" id="archivo" class="archivo col-md-12">
                </span>

                <span>
                    <input type="hidden" name="tipo" id="tipo" value="">
                </span>

                <label for="padre" class="col-md-2 control-label">
                    Magnitud a cargar:
                </label>

                <div class="col-md-4">
                    <g:select id="magnitud" name="magnitud" from="${magnitud}" optionKey="magn__id" optionValue="nombre"
                              value="${magn__id}" class="many-to-one form-control input-sm"
                              noSelection="['null': '']"/>
                </div>

                <div class="col-md-1"></div>
                <div class="col-md-3" id="spinner">
                </div>
            </div>
        </div>
    </g:uploadForm>
--}%
</div>


<script type="text/javascript">
/*
    $("#datosMinutos").click(function () {
        if($("#archivo").val()!=""){
            var dialog = cargarLoader("Cargando...");
            $("#tipo").val("Minuto")
            $("#frmaArchivo").submit();
        }else{
            bootbox.alert("No ha ingresado ningún archivo para ser cargado")
        }
    });
*/

    $("#datosMinutos").click(function () {
        var dialog = cargarLoader("Cargando datos minutos...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'leeCSV')}',
            data:{
                tipo: 'prod'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al cargar datos de minutos","error")
                }
            }
        });
    });

    $("#prueba").click(function () {
        var dialog = cargarLoader("Cargando datos minutos...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'leeCSV')}',
            data:{
                tipo: 'prueba'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al cargar datos de minutos","error")
                }
            }
        });
    });

/*
    $("#prueba").click(function () {
        if($("#archivo").val()!=""){
            var dialog = cargarLoader("Cargando...");
            $("#tipo").val("Prueba")
            $("#frmaArchivo").submit();
        }else{
            bootbox.alert("No ha ingresado ningún archivo para ser cargado")
        }
    });
*/

    $("#calcular").click(function () {
        var dialog = cargarLoader("Procesando datos de 10m. 1h, 8h, 24h y 72h...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'calcular')}',
            data:{
                valor: 'valores'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al calcular..","error")
                }
            }
        });
    });

    $("#calculaDir").click(function () {
        var dialog = cargarLoader("Procesando datos dirección de 10m. 1h, 8h, 24h y 72h...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'calcularDir')}',
            data:{
                valor: 'valores'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al calcular dirección del viento","error")
                }
            }
        });
    });

    $("#cargaIUV").click(function () {
        var dialog = cargarLoader("Cargando datos de IUV...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'cargaIUV')}',
            data:{
                valor: 'valores'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al cargar datos de IUV","error")
                }
            }
        });
    });

    $("#mueveArch").click(function () {
        var dialog = cargarLoader("Gestionando archivos de datos...");
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'mueveArch')}',
            data:{
                valor: 'valores'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error al gestionar los archivos de datos","error")
                }
            }
        });
    });

    $("#viento").click(function () {
        $.ajax({
            type: 'POST',
            url:'${createLink(controller: 'datos', action: 'calcular')}',
            data:{
                valor: 'viento'
            },
            success: function (msg) {
                if(msg == 'ok'){
                    location.reload()
                }else{
                    log("Error")
                }
            }
        });
    });


</script>


</body>
</html>
