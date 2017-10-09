//table上下左右操作
//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/**
 * 重新添加选中样式
 * @param rangeObj    在大范围里查要选中的对象
 * @param currentObj  要选中的对象 
 * @return
 */
function addSelectClass(rangeObj,currentObj){
	rangeObj.filter(".selected").removeClass("selected");
	$(currentObj).addClass("selected");
}
/**
 * 换行时获取坐标td对象
 * @param rows   所有的tr行
 * @param rowIndex   要获取的行号，从0
 * @param colIndex   要获取的列号，从0
 * @return
 */
function otherRowSelectCell(rows,rowIndex,colIndex){
	var obj;
	obj=$(rows.get(rowIndex)).find(">td:eq("+colIndex+")");
	return obj;
}
/**
 * stable上下左右移动
 * @param tbody         
 * @param currentTdObj  操作前的td对象
 * @param type 键盘   上 up  下down   左left   右right     回车enter
 * @return
 */
function aioKeyBoardReturnTdObj(tbody,currentTdObj,type){
	if($(tbody).find("td").filter(".selected").length>0){
		currentTdObj = $(tbody).find("td").filter(".selected");
	}
	//if("right"==type || "left"==type){
	//	currentTdObj = $(tbody).find("td").filter(".selected");
	//}
	//currentTdObj.find("input").select();
	var obj;
	var countRow;  //总共行数
	var countCol;  //总共列数
	var rows=tbody.find(">tr:visible");  //所有tr对象
	
	if($(tbody).attr("notLastTd")&&$(tbody).attr("notLastTd")=="true"){
		countCol=parseInt($(rows.get(0)).find(">td").length);  //总列  从1开始        //减1是最后一行为删除操作
	}else{
		countCol=parseInt($(rows.get(0)).find(">td").length-1);  //总列  从1开始        //减1是最后一行为删除操作
	}
	countRow=rows.length;                        //总行  从1开始
	
	var curTdsNotSelf=$(currentTdObj).siblings(); //当前行所有td
	//var curRowIndex=eval($($(curTdsNotSelf).get(0)).find(">div").text());         //当前第几行   从1开始
	var curRowIndex=$(curTdsNotSelf).parent().index()+1;         //当前第几行   从1开始
	var curColIndex=eval($(currentTdObj).index()+1);                 //当前第几列   从1开始
	var curTds=$(rows.get(eval(curRowIndex-1))).find(">td");
	
	if(type=="enter"||type=="right"){
		if(curColIndex==countCol){  //行最后一个
			if(countRow==curRowIndex){  //行与列的最后一个
				return;
			}else{
				obj=otherRowSelectCell(rows,curRowIndex,1);
			}
		}else{ 
			obj=curTds.get(curColIndex);
		}
	}else if(type=="up"){
		if(curRowIndex==1){
			return;
		}else{
			obj=otherRowSelectCell(rows,eval(curRowIndex-2),eval(curColIndex-1));
		}
	}else if(type=="down"){
		if(curRowIndex==countRow){
			return;
		}else{
			obj=otherRowSelectCell(rows,curRowIndex,eval(curColIndex-1));
		}
	}else if(type=="left"){
		if(curColIndex==2){   //列的第一个 
			if(curRowIndex==1){//第一行
				return;
			}else{  //上一行的最后一个
				obj=otherRowSelectCell(rows,eval(curRowIndex-2),eval(countCol-1));
			}
		}else{ //前一个是序号
			obj=curTds.get(eval(curColIndex-2));
		}
	}
	rows.find('td').filter(".selected").removeClass("selected");
	return obj;
}