//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*-------------------------------------------具体仓库回调  初始化-----------------------------------------------*/
/**
 * 仓库单据
 * @param json
 * @return
 */
function storageCallBackNotDialog(storage,$this){
	//$this  是写仓库编号回车获取当前td  与选中的td是一样
	var $parent = navTab.getCurrentPanel();
	var discount = $parent.find("#discounts").val();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	
	//两个表格时
	var $billType  = $parent.find("input#billType").val();
	if($billType){
		selectTd = $parent.find("tbody[type='"+$billType+"'] tr td").filter(".selected");
	}
	
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	
	
	//封装值
	var storageId=selectTr.find("input[cname='storageId']").val();
	if(!storageId){//动态创建仓库隐藏信息
		var selectTr=selectTd.parent();
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' class='stealth' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"].storageId' cname='storageId'/>";  //仓库Id
		str+="<input type='hidden' cname='oldStorageCode'/>";//据仓库旧编号    方便修改编号  弹出修改，还是查询 
		str+="<input type='hidden' cname='oldStorageFullName'/>";
		str+="<input type='hidden' cname='storageSupId'/>";
		lastTd.find("div").append(str);
	}
	
	
	selectTr.find("input[cname='storageId']").val(storage.id);
	selectTr.find("input[cname='storageCode']").val(storage.code);//仓库编号
	selectTr.find("input[cname='oldStorageCode']").val(storage.code);//仓库编号
	selectTr.find("input[cname='storageFullName']").val(storage.fullName);//仓库全名
	selectTr.find("input[cname='oldStorageFullName']").val(storage.fullName);//仓库全名
	selectTr.find("input[cname='storageSupId']").val(storage.supId);//仓库上级Id
	
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	
}



/**
 * 多仓库区分带回
 * @param storage
 * @param caseVal
 * @return
 */
function storageCaseCallBackNotDialog(storage,caseVal){
	//$this  是写仓库编号回车获取当前td  与选中的td是一样
	var $parent = navTab.getCurrentPanel();
	var discount = $parent.find("#discounts").val();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	
	//两个表格时
	var $billType  = $parent.find("input#billType").val();
	if($billType){
		selectTd = $parent.find("tbody[type='"+$billType+"'] tr td").filter(".selected");
	}
	
	if(selectTd.length==0){
		selectTd=$this;
	}
	var selectTr =  selectTd.parent();
	
	
	//封装值
	var storageId=selectTr.find("input[cname='"+caseVal+"StorageId']").val();
	if(!storageId){//动态创建仓库隐藏信息
		var selectTr=selectTd.parent();
		var lastTd=selectTr.find("td").last();
		var str="<input type='hidden' class='stealth' name='"+selectTr.parent().attr("preDataName")+"["+selectTr.index()+"]."+caseVal+"StorageId' cname='"+caseVal+"StorageId'/>";  //仓库Id
		str+="<input type='hidden' cname='old"+caseVal+"StorageCode'/>";//据仓库旧编号    方便修改编号  弹出修改，还是查询 
		str+="<input type='hidden' cname='old"+caseVal+"StorageFullName'/>";
		str+="<input type='hidden' cname='"+caseVal+"StorageSupId'/>";
		lastTd.find("div").append(str);
	}
	
	
	selectTr.find("input[cname='"+caseVal+"StorageId']").val(storage.id);
	selectTr.find("input[cname='"+caseVal+"StorageCode']").val(storage.code);//仓库编号
	selectTr.find("input[cname='old"+caseVal+"StorageCode']").val(storage.code);//仓库编号
	selectTr.find("input[cname='"+caseVal+"StorageFullName']").val(storage.fullName);//仓库全名
	selectTr.find("input[cname='old"+caseVal+"StorageFullName']").val(storage.fullName);//仓库全名
	selectTr.find("input[cname='"+caseVal+"StorageSupId']").val(storage.supId);//仓库上级Id
	
	showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在span中显示出来
	
}
/*----------------------------------------end---具体仓库回调  初始化--------------------------------------------*/









/*-------------------------------------------公共方法-----------------------------------------------*/
/*----------------------------------------end---公共方法--------------------------------------------*/