//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*------------------------------------------------stable扩展功能---------------------------------------------------*/
/**
 * 单据表格滚动(左右移动，滚动条也动)
 */
function  billColScroll(objTd){
	if(!objTd){
		return;
	}
	var $td=$(objTd);
	var colsLenth=$($td.parent().parent().parent()).width();
	var showLenth=$($td.parent().parent().parent().parent().parent()).width();
	
	var tds=$($td).prevAll();
	var width=0;
	$(tds).each(function(key,leftTd){
		width=width+$(leftTd).width();
	});
	width=width+$($td).width();
	
	var scrollLeftLength=0;
	if(showLenth-300<=width){
		scrollLeftLength=width-(showLenth-300)
	}else{
		scrollLeftLength=0;
	}
	var $grid = $(objTd).parents("div.grid");
	var scroller = $(".gridScroller", $grid);
	var header = $(".gridThead", $grid);
	var tbody = $(".gridTbody2", $grid).find(".gridTbody");
	header.css("position", "relative");
	header.css("left", scroller.cssv("left") - scrollLeftLength);
	tbody.scrollLeft(scrollLeftLength);
	scroller.scrollLeft(scrollLeftLength);
    return false;
}
/**
 * 单据表格滚动(上下移动，滚动条也动)
 */
function  billVerticalScroll(objTd){
	if(!objTd){
		return;
	}
	var $td=$(objTd);
	var $tr = $(objTd).parent();
	var parantScroll = $td.parents(".gridTbody2:first");
	var showLenth=parantScroll.height();
	var height=0;
	var trs=$($tr).prevAll();
	$(trs).each(function(key,topTr){
		height=height+$(topTr).height();
	});
	var scrollTopLength=0;
	if(height<showLenth-550){
		scrollTopLength=0;
	}else{
		scrollTopLength=height-showLenth+50;
		parantScroll.scrollTop(scrollTopLength);
		
	}
	return false;
}
/**
 * tr 垂直滚动
 */
function  TrVerticalScroll(objTr){
	if(!objTr){
		return;
	}
	var $tr = $(objTr);
	var parantScroll = $tr.parents(".gridScroller:first") || $tr.parents(".gridTbody2:first");
	var showLenth=parantScroll.height();
	var height=0;
	var trs=$($tr).prevAll();
	$(trs).each(function(key,topTr){
		height=height+$(topTr).height();
	});
	
	var scrollTopLength=0;
	if(height<showLenth-550){
		scrollTopLength=0;
	}else{
		scrollTopLength=height-showLenth+50;
		parantScroll.scrollTop(scrollTopLength);
		
	}
	return false;
}
/*---------------------------------------------end---stable扩展功能------------------------------------------------*/


/*------------------------------------------------销售订单,进货订单---------------------------------------------------*/
//弹出单位
function sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3,whichCallBack){
	var baseUnit =val;//单位
    //赋值
    var panel=navTab.getCurrentPanel();
    var billType = panel.find("#billType").val();
    var _td=$(panel).find("td").filter(".selected");
    if(billType){
    	_td=$("tbody[type='"+billType+"']",panel).find("td").filter(".selected");
    	panel=$("tbody[type='"+billType+"']",panel).parents("div.grid:first");
    }
    var currentTr=$(_td).parent();
    
    var oldSelectUnitId=currentTr.find("input[cname='selectUnitId']").val();
    //var price=currentTr.find("input[cname='price']").val();//单价
	var downAmount=currentTr.find("input[cname='amount']").val(); //数量
	currentTr.find("input[cname='selectUnitId']").val(valId);    //你选择单位的ID
    currentTr.find("input[cname='baseUnit']").val(baseUnit);//单位
    currentTr.find("td[cname='baseUnit']").children("div").text(baseUnit);
	

    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
    xsUnitChange(currentTr,downAmount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);

    
    var retailPrice=0;
    var barCode = "";
    if(valId==1){
    	retailPrice=currentTr.find("input[cname='retailPrice1']").val();
    	barCode=currentTr.find("input[cname='barCode1']").val();
    }else if(valId==2){
    	retailPrice=currentTr.find("input[cname='retailPrice2']").val();
    	barCode=currentTr.find("input[cname='barCode2']").val();
    }else if(valId==3){
    	retailPrice=currentTr.find("input[cname='retailPrice3']").val();
    	barCode=currentTr.find("input[cname='barCode3']").val();
    }
    currentTr.find("input[cname='barCode']").val(barCode);//条码
    currentTr.find("input[cname='retailPrice']").val(retailPrice);//零售价
    
    /*//成本价格切换
    var costPrice = currentTr.find("input[cname='costPrice']").val();
    if(costPrice){
		costPrice =getConversionPrice(costPrice,valId,unitRelation1,unitRelation2,unitRelation3,oldSelectUnitId);
		currentTr.find("input[cname='costPrice']").val(costPrice);//对应单位的成本价格
	}*/
	
	
	
    rowAmountPriceDiscountChange(currentTr);//数量，单价，折扣  (横向)
	
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	sellbookOrboughgTotal(panel);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	retailMoneysTotal(panel);//合计零售金额
	
}
/**
 * 多个表格选择一个商品单位
 * @param valId
 * @param val
 * @param calculateUnit1
 * @param calculateUnit2
 * @param calculateUnit3
 * @param unitRelation1
 * @param unitRelation2
 * @param unitRelation3
 * @param retailPrice1
 * @param retailPrice2
 * @param retailPrice3
 * @return
 */
function moreTbaleBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3){
	var baseUnit =val;//单位
    //赋值
    var panel=navTab.getCurrentPanel();
    var billType = $(panel).find("input#billType").val();
    var $tbody =  $(panel).find("tbody[type='"+billType+"']");
    var $tbodyDiv =$(panel).find("tbody[type='"+billType+"']").parents("div.gridTbody2:first");
	var $tfootDiv = $tbodyDiv.next("div.gridScroller");
    var _td=$tbody.find("td").filter(".selected");
    var currentTr=$(_td).parent();
    var grid = $tbody.parents("div.grid:first");
    
    var univalence=currentTr.find("input[cname='price']").val();//单价
	var downAmount=currentTr.find("input[cname='amount']").val();; //最小单位数量
	currentTr.find("input[cname='selectUnitId']").val(valId);    //你选择单位的ID
    currentTr.find("input[cname='baseUnit']").val(baseUnit);//单位
    currentTr.find("td[cname='baseUnit']").children("div").text(baseUnit);
	
    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
    xsUnitChange(currentTr,downAmount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);

	currentTr.find("input[cname='price']").val(univalence);//单价
	var retailPrice=0;
    var barCode = "";
    if(valId==1){
    	retailPrice=currentTr.find("input[cname='retailPrice1']").val();
    	barCode=currentTr.find("input[cname='barCode1']").val();
    }else if(valId==2){
    	retailPrice=currentTr.find("input[cname='retailPrice2']").val();
    	barCode=currentTr.find("input[cname='barCode2']").val();
    }else if(valId==3){
    	retailPrice=currentTr.find("input[cname='retailPrice3']").val();
    	barCode=currentTr.find("input[cname='barCode3']").val();
    }
    currentTr.find("input[cname='barCode']").val(barCode);//条码
    currentTr.find("input[cname='retailPrice']").val(retailPrice);//零售价
	
    rowAmountPriceDiscountChange(currentTr);//数量，单价，折扣  (横向)
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	
	var amounts  = $tfootDiv.find("#amounts").val();
	var moneys  = $tfootDiv.find("#moneys").val();
	if(billType=="in"){
		panel.find("#inAmount").val(amounts);
		panel.find("#inMoney").val(moneys);
	}else{
		panel.find("#outAmount").val(amounts);
		panel.find("#outMoney").val(moneys);
	}
	privilegeMoneyTotal();//合计优惠后金额
	gapMoneyCalculate(panel);//算出换货差额
	privilegeMoneyDarter();//优惠金额
	sellbookOrboughgTotal(grid);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	retailMoneysTotal(grid);//合计零售金额
}
//弹出数量  
function sellbookInputAmount(amount){
    var panel=navTab.getCurrentPanel();
    var _td=$(panel).find("td").filter(".selected");//单据td
    var currentTr=$(_td).parent();
    
    if($.isNumeric(amount)){
    	currentTr.find("input[cname='amount']").val(amount);//数量
        currentTr.find("td[cname='amount']").children("div").text(amount);
	}else{
		currentTr.find("input[cname='amount']").val("");
	    currentTr.find("td[cname='amount']").children("div").text("");
	}
    
	/*rowAmountPriceDiscountChange(currentTr);//数量，单价，折扣  (横向)
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值在span中显示出来
	var $parent = navTab.getCurrentPanel();
	sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	privilegeMoneyTotal();*/
	
}
/**
 * 换货多表格数量带回
 * @param amount
 * @return
 */
function moreTableInputAmount(amount){
    var panel=navTab.getCurrentPanel();
    var $billType  = panel.find("input#billType").val();
	var $gridDiv = panel.find("tbody[type='"+$billType+"']").parents("div.grid:first");
    var _td=$gridDiv.find("td").filter(".selected");//单据td
    var currentTr=$(_td).parent();
    
    if($.isNumeric(amount)){
    	currentTr.find("input[cname='amount']").val(amount);//数量
        currentTr.find("td[cname='amount']").children("div").text(amount);
	}else{
		currentTr.find("input[cname='amount']").val("");
	    currentTr.find("td[cname='amount']").children("div").text("");
	}
    
    rowAmountPriceDiscountChange(currentTr);//数量，单价，折扣  (横向)
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值在span中显示出来
	amountsTotal($gridDiv);//table合计总数量
	moneysTotal($gridDiv);//table合计总金额
	discountMoneysTotal($gridDiv);//table合计折扣总金额
	taxesTotal($gridDiv);//合计税额
	taxMoneysTotal($gridDiv);//合计含税金额
	var whichCallBack = $("#whichCallBack",panel).val();
	
	barterParamTotal($gridDiv,whichCallBack);//合计换货参数

	
}
//期初库存商品绑定数量
function productInitAmount(amount){
	var panel=getCurrentPanel();
	$("#amount",panel).val(amount);
	countTwoModelVal('amount','mul','price','money');
}

//报损单绑定数量
function breakageBillAmount(amount){
	var currentTr=getTableCurrentTr();//当前行
	if($.isNumeric(amount)){
    	currentTr.find("input[cname='amount']").val(amount);//数量
        currentTr.find("td[cname='amount']").children("div").text(amount);
	}else{
		currentTr.find("input[cname='amount']").val("");
	    currentTr.find("td[cname='amount']").children("div").text("");
	}
}


//生产模板输入配套数量焦点离开
function inputAssortAmountBlur(obj){
	var $dialog = $(obj).parents("div.dialog:first");//当前dialog
	//所有行
	var allTr = $dialog.find("tbody tr");
	
	var currentTr = $(obj).parents("tr:first");//当前Tr
	if($(obj)[0].nodeName=="TR") currentTr = $(obj);
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		return;
	}
	var assortAmount = Number($(obj).val());//配套量
	var sckAmount = currentTr.find("input[cname='sckAmount']").val();//库存数量
	var produceAmount = $("#produceAmount",$dialog).val();//生产数量
	
	var $needAmount = currentTr.find("input[cname='needAmount']");//需求量
	var $remainAmount = currentTr.find("input[cname='remainAmount']");//库存余量
	
	if(!isNaN(produceAmount)  && !isNaN(assortAmount) && produceAmount!=0 && assortAmount!=0){
		$needAmount.val(round(produceAmount * assortAmount,4));
	}
	
	if(sckAmount=="" || sckAmount!="undefined" && sckAmount!=undefined)sckAmount==0;
	$remainAmount.val(round(sckAmount - produceAmount * assortAmount,4));
	
	aioBillOnblurRemoveWidget(obj);//清除input
	
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	
	var num=[];//可生产数量列表
	for ( var k = 0; k < allTr.length; k++) {
		var $tr1=$(allTr[k]);
		var sckAmount=$tr1.find("input[cname='sckAmount']").val();//库存数量
		var assortAmount=$tr1.find("input[cname='assortAmount']").val();//配套数量
		if(sckAmount!="undefined" && sckAmount!=undefined){
			var amount = sckAmount;
			if(assortAmount!="undefined" && assortAmount!=undefined && assortAmount!=""){
				amount = parseInt(sckAmount/assortAmount);
			}
			num.push(amount);
		}
	}
	var $allowAmount = $("#allowAmount",$dialog);//可生产数量
	var minAmount=0;
	for(k=0;k<num.length;k++){
		if(k==0) minAmount = num[k];
		if(num[k]=="" || num[k]<0){
			minAmount = 0;
			break;
		}
		minAmount = Math.min(minAmount,num[k]);
	}
	$allowAmount.text(minAmount);//设置可生产数量
	
}

//生产模板输入生产数量
function inputProduceAmountBlur(obj,$panel){
	if(obj && obj!=""){
		var $parent = $(obj).parents("div.dialog:first");//当前dialog面板
		var produceAmount = $(obj).val();//生产数量
	}else{
		var $parent = $panel;
		var produceAmount = $("#produceAmount",$parent).val();//生产数量
	}
	//所有行
	var allTr = $parent.find("tbody tr");
	for ( var i = 0; i < allTr.length; i++) {
		var $tr=$(allTr[i]);
		var assortAmount = $tr.find("input[cname='assortAmount']").val();//配套数量
		
		var sckAmount = $tr.find("input[cname='sckAmount']").val();//库存数量
		var $needAmount = $tr.find("input[cname='needAmount']");//需求量
		var $remainAmount = $tr.find("input[cname='remainAmount']");//库存余量
		
		if(!isNaN(produceAmount)  && !isNaN(assortAmount) && produceAmount!=0 && assortAmount!=0){
			$needAmount.val(round(produceAmount * assortAmount,4));
		}
		if(sckAmount=="" || sckAmount!="undefined" && sckAmount!=undefined)sckAmount==0;
		
		if($tr.find("input[cname='sckAmount']").val()!=""){
			$remainAmount.val(round(sckAmount - produceAmount * assortAmount,4));
		}
		
		showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值显示出来
		
	}
	
	
	
}

//生产模板得到最小生产数量
function getMinAmountByArray(allTr,$parent){
	var num=[];//可生产数量列表
	for ( var k = 0; k < allTr.length; k++) {
		var $tr1=$(allTr[k]);
		var sckAmount=$tr1.find("input[cname='sckAmount']").val();//库存数量
		var assortAmount=$tr1.find("input[cname='assortAmount']").val();//配套数量
		if(sckAmount!="undefined" && sckAmount!=undefined){
			var amount = sckAmount;
			if(assortAmount!="undefined" && assortAmount!=undefined && assortAmount!=""){
				amount = parseInt(sckAmount/assortAmount);
			}
			num.push(amount);
		}
	}
	var $allowAmount = $("#allowAmount",$parent);//可生产数量
	var minAmount=0;
	for(k=0;k<num.length;k++){
		if(k==0) minAmount = num[k];
		if(num[k]=="" || num[k]<0){
			minAmount = 0;
			break;
		}
		minAmount = Math.min(minAmount,num[k]);
	}
	$allowAmount.attr('text',minAmount);//设置可生产数量  (兼容IE8的写法)
}

/**
 * 输入成本调价后单价
 * @param obj
 * @return
 */
function inputLastPrice(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	if($(obj)[0] && $(obj)[0].nodeName=="TR") currentTr = $(obj);
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		$(obj).remove();
		return;
	}
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var money = currentTr.find("input[cname='money']").val();//金额
	var lastPrice = currentTr.find("input[cname='lastPrice']").val();//调整后单价
	if($.trim(lastPrice)=="" || $.trim(lastPrice)==0){
		lastPrice = 0;
	}
	if($.isNumeric(lastPrice)){
		lastPrice = round(lastPrice,4);
		currentTr.find("input[cname='lastPrice']").val(lastPrice==0?"":lastPrice);//调整后单价
		currentTr.find("td[cname='lastPrice']").children("div").text(lastPrice==0?"":lastPrice);//调整后单价
	}
	if($.isNumeric(amount) && $.isNumeric(lastPrice) ){
		currentTr.find("input[cname='lastMoney']").val(round(lastPrice*amount,4));//调整后金额
		currentTr.find("td[cname='lastMoney']").children("div").text(round(lastPrice*amount,4));//调整后金额
	}else{
		currentTr.find("input[cname='lastMoney']").val("");//调整后金额
		currentTr.find("td[cname='lastMoney']").children("div").text("");//调整后金额
	}
	
	if($.isNumeric(amount) && $.isNumeric(lastPrice) && $.isNumeric(money)){
		currentTr.find("input[cname='adjustMoney']").val(round(lastPrice*amount-money,4));//调整金额
		currentTr.find("td[cname='adjustMoney']").children("div").text(round(lastPrice*amount-money,4));//调整金额
	}else{
		currentTr.find("input[cname='adjustMoney']").val("");//调整金额
		currentTr.find("td[cname='adjustMoney']").children("div").text("");//调整金额
	}
	var $parent = navTab.getCurrentPanel()||$(obj).parents("div.grid:first");
	lastMoneyTotal($parent);//合计调整后金额
	adjustMoneyTotal($parent);//合计调整金额
	sellbookOrboughgTotal($parent);
}

/**
 * 输入成本调价后金额
 * @param obj
 * @return
 */
function inputLastMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	if($(obj)[0].nodeName=="TR") currentTr = $(obj);
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		$(obj).remove();
		return;
	}
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var money = currentTr.find("input[cname='money']").val();//金额
	var lastMoney = currentTr.find("input[cname='lastMoney']").val();//调整后金额
	if($.trim(lastMoney)=="" || $.trim(lastMoney)==0){
		return;
	}
	if($.isNumeric(lastMoney)){
		lastMoney = round(lastMoney,4);
		currentTr.find("input[cname='lastMoney']").val(lastMoney==0?"":lastMoney);//调整后金额
		currentTr.find("td[cname='lastMoney']").children("div").text(lastMoney==0?"":lastMoney);//调整后金额
	}
	if($.isNumeric(amount) && $.isNumeric(lastMoney) ){
		currentTr.find("input[cname='lastPrice']").val(round(lastMoney/amount,4));//调整后单价
		currentTr.find("td[cname='lastPrice']").children("div").text(round(lastMoney/amount,4));//调整后单价
	}else{
		currentTr.find("input[cname='lastPrice']").val("");//调整后金额
		currentTr.find("td[cname='lastPrice']").children("div").text("");//调整后金额
	}
	
	if($.isNumeric(amount) && $.isNumeric(lastMoney) && $.isNumeric(money)){
		currentTr.find("input[cname='adjustMoney']").val(round(money-lastMoney,4));//调整金额
		currentTr.find("td[cname='adjustMoney']").children("div").text(round(money-lastMoney,4));//调整金额
	}else{
		currentTr.find("input[cname='adjustMoney']").val("");//调整金额
		currentTr.find("td[cname='adjustMoney']").children("div").text("");//调整金额
	}
	var $parent = navTab.getCurrentPanel();
	lastMoneyTotal($parent);//合计调整后金额
	adjustMoneyTotal($parent);//合计调整金额
}






//整体折扣变化
function billDiscountsChange(obj){
	
	var discounts=$(obj).val();
	var oldDiscounts=$(obj).attr("oldValue");
	if(oldDiscounts&&discounts&&discounts!=""&&discounts!=oldDiscounts){
		var trs=$("#"+$(obj).attr("bodyId")).find("tr");
		$(trs).each(function(i){
			$this=$(this);
			var productId=$this.find("input[cname='productId']").val();
			if(productId&&productId!=""){
				$this.find("input[cname='discount']").val(discounts);
				$this.find("td[cname='discount']").children("div").text(discounts);
				rowAmountPriceDiscountChange($this);//数量，单价，折扣  (横向)
				showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
				var $parent = navTab.getCurrentPanel();
				amountsTotal($parent);//table合计总数量
				moneysTotal($parent);//table合计总金额
				discountMoneysTotal($parent);//table合计折扣总金额
			}
		});
	}
	$(obj).attr("oldValue",discounts);  //把新值赋给旧值
}
//输入整单折扣，单据折扣跟着变化 eg:进货订单
function inputAllDiscounts(obj,params){
	var discount = $(obj).val();
	if(!discount||discount==""){
		discount=1;
		$(obj).val(1);
	}
	//if($.trim(discount)=="" || !$.isNumeric(discount))return;
	var $parent = navTab.getCurrentPanel();
	var tbody = "";
	if(params.tbody)tbody = params.tbody;
	var cname  = "discount";
	if(params.cname)cname = params.cname;
	var event = "amountPriceDiscountChange";
	if(params.event)event = params.event;
	var $callback = eval('(' + event + ')');
	$("#"+tbody,$parent).find("td[cname='discount']").each(function(){
		    var productId=$(this).parents("tr:eq(0)").find("input[cname='productId']").val(); //当前行有没有商品ID
			if(!productId||productId=="")return false;
	    	$(this).children("div").text(discount);
    });
    $("#"+tbody,$parent).find("input[cname='discount']").each(function(){
    	$(this).val(discount);
    	$callback($(this),false);
    });
}


/*----------------------------------------------end--销售订单,进货订单------------------------------------------------*/

/*------------------------------------------------单据图片辅助功能------------------------------------------------*/
//商品合并
function productMerge(obj){
	var $parent = getCurrentPanel();
	var mergeBtnObj=$parent.find("#"+$(obj).attr("bodyId"));
	var trs=mergeBtnObj.find(">tr");
	var workLines=new Array();  //有效记录行
	trs.each(function(i){
		$this=$(this);
		var prodcutId=$this.find("input[cname='productId']").val();
		if(prodcutId&&prodcutId!=""){
			workLines.push($this);
		}
	});
	var map = {};  //map<商品ID，list<tr>>
	$(workLines).each(function(key1,value1){
		var storageId1=$(value1).find("input[cname='storageId']").val()||$("#storageId",$parent).val()||"";
		var prodcutId1=$(value1).find("input[cname='productId']").val();
		var discount1=$(value1).find("input[cname='discount']").val()||"1";
		var baseUnit1=$(value1).find("input[cname='baseUnit']").val()||"";
		var batch1=$(value1).find("input[cname='batch']").val()||"";
		var produceDate1=$(value1).find("input[cname='produceDate']").val()||"";
		var produceEndDate1=$(value1).find("input[cname='produceEndDate']").val()||"";
		$(workLines).each(function(key2,value2){
			var storageId2=$(value2).find("input[cname='storageId']").val()||$("#storageId",$parent).val()||"";
			var prodcutId2=$(value2).find("input[cname='productId']").val();
			var discount2=$(value2).find("input[cname='discount']").val()||"1";
			var baseUnit2=$(value2).find("input[cname='baseUnit']").val()||"";
			var batch2=$(value2).find("input[cname='batch']").val()||"";
			var produceDate2=$(value2).find("input[cname='produceDate']").val()||"";
			var produceEndDate2=$(value2).find("input[cname='produceEndDate']").val()||"";
			var costArith =$(value2).find("input[cname='costArith']").val()||"1";
			if(costArith==1){
				if(key1!=key2&&storageId1==storageId2&&prodcutId1==prodcutId2&&discount1&&discount1==discount2&&baseUnit1==baseUnit2){  //仓库ID-商品ID-折扣 -单位 (一样)
					var has = prodcutId1 in map;
					var list;
					if(has==true){  //存在该商品
						list=map[prodcutId1];
					}else{
						list=new Array();
					}
					list.push(value2);
					map[prodcutId1]=list;
					//delete map[key];
					return false;
				}
			}else{
				if(batch1==batch2&&produceDate1==produceDate2&&produceEndDate1==produceEndDate2&&key1!=key2&&storageId1==storageId2&&prodcutId1==prodcutId2&&discount1&&discount1==discount2&&baseUnit1==baseUnit2){  //仓库ID-商品ID-折扣 -单位 (一样)
					var has = prodcutId1 in map;
					var list;
					if(has==true){  //存在该商品
						list=map[prodcutId1];
					}else{
						list=new Array();
					}
					list.push(value2);
					map[prodcutId1]=list;
					//delete map[key];
					return false;
				}
			}
			
		});
	});
	var delTrs=new Array(); //要删除的tr
	$.each(map,function(key,values){
        var listTrs=values;
        var amounts=0; //数量
        var minIndex=parseInt($.trim(listTrs[0].find("td:first").text()));
        $(listTrs).each(function(i){
        	$tr=$(this);
        	var amount=$tr.find("input[cname='amount']").val();  //商品数量累加
        	if(amount&&amount>0){
        		amounts+=round(amount,4);
        	}
        	var currendIndex=parseInt($.trim($tr.find("td:first").text()));
        	if(minIndex>currendIndex){
        		minIndex=currendIndex;
        	}else{
        		delTrs.push($tr);
        	}
        });
        var minTr=mergeBtnObj.find(">tr:eq("+parseInt(minIndex-1)+")");
        minTr.find("input[cname='amount']").val(amounts);
        minTr.find("td[cname='amount']").children().text(amounts);
        rowAmountPriceDiscountChange(minTr);//数量，单价，折扣  (横向)
        showCurrentTrWidgetToSpan(minTr);//让tr里面所有td中的input值显示出来
	});
    $(delTrs).each(function(){
    	removeTrChangeAttr($(this));
    }); 
    //重新计算合计
    trs=mergeBtnObj.find(">tr");
	trs.each(function(i){
		$this=$(this);
		var prodcutId=$this.find("input[cname='productId']").val();
		if(prodcutId&&prodcutId!=""){
			var $currentElement = $this.find("td[cname='amount']");
			var ev  = $currentElement.attr("onBlur");
			var arr = new Array();
			 arr = ev.split("(");
			if(arr[0]){
				var $callback = eval('(' + arr[0] + ')');
				$callback($currentElement);
			}
			return;
		}
	});
	privilegeMoneyTotal();//优惠后的金额
   return true;
	
}

//附件
function openOrderFujianDialog(basePath,tableId,action,billId){
	var $panel = getCurrentPanel();
	var ids = $("#orderFuJianIds",$panel).val();
	if(!ids)ids="";
	var options={};
	options.width="525";
    options.height="400";
    options.jsonData={tableId:tableId,action:action,billId:billId};	   
	$.pdialog.open(basePath+"/upload/toOrderFujianUpLoad/"+ids, "order_fujianDialog_id", "单据附件操作", options);
}
/*----------------------------------------------end--单据图片辅助功能------------------------------------------------*/

/**
 * 输入借方金额
 * @param obj
 * @return
 */
function inputDebitMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var accountsId=$("input[cname='accountsId']",currentTr).val();
	if(!accountsId||accountsId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	var debitMoney = currentTr.find("input[cname='debitMoney']").val()||0;
	if($.isNumeric(debitMoney) && debitMoney!=0){
		currentTr.find("td[cname='debitMoney']").children("div").val(round(debitMoney,4));//
		currentTr.find("td[cname='debitMoney']").children("div").text(round(debitMoney,4));//
	}else{
		currentTr.find("input[cname='debitMoney']").val("");//
		currentTr.find("td[cname='debitMoney']").children("div").text("");//
	}
	var $parent = navTab.getCurrentPanel();
	debitMoneyTotal($parent);
}

/**
 * 输入贷方金额
 * @param obj
 * @return
 */
function inputLendMoney(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var accountsId=$("input[cname='accountsId']",currentTr).val();
	if(!accountsId||accountsId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	var lendMoney = currentTr.find("input[cname='lendMoney']").val()||0;
	if($.isNumeric(lendMoney) && lendMoney!=0){
		currentTr.find("td[cname='lendMoney']").children("div").val(round(lendMoney,4));//贷方金额
		currentTr.find("td[cname='lendMoney']").children("div").text(round(lendMoney,4));//贷方金额
	}else{
		currentTr.find("input[cname='lendMoney']").val("");//贷方金额
		currentTr.find("td[cname='lendMoney']").children("div").text("");//贷方金额
	}
	var $parent = navTab.getCurrentPanel();
	lendMoneyTotal($parent);
}






/*------------------------------------------------库存盘点单--------------------------------------------------------*/
//弹出数量改变
function takeStockInputAmount(amount){
    var panel=navTab.getCurrentPanel();
    var _td=$(panel).find("td").filter(".selected");//单据td
    var currentTr=$(_td).parent();
    
    if($.isNumeric(amount)){
    	currentTr.find("input[cname='amount']").val(amount);//数量
        currentTr.find("td[cname='amount']").children("div").text(amount);
	}else{
		currentTr.find("input[cname='amount']").val("");
	    currentTr.find("td[cname='amount']").children("div").text("");
	}

	var calculateUnit1=currentTr.find("input[cname='calculateUnit1']").val();//单位名称1
    var calculateUnit2=currentTr.find("input[cname='calculateUnit2']").val();//单位名称2
    var calculateUnit3=currentTr.find("input[cname='calculateUnit3']").val();//单位名称3
    var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();//单位关系1
    var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();//单位关系2
    var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();//单位关系3
    
    var stcokAmount=currentTr.find("input[cname='sckAmount']").val();//库存数量
	var sckCostPrice=currentTr.find("input[cname='price']").val();//成本单价
    
    //计算辅助数量
	var helpAmount=helpAmountStr(amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
	
	if($.isNumeric(amount)){
		currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量
		currentTr.find("td[cname='helpAmount']").children("div").text(helpAmount);//辅助数量
	}else{
		currentTr.find("input[cname='helpAmount']").val("");//辅助数量
		currentTr.find("td[cname='helpAmount']").children("div").text("");//辅助数量
	}
	
	//盈亏数量和盈亏金额
	if($.isNumeric(amount)&&amount>0&&amount-stcokAmount!=0&&(amount-stcokAmount)*sckCostPrice!=0){
		//计算亏盈数量
		currentTr.find("input[cname='gainAndLossAmount']").val(amount-stcokAmount);
		currentTr.find("td[cname='gainAndLossAmount']").children("div").text(amount-stcokAmount);
		//计算亏盈金额
		currentTr.find("input[cname='gainAndLossMoney']").val(round((amount-stcokAmount)*sckCostPrice,4));
		currentTr.find("td[cname='gainAndLossMoney']").children("div").text(round((amount-stcokAmount)*sckCostPrice,4));
		
	}else{
		//计算亏盈数量
		currentTr.find("input[cname='gainAndLossAmount']").val("");
		currentTr.find("td[cname='gainAndLossAmount']").children("div").text("");
		//计算亏盈金额
		currentTr.find("input[cname='gainAndLossMoney']").val("");
		currentTr.find("td[cname='gainAndLossMoney']").children("div").text("");
	}
	var $parent = navTab.getCurrentPanel();
	amountsTotal($parent);//table合计总数量
	sckGainAndLossAmountsTotal($parent);//table合计亏盈数量
	sckKYMoneyTotal($parent);//合计亏盈金额
	
}

//盘点数量失去焦点的改变
function inputTakeCount(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		var cname = $(obj).attr("cname");
		currentTr.find("td[cname='"+cname+"']").children("div").text("");
		return;
	}
	
	aioBillOnblurRemoveWidget(obj);
	
	var calculateUnit1=currentTr.find("input[cname='calculateUnit1']").val();//单位名称1
    var calculateUnit2=currentTr.find("input[cname='calculateUnit2']").val();//单位名称2
    var calculateUnit3=currentTr.find("input[cname='calculateUnit3']").val();//单位名称3
    var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();//单位关系1
    var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();//单位关系2
    var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();//单位关系3
    
	var amount = currentTr.find("input[cname='amount']").val();//盘点数量
	if($.isNumeric(amount)){
		amount = round(amount,4);
		currentTr.find("input[cname='amount']").val(amount);//辅助数量
	}
	var stcokAmount=currentTr.find("input[cname='sckAmount']").val();//库存数量
	var sckCostPrice=currentTr.find("input[cname='price']").val();//成本单价
	if(sckCostPrice=="")sckCostPrice=0;
	 //计算辅助数量
	var helpAmount=helpAmountStr(amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
	
	if($.isNumeric(amount)){
		currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量
	}else{
		currentTr.find("input[cname='helpAmount']").val("");//辅助数量
	}
	if($.isNumeric(amount)){
		//计算亏盈数量
		currentTr.find("input[cname='gainAndLossAmount']").val(round(amount-stcokAmount,4));
		//计算亏盈金额
		currentTr.find("input[cname='gainAndLossMoney']").val(round((amount-stcokAmount)*sckCostPrice,4));
	}else{
		//计算亏盈数量
		currentTr.find("input[cname='gainAndLossAmount']").val("");
		//计算亏盈金额
		currentTr.find("input[cname='gainAndLossMoney']").val("");
	}
	
	showCurrentTrWidgetToSpan(currentTr);//成本权限
	
	var $parent = navTab.getCurrentPanel();
	amountsTotal($parent);//table合计盘点总数量
	sckGainAndLossAmountsTotal($parent);//table合计亏盈数量
	sckKYMoneyTotal($parent);//合计亏盈金额
	
}


//库存盘点单切换仓库时校验并返回需要清空的行数组
function takeStockCutStorage(id){
	var $parent = navTab.getCurrentPanel();
	if(id){
		$parent = $("#"+id,$parent).parents("div.grid");
	}
	var $trs=$("tbody:first tr",$parent);
	var delTrs=[];
	
	for ( var i = 0; i < $trs.length; i++) {
		var productId=$($trs[i]).find("input[cname='productId']").val();
		if(productId && productId!=""){
			delTrs.push($($trs[i])); //添加到最后
		}
	}
	return delTrs;
}

/*----------------------------------------------end--库存盘点单-----------------------------------------------------*/



/*------------------------------------------------报损单--------------------------------------------------------*/
//根据参数(仓库，单位，批次，数量)改变单价,金额赋值
function paramCallBackPrcie(obj){
	var currentTr;//当前Tr
	if(obj){
		currentTr = $(obj).parents("tr:first");//当前Tr
		aioBillOnblurRemoveWidget(obj);
	}else {
		currentTr=getTableCurrentTr();
	}
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手ajax请求
	
	var productId=currentTr.find("input[cname='productId']").val();
	
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		var cname = $(obj).attr("cname");
		currentTr.find("td[cname='"+cname+"']").children("div").text("");
		return;
	}
	
	var storageId=currentTr.find("input[cname='storageId']").val();
	if(!storageId){
		storageId=$("#storageId",$parent).val();
	}
	var costArith=currentTr.find("input[cname='costArith']").val();
	var amount=currentTr.find("input[cname='amount']").val();
	
	
	var oldSelectUnitId=currentTr.find("input[cname='oldSelectUnitId']").val();
	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	var calculateUnit1=currentTr.find("input[cname='calculateUnit1']").val();
	var calculateUnit2=currentTr.find("input[cname='calculateUnit2']").val();
	var calculateUnit3=currentTr.find("input[cname='calculateUnit3']").val();
	var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();
	var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();
	var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();
	var multiple;//倍数
    if(selectUnitId==1){
    	multiple=unitRelation1;
    }else if (selectUnitId==2) {
    	multiple=unitRelation2;
	}else if (selectUnitId==3) {
		multiple=unitRelation3;
	}
    
    var baseAmount=round(amount * multiple,4);//实际数量(基本数量)
    currentTr.find("input[cname='baseAmount']").val(baseAmount);//赋值基本数量
    var helpAmount=helpAmountStr(baseAmount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
    currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量
    
    var basePrice = currentTr.find("input[cname='basePrice']").val();//原价格
	currentTr.find("input[cname='price']").val(round(basePrice * multiple,4));
	currentTr.find("input[cname='money']").val(round(baseAmount * basePrice,4));
    
	
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}
//单位带回
function unitCallBack(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3){
	var baseUnit =val;//单位
    //赋值
    var panel=navTab.getCurrentPanel();
    var _td=$(panel).find("td").filter(".selected");
    var currentTr=$(_td).parent();
   
	   
	currentTr.find("input[cname='selectUnitId']").val(valId);    //选择单位的ID
    currentTr.find("input[cname='baseUnit']").val(baseUnit);//单位
    currentTr.find("td[cname='baseUnit']").children("div").text(baseUnit);
    
    
    var retailPrice=0;
    var barCode = "";
    if(valId==1){
    	retailPrice=currentTr.find("input[cname='retailPrice1']").val();
    	barCode=currentTr.find("input[cname='barCode1']").val();
    }else if(valId==2){
    	retailPrice=currentTr.find("input[cname='retailPrice2']").val();
    	barCode=currentTr.find("input[cname='barCode2']").val();
    }else if(valId==3){
    	retailPrice=currentTr.find("input[cname='retailPrice3']").val();
    	barCode=currentTr.find("input[cname='barCode3']").val();
    }
    currentTr.find("input[cname='barCode']").val(barCode);//条码
    currentTr.find("input[cname='retailPrice']").val(retailPrice);//零售价
    

    var amount=currentTr.find("input[cname='amount']").val();//数量
    var basePrice=currentTr.find("input[cname='basePrice']").val();//单价
    var multiple;//倍数
    if(valId==1){
    	multiple=unitRelation1;
    }else if (valId==2) {
    	multiple=unitRelation2;
	}else if (valId==3) {
		multiple=unitRelation3;
	}
    var baseAmount = round(amount * multiple,4);//实际数量
    var price = round(basePrice * multiple,4);//切换单价
    var money = round(price * amount,4);//切换金额
    var helpAmount=helpAmountStr(baseAmount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
    currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量
    currentTr.find("input[cname='baseAmount']").val(baseAmount);//赋值实际数量
    currentTr.find("input[cname='price']").val(price);//赋值实际数量
    currentTr.find("input[cname='money']").val(money);//赋值实际数量
    
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	
}



/*----------------------------------------------end--报损单-----------------------------------------------------*/


/*------------------------------------------------报溢单--------------------------------------------------------*/
//根据参数(仓库，单位)改变单价,金额
function paramCallBackOverflowPrcie(){
	
	var currentTr=getTableCurrentTr();//当前Tr
	
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手ajax请求
	
	var productId=currentTr.find("input[cname='productId']").val();
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		var cname = $(obj).attr("cname");
		currentTr.find("td[cname='"+cname+"']").children("div").text("");
		return;
	}
	
	var storageId=currentTr.find("input[cname='storageId']").val();
	var costArith=currentTr.find("input[cname='costArith']").val();
	var amount=currentTr.find("input[cname='amount']").val();
	
	
	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();
	var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();
	var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();
	var multiple;//倍数
	if(selectUnitId==1){
		multiple=unitRelation1;
	}else if (selectUnitId==2) {
		multiple=unitRelation2;
	}else if (selectUnitId==3) {
		multiple=unitRelation3;
	}
  
  var baseAmount=amount * multiple;//基本数量
  currentTr.find("input[cname='baseAmount']").val(baseAmount);//赋值基本数量
  
  var basePrice = currentTr.find("input[cname='basePrice']").val();//原价格
  
	if(costArith==1){//移动加权商品
		//移动加权商品ajax请求得到单价
		$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/avgProPice",
		   dataType: "json",
		   data: {storageId:storageId,productId:productId,costArith:costArith},
		   async: false,
		   success: function(json){
		      if($.isNumeric(json.avgPrice)){
		    	  currentTr.find("input[cname='basePrice']").val(json.avgPrice);
		    	  currentTr.find("input[cname='price']").val(json.avgPrice * multiple);
		    	  currentTr.find("input[cname='money']").val(baseAmount * json.avgPrice);
		      }else {
		    	  	//没有成本就拿最近进价
			  		$.ajax({
			  			   type: "POST",
			  			   url: basePath+"/stock/stock/lastBuyPrice",
			  			   dataType: "json",
			  			   data: {productId:productId,selectUnitId:selectUnitId},
			  			   async: false,
			  			   success: function(json){
				  			      if($.isNumeric(json.lastBuyPrice)){
				  			    	  currentTr.find("input[cname='basePrice']").val(json.lastBuyPrice / multiple);
				  			    	  currentTr.find("input[cname='price']").val(json.lastBuyPrice);
				  			    	  currentTr.find("input[cname='money']").val(amount * json.lastBuyPrice);
				  			      }else {
				  			    	  currentTr.find("input[cname='basePrice']").val("");
					  		    	  currentTr.find("input[cname='price']").val("");
					  		    	  currentTr.find("input[cname='money']").val("");
					  		    	  currentTr.find("td[cname='price']").find("div").html("");
					  		    	  currentTr.find("td[cname='money']").find("div").html("");
				  			      }
			  			   }
			  		});
		      }
		      
		   }
		});
	}else{
		//非移动加权成本算法商品,则以最近进价入库
		$.ajax({
			   type: "POST",
			   url: basePath+"/stock/stock/lastBuyPrice",
			   dataType: "json",
			   data: {productId:productId,selectUnitId:selectUnitId},
			   async: false,
			   success: function(json){
	  			      if($.isNumeric(json.lastBuyPrice)){
		  			      currentTr.find("input[cname='basePrice']").val(json.lastBuyPrice / multiple);
		  			      currentTr.find("input[cname='price']").val(json.lastBuyPrice);
		  			      currentTr.find("input[cname='money']").val(amount * json.lastBuyPrice);
	  			      }else {
	  			    	  currentTr.find("input[cname='basePrice']").val("");
		  		    	  currentTr.find("input[cname='price']").val("");
		  		    	  currentTr.find("input[cname='money']").val("");
		  		    	  currentTr.find("td[cname='price']").find("div").html("");
		  		    	  currentTr.find("td[cname='money']").find("div").html("");
	  			      }
			   }
		});
	}
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}

//数量改变    
function overflowAmountChange(obj){
	var currentTr;//当前Tr
	if(obj){
		currentTr = $(obj).parents("tr:first");//当前Tr
	}else {
		currentTr=getTableCurrentTr();
	}
	var $parent = navTab.getCurrentPanel();
	
	var productId=currentTr.find("input[cname='productId']").val();
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		var cname = $(obj).attr("cname");
		currentTr.find("td[cname='"+cname+"']").children("div").text("");
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	
	var amount=$(obj).val();
	var price=currentTr.find("input[cname='price']").val();//单价
	var money="";
	if(price!=""){
		money=round(amount*price,4);
	}
	
//	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	var calculateUnit1=currentTr.find("input[cname='calculateUnit1']").val();
	var calculateUnit2=currentTr.find("input[cname='calculateUnit2']").val();
	var calculateUnit3=currentTr.find("input[cname='calculateUnit3']").val();
	var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();
	var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();
	var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();
	xsUnitChange(currentTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
/*	var multiple;//倍数
	if(selectUnitId==1){
		multiple=unitRelation1;
	}else if (selectUnitId==2) {
		multiple=unitRelation2;
	}else if (selectUnitId==3) {
		multiple=unitRelation3;
	}
  
	var baseAmount=amount * multiple;//基本数量
	currentTr.find("input[cname='baseAmount']").val(baseAmount);//赋值基本数量
	var helpAmount=helpAmountStr(baseAmount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
    currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量*/
    currentTr.find("input[cname='money']").val(money);//金额
    
    showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
	
}



/*----------------------------------------------end--报溢单-----------------------------------------------------*/



/*-----------------------------其它入库   (单位,仓库) 改变(单价,金额)跟着改变-------------------------------*/
function paramCallBackOtherInPrcie(){
	var currentTr=getTableCurrentTr();//当前Tr
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手ajax请求
	var productId=currentTr.find("input[cname='productId']").val();
	var storageId=currentTr.find("input[cname='storageId']").val();
	//子表没有拿头表
	if(!storageId){
		storageId=$parent.find("#storageId").val();
	}
	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	var costArith=currentTr.find("input[cname='costArith']").val();
	var amount=currentTr.find("input[cname='amount']").val();
  
	// 商品带回获取商品单价   优先级   1.平均成本单价---》2.最近进价
    var price=getProductBackPrice(basePath,storageId,productId,costArith,selectUnitId);
    currentTr.find("input[cname='price']").val(price);
    if(price){
    	currentTr.find("input[cname='money']").val(price*amount);
    }else{
    	currentTr.find("input[cname='price']").val("");
    	currentTr.find("input[cname='money']").val("");
    }
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}

function paramCallBackCostPrcie(unitRelation1,unitRelation2,unitRelation3,oldSelectUnitId){
	var $parent = navTab.getCurrentPanel();
	var currentTr=getTableCurrentTr();//当前Tr
	 var billType = $parent.find("#billType").val();
	 if(billType){
	    	
		 $parent=$("tbody[type='"+billType+"']",$parent).parents("div.grid:first");
	 }
	var amount=currentTr.find("input[cname='amount']").val();
	var price = currentTr.find("input[cname='price']").val();
	var selectUnitId = currentTr.find("input[cname='selectUnitId']").val();
	
	price = getConversionPrice(price,oldSelectUnitId,unitRelation1,unitRelation2,unitRelation3,selectUnitId)
	
	currentTr.find("input[cname='price']").val(round(price,4));
    if(price){
    	currentTr.find("input[cname='money']").val(round(price*amount,4));
    }else{
    	currentTr.find("input[cname='price']").val("");
    	currentTr.find("input[cname='money']").val("");
    }
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}
/*----------------------------end-其它入库   (单位,仓库) 改变(单价,金额)跟着改变-------------------------------*/

/*-----------------------------其它出库   (单位,批次) 改变(单价,金额)跟着倍数增加减-------------------------------*/
function paramCallBackOtherOutPrcie(){
	var currentTr=getTableCurrentTr();//当前Tr
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手ajax请求
	var productId=currentTr.find("input[cname='productId']").val();
	var storageId=currentTr.find("input[cname='storageId']").val();
	//子表没有拿头表
	if(!storageId){
		storageId=$parent.find("#storageId").val();
	}
	var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
	var costArith=currentTr.find("input[cname='costArith']").val();
	var amount=currentTr.find("input[cname='amount']").val();
  
	// 商品带回获取商品单价   优先级   1.平均成本单价---》2.最近进价
    var price=getProductBackPrice(basePath,storageId,productId,costArith,selectUnitId);
    currentTr.find("input[cname='price']").val(price);
    if(price){
    	currentTr.find("input[cname='money']").val(price*amount);
    }else{
    	currentTr.find("input[cname='price']").val("");
    	currentTr.find("input[cname='money']").val("");
    }
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	//
	
	
	
	var amount=selectTr.find("input[cname='amount']").val();
	var selectUnitId=selectTr.find("input[cname='selectUnitId']").val();//选择的单位ID
	var calculateUnit1=lastTd.find("input[cname='calculateUnit1']").val();
	var calculateUnit2=lastTd.find("input[cname='calculateUnit2']").val();
	var calculateUnit3=lastTd.find("input[cname='calculateUnit3']").val();
	var unitRelation1=lastTd.find("input[cname='unitRelation1']").val();
	var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
	var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
	
	if(selectUnitId==2&&unitRelation2){
		costPrice=costPrice*unitRelation2;
	}else if(selectUnitId==3&&unitRelation3){
		costPrice=costPrice*unitRelation3;
	}

	if(costPrice){
    	selectTr.find("input[cname='price']").val(costPrice);
    	selectTr.find("input[cname='money']").val(costPrice*amount);
    }else{
    	selectTr.find("input[cname='price']").val("");
    	selectTr.find("input[cname='money']").val("");
    }
	var helpAmount=helpAmountStr(1,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
	selectTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量 
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
}
/*----------------------------end-其它出库   (单位,批次) 改变(单价,金额)跟着倍数增加减-------------------------------*/



/*-----------------------------------------------列配置------------------------------------------------------------*/
//切换Td能输入
function cutTdInput(obj,name,cla,len){
	var $p = $(obj).parent();
	
	if($(obj).find("input[name="+name+"]") && $(obj).find("input[name="+name+"]").length>0){
		return;
	}
	var text = $.trim($p.find("input[cname="+name+"]").val());
	$(obj).html("<input type='text' class='stealth "+cla+"' name='"+name+"' value='' maxlength='"+len+"'/>");
	$(obj).find("input[type=text]").focus().val(text);
	$(obj).find("input[type=text]").blur(function (){
		if($(this).hasClass("error")){
			return;
		}
		var val  = $(this).val();
		var oldName = $p.find("input[cname="+name+"]").val();
		$p.find("input[cname="+name+"]").val(val);
		if(val!=oldName){
			$p.find("input[cname="+name+"]").attr("name",$p.find("input[cname="+name+"]").attr("rname"));
		}
		var  $td = $(this).parent();
		$td.html("<div>"+val+"</div>");
	});
	 
	 $(obj).find("input[name="+name+"]").dblclick(function(event){
		var $pTd = $(this).parents("td:first");
		tdDblclick(event,$pTd,$p);
	});
	
}

function callBackFormat(obj){
	var $parent = getCurrentPanel();
	var $selTr = $parent.find("tr.selected");
	if(obj){
		$selTr = $(obj);
	}
	var format  = $.trim($selTr.find("td").eq(1).text());

	$.pdialog.close($parent);
	$parent = getCurrentPanel();
	$selTr = $parent.find("tr.selected");
	var $td = $selTr.find("td").eq(1);
	var name = $td.attr("cname")||"format";

	$selTr.find("input[cname='"+name+"']").val(format);
	$td.text(format);
	
}
//选中事件
function selectTrClick(){
	var $parent = getCurrentPanel();
	var $tr=$parent.find("tr").filter(".selected");
	var url=$tr.attr("url");
	var nodeType1 =$tr.attr("nodeType1");
	if(url ||( nodeType1 && nodeType1==2)){
		alertMsg.error("不能选择一类基础资料。");
	}else{
		$tr.dblclick();
	}
}
//选中事件
function selectTrdbClick(){
	var $parent = getCurrentPanel();
	var $tr=$parent.find("tr").filter(".selected");
	var nodeType1 =$tr.attr("nodeType1");
	if( nodeType1 && nodeType1==2){
		alertMsg.error("不能选择一类基础资料。");
	}else{
		$tr.dblclick();
	}
	
}
//选中一类事件
function selectTrSortClick(){
	var $parent = getCurrentPanel();
	var $tr=$parent.find("tr").filter(".selected");
	var result=$tr.attr("result");
	result = eval("("+result+")");
	if(result){
		$.bringBack(result);
	}else{
		alertMsg.error("不能选择一类基础资料。");
	}
}
/*----------------------------------------------end列配置----------------------------------------------------------*/




/*------------------------------------------------公共方法---------------------------------------------------------*/
//得到当前操作行
function getTableCurrentTr(){
	var $parent = navTab.getCurrentPanel();
	var billType = $parent.find("#billType").val();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(billType){
		selectTd = $parent.find("tbody[type='"+billType+"'] tr td").filter(".selected")
	}
	
	if(selectTd.length==0){
		selectTd=this;
	}
	var selectTr =  selectTd.parent();
	return selectTr;
}

//单据   商品编号离开    删除或清空
function objDelOrClear(obj,cname){
	var billType=$("#whichCallBack",navTab.getCurrentPanel()).val();
	var $trParent = $(obj).parents("tr:first");
	var param=$(obj).parent().parent().attr("param");
	var $tbodyParent=$trParent.parent();
	if(cname&&cname=="accountsCode"){                //会计科目编号
		var code=$(obj).val();
		var oldCode=$($trParent).find("input[cname='oldAccountsCode']").val();
		billAccountsBlur($trParent,obj,billType,oldCode,code);
	}else if(cname&&cname=="accountsFullName"){     //会计科目全名
		var code=$(obj).val();
		var oldCode=$($trParent).find("input[cname='oldAccountsFullName']").val();
		billAccountsBlur($trParent,obj,billType,oldCode,code);
	}else if(cname&&cname=="code"){                 //商品编号
		var code=$(obj).val();
		var oldCode=$($trParent).find("input[cname='oldCode']").val();
		billProductBlur($trParent,obj,billType,oldCode,code);
	}else if(cname&&cname=="fullName"){             //商品全名
		var fullName=$(obj).val();
		var oldFullName=$($trParent).find("input[cname='oldFullName']").val();
		billProductBlur($trParent,obj,billType,oldFullName,fullName);
	}else if(cname&&cname=="storageCode"){          //仓库编号
		var storageCode=$(obj).val();
		var oldStorageCode=$($trParent).find("input[cname='oldStorageCode']").val();
		billStorageBlur($trParent,obj,oldStorageCode,storageCode);
	}else if(cname&&cname=="storageFullName"){      //仓库全名
		var storagefullName=$(obj).val();
		var oldStorageFullName=$($trParent).find("input[cname='oldStorageFullName']").val();
		billStorageBlur($trParent,obj,oldStorageFullName,storagefullName);
	}else if(cname&&cname==param+"StorageCode"){          //出入库编号
		var storageCode=$(obj).val();
		var oldStorageCode=$($trParent).find("input[cname='old"+cname+"']").val();
		billStorageBlur($trParent,obj,oldStorageCode,storageCode);
	}else if(cname&&cname==param+"StorageFullName"){      //出入库全名
		var storagefullName=$(obj).val();
		var oldStorageFullName=$($trParent).find("input[cname='old"+cname+"']").val();
		billStorageBlur($trParent,obj,oldStorageFullName,storagefullName);
	}
	aioBillOnblurRemoveWidget(obj);               //单据自动生成控件离开事件     去掉控件 
}
//单据商品编号  全名   离开事件
function billProductBlur($trParent,obj,billType,oldValue,value){
	if(oldValue&&oldValue!=""&&(value==""||value==undefined)){//清空当前行
		billClearTrData($trParent);//单据清除行数据
		var $parent = navTab.getCurrentPanel();
		if(billType=="sellBook"||billType=="bought"){ //销售订单   进货订单
			sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
		}else if(billType=="sell"){ //销售单
			sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			privilegeMoneyTotal();
		}else if(billType=="purchase"){ //进货单
			
			sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			privilegeMoneyTotal();
		}else if(billType=="takeStockBill"){ //库存盘点单
			sellbookOrboughgTotal($parent);// (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			sckAmountTotal($parent);//table合计库存总数量
		}else if(billType=="sellBarter" || "purchaseBarter" == billType){//换货单
			//sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			var $billType  = $parent.find("input#billType").val();
			var $tbodyDiv = $parent.find("tbody[type='"+$billType+"']").parents("div.gridTbody2:first");
			var $tfootDiv = $tbodyDiv.next("div.gridScroller");
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
			gapMoneyCalculate($parent,billType);//算出换货差额
			privilegeMoneyDarter();//优惠金额
		}else if("dismountBill" == billType){//拆装单
			var $billType  = $parent.find("input#billType").val();
			var $tbodyDiv = $parent.find("tbody[type='"+$billType+"']").parents("div.gridTbody2:first");
			var $tfootDiv = $tbodyDiv.next("div.gridScroller");
			moreTableTotal($tbodyDiv,$tfootDiv);//(总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			var amounts  = $tfootDiv.find("#amounts").val();
			var moneys  = $tfootDiv.find("#moneys").val();
			if($billType=="in"){
				$parent.find("#inAmount").val(amounts);
				$parent.find("#inMoney").val(moneys);
			}else{
				$parent.find("#outAmount").val(amounts);
				$parent.find("#outMoney").val(moneys);
			}
		}else if("adjustCost" == billType){//成本调价单
			sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			
			lastMoneyTotal($parent);//合计调整后金额
		}else{
			sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			privilegeMoneyTotal();
		}
	}else if((!oldValue||oldValue=="")&&value!=""){  //无商品，写编号就清空
		$(obj).val("");
	}else if(oldValue&&oldValue!=""&&value!=""){  //有商品   写编号还愿
		$(obj).val(oldValue);
	}
}


//单据仓库编号  全名   离开事件
function billStorageBlur($trParent,obj,oldValue,value){
	if(oldValue!=""&&value==""){//仓库相关信息  （storageId storageCode oldStorageCode storageFullName oldStorageFullName）
		var td = $(obj).parents("td:first");
		var param = td.attr("param");
		if(param){
			$trParent.find("input[cname='"+param+"StorageSupId']").val(""); 
			$trParent.find("input[cname='"+param+"StorageId']").val(""); 
			$trParent.find("input[cname='"+param+"StorageCode']").val(""); 
			$trParent.find("input[cname='old"+param+"StorageCode']").val(""); 
			$trParent.find("input[cname='"+param+"StorageFullName']").val(""); 
			$trParent.find("input[cname='old"+param+"StorageFullName']").val("");
	        
			$trParent.find("td[cname='"+param+"StorageCode']").children("div").text("");
			$trParent.find("td[cname='"+param+"StorageFullName']").children("div").text("");
		}else{
			$trParent.find("input[cname='storageSupId']").val(""); 
			$trParent.find("input[cname='storageId']").val(""); 
			$trParent.find("input[cname='storageCode']").val(""); 
			$trParent.find("input[cname='oldStorageCode']").val(""); 
			$trParent.find("input[cname='storageFullName']").val(""); 
			$trParent.find("input[cname='oldStorageFullName']").val("");
	        
			$trParent.find("td[cname='storageCode']").children("div").text("");
			$trParent.find("td[cname='storageFullName']").children("div").text("");
		}
	}else if((!oldValue||oldValue=="")&&value!=""){
		$(obj).parent().text("");
	}else if(oldValue!=""&&value!=""){  
		$(obj).parent().text(oldValue);
	}
}

//单据会计科目编号  全名   离开事件
function billAccountsBlur($trParent,obj,billType,oldValue,value){
	var $parent = navTab.getCurrentPanel();
	if(oldValue!=""&&value==""){//仓库相关信息  （storageId storageCode oldStorageCode storageFullName oldStorageFullName）
		$trParent.find("input").remove();
		showCurrentTrWidgetToSpan($trParent);//让tr里面所有td中的input值在中显示出来
		clearTrWidgetSpanVal($trParent);
		if(billType&&billType=="getMoney"){
			getMoneyTrChange($trParent);//行变化
		}else{
			getMoneyTrChange($trParent);//行变化
		}
		
	}else if((!oldValue||oldValue=="")&&value!=""){
		$(obj).parent().text("");
	}else if(oldValue!=""&&value!=""){  
		$(obj).parent().text(oldValue);
	}
	moneysTotal($parent);
	if("otherEarn" == billType){
		var $moneys = $parent.find("#moneys");
		if($moneys){
			$parent.find("#privilegeMoney").val($moneys.val());
		}
	}
}





//表格自动添加一行
function addTr(id,obj){
	var $parent = navTab.getCurrentPanel();
	if(obj){
		$parent=obj.parents("div.grid:first");
	}
	var lastTr=$("#"+id,$parent).find("tr:last");
	var html="<tr>"+$(lastTr).html()+"</tr>";
	var index = 1;
	var text = lastTr.find("td:first").text();
	
	if(""!=text && $.isNumeric($.trim(text))){
		index=parseInt($.trim(text))+1;
	}
	
	lastTr.after(html);
	
	var newTr = $("#"+id,$parent).find("tr:last");
	newTr.children().removeClass("selected");
	billClearTrData($(newTr));//单据清除行数据 及隐藏域
	newTr.find("td:first").html("<div>"+index+"</div>");
	newTr.attr("class","");
	//绑定事件 
	var $grid = newTr.parents("div.grid");
	var tbody =newTr.parents("tbody");
	trsBindEvent(newTr, "","order","",$grid,tbody,$parent,false)
}
//收款单表格自动添加一行   下面一个表格
function addTr2(id){
	var nav = navTab.getCurrentPanel();
	var basePathId=$("#basePathId",nav).val();
	var $parent = nav.find("div.grid:eq(1)");
	if(!$parent){
		return;
	}
	var lastTr=$("#"+id,$parent).find("tr:last");
	var html="<tr target='tid' rel='' title='' url='"+basePathId+"/common/bill/lookBill/' cTarget='openNavTab'>"+$(lastTr).html()+"</tr>";
	lastTr.after(html);
	
	var newTr = $("#"+id,$parent).find("tr:last");
	newTr.children().removeClass("selected");
	clearTrWidgetSpanVal($(newTr),"all");
	//绑定事件 
	var tbody =newTr.parents("tbody");
	trsBindEvent(newTr, "","order","",$parent,tbody,nav,false);
}

//收款单表格自动添加一行   下面一个表格
function addTr3(areaId){
	var $parent = $("#"+areaId,getCurrentPanel());
	if(!$parent){
		return;
	}
	var lastTr=$parent.find("tr:last");
	var html="<tr>"+$(lastTr).html()+"</tr>";
	lastTr.after(html);
	
	var newTr = $parent.find("tr:last");
	newTr.children().removeClass("selected");
	clearTrWidgetSpanVal($(newTr),"all");
	
	//绑定事件 
	var tbody =newTr.parents("tbody");
	var trs=$parent.find("tr");
	trsBindEvent(trs, "",undefined,"",$parent,tbody,$parent,fasle);
}
//删除tr
function removeTrChangeAttr($tr){
	var $allTr  = $tr.nextAll();
	var index = parseInt($.trim($tr.find("td:first").text()));
	var $siblingTrs = $tr.siblings();
	if($siblingTrs.length==0){
		return false;
	}
	$allTr.each(function(){
		var $this = $(this);
		var tIndex = parseInt($.trim($this.find("td:first").text()));
		var newIndex  = tIndex-1;
		if(index<tIndex){
			$this.find("td:first").html("<div>"+newIndex+"</div>");
			var seq = newIndex-1;
			$this.find("input").each(function(){
				var $input = $(this);
				var name = $input.attr("name");
				if(name){
					var newName = name.replace("["+newIndex+"]","["+seq+"]");
					$input.attr("name",newName);
				}
				
			});
		}
	});
	
	
	//var $grid = navTab.getCurrentPanel();
	var $grid = $tr.parents("div.grid:first");
	$tr.remove();
	var thead = $grid.find("thead");
	var tbody = $grid.find("tbody");
	var tfoot = $grid.find("tfoot");
	var ftr = $grid.find("tbody").children();
	var ttr = $grid.find("tfoot").children();
	var options = "";
	var $Td  = $tr.children();
	var firstTrTd =  tbody.find("tr:first").children();
	for(var i=0;i<$Td.length;i++){
	  $(firstTrTd[i]).width($($Td[i]).width());
    }
	
	
	var lastH = $(">tr:last-child", thead);
	var thAllIndex = lastH.index();
	
	
	theadDrag(thAllIndex,$grid,thead,tbody,tfoot,ftr,ttr,options);
	return true;
}


//报溢单据录入成本单价
function billInputCostPrice(json){
	DWZ.ajaxDone(json);
	if (json.statusCode == DWZ.statusCode.ok){
		$.pdialog.closeCurrent();
		
		if(json.trIndex){
			var price=round(json.proPrice,4);
			var trIndex=json.trIndex-1;
			var proId=json.proId;
			var unitId=json.proUnitId;
			var $parent = getCurrentPanel();
			var selectTr = $("tbody tr:eq("+trIndex+")",$parent);
//			if(json.type){
//				selectTr = $("tbody[type="+json.type+"] tr:eq("+trIndex+")",$parent);
//			}
			
			var amount=selectTr.find("input[cname='amount']").val();//数量
			var baseAmount=selectTr.find("input[cname='baseAmount']").val();//基本数量
			var money = round(price*amount,4);
			
			selectTr.find("input[cname='price']").val(price);
			selectTr.find("input[cname='basePrice']").val(round(money / baseAmount,4));
			selectTr.find("input[cname='costPrice']").val(round(money / baseAmount,4));
			selectTr.find("input[cname='money']").val(money);
			
			
			showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
			amountsTotal($parent);//table合计总数量
			moneysTotal($parent);//table合计总金额
			
			//提交from
			$("form:first",$parent).submit();
		}
	}else {
		alertMsg.error("异常！");
	}
}
/*----------------------------------------------end--公共方法------------------------------------------------------*/
