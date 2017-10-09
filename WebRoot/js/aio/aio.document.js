//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/**
 * JS一加载监听事件
 */
$(function (){
	$(document).click(function (event){
		var $table = $(event.target).parents("table[model='order']");
        if($table.length>0){
        	if(!$(document).data("events").keydown)bindTableEvent();
        }else{
        	$(document).unbind("keydown");
        }
       
	});
	$(document).dblclick(function (event){
		var $table = $(event.target).parents("table");
        if($table.length>0){
        	if(!$(document).data("events").keydown)bindTableEvent();
        }else{
        	$(document).unbind("keydown");
        }
		
	});
});
/**
 * 监听stable事件
 * @return
 */
function bindTableEvent(){
	$(document).keydown(function(event){
		//var $parent = navTab.getCurrentPanel();
		var $parent =getCurrentPanel();
		
		var $currentElement = $(document.activeElement);
		var $nodeName = $currentElement[0].nodeName;
		var $orderTbody = $("tbody[model='order']",$parent);//单据表格
		if($nodeName!="BODY" && $orderTbody.length==0){
			return;
		}
		if($currentElement.parents("tbody[model='order']").length==0 && $currentElement.find("tbody[model='order']").length==0){
			return;
		}
		if($orderTbody.length>0){
			var billType = $parent.find("input#billType").val();
			if(billType)$parent = $parent.find("tbody[type='"+billType+"']").parents("div.grid");
			orderKeydown($parent,event);
		}
	});
}
/**
 * 监听键盘事件
 * @param $parent
 * @param event
 * @return
 */
function orderKeydown($parent,event){
	var $orderTbody = $("tbody[model='order']",$parent);
	$orderTbody.each(function(){
		 
		var $selTd = $("td.selected",$(this));
		if($selTd.length==0){
			$selTd=$(document.activeElement).parents("td:eq(0)");
		}
		
		if($selTd.length>0){
           var $grid = $(this).parents("div.grid:first");
			var obj;
			//键盘左(37)上(38)右(39)下(40) 回车enter（13）
			if(event.keyCode == "13"){
				//var $parent = navTab.getCurrentPanel();
				var $parent =getCurrentPanel();
				if($selTd.attr("cname")=="accountsCode"){            //单据会计科目编号回车   调用双击事件
					$selTd.addClass("selected"); 
					tdDblclick(event,$selTd,$grid);     
				}else if($selTd.attr("cname")=="accountsFullName"){  //单据会计科目全名回车   调用双击事件
					$selTd.addClass("selected"); 
					tdDblclick(event,$selTd,$grid);     
				}else if($selTd.attr("cname")=="code"){              //单据商品编号回车   调用双击事件
					$selTd.addClass("selected");                     
	        		tdDblclick(event,$selTd,$grid);                  
	        	}else if($selTd.attr("cname")=="fullName"){          //单据商品全名回车   调用双击事件
	        		$selTd.addClass("selected");                     
	        		tdDblclick(event,$selTd,$grid);                  
	        	}else if($selTd.attr("cname")=="storageCode"){       //单据仓库编号回车   调用双击事件
	        		$selTd.addClass("selected");                     
	        		tdDblclick(event,$selTd,$grid);                  
	        	}else if($selTd.attr("cname")=="storageFullName"){   //单据仓库全名回车   调用双击事件
	        		$selTd.addClass("selected");
	        		tdDblclick(event,$selTd,$grid);
	        	}else{
	        		obj=aioKeyBoardReturnTdObj($(this),$selTd,"enter");
		    		billColScroll(obj);
	        	}
				
				elementEvent();
				//aioBillOnblurRemoveWidget($selTd.find("input")); 
				
				
		    }else if(event.keyCode=="37"){
		    	//elementEvent();
		    	var $currentElement = $(document.activeElement);
		    	var $nodeName = $currentElement[0].nodeName;
		    	if("INPUT"==$nodeName){return}
		    	obj=aioKeyBoardReturnTdObj($(this),$selTd,"left");
		    	//aioBillOnblurRemoveWidget($selTd.find("input")); 
		    	if(!obj)return;
		    	var $left = $(obj).offset().left;
		    	var $body = $(document.body).width();
	        	if($left<=0 || ($body-$left)<=0)billColScroll(obj);
		    }else if(event.keyCode=="38"){
		    	elementEvent();
		    	obj=aioKeyBoardReturnTdObj($(this),$selTd,"up");
		    	//aioBillOnblurRemoveWidget($selTd.find("input")); 
		    	event.preventDefault();
		    	billVerticalScroll(obj);
		    }else if(event.keyCode=="39"){
		    	//elementEvent();
		    	var $currentElement = $(document.activeElement);
		    	var $nodeName = $currentElement[0].nodeName;
		    	if("INPUT"==$nodeName){return}
		    	obj=aioKeyBoardReturnTdObj($(this),$selTd,"right");
		    	//aioBillOnblurRemoveWidget($selTd.find("input")); 
	        	billColScroll(obj);
		    }else if(event.keyCode=="40"){
		    	elementEvent();
		    	obj=aioKeyBoardReturnTdObj($(this),$selTd,"down");
		    	//aioBillOnblurRemoveWidget($selTd.find("input")); 
		    	event.preventDefault();
		    	billVerticalScroll(obj);
		    	
		    }else{
		    	
		    	var sel = false;
		    	if($($selTd).hasClass("selected")){
		    		sel = true;
		    	}
		    	if($($selTd).attr("readonly")&&$($selTd).attr("readonly")=="readonly"){
					  return;  
				}
		    	$($(this)).find("td").filter(".selected").removeClass("selected");
		    	showCurrentTdWidget($selTd,sel);
		    }
			$(obj).addClass("selected").select();
		}
	});
}
/**
 * 键盘监听处理回调
 * @return
 */
function elementEvent(){
	var $currentElement = $(document.activeElement);
	var $nodeName = $currentElement[0].nodeName;
	if("INPUT"==$nodeName){
		var ev  = $currentElement.attr("onBlur");
		$currentElement.removeAttr("onBlur");
		var arr = new Array();
		 arr = ev.split("(");
		if(arr[0]){
			var $callback = eval('(' + arr[0] + ')');
			$callback($currentElement);
		}
	}
}