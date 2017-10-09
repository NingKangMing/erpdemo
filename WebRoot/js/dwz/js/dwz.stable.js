/**
 * @author Roger Wu v1.0
 * @author ZhangHuihua@msn.com 2011-4-1
 */


(function($){
	$.fn.jTable = function(options){
		return this.each(function(){
		 	var $table = $(this),totalWidth=0,aStyles = [];
			var $tc = $table.parent().addClass("j-resizeGrid"); // table parent container
			var tcWidth = $tc.width();
		 	
		 	//表格及窗体模式
			var  model = $table.attr("model");  //表格模式
			var pattern = $table.attr("pattern");//窗口模式
		 	var $parent = navTab.getCurrentPanel();
		 	if(pattern&&pattern=="dialog")$parent = getCurrentPanel();  //如果表格的显示为Dialog形式
            var modelStr="",patternStr="";
            if(model)modelStr = "model='"+model+"'";
            if(pattern)patternStr="pattern='"+pattern+"'";
            
            
            //-----------------------------------thead-------------------------------------  
			$table.wrap("<div class='grid'></div>");
			var $grid = $table.parent().html($table.html());
			var thead = $grid.find("thead");
			
			//(单个宽度，样式，总宽度)
			var lastH = $(">tr:last-child", thead);
            var ths = lastH.find("th");
			for(var i = 0, l = ths.size(); i < l; i++) {
				var $th = $(ths[i]);
				var style = [], width = 100;
				var attrWidth = parseInt($th.attr("width"));
				if(attrWidth>0)width = attrWidth;
				style[0] = width;
				style[1] = $th.attr("align") || "center";
				aStyles[aStyles.length] = style;
				totalWidth += width;
			}
			thead.wrap("<div class='gridHeader'><div class='gridThead'><table "+patternStr+" "+modelStr+" style='width:" + (totalWidth-20)+ "px;'></table></div></div>");
			
			//如果有表头,处理表头宽度（如进货单位分布）
			var thAllIndex = lastH.index();
			if(thAllIndex!=0){
				var colgroup = '<colgroup>';
				for(var i=0;i<ths.size();i++){
					colgroup += '<col width="'+aStyles[i][0]+'">';
				}
				colgroup +='</colgroup>';
				thead.before(colgroup);
				var firstH = $(">tr:first-child", thead);
				var colspanStep = 0;
				$(">th",firstH).each(function(i){
					var $th = $(this);
					$th.html("<div class='gridCol' title='"+$th.text()+"'>"+ $th.html() +"</div>");	
					var colspan = $th.attr("colspan");
					var cols = colspan ? parseInt(colspan) : 0;
					var fwidth = 0;
					for(var j=0;j<cols;j++){
						fwidth += aStyles[colspanStep+j][0];
					}
					$th.addClass(aStyles[colspanStep][1]).hoverClass("hover").removeAttr("align").removeAttr("width").width(fwidth);
					colspanStep += cols;
				});
			}
			
			//表头最后一行 （排序）
			ths.each(function(i){
				var $th = $(this), style = aStyles[i];
				$th.html("<div class='gridCol' title='"+$th.text()+"'>"+ $th.html() +"</div>");	
				$th.addClass(style[1]).hoverClass("hover").removeAttr("align").removeAttr("width").width(style[0]);
			}).filter("[orderField]").orderBy({
				targetType: $table.attr("targetType"),
				rel:$table.attr("rel"),
				asc: $table.attr("asc") || "asc",
				desc:  $table.attr("desc") || "desc"
			});
			//------------------------------end----thead---------------------------------------------------
			
			
			//----------------------------------tfoot---------------------------------------------------
			var tfoot = $grid.find(">tfoot");
            tfoot.wrap("<div class='gridScroller'" + " style='width:" + (tcWidth-2) + "px;height:100%;'><div class='gridTbody'><table "+modelStr+" style='width:" + (totalWidth-20) + "px;'></table></div></div>");
            //为后面准备参数
            var ttr = $(">tr:first-child", tfoot);
            var cla = "gridScroller";
            var sty= "";
            if(tfoot && tfoot.length>0){
            	cla = "gridTbody2";
            	sty="overflow-x:hidden;";
            }
            //------------------------------end----tfoot---------------------------------------------------
            
            
            //----------------------------------tbody---------------------------------------------------
            var tbody = $grid.find(">tbody");
            var layoutH = $table.attr("layoutH");
			var layoutStr = layoutH ? " layoutH='" + layoutH + "'" : "";
			if(model)tbody.attr("model",model);
			if(pattern)tbody.attr("pattern",pattern);
			tbody.wrap("<div class='"+cla+"'" + layoutStr + " style='width:" + (tcWidth-2) + "px;"+sty+"'><div class='gridTbody'><table  "+patternStr+" "+modelStr+" style='width:" + (totalWidth-20) + "px;'></table></div></div>");
			var ftr = $(">tr:first-child", tbody);
			//-------------------------------end---tbody---------------------------------------------------
			
			
			//----------------------------------tr td处理---------------------------------------------------
			//合并tbody tfoot的tr
			var $trs = tbody.find('>tr');
			var $tftrs = tfoot.find('>tr');
			$trs = $.merge($trs,$tftrs);
			
			var selectFlag=true;//eg:不同批次的同ID商品只选中一次
			//行初始化 
			$trs.each(function(i){
				var selectId = $parent.find("#selectedObjectId").val();//初始化需要选中的ID
				var $tr = $(this);    //行
				var $tds=$tr.find("td");  //单元格集合
				var isSelect=$tr.attr("isSelect");
				if($table.attr("model")==undefined){  //非单据
					//选中操作行
					if(selectId && selectId!=0 && isSelect=="true" && selectFlag){
						if($tr.attr("rel")==selectId)$tr.addClass("selected");
						$parent.find("#selectedObjectId").val(selectId);
					}else if(i==0 && isSelect!="false"){  //默认第一行选中
						$tr.addClass("selected");
						if(isSelect){//设置值
							var selectId=$tr.attr("rel");
							$parent.find("#selectedObjectId").val(selectId);
							selectFlag=false;
						}
						var sTarget = $tr.attr("target");
						if (sTarget) {
							if ($("#"+sTarget, $grid).size() == 0) {
								$grid.prepend('<input id="'+sTarget+'" name="'+sTarget+'" type="hidden" />');
							}
							$("#"+sTarget, $grid).val($tr.attr("rel"));
						}
						//设置当前行的状态,用于是否执行后面的操作
						var status = $tr.attr("status");
						if(status){
							if ($("#status", $grid).size() == 0) {
								$grid.prepend('<input id="status" type="hidden" />');
							}
							if(status==2){
								$("#status", $grid).val(true);
							}else{
								$("#status", $grid).val(false);
							}
						}
						//end
						//单据红冲状态
						var isRCW = $tr.attr("isRCW");
						if(isRCW){
							if ($("#isRCW", $grid).size() == 0) {
								$grid.prepend('<input id="isRCW" type="hidden" />');
							}
							$("#isRCW", $grid).val(isRCW);
						}
						//end
					}
				}else if($table.attr("model")=="order"){    //单据      
					//选中第一个非行号单元格
					if(i==0){
						$trs.filter(".selected").removeClass("selected");
						$($tds.get(1)).addClass("selected");
					}
				}
				//根据ID数组选中checkBox   
				if($tr.attr("selectCheckboxObjs")){
					var selectObjArr= $tr.attr("selectCheckboxObjs").split(",");   //2,5,4,5
					$.each(selectObjArr,function(key,val){
						if(val==$tr.attr("rel")){ 
							var checkBoxObj=$tr.find("input[type='checkbox']:eq(0)");
							$(checkBoxObj).attr("checked","checked");
						}	
					});
				}
				//end 根据ID数组选中checkBox   
				if($tr.attr("objectId")&&$tr.attr("objectId")==$tr.attr("rel")){  //根据ID行选中
					$trs.filter(".selected").removeClass("selected");
					$tr.addClass("selected");
					var sTarget = $tr.attr("target");
					if (sTarget) {
						if ($("#"+sTarget, $grid).size() == 0) {
							$grid.prepend('<input id="'+sTarget+'" name="'+sTarget+'" type="hidden" />');
						}
						$("#"+sTarget, $grid).val($tr.attr("rel"));
					}
				}
			});
			//end 初始化选中
			
			//给tr 绑定一些事件
			trsBindEvent($trs, aStyles,model,pattern,$grid,tbody,$parent);
			//------------------------------end----tr td处理---------------------------------------------------
			
			
			//---------------------------------重置主体---------------------------------------------------
			if(thAllIndex!=0){
				$(tbody).parent().width($(thead).parent().width());
				$(">td",ftr).each(function(i){
					var width = $(">tr:last-child>th", thead).eq(i).width();
					$(this).width(width);
				});
				$(">td",ttr).each(function(i){
					var width = $(">tr:last-child>th", thead).eq(i).width();
					$(this).width(width);
				});
			}
			$grid.append("<div class='resizeMarker' style='height:300px; left:57px;display:none;'></div><div class='resizeProxy' style='height:300px; left:377px;display:none;'></div>");
			//-------------------------------end--重置主体---------------------------------------------------
			
			
			//---------------------------------水平滚动---------------------------------------------------
			var scroller = $(".gridScroller", $grid);
			scroller.scroll(function(event){
				var header = $(".gridThead", $grid);
				
				var tbody = $(".gridTbody2", $grid).find(".gridTbody");
				if(scroller.scrollLeft() > 0){
					header.css("position", "relative");
					var scroll = scroller.scrollLeft();
					header.css("left", scroller.cssv("left") - scroll);
					//tbody.scrollLeft(scroller.scrollLeft());
					tbody.css("position", "relative");
     				tbody.css("left", scroller.cssv("left") - scroll);
				}
				if(scroller.scrollLeft() == 0) {
					header.css("position", "relative");
					header.css("left", "0px");
					//tbody.scrollLeft(scroller.scrollLeft());
					tbody.css("position", "relative");
					tbody.css("left", "0px");
				}
		        return false;
			});	
			//----------------------------end-----水平滚动---------------------------------------------------
			
			
			//------------------------------------表头拖拽--------------------------------------
			theadDrag(thAllIndex,$grid,thead,tbody,tfoot,ftr,ttr,options);
			//---------------------------------end---表头拖拽--------------------------------------
			
			
			//--------------------------------浏览器大小改变 table宽度变化-----------------------------------------
			function _resizeGrid(){
				$("div.j-resizeGrid").each(function(){
					var width = $(this).width();
					if (width){
						$("div.gridScroller", this).width(width+"px");
						if($("div.gridTbody2", this)){
							$("div.gridTbody2", this).width(width+"px");
						}
						
					}
				});
			}
			$(window).unbind(DWZ.eventType.resizeGrid).bind("resizeGrid", _resizeGrid);
			//-----------------------------end---浏览器大小改变 table宽度变化-----------------------------------------
			
			
			//------------------------------------合计------------------------------------------------
			tfootCount(tbody,tfoot);       //报表
			tfootInputCount(tbody,tfoot);  //单据草稿input合计
			//--------------------------------end----合计------------------------------------------------
		});
	};
   
	
	$.jTableTool = {
		getLeft:function(obj) {
			var width = 0;
			$(obj).prevAll().each(function(){
				width += $(this).outerWidth();
			});
			return width - 1;
		},
		getRight:function(obj) {
			var width = 0;
			$(obj).prevAll().andSelf().each(function(){
				width += $(this).outerWidth();
			});
			return width - 1;
		},
		getTop:function(obj) {
			var height = 0;
			$(obj).parent().prevAll().each(function(){
				height += $(this).outerHeight();
			});
			return height;
		},
		getHeight:function(obj, parent) {
			var height = 0;
			var head = $(obj).parent();
			head.nextAll().andSelf().each(function(){
				height += $(this).outerHeight();
			});
			$(".gridTbody", parent).children().each(function(){
				height += $(this).outerHeight();
			});
			return height;
		},
		getCellNum:function(obj) {
			return $(obj).prevAll().andSelf().size();
		},
		getColspan:function(obj) {
			return $(obj).attr("colspan") || 1;
		},
		getStart:function(obj) {
			var start = 1;
			$(obj).prevAll().each(function(){
				start += parseInt($(this).attr("colspan") || 1);
			});
			return start;
		},
		getHeadColIndex:function(index,headRow){
			var thIndex=0,hhcol;
			$('th',headRow).each(function(i){
				thIndex += parseInt($(this).attr("colspan") || 1);
				if(index<thIndex){
					hhcol = $(this);
					return false;
				}
			});
			return hhcol;
		},
		getPageCoord:function(element){
			var coord = {x: 0, y: 0};
			while (element){
			    coord.x += element.offsetLeft;
			    coord.y += element.offsetTop;
			    element = element.offsetParent;
			}
			return coord;
		},
		getOffset:function(obj, evt){
			if($.browser.msie ) {
				var objset = $(obj).offset();
				var evtset = {
					offsetX:evt.pageX || evt.screenX,
					offsetY:evt.pageY || evt.screenY
				};
				var offset ={
			    	offsetX: evtset.offsetX - objset.left,
			    	offsetY: evtset.offsetY - objset.top
				};
				return offset;
			}
			var target = evt.target;
			if (target.offsetLeft == undefined){
			    target = target.parentNode;
			}
			var pageCoord = $.jTableTool.getPageCoord(target);
			var eventCoord ={
			    x: window.pageXOffset + evt.clientX,
			    y: window.pageYOffset + evt.clientY
			};
			var offset ={
			    offsetX: eventCoord.x - pageCoord.x,
			    offsetY: eventCoord.y - pageCoord.y
			};
			return offset;
		}
	};
})(jQuery);


//tfoot  合计
function tfootCount($tbody,$tfoot){
	var $trs=$("tr",$tbody);
	var tfootTds=$("td",$tfoot);
	$(tfootTds).each(function(i,tfootTd){
		var hasCount=$(tfootTd).attr("hascount");
        if(!hasCount||hasCount!="true")return true;
        var count=0;
        $($trs).each(function(j,tbodyTr){
        	var td=$(tbodyTr).find(">td:eq("+i+")");
			var val=$(tbodyTr).find(">td:eq("+i+")").children("div").text();
//			if(val=="***"){
//				count="***"; 
//				return false;
//			}
			count=numberOperat(count,val,"+");
		});
        var pointLen=$(tfootTd).attr("len");
        if(!pointLen)pointLen=4;
        if(count!=0){
        	$(tfootTd).children("div").text(numberSaveLen(count,parseInt(pointLen)));
        }
	});
}

//tfoot input  合计
function tfootInputCount($tbody,$tfoot){
	var $trs=$("tr",$tbody);
	var tfootTds=$("td",$tfoot);
	$(tfootTds).each(function(i,tfootTd){
		var hasCount=$(tfootTd).find("input").attr("hascount");
		
        if(!hasCount||hasCount!="true")return true;
        var count=0;
        $($trs).each(function(j,tbodyTr){
        	var td=$(tbodyTr).find(">td:eq("+i+")");
			var val=$(tbodyTr).find(">td:eq("+i+")").children("div").text();
			count=numberOperat(count,val,"+");
//			if(val=="***"){
//				count="***"; 
//				return false;
//			}
		});
        var pointLen=$(tfootTd).attr("len");
        if(!pointLen)pointLen=4;
        if(count!=0){
        	$(tfootTd).find("input").val(numberSaveLen(count,parseInt(pointLen)));
        }
	});
}

//表头拖拽
function theadDrag(thAllIndex,$grid,thead,tbody,tfoot,ftr,ttr,options){
   if($(thead).attr("drag")&&($(thead).attr("drag")==false||$(thead).attr("drag")=="false"))return false;
   $(">tr:last-child", thead).each(function(){
		$(">th", this).each(function(i){
			var th = this, $th = $(this);
			$th.mouseover(function(event){
				var offset = $.jTableTool.getOffset(th, event).offsetX;
				if($th.outerWidth() - offset < 5) {
					$th.css("cursor", "col-resize").mousedown(function(event){

						$(".resizeProxy", $grid).show().css({
							left: $.jTableTool.getRight(th)- $(".gridScroller", $grid).scrollLeft(),
							top:$.jTableTool.getTop(th),
							height:$.jTableTool.getHeight(th,$grid),
							cursor:"col-resize"
						});
						$(".resizeMarker", $grid).show().css({
								left: $.jTableTool.getLeft(th) + 1 - $(".gridScroller", $grid).scrollLeft(),
								top: $.jTableTool.getTop(th),
								height:$.jTableTool.getHeight(th,$grid)									
						});
						$(".resizeProxy", $grid).jDrag($.extend(options, {scop:true, cellMinW:20, relObj:$(".resizeMarker", $grid)[0],
								move: "horizontal",
								event:event,
								stop: function(){
									var pleft = $(".resizeProxy", $grid).position().left;
									var mleft = $(".resizeMarker", $grid).position().left;
									var move = pleft - mleft - $th.outerWidth() -9;

									//var cols = $.jTableTool.getColspan($th);
									var cellNum = $.jTableTool.getCellNum($th);
									//var cellNum= $th.index()+1;
									var oldW = $th.width(), newW = $th.width() + move;
									var $dcell = $(">td", ftr).eq(cellNum - 1);
									var $tcell = $(">td", ttr).eq(cellNum - 1);//tfoot tr宽度变换 kkz
									
									//更新头部合并单元格
									$th.width(newW + "px");
									$dcell.width(newW+ "px");
									$tcell.width(newW+ "px");
									
									if(thAllIndex!=0){ //双表头  eg：进货单位分布
										var firstH = $(">tr:first-child", thead);
										var colgroup = $(">colgroup",thead.parent());
										
										var $hhcell = $.jTableTool.getHeadColIndex(cellNum-1,firstH);
										var $cgcol = $(">col",colgroup).eq(cellNum - 1);
										//更新头部合并单元格
										$cgcol.attr("width",newW+7);
										$hhcell.width($hhcell.width() + move+7);
									}
									
									var $table1 = $(thead).parent();
									$table1.width($table1.width()+move+7);
									var $table2 = $(tbody).parent();
									$table2.width($table1.width());
									var $table3 = $(tfoot).parent();
									$table3.width($table1.width());
									
									$(".resizeMarker,.resizeProxy", $grid).hide();
									return false;
								}
							})
						);
						return false;
					});
				} else {
					$th.css("cursor", $th.attr("orderField") ? "pointer" : "default");
					$th.unbind("mousedown");
				}
				return false;
			});
		});
	});
}
/**
 * 加载 table 的Tr 时绑定一些事件
 * @param $trs
 * @return
 */
function trsBindEvent($trs, aStyles,model,pattern,$grid,tbody,$parent,tdHasDiv){
	$trs.hoverClass().each(function(){
		var $tr = $(this);
		var $ftds = $(">td", $tr);
		$ftds.each(function(i){
			var $ftd = $($ftds[i]);
			if((!tdHasDiv&&tdHasDiv!=false)||tdHasDiv==true)$ftd.html("<div>" + $ftd.html() + "</div>");
			if (i < aStyles.length){
				$ftd.width(aStyles[i][0]);
				$ftd.addClass(aStyles[i][1]);
			} 
		});
		
		var $tdDb;
		//单元格点击  事件
		$ftds.each(function(i){
			var $td=$(this);
			$td.click(function(){ //单元格单击
			   if($td.index()!=0&&$td.parent().parent()[0].nodeName!="TFOOT"){
				   if($td.hasClass("selected")){
					   if($td.attr("readonly")&&$td.attr("readonly")=="readonly"){
						  return;  
					   }
					   showCurrentTdWidget($td,true);
					   $td.removeClass("selected");
				   }else{
					   if(document.activeElement && document.activeElement.nodeName!="INPUT"){
						   addSelectClass(tbody.find('td'),$td);
					   }
				   }
				   
				   //一个页面两个表格带回所需传参确定是哪个表格
			       var $tbody = $td.parents("tbody:first");
			       if($tbody.attr("type")){
			           var $parent = navTab.getCurrentPanel();
			           $parent.find("input#billType").val($tbody.attr("type"));
			       }
			       //end一个页面两个表格带回所需传参确定是哪个表格
			       
			   }
			});
			$td.dblclick(function(event){
				$tdDb = $(this);
				if($td.index()!=0&&$td.parent().parent()[0].nodeName!="TFOOT"){
					addSelectClass(tbody.find('td'),$tdDb);
				}
				tdDblclick(event,$tdDb,$parent);
			});
			
			
		});
		//end 单元格点击  事件
		
		
		
		
		$tr.click(function(event){
			if(model==undefined){  //非单据  
				var $trP = $tr.parent();
				if("TFOOT"==$trP[0].nodeName){
					return false;
				}
				$trs.filter(".selected").removeClass("selected");
				//这句有很大的影响$("td",$trs).filter(".selected").removeClass("selected");
				$tr.addClass("selected");
				
				
				if($tr.attr("isSelect")){
					var selectId=$tr.attr("rel");
					$parent.find("#selectedObjectId").val(selectId);
				}
				
				/*if(!$tr.attr("cTarget")){
					return;
				}*/
				var cTarget = $tr.attr("cTarget");
				var url = $tr.attr("url");
				if("ajaxTodo"==cTarget){
					var crel = $tr.attr("crel");
					if (crel) {
						var $crel = $("#"+crel);
						$crel.loadUrl(url, {}, function(){
							$crel.find("[layoutH]").layoutH();
						});
					}
					event.preventDefault();
			
				}
			}else if(model=="order"){    //单据      
				//选中第一个非行号单元格     在单元格里做
				
			}
			
			//设置当前行的状态,用于是否执行后面的操作
			var status = $tr.attr("status");
			if(status){
				if ($("#status", $grid).size() == 0) {
					$grid.prepend('<input id="status" type="hidden" />');
				}
				if(status==2){
					$("#status", $grid).val(true);
				}else{
					$("#status", $grid).val(false);
				}
			}
			//end
			
			//单据红冲状态
			var isRCW = $tr.attr("isRCW");
			if(isRCW){
				if ($("#isRCW", $grid).size() == 0) {
					$grid.prepend('<input id="isRCW" type="hidden" />');
				}
				$("#isRCW", $grid).val(isRCW);
			}
			//end
			
			var sTarget = $tr.attr("target");
			if (sTarget) {
				if ($("#"+sTarget, $grid).size() == 0) {
					$grid.prepend('<input id="'+sTarget+'" name="'+sTarget+'" type="hidden" />');
				}
				$("#"+sTarget, $grid).val($tr.attr("rel"));
			}
			
			
			/*var trClickCallBack = $tr.attr("trClickCallBack");
			if(trClickCallBack){
				var arr = new Array();
				 arr = trClickCallBack.split("(");
				if(arr[0]){
					var $callback = eval('(' + arr[0] + ')');
					$callback($tr);
				}
			}*/
			
			
			
		});
		
		$tr.dblclick(function(event){
			//父节点行选中不可进入
			if($tr.attr("isCheckbox")){
				var checkBox=$tr.find("input[name='selectRow']");
				if($(checkBox).is(':checked')){
					alertMsg.warn($tr.attr("isCheckboxTxt") || DWZ.msg("alertSelectMsg"));
					return false;
				}
			}
			if($tdDb&&$tdDb.attr("removeTrDb")){
				return false;
			}
			var $trP = $tr.parent();
			if("TFOOT"==$trP[0].nodeName){
				return false;
			}
			if(!$tr.attr("cTarget")){
				return false;
			}
			var cTarget = $tr.attr("cTarget");
			var url = unescape($tr.attr("url")).replaceTmById($(event.target).parents(".unitBox:first"));
			//关联ID 参数
			var relevancyIds=$tr.attr("relevancyIds");
			var relevancyArea=$.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
			if(relevancyArea.is(":hidden")){
				relevancyArea= navTab.getCurrentPanel();
			};
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
				var str="({";
				if(relevancyIds.indexOf(",")!=-1){
					relevancyIdsArray=relevancyIds.split(",");
					for(var i=0;i<relevancyIdsArray.length;i++){
						/*var key=relevancyIdsArray[i];
						var val=relevancyArea.find("input[id='"+relevancyIdsArray[i]+"']").val();*/
						var valObj=relevancyArea.find("[id='"+relevancyIdsArray[i]+"']");
						var val=valObj.val();
						var key=valObj.attr("name");
						if(!key){
							key=relevancyIdsArray[i];
						}
						str+="'"+key+"':'"+val+"'";
						if(i!=relevancyIdsArray.length-1){
							str+=",";
						}
					}
				}else {
					str+="'"+relevancyIds+"':'"+$("input[id='"+relevancyIds+"']",relevancyArea).val()+"'";
				}
				str+="})";
				jsonData = eval(str);
			}
			var serialize = eval($tr.attr("serialize") || "false");//是否连载父页面的数据
			if(true==serialize){
				var $parent = $tr.closest("div[id='"+$tr.attr("crel")+"']");
				var $pagerForm = $("#pagerForm", $parent);
				jsonData = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
				for ( var i = 0; i < jsonData.length; i++) {
					var n = jsonData[i].name;
					if(n == "selectedObjectId"){
						jsonData.splice(i, 1);
					}
				}
			}
			if(cTarget=="ajax"){
				var crel = $tr.attr("crel");
				if (crel) {
					var $crel = $("#"+crel,getCurrentPanel());
					$crel.loadUrl(url, jsonData, function(){
						$crel.find("[layoutH]").layoutH();
					},"post");
				}
				event.preventDefault();
				//选中左树中的父节点
				var trel = $tr.attr("trel");
				var nodeId = $tr.attr("rel");
				if(trel){
					$('#'+trel).treeSelectNote(nodeId);
				}
			}else if(cTarget=="dialog"){
				var $dialog=$.pdialog.getCurrent();
				var $this = $(this);
				var title = $this.attr("title") || '';
				var rel = $this.attr("drel") || "_blank";
				
				var options = {};
				var w = $this.attr("widths");
				var h = $this.attr("heights");
				if (w) options.width = w;
				if (h) options.height = h;
				options.max = eval($this.attr("max") || "false");
				options.mask = eval($this.attr("mask") || "true");
				options.maxable = eval($this.attr("maxable") || "false");
				options.minable = eval($this.attr("minable") || "false");
				options.fresh = eval($this.attr("fresh") || "true");
				options.resizable = eval($this.attr("resizable") || "false");
				options.drawable = eval($this.attr("drawable") || "true");
				options.close = eval($this.attr("close") || "");
				options.param = $this.attr("param") || "";
				
				options.aimTabId=$this.attr("aimTabId") || "";//设置目标Tab组件Id
				options.aimUrl=$this.attr("aimUrl") || "";//设置目标url
				options.aimTitle=$this.attr("aimTitle") || "";//设置目标标题
				
				options.jsonData = jsonData;//提交参数
				options.type = $this.attr("type")||"GET";
				
                
				//商品弹出dialog checkbox  传入选择的ids 
				var url=$this.attr("url");
				var whichDialog=$this.attr("whichDialog");
				var singleId=$this.attr("single"); //单选
				if(singleId&&singleId=="singleObj"){
					//单据弹出商品   双击tr  禁用
				}else{
					if(whichDialog=="xsddProdcut"){  //eg:销售订单弹出商品dialog   tr
						var objId=$this.attr("newCheckboxId");  //多选
						if(objId){
							url=url.substring(0,url.lastIndexOf("-")+1)+$("#"+objId).val();
						}
						url = unescape(url).replaceTmById($(event.target).parents(".unitBox:first"));
						DWZ.debug(url);
						if (!url.isFinishedTm()) {
							alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
							return false;
						}
						$.pdialog.open(url, rel, title, options);
					}else if(whichDialog=="xsdStorage"){//eg:销售单弹出仓库dialog  tr
						var $dialogParent=$.pdialog.getCurrent();
						var selectTr=$dialogParent.find("tr").filter(".selected");
						var nodeType1=selectTr.attr("nodeType1");
						if(nodeType1==1){
							xscheckBack();//调用选中按钮事件
						}else{
							$.pdialog.open(url, rel, title, options);
						}
					}else if(whichDialog=="jsonData"){  //url传参数改成data传参数
						options.jsonData=setUrlParaToJsonData($this)
						/*var objId=$this.attr("newCheckboxId");  //多选
						if(objId){
							url=url.substring(0,url.lastIndexOf("-")+1)+$("#"+objId).val();
						}*/
						url = unescape(url).replaceTmById($(event.target).parents(".unitBox:first"));
						DWZ.debug(url);
						if (!url.isFinishedTm()) {
							alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
							return false;
						}
						$.pdialog.open(url, rel, title, options);
					}else{
						$.pdialog.open(url, rel, title, options);  //为了兼容   经手人，仓库，购买单位查询带回
					}
					
				}
				//end  商品弹出dialog checkbox  传入选择的ids 
				return false;
			}else if(cTarget=="ajaxBack"){
				var url  = $(this).attr("url") || "#";
		    	var callBack = $(this).attr("callback"); 
				if (! $.isFunction($callback)) var $callback = eval('(' + callBack + ')');
				$.ajax({
					type:'POST',
					url:url,
					dataType:"json",
					cache: false,
					success:$callback,
					error: DWZ.ajaxError
				});
			}else if(cTarget=="citeEvent"){
				var citeEventModuleId = $tr.attr("citeEventModuleId");//引用事件的组件ID
				if(citeEventModuleId){
					var citeEventType = $tr.attr("citeEventType");//引用事件类型
					var module = $("#"+citeEventModuleId,getCurrentPanel());
					if(citeEventType=="click"){
						module.click();
					}
				}
			}
			
		});
	});
}


