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
%{--    <asset:stylesheet src="/apli/bootstrap.css"/>--}%
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/css/bootstrap.min.css">
    <asset:stylesheet src="/bootstrap-grid.css"/>
    <asset:stylesheet src="/bootstrap-reboot.css"/>
    <asset:stylesheet src="/jquery/jquery-ui-1.10.3.custom.min.css"/>
    <asset:stylesheet src="/apli/lzm.context-0.5.css"/>
    <asset:stylesheet src="/apli/jquery.pnotify.js"/>
    <asset:stylesheet src="/apli/jquery.pnotify.default.css"/>
    <asset:stylesheet src="/apli/font-awesome.min.css"/>
    <asset:stylesheet src="/apli/CustomSvt.css"/>
    <asset:stylesheet src="/apli/tulpen/stylesheet.css"/>

%{--    <asset:stylesheet src="/jquery/datepicker.css"/>--}%

    <asset:javascript src="/jquery/jquery-2.2.4.js"/>
    <asset:javascript src="/jquery/jquery-ui-1.10.3.custom.min.js"/>

    <asset:javascript src="/apli/moment.js"/>
    <asset:javascript src="/apli/moment-with-locales.js"/>

        <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
%{--    <asset:javascript src="/apli/bootstrap.js"/>--}%

    <asset:javascript src="/apli/funciones.js"/>
    <asset:javascript src="/apli/functions.js"/>
    <asset:javascript src="/apli/loader.js"/>
    <asset:javascript src="/apli/bootbox.js"/>
    <asset:javascript src="/apli/lzm.context-0.5.js"/>

    <asset:javascript src="/jquery-validation-1.11.1/js/jquery.validate.min.js"/>
    <asset:javascript src="/jquery-validation-1.11.1/js/jquery.validate.js"/>
    <asset:javascript src="/jquery-validation-1.11.1/localization/messages_es.js"/>

    <asset:javascript src="/apli/jquery.pnotify.js"/>
    <asset:javascript src="/apli/fontawesome.all.min.js"/>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datetimepicker/4.7.14/js/bootstrap-datetimepicker.min.js"></script>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datetimepicker/4.7.14/css/bootstrap-datetimepicker.min.css">


    <script type="text/javascript">
        var spinner = $('<asset:image src="apli/spinner32.gif" style="padding: 40px;"/>');
        var spinnerSquare64 = $('<asset:image src="/spinner_64.GIF" style="padding: 40px;"/>');
    </script>

    <g:layoutHead/>

</head>

<body>


<div id="modalTabelGray"></div>

<div class="container" style="min-width: 1000px !important; margin-top: 80px; overflow-y: hidden">
    <g:layoutBody/>
</div>

<asset:javascript src="jquery/application.js"/>

</body>
</html>
