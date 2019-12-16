/*
 El del tutorial
 http://stackoverflow.com/questions/18666601/use-bootstrap-3-dropdown-menu-as-context-menu
 http://jsfiddle.net/KyleMit/X9tgY/

 mezclado con el tutorial de los ratings
 http://www.switchonthecode.com/tutorials/how-to-build-a-star-ratings-jquery-plugin

 el tutorial de plugin authoring
 https://github.com/jquery-boilerplate/jquery-boilerplate/wiki/How-did-we-get-here%3F

 la parte de context menu del jstree
 http://www.jstree.com/plugins/

 snippet submenu
 http://www.bootply.com/86684 (js + css stackoverflow user Skelly)
 (para navbar)
 http://www.bootply.com/92442

 */
;
(function ($, window) {
    $.fn.contextMenu = function (settings) {

        //default settings
        var defaults = {
            /**
             * a boolean indicating if the menu should be shown aligned with the element.
             * Defaults to 'false', otherwise the menu is show on the top-left corner of the element.
             */
            show_at_element : false,
            /**
             * an object of actions, or a function that accepts a node and returns the items.
             *
             * Each action consists of a key (a unique name) and a value which is an object with the
             * following properties (only label and action are required):
             *
             * * 'separator_before' - a boolean indicating if there should be a separator before this item
             * * 'separator_after' - a boolean indicating if there should be a separator after this item
             * * 'disabled' - a boolean indicating if this action should be disabled, or a function that accepts a node and returns a boolean.
             * * 'label' - a string - the name of the action (could be a function returning a string)
             * * 'action' - a function to be executed if this item is chosen ($element: the element that was right clicked,
             *                                                                $item:    the menu item clicked (li)
             * * 'icon' - a string, can be a path to an icon or a className,
             * if using an image that is in the current directory use a './' prefix, otherwise it will be detected as a class
             */
            items           : function (o, cb) { // Could be an object directly
                return {
                    header : {
                        header : true,
                        label  : "Nice header",
                        icon   : "glyphicon glyphicon-star-empty"
                    },
                    create : {
                        separator_before : false,
                        separator_after  : true,
                        disabled         : false,
                        label            : "Create",
                        action           : function ($element, $item) {
                            alert("You clicked the CREATE button on element with id " + $element.attr("id"));
                        }
                    },
                    remove : {
                        separator_before : false,
                        icon             : false,
                        separator_after  : false,
                        disabled         : false,
                        label            : "Delete",
                        action           : function ($element, $item) {
                            alert("You clicked the DELETE button on element with id " + $element.attr("id"));
                        }
                    },
                    links  : {
                        separator_before : true,
                        icon             : "./webapp/images/internet.png",
                        separator_after  : false,
                        label            : "Links",
                        action           : false,
                        submenu          : {
                            google   : {
                                separator_before : false,
                                separator_after  : false,
                                label            : "Google",
                                url              : "http://www.google.com",
                                target           : "_blank"
                            },
                            facebook : {
                                separator_before : false,
                                icon             : false,
                                separator_after  : false,
                                disabled         : true,
                                label            : "Facebook",
                                url              : "http://www.facebook.com"
                            }
                        }
                    }
                };
            } // default items
        };

        settings = extendRemove(defaults, settings || {});

        //Go through each object in the selector and create a ratings control.
        return this.each(function () {
            var element = this, $element = $(element);
//            console.log($element);
            // Open context menu
            $element.on("contextmenu", function (e) {
                if ($.isFunction(settings.onHide)) {
                    settings.onHide.call(this, $element);
                }
                $(".lzm-dropdown-menu").remove();
                e.preventDefault();
                e.stopPropagation();
                var cont = true;
                if ($.isFunction(settings.onShow)) {
                    cont = settings.onShow.call(this, $element);
                }
                if (cont || cont === undefined) {
                    showContextMenu(e.currentTarget, e.pageX, e.pageY, e);

                    if ($.isFunction(settings.afterShow)) {
                        settings.afterShow.call(this, $element);
                    }
                }
                return false;
            }); // Open context menu
            //make sure menu closes on any click
            $(document).on("click", function (e) {
                if ((e.button != 2 && e.buttons != 2)) {
                    if ($.isFunction(settings.onHide)) {
                        settings.onHide.call(this, $element);
                    }
                    $(".lzm-dropdown-menu").remove();
                }
            });
        });

        function createContextMenu(items, $element, submenu) {
            var $menu = $("<ul class='lzm-dropdown-menu dropdown-menu'>");
            if(submenu) {
                $menu.addClass("lzm-dropdown-submenu-content");
            }
            $.each(items, function (i, obj) {

                var disabled = obj.disabled;
                if ($.isFunction(disabled)) {
                    disabled = disabled.call(this, $element);
                }

                if (obj.separator_before) {
                    $menu.append("<li class='divider'></li>");
                }
                var clase = "";
                if (obj.header) {
                    clase = "dropdown-header lzm-dropdown-header";
                }
                if (disabled) {
                    clase = "disabled";
                }
                if (obj.submenu) {
                    clase = "lzm-dropdown-submenu";
                }

                if (clase != "") {
                    clase = "class='" + clase + "'";
                }

                var $li = $("<li " + clase + ">");

                if (obj.icon) {
                    var $i = $("<i> ");
                    if (obj.icon.indexOf("/") !== -1 || obj.icon.indexOf(".") !== -1) {
                        $i.css({
                            background : "url('" + obj.icon + "') center center no-repeat"
                        }).append("&nbsp;&nbsp;&nbsp;&nbsp;");
                    } else {
                        $i.addClass(obj.icon);
                    }
                }

                if (!obj.header) {
                    var $a = $("<a tabindex='-1' href='#'>");
                    if ($i) {
                        $a.append($i).append("&nbsp;");
                    }
                    $a.append(obj.label);
                    if (obj.url && !disabled) {
                        $a.attr("href", obj.url);
                        if (obj.target) {
                            $a.attr("target", obj.target);
                        }
                    }
                    if (obj.action && !disabled) {
                        $a.click(function () {
                            if ($.isFunction(obj.action)) {
                                obj.action.call(this, $element, $li);
                            }
                        });
                    }
                    $li.append($a);
                } else {
                    if ($i) {
                        $li.append($i).append("&nbsp;");
                    }
                    $li.append(obj.label);
                }

                if (obj.submenu) {
                    $li.append(createContextMenu(obj.submenu, $element, true));
                }
                $menu.append($li);

                if (obj.separator_after) {
                    $menu.append("<li class='divider'></li>");
                }
            });
            return $menu;
        }

        function showContextMenu(element, x, y, e) {
            var $element = $(element);
            if (!$element) {
                return false;
            }
            var offset, items = settings.items;
            if (settings.show_at_element || x === undefined || y === undefined) {
                offset = $element.offset();
                x = offset.left;
                y = offset.top;
            }
            if ($.isFunction(items)) {
                items = items.call(this, element);
            }
            var $menu = "";
            if ($.isPlainObject(items)) {
                $menu = createContextMenu(items, $element, false);
            }
            if ($menu) {
                $menu.appendTo("body")
                    .data("invokedOn", $element)
                    .css({
                        display  : "block",
                        position : "absolute",
                        left     : getLeftLocation(x) - 20,
                        top      : getTopLocation(y)
                    });
            }
        }

        function extendRemove(target, props) {
            $.extend(target, props);
            for (var name in props) {
                if (props[name] == null || props[name] == undefined) {
                    target[name] = props[name]
                }
            }
            return target
        } //extendRemove

        function getLeftLocation(x) {
            var mouseWidth = x;
            var pageWidth = $(window).width();
            var menuWidth = $(settings.menuSelector).width();

            // opening menu would pass the side of the page
            if (mouseWidth + menuWidth > pageWidth &&
                menuWidth < mouseWidth) {
                return mouseWidth - menuWidth;
            }
            return mouseWidth;
        } //getLeftLocation

        function getTopLocation(y) {
            var mouseHeight = y;
            var pageHeight = $(window).height();
            var menuHeight = $(settings.menuSelector).height();

            // opening menu would pass the bottom of the page
            if (mouseHeight + menuHeight > pageHeight &&
                menuHeight < mouseHeight) {
                return mouseHeight - menuHeight;
            }
            return mouseHeight;
        } //getTopLocation

    };
})(jQuery, window);