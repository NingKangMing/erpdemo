//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*------------------------------------------具体商品回调  初始化-----------------------------------------------*/
/**
 * 销售订单，进货订单
 * @param json
 * @param $this
 * @return
 */
function billCallBackNotDialog(json,$this){
	//$this  是写商品编号回车获取当前td  与选中的td是一样
	var $parent = navTab.getCurrentPanel();
	var discount = $parent.find("#discounts").val();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  $(selectTd).parent();
	for(var i=0;i<json.length;i++){
		var object=json[i];
	    //selectTr=filterTr(selectTr);//当前行是否有商品
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		
	    selectTr.find("input[cname='amount']").val(1);//数量
	    var currentDiscount = discount;
		if(discount!=""){//折扣
			selectTr.find("input[cname='discount']").val(discount);
		}else{
			selectTr.find("input[cname='discount']").val(object.discount);
			currentDiscount = object.discount;
		}
		selectTr.find("input[cname='price']").val(object.price);//单价
        selectTr.find("input[cname='money']").val(object.price);//总金额
        var discountPrice = "";
        if($.isNumeric(currentDiscount*object.price)){
        	discountPrice = round(currentDiscount*object.price,4);
        }
        selectTr.find("input[cname='discountPrice']").val(discountPrice);//折扣单价
        selectTr.find("input[cname='discountMoney']").val(discountPrice);//折扣金额
        if(selectTr.find("input[cname='discount']")){
        	selectTr.find("input[cname='taxPrice']").val(discountPrice);//含税单价
            selectTr.find("input[cname='taxMoney']").val(discountPrice);//含税金额
        }else{
        	selectTr.find("input[cname='taxPrice']").val(object.price);//含税单价
            selectTr.find("input[cname='taxMoney']").val(object.price);//含税金额
        }
        
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
        if(!$.isNumeric(object.price) || object.price==0){
	    	selectTr.find("input[cname='status']").val("赠品");//状态
	    }
       // prodcutHiddenAttr(selectTr,object);//商品隐藏字段
        showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在中显示出来
        selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
//	calculateFooter($parent); //合计注脚
}
/**
 * 销售单  进货退货   同价调拨单   变价调拨单
 * @param json
 * @param $this
 * @return
 */
function xsdBillCallBackNotDialog(json,$this){
	//billCallBackNotDialog(json,$this);
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var whichCallBack=$("#whichCallBack",$parent).val();
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	var discount = $parent.find("#discounts").val();
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
		/*---销售订单-------*/
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		
	    selectTr.find("input[cname='amount']").val(1);//数量
	    var currentDiscount = discount;
		if(discount!=""){//折扣
			selectTr.find("input[cname='discount']").val(discount);
		}else{
			selectTr.find("input[cname='discount']").val(object.discount);
			currentDiscount = object.discount;
		}
		if(whichCallBack=="parityAllot"){//同价调拨
			selectTr.find("input[cname='price']").val(object.costPrice);//单价
	        selectTr.find("input[cname='money']").val(object.costPrice*1);//总金额
	        if(!$.isNumeric(object.costPrice) || object.costPrice==0){
		    	selectTr.find("input[cname='status']").val("赠品");//状态
		    }
	        selectTr.find("input[cname='discountPrice']").val(object.costPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(object.costPrice);//折扣金额
	        selectTr.find("input[cname='taxPrice']").val(object.costPrice);//含税单价
	        selectTr.find("input[cname='taxMoney']").val(object.costPrice);//含税金额
		}else{
			selectTr.find("input[cname='price']").val(object.price);//单价
	        selectTr.find("input[cname='money']").val(object.price);//总金额
	        if(!$.isNumeric(object.price) || object.price==0){
		    	selectTr.find("input[cname='status']").val("赠品");//状态
		    }
	        var discountPrice = "";
	        if($.isNumeric(currentDiscount*object.price)){
	        	discountPrice = round(currentDiscount*object.price,4);
	        }
	        selectTr.find("input[cname='discountPrice']").val(discountPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(discountPrice);//折扣金额
	        selectTr.find("input[cname='taxPrice']").val(discountPrice);//含税单价
	        selectTr.find("input[cname='taxMoney']").val(discountPrice);//含税金额
		}
		
        
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
       
		/*-end 销售订单----*/
		
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
	    
	    //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
	    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	    
	    
	    //添加成本单价（手工指定法，   移动加权库存没货就销售）
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";    //手工指定法选择成本价格
		lastTd.find("div").append(str);
		if(whichCallBack&&whichCallBack=="purchaseReturn" || whichCallBack=="parityAllot" || whichCallBack=="sellBarter" 
			 || whichCallBack=="dismountBill"){//进货退货，同价调拨，销售换货，拆装单
		    	//添加成本单价
				var lastTd=selectTr.find("td").last();
				var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
				lastTd.find("div").append(str);
		 }
	    
	    trIndexs+=","+selectTr.index();                //商品带回要填充的tr
		trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
		productIds+=","+object.product.id;             //商品ID
	    
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId);
}
/**
 * 进货退货单   销售退货单
 * @param json
 * @param $this
 * @return
 */  
function putBillCallBackNotDialog(json,$this){
	billCallBackNotDialog(json,$this);
	var $parent = navTab.getCurrentPanel();
	var whichCallBack=$("#whichCallBack",$parent).val();
	
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	for(var i=0;i<json.length;i++){
		var object=json[i];
		
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
	    
	    //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
	    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	    
	    if(whichCallBack&&(whichCallBack=="sellReturn"||whichCallBack=="stockOtherin")){
	    	//添加成本单价
			var lastTd=selectTr.find("td").last();
			var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";   
			lastTd.find("div").append(str);
	    }
	   
	    showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	    selectTr = selectTr.next();
	}
	retailMoneysTotal($parent);//合计零售金额
	privilegeMoneyTotal();//合计优惠后金额
}
/**
 * 进货换货单    销售换货单
 * @param json
 * @param $this
 * @return
 */
function barterBillCallBackNotDialog(json,$this){
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var $billType  = $parent.find("input#billType").val();
	var selectTd = $parent.find("tbody[type='"+$billType+"'] tr td").filter(".selected");
	var $tbodyDiv = $parent.find("tbody[type='"+$billType+"']").parents("div.gridTbody2:first");
	var $tfootDiv = $tbodyDiv.next("div.gridScroller");
	var whichCallBack=$("#whichCallBack",$parent).val();
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();

	
	var inStorageCode = $parent.find("#inStorageCode").val();
	var inStorageId = $parent.find("#inStorageId").val();
	var inStorageName = $parent.find("#inStorageName").val();
	var inStorageSupId = $parent.find("#inStorageSupId").val();
	
	var outStorageCode = $parent.find("#outStorageCode").val();
	var outStorageId = $parent.find("#outStorageId").val();
	var outStorageName = $parent.find("#outStorageName").val();
	var outStorageSupId = $parent.find("#outStorageSupId").val();
	
	if($billType=="in"){//换入
		for(var i=0;i<json.length;i++){
			var object=json[i];
		    if(selectTr.find("input[cname='productId']")){
		    	billClearTrData(selectTr);//单据清除行数据
		    }
		    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
		    
		    selectTr.find("input[cname='storageFullName']").val(1);//仓库编号
		    
			billProductHiddenAndValue(selectTr,object,"helpInUtil");//单据隐藏商品对象并赋值
			billStorageHiddenAndValue(selectTr,inStorageSupId,inStorageId,inStorageCode,inStorageName);//单据隐藏仓库对象并赋值
			
			var price=object.price;
		    selectTr.find("input[cname='amount']").val(1);//数量
			selectTr.find("input[cname='discount']").val(object.discount);//折扣
			
			if(whichCallBack=="purchaseBarter"){
				var productId = object.product.id;
				var selectUnitId = 1;
				//得到商品最近进价
				$.ajax({
		  			   type: "POST",
		  			   url: basePath+"/stock/stock/lastBuyPrice",
		  			   dataType: "json",
		  			   data: {productId:productId,selectUnitId:selectUnitId},
		  			   async: false,
		  			   success: function(json){
		  			      if($.isNumeric(json.lastBuyPrice)){
		  			    	  price = json.lastBuyPrice;
		  			      }
		  			   }
		  		});
			}
			selectTr.find("input[cname='price']").val(price);//单价
	        selectTr.find("input[cname='money']").val(price);//总金额
	        var discountPrice = "";
	        if($.isNumeric(object.discount*price)){
	        	discountPrice = round(object.discount*price,4);
	        }
	        selectTr.find("input[cname='discountPrice']").val(discountPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(discountPrice);//折扣金额
	        selectTr.find("input[cname='taxPrice']").val(discountPrice);//含税单价
	        selectTr.find("input[cname='taxMoney']").val(discountPrice);//含税金额
	        
	        //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
		    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
		    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
	        
		    if(!$.isNumeric(price) || price==0){
			    if(!$.isNumeric(price) || price==0){
			    	selectTr.find("input[cname='status']").val("赠品");//状态
			    }
		    }
		  //添加成本单价
			var lastTd=selectTr.find("td").last();
			var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
			lastTd.find("div").append(str);
			
	        showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在中显示出来
	        selectTr= hasAddTr(selectTr);//是否增加一行tr
		}
		
	}else{//换出
		var trIndexs="";          //商品带回要填充的tr
		var trcostAriths="";      //是否有dialog弹出（手工指定法）
		var productIds="";        //商品ID
		var tbodyId=selectTr.parent().attr("id");
		for(var i=0;i<json.length;i++){
			var object=json[i];
		    if(selectTr.find("input[cname='productId']")){
		    	billClearTrData(selectTr);//单据清除行数据
		    }
		    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
		    
			billProductHiddenAndValue(selectTr,object,"helpOutUtil");//单据隐藏商品对象并赋值
			billStorageHiddenAndValue(selectTr,outStorageSupId,outStorageId,outStorageCode,outStorageName);//单据隐藏仓库对象并赋值
			
			var price=object.price;
			
		    selectTr.find("input[cname='amount']").val(1);//数量
			selectTr.find("input[cname='discount']").val(object.discount);//折扣
			
			if(whichCallBack=="purchaseBarter"){
				var productId = object.product.id;
				var selectUnitId = 1;
				//得到商品最近进价
				$.ajax({
		  			   type: "POST",
		  			   url: basePath+"/stock/stock/lastBuyPrice",
		  			   dataType: "json",
		  			   data: {productId:productId,selectUnitId:selectUnitId},
		  			   async: false,
		  			   success: function(json){
		  			      if($.isNumeric(json.lastBuyPrice)){
		  			    	  price = json.lastBuyPrice;
		  			      }
		  			   }
		  		});
			}
			selectTr.find("input[cname='price']").val(price);//单价
	        selectTr.find("input[cname='money']").val(price);//总金额
	        var discountPrice = "";
	        if($.isNumeric(object.discount*price)){
	        	discountPrice = round(object.discount*price,4);
	        }
	        selectTr.find("input[cname='discountPrice']").val(discountPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(discountPrice);//折扣金额
	        selectTr.find("input[cname='taxPrice']").val(discountPrice);//含税单价
	        selectTr.find("input[cname='taxMoney']").val(discountPrice);//含税金额
	        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
		    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
	        
		    if(!$.isNumeric(price) || price==0){
			    if(!$.isNumeric(price) || price==0){
			    	selectTr.find("input[cname='status']").val("赠品");//状态
			    }
		    }
			/*-end----*/
		    
		    selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
		    selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额	
	
	    	//添加成本单价
			var lastTd=selectTr.find("td").last();
			var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
			lastTd.find("div").append(str);
		    
		    trIndexs+=","+selectTr.index();                //商品带回要填充的tr
			trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
			productIds+=","+object.product.id;             //商品ID
		    
		    selectTr= hasAddTr(selectTr);//是否增加一行tr
		}
		trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
		productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
		trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
		callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId);
	}
	moreTableTotal($tbodyDiv,$tfootDiv);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	
	var amounts  = $tfootDiv.find("#amounts").val();
	var moneys  = $tfootDiv.find("#moneys").val();
	
	if($tfootDiv.find("#discountMoneys").length>0){
		 moneys = $tfootDiv.find("#discountMoneys").val();
	}
	if($tfootDiv.find("#taxMoneys").length>0){
		moneys = $tfootDiv.find("#taxMoneys").val();
	}
	if($billType=="in"){
		$parent.find("#inAmount").val(amounts);
		$parent.find("#inMoney").val(moneys);
	}else{
		$parent.find("#outAmount").val(amounts);
		$parent.find("#outMoney").val(moneys);
	}
	//privilegeMoneyTotal();//合计优惠后金额
	gapMoneyCalculate($parent,whichCallBack);//算出换货差额
	privilegeMoneyDarter();//优惠金额
}
/**
 * 库存盘点单
 * @param json
 * @param $this
 * @return
 */
function takeStockBillCallBack(json,$this){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	//所有行
	//var allTr = $parent.find("tbody tr");
	var storageId = $parent.find("#storageId").val();
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
		var productId=object.product.id;
		
		/*//修改操作行
		for ( var j = 0; j < allTr.length; j++) {
			var $tr=$(allTr[j]);
			var trIndex=$tr.index();
			var proIdTd=$tr.find("td:last input[cname='productId']").val();
			if(proIdTd==productId){
				selectTr=$parent.find("#"+tbodyId).find("tr:eq("+trIndex+")");
				break;
			}
		}*/
		 
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
	    
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		
		//添加商品成本价
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";
		lastTd.find("div").append(str);
		
		var helpAmount=helpAmountStr(object.samount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
		selectTr.find("input[cname='sckAmount']").val(object.samount);//库存数量 
		selectTr.find("input[cname='sckHelpAmount']").val(helpAmount);//辅助库存数量 
		selectTr.find("input[cname='sckRetailMoney']").val(object.samount * object.product.retailPrice1==0?'':strNullConvert(object.samount * object.product.retailPrice1));//零售金额
//		selectTr.find("input[cname='amount']").val(1);//设置盘点数量默认值
//		var helpAmount2=helpAmountStr(1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
//		selectTr.find("input[cname='helpAmount']").val(helpAmount2);//辅助盘点数量
		
		var costArith = object.product.costArith;
		
		trIndexs+=","+selectTr.index();                //商品带回要填充的tr索引
		trcostAriths+=","+costArith;    //成本算法列表
		productIds+=","+object.product.id;             //商品ID
		
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callBackTackStockPrice(trIndexs,productIds,trcostAriths,tbodyId);//库存盘点得到单价
	//库存盘点单   (盘点总数量,库存总数量,库存总金额,亏盈总数量,亏盈总金额,零售总金额)统计
	takeStockBillTotal($parent);
}
/**
 * 报损单
 * @param json
 * @param $this
 * @return
 */
function breakageBillCallBack(json,$this){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
		 
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
	    
	    //添加成本单价（手工指定法，   移动加权库存没货就销售）
		var lastTd=selectTr.find("td").last();
		var str0="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";    //手工指定法选择成本价格
		var str1="<input type='hidden' cname='baseAmount' value='1' name='brekageBillDetail["+selectTr.index()+"].baseAmount'/>";    //实际数量(基本数量)
		var str2="<input type='hidden' cname='basePrice' name='brekageBillDetail["+selectTr.index()+"].basePrice' />";//基本单价
		lastTd.find("div").append(str0);
		lastTd.find("div").append(str1);
		lastTd.find("div").append(str2);
		
		
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
		
	    selectTr.find("input[cname='amount']").val(1);//数量
	    var helpAmount=helpAmountStr(1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
		selectTr.find("input[cname='helpAmount']").val(helpAmount);//辅助库存数量 
        
		var costArith = object.product.costArith;
		var costPrice = object.costPrice;
		if(costArith==4 || (costArith==1 && $.isNumeric(costPrice)==false)){
		    trIndexs+=","+selectTr.index();                //商品带回要填充的tr索引
			trcostAriths+=","+costArith;    			   //成本算法列表
			productIds+=","+object.product.id;             //商品ID
		}else if (costArith==1) {
			selectTr.find("input[cname='basePrice']").val(costPrice);
	    	selectTr.find("input[cname='price']").val(costPrice);
	    	selectTr.find("input[cname='money']").val(costPrice);
			showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
		}
	    
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callbackProToCostPrice(trIndexs,productIds,trcostAriths,tbodyId);
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}
/**
 * 报溢单
 * @param json
 * @param $this
 * @return
 */
function overflowBillCallBack(json,$this){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	
	var trIndexs="";          //商品带回要填充的tr索引
	var trcostAriths="";      //算法
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	var preDataName=selectTr.parent().attr("preDataName");//明细名称
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
		 
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
	    
	    //添加基本数量基本单价
		var lastTd=selectTr.find("td").last();
		var str0="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";    //手工指定法选择成本价格
//		var str1="<input type='hidden' cname='baseAmount' value='1' name='"+preDataName+"["+selectTr.index()+"].baseAmount'/>";//基本数量
		var str2="<input type='hidden' cname='basePrice' name='"+preDataName+"["+selectTr.index()+"].basePrice' />";//基本单价
		lastTd.find("div").append(str0);
//		lastTd.find("div").append(str1);
		lastTd.find("div").append(str2);
		
		
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
		
	    selectTr.find("input[cname='amount']").val(1);//数量
	    var helpAmount=helpAmountStr(1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
		selectTr.find("input[cname='helpAmount']").val(helpAmount);//辅助库存数量 
        
		var costArith = object.product.costArith;
		var costPrice = object.costPrice;
		if(costArith==4 || (costArith==1 && $.isNumeric(costPrice)==false)){
		    trIndexs+=","+selectTr.index();                //商品带回要填充的tr索引
		    trcostAriths+=","+costArith;    			   //成本算法列表
			productIds+=","+object.product.id;             //商品ID
		}else if (costArith==1) {
			selectTr.find("input[cname='basePrice']").val(costPrice);
	    	selectTr.find("input[cname='price']").val(costPrice);
	    	selectTr.find("input[cname='money']").val(costPrice);
			showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
		}
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callBackLastBuyPrice(trIndexs,productIds,trcostAriths,tbodyId);//带回报溢单价
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}
/**
 * 其它入库单 
 * @param json
 * @param $this
 * @return
 */
function putOtherBillCallBackNotDialog(json,$this){
	var $parent = navTab.getCurrentPanel();
	var whichCallBack=$("#whichCallBack",$parent).val();
	var basePath=$parent.find("#basePathId").val();//用于ajax请求
	
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	for(var i=0;i<json.length;i++){
		var object=json[i];
		
		if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		selectTr.find("input[cname='amount']").val(1);//数量
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
        
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
	    
	    //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
	    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	    
	    if(whichCallBack&&(whichCallBack=="stockOtherout"||whichCallBack=="stockOtherin")){
	    	//添加成本单价
			var lastTd=selectTr.find("td").last();
			var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";   
			lastTd.find("div").append(str);
	    }
	   
	    var price=getProductBackPrice(basePath,storageId,object.product.id,object.product.costArith,1);
	    if(price){
	    	selectTr.find("input[cname='price']").val(price);   //单价
	    	var amount =selectTr.find("input[cname='amount']").val();
		    selectTr.find("input[cname='money']").val(amount*price);   //金额
	    }else{
	    	//清空前面赋值
	    	selectTr.find("input[cname='price']").val("");   //单价
		    selectTr.find("input[cname='money']").val("");   //金额
	    }
	    
	    
	    if(!$.isNumeric(price) || price==0){
	    	selectTr.find("input[cname='status']").val("赠品");//状态
	    }
	    
	    showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	retailMoneysTotal($parent);//合计零售金额
}
/**
 * 其它出库单 
 * @param json
 * @param $this
 * @return
 */
function qtckdBillCallBackNotDialog(json,$this){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var whichCallBack=$("#whichCallBack",$parent).val();
	var basePath=$parent.find("#basePathId").val();//用于ajax请求
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	//var discount = $parent.find("#discounts").val();
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		
	    selectTr.find("input[cname='amount']").val(1);//数量
		
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
		
		//billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
	    
	    //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
	    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	    
	    
	    var price=getProductBackPrice(basePath,storageId,object.product.id,object.product.costArith,1);
	    if(price){
	    	selectTr.find("input[cname='price']").val(price);   //单价
	    	var amount =selectTr.find("input[cname='amount']").val();
		    selectTr.find("input[cname='money']").val(amount*price);   //金额
	    }else{
	    	//清空前面赋值
	    	selectTr.find("input[cname='price']").val("");   //单价
		    selectTr.find("input[cname='money']").val("");   //金额
	    }
	    
	    //添加成本单价（手工指定法，   移动加权库存没货就销售）
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";    //手工指定法选择成本价格
		lastTd.find("div").append(str);
		if(whichCallBack&&whichCallBack=="purchaseReturn"){
		    	//添加成本单价
				var lastTd=selectTr.find("td").last();
				var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
				lastTd.find("div").append(str);
		 }
	    
	    trIndexs+=","+selectTr.index();                //商品带回要填充的tr
		trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
		productIds+=","+object.product.id;             //商品ID
	    
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId);
}
/**
 * 生产模板dialog形式带回
 * @param json
 * @param $this
 * @return
 */
function produceTemplateCallBackNotDialog(json,$this){
	var $parent = getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTr =  selectTd.parent();
	var tbodyId=selectTr.parent().attr("id");
	
	//所有行
	var allTr = $parent.find("tbody tr");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
		var productId=object.product.id;
		
		//修改操作行
		var needTrIndex=selectTr.index();
		for ( var j = 0; j < allTr.length; j++) {
			var $tr=$(allTr[j]);
			var trIndex=$tr.index();
			var tdProId=$tr.find("input[cname='productId']").val();//原有商品ID
			if(tdProId && tdProId!="" && tdProId==productId){
				needTrIndex=trIndex;
				selectTr =  $parent.find("#"+tbodyId).find("tr:eq("+needTrIndex+")");
			}
		}
		
		if(selectTr.find("input[cname='productId']")){
			billClearTrData(selectTr);//单据清除行数据
		}
		addHiddenCurrentWidget(selectTr);//生成隐藏控件
		
		billProductHiddenAndValue(selectTr,object,"helpUitl","noUnit");//单据隐藏商品对象并赋值(不需要单位)
		
		
	    //生产模板单
		selectTr.find("input[cname='sckAmount']").val(object.samount);//库存数量 
		selectTr.find("input[cname='remainAmount']").val(object.samount);//库存余量 
		
		
		showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
		//var ckeckTd=selectTr.find("td[cname=assortAmount]");//要选中的TD
		//addSelectClass($parent.find("#"+tbodyId).find('td'),ckeckTd);//选中
		
		selectTr= hasAddTr(selectTr,selectTr);//是否增加一行tr
		selectTr.attr("isNotBatch","yes");//单独处理TR上的属性
	}
	
	getMinAmountByArray(allTr,$parent);//赋值最小生产数量
	
	
}
/**
 * 成本调价单
 * @param json
 * @param $this
 * @return
 */
function adjustCostCallBackNotDialog(json,$this){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var whichCallBack=$("#whichCallBack",$parent).val();
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	var discount = $parent.find("#discounts").val();
	var storageCode = $parent.find("#storageCode").val();
	var storageId = $parent.find("#storageId").val();
	var storageName = $parent.find("#storageName").val();
	var storageSupId = $parent.find("#storageSupId").val();
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	
	for(var i=0;i<json.length;i++){
		var object=json[i];
	
	    if(selectTr.find("input[cname='productId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象     
	    
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		
	    selectTr.find("input[cname='amount']").val(object.samount);//数量
	
		selectTr.find("input[cname='price']").val(object.costPrice);//单价
        selectTr.find("input[cname='money']").val(object.costPrice*object.samount);//总金额
        
        selectTr.find("input[cname='lastPrice']").val(object.costPrice);//单价
        selectTr.find("input[cname='lastMoney']").val(object.costPrice*object.samount);//总金额
      
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,object.samount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
	    
	    //selectTr.find("input[cname='retailPrice']").val(object.product.retailPrice1);   //零售价
	    //selectTr.find("input[cname='retailMoney']").val(object.product.retailPrice1);   //零售金额
	    
	    
	    //添加成本单价（手工指定法，   移动加权库存没货就销售）
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='helpUitl["+selectTr.index()+"].costPrice'/>";    //手工指定法选择成本价格
		lastTd.find("div").append(str);
		if(whichCallBack&&whichCallBack=="adjustCost"){//成本调价单
		    	//添加成本单价
				var lastTd=selectTr.find("td").last();
				var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
				lastTd.find("div").append(str);
		 }
	    
	    trIndexs+=","+selectTr.index();                //商品带回要填充的tr
		trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
		productIds+=","+object.product.id;             //商品ID
	    
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId);
}
/**
 * 拆装单
 * @param json
 * @param $this
 * @return
 */
function dismountBillCallBackNotDialog(json,$this){
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var $billType  = $parent.find("input#billType").val();
	var selectTd = $parent.find("tbody[type='"+$billType+"'] tr td").filter(".selected");
	var $tbodyDiv = $parent.find("tbody[type='"+$billType+"']").parents("div.gridTbody2:first");
	var $tfootDiv = $tbodyDiv.next("div.gridScroller");
	var whichCallBack=$("#whichCallBack",$parent).val();
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();

	
	var storageCode = $parent.find("#"+$billType+"StorageCode").val();
	var storageId = $parent.find("#"+$billType+"StorageId").val();
	var storageName = $parent.find("#"+$billType+"StorageName").val();
	var storageSupId = $parent.find("#"+$billType+"StorageSupId").val();
	
	if($billType=="in"){//换入
		for(var i=0;i<json.length;i++){
			var object=json[i];
		    if(selectTr.find("input[cname='productId']")){
		    	billClearTrData(selectTr);//单据清除行数据
		    }
		    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
		    
			billProductHiddenAndValue(selectTr,object,"helpInUtil");//单据隐藏商品对象并赋值
			billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
			
		    selectTr.find("input[cname='amount']").val(1);//数量
			selectTr.find("input[cname='discount']").val(1);//折扣
			
			var price=object.costPrice;
			
			selectTr.find("input[cname='price']").val(price);//单价
	        selectTr.find("input[cname='money']").val(price);//总金额
	        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
		    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
	        
		    if(!$.isNumeric(price) || price==0){
			    
			    	selectTr.find("input[cname='status']").val("赠品");//状态
			    
		    }
	        showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在中显示出来
	     
	        selectTr= hasAddTr(selectTr);//是否增加一行tr
		}
		
	}else{//换出
		var trIndexs="";          //商品带回要填充的tr
		var trcostAriths="";      //是否有dialog弹出（手工指定法）
		var productIds="";        //商品ID
		var tbodyId=selectTr.parent().attr("id");
		for(var i=0;i<json.length;i++){
			var object=json[i];
		    if(selectTr.find("input[cname='productId']")){
		    	billClearTrData(selectTr);//单据清除行数据
		    }
		    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
		    
			billProductHiddenAndValue(selectTr,object,"helpOutUtil");//单据隐藏商品对象并赋值
			billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值
			
		    selectTr.find("input[cname='amount']").val(1);//数量
		
			var price=object.costPrice;//成本价
			
			selectTr.find("input[cname='price']").val(price);//单价
	        selectTr.find("input[cname='money']").val(price);//总金额
	        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
		    xsUnitChange(selectTr,1,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
	        
		    if(!$.isNumeric(price) || price==0){
			  
			    	selectTr.find("input[cname='status']").val("赠品");//状态
			    
		    }
			/*-end----*/
	
	    	//添加成本单价
			var lastTd=selectTr.find("td").last();
			var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice'/>";   
			lastTd.find("div").append(str);
		    
		    trIndexs+=","+selectTr.index();                //商品带回要填充的tr
			trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
			productIds+=","+object.product.id;             //商品ID
		    
		    selectTr= hasAddTr(selectTr);//是否增加一行tr
		}
		trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
		productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
		trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
		callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId);
	}
	moreTableTotal($tbodyDiv,$tfootDiv);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	
	var amounts  = $tfootDiv.find("#amounts").val();
	var moneys  = $tfootDiv.find("#moneys").val()||$tfootDiv.find("#discountMoneys").val()||$tfootDiv.find("#taxMoneys").val();
	if($billType=="in"){
		$parent.find("#inAmount").val(amounts);
		$parent.find("#inMoney").val(moneys);
	}else{
		$parent.find("#outAmount").val(amounts);
		$parent.find("#outMoney").val(moneys);
	}
}
/**
 * 商品组装带回
 * @return
 */
function tempCkdCallBack(json){
	DWZ.ajaxDone(json);
	if(json.msg){
		alertMsg.warn(json.msg);
		return false;
	}
	$.pdialog.closeCurrent();  //关闭当前Dialog
	var object = json["product"];
	var productDetail = json["productDetail"];
	
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var inStorageCode = $parent.find("#inStorageCode").val();
	var inStorageId = $parent.find("#inStorageId").val();
	var inStorageName = $parent.find("#inStorageName").val();
	var inStorageSupId = $parent.find("#inStorageSupId").val();
	
	var outStorageCode = $parent.find("#outStorageCode").val();
	var outStorageId = $parent.find("#outStorageId").val();
	var outStorageName = $parent.find("#outStorageName").val();
	var outStorageSupId = $parent.find("#outStorageSupId").val();
	
	var $inGrid = $("tbody[type='in']",$parent).parents("div.grid:first");
	var $outGrid = $("tbody[type='out']",$parent).parents("div.grid:first");
	
	var selectInTd = $inGrid.find("tbody tr td").filter(".selected");
	var selectOutTd = $outGrid.find("tbody tr td").filter(".selected");
	
	var selectInTr =  selectInTd.parent();
	var selectOutTr =  selectOutTd.parent();
	
	var $inTrs = $inGrid.find("tbody tr");
	for ( var i = 0; i < $inTrs.length; i++) {
		var $trProId = $($inTrs[i]).find("input[cname='productId']").val();
		if(!$trProId){
			selectInTr = $($inTrs[i]);
			break;
		}
	}
	var $outTrs = $outGrid.find("tbody tr");
	for ( var i = 0; i < $outTrs.length; i++) {
		var $trProId = $($outTrs[i]).find("input[cname='productId']").val();
		if(!$trProId){
			selectOutTr = $($outTrs[i]);
			break;
		}
	}
	
	
    if(selectInTr.find("input[cname='productId']")){
    	billClearTrData(selectInTr);//单据清除行数据
    }
    addHiddenCurrentWidget(selectInTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
    
	billProductHiddenAndValue(selectInTr,object,"helpInUtil");//单据隐藏商品对象并赋值
	billStorageHiddenAndValue(selectInTr,inStorageSupId,inStorageId,inStorageCode,inStorageName);//单据隐藏仓库对象并赋值
	
	selectInTr.find("input[cname='amount']").val(object.amount);//数量
	selectInTr.find("input[cname='discount']").val(1);//折扣
	
	var price=object.costPrice;
	
	selectInTr.find("input[cname='price']").val(price);//单价
	selectInTr.find("input[cname='money']").val(price);//总金额
    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
    xsUnitChange(selectInTr,object.amount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
    
    if(!$.isNumeric(price) || price==0){
	    if(!$.isNumeric(object.product.retailPrice1) || object.product.retailPrice1==0){
	    	selectInTr.find("input[cname='status']").val("赠品");//状态
	    }
    }
    showCurrentTrWidgetToSpan(selectInTr);//让tr里面所有td中的input值在中显示出来
 
    selectInTr= hasAddTr(selectInTr);//是否增加一行tr

    sellbookOrboughgTotal($inGrid);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	 
	 var inAmounts  = $inGrid.find("#amounts").val();
	 var inMoneys  = $inGrid.find("#moneys").val()||$inGrid.find("#discountMoneys").val()||$inGrid.find("#taxMoneys").val();
	 $parent.find("#inAmount").val(inAmounts);
	 $parent.find("#inMoney").val(inMoneys);
	 
	
	// 换出
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectOutTr.parent().attr("id");
	for(var i=0;i<productDetail.length;i++){
		var object=productDetail[i];
	    if(selectOutTr.find("input[cname='productId']")){
	    	billClearTrData(selectOutTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectOutTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectOutTr,object,"helpOutUtil");//单据隐藏商品对象并赋值
		billStorageHiddenAndValue(selectOutTr,outStorageSupId,outStorageId,outStorageCode,outStorageName);//单据隐藏仓库对象并赋值
		
		selectOutTr.find("input[cname='amount']").val(object.amount);//数量
	
		var price=object.costPrice;//成本价
		
		selectOutTr.find("input[cname='price']").val(price);//单价
		selectOutTr.find("input[cname='money']").val(price);//总金额
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectOutTr,object.amount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
        
	    if(!$.isNumeric(price) || price==0){
		    if(!$.isNumeric(object.product.retailPrice1) || object.product.retailPrice1==0){
		    	selectOutTr.find("input[cname='status']").val("赠品");//状态
		    }
	    }
		/*-end----*/

    	//添加成本单价
		var lastTd=selectOutTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='"+selectOutTr.parent().attr("preDataName")+"["+selectOutTr.index()+"].costPrice'/>";   
		lastTd.find("div").append(str);
	    
	    trIndexs+=","+selectOutTr.index();                //商品带回要填充的tr
		trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
		productIds+=","+object.product.id;             //商品ID
	    
		selectOutTr= hasAddTr(selectOutTr);//是否增加一行tr
	 }
	 trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	 productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	 trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	 callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId);
	 
	 sellbookOrboughgTotal($outGrid);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
		
	 var outAmounts  = $outGrid.find("#amounts").val();
	 var outMoneys  = $outGrid.find("#moneys").val()||$outGrid.find("#discountMoneys").val()||$outGrid.find("#taxMoneys").val();
	
	 $parent.find("#outAmount").val(outAmounts);
	 $parent.find("#outMoney").val(outMoneys);
	 
}
/**
 * 商品拆分带回
 * @return
 */
function tempDisCallBack(json){
	DWZ.ajaxDone(json);
	if(json.msg){
		alertMsg.warn(json.msg);
		return false;
	}
	$.pdialog.closeCurrent();  //关闭当前Dialog
	var object = json["product"];
	var productDetail = json["productDetail"];
	
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var inStorageCode = $parent.find("#inStorageCode").val();
	var inStorageId = $parent.find("#inStorageId").val();
	var inStorageName = $parent.find("#inStorageName").val();
	var inStorageSupId = $parent.find("#inStorageSupId").val();
	
	var outStorageCode = $parent.find("#outStorageCode").val();
	var outStorageId = $parent.find("#outStorageId").val();
	var outStorageName = $parent.find("#outStorageName").val();
	var outStorageSupId = $parent.find("#outStorageSupId").val();
	
	var $inGrid = $("tbody[type='in']",$parent).parents("div.grid:first");
	var $outGrid = $("tbody[type='out']",$parent).parents("div.grid:first");
	
	var selectInTd = $inGrid.find("tbody tr td").filter(".selected");
	var selectOutTd = $outGrid.find("tbody tr td").filter(".selected");
	
	var selectInTr =  selectInTd.parent();
	var selectOutTr =  selectOutTd.parent();
	
	var $inTrs = $inGrid.find("tbody tr");
	for ( var i = 0; i < $inTrs.length; i++) {
		var $trProId = $($inTrs[i]).find("input[cname='productId']").val();
		if(!$trProId){
			selectInTr = $($inTrs[i]);
			break;
		}
	}
	var $outTrs = $outGrid.find("tbody tr");
	for ( var i = 0; i < $outTrs.length; i++) {
		var $trProId = $($outTrs[i]).find("input[cname='productId']").val();
		if(!$trProId){
			selectOutTr = $($outTrs[i]);
			break;
		}
	}
	
	//出库
    if(selectOutTr.find("input[cname='productId']")){
    	billClearTrData(selectOutTr);//单据清除行数据
    }
    addHiddenCurrentWidget(selectOutTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
    
	billProductHiddenAndValue(selectOutTr,object,"helpInUtil");//单据隐藏商品对象并赋值
	billStorageHiddenAndValue(selectOutTr,outStorageSupId,outStorageId,outStorageCode,outStorageName);//单据隐藏仓库对象并赋值
	
	selectOutTr.find("input[cname='amount']").val(object.amount);//数量
	selectOutTr.find("input[cname='discount']").val(1);//折扣
	
	var price=object.costPrice;
	
	selectOutTr.find("input[cname='price']").val(price);//单价
	selectOutTr.find("input[cname='money']").val(price);//总金额
 //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
    xsUnitChange(selectOutTr,object.amount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
 
    if(!$.isNumeric(price) || price==0){
	    if(!$.isNumeric(object.product.retailPrice1) || object.product.retailPrice1==0){
	    	selectOutTr.find("input[cname='status']").val("赠品");//状态
	    }
    }
 showCurrentTrWidgetToSpan(selectOutTr);//让tr里面所有td中的input值在中显示出来

 selectOutTr= hasAddTr(selectOutTr);//是否增加一行tr

 sellbookOrboughgTotal($outGrid);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
 
 var outAmounts  = $outGrid.find("#amounts").val();
 var outMoneys  = $outGrid.find("#moneys").val()||$outGrid.find("#discountMoneys").val()||$outGrid.find("#taxMoneys").val();
 $parent.find("#outAmount").val(outAmounts);
 $parent.find("#outMoney").val(outMoneys);
	
	// 入库
	
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectInTr.parent().attr("id");
	for(var i=0;i<productDetail.length;i++){
		var object=productDetail[i];
	    if(selectInTr.find("input[cname='productId']")){
	    	billClearTrData(selectInTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectInTr);//添加隐藏当前行控件对象      eg:选择商品  显示当前行所有的控件对象
	    
		billProductHiddenAndValue(selectInTr,object,"helpOutUtil");//单据隐藏商品对象并赋值
		billStorageHiddenAndValue(selectInTr,inStorageSupId,inStorageId,inStorageCode,inStorageName);//单据隐藏仓库对象并赋值
		
		selectInTr.find("input[cname='amount']").val(object.amount);//数量
	
		var price=object.costPrice;//成本价
		
		selectInTr.find("input[cname='price']").val(price);//单价
		selectInTr.find("input[cname='money']").val(price);//总金额
        //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectInTr,object.amount,object.product.calculateUnit1,object.product.calculateUnit2,object.product.calculateUnit3,object.product.unitRelation1,object.product.unitRelation2,object.product.unitRelation3);
        
	    if(!$.isNumeric(price) || price==0){
		    if(!$.isNumeric(object.product.retailPrice1) || object.product.retailPrice1==0){
		    	selectInTr.find("input[cname='status']").val("赠品");//状态
		    }
	    }
		/*-end----*/

    	//添加成本单价
		var lastTd=selectInTr.find("td").last();
		var str="<input type='hidden' cname='costPrice' name='"+selectInTr.parent().attr("preDataName")+"["+selectInTr.index()+"].costPrice'/>";   
		lastTd.find("div").append(str);
	    
	    trIndexs+=","+selectInTr.index();                //商品带回要填充的tr
		trcostAriths+=","+object.product.costArith;    //是否有dialog弹出（手工指定法）
		productIds+=","+object.product.id;             //商品ID
	    
		selectInTr= hasAddTr(selectInTr);//是否增加一行tr
	 }
	 trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1, trIndexs.length);
	 productIds=productIds.substring(productIds.indexOf(",")+1, productIds.length);
	 trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1, trcostAriths.length);
	 callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId);
	 
	 sellbookOrboughgTotal($inGrid);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
		
	 var inAmounts  = $inGrid.find("#amounts").val();
	 var inMoneys  = $inGrid.find("#moneys").val()||$inGrid.find("#discountMoneys").val()||$inGrid.find("#taxMoneys").val();
	
	 $parent.find("#inAmount").val(inAmounts);
	 $parent.find("#inMoney").val(inMoneys);
	 
	 
	
}
/*----------------------------------------end---具体商品回调  初始化--------------------------------------------*/








/*-------------------------------------------stable td 可输入控件生成-----------------------------------------------*/
/**
 * 根据tr里面的td生成可输入控件
 * @param selectTr
 */
function addHiddenCurrentWidget(selectTr){
	$(selectTr.find("td")).each(function(i,tdObj){
		createWidget(selectTr,tdObj);//根据td生成  控件
	});
}
/**
 * 可输入控件  生成
 * @param selectTr
 * @param tdObj
 * @param isHidden
 * @return
 */
function createWidget(selectTr,tdObj,isHidden){
	var $tdObj=$(tdObj);
	//先判断td里面有没有值
	var valObj=$tdObj.find("div").text();
	var val="";
	if(valObj&&$.trim(valObj)!=""){  //有值
		val=valObj;
	}
	if($tdObj.attr("requiredData")=="noNeed"){ //不用生成控件
		return false;
	}
	if($tdObj.attr("cname")){
		var str="";
		//控件类型 
		var type  = $tdObj.attr("type")||"text";
		if(type=="select"){       
		}else if(type=="radio"){
		}else{
			str+="<input cname='"+$tdObj.attr("cname")+"'";
			if(isHidden&&isHidden=="false"){         //单击单元格调出显示的控件
				if($tdObj.attr("onBlur")){
					str+=' onBlur="'+$tdObj.attr("onBlur")+'"';
				}else{
					str+=" onBlur='aioBillOnblurRemoveWidget(this)'";
				}
				str+="type='"+type+"'";
			}else{
				str+="type='hidden'";
			}
		}
		//样式验证
		if($tdObj.attr("validateAttr")&&$tdObj.attr("validateAttr")!=""){
			str+="class='stealth "+$tdObj.attr("validateAttr")+"'";
		}else{
			str+="class='stealth'";
		}
		//是否传到后台  
		if($tdObj.attr("requiredData")&&$tdObj.attr("requiredData")=="need"){
			if($tdObj.attr("name")){
				str+=" name='"+$(selectTr).parent().attr("preDataName")+"["+$(selectTr).index()+"]."+$tdObj.attr("name")+"'";
			}else{
				str+=" name='"+$(selectTr).parent().attr("preDataName")+"["+$(selectTr).index()+"]."+$tdObj.attr("cname")+"'";
			}
		}
		//readonly/是否只读
		if($tdObj.attr("readonly")&&$tdObj.attr("readonly")=="readonly"){            
			str+=" readonly='readonly'";
		}
		//最大长度
		if($tdObj.attr("maxlength")){            
			str+=" maxlength='"+$tdObj.attr("maxlength")+"'";
		}
		str+=" value='"+val+"' />";
		if(isHidden&&isHidden=="false"){    //单击单元格调出显示的控件
			$tdObj.find("div").html(str);
			//TODO
			//后面控件再做扩展
			var widgetObj=$($($tdObj).find("input[cname='"+$tdObj.attr("cname")+"']"));
			widgetObj.focus();
		}else{
			//放到最后
			var lastTd=$(selectTr).find("td").last();
			lastTd.find("div").append(str);
		}
		if ($.fn.datepicker){
			$('input.dateISO',$tdObj).each(function(){
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
	}
	return false;
}
/*----------------------------------------end---stable td 可输入控件生成--------------------------------------------*/




/*-------------------------------------------公共方法-----------------------------------------------*/


/**
 * 隐藏商品对象 并 赋值
 * @param selectTr
 * @param object
 * @param helpObj
 * @param isNoUnit
 * @return
 */
function billProductHiddenAndValue(selectTr,object,helpObj,isNoUnit){
	//添加隐藏控件
    var lastTd=selectTr.find("td").last();
    var str="<input type='hidden' cname='supId'/>"; //父级ID
    
    //生产日期  批号
    var produceDate = selectTr.find("td[cname='produceDate']");
    var produceEndDate = selectTr.find("td[cname='produceEndDate']");
    var batch = selectTr.find("td[cname='batch']");
    var isNotBatch = selectTr.attr("isNotBatch");//是否需要批次  生产日期    eg:生产模板
    if(!isNotBatch){
	    if(produceDate.length==0){
	    	str+="<input class='stealth date' type='hidden'  maxlength='50' readonly='readonly' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].produceDate' cname='produceDate'>";
	    }
	    if(produceEndDate.length==0){
	    	str+="<input class='stealth date' type='hidden'  maxlength='50' readonly='readonly' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].produceEndDate' cname='produceEndDate'>";
	    }
	    if(batch.length==0){
	    	str+="<input class='stealth' type='hidden'  maxlength='50' readonly='readonly' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].batch'  cname='batch'>";
	    }
    }
    str+="<input type='hidden' cname='productId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].productId' />";  //商品Id
	str+="<input type='hidden' cname='oldCode'/>";           //单据商品旧编号    方便修改编号  弹出修改，还是查询
	str+="<input type='hidden' cname='oldFullName'/>";
	if(!isNoUnit){
		str+="<input type='hidden' cname='selectUnitId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].selectUnitId'/>";//选择单位名称对应的ID
	}
	
	str+="<input type='hidden' cname='validity'/>";
	
	str+="<input type='hidden' cname='barCode1'/>";    //条码
	str+="<input type='hidden' cname='barCode2'/>";
	str+="<input type='hidden' cname='barCode3'/>";
	str+="<input type='hidden' cname='calculateUnit1'/>";    //单位名称1
	str+="<input type='hidden' cname='calculateUnit2'/>";
	str+="<input type='hidden' cname='calculateUnit3'/>";
	str+="<input type='hidden' cname='unitRelation1' name='product["+selectTr.index()+"].unitRelation1'/>";   //单位关系1            这个可以不用传到后台去
	str+="<input type='hidden' cname='unitRelation2' name='product["+selectTr.index()+"].unitRelation2'/>";   //单位关系2
	str+="<input type='hidden' cname='unitRelation3' name='product["+selectTr.index()+"].unitRelation3'/>";   //单位关系3
	str+="<input type='hidden' cname='retailPrice1'/>";      //零售价1
	str+="<input type='hidden' cname='retailPrice2'/>";
	str+="<input type='hidden' cname='retailPrice3'/>";
	str+="<input type='hidden' cname='costArith'/>";
	//当前行，用于错误提示
	if(!helpObj) helpObj = "helpUitl";
	str+="<input type='hidden' cname='trIndex' name='"+helpObj+"["+selectTr.index()+"].trIndex' value='"+selectTr.index()+"'/>";   
	
	
	var selectUnitId=object.selectUnitId;
	if(!selectUnitId){
		selectUnitId=1;
	}
	var barCode=object.product.barCode1;
	var proBaseUnit=object.product.calculateUnit1;
	var proBaseRetailPrice=object.product.retailPrice1;
	if(selectUnitId==2){
		barCode=object.product.barCode2;
		proBaseUnit=object.product.calculateUnit2;
		proBaseRetailPrice=object.product.retailPrice2;
	}else if(selectUnitId==3){
		barCode=object.product.barCode3;
		proBaseUnit=object.product.calculateUnit3;
		proBaseRetailPrice=object.product.retailPrice3;
	}
	
	
	
	var tds=selectTr.find("td");
	tds.each(function(i,td){
		if(i==0){   //第一个
			return;
		}
		var cname=$(td).attr("cname");
		
		switch (cname){
			case "proSpell":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proSpell']").val(strNullConvert(object.product.spell));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proSpell' value='"+strNullConvert(object.product.spell)+"' />";//拼音码
				}
			break;
			case "proBaseUnit":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proBaseUnit']").val(strNullConvert(object.product.calculateUnit1));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proBaseUnit' value='"+strNullConvert(proBaseUnit)+"' />";//基本单位
				}
			break;
			case "proStandard":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proStandard']").val(strNullConvert(object.product.standard));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proStandard' value='"+strNullConvert(object.product.standard)+"' />";//规格
				}
			break;
			case "proModel":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proModel']").val(strNullConvert(object.product.model));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proModel' value='"+strNullConvert(object.product.model)+"' />";//型号
				}
			break;
			case "proField":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proField']").val(strNullConvert(object.product.field));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proField' value='"+strNullConvert(object.product.field)+"' />";//产地
				}
				
			break;
			case "proValidity":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proValidity']").val(strNullConvert(object.product.validity));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proValidity' value='"+strNullConvert(object.product.validity)+"' />";//有效期
				}
				
			break;
			case "proMemo":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proMemo']").val(strNullConvert(object.product.memo));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proMemo' value='"+strNullConvert(object.product.memo)+"' />";//商品备注
				}
				
			break;
			case "proCostArith":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proCostArith']").val(strNullConvert(object.product.costArith));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proCostArith' value='"+strNullConvert(object.product.costArith)+"' />";//成本算法
				}
				
			break;
			case "proBaseBarCode":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proBaseBarCode']").val(strNullConvert(object.product.barCode1));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proBaseBarCode' value='"+strNullConvert(barCode)+"' />";//商品基本条码
				}
				
			break;
			case "proBaseRetailPrice":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proBaseRetailPrice']").val(strNullConvert(object.product.retailPrice1));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proBaseRetailPrice' value='"+strNullConvert(proBaseRetailPrice)+"' />";//商品基本零售价
				}
				
			break;
			case "proPrice":
				if($(td).attr("isRedundancy")){
					selectTr.find("input[cname='proPrice']").val(strNullConvert(object.product.avgPrice));//冗余字段直接赋值 
				}else{
					str+="<input type='hidden' cname='proPrice' value='"+strNullConvert(object.product.avgPrice)+"' />";//商品成本单价
				}
				
			break;
		}
	});
	
	lastTd.find("div").append(str);

	selectTr.find("input[cname='supId']").val(object.product.supId);                      //商品父级Id
	selectTr.find("input[cname='productId']").val(object.product.id);                     //商品ID
	selectTr.find("input[cname='code']").val(object.product.code);                        //商品编号
	selectTr.find("input[cname='oldCode']").val(object.product.code);                     //商品编号   单据商品旧编号    方便修改编号  弹出修改，还是查询 
	selectTr.find("input[cname='fullName']").val(object.product.fullName);                //商品全称
	selectTr.find("input[cname='oldFullName']").val(object.product.fullName);             //商品全名   单据商品旧全名    方便修改全名  弹出修改，还是查询 
	
	selectTr.find("input[cname='validity']").val(object.product.validity);                //商品有效天
	
	selectTr.find("input[cname='smallName']").val(object.product.smallName);              //
	selectTr.find("input[cname='spell']").val(object.product.spell);                      //
	selectTr.find("input[cname='standard']").val(object.product.standard);                //
	selectTr.find("input[cname='model']").val(object.product.model);                      //
	selectTr.find("input[cname='field']").val(object.product.field);                      //
	selectTr.find("input[cname='proMemo']").val(object.product.memo);                     //
	if(object.product.savePath&&object.product.savePath!=''){
		selectTr.find("input[cname='savePath']").val(object.product.savePath);
	}

	
	selectTr.find("input[cname='baseUnit']").val(proBaseUnit);//单位
	selectTr.find("input[cname='assistUnit']").val(object.product.assistUnit);//辅助单位
	selectTr.find("input[cname='selectUnitId']").val(selectUnitId);    //默认选择单位名称1对应
	selectTr.find("input[cname='barCode']").val(barCode);       //条码
	selectTr.find("input[cname='barCode1']").val(object.product.barCode1);//条码
    selectTr.find("input[cname='barCode2']").val(object.product.barCode2);//条码
    selectTr.find("input[cname='barCode3']").val(object.product.barCode3);//条码
    selectTr.find("input[cname='calculateUnit1']").val(object.product.calculateUnit1);//单位名称1
    selectTr.find("input[cname='calculateUnit2']").val(object.product.calculateUnit2);//单位名称2
    selectTr.find("input[cname='calculateUnit3']").val(object.product.calculateUnit3);//单位名称3
    selectTr.find("input[cname='unitRelation1']").val(object.product.unitRelation1);//单位关系1
    selectTr.find("input[cname='unitRelation2']").val(object.product.unitRelation2);//单位关系2
    selectTr.find("input[cname='unitRelation3']").val(object.product.unitRelation3);//单位关系3
    selectTr.find("input[cname='retailPrice']").val(proBaseRetailPrice);//零售价
    selectTr.find("input[cname='retailPrice1']").val(object.product.retailPrice1);//零售价1
    selectTr.find("input[cname='retailPrice2']").val(object.product.retailPrice2);//零售价2
    selectTr.find("input[cname='retailPrice3']").val(object.product.retailPrice3);//零售价3
    selectTr.find("input[cname='retailMoney']").val(proBaseRetailPrice);   //零售金额
    selectTr.find("input[cname='costArith']").val(object.product.costArith);      //算法
    //end   添加隐藏控件
}


/**
 * 包含手工指定法    弹出窗体
 * @param trIndexs
 * @param productIds
 * @param trcostAriths
 * @param tbodyId
 * @return
 */
function callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId){
	var $parent = navTab.getCurrentPanel();
	var whichCallBack=$("#whichCallBack",$parent).val();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var trIndex;
	var productId;
	var trcostArith;
	if(trIndexs.indexOf(",")>0){
		trIndex=trIndexs.substring(0,trIndexs.indexOf(","));
		productId=productIds.substring(0,productIds.indexOf(","));
		trcostArith=trcostAriths.substring(0,trcostAriths.indexOf(","));
		
		trIndexs=trIndexs.substring(trIndexs.indexOf(",")+1,trIndexs.length);
		productIds=productIds.substring(productIds.indexOf(",")+1,productIds.length);
		trcostAriths=trcostAriths.substring(trcostAriths.indexOf(",")+1,trcostAriths.length);
	}else{
		trIndex=trIndexs;
		productId=productIds;
		trcostArith=trcostAriths;
		
		trIndexs="";
		productIds="";
		trcostAriths="";
	}
	
	
	var selectTr =  $parent.find("#"+tbodyId).find("tr:eq("+trIndex+")");
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	
	
	if(trcostArith==4){  //手工指定法
		var storageId = $parent.find("#storageId").val();
		
		var billStorageId = selectTr.find("td[cname='code']").attr("storageId");
		if("outStorageId" == billStorageId){
			storageId = selectTr.find("input[cname='outStorageId']").val();
		}else {
			storageId = selectTr.find("input[cname='storageId']").val();
		}
		if(!storageId||storageId==""||storageId==0){
			storageId=$parent.find("#"+billStorageId).val();
			storageId=storageId==undefined?0:storageId;
		}
		
		var options = {};
	    options.width="540";
	    options.height="350";
	    options.param={module:whichCallBack,storageId:storageId,productId:productId,trIndex:trIndex,trIndexs:trIndexs,productIds:productIds,trcostAriths:trcostAriths,tbodyId:tbodyId};
	    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "xsd_selPrdManSelPrd_dialog", "商品库存批次表", options);
	}else{
		if(trIndexs.indexOf(",")>0||trIndexs!=""){
			callbackStockWhichPrd(trIndexs,productIds,trcostAriths,tbodyId);
		}
	}
	sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	retailMoneysTotal($parent);//合计零售金额
	privilegeMoneyTotal();//合计优惠后金额
	if(whichCallBack=="adjustCost"){//成本调价单
		lastMoneyTotal($parent);//合计调整后金额
	}
}

/**
 * 单据自动生成控件离开事件     去掉控件 
 * @param obj
 * @return
 */
function aioBillOnblurRemoveWidget(obj){
	if($(obj)[0].nodeName=="TD"||$(obj)[0].nodeName=="DIV"){
		return false;
	}
	if($(obj).parents("td").attr("validateattr")&&$(obj).parents("td").attr("validateattr")!=""){
		if($(obj).valid()==false){
			$(obj).val("");
		}
	}
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	if($(obj)[0] && $(obj)[0].nodeName=="TR") currentTr = $(obj);
	var cname = $(obj).attr("cname");
	var val =  $(obj).val();
	var $td = $(obj).parent();
	var isContains = $.contains($td,$(obj));
	if(!$(obj).hasClass("dateISO") && isContains){
		$(obj).remove();
		$td.text(val);
	}
	currentTr.find("input[cname='"+cname+"']").val(val);
}

/**
 * 让tr里面所有td中的input值显示出来
 * @param selectTr
 * @return
 */
function showCurrentTrWidgetToSpan(selectTr){
	$(selectTr.find("td")).each(function(i,tdObj){
		var $divInput=$(tdObj).find("div");
		var hiddenVal=$("input[cname='"+$(tdObj).attr("cname")+"']",selectTr).val();
		//商品图片
		var isSavePath = $(tdObj).attr("cname");
		if(isSavePath&&isSavePath=="savePath"){
			if(hiddenVal&&hiddenVal!=''){
				$divInput.text("");
				hiddenVal=projectBasePath+"/upLoadImg/"+hiddenVal;
				var savePath = "<img style='width:20px; height:20px;' src='"+hiddenVal+"' onmouseout='toolTip()' onmouseover='toolTip(\"<img src="+hiddenVal+" width=400px height=350px\")'/>";
				$divInput.append(savePath);
			}
			return true;
		}
		//end商品图片
		var costLookPermission = $(tdObj).attr("costLookPermission");
		if(hiddenVal||hiddenVal==""){
			if(costLookPermission && costLookPermission=="lacksPermission"){
				$divInput.text("***");
				$(tdObj).attr("readonly","readonly");
			}else{
				$divInput.text(hiddenVal);
			}
		}
	});
}

/**
 * 清除让tr里面所有td中值
 * @param selectTr
 * @param hasAll
 * @return
 */
function clearTrWidgetSpanVal(selectTr,hasAll){
	var $tds=selectTr.find("td");
	$($tds).each(function(i,tdObj){
		if(hasAll){
			var $divInput=$(tdObj).find("div");
			$divInput.html("");
		}else{
			if(i==0){  //行号
				return;
			}else if(i==$tds.length-1){//删除
				return;  
			}else{
				var $divInput=$(tdObj).find("div");
				$divInput.html("");
			}
		}
		
	});
}
/**
 * 选择td再单击调出
 * @param selectTd
 * @param sel
 * @return
 */
function showCurrentTdWidget(selectTd,sel){
	var cname=$(selectTd).attr("cname");

	if(!cname || $(selectTd).find("input[cname='"+cname+"']").length>0){
		return;
	}
	var selectTr=$(selectTd).parent();
	var widgetObj=$(selectTr).find("input[cname='"+cname+"']");
	createWidget(selectTr,selectTd,"false");//根据th生成td里  控件
	$(selectTr).find("input[cname='"+cname+"']").val(widgetObj.val());
	if(sel){
		$(selectTd).find("input[cname='"+cname+"']:eq(0)").select();
	}
}
/*----------------------------------------end---公共方法--------------------------------------------*/





/*-------------------------------------------辅助公共方法-----------------------------------------------*/
/**
 * 单据清除tr数据
 * @param $trParent
 */
function billClearTrData($trParent){
	var tds=$trParent.find("td");
	//第一个行号与最后一个隐藏域单独处理
	tds.each(function(i,td){
		if(i==0){   //第一个
			return;
		}
		if(i==tds.length-1){  //最后一个
			var delLink=$(td).find("a");
            var hiddenWeidgets=delLink.nextAll();
            hiddenWeidgets.remove();
            $(td).find("input").val(""); //清空
		}else{
			$(td).html("<div></div>");
		}
	});
}
/**
 * 单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
 * @param currentTr
 * @param amount
 * @param calculateUnit1
 * @param calculateUnit2
 * @param calculateUnit3
 * @param unitRelation1
 * @param unitRelation2
 * @param unitRelation3
 * @return
 */
function xsUnitChange(currentTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3){
	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	if(selectUnitId==1){
		amount=amount;
	}else if(selectUnitId==2){
		amount=amount*unitRelation2;
	}else if(selectUnitId==3){
		amount=amount*unitRelation3;
	}
	var helpAmount=helpAmountStr(amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);//辅助数量
    //辅助数量   eg:1箱2包5支
	if($.isNumeric(amount)){
		currentTr.find("input[cname='helpAmount']").val(helpAmount);
	}else{
		currentTr.find("input[cname='helpAmount']").val("");        
	}
	//基础数量      eg:125支
	if($.isNumeric(amount)&&amount!=0){
		currentTr.find("input[cname='baseAmount']").val(round(amount,4)+calculateUnit1);
	}else{
		currentTr.find("input[cname='baseAmount']").val("");
	}
	//辅助数量1  eg:12.5包
	if($.isNumeric(amount)&&amount!=0&&$.isNumeric(unitRelation2)&&calculateUnit2!=null&&calculateUnit2!=""){
		helpAmount1=amount/unitRelation2;
		helpAmount1=round(helpAmount1,4)+calculateUnit2;
		currentTr.find("input[cname='baseAmount1']").val(helpAmount1);
	}else{
		currentTr.find("input[cname='baseAmount1']").val("");
	}
	//辅助数量2   eg:1.25箱
	if($.isNumeric(amount)&&amount!=0&&$.isNumeric(unitRelation3)&&calculateUnit3!=null&&calculateUnit3!=""){
		helpAmount2=amount/unitRelation3;
		helpAmount2=round(helpAmount2,4)+calculateUnit3;
		currentTr.find("input[cname='baseAmount2']").val(helpAmount2);
	}else{
		currentTr.find("input[cname='baseAmount2']").val("");
	}
}
/*----------------------------------------end---辅助公共方法--------------------------------------------*/



