/*索引创建规范      表名_列名*/
/*查看表所有索引  show index from xs_sell_bill*/
/*删除索引 drop index xs_sell_bill_recodeDate on xs_sell_bill*/



/*-----------------------------------------基本信息 ---------------------------------------*/
/*往来单位*/
drop index b_unit_pids on b_unit;
alter table b_unit add INDEX `b_unit_pids` (`pids`);
/*仓库*/
drop index b_storage_pids on b_storage;
alter table b_storage add INDEX `b_storage_pids` (`pids`);
/*商品*/
drop index b_product_supId on b_product;
drop index b_product_pids on b_product;
alter table b_product add INDEX `b_product_supId` (`supId`);
alter table b_product add INDEX `b_product_pids` (`pids`);
/*商品单位*/
drop index b_b_product_unit_supId on b_product_unit;
drop index b_b_product_unit_pids on b_product_unit;
alter table b_product_unit add INDEX `b_product_unit_productId` (`productId`);
alter table b_product_unit add INDEX `b_product_unit_selectUnitId` (`selectUnitId`);
/*------------------------------------end--基本信息 ---------------------------------------*/


/*单据  报表列配置*/
drop index sys_bill_row_billId on sys_bill_row;
drop index fz_report_row_reportType on fz_report_row;
alter table sys_bill_row add INDEX `sys_bill_row_billId` (`billId`);
alter table fz_report_row add INDEX `fz_report_row_reportType` (`reportType`);



/*商品历史记录表*/
drop index cc_stock_records_billTypeId on cc_stock_records;
drop index cc_stock_records_productId on cc_stock_records;
drop index cc_stock_records_createTime on cc_stock_records;
alter table cc_stock_records add INDEX `cc_stock_records_billTypeId` (`billTypeId`);
alter table cc_stock_records add INDEX `cc_stock_records_productId` (`productId`);
alter table cc_stock_records add INDEX `cc_stock_records_createTime` (`createTime`);



/*销售单*/
drop index xs_sell_bill_recodeDate on xs_sell_bill;
drop index xs_return_bill_recodeDate on xs_return_bill;
drop index xs_barter_bill_recodeDate on xs_barter_bill;
alter table xs_sell_bill add INDEX `xs_sell_bill_recodeDate` (`recodeDate`);
alter table xs_return_bill add INDEX `xs_return_bill_recodeDate` (`recodeDate`);
alter table xs_barter_bill add INDEX `xs_barter_bill_recodeDate` (`recodeDate`);


/*经营历程*/
drop index bb_billhistory_billTypeId on bb_billhistory;
drop index bb_billhistory_billId on bb_billhistory;
drop index bb_billhistory_recodeDate on bb_billhistory;
alter table bb_billhistory add INDEX `bb_billhistory_billTypeId` (`billTypeId`);
alter table bb_billhistory add INDEX `bb_billhistory_billId` (`billId`);
alter table bb_billhistory add INDEX `bb_billhistory_recodeDate` (`recodeDate`);


/*财务历史记录*/
drop index cw_pay_type_billTypeId on cw_pay_type;
drop index cw_pay_type_billId on cw_pay_type;
alter table cw_pay_type add INDEX `cw_pay_type_billTypeId` (`billTypeId`);
alter table cw_pay_type add INDEX `cw_pay_type_billId` (`billId`);


/*单位应收应付历史记录*/
drop index cw_arap_records_unitId on cw_arap_records;
alter table cw_arap_records add INDEX `cw_arap_records_unitId` (`unitId`);
