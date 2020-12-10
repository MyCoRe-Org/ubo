(function($){
    $.widget("ui.combobox", $.ui.autocomplete, {
        options: {
            /* override default values here */
            minLength: 2,
            /* the argument to pass to ajax to get the complete list */
            ajaxGetAll: {
                get: "all"
            }
        },
        
        _create: function(){
            if (this.element.is("SELECT")) {
                this._selectInit();
                return;
            }
            
            $.ui.autocomplete.prototype._create.call(this);
            var input = this.element;
            input.addClass("ui-widget ui-widget-content ui-corner-left");
            
            this.button = $("<button type='button'>&nbsp;</button>").attr("tabIndex", -1).attr("title", "Show All Items").insertAfter(input).button({
                icons: {
                    primary: "ui-icon-triangle-1-s"
                },
                text: false
            }).removeClass("ui-corner-all").addClass("ui-corner-right ui-button-icon").click(function(event){
                // close if already visible
                if (input.combobox("widget").is(":visible")) {
                    input.combobox("close");
                    return;
                }
                // when user clicks the show all button, we display the cached full menu
                var data = input.data("combobox");
                clearTimeout(data.closing);
                if (!input.isFullMenu) {
                    data._swapMenu();
                    input.isFullMenu = true;
                }
                /* input/select that are initially hidden (display=none, i.e. second level menus), 
                 will not have position cordinates until they are visible. */
                input.combobox("widget").css("display", "block").position($.extend({
                    of: input
                }, data.options.position));
                input.focus();
                data._trigger("open");
            });
            
            /* to better handle large lists, put in a queue and process sequentially */
            $(document).queue(function(){
                var data = input.data("combobox");
                if ($.isArray(data.options.source)) {
                    $.ui.combobox.prototype._renderFullMenu.call(data, data.options.source);
                }
                else 
                    if (typeof data.options.source === "string") {
                        $.getJSON(data.options.source, data.options.ajaxGetAll, function(source){
                            $.ui.combobox.prototype._renderFullMenu.call(data, source);
                        });
                    }
                    else {
                        $.ui.combobox.prototype._renderFullMenu.call(data, data.source());
                    }
            });
        },
        
        /* initialize the full list of items, this menu will be reused whenever the user clicks the show all button */
        _renderFullMenu: function(source){
            var self = this, input = this.element, ul = input.data("combobox").menu.element, lis = [];
            source = this._normalize(source);
            input.data("combobox").menuAll = input.data("combobox").menu.element.clone(true).appendTo("body");
            for (var i = 0; i < source.length; i++) {
                lis[i] = "<li class=\"ui-menu-item\" role=\"menuitem\"><a class=\"ui-corner-all\" tabindex=\"-1\">" + source[i].label + "</a></li>";
            }
            ul.append(lis.join(""));
            this._resizeMenu();
            // setup the rest of the data, and event stuff
            setTimeout(function(){
                self._setupMenuItem.call(self, ul.children("li"), source);
            }, 0);
            input.isFullMenu = true;
        },
        
        /* incrementally setup the menu items, so the browser can remains responsive when processing thousands of items */
        _setupMenuItem: function(items, source){
            var self = this, itemsChunk = items.splice(0, 500), sourceChunk = source.splice(0, 500);
            for (var i = 0; i < itemsChunk.length; i++) {
                $(itemsChunk[i]).data("item.autocomplete", sourceChunk[i]).mouseenter(function(event){
                    self.menu.activate(event, $(this));
                }).mouseleave(function(){
                    self.menu.deactivate();
                });
            }
            if (items.length > 0) {
                setTimeout(function(){
                    self._setupMenuItem.call(self, items, source);
                }, 0);
            }
            else { // renderFullMenu for the next combobox.
                $(document).dequeue();
            }
        },
        
        /* overwrite. to cleanup additional stuff that was added */
        destroy: function(){
            if (this.element.is("SELECT")) {
                this.input.remove();
                this.element.removeData().show();
                return;
            }
            // super()
            $.ui.autocomplete.prototype.destroy.call(this);
            // clean up new stuff
            this.element.removeClass("ui-widget ui-widget-content ui-corner-left");
            this.button.remove();
        },
        
        /* overwrite. to swap out and preserve the full menu */
        search: function(value, event){
            var input = this.element;
            if (input.isFullMenu) {
                this._swapMenu();
                input.isFullMenu = false;
            }
            // super()
            $.ui.autocomplete.prototype.search.call(this, value, event);
        },
        
        _change: function(event){
            abc = this;
            if (!this.selectedItem) {
                var matcher = new RegExp("^" + $.ui.autocomplete.escapeRegex(this.element.val()) + "$", "i"), match = $.grep(this.options.source, function(value){
                    return matcher.test(value.label);
                });
                if (match.length) {
                    match[0].option.selected = true;
                }
                else {
                    // remove invalid value, as it didn't match anything
                    this.element.val("");
                    if (this.options.selectElement) {
                        this.options.selectElement.val("");
                    }
                }
            }
            // super()
            $.ui.autocomplete.prototype._change.call(this, event);
        },
        
        _swapMenu: function(){
            var input = this.element, data = input.data("combobox"), tmp = data.menuAll;
            data.menuAll = data.menu.element.hide();
            data.menu.element = tmp;
        },
        
        /* build the source array from the options of the select element */
        _selectInit: function(){
            var select = this.element.hide(), selected = select.children(":selected"), value = selected.val() ? selected.text() : "";
            this.options.source = select.children("option[value!='']").map(function(){
                return {
                    label: this.text,
                    option: this
                };
            }).toArray();
            var userSelectCallback = this.options.select;
            var userSelectedCallback = this.options.selected;
            this.options.select = function(event, ui){
                ui.item.option.selected = true;
                if (userSelectCallback) 
                    userSelectCallback(event, ui);
                // compatibility with jQuery UI's combobox.
                if (userSelectedCallback) 
                    userSelectedCallback(event, ui);
            };
            this.options.selectElement = select;
            this.input = $("<input>").insertAfter(select).val(value).combobox(this.options);
        }
    });
})(jQuery);
