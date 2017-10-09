/**
 * @author ZhangHuihua@msn.com
 */
(function($){
	var _lookup = {currentGroup:"", suffix:"", $target:null, pk:"id"};
	var _util = {
		_lookupPrefix: function(key){
			var strDot = _lookup.currentGroup ? "." : "";
			return _lookup.currentGroup + strDot + key + _lookup.suffix;
		},
		lookupPk: function(key){
			return this._lookupPrefix(key);
		},
		lookupField: function(key){
			return this.lookupPk(key);
		}
	};
	
	$.extend({
		bringBackSuggest: function(args){
			var module=_lookup['$target'].attr("module");
		 
			if(module=="navTab"){
				var $box = navTab.getCurrentPanel();
			}else {
				var $box = _lookup['$target'].parents(".unitBox:first");
			}
			$box.find(":input").each(function(){
				var $input = $(this), inputName = $input.attr("name");
				
				for (var key in args) {
					var name = (_lookup.pk == key) ? _util.lookupPk(key) : _util.lookupField(key);

					if (name == inputName) {
						$input.val(args[key]);
						$input.attr("oldTxt",args[key]);
						break;
					}
				}
			});
			if(args.staffId){
				$box.find("input[name='staff.id']").val(args.staffId);
			}
            if(args.staffName){
            	$box.find("input[name='staff.name']").val(args.staffName);
			}
			if(args.departmentId){
				$box.find("input[name='department.id']").val(args.departmentId);
			}
            if(args.departmentName){
            	$box.find("input[name='department.fullName']").val(args.departmentName);
			}
            
            if(args.settlePeriod){
            	var deliveryDateObj = $box.find("#deliveryDate");
            	var now = new Date();
            	var year = now.getFullYear();
            	var month = now.getMonth();
            	var day = now.getDate();
            	day = eval(day+parseInt(args.settlePeriod));
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
            	$box.find("#deliveryDate").val(year+"-"+month+"-"+day);
			}
            
            
		},
		bringBack: function(args){
			$.bringBackSuggest(args);
			var isReload=_lookup['$target'].attr("isReload");//是否重载
			var reloadId=_lookup['$target'].attr("reloadId");//载入的ID
			
			//是否需要重新载入
			if(isReload && reloadId && args){
				//父选项卡
				var $box = _lookup['$target'].parents(".unitBox:first");
				var showNameId = _lookup['$target'].attr("showNameId");//需要显示名字的ID
				$box.find("[id='"+showNameId+"']").text(args.fullName);//赋值
				
				//重新载入列表
				var $pagerForm = $("#pagerForm", $box);
				//var formArgs = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {}
				//navTabPageBreak(formArgs, reloadId);
				notAsyncDivSearch($pagerForm,reloadId);
				$box.find("[name='"+showNameId+"']").val(args.fullName);
				
			}
			$.pdialog.closeCurrent();
			
			var backParam = _lookup['$target'].attr("backParam");//需要带回参数
			if(backParam && args){
				//当前面板
				var $panel = _lookup['$target'].parents(".unitBox:first");
				var basePath = $("#basePathId",$panel).val();//路径
				//得到允许换货金额
				if(backParam=="inAllowMoney" || backParam=="outAllowMoney"){
					var recodeTime = $("#recodeTime",$panel).val();//录单时间
					if(backParam=="inAllowMoney")  var type="in";
					if(backParam=="outAllowMoney") var type="out";
					var objId = args.id;
					var money=ajaxGetAllowMoney(basePath,objId,recodeTime,type);
					if($.isNumeric(money)){
						var lab = $("#"+backParam, $panel);
						lab.text(money);
					}
				}else if (backParam=="reloadSckAmount") {//换仓库刷新库存数量
					var objId = args.id;//仓库
					var productIds="";
					//所有行
					var allTr = $panel.find("tbody tr");
					for (var i = 0; i < allTr.length; i++) {
						var $tr=$(allTr[i]);
						var productId = $tr.find("input[cname='productId']").val();//商品ID
						if(productId!="" && productId!="undefined" && productId!=undefined){
							productIds=productIds+productId+","
							$tr.find("input[cname='sckAmount']").val("");
							$tr.find("input[cname='remainAmount']").val("");
							showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值显示出来
						}
					}
					productIds=productIds.substring(0, productIds.length-1);
					if(productIds!=""){
						var recode = ajaxGetSckAmountByStorageId(basePath,objId,productIds);
						for( var j = 0; j < recode.length; j++){
							var object=recode[j];
							for (var k = 0; k < allTr.length; k++) {
								var $tr=$(allTr[k]);
								var productId = $tr.find("input[cname='productId']").val();//商品ID
								if(productId=="" || productId=="undefined" || productId==undefined)continue;
								
								if(productId==object.productId){
									$tr.find("input[cname='sckAmount']").val(object.amount);
									$tr.find("input[cname='remainAmount']").val(object.amount);
								}
								showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值显示出来
							}
						}
						if(recode.length>0){
							inputProduceAmountBlur("",$panel);//运算
							getMinAmountByArray(allTr,$panel);//赋值最小生产数量
						}
						
					}
				}else if(backParam=="getOrderByUnit"){  //收款单  根据单位   刷出该单位单据
					var $navTab = getCurrentPanel();
					var whichCallBack = $("#whichCallBack",$navTab).val();
					var orderTobody=$("#moneyBillOrderbody",$navTab);
					var objId = args.id;
					//对应业务处理
					ajaxGetOrderByUnit(basePath,objId,orderTobody,false,whichCallBack);
				}else if(backParam=="backPrdStock"){ //成本调价单 刷新 成本单价和数量
					var objId = args.id;//仓库
					var productIds="";
					//所有行
					var allTr = $panel.find("tbody tr");
					for (var i = 0; i < allTr.length; i++) {
						var $tr=$(allTr[i]);
						var productId = $tr.find("input[cname='productId']").val();//商品ID
						if(productId!="" && productId!="undefined" && productId!=undefined){
							productIds=productIds+productId+","
							$tr.find("input[cname='sckAmount']").val("");
							$tr.find("input[cname='remainAmount']").val("");
							showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值显示出来
						}
					}
					productIds=productIds.substring(0, productIds.length-1);
					if(productIds!=""){
						var recode = ajaxGetStock(basePath,objId,productIds);
						for( var j = 0; j < recode.length; j++){
							var object=recode[j];
							for (var k = 0; k < allTr.length; k++) {
								var $tr=$(allTr[k]);
								var productId = $tr.find("input[cname='productId']").val();//商品ID
								if(productId=="" || productId=="undefined" || productId==undefined)continue;
								
								if(productId==object.productId){
									$tr.find("input[cname='amount']").val(object.amount);
									$tr.find("input[cname='price']").val(object.avgPrice);
									if($.isNumeric(object.amount*object.avgPrice)){
										$tr.find("input[cname='money']").val(object.amount*object.avgPrice);
									}else{
										$tr.find("input[cname='money']").val("");
									}
									
								}
								showCurrentTrWidgetToSpan($tr);//让tr里面所有td中的input值显示出来
								inputLastPrice($tr);
							}
						}
						
					}
				}else if(backParam=="ageAnalysisSearch"){   //账龄分析   查询窗体选择单位
					var currentAreaId = _lookup['$target'].attr("currentAreaId");//回调要处理区域
					arapAnalysisUnitBack(currentAreaId);
				}else if(backParam=="cw_arapOverGetOrPayMoney"){  //超期收付款  切换单位过滤
					var $parent= navTab.getCurrentPanel();
					var inputVal=$("#unitId",$("#pageSearch",$parent)).val();
					refreshCwArapOverGetOrPayMoney($parent,"unitId",inputVal);
				}else if(backParam=="jsOperate"){//option带回时触发js函数操作
					var $Panel = getCurrentPanel();
					var itemNum = args.itemNum;//选项
					if(itemNum){
						if($("#itemNum", $Panel).size() == 0) {
							$Panel.prepend('<input id="itemNum" name="itemNum" type="hidden" />');
						}
						$("#itemNum", $Panel).val(itemNum);
					}
					var jsFn = _lookup['$target'].attr("jsFn");
					if (!$.isFunction(jsFn)) jsFn = eval('(' + jsFn + ')');
				}
				
			}
			
			var ctarget = _lookup['$target'].attr("ctarget");//是否打开一个组件
			if(ctarget){
				var url = _lookup['$target'].attr("url");
				var cRel = _lookup['$target'].attr("cRel") || "_blank";//目标对象ID
				var fresh = eval(_lookup['$target'].attr("fresh") || "true");
				var external = eval(_lookup['$target'].attr("external") || "false");
				var title = _lookup['$target'].attr("ctitle") || _lookup['$target'].text();
				
				DWZ.debug(url);
				if (!url.isFinishedTm()) {
					alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
					return false;
				}
				if(_lookup['$target'].attr("callBefore")){
					var isOpen  = true;
					var callBefore = _lookup['$target'].attr("callBefore") ;
					if (! $.isFunction(callBefore)) callBefore = eval('(' + callBefore + ')');
					var options = new Object();
					options.url=url;
					options.checkout=true; 
					options.obj = _lookup['$target'];
					isOpen=callBefore(options);
					if(!isOpen)return false;
				}
				
				var $pagerForm = $("#pagerForm", navTab.getCurrentPanel());
			    data = $pagerForm.size()>0 ? $pagerForm.serializeArray() : {};
			    for ( var i = 0; i < data.length; i++) {
					if(data[i].name=="pageNum" || data[i].name=="numPerPage" || data[i].name=="orderField" || data[i].name=="orderDirection")
					delete data[i];
				}
			    
			    if(ctarget=="navTab"){
			    	navTab.openTab(cRel, url,{title:title, fresh:fresh, external:external,data:data});
			    }else if(ctarget="dialog"){
			    	var navTabType=_lookup['$target'].attr("navTabType");
			    	if(navTabType&&navTabType=="priceDiscountTrack"){ //连续弹多个dialog 没规律 
			    		//先弹单位  再弹商品    再刷新navtab   
			    		var which=_lookup['$target'].attr("which");
			    		if(which&&which=="unit"){
			    			_lookup['$target'].attr("lookupGroup","product");
			    			_lookup = $.extend(_lookup, {currentGroup:"product"});
			    			_lookup['$target'].attr("which","product");
			    		}else if(which&&which=="product"){
			    			url=_lookup['$target'].attr("aimUrl");
			    			cRel=_lookup['$target'].attr("aimTabId");
			    			var $parent =navTab.getCurrentPanel();
			    			var $box = $parent.find("#" + cRel);
		    				$box.ajaxUrl( {
	    						type : "POST",
	    						url : url,
	    						data : data,
	    						callback : function() {
	    							$box.find("[layoutH]").layoutH();
	    						}
	    					});
			    			_lookup['$target'].attr("lookupGroup","unit");
			    			return false;
			    		}
			    	}
			    	
			    	var aimUrl = _lookup['$target'].attr("aimUrl");
			    	var aimTitle = _lookup['$target'].attr("aimTitle");
			    	var aimTabId = _lookup['$target'].attr("aimTabId");
			    	var width2 = _lookup['$target'].attr("width2");
			    	var height2 = _lookup['$target'].attr("height2");
			    	var item={};  
					item['name'] = "btnPattern"; 
					item['value'] = _lookup['$target'].attr("btnPattern2"); 
					data.push(item); 
			    	var options = new Object();
			    	options.jsonData = data;
			    	options.aimTabId = aimTabId;
			    	options.aimUrl = aimUrl;
			    	options.aimTitle = aimTitle;
			    	options.width = width2;
			    	options.height = height2;
			    	options.type="POST";
			    	$.pdialog.open(url, "_blank", title, options);
			    }
			}
		}
	});
	
	$.fn.extend({
		lookup: function(){
			return this.each(function(){
				var $this = $(this), options = {mask:true, 
					width:$this.attr('width')||820, height:$this.attr('height')||400,
					maxable:eval($this.attr("maxable") || "true"),
					resizable:eval($this.attr("resizable") || "true"),
					type:$this.attr("type") || "GET",
					btnPattern:$this.attr("btnPattern"),
					screenType:$this.attr("screenType")
				};
				$this.click(function(event){
					_lookup = $.extend(_lookup, {
						currentGroup: $this.attr("lookupGroup") || "",
						suffix: $this.attr("suffix") || "",
						$target: $this,
						pk: $this.attr("lookupPk") || "id"
					});
					
					//连续弹多个dialog 没规律    eg:价格跟踪 新增加    中途关闭
					var navTabType=_lookup['$target'].attr("navTabType");
			    	if(navTabType&&navTabType=="priceDiscountTrack"){ //
			    		//先弹单位  再弹商品    再刷新navtab   
			    		var lookupGroup=_lookup['$target'].attr("lookupGroup");
			    		if(lookupGroup&&lookupGroup=="product"){
			    			_lookup['$target'].attr("lookupGroup","unit");
			    			_lookup = $.extend(_lookup, {currentGroup:"unit"});
			    			_lookup['$target'].attr("which","unit");
			    		}
			    	}	
			    	//end 连续弹多个dialog 没规律    eg:价格跟踪 新增加    中途关闭
			    	
			    	
					var verifyFun=$this.attr("verifyFun");//校验的函数
					var delTrs=[];//需要清空的行数组
					if(verifyFun){
						delTrs=eval('(' + verifyFun + ')');//执行校验
					}
					
					if(delTrs.length>0){
						alertMsg.confirm("已生成该仓库的数据，更换仓库需清空重新录入!", {
							okCall: function(){
								for ( var j = 0; j < delTrs.length; j++) {
									delTr=delTrs[j];
									billClearTrData(delTr);//单据清除行数据
									if(j==delTrs.length-1){
										//清空tfoot中的统计
										var tfoot=delTr.parents("div.grid").find("tfoot");
										tfoot.find("input").val("");
									}
								}
								
								
								var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
								if (!url.isFinishedTm()) {
									alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
									return false;
								}
								var isAll=$this.attr("isAll");
								options.jsonData=new Object();
								if(isAll&&isAll=="false"){
									var para=$this.prev("[lookName="+$this.attr("lookupGroup")+"]").val();//得到文本框值
									options.jsonData={term:"quick",termVal:para,isAll:isAll};
								}
								$this.attr("isAll",true);
								
								options.param = $this.attr("param");
								options.btnPattern = $this.attr("btnPattern");
								options.screenType = $this.attr("screenType");
								options.lookupgroup = $this.attr("lookupgroup");
								options.lookup = _lookup;
								$.pdialog.open(url, "_blank", $this.attr("title") || $this.attr("warn") || $this.text(), options);
								
								return false;
							}
						});
						return false;
					}
					
					var url = unescape($this.attr("href")).replaceTmById($(event.target).parents(".unitBox:first"));
					if (!url.isFinishedTm()) {
						alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
						return false;
					}
					
					var isAll=$this.attr("isAll");
					options.jsonData= new Object();
					if(isAll&&isAll=="false"){
						var para=$this.prev("[lookName="+$this.attr("lookupGroup")+"]").val();//得到文本框值
						options.jsonData={term:"quick",termVal:para,isAll:isAll};
					}
					$this.attr("isAll",true);
					
					options.param = $this.attr("param");
					options.btnPattern = $this.attr("btnPattern");
					options.lookupgroup = $this.attr("lookupgroup");
					options.lookup = _lookup;
					var opRel = $this.attr("opRel") || "_blank";
					$.pdialog.open(url, opRel, $this.attr("title") || $this.attr("warn") || $this.text(), options);
					return false;
				});
			});
		},
		multLookup: function(){
			return this.each(function(){
				var $this = $(this), args={};
				$this.click(function(event){
					var $unitBox = $this.parents(".unitBox:first");
					$unitBox.find("[name='"+$this.attr("multLookup")+"']").filter(":checked").each(function(){
						var _args = DWZ.jsonEval($(this).val());
						for (var key in _args) {
							var value = args[key] ? args[key]+"," : "";
							args[key] = value + _args[key];
						}
					});

					if ($.isEmptyObject(args)) {
						alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
						return false;
					}
					$.bringBack(args);
				});
			});
		},
		suggest: function(){
			var op = {suggest$:"#suggest", suggestShadow$: "#suggestShadow"};
			var selectedIndex = -1;
			return this.each(function(){
				var $input = $(this).attr('autocomplete', 'off').keydown(function(event){
					if (event.keyCode == DWZ.keyCode.ENTER && $(op.suggest$).is(':visible')) return false; //屏蔽回车提交
				});
				
				var suggestFields=$input.attr('suggestFields').split(",");
				
				function _show(event){
					var offset = $input.offset();
					var iTop = offset.top+this.offsetHeight;
					var $suggest = $(op.suggest$);
					if ($suggest.size() == 0) $suggest = $('<div id="suggest"></div>').appendTo($('body'));

					$suggest.css({
						left:offset.left+'px',
						top:iTop+'px'
					}).show();
					
					_lookup = $.extend(_lookup, {
						currentGroup: $input.attr("lookupGroup") || "",
						suffix: $input.attr("suffix") || "",
						$target: $input,
						pk: $input.attr("lookupPk") || "id"
					});

					var url = unescape($input.attr("suggestUrl")).replaceTmById($(event.target).parents(".unitBox:first"));
					if (!url.isFinishedTm()) {
						alertMsg.error($input.attr("warn") || DWZ.msg("alertSelectMsg"));
						return false;
					}
					
					var postData = {};
					postData[$input.attr("postField")||"inputValue"] = $input.val();

					$.ajax({
						global:false,
						type:'POST', dataType:"json", url:url, cache: false,
						data: postData,
						success: function(response){
							if (!response) return;
							var html = '';

							$.each(response, function(i){
								var liAttr = '', liLabel = '';
								
								for (var i=0; i<suggestFields.length; i++){
									var str = this[suggestFields[i]];
									if (str) {
										if (liLabel) liLabel += '-';
										liLabel += str;
									}
								}
								for (var key in this) {
									if (liAttr) liAttr += ',';
									liAttr += key+":'"+this[key]+"'";
								}
								html += '<li lookupAttrs="'+liAttr+'">' + liLabel + '</li>';
							});
							
							var $lis = $suggest.html('<ul>'+html+'</ul>').find("li");
							$lis.hoverClass("selected").click(function(){
								_select($(this));
							});
							if ($lis.size() == 1 && event.keyCode != DWZ.keyCode.BACKSPACE) {
								_select($lis.eq(0));
							} else if ($lis.size() == 0){
								var jsonStr = "";
								for (var i=0; i<suggestFields.length; i++){
									if (_util.lookupField(suggestFields[i]) == event.target.name) {
										break;
									}
									if (jsonStr) jsonStr += ',';
									jsonStr += suggestFields[i]+":''";
								}
								jsonStr = "{"+_lookup.pk+":''," + jsonStr +"}";
								$.bringBackSuggest(DWZ.jsonEval(jsonStr));
							}
						},
						error: function(){
							$suggest.html('');
						}
					});

					$(document).bind("click", _close);
					return false;
				}
				function _select($item){
					var jsonStr = "{"+ $item.attr('lookupAttrs') +"}";
					
					$.bringBackSuggest(DWZ.jsonEval(jsonStr));
				}
				function _close(){
					$(op.suggest$).html('').hide();
					selectedIndex = -1;
					$(document).unbind("click", _close);
				}
				
				$input.focus(_show).click(false).keyup(function(event){
					var $items = $(op.suggest$).find("li");
					switch(event.keyCode){
						case DWZ.keyCode.ESC:
						case DWZ.keyCode.TAB:
						case DWZ.keyCode.SHIFT:
						case DWZ.keyCode.HOME:
						case DWZ.keyCode.END:
						case DWZ.keyCode.LEFT:
						case DWZ.keyCode.RIGHT:
							break;
						case DWZ.keyCode.ENTER:
							_close();
							break;
						case DWZ.keyCode.DOWN:
							if (selectedIndex >= $items.size()-1) selectedIndex = -1;
							else selectedIndex++;
							break;
						case DWZ.keyCode.UP:
							if (selectedIndex < 0) selectedIndex = $items.size()-1;
							else selectedIndex--;
							break;
						default:
							_show(event);
					}
					$items.removeClass("selected");
					if (selectedIndex>=0) {
						var $item = $items.eq(selectedIndex).addClass("selected");
						_select($item);
					}
				});
			});
		},
		
		itemDetail: function(){
			return this.each(function(){
				var $table = $(this).css("clear","both"), $tbody = $table.find("tbody");
				var fields=[];

				$table.find("tr:first th[type]").each(function(i){
					var $th = $(this);
					var field = {
						type: $th.attr("type") || "text",
						patternDate: $th.attr("dateFmt") || "yyyy-MM-dd",
						name: $th.attr("name") || "",
						defaultVal: $th.attr("defaultVal") || "",
						size: $th.attr("size") || "12",
						enumUrl: $th.attr("enumUrl") || "",
						lookupGroup: $th.attr("lookupGroup") || "",
						lookupUrl: $th.attr("lookupUrl") || "",
						lookupPk: $th.attr("lookupPk") || "id",
						suggestUrl: $th.attr("suggestUrl"),
						suggestFields: $th.attr("suggestFields"),
						postField: $th.attr("postField") || "",
						fieldClass: $th.attr("fieldClass") || "",
						fieldAttrs: $th.attr("fieldAttrs") || ""
					};
					fields.push(field);
				});
				
				$tbody.find("a.btnDel").click(function(){
					var $btnDel = $(this);
					
					if ($btnDel.is("[href^=javascript:]")){
						$btnDel.parents("tr:first").remove();
						initSuffix($tbody);
						return false;
					}
					
					function delDbData(){
						$.ajax({
							type:'POST', dataType:"json", url:$btnDel.attr('href'), cache: false,
							success: function(){
								$btnDel.parents("tr:first").remove();
								initSuffix($tbody);
							},
							error: DWZ.ajaxError
						});
					}
					
					if ($btnDel.attr("title")){
						alertMsg.confirm($btnDel.attr("title"), {okCall: delDbData});
					} else {
						delDbData();
					}
					
					return false;
				});

				var addButTxt = $table.attr('addButton') || "Add New";
				if (addButTxt) {
					var $addBut = $('<div class="button"><div class="buttonContent"><button type="button">'+addButTxt+'</button></div></div>').insertBefore($table).find("button");
					var $rowNum = $('<input type="text" name="dwz_rowNum" class="textInput" style="margin:2px;" value="1" size="2"/>').insertBefore($table);
					
					var trTm = "";
					$addBut.click(function(){
						if (! trTm) trTm = trHtml(fields);
						var rowNum = 1;
						try{rowNum = parseInt($rowNum.val())} catch(e){}

						for (var i=0; i<rowNum; i++){
							var $tr = $(trTm);
							$tr.appendTo($tbody).initUI().find("a.btnDel").click(function(){
								$(this).parents("tr:first").remove();
								initSuffix($tbody);
								return false;
							});
						}
						initSuffix($tbody);
					});
				}
			});
			
			/**
			 * 删除时重新初始化下标
			 */
			function initSuffix($tbody) {
				$tbody.find('>tr').each(function(i){
					$(':input, a.btnLook, a.btnAttach', this).each(function(){
						var $this = $(this), name = $this.attr('name'), val = $this.val();

						if (name) $this.attr('name', name.replaceSuffix(i));
						
						var lookupGroup = $this.attr('lookupGroup');
						if (lookupGroup) {$this.attr('lookupGroup', lookupGroup.replaceSuffix(i));}
						
						var suffix = $this.attr("suffix");
						if (suffix) {$this.attr('suffix', suffix.replaceSuffix(i));}
						
						if (val && val.indexOf("#index#") >= 0) $this.val(val.replace('#index#',i+1));
					});
				});
			}
			
			function tdHtml(field){
				var html = '', suffix = '';
				
				if (field.name.endsWith("[#index#]")) suffix = "[#index#]";
				else if (field.name.endsWith("[]")) suffix = "[]";
				
				var suffixFrag = suffix ? ' suffix="' + suffix + '" ' : '';
				
				var attrFrag = '';
				if (field.fieldAttrs){
					var attrs = DWZ.jsonEval(field.fieldAttrs);
					for (var key in attrs) {
						attrFrag += key+'="'+attrs[key]+'"';
					}
				}
				switch(field.type){
					case 'del':
						html = '<a href="javascript:void(0)" class="btnDel '+ field.fieldClass + '">删除</a>';
						break;
					case 'lookup':
						var suggestFrag = '';
						if (field.suggestFields) {
							suggestFrag = 'autocomplete="off" lookupGroup="'+field.lookupGroup+'"'+suffixFrag+' suggestUrl="'+field.suggestUrl+'" suggestFields="'+field.suggestFields+'"' + ' postField="'+field.postField+'"';
						}

						html = '<input type="hidden" name="'+field.lookupGroup+'.'+field.lookupPk+suffix+'"/>'
							+ '<input type="text" name="'+field.name+'"'+suggestFrag+' lookupPk="'+field.lookupPk+'" size="'+field.size+'" class="'+field.fieldClass+'"/>'
							+ '<a class="btnLook" href="'+field.lookupUrl+'" lookupGroup="'+field.lookupGroup+'" '+suggestFrag+' lookupPk="'+field.lookupPk+'" title="查找带回">查找带回</a>';
						break;
					case 'attach':
						html = '<input type="hidden" name="'+field.lookupGroup+'.'+field.lookupPk+suffix+'"/>'
							+ '<input type="text" name="'+field.name+'" size="'+field.size+'" readonly="readonly" class="'+field.fieldClass+'"/>'
							+ '<a class="btnAttach" href="'+field.lookupUrl+'" lookupGroup="'+field.lookupGroup+'" '+suffixFrag+' lookupPk="'+field.lookupPk+'" width="560" height="300" title="查找带回">查找带回</a>';
						break;
					case 'enum':
						$.ajax({
							type:"POST", dataType:"html", async: false,
							url:field.enumUrl, 
							data:{inputName:field.name}, 
							success:function(response){
								html = response;
							}
						});
						break;
					case 'date':
						html = '<input type="text" name="'+field.name+'" value="'+field.defaultVal+'" class="date '+field.fieldClass+'" dateFmt="'+field.patternDate+'" size="'+field.size+'"/>'
							+'<a class="inputDateButton" href="javascript:void(0)">选择</a>';
						break;
					default:
						html = '<input type="'+field.type+'" name="'+field.name+'" value="'+field.defaultVal+'" size="'+field.size+'" class="'+field.fieldClass+'" '+attrFrag+'/>';
						break;
				}
				return '<td>'+html+'</td>';
			}
			function trHtml(fields){
				var html = '';
				$(fields).each(function(){
					html += tdHtml(this);
				});
				return '<tr class="unitBox">'+html+'</tr>';
			}
		},
		
		selectedTodo: function(){
			
			function _getIds(selectedIds, targetType){
				var ids = "";
				var $box = targetType == "dialog" ? $.pdialog.getCurrent() : navTab.getCurrentPanel();
				$box.find("input:checked").filter("[name='"+selectedIds+"']").each(function(i){
					var val = $(this).val();
					ids += i==0 ? val : ","+val;
				});
				return ids;
			}
			return this.each(function(){
				var $this = $(this);
				var selectedIds = $this.attr("rel") || "ids";
				var postType = $this.attr("postType") || "map";

				$this.click(function(){
					var targetType = $this.attr("targetType");
					var ids = _getIds(selectedIds, targetType);
					if (!ids) {
						alertMsg.error($this.attr("warn") || DWZ.msg("alertSelectMsg"));
						return false;
					}
					
					var _callback = $this.attr("callback") || (targetType == "dialog" ? dialogAjaxDone : navTabAjaxDone);
					if (! $.isFunction(_callback)) _callback = eval('(' + _callback + ')');
					
					function _doPost(){
						$.ajax({
							type:'POST', url:$this.attr('href'), dataType:'json', cache: false,
							data: function(){
								if (postType == 'map'){
									return $.map(ids.split(','), function(val, i) {
										return {name: selectedIds, value: val};
									})
								} else {
									var _data = {};
									_data[selectedIds] = ids;
									return _data;
								}
							}(),
							success: _callback,
							error: DWZ.ajaxError
						});
					}
					var title = $this.attr("title");
					if (title) {
						alertMsg.confirm(title, {okCall: _doPost});
					} else {
						_doPost();
					}
					return false;
				});
				
			});
		}
	});
})(jQuery);

