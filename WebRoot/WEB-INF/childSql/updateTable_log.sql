/*-------------子账套数据库结构更新日志记录--以##yyyymmdd##开头  以#@yyyymmdd@#结束 用于更新（按日期顺序升序）--------------*/


/*-------------##20150204##rq单据类型添加是否是物流单据-----------------*/
alter table `sys_billtype` add column `isFlow` INT(1) DEFAULT 0 COMMENT '是否是物流单据 0否 1是' after `isArap`;
alter table `sys_billtype` add column `isInSck` INT(1) DEFAULT 0 COMMENT '是否是入库单据 0无关 1是-1否2双向' after `isFlow`;
/*-------------#@20150204@#rq单据类型添加是否是物流单据-----------------*/


/*-------------##20150210##单据类型加显示等级-----------------*/
/*单据类型加显示等级*/
alter table sys_billtype add column `version` INT(3) DEFAULT 1 COMMENT '版本号显示等级';
/*-------------#@20150210@#修改-----------------*/


/*-------------##20150211##单据附件-----------------*/
alter table aioerp_file add column `isDraft` int(1) DEFAULT 1 COMMENT '0.过账单据 1草稿单据 ';   
/*-------------#@20150211@#单据附件-----------------*/


/*-------------##20150307##销售订单增加  单据上折扣单价  改成折后单价-----------------*/
alter table xs_sellbook_detail add column `retailPrice` decimal(20,4)  COMMENT '零售价';
alter table xs_sellbook_detail add column `retailMoney` decimal(20,4)  COMMENT '零售金额';
/*-------------#@20150307@# 销售订单增加  单据上折扣单价  改成折后单价-----------------*/


/*-------------##20150314##lzm报表保存用户保存日期-----------------*/
DROP TABLE IF EXISTS sys_user_searchdate;
CREATE TABLE sys_user_searchdate(                                              
   id INT(11) NOT NULL AUTO_INCREMENT COMMENT '用户查询日期历史记录',  
   startDate DATETIME DEFAULT NULL COMMENT '开始日期',                       
   endDate DATETIME DEFAULT NULL COMMENT '结束日期',                    
   userId INT(11) DEFAULT NULL COMMENT '针对的用户',                        
   PRIMARY KEY (id)                                                              
 ) ENGINE=INNODB DEFAULT CHARSET=utf8;
/*-------------#@20150314@#lzm报表保存用户保存日期-----------------*/



/*-------------##20150418##单据明细增加商品到期日期 -----------------*/
/*销售订单*/
alter table xs_sellbook_detail add column `produceEndDate` date  COMMENT '到期日期';
/*销售单*/
alter table xs_sell_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table xs_sell_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*销售退货单*/
alter table xs_return_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table xs_return_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*其它入库单*/
alter table cc_otherin_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_otherin_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*其它出库单*/
alter table cc_otherout_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_otherout_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*进货订单*/
alter table cg_bought_detail add column `produceEndDate` date  COMMENT '到期日期';
/*进货单*/
alter table cg_purchase_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cg_purchase_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*进货退货单*/
alter table cg_return_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cg_return_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*销售换货*/
alter table xs_barter_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table xs_barter_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*同价调拨单*/
alter table cc_parityallot_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_parityallot_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*拆装单*/
alter table cc_dismount_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_dismount_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*成本调价单*/
alter table cc_adjust_cost_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_adjust_cost_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*进货换货单*/
alter table cg_barter_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cg_barter_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*库存盘点单*/
alter table cc_takestock_detail add column `produceEndDate` date  COMMENT '到期日期';
/*报损单*/
alter table cc_breakage_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_breakage_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*报溢单*/
alter table cc_overflow_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_overflow_draft_detail add column `produceEndDate` date  COMMENT '到期日期';
/*变价调拨单*/
alter table cc_difftallot_detail add column `produceEndDate` date  COMMENT '到期日期';
alter table cc_difftallot_draft_detail add column `produceEndDate` date  COMMENT '到期日期';

/*-------------#@20150418@#end单据增加商品到期日期 -----------------*/




/*-------------##20150423##商品信息增加是否管理到期日期-----------------*/
alter table `cc_stock_init` add column `produceEndDate` date DEFAULT NULL COMMENT '到期日期' after `produceDate`;
alter table `cc_stock` add column `produceEndDate` date DEFAULT NULL COMMENT '到期日期' after `produceDate`;
/*-------------#@20150423@#商品信息增加是否管理到期日期-----------------*/


/*-------------##20150514##商品信息增加是否管理到期日期-----------------*/
alter table `cc_stock_records` add column `produceEndDate` date DEFAULT NULL COMMENT '到期日期' after `produceDate`;
alter table `cc_stock_records_draft` add column `produceEndDate` date DEFAULT NULL COMMENT '到期日期' after `produceDate`;
/*-------------#@20150514@#商品信息增加是否管理到期日期-----------------*/



/*-------------##20150520##-----------------*/
/*------------进-------------*/
/*进货单明细*/
alter table `cg_purchase_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cg_purchase_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*进货退货单明细*/
alter table `cg_return_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cg_return_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*进货换货单明细*/
alter table `cg_barter_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cg_barter_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*----------end--进-------------*/
/*------------销-------------*/
/*销售单明细*/
alter table `xs_sell_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `xs_sell_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*销售退货单明细*/
alter table `xs_return_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `xs_return_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*销售换货单明细*/
alter table `xs_barter_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `xs_barter_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*---------end---销-------------*/
/*------------存-------------*/
/*库存盘点单单明细*/
alter table `cc_takestock_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*报损单单明细*/
alter table `cc_breakage_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_breakage_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*报溢单单明细*/
alter table `cc_overflow_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_overflow_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*其它入库单单明细*/
alter table `cc_otherin_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_otherin_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*其它出库单单明细*/
alter table `cc_otherout_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_otherout_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*同价调拨单单明细*/
alter table `cc_parityallot_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_parityallot_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*变价调拨单单明细*/
alter table `cc_difftallot_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_difftallot_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*生产拆装单单明细*/
alter table `cc_dismount_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_dismount_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*成本调价单单明细*/
alter table `cc_adjust_cost_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cc_adjust_cost_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*----------end--存-------------*/
/*------------财-------------*/
/*收款单单明细*/
alter table `cw_getmoney_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_getmoney_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*付款单单明细*/
alter table `cw_paymoney_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_paymoney_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*其它收入单单明细*/
alter table `cw_otherearn_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_otherearn_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*费用单单明细*/
alter table `cw_fee_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_fee_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*内部转款单单明细*/
alter table `cw_transfer_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_transfer_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*固定资产购置单单明细*/
alter table `cw_addassets_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_addassets_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*固定资产折旧单单明细*/
alter table `cw_deprassets_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_deprassets_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*固定资产变卖单单明细*/
alter table `cw_subassets_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_subassets_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*会计凭证单明细*/
alter table `cw_accountvoucher_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_accountvoucher_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*应(收/付)增减单明细*/
alter table `cw_c_unitgetorpay_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_c_unitgetorpay_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*资金增减单明细*/
alter table `cw_c_money_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
alter table `cw_c_money_draft_detail` add column `rcwId` int(11) NULL COMMENT '红冲引用原单据ID' after `userId`;
/*----------end--财-------------*/

/*单据类型表*/
alter TABLE sys_billtype add draftDetailTableName VARCHAR(255) DEFAULT null COMMENT '对应单据明细表明' AFTER biillTableName;
alter TABLE sys_billtype add draftTableName varchar(255) DEFAULT NULL COMMENT '对应的草稿表名' AFTER biillTableName;
alter TABLE sys_billtype add billDetailTableName VARCHAR(255) DEFAULT null COMMENT '对应单据明细表明' AFTER biillTableName;
/*-------------#@20150520@#-----------------*/

/*-------------##20150528##-----------------*/
/*字段类型后面加binary关键字，以区分大小写*/
alter table sys_billcodeconfig_format MODIFY COLUMN formatCh char(50) BINARY;
alter table sys_billcodeconfig_format MODIFY COLUMN formatHg char(50) BINARY;
alter table sys_billcodeconfig_format MODIFY COLUMN formatDd char(50) BINARY;
alter table sys_billcodeconfig_format MODIFY COLUMN formatNo char(50) BINARY;
alter table sys_billcodeconfig_format MODIFY COLUMN formatKg char(50) BINARY;
/*-------------#@20150528@#-----------------*/



/*-------------##20150608##cc_stock_records与草稿表存在的差异-----------------*/ 
alter table `cc_stock_records_draft` add column `remainAmount` decimal(20,4) NULL COMMENT '库存余量     废除字段' after `billAbstract`;
alter table `cc_stock_records_draft` add column `remainMoney` decimal(20,4) NULL COMMENT '库存余量     废除字段' after `billAbstract`;
alter table `cc_stock_records_draft` add column `isRCW` int(1) NULL COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)' after `billAbstract`;
/*-------------#@20150528@#cc_stock_records与草稿表存在的差异-----------------*/


/*-------------##20150608##cc_stock增加成本金额，商品明细增加单据明细ID-----------------*/
alter table `cc_stock` add column `costMoneys` decimal(30,4) NULL COMMENT '成本总金额' after `costPrice`;
alter table `cc_stock_records` add column `detailId` int(11) NULL COMMENT '单据明细ID' after `isRCW`;
alter table `cc_stock_records_draft` add column `detailId` int(11) NULL COMMENT '单据明细ID' after `isRCW`;
/*-------------#@20150608@#cc_stock增加成本金额，商品明细增加单据明细ID-----------------*/



/*-------------##20150617##进销存做单默认单位-----------------*/
alter table `b_product_unit` add column `inDefaultUnit` int(1) NULL COMMENT '进货单据默认单位' after `barCode`;
alter table `b_product_unit` add column `stockDefaultUnit` int(1) NULL COMMENT '库存单据默认单位' after `inDefaultUnit`;
alter table `b_product_unit` add column `outDefaultUnit` int(1) NULL COMMENT '销售单据默认单位' after `stockDefaultUnit`;
/*-------------#@20150617@#进销存做单默认单位-----------------*/



