<%--
  Created by IntelliJ IDEA.
  User: guido
  Date: 27/06/14
  Time: 09:04 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <title>Final de Sesión</title>
</head>

<body>
<h1>Su Sesión ha finalizado</h1>
<p class="alert alert-dismissable alert-warning">EL sistema finaliza la sesión del usuario una vez transcurridos 5 minutos Vuelva a ingresar al sistema para que pueda seguir trabajando</p>


<div class="btn-toolbar toolbar">
    <div class="btn-group">
        <g:link controller="login" action="login" class="btn btn-primary btn-lg">
            <i class="fa fa-file-o"></i> Ingresar al Sistema
        </g:link>
    </div>
</div>


</body>
</html>