(function($){
    /** Container of functions that may be invoked by the Tapestry.init() function. */
    $.extend(Tapestry.Initializer, {
    	jqDraggable: function(specs){
            var e = $(specs.selector);
            e.draggable(specs.params);
            e.data("drag-context", specs.context);
        }
    });
})(jQuery);