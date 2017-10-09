function initEnv() {
	$("body").append(DWZ.frag["dwzFrag"]);

	if ( $.browser.msie && /6.0/.test(navigator.userAgent) ) {
		try {
			document.execCommand("BackgroundImageCache", false, true);
		}catch(e){}
	}
	//清理浏览器内存,只对IE起效
	if ($.browser.msie) {
		window.setInterval("CollectGarbage();", 10000);
	}

	$(window).resize(function(){
		initLayout();
		$(this).trigger(DWZ.eventType.resizeGrid);
	});

	var ajaxbg = $("#background,#progressBar");
	ajaxbg.hide();
	$(document).ajaxStart(function(){
		ajaxbg.show();
	}).ajaxStop(function(){
		ajaxbg.hide();
	});
	
	$("#leftside").jBar({minW:150, maxW:700});
	
	if ($.taskBar) $.taskBar.init();
	navTab.init();
	if ($.fn.switchEnv) $("#switchEnvBox").switchEnv();
	if ($.fn.navMenu) $("#navMenu").navMenu();
		
	setTimeout(function(){
		initLayout();
		initUI();
		
		// navTab styles
		var jTabsPH = $("div.tabsPageHeader");
		jTabsPH.find(".tabsLeft").hoverClass("tabsLeftHover");
		jTabsPH.find(".tabsRight").hoverClass("tabsRightHover");
		jTabsPH.find(".tabsMore").hoverClass("tabsMoreHover");
	
	}, 10);

}
function initLayout(){
	var a=$(window).width();
	var b=DWZ.ui.sbar;
	var c=$("#leftside").width();
	if(c>0){
		var iContentW = $(window).width() - (DWZ.ui.sbar ? $("#leftside").width() + 20 : 34) - 5;
	}else{
		var iContentW = $(window).width() - (DWZ.ui.sbar ? $("#leftside").width() + 0 : 34) - 5;
	}
	var iContentH = $(window).height() - $("#header").height() - $('#container .tabsPageHeader').height()-4;

	$("#container").width(iContentW);
	$("#container .tabsPageContent").height(iContentH - 14).find("[layoutH]").layoutH();
	$("#sidebar, #sidebar_s .collapse, #splitBar, #splitBarProxy").height(iContentH - 5);
	$("#taskbar").css({top: iContentH + $("#header").height() + 5, width:$(window).width()});
}

function initUI(_box){
	var $p = $(_box || document);

	$("div.panel", $p).jPanel();

	//tables
	$("table.table", $p).jTable();
	
	// css tables
	//没用到$('table.list', $p).cssTable();

	//auto bind tabs
	$("div.tabs", $p).each(function(){
		var $this = $(this);
		var options = {};
		options.currentIndex = $this.attr("currentIndex") || 0;
		options.eventType = $this.attr("eventType") || "click";
		$this.tabs(options);
	});

	//没用到$("ul.tree", $p).jTree();
	$('div.accordion', $p).each(function(){
		var $this = $(this);
		$this.accordion({fillSpace:$this.attr("fillSpace"),alwaysOpen:true,active:0});
	});

	$(":button.checkboxCtrl, :checkbox.checkboxCtrl", $p).checkboxCtrl($p);
	
	if ($.fn.combox) $("select.combox",$p).combox();
	
	/*//没用到
	 * if ($.fn.xheditor) {
		$("textarea.editor", $p).each(function(){
			var $this = $(this);
			var op = {html5Upload:false, skin: 'vista',tools: $this.attr("tools") || 'full'};
			var upAttrs = [
				["upLinkUrl","upLinkExt","zip,rar,txt"],
				["upImgUrl","upImgExt","jpg,jpeg,gif,png"],
				["upFlashUrl","upFlashExt","swf"],
				["upMediaUrl","upMediaExt","avi"]
			];
			
			$(upAttrs).each(function(i){
				var urlAttr = upAttrs[i][0];
				var extAttr = upAttrs[i][1];
				
				if ($this.attr(urlAttr)) {
					op[urlAttr] = $this.attr(urlAttr);
					op[extAttr] = $this.attr(extAttr) || upAttrs[i][2];
				}
			});
			
			$this.xheditor(op);
		});
	}*/
	
	if ($.fn.uploadify) {
		$(":file[uploaderOption]", $p).each(function(){
			var $this = $(this);
			var options = {
				fileObjName: $this.attr("name") || "file",
				auto: true,
				multi: true,
				onUploadError: uploadifyError
			};
			
			var uploaderOption = DWZ.jsonEval($this.attr("uploaderOption"));
			$.extend(options, uploaderOption);

			DWZ.debug("uploaderOption: "+DWZ.obj2str(uploaderOption));
			
			$this.uploadify(options);
		});
	}
	
	// init styles
	$("input[type=text], input[type=password], textarea", $p).addClass("textInput").focusClass("focus");

	$("input[readonly], textarea[readonly]", $p).addClass("readonly");
	$("input[disabled=true], textarea[disabled=true]", $p).addClass("disabled");

	$("input[type=text]", $p).not("div.tabs input[type=text]", $p).filter("[alt]").inputAlert();

	//Grid ToolBar
	$("div.panelBar li, div.panelBar", $p).hoverClass("hover");

	//Button
	$("div.button", $p).hoverClass("buttonHover");
	$("div.buttonActive", $p).hoverClass("buttonActiveHover");
	
	//tabsPageHeader
	$("div.tabsHeader li, div.tabsPageHeader li, div.accordionHeader, div.accordion", $p).hoverClass("hover");

	//validate form
	$("form.required-validate", $p).each(function(){
		var $form = $(this);
		$form.validate({
			onsubmit: false,
			focusInvalid: false,
			focusCleanup: true,
			errorElement: "span",
			ignore:".ignore",
			invalidHandler: function(form, validator) {
				var errors = validator.numberOfInvalids();
				if (errors) {
					var message = DWZ.msg("validateFormError",[errors]);
					alertMsg.error(message);
				} 
			}
		});
		
		$form.find('input[customvalid]').each(function(){
			var $input = $(this);
			$input.rules("add", {
				customvalid: $input.attr("customvalid")
			})
		});
	});

	if ($.fn.datepicker){
		$('input.date', $p).each(function(){
			var $this = $(this);
			var opts = {};
			if ($this.attr("dateFmt")) opts.pattern = $this.attr("dateFmt");
			if ($this.attr("minDate")) opts.minDate = $this.attr("minDate");
			if ($this.attr("maxDate")) opts.maxDate = $this.attr("maxDate");
			if ($this.attr("mmStep")) opts.mmStep = $this.attr("mmStep");
			if ($this.attr("ssStep")) opts.ssStep = $this.attr("ssStep");
			$this.datepicker(opts);
		});
	}
	
	// 关闭dialog
	$("button[class=close]",$p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			var dialog = $this.parents("div.dialog");
			$.pdialog.close(dialog);
			return false;
		});
	});
	
	
	//报表切换列表
	$("a[target=linePost]",$p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			var $panel = getCurrentPanel();
			var rel = $this.attr("rel");
			var aimObj = $("#"+rel,$panel);
			var $pagerForm = $("form",aimObj);
			var params = $this.attr("params");
			var vals = $this.attr("vals");
			if(params && $.trim(params)!="" && vals && $.trim(vals)!=""){
				if(params.indexOf(",")!=-1){
					var paramL = params.split(",");
					var valL = vals.split(",");
					for(var i=0;i<paramL.length;i++){
						var key = paramL[i];
						var val = valL[i];
						$("#"+key,$pagerForm).val(val);
					}
				}else{
						$("#"+params,$pagerForm).val(vals);
				}
			}
			$("#selectedObjectId",$pagerForm).val(0);
			//提交刷新
			//divSearch($pagerForm, rel);
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
			for ( var i = 0; i < args.length; i++) {
				var n = args[i].name;
				if(n == "selectedObjectId"){
					args.splice(i, 1);
				}
			}
			navTabPageBreak(args, rel);//刷新
			
		});
	});
	
	
	//给input加上清除缓存属性autocomplete="off"
	/*$("input",$p).each(function(){
		var $this = $(this);
		$this.attr("autocomplete","off");
	});*/
	$("input",$p).attr("autocomplete","off");
	
	

	// navTab
	$("a[target=navTab]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			var title = $this.attr("title") || $this.text();
			var tabid = $this.attr("rel") || "_blank";
			var fresh = eval($this.attr("fresh") || "true");
			var external = eval($this.attr("external") || "false");
			var dynamic = eval($this.attr("dynamic") || "false");
			var method = $this.attr("method") || "GET";//提交方式
			var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
			
			var $parent  =  getCurrentPanel();//取值区域
			var $selTr  = $("tr.selected:first",$parent)
			if($selTr.attr("title") && dynamic){
				title = $selTr.attr("title");//以选中TR的title为主
			}
			if($selTr.attr("cRel") && dynamic){
				tabid = $selTr.attr("cRel");//以选中TR的title为主
			}
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			var isOpen = true;
			if($this.attr("callBefore")){
				var callBefore = $this.attr("callBefore") ;
				if (! $.isFunction(callBefore)) callBefore = eval('(' + callBefore + ')');
				var opt = new Object();
				opt.url=url;
				opt.checkout=true; 
				opt.obj = $this;
				isOpen=callBefore(opt);
			}
			
			var data = {};
			var param = $this.attr("param");
			var verify = $this.attr("verify");//需要校验的参数
			var verifyText = $this.attr("verifyText");//校验文字
			if(param && $.trim(param)!=""){
				var jsonData={};//提交的参数Data
				var str="({";
				if(param.indexOf(",")!=-1){
					var params = param.split(",");
					for(var i=0;i<params.length;i++){
						var key = params[i];
						var val = $("input[id='"+params[i]+"']",$parent).val();
						if(key==verify&&(val==0||val=="")){
							alertMsg.error(verifyText);
							return false;
						}
						str += "'"+key+"':'"+val+"'";
						if(i!=params.length-1){
							str+=",";
						}
					}
				}else{
					var val = $("input[id='"+param+"']",$parent).val();
					if(param==verify&&(val==0||val=="")){
						alertMsg.error(verifyText);
						return false;
					}
					str+="'"+param+"':'"+val+"'";
				}
				str+="})";
				jsonData = eval(str);
				data = jsonData;
			}
			
			var serialize = eval($this.attr("serialize") || "false");//是否连载父页面的数据
			if(true==serialize){
				var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			    data = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
			}
			
			if(!isOpen)return false;
			navTab.openTab(tabid, url,{title:title,fresh:fresh,freshBtn:fresh ,external:external,data:data,method:method});

			event.preventDefault();
		});
	});

	//dialogs
	$("a[target=dialog]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			
			//是否执行后续操作
			if($this.attr("isExec")){
				var isExec=$this.attr("isExec").replaceTmById($(event.target).parents(".unitBox:first"));
				if(isExec=="false"){
					alertMsg.warn($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
			}
			//end
			
			//自定义函数校验
			var verifyFun=$this.attr("verifyFun");
			if(verifyFun){
				var flag=eval('(' + verifyFun + ')');//执行校验
				if(flag==false){
					return false;
				}
			}
			
			var title = $this.attr("caption") || $this.attr("title") || $this.text();
			var rel = $this.attr("rel") || "_blank";
			var options = {};
			var w = $this.attr("width");
			var h = $this.attr("height");
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
			options.type = $this.attr("type")|| "GET";
			
			options.aimTabId=$this.attr("aimTabId") || "";//设置目标Tab组件Id
			
			options.aimDlgId=$this.attr("aimDlgId") || "";//设置目标dialog组件Id
			options.aimWidth=$this.attr("aimWidth") || "";//设置目标dialog组件宽度
			options.aimHeight=$this.attr("aimHeight") || "";//设置目标dialog组件高度
			
			options.aimUrl=$this.attr("aimUrl") || "";//设置目标url
			options.aimTitle=$this.attr("aimTitle") || "";//设置目标标题
			
			options.aimDiv=$this.attr("aimDiv") || "";//设置目标Div的ID

			var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			
			
			//打开前验证
			var isOpen = true;
			if($this.attr("callBefore")){
				var callBefore = $this.attr("callBefore") ;
				if (! $.isFunction(callBefore)) callBefore = eval('(' + callBefore + ')');
				var opt = new Object();
				opt.url=url;
				if($this.attr("url"))opt.url=$this.attr("url");
				opt.checkout=true; 
				opt.obj = $this;
				isOpen=callBefore(opt);
			}
			if(!isOpen)return false;
			
			
			
			var $parent = getCurrentPanel();//取值区域
			/*var $parent  =  $.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
			if($parent.is(":hidden")){
				if($(".dialog").is(":visible")){
					$parent=$(".dialog");
				}else{
					$parent= navTab.getCurrentPanel();
				}
			};*/
			var param = $this.attr("param");
			var verify = $this.attr("verify");//需要校验的参数
			var verifyText = $this.attr("verifyText");//校验文字
			if(param && $.trim(param)!=""){
				var jsonData={};//提交的参数Data
				var str="({";
				if(param.indexOf(",")!=-1){
					var params = param.split(",");
					for(var i=0;i<params.length;i++){
						var key = params[i];
						var val = $("input[id='"+params[i]+"']",$parent).val();
						if(key==verify&&(val==0||val=="")){
							alertMsg.error(verifyText);
							return false;
						}
						str += "'"+key+"':'"+val+"'";
						if(i!=params.length-1){
							str+=",";
						}
					}
				}else{
					str+="'"+param+"':'"+$("[id='"+param+"']",$parent).val()+"'";
				}
				str+="})";
				jsonData = eval(str);
				options.jsonData = jsonData;
			}
			var serialize = eval($this.attr("serialize") || "false");//是否连载父页面的数据
			
			if(true==serialize){
				var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
				options.jsonData = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
			}
			var aimParam = $this.attr("aimParam")||false;
			if(aimParam){
				options.jsonData={'aimTabId':options.aimTabId,'aimUrl':options.aimUrl,'aimTitle':options.aimTitle};
			}
			$.pdialog.open(url, rel, title, options);
			
			return false;
		});
	});
	
	
	$("a[target=ajaxPost]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			//关联ID 参数
			var relevancyIds=$this.attr("relevancyIds");
			relevancyArea=getCurrentPanel();
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
				var str="({";
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
				str+="})";
				jsonData = eval(str);
			}
			
			$.ajax({
				type:'POST',
				url:$this.attr("href"),
				data:jsonData,
				cache: false,
				error: DWZ.ajaxError
			});
		});
	});
	
	$("a[target=ajax]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			var rel = $this.attr("rel");
			//选中上级左树
			var trel = $this.attr("trel");
			var type = $this.attr("type") || "GET";
			if(trel){
				$('#'+trel,getCurrentPanel()).treeSelectNote($this.attr("nodeId"));
			}//end
			
			//关联ID 参数
			var relevancyIds=$this.attr("relevancyIds");
//			var relevancyArea=$.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
//			if(relevancyArea.is(":hidden")){
//				relevancyArea= navTab.getCurrentPanel();
//			};
			
			relevancyArea=getCurrentPanel();
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
				var str="({";
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
				str+="})";
				jsonData = eval(str);
			}
			
			var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
			if (rel) {
				var $rel = $("#"+rel,getCurrentPanel());
				var serialize = eval($this.attr("serialize") || "false");//是否连载父页面的数据
				if(true==serialize){
					var $pagerForm = $("#pagerForm", $rel);
					jsonData = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
					for ( var i = 0; i < jsonData.length; i++) {
						var n = jsonData[i].name;
						if(n == "selectedObjectId"){
							jsonData.splice(i, 1);
						}
					}
				}
				$rel.loadUrl(url, jsonData, function(){
					$rel.find("[layoutH]").layoutH();
				},type);
			}
			var confirmTxt=$this.attr("confirmTxt");
			var callback=$this.attr("callback");
			if(confirmTxt){
				alertMsg.confirm(confirmTxt, {
					okCall: function(){
						if(callback){//有回调函数
							var $callback = callback || navTabAjaxDone;
							if (! $.isFunction($callback)) $callback = eval('(' + callback + ')');
							$.ajax({
								type:'POST',
								url:url,
								dataType:"json",
								data:jsonData,
								cache: false,
								success: $callback,
								error: DWZ.ajaxError
							});
						}
					}
				});
			}
           
			event.preventDefault();
		});
	});
	
	//根据状态改变提示语句
	$("a[target=statusToAjaxTodo]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			//关联ID 参数
			var relevancyIds=$this.attr("relevancyIds");
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				relevancyArea=getCurrentPanel();
				if(relevancyIds){
					var str="({";
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
					str+="})";
					jsonData = eval(str);
				}
			}
			
			var confirmTxt=$this.attr("confirmTxt");
			var statusToconfirmTxt=$this.attr("statusToconfirmTxt");
			
			//是否更换confirmTxt文字
			if($this.attr("status")){
				var status=$this.attr("status").replaceTmById($(event.target).parents(".unitBox:first"));
				if(status=="true" && statusToconfirmTxt){
					confirmTxt=statusToconfirmTxt;
				}
			}
			var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			
			var callback=$this.attr("callback");
			
			if(confirmTxt){
				alertMsg.confirm(confirmTxt, {
					okCall: function(){
							var $callback = callback || navTabAjaxDone;
							if (! $.isFunction($callback)) $callback = eval('(' + callback + ')');
							$.ajax({
								type:'POST',
								url:url,
								dataType:"json",
								data:jsonData,
								cache: false,
								success: $callback,
								error: DWZ.ajaxError
						});
					}
				});
			}
			event.preventDefault();
		});
	});
	
	
	// navTab
	$("div[target=navTab]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			var title = $this.attr("title") || $this.text();
			var tabid = $this.attr("rel") || "_blank";
			var fresh = eval($this.attr("fresh") || "true");
			var external = eval($this.attr("external") || "false");
			var url = unescape($this.attr("url")).replaceTmById($(event.target).parents(".unitBox:first"));
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			navTab.openTab(tabid, url,{title:title, fresh:fresh, external:external});

			event.preventDefault();
		});
	});
	
	$("button[target=checkedBack]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			
			var url = unescape($this.attr("url")).replaceTmById($(event.target).parents(".unitBox:first"));
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			var callBack = $this.attr("callback");
			
			//自定义函数校验
			var verifyFun=$this.attr("verifyFun");
			if(verifyFun){
				var flag=eval('(' + verifyFun + ')');//执行校验
				if(flag==false){
					return false;
				}
			}
			
			//关联ID 参数
			var relevancyIds=$this.attr("relevancyIds");
			var jsonData={};//提交的参数Data
			if(relevancyIds){
				relevancyArea=getCurrentPanel();
				if(relevancyIds){
					var str="({";
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
					str+="})";
					jsonData = eval(str);
				}
			}
			if (! $.isFunction(callBack)) var $callback = eval('(' + callBack + ')');
			$.ajax({
				type:'POST',
				url:url,
				data:jsonData,
				dataType:"json",
				cache: false,
				success:$callback,
				error: DWZ.ajaxError
			});
		});
	});
	
	
	//dialogs
	$("div[target=dialog]", $p).each(function(){
		$(this).click(function(event){
			var $this = $(this);
			
			//是否执行后续操作
			if($this.attr("isExec")){
				var isExec=$this.attr("isExec").replaceTmById($(event.target).parents(".unitBox:first"));
				if(isExec=="false"){
					return false;
				}
			}
			//end
			
			var title =$this.attr("caption") || $this.attr("title") || $this.text();
			var rel = $this.attr("rel") || "_blank";
			var options = {};
			var w = $this.attr("width");
			var h = $this.attr("height");
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
			options.type = $this.attr("type")|| "GET";
			
			options.aimTabId=$this.attr("aimTabId") || "";//设置目标Tab组件Id
			options.aimUrl=$this.attr("aimUrl") || "";//设置目标url
			options.aimTitle=$this.attr("aimTitle") || "";//设置目标标题
			options.jsonData={'aimTabId':options.aimTabId,'aimUrl':options.aimUrl,'aimTitle':options.aimTitle};
			var url = unescape($this.attr("url")).replaceTmById($(event.target).parents(".unitBox:first"));
			DWZ.debug(url);
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			$.pdialog.open(url, rel, title, options);
			
			return false;
		});
	});
	
	$("div.pagination", $p).each(function(){
		var $this = $(this);
		$this.pagination({
			targetType:$this.attr("targetType"),
			rel:$this.attr("rel"),
			totalCount:$this.attr("totalCount"),
			numPerPage:$this.attr("numPerPage"),
			pageNumShown:$this.attr("pageNumShown"),
			currentPage:$this.attr("currentPage")
		});
	});
	
	$(".tabsCloseAll", $p).each(function(){
		var $this = $(this);
		$this.click(function(){
			navTab.closeAllTab();
		});
	});

	//没用到if ($.fn.sortDrag) $("div.sortDrag", $p).sortDrag();

	// dwz.ajax.js
	if ($.fn.ajaxTodo) $("a[target=ajaxTodo]", $p).ajaxTodo();
	//没用到if ($.fn.dwzExport) $("a[target=dwzExport]", $p).dwzExport();

	if ($.fn.lookup) $("a[lookupGroup]", $p).lookup();
	if ($.fn.multLookup) $("[multLookup]:button", $p).multLookup();
	if ($.fn.suggest) $("input[suggestFields]", $p).suggest();
	//没用到if ($.fn.itemDetail) $("table.itemDetail", $p).itemDetail();
	if ($.fn.selectedTodo) $("a[target=selectedTodo]", $p).selectedTodo();
	if ($.fn.pagerForm) $("form[rel=pagerForm]", $p).pagerForm({parentBox:$p});
	
	// 这里放其他第三方jQuery插件...
	if($.fn.tableRowSort) $("a[target=tableRowSort]",$p).tableRowSort();
	if($.fn.tableRowSortSave) $("a[target=tableRowSortSave]",$p).tableRowSortSave();
	if($.fn.tree) $("ul[target=ztree]",$p).tree();
	if($.fn.tMenu) $("ul[target=tmenu]",$p).tMenu();
	//if($.fn.checkSpecificKey) $("input[type='text']",$p).checkSpecificKey(); //文本框禁止输入特殊字符
	if($.fn.aiovalidate) $("form.tableForm",$p).aiovalidate();
	if($.fn.showErrorMes) $("input.error",$p).showErrorMes();
	if($.fn.addTrEvent) $("a[target=addTr]",$p).addTrEvent();
	if($.fn.aioUpAjax) $("a[target=aioUpAjax]",$p).aioUpAjax();  //商品dialog点击上级
	if($.fn.checkedBack) $("a[target=checkedBack]",$p).checkedBack();//选中带回
	if($.fn.checkedBack) $("tr[target=checkedBack]",$p).checkedBack();//单据商品带回
	if($.fn.trOpenNavTab) $("tr[cTarget=openNavTab]",$p).trOpenNavTab();//TR打开一个NavTab
	if($.fn.liAjaxCheck) $("ul[target=ajax]",$p).liAjaxCheck();//ul li 选中事件
	if($.fn.cutRowShowOrHide) $("td[target=cutShow]",$p).cutRowShowOrHide();//单据配置列是否显示
	/*if($.fn.tableAjaxCheck) $("tbody[target=ajaxTodo]",$p).tableAjaxCheck();//table tr 选中事件*/
	//if($.fn.allCheck) $("input[name=allCheck]",$p).allCheck();//checkBox 全选事件   系统没有用到
    if($.fn.inputEnterCallBack) $("input.enter",$p).inputEnterCallBack();//input enter键查找回车带回
    if($.fn.inputLookEnterCallBack) $("input[lookName]",$p).inputLookEnterCallBack();//input查找回车带回
    if($.fn.buttonFormSubmit) $("button[type='button'].submit",$p).buttonFormSubmit();//button 按钮点击 form表单提交
	if($.fn.inputRadioAjax) $("input[type=radio][target=ajax]",$p).inputRadioAjax();//radio单选按钮提交
	if($.fn.inputCheckboxAjax) $("input[type=checkbox][target=ajax]",$p).inputCheckboxAjax();//checkbox复选按钮提交
	if($.fn.formSubmit) $("a[target=submit]",$p).formSubmit();//表单提交
	if($.fn.print) $("a[target=print]",$p).print();//打印插件
	
	
}


