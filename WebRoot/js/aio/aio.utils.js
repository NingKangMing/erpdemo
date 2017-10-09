//辅助方法
//请使用  shift+cltr+/ 查看方法分布（切记别乱写）

/**
 * 导航页菜单切换
 * @param aimIds
 * @param aimId
 * @return
 */
function guideMenu(aimIds,aimId){
	var $panel = navTab.getCurrentPanel();
	$('#'+aimIds+'>div').hide();
	$("[module='"+aimId+"']",$panel).show();
}
/**
 * 文本框清空了补0
 * @param thisObj
 * @return
 */
function inputNullRepZero(thisObj){
	var $obj=$(thisObj);
	var inputVal = $obj.val();
	if(inputVal==""){
		 $obj.val(0);
	}
}
/**
 * 空值转换
 * @param str
 * @return
 */
function strNullConvert(str){
	if(str==null || str==undefined ||str==NaN){
		str="";
	}
	return str;
}
/**
 * 小数四舍五入到e位
 * @param v
 * @param e
 * @return
 */
function round(v,e){
	var t=1;
	for(;e>0;t*=10,e--);
	for(;e<0;t/=10,e++);
	return Math.round(v*t)/t;
}
/**
 * JS取模
 */
function numberModel(a,b){
   if(b&&b!=""){
	   return a-(b*round(a/b));
   }else{
	   return a;
   }
}

/**
 * 文本框禁止输入特殊字符  
 * @param obj
 * @return
 */
function showKeyPress(obj) {
    var code = $(obj).attr("value");
	var txt=new RegExp("[\\`,\\~,\#,\\$,\\%,\\^,\\&,\\\\,\\?,\\|,\\<,\\>,\\{,\\},\\(,\\),\\',\\;,\\=,\"]");
	if (txt.test(code)) {
    	$(obj).val(code.replace(txt,""));
     }
}
/**
 * 把中文转成拼音,截取简写
 * @param chainId
 * @param pingYinId
 * @param smallId
 * @return
 */
function pingyinCode(chainId,pingYinId,smallId){
	$("#"+pingYinId).val(makePy($("#"+chainId).val()));
	$("#"+smallId).val(cutStr($("#"+chainId).val(),8));
}
/**
 * param str 要截取的字符串
 * param L 要截取的字节长度，注意是字节不是字符，一个汉字两个字节
 * return 截取后的字符串
 */
function cutStr(str,L){    
    var result = '',
        strlen = str.length, // 字符串长度
        chrlen = str.replace(/[^\x00-\xff]/g,'**').length; // 字节长度

    if(chrlen<=L){return str;}
    
    for(var i=0,j=0;i<strlen;i++){
        var chr = str.charAt(i);
        if(/[\x00-\xff]/.test(chr)){
            j++; // ascii码为0-255，一个字符就是一个字节的长度
        }else{
            j+=2; // ascii码为0-255以外，一个字符就是两个字节的长度
        }
        if(j<=L){ // 当加上当前字符以后，如果总字节长度小于等于L，则将当前字符真实的+在result后
            result += chr;
        }else{ // 反之则说明result已经是不拆分字符的情况下最接近L的值了，直接返回
            return result;
        }
    }
}
/**
 * 计算两个组件的值 operation运算关系       如(A+B=C)
 * @param A
 * @param operation
 * @param B
 * @param C
 * @return
 */
function countTwoModelVal(A,operation,B,C){
	var $Panel=getCurrentPanel();
	var aVal=$("#"+A,$Panel).val();
	var bVal=$("#"+B,$Panel).val();
	var $c=$("#"+C,$Panel);
	var result;
	if(operation=="add"){
		result = aVal+bVal;
	}else if (operation=="sub") {
		result = aVal-bVal;
	}else if (operation=="mul") {
		result = aVal*bVal;
	}else if (operation=="div") {
		result = aVal/bVal;
	}
	if(!isNaN(result)){
		$c.val(result);
	}
}
/**
 * 两个数字操作     type=（+  -）
 * @param firstNum
 * @param seconedNub
 * @param type
 * @return
 */	
function numberOperat(firstNum,seconedNub,type){
	firstNum=$.isNumeric(firstNum) ? parseFloat(firstNum) : 0;
	seconedNub=$.isNumeric(seconedNub) ? parseFloat(seconedNub) : 0;
	var returnNum;
	if(type=="+"){
		returnNum=firstNum+seconedNub;
	}else if(type=="-"){
		returnNum=firstNum-seconedNub;
	}
	return returnNum;
}
/**
 * 保留小数点后面多少位               eg: -12.36846,4
 * @param num
 * @param len
 * @return
 */
function numberSaveLen(num,len) {
	var returnNum=num;
	num=num+"";
	var index= num.indexOf(".");
	if(index>0){  //存在小数点
		var l=num.substring(index+1,num.length);
		if(l>len){//在于保留位数长度
			var zhenSu=num.substring(0,index);                           //小数点前面                                             eg: -12
			var xiaoshu=num.substring(index+1,index+len);                //小数点后面的保留位数                         eg:368
			var last=num.substring(index+len,index+len+1);               //小数点后面的  eg:4
			var xiaoshuw=num.substring(index+len+1,index+len+2);         //保留小数点位数 后面的第一个数字    eg:6
			if(eval(xiaoshuw>5)){
				last=parseInt(last)+1;
			}
			returnNum=zhenSu+"."+xiaoshu+""+last;
		}
	}
	return returnNum;
}
/**
 * 判断两个日期大小
 * @param startTime
 * @param endTime
 * @return
 */
function checkEndTime(startTime,endTime){  
    var start=new Date(startTime.replace("-", "/").replace("-", "/"));  
    var end=new Date(endTime.replace("-", "/").replace("-", "/"));  
    if(end<start){  
        return false;  
    }  
    return true;  
}
