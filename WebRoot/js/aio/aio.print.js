/**
 * 报表打印
 * 请使用  shift+cltr+/ 查看方法分布（切记别乱写）
 */
//(function($) {
	//装截数据
	function loadPrintData(printData){
		var ReportNo = printData.reportNo;
		var ReportName = printData.reportName;
		var HeadData = printData.headData;
		var EndData = ["",""];
		var RowData = printData.rowData;
		var ColTitle = printData.colTitle;
		var ColWidth = printData.colWidth;
		
		
		var navTabId = printData.navTabId;
		var callbackType = printData.callbackType;
		var isClose = printData.isClose;

		var result = pwData.WriteData(ReportNo, ReportName, JSON.stringify(HeadData), JSON.stringify(EndData), JSON.stringify(ColWidth), JSON.stringify(ColTitle), JSON.stringify(RowData));	
        switch (result) {
            case 1:
            	alertMsg.warn("系统初始化错误");
                break;
            case 2:
            	alertMsg.warn("传入参数错误");
                break;
            case 3:
            	alertMsg.warn("列数错误");
                break;
            case 4:
            	alertMsg.warn("创建打印表错误");
                break;
            case 5:
            	alertMsg.warn("打开表格错误");
                break;
            case 10:
            	alertMsg.warn("发生未知错误");
                break;
            case 0:
                //无错误
            	if(navTabId!="undefined" && navTabId!=undefined){
	            	if(callbackType=="closeCurrent"){
	            		setTimeout(function(){navTab.closeCurrentTab(navTabId,false);}, 100);
	            	}
	            	if(navTabId!="undefined"){
	            		if(isClose!="undefined"){
	                		navTab.closeCurrentTab(navTabId,false);
	            			return false;
	                	}
	            		navTab.reloadFlag(navTabId);
	            	}
            	}
                break;
        }
        return result == 0;
	}

	//打印有对话框
	function print(json){
		if(loadPrintData(json)){
			var result = pwData.Print();
			/*if(result==0){
				//单据累计打印次数
				var billData=msg["billData"];
				var tableName=billData["table"];
				var base=billData["base"];
				var id=billData["id"];
				$.ajax({
					type:'POST',
					url:base+"/common/bill/addPrintCount",
					dataType:"json",
					data:{tableName:tableName,id:id},
					cache: false,
					success: function(msg){
						//请求成功
				    }
				});
			}*/
		}
	}
	
	
	/**
	 * 打印之前过账保存
	 */
	function beforePrintSave(json){
		var printType = json.printType;
		if(json.postForm){
			alertMsg.confirm("您确定要过账并打印吗？", {
				okCall: function(){
					var postForm = json.postForm;
					var printUrl = json.printUrl;
					
					var $parent  =  getCurrentPanel();//取值区域
					var $pagerForm = $("#"+postForm, $parent);
					$pagerForm.append("<input type='hidden' name='printType' value='"+printType+"'/>");
					$pagerForm.append("<input type='hidden' name='postForm' value='"+postForm+"'/>");
					$pagerForm.append("<input type='hidden' name='printUrl' value='"+printUrl+"'/>");
					$pagerForm.submit();					
					return false;
				},
				cancelCall: function(){
					return false;
				}
			});
		}else{
			showPrintDialog(json);
		}
	}

	//打印预览
	function preview(json){
		if(loadPrintData(json)){
			pwData.Preview();
		}
	}

	//打印模板编辑
	function design(json){
		if(loadPrintData(json)){
			pwData.Design();
		}
	}

	//直接打印
	function printWithoutDialog(json){
		if(loadPrintData(json)){
			pwData.PrintWithoutDialog();
		}
	}
	
	//导出数据
	function printExport(json){
		if(loadPrintData(json)){
			pwData.ExportXLS();
		}
	}
	
	function showPrintDialog(json){
		if(loadPrintData(json)){
			var options = {};
			options.width = 152;
			options.height = 290;
			$.pdialog.open(projectBasePath+"/printDialog", "print", "打印",options);
		}
	}
	
	$.fn.extend({
		print : function() {
			return this.each(function() {
				var $this = $(this);
				//var targetType = $this.attr("targetType") || "print";
				//var $callback = "beforePrintSave";
				//if (! $.isFunction($callback)) $callback = eval('(' + $callback + ')');
				$this.click(function(event){
					var jsonData = {};
					var url = unescape($this.attr("href"));
					var rel = $this.attr('rel');
					var serialize = eval($this.attr("serialize") || "false");//是否连载父页面的数据
					if(true==serialize){
						var $parent  =  getCurrentPanel();//取值区域
						var $pagerForm = $("#"+rel, $parent);
						jsonData = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
						//获取总记录数
						var pagination = $parent.find(".pagination");
						
						if(pagination){
							var totalCount = $(pagination).attr("totalCount");
							var item={};
							item['name'] = "totalCount"; 
							item['value'] = totalCount; 
							jsonData.push(item);
						}
						var item1={};
						item1['name'] = "postForm"; 
						item1['value'] = rel; 
						jsonData.push(item1);
						var item2={};
						item2['name'] = "printType"; 
						item2['value'] = $this.attr("targetType") || "print";
						jsonData.push(item2);
						var item3={};
						item3['name'] = "printUrl"; 
						item3['value'] = url;
						jsonData.push(item3);
						
						//end获取总记录数
					}
					if(typeof(pwData)=='undefined' || typeof(pwData.WriteData)=='undefined'){
						alertMsg.warn('打印插件未安装!');
						return false;
					}
					$.ajax({
						type:'POST',
						url:url,
						dataType:"json",
						data:jsonData,
						cache: false,
						success: function (data){
						   var printType = data.printType;
						   if(printType){
							   beforePrintSave(data);
						   }else{
							   showPrintDialog(data);
						   }
					    },
						error: DWZ.ajaxError
					});
					return false;
				});
			});
		}
	});
//})(jQuery);


