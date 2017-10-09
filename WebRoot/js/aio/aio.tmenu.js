(function($){
	$.fn.extend({
		tMenu: function(){
			return this.each(function(){
				var $this = $(this);
				$this.addClass('tmenu');
				$('a',$this).click(function(event){
					var $ul = $(this).siblings();
					if($ul.length>0){
						var top = $(this).offset().top;
						var left = $(this).offset().left;
						if(top + $ul.height() > $(window).height() - 20) {
							top = - $ul.height()-2;
						}else{
							top = $(this).height();
						}
						if(left + $ul.width() > $(window).width()) {
							left = - ($ul.width() + $(this).width())/2;
						}else{
							left = - (($ul.width()-$(this).width())/2);
						}
						$ul.css({left:left,top:top}).show();
						
						$(document).click(function(event){
							$('ul',$this).hide();
						});
						event.stopImmediatePropagation();
					}
				});
			});
		}
	});
})(jQuery);