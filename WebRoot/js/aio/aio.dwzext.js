//请使用  shift+cltr+/ 查看方法分布（切记别乱写）


/**
 * 当前活动面板
 * @return
 */
function getCurrentPanel(){
	var $Panel  =  $.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
	if($Panel.is(":hidden")){
		if($(".dialog").is(":visible")){
			$Panel=$(".dialog:visible");
			//拿取z-index 最大的当前面板
			if($Panel.length>1){
				var newPanel=$($Panel[0]);
				for ( var i = 0; i < $Panel.length; i++) {
					var max=parseFloat(newPanel.css("z-index"));
					var cur=parseFloat($($Panel[i]).css("z-index"));
					if(cur>max){
						newPanel=$($Panel[i]);
					}
				}
				$Panel=newPanel;
			}
		}else{
			$Panel= navTab.getCurrentPanel();
		}
	};
	return $Panel;
}
/**
 * url属性relevancyIds 传参数换成data
 */
function setUrlParaToJsonData($this){
	//关联ID 参数
	var relevancyIds=$this.attr("relevancyIds");
	var relevancyArea=getCurrentPanel();//取值区域
	var jsonData={};//提交的参数Data
	if(relevancyIds){
		//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
		var str="({";
		relevancyIdsArray=relevancyIds.split(",");
		for(var i=0;i<relevancyIdsArray.length;i++){
			var key=relevancyIdsArray[i];
			var valObj=relevancyArea.find("#"+relevancyIdsArray[i]);
			var key=valObj.attr("name");
			var val=valObj.val();
			str+=key+":'"+val+"'";
			if(i!=relevancyIdsArray.length-1){
				str+=",";
			}
		}
		str+="})";
		jsonData = eval(str);
	}
	return jsonData;
}
/**
 * 按钮改变form的action提交地址
 * @param obj
 * @return
 */
function btnChangeFormAction(obj){
	var $obj=$(obj);
	var action=$obj.attr("action");
	var $form=$obj.parents("form");
	$form.attr("action",action);
	if (!$form.valid()) {
		return false;
	}
	$form.submit();
}
/**
 * option改变提交地址  不通用
 * @param termId
 * @return
 */
function submitChangeAction(termId){
		var formObj=$.pdialog.getCurrent().find("form");
		var formAction=formObj.attr("action");
		var action="";
		var termVal=formObj.find("#"+termId).val();
		if(termVal!="" && termVal){
			action=formAction.substring(0,formAction.lastIndexOf("/")+1)+"dialogSearch";
		}else{
			action=formAction.substring(0,formAction.lastIndexOf("/")+1)+"dialogList";
		}
		formObj.attr("action",action);
		formObj.submit();
}


/**
 * dwz.datepicker.js 选择日期年月日    回调
 * @return
 */
function datepickerCallBack(obj){
	var  currentTd = $(obj).parents("td:first");//当前Tr
	if(currentTd.attr("needEndDate")&&currentTd.attr("needEndDate")=="yes"){
		var  currentTr = $(obj).parents("tr:first");//当前Tr
		if($(obj)[0] && $(obj)[0].nodeName=="TR") currentTr = $(obj);
		var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
		if(!productId||productId==""){
			$(obj).remove();
			return;
		}
		currentTr.find("input[cname='produceDate']").val($(obj).val());
		var costArith = currentTr.find("input[cname='costArith']").val();//算法
		if(costArith&&costArith==4){
			var produceDate=currentTr.find("input[cname='produceDate']").val();//生产日期
			var validity = currentTr.find("input[cname='validity']").val();//有效天
			if(produceDate&&validity&&validity!=0){
				var now = new Date(produceDate);
		    	var year = now.getFullYear();
		    	var month = now.getMonth();
		    	var day = now.getDate();
		    	day = eval(day+parseInt(validity));
		    	now.setFullYear(year, month,day);
		    	year = now.getFullYear();
		    	month = now.getMonth()+1;
		    	day = now.getDate();
		    	if(month<10){
		    		month="0"+month;
		    	}
		    	if(day<10){
		    		day="0"+day;
		    	}
		    	currentTr.find("input[cname='produceEndDate']").val(year+"-"+month+"-"+day);
			}
		}
		showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
		return false;
	}
	
	var where = $(obj).attr("where");
	if(where&&where=="initProduct"){
		var $panel=getCurrentPanel();
		var produceDate = $("#produceDate",$panel).val();
		var validity = $("#validity",$panel).val();
		if(produceDate&&validity&&validity!=0){
			var now = new Date(produceDate);
	    	var year = now.getFullYear();
	    	var month = now.getMonth();
	    	var day = now.getDate();
	    	day = eval(day+parseInt(validity));
	    	now.setFullYear(year, month,day);
	    	year = now.getFullYear();
	    	month = now.getMonth()+1;
	    	day = now.getDate();
	    	if(month<10){
	    		month="0"+month;
	    	}
	    	if(day<10){
	    		day="0"+day;
	    	}
	    	$("#produceEndDate",$panel).val(year+"-"+month+"-"+day);
		}
		return false
	}
	
	
	
	var $panel=getCurrentPanel();
	var whichCallBack=$("#whichCallBack",$panel).val();
	if("cw_arapOverGetOrPayMoney"==whichCallBack){  //往来查询--》超期应收应付款
		var $parent= navTab.getCurrentPanel();
		var inputVal=$("#pageSearch",$parent).find("input[name='stopDate']").val();
		refreshCwArapOverGetOrPayMoney($parent,"stopDate",inputVal);
	}
}
/**
 * 把navtab  pagerForm  隐藏的值填充到dialog上去
 * @return
 */
function dialoagSubmitWithnavTabForm(rel){
	//navTab
	var $navTab=navTab.getCurrentPanel();
	var inputs=$("#pagerForm",$navTab).find("input[type='hidden']");
	//当前dialog 
	var $dialog=getCurrentPanel();
	var firstForm=$("form",$dialog);
	
	//把navtab  #pagerForm的input赋值到dialog  提交的form中去
	$(inputs).each(function(key,input){
		if(!areaHasSameInputName(firstForm,$(input).attr("name"))){
			var inp="<input type='hidden' name='"+$(input).attr("name")+"' value='"+$(input).val()+"'>";
			$(firstForm).append(inp);
		}
	});
	//firstForm.submit();
	$.pdialog.close($dialog);
	navTab.reload($(firstForm).attr('action'), {data: $(firstForm).serializeArray()});
}
/**
 * 在指区域 查找是否有跟自己一样名字的input
 * @param area
 * @param inputName
 * @return
 */
function areaHasSameInputName(area,inputName){
	var re=$(area).find('input[name='+"'"+inputName+"'"+']');
	if(re.length<1){
		return undefined;
	}else{
		return re;
	}
}

/**
 * 同步请求处理div上的局部查询, 会重新载入指定div
 * @param {Object} form
 */
function notAsyncDivSearch(form, rel){
	var $form = $(form);
	if (form[DWZ.pageInfo.pageNum]) form[DWZ.pageInfo.pageNum].value = 1;
	if (rel) {
		var $box = $("#" + rel);
		$box.ajaxUrl({
			type:"POST", url:$form.attr("action"), data: $form.serializeArray(), async:false, callback:function(){
				$box.find("[layoutH]").layoutH();
			}
		});
	}
	return false;
}
/**
 * dialog 查询刷新Div
 * @param form
 * @param divId
 * @return
 */
function aioDivFilter(form,divId){
	$.pdialog.close($.pdialog.getCurrent());
	divSearch(form,divId);
	return false;
}
/**
 * dialog 查询刷新Div
 * @param form
 * @param divId
 * @return
 */
function aioDivRefresh(form,divId){
	$.pdialog.close($.pdialog.getCurrent());
	var $parent = getCurrentPanel();
	var $form = $("#pagerForm",$parent)
	var data = $(form).serializeArray();
	for(var i=0;i< data.length;i++){
		$("[name='"+data[i].name+"']",$form).val(data[i].value);
	}
	divSearch($form,divId);
	return false;
}



/**
 * 执行ajax请求的回调不刷新控件
 * @param json
 * @return
 */
function ajaxPostCallBack(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.isClose!="no"){
			$.pdialog.closeCurrent();
		}
		if(json.msg){
			alertMsg.info(json.msg);
		}
	}else{
		if(json.msg){
			alertMsg.warn(json.msg);
		}
	}
}

function iframeAjaxCallBack(txt){
	DWZ.ajaxDone(txt);
	var statusCode= txt.substring(txt.indexOf("[")+1,txt.indexOf("]"));
	var msg = txt.substring(txt.indexOf("]")+1,txt.indexOf("</pre>"));
	if (statusCode == DWZ.statusCode.ok){
		$.pdialog.closeCurrent();
		if(msg){
			alertMsg.info(msg);
		}
	}else{
		if(msg){
			alertMsg.warn(msg);
		}
	}
}
/**
 * 刷新DIV
 * @param json
 * @return
 */
function reloadNavTabDiv(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.url){
			var url=json.url;
			var rel = json.rel;
			var options={};
			var $parent = getCurrentPanel();
			var $form = $parent.find("#pagerForm");
			if($form){
				options = $form.serializeArray();
			}
			var $box = $("#" + rel,getCurrentPanel());
			$box.ajaxUrl({
				type:"POST", url:url, data: options, callback:function(){
					$box.find("[layoutH]").layoutH();
				}
			});
		}
		if(json.message){
			alertMsg.correct(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.timeout){
		if(json.message){
			alertMsg.warn(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.error){
		var msg=json.message||"异常！"
		alertMsg.error(msg);
	}
}
/**
 * dialog执行操作刷新dialog
 * @return
 */
function dialogReloadDialog(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.isClose!="no"){
			$.pdialog.closeCurrent();
		}
		if(json.url){
			var url=json.url;
			var rel = json.rel;
			var options={};
			var isTrigger = json.isTrigger;
			if(rel){
				var $box = $("#" + rel,getCurrentPanel());
				$box.ajaxUrl({
					type:"POST", url:url, data: options, callback:function(){
						$box.find("[layoutH]").layoutH();
						if(isTrigger){
							$("tr.selected",$box).click();
						}
					}
				});
			}else{
				var options={};
				options.data=json.data;
				$.pdialog.reload(url, options);
			}
			
		}else{
			var isTrigger = json.isTrigger;
			var rel = json.rel;
			if(rel){
				var $box = $("#" + rel,getCurrentPanel());
				if(isTrigger){
					$("tr.selected",$box).click();
				}
			}
		}
		if(json.isCloseType){
			if(json.isCloseType=="init"){
				navTab.closeTab("productInitView");
				navTab.closeTab("unitInitView");
				navTab.closeTab("bankInitView");
				navTab.closeTab("assetsInitView");
				navTab.closeTab("financeInitView");
			}
		}
		if(json.message){
			alertMsg.correct(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.timeout){
		if(json.confirmMsg){
			alertMsg.confirm(json.confirmMsg || DWZ.msg("forwardConfirmMsg"), {
				okCall: function(){
					var $panel = getCurrentPanel();
					var $input = $("input[name='isOption']",$panel);
					$input.val(false);
					var $form = $("form:first",$panel);
					$form.submit();
				},
				cancelCall: function(){
					return false;
				}
			});
			return false;
		}
		if(json.message){
			alertMsg.warn(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.error){
		var msg=json.message||"异常！"
		alertMsg.error(msg);
	}
}

/**
 * 处理div上的局部查询, 会重新载入指定div
 * @param {Object} form
 */
function dialogTreeAjaxDone(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		//更新右边
		if(json.rel){
			var $parent = navTab.getCurrentPanel();
			var $box = $parent.find("#" + json.rel);
			var $pagerForm = $("#pagerForm", $parent);
			//分类    自动跳转最后一页并选中
			if(json.opterate=="showLastPage"){   //空白新增，  复制新增完， 显示最后一页
				//获得最后一页分页
				$("#showLastPage",$pagerForm).val("true");
				$("#selectedObjectId",$pagerForm).val(json.selectedObjectId);
			}
			//end 分类    自动跳转最后一页并选中
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			
			$box.ajaxUrl({
				type:"POST", url:json.url, data:args, callback:function(){
					$box.find("[layoutH]").layoutH();
				}
			});
		}
		if(json.code){
			$("input[id='code']").val(json.code);
		}
		//更新左边
		if(json.trel){
			$('#'+json.trel).treeAddNode(json.pnode);
		}
		if ("closeCurrent" == json.callbackType) {
			$.pdialog.closeCurrent();
		}
		
	}
}

/**
 * 删除树和更新列表 
 * @param json
 * @return
 */
function removeTreeAjaxTodo(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		var $parent = navTab.getCurrentPanel();
		//更新右边
		if (json.navTabId){ //把指定navTab页面标记为需要“重新载入”。注意navTabId不能是当前navTab页面的
			navTab.reloadFlag(json.navTabId);
		} else { //重新载入当前navTab页面
			//更新右边为父级
			if(json.url){
				var $box = $parent.find("#" + json.rel);
				var $pagerForm = $("#pagerForm", $parent);
				//删除 选中上级
				if(json.selectedObjectId){
					$("#selectedObjectId",$pagerForm).val(json.selectedObjectId);
				}
				//end 删除 选中上级
				var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
				$box.ajaxUrl({
					type:"POST", url:json.url, data:args, callback:function(){
						$box.find("[layoutH]").layoutH();
					}
				});
			}else{
				var $pagerForm = $("#pagerForm", $parent);
				var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
				navTabPageBreak(args, json.rel);
			}
			
		}
		
		if ("closeCurrent" == json.callbackType) {
			setTimeout(function(){navTab.closeCurrentTab(json.navTabId);}, 100);
		} else if ("forward" == json.callbackType) {
			navTab.reload(json.forwardUrl);
		} else if ("forwardConfirm" == json.callbackType) {
			alertMsg.confirm(json.confirmMsg || DWZ.msg("forwardConfirmMsg"), {
				okCall: function(){
					navTab.reload(json.forwardUrl);
				},
				cancelCall: function(){
					navTab.closeCurrentTab(json.navTabId);
				}
			});
		}else {
			$parent.find(":input[initValue]").each(function(){
				var initVal = $(this).attr("initValue");
				$(this).val(initVal);
			});
		}
		if(json.trel){
			$('#'+json.trel).treeRemoveNode(json.pnode);
		}
		
	}
}

/**
 * 更新树和列表
 * @param json
 * @return
 */
function updateTodoAndTee(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		//更新右边
		if(json.rel){
			var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			if(json.selectedObjectId){   //修改成功选中该对象
				$("#selectedObjectId",$pagerForm).val(json.selectedObjectId);
			}
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			navTabPageBreak(args, json.rel);
		}
		//更新左边
		if(json.trel){
			$('#'+json.trel).treeUpdateNode(json.node);
		}
		if ("closeCurrent" == json.callbackType) {
			$.pdialog.closeCurrent();
		}
	}
}



//重写      dialogAjaxDone回调函数
function aioDialogAjaxDone(json){
	if(json.reset=="reset"){  //空白新增   清空        1.清空里面的值（控件太多用这种比较好）   2.有些对象要重新赋值
		//open:function(url, dlgid, title, options) {
		$.pdialog.open(json.base+json.loadDialogUrl,json.dialogId);
	}else{
		//自己扩展
	}
	
	if(json.opterate=="showLastPage"){   //空白新增，  复制新增完， 显示最后一页
		//获得最后一页分页
		var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
		$("#showLastPage",$pagerForm).val("true");
		$("#selectedObjectId",$pagerForm).val(json.selectedObjectId);
		
	}
	dialogAjaxDone(json);  //调用dwz本身
}


/**
 * 锁屏只关闭自己，不刷新其它的控件
 * @param json
 * @return
 */
function suoPingCallBark(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if ("closeCurrent" == json.callbackType) {
			//$.pdialog.closeCurrent();
			var dialog = getCurrentPanel();
			if(dialog){
				$.pdialog.close(dialog);
			}
		}
		if(json.isSP=="false" || json.isSP==false){
			sysPant();
		}
	}
}


//dialog刷新option
function aioDialogReloadOption(json){
	if(json.message){
		alertMsg.warn(json.message);
		return false;
	}
	if(json.reset=="reset"){  //空白新增   清空        1.清空里面的值（控件太多用这种比较好）   2.有些对象要重新赋值
		var options = {async:false,opterate:json.opterate,selectedObjectId:json.selectedObjectId};
		$.pdialog.open(json.base+json.loadDialogUrl,json.dialogId,"新增",options);
	}
}


//dialog回调打开或重载NavTab
function aioDialogOpenNavTab(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		var tabid = json.aimTabId;
		var url = json.aimUrl;
		var title = json.aimTitle;
		var fresh = true;//是否新的
		var external = false;//是否iframe
		var data=json.data;
		
		if(json.data){
			setTimeout(function(){$.pdialog.closeCurrent();}, 100);
			navTab.openTab(tabid, url,{title:title,data:data, fresh:fresh, external:external,method:"POST"});
		}else {
			alertMsg.error("提交参数异常！");
			return false;
		}
	}
}

//dialog回调打开或重载新的dialog
function aioDialogOpenDialog(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		$.pdialog.closeCurrent();
		if(json.data){
			var url=json.aimUrl;
			var dlgId=json.aimDlgId;
			var dlgTitle=json.aimTitle;
			var options={};
			options.width=json.aimWidth;
		    options.height=json.aimHeight;
		    options.jsonData=json.data;	   
			$.pdialog.open(url, dlgId, dlgTitle, options);
		}else {
			alertMsg.error("提交参数异常！");
			return false;
		}
	}
}


//dialog提交参数查询刷新NavTab
function aioDialogSearchNavTab(json) {
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		var tabid = json.aimTabId;
		var url = json.aimUrl;
		var data=json.data;
		
		if(json.data){
			setTimeout(function(){$.pdialog.closeCurrent();}, 100);
			navTab.reload(url,{data:data, navTabId:tabid, callback:null,method:"POST"});
		}else {
			alertMsg.error("提交参数异常！");
			return false;
		}
		
	}
}

//重新请求Form
function aioReloadForm(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if($.pdialog.getCurrent()){
			$.pdialog.closeCurrent();
		}
		
		
		if(json.rel){
			var $pagerForm = $("#pagerForm", getCurrentPanel().find("#"+json.rel));
			if(json.data){
				var obj=json.data;
				
				$.each(obj,function(i){  
				    var key = i;  
				    var value = obj[i];  
				    $("input[id='"+key+"']",$pagerForm).val(value);
				}); 
			}
			
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			navTabPageBreak(args, json.rel);
		}
		if(json.message){
			alertMsg.info(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.timeout){
		if(json.message){
			alertMsg.warn(json.message);
		}
	}else if(json.statusCode == DWZ.statusCode.error){
		var msg=json.message||"异常！";
		alertMsg.error(msg);
	}
}

//dialog提交
function aioDialogPostCallBreak(json){
	if(json.aimTabId && json.aimTabId!=""){
		aioDialogOpenNavTab(json);
	}else if (json.aimDlgId && json.aimDlgId!="") {
		aioDialogOpenDialog(json);
	}else if (json.rel && json.rel!="") {
		aioReloadForm(json);
	}
}




//表单提交验证不通过弹出dialog 
function navTabAjaxDoneDialog(json){
	DWZ.ajaxDone(json);
	
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.printType){//单据保存过账后打印
			afterSavePrint(json);
			return false;
		}
		if(json.message){
			alertMsg.info(json.message);
		}
		if(json.isClose){//单据保存是否关闭
			navTab.closeCurrentTab(json.navTabId,false);
			return false;
		}
		
		if ("closeCurrent" == json.callbackType) {
			navTab.closeCurrentTab(json.navTabId,false);
			//草稿批量过账ajax请求下面的草稿
			if(json.draftStrs){
				var data = {checkObjIds:json.draftStrs};
				navTab.openTab("_bank", projectBasePath+"/common/bill/editDraftBill",{title:"批量过账",data:data,method:"post"});
			}
		} else if ("forward" == json.callbackType) {
			navTab.reload(json.forwardUrl);
		} else if ("forwardConfirm" == json.callbackType) {
			alertMsg.confirm(json.confirmMsg || DWZ.msg("forwardConfirmMsg"), {
				okCall: function(){
					navTab.reload(json.forwardUrl);
				},
				cancelCall: function(){
					navTab.closeCurrentTab(json.navTabId);
				}
			});
		}else {
			navTab.getCurrentPanel().find(":input[initValue]").each(function(){
				var initVal = $(this).attr("initValue");
				$(this).val(initVal);
			});
		}
		
		if (json.navTabId){ //把指定navTab页面标记为需要“重新载入”。注意navTabId不能是当前navTab页面的
			navTab.reloadFlag(json.navTabId);
		}else{ //重新载入当前navTab页面
			var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			navTabPageBreak(args, json.rel);
		}
		
		if(json.dialog){  //用于navtab表单提交       验证不通过打开一个dialog提示信息   eg:销售单保存商品数量不够提示负库存
			var url=json.url;
			var dialogId=json.dialogId;
			var dialogTitle=json.dialogTitle;
			var options={};
			options.width=json.width;
		    options.height=json.height;
		    options.param=json.params;
		    options.jsonData=json.jsonData;
		    options.type= json.type || "post";
			$.pdialog.open(url, dialogId, dialogTitle, options);
		}
		
		if(json.title){
			alertMsg.confirm(json.title, {
				okCall: function(){
					var $parent = navTab.getCurrentPanel();
					$("#"+json.formId,$parent).submit();
				}
			});
		}
		
		if(json.unitFlag!=undefined&&!json.unitFlag&&json.dialog){          //提示单位+成本输入
			alertMsg.confirm(json.confirmMsg || DWZ.msg("forwardConfirmMsg"), {
				okCall: function(){
					if(json.dialog){  //用于navtab表单提交       验证不通过打开一个dialog提示信息   eg:销售单保存商品数量不够提示负库存
						var url=json.url;
						var dialogId=json.dialogId;
						var dialogTitle=json.dialogTitle;
						var options={};
						options.width=json.width;
					    options.height=json.height;
					    options.param=json.params;
					    options.jsonData=json.jsonData;	   
						$.pdialog.open(url, dialogId, dialogTitle, options);
				    }
				}
			});
		}else if(json.unitFlag!=undefined&&!json.unitFlag&&!json.dialog){   //提示单位
			alertMsg.confirm(json.confirmMsg || DWZ.msg("forwardConfirmMsg"), {
				okCall: function(){
					var $parent = navTab.getCurrentPanel();
			    	/*var hasComfirmInp=$("#priceStockHasComfirmId",$parent);
					hasComfirmInp.val("has");
					$("#qtjhd_otherinForm",$parent).submit();*/
					var hasComfirmInp=$("#"+json.stockHasComfirmId,$parent);
					hasComfirmInp.val("has");
					$("#"+json.fromId,$parent).submit();
				}
			});
		}else if(json.unitFlag!=undefined&&json.unitFlag&&json.dialog){     //成本输入
			if(json.dialog){  //用于navtab表单提交       验证不通过打开一个dialog提示信息   eg:销售单保存商品数量不够提示负库存
				var url=json.url;
				var dialogId=json.dialogId;
				var dialogTitle=json.dialogTitle;
				var options={};
				options.width=json.width;
			    options.height=json.height;
			    options.param=json.params;
			    options.jsonData=json.jsonData;
			    options.type="post";
				$.pdialog.open(url, dialogId, dialogTitle, options);
		    }
		}
		
	}else{
		if(json.reloadCodeTitle){
			alertMsg.confirm(json.reloadCodeTitle, {
				okCall: function(){
					var $parent = navTab.getCurrentPanel();
					var $form = $("#"+json.formId,$parent);
					if($.isEmptyObject($form) || $form.length==0)$form=$parent.find("form");
					var $billCode = $("#billCode",$form);
					var $billNum = $("#billNum",$form);
					var $showBillCode = $("#showBillCode",$form);
					var billTypeId = json.billTypeId;
					
					$.ajax({
						type: 'POST',
						url:projectBasePath+"/sys/billCodeConfig/reloadAutoCode",
						data:{'billTypeId':billTypeId},
						dataType:"json",
						async: false,
						cache: false,
						success: function(json){
							$billCode.val(json.code);
							$billNum.val(json.codeIncrease);
							$showBillCode.val(json.code);
							if(json.codeAllowEdit=="1" || json.codeAllowEdit==1){
								$showBillCode.attr("readonly","readonly");
							}
							draftPostAutoSubmit();
						}
					});
					
//					var strCode = $billCode.val();
//					var comStr = strCode.substring(0,strCode.lastIndexOf("-"));
//					var numStr = strCode.substring(strCode.lastIndexOf("-")+1,strCode.length);
//					var len = numStr.length;
//					var num = parseInt(numStr)+1+"";
//					len = len - num.length;
//					var subStr = "";
//					for(var i = 0;i<len;i++){
//						subStr += "0";
//					}
//					numStr = subStr+num;
//					var newCode = comStr+"-"+numStr;
//					
//					$billCode.val(newCode);
//					$billNum.val(parseInt($billNum.val())+1);
//					$showBillCode.val(newCode);
				}
			});
		}
	}
}





/**
 * navtab 局部刷新table
 * @return
 */
function aioNavtabPartRefresh(rel,url,data){
	var $parent =navTab.getCurrentPanel();
	var $box = $parent.find("#" + rel);
	$box.ajaxUrl( {
		type : "POST",
		url : url,
		data : data,
		callback : function() {
			$box.find("[layoutH]").layoutH();
		}
	});
}

function dialogPartTrigger(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.isClose!="no"){
			$.pdialog.closeCurrent();
		}
		if(json.message){
			alertMsg.correct(json.message);
		}
	}
	var $parent = getCurrentPanel();
	var $ul  = $parent.find("ul[target='ajax']");
	$ul.each(function(){
		var $li  = $(this).children(".selected:first");
		$li.children().click();
	});
}