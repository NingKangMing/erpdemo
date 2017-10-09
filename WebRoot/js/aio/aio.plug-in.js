//插件

/**
 * 表格行上下移操作
 * author qidi<inpressing@qq.com>
 * date 2014-04-12
 */
(function($) {
$.fn.extend( {
	tableRowSort : function() {
		return this.each(function() {
			var $this = $(this);
			$this.click(function(event){
				var rel = $this.attr("rel") || "table";
				var targetType = $this.attr("targetType") || "navTab";
				var sort = $this.attr("sort") || "up";
				var $parent = (targetType=="navTab") ?  navTab.getCurrentPanel() : $.pdialog.getCurrent();
				var $box = $parent.find("#" + rel);
				if($box.length==0){
					$box=$parent;
				}
				var selected = $box.find("tr[class='selected']");
				if(selected.length==0){
					alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
				var tbody = $(".gridScroller", $parent) || $(".gridTbody2", $parent);
				$selected = $(selected[0]);
				if(sort=="up"){
					var preRow = $selected.prev();
					if(preRow.length>0){
						var preRowClone = $(preRow).clone();
						var selectedClone = $(selected[0]).clone();
						$(selected[0]).insertBefore(preRow);
						
						//更新选中行
						var sorder = $selected.attr("order");
						var porder = $(preRow).attr("order");
						
						$selected.find("td:eq(0)").find("a").text($(preRow).find("td:eq(0)").find("a").text());
						$selected.attr("order",porder).attr("_edit",true);
						
						//更新上一行
						$(preRow).find("td").eq(0).find("a").text($(selectedClone).find("td:eq(0)").find("a").text());
						$(preRow).attr("order",sorder).attr("_edit",true); 
						
						//tbody.scrollTop(preRow.position().top);
						TrVerticalScroll($selected);
					}
				}else{
					var nextRow = $(selected[0]).next();
					if(nextRow.length>0){
						var nextRowClone = $(nextRow).clone();;
						var selectedClone = $selected.clone();
						
						//更新选中行
						var sorder = $selected.attr("order");
						var norder = $(nextRow).attr("order");
						$selected.insertAfter(nextRow);
						$selected.find("td:eq(0)").find("a").text($(nextRow).find("td:eq(0)").find("a").text());
						$selected.attr("order",norder).attr("_edit",true);
						
						//更新下一行
						$(nextRow).find("td").eq(0).find("a").text($(selectedClone).find("td:eq(0)").find("a").text());
						$(nextRow).attr("order",sorder).attr("_edit",true);
		
						//tbody.scrollTop(nextRow.position().top);
						TrVerticalScroll($selected);
					}
				}
				return false;
			});
		});
	},
	tableRowSortSave : function() {
		return this.each(function() {
			var $this = $(this);
			$this.click(function(event){
				var rel = $this.attr("rel") || "table";
				var targetType = $this.attr("targetType") || "navTab";
				var $parent = (targetType=="navTab") ?  navTab.getCurrentPanel() : $.pdialog.getCurrent();
				var $box = $parent.find("#" + rel);
				if($box.length==0){
					$box=$parent;
				}
				var edits = $box.find("tr[_edit=true]");
				if(edits.length==0){
					alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
				var rows = new Array();
				var ids = new Array(); 
				var orders = new Array(); 
				for(var i=0;i<edits.length;i++){
					var id = $(edits[i]).attr("rel");
					var order = $(edits[i]).attr("order");
					var oldOrder = $(edits[i]).attr("oldOrder");
					//只修改变换的数据
					if(order != oldOrder){
						//rows.push({'id':id,'order':order});
						ids.push(id);
						orders.push(order);
					}
					
				}
				var url = $this.attr("href");
				var $callback = $this.attr("callback") || navTabAjaxDone;
				if (! $.isFunction($callback)) $callback = eval('(' + callback + ')');
				$.ajax({
					type:'POST',
					url:url,
					data:{'ids':ids.toString(),'orders':orders.toString()},
					dataType:"json",
					cache: false,
					success: $callback,
					error: DWZ.ajaxError
				});
				return false;
			});
		});
	},
	addTrEvent:function(){
		return this.each(function(){
			var $this = $(this);
			var rel  =  $this.attr("rel") || "tbody";
			$this.click(function(){
				addTr(rel,$this);
				return false;
			});
			
		});
	},
	cutRowShowOrHide:function(){
		return this.each(function (){
			var $this = $(this);
			var url = $this.attr("url");
			$this.addClass("pointer");
			$this.click(function(){
				$.ajax({
					type:'POST',
					url:url,
					dataType:"json",
					cache: false,
					success: function(data){
					   if(data && data.status==2){
						   $this.html("<div><a title='显示' href='javascript:void(0);' class='btnSelect'></a> </div>");
						   var linkageIds = data.linkageIds;
						   if(linkageIds && linkageIds.length>0){
							   var $tbody = $this.parents("tbody:first");
							   for(var i=0;i<linkageIds.length;i++){
								   $tbody.find("tr[rel='"+linkageIds[i]+"']").find("td:last").html("<div><a title='显示' href='javascript:void(0);' class='btnSelect'></a> </div>");
							   }
						   }
					   }else{
						   $this.html("");
						   if(data){
							   var linkageIds = data.linkageIds;
							   if(linkageIds && linkageIds.length>0){
								   var $tbody = $this.parents("tbody:first");
								   for(var i=0;i<linkageIds.length;i++){
									   $tbody.find("tr[rel='"+linkageIds[i]+"']").find("td:last").html("");
								   }
							   } 
						   }
						   
					   }
				    },
					error: DWZ.ajaxError
				});
			});
			
		});
	}
});
})(jQuery);


//树插件
(function($) {
	$.fn.extend( {
		tree : function() {
			return this.each(function() {
				$this = $(this);
				var url = $this.attr("url");
				var target = $this.attr('ctarget') || "ajax";
				var rel = $this.attr("rel") || "_blank";
				var trel = $this.attr("trel") || "tree";
				var relevancyIds=$this.attr("relevancyIds");
				// 加载数据
					$.ajax( {
						type : 'GET',
						url : url,
						dataType : 'json',
						timeout : 50000,
						cache : false,
						error : function(xhr) {
							// alert('Error loading XML document: ' + pageFrag +
							// "\nHttp status: " + xhr.status + " " +
							// xhr.statusText);
					},
					success : function(json) {
						var setting = {
							view : {
								showIcon : false,
								showLine : true,
								dblClickExpand : true,
								selectedMulti : true,
								expandAll : true
							},
							data : {
								simpleData : {
									enable : true,
									value : ""
								}
							}
						};
						var ztree = $.fn.zTree.init($this, setting, json);
						var selectNodeId=$this.attr("selectNodeId");
						var node;
						if(selectNodeId){
							node= ztree.getNodeByParam("id",selectNodeId, null);
						}else{
							node = ztree.getNodeByParam("id",0, null);
							if(!node){
								node = ztree.getNodes()[0];
							}
						}
						
						ztree.selectNode(node);
						
						ztree.expandAll(true);
						
						$this.find('a').attr( {target : target,rel : rel});
						if(relevancyIds){
							$this.find('a').attr("relevancyIds",relevancyIds);//设置提交参数
						}
						$this.initUI();
					}
					});
				});
		},
		treeAddNode:function(node){
			var $this = $(this);
			var target = $this.attr('ctarget') || "ajax";
			var rel = $this.attr("rel") || "_blank";
			var trel = $this.attr('id');
			
			var tree = $.fn.zTree.getZTreeObj(trel);
			var pnodes = tree.getNodesByParam("id",node.pId,null);
			var cnode = tree.getNodesByParam("id",node.id,null);
			if(cnode.length<=0){
				tree.addNodes(pnodes[0],node);
			}
			tree.selectNode(tree.getNodeByParam("id",node.id,null));
			
			$this.find("a[target!='ajax']").attr( {target : target,rel : rel}).parent().initUI();
		},
		treeRemoveNode:function(node){
			var $this = $(this);
			var trel = $this.attr('id');
			var tree = $.fn.zTree.getZTreeObj(trel);
			var cnode = tree.getNodeByParam("id",node.id,null);
			tree.removeNode(cnode);
			//选中父节点
			var pnode = tree.getNodeByParam("id",node.pId,null);
			if(pnode){
				tree.selectNode(pnode);
			}
		},
		treeUpdateNode:function(node){
			var $this = $(this);
			var trel = $this.attr('id');
			var tree = $.fn.zTree.getZTreeObj(trel);
			var cnode = tree.getNodeByParam("id",node.id,null);
			if(null!=cnode){
				cnode.name=node.name;
				tree.updateNode(cnode);
			}
			
			
		},
		treeSelectNote:function(nodeId){
			var $this = $(this);
			var trel = $this.attr('id');
			var tree = $.fn.zTree.getZTreeObj(trel);
			if(tree){
				var cnode = tree.getNodeByParam("id",nodeId,null);
				if(cnode){
					tree.selectNode(cnode);
				}
			}
		}
	});
})(jQuery);


//文本框禁止输入特殊字符
$.fn.extend( {
	checkSpecificKey : function() {
	    return this.each(function() {
	    	 var obj = $(this);
	    	 obj.keyup(function () {
				 // showKeyPress(obj);
			  });
	    	
	    });	 
  },
  showErrorMes:function(){
		var xOffset = -20; // x distance from mouse
	    var yOffset = 20; // y distance from mouse  
	  return this.each(function(){
		  var obj = $(this);
		  obj.hover(
				  function(e) {
						if($(this).attr('tip') != undefined){
							if(!$(this).hasClass("error"))return;
							var top = (e.pageY + yOffset);
							var left = (e.pageX + xOffset);
							$('body').append( '<p id="vtip" style="color: red;"><img id="vtipArrow" src="js/easyvalidator/images/vtip_arrow.png"/>' + $(this).attr('tip') + '</p>' );
							$('p#vtip').css("top", top+"px").css("left", left+"px");
							$('p#vtip').bgiframe();
						}
					},
					function() {
						if($(this).attr('tip') != undefined){
							$("p#vtip").remove();
						}
					}
		  ).mousemove(
				function(e) {
					if($(this).attr('tip') != undefined){
						var top = (e.pageY + yOffset);
						var left = (e.pageX + xOffset);
						$("p#vtip").css("top", top+"px").css("left", left+"px");
					}
				}
			).blur(function(){
				if($("p#vtip"))
				$("p#vtip").remove();
			}).focus(function(){
				if($("p#vtip"))
			    $("p#vtip").remove();
			});

	  });
  },
  aiovalidate:function(){
	  return this.each(function(){
		  var obj = $(this);
		  $(obj).validate({
			    onsubmit: false,
				focusInvalid: false,
				focusCleanup: true,
			    errorClass: "error",
			    ignore:".ignore",
		    	showErrors:function(errorMap,errorList){
			      if(errorList.length==0){
			    	  var errors = $("input[tip]:not(.error)",obj);
			    	  errors.each(function(){
			    		  $(this).removeAttr("tip");
			    		  $(this).unbind("showErrorMes");
			    	  });
			    	  
			      }
			      for(var i=0;i<errorList.length;i++){
			    	  var aa = errorList[i].element;
			    	  $(errorList[i].element).filter(":text").val("");
			    	  $(errorList[i].element).addClass("error");
			    	  $(errorList[i].element).attr("tip",errorList[i].message);
			    	  $(errorList[i].element).showErrorMes();
			      }
		        },
		        invalidHandler: function(form, validator) {
					var errors = validator.numberOfInvalids();
					if (errors) {
						var message = DWZ.msg("validateFormError",[errors]);
						alertMsg.error(message);
					} 
				}
		  });
	  });
  },
  aioUpAjax:function(){  //商品dialog上级  记录checkbox  ID
	  return this.each(function(){
		 $(this).click(function(event){
			var $this = $(this);
			var $parent=$.pdialog.getCurrent();
			var rel = $this.attr("rel");
			var newCheckBoxIds=$("#"+$this.attr("newCheckBoxsId")).val();    //修改之后
			var href=$this.attr("href");
			
			//关联ID 参数
			var relevancyIds=$this.attr("relevancyIds");
			var relevancyArea=$.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
			if(relevancyArea.is(":hidden")){
				relevancyArea= navTab.getCurrentPanel();
			};
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
				var str="({";
				relevancyIdsArray=relevancyIds.split(",");
				for(var i=0;i<relevancyIdsArray.length;i++){
					/*var key=relevancyIdsArray[i];
					var val=relevancyArea.find("#"+relevancyIdsArray[i]).val();
					str+=key+":'"+val+"'";*/
					var valobj=relevancyArea.find("#"+relevancyIdsArray[i]);
					var key=valobj.attr("name");
					if(!key){
						key=relevancyIdsArray[i];
					}
					var val=valobj.val();
					str+=key+":'"+val+"'";
					if(i!=relevancyIdsArray.length-1){
						str+=",";
					}
				}
				str+="})";
				jsonData = eval(str);
			}
			
			
			if(newCheckBoxIds){
				href=href+"-"+newCheckBoxIds;
			}
			if (rel) {
				var $rel = $("#"+rel);
				$rel.loadUrl(href, jsonData, function(){
					$rel.find("[layoutH]").layoutH();
				});
			}
			
			event.preventDefault();
		});
	 });
  },
  liAjaxCheck:function(){//ul li 选中提交
	  return this.each(function(){
		  var $this = $(this);
		  //默认选中第一个
		  var $firstLi = $this.find("li:first-child");
		  var frel = $firstLi.find("a").attr("rel")
		  var fhref = $firstLi.find("a").attr("href")
		  fhref = unescape(fhref).replaceTmById($this.parents(".unitBox:first"));
		  if($firstLi && frel){
			  $firstLi.addClass("selected");
			  var $frel = $("#"+frel);
				$frel.loadUrl(fhref, {}, function(){
					$frel.find("[layoutH]").layoutH();
				});
		  }
		  var $li = $this.children();
		  $li.each(function(){
			  var li = $(this);
			  li.click(function(event){
				  keydownSelectLi(li);
				  event.preventDefault();
			  });
			 
		  });
		  
		  $this.bind('keydown',function(event){
			  var selLi  = $(this).children(".selected");
			  if(event.keyCode == "38"){//上
				 var prev = selLi.prev();
				 if(prev.length>0){
					 keydownSelectLi(prev);
				 }
			  }else if(event.keyCode == "40"){//下
				  var next = selLi.next();
				  if(next.length>0){
					  keydownSelectLi(next);
				  }
			  }
		  });
		  
	  });
  },
  tableAjaxCheck:function(){ //table tr 选中提交
	  return this.each(function(){
		  var $this = $(this);
		  //默认选中第一个
		  var $first = $this.find("tr:first-child");
		  var rel = $first.attr("crel");
		  var url = $first.attr("url");
		  var $parent = $.pdialog.getCurrent()||navTab.getCurrentPanel();
		  if(rel && url){
			  var $rel = $parent.find("#"+rel);
			  $rel.loadUrl(url, {}, function(){
				  $rel.find("[layoutH]").layoutH();
			 });
		  }
	
		  $this.bind('keydown',function(event){
			  var sel  = $(this).children("tr.selected");
			  if(event.keyCode == "38"){//上
				 var prev = sel.prev();
				 if(prev.length>0){
					 prev.click(); 
				 }
			  }else if(event.keyCode == "40"){//下
				  var next = sel.next();
				  if(next.length>0){
					  next.click(); 
				  }
			  }
		  });
	  });
  },
  
  allCheck:function(){
	  return this.each(function(){
		  var $this = $(this);
		  $this.click(function(){
			  var name = $this.val();
			  var $parent = $.pdialog.getCurrent()||navTab.getCurrentPanel();
			  var che = $(this).attr("checked");
			  if(che=="checked"){
				  $parent.find("input:checkbox[name='"+name+"']").attr("checked","checked");
			  }else{
				  $parent.find("input:checkbox[name='"+name+"']").removeAttr("checked");
			  }
		  });
		  
	  });
  },
  inputEnterCallBack:function(){
	  return this.each(function(){
		  var $this = $(this);
		 
		  $this.dblclick(function(event){
			  $this.next("a").click();
		  });
		  $this.keydown(function(event){
			  switch(event.keyCode) {
			  case 13:
				  var name = $this.val();
				  var $a = $this.next("a");
				  $a.attr("param",name).click();
				  $a.attr("param","");
				  break;
			  }
		  });
		  
		  $this.blur(function(){
			  var tval = $this.val();
			  var name = $this.attr("name");
			  var $a = $this.next("a");
			  var lookupgroup = $a.attr("lookupgroup");
			  var $divP = $this.parents("div:first");
			  if($.trim(tval)==""){
				  $this.val("");
				  $("input[name ^='"+lookupgroup+".']",$divP).val("");
			  }else{
				  var hiddenVal  = $("input:hidden[name='"+name+"']:first",$divP).val();
				  $this.val(hiddenVal);
			  }
		  });
	  });
  },
  //input回车查找带回
  inputLookEnterCallBack:function(){
	  return this.each(function(){
		  $(this).dblclick(function(event){
			  	var $this = $(this);
				var lookName = $this.attr("lookName");
				var $btnLook = $this.next("a[lookupgroup="+lookName+"]");//btnLook查找带回插件
				$btnLook.click();
			});
			$(this).keydown(function(event){
				var $this = $(this);
				var lookName = $this.attr("lookName");
				var $btnLook = $this.next("a[lookupgroup="+lookName+"]");//btnLook查找带回插件
				
				switch(event.keyCode) {
				  case 13://回车
					 $btnLook.attr("isAll",false);
					 $btnLook.click();
				  break;
				}
			});
			$(this).blur(function(event){
				var $this = $(this);
				var lookId = $this.attr("lookId");
				var $parent= $this.parent();
				var $lookId = $("input[name='"+lookId+"']",$parent);//ID对象
				var oldTxt = $this.attr("oldTxt");
				
				if($.trim($this.val()) == ""){
				   $this.removeAttr("oldTxt");
				   $lookId.val("");
				}
				if($lookId.val()==""&&!$this.attr("oldTxt")){
					$this.val("");
				}else {
					$this.val(oldTxt);
				}
			});
	  });
  },
  
  buttonFormSubmit:function(){
	 return $(this).each(function(){
		 var $this = $(this);
		 $this.click(function(){
			 $this.parents("form:first").submit();
		 });
	 });
  },
  
  trOpenNavTab:function(){
	  return $(this).each(function (){
		  var $this = $(this);
		  $this.dblclick(function(event){
			    var isBill=$this.attr("isBill"); //是否是单据
			    if(isBill&&isBill=="no"){
			    	alertMsg.error("请选择一张单据");
			    	return;
			    }
			  
			    var sameId=$this.attr("sameId");  //直接调用下面相同功能的按钮
			    if(sameId){
			    	var $parent =getCurrentPanel();
			    	$("#"+sameId,$parent).click();
			    	return ;
			    }
			    
			    var nodeType1=$this.attr("nodeType1");
			    if(nodeType1&&nodeType1==2){ //分类节点   返回  
			    	return;
			    }
				var title = $this.attr("title") || $this.text();
				var tabid = $this.attr("cRel") || "_blank";
				var fresh = eval($this.attr("fresh") || "true");
				var external = eval($this.attr("external") || "false");
				var url = unescape($this.attr("url"));
				DWZ.debug(url);
				if (!url.isFinishedTm()) {
					alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
				var serialize = eval($this.attr("serialize") || "false");//是否连载父页面的数据
				var data = {};
				if(true==serialize){
					var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
				    data = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
				}
				var isOpen = true;
				if($this.attr("callBefore")){
					var callBefore = $this.attr("callBefore") ;
					if (! $.isFunction(callBefore)) callBefore = eval('(' + callBefore + ')');
					var options = new Object();
					options.url=url;
					options.checkout=true; 
					options.obj = $this;
					isOpen=callBefore(options);
				}
				if(!isOpen)return false;
				navTab.openTab(tabid, url,{title:title, fresh:fresh, external:external,data:data});

				event.preventDefault();
		  });
	  });
  },
  
  inputRadioAjax:function(){
	  return this.each(function(){
		  var $this = $(this);
		  $this.click(function(){
			  $panal=getCurrentPanel();//当前面板
			  var rel=$this.attr("rel");
			  $parent=$("[id="+rel+"]",$panal);
			  $pagerForm=$("form[id='pagerForm']",$parent);
			  $("[name="+$this.attr("name")+"]",$pagerForm).val($this.val());
			  var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
			  navTabPageBreak(args,rel);
		  });
	  });
  },
  
  inputCheckboxAjax:function(){ //系统配置点击复选框
	  return this.each(function(){
		  var $this = $(this);
		  $this.click(function(){
			  $panal = getCurrentPanel();//当前面板
			  var url = $this.attr("ajaxUrl");
			  
			  var key = $this.attr("name");
			  var val = $this.attr("rel");
			  
			  var key1 = "val";
			  var val1 = $this.is(':checked')==true?1:0;
				  
			  var str="({";
			  str+="'"+key+"':'"+val+"',";
			  str+="'"+key1+"':'"+val1+"'";
			  str+="})";
			  data = eval(str);
			  
			  $.ajax({
					type:'POST',
					url:url,
					data:data,
					dataType:"json",
					cache: false,
					//success:$callback,
					error: DWZ.ajaxError
				});
		  });
	  });
  },
  
  formSubmit:function(){
	  return this.each(function(){
		  var $this = $(this);
		  $this.click(function(){
			 var $form = $this.parents("form:first");
			 if(!$form.valid()){
				return false; 
			 }
			 var action = $this.attr("href");
			 if(action)$form.attr("action",action);
			 $form.submit();
			 return false; 
		  });
	  });
  }
  
});

(function($){
	$.fn.extend({ //库存商品带回
		checkedBack:function(){
		    return this.each(function(){
		    	var $this = $(this);
		    	var opterate=$this.attr("opterate");
		    	var url  = $this.attr("url") || "#";
		    	var id  = $this.attr("irel");  //多选
		    	var singleId  = $this.attr("single");  //单选
		    	var callBack = $this.attr("callback"); 
		    	var $callback = callBack || boughtBookCallBack;
		    	var param  = $this.attr("param")||""; 
		    	if(id){  
		    		$this.click(function(event){
		    			var $parent = navTab.getCurrentPanel();
		    			if(opterate&&opterate=="select"){//单据  商品弹出  checkbox多选
		    				var newProductIds=$("#"+id).val();
				            if(newProductIds==null||newProductIds==""){
				               alertMsg.error("请至少选择一条记录!");
				               return;
				            }
				            url+=newProductIds;
				            url=url+"-"+$("#whichCallBack",$parent).val();  //不同模块调用函数不同
				           
				            var $dialog=getCurrentPanel();
				            var storageId=$("#storageId",$dialog).val();
				            var billType=$("#billType",$dialog).val();
				            var unitId=$("#unitId",$dialog).val();
				            var term=$("#term",$dialog).val();
				            var termVal=$("#termVal",$dialog).val();
				            
				            
				            data={'storageId':storageId,"billType":billType,"unitId":unitId,"term":term,"termVal":termVal,'whichCallBack':$("#whichCallBack",$parent).val(),'newDownSelectObjs':$("#b_dialog_product_page_checkboxIds",$dialog).val()}
				            checkedBackAjax(url,$callback,data);
				    		return false;
		    			}else{//修改库存商品选中按钮
		    				var $dialog=$.pdialog.getCurrent() || navTab.getCurrentPanel();
		    				var productId=$dialog.find("tr").filter(".selected").attr("rel");
		    				url=url+productId+"-"+$("#whichCallBack",$parent).val();  //不同模块调用函数不同
		    				var storageId=$("#storageId",$parent).val();
		    				var billType=$("#billType",$dialog).val();
				            var unitId=$("#unitId",$dialog).val();
				            data={'storageId':storageId,"billType":billType,"unitId":unitId}
				            checkedBackAjax(url,$callback,data);
				    		return false;
		    			}
			    	});
		    	}
                if(singleId&&singleId=="singleObj"){  //单据  商品弹出  双击TR 单选
                	$this.dblclick(function(event){
                		var $parent = navTab.getCurrentPanel();
                		var whichTr=$this.attr("whichTr");
                		if(whichTr&&whichTr=="accountsMulit"){   //会计科目多选择dialog
                			data={'whichCallBack':$("#whichCallBack",getCurrentPanel()).val(),'newDownSelectObjs':$("#b_dialog_product_page_checkboxIds",$parent).val()}
                		}else{
                			var productId=$("#storageProdcutId",$.pdialog.getCurrent()).val();
                    		url=url+"-"+$("#whichCallBack",$parent).val()+"-"+productId+"-"+param;  //不同模块调用函数不同
                    		var storageId=$("#storageId",getCurrentPanel()).val();
                    		var billType=$("#billType",$dialog).val();
				            var unitId=$("#unitId",$dialog).val();
				            data={'storageId':storageId,"billType":billType,"unitId":unitId}
                		}
			            checkedBackAjax(url,$callback,data);
			    		return false;
			    	});
                }

		    });
	    }
	});
})(jQuery);
function checkedBackAjax(url,callBack,data){
	if (! $.isFunction($callback)) var $callback = eval('(' + callBack + ')');
	$.ajax({
		type:'POST',
		url:url,
		data:data,
		dataType:"json",
		cache: false,
		success:$callback,
		error: DWZ.ajaxError
	});
}
function optionOneData(json,options){
	$.pdialog.closeCurrent();
	var lookupgroup = options.lookupgroup;
	var _lookup = options.lookup;
	var module=_lookup['$target'].attr("module");
	if(module=="navTab"){
		var $box = navTab.getCurrentPanel();
	}else {
		var $box = _lookup['$target'].parents(".unitBox:first");
	}
	DWZ.ajaxDone(json);
	for (var key in json) {
		$("input[name='"+lookupgroup+"."+key+"']",$box).val(json[key]);
		$("input[name='"+lookupgroup+"."+key+"']",$box).attr("oldTxt",json[key]);
	}
}





