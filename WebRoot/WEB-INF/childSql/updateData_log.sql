/*-------------子账套数据更新日志记录--以##yyyymmdd##开头  以#@yyyymmdd@#结束 用于更新（按日期顺序升序）--------------*/


/*-------------##20150202##rq单位应收应付添加初始化数据-----------------*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位简名','smallName','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位简名' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位拼音码','spell','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位拼音码' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位地址','address','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位地址' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位电话','phone','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位电话' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '电子邮件','email','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='电子邮件' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '联系人一','contact1','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='联系人一' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '手机一','mobile1','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='手机一' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '联系人二','contact2','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='联系人二' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '手机二','mobile2','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='手机二' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '默认经手人','staffFullName2','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='默认经手人' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '开户银行','bank','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='开户银行' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '银行账号','bankAccount','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='银行账号' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '邮政编码','zipCode','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='邮政编码' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '传真','fax','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='传真' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税号','tariff','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税号' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '地区全名','areaFullName2','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='地区全名' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '适用价格','fitPrice','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='适用价格' and `reportType`='cw520');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '状态','status','cw520',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='状态' and `reportType`='cw520');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'cw520';
/*-------------#@20150202@#rq单位应收应付添加初始化数据-----------------*/


/*-------------##20150203##lzm增加单据图片初始化数据-----------------*/
/*销售订单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,2 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=2);
/*销售单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,4 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=4);
/*销售退货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,7 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=7);
/*其它入库单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,10 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=10);
/*其它出库单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,11 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=11);
/*进货订单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,1 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=1);
/*进货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,5 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=5);
/*进货退货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,6 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=6);
/*销售换货*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,13 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=13);
/*同价调拨单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,14 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=14);
/*拆装单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,16 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=16);
/*成本调价单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,20 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=20);
/*进货换货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,12 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=12);
/*库存盘点单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,3 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=3);
/*报损单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,8 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=8);
/*报溢单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,9 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=9);
/*变价调拨单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '商品图片','商品图片','savePath',1,50,100,15 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品图片' and `billId`=15);
/*-------------#@20150203@#lzm增加单据图片初始化数据-----------------*/


/*-------------##20150204##rq单据类型添加是否是物流单据-----------------*/
UPDATE sys_billtype SET isFlow=1 WHERE id IN (6,8,4,9,14,15,5,7,20,10,11);
UPDATE sys_billtype SET isInSck=1 WHERE id IN (5,7,9,10);
UPDATE sys_billtype SET isInSck=-1 WHERE id IN (4,6,8,11);
UPDATE sys_billtype SET isInSck=2 WHERE id IN (12,13,14,15,16);
/*-------------#@20150204@#rq单据类型添加是否是物流单据-----------------*/


/*-------------##20150206##rq修改期初单据列配置-----------------*/
DELETE FROM sys_bill_row WHERE billId=3 AND CODE='rlPrice' OR CODE='rlMoney';
UPDATE sys_bill_row SET STATUS = 2,isSys = 1 WHERE billId=8 AND NAME='单位';
UPDATE sys_bill_row SET STATUS = 2,isSys = 1 WHERE billId=9 AND NAME='单位';
UPDATE sys_bill_row SET STATUS = 2,isSys = 1 WHERE billId=15 AND NAME='单位';
UPDATE sys_bill_row SET STATUS = 2,linkage = '2' WHERE billId=15 AND (CODE='retailPrice' OR CODE='retailMoney');
/*-------------#@20150206@#rq修改期初单据列配置-----------------*/


/*-------------##20150210##单据类型加显示等级-----------------*/
/*单据类型加显示等级*/
update sys_billtype set version = 2
where id >=17 and id<=34 and id!=20;
/*更新成本调价单权限参数*/


/**-修改期初单据列配置-----------------*/
DELETE FROM sys_bill_row WHERE billId=9 AND CODE = 'status';
/*-------------#@20150210@#修改-----------------*/


/*-------------##20150307##销售订单增加  单据上折扣单价  改成折后单价-----------------*/
insert  into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`linkage`,`billId`)
select '零售价','零售价','retailPrice',2,211,100,'2',2 from dual
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='零售价' and `billId`=2);
insert  into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`linkage`,`billId`)
select '零售金额','零售金额','retailMoney',2,212,100,'2',2 from dual
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='零售金额' and `billId`=2);
/*修改单据*/
update sys_bill_row set name='折后单价' where name='折扣单价';
update sys_bill_row set showName='折后单价' where name='折扣单价' and showName='折扣单价';
update sys_bill_row set name='折后金额 ' where name='折扣金额 ';
update sys_bill_row set showName='折后金额 ' where name='折扣金额 ' and showName='折扣金额 ';
/*-------------#@20150307@# 销售订单增加  单据上折扣单价  改成折后单价-----------------*/


/*-------------##20150317##rq盘点 报损 报溢 单添加条码字段-----------------*/
INSERT INTO sys_bill_row(NAME,CODE,STATUS,billId) 
SELECT '条码','barCode',1,3 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`billId` FROM `sys_bill_row` WHERE NAME='条码' AND billId=3);
INSERT INTO sys_bill_row(NAME,CODE,STATUS,billId) 
SELECT '条码','barCode',1,8 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`billId` FROM `sys_bill_row` WHERE NAME='条码' AND billId=8);
INSERT INTO sys_bill_row(NAME,CODE,STATUS,billId) 
SELECT '条码','barCode',1,9 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`billId` FROM `sys_bill_row` WHERE NAME='条码' AND billId=9);
UPDATE sys_bill_row SET showName = NAME , rank =id , width = 100 WHERE  (billId = 3 OR billId = 8 OR billId = 9);
/*-------------#@20150317@#rq盘点 报损 报溢 单添加条码字段-----------------*/


/*-------------##20150321##lzm全能进销存增加折装单-----------------*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产入库数量','dismountInAmount','cc514',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产入库数量' and `reportType`='cc514');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产入库金额','dismountInMoney','cc514',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产入库金额' and `reportType`='cc514');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产出库数量','dismountOutAmount','cc514',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产出库数量' and `reportType`='cc514');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产出库金额','dismountOutMoney','cc514',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产出库金额' and `reportType`='cc514');
UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'cc514';
/*-------------#@20150321@#rqlzm全能进销存增加折装单-----------------*/



/*-------------##20150326##lzm销售单增加商品备注-----------------*/
INSERT  INTO `sys_bill_row`(`name`, `showName`, `code`, `billId`, `status`, `rank`, `width`, `userId`, `isSys`, `linkage`) 
select '商品备注','商品备注','proMemo','4','1','1351','100',NULL,'0',NULL from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='商品备注' and `billId`='4');
/*-------------#@20150326@#rqlzm销售单增加商品备注-----------------*/



/*-------------##20150408##修改 进货退货差价  科目  不是往来对账 -----------------*/
UPDATE cw_pay_type SET hasAccount = 0 WHERE accountId = 27;
/*-------------#@20150408@#end修改 进货退货差价  科目  不是往来对账-----------------*/


/*-------------##20150411##修改单据万能查询联动 -----------------*/
UPDATE sys_billtype SET itemNum = '' WHERE itemNum IS NULL;
/*-------------#@20150411@#end修改单据万能查询联动-----------------*/



/*-------------##20150418##单据增加商品失效日期 -----------------*/
/*销售订单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,2 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=2);
/*销售单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,4 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=4);
/*销售退货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,7 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=7);
/*其它入库单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,10 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=10);
/*其它出库单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,11 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=11);
/*进货订单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,1 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=1);
/*进货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,5 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=5);
/*进货退货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,6 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=6);
/*销售换货*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,13 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=13);
/*同价调拨单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,14 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=14);
/*拆装单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,16 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=16);
/*成本调价单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,20 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=20);
/*进货换货单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,12 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=12);
/*库存盘点单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,3 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=3);
/*报损单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,8 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=8);
/*报溢单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,9 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=9);
/*变价调拨单*/
insert into `sys_bill_row`(`name`,`showName`,`code`,`status`,`rank`,`width`,`billId`) 
select '到期日期','到期日期','produceEndDate',1,55,100,15 from dual 
where not exists(select `name`,`billId` from `sys_bill_row` where `name`='到期日期' and `billId`=15);
/*-------------#@20150418@#end单据增加商品失效日期 -----------------*/






/*-------------##20150421##单据商品dialog列配置 -----------------*/
/*--商品多选--*/
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品编号','商品编号','product.code','zj100','2','358','80','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品编号' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品全名','商品全名','product.fullName','zj100','2','359','150','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品全名' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品简称','商品简称','product.smallName','zj100','1','360','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品简称' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品拼音','商品拼音','product.spell','zj100','1','361','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品拼音' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '规格','规格','product.standard','zj100','2','362','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='规格' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '型号','型号','product.model','zj100','2','363','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='型号' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '产地','产地','product.field','zj100','1','364','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='产地' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '单位','单位','product.calculateUnit1','zj100','2','365','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '基本数量','基本数量','samount','zj100','2','366','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='基本数量' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '辅助数量','辅助数量','product.helpUnit','zj100','1','367','100','1','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='辅助数量' and `reportType`='zj100');
INSERT  INTO `fz_report_row`(`name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '基本条码','基本条码','product.barCode1','zj100','2','368','100','2','1' from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='基本条码' and `reportType`='zj100');
/*-------------#@20150421@#end单据商品dialog列配置 -----------------*/


/*-------------##20150423##option列配置 -----------------*/
/*业务统计销售未付款改为销售未收款*/
UPDATE fz_report_row SET NAME = '销售未收款',showName='销售未收款' WHERE reportType IN ('cw524','cw525','cw526','cw527') AND NAME='销售未付款';

/*单位引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位编号','code','zj101',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位编号' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位全名','fullName','zj101',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位全名' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位简称','smallName','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位简名' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位拼音码','spell','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位拼音码' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位地址','address','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位地址' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位电话','phone','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位电话' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '电子邮件','email','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='电子邮件' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '联系人一','contact1','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='联系人一' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '手机一','mobile1','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='手机一' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '联系人二','contact2','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='联系人二' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '手机二','mobile2','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='手机二' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '默认经手人','staffFullName2','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='默认经手人' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '开户银行','bank','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='开户银行' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '银行账号','bankAccount','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='银行账号' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '邮政编码','zipCode','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='邮政编码' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '传真','fax','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='传真' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税号','tariff','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税号' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '地区全名','areaFullName2','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='地区全名' and `reportType`='zj101');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '适用价格','fitPrice','zj101',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='适用价格' and `reportType`='zj101');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'zj101';

/*仓库引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库编号','code','zj102',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库编号' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库全名','fullName','zj102',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库全名' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库简称','smallName','zj102',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库简称' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库拼音码','spell','zj102',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库拼音码' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '负责人','functionary','zj102',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='负责人' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '负责人电话 ','phone','zj102',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='负责人电话' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库地址 ','stoAddress','zj102',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库地址' and `reportType`='zj102');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '备注 ','memo','zj102',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='备注' and `reportType`='zj102');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'zj102';


/*职员引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '职员编号','code','zj103',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='职员编号' and `reportType`='zj103');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '职员全名','fullName','zj103',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='职员全名' and `reportType`='zj103');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '职员拼音码','spell','zj103',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='职员拼音码' and `reportType`='zj103');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '电话','phone','zj103',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='电话' and `reportType`='zj103');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '职务','role','zj103',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='职务' and `reportType`='zj103');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'zj103';


/*部门引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '部门编号','code','zj104',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='部门编号' and `reportType`='zj104');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '部门全名','fullName','zj104',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='部门全名' and `reportType`='zj104');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '部门拼音码','spell','zj104',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='部门拼音码' and `reportType`='zj104');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '备注','memo','zj104',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='备注' and `reportType`='zj104');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'zj104';

/*进货订单引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单据编号','code','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单据编号' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位编号','unit.code','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位编号' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位全称','unit.fullName','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位全称' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '录单日期','recodeDate','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='录单日期' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '交货日期','deliveryDate','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='交货日期' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '经手人编号','staff.code','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='经手人编号' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '经手人全名','staff.name','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='经手人全名' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库编号','storage.code','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库编号' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库全名','storage.fullName','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库全名' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '附加说明','memo','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='附加说明' and `reportType`='jhdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '摘要','remark','jhdd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='摘要' and `reportType`='jhdd');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'jhdd';

/*进货订单明细引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品编号','code','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品编号' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品全名','fullName','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品全名' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品简称','smallName','jhddyy',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品简称' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品拼音码','spell','jhddyy',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品拼音码' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品规格','standard','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品规格' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品型号','model','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品型号' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品产地','field','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品产地' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品图片','savePath','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品图片' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '数量','conUntreatedAmount','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='数量' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位','unit','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单价','price','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单价' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '金额','money','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='金额' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '折扣','discount','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='折扣' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税率','taxRate','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税率' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税额','taxes','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税额' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产日期','produceDate','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产日期' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '批号','batch','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='批号' and `reportType`='jhddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '备注','memo','jhddyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='备注' and `reportType`='jhddyy');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'jhddyy';

/*进货单引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单据编号','code','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单据编号' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位编号','unit.code','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位编号' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位全称','unit.fullName','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位全称' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '录单日期','recodeDate','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='录单日期' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '经手人编号','staff.code','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='经手人编号' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '经手人全名','staff.name','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='经手人全名' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '部门编号','department.code','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='部门编号' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '部门全名','department.fullName','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='部门全名' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库编号','storage.code','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库编号' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '仓库全名','storage.fullName','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='仓库全名' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '附加说明','memo','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='附加说明' and `reportType`='jhd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '摘要','remark','jhd',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='摘要' and `reportType`='jhd');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'jhd';


/*进货单明细引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品编号','code','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品编号' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品全名','fullName','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品全名' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品简称','smallName','jhdyy',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品简称' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品拼音码','spell','jhdyy',1 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品拼音码' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品规格','standard','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品规格' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品型号','model','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品型号' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品产地','field','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品产地' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '商品图片','savePath','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='商品图片' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '数量','conUntreatedAmount','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='数量' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单位','unit','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单位' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '单价','price','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='单价' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '金额','money','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='金额' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '折扣','discount','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='折扣' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税率','taxRate','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税率' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '税额','taxes','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='税额' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '生产日期','produceDate','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='生产日期' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '批号','batch','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='批号' and `reportType`='jhdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
select '备注','memo','jhdyy',2 from dual 
where not exists(select `name`,`reportType` from `fz_report_row` where `name`='备注' and `reportType`='jhdyy');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'jhdyy';


/*销售订单引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单据编号','bill.code','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单据编号' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位编号','unit.code','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位编号' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位全称','unit.fullName','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位全称' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '录单日期','bill.recodeDate','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='录单日期' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '交货日期','bill.deliveryDate','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='交货日期' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '经手人编号','staff.code','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='经手人编号' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '经手人全名','staff.name','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='经手人全名' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '仓库编号','storage.code','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='仓库编号' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '仓库全名','storage.fullName','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='仓库全名' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '附加说明','bill.memo','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='附加说明' AND `reportType`='xsdd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '摘要','bill.remark','xsdd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='摘要' AND `reportType`='xsdd');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'xsdd';


/*销售订单明细引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品编号','code','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品编号' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品全名','fullName','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品全名' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品简称','smallName','xsddyy',1 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品简称' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品拼音码','spell','xsddyy',1 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品拼音码' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品规格','standard','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品规格' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品型号','model','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品型号' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品产地','field','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品产地' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品图片','savePath','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品图片' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '数量','untreatedAmount','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='数量' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位','unit','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单价','basePrice','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单价' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '金额','money','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='金额' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '折扣','discount','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='折扣' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '税率','taxRate','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='税率' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '税额','taxes','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='税额' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '生产日期','produceDate','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='生产日期' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '批号','batch','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='批号' AND `reportType`='xsddyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '备注','memo','xsddyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='备注' AND `reportType`='xsddyy');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'xsddyy';


/*销售单引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单据编号','bill.code','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单据编号' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位编号','unit.code','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位编号' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位全称','unit.fullName','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位全称' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '录单日期','bill.recodeDate','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='录单日期' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '收款日期','bill.deliveryDate','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='交货日期' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '经手人编号','staff.code','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='经手人编号' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '经手人全名','staff.name','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='经手人全名' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '仓库编号','storage.code','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='仓库编号' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '仓库全名','storage.fullName','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='仓库全名' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '附加说明','bill.memo','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='附加说明' AND `reportType`='xsd');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '摘要','bill.remark','xsd',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='摘要' AND `reportType`='xsd');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'xsd';


/*销售单明细引用*/
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品编号','code','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品编号' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品全名','fullName','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品全名' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品简称','smallName','xsdyy',1 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品简称' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品拼音码','spell','xsdyy',1 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品拼音码' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品规格','standard','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品规格' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品型号','model','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品型号' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品产地','field','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品产地' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '商品图片','savePath','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='商品图片' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '数量','untreatedAmount','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='数量' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单位','unit','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单位' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '单价','basePrice','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='单价' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '金额','money','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='金额' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '折扣','discount','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='折扣' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '税率','taxRate','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='税率' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '税额','taxes','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='税额' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '生产日期','produceDate','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='生产日期' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '批号','batch','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='批号' AND `reportType`='xsdyy');
INSERT  INTO `fz_report_row`(`name`,`code`,`reportType`,`status`) 
SELECT '备注','memo','xsdyy',2 FROM DUAL 
WHERE NOT EXISTS(SELECT `name`,`reportType` FROM `fz_report_row` WHERE `name`='备注' AND `reportType`='xsdyy');

UPDATE fz_report_row SET showName = NAME , rank =id , width = 100 WHERE  reportType = 'xsdyy';


/*-------------#@20150423@#end option列配置 -----------------*/

/*-------------##20150425## cdc 列配置加入【商品销售明细汇总表】表头信息------------*/
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
SELECT '单据日期','单据日期','recodeDate','xs528','2','1592','100','2','1' from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单据日期' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '单据编号','单据编号','code','xs528','2','1593','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单据编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品编号','商品编号','productCode','xs528','2','1594','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='商品编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '商品全名','商品全名','productFullName','xs528','2','1595','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='商品全名' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '职员编号','职员编号','staffCode','xs528','2','1596','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='职员编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '职员全名','职员全名','staffFullName','xs528','2','1597','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='职员全名' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '部门编号','部门编号','departmentCode','xs528','2','1598','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='部门编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '部门全名','部门全名','departmentFullName','xs528','2','1599','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='部门全名' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '仓库编号','仓库编号','storageCode','xs528','2','1600','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='仓库编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '仓库全名','仓库全名','storageFullName','xs528','2','1601','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='仓库全名' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '单位编号','单位编号','unitCode','xs528','2','1602','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单位编号' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '单位全名','单位全名','unitFullName','xs528','2','1603','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单位全名' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '销售数量','销售数量','baseAmount','xs528','2','1604','100','2','2'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='销售数量' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '辅助单位','辅助单位','assistUnit','xs528','2','1605','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='辅助单位' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '辅助数量','辅助数量','helpAmount','xs528','2','1606','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='辅助数量' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '金额','金额','money','xs528','2','1608','100','2','2'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='金额' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '折后金额','折后金额','discountMoney','xs528','2','1611','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='折后金额' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '税额','税额','taxes','xs528','2','1612','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='税额' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '含税金额','含税金额','taxMoney','xs528','2','1614','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='含税金额' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '成本金额','成本金额','costMoneys','xs528','2','1615','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='成本金额' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '备注','备注','remark','xs528','2','1616','100','1','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='备注' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '单价','单价','basePrice','xs528','2','1607','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单价' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '折扣','折扣','discount','xs528','2','1609','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='折扣' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '折后单价','折后单价','discountPrice','xs528','2','1610','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='折后单价' and `reportType`='xs528');
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
select '含税单价','含税单价','taxPrice','xs528','2','1613','100','2','1'from DUAL WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='含税单价' and `reportType`='xs528');

/*-------------#@20150425@# end 列配置加入加入【商品销售明细汇总表】表头信息------------*/



/*-------------##20150504## cdc 修改列配置信息：【商品销售明细汇总表】------------*/
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
SELECT '单据类型','单据类型','billTypeId','xs528','2','1591','200','1','1' from DUAL 
WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='单据类型' and `reportType`='xs528');
update fz_report_row set isOrder = 1 WHERE reportType = 'xs528' and `code` in('assistUnit','helpAmount');
update fz_report_row set width = 200 where reportType = 'xs528' and `code` in('recodeDate','code','productCode','productFullName');
update fz_report_row set isCount = 2 where reportType = 'xs528' AND `name` LIKE '%金额';
/*-------------#@20150504@# end 修改列配置信息：【商品销售明细汇总表】------------*/



/*-------------##20150516##------------*/
update sys_bill_row set name='含税单价',showName='含税单价' where name='含税率单价' and billId in(1,2,4,5,6,7,13);
/*-------------#@20150516@# end------------*/


/*-------------##20150520##经营历程明细账本增加过账时间------------*/
insert into `fz_report_row` ( `name`, `showName`, `code`, `reportType`, `status`, `rank`, `width`, `isOrder`, `isCount`) 
SELECT '过账时间','过账时间','sckRos.createTime','cc521','1','100','100','1','1' from DUAL 
WHERE not EXISTS (select `name`,`reportType` from `fz_report_row` where `name`='过账时间' and `reportType`='cc521');
/*-------------#@20150520@#经营历程明细账本增加过账时间 end------------*/


/*-------------##20150022##------------*/
update sys_billtype set billDetailTableName = 'cg_bought_detail' where id = 1;
update sys_billtype set billDetailTableName = 'xs_sellbook_detail' where id = 2;
update sys_billtype set billDetailTableName = 'cc_takeStock_detail' where id = 3;

update sys_billtype set billDetailTableName = 'xs_sell_detail',draftDetailTableName = 'xs_sell_draft_detail',draftTableName = 'xs_sell_draft_bill' where id = 4;
update sys_billtype set billDetailTableName = 'cg_purchase_detail',draftDetailTableName = 'cg_purchase_draft_detail',draftTableName = 'cg_purchase_draft_bill' where id = 5;
update sys_billtype set billDetailTableName = 'cg_return_detail',draftDetailTableName = 'cg_return_draft_detail',draftTableName = 'cg_return_draft_bill' where id = 6;
update sys_billtype set billDetailTableName = 'xs_return_detail',draftDetailTableName = 'xs_return_draft_detail',draftTableName = 'xs_return_draft_bill' where id = 7;
update sys_billtype set billDetailTableName = 'cc_breakage_detail',draftDetailTableName = 'cc_breakage_draft_detail',draftTableName = 'cc_breakage_draft_bill' where id = 8;
update sys_billtype set billDetailTableName = 'cc_overflow_detail',draftDetailTableName = 'cc_overflow_draft_detail',draftTableName = 'cc_overflow_draft_bill' where id = 9;
update sys_billtype set billDetailTableName = 'cc_otherin_detail',draftDetailTableName = 'cc_otherin_draft_detail',draftTableName = 'cc_otherin_draft_bill' where id = 10;
update sys_billtype set billDetailTableName = 'cc_otherout_detail',draftDetailTableName = 'cc_otherout_draft_detail',draftTableName = 'cc_otherout_draft_bill' where id = 11;
update sys_billtype set billDetailTableName = 'cg_barter_detail',draftDetailTableName = 'cg_barter_draft_detail',draftTableName = 'cg_barter_draft_bill' where id = 12;
update sys_billtype set billDetailTableName = 'xs_barter_detail',draftDetailTableName = 'xs_barter_draft_detail',draftTableName = 'xs_barter_draft_bill' where id = 13;
update sys_billtype set billDetailTableName = 'cc_parityallot_detail',draftDetailTableName = 'cc_parityallot_draft_detail',draftTableName = 'cc_parityallot_draft_bill' where id = 14;
update sys_billtype set billDetailTableName = 'cc_difftallot_detail',draftDetailTableName = 'cc_difftallot_draft_detail',draftTableName = 'cc_difftallot_draft_bill' where id = 15;
update sys_billtype set billDetailTableName = 'cc_dismount_detail',draftDetailTableName = 'cc_dismount_draft_detail',draftTableName = 'cc_dismount_draft_bill' where id = 16;
update sys_billtype set billDetailTableName = 'cw_getmoney_detail',draftDetailTableName = 'cw_getmoney_draft_detail',draftTableName = 'cw_getmoney_draft_bill' where id = 17;
update sys_billtype set billDetailTableName = 'cw_otherearn_detail',draftDetailTableName = 'cw_otherearn_draft_detail',draftTableName = 'cw_otherearn_draft_bill' where id = 18;
update sys_billtype set billDetailTableName = 'cw_paymoney_detail',draftDetailTableName = 'cw_paymoney_draft_detail',draftTableName = 'cw_paymoney_draft_bill' where id = 19;
update sys_billtype set billDetailTableName = 'cc_adjust_cost_detail',draftDetailTableName = 'cc_adjust_cost_draft_detail',draftTableName = 'cc_adjust_cost_draft_bill' where id = 20;
update sys_billtype set billDetailTableName = 'cw_transfer_detail',draftDetailTableName = 'cw_transfer_draft_detail',draftTableName = 'cw_transfer_draft_bill' where id = 21;
update sys_billtype set billDetailTableName = 'cw_fee_detail',draftDetailTableName = 'cw_fee_draft_detail',draftTableName = 'cw_fee_draft_bill' where id = 22;
update sys_billtype set billDetailTableName = 'cw_addassets_detail',draftDetailTableName = 'cw_addassets_draft_detail',draftTableName = 'cw_addassets_draft_bill' where id = 23;
update sys_billtype set billDetailTableName = 'cw_deprassets_detail',draftDetailTableName = 'cw_deprassets_draft_detail',draftTableName = 'cw_deprassets_draft_bill' where id = 24;
update sys_billtype set billDetailTableName = 'cw_subassets_detail',draftDetailTableName = 'cw_subassets_draft_detail',draftTableName = 'cw_subassets_draft_bill' where id = 25;
update sys_billtype set billDetailTableName = 'cw_c_unitgetorpay_detail',draftDetailTableName = 'cw_c_unitgetorpay_draft_detail',draftTableName = 'cw_c_unitgetorpay_draft_bill' where id = 26;
update sys_billtype set billDetailTableName = 'cw_c_unitgetorpay_detail',draftDetailTableName = 'cw_c_unitgetorpay_draft_detail',draftTableName = 'cw_c_unitgetorpay_draft_bill' where id = 27;
update sys_billtype set billDetailTableName = 'cw_c_unitgetorpay_detail',draftDetailTableName = 'cw_c_unitgetorpay_draft_detail',draftTableName = 'cw_c_unitgetorpay_draft_bill' where id = 28;
update sys_billtype set billDetailTableName = 'cw_c_unitgetorpay_detail',draftDetailTableName = 'cw_c_unitgetorpay_draft_detail',draftTableName = 'cw_c_unitgetorpay_draft_bill' where id = 29;
update sys_billtype set billDetailTableName = 'cw_c_money_detail',draftDetailTableName = 'cw_c_money_draft_detail',draftTableName = 'cw_c_money_draft_bill' where id = 30;
update sys_billtype set billDetailTableName = 'cw_c_money_detail',draftDetailTableName = 'cw_c_money_draft_detail',draftTableName = 'cw_c_money_draft_bill' where id = 31;
update sys_billtype set billDetailTableName = 'cw_accountvoucher_detail',draftDetailTableName = 'cw_accountvoucher_draft_detail',draftTableName = 'cw_accountvoucher_draft_bill' where id = 32;
/*-------------#@20150022@# end------------*/


/*-------------##20150606##更新自动锁屏时间------------*/
update aioerp_sys set value1='1000' where key1='autoSPTime';
update aioerp_sys set value1='1' where key1='codeCreateRule';
/*-------------#@20150606@#更新自动锁屏时间 end------------*/


/*-------------##20150608##cc_stock增加成本金额-----------------*/
update cc_stock set costMoneys=amount*costPrice;
/*-------------#@20150608@#cc_stock增加成本金额-----------------*/

/*-------------##20150613##商品销售明细修正报表字段取值-----------------*/
UPDATE fz_report_row SET code = 'memo' where reportType = 'xs528' AND name='备注';

update fz_report_row set  width = 150 WHERE  reportType = 'djzx500' and name='单据编号';
update fz_report_row set  width = 120 WHERE  reportType = 'djzx500' and name='录单时间';
update fz_report_row set  width = 120 WHERE  reportType = 'djzx500' and name='过账时间';
update fz_report_row set  width = 150 WHERE  reportType = 'djzx501' and name='单据编号';
update fz_report_row set  width = 120 WHERE  reportType = 'djzx501' and name='录单时间';
update fz_report_row set  width = 120 WHERE  reportType = 'djzx501' and name='存盘时间';
/*-------------#@20150613@#商品销售明细修正报表字段取值-----------------*/


/*-------------##20150616##商品销售明细修正报表字段取值-----------------*/
/*销售订单，采购订单 头表状态已完成  但实际未完成  修改回来*/
UPDATE xs_sellbook_bill b set relStatus = 1
where EXISTS (SELECT 1 FROM xs_sellbook_detail d WHERE d.billId = b.id 
HAVING SUM(IFNULL(d.arrivalAmount,0)) < b.amounts) and b.relStatus <> 3;
update cg_bought_bill b set `status` = 1 
where EXISTS (SELECT 1 from cg_bought_detail d WHERE d.billId = b.id 
HAVING SUM(IFNULL(d.arrivalAmount,0)) < b.amounts) and b.`status` <> 3;
update cg_bought_bill b set `status` = 3
WHERE memo = '[强制完成]';
/*销售订单明细强制完成  其它参数数量归位*/
update xs_sellbook_detail set arrivalAmount=baseAmount,untreatedAmount=0,replenishAmount=0 where  forceAmount>0;
/*红冲ID重新生成*/
delete from aioerp_sys where key1='reloadBillRcw';
/*-------------#@20150616@#商品销售明细修正报表字段取值-----------------*/


/*-------------##20150617##进销存做单默认单位-----------------*/
update b_product_unit set inDefaultUnit = 1 , stockDefaultUnit = 1 , outDefaultUnit = 1 where selectUnitId=1;
/*-------------#@20150617@#进销存做单默认单位-----------------*/

