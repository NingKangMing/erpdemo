//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/**
 * 单据回车调用td双击
 * @param event
 * @param $this
 * @param $parent
 * @return
 */
function tdDblclick(event,$this,$parent){
	if($this.attr("hasMark")||$this.attr("hasMark")=="fz_priceDiscountTrack"){
		//价格跟踪标记
		fzHasMarkOrNot("table");
		return;
	}
	
	var alertDialog=true;
	var productId=$this.attr("productId");
	var currentPrdId=$this.parent().find("input[cname='"+productId+"']").val();  //商品ID
	
	var cTarget = $this.attr("cTarget");
	if(!cTarget){  //是否弹出dialog
		return;
	}
	
	//关联ID 参数
	var relevancyIds=$this.attr("relevancyIds");
	var verify=$this.attr("verify");//需要校验的参数
	var	verifyText=$this.attr("verifyText");//校验提示文字
	var jsonData={};//提交的参数Data
	if(relevancyIds){
		//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
		var str="({";
		relevancyIdsArray=relevancyIds.split(",");
		for(var i=0;i<relevancyIdsArray.length;i++){
			/*var key=relevancyIdsArray[i];
			var val=$parent.find("[id='"+relevancyIdsArray[i]+"']").val();*/
			var valObj=$parent.find("[id='"+relevancyIdsArray[i]+"']");
			var val=valObj.val();
			var key=valObj.attr("name");
			if(!key){
				key=relevancyIdsArray[i];
			}
			
			//校验
			if(key==verify&&(val==""||val==0)){
				alertMsg.info(verifyText);
				return false;
			}
			
			str+="'"+key+"':'"+val+"'";
			if(i!=relevancyIdsArray.length-1){
				str+=",";
			}
		}
		str+="})";
		jsonData = eval(str);
	}
	
	
	//单位，数量 是否弹出
	var hasDialog=$this.attr("hasDialog");
	if(hasDialog&&hasDialog=="dialogNotNull"){  //销售订单  没选择商品      单位，数量禁用双击事件
		var value=$this.find("input:eq(0)").val();
		if(!currentPrdId || currentPrdId==""){
			return;
		}
	}
	
	if(cTarget=="dialog"){
		var url = $this.attr("url");
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
		options.param = $this.attr("param") || "";
		
		options.jsonData = jsonData;//提交参数
		
		//一个页面两个表格带回所需传参确定是哪个表格
        var $tbody = $this.parents("tbody:first");
        var $parent = navTab.getCurrentPanel();
        if($tbody.attr("type")){
        	$parent.find("input#billType").val($tbody.attr("type"));
        }
      //end一个页面两个表格带回所需传参确定是哪个表格
        
		//var url = unescape($this.attr("url")).replaceTmById($(event.target).parents(".unitBox:first"));
		var url = $this.attr("url");
		DWZ.debug(url);
		
		//var cname=$this.find("input[type='text']:eq(0)").attr("cname");
		var cname=$this.attr("cname"); //accountsCode   accouontCode
		if(cname&&cname=="accountsCode"){ //科目编号
			var code=$this.parent().find("input[cname='accountsCode']").val();  
			var oldCode=$this.parent().find("input[cname='oldAccountsCode']").val();
			var result=billAccountsDbClick($this,url,oldCode,code==undefined?"":code,"accountsCode");//单据商品编号  全名   双击
			oldCode=$this.parent().find("input[cname='oldAccountsCode']").val();
			if(!oldCode){
				$this.parent().find("input[cname='accountsCode']").val("");
			}
			alertDialog=result.substring(0,result.indexOf("-"));
			url=result.substring(result.indexOf("-")+1,result.length);
		}else if(cname&&cname=="accountsFullName"){//科目全称
			var fullName=$this.parent().find("input[cname='accountsFullName']").val();  
			var oldFullName=$this.parent().find("input[cname='oldAccountsFullName']").val();
			var result=billAccountsDbClick($this,url,oldFullName,fullName==undefined?"":fullName,"accountsFullName");//单据商品编号  全名   双击
			oldFullName=$this.parent().find("input[cname='oldAccountsFullName']").val();
			if(!oldFullName){
				$this.parent().find("input[cname='accountsFullName']").val("");
			}
			alertDialog=result.substring(0,result.indexOf("-"));
			url=result.substring(result.indexOf("-")+1,result.length);
		}else if(cname&&cname=="code"){  //商品编号
			var code=$this.parent().find("input[cname='code']").val();  
			var oldCode=$this.parent().find("input[cname='oldCode']").val();
			var result=billProdcutDbClick($this,url,currentPrdId,oldCode,code==undefined?"":code,"code");//单据商品编号  全名   双击
			oldCode=$this.parent().find("input[cname='oldCode']").val();
			if(!oldCode){
				$this.parent().find("input[cname='code']").val("");
			}
			alertDialog=result.substring(0,result.indexOf("-"));
			url=result.substring(result.indexOf("-")+1,result.length);
			var unitId=$this.attr("unitId");
			var currentUnitId=$parent.find("#"+unitId+"").val();  //单位Id
			
			var billType = $this.attr("billType");//单据类型
			var item={};  
			item['unitId'] = currentUnitId; 
			if(billType){
				item['billType'] = billType; 
			}
			options.jsonData=item;
		}else if(cname&&cname=="fullName"){ //商品全名
			var fullName=$this.parent().find("input[cname='fullName']").val();  
			var oldFullName=$this.parent().find("input[cname='oldFullName']").val();
			var result=billProdcutDbClick($this,url,currentPrdId,oldFullName,fullName==undefined?"":fullName,"fullName");//单据商品编号  全名   双击
			oldFullName=$this.parent().find("input[cname='oldFullName']").val();
			if(!oldFullName){
				$this.parent().find("input[cname='fullName']").val("");
			}
			alertDialog=result.substring(0,result.indexOf("-"));
			url=result.substring(result.indexOf("-")+1,result.length);
			
			var unitId=$this.attr("unitId");
			var currentUnitId=$parent.find("#"+unitId+"").val();  //单位Id
			
			var billType = $this.attr("billType");//单据类型
			var item={};  
			item['unitId'] = currentUnitId; 
			if(billType){
				item['billType'] = billType; 
			}
			options.jsonData=item;
		}else if(currentPrdId!=""&&currentPrdId>0){//弹出   单位，数量
			if(cname=="baseUnit"){  //单位
				var baseUnitId=$this.parent().find("input[cname='selectUnitId']").val(); 
				url=url+currentPrdId+"-"+baseUnitId;
				var item={};  
				item['unitId'] = $("#"+$this.attr("unitId"),$parent).val();
				item['billType'] = $this.attr("billType");
				options.jsonData=item;
			}else if(cname=="amount"){//数量
				var baseUnit=$this.attr("baseUnit");
				var selectUnitId=$this.parent().find("input[cname='"+baseUnit+"']").val();  
				var currentObjVal=$this.find("input").val()||$this.find("div").text();  
				//url=url+currentPrdId+"-"+currentObjVal+"-"+encodeURI(selectUnitId);
				url=url+currentPrdId+"-"+encodeURI(selectUnitId);
				if(isNaN(currentObjVal)){
					return false;
				}
				var item={};  
				item['amount'] = currentObjVal; 
				options.jsonData=item;
			}else if(cname=="storageCode"){ //仓库编号（销售单）
				var code=$this.parent().find("input[cname='storageCode']").val();  
				var oldCode=$this.parent().find("input[cname='oldStorageCode']").val();
				var result=billStorageDbClick($this,url,currentPrdId,oldCode,code,"code");//仓库编号  全名   双击
				oldCode=$this.parent().find("input[cname='oldStorageCode']").val();
				if(!oldCode){
					$this.parent().find("input[cname='storageCode']").val("");
				}
				alertDialog=result.substring(0,result.indexOf("-"));
				url=result.substring(result.indexOf("-")+1,result.length);
			}else if(cname=="storageFullName"){ //仓库全名（销售单）
				var storageFullName=$this.parent().find("input[cname='storageFullName']").val();  
				var oldStorageFullName=$this.parent().find("input[cname='oldStorageFullName']").val();
				var result=billStorageDbClick($this,url,currentPrdId,oldStorageFullName,storageFullName,"fullName");//仓库编号  全名   双击
				oldStorageFullName=$this.parent().find("input[cname='oldStorageFullName']").val();
				if(!oldStorageFullName){
					$this.parent().find("input[cname='storageFullName']").val("");
				}
				alertDialog=result.substring(0,result.indexOf("-"));
				url=result.substring(result.indexOf("-")+1,result.length);
			}else if(cname==options.param+"StorageCode"){//多仓库：同价调拨单
				
				var code=$this.parent().find("input[cname='"+options.param+"StorageCode']").val();  
				var oldCode=$this.parent().find("input[cname='old"+options.param+"StorageCode']").val();
				var result=billStorageDbClick($this,url,currentPrdId,oldCode,code,"code");//仓库编号  全名   双击
				oldCode=$this.parent().find("input[cname='old"+options.param+"StorageCode']").val();
				if(!oldCode){
					$this.parent().find("input[cname='"+options.param+"StorageCode']").val("");
				}
				options.jsonData = {"case":options.param};
				alertDialog=result.substring(0,result.indexOf("-"));
				url=result.substring(result.indexOf("-")+1,result.length);
			}else if(cname==options.param+"StorageFullName"){ //多仓库全名：同价调拨
				var storageFullName=$this.parent().find("input[cname='"+options.param+"StorageFullName']").val();  
				var oldStorageFullName=$this.parent().find("input[cname='old"+options.param+"StorageFullName']").val();
				var result=billStorageDbClick($this,url,currentPrdId,oldStorageFullName,storageFullName,"fullName");//仓库编号  全名   双击
				oldStorageFullName=$this.parent().find("input[cname='old"+options.param+"StorageFullName']").val();
				if(!oldStorageFullName){
					$this.parent().find("input[cname='"+options.param+"StorageFullName']").val("");
				}
				options.jsonData = {"case":options.param};
				alertDialog=result.substring(0,result.indexOf("-"));
				url=result.substring(result.indexOf("-")+1,result.length);
			}else if(cname=="price"){
				var baseUnit=$this.attr("baseUnit");
				var selectUnitId=$this.parent().find("input[cname='"+baseUnit+"']").val();
				var $navTab=navTab.getCurrentPanel();
				var storageId=$this.attr("storageId");
				var currentStorageId=$this.parent().find("input[cname='"+storageId+"']").val();  //仓库ID
				if(currentStorageId==undefined||currentStorageId==""||currentStorageId=="0"){
					currentStorageId=$navTab.find("#"+storageId).val();
				}
				var item={};  
				item['productId'] = currentPrdId;
				item['storageId'] = currentStorageId;
				item['selectUnitId'] = selectUnitId;
				options.jsonData=item;
			}else{
				//TODO
			}
		}else if(cname&&(cname=="produceDate"||cname=="produceEndDate"||cname=="batch")){  //  手工指定法再次选择批次，价格，生产日期
			var tr=$this.parent();
			var manSel = $this.attr("manSel");
			var costArith= tr.find("input[cname='costArith']").val(); 
			if(manSel&&manSel=="true"&&costArith&&costArith=="4"){  //判断是否弹出批次商品
				var whichCallBack=$("#whichCallBack",$parent).val();
				var takeStock = $("#module",$parent).val();
				if(takeStock && whichCallBack=="takeStockBill"){
					whichCallBack = takeStock;
				}
				var storageId;
				var productId;
				var tbodyId=$this.attr("tbodyId")
				storageId=tr.find("input[cname='storageId']").val();
				if(!storageId){
					storageId=tr.find("input[cname='outStorageId']").val();
				}
				var $navTab=navTab.getCurrentPanel();
				var basePath=$navTab.find("#basePathId").val();
				if(storageId==undefined||storageId==""||storageId=="0"){
					storageId=$navTab.find("#"+$this.attr("totalStorageId")).val();
				}
				productId=tr.find("input[cname='productId']").val();
				var trIndex=tr.index();
				//打一个dialog
				var options = {};
			    options.width="540";
			    options.height="350";
			    options.param={module:whichCallBack,storageId:storageId,productId:productId,trIndex:trIndex,tbodyId:tbodyId};
			    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "xsd_selPrdManSelPrd_dialog", "商品库存批次表", options);
			    return false;
			}else {
				return false;
			}
			
		}
		
		if(alertDialog=="true"||alertDialog==true){
			if (!url.isFinishedTm()) {
				alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
				return false;
			}
			$.pdialog.open(url, rel, title, options);
		}
		
	}
	return false;
}
/**
 * 单据商品编号  全名   双击
 * @param $this
 * @param url
 * @param currentPrdId
 * @param oldVal
 * @param val
 * @param attr
 * @return
 */
function billProdcutDbClick($this,url,currentPrdId,oldVal,val,attr){
	var $parent = getCurrentPanel();
	var billStorageId=$this.attr("storageId"); //头表仓库Id
	var storageId=$this.parent().find("input[cname='storageId']").val();  //1.tr行仓库ID    2.头表仓库Id
	if(!storageId||storageId==""||storageId==0){
		storageId=$parent.find("#"+billStorageId).val();
		storageId=storageId==undefined?0:storageId;
	}
	
	var alertDialog=true;
	var editUrl=$this.attr("editUrl");
	var enterSearch=$this.attr("enterSearch");
	if(val!=""&&val!=oldVal){  //查询
		var billType=$("#whichCallBack",$parent).val();//单据类型
		var searchUrl=$this.attr("searchUrl")+billType+"-"+attr+"-"+decodeURI(val)+"-"+storageId;
		
		//关联ID 参数
		var relevancyIds=$this.attr("relevancyIds");
		var verify=$this.attr("verify");//需要校验的参数
		var	verifyText=$this.attr("verifyText");//校验提示文字
		var relevancyArea=getCurrentPanel();//当前活动面板
		var jsonData;//提交的参数Data
		if(relevancyIds){
			//var str="({ moudle: 'storageProData', handleDate: '', storageId: ''})";
			var str="({";
			relevancyIdsArray=relevancyIds.split(",");
			for(var i=0;i<relevancyIdsArray.length;i++){
				/*var key=relevancyIdsArray[i];
				var val=relevancyArea.find("[id='"+relevancyIdsArray[i]+"']").val();*/
				var valObj=relevancyArea.find("[id='"+relevancyIdsArray[i]+"']");
				var val=valObj.val();
				var key=valObj.attr("name");
				if(!key){
					key=relevancyIdsArray[i];
				}
				//校验
				if(key==verify&&(val==""||val==0)){
					alertMsg.info(verifyText);
					return false;
				}
				
				str+="'"+key+"':'"+val+"'";
				if(i!=relevancyIdsArray.length-1){
					str+=",";
				}
			}
			str+="})";
			jsonData = eval(str);
			
		}else{
			var unitId=$this.attr("unitId");
			var currentUnitId=$parent.find("#"+unitId+"").val();  //单位Id
			
			var billType = $this.attr("billType");//单据类型
			var item={};  
			item['unitId'] = currentUnitId; 
			if(billType){
				item['billType'] = billType; 
			}
			jsonData = item;
		}
		
		$.ajax( {
			type : 'GET',
			url : searchUrl,
			data : jsonData,
			dataType : 'json',
			timeout : 50000,
			async:false,
			cache : false,
			success : function(json) {
		       if(json.success==200){  //有一条记录
		    	   alertDialog=false;
		    	   //多个带回，一个带回
		    	   billProductCallBack(json.whichCallBack,$(json.obj),$this);
		       }else{   //弹出商品窗体
		    	   url+="search-"+attr+"-"+decodeURI(val);
		       }
		       $this.addClass("selected");
		    }
		});
	}else if(oldVal!=""&&val==oldVal){ //商品修改弹出商品
		var productSupId=$this.parent().find("input[cname='"+$this.attr("productSupId")+"']").val();
		url=editUrl+currentPrdId+"-"+productSupId+"-"+storageId;
	}else if(val==""){     //空弹出商品
		url+="toDialog-"+storageId;
	}
	return alertDialog+"-"+url;
}
/**
 * 单据仓库编号  全名   双击
 * @param $this
 * @param url
 * @param currentPrdId
 * @param oldVal
 * @param val
 * @param attr
 * @return
 */
function billStorageDbClick($this,url,currentPrdId,oldVal,val,attr){
	var alertDialog=true;
	if(val==""){
		url=url+"-"+currentPrdId;
	}else if(val!=""&&val!=oldVal){  //查询
		var whichCallBack=$("#whichCallBack",navTab.getCurrentPanel()).val();//单据类型
		var caseVal = $this.attr("param")||"";//多仓库用于区分前缀参数
		var searchUrl=$this.attr("searchUrl")+whichCallBack+"-"+currentPrdId+"-"+attr+"-"+encodeURI(val);
		$.ajax( {
			type : 'GET',
			url : searchUrl,
			dataType : 'json',
			timeout : 50000,
			async:false,
			cache : false,
			success : function(json) {
		       if(json.success==200){  //有一条记录
		    	   alertDialog=false;
		    	
		    	   billStorageCheckBack(billType,json.obj.storage,caseVal);
		       }else{   //弹出仓库窗体
		    	   url=url.substring(0,url.lastIndexOf("/")+1);
		    	   val=encodeURI(val);
		    	   url+="search-"+attr+"-"+val;
		       }
		       $this.addClass("selected");
		    }
		});
	}else if(oldVal!=""&&val==oldVal){ //商品修改弹出商品
		var editUrl=$this.attr("editUrl");
		var param = $this.attr("param");
		var storageId=$this.parent().find("input[cname='"+$this.attr("storageId")+"']").val();
		var storageSupId=$this.parent().find("input[cname='"+$this.attr("storageSupId")+"']").val();
		url=editUrl+currentPrdId+"-"+storageId+"-"+storageSupId;
	}
	return alertDialog+"-"+url;
}
/**
 * 单据会计科目编号  全名   双击
 * @param $this
 * @param url
 * @param oldVal
 * @param val
 * @param attr
 * @return
 */
function billAccountsDbClick($this,url,oldVal,val,attr){
	var alertDialog=true;
	var billType=$("#whichCallBack",navTab.getCurrentPanel()).val();//单据类型
	if(val==""){
		url=url+"toDialog-"+billType+"-select";
	}else if(val!=""&&val!=oldVal){  //查询
		//var billType=$("#whichCallBack",navTab.getCurrentPanel()).val();//单据类型
		var searchUrl=$this.attr("searchUrl")+billType+"-"+attr+"-"+encodeURI(val);
		$.ajax( {
			type : 'GET',
			url : searchUrl,
			dataType : 'json',
			timeout : 50000,
			async:false,
			cache : false,
			success : function(json) {
		       if(json.success==200){  //有一条记录
		    	   alertDialog=false;
		    	   billAccountsCheckBack(json["whichCallBack"],$(json["obj"]))
		       }else{   //弹会计科目窗体
		    	   url=url.substring(0,url.lastIndexOf("/")+1);
		    	   val=encodeURI(val);
		    	   url+="search-"+billType+"-"+attr+"-"+val+"-select";
		       }
		       $this.addClass("selected");
		    }
		});
	}else if(oldVal!=""&&val==oldVal){ //会计科目修改弹出商品
		var editUrl=$this.attr("editUrl");
		var accountsId=$this.parent().find("input[cname='accountsId']").val();
		var accountsSupId=$this.parent().find("input[cname='accountsSupId']").val();
		url=editUrl+accountsId+"-"+accountsSupId+"-"+billType;
	}
	return alertDialog+"-"+url;
}


