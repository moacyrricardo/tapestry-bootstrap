(function ($) {
    /** Container of functions that may be invoked by the Tapestry.init() function. */
    $.extend(Tapestry.Initializer, {
        jqDroppable: function (specs) {
            var d = $(specs.selector);
            d.droppable(specs.params);
            d.data("drop-context", specs.context);
            d.bind("drop", function (event, ui) {
                var thisCopy = this;
                var draggable = $(ui.draggable);
                var dropCtx = $(event.currentTarget).data("drop-context");
                var dragCtx = draggable.data("drag-context");
                var url = specs.BaseURL + dropCtx + dragCtx;
                if (specs.zoneSelector) {
                    var element = $(specs.zoneSelector);
                    element.tapestryZone("update", {url: url});
                } else {
                    $.get(url)
                        .success(
                        function (data) {
                            if (data.redirectURL) {
                                // Check for complete URL.
                                if (/^https?:/.test(data.redirectURL)) {
                                    window.location = redirectURL;
                                    return;
                                }
                                window.location.pathname = data.redirectURL;
                            } else {
                                if (data.ok) {
                                    draggable.appendTo(thisCopy);
                                } else {
                                    // TODO KK handle errors ?
                                    // data.errors === undefined || data.errors.length === undefined || data.errors.length == 0
                                }
                            }
                        }
                    );
                }
            });
        }
    });
})(jQuery);