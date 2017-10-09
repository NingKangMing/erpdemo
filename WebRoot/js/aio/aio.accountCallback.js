//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/*--------------------------------------------具体会计科目回调  初始化-----------------------------------------------*/
/**
 * 会计科目单据
 * @param json
 * @return
 */
function skdBillCallBack(json){
	var $parent = navTab.getCurrentPanel();
	var selectTd = $parent.find("tbody tr td").filter(".selected");
	var selectTr =  $($(selectTd).parent()[0]);
	for(var i=0;i<json.length;i++){
		var object=json[i];
	    //selectTr=filterTr(selectTr);//当前行是否有商品
	    if(selectTr.find("input[cname='accountsId']")){
	    	billClearTrData(selectTr);//单据清除行数据
	    }
	    addHiddenCurrentWidget(selectTr);//添加隐藏当前行控件对象  
	    billAccountsHiddenAndValue(selectTr,object);//单据隐藏会计科目对象并赋值
        showCurrentTrWidgetToSpan(selectTr);//让tr里面所有td中的input值在中显示出来
        selectTr= hasAddTr(selectTr);//是否增加一行tr
	}
}
/*-----------------------------------------end---具体会计科目回调  初始化--------------------------------------------*/









/*--------------------------------------------公共方法-----------------------------------------------*/
/*-----------------------------------------end---公共方法--------------------------------------------*/