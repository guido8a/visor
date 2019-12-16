<!doctype html>
<html lang="en" class="no-js">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>
        <g:layoutTitle default="Visor"/>
    </title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <asset:link rel="icon" href="favicon.ico" type="image/x-ico"/>

    <g:layoutHead/>

    <asset:stylesheet src="/apli/bootstrap.css"/>
    <asset:stylesheet src="/apli/font-awesome.min.css"/>
    <asset:stylesheet src="/jquery/jquery-ui-1.10.3.custom.min.css"/>
    <asset:stylesheet src="/apli/lzm.context-0.5.css"/>

    <asset:javascript src="/jquery/jquery-1.9.1.js"/>
    <asset:javascript src="/jquery/jquery.validate.min.js"/>
    <asset:javascript src="/jquery/jquery.validate.custom.js"/>
    <asset:javascript src="/jquery/jquery-ui-1.10.3.custom.min.js"/>
    <asset:javascript src="/jquery/messages_es.js"/>

    <asset:javascript src="/apli/fontawesome.all.min.js"/>

    <asset:javascript src="/apli/bootstrap.min.js"/>
    <asset:javascript src="/apli/funciones.js"/>
    <asset:javascript src="/apli/functions.js"/>
    <asset:javascript src="/apli/loader.js"/>
    <asset:javascript src="/apli/bootbox.js"/>
    <asset:javascript src="/apli/lzm.context-0.5.js"/>

</head>

<body>

<div id="modalTabelGray"></div>

<div class="container" style="min-width: 1000px !important;">
    <g:layoutBody/>
</div>

<div id="spinner" class="spinner" style="display:none;">
    <g:message code="spinner.alt" default="Loading&hellip;"/>
</div>


</body>
</html>
