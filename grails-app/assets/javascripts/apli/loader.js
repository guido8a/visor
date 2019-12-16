/**
 * Created by svt on 1/16/14.
 */

jQuery.fn.center = function () {
    this.css("position","absolute");
    this.css("top", Math.max(0, (($(window).height() - $(this).outerHeight()) / 2) +
        $(window).scrollTop()) + "px");
    this.css("left", Math.max(0, (($(window).width() - $(this).outerWidth()) / 2) +
        $(window).scrollLeft()) + "px");
    return this;
}

function openLoader(){
    $('#modalTabelGray').css('height', $(document).height()).css('width', $(document).width()).fadeIn();
    $("#modalDiv").show().center();

}
function openLoader(msg){
    $('#modalTabelGray').css('height', $(document).height()).css('width', $(document).width()).fadeIn();
    $(".loading-title").html(msg);
    $("#modalDiv").show().center();

}
function closeLoader(){
    $('#modalTabelGray').hide(1000);
    $("#modalDiv").hide();
}
