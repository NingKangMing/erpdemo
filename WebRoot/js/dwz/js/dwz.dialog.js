/**
 * @author Roger Wu
 * reference:dwz.drag.js, dwz.dialogDrag.js, dwz.resize.js, dwz.taskBar.js
 */
(function($){
	$.pdialog = {
		_op:{height:300, width:580, minH:40, minW:50, total:20, max:false, mask:true, resizable:false, drawable:true, maxable:false,minable:false,fresh:true},
		_current:null,
		_zIndex:42,
		getCurrent:function(){
			return this._current;
		}, 
		refreshCurrent:function(){
			var cDialog = this._current;
			var url = $(cDialog).data("url");
			var dlgid = $(cDialog).data("id");
			this.open(url, dlgid, "", {});
		},
		reload:function(url, options){
			var op = $.extend({data:{}, dialogId:"", callback:null}, options);
			var dialog = (op.dialogId && $("body").data(op.dialogId)) || this._current;
			if(dialog.is(":hidden")){
				dialog=getCurrentPanel();
				this._current=dialog;
			}
			if (dialog){
				var jDContent = dialog.find(".dialogContent");
				jDContent.ajaxUrl({
					type:"POST", url:url, data:op.data, callback:function(response){
						jDContent.find("[layoutH]").layoutH(jDContent);
						//$(".pageContent", dialog).width($(dialog).width()-14);
						$(":button.close", dialog).click(function(){
							$.pdialog.close(dialog);
							return false;
						});
						if ($.isFunction(op.callback)) op.callback(response);
					}
				});
			}
		},
		//打开一个层
		open:function(url, dlgid, title, options) {
			var op = $.extend({},$.pdialog._op, options);
			var dialog = $("body").data(dlgid);
			var params = "";
			var param = "";
			if(options && options.param){
				var $parent  =  navTab.getCurrentPanel();
				param  = options.param;
				params = $parent.find("input[name='"+options.param+"']").val();
			}
			//重复打开一个层
			if(dialog) {
				if(dialog.is(":hidden")) {
					dialog.show();
				}
				if(op.fresh || url != $(dialog).data("url")){
					dialog.data("url",url);
					dialog.find(".dialogHeader").find("h1").html(title);
					this.switchDialog(dialog);
					var jDContent = dialog.find(".dialogContent");
					var datas  = new Object();
					if(params){
						datas.params = params;
					}
					if(param){
						datas.param = param;
					}
					if(options&&options.jsonData){
						datas = options.jsonData;
					}
					var ajaxType=undefined;
					if(options){
						ajaxType=options.type;
					}
					
					//增加按钮模式参数
					if(options&&options.btnPattern){
						datas.btnPattern=options.btnPattern;
					}
					//过滤类型参数
					if(options&&options.screenType){
						datas.screenType=options.screenType;
					}
					
					jDContent.loadUrl(url, datas, function(data){
						if(data.statusCode && data.statusCode==DWZ.statusCode.ok && data.obj){
							optionOneData(data.obj,options);//一条数据直接带回
						}
						
						var firstInp = jDContent.find("input[type='text']:first");
						var v = firstInp.val();
						firstInp.val("").focus().val(v);//让第一个输入框获得焦点,把光标定位到最后
						
						//设置optionsAdd参数
						if(options && options.opterate && options.selectedObjectId && options.selectedObjectId != ""){
							if(options.opterate == "showLastPage"){
								dialog.find("form").find("#showLastPage").val("true");
								dialog.find("form").find("#selectedObjectId").val(options.selectedObjectId);
							}
						}
						
						jDContent.find("[layoutH]").layoutH(jDContent);
						$(".pageContent", dialog).width($(dialog).width()-14);
						/*$("button.close").click(function(){
							$.pdialog.close(dialog);
							return false;
						});*/
					},ajaxType);
					
				}
			
			} else { //打开一个全新的层
				var hasClose =options.close; 
				if(hasClose==false||hasClose=="false"){
					$("body").append(DWZ.frag["dialogFragHasClose"]);
				}else{
					$("body").append(DWZ.frag["dialogFrag"]);
				}
				dialog = $(">.dialog:last-child", "body");
				dialog.data("id",dlgid);
				dialog.data("url",url);
				if(options.close) dialog.data("close",options.close);
				if(options.param) dialog.data("param",options.param);
				($.fn.bgiframe && dialog.bgiframe());
				
				dialog.find(".dialogHeader").find("h1").html(title);
				$(dialog).css("zIndex", ($.pdialog._zIndex+=2));
				$("div.shadow").css("zIndex", $.pdialog._zIndex - 3).show();
				$.pdialog._init(dialog, options);
				$(dialog).click(function(){
					$.pdialog.switchDialog(dialog);
				});
				
				if(op.resizable)//dialog窗体大小变化
					dialog.jresize();  
				if(op.drawable)
				 	dialog.dialogDrag();
				$("a.close", dialog).click(function(event){ 
					$.pdialog.close(dialog);
					return false;
				});
				if (op.maxable) {
					//窗体不允许最大化
					$("a.maximize", dialog).show().click(function(event){
						$.pdialog.switchDialog(dialog);
						$.pdialog.maxsize(dialog);
						dialog.jresize("destroy").dialogDrag("destroy");
						return false;
					});
				} else {
					$("a.maximize", dialog).hide();
				}
				$("a.restore", dialog).click(function(event){
					$.pdialog.restore(dialog);
					dialog.jresize().dialogDrag();
					return false;
				});
				if (op.minable) {
					//窗体不允许最小化
					$("a.minimize", dialog).show().click(function(event){
						$.pdialog.minimize(dialog);
						return false;
					});
				} else {
					$("a.minimize", dialog).hide();
				}
				$("div.dialogHeader a", dialog).mousedown(function(){
					return false;
				});
				$("div.dialogHeader", dialog).dblclick(function(){
					if($("a.restore",dialog).is(":hidden"))
						$("a.maximize",dialog).trigger("click");
					else
						$("a.restore",dialog).trigger("click");
				});
				if(op.max) {
//					$.pdialog.switchDialog(dialog);
					$.pdialog.maxsize(dialog);
					dialog.jresize("destroy").dialogDrag("destroy");
				}
				$("body").data(dlgid, dialog);
				$.pdialog._current = dialog;
				$.pdialog.attachShadow(dialog);
				//load data
				var jDContent = $(".dialogContent",dialog);
				var datas  = new Object();
				if(params){
					datas.params = params;
				}
				
				if(param){
					datas.param = param;
				}

				if(options.jsonData && !$.isEmptyObject(options.jsonData)){
					datas = options.jsonData;
				}
			
				//增加按钮模式参数
				if(options&&options.btnPattern){
					datas.btnPattern=options.btnPattern;
				}
				//过滤类型参数
				if(options&&options.screenType){
					datas.screenType=options.screenType;
				}
				
				jDContent.loadUrl(url, datas, function(data){
					if(data.statusCode && data.statusCode==DWZ.statusCode.ok && data.obj){
						optionOneData(data.obj,options);//一条数据直接带回
					}
					
					var firstInp = jDContent.find("input[type='text']:first");
					var v = firstInp.val();
					firstInp.val("").focus().val(v);//让第一个输入框获得焦点,把光标定位到最后
					
					//设置dialog请求新URL并打开NavTab的提交参数
					if(options.aimUrl && options.aimUrl != ""){
						if(options.aimTabId && options.aimTabId != ""){
							dialog.find("#aimTabId").val(options.aimTabId);//设置目标Tab组件ID
						}else if (options.aimDlgId && options.aimDlgId != "") {
							dialog.find("#aimDlgId").val(options.aimDlgId);//设置目标dialog组件ID
							dialog.find("#aimWidth").val(options.aimWidth);//设置目标dialog宽度
							dialog.find("#aimHeight").val(options.aimHeight);//设置目标dialog高度
						}
						dialog.find("#aimUrl").val(options.aimUrl);//设置目标URl
						dialog.find("#aimTitle").val(options.aimTitle);//设置目标标题
					}
					//end
					//设置dialog给目标DIV赋值的提交参数
					if(options.aimDiv && options.aimDiv != ""){
						dialog.find("#aimDiv").val(options.aimDiv);//设置目标div的ID
					}
					//end
					
					jDContent.find("[layoutH]").layoutH(jDContent);
					$(".pageContent", dialog).width($(dialog).width()-14);
					/*$("button.close").click(function(){
						$.pdialog.close(dialog);
						return false;
					});*/
				},options.type);
					
			}
			if (op.mask) {
				//$(dialog).css("zIndex", 1000);
				var $dialogs=$("div[class='dialog']");
				for(var i=0;i<$dialogs.length;i++){
					$dialog=$($dialogs[i]);
					$dialog.css("zIndex", 1000+i);
					$("#dialogBackground").css("zIndex", 1000+i);
				}
				
				$("a.minimize",dialog).hide();
				$(dialog).data("mask", true);
				$("#dialogBackground").show();
			}else {
				//add a task to task bar
				if(op.minable) $.taskBar.addDialog(dlgid,title);
			}
			
		},
		/**
		 * 切换当前层
		 * @param {Object} dialog
		 */
		switchDialog:function(dialog) {
			var $dialogs=$("div[class='dialog']");
			dialog=$($dialogs[0]);
			var i = dialog.css("zIndex");
			for(var i=0;i<$dialogs.length;i++){
				$dialog=$($dialogs[i]);
				if($dialog.css("zIndex")>i){
					dialog=$dialog;
				}
			}
			
			
			var index = $(dialog).css("zIndex");
			$.pdialog.attachShadow(dialog);
			if($.pdialog._current) {
				var cindex = $($.pdialog._current).css("zIndex");
				$($.pdialog._current).css("zIndex", index);
				$(dialog).css("zIndex", cindex);
				$("div.shadow").css("zIndex", cindex - 1);
				$.pdialog._current = dialog;
			}
			$.taskBar.switchTask(dialog.data("id"));
		},
		/**
		 * 给当前层附上阴隐层
		 * @param {Object} dialog
		 */
		attachShadow:function(dialog) {
			var shadow = $("div.shadow");
			if(shadow.is(":hidden")) shadow.show();
			shadow.css({
				top: parseInt($(dialog)[0].style.top) - 2,
				left: parseInt($(dialog)[0].style.left) - 4,
				height: parseInt($(dialog).height()) + 8,
				width: parseInt($(dialog).width()) + 8,
				zIndex:parseInt($(dialog).css("zIndex")) - 1
			});
			$(".shadow_c", shadow).children().andSelf().each(function(){
				$(this).css("height", $(dialog).outerHeight() - 4);
			});
		},
		_init:function(dialog, options) {
			var op = $.extend({}, this._op, options);
			var height = op.height>op.minH?op.height:op.minH;
			var width = op.width>op.minW?op.width:op.minW;
			if(isNaN(dialog.height()) || dialog.height() < height){
				$(dialog).height(height+"px");
				$(".dialogContent",dialog).height(height - $(".dialogHeader", dialog).outerHeight() - $(".dialogFooter", dialog).outerHeight() - 6);
			}
			if(isNaN(dialog.css("width")) || dialog.width() < width) {
				$(dialog).width(width+"px");
			}
			
			var iTop = ($(window).height()-dialog.height())/2;
			dialog.css({
				left: ($(window).width()-dialog.width())/2,
				top: iTop > 0 ? iTop : 0
			});
		},
		/**
		 * 初始化半透明层
		 * @param {Object} resizable
		 * @param {Object} dialog
		 * @param {Object} target
		 */
		initResize:function(resizable, dialog,target) {
			$("body").css("cursor", target + "-resize");
			resizable.css({
				top: $(dialog).css("top"),
				left: $(dialog).css("left"),
				height:$(dialog).css("height"),
				width:$(dialog).css("width")
			});
			resizable.show();
		},
		/**
		 * 改变阴隐层
		 * @param {Object} target
		 * @param {Object} options
		 */
		repaint:function(target,options){
			var shadow = $("div.shadow");
			if(target != "w" && target != "e") {
				shadow.css("height", shadow.outerHeight() + options.tmove);
				$(".shadow_c", shadow).children().andSelf().each(function(){
					$(this).css("height", $(this).outerHeight() + options.tmove);
				});
			}
			if(target == "n" || target =="nw" || target == "ne") {
				shadow.css("top", options.otop - 2);
			}
			if(options.owidth && (target != "n" || target != "s")) {
				shadow.css("width", options.owidth + 8);
			}
			if(target.indexOf("w") >= 0) {
				shadow.css("left", options.oleft - 4);
			}
		},
		/**
		 * 改变左右拖动层的高度
		 * @param {Object} target
		 * @param {Object} tmove
		 * @param {Object} dialog
		 */
		resizeTool:function(target, tmove, dialog) {
			$("div[class^='resizable']", dialog).filter(function(){
				return $(this).attr("tar") == 'w' || $(this).attr("tar") == 'e';
			}).each(function(){
				$(this).css("height", $(this).outerHeight() + tmove);
			});
		},
		/**
		 * 改变原始层的大小
		 * @param {Object} obj
		 * @param {Object} dialog
		 * @param {Object} target
		 */
		resizeDialog:function(obj, dialog, target) {
			var oleft = parseInt(obj.style.left);
			var otop = parseInt(obj.style.top);
			var height = parseInt(obj.style.height);
			var width = parseInt(obj.style.width);
			if(target == "n" || target == "nw") {
				tmove = parseInt($(dialog).css("top")) - otop;
			} else {
				tmove = height - parseInt($(dialog).css("height"));
			}
			$(dialog).css({left:oleft,width:width,top:otop,height:height});
			$(".dialogContent", dialog).css("width", (width-12) + "px");
			$(".pageContent", dialog).css("width", (width-14) + "px");
			if (target != "w" && target != "e") {
				var content = $(".dialogContent", dialog);
				content.css({height:height - $(".dialogHeader", dialog).outerHeight() - $(".dialogFooter", dialog).outerHeight() - 6});
				content.find("[layoutH]").layoutH(content);
				$.pdialog.resizeTool(target, tmove, dialog);
			}
			$.pdialog.repaint(target, {oleft:oleft,otop: otop,tmove: tmove,owidth:width});
			
			$(window).trigger(DWZ.eventType.resizeGrid);
		},
		close:function(dialog) {
			if(typeof dialog == 'string') dialog = $("body").data(dialog);
			var close = dialog.data("close");
			var go = true;
			if(close && $.isFunction(close)) {
				var param = dialog.data("param");
				if(param && param != ""){
					param = DWZ.jsonEval(param);
					go = close(param);
				} else {
					go = close();
				}
				if(!go) return;
			}
			
			$(dialog).hide();
			$("div.shadow").hide();
			if($(dialog).data("mask")){
				//关闭多层dialog时限制模式窗口
				var zIndex=0;
				var $dialogs=$("div[class='dialog']:visible");
				for(var i=0;i<$dialogs.length;i++){
					$dialog=$($dialogs[i]);
					var	z=$dialog.css("zIndex");
					if(z>zIndex)zIndex=z;
				}
				if($dialogs.length>0){
					$("#dialogBackground").css("zIndex", zIndex);
					$("#dialogBackground").show();
				}else{
					$("#dialogBackground").hide();
				}
			} else{
				if ($(dialog).data("id")) $.taskBar.closeDialog($(dialog).data("id"));
			}
			$("body").removeData($(dialog).data("id"));
			$(dialog).trigger(DWZ.eventType.pageClear).remove();
			
			
			var confirmType=$("#confirmType",dialog).val();   //当dialog关闭回调其它函数
			if(confirmType&&confirmType=="xs_costArith"){  //销售单   商品为手工指定法
				var trIndexs=$("#trIndexs",dialog).val();
				var productIds=$("#productIds",dialog).val();
				var trcostAriths=$("#trcostAriths",dialog).val();
				var tbodyId=$("#tbodyId",dialog).val();
				var module=$("#module",dialog).val();//区分模块
				if(trIndexs!=""){
					if(module && module=="breakageBill"){
						callbackProToCostPrice(trIndexs,productIds,trcostAriths,tbodyId);
					}else if(module && module=="takeStock"){
						callBackTackStockPrice(trIndexs,productIds,trcostAriths,tbodyId);
					}else{
						callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId);
					}
					
				}
			}else if (confirmType&&confirmType=="dialogCloseReload") {//dialog关闭后刷新   eg:期初商品库存关闭批次dialog时回调
				var panel=getCurrentPanel();
				var $pagerForm=$("#pagerForm",panel);
				if(panel.hasClass("dialog")){
					var showLastPage=$("#showLastPage",dialog).val();
					var selectedObjectId=$("#selectedObjectId",dialog).val();
					var supId=$("#supId",dialog).val();
					if(showLastPage&&selectedObjectId){
						$("#showLastPage",$pagerForm).val(showLastPage);
						$("#selectedObjectId",$pagerForm).val(selectedObjectId);
						$("#supId",$pagerForm).val(supId);
					}
					var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
					dialogPageBreak(args);//刷新
					
				}else{
					var aimRel=$("#aimRel",dialog).val();
					var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
					navTabPageBreak(args, aimRel);//刷新
				}
			}else if (confirmType&&confirmType=="reloadNavTab") {//刷新NavTab
				var panel=getCurrentPanel();
				var $pagerForm=$("#pagerForm",panel);
				var isSearch = $("#isSearch",dialog).val();
				if(isSearch>0){
					$("#isSearch",$pagerForm).val(0);
					var aimUrl=$("#aimUrl",dialog).val();
					navTab.reload(aimUrl);
				}
			}else if (confirmType&&confirmType=="reloadParentPage") {//刷新父页面
				var panel=getCurrentPanel();
				var $pagerForm=$("#pagerForm",panel);
				if($pagerForm){
					var url = $pagerForm.attr("action");
					var divId = $pagerForm.parents("div[id]:first").attr("id");
					var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
					if(panel.attr("class").indexOf("dialog")!=-1){
						dialogPageBreak(args, divId);//刷新
					}else{
						navTabPageBreak(args, divId);//刷新
					}
				}
			}else if(confirmType&&confirmType=="fujian"){  //单据附件
				  var fuJianIds = $("#fuJianIds",dialog).val();
			      var nvaTable = navTab.getCurrentPanel();
			      $("#orderFuJianIds",nvaTable).val(fuJianIds);
			      if(fuJianIds&&fuJianIds.length>0){
			    	  $("#hasFujianClassId",nvaTable).addClass("fujianClass");
			      }else{
			    	  $("#hasFujianClassId",nvaTable).removeClass("fujianClass");
			      }
			}
			
		},
		closeCurrent:function(){
			this.close($.pdialog._current);
		},
		checkTimeout:function(){
			var $conetnt = $(".dialogContent", $.pdialog._current);
			var json = DWZ.jsonEval($conetnt.html());
			if (json && json.statusCode == DWZ.statusCode.timeout) this.closeCurrent();
		},
		maxsize:function(dialog) {
			$(dialog).data("original",{
				top:$(dialog).css("top"),
				left:$(dialog).css("left"),
				width:$(dialog).css("width"),
				height:$(dialog).css("height")
			});
			$("a.maximize",dialog).hide();
			$("a.restore",dialog).show();
			var iContentW = $(window).width();
			var iContentH = $(window).height() - 34;
			$(dialog).css({top:"0px",left:"0px",width:iContentW+"px",height:iContentH+"px"});
			$.pdialog._resizeContent(dialog,iContentW,iContentH);
		},
		restore:function(dialog) {
			var original = $(dialog).data("original");
			var dwidth = parseInt(original.width);
			var dheight = parseInt(original.height);
			$(dialog).css({
				top:original.top,
				left:original.left,
				width:dwidth,
				height:dheight
			});
			$.pdialog._resizeContent(dialog,dwidth,dheight);
			$("a.maximize",dialog).show();
			$("a.restore",dialog).hide();
			$.pdialog.attachShadow(dialog);
		},
		minimize:function(dialog){
			$(dialog).hide();
			$("div.shadow").hide();
			var task = $.taskBar.getTask($(dialog).data("id"));
			$(".resizable").css({
				top: $(dialog).css("top"),
				left: $(dialog).css("left"),
				height:$(dialog).css("height"),
				width:$(dialog).css("width")
			}).show().animate({top:$(window).height()-60,left:task.position().left,width:task.outerWidth(),height:task.outerHeight()},250,function(){
				$(this).hide();
				$.taskBar.inactive($(dialog).data("id"));
			});
		},
		_resizeContent:function(dialog,width,height) {
			var content = $(".dialogContent", dialog);
			content.css({width:(width-12) + "px",height:height - $(".dialogHeader", dialog).outerHeight() - $(".dialogFooter", dialog).outerHeight() - 6});
			content.find("[layoutH]").layoutH(content);
			$(".pageContent", dialog).css("width", (width-14) + "px");
			
			$(window).trigger(DWZ.eventType.resizeGrid);
		}
	};
})(jQuery);