/**
 * Created by luz on 1/15/14.
 */
/*!
 * jQuery Validation Plugin 1.11.1
 *
 * http://bassistance.de/jquery-plugins/jquery-plugin-validation/
 * http://docs.jquery.com/Plugins/Validation
 *
 * Copyright 2013 Jörn Zaefferer
 * Released under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 */
(function () {

    function stripHtml(value) {
        // remove html tags and space chars
        return value.replace(/<.[^<>]*?>/g, ' ').replace(/&nbsp;|&#160;/gi, ' ')
            // remove punctuation
            .replace(/[.(),;:!?%#$'"_+=\/\-]*/g, '');
    }

    jQuery.validator.addMethod("maxWords", function (value, element, params) {
        return this.optional(element) || stripHtml(value).match(/\b\w+\b/g).length <= params;
    }, jQuery.validator.format("Por favor ingrese {0} o menos palabras."));

    jQuery.validator.addMethod("minWords", function (value, element, params) {
        return this.optional(element) || stripHtml(value).match(/\b\w+\b/g).length >= params;
    }, jQuery.validator.format("Por favor ingrese al menos {0} palabras."));

    jQuery.validator.addMethod("rangeWords", function (value, element, params) {
        var valueStripped = stripHtml(value);
        var regex = /\b\w+\b/g;
        return this.optional(element) || valueStripped.match(regex).length >= params[0] && valueStripped.match(regex).length <= params[1];
    }, jQuery.validator.format("Por favor ingrese entre {0} y {1} palabras."));

    jQuery.validator.addMethod("notEqual", function (value, element, params) {
        return value != params;
    }, jQuery.validator.format("No ingrese ese valor."));

}());

jQuery.validator.addMethod("letterswithbasicpunc", function (value, element) {
    return this.optional(element) || /^[a-z\-.,()'"\s]+$/i.test(value);
}, "Por favor ingrese únicamente letras o signos de puntuación.");

jQuery.validator.addMethod("alphanumeric", function (value, element) {
    return this.optional(element) || /^\w+$/i.test(value);
}, "Por favor ingrese únicamente letras, números o guiones bajos");

jQuery.validator.addMethod("lettersonly", function (value, element) {
    return this.optional(element) || /^[a-z]+$/i.test(value);
}, "Por favor ingrese solo letras");

jQuery.validator.addMethod("nowhitespace", function (value, element) {
    return this.optional(element) || /^\S+$/i.test(value);
}, "Por favor no ingrese espacios");

jQuery.validator.addMethod("integer", function (value, element) {
    return this.optional(element) || /^-?\d+$/.test(value);
}, "Por favor ingrese un número entero positivo o negativo.");

/*
 7 dígitos
 */
//jQuery.validator.addMethod("telefono", function (value, element) {
//    return this.optional(element) || /^\d{7}$/.test(value);
//}, "Por favor ingrese un número de teléfono válido (7 dígitos)");

/*
 10 dígitos, empzando con un 0
 */
jQuery.validator.addMethod("celular", function (value, element) {
    return this.optional(element) || /^0\d{9}$/.test(value);
}, "Por favor ingrese un número celular válido (10 dígitos empezando con un 0)");

/**
 * Return true if the field value matches the given format RegExp
 *
 * @example jQuery.validator.methods.pattern("AR1004",element,/^AR\d{4}$/)
 * @result true
 *
 * @example jQuery.validator.methods.pattern("BR1004",element,/^AR\d{4}$/)
 * @result false
 *
 * @name jQuery.validator.methods.pattern
 * @type Boolean
 * @cat Plugins/Validate/Methods
 */
jQuery.validator.addMethod("pattern", function (value, element, param) {
    if (this.optional(element)) {
        return true;
    }
    if (typeof param === 'string') {
        param = new RegExp('^(?:' + param + ')$');
    }
    return param.test(value);
}, "Formato inválido.");

jQuery.validator.addMethod("cedula", function (value, element) {
    /**
     *      - Los 2 primeros digitos deben ser entre 01 y 24 (provincia de expedicion)
     *      - El 3r digito es un numero del 0 al 6
     *      - el 10mo es verificador
     *      - coeficientes: 2.1.2.1.2.1.2.1.2
     *
     *          1715068159
     *          1716518160
     *          1706920467
     *          1704430139
     *          1718058827
     *
     *      ejemplo:
     *               1  7  1  5  0  6  8  1  5  9
     *               2  1  2  1  2  1  2  1  2
     *               --------------------------------
     *               2  7  2  5  0  6  16 1  10         si la multiplicacion es >=10, se resta 9
     *               2  7  2  5  0  6  7  1  1   (+) = 31   a la suma se le ressta de la decena superior
     *                                                      40-31 = 9 <-- ultimo digito
     */

    var cant = value.length
    if (cant == 10) {
        var provincia = value.substr(0, 2);
        if (parseInt(provincia) >= 1 && parseInt(provincia) <= 24) {
            var tercer = value.substr(2, 1);
            if (tercer >= 0 && tercer <= 6) {
                var coeficientes = [2, 1, 2, 1, 2, 1, 2, 1, 2];
                var verificador = value.substr(9, 1);
                var suma = 0;
                for (var i = 0; i < 9; i++) { //para los 9 digitos de la cedula (sin el verificador)
                    var digito = parseInt(value[i]);
                    var coef = coeficientes[i];
                    var mult = digito * coef;
                    if (mult >= 10) {
                        mult -= 9;
                    }
                    suma += mult;
                }
                var decena = (10 - suma % 10) + suma;
                var ver = decena - suma;
                if (ver == 10) {
                    ver = 0;
                }
                if (ver == parseInt(verificador)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    } else {
        if (cant == 0) {
            return true;
        }
    }
    return false;

}, jQuery.format("Por favor ingrese un número de cédula válido."));