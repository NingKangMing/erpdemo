//做单据相关函数
//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*-------------------------------------------单据(商品)(会计科目)回调-----------------------------------------------*/
/**
 * 单据商品带回回调    eg:销售单选择商品双击商品回调 
 * @param res
 * @return
 */
function  billCallBack(res){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(res);
	var json=res["list"];
	var whichCallBack=res["whichCallBack"];
	if(!whichCallBack || whichCallBack=="undefined"){
		var $parent = getCurrentPanel();
		whichCallBack = $("#whichCallBack",$parent).val();
	}
	billProductCallBack(whichCallBack,json);
	//选中下个可输入的td
	var $parent = getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTds = selectTd.nextAll();
	for ( var i = 0; i < selectTds.length; i++) {
		var $td = $(selectTds[i]);
		var cname = $td.attr("cname");
		var readonly = $td.attr("readonly");
		if(readonly!="readonly" && cname && cname!="code" && cname!="fullName" ){
			selectTd.removeClass("selected");
			$td.addClass("selected");
			bindTableEvent();
			break;
		}
	}
}
/**
 *  商品带回 - 具体单据回调函数
 */
function billProductCallBack(whichCallBack,json,$this){
	if(whichCallBack=="sellBook"||whichCallBack=="bought"){                  //销售订单，进货订单
		billCallBackNotDialog(json);
	}else if(whichCallBack=="sell" || whichCallBack=="purchaseReturn"|| whichCallBack=="parityAllot"|| whichCallBack=="difftAllotBill"){      //销售单  进货退货   同价调拨单   变价调拨单
		xsdBillCallBackNotDialog(json);
	}else if(whichCallBack=="purchase"||whichCallBack=="sellReturn"){        //进货退货单   销售退货单
		putBillCallBackNotDialog(json);                                      
	}else if(whichCallBack=="sellBarter" || whichCallBack=="purchaseBarter"){//进货换货单    销售换货单
		barterBillCallBackNotDialog(json);   
	}else if(whichCallBack=="takeStockBill"){                                //库存盘点单
		takeStockBillCallBack(json);
	}else if(whichCallBack=="breakageBill"){ 				                 //报损单
		breakageBillCallBack(json);                                          
	}else if(whichCallBack=="overflowBill"){ 				                 //报溢单
		overflowBillCallBack(json);                                          
	}else if(whichCallBack=="stockOtherin"){                                 //其它入库单
		putOtherBillCallBackNotDialog(json);                                                                  
	}else if(whichCallBack=="stockOtherout"){                                //其它出库单
		qtckdBillCallBackNotDialog(json);
	}else if(whichCallBack=="dismountBill"){	                             //拆装单
		dismountBillCallBackNotDialog(json);
	}else if(whichCallBack=="produceTemplate"){	                             //生产模板
		produceTemplateCallBackNotDialog(json);
	}else if(whichCallBack=="adjustCost"){
		adjustCostCallBackNotDialog(json);                                   //成本调价单
	}else if(whichCallBack=="商品组装带回"){
		//这样两个加  所有商品带回只有一个入口
	}else if(whichCallBack=="商品拆分带回"){
		//这样两个加  所有商品带回只有一个入口
	}else{
		alert("请在billCallBack写上你的回调函数！");
	}
}
/**
 * 单据仓库带回回调   
 * @param res
 * @return
 */
function storageCheckBack(res){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(res);
	var storage=res["storage"];
	var whichCallBack=res["whichCallBack"];
	var caseVal = res["case"]||"";//多仓库用于区分前缀参数
	billStorageCheckBack(whichCallBack,storage,caseVal);
	
	//选中下个可输入的td
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTds = selectTd.nextAll();
	for ( var i = 0; i < selectTds.length; i++) {
		var $td = $(selectTds[i]);
		var cname = $td.attr("cname");
		var readonly = $td.attr("readonly");
		if(readonly!="readonly" && cname && cname!="storageCode" && cname!="storageFullName" ){
			selectTd.removeClass("selected");
			$td.addClass("selected");
			bindTableEvent();
			break;
		}
	}
}
/**
 * 仓库带回 - 具体单据回调函数
 * @return
 */
function billStorageCheckBack(whichCallBack,storage,caseVal){
	if(whichCallBack=="sell" || whichCallBack == "purchase"){  //销售单
		storageCallBackNotDialog(storage);
	}else if(whichCallBack=="sellReutrn"){   //销售退货单
		storageCallBackNotDialog(storage);
	}else if (whichCallBack=="breakageBill") {//报损单  改变仓库
		storageCallBackNotDialog(storage);//仓库赋值
		changeStorageSelectPro();//手工指定法商品更换仓库重新选择该仓库的批次商品
		paramCallBackPrcie();//根据仓库改变单价
	}else if (whichCallBack=="overflowBill") {//报溢单  改变仓库
		storageCallBackNotDialog(storage);//仓库赋值
		paramCallBackOverflowPrcie();//根据仓库改变单价
	}else if(whichCallBack=="stockOtherin"){   //其它入库单
		storageCallBackNotDialog(storage);
		paramCallBackOtherInPrcie();//触发价格变动
	}else if(whichCallBack=="stockOtherout"){  //其它出库单
		storageCallBackNotDialog(storage);
		paramCallBackOtherInPrcie();//触发价格变动
	}else if(whichCallBack=="parityAllot" || whichCallBack=="difftAllotBill"){//同价调拨||变价调拨
		storageCaseCallBackNotDialog(storage,caseVal);
	}
	else{
		storageCallBackNotDialog(storage);
	}
}
/**
 * 单据会计科目带回
 * @param res
 * @return
 */
function accountsCheckBack(res){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(res);
	var json=res["list"];
	var sysAccounts = res["sysAccounts"];
	var whichCallBack=res["whichCallBack"];
	billAccountsCheckBack(whichCallBack,json,sysAccounts);
}
/**
 * 会计科目带回 - 具体单据回调函数
 */
function billAccountsCheckBack(whichCallBack,json,sysAccounts){
	if(whichCallBack=="getMoney" || whichCallBack=="payMoney" ){   //收款单   //付款单
		skdBillCallBack(json);
	}else if(whichCallBack=="otherEarn"){
		skdBillCallBack(json);
	}else if (whichCallBack=="transfer") {
		skdBillCallBack(json); 
	}else if(whichCallBack=="feeBill"){               //费用单
		skdBillCallBack(json);
	}else if(whichCallBack=="cwunitGetDown"){         //调账-单位应收减少
		skdBillCallBack(json);
	}else if(whichCallBack=="cwunitGetUp"){           //调账-单位应收增加 
		skdBillCallBack(json);
	}else if(whichCallBack=="cwunitPayDown"){         //调账-单位应付减少
		skdBillCallBack(json);
	}else if(whichCallBack=="cwunitPayUp"){           //调账-单位应付增加
		skdBillCallBack(json);
	}else if(whichCallBack=="cwmoneyDown"){           //调账-资金减少
		skdBillCallBack(json);
	}else if(whichCallBack=="cwmoneyUp"){             //调账-资金增加
		skdBillCallBack(json);
	}else if (whichCallBack=="assets") {			  //固定资产
		skdBillCallBack(json);
	}else if(whichCallBack=="accountVoucher"){           //会计凭证
		sysAccountsCallBack(json,sysAccounts);
	}else if(whichCallBack=="singleAccounts"){         //单据右下角选择会计科目
		var $parent=getCurrentPanel();
		$("#accountId",$parent).val(json[0].id);
		$("#accountFullName",$parent).val(json[0].fullName);
	}else{
		skdBillCallBack(json);
		alert("多个会计科目带回，一个带回 写上自己的提示，方便开发定位问题");
	}
}
/*----------------------------------------end---单据(商品)(会计科目)回调--------------------------------------------*/




/*-------------------------------------------弹出 (辅助单位，数量，价格)-----------------------------------------------*/
/**
 * 弹出辅助单位    回调(从界面拿值)
 */
function baseUnitDialog(){
	var tbody =$("#calculate_unit_id",$.pdialog.getCurrent());
	var tr0=$(tbody).find(">tr:eq(0)");
	var tr1=$(tbody).find(">tr:eq(1)");
	var tr2=$(tbody).find(">tr:eq(2)");
	var calculateUnit1=$(tr0).find(">td:eq(1)").find(">div").text();  //单位名称
	var calculateUnit2=$(tr1).find(">td:eq(1)").find(">div").text();
	var calculateUnit3=$(tr2).find(">td:eq(1)").find(">div").text();
	var unitRelation1=$("input[name='unitRelation1']",tbody).val();   //单位关系
	var unitRelation2=$("input[name='unitRelation2']",tbody).val();
	var unitRelation3=$("input[name='unitRelation3']",tbody).val();
	var retailPrice1=$("input[name='retailPrice1']",tbody).val();     // 零售价
	var retailPrice2=$("input[name='retailPrice2']",tbody).val();  
	var retailPrice3=$("input[name='retailPrice3']",tbody).val();  
    var tr=$(tbody).find(">tr").filter(".selected");
    var secondTd=$(tr).find(">td").last();
    var val=$(secondTd).find(">div").text();
    if(!val||val==""){
    	alertMsg.info("所选的商品辅助单位不存在或单位关系为0，请重新选择!");
    	return true;
    }
    var basePathId=$("#basePathId",tbody).val();
    var productId=$("#productId",tbody).val();
    var unitId=$("#unitId",tbody).val();
    var billType=$("#billType",tbody).val();
    var valId=$(tr).attr("trIndex");
    $.pdialog.closeCurrent();  //关闭当前Dialog
    
    
    
    var panel=navTab.getCurrentPanel();
	var whichCallBack=$("#whichCallBack",panel).val();
    if(whichCallBack=="sellBook"||whichCallBack=="bought"){       //销售订单，进货订单
    	//商品价格折扣跟踪
    	var currentTd=productPriceDiscountTrack(basePathId,billType,unitId,productId,valId);
    	//触发价格变化函数
    	amountPriceDiscountChange($(currentTd),false);
    	sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="takeStockBill"){                     //库存盘点单
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="sell"){                              //销售单
		//商品价格折扣跟踪
    	var currentTd=productPriceDiscountTrack(basePathId,billType,unitId,productId,valId);
    	//触发价格变化函数
    	amountPriceDiscountChangePrivilege($(currentTd),false);
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="purchase" || whichCallBack=="purchaseReturn"){                          //进货单
		//商品价格折扣跟踪
    	var currentTd=productPriceDiscountTrack(basePathId,billType,unitId,productId,valId);
    	//触发价格变化函数
    	amountPriceDiscountChangePrivilege($(currentTd),false);
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="sellReturn"){                        //销售退货
		//商品价格折扣跟踪
    	var currentTd=productPriceDiscountTrack(basePathId,billType,unitId,productId,valId);
    	//触发价格变化函数
    	amountPriceDiscountChangePrivilege($(currentTd),false);
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="sellBarter" || whichCallBack=="purchaseBarter"){                        //销售换货
		//商品价格折扣跟踪
    	var currentTd=productPriceDiscountTrack(basePathId,billType,unitId,productId,valId);
    	//触发价格变化函数
    	amountPriceDiscountChangePrivilege($(currentTd),false);
		//sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
    	moreTbaleBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}else if(whichCallBack=="breakageBill"){                      //报损单
		unitCallBack(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
		paramCallBackPrcie();//触发价格变动
	}else if(whichCallBack=="overflowBill"){                      //报溢单
		unitCallBack(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
		paramCallBackOverflowPrcie();//触发价格变动
	}else if(whichCallBack=="stockOtherin"){                        //其它入库单
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
		paramCallBackOtherInPrcie();//触发价格变动
	}else if(whichCallBack=="stockOtherout"){                       //其它出库单
		var currentTr=getTableCurrentTr();//当前Tr
		var odlSelectUnitId = currentTr.find("input[cname='selectUnitId']").val();
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3,"stockOtherout");
		paramCallBackCostPrcie(unitRelation1,unitRelation2,unitRelation3,odlSelectUnitId);//触发价格变动
	}/*else if(whichCallBack=="sellBarter" ||　whichCallBack=="purchaseBarter"){ //换货单
		moreTbaleBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}*/else if(whichCallBack=="parityAllot"){//同价调拨
		var currentTr=getTableCurrentTr();//当前Tr
		var odlSelectUnitId = currentTr.find("input[cname='selectUnitId']").val();
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
		paramCallBackCostPrcie(unitRelation1,unitRelation2,unitRelation3,odlSelectUnitId);//触发价格变动
	}else if(whichCallBack=="dismountBill"){// 拆装单
		var currentTr=getTableCurrentTr();//当前Tr
		var odlSelectUnitId = currentTr.find("input[cname='selectUnitId']").val();
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
		paramCallBackCostPrcie(unitRelation1,unitRelation2,unitRelation3,odlSelectUnitId);//触发价格变动
		var grid = currentTr.parents("div.grid:first");
		var amounts  = grid.find("#amounts").val();
		var moneys  = grid.find("#moneys").val();
		
		if(panel.find("#billType").val()=="in"){
			panel.find("#inAmount").val(amounts);
			panel.find("#inMoney").val(moneys);
		}else{
			panel.find("#outAmount").val(amounts);
			panel.find("#outMoney").val(moneys);
		}
		
	}
	else{
		sellbookBaseUnit(valId,val,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3,retailPrice1,retailPrice2,retailPrice3);
	}
}
/**
 * 弹出数量  
 */
function inputAmountDialog(){
	var tbody =$("#record_amount_id");  ////弹出数量 tbody
    var trs=$(tbody).find(">tr");  //弹出数量 trs
    //单位名称  eg 1.支    2.包      3.箱
    var calculateUnit1;
    var calculateUnit2;
    var calculateUnit3;
    //数量
    var amount1;
    var amount2;
    var amount3;
    //单位换算关系
    var unitRelation1;
    var unitRelation2;
    var unitRelation3;
    trs.each(function(i){
        var $this=$(this);
        if(i==0){
          calculateUnit1=$this.find(">td:eq(1)").find("div").text();
          amount1=$this.find(">td:eq(2)").find("input").val();
          unitRelation1=$this.find(">td:eq(3)").find("div").text();
        }else if (i==1) {
          calculateUnit2=$this.find(">td:eq(1)").find("div").text();
          amount2=$this.find(">td:eq(2)").find("input").val();
          unitRelation2=$this.find(">td:eq(3)").find("div").text();
        }else if(i==2){
          calculateUnit3=$this.find(">td:eq(1)").find("div").text();
          amount3=$this.find(">td:eq(2)").find("input").val();
          unitRelation3=$this.find(">td:eq(3)").find("div").text();
        }
    });
    $.pdialog.closeCurrent();  //关系当前Dialog
    var panel=navTab.getCurrentPanel();
    var _td=$(panel).find("td").filter(".selected");//单据td
    var currentTr=$(_td).parent();
	
    var amount=0;
    //数量 
    if(amount1&&amount1!=""&&unitRelation1!=""){
    	amount+=unitRelation1*amount1;
    }
    if(amount2&&amount2!=""&&unitRelation2!=""){
		amount+=unitRelation2*amount2;
	}
    if(amount3&&amount3!=""&&unitRelation3!=""){
		amount+=unitRelation3*amount3;
	}

    
    var panel=navTab.getCurrentPanel();
    var _td=$(panel).find("td").filter(".selected");//单据td
    var currentTr=$(_td).parent();
    var selectUnitId=currentTr.find("input[cname='selectUnitId']").val();
    if(selectUnitId==1){
    	amount=amount;
    }else if(selectUnitId==2){
    	if(unitRelation2&&unitRelation2!=""){
    		amount=amount/unitRelation2;
    	}else{
    		amount=0;
    	}
    }else if(selectUnitId==3){
    	if(unitRelation3&&unitRelation3!=""){
    		amount=amount/unitRelation3;
    	}else{
    		amount=0;
    	}
    }
    
    amount = round(amount,4);
	var whichCallBack=$("#whichCallBack",panel).val();
    if(whichCallBack=="sellBook"||whichCallBack=="bought"){         //销售订单，进货订单
    	sellbookInputAmount(amount);
    	amountPriceDiscountChange($(_td),false);
	}else if(whichCallBack=="takeStockBill"){                       //库存盘点单
		takeStockInputAmount(amount);
	}else if(whichCallBack=="sell" || whichCallBack=="purchase"){   //销售单
		sellbookInputAmount(amount);
		amountPriceDiscountChangePrivilege($(_td),false);
	}else if(whichCallBack=="sellReturn" || whichCallBack=="purchaseReturn"){ //销售退货 ，进货退货
		sellbookInputAmount(amount);
		amountPriceDiscountChangePrivilege($(_td),false);
	}else if(whichCallBack=="productInit"){                         //期初数据-》期初商品库存修改
		productInitAmount(amount);
	}else if (whichCallBack=="breakageBill" || whichCallBack=="overflowBill") {	//报损单，报溢单
		breakageBillAmount(amount);
		paramCallBackPrcie();//触发报损单改变
	}else if(whichCallBack=="stockOtherin"){                         //其它入库单
		sellbookInputAmount(amount);
		amountPriceDiscountChangePrivilege($(_td),false);
	}else if(whichCallBack=="stockOtherout"){                        //其它出库单
		sellbookInputAmount(amount);
		amountPriceDiscountChangePrivilege($(_td),false);
	}else if(whichCallBack=="sellBarter"){                           //销售换货
		moreTableInputAmount(amount);
	}else if(whichCallBack=="parityAllot"){                          //同价调拨单
		sellbookInputAmount(amount);
		amountPriceDiscountChange($(_td),false);
	}else if(whichCallBack=="dismountBill"){                           //拆装单
		moreTableInputAmount(amount);
	}else{
		alert("请在inputAmountDialog写上你的回调函数！");
	}
	
}
/**
 * 弹出价格
 * @param obj
 * @return
 */
function inputPriceDialog(obj){
	var $parent = getCurrentPanel();
	var $selTr = $parent.find("tr.selected");
	if(obj){
		$selTr = $(obj);
	}
	var price  =  $selTr.find("input[name='price']:first").val();
	$.pdialog.close($parent);
	
	$parent = getCurrentPanel();
	var $billType  = $parent.find("input#billType").val();
	var $selTd = $parent.find("td.selected");
	var currentTr = $selTd.parents("tr:first");
	if($billType){
		
		$selTd = $parent.find("tbody[type='"+$billType+"']").find("td.selected");
		currentTr = $selTd.parents("tr:first");
	}
	if($.isNumeric(price)){
		currentTr.find("input[cname='price']").val(price);//单价
		currentTr.find("td[cname='price']").children("div").text(price);
	}else{
		currentTr.find("input[cname='price']").val("");
		currentTr.find("td[cname='price']").children("div").text("");
	}
	var whichCallBack=$("#whichCallBack",$parent).val();
	if("bought" == whichCallBack){
		amountPriceDiscountChange(currentTr,false);
	}else if("purchase" == whichCallBack || "purchaseReturn" == whichCallBack){
		amountPriceDiscountChangePrivilege(currentTr,false);
	}else if("sellBarter" == whichCallBack || whichCallBack=="purchaseBarter"){
		amountPriceDiscountChange(currentTr,false);
	}else if("sellBook" == whichCallBack){
		amountPriceDiscountChange(currentTr,false);
	}else if("sell" == whichCallBack||"sellReturn" == whichCallBack){
		amountPriceDiscountChangePrivilege(currentTr,false);
	}else{
		alert(" inputPriceDialog 写上你的回调函数");
	}
	
}
/**
 * 数量  转成  5箱3包2支
 */
function helpAmountStr(amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3){
    var helpAmount="";  //辅助数量
    if(amount<=0){
    	return "";
    }
    if(!isNaN(unitRelation3)&&unitRelation3>0){
		if(round(amount/unitRelation3>=1)){
			helpAmount+= round(parseInt(amount/unitRelation3),4)+calculateUnit3;
			amount=numberModel(amount,unitRelation3);
		}
    }
	if(!isNaN(unitRelation2)&&unitRelation2>0){
		if(round(amount/unitRelation2>=1)){
			helpAmount+= round(parseInt(amount/unitRelation2),4)+calculateUnit2;
			amount=numberModel(amount,unitRelation2);
		}
    }
	if(!isNaN(unitRelation1)&&unitRelation1>0){
		if(amount>0){
			helpAmount+=round(amount,4)+calculateUnit1;
		}
    }
   return helpAmount;
}
/*--------------------------------------end-----(辅助单位，数量，价格)-----------------------------------------------*/








/*-------------------------------------------stable相关函数函数-----------------------------------------------*/
/**
 * 删除表格一行  回调各自单据函数
 */
function removeTr(obj){
	var whichCallBack=$("#whichCallBack",getCurrentPanel()).val();
	var $tr=$(obj).parents("tr:first");
	var productId=$tr.find("input[cname='productId']").val(); //当前行有没有商品ID
	
	var $parentDialog = $(obj).parents("div.dialog:first");//当前dialog面板
	var $tbody = $tr.parents("tbody:first");
	removeTrChangeAttr($tr);//删除tr
	if(productId&&productId!=""){
		var $parent = navTab.getCurrentPanel();
	    if($tbody.attr("type")){
	    	$parent.find("input#billType").val($tbody.attr("type"));
	    }
	    
		if(whichCallBack=="sellBook"||whichCallBack=="bought"){//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
			sellbookOrboughgTotal($parent);
		}else if(whichCallBack=="takeStockBill"){//库存盘点单   (总数量,库存总数量,库存总金额,亏盈总数量,亏盈总金额,零售总金额)统计
			takeStockBillTotal($parent);
		}else if ("purchase" == whichCallBack||"sell"==whichCallBack || "purchaseReturn" == whichCallBack|| "sellReturn" == whichCallBack){
			purchaseOrSellTotal($parent);
		}else if("stockOtherin" == whichCallBack||"stockOtherout"==whichCallBack){   
			purchaseOrSellTotal($parent);
		}else if ("breakageBill" == whichCallBack || "overflowBill"==whichCallBack || "parityAllot"==whichCallBack){
			amountsTotal($parent);//table合计总数量
			moneysTotal($parent);//table合计总金额
			retailMoneysTotal($parent);//合计零售金额
		}else if("sellBarter" == whichCallBack || "purchaseBarter"==whichCallBack || "dismountBill"==whichCallBack){
			  var $billType  = $parent.find("input#billType").val();
			  var $gridDiv = $parent.find("tbody[type='"+$billType+"']").parents("div.grid:first");
			  sellbookOrboughgTotal($gridDiv);//合计其他字段
			  retailMoneysTotal($gridDiv);//合计零售金额
			  var amounts  = $gridDiv.find("#amounts").val();
			  var moneys  = $gridDiv.find("#moneys").val();
			  if($billType=="in"){
					$parent.find("#inAmount").val(amounts);
					$parent.find("#inMoney").val(moneys);
				}else{
					$parent.find("#outAmount").val(amounts);
					$parent.find("#outMoney").val(moneys);
			  }
			  gapMoneyCalculate($parent);//算出换货差额
			  privilegeMoneyDarter();//优惠金额
		}else if("produceTemplate" == whichCallBack){//生产模板
			var $allowAmount = $("#allowAmount",$parentDialog);//可生产数量
			var table = $parentDialog.find("div.gridTbody table");//当前单据表格
			//所有行
			var allTr = table.find("tbody tr");
			getMinAmountByArray(allTr,$parentDialog);//赋值最小生产数量
			
		}else if("adjustCost"==whichCallBack){
			amountsTotal($parent);//table合计总数量
			moneysTotal($parent);//table合计总金额
			lastMoneyTotal($parent);//合计调整后金额
			adjustMoneyTotal($parent);//合计调整金额
		}else{
			alert("请在aio.table.js的removeTr增加自己的回调！");
		}
		return true;
	}
	//会计科目
	var accountsId=$tr.find("input[cname='accountsId']").val(); //当前行有没有会计科目ID
	if(accountsId&&accountsId!=""){
		var $parent = navTab.getCurrentPanel();
		if(whichCallBack=="getMoney"){        //收款单
			getMoneyTrChange(); //行变化
		}else if(whichCallBack=="feeBill"){   //费用单
			feeBillTrChange(); 
		}else if("accountVoucher"==whichCallBack){
			debitMoneyTotal($parent);
			lendMoneyTotal($parent);
		}else {
			feeBillTrChange(); 
		}	
	}
}
/**
 * 清空tr值
 * @param obj
 * @return
 */
function clearTr(obj){
	var $obj = $(obj);
	var $tr = $obj.parents("tr");
	var $tds = $("td",$tr);
	$tds.each(function(i){
		var $td = $(this);
		var inp = $("input",$td);
		if(inp.length==0){
			var cname = $td.attr("cname");
			$("input[cname='"+cname+"']",$tr).val("");
		}
	});
	showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值在中显示出来
}
/**
 * table判断是否有下一行   没有增加一行
 * @param selectTr
 * @param obj
 * @return
 */
function hasAddTr(selectTr,obj){
	var newSelectTr=selectTr.next();
    if(newSelectTr.length<=0||newSelectTr.is(":hidden")){
    	addTr(selectTr.parent().attr("id"),obj);
    }
    selectTr = selectTr.next();
    return selectTr;
}
/**
 * 递归判断下一行是否有商品
 * @param selectTr
 * @return
 */
function filterTr(selectTr){
	if(selectTr.find("td").filter(".selected").length>0){
		return selectTr;
	}else{
		var productId=selectTr.find("input[cname='productId']").val();
		if(productId&&productId!=""&&productId>0){
			selectTr=selectTr.next();
			selectTr=filterTr(selectTr);
			return selectTr;
		}else{
			return selectTr;
		}
	}
}
/**
 * 得到表格当前选中行   (多表格不支持)
 * @return
 */
function aioGetSelectTr(){
	var $Panel=getCurrentPanel();
	var $tbody=$("tbody:first",$Panel);
	var $selectTr=$("tr.selected",$tbody);
	return $selectTr;
}
/*----------------------------------------end---stable相关函数函数-----------------------------------------------*/




/**
 * 手工指定法商品更换仓库重新选择该仓库的批次商品
 */
function changeStorageSelectPro(){
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于手工指定法    弹出窗体url
	var module = $parent.find("#module").val();//区分模块
	var currentTr=getTableCurrentTr();//当前Tr
	var lastTd=currentTr.find("td").last();//最后一个td
	var costArith=currentTr.find("input[cname='costArith']").val();
	var tbodyId=currentTr.parent().attr("id");
	var storageId=currentTr.find("input[cname='storageId']").val();
	var productId=currentTr.find("input[cname='productId']").val();
	var trIndex=currentTr.index();//当前行索引
	if(costArith==4){  //手工指定法
		//清空还原数据
		lastTd.find("input[cname='amount']").val(1);//数量
		lastTd.find("input[cname='baseAmount']").val(1);//实际数量(基本数量)
		var calculateUnit1=lastTd.find("input[cname='calculateUnit1']").val();
		var calculateUnit2=lastTd.find("input[cname='calculateUnit2']").val();
		var calculateUnit3=lastTd.find("input[cname='calculateUnit3']").val();
		var unitRelation1=lastTd.find("input[cname='unitRelation1']").val();
		var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
		var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
		
		lastTd.find("input[cname='basePrice']").val("");//基本单价
		lastTd.find("input[cname='price']").val("");
		lastTd.find("input[cname='money']").val("");
    	lastTd.find("input[cname='selectUnitId']").val(1);//还原单位ID
    	lastTd.find("input[cname='baseUnit']").val(calculateUnit1);//还原单位
    	var helpAmount=helpAmountStr(1,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
    	currentTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量 
    	
    	currentTr.find("td[cname='price']").find("div").html("");
    	currentTr.find("td[cname='money']").find("div").html("");
    	currentTr.find("td[cname='baseUnit']").find("div").html(calculateUnit1);
    	currentTr.find("td[cname='helpAmount']").find("div").html(helpAmount);
		
		amountsTotal($parent);//table合计总数量
		moneysTotal($parent);//table合计总金额
		
		var options = {};
	    options.width="540";
	    options.height="350";
	    options.param={module:module,storageId:storageId,productId:productId,trIndex:trIndex,tbodyId:tbodyId};
	    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "PrdManSelPrd_dialog", "商品库存批次表", options);
	}
}




/*-------------------------------------------商品带回回调（手工指定法）商品-----------------------------------------------*/
/**
 * 选择手工指定法回调        例如：换货单换出商品选择   组拆单   （多仓库）
 * @param trIndexs
 * @param productIds
 * @param trcostAriths
 * @param tbodyId
 * @return
 */
function callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId){
	var $parent = navTab.getCurrentPanel();
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
		if(!storageId) storageId = $parent.find("#outStorageId").val();
		var options = {};
	    options.width="540";
	    options.height="350";
	    options.param={storageId:storageId,productId:productId,trIndex:trIndex,trIndexs:trIndexs,productIds:productIds,trcostAriths:trcostAriths,tbodyId:tbodyId};
	    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "xsd_selPrdManSelPrd_dialog", "商品库存批次表", options);
	}else{
		if(trIndexs.indexOf(",")>0||trIndexs!=""){
			callbackStockWhichProduct(trIndexs,productIds,trcostAriths,tbodyId);
		}
	}
}
/**
 * 选择手工指定法回调       手工指定法弹出批次窗体 其它算法赋值成本单价   eg：报损单
 */
function callbackProToCostPrice(trIndexs,productIds,trcostAriths,tbodyId){
	var $parent = navTab.getCurrentPanel();
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
	
	var selectUnitId=selectTr.find("input[cname='selectUnitId']").val();
	
	var storageId = $parent.find("#storageId").val();
	if(!storageId)storageId = $parent.find("#storageId").val();
	var module = $parent.find("#module").val();//区分模块
	
	if(trcostArith==4){  //手工指定法单价
		var options = {};
	    options.width="540";
	    options.height="350";
	    options.param={module:module,storageId:storageId,productId:productId,trIndex:trIndex,trIndexs:trIndexs,productIds:productIds,trcostAriths:trcostAriths,tbodyId:tbodyId};
	    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "PrdManSelPrd_dialog", "商品库存批次表", options);
	}else{
//		//其它商品ajax请求得到平均成本单价
//		$.ajax({
//		   type: "POST",
//		   url: basePath+"/stock/stock/avgProPice",
//		   dataType: "json",
//		   data: {storageId:storageId,productId:productId,costArith:trcostArith},
//		   async: false,
//		   success: function(json){
//		      if($.isNumeric(json.avgPrice)){
//		    	  selectTr.find("input[cname='basePrice']").val(json.avgPrice);
//		    	  selectTr.find("input[cname='price']").val(json.avgPrice);
//		    	  selectTr.find("input[cname='money']").val(json.avgPrice);
//		      }
//		      if(trcostArith==1 && $.isNumeric(json.avgPrice)==false){//移动加权算法没有成本单价拿最近进价
//		    	  	//没有成本就拿最近进价
//			  		$.ajax({
//			  			   type: "POST",
//			  			   url: basePath+"/stock/stock/lastBuyPrice",
//			  			   dataType: "json",
//			  			   data: {productId:productId,selectUnitId:selectUnitId},
//			  			   async: false,
//			  			   success: function(json){
//			  			      if($.isNumeric(json.lastBuyPrice)){
//			  			    	  selectTr.find("input[cname='basePrice']").val(json.lastBuyPrice);
//			  			    	  selectTr.find("input[cname='price']").val(json.lastBuyPrice);
//			  			    	  selectTr.find("input[cname='money']").val(json.lastBuyPrice);
//			  			      }
//			  			   }
//			  		});
//		      }
//		   }
//		});
		//没有成本就拿最近进价
  		$.ajax({
  			   type: "POST",
  			   url: basePath+"/stock/stock/lastBuyPrice",
  			   dataType: "json",
  			   data: {productId:productId,selectUnitId:selectUnitId},
  			   async: false,
  			   success: function(json){
  			      if($.isNumeric(json.lastBuyPrice)){
  			    	  selectTr.find("input[cname='basePrice']").val(json.lastBuyPrice);
  			    	  selectTr.find("input[cname='price']").val(json.lastBuyPrice);
  			    	  selectTr.find("input[cname='money']").val(json.lastBuyPrice);
  			      }
  			   }
  		});
		//var productPrice=getProductBackPrice(basePath,storageId,productId,trcostArith,selectUnitId);
		/*if($.isNumeric(productPrice)){
			selectTr.find("input[cname='basePrice']").val(productPrice);
		    selectTr.find("input[cname='price']").val(productPrice);
		    selectTr.find("input[cname='money']").val(productPrice);
		}*/
		
		if(trIndexs.indexOf(",")>0||trIndexs!=""){
			callbackProToCostPrice(trIndexs,productIds,trcostAriths,tbodyId);
		}
	}
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
}
/**
 * 选择手工指定法回调
 * @param trIndexs
 * @param productIds
 * @param trcostAriths
 * @param tbodyId
 * @return
 */
function callBackTackStockPrice(trIndexs,productIds,trcostAriths,tbodyId){
	var $parent = navTab.getCurrentPanel();
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
	var storageId = $parent.find("#storageId").val();
	if(!storageId) storageId = $parent.find("#outStorageId").val();
	var module = $parent.find("#module").val();//区分模块
	
	if(trcostArith!=1){
		var options = {};
	    options.width="540";
	    options.height="350";
	    options.param={module:module,storageId:storageId,productId:productId,trIndex:trIndex,trIndexs:trIndexs,productIds:productIds,trcostAriths:trcostAriths,tbodyId:tbodyId};
	    $.pdialog.open(basePath+"/stock/stock/manSelPrdSotock/", "PrdManSelPrd_dialog", "商品库存批次表", options);
	}else {
		//移动加权商品ajax请求得到平均成本单价
		$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/avgProPice",
		   dataType: "json",
		   data: {storageId:storageId,productId:productId,costArith:trcostArith},
		   async: false,
		   success: function(json){
		      if($.isNumeric(json.avgPrice)){
		    	  selectTr.find("input[cname='price']").val(json.avgPrice);
		    	  var amount = selectTr.find("input[cname='sckAmount']").val();
		    	  selectTr.find("input[cname='sckMoney']").val(round(amount*json.avgPrice,4));
		      }
		   }
		});
		if(trIndexs.indexOf(",")>0||trIndexs!=""){
			callBackTackStockPrice(trIndexs,productIds,trcostAriths,tbodyId);
		}
	}
	
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	//库存盘点单   (盘点总数量,库存总数量,库存总金额,亏盈总数量,亏盈总金额,零售总金额)统计
	takeStockBillTotal($parent);
}
/**
 * 选择手工指定法回调
 * @param trIndexs
 * @param productIds
 * @param trcostAriths
 * @param tbodyId
 * @return
 */
function callBackLastBuyPrice(trIndexs,productIds,trcostAriths,tbodyId){
	var $parent = navTab.getCurrentPanel();
	var basePath=$parent.find("#basePathId").val();//用于ajax请求
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
	var selectUnitId=selectTr.find("input[cname='selectUnitId']").val();//选择的单位ID
	var unitRelation2 = selectTr.find("input[cname='unitRelation2']").val();
	var unitRelation3 = selectTr.find("input[cname='unitRelation3']").val();
	
	var storageId = $parent.find("#storageId").val();
	
	//没有成本就拿最近进价   || 非移动加权成本算法商品,则以最近进价入库
	$.ajax({
		   type: "POST",
		   url: basePath+"/stock/stock/lastBuyPrice",
		   dataType: "json",
		   data: {productId:productId,selectUnitId:selectUnitId},
		   async: false,
		   success: function(json){
		      if($.isNumeric(json.lastBuyPrice)){
		    	  var basePrice = getConversionPrice(json.lastBuyPrice,selectUnitId,1,unitRelation2,unitRelation3,1)
		    	  selectTr.find("input[cname='basePrice']").val(basePrice);
		    	  selectTr.find("input[cname='price']").val(json.lastBuyPrice);
		    	  selectTr.find("input[cname='money']").val(json.lastBuyPrice);
		      }
		   }
	});
		
	
	if(trIndexs.indexOf(",")>0||trIndexs!=""){
		callBackLastBuyPrice(trIndexs,productIds,trcostAriths,tbodyId);
	}
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
}
/**
 * 双击手工指定法Tr
 * @param tbodyId
 * @param currentIndexTr
 * @param module
 * @return
 */
function stockProductBatchCallBack(tbodyId,currentIndexTr,module){
	var currentDialog=$.pdialog.getCurrent();
	var dialogSelectTr = currentDialog.find("tbody tr").filter(".selected");
	//商品批次信息
	var amount=dialogSelectTr.find("input[name='amount']").val(); //数量
	var costPrice=dialogSelectTr.find("input[name='costPrice']").val(); //成本价格 
	if(!costPrice||costPrice==""){
		costPrice=0;
	}
	var money=dialogSelectTr.find("input[name='money']").val(); //金额
	var batch=dialogSelectTr.find("input[name='batch']").val(); //批号 
	var produceDate=dialogSelectTr.find("input[name='produceDate']").val(); //生产日期 
	var produceEndDate=dialogSelectTr.find("input[name='produceEndDate']").val(); //到期日期 
	
	//赋值
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $parent.find("#whichCallBack").val();
	var basePath=$parent.find("#basePathId").val();//用于ajax请求
	var selectTr=$("#"+tbodyId,$parent).find("tr:eq("+currentIndexTr+")");
	var lastTd=selectTr.find("td").last();
	var costPriceInp = lastTd.find("input[cname='costPrice']").val();
	lastTd.find("input[cname='batch']").val(batch);
	lastTd.find("input[cname='produceDate']").val(produceDate);
	lastTd.find("input[cname='produceEndDate']").val(produceEndDate);
	lastTd.find("input[cname='costPrice']").val(costPrice);
	
	/*//保质期
	var validity =  lastTd.find("input[cname='validity']").val();
	if(validity&&validity!=0){
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
		lastTd.find("input[cname='produceEndDate']").val(year+"-"+month+"-"+day);
	}else{
		lastTd.find("input[cname='produceEndDate']").val(produceDate);
	}*/
	
	
	
	
	//库存盘点单选择商品批次改变
	if(module!=""&&module=="takeStock"){
		var calculateUnit1=lastTd.find("input[cname='calculateUnit1']").val();
		var calculateUnit2=lastTd.find("input[cname='calculateUnit2']").val();
		var calculateUnit3=lastTd.find("input[cname='calculateUnit3']").val();
		var unitRelation1=lastTd.find("input[cname='unitRelation1']").val();
		var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
		var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
		var retailPrice=lastTd.find("input[cname='retailPrice1']").val();
		
		var helpAmount=helpAmountStr(amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
		lastTd.find("input[cname='price']").val(costPrice);//单价
		lastTd.find("input[cname='sckAmount']").val(amount);//库存数量 
		lastTd.find("input[cname='sckHelpAmount']").val(helpAmount);//辅助库存数量 
		//lastTd.find("input[cname='amount']").val(amount);//设置盘点数量默认值
		//lastTd.find("input[cname='helpAmount']").val(helpAmount);//辅助盘点数量
		lastTd.find("input[cname='sckMoney']").val(money);//库存金额
		lastTd.find("input[cname='sckRetailMoney']").val(amount * retailPrice==0?'':strNullConvert(round(amount * retailPrice,4)));//零售金额
		//库存盘点单   (盘点总数量,库存总数量,库存总金额,亏盈总数量,亏盈总金额,零售总金额)统计
		takeStockBillTotal($parent);
		var selectTd=selectTr.find("td[cname=amount]");//要选中的TD
		addSelectClass($parent.find("#"+tbodyId).find('td'),selectTd);//选中
		//inputTakeCount(obj);
	}else if(module=="breakageBill" ||whichCallBack=="parityAllot"|| whichCallBack=="dismountBill"){//报损单 ，同价调拨单，拆装单
		if(costPrice && costPrice!=""){
			lastTd.find("input[cname='basePrice']").val(costPrice);//基本单价
			
			var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
			var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
			var selectUnitId=lastTd.find("input[cname='selectUnitId']").val();
			var price = getConversionPrice(costPrice, 1, 1, unitRelation2, unitRelation3, selectUnitId);
			lastTd.find("input[cname='price']").val(price);
			var amount = lastTd.find("input[cname='amount']").val();
			if($.isNumeric(amount) && $.isNumeric(costPrice)){
				lastTd.find("input[cname='money']").val(round(price * amount,4));
			}
			
			lastTd.find("input[cname='batch']").val(batch);
			lastTd.find("input[cname='produceDate']").val(produceDate);
			lastTd.find("input[cname='status']").val("");
			amountsTotal($parent);//table合计总数量
			moneysTotal($parent);//table合计总金额
		}else {
			var calculateUnit1=lastTd.find("input[cname='calculateUnit1']").val();
			var calculateUnit2=lastTd.find("input[cname='calculateUnit2']").val();
			var calculateUnit3=lastTd.find("input[cname='calculateUnit3']").val();
			var unitRelation1=lastTd.find("input[cname='unitRelation1']").val();
			var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
			var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
			
			lastTd.find("input[cname='basePrice']").val("");//基本单价
			lastTd.find("input[cname='price']").val("");
			lastTd.find("input[cname='money']").val("");
			lastTd.find("input[cname='batch']").val("");
			lastTd.find("input[cname='produceDate']").val("");
	    	lastTd.find("input[cname='selectUnitId']").val(1);//还原单位ID
	    	lastTd.find("input[cname='baseUnit']").val(calculateUnit1);//还原单位
	    	var helpAmount=helpAmountStr(1,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
			selectTr.find("input[cname='helpAmount']").val(helpAmount);//辅助数量 
	    	
			selectTr.find("td[cname='price']").find("div").html("");
			selectTr.find("td[cname='money']").find("div").html("");
			selectTr.find("td[cname='batch']").find("div").html("");
			selectTr.find("td[cname='produceDate']").find("div").html("");
			
			selectTr.find("td[cname='baseUnit']").find("div").html(calculateUnit1);
			selectTr.find("td[cname='helpAmount']").find("div").html(helpAmount);
		}
		var grid = $("#"+tbodyId,$parent).parents("div.grid:first");
		if(!grid){
			grid = $parent;
		}
		amountsTotal(grid);//table合计总数量
		moneysTotal(grid);//table合计总金额
		if(whichCallBack=="dismountBill"){//拆装单
			var moneys = grid.find("#moneys").val();
		    $parent.find("#outMoney").val(moneys);//入库金额
		}
		 
	}else if(module!=""&&module=="stockOtherout"){ //其它出库单
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
		var grid = $("#"+tbodyId,$parent).parents("div.grid:first");
		if(!grid){
			grid = $parent;
		}
		amountsTotal(grid);//table合计总数量
		moneysTotal(grid);//table合计总金额
	} else if(whichCallBack=="adjustCost"){//成本调价单
		
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
			selectTr.find("input[cname='amount']").val(amount);
	    	selectTr.find("input[cname='price']").val(costPrice);
	    	selectTr.find("input[cname='money']").val(costPrice*amount);
	    	selectTr.find("input[cname='lastPrice']").val(costPrice);
	    	selectTr.find("input[cname='lastMoney']").val(costPrice*amount);
	    	selectTr.find("input[cname='batch']").val(batch);
	    }else{
	    	selectTr.find("input[cname='price']").val("");
	    	selectTr.find("input[cname='money']").val("");
	    	selectTr.find("input[cname='lastPrice']").val("");
	    	selectTr.find("input[cname='lastMoney']").val("");
	    	selectTr.find("input[cname='batch']").val("");
	    }
    
		xsUnitChange(selectTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3)
		showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
		var grid = $("#"+tbodyId,$parent).parents("div.grid:first");
		if(!grid){
			grid = $parent;
		}
		amountsTotal(grid);//table合计总数量
		moneysTotal(grid);//table合计总金额
		lastMoneyTotal(grid);//合计调价后金额
	}
	
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值显示出来
	$.pdialog.closeCurrent();  //关闭当前Dialog
}
/**
 * 移动商品   负库存提示确认
 */
function xsNagetiveStockComfirm(){
	var $pdialog=$.pdialog.getCurrent();
	
	var modelType = $("#modelType",$pdialog).val();
	var basePath=$("#basePath",$pdialog).val();
	var trIndexs=$("#trIndexs",$pdialog).val();
	var productIds=$("#productIds",$pdialog).val();
	var negativeStockAmounts=$("#negativeStockAmounts",$pdialog).val();
	var costAriths=$("#costAriths",$pdialog).val();
	var selectUnitIds=$("#selectUnitIds",$pdialog).val();
	var prdUnitNames=$("#prdUnitNames",$pdialog).val();
	var notStocks=$("#notStocks",$pdialog).val();
	
	var negativeStockAmountArra=negativeStockAmounts.split(":");
	
	var trIndexsArra=trIndexs.split(":");
	var costArithsArra=costAriths.split(":");
	var selectUnitIdsArra=selectUnitIds.split(":");
	var prdUnitNamesArra=prdUnitNames.split(":");
	var notStocksArra=notStocks.split(":");
	
	var flag=false;
	$(notStocks.split(":")).each(function(i,val){
		var firstStr=negativeStockAmountArra[i].substring(0,1);
		if(val=="2"){  
			//弹出填写成本价格
			flag=true;
		}
	});
	
	if(flag==true){
		var newTrIndexs="";
		var prdCodes="";
		var prdFullNames="";
		var newSelectUnitIds="";
		var newPrdUnitNames="";
		var newCostAriths="";
		var newNotStocks="";
		var $trs=$("tbody tr",$pdialog);
		$($trs).each(function(i,tr){
			if("2"==notStocksArra[i]){
				newTrIndexs+=":"+trIndexsArra[i];
				prdCodes+=":"+encodeURI($(tr).find("td[ccname='code']").children("div").text());
				prdFullNames+=":"+encodeURI($(tr).find("td[ccname='fullName']").children("div").text());
				newSelectUnitIds+=":"+selectUnitIdsArra[i];
				newPrdUnitNames+=":"+prdUnitNamesArra[i];
				newCostAriths+=":"+costArithsArra[i];
				newNotStocks+=":"+notStocksArra[i];
			}
		});
		newTrIndexs=newTrIndexs.substring(1,newTrIndexs.length);
		prdCodes=prdCodes.substring(1,prdCodes.length);
		prdFullNames=prdFullNames.substring(1,prdFullNames.length);
		newSelectUnitIds=newSelectUnitIds.substring(1,newSelectUnitIds.length);
		newPrdUnitNames=newPrdUnitNames.substring(1,newPrdUnitNames.length);
		newCostAriths=newCostAriths.substring(1,newCostAriths.length);
		newNotStocks=newNotStocks.substring(1,newNotStocks.length);
		
		
		
		var options={};
		options.width="450";
	    options.height="300";
	    options.type="post";
	    
	    options.param={modelType:modelType,selectUnitIds:newSelectUnitIds,trIndexs:newTrIndexs,prdCodes:prdCodes,prdFullNames:prdFullNames,prdUnitNames:newPrdUnitNames,costAriths:newCostAriths,notStocks:newNotStocks};
	    //options.param={modelType:"sell",selectUnitIds:newSelectUnitIds,trIndexs:newTrIndexs,prdCodes:prdCodes,prdFullNames:prdFullNames,prdUnitNames:newPrdUnitNames,costAriths:newCostAriths,notStocks:newNotStocks};
		$.pdialog.open(basePath+"/sell/sell/getNagetiveStockPrice/", "xsd_nagetive_price_info", "商品成本价格", options);
		return;
	}else{
		var $parent = navTab.getCurrentPanel();
		var hasComfirmInp=$("#nagetiveStockHasComfirmId",$parent);
		hasComfirmInp.val("has");
		$.pdialog.closeCurrent();  //关闭当前Dialog
		var whichCallBack=$("#whichCallBack",$parent).val();
		if(whichCallBack&&whichCallBack=="sell"){
			$("#xs_sellForm",$parent).submit();
		}else if(whichCallBack&&whichCallBack=="stockOtherout"){
			$("#cc_qtckdForm",$parent).submit();
		}
	}
	
}
/**
 * 进货退货商品成本价校验输入
 * @return
 */
function nagetiveStockComfirm(){
	var $pdialog=$.pdialog.getCurrent();
	var flag  =  $("#flag",$pdialog).val();
	var url  = $("form:first",$pdialog).attr("action");
	var dialogId = $("input#dialogId",$pdialog).val();
	$.pdialog.closeCurrent();  //关闭当前Dialog
	if("true"==flag){
		var options={};
		options.width="500";
	    options.height="300";
	    $.pdialog.open(url,dialogId, "商品成本价格", options);
	}else{
		var $parent = navTab.getCurrentPanel();
		$("input#needComfirm",$parent).val(false);
		$("form:first",$parent).submit();
	}
}
/**
 * eg:销售时校验单价低于（最低售价等） 是否继续进行提示框
 * @return
 */
function sellUnPriceConfim(){
	var $pdialog = getCurrentPanel();
	var way  =  $("#way",$pdialog).val();
	$.pdialog.closeCurrent();  //关闭当前Dialog
	var $parent = getCurrentPanel();
	var unPriceConfim = $("input#unPriceConfim",$parent).val();
	unPriceConfim = unPriceConfim.replace(way,"");
	$("input#unPriceConfim",$parent).val(unPriceConfim);
	$("form:first",$parent).submit();
}

/*------------都是提示输入成本价格(重复了很多)--------------*/
/**
 * eg:销售单提交-负库存提示-确认-输入价格 -确认
 */
function xsNagetiveStockPrice(){
	//var $pdialog=$.pdialog.getCurrent();  //输入价格dialog
	var $pdialog=$("body").data("xsd_nagetive_price_info");//输入价格dialog
	var $parent = navTab.getCurrentPanel();
	var $trs=$("tbody tr",$pdialog);
	
	var type="";
    //把用户填写的价格填充到销售单里去		
	$trs.each(function(i,tr){
		type=$(tr).find("input[ccname='type']").val();
		var costPrice=$(tr).find("input[ccname='costPrice']").val();
		var trIndex=$(tr).find("input[ccname='trIndex']").val();
		var selectUnitIds=$(tr).find("input[ccname='selectUnitIds']").val();
		var trIndexArr=trIndex.split(",");
		$(trIndexArr).each(function(j,nvaTrNum){
			var nvaTr=$parent.find("tbody tr:eq("+eval(nvaTrNum-1)+")");
			var lastTd=nvaTr.find("td").last();
			var selectUnitId=lastTd.find("input[cname='selectUnitId']").val();
			var unitRelation1=lastTd.find("input[cname='unitRelation1']").val();
			var unitRelation2=lastTd.find("input[cname='unitRelation2']").val();
			var unitRelation3=lastTd.find("input[cname='unitRelation3']").val();
			//单位对应的价格      换算     最小单位价格        eg:   1C=10B=100A    C 100 换算A=1
			var newCostPrice =getConversionPrice(costPrice,selectUnitIds,unitRelation1,unitRelation2,unitRelation3,1);
			//var newCostPrice=unitLitterPrice(costPrice,selectUnitIds,selectUnitId,unitRelation1,unitRelation2,unitRelation3);
			lastTd.find("input[cname='costPrice']").val(newCostPrice); //记录当前行，方便错误提示
		});
	});
	
	if(type=="sell"){//销售单
		var hasComfirmInp=$("#nagetiveStockHasComfirmId",$parent);
		hasComfirmInp.val("has");
		$.pdialog.close($pdialog);  //输入价格dialog
		
		var oldDialog = $("body").data("xsd_nagetive_info");
		$.pdialog.close(oldDialog);  //负库存提示dialog
		$("#xs_sellForm",$parent).submit();
	}else if(type=="sellReturn"){//销售退货
		var hasComfirmInp=$("#priceStockHasComfirmId",$parent);
		hasComfirmInp.val("has");
		$.pdialog.close($pdialog);  //输入价格dialog
		$("#xsth_sellForm",$parent).submit();
	}else if(type=="otherin"){//其它入库单
		var hasComfirmInp=$("#priceStockHasComfirmId",$parent);
		hasComfirmInp.val("has");
		$.pdialog.close($pdialog);  //输入价格dialog
		$("#qtjhd_otherinForm",$parent).submit();
	}else if(type=="otherout"){//其它出库单
		var hasComfirmInp=$("#nagetiveStockHasComfirmId",$parent);
		hasComfirmInp.val("has");
		$.pdialog.close($pdialog);  //输入价格dialog
		
		var oldDialog = $("body").data("xsd_nagetive_info");
		$.pdialog.close(oldDialog);  //负库存提示dialog
		$("#cc_qtckdForm",$parent).submit();
	}
}
/**
 * 货物减少，负库存提示输入成本价
 * 确认提交
 * @return
 */
function nagetiveStockPrice(){
	var $parent = navTab.getCurrentPanel();
	var whichCallBack  = $parent.find("#whichCallBack").val();
	var billType = $parent.find("#billType").val();
	var $pdialog=$.pdialog.getCurrent();
	var $form = $("form:first",$pdialog);
	if (!$form.valid()) {
		return false;
	}
	
	var $trs=$("tbody:first tr",$parent);
    if(billType){
    	$trs=$("tbody[type='out']:first tr",$parent);
    }
    
	var $trIndexs = $("input[name='trIndex']",$form); 
	var $costPrices = $("input[name='costPrice']",$form); 
	$trIndexs.each(function(i,trIndex){
		var val  = $(trIndex).val();
		var tr = $trs[val-1];
		var costPrice = $($costPrices[i]).val();
		$(tr).find("input[cname='costPrice']").val(costPrice);
		if(whichCallBack=="parityAllot"){// 同价调拨单
			$(tr).find("input[cname='price']").val(costPrice);
			var amount=$(tr).find("input[cname='amount']").val();
			if($.isNumeric(costPrice*amount)){
				$(tr).find("input[cname='money']").val(round(costPrice*amount,4));
				$(tr).find("input[cname='status']").val("");
			}else{
				$(tr).find("input[cname='money']").val("");
			}
			showCurrentTrWidgetToSpan($(tr));//让tr里面所有td中的input值显示出来
		}
	});
	if(whichCallBack=="parityAllot"){// 同价调拨单
		amountsTotal($parent);//table合计总数量
		moneysTotal($parent);//table合计总金额
	}
	$.pdialog.closeCurrent();  //关闭当前Dialog
	
	$("input#needComfirm",$parent).val(false);
	$("form:first",$parent).submit(); //单据提交
	
}
/**
 * 货物减少，负库存提示输入成本价
 * 确认提交
 * @return
 */
function nagetiveStockPrice2(){
	var $parent = navTab.getCurrentPanel();
	var $pdialog=$.pdialog.getCurrent();
	var $form = $("form:first",$pdialog);
	if (!$form.valid()) {
		return false;
	}
	var $trs=$("tbody:first tr",$parent);
	var $trIndexs = $("input[name='trIndex']",$form); 
	var $prices = $("input[name='price']",$form); 
	$trIndexs.each(function(i,trIndex){
		var val  = $(trIndex).val();
		var tr = $trs[val-1];
		var price = $($prices[i]).val();
		$(tr).find("input[cname='price']").val(price);
		var amount = $(tr).find("input[cname='amount']").val();
		var baseAmount = $(tr).find("input[cname='baseAmount']").val();
		var money = round(price * amount,4);
		var basePrice = round(money / baseAmount,4);
		$(tr).find("input[cname='money']").val(money);
		$(tr).find("input[cname='basePrice']").val(basePrice);
		$(tr).find("input[cname='costPrice']").val(basePrice);
		
		showCurrentTrWidgetToSpan($(tr));//让tr里面所有td中的input值显示出来
	});
	amountsTotal($parent);//table合计总数量
	moneysTotal($parent);//table合计总金额
	
	$.pdialog.closeCurrent();  //关闭当前Dialog
	
	$("input#needComfirm",$parent).val(false);
	$("form:first",$parent).submit(); //单据提交
}
/**
 * 提示输入成本价  eg:销售换货单
 */
function inputXshrStockPrice(){
	var $parent = navTab.getCurrentPanel();
	var whichCallBack  = $parent.find("#whichCallBack").val();
	var $pdialog=$.pdialog.getCurrent();
	var $form = $("form:first",$pdialog);
	if (!$form.valid()) {
		return false;
	}
	
	var $trs=$("tbody[type='in']:first tr",$parent);
    
	var $trIndexs = $("input[name='trIndex']",$form); 
	var $costPrices = $("input[name='costPrice']",$form); 
	$trIndexs.each(function(i,trIndex){
		var val  = $(trIndex).val();
		var tr = $trs[val-1];
		var costPrice = $($costPrices[i]).val();
		$(tr).find("input[cname='costPrice']").val(costPrice);
		
	});
	
	$.pdialog.closeCurrent();  //关闭当前Dialog
	$("form:first",$parent).submit(); //单据提交
	
}
/*----------end--都是提示输入成本价格--------------*/

/*--------提示输入成本价格转成最小单位价格----------*/
/**
 * 转换数量
 * @param amount
 * @param selectUnitId
 * @param unitRelation1
 * @param unitRelation2
 * @param unitRelation3
 * @param newSelectUnitId  最终要转换的单位的数量
 * @return
 */
function getConversionAmount(amount,selectUnitId,unitRelation1,unitRelation2,unitRelation3, newSelectUnitId){
	var selUnitRelation = 0;
	var  oldUnitRelation = 0;
	
	//运算出正确数量
	if(newSelectUnitId && selectUnitId  && newSelectUnitId != selectUnitId){
		if(selectUnitId==1){
			selUnitRelation = unitRelation1;
		}else if(selectUnitId==2){
			selUnitRelation = unitRelation2;
		}else if(selectUnitId==3){
			selUnitRelation = unitRelation3;
		}
		if(newSelectUnitId==1){
			oldUnitRelation = unitRelation1;
		}else if(newSelectUnitId==2){
			oldUnitRelation = unitRelation2;
		}else if(newSelectUnitId==3){
			oldUnitRelation = unitRelation3;
		}
		if(selUnitRelation != 0){
			amount = selUnitRelation*amount/oldUnitRelation;
		}
		
	}
	if($.isNumeric(amount)){
		return amount;
	}
	return 0;
}
/**
 * 转换单价
 * @param price
 * @param selectUnitId
 * @param unitRelation1
 * @param unitRelation2
 * @param unitRelation3
 * @param newSelectUnitId 最终要转换的单位的价格
 * @return
 */
function getConversionPrice(price,selectUnitId,unitRelation1,unitRelation2,unitRelation3,newSelectUnitId) {
	var  selUnitRelation = 0;
	var  oldUnitRelation = 0;
	//运算出正确的价格
	if(newSelectUnitId !=null && selectUnitId !=null && newSelectUnitId != selectUnitId){
		if(selectUnitId==1){
			selUnitRelation = unitRelation1;
		}else if(selectUnitId==2){
			selUnitRelation = unitRelation2;
		}else if(selectUnitId==3){
			selUnitRelation = unitRelation3;
		}
		
		if(newSelectUnitId==1){
			oldUnitRelation = unitRelation1;
		}else if(newSelectUnitId==2){
			oldUnitRelation = unitRelation2;
		}else if(newSelectUnitId==3){
			oldUnitRelation = unitRelation3;
		}
		
		if(selUnitRelation != 0){
			price = round(oldUnitRelation * price / selUnitRelation,4);
		}
	}
	if($.isNumeric(price)){
		return price;
	}
	return "";
}
/*------end--提示输入成本价格转成最小单位价格--------*/
/*----------------------------------------end---商品带回回调（手工指定法）商品--------------------------------------------*/





/*-------------------------------------------引单做单-----------------------------------------------*/
/**
 * 进货单 选择 进货订单 带回
 * @param json
 * @return
 */
function billCallBackDetail(json){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(json);
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd= $parent.find("tbody tr td:first");
	}
	var selectTr =  selectTd.parent();
	var storageSupId = $parent.find("#storageSupId").val();
	var storageId = $parent.find("#storageId").val();//仓库
	var storageCode = $parent.find("#storageCode").val();
	var storageName = $parent.find("#storageName").val();
	var discount = $parent.find("#discounts").val();//折扣
	var detailList = json.detailList;
	var bill = json.bill;
	if(bill){
		var unitId = $("#unitId",$parent).val();//供货单位
		var unitName = $("#unitName",$parent).val();
		if($.trim(unitId)=="" ||$.trim(unitId)=="0"){
			$("#unitId",$parent).val(bill.unit.id);
			$("#unitName",$parent).val(bill.unit.fullName);
		}
		var staffId = $("#staffId",$parent).val();//经收人
		var staffName = $("#staffName",$parent).val();
		if($.trim(staffId)=="" ||$.trim(staffId)=="0"){
			$("#staffId",$parent).val(bill.staff.id);
			$("#staffName",$parent).val(bill.staff.name);
		}
		var departmentId = $("#departmentId",$parent).val();
		var departmentName = $("#departmentName",$parent).val();
		if($.trim(departmentId)=="" && bill.department){
			$("#departmentId",$parent).val(bill.department.id);
			$("#departmentName",$parent).val(bill.department.fullName);
		}
		if($.trim(storageId)=="" ||$.trim(storageId)=="0"){
			storageSupId = bill.storage.supId;
			storageId = bill.storage.id;
			storageCode = bill.storage.code;
			storageName = bill.storage.fullName;
			$("#storageSupId",$parent).val(storageSupId);
			$("#storageId",$parent).val(storageId);
			$("#storageCode",$parent).val(storageCode);
			$("#storageName",$parent).val(storageName);
		}
		var remark = $("#remark",$parent).val();//摘要
		if($.trim(remark)=="" ){
			$("#remark",$parent).val(bill.bill.remark);
		}
		var memo = $("#memo",$parent).val();//附加说明
		if($.trim(memo)=="" ){
			$("#memo",$parent).val(bill.bill.memo);
		}
		var discounts = $("#discounts",$parent).val();//折扣
		if($.trim(discounts)=="" ){
			$("#discounts",$parent).val(bill.bill.discounts);
		}
	}
	if(!detailList){
		return false;
	}
	var showDiscount = json.showDiscount;
	var showTaxRate = json.showTaxRate;

	for(var i=0;i<detailList.length;i++){
		var object=detailList[i];
		selectTr=filterTr(selectTr);//当前行是否有商品
	    billClearTrData(selectTr);//单据清除行数据
		addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
	
		
		var calculateUnit1=object.product.calculateUnit1; //单位名称
		var calculateUnit2=object.product.calculateUnit2;
		var calculateUnit3=object.product.calculateUnit3;
		var unitRelation1=object.product.unitRelation1;   //单位关系
		var unitRelation2=object.product.unitRelation2;
		var unitRelation3=object.product.unitRelation3;
		var selectUnitId = object.detail.selectUnitId || 1;
		
		
		//添加商品隐藏控件
	    var lastTd=selectTr.find("td").last();
	   
	    var str="<input type='hidden' cname='detailId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].detailId' />";  //订单明细ID
		lastTd.find("div").append(str);
	    
		
		selectTr.find("input[cname='detailId']").val(object.detail.id);   //订单明细ID
		var costPrice = object.detail.costPrice;
		if(costPrice){
			var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice' />";  //明细上平均成本价
			str+="<input type='hidden' name='helpUitl["+selectTr.index()+"].costPrice' cname='costPrice'>";
			lastTd.find("div").append(str);
			//costPrice =getConversionPrice(costPrice,selectUnitId,unitRelation1,unitRelation2,unitRelation3,1);
			selectTr.find("input[cname='costPrice']").val(costPrice);
		}
		
        billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
        if(object.storage){
        	storageId=object.storage.id;
        	storageCode=object.storage.code;
        	storageName=object.storage.fullName;
        }
		billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值

		var baseUnit = "";
		var  untreatedAmount = object.detail.untreatedAmount || object.detail.amount;//未处理的数据
		untreatedAmount = getConversionAmount(untreatedAmount, 1, unitRelation1, unitRelation2, unitRelation3, selectUnitId);
		var  amount = untreatedAmount;
		var retailPrice = 0;
		if(object.detail.selectUnitId == 1){
			baseUnit =calculateUnit1;
			retailPrice = object.product.retailPrice1;
			amount=untreatedAmount*unitRelation1;
		}else if(object.detail.selectUnitId == 2){
			baseUnit =calculateUnit2;
			retailPrice = object.product.retailPrice2;
			amount=untreatedAmount*unitRelation2;
		}else if(object.detail.selectUnitId == 3){
			baseUnit =calculateUnit3;
			retailPrice = object.product.retailPrice3;
			amount=untreatedAmount*unitRelation3;
		}
		
		selectTr.find("input[cname='baseUnit']").val(baseUnit);//单位
		selectTr.find("input[cname='selectUnitId']").val(object.detail.selectUnitId);//商品运算单位ID
		
	    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
		
		//单据字段赋值
		selectTr.find("input[cname='amount']").val(untreatedAmount);//数量
		selectTr.find("input[cname='arrivalAmount']").val(object.detail.arrivalAmount);//到货数量
		if(!showDiscount && !showTaxRate){
			selectTr.find("input[cname='price']").val(object.detail.taxPrice);//单价
	        selectTr.find("input[cname='money']").val(object.detail.taxMoney);//总金额                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
		}else if(showDiscount && !showTaxRate){
			selectTr.find("input[cname='price']").val(object.detail.price);//单价
	        selectTr.find("input[cname='money']").val(object.detail.money);//总金额
	        selectTr.find("input[cname='discountPrice']").val(object.detail.taxPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(object.detail.taxMoney);//折扣金额
	        var detailDiscount = round(object.detail.taxPrice/object.detail.price,4);
	        if($.isNumeric(detailDiscount)){
	        	selectTr.find("input[cname='discount']").val(detailDiscount);//折扣
	        }else{
	        	selectTr.find("input[cname='discount']").val("");//折扣
	        }
	        
		}else if(!showDiscount && showTaxRate){
			selectTr.find("input[cname='price']").val(object.detail.discountPrice);//单价
			selectTr.find("input[cname='money']").val(object.detail.discountMoney);//总金额
			selectTr.find("input[cname='taxRate']").val(object.detail.taxRate);//税率
	        selectTr.find("input[cname='taxPrice']").val(object.detail.taxPrice);//含税单价
	        selectTr.find("input[cname='taxes']").val(object.detail.taxes);//税额
	        selectTr.find("input[cname='taxMoney']").val(object.detail.taxMoney);//含税金额
	      
	       
		}else{
			selectTr.find("input[cname='price']").val(object.detail.price);//单价
	        selectTr.find("input[cname='money']").val(object.detail.money);//总金额
	        selectTr.find("input[cname='discount']").val(object.detail.discount);//折扣
	        selectTr.find("input[cname='discountPrice']").val(object.detail.discountPrice);//折扣单价
	        selectTr.find("input[cname='discountMoney']").val(object.detail.discountMoney);//折扣金额
	        selectTr.find("input[cname='taxRate']").val(object.detail.taxRate);//税率
	        selectTr.find("input[cname='taxPrice']").val(object.detail.taxPrice);//含税单价
	        selectTr.find("input[cname='taxes']").val(object.detail.taxes);//税额
	        selectTr.find("input[cname='taxMoney']").val(object.detail.taxMoney);//含税金额
		}
		
        selectTr.find("input[cname='memo']").val(object.detail.memo);   //备注
        selectTr.find("input[cname='produceDate']").val(object.detail.produceDate);   //生产日期
        selectTr.find("input[cname='batch']").val(object.detail.batch);   //批号
        selectTr.find("input[cname='message']").val(object.detail.message);   //附加信息
        selectTr.find("input[cname='barCode']").val(object.product.barCode);//条码
        selectTr.find("input[cname='retailPrice']").val(retailPrice);//零售价
        selectTr.find("input[cname='retailMoney']").val((retailPrice)*(object.detail.amount));//零售金额
	    
        if(!$.isNumeric(object.detail.price) || object.detail.price==0){
        	
	    	selectTr.find("input[cname='status']").val("赠品");//状态
	    }
	    showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	    amountPriceDiscountChangePrivilege(selectTr,false);
	    selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
	
	//sellbookOrboughgTotal($parent);//销售订单，进货订单  (总数量,总金额,折扣总金额,合计税额,合计含税金额)统计
	//retailMoneysTotal($parent);//合计零售金额
	//privilegeMoneyTotal();//合计优惠后金额
}
/**
 * 销售单 选择销售订单     带回
 * @param json
 * @return
 */
function xsdBillCallBackDetail(json){
	$.pdialog.closeCurrent();  //关闭当前Dialog
	DWZ.ajaxDone(json);
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	if(selectTd.length==0){
		selectTd= $parent.find("tbody tr td:first");
	}
	var selectTr =  selectTd.parent();
	var storageSupId = $parent.find("#storageSupId").val();
	var storageId = $parent.find("#storageId").val();//仓库
	var storageCode = $parent.find("#storageCode").val();
	var storageName = $parent.find("#storageName").val();
	var discount = $parent.find("#discounts").val();//折扣
	var detailList = json.detailList;
	var bill = json.bill;
	if(bill){
		var unitId = $("#unitId",$parent).val();//供货单位
		var unitName = $("#unitName",$parent).val();
		if($.trim(unitId)=="" || $.trim(unitId)=="0"){
			$("#unitId",$parent).val(bill.unit.id);
			$("#unitName",$parent).val(bill.unit.fullName);
		}
		var staffId = $("#staffId",$parent).val();//经收人
		var staffName = $("#staffName",$parent).val();
		if($.trim(staffId)=="" ||$.trim(staffId)=="0"){
			$("#staffId",$parent).val(bill.staff.id);
			$("#staffName",$parent).val(bill.staff.name);
		}
		var departmentId = $("#departmentId",$parent).val();
		var departmentName = $("#departmentName",$parent).val();
		if(($.trim(departmentId)==""||$.trim(departmentId)=="0") && bill.department){
			$("#departmentId",$parent).val(bill.department.id);
			$("#departmentName",$parent).val(bill.department.fullName);
		}
		if($.trim(storageId)=="" ||$.trim(storageId)=="0"){
			storageSupId = bill.storage.supId;
			storageId = bill.storage.id;
			storageCode = bill.storage.code;
			storageName = bill.storage.fullName;
			$("#storageSupId",$parent).val(storageSupId);
			$("#storageId",$parent).val(storageId);
			$("#storageCode",$parent).val(storageCode);
			$("#storageName",$parent).val(storageName);
		}
		var remark = $("#remark",$parent).val();//摘要
		if($.trim(remark)=="" ){
			$("#remark",$parent).val(bill.bill.remark);
		}
		var memo = $("#memo",$parent).val();//附加说明
		if($.trim(memo)=="" ){
			$("#memo",$parent).val(bill.bill.memo);
		}
		var discounts = $("#discounts",$parent).val();//折扣
		if($.trim(discounts)=="" ){
			$("#discounts",$parent).val(bill.bill.discounts);
		}
	}
	if(!detailList){
		return false;
	}
	var trIndexs="";          //商品带回要填充的tr
	var trcostAriths="";      //是否有dialog弹出（手工指定法）
	var productIds="";        //商品ID
	var tbodyId=selectTr.parent().attr("id");
	for(var i=0;i<detailList.length;i++){
		var object=detailList[i];
		selectTr=filterTr(selectTr);//当前行是否有商品
		
		addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象      eg:销售单选择商品  显示当前行所有的控件对象
		//添加商品隐藏控件
	    var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' cname='detailId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].detailId' />";  //订单明细ID
		lastTd.find("div").append(str);
		selectTr.find("input[cname='detailId']").val(object.detail.id);   //订单明细ID
		if(object.detail.costPrice){
			var str="<input type='hidden' cname='costPrice' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].costPrice' />";  //明细上平均成本价
			lastTd.find("div").append(str);
			selectTr.find("input[cname='costPrice']").val(object.detail.costPrice);
		}
		
		billProductHiddenAndValue(selectTr,object);//单据隐藏商品对象并赋值
		billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName);//单据隐藏仓库对象并赋值

		var baseUnit = "";
		var  untreatedAmount = object.detail.untreatedAmount || object.detail.amount;//未处理的数据
		var calculateUnit1=object.product.calculateUnit1; //单位名称
		var calculateUnit2=object.product.calculateUnit2;
		var calculateUnit3=object.product.calculateUnit3;
		var unitRelation1=object.product.unitRelation1;   //单位关系
		var unitRelation2=object.product.unitRelation2;
		var unitRelation3=object.product.unitRelation3;
		var selectUnitId = object.detail.selectUnitId || 1;
		untreatedAmount = getConversionAmount(untreatedAmount, 1, unitRelation1, unitRelation2, unitRelation3, selectUnitId);
		var  amount = untreatedAmount;
		var retailPrice = 0;
		if(object.detail.selectUnitId == 1){
			baseUnit =calculateUnit1;
			retailPrice = object.product.retailPrice1;
			amount=untreatedAmount*unitRelation1;
		}else if(object.detail.selectUnitId == 2){
			baseUnit =calculateUnit2;
			retailPrice = object.product.retailPrice2;
			amount=untreatedAmount*unitRelation2;
		}else if(object.detail.selectUnitId == 3){
			baseUnit =calculateUnit3;
			retailPrice = object.product.retailPrice3;
			amount=untreatedAmount*unitRelation3;
		}
		
		selectTr.find("input[cname='baseUnit']").val(baseUnit);//单位
		selectTr.find("input[cname='selectUnitId']").val(object.detail.selectUnitId);//商品运算单位ID
		
	    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
	    xsUnitChange(selectTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);
		
		//单据字段赋值
		selectTr.find("input[cname='amount']").val(untreatedAmount);//数量
		selectTr.find("input[cname='arrivalAmount']").val(object.detail.arrivalAmount);//到货数量
		selectTr.find("input[cname='discount']").val(object.detail.discount);//折扣
		selectTr.find("input[cname='price']").val(object.detail.price);//单价
        selectTr.find("input[cname='money']").val(object.detail.money);//总金额
        selectTr.find("input[cname='discountPrice']").val(object.detail.discountPrice);//折扣单价
        selectTr.find("input[cname='discountMoney']").val(object.detail.discountMoney);//折扣金额
        selectTr.find("input[cname='taxRate']").val(object.detail.taxRate);//税率
        selectTr.find("input[cname='taxPrice']").val(object.detail.taxPrice);//含税单价
        selectTr.find("input[cname='taxes']").val(object.detail.taxes);//税额
        selectTr.find("input[cname='taxMoney']").val(object.detail.taxMoney);//含税金额
        selectTr.find("input[cname='memo']").val(object.detail.memo);   //备注
        selectTr.find("input[cname='produceDate']").val(object.detail.produceDate);   //生产日期
        selectTr.find("input[cname='batch']").val(object.detail.batch);   //批号
        selectTr.find("input[cname='message']").val(object.detail.message);   //附加信息
        selectTr.find("input[cname='barCode']").val(object.product.barCode);//条码
        selectTr.find("input[cname='retailPrice']").val(retailPrice);//零售价
        selectTr.find("input[cname='retailMoney']").val((retailPrice)*(object.detail.amount));//零售金额
	    
        if(!$.isNumeric(object.detail.price) || object.detail.price==0){
        	
	    	selectTr.find("input[cname='status']").val("赠品");//状态
	    }
	    showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	    amountPriceDiscountChangePrivilege(selectTr,false);
	    
	    
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
/*----------------------------------------end---引单做单--------------------------------------------*/





/*-------------------------------------------单据(商品)(仓库)(会计科目)带回隐藏值-----------------------------------------------*/
/**
 * 隐藏仓库值 并赋值
 * @param selectTr
 * @param storageSupId
 * @param storageId
 * @param storageCode
 * @param storageName
 * @return
 */
function billStorageHiddenAndValue(selectTr,storageSupId,storageId,storageCode,storageName){
	//添加隐藏控件
    var lastTd=selectTr.find("td").last();
	var str="<input type='hidden' class='stealth' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].storageId' cname='storageId'/>";  //仓库Id
	str+="<input type='hidden' cname='oldStorageCode'/>";//据仓库旧编号    方便修改编号  弹出修改，还是查询 
	str+="<input type='hidden' cname='oldStorageFullName'/>";
	str+="<input type='hidden' cname='storageSupId'/>";
	lastTd.find("div").append(str);
	selectTr.find("input[cname='storageSupId']").val(storageSupId);   
	selectTr.find("input[cname='storageId']").val(storageId==""?0:storageId);   //仓库Id
    selectTr.find("input[cname='oldStorageCode']").val(storageCode);
    selectTr.find("input[cname='oldStorageFullName']").val(storageName); 
	//end 添加隐藏控件
	
    selectTr.find("input[cname='storageCode']").val(storageCode);   //仓库编号
    selectTr.find("input[cname='storageFullName']").val(storageName);   //仓库名称
}
/**
 * 隐藏会计科目值并赋值
 * @param selectTr
 * @param object
 * @param helpObj
 * @return
 */
function billAccountsHiddenAndValue(selectTr,object,helpObj){
	//添加隐藏控件
    var lastTd=selectTr.find("td").last();
    var str="<input type='hidden' cname='accountsSupId'/>"; //父级ID
	str+="<input type='hidden' cname='accountsId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].accountsId' />";  //商品Id
	str+="<input type='hidden' cname='oldAccountsCode'/>";              //单据商品旧编号    方便修改编号  弹出修改，还是查询
	str+="<input type='hidden' cname='oldAccountsFullName'/>";
	//当前行，用于错误提示
	if(!helpObj) helpObj = "helpUitl";
	str+="<input type='hidden' cname='trIndex' name='"+helpObj+"["+selectTr.index()+"].trIndex' value='"+selectTr.index()+"'/>";   
	var tds=selectTr.find("td");
	lastTd.find("div").append(str);
	

	selectTr.find("input[cname='accountsSupId']").val(object.supId);              //父级Id
	selectTr.find("input[cname='accountsId']").val(object.id);           //ID
	selectTr.find("input[cname='accountsCode']").val(object.code);               //编号
	selectTr.find("input[cname='oldAccountsCode']").val(object.code);            // 编号   单据商品旧编号    方便修改编号  弹出修改，还是查询 
	selectTr.find("input[cname='accountsFullName']").val(object.fullName);       //全称
	selectTr.find("input[cname='oldAccountsFullName']").val(object.fullName);    //全名   单据旧全名    方便修改全名  弹出修改，还是查询 
	selectTr.find("input[cname='accountsSmallName']").val(object.smallName);    
	selectTr.find("input[cname='accountsSpell']").val(object.spell);    
	selectTr.find("input[cname='accountsMemo']").val(object.memo);    
	
	
	//end   添加隐藏控件
}
/**
 * 隐藏明细科目并赋值   eg:会计科目
 * @param selectTr
 * @param object
 * @return
 */
function detailAccountsHiddenAndValue(selectTr,object){
    //父级Id
	var lastTd=selectTr.find("td").last();
	var str="<input type='hidden' cname='detailId' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].detailId' />";  //明细科目的Id
	lastTd.find("div").append(str);
    selectTr.find("input[cname='accountsDetail']").val(object.fullName);
    selectTr.find("input[cname='detailId']").val(object.id);
}
/*-----------------------------------------end--单据(商品)(仓库)(会计科目)带回隐藏值--------------------------------------------*/






/*-------------------------------------------公共方法-----------------------------------------------*/
/**
 * 查看单据   下一张
 * @return
 */
function getRealBill(realBillType){
	var $tab = navTab._getTabs().filter(".selected");
	var url = $tab.attr("url");
	jQuery.ajax({
		type:'POST',
		url:url,
		dataType:'json',
		async: false,
		data:{'reloadType':realBillType},
		timeout: 5000,
		cache: false,
		success: function(res){
			url= projectBasePath+res.url;
			$tab.attr("url",url);
		}
	});
	navTab.reload(url);
}
/**
 * 报表增加基本信息筛选 确认按钮
 * @return
 */
function reprotBaseSearchSubmit(){
	var $dialog = getCurrentPanel();
	var nvaTabRel = $("#aimTabId",$dialog).val();                 //设置目标Tab组件ID
	var nvaTabUrl = $("#aimUrl",$dialog).val();                   //设置目标URl
	var nvaTabTitle = $("#aimTitle",$dialog).val();               //设置目标标题
	var aimDiv = $("#aimDiv",$dialog).val();               		  //设置刷新DIV
	
	var submitFormId = $("#submitFormId",$dialog).val();       //筛选后把下面input封装到的input 表单ID
	var searchBaseAttr = $("#searchBaseAttr",$dialog).val();   //基本信息属性
	var searchBaseVal = $("#searchBaseVal",$dialog).val();     //基本信息属性值
	//关闭当前dialog
	$.pdialog.closeCurrent();  
	
	var $panel = getCurrentPanel();
	var submitForm = $("#"+submitFormId,$panel);         //筛选后把下面input封装到的input 表单ID
	$("#searchBaseAttr",$panel).val(searchBaseAttr);
	$("#searchBaseVal",$panel).val(searchBaseVal);
	$("input[name=pageNum]",$panel).val(1);
	
	//进货特殊处理
	if(!searchBaseVal){
		$("input[name=isRow]",$panel).val("false");
	}
	
	var $fromData=submitForm.serializeArray();
	if(aimDiv){
		var $rel = $("#"+aimDiv,$panel);
		$rel.loadUrl(nvaTabUrl, $fromData, function(){
			$rel.find("[layoutH]").layoutH();
		},'post');
	}else {
		navTab.openTab(nvaTabRel, nvaTabUrl,{title:nvaTabTitle,data:$fromData,freshBtn:true,method:"POST"});
	}
}
/**
 * 单据附件下载
 * @param obj
 * @return
 */
function orderFujianDownChangeUrl(obj){
	var $obj=$(obj);
	var $parent=getCurrentPanel();
	var tr=$parent.find("tr").filter(".selected");
	if(tr){
		var id=tr.attr("rel");
		if(id&&id!=0){
			var url=$obj.attr("url");
			url = url+id;
			$.ajax({
				type:'POST',
				url:url+"-validate",
				dataType:"json",
				cache: false,
				success:function(data){
					if(data.statusCode!="200"){
						alertMsg.error(data.message);
						return false;
					}else{
						window.location.href=url+"-down";
					}
			    },
				error: DWZ.ajaxError
			});
		}else{
			alertMsg.error("请选择一条记录");
			return false;
		}
	}
}
/**
 * 商品价格折扣跟踪  商品带回时触发
 * @return
 */
function productPriceDiscountTrack(basePathId,billType,unitId,productId,valId){
	var panel=navTab.getCurrentPanel();
	//整单折扣
	var allDiscount=$("#discounts",panel);
	var $billType  = panel.find("input#billType").val();
	var $gridDiv = panel;
	if(panel.find("input#billType"))$gridDiv = panel.find("tbody[type='"+$billType+"']").parents("div.grid:first");
    var _td=$gridDiv.find("td").filter(".selected");//单据td
   
    var currentTr=$(_td).parent();
	$.ajax({
		type: 'post',
		url: basePathId+"/base/product/calculateUnitCall",
		data: {billType:billType,unitId:unitId,productId:productId,selectUnitId:valId},
		async: false,
		cache: false,
		dataType:'json',
		success: function(data){
			var trackPrice=data["price"];
			var trackDiscount=data["discount"];
			
			
		    currentTr.find("input[cname='price']").val(trackPrice);
		    if(!allDiscount||allDiscount.val()==""||allDiscount.val()=="0"){
		    	currentTr.find("input[cname='discount']").val(trackDiscount);
		    }
		},
		error: DWZ.ajaxError
	});
	return _td;
}
/**
 * 打开navTab之前进行校验
 */
function callBeforeNavTab (options){
	var url = unescape(options.url);
	DWZ.debug(url);
	if (!url.isFinishedTm()) {
		alertMsg.error($(options.obj).attr("warn") || DWZ.msg("alertSelectMsg"));
		return false;
	}
	var serialize = eval($(options.obj).attr("serialize") || "false");//是否连载父页面的数据
	var data = {checkout:options.checkout};
	if(true==serialize){
		var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
	    data = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
	    var checkout =  new Object();
	    checkout.name="checkout";
	    checkout.value=options.checkout;
	    data.push({name:"checkout",value:options.checkout});
	}
	var $callback =navTabAjaxDone;
	var result=$.ajax({
		type:'GET',
		url:url,
		data:data,
		dataType:"json",
		cache: false,
		async: false,
		success:$callback,
		error: DWZ.ajaxError
	});
	if(result.responseText&& result.responseText.indexOf("statusCode")!=-1 ){
		return false;
	}else{
		return true;
	}
}
/**
 * 打开navTab之前进行校验
 */
function callBeforeNavTabValidate (options){
	var flag=false;
	
	var $this=options.obj;  //事件源
	var modelType=$this.attr("modelType");
	if(!modelType){
		return true;
	}
	var url = unescape($this.attr("href1")).replaceTmById($this.parents(".unitBox:first"));
	//var url = unescape($this.attr("href1"));
	DWZ.debug(url);
	if (!url.isFinishedTm()) {
		alertMsg.error($(options.obj).attr("warn") || DWZ.msg("alertSelectMsg"));
		return flag;
	}
	var data={};
	if(modelType=="newUrlValidate"){   //进入验证url验证返回
		$.ajax({
			type:'POST',
			url:url,
			data:data,
			dataType:"json",
			cache: false,
			async: false,
			success: function(data){
			    if(data.statusCode!=200){
			    	alertMsg.error(data.message);
			    }else{
			    	flag=true;
			    }
		    },
			error: DWZ.ajaxError
		});
	}else {
		//扩展
	}
	return flag;
}
/*----------------------------------------end---公共方法--------------------------------------------*/



