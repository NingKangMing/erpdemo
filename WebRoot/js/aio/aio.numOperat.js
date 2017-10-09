//请使用  shift+cltr+/ 查看方法分布（切记别乱写）
//-----------------------------------通用字段合计(横向 ,纵向,优惠)--------------------------------------
/**
 * 数量，单价，折扣    (横向,纵向,优惠)
 * @param obj
 * @param isRemove
 * @return
 */
function amountPriceDiscountChangePrivilege(obj,isRemove){
	amountPriceDiscountChange(obj,isRemove);//表格内的数值重新计算
	privilegeMoneyTotal();//合计优惠后的金额
}
/**
 * 金额           (横向,纵向,优惠)
 * @param obj
 * @return
 */
function moneyChangePrivilege(obj){
	moneyChange(obj);
	privilegeMoneyTotal();
}
/**
 * 折后单价         (横向,纵向,优惠)
 * @param obj
 * @return
 */
function discountPriceChangePrivilege(obj){
	discountPriceChange(obj);
	privilegeMoneyTotal();
}
/**
 * 折后金额         (横向,纵向,优惠)
 * @param obj
 * @return
 */
function discountMoneyChangePrivilege(obj){
	discountMoneyChange(obj);
	privilegeMoneyTotal();
}
/**
 * 税率           (横向,纵向,优惠)
 * @param obj
 * @return
 */
function taxRateChangePrivilege(obj){
	taxRateChange(obj);
	privilegeMoneyTotal();
}
/**
 * 含税单价         (横向,纵向,优惠)
 * @param obj
 * @return
 */
function taxPriceChangePrivilege(obj){
	taxPriceChange(obj);
	privilegeMoneyTotal();
}
/**
 * 含税金额         (横向,纵向,优惠)
 * @param obj
 * @return
 */
function taxMoneyChangePrivilege(obj){
	taxMoneyChange(obj);
	privilegeMoneyTotal();
}
//------------------------------end--通用字段合计(横向   纵向     优惠)--------------------------------------



//-----------------------------------通用字段合计(横向 ,纵向)--------------------------------------
/**
 * 数量，单价，折扣     (横向 ,纵向)
 * @param obj
 * @param isRemove
 * @return
 */
function amountPriceDiscountChange(obj,isRemove){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	if($(obj)[0] && $(obj)[0].nodeName=="TR") currentTr = $(obj);
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		$(obj).remove();
		return;
	}
	if(isRemove==undefined || isRemove)aioBillOnblurRemoveWidget(obj);
	rowAmountPriceDiscountChange(currentTr);//数量，单价，折扣  (横向)
	showCurrentTrWidgetToSpan(currentTr);//让tr里面所有td中的input值显示出来
	
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){//进货换货    销售换货    生产拆装单
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	sellbookOrboughgTotal($parent);//销售订单，进货订单       总合计(数量,金额,折后金额,税额,含税金额,零售金额)
}
/**
 * 金额            (横向 ,纵向)    
 * @param obj
 * @return
 */
function moneyChange(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var money = currentTr.find("input[cname='money']").val();//金额
	var discount = currentTr.find("input[cname='discount']").val()||1;//折扣
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	if($.trim(amount)=="" || $.trim(amount)==0){
		alertMsg.error("请输入数量!");
		currentTr.find("input[cname='money']").val("");
		currentTr.find("td[cname='money']").children("div").text("");
		return;
	}
	if($.isNumeric(money)){
		money = round(money,4);
		currentTr.find("input[cname='money']").val(money==0?"":money);//金额
	}
	if($.isNumeric(money) && $.isNumeric(amount) && $.isNumeric(round(money/amount,4))){
		currentTr.find("input[cname='price']").val(round(money/amount,4));//单价
	}else{
		currentTr.find("input[cname='price']").val("");//单价
	}
	if($.isNumeric(money) && $.isNumeric(discount) && $.isNumeric(round(money*discount,4))){
		currentTr.find("input[cname='discountMoney']").val(round(money*discount,4));//折后金额
	}else{
		currentTr.find("input[cname='discountMoney']").val("");//折后金额
	}
	if($.isNumeric(money) && $.isNumeric(discount) && $.isNumeric(amount) && $.isNumeric(round(money*discount/amount,4))){
		currentTr.find("input[cname='discountPrice']").val(round(money*discount/amount,4));//折后价
	}else{
		currentTr.find("input[cname='discountPrice']").val("");//折后价
	}
	if($.isNumeric(money) && $.isNumeric(discount) && $.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(round(money*discount/amount*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxPrice']").val(round(money*discount/amount*(taxRate/100+1),4));//含税单价
	}else{
		currentTr.find("input[cname='taxPrice']").val("");//含税单价
	}
	if($.isNumeric(money) && $.isNumeric(discount) && $.isNumeric(taxRate) && $.isNumeric(round(money*discount*taxRate/100,4)) && round(money*discount*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(money*discount*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	if($.isNumeric(money) && $.isNumeric(discount) && $.isNumeric(taxRate) && $.isNumeric(round(money*discount*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxMoney']").val(round(money*discount*(taxRate/100+1),4));//税额
	}else{
		currentTr.find("input[cname='taxMoney']").val("");//含税金额
	}
	if($.trim(money)=="0" || $.trim(money)==""){
		currentTr.find("input[cname='status']").val("赠品");
	}else{
		currentTr.find("input[cname='status']").val("");
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	moneysTotal($parent);//table合计总金额
	discountMoneysTotal($parent);//table合计折扣总金额
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
	
}
/**
 * 折后单价          (横向 ,纵向)
 * @param obj
 * @return
 */
function discountPriceChange(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	var  price = currentTr.find("input[cname='price']").val();//单价
	var  discountPrice = currentTr.find("input[cname='discountPrice']").val();//折后价
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	
	if(price =="" || isNaN(price)){
		 alertMsg.error("请输入单价!");
		 currentTr.find("input[cname='discountPrice']").val("");
		 currentTr.find("td[cname='discountPrice']").children("div").text("");
		 return;
	}
	
	if($.isNumeric(discountPrice)){
		discountPrice = round(discountPrice,4);
		currentTr.find("input[cname='discountPrice']").val(discountPrice==0?"":discountPrice);//折后价
	}
	if($.isNumeric(discountPrice) && $.isNumeric(price) && $.isNumeric(round(discountPrice/price,4))){
		 currentTr.find("input[cname='discount']").val(round(discountPrice/price,4));//折扣
	}else{
		currentTr.find("input[cname='discount']").val("");//折扣
	}
	if($.isNumeric(discountPrice) && $.isNumeric(price) && $.isNumeric(amount) && $.isNumeric(round(discountPrice*price*amount,4))){
		currentTr.find("input[cname='discountMoney']").val(round(discountPrice*amount,4));//折后金额
	}else{
		currentTr.find("input[cname='discountMoney']").val("");//折后金额
	}
	if($.isNumeric(discountPrice) && $.isNumeric(taxRate) && $.isNumeric(round(discountPrice*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxPrice']").val(round(discountPrice*(taxRate/100+1),4));//含税价
	}else{
		currentTr.find("input[cname='taxPrice']").val("");//含税价
	}
	if($.isNumeric(discountPrice) && $.isNumeric(price) && $.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(round(discountPrice*price*amount*taxRate/100,4)) && round(discountPrice*price*amount*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(discountPrice*amount*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	if($.isNumeric(discountPrice) && $.isNumeric(price) && $.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(round(discountPrice*price*amount*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxMoney']").val(round(discountPrice*amount*(taxRate/100+1),4));//税额
	}else{
		currentTr.find("input[cname='taxMoney']").val("");//税额
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	discountMoneysTotal($parent);//table合计折扣总金额
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
}
/**
 * 折后金额          (横向 ,纵向)
 * @param obj
 * @return
 */
function discountMoneyChange(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	var  discountMoney = currentTr.find("input[cname='discountMoney']").val();//折后金额
	var  price = currentTr.find("input[cname='price']").val();//单价
	var  money = currentTr.find("input[cname='money']").val();//金额
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	if((price =="" || isNaN(price)) && discountMoney!="" && discountMoney!=0){
		 alertMsg.error("请输入单价!");
		 currentTr.find("input[cname='discountMoney']").val("");
		 currentTr.find("td[cname='discountPrice']").children("div").text("");
		 return;
	}
	if($.isNumeric(discountMoney)){
		discountMoney = round(discountMoney,4);
		currentTr.find("input[cname='discountMoney']").val(discountMoney==0?"":discountMoney);//折后金额
	}
	
	if($.isNumeric(discountMoney) && $.isNumeric(money) && $.isNumeric(round(discountMoney/money,4))){
		 currentTr.find("input[cname='discount']").val(round(discountMoney/money,4));//折扣
	}else{
		currentTr.find("input[cname='discount']").val("");//折扣
	}
	
	if($.isNumeric(discountMoney) && $.isNumeric(amount) && $.isNumeric(round(discountMoney/amount,4))){
		currentTr.find("input[cname='discountPrice']").val(round(discountMoney/amount,4));//折后价
	}else{
		currentTr.find("input[cname='discountPrice']").val("");//折后价
	}
	if($.isNumeric(discountMoney) && $.isNumeric(amount)&& $.isNumeric(taxRate) && $.isNumeric(round(discountMoney/amount,4))){
		currentTr.find("input[cname='taxPrice']").val(round(discountMoney/amount*(taxRate/100+1),4));//含税单价
	}else{
		currentTr.find("input[cname='taxPrice']").val("");//含税单价
	}
	
	if($.isNumeric(discountMoney)&& $.isNumeric(taxRate) && $.isNumeric(round(discountMoney*taxRate/100,4)) && round(discountMoney*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(discountMoney*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	
	if($.isNumeric(discountMoney)&& $.isNumeric(taxRate) && $.isNumeric(round(discountMoney*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxMoney']").val(round(discountMoney*(taxRate/100+1),4));//含税金额
	}else{
		currentTr.find("input[cname='taxMoney']").val("");//含税金额
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill" ){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	discountMoneysTotal($parent);//table合计折扣总金额
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
	
	
}
/**
 * 税率            (横向 ,纵向)
 * @param obj
 * @return
 */
function taxRateChange(obj){
	var  currentTr = $(obj).parents("tr:first");//当前Tr
	var productId=currentTr.find("input[cname='productId']").val(); //当前行有没有商品ID
	if(!productId||productId==""){
		//$(obj).val("");
		$(obj).remove();
		return;
	}
	aioBillOnblurRemoveWidget(obj);
	var  discountPrice = currentTr.find("input[cname='discountPrice']").val()||currentTr.find("input[cname='price']").val();//折后单价/单价
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	if($.isNumeric(taxRate)){
		taxRate = round(taxRate,4);
		currentTr.find("input[cname='taxRate']").val(taxRate);
	}
	var  discountMoney= currentTr.find("input[cname='discountMoney']").val()||currentTr.find("input[cname='money']").val();//折后金额/金额
	if($.isNumeric(discountPrice) && $.isNumeric(taxRate) && $.isNumeric(round(parseFloat(discountPrice)+parseFloat(discountPrice*taxRate/100),4))){
		currentTr.find("input[cname='taxPrice']").val(round(parseFloat(discountPrice)+parseFloat(discountPrice*taxRate/100),4));//含税单价
	}else{
		currentTr.find("input[cname='taxPrice']").val("");//含税单价
	}
	if($.isNumeric(discountMoney) && $.isNumeric(taxRate) && $.isNumeric(round(discountMoney*taxRate/100,4)) && round(discountMoney*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(discountMoney*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	if($.isNumeric(discountMoney)&& $.isNumeric(taxRate) && $.isNumeric(round(parseFloat(discountMoney)+parseFloat(discountMoney*taxRate/100),4))){
		currentTr.find("input[cname='taxMoney']").val(round(parseFloat(discountMoney)+parseFloat(discountMoney*taxRate/100),4));//含税金额
	}else{
		currentTr.find("input[cname='taxMoney']").val("");//含税金额
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
}
/**
 * 含税单价          (横向 ,纵向)
 * @param obj
 * @return
 */
function taxPriceChange(obj){
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
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var taxPrice = currentTr.find("input[cname='taxPrice']").val();//含税单价
	var discount = currentTr.find("input[cname='discount']").val()||1;//折扣
	
	if($.isNumeric(taxPrice)){
		taxPrice = round(taxPrice,4);
		currentTr.find("input[cname='taxPrice']").val(taxPrice);
	}
	
	if($.isNumeric(taxPrice) && $.isNumeric(taxRate) && $.isNumeric(round(taxPrice/(taxRate/100+1),4))){
		currentTr.find("input[cname='discountPrice']").val(round(taxPrice/(taxRate/100+1),4));//折后单价
	}else{
		currentTr.find("input[cname='discountPrice']").val("");//折后单价
	}
	if($.isNumeric(taxPrice) && $.isNumeric(taxRate) && $.isNumeric(discount)&& $.isNumeric(round(taxPrice/(taxRate/100+1)/discount,4))){
		currentTr.find("input[cname='price']").val(round(taxPrice/(taxRate/100+1)/discount,4));//单价
	}else{
		currentTr.find("input[cname='price']").val("");//单价
	}
	if($.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(taxPrice) && $.isNumeric(round(taxPrice/(taxRate/100+1)*amount,4))) {
		currentTr.find("input[cname='discountMoney']").val(round(taxPrice/(taxRate/100+1)*amount,4));//折后金额
	}else{
		currentTr.find("input[cname='discountMoney']").val("");//折后金额
	}
	if($.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(taxPrice) && $.isNumeric(discount) && $.isNumeric(round(taxPrice/(taxRate/100+1)*amount/discount,4))) {
		currentTr.find("input[cname='money']").val(round(taxPrice/(taxRate/100+1)*amount/discount,4));//金额
	}else{
		currentTr.find("input[cname='money']").val("");//金额
	}
	
	if($.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(taxPrice) && $.isNumeric(round(taxPrice/(taxRate/100+1)*amount*taxRate/100,4)) && round(taxPrice/(taxRate/100+1)*amount*taxRate/100,4)!=0 ) {
		currentTr.find("input[cname='taxes']").val(round(taxPrice/(taxRate/100+1)*amount*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	if($.isNumeric(amount) && $.isNumeric(taxPrice) && $.isNumeric(round(amount*taxPrice,4))){
		currentTr.find("input[cname='taxMoney']").val(round(amount*taxPrice,4));//含税金额
	}else{
		currentTr.find("input[cname='taxMoney']").val("");//含税金额
	}

	if($.trim(taxPrice)=="0" || $.trim(taxPrice)==""){
		currentTr.find("input[cname='status']").val("赠品");
	}else{
		currentTr.find("input[cname='status']").val("");
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	moneysTotal($parent);//table合计总金额
	discountMoneysTotal($parent);//table合计折扣总金额
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
}
/**
 * 含税金额          (横向 ,纵向)
 * @param obj
 * @return
 */
function taxMoneyChange(obj){
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
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;//税率
	var amount = currentTr.find("input[cname='amount']").val();//数量
	var taxMoney = currentTr.find("input[cname='taxMoney']").val();//含税金额
	var discount = currentTr.find("input[cname='discount']").val()||1;//折扣
	if($.isNumeric(taxMoney)){
		taxMoney = round(taxMoney,4);
		currentTr.find("input[cname='taxMoney']").val(taxMoney);
	}
	if($.isNumeric(taxMoney) && $.isNumeric(taxRate) && $.isNumeric(round(taxMoney/(taxRate/100+1),4))){
		currentTr.find("input[cname='discountMoney']").val(round(taxMoney/(taxRate/100+1),4));//折后金额
	}else{
		currentTr.find("input[cname='discountMoney']").val("");//折后金额
	}
	if($.isNumeric(taxMoney) && $.isNumeric(taxRate) && $.isNumeric(discount) && $.isNumeric(round(taxMoney/(taxRate/100+1)/discount,4))){
		currentTr.find("input[cname='money']").val(round(taxMoney/(taxRate/100+1)/discount,4));//金额
	}else{
		currentTr.find("input[cname='money']").val("");//金额
	}
	
	if($.isNumeric(taxMoney) && $.isNumeric(taxRate) && $.isNumeric(round(taxMoney/(taxRate/100+1)*taxRate/100,4)) && round(taxMoney/(taxRate/100+1)*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(taxMoney/(taxRate/100+1)*taxRate/100,4));//税额
	}else{
		currentTr.find("input[cname='taxes']").val("");//税额
	}
	
	if($.isNumeric(taxMoney) && $.isNumeric(amount) && parseFloat(amount)!=0 && $.isNumeric(round(taxMoney/amount,4))){
		currentTr.find("input[cname='taxPrice']").val(round(taxMoney/amount,4));//含税单价
	}else{
		currentTr.find("input[cname='taxPrice']").val("");//含税单价
	}
	if($.isNumeric(taxMoney) && $.isNumeric(taxRate)&& $.isNumeric(amount)  && parseFloat(amount)!=0 && $.isNumeric(round(taxMoney/(taxRate/100+1)/amount,4))){
		currentTr.find("input[cname='discountPrice']").val(round(taxMoney/(taxRate/100+1)/amount,4));//折后单价
	}else{
		currentTr.find("input[cname='discountPrice']").val("");//折后单价
	}
	if($.isNumeric(taxMoney) && $.isNumeric(taxRate)&& $.isNumeric(discount)&& $.isNumeric(amount) && parseFloat(amount)!=0  && parseFloat(discount)!=0 && $.isNumeric(round(taxMoney/(taxRate/100+1)/discount/amount,4))){
		currentTr.find("input[cname='price']").val(round(taxMoney/(taxRate/100+1)/discount/amount,4));//折后单价
	}else{
		currentTr.find("input[cname='price']").val("");//折后单价
	}
	if($.trim(taxMoney)=="0" || $.trim(taxMoney)==""){
		currentTr.find("input[cname='status']").val("赠品");
	}else{
		currentTr.find("input[cname='status']").val("");
	}
	showCurrentTrWidgetToSpan(currentTr);
	//合计
	var $parent = navTab.getCurrentPanel();
	var whichCallBack = $("#whichCallBack",$parent).val();
	if(whichCallBack=="purchaseBarter" || whichCallBack == "sellBarter" || whichCallBack == "dismountBill"){
		$parent= currentTr.parents("div.grid:first");
		barterParamTotal($parent,whichCallBack);//合计换货参数
	}
	moneysTotal($parent);//table合计总金额
	discountMoneysTotal($parent);//table合计折扣总金额
	taxesTotal($parent);//合计税额
	taxMoneysTotal($parent);//合计含税金额
}
//------------------------------end--通用字段合计(横向   纵向)--------------------------------------



//-----------------------------------通用字段合计(横向 )--------------------------------------
/**
 * 数量，单价，折扣  (横向)
 * @param currentTr   
 * @return
 */
function rowAmountPriceDiscountChange(currentTr){
	var price=currentTr.find("input[cname='price']").val();                    //单价
	var amount = currentTr.find("input[cname='amount']").val();                //数量
	var discount = currentTr.find("input[cname='discount']").val()||1;         //折扣
	var baseUnit = currentTr.find("input[cname='baseUnit']").val();            //单位
	var taxRate = currentTr.find("input[cname='taxRate']").val()||0;           //税率
	var calculateUnit1=currentTr.find("input[cname='calculateUnit1']").val();  //单位名称1
    var calculateUnit2=currentTr.find("input[cname='calculateUnit2']").val();  //单位名称2
    var calculateUnit3=currentTr.find("input[cname='calculateUnit3']").val();  //单位名称3
    var unitRelation1=currentTr.find("input[cname='unitRelation1']").val();    //单位关系1
    var unitRelation2=currentTr.find("input[cname='unitRelation2']").val();    //单位关系2
    var unitRelation3=currentTr.find("input[cname='unitRelation3']").val();    //单位关系3
    var retailPrice = currentTr.find("input[cname='retailPrice']").val();      //零售单价
    
    //单位之间转换（辅助数量，基础数量，辅助数量1，辅助数量2）
    xsUnitChange(currentTr,amount,calculateUnit1,calculateUnit2,calculateUnit3,unitRelation1,unitRelation2,unitRelation3);

    //金额      =  单价*数量
	if($.isNumeric(price)&& $.isNumeric(amount)&& $.isNumeric(round(price*amount,4))){
		currentTr.find("input[cname='money']").val(round(price*amount,4));
	}else{
		currentTr.find("input[cname='money']").val("");
	}
	
	//折后单价     =  单价*折扣
	if($.isNumeric(price) && $.isNumeric(discount) && $.isNumeric(round(price*discount,4))){
		currentTr.find("input[cname='discountPrice']").val(round(price*discount,4));
	}else{
		currentTr.find("input[cname='discountPrice']").val("");
	}
	
	//折后金额      =  单价*折扣*数量
	if($.isNumeric(price)&& $.isNumeric(discount) && $.isNumeric(amount) && $.isNumeric(round(price*discount*amount,4))){
		currentTr.find("input[cname='discountMoney']").val(round(price*discount*amount,4));
	}else{
		currentTr.find("input[cname='discountMoney']").val("");
	}
	
	//含税单价      =   单价*折扣*(税率/100+1)
	if($.isNumeric(price) && $.isNumeric(discount) && $.isNumeric(taxRate) && $.isNumeric(round(price*discount*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxPrice']").val(round(price*discount*(taxRate/100+1),4));
	}else{
		currentTr.find("input[cname='taxPrice']").val("");
	}
	
	//税额       =  单价*折扣*数量*税率/100
	if($.isNumeric(price) && $.isNumeric(discount) && $.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(round(price*discount*amount*taxRate/100,4)) && round(price*discount*amount*taxRate/100,4)!=0){
		currentTr.find("input[cname='taxes']").val(round(price*discount*amount*taxRate/100,4));
	}else{
		currentTr.find("input[cname='taxes']").val("");
	}
	
	//含税金额      =  单价*折扣*数量*(税率/100+1)
	if($.isNumeric(price) && $.isNumeric(discount) && $.isNumeric(amount) && $.isNumeric(taxRate) && $.isNumeric(round(price*discount*amount*(taxRate/100+1),4))){
		currentTr.find("input[cname='taxMoney']").val(round(price*discount*amount*(taxRate/100+1),4));
	}else{
		currentTr.find("input[cname='taxMoney']").val("");
	}
	
	
	//零售金额      =   数量*零售单价
	if($.isNumeric(amount) && $.isNumeric(retailPrice) && $.isNumeric(round(amount*retailPrice,4))){
		currentTr.find("input[cname='retailMoney']").val(round(amount*retailPrice,4));
	}else{
		currentTr.find("input[cname='retailMoney']").val("");
	}
	
	//状态
	if($.trim(price)=="0" || $.trim(price)==""){
		currentTr.find("input[cname='status']").val("赠品");
	}else{
		currentTr.find("input[cname='status']").val("");
	}
}
//------------------------------end--通用字段合计(横向 )--------------------------------------	



//-----------------------------------通用字段合计(纵向)--------------------------------------
//-------通用字段合计----------
/**
* 总数量     单表格
* @param $parent
* @return
*/
function amountsTotal($parent){
	oneTableCount($parent,"amount","amounts");//单表格合计
}
/**
* 总数量     双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function amountsMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"amount","amounts"); //双表格合计
}
/**
* 总金额     单表格
* @param $parent
* @return
*/
function moneysTotal($parent){
	oneTableCount($parent,"money","moneys");//单表格合计
}
/**
* 总金额     双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function moneysMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"money","moneys"); //双表格合计
}
/**
* 折后总金额   单表格
* @param $parent
* @return
*/
function discountMoneysTotal($parent){
	oneTableCount($parent,"discountMoney","discountMoneys");//单表格合计
}
/**
* 折后总金额   双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function discountMoneysMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"discountMoney","discountMoneys"); //双表格合计
}
/**
* 税额      单表格
* @param $parent
* @return
*/
function taxesTotal($parent){
	oneTableCount($parent,"taxes","taxes");//单表格合计
}
/**
* 税额      双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function taxesMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"taxes","taxes"); //双表格合计
}
/**
* 含税金额    单表格
* @param $parent
* @return
*/
function taxMoneysTotal($parent){
	oneTableCount($parent,"taxMoney","taxMoneys");//单表格合计
}
/**
* 含税金额    双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function taxMoneysMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"taxMoney","taxMoneys"); //双表格合计
}
/**
* 零售金额    单表格
* @param $parent
* @return
*/
function retailMoneysTotal($parent){
	oneTableCount($parent,"retailMoney","retailMoneys");//单表格合计
}
/**
* 零售金额    双表格
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function retailMoneysMoreTotal(tbodyDiv,tfootDiv){
	twoTableCount(tbodyDiv,tfootDiv,"retailMoney","retailMoneys"); //双表格合计
}
//-----end--通用字段合计-------

//------合计公共方法(单  双表格)----------
/**
* 单表格   总合计(数量,金额,折后金额,税额,含税金额,零售金额)
* @param $parent
* @return
*/
function sellbookOrboughgTotal($parent){
	amountsTotal($parent);         //总数量
	moneysTotal($parent);          //总金额
	discountMoneysTotal($parent);  //折后总金额
	taxesTotal($parent);           //总税额
	taxMoneysTotal($parent);       //总含税金额
	retailMoneysTotal($parent);    //总零售金额
}
/**
* 双表格   总合计(数量,金额,折后金额,税额,含税金额,零售金额)
* @param tbodyDiv
* @param tfootDiv
* @return
*/
function moreTableTotal(tbodyDiv,tfootDiv){
	amountsMoreTotal(tbodyDiv,tfootDiv);        //总数量
	moneysMoreTotal(tbodyDiv,tfootDiv);         //总金额
	discountMoneysMoreTotal(tbodyDiv,tfootDiv); //折后总金额
	taxesMoreTotal(tbodyDiv,tfootDiv);          //总税额
	taxMoneysMoreTotal(tbodyDiv,tfootDiv);      //总含税金额
	retailMoneysMoreTotal(tbodyDiv,tfootDiv);   //总零售金额
}
/**
* 单表格   总合计(数量,金额,折后金额,税额,含税金额,零售金额)(优惠后金额,入库金额)  
* @param $parent
* @return
*/
function purchaseOrSellTotal($parent){
	sellbookOrboughgTotal($parent);   //单表格   总合计(数量,金额,折后金额,税额,含税金额,零售金额)
	privilegeMoneyTotal();            //优惠后金额
	putMoneysTotal($parent);          //入库金额
}
/**
* 单表格合计
* @param area        表格区域
* @param cnameAttr   要合计对象的cname
* @param countId     合计好要赋值对象ID
* @return
*/
function oneTableCount(area,cnameAttr,countId){
	var cnameAttrList = area.find("input[cname='"+cnameAttr+"']");
	var count = 0;
	cnameAttrList.each(function(){
		var amount = $(this).val();
		if(amount!="" && $.isNumeric(amount)){
			count = count+parseFloat(amount);
		}
	});
	if($.isNumeric(count) && count != 0){
		area.find("#"+countId).val(round(count,4))
	}else{
		area.find("#"+countId).val("");
	}
}
/**
* 双表格合计    这个其实可以改成单表格合计
* @param tbodyDiv
* @param tfootDiv
* @param cnameAttr
* @param countId
* @return
*/
function twoTableCount(tbodyDiv,tfootDiv,cnameAttr,countId){
	var cnameAttrList = tbodyDiv.find("input[cname='"+cnameAttr+"']");
	var count = 0;
	cnameAttrList.each(function(){
		var amount = $(this).val();
		if(amount!="" && $.isNumeric(amount)){
			count = count+parseFloat(amount);
		}
	});
	if($.isNumeric(count) && count != 0){
		tfootDiv.find("#"+countId).val(round(count,4))
	}else{
		tfootDiv.find("#"+countId).val("");
	}
}
//--end-合计公共方法(单  双表格)----------

/*-------库存盘点单   合计-----------*/
//库存盘点单   (盘点总数量,库存总数量,库存总金额,亏盈总数量,亏盈总金额,零售总金额)统计
function takeStockBillTotal($parent){
	amountsTotal($parent);                //盘点总数量
	sckAmountTotal($parent);              //库存总数量
	sckMoneyTotal($parent);               //库存总金额
	sckGainAndLossAmountsTotal($parent);  //亏盈总数量
	sckKYMoneyTotal($parent);             //亏盈总金额
	sckLSMoneyTotal($parent);             //零售总金额
}
//合计库存总数量
function sckAmountTotal($parent){
	oneTableCount($parent,"sckAmount","sckAmounts");//单表格合计
}
//合计库存总金额
function sckMoneyTotal($parent){
	oneTableCount($parent,"sckMoney","sckMoneys");//单表格合计
}
//合计库存亏盈数量
function sckGainAndLossAmountsTotal($parent){
	oneTableCount($parent,"gainAndLossAmount","gainAndLossAmounts");//单表格合计
}
//合计库存亏盈金额
function sckKYMoneyTotal($parent){
	oneTableCount($parent,"gainAndLossMoney","gainAndLossMoneys");//单表格合计
}
//合计零售金额
function sckLSMoneyTotal($parent){
	oneTableCount($parent,"sckRetailMoney","sckRetailMoneys");//单表格合计
}
/*---end---库存盘点单   合计-----------*/

/*-------未分类   合计-----------*/
/**
* 合计成本调价后金额
* @param $parent
* @return
*/
function lastMoneyTotal($parent){
	oneTableCount($parent,"lastMoney","lastMoneys");//单表格合计
}
/**
* 合计调整金额
* @param $parent
* @return
*/
function adjustMoneyTotal($parent){
	oneTableCount($parent,"adjustMoney","adjustMoneys");//单表格合计
}
/**
* 合计借方金额
* @param $parent
* @return
*/
function debitMoneyTotal($parent){
	oneTableCount($parent,"debitMoney","debitMoneys");//单表格合计
}
/**
* 合计贷方金额
* @param $parent
* @return
*/
function lendMoneyTotal($parent){
	oneTableCount($parent,"lendMoney","lendMoneys");//单表格合计
}
/**
* table结算总金额   eg：收款单
* @param $parent
* @return
*/
function settlementMmoneysTotal($parent){
	var  $money= $parent.find("input[cname='settlementAmount']");//结算总金额
	var moneySum = 0;
	$money.each(function(){
		var money = $(this).val();
		if(money!="" && !isNaN(money)){
			moneySum = moneySum+parseFloat(money);
		}
	});
	if($.isNumeric(moneySum)){
		return round(moneySum,4);
	}else{
		return 0;
	}
}
/**
* 计算出换货差额
* @author kkz
* @param $parent
* @return
*/
function gapMoneyCalculate($parent,whichCallBack){
	var inMoney = $parent.find("input#inMoney").val();//换入金额
	var outMoney = $parent.find("input#outMoney").val();//换出金额
	if(!$.isNumeric(inMoney)) inMoney = 0;
	if(!$.isNumeric(outMoney)) outMoney = 0;
	var gapMoney = 0;
	if("sellBarter"==whichCallBack){
		gapMoney = round(outMoney-inMoney,4);
	}else{
		gapMoney = round(inMoney-outMoney,4);
	}

	$parent.find("#gapMoney").val(round(gapMoney,4));
}
//table合计入库金额
function putMoneysTotal($parent){
	oneTableCount($parent,"putMoney","putMoneys");//单表格合计
}
//合计优惠后的金额
function privilegeMoneyTotal(){
	var $parent = navTab.getCurrentPanel();
	var money = $parent.find("#taxMoneys").val()||$parent.find("#discountMoneys").val()||$parent.find("#moneys").val();
	var privilege = $parent.find("#privilege").val();//优惠额
	if($.trim(privilege) ==""){
		privilege = 0;
		$parent.find("#privilege").val(0);
	}
	if($.isNumeric(money) && $.isNumeric(privilege)){
		$parent.find("#privilegeMoney").val(round(money-privilege,4));
	}else{
		$parent.find("#privilegeMoney").val(0);
	}
	
}
/**
* 换货单的优惠金额
* @author kkz
* @return
*/
function privilegeMoneyDarter(){
	var $parent = navTab.getCurrentPanel();
	var money = $parent.find("#gapMoney").val();
	var privilege = $parent.find("#privilege").val();//优惠额
	if($.trim(privilege) ==""){
		privilege = 0;
		$parent.find("#privilege").val(0);
	}
	if($.isNumeric(money) && $.isNumeric(privilege)){
		$parent.find("#privilegeMoney").val(round(money-privilege,4));
	}else{
		$parent.find("#privilegeMoney").val(0);
	}
	
}
/**
* 换货参数合计
* @return
*/
function barterParamTotal($parent,whichCallBack){
	var $page = $parent.parents("div.page:first");
	var type = $parent.find("tbody[type]").attr("type");
	
	var $amount = $parent.find("input[cname='amount']");//数量
	var amountSum = 0;
	$amount.each(function(){
		var amount = $(this).val();
		if(amount!="" && !isNaN(amount)){
			amountSum = amountSum+parseFloat(amount);
		}
	});
	
	var $money = $parent.find("input[cname='taxMoney']") || $parent.find("input[cname='discountMoney']") || $parent.find("input[cname='money']");//金额
	if($money.length==0)$money=$parent.find("input[cname='discountMoney']");
	if($money.length==0)$money=$parent.find("input[cname='money']");
	var moneySum = 0;
	$money.each(function(){
		var money = $(this).val();
		if(money!="" && !isNaN(money)){
			moneySum = moneySum+parseFloat(money);
		}
	});
	
	var amontObj = $page.find("#"+type+"Amount");
	var moneyObj = $page.find("#"+type+"Money");
	
	if($.isNumeric(amountSum)){
		amontObj.val(round(amountSum,4));//合计数量
	}else{
		amontObj.val(0);//合计数量
	}
	if($.isNumeric(moneySum)){
		moneyObj.val(round(moneySum,4));//合计金额
	}else{
		moneyObj.val(0);//合计金额
	}
	
	var privilege = $page.find("#privilege").val();//优惠金额
	var outMoney = $page.find("#outMoney").val();//换出金额
	var inMoney = $page.find("#inMoney").val();//换入金额
	
	var gapMoney = $page.find("#gapMoney");//差额
	var privilegeMoney = $page.find("#privilegeMoney");//优惠后金额
	if("sellBarter"==whichCallBack){
		gapMoney.val(round(outMoney-inMoney,4));
		privilegeMoney.val(round(outMoney-inMoney-privilege,4));
	}else{
		gapMoney.val(round(inMoney-outMoney,4));
		privilegeMoney.val(round(inMoney-outMoney-privilege,4));
	}
}
/*---end---未分类   合计-----------*/
//------------------------------end--通用字段合计(纵向)--------------------------------------	
