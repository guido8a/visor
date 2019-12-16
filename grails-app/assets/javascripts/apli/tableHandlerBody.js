/**
 * Created with IntelliJ IDEA.
 * User: luz
 * Date: 11/21/12
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */

var tabla = $('#tablaPrecio');

function doHighlight(params) {
    if (!params.tiempo) {
        params.tiempo = 2000;
    }
    params.elem.addClass(params.clase, params.tiempo, function () {
        params.elem.removeClass(params.clase, params.tiempo);
    });
}

function scroll() {
    var container = $('#divTabla'), scrollTo = $('.selected');
    var newPos = scrollTo.offset().top - container.offset().top + container.scrollTop() - 100;
//    container.animate({
//        scrollTop : newPos
//    }, 200);
    container.scrollTop(newPos);
}

$(document).keyup(function (ev) {
    var sel = $(".selected");
    var celdaIndex = sel.index();
    var tr = sel.parent();
    var filaIndex = tr.index();
    var ntr;
    switch (ev.keyCode) {
        case 38: //arriba
            var target = $(ev.target);
            if (!target.hasClass("editando")) {
                if (filaIndex > 0) {
                    ev.stopPropagation();
                    ntr = tr.prev();
//                console.log(sel, celdaIndex, tr, filaIndex, ntr);
                    seleccionar(ntr.children().eq(celdaIndex));
                    scroll();
                }
            }
            break;
        case 40: //abajo
            var target = $(ev.target);
            if (!target.hasClass("editando")) {
//                var cant = $('#tablaPrecios > tbody > tr').size();
                var cant = tabla.children("tbody").children("tr").size();
                if (filaIndex < cant - 1) {
                    ev.stopPropagation();
                    ntr = tr.next();
//                    console.log(sel, celdaIndex, tr, filaIndex, ntr, ntr.children().eq(celdaIndex));
                    if (ntr.length > 0 && ntr.children().eq(celdaIndex).length > 0) {
                        seleccionar(ntr.children().eq(celdaIndex));
                        scroll();
                    }
                }
            }
            break;
        case 13: //enter
            var target = $(ev.target);
            if (target.hasClass("editando")) {
//                stopEdit();
                var value = target.val();
                $(".selected").html(number_format(value, decimales, ".", ""));
                sel.data("valor", value);
                afterStopEdit();
            } else {
                doEdit(sel);
            }
            break;
        case 27: //esc
            stopEdit();
            break;

        case 37: //izq
            var target = $(ev.target);
            if (!target.hasClass("editando")) {
                var prev = sel.prev();
                if (prev.hasClass("editable")) {
                    seleccionar(prev);
                }
            }
            break;
        case 39: //der
            var target = $(ev.target);
            if (!target.hasClass("editando")) {
                var next = sel.next();
                if (next.hasClass("editable")) {
                    seleccionar(next);
                }
            }
            break;
        default:
            return true;
    }
});