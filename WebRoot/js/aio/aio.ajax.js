//业务需要的ajax
//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*--------------登录------------*/
/**
 * 用户登录
 */
function userLogin(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		if ("forward" == json.callbackType) {
			location.href = projectBasePath + json.url;
		}else if("submit" == json.callbackType){
			var $form = $('#' + json.rel);
			$('#whichDbId', $form).val(json.whichDbIds);
			$form.data('oldurl', $form.attr('action'));
			$form.attr('action', projectBasePath + '/user/login');
			$form.submit();
			$form.attr('action',$form.data('oldurl'));
		}else if("dialog" == json.callbackType){
			var options = {};
			options.width = 240;
			options.height = 120;
			$.pdialog.open(projectBasePath+"/supAdmin/user/toWhickDbList?dbIdStrs="+json.whichDbIds, "toWhickDbListRel", "请选择账套", options);
		}
	}
}
/**
 *确定账套提交
 */
 function selectWhichDbAfter(){
 	var $dialog=getCurrentPanel();
 	var whichDbId=$("#loginWhichDbId",$dialog).val();
 	$.pdialog.close($dialog);
 	var whichDbObj=$("#whichDbId");
 	whichDbObj.val(whichDbId);
 	var $form=$(whichDbObj.parents("form"));
 	$form.data('oldurl', $form.attr('action'));
 	$form.attr("action",projectBasePath+"/user/login");
 	$form.submit();
 	$form.attr("action", $form.data('oldurl'));
 }
/*------------end-登录-------------*/

 
/*------------------------------------------基本信息-----------------------------------------------*/
/**
 * 基本信息过滤筛选内容
 */
function filter(json){
	DWZ.ajaxDone(json);
	if(json.close){
		 $.pdialog.close(json.close);  //关掉筛选dialog
	}
	if(json.rel){
		 $("#"+json.rel).find("form:first").attr("action",json.action);
	}
	if(json.attrId){
		$("#"+json.attrId).val(json.screenPara);
		$("#"+json.valId).val(json.screenVal);
    }

    navTabAjaxDone(json);

}
/**
 * 好像没有引用
 */
function search(json){
	DWZ.ajaxDone(json);
	if(json.close){
		 $.pdialog.close(json.close);  //关掉查询dialog
	}
	$panel = getCurrentPanel();
	if(json.rel){
		var $pagerForm = $("#pagerForm", $panel);
		$("#"+json.rel).find("form:first").attr("action",json.action);
	}
	$("#"+json.attrId,$panel).val(json.searchPara);
	$("#"+json.valId,$panel).val(json.searchVal);
	$("#"+json.sup,$panel).val(json.supId);
	$("#"+json.nodeId,$panel).val(json.node);
	
	var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
	navTabPageBreak(args, json.rel);

}
/**
 * 过滤筛选内容
 * @param json
 * @return
 */
function filter2(json){
	DWZ.ajaxDone(json);
	if(json.close){
		 $.pdialog.close(json.close);  //关掉筛选dialog
	}
	if(json.rel){
		 $("#"+json.rel).find("form:first").attr("action",json.action);
	}
	var attr="";  //用   name#sex  拼接过滤条件
    var value="";
    if(json.attrId){
    	var $attr = $("#"+json.attrId).val();
    	var $value = $("#"+json.valId).val();
    	if($attr!=""){
    		var screenPara = $attr.split("#");
    		var screenVal = $value.split("#");
    		if(json.screenVal!=""){
    			attr = $attr+"#"+json.screenPara;
        		value = $value+"#"+json.screenVal;
    		}
    		$("#"+json.attrId).val(attr);
    		$("#"+json.valId).val(value);
    	}else{
    		if(json.screenVal!=""){
    			$("#"+json.attrId).val(json.screenPara);
        		$("#"+json.valId).val(json.screenVal);
    		}
    	}
    }
    $("#"+json.scopeId).val(json.scope);
    navTabAjaxDone(json);

}
/**
 * 会计科目系统不能删除
 * @param obj
 * @return
 */
function bAccountListDel(obj){
    $this=$(obj);
    var $parent=navTab.getCurrentPanel();
    var tr=$($parent).find("tr").filter(".selected");
    var hasDel= tr.attr("hasDel");
    if(hasDel&&hasDel==1){  //1系统行不能删除    0可以删除
       alertMsg.error("系统行不能删除.");
    }else{
    	var url=$this.attr("rel");
    	var objId=tr.attr("rel");
    	url=url+"-"+objId;
		DWZ.debug(url);
		if (!url.isFinishedTm()) {
			alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
			return false;
		}
    	var title = $this.attr("title");
		if (title) {
			alertMsg.confirm(title, {
				okCall: function(){
					ajaxTodo(url, $this.attr("callback"));
				}
			});
		} else {
			ajaxTodo(url, $this.attr("callback"));
		}
    }
}
/**
 * 会计科目系统不能增加下一级
 * @param obj
 * @return
 */
function bAccountListSort(obj){
    $this=$(obj);
    var $parent=navTab.getCurrentPanel();
    var tr=$($parent).find("tr").filter(".selected");
    var hasDel= tr.attr("hasDel");
    if(hasDel&&hasDel==1){  //1系统行不能删除    0可以删除
       alertMsg.error("系统行不能增加下一级.");
       return false;
    }
}
/**
 * 会计科目系统不能停用
 * @param obj
 * @return
 */
function bAccountListDisable(obj){
    $this=$(obj);
    var $parent=navTab.getCurrentPanel();
    var tr=$($parent).find("tr").filter(".selected");
    var hasDel= tr.attr("hasDel");
    if(hasDel&&hasDel==1){  //1系统行不能删除    0可以删除
       alertMsg.error("系统行不能停用.");
       return false;
    }
}
/**
 * 基本信息  增加下一级验证
 * @param obj
 * @return
 */
function openDialogValidate(obj){
	var $navTab=getCurrentPanel();
	var $this=$(obj);
	var $beforeUrl=$this.attr("beforeUrl");
	var $selectTr=$navTab.find("tr").filter(".selected");
	var objId=$selectTr.attr("rel");
	$.ajax({
		type:'POST',
		url:$beforeUrl,
		data:{id:objId},
		dataType:"json",
		cache: false,
		success:function(data){
			if(data.statusCode!="200"){
				alertMsg.error("已经存在数据，不能增加下级");
				return false;
			}
			var options = {};
			options.width = $this.attr("width");
			options.height = $this.attr("height");
			options.max = eval($this.attr("max") || "false");
			options.mask = eval($this.attr("mask") || "true");
			options.maxable = eval($this.attr("maxable") || "false");
			options.minable = eval($this.attr("minable") || "false");
			options.fresh = eval($this.attr("fresh") || "true");
			options.resizable = eval($this.attr("resizable") || "false");
			options.drawable = eval($this.attr("drawable") || "true");
			options.close = eval($this.attr("close") || "");
			var url = $this.attr("href1")+objId;
			$.pdialog.open(url, $this.attr("rel"), $this.attr("title"), options);
	    },
		error: DWZ.ajaxError
	});
}
/**
 * 修改表格中的状态
 * @param json
 * @return
 */
function editStatus(json){
	DWZ.ajaxDone(json);
	var $parent = navTab.getCurrentPanel();
	if(json.status==1){
		$("#status"+json.id,$parent).text("停用");
	}else{
		$("#status"+json.id,$parent).text("启用");
	}
}
/**
 * 增加dialog 停用checkBox
 * @param obj
 * @param aimId
 * @return
 */
function stopAndStart(obj,aimId){
	var $parent = getCurrentPanel();
	var aim=$("#"+aimId,$parent);
	if($(obj).attr("checked")){
		aim.val(1);
	}else {
		aim.val(2);
	}
}
/*---------------------------------------end---基本信息--------------------------------------------*/




/*------------------------------------------期初数据-----------------------------------------------*/
/**
 * 校验期初财务数据,eg:库存商品项目不能修改期初
 */
function verifyFinanceInitEdit(){
	var $selectTr=aioGetSelectTr();
	var accountType=$selectTr.attr("accountType");//会计科目类型
	if(accountType=="0" || accountType=="0005" || accountType=="0006" || accountType=="000415" || accountType=="00016" || accountType=="00045" || accountType=="000420" || accountType=="000421"){
		return true;
	}else {
		alertMsg.warn("本行金额不能直接的修改！");
		return false;
	}
}
/**
 * 校验商品成本算法是否为1（移动加权）移动加权不弹批次dialog  eq:（期初商品   库存状况）批次详情
 * @return
 */
function verifyProCostArithToBatch(){
	var $selectTr=aioGetSelectTr();
	var costArith=$selectTr.attr("costArith");
	if(costArith==1){
		alertMsg.warn("该算法是加权平均法，不能查看批次！");
		return false;
	}else {
		return true;
	}
}
/*---------------------------------------end---期初数据--------------------------------------------*/





/*------------------------------------------进-----------------------------------------------*/
/**
 * 单位进货优惠统计表   部门进货优惠统计表  职员进货优惠统计表 
 * @param obj
 * @return
 */
function billTypeChecked(obj){
	var $parent  = getCurrentPanel();
	var $val  =  $(obj).val();
	if("unit" == $val){
		$("[name ^='unit.']",$parent).attr("disabled",true);
		$("a[lookupgroup=unit]",$parent).css("visibility", "hidden");
		$("[name ^='staff.']",$parent).attr("disabled",false);
		$("a[lookupgroup=staff]",$parent).css("visibility", "visible");
	}else if("staff" == $val){
		$("[name ^='unit.']",$parent).attr("disabled",false);
		$("a[lookupgroup=unit]",$parent).css("visibility", "visible");
		$("[name ^='staff.']",$parent).attr("disabled",true);
		$("a[lookupgroup=staff]",$parent).css("visibility", "hidden");
	}else{
		$("[name ^='unit.']",$parent).attr("disabled",false);
		$("a[lookupgroup=unit]",$parent).css("visibility", "visible");
		$("[name ^='staff.']",$parent).attr("disabled",false);
		$("a[lookupgroup=staff]",$parent).css("visibility", "visible");
	}
}
/*---------------------------------------end---进--------------------------------------------*/







/*------------------------------------------销-----------------------------------------------*/
/*---销售单-》仓库dialog里面事件-------*/
/**
 * 销售单，仓库，点击-仅显示有货仓库checkbox
 */
function xshasProductStock(obj,stoageOptionFromId){
	var $parent=$.pdialog.getCurrent();
	var searchFrom=$("#"+stoageOptionFromId, $parent);
	var newAction=searchFrom.attr("action");
	var checkboxObj=$(obj).attr("checked");
	if(checkboxObj=="checked"){
		newAction=newAction.substring(0,newAction.lastIndexOf("/")+1)+"hasProductStockShow-has";
	}else{
		newAction=newAction.substring(0,newAction.lastIndexOf("/")+1)+"hasProductStockShow-all";
	}
	searchFrom.attr("action",newAction);
	searchFrom.submit();
}
/**
 * 选中按钮 点击事件(eg:销售单   选择仓库分类)
 * @return
 */
function xscheckBack(){
	var $dialogParent=$.pdialog.getCurrent();
	
	var $parent = navTab.getCurrentPanel();
	var selectTr=$dialogParent.find("tr").filter(".selected");
	var nodeType1=selectTr.attr("nodeType1");
	
	if(nodeType1==2){
		alertMsg.error("不能选择一类基础资料!");
		return;
	}
	var param = selectTr.attr("param");
	var url=selectTr.attr("url");
	var productId=$("#storageProdcutId",$dialogParent).val();
	url=url+"-"+$("#whichCallBack",$parent).val()+"-"+productId+"-"+param;
	var $callback=selectTr.attr("callback");
	checkedBackAjax(url,$callback);
}
/**
 * 选中按钮 点击事件(eg:销售单   选择仓库分类)
 * @return
 */
function cwcheckBack(){
	var $dialogParent=$.pdialog.getCurrent();
	
	var $parent = navTab.getCurrentPanel();
	var selectTr=$dialogParent.find("tr").filter(".selected");
	var nodeType1=selectTr.attr("nodeType1");
	
	var param = selectTr.attr("param");
	var url=selectTr.attr("url");
	var newDownSelectObjs=$("#b_dialog_product_page_checkboxIds",$dialogParent).val();
	var $callback=selectTr.attr("callback");
	var data={whichCallBack:$("#whichCallBack",$parent).val(),newDownSelectObjs:newDownSelectObjs};
	checkedBackAjax(url,$callback,data);
}
/*---end---销售单-》 仓库dialog里面事件------*/

/*---多支付方式(eg:销售单  付款金额)--------*/
/**
 * 打开一个dialog, payType是指定费用id和金额字段的前缀规则
 */
function aioOpenDialog(obj,payTypeIdStrs,payTypeMoneyStrs,payType){
	var $this=$(obj);
	var title = $this.attr("title") || $this.text();
	var rel = $this.attr("rel") || "_blank";
	var options = {};
	var w = $this.attr("dwidth");
	var h = $this.attr("dheight");
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
	var url = unescape($this.attr("href"));  //这里是href  在input里面添加  url验证不通过
	DWZ.debug(url);
	if (!url.isFinishedTm()) {
		alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
		return false;
	}
	if(payTypeIdStrs){
		options.param = $this.attr("param") || payType||"payType";
		var $parent= navTab.getCurrentPanel();
		var ids=$("#"+payTypeIdStrs,$parent).val();
		var moneys=$("#"+payTypeMoneyStrs,$parent).val();
		options.jsonData={'ids':ids,'moneys':moneys};
		//url=url+ids+"-"+moneys;
	}
	$.pdialog.open(url, rel, title, options);
}
/**
 * 多支付方式    带回付款金额
 * @param payType
 * @return
 */
function cwPayTypeDialogTotal(payType){
	var tbody=$("#cw_payType_bodyId",$.pdialog.getCurrent());
	var payType=$("#payType",$.pdialog.getCurrent()).val();  //支付类型    单据支付还是运费支付 
	var trs=tbody.find("tr");
	var payTypeIds="";
	var payTypeMoneys="";
	var totalMoney=0;
	var index=0;
	var showAccount="";  //显示的银行多账户
	var flag=false;
	$(trs).each(function(i,tr){
		var money=$(tr).find("td:eq(3)").find("input").val();
		if(money<0){
			alertMsg.warn("支付金额必须大于0");
			flag=true;
			return false;
		}
		if(!isNaN(money)&&money>0){
			index+=1;
			totalMoney=totalMoney+parseFloat(money);
			showAccount=$(tr).find("td:eq(2)").text();
			payTypeIds=payTypeIds+","+$(tr).find("td:eq(0)").find("input").val();
			payTypeMoneys=payTypeMoneys+","+money;
		}
	});
	if(flag)return;
	
	if(payTypeIds.length>0){
		payTypeIds=payTypeIds.substring(1, payTypeIds.length);
		payTypeMoneys=payTypeMoneys.substring(1, payTypeMoneys.length);
	}
	var $parent=navTab.getCurrentPanel();
	$("#"+payType+"IdStrs",$parent).val(payTypeIds); //记录有数据的支付ID    eg:1,2
	$("#"+payType+"MoneyStrs",$parent).val(payTypeMoneys);
	if(index==0){
		$("#"+payType+"Accounts",$parent).val(""); //收款账户
	}else if(index==1){
		$("#"+payType+"Accounts",$parent).val(showAccount); 
	}else{
		$("#"+payType+"Accounts",$parent).val("现金银行多账户"); 
	}
	$("#"+payType+"Moneys",$parent).val(totalMoney); //总金额
	$.pdialog.closeCurrent();
}
/*---end--多支付方式---------*/

/*------销售查询-》商品销售统计----------*/
/**
 * 先打开navTab再加加载数据     eg:销售查询-》商品销售统计    查询条件  点击确定
 */
function fristOpenNavTabAfterLoadData(obj,modelType,base){
	var $obj=$(obj);
	if($obj.attr("hasBeforCall")&&$obj.attr("hasBeforCall")=="ageAnalysisUnitSelect"){
		var flag=arapAgeAnalysisBeforCall("ageAnalysisUnitSelect");
		if(flag==false){
			return false;
		}
	}
	if($obj.attr("hasBeforCall")&&$obj.attr("hasBeforCall")=="checkedProductBeforCall"){
		var flag= checkedProductBeforCall();
		if(flag==false){
			return false;
		}
	}
	//单位  仓库分布
	if($obj.attr("hasBeforCall")&&$obj.attr("hasBeforCall")=="unitOrStorageMulitSelect"){
		var flag= checkedUnitOrStorageBeforCall(modelType,base);
		if(flag==false){
			return false;
		}
	}
	var $formObj=$obj.parents("form");
	var nvaTabRel=$formObj.find("#aimTabId").val();  
	var nvaTabUrl=$formObj.find("#aimUrl").val();  
	var nvaTabTitle=$formObj.find("#aimTitle").val();
	var nvaTabDiv=$formObj.find("#aimDiv").val();
	var dialog = $obj.parents("div.dialog");
	//var isfresh = $obj.attr("fresh")|| false;
	var isfresh = $obj.attr("fresh")|| true;
	$.pdialog.close(dialog);
	var $fromData=$formObj.serializeArray();
	if(modelType){
		var item={};  
		item['name'] = "modelType"; 
		item['value'] = modelType; 
		$fromData.push(item);
	}
	navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData,freshBtn:isfresh,method:"POST"});
	//navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData});
}
/**
 * eg:销售查询-》商品销售统计    点击明细账本
 * @param obj
 * @param tableBodyId
 * @param formId
 * @param otherId
 * @return
 */
function getIdAndOpenNavTab(obj,tableBodyId,formId,otherId){
	var $obj=$(obj);
	var nvaTabRel=$obj.attr("aimTabId");
	var nvaTabUrl=$obj.attr("aimUrl");
	var nvaTabTitle=$obj.attr("aimTitle");
	var $parent= navTab.getCurrentPanel();
	var productFullName=$("#"+otherId,$parent).val();  //特殊值处理
	var $tr=$("#"+tableBodyId,$parent).find(">tr").filter(".selected");
	if($tr&&$tr.length>0){
		var id=$tr.attr("rel");   //商品ID
		var nodeType1=$tr.attr("nodeType1");
		
		var $fromData=$("#"+formId,$parent).serializeArray();
		var item={};  
		item['name'] = "id"; 
		item['value'] = id; 
		$fromData.push(item);
		var item1={};  
		item1['name'] = "nodeType1"; 
		item1['value'] = nodeType1; 
		$fromData.push(item1);
		if(productFullName){
			var item2={};  
			item1['name'] = "otherName"; 
			item1['value'] = productFullName; 
			$fromData.push(item1);
		}
		
		var fresh=$obj.attr("fresh");
		
		navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData,method:"POST"});
		/*if(fresh&&fresh=="false"){
			navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData,freshBtn:false});
		}else{
			navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData,method:"POST"});
		}*/
	}else{
		alertMsg.error("请选择一条记录!");
	}
}
/**
 * //{A.obj触发对象  B.hiddenFiltter隐藏过滤条（用于分页排序用）     C.formId(要提交的form ID)  D.refreshRel要刷新的对象   E parasIds关联ID eg 销售成本表 单据类型-是否显示红冲
 * eg:销售查询--商品销售统计 -过滤（全部,显示等于0,显示大于0）事件   
 * @param obj
 * @param hiddenFiltter
 * @param formId
 * @param refreshRel
 * @param parasIds
 * @return
 */
function xsReportAmountChange(obj,hiddenFiltter,formId,refreshRel,parasIds){
	var $parent= navTab.getCurrentPanel();
	
	if(parasIds){
		var paras=parasIds.split(",");
		var parasVals="";
		$(paras).each(function (key,val){
			parasVals+="-"+$("#"+val,$parent).val()
		});
		parasVals=parasVals.substring(1, parasVals.length);
		$("#"+hiddenFiltter,$parent).val(parasVals);
	}else{
		var objVal=$(obj).val();
		$("#"+hiddenFiltter,$parent).val(objVal);
	}
	var $form=$("#"+formId,$parent);
  navTabPageBreak($form.serializeArray() , refreshRel);
}
/*--------end---销售查询-》商品销售统计----------*/

/*-------单位分布选择多个单位---------*/
/**
 * 弹出多选择单位库
 */
function alertMutilUnitDialog(obj,base,selectIdsObj){
	var $obj=$(obj);
	var selectIds=$("#"+selectIdsObj,$obj.parent()).val();
	
	var options={};
	var str={'selectIds':selectIds};
	options.jsonData = eval(str);
	$.pdialog.open(base+"/base/unit/unitMultiOption/toDialog", "b_storage_mutil_select_dialog", "单位多选", options);
}
/**
 * 带回
 */
function unitMultiOptionSelected(obj){
	var $dialog=getCurrentPanel();
	var $obj=$(obj);
	var nodeName=$obj[0].nodeName;
	var selectIds="";
	if(nodeName!="TR"){
		selectIds=$("#b_dialog_product_page_checkboxIds",$dialog).val();
	}
	
	if(!selectIds||selectIds==""){
		alertMsg.error("请至少选择一个")
		return false;
	}
	
	var url=$obj.attr("url");
	$.ajax({
		type:'POST',
		url:url,
		data:{selectIds:selectIds},
		dataType:"json",
		cache: false,
		success:function(data){
			$.pdialog.closeCurrent();  //关闭当前Dialog
			var omUserDialog=getCurrentPanel();
			var storageIds=data.storageIds;
			var storageFullNames=data.storageFullNames;
			$("#om_loginUser_storage_id",omUserDialog).val(storageIds);
			$("#om_loginUser_storage",omUserDialog).val(storageFullNames);
	    },
		error: DWZ.ajaxError
	});
	
	
}
/*----end--单位分布选择多个单位--------*/

/**
 * 按钮动态提交form  eg:销售查询  - 单位分布-上级按钮
 */
function upDataByFormPara(pageFromId,url,rel){
	var $parent= navTab.getCurrentPanel();
	var base=$("#projectBasePath",$parent).val();
	var pageFrom=$("#"+pageFromId,$parent);
	var action=base+url;
	
	var $box = $parent.find("#" + rel);
	$box.ajaxUrl( {
		type : "POST",
		url : action,
		data : $(pageFrom).serializeArray(),
		callback : function() {
			$box.find("[layoutH]").layoutH();
		}
	});
}
/**
 * 销售报表  （单位，仓库）销售分布校验最大个数
 */
function checkedUnitOrStorageBeforCall(modelType,base){
	var flag = true;
	var $dialog=getCurrentPanel();
	var storageIds=$("#om_loginUser_storage_id",$dialog).val();
	if(modelType=="unit"){
		if(storageIds=='undefined'||storageIds==""){
			alertMsg.error("请选择往来单位！");	
			return false;
		}
	}else if(modelType=="storage"){
		if(storageIds=='undefined'||storageIds==""){
			alertMsg.error("请选择仓库！");	
			return false;
		}
	}
	
	jQuery.ajax({
		type:'POST',
		url:base+"/base/storage/storageDisplayCount",
		dataType:'json',
		async: false,
		data:{storageIds:storageIds,modelType:modelType},
		timeout: 5000,
		cache: false,
		success: function(response){
			var json = DWZ.jsonEval(response);
			if (json.statusCode==DWZ.statusCode.error){
				if (json.message) alertMsg.error(json.message);
				flag = false;
			} else {
				flag = true;
			}
		}
	});
	
	return flag;
}
/*---------------------------------------end---销--------------------------------------------*/







/*------------------------------------------存-----------------------------------------------*/
/**
 * 全部仓库
 */
function allStorage(treeId,listFormId){
	$("#storgeName", navTab.getCurrentPanel()).text("全部仓库");
	$("#"+treeId, navTab.getCurrentPanel()).tree();
	var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
	$pagerForm.find("[id='storage.id']").val(0);
	$pagerForm.find("#selectedObjectId").val(0);
	$pagerForm.find("input[name='supId']").val(0);
	var formArgs = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
	navTabPageBreak(formArgs, "stockStatusList");
}

/**
 * eq:库存上下限select改变事件
 * @param thisObj
 * @param aimId
 * @return
 */
function selectChangeJs(thisObj,aimId){
	var $obj=$(thisObj);
	var page = $obj.parents("div.pageContent");//同一页面的作用域
	var aimObj = $("#"+aimId,page);
	var selVal = $obj.val();
	if(selVal==0){
		aimObj.val("+");
		aimObj.attr("disabled",true);
	}else {
		aimObj.attr("disabled",false);
	}
}
/**
 * 虚拟库存查询  公式设置
 * @param obj
 * @param id
 * @return
 */
function toValues(obj,id){
	var value = $(obj).attr("value");
	var $parent = getCurrentPanel();
	var cur = $parent.find("#"+id);
	if(cur){
		var curVal = cur.val();
		cur.val(curVal+value)
	}
	var $li = $(obj).parents("li:first");
	if($li){
		$li.siblings(".selected").removeClass("selected");
		$li.addClass("selected");
	}

}
/**
 * 虚拟库存查询  公式检查
 * @param obj
 * @param id
 * @return
 */
function verifyFormula(id,notAlert){
	var $parent = getCurrentPanel();
	var cur = $parent.find("#"+id);
	var curVal = cur.val();
	if(curVal==""){
		alertMsg.warn("请输入公式!");
		return false;
	}
	var repVal = curVal.replace(/(库存数量)/g,"1").replace(/(进货订货)/g,"1").replace(/(销售订货)/g,"1").replace(/(草稿数量)/g,"1");
	//repVal = repVal.replace(/( )/g,"");
	
	try{
		eval(" var ret = " +repVal);
		if(!$.isNumeric(ret)){
			alertMsg.error("公式不正确,请重新输入!");
			return false;
		}else{
			if(!notAlert)alertMsg.correct("公式正确!");
		}
	}catch(e){
		alertMsg.error("公式不正确,请重新输入!");
		return false;
	}
	return true;
	
}
/**
 * 虚拟库存查询  公式设置 确认
 * @param id
 * @return
 */
function formulaSubmit(id){
	var verify = verifyFormula(id,true);
	if(verify){
		var $parent = getCurrentPanel();
		$parent.find("#formulaForm").submit();
	}
}
/*---------------------------------------end---存--------------------------------------------*/








/*------------------------------------------财务-----------------------------------------------*/
/**
 * 往来查询--》超期应收应付款    切换单位日期重新刷新
 * @param   dialog  navtab
 * @param   要提交form里面赋值的ID
 * @param   切换后的值
 * @return
 */
function refreshCwArapOverGetOrPayMoney($parent,inputId,inputVal){
	$("#"+inputId,$parent).val(inputVal);
	var $form=$("#pagerForm",$parent);
	var refreshRel=$("#listID",$form).val();
    navTabPageBreak($form.serializeArray() , refreshRel);
}

/*--------收款单---------*/
/**
 * 是否有预收账款   收款单
 * @return
 */
function accountsChangePreGetMoney(obj){
	var $parent = navTab.getCurrentPanel();
	//可分配金额
	var canAssignMoneyObj=$("#canAssignMoney",$parent);
	//预收账款
	var preGetMoneyObj=$("#preGetMoney",$parent);
	
	var hasPreGetMoney=$("#hasPreGetMoney");
	var hasPreGetMoneyVal=hasPreGetMoney.val();
	if(hasPreGetMoneyVal&&hasPreGetMoneyVal=="no"){
		hasPreGetMoney.val("yes");
		preGetMoneyObj.val(canAssignMoneyObj.val());
		canAssignMoneyObj.val("0");
	}else{
		hasPreGetMoney.val("no");
		canAssignMoneyObj.val(preGetMoneyObj.val());
		preGetMoneyObj.val("0");
	}
}
/**
 * 根据带回的往来单位ID 获取收款类型的单据
 * @param basePath 
 * @param unitId 单位ID
 * @param orderTobody 单位所对应的单据  填充的表格
 * @param isReload    是否重新结算
 * @return
 */
function ajaxGetOrderByUnit(basePath,unitId,orderTobody,isReload,whichCallBack,billId,isDraft){
	$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/getOrderByUnit",
		   dataType: "json",
		   data: {unitId:unitId,isReload:isReload,whichCallBack:whichCallBack,billId:billId,isDraft:isDraft},
		   async: false,
		   success: function(json){
			   var orderList = json.list;
			   var $trs=orderTobody.find("tr");
			   $trs.each(function(key,val){
				 //清空
				 clearTrWidgetSpanVal($(val),"all");
			   });
			   
			   $(orderList).each(function(i,obj){
				   var obj=orderList[i];
				   var $tr=orderTobody.find("tr:eq("+i+")");
				   if($tr.length==0){
					   var preTr=orderTobody.find("tr:eq(0)");
					   addTr2(preTr.parent().attr("id"));
					   $tr=orderTobody.find("tr:eq("+i+")");
				   }
				   addHiddenCurrentWidget($tr);	  //把tr cname 生成控件			   
				   var lastTd=$tr.find("td").last();
				   str="<input type='hidden' cname='id' name='"+$tr.parent().attr("preDataName")+"["+$tr.index()+"].id' />";                //Id
				   str+="<input type='hidden' cname='orderType' name='"+$tr.parent().attr("preDataName")+"["+$tr.index()+"].orderType' />"; //单据类型
				   str+="<input type='hidden' cname='lastMoney' name='"+$tr.parent().attr("preDataName")+"["+$tr.index()+"].lastMoney' />"; //单据类型
				   lastTd.find("div").append(str);
				   $tr.find("input[cname='trIndex']").val(i+1);                              //行号
				   $tr.find("input[cname='orderType']").val(obj.billTypeId);                 //单据类型
				   $tr.find("input[cname='id']").val(obj.id);                                //单据Id
				   $tr.find("input[cname='code']").val(obj.code);                            //编号
				   $tr.find("input[cname='recodeDate']").val(obj.recodeDate);                //录单日期
				   $tr.find("input[cname='remark']").val(obj.remark);                        //摘要
				   var money=0;
				   if(obj.taxMoneys&&!isNaN(obj.taxMoneys)){
					   money=parseFloat(obj.taxMoneys);
				   }
				   $tr.find("input[cname='money']").val(money);                              //金额
				   $tr.find("input[cname='lastMoney']").val(obj.lastMoney);                      //余额
				   showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值在中显示出来
				   //修改TR属性值 
				   var rel=obj.id+"-"+obj.billTypeId;
				   $tr.attr("rel",rel);
				   var url=$tr.attr("url");
				   $tr.attr("url",url+rel);
				   $tr.attr("title",obj.navTabTitle);
				   if(!obj.id||obj.id==0){
					   $tr.attr("isBill","no"); 
				   }
				   
				   
				   //可分配金额重新计算
				   var $navTab = navTab.getCurrentPanel();
				   var moneys=0;
				   var moneys1= $("#moneys",$navTab).val();
				   if(moneys1&&!isNaN(moneys1)){
					   moneys=parseFloat(moneys1);
				   }
				   var privilege=0;
				   var privilege1=$("#privilege",$navTab).val();
				   if(privilege1&&!isNaN(privilege1)){
					   privilege=parseFloat(privilege1);
				   }
				   var canAssignMoney=parseFloat(moneys+privilege);
				   $("#canAssignMoney",$navTab).val(canAssignMoney);
				   //end可分配金额重新计算
			   });
		   }
	});
}
/**
 * 输入金额
 * @param obj
 * @return
 */
function inputAccountsMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var accountsId=$("input[cname='accountsId']",currentTr).val();
	if(!accountsId||accountsId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	getMoneyTrChange(currentTr);//行变化
}
/**
* 行变化
* @param currentTr  当前行
* @return
*/
function getMoneyTrChange(currentTr){
	var $parent = navTab.getCurrentPanel();
	var $accountGrid;
	if(currentTr){
	    $accountGrid=currentTr.parents(".grid");
	}else{
		$accountGrid=$parent.find(".grid:eq(0)");
	}
	
	moneysTotal($accountGrid);//收款单合计总金额
	
	//收款单合计总金额
	var moneysObj=$("#moneys",$parent);
	var moneysObjVal=0;
	if(moneysObj.val()&&!isNaN(moneysObj.val())){
		moneysObjVal=parseFloat(moneysObj.val());
	}
	
	//结算总金额
	var settlementMmoneys= settlementMmoneysTotal($parent);
	
	//优惠金额
	var privilegeObj=$("#privilege",$parent);
	var privilegeObjVal=0;
	if(privilegeObj.val()&&!isNaN(privilegeObj.val())){
		privilegeObjVal=parseFloat(privilegeObj.val());
	}
	//可分配金额
	var canAssignMoneyObj=$("#canAssignMoney",$parent);
	var canAssignMoneyObjVal=0;
	if(canAssignMoneyObj.val()&&!isNaN(canAssignMoneyObj.val())){
		canAssignMoneyObjVal=parseFloat(canAssignMoneyObj.val());
	}
	//预收账款
	var hasPreGetMoney=$("#hasPreGetMoney",$parent).val();
	var preGetMoneyObj=$("#preGetMoney",$parent);
	var preGetMoneyObjVal=0;
	if(preGetMoneyObj.val()&&!isNaN(preGetMoneyObj.val())){
		preGetMoneyObjVal=parseFloat(preGetMoneyObj.val());
	}
	//合计
	var privilegeMoneyObj=$("#privilegeMoney",$parent);
	var privilegeMoneyObjVal=0;
	if(privilegeMoneyObj.val()&&!isNaN(privilegeMoneyObj.val())){
		privilegeMoneyObjVal=parseFloat(privilegeMoneyObj.val());
	}
	
	var privilegeMoneyVal=moneysObjVal+privilegeObjVal;  //金额合计+优惠金额
	//合计赋值  
	privilegeMoneyObj.val(privilegeMoneyVal);
	
	if(hasPreGetMoney&&hasPreGetMoney=="yes"){
		//可分配金额赋值
		canAssignMoneyObj.val("0");
		preGetMoneyObj.val(round(privilegeMoneyVal-settlementMmoneys,4));  //合计-优惠金额
	}else{
		//预收账款赋值
		canAssignMoneyObj.val(round(privilegeMoneyVal-settlementMmoneys,4));
		preGetMoneyObj.val("0");  //合计-优惠金额
	}
}
/**
* 行变化
* @param currentTr  当前行
* @return
*/
function feeBillTrChange(currentTr){
	var $parent = navTab.getCurrentPanel();
	var $accountGrid;
	if(currentTr){
	    $accountGrid=currentTr.parents(".grid");
	}else{
		$accountGrid=$parent.find(".grid:eq(0)");
	}
	moneysTotal($accountGrid);//收款单合计总金额
	//收款单合计总金额
	var moneysObj=$("#moneys",$parent);
	var moneysObjVal=0;
	if(moneysObj.val()&&!isNaN(moneysObj.val())){
		moneysObjVal=parseFloat(moneysObj.val());
	}
	//合计赋值  
	var privilegeMoneyObj=$("#privilegeMoney",$parent);
	privilegeMoneyObj.val(moneysObjVal);
	
}
/**
* 重新结算行变化
* @param currentTr  当前行
* @return
*/
function getMoneyReTrChange(currentTr){
	var $parent = navTab.getCurrentPanel();
	var $accountGrid;
	if(currentTr){
	    $accountGrid=currentTr.parents(".grid");
	}else{
		$accountGrid=$parent.find(".grid:eq(0)");
	}
	
	
	//结算总金额
	var settlementMmoneys= settlementMmoneysTotal($parent);
	
	//优惠金额
	var privilegeObj=$("#privilege",$parent);
	var privilegeObjVal=0;
	if(privilegeObj.val()&&!isNaN(privilegeObj.val())){
		privilegeObjVal=parseFloat(privilegeObj.val());
	}
	//可分配金额
	var canAssignMoneyObj=$("#canAssignMoney",$parent);
	var canAssignMoneyObjVal=0;
	if(canAssignMoneyObj.val()&&!isNaN(canAssignMoneyObj.val())){
		canAssignMoneyObjVal=parseFloat(canAssignMoneyObj.val());
	}
	//预收账款
	var hasPreGetMoney=$("#hasPreGetMoney",$parent).val();
	var preGetMoneyObj=$("#preGetMoney",$parent);
	var preGetMoneyObjVal=0;
	if(preGetMoneyObj.val()&&!isNaN(preGetMoneyObj.val())){
		preGetMoneyObjVal=parseFloat(preGetMoneyObj.val());
	}
	//合计
	var privilegeMoneyObj=$("#privilegeMoney",$parent);
	var privilegeMoneyObjVal=0;
	if(privilegeMoneyObj.val()&&!isNaN(privilegeMoneyObj.val())){
		privilegeMoneyObjVal=parseFloat(privilegeMoneyObj.val());
	}
	
	var privilegeMoneyVal=privilegeMoneyObjVal;
	
	if(hasPreGetMoney&&hasPreGetMoney=="yes"){
		//可分配金额赋值
		canAssignMoneyObj.val("0");
		preGetMoneyObj.val(round(privilegeMoneyVal-settlementMmoneys,4));  //合计-优惠金额
	}else{
		//预收账款赋值
		canAssignMoneyObj.val(round(privilegeMoneyVal-settlementMmoneys,4));
		preGetMoneyObj.val("0");  //合计-优惠金额
	}
}
/**
 * 按单结算   打上√ 回调函数
 * @return
 */
function selectOrderTrue(whichCallBack,$td){
	var $parent = navTab.getCurrentPanel();
	var $tr=$td.parent();

	if("getMoney"==whichCallBack || "payMoney"==whichCallBack){
	   var lastMoney=0;
	   var lastMoneyObj=$tr.find("input[cname='lastMoney']");
	   if(lastMoneyObj.val()&&!isNaN(lastMoneyObj.val())){
		   lastMoney=parseFloat(lastMoneyObj.val());
	   }

	   var hasPreGetMoney=$("#hasPreGetMoney",$parent).val();      //是否挂预收账款
	   var settlementAmountObj=$tr.find("input[cname='settlementAmount']");
	   var settlementAmountObjVal=0;
	   if(settlementAmountObj.val()&&!isNaN(settlementAmountObj.val())){
		   settlementAmountObjVal=parseFloat(settlementAmountObj.val());
	   }
	   if(hasPreGetMoney&&hasPreGetMoney=="no"){  //没有挂在预收账款上
		   var canAssignMoney=parseFloat($("#canAssignMoney",$parent).val());//可分配金额
		   if(isNaN(canAssignMoney)){
			   canAssignMoney=0;
		   }
		   canAssignMoney=round(settlementAmountObjVal+canAssignMoney,4);
		   if(canAssignMoney>=lastMoney){  //可分配金额>=余额
			   if(lastMoney==0){
				   $tr.find("input[cname='settlementAmount']").val(""); 
			   }else{
				   $tr.find("input[cname='settlementAmount']").val(lastMoney);
			   }
			   $("#canAssignMoney",$parent).val(round(canAssignMoney-lastMoney,4));
		   }else{                         //可分配金额<余额
			   if(lastMoney==0){
				   $tr.find("input[cname='settlementAmount']").val("");
			   }else{
				   $tr.find("input[cname='settlementAmount']").val(canAssignMoney);
			   }
			   $("#canAssignMoney",$parent).val("0");
		   }
	   }else{                                     //挂在预收账款上
		  var preGetMoney=parseFloat($("#preGetMoney",$parent).val());     //预收账款
		  preGetMoney=round(settlementAmountObjVal+preGetMoney,4);
		  if(preGetMoney>=lastMoney){  //预收账款>=余额
			   if(lastMoney==0){
				   $tr.find("input[cname='settlementAmount']").val("");
			   }else{
				   $tr.find("input[cname='settlementAmount']").val(lastMoney);  
			   }
			   $("#preGetMoney",$parent).val(round(preGetMoney-lastMoney,4));
		   }else{                         //预收账款<余额
			   if(lastMoney==0){
				   $tr.find("input[cname='settlementAmount']").val("");
			   }else{
				   $tr.find("input[cname='settlementAmount']").val(preGetMoney); 
			   }
			   $("#preGetMoney",$parent).val("0");
		   }
	   }
    }else{
    	alert("√上的回调函数");
    }
	showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值在中显示出来
}
/**
 * 按单结算   去掉√ 回调函数
 * @return
 */
function selectOrderFalse(whichCallBack,$td){
	var $parent = navTab.getCurrentPanel();
	var $tr=$td.parent();
	if("getMoney"==whichCallBack || "payMoney"==whichCallBack){
	   var settlementAmount=parseFloat($tr.find("input[cname='settlementAmount']").val());   //结算金额
	   var hasPreGetMoney=$("#hasPreGetMoney",$parent).val();      //是否挂预收账款
	   
	   if(hasPreGetMoney&&hasPreGetMoney=="no"){  //没有挂在预收账款上
		   var canAssignMoney=parseFloat($("#canAssignMoney",$parent).val());//可分配金额
		   $tr.find("input[cname='settlementAmount']").val("");
		   $("#canAssignMoney",$parent).val(round(canAssignMoney+settlementAmount,4));
	   }else{                                     //挂在预收账款上
		  var preGetMoney=parseFloat($("#preGetMoney",$parent).val());     //预收账款
		  $tr.find("input[cname='settlementAmount']").val("");
		  $("#preGetMoney",$parent).val(round(preGetMoney+settlementAmount,4));
	   }
    }else{
    	alert("去掉√的回调函数");
    }
	showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值在中显示出来
}
/**
 * 优惠金额离开事件
 * @return
 */
function getMoneyPrivilegeMoneyOnblur(){
	var $parent = navTab.getCurrentPanel();
	//收款单合计总金额
	var moneysObj=$("#moneys",$parent);
	var moneysObjVal=0;
	if(moneysObj.val()&&!isNaN(moneysObj.val())){
		moneysObjVal=parseFloat(moneysObj.val());
	}
	//合计
	var privilegeMoneyObj=$("#privilegeMoney",$parent);
	var privilegeMoneyObjVal=0;
	if(privilegeMoneyObj.val()&&!isNaN(privilegeMoneyObj.val())){
		privilegeMoneyObjVal=parseFloat(privilegeMoneyObj.val());
	}
	//旧的优惠金额
	var oldPrivilegeObjVal=round(privilegeMoneyObjVal-moneysObjVal,4);
	//优惠金额
	var privilegeObj=$("#privilege",$parent);
	var privilegeObjVal=0;
	if(privilegeObj.val()&&!isNaN(privilegeObj.val())){
		privilegeObjVal=parseFloat(privilegeObj.val());
	}
	//优惠金额-旧的优惠金额
	var gapMoney=privilegeObjVal-oldPrivilegeObjVal;
	
	//合计重新赋值
	$("#privilegeMoney",$parent).val(privilegeMoneyObjVal+gapMoney);
	
	var hasPreGetMoney=$("#hasPreGetMoney",$parent).val();
	if(hasPreGetMoney&&hasPreGetMoney=="yes"){
		//预收账款
		var preGetMoneyObj=$("#preGetMoney",$parent);
		var preGetMoneyObjVal=0;
		if(preGetMoneyObj.val()&&!isNaN(preGetMoneyObj.val())){
			preGetMoneyObjVal=parseFloat(preGetMoneyObj.val());
			$("#preGetMoney",$parent).val(preGetMoneyObjVal+gapMoney);
		}
	}else{
		//可分配金额
		var canAssignMoneyObj=$("#canAssignMoney",$parent);
		var canAssignMoneyObjVal=0;
		if(canAssignMoneyObj.val()&&!isNaN(canAssignMoneyObj.val())){
			canAssignMoneyObjVal=parseFloat(canAssignMoneyObj.val());
			$("#canAssignMoney",$parent).val(canAssignMoneyObjVal+gapMoney);
		}
	}
}
/**
 * 输入结算金额离开事件
 * @return
 */
function inputAccountsSettlementMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var orderId=$("input[cname='id']",currentTr).val();
	if(!orderId||orderId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	getMoneyTrChange(currentTr);//行变化
}
/**
 * 重新结算 输入结算金额离开事件
 * @return
 */
function inputReAccountsSettlementMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var orderId=$("input[cname='id']",currentTr).val();
	if(!orderId||orderId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	
	getMoneyReTrChange(currentTr);
}
/**
 * 自动分配结算金额
 * @return
 */
function accountsAutoSettlementMoney(){
	var nav = navTab.getCurrentPanel();
	var $parent = nav.find("div.grid:eq(1)");
	if(!$parent){
		return;
	}
	var trs=$parent.find("tr");
	trs.each(function(key,tr){
		if(key!=0){
			var $tr=$(tr);
			var $selectTd=$tr.find("td[cname='select']");
			accountSettleMentAmouont($selectTd,"check");
		}
	});
	trs.each(function(key,tr){
		if(key!=0){
			var $tr=$(tr);
			var $selectTd=$tr.find("td[cname='select']");
			accountSettleMentAmouont($selectTd,"check");
		}
	});
	
	
}
/**
 * dwz.stable.js  结算某张单据
 * @param $td
 * @param newCheeck  重新分配
 * @return
 */
function accountSettleMentAmouont(td,newCheck){
	var $td=$(td);
	if($td.attr("isSelectTrue")!=undefined){
 	   if(!$td.parent().find("input[cname='id']").val()){
 		   return;
 	   }
 	   var whichCallBackNavTab=getCurrentPanel();
 	   var whichCallBack=$("#whichCallBack",whichCallBackNavTab).val();
 	   if(newCheck&&newCheck=="check"){  //自动分配
 		  $td.attr("isSelectTrue","false");
 	   }
 	   
 	   if($td.attr("isSelectTrue")=="false"){
 		   selectOrderTrue(whichCallBack,$td);//√上的回调函数
 		   var settlementAmount= 0;
 		   if($td.parent().find("input[cname='settlementAmount']").val()){
 			   settlementAmount= parseFloat($td.parent().find("input[cname='settlementAmount']").val());
 		   }
 		   if(settlementAmount!=0){
 			   $td.attr("isSelectTrue","true");
	    		   $td.html("<div>√</div>");
 		   }
 	   }else{
 		   $td.attr("isSelectTrue","false");
 		   $td.html("<div></div>"); 
 		   selectOrderFalse(whichCallBack,$td);//去掉√的回调函数
 	   }
    }
}
/**
 * 重新结算
 * @return
 */
function reloadCountMoney(isDraft){
	var $parent=getCurrentPanel();
	//查看
	var getMoneyReCount=$("#getMoneyReCount",$parent);
	var getMoneyAutoCountLook=$("#getMoneyAutoCountLook",$parent);
	//修改
	var getMoneyReCountSave=$("#getMoneyReCountSave",$parent);
	var getMoneyAutoCountEdit=$("#getMoneyAutoCountEdit",$parent);
	
	getMoneyReCount.css("display","none");
	getMoneyAutoCountLook.css("display","none");
	
	getMoneyReCountSave.css("display","block");
	getMoneyAutoCountEdit.css("display","block");
	
	
	var basePath = $("#basePathId",$parent).val();//路径
	var unitId = $("#unitId",$parent).val();

	var orderTobody=$("#moneyBillOrderbody",$parent);
	var billId = $("#billId",$parent).val();

	//得到单据上的结算总金额
	/*var  $money= orderTobody.find("input[cname='settlementAmount']");//结算
	var moneySum = 0;
	$money.each(function(){
		var money = $(this).val();
		if(money!="" && !isNaN(money)){
			moneySum = moneySum+parseFloat(money);
		}
	});
	if(parseFloat(moneySum)>0){
		moneySum=round(moneySum,4);//结算金额
	}else{
		moneySum=0;
	}*/
	
	var whichCallBack = $("#whichCallBack",$parent).val();
	//重新加载
	ajaxGetOrderByUnit(basePath,unitId,orderTobody,true,whichCallBack,billId,isDraft);
	
	/*var canAssignMoney=0;
	var assign=$("#canAssignMoney",$parent).val();
	if(assign&&assign!="" && !isNaN(assign)){
		canAssignMoney = canAssignMoney+parseFloat(assign);
	}
	$("#canAssignMoney",$parent).val(round(canAssignMoney+moneySum,4));*/
	
}

/*----------end-收款单--------*/

/*-----------账龄分析  查询窗体-------------*/
/**
 * 账龄分析  查询窗体  日期间隔
 * @return
 */
function arapAnalysisDayGay(obj){
	var $obj=$(obj);
	var currentInput=$obj.parent().find("input");
	if(!currentInput.val()){
		currentInput.val("30");
	}
	var $tbody=$obj.parents("tbody");
	aioBillOnblurRemoveWidget(obj);
	var startDay=1;
	var endDay=0;
	
	//1-30
	var day1=$tbody.find("input[cname='day1']").val();
	endDay=startDay+parseInt(day1)-1;
	var day1=$("#day1",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//31-60
	var day2=$tbody.find("input[cname='day2']").val();
	endDay=startDay+parseInt(day2)-1;
	var day1=$("#day2",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//61-90
	var day3=$tbody.find("input[cname='day3']").val();
	endDay=startDay+parseInt(day3)-1;
	var day1=$("#day3",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//91-120
	var day4=$tbody.find("input[cname='day4']").val();
	endDay=startDay+parseInt(day4)-1;
	var day1=$("#day4",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//121-150
	var day5=$tbody.find("input[cname='day5']").val();
	endDay=startDay+parseInt(day5)-1;
	var day1=$("#day5",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//151-180
	var day6=$tbody.find("input[cname='day6']").val();
	endDay=startDay+parseInt(day6)-1;
	var day1=$("#day6",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//181-210
	var day7=$tbody.find("input[cname='day7']").val();
	endDay=startDay+parseInt(day7)-1;
	var day1=$("#day7",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//211-240
	var day8=$tbody.find("input[cname='day8']").val();
	endDay=startDay+parseInt(day8)-1;
	var day1=$("#day8",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//241-270
	var day9=$tbody.find("input[cname='day9']").val();
	endDay=startDay+parseInt(day9)-1;
	var day1=$("#day9",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//271-300
	var day10=$tbody.find("input[cname='day10']").val();
	endDay=startDay+parseInt(day10)-1;
	var day1=$("#day10",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//301-330
	var day11=$tbody.find("input[cname='day11']").val();
	endDay=startDay+parseInt(day11)-1;
	var day1=$("#day11",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//331-360
	var day12=$tbody.find("input[cname='day12']").val();
	endDay=startDay+parseInt(day12)-1;
	var day1=$("#day12",$tbody).html("从"+startDay+"至"+endDay+"天");
	startDay=endDay+1;
	//361以上
	var day13=$tbody.find("input[cname='day13']").val();
	var day1=$("#day13",$tbody).html(startDay+"天以上");
}
/**
 * 账龄分析  查询窗体  选择单位回调
 * @return
 */
function arapAnalysisUnitBack(currentAreaId){
	var $panel=getCurrentPanel();
	var unitId=$panel.find("input[name='unit.id']").val();
	var $base=$("#basePathId",$panel).val();
	var unitAreaTbody=$("#"+currentAreaId,$panel);
	$.ajax({
		type:'POST',
		url:$base+"/base/unit/unitListById",
		data: {unitId:unitId},
		dataType:"json",
		cache: false,
		success:function(data){
			//获取已有的单位ID
			var units=unitAreaTbody.find("input[name='unitIds']").val();
			var trIndex=0;
			if(units||units==""){
				units==",";
			}else{
				trIndex=units.split(",").length-2;
			}
			if(data&&data.length>0){
				for ( var i = 0; i < data.length; i++) {
					var unit=data[i];
					var unitId=unit.id;
					var index=units.indexOf(unitId);
					if(index==-1){
						units=units+unitId+",";
					}
					var tr=unitAreaTbody.find("tr:eq("+trIndex+")");
					tr.attr("unitId",unit.id);
					tr.find("td:eq(0)").children("div").text(unit.code);
					tr.find("td:eq(1)").children("div").text(unit.fullName);
					trIndex=trIndex+1;
					if(trIndex>=12){
						//增加一行
						addTr3(currentAreaId)
					}
				}
			}
			//处理完
			unitAreaTbody.find("input[name='unitIds']").val(units)
		},
		error: DWZ.ajaxError
	});
}
/**
 * 账龄分析  查询窗体  删除一行
 * @return
 */
function arapAnalysisUnitClearTr(currentAreaId){
	var $panel=getCurrentPanel();
	var unitAreaTbody=$("#"+currentAreaId,$panel);
	var tr=unitAreaTbody.find("tr").filter(".selected");
	var unitId=tr.attr("unitId");
	if(unitId&&unitId!=""){
		var unitIds=unitAreaTbody.find("input[name='unitIds']").val();
		var arry=unitIds.split(","+unitId+",");
		var newUnitIds="";
		if(arry.length==1){
			newUnitIds=arry[0]+",";
		}else if(arry.length==2){
			newUnitIds=arry[0]+","+arry[1];
		}
		unitAreaTbody.find("input[name='unitIds']").val(newUnitIds);
		tr.attr("unitId","");
		tr.find("td:eq(0)").children("div").text("");
		tr.find("td:eq(1)").children("div").text("");
		unitAreaTbody.find("tr").removeClass("selected");
		unitAreaTbody.find("tr:eq(0)").addClass("selected");
	}
}
/**
 * 账龄分析  查询窗体  全部清除
 * @return
 */
function arapAnalysisUnitClearAllTr(currentAreaId){
	var $panel=getCurrentPanel();
	var unitAreaTbody=$("#"+currentAreaId,$panel);
	var unitIds=unitAreaTbody.find("input[name='unitIds']").val();
	var trs=unitAreaTbody.find("tr");
	for ( var i = 0; i < trs.length; i++) {
		var tr=$(trs[i]);
		var unitId=tr.attr("unitId");
		if(unitId&&unitId!=""){
			var arry=unitIds.split(","+unitId+",");
			var newUnitIds="";
			if(arry.length==1){
				newUnitIds=arry[0]+",";
			}else if(arry.length==2){
				newUnitIds=arry[0]+","+arry[1];
			}
			unitAreaTbody.find("input[name='unitIds']").val(newUnitIds);
			tr.attr("unitId","");
			tr.find("td:eq(0)").children("div").text("");
			tr.find("td:eq(1)").children("div").text("");
		}
	}
	unitAreaTbody.find("input[name='unitIds']").val(",");
	unitAreaTbody.find("tr").removeClass("selected");
	unitAreaTbody.find("tr:eq(0)").addClass("selected");
}
/**
 * 账龄分析  查询窗体  提交前检验单位是否选择
 * @return
 */
function arapAgeAnalysisBeforCall(ageAnalysisUnitSelect){
	var $panel=getCurrentPanel();
	var unitAreaTbody=$("#"+ageAnalysisUnitSelect,$panel);
	var unitIds=unitAreaTbody.find("input[name='unitIds']").val();
	if(!unitIds||unitIds==","){
		alertMsg.error("请选择单位");
		return false;
	}
	return true;
}
function checkedProductBeforCall(){
	var $panel=getCurrentPanel();
	
	var productids=$panel.find("input[name=productIdArr]:checked").val();
	if(!productids||productids==","||productids==""){
		alertMsg.error("请选择商品");
		return false;
	}
	return true;
}
/*------end-账龄分析  查询窗体-------------*/

/**
 * 会计凭证 
 */
function sysAccountsCallBack(json,sysAccounts){
	
	var noCode = ["00014","00015","00017","00018","00046","000411","000414","000417","000412"];
	var dialogCode = ["00013","000413"];
	var code = json[0].type;
	for(var i=0;i<noCode.length;i++){
		if(noCode[i]==code){
			alertMsg.warn("不能选择此科目！");
			return;
		}
	}
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTr =  $($(selectTd).parent()[0]);

	var object=sysAccounts;
    if(selectTr.find("input[cname='accountsId']")){
    	billClearTrData(selectTr);//单据清除行数据
    }
    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象  
    
    var isObject = true;
    for(var i=0;i<dialogCode.length;i++){
    	if(dialogCode[i]==code){
    		isObject = false;
    		break;
    	}
    }
    if(isObject){
    	 detailAccountsHiddenAndValue(selectTr,json[0]);//隐藏明细科目
         billAccountsHiddenAndValue(selectTr,object);//单据隐藏会计科目对象并赋值
    }else{
    	 billAccountsHiddenAndValue(selectTr,json[0]);//单据隐藏会计科目对象并赋值
		 selectTr.find("td[cname='accountsDetail']:first").removeAttr("hasDialog");
		 selectTr.find("td[cname='accountsDetail']:first").dblclick();
    }
    showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在中显示出来
    selectTr= hasAddTr(selectTr);//是否增加一行tr
  
	
}
/**
 * 会计科目选择单位  回调
 * @param res
 * @return
 */
function unitCheckBack(res){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(res);
	var $parent = getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTr = selectTd.parents("tr:first");
	var unit = selectTr.find("input[cname=unitId]");
	if($.isEmptyObject(unit) || unit.length==0){
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='unitId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].unitId' />";  //单位的Id
		lastTd.find("div").append(str);
	}
	selectTr.find("td[cname=accountsDetail]").text(res.unit.fullName);
	selectTr.find("input[cname=accountsDetail]").val(res.unit.fullName);
	selectTr.find("input[cname=unitId]").val(res.unit.id);
}
/*---------------------------------------end---财务--------------------------------------------*/






/*------------------------------------------单据中心-----------------------------------------------*/
/**
 * 草稿批量过账-自动提交
 * @return
 */
function draftPostAutoSubmit(){
	var $panel=getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$panel).val();
	var autoPost = $("#autoPost",$panel).val();
	if(whichCallBack && autoPost && autoPost=="true"){
		var $form = $("form",$panel);
		if (!$form.valid()) {
			return false;
		}
		$form.submit();
	}
}
/**
 * 万能查询选项是否禁用
 * @return
 */
function optionIsDisable(){
	var $Panel = getCurrentPanel();
	var itemVal = $("#itemNum",$Panel).val();
	
	var isMemberT = $("td[id='isMember']",$Panel);
	var isMember = $("td[id='isMember']~td:first");
	
	var isCouponT = $("td[id='isCoupon']",$Panel);
	var isCoupon = $("td[id='isCoupon']~td:first");
	
	var priceT = $("td[id='price']",$Panel);
	var price = $("td[id='price']~td:first");
	
	var discountT = $("td[id='discount']",$Panel);
	var discount = $("td[id='discount']~td:first");
	
	var taxrateT = $("td[id='taxrate']",$Panel);
	var taxrate = $("td[id='taxrate']~td:first");
	
	//禁用会员卡情况
	if(itemVal.indexOf("0") == -1){
		isMemberT.css("color","gray");
		$("select,input",isMember).each(function(){
			$(this).val(0);
			$(this).attr("disabled","disabled");
		});
	}else{
		isMemberT.css("color","black");
		$("select,input",isMember).each(function(){
			$(this).val(0);
			if(this.nodeName=="SELECT")
			$(this).removeAttr("disabled");
		});
	}
	
	//禁用优惠情况
	if(itemVal.indexOf("1") == -1){
		isCouponT.css("color","gray");	
		$("select,input",isCoupon).each(function(){
			$(this).val(0);
			$(this).attr("disabled","disabled");
		});
	}else{
		isCouponT.css("color","black");
		$("select,input",isCoupon).each(function(){
			$(this).val(0);
			if(this.nodeName=="SELECT")
			$(this).removeAttr("disabled");
		});
	}
	
	//禁用单价
	if(itemVal.indexOf("2") == -1){
		priceT.css("color","gray");
		$("select,input",price).each(function(){
			$(this).val(0);
			$(this).attr("disabled","disabled");
		});
	}else{
		priceT.css("color","black");
		$("select,input",price).each(function(){
			$(this).val(0);
			if(this.nodeName=="SELECT"){
				$(this).removeAttr("disabled");
			}else if (this.nodeName=="INPUT") {
				$(this).attr("disabled","disabled");
			}
		});
	}
	
	//禁用折扣
	if(itemVal.indexOf("3") == -1){
		discountT.css("color","gray");
		$("select,input",discount).each(function(){
			$(this).val(0);
			$(this).attr("disabled","disabled");
		});
	}else{
		discountT.css("color","black");
		$("select,input",discount).each(function(){
			$(this).val(0);
			if(this.nodeName=="SELECT"){
				$(this).removeAttr("disabled");
			}else if (this.nodeName=="INPUT") {
				$(this).attr("disabled","disabled");
			}
		});
	}
	
	//禁用税率
	if(itemVal.indexOf("4") == -1){
		taxrateT.css("color","gray");
		$("select,input",taxrate).each(function(){
			$(this).val(0);
			$(this).attr("disabled","disabled");
		});
	}else{
		taxrateT.css("color","black");
		$("select,input",taxrate).each(function(){
			$(this).val(0);
			if(this.nodeName=="SELECT"){
				$(this).removeAttr("disabled");
			}else if (this.nodeName=="INPUT") {
				$(this).attr("disabled","disabled");
			}
		});
	}
}
/**
 * 万能查询选项重置
 * @return
 */
function reseBillSearchFrom(obj){
	$obj = $(obj);
	$form = $obj.closest("form");
	$table = $("table",$form);
	$("select",$table).each(function(){
			$(this).val(0);
		});
	$("input",$table).not($("input[class*='date']")).each(function(){
		$(this).val("");
	});
	$("input[isNotRese='true']",$table).each(function(){
		$(this).val("0");
		$(this).attr("disabled","disabled");
	});
}
/**
 * 下拉框改变
 * @param obj
 * @param aimId
 * @return
 */
function selectChanage(obj,aimId){
	$obj=$(obj);
	var $parent=$obj.parents("div.dialog");
	var aim=$parent.find("#"+aimId);
	if($obj.val()==0){
		aim.val(0);
		aim.attr("disabled","disabled");
	}else {
		aim.removeAttr("disabled");
	}
}
/**
 * 校验单据是否为红冲   红冲单据操作
 * @param num
 * @return
 */
function verifyBillIsRCW(num){
	var flag=true;
	var msg="这张单据已经是红冲单据，不能再反冲！";
	if(num==2){
		msg="这张单据已经是红冲单据，不能再修改！";
	}
	
	var $selectTr=aioGetSelectTr();
	var rel=$selectTr.attr("rel");
	if(!rel){
		alertMsg.warn("请选择单据！");
		flag=false;
	}
	var isRCW=$selectTr.attr("isRCW");
	if(isRCW && isRCW!=0){
		alertMsg.warn(msg);
		flag=false;
	}
	return flag;
}
/**
 * 校验草稿是否是物流单据可以转换  业务草稿复制单据操作
 * @return
 */
function verifyFlowBillToCopy(){
	var flag=true;
	//(6,8,4,9,14,15,5,7,20,10,11)
	var $Panel = getCurrentPanel();
	var isFlow = $("tbody > tr[class='selected']",$Panel).attr("isflow");
	if(isFlow==0){
		alertMsg.warn("该单据不在允许复制的单据范围，不能复制！");
		flag=false;
	}
	return flag;
}
/*---------------------------------------end---单据中心--------------------------------------------*/






/*------------------------------------------辅助功能-----------------------------------------------*/
/*-------数据搬移-------*/
/**
 * 基本信息搬移开始提交
 * @param obj
 * @return
 */
function baseMoveFormAction(obj){
	$this=$(obj);
	var $panel = getCurrentPanel();//作用域
	var $form = $this.closest("form");
	var moveNode = $("input[name='moveNode']:checked",$form).val();
	
	var $socForm=$panel.find("div[id='sourceList']").find("form");//源form
	var selectObjs = $("input[name='selectObjs']",$socForm).val();
	if(selectObjs==""){
		alertMsg.warn("请选择需要搬移的源节点数据！");
		return false;
	}
	
	var $aimForm=$panel.find("div[id='aimList']").find("form");//目标form
	var aimId = $("input[name='aimId']",$aimForm).val();
	if(aimId==""){
		alertMsg.warn("请选择目标节点！");
		return false;
	}
	var nodeStr="同级节点"
	if(moveNode==-1){
		nodeStr="子级节点";
	}
	var td = $aimForm.find("tbody").find("tr[rel='"+aimId+"']").find("td")[2];
	var aimStr = $(td).text();
	alertMsg.confirm(("您是否要将选中的信息搬移到：【"+aimStr+"】的【"+nodeStr+"】上"), {
		okCall: function(){
			var str="({";
				str+="'moveNode':'"+moveNode+"',";//节点选择
				str+="'aimId':'"+aimId+"',";//目标ID
				str+="'selectObjs':'"+selectObjs+"'";//选中的ID列表
			str+="})";
			jsonData = eval(str);
			
			$.ajax({
				type: $form.attr("method") || 'POST',
				url:$form.attr("action"),
				data:jsonData,
				dataType:"json",
				cache: false,
				success: baseMoveCallBack || DWZ.ajaxDone,
				error: DWZ.ajaxError
			});
		},
		cancelCall: function(){
		}
	});
	
}
/**
 * 基本数据搬移回调
 * @param json
 * @return
 */
function baseMoveCallBack(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		var $panel = getCurrentPanel();//作用域
		
		var $box1 = $panel.find("div[id='sourceList']");
		var $socForm = $box1.find("form");//源form
		var selectObjs = $("input[name='selectObjs']",$socForm).val("");
		var url1 = $socForm.attr("action");
		var args1 = $socForm.size()>0 ? $socForm.serializeArray() : {}
		$box1.ajaxUrl({
			type:"POST", url:url1, data:args1, callback:function(){
				$box1.find("[layoutH]").layoutH();
			}
		});
		
		var $box2 = $panel.find("div[id='aimList']");
		var $aimForm = $box2.find("form");//目标form
		//$aimForm.submit();
		var url2 = $aimForm.attr("action");
		var args2 = $aimForm.size()>0 ? $aimForm.serializeArray() : {}
		$box2.ajaxUrl({
			type:"POST", url:url2, data:args2, callback:function(){
				$box2.find("[layoutH]").layoutH();
			}
		});
		
		if(json.msg)
		alertMsg.info(json.msg);
		return false;
	}
	if(json.msg)
	alertMsg.warn(json.msg);
}
/**
 * 复选框封装成id字符串
 * @param obj
 * @param strId 保存的id字符串列表input
 * @return
 */
function checkboxPostList(obj,strId){   //点击修改URL
	var $panel = getCurrentPanel();
	var newIds=$("#"+strId,$panel).val();  //当前页选择checkbox  id
	$this=$(obj);
	var tr=$this.parents("tr:first");
	var val=$(tr).attr("rel");
	val=pidsConvertStr(val);//转换
	if($(tr).attr("isCheckbox") && $this.is(':checked')){//点击父节点时校验
		var flag = verifySonIsCheck(newIds,val);
		if(flag){
			alertMsg.warn("子节点已被选中，不能选中父节点！");
			$this.attr("checked", false);
			return false;
		}
	}
	if(newIds==""){
		newIds=val;
	}else{
		newIds=","+newIds+",";
		if(newIds.indexOf(","+val+",")>-1){  //存在，  删除
			newIds=newIds.replace(","+val+",",",");
		}else {   //不存在    增加
			newIds=newIds+val+",";
		}
		if(newIds.length>1){
			newIds=newIds.substring(1,newIds.length-1); 
		} else{
			newIds="";
		} 
	}
	$("#"+strId,$panel).val(newIds);
}
/**
 * pids转换格式(可加参数扩展)
 * @param source
 * @return
 */
function pidsConvertStr(source){
	var str=source.replace("{0}","");//{0}
    str=str.replace(/}{/g,"-");
	str=str.substring(1,str.length-1);
	return str;
}


/**
 * 选中父节点时校验子节点是否被选中
 * @param strs	比对数据列表
 * @param str	当前值
 * @return
 */
function verifySonIsCheck(strs,str){
	var ss = strs.split(",");
	for ( var i = 0; i < ss.length; i++) {
		if(ss[i].indexOf(str+"-")!=-1){
			return true;
		}
	}
	return false;
}
/*-----end--数据搬移-------*/

/**
 * eg:物价管理  公试设置
 * @param obj
 * @return
 */
function formulaItemChange(obj){
	var $trP = $(obj).parents("tr:first");
	var selVal = $(obj).val();
	var operate =  $trP.find("select[name$='operate']");
	var param =  $trP.find("input[name$='param']");
	if(selVal==0){
		operate.val("+");
		operate.attr("disabled",true);
		param.val(0);
		param.attr("disabled",true);
	}else {
		operate.attr("disabled",false);
		param.attr("disabled",false);
	}
}
/**
 * dialog改变值带回并提交（点击另存为触发）   eg:生产模板  请输入另存为新模板的名称
 * @param thisObj
 * @param paramId
 * @param actionUrl
 * @return
 */
function backDialogValSubmit(thisObj,paramId,actionUrl){
	var $dialog = $(thisObj).parents("div.dialog");
	var paramVal = $("#"+paramId,$dialog).val();
	if(paramVal==""){
		alertMsg.warn("必须指定模板名称，且不能与当前已有的模板名称重复！");
		return false;
	}
	$.pdialog.close($dialog);//关闭
	$dialog = getCurrentPanel();//得到当前dialog
	//$("#"+paramId,$dialog).val(paramVal);
	$("#copyTmpName",$dialog).val(paramVal);//另存为新模板名
	var $form = $("form",$dialog);
	$form.attr("action",actionUrl);
	if (!$form.valid()) {
		return false;
	}
	$form.submit();
}

/*-------价格跟踪-------*/
/**
 * 价格跟踪  指定删除单位   商品
 */
function deleleByUnitOrProduct(obj,rel){
	var $obj=$(obj); 
	var url =$obj.parents("form").attr("action");
	$.pdialog.closeCurrent();  //关闭当前Dialog
	var navTab=getCurrentPanel();
	var data=$("#pagerForm",navTab).serializeArray();
	aioNavtabPartRefresh(rel,url,data);
}
/**
 * 价格跟踪  标记
 * @return
 */
function fzHasMarkOrNot(whichType){
	var navTab=getCurrentPanel();
	var selectTr=navTab.find("tbody tr").filter(".selected");
	var $this=selectTr.find("td[removeTrDb='true']");
	var base=$("#basePathId",navTab).val();
	
	if($this.attr("hasMark")||$this.attr("hasMark")=="fz_priceDiscountTrack"){
		var rel=selectTr.attr("rel");
		var type="";
		var text=$this.children("div").text();
		if(text&&text=="√"){
			type="remove";
		}else{
			type="has";
		}
		if(whichType&&whichType=="button"&&type=="has"){
			return;
		}
		jQuery.ajax({
			type:'POST',
			url:base+"/fz/priceDiscountTrack/isMark/"+type+"-"+rel,
			dataType:'json',
			timeout: 5000,
			cache: false,
			success: function(res){
				if(text&&text=="√"){
					$this.children("div").text("");
				}else{
					$this.children("div").text("√");
				}
			}
		});
	}
}
/*----end---价格跟踪-------*/



/*------om接口用户选择多个仓库---------*/
/**
 * 弹出多选择仓库
 */
function alertMutilStorageDialog(obj,base,selectIdsObj){
	var $obj=$(obj);
	var selectIds=$("#"+selectIdsObj,$obj.parent()).val();
	
	var options={};
	var str={'selectIds':selectIds};
	options.jsonData = eval(str);
	$.pdialog.open(base+"/base/storage/storageMultiOption/toDialog", "b_storage_mutil_select_dialog", "仓库多选", options);
}
/**
 * 带回
 */
function storageMultiOptionSelected(obj){
	var $dialog=getCurrentPanel();
	var $obj=$(obj);
	var nodeName=$obj[0].nodeName;
	var selectIds="";
	if(nodeName!="TR"){
		selectIds=$("#b_dialog_product_page_checkboxIds",$dialog).val();
	}
	
	if(!selectIds||selectIds==""){
		alertMsg.error("请至少选择一个")
		return false;
	}
	
	var url=$obj.attr("url");
	$.ajax({
		type:'POST',
		url:url,
		data:{selectIds:selectIds},
		dataType:"json",
		cache: false,
		success:function(data){
			$.pdialog.closeCurrent();  //关闭当前Dialog
			var omUserDialog=getCurrentPanel();
			var storageIds=data.storageIds;
			var storageFullNames=data.storageFullNames;
			$("#om_loginUser_storage_id",omUserDialog).val(storageIds);
			$("#om_loginUser_storage",omUserDialog).val(storageFullNames);
	    },
		error: DWZ.ajaxError
	});
	
	
}
/*------end--om接口用户选择多个仓库---------*/

/**
 * 导入基本信息  校验上传类型
 * @param thisObj
 * @param aimId
 * @return
 */
function aioImportExcel(thisObj,aimId){
	var $obj=$(thisObj);
	var form = $obj.parents("form");//同一form的作用域
	var aimObj = $("#"+aimId,form);
	var aimVal = aimObj.val();
	if(aimVal==undefined||$.trim(aimVal)==""){
		alertMsg.warn("请选择上传文件！");
	}else {
		var fileArr=aimVal.split("//");
		var fileTArr=fileArr[fileArr.length-1].toLowerCase().split(".");
		var filetype=fileTArr[fileTArr.length-1];
		if(filetype!="xls"&&filetype!="xlsx"){
	        alertMsg.warn("上传文件必须为Excel文件！");
		}else {
			form.submit();
		}
	}
	
}

/**
 * 单据编号设置
 * @param obj
 * @return
 */
function changeButton(obj){
	var sib = $(obj).siblings("a.buttonDisabled");
	$(obj).attr("class","buttonDisabled");
	
	sib.each(function(){
		$(this).attr("class","button");
	});
}
/*---------------------------------------end---辅助功能--------------------------------------------*/






/*------------------------------------------系统维护-----------------------------------------------*/
/*----数据备份/恢复-----*/
/**
 * 服务器恢复
 */
function sysRecoverData(url){
	var $dialog=getCurrentPanel();
	var fileName=$("#selectedObjectId",$dialog).val();
	var dataBaseNameId=$("#dataBaseNameId",$dialog).val();
	if(!fileName||fileName==""){
		alertMsg.error("请输入或选择要恢复的文件名")
		return false;
	}
	alertMsg.confirm("请确定是否要恢复数据？", {
		okCall: function(){
			$.ajax({
				type:'POST',
				url:url,
				data: {fileName:fileName,dataBaseNameId:dataBaseNameId},
				dataType:"json",
				cache: false,
				success:dialogAjaxDone1,
				error: DWZ.ajaxError
			});
		}
	});
}
/*------end--数据备份/恢复-----*/

/*-----------结存-----------*/
/**
 * 月结存tab
 */
function sysMonthEndTabChange(){
   var $panel=getCurrentPanel();
   $("#monthEndSubmit",$panel).show();
   $("#unMonthEndSubmit",$panel).hide();
   var $form=$("form",$panel);
   $form.attr("action",$("#monthEndSubmit",$panel).attr("href1"));
}
/**
 * 反月结存tab
 */
function sysUnMonthEndTabChange(){
	var $panel=getCurrentPanel();
	$("#monthEndSubmit",$panel).hide();
	$("#unMonthEndSubmit",$panel).show();
	var $form=$("form",$panel);
	$form.attr("action",$("#unMonthEndSubmit",$panel).attr("href"));
}
/**
 * 月结存   提交表单前验证 月结存时间
 */
function submitBeforeValidate(obj,actionType){
	var flag=true;
	var $form=$(obj).parents("form");
	if(actionType=="editUnitInit"){   //修改单位期初应收应付
		var beginGetMoney=$form.find("input[name='unit.beginGetMoney']").val();  //期初应收
		var beginPayMoney=$form.find("input[name='unit.beginPayMoney']").val();  //期初应付
		if(!beginGetMoney){
			beginGetMoney=0;
		}
        if(!beginPayMoney){
        	beginPayMoney=0;
		}
        if(beginGetMoney!=0&&beginPayMoney!=0){
        	alertMsg.error("期初应收与期初应付不能同时有值");
        	flag=false;
        }
	}else if(actionType=="sysMonthEnd"){  //月结存 期间数
		//验证本月结束
		var $dialog=getCurrentPanel();
		var $form=$("form",$dialog);
		var startDate=$("#startDate",$form).val();
		var endDate=$("#endDate",$form).val();
		if(!endDate||endDate==""){
		   alertMsg.error("月结存时间小于开始时间");	
		   flag=false;
		}else{
		   flag=checkEndTime(startDate,endDate);
		   if(flag==false){
			   alertMsg.error("月结存时间小于开始时间");
			   flag=false;
		   }
		}
		
		//月结期间
		var $monthCountObj=$("#monthCount",$form);
	    var monthCount=$monthCountObj.val();
	    if(!monthCount){
	    	monthCount=0;
	    }
		if($monthCountObj.attr("readonly")){
			if(monthCount==12){
				alertMsg.error("月结期间已为11，不能做月结存！");	
				flag=false;
			}
		}else{
			if(monthCount>11||monthCount<1){
				alertMsg.error("月结期间只能为1到11整数");	
				flag=false;
			}
		}
	}
	if(flag){
		$form.submit();
	}
}
/**
 * 系统不使用多年账
 */
function sysOneYearEnd(){
	var $dialog=getCurrentPanel();
	$("#hasOneYearEnd",$dialog).show();
	$("#hasManyYearEnd",$dialog).hide();
		
}
/**
 * 系统使用多年账
 */
function sysManyYearEnd(){
	var $dialog=getCurrentPanel();
	$("#hasOneYearEnd",$dialog).hide();
	$("#hasManyYearEnd",$dialog).show();
}
/*------end-结存------------*/


/*-------系统维护->用户及权限设置->权限分类定位----------*/
/**
 * 系统维护->用户及权限设置->权限分类定位
 * @param id
 * @param ul
 * @return
 */
function locationObj(id,ul){
	var objVal = $("#"+id+":visible").val();
	if($.trim(objVal)==""){
		return;
	}
	//var $ul = $("#"+ul+":visible");
	var $ul = $("ul[id='"+ul+"']:visible");
	var $selLi = $ul.children(".selected");
	var $siblings = $selLi.siblings();
	var $div = $ul.parents("div:first");
	$siblings.each(function(){
		var text = $(this).find("a").text();
		if(text.indexOf(objVal)>=0){
			var obj = $(this);
			keydownSelectLi(obj);
			
			var prevs=$(obj).prevAll();
			var height = 0;
			$(prevs).each(function(key,topTr){
				height=height+$(topTr).height();
			});
			$div.scrollTop(height+10);
			return;
		}
	});
}
/**
 * 系统维护->用户及权限设置->权限分类定位  选中批定的li
 */
function keydownSelectLi(obj){
	  var li = $(obj);
	  var siblings = li.siblings();
	  li.addClass("selected");
	  siblings.removeClass("selected");
	  var href = li.find("a").attr("href");
	  href = unescape(href).replaceTmById($(li).parents(".unitBox:first"));
	  var rel = li.find("a").attr("rel");
	  if (rel) {
			var $rel = $("#"+rel);
			$rel.loadUrl(href, {}, function(){
				$rel.find("[layoutH]").layoutH();
			});
	  }
}

/**
 * eg:用户及权限设置
 * @param obj
 * @param isRel
 * @return
 */
function checkRelevanceTable(obj,isRel){
	var $Panel= $(obj).parents("div.grid:first");
	var name = $(obj).attr("name");
	if($(obj).attr("checked")){
		if(name=="*"){
			$Panel.find("input[type='checkbox']:not(:disabled)").attr("checked",true);
		}else{
			if(isRel){
				$Panel.find("input[type='checkbox'][name*='"+name+"']:not(:disabled)").attr("checked",true);
				$Panel.find("input[type='checkbox'][name*='see']:not(:disabled)").attr("checked",true);
			}else{
				$(obj).parents("tr:first").find("input[type='checkbox'][name*='see']:not(:disabled)").attr("checked",true);
			}
			
		}
	}else{
		if(name=="*"){
			$Panel.find("input[type='checkbox']:not(:disabled)").removeAttr("checked");
		}else{
			if(isRel){
				$Panel.find("input[type='checkbox'][name*='"+name+"']:not(:disabled)").removeAttr("checked");
				if(name=="see"){
					$Panel.find("input[type='checkbox']:not(:disabled)").removeAttr("checked");
				}
			}else{
				if(name.indexOf("see")>0){
					$(obj).parents("tr:first").find("input[type='checkbox']:not(:disabled)").removeAttr("checked");
				}
			}
			
		}
	}
	//提交保存选择数据
	var $parent = getCurrentPanel();
	var userId  = $parent.find("input#userId").val();
	var url =  $parent.find("form#pageForm").attr("action");
	
    var permission = "";
	$Panel.find("input[type='checkbox'][name^='permission']:checked").each(function(){
		permission+=$(this).val()+",";
		
	});
	var noPermission = "";
	$Panel.find("input[type='checkbox'][name^='permission']:not(:checked)").each(function(){
		noPermission+=$(this).val()+",";
		
	});
	$.ajax({
		type:'POST',
		url:url,
		dataType:"json",
		data:{'userId':userId,'permission':permission,'noPermission':noPermission},
		cache: false,
		error: DWZ.ajaxError
	});
}
/**
 * 权限设置用户点击
 * @param obj
 * @param grade
 * @return
 */
function trRelevanceLi(obj,grade,loginGrade){
	var $parent = getCurrentPanel();
	var a = $(obj);
	var targetTr = $(obj).attr("target");
	var relTr  = $(obj).attr("rel");
	if(targetTr){
		$parent.find("#"+targetTr).val(relTr);
	}
	var $ul  = $parent.find("ul[target='ajax']");
	$ul.each(function(){
		var $li  = $(this).children(".selected:first");
		$li.children().click();
	});
	
	if(grade!=1){
		$parent.find("#deleteUserHide").hide();
		$parent.find("#deleteUserShow").show();
		if(!loginGrade||loginGrade==1){
			$parent.find("#passUserHide").hide();
			$parent.find("#passUserShow").show();
		}
		$parent.find("#copyUserHide").hide();
		$parent.find("#copyUserShow").show();
		$parent.find("#editStatusHide").hide();
		$parent.find("#editStatusShow").show();
		$parent.find("#allPermissionHide").hide();
		$parent.find("#allPermissionShow").show();
		$parent.find("#noPermissionHide").hide();
		$parent.find("#noPermissionShow").show();
	}else{
		$parent.find("#deleteUserHide").show();
		$parent.find("#deleteUserShow").hide();
		$parent.find("#passUserHide").show();
		$parent.find("#passUserShow").hide();
		$parent.find("#copyUserHide").show();
		$parent.find("#copyUserShow").hide();
		$parent.find("#editStatusHide").show();
		$parent.find("#editStatusShow").hide();
		$parent.find("#allPermissionHide").show();
		$parent.find("#allPermissionShow").hide();
		$parent.find("#noPermissionHide").show();
		$parent.find("#noPermissionShow").hide();
	}
}

/*-----end--系统维护->用户及权限设置->权限分类定位----------*/


/*-------基本信息授权----------*/
/**
 * 基本信息授权 点击td
 */
function tdRelevanceTr(obj,id){
	var $tr = $(obj).parents("tr:first");
	$tr.click();
	var $parent = getCurrentPanel();
	$parent.find("#"+id).click();
}
/*-----end--基本信息授权----------*/

/**
 * 保存过账之后打印
 */
function afterSavePrint(json){
	
	if(json.message){
		alertMsg.info(json.message);
		
		if(!json.callbackType)return false;
	}
	
	if(typeof(pwData)=='undefined' || typeof(pwData.WriteData)=='undefined'){
		alertMsg.warn('打印插件未安装!');
		return false;
	}
	var postForm = json.postForm;
	var url = json.printUrl;
	var printType = json.printType;
	
	//add返回的参数
	var callbackType = json.callbackType;
	var navTabId = json.navTabId;
	var isClose = json.isClose;
	
	var $parent  =  getCurrentPanel();//取值区域
	var $pagerForm = $("#"+postForm, $parent);
	var jsonData = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
	var item1={};
	item1['name'] = "ifSave"; 
	item1['value'] = false; 
	jsonData.push(item1);
	
	var item2={};
	item2['name'] = "callbackType"; 
	item2['value'] = callbackType; 
	jsonData.push(item2);
	var item3={};
	item3['name'] = "navTabId"; 
	item3['value'] = navTabId; 
	jsonData.push(item3);
	var item4={};
	item4['name'] = "isClose"; 
	item4['value'] = isClose; 
	jsonData.push(item4);
	
	if (! $.isFunction(printType)) printType = eval('(' + printType + ')');
	
	$.ajax({
		type:'POST',
		url:url,
		dataType:"json",
		data:jsonData,
		cache: false,
		success: printType,
		error: DWZ.ajaxError
	});
	return false;
}
/*---------------------------------------end---系统维护--------------------------------------------*/






/*------------------------------------------公共方法-----------------------------------------------*/
/*---------做单选择商品  会计科目控件--------*/
/**
 * 商品多选框全选反选
 * @param obj
 * @param pageParaId
 * @param searchParaId
 * @return
 */
function checkAllVersa(obj,pageParaId,searchParaId){
	var ids="";
	if($(obj).attr("checked")){
		  var parentTrs=$("#b_product_dialog_tbody > tr");
		  for ( var i = 0; i < parentTrs.length; i++) {
			  ids+=$(parentTrs[i]).attr("rel");
			  if(i<parentTrs.length-1){
				  ids+=",";
			  }
		  }
	}
	$("#"+pageParaId).val(ids);  //当前页选择checkbox  id
}
/**
 * 商品多选框全选反选
 * @param obj
 * @param checkObjIds
 * @param targetId
 * @return
 */
function checkAllAndVersa(obj,checkObjIds,targetId){
	var ids="";
	if($(obj).attr("checked")){
		  var parentTrs=$("#"+targetId+" > tr");
		  for ( var i = 0; i < parentTrs.length; i++) {
			  ids+=$(parentTrs[i]).attr("rel")+",";
		  }
	}
	$("#"+checkObjIds,getCurrentPanel()).val(ids);  //当前页选择checkbox  id
}
/**
* 商品dialog   点击checkbox事件
* obj              当前控件对象
* pageParaId       翻页记录 新checkbox  ids  分页参数 
* searchParaId     查询参数
*/
function getProductIdList(obj,pageParaId,searchParaId){   //点击修改URL
  var newProductIds=$("#"+pageParaId,getCurrentPanel()).val();  //当前页选择checkbox  id
  $this=$(obj);
  var parentTr=$this.parents("tr:first");
  var parentTrObjPrdId=$(parentTr).attr("rel");
  if(newProductIds==""){  //
	  newProductIds=parentTrObjPrdId;
  }else{
	  newProductIds=","+newProductIds+",";
		  if(newProductIds.indexOf(","+parentTrObjPrdId+",")>-1){  //存在，  删除
         newProductIds=newProductIds.replace(","+parentTrObjPrdId+",",",");
      }else {   //不存在    增加
         newProductIds=newProductIds+parentTrObjPrdId+",";
      }
		if(newProductIds.length>1){
			newProductIds=newProductIds.substring(1,newProductIds.length-1); 
		} else{
			newProductIds="";
		} 
      
  }
  $("#"+pageParaId,getCurrentPanel()).val(newProductIds);    //查询传参数  
  $("#"+searchParaId,getCurrentPanel()).val(newProductIds);  //翻页传参数  
}
/*-------end--做单选择商品  会计科目控件--------*/

/**
 * 拼接checkBox的值   手动增加复选框 （这个dwz本身就有了）
 * @param obj
 * @param checkObjIds
 * @return
 */
function checkItem(obj,checkObjIds){
	var ids=$("#"+checkObjIds,getCurrentPanel()).val();
	if($(obj).attr("checked")){
		ids=ids+$(obj).val()+","
	}else{
		var pattern = $(obj).val()+",";
		ids=ids.replace(new RegExp(pattern),"");
	}
	$("#"+checkObjIds,getCurrentPanel()).val(ids);
}

/*---------做单ajax请求后台--------*/
/**
 * 根据带回的往来单位ID和类型查询允许换货金额
 * @param unitId 单位ID
 * @param type   类型(in,out)
 * @return
 */
function ajaxGetAllowMoney(basePath,unitId,recodeTime,type){
	var money=0;
	$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/allowBarterMoney",
		   dataType: "json",
		   data: {unitId:unitId,recodeTime:recodeTime,type:type},
		   async: false,
		   success: function(json){
			   money = json.money;
		   }
	});
	return money;
}
/**
 * 换货单超出允许金额提示
 * 确认提交
 * @return
 */
function confirmAllow(){
	var $parent = navTab.getCurrentPanel();
	var $pdialog=$.pdialog.getCurrent();
	$.pdialog.closeCurrent();  //关闭当前Dialog
	$("input#confirmAllow",$parent).val(false);
	$("form:first",$parent).submit(); //单据提交
}
/**
 *  指定仓库   1.移动加权（成本价格-》最近最近进价  转换成大单位价格）   2.手工指定法  （最近进货价格  本身就是大单位价格）
 */
function getProductBackPrice(basePath,storageId,productId,trcostArith,selectUnitId){
    var avgPrice;
	//其它商品ajax请求得到平均成本单价
	$.ajax({
	   type: "POST",
	   url: basePath+"/stock/stock/avgPiceOrLastBugPriceBySelectUnitId",
	   dataType: "json",
	   data: {storageId:storageId,productId:productId,selectUnitId:selectUnitId,costArith:trcostArith},
	   async: false,
	   success: function(json){
	      if($.isNumeric(json.avgPrice)){
	    	  avgPrice=json.avgPrice;
	      }
	   }
	});
	return avgPrice;
}
/**
 * 根据带回的仓库ID查询多商品库存数量
 * @param storageId 	仓库ID
 * @param productIds    商品id数组
 * @return
 */
function ajaxGetSckAmountByStorageId(basePath,storageId,productIds){
	var recode={};
	$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/getManyProSckAmount",
		   dataType: "json",
		   data: {storageId:storageId,productIds:productIds},
		   async: false,
		   success: function(json){
			   recode = json.list;
		   }
	});
	return recode;
}
/**
 * 根据带回的仓库ID查询多商品库存
 * @param storageId 	仓库ID
 * @param productIds    商品id数组
 * @return
 */
function ajaxGetStock(basePath,storageId,productIds){
	var recode={};
	$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/getManyProSck",
		   dataType: "json",
		   data: {storageId:storageId,productIds:productIds},
		   async: false,
		   success: function(json){
			   recode = json.list;
		   }
	});
	return recode;
}
/*-------end--做单ajax请求后台--------*/

/**
 * 下拉框提交   eg:报表过滤条件值改变
 * @param obj
 * @param aimRel
 * @param aimId
 * @return
 */
function selectPost(obj,aimRel,aimId){
	var $parent  =  $.pdialog.getCurrent() || navTab.getCurrentPanel();//取值区域
	if($parent.is(":hidden")){
		$parent= navTab.getCurrentPanel();
	};
	$obj=$(obj);
	var $pagerForm = $("#pagerForm", $parent);
	$pagerForm.find("[id='"+aimId+"']").val($obj.val());
	
	var args = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
	navTabPageBreak(args, aimRel);
}
/*------------------------------------end------公共方法-----------------------------------------------*/




 

