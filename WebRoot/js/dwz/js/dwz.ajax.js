/**
 * @author ZhangHuihua@msn.com
 * 
 */

/**
 * 普通表单提交
 */
function formCallback(form, callback) {
	var $form = $(form);
	$.ajax({
		type: form.method || 'POST',
		url:$form.attr("action"),
		data:$form.serializeArray(),
		dataType:"json",
		cache: false,
		success: callback || DWZ.ajaxDone,
		error: DWZ.ajaxError
	});
	return false;
}

/**
 * 普通ajax表单提交
 * @param {Object} form
 * @param {Object} callback
 * @param {String} confirmMsg 提示确认信息
 */
function validateCallback(form, callback, confirmMsg) {
	var $form = $(form);

	if (!$form.valid()) {
		return false;
	}
	
	var _submitFn = function(){
		$.ajax({
			type: form.method || 'POST',
			url:$form.attr("action"),
			data:$form.serializeArray(),
			dataType:"json",
			cache: false,
			success: callback || DWZ.ajaxDone,
			error: DWZ.ajaxError
		});
	}
	
	if (confirmMsg) {
		alertMsg.confirm(confirmMsg, {okCall: _submitFn});
	} else {
		_submitFn();
	}
	
	return false;
}
/**
 * 带文件上传的ajax表单提交
 * @param {Object} form
 * @param {Object} callback
 */
function iframeCallback(form, callback){
	var $form = $(form), $iframe = $("#callbackframe");
	if(!$form.valid()) {return false;}

	if ($iframe.size() == 0) {
		$iframe = $("<iframe id='callbackframe' name='callbackframe' src='about:blank' style='display:none'></iframe>").appendTo("body");
	}
	if(!form.ajax) {
		$form.append('<input type="hidden" name="ajax" value="1" />');
	}
	form.target = "callbackframe";
	
	_iframeResponse($iframe[0], callback || DWZ.ajaxDone);
}
function _iframeResponse(iframe, callback){
	var $iframe = $(iframe), $document = $(document);
	
	$document.trigger("ajaxStart");
	
	response = $iframe.contents().find("body").text();
	if(response==""){
		$document.trigger("ajaxStop");
	}
	
	$iframe.bind("load", function(event){
		$iframe.unbind("load");
		$document.trigger("ajaxStop");
		
		if (iframe.src == "javascript:'%3Chtml%3E%3C/html%3E';" || // For Safari
			iframe.src == "javascript:'<html></html>';") { // For FF, IE
			return;
		}

		var doc = iframe.contentDocument || iframe.document;

		// fixing Opera 9.26,10.00
		if (doc.readyState && doc.readyState != 'complete') return; 
		// fixing Opera 9.64
		if (doc.body && doc.body.innerHTML == "false") return;
	   
		var response;
		
		if (doc.XMLDocument) {
			// response is a xml document Internet Explorer property
			response = doc.XMLDocument;
		} else if (doc.body){
			try{
				response = $iframe.contents().find("body").text();
				response = jQuery.parseJSON(response);
			} catch (e){ // response is html document or plain text
				response = doc.body.innerHTML;
			}
		} else {
			// response is a xml document
			response = doc;
		}
		
		callback(response);
	});
	
}

/**
 * navTabAjaxDone是DWZ框架中预定义的表单提交回调函数．
 * 服务器转回navTabId可以把那个navTab标记为reloadFlag=1, 下次切换到那个navTab时会重新载入内容. 
 * callbackType如果是closeCurrent就会关闭当前tab
 * 只有callbackType="forward"时需要forwardUrl值
 * navTabAjaxDone这个回调函数基本可以通用了，如果还有特殊需要也可以自定义回调函数.
 * 如果表单提交只提示操作是否成功, 就可以不指定回调函数. 框架会默认调用DWZ.ajaxDone()
 * <form action="/user.do?method=save" onsubmit="return validateCallback(this, navTabAjaxDone)">
 * 
 * form提交后返回json数据结构statusCode=DWZ.statusCode.ok表示操作成功, 做页面跳转等操作. statusCode=DWZ.statusCode.error表示操作失败, 提示错误原因. 
 * statusCode=DWZ.statusCode.timeout表示session超时，下次点击时跳转到DWZ.loginUrl
 * {"statusCode":"200", "message":"操作成功", "navTabId":"navNewsLi", "forwardUrl":"", "callbackType":"closeCurrent", "rel"."xxxId"}
 * {"statusCode":"300", "message":"操作失败"}
 * {"statusCode":"301", "message":"会话超时"}
 * 
 */
function navTabAjaxDone(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if(json.printType){//单据保存过账后打印
			afterSavePrint(json);
			return false;
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
					navTab.closeCurrentTab(json.navTabId,false);
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
		} else { //重新载入当前navTab页面
			var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			navTabPageBreak(args, json.rel);
		}
	}
}

/**
 * dialog上的表单提交回调函数
 * 当前navTab页面有pagerForm就重新加载
 * 服务器转回navTabId，可以重新载入指定的navTab. statusCode=DWZ.statusCode.ok表示操作成功, 自动关闭当前dialog
 * 
 * form提交后返回json数据结构,json格式和navTabAjaxDone一致
 */
function dialogAjaxDone(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if (json.navTabId){
			navTab.reload(json.forwardUrl, {navTabId: json.navTabId});
		} else {
			var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
			navTabPageBreak(args, json.rel);
		}
		if(json.code){
			$("input[id='code']").val(json.code);
		}
		if ("closeCurrent" == json.callbackType) {
			//$.pdialog.closeCurrent();
			var dialog = getCurrentPanel();
			if(dialog){
				$.pdialog.close(dialog);
			}
		}else if("refreshCurrent" == json.callbackType){
			$.pdialog.refreshCurrent();
		}
		
		if(json.isSP=="false" || json.isSP==false){
			sysPant();
		}
	}
}
function dialogAjaxDone1(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if (json.navTabId){
			navTab.reload(json.forwardUrl, {navTabId: json.navTabId});
		} else{
		}
		
		if (json.navTabForm){
			var $pagerForm = $("#pagerForm", getCurrentPanel());
			navTab.reload(json.forwardUrl, {navTabId: json.navTabId,data: $pagerForm.serializeArray()});
		}
		
		if(json.code){
			$("input[id='code']").val(json.code);
		}
		if ("closeCurrent" == json.callbackType) {
			$.pdialog.closeCurrent();
		}else if("refreshCurrent" == json.callbackType){
			$.pdialog.refreshCurrent();
		}else if("closeAll" == json.callbackType){
			$.pdialog.closeCurrent();
			navTab.closeAllTab();
		}
		
		if("closeCurentRefreshNext" == json.callbackType1){
			var cDialog = getCurrentPanel();
			var url = $(cDialog).data("url");
			var dlgid = $(cDialog).data("id");
			$.pdialog.open(url, dlgid, "", {});
		}
		
		if("refreshDialogByPara"==json.callbackType1){  //例如：单据附件删除重新获取 pageForm数据
			var cDialog = getCurrentPanel();
			var url = $(cDialog).data("url");
			var dlgid = $(cDialog).data("id");
			if(json.selectedObjectId){
				$("#selectedObjectId",$("#pagerForm", getCurrentPanel())).val(json.selectedObjectId);
			}
			var $pagerForm = $("#pagerForm", getCurrentPanel());
			var options={};
			options.jsonData = $pagerForm.serializeArray();
			$.pdialog.open(url, dlgid, "", options);
		}
	}
}

/**
 * 处理navTab上的查询, 会重新载入当前navTab
 * @param {Object} form
 */
function navTabSearch(form, navTabId){
	var $form = $(form);
	if (form[DWZ.pageInfo.pageNum]) form[DWZ.pageInfo.pageNum].value = 1;
	navTab.reload($form.attr('action'), {data: $form.serializeArray(), navTabId:navTabId});
	return false;
}
/**
 * 处理dialog弹出层上的查询, 会重新载入当前dialog
 * @param {Object} form
 */
function dialogSearch(form){
	var $form = $(form);
	if (form[DWZ.pageInfo.pageNum]) form[DWZ.pageInfo.pageNum].value = 1;
	$.pdialog.reload($form.attr('action'), {data: $form.serializeArray()});
	return false;
}
function dwzSearch(form, targetType){
	if (targetType == "dialog") dialogSearch(form);
	else navTabSearch(form);
	return false;
}
/**
 * 处理div上的局部查询, 会重新载入指定div
 * @param {Object} form
 */
function divSearch(form, rel){
	var $form = $(form);
	if (form[DWZ.pageInfo.pageNum]) form[DWZ.pageInfo.pageNum].value = 1;
	if (rel) {
		var $box = $("#" + rel);
		$box.ajaxUrl({
			type:"POST", url:$form.attr("action"), data: $form.serializeArray(), callback:function(){
				$box.find("[layoutH]").layoutH();
			}
		});
	}
	return false;
}
/**
 * 
 * @param {Object} args {pageNum:"",numPerPage:"",orderField:"",orderDirection:""}
 * @param String formId 分页表单选择器，非必填项默认值是 "pagerForm"
 */
function _getPagerForm($parent, args) {
	var form = $("#pagerForm", $parent).get(0);

	if (form) {
		if (args["pageNum"]) form[DWZ.pageInfo.pageNum].value = args["pageNum"];
		if (args["numPerPage"]) form[DWZ.pageInfo.numPerPage].value = args["numPerPage"];
		if (args["orderField"]) form[DWZ.pageInfo.orderField].value = args["orderField"];
		if (args["orderDirection"] && form[DWZ.pageInfo.orderDirection]) form[DWZ.pageInfo.orderDirection].value = args["orderDirection"];
		
		if (args["aheadDay"]) $(form).find("input[name=aheadDay]").val(args["aheadDay"])  ;
	}
	
	return form;
}


/**
 * 处理navTab中的分页和排序
 * targetType: navTab 或 dialog
 * rel: 可选 用于局部刷新div id号
 * data: pagerForm参数 {pageNum:"n", numPerPage:"n", orderField:"xxx", orderDirection:""}
 * callback: 加载完成回调函数
 */
function dwzPageBreak(options){
	var op = $.extend({ targetType:"navTab", rel:"", data:{pageNum:"", numPerPage:"", orderField:"", orderDirection:""}, callback:null}, options);
	var $parent = op.targetType == "dialog" ? $.pdialog.getCurrent() : navTab.getCurrentPanel();
	if($parent.is(":hidden")){
		$parent = getCurrentPanel();
	}

	if (op.rel) {
		var $box = $parent.find("#" + op.rel);
		var form = _getPagerForm($box, op.data);
		if (form) {
			$box.ajaxUrl({
				type:"POST", url:$(form).attr("action"), data: $(form).serializeArray(), callback:function(){
					$box.find("[layoutH]").layoutH();
				}
			});
		}
	} else {
		var form = _getPagerForm($parent, op.data);
		var params = $(form).serializeArray();
		
		if (op.targetType == "dialog") {
			if (form) $.pdialog.reload($(form).attr("action"), {data: params, callback: op.callback});
		} else {
			if (form) navTab.reload($(form).attr("action"), {data: params, callback: op.callback});
		}
	}
}
/**
 * 处理navTab中的分页和排序
 * @param args {pageNum:"n", numPerPage:"n", orderField:"xxx", orderDirection:""}
 * @param rel： 可选 用于局部刷新div id号
 */
function navTabPageBreak(args, rel){
	dwzPageBreak({targetType:"navTab", rel:rel, data:args});
}
/**
 * 处理dialog中的分页和排序
 * 参数同 navTabPageBreak 
 */
function dialogPageBreak(args, rel){
	dwzPageBreak({targetType:"dialog", rel:rel, data:args});
}


function ajaxTodo(url, callback, jsonData){
	var $callback = callback || navTabAjaxDone;
	if (! $.isFunction($callback)) $callback = eval('(' + callback + ')');
	var fullName = getCurrentPanel().find("#selectedObjectId").val();
	var data = {"fullName":fullName};
	if(jsonData && !$.isEmptyObject(jsonData)) data = jsonData;
	$.ajax({
		type:'POST',
		url:url,
		data: data,
		dataType:"json",
		cache: false,
		success: $callback,
		error: DWZ.ajaxError
	});
}

/**
 * http://www.uploadify.com/documentation/uploadify/onqueuecomplete/	
 */
function uploadifyQueueComplete(queueData){
	/*var msg = "The total number of files uploaded: "+queueData.uploadsSuccessful+"<br/>"
		+ "The total number of errors while uploading: "+queueData.uploadsErrored+"<br/>"
		+ "The total number of bytes uploaded: "+queueData.queueBytesUploaded+"<br/>"
		+ "The average speed of all uploaded files: "+queueData.averageSpeed;*/
	var msg = "上传成功数量: "+queueData.uploadsSuccessful;
	
	if (queueData.uploadsErrored) {
		alertMsg.error(msg);
	} else {
		alertMsg.correct(msg);
	}
}
/**
 * http://www.uploadify.com/documentation/uploadify/onuploadsuccess/
 */
function uploadifySuccess(file, data, response){
	//--------ulzm
	data=$.parseJSON(data);
	//str="<img style='height:97px; width:100%' src='"+data.host+"aioerpFilePath/"+data.savePath+"'/>";
	str="<img style='height:97px; width:100%' src='"+data.base+"/upLoadImg/"+data.savePath+"'/>";
	$("#b_showProductImg").html(str);             //显示图片
	$("#product_img_id").val(data.recordId);        //保存  ID
	$.pdialog.close("b_product_img");  //关闭dialog
	//initUI();
	//---------end ulzm
	
	
	//----------------原来
	//alert(data)
	//----------------end 原来
}

function uploadifySuccessOrder(file, data, response){
	$.pdialog.close("b_product_img");  //关闭dialog
	data=$.parseJSON(data);
	var panel = getCurrentPanel();
	var ids = $("#fuJianIds",panel).val();
	if(ids&&ids.length>0){
		ids=ids+","+data.id;
	}else{
		ids=data.id;
	}
	var options={};
	options.dialogId="order_fujianDialog_id";
	options.jsonData={tableId:data.tableId};
	$.pdialog.open(data.base+"/upload/toOrderFujianUpLoad/"+ids, "order_fujianDialog_id", "单据附件操作", options);
	//$.pdialog.reload(data.base+"/upload/toOrderFujianUpLoad/", options);
}

/**
 * http://www.uploadify.com/documentation/uploadify/onuploaderror/
 */
function uploadifyError(file, errorCode, errorMsg) {
	alertMsg.error(errorCode+": "+errorMsg);
}


/**
 * http://www.uploadify.com/documentation/
 * @param {Object} event
 * @param {Object} queueID
 * @param {Object} fileObj
 * @param {Object} errorObj
 */
function uploadifyError(event, queueId, fileObj, errorObj){
	alert("event:" + event + "\nqueueId:" + queueId + "\nfileObj.name:" 
		+ fileObj.name + "\nerrorObj.type:" + errorObj.type + "\nerrorObj.info:" + errorObj.info);
}


$.fn.extend({
	ajaxTodo:function(){
		return this.each(function(){
			var $this = $(this);
			$this.click(function(event){
				
				var alterExecText = $this.attr("alterExecText");//改变执行状态的confrim文本
				
				//是否执行后续操作
				if($this.attr("isExec")){
					var isExec=$this.attr("isExec").replaceTmById($(event.target).parents(".unitBox:first"));
					if(isExec=="false" && !alterExecText){
						alertMsg.warn($this.attr("warn") || DWZ.msg("alertSelectMsg"));
						return false;
					}
				}
				
				//自定义函数校验
				var verifyFun=$this.attr("verifyFun");
				if(verifyFun){
					var flag=eval('(' + verifyFun + ')');//执行校验
					if(flag==false){
						return false;
					}
				}
				
				var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
				DWZ.debug(url);
				if (!url.isFinishedTm()) {
					alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
				//传值
				var name = $this.attr("rname");
				var dialogId = $this.attr("dialogId");
				var parameters =  new Array();
				var $parent =  getCurrentPanel();
                if(dialogId){
                	$parent = $("body").data(dialogId);
				}
				if(name){
					$parent.find("input[name='"+name+"']:checked").each(function(){
						parameters.push($(this).val());
					});
					if(parameters.length<=0){
						alertMsg.error($this.attr("warn")
								|| DWZ.msg("alertSelectMsg"));
			            return false;
					}
					url = url+"-"+parameters.toString();
				}
				var title = $this.attr("title");
				
				//红冲是否提示选择
				var flag = true; 
				if($this.attr("isRCW")){
					var isRCW=$this.attr("isRCW").replaceTmById($(event.target).parents(".unitBox:first"));
					if(isRCW==0){
						flag = false;
					}
				}
				
				var jsonData={};//提交的参数Data
				var param = $this.attr("param");
				var verify = $this.attr("verify");//需要校验的参数
				var verifyText = $this.attr("verifyText");//校验文字
				if(param && $.trim(param)!=""){
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
				}
				
				if (title) {
					alertMsg.confirm(title, {
						okCall: function(){
							//改变执行状态的confrim文本
							if(alterExecText && flag==true){
								alertMsg.confirm(alterExecText, {
									okCall: function(){
										ajaxTodo(url, $this.attr("callback"));
									}
								});
							}else{
								ajaxTodo(url, $this.attr("callback"),jsonData);
							}
						}
					});
				} else {
					ajaxTodo(url, $this.attr("callback"));
				}
				event.preventDefault();
			});
		});
	},
	dwzExport: function(){
		function _doExport($this) {
			var $p = $this.attr("targetType") == "dialog" ? $.pdialog.getCurrent() : navTab.getCurrentPanel();
			var $form = $("#pagerForm", $p);
			var url = $this.attr("href");
			window.location = url+(url.indexOf('?') == -1 ? "?" : "&")+$form.serialize();
		}
		
		return this.each(function(){
			var $this = $(this);
			$this.click(function(event){
				var title = $this.attr("title");
				if (title) {
					alertMsg.confirm(title, {
						okCall: function(){_doExport($this);}
					});
				} else {_doExport($this);}
			
				event.preventDefault();
			});
		});
	}
});

