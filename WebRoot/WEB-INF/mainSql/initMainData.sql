
/*超级管理员*/
insert  into `sys_user`(`id`,`username`,`password`,`isOnline`,`lastTime`,`lastIp`,`addTime`,`status`,`privs`,`staffId`,`grade`,`productPrivs`,`unitPrivs`,`storagePrivs`,`accountPrivs`) values (1,'admin','21232f297a57a5a743894a0e4a801fc3',NULL,'2014-11-13 13:48:38',NULL,'0000-00-00 00:00:00',2,'*',NULL,3,'*','*','*','*');

/*以aioerp界面为主2014_11_26*/
insert  into `sys_permission_type`(`id`,`name`,`pid`,`rank`) values (1,'全部单据',1,1),(2,'进货类单据',1,2),(3,'销售类单据',1,3),(4,'库存类单据',1,4),(5,'财务类单据',1,5),(10,'全部报表',2,NULL),(11,'进货类报表',2,NULL),(12,'销售类报表',2,NULL),(13,'库存类报表',2,NULL),(14,'往来类报表',2,NULL),(15,'财务类报表',2,NULL),(18,'全部权限',3,NULL),(19,'基本信息',3,NULL),(20,'期初建账',3,NULL),(21,'辅助功能',3,NULL),(22,'系统维护',3,NULL),(23,'单据其它权限',3,NULL),(24,'全部基本信息字段',4,NULL),(25,'科目',4,NULL),(26,'商品',4,NULL),(27,'单位',4,NULL),(28,'职员',4,NULL),(29,'仓库',4,NULL),(30,'部门',4,NULL),(31,'地区',4,NULL),(32,'单据类型',4,NULL),(33,'附加说明',4,NULL),(34,'账外库存业务类型',4,NULL),(35,'现金流量项目',4,NULL),(36,'商品信息',5,NULL),(37,'费用合计统计',5,NULL),(38,'单位信息',5,NULL),(39,'职员信息',5,NULL),(40,'仓库信息',5,NULL),(41,'明细账本',5,NULL),(42,'库存状况',5,NULL),(43,'商品明细账本',5,NULL),(44,'月结存信息表',5,NULL),(45,'月结存信息框',5,NULL),(46,'图形对比-会计科目',5,NULL),(47,'年度费用表',5,NULL),(48,'经营利润表',5,NULL),(49,'资产负债表',5,NULL),(50,'现金银行',5,NULL),(51,'其它收入统计',5,NULL),(52,'固定资产',5,NULL),(53,'年结存信息框',5,NULL),(54,'部门费用分布表',5,NULL),(55,'职员费用分布表',5,NULL),(56,'单位费用分布表',5,NULL),(57,'经营历程',5,NULL),(58,'资产负债表（树形表）',5,NULL),(59,'现金流量项目',5,NULL),(60,'部门固定资产折旧明细',5,NULL),(61,'往来对账-按商品',5,NULL),(62,'往来分析',5,NULL),(63,'账龄分析',5,NULL),(64,'期初财务数据',5,NULL),(65,'生成模板',5,NULL),(66,'商品库存批次表',5,NULL),(67,'往来对账-按单据',5,NULL),(68,'查看草稿单据',5,NULL),(69,'以下商品出现负库存',5,NULL),(70,'年度利润对比表',5,NULL),(71,'物价管理',5,NULL),(72,'年度资产负债对比表',5,NULL),(73,'年结存信息表',5,NULL);

/* 一级1到5000 (-)  二级(10000到50000)  (-)   三级(60000到70000)      用-连接    
 * eg:往来报表->应收应付->单位应收应付
 *
 * 一级：(1到5000)  1基本信息      2期初数据    3进货管理     4销售管理       5库存管理       6代财务管理        7单据中心    8辅助功能    9系统维护    10帮助中心    
 * (s代表查看),(e代表录入),(t代表过账),(p代表打印),(a代表新增),(u代表修改删除)
 *
 */
##-----------------------------------------------------------------------------------------------------
/**
1基本信息
二级菜单(20-30)
200-250
商品信息                                               1-201              {查看执行1-201-s      新增1-201-a     修改删除1-201-u     打印1-201-p}
往来单位1-21
	地区信息                                  1-21-202           {查看执行1-21-202-s   新增1-21-202-a  修改删除1-21-202-u  打印1-21-202-p}
	单位信息                                  1-21-203           {查看执行1-21-203-s   新增1-21-203-a  修改删除1-21-203-u  打印1-21-203-p}
职员信息1-22
	部门信息                                  1-22-204           {查看执行1-22-204-s   新增1-22-204-a  修改删除1-22-204-u  打印1-22-204-p}
	职员信息                                  1-22-205           {查看执行1-22-205-s   新增1-22-205-a  修改删除1-22-205-u  打印1-22-205-p}
仓库信息                                               1-206              {查看执行1-206-s      新增1-206-a     修改删除1-206-u     打印1-206-p}
银行账户                                               1-207              {查看执行1-207-s      新增1-207-a     修改删除1-207-u     打印1-207-p}
费用类型                                               1-208              {查看执行1-208-s      新增1-208-a     修改删除1-208-u     打印1-208-p}
收入类型                                               1-209              {查看执行1-209-s      新增1-209-a     修改删除1-209-u     打印1-209-p}
固定资产                                               1-210              {查看执行1-210-s      新增1-210-a     修改删除1-210-u     打印1-210-p}
会计科目                                               1-211              {查看执行1-211-s      新增1-211-a     修改删除1-211-u     打印1-211-p}
*/ 


##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('商品信息',',18,19,','1-201-s','1-201-a','1-201-u','1-201-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('地区信息',',18,19,','1-21-202-s','1-21-202-a','1-21-202-u','1-21-202-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('单位信息',',18,19,','1-21-203-s','1-21-203-a','1-21-203-u','1-21-203-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('部门信息',',18,19,','1-22-204-s','1-22-204-a','1-22-204-u','1-22-204-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('职员信息',',18,19,','1-22-205-s','1-22-205-a','1-22-205-u','1-22-205-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('仓库信息',',18,19,','1-206-s','1-206-a','1-206-u','1-206-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('银行账户',',18,19,','1-207-s','1-207-a','1-207-u','1-207-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('费用类型',',18,19,','1-208-s','1-208-a','1-208-u','1-208-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('收入类型',',18,19,','1-209-s','1-209-a','1-209-u','1-209-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('固定资产',',18,19,','1-210-s','1-210-a','1-210-u','1-210-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('会计科目',',18,19,','1-211-s','1-211-a','1-211-u','1-211-p');


##-----------------------------------------------------------------------------------------------------
/**
2期初数据
二级菜单(30-40)
250-300
期初库存商品                                      2-251              {查看执行2-251-s      修改删除2-251-u     打印2-251-p}
期初应收应付                                      2-252              {查看执行2-252-s      修改删除2-252-u     打印2-252-p}
期初现金银行                                      2-253              {查看执行2-253-s      修改删除2-253-u     打印2-253-p}
期初固定资产                                      2-254              {查看执行2-254-s      修改删除2-254-u     打印2-254-p}
期初财务数据                                      2-255              {查看执行2-255-s      修改删除2-255-u     打印2-255-p}
期初建账..开账                               2-256              {查看执行2-256-s      修改删除                                         打印                      }
*/ 

##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('期初库存商品',',18,20,','2-251-s','2-251-u','2-251-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('期初应收应付',',18,20,','2-252-s','2-252-u','2-252-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('期初现金银行',',18,20,','2-253-s','2-253-u','2-253-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('期初固定资产',',18,20,','2-254-s','2-254-u','2-254-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('期初财务数据',',18,20,','2-255-s','2-255-u','2-255-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('期初建账..开账',',18,20,','2-256-s');


##-----------------------------------------------------------------------------------------------------
/**
3进货管理
二级菜单(40-60)
300-400
进货订单			     3-301              {查看301-s   录入3-301-e   过账                           打印301-p}
进货单				     3-302              {查看302-s   录入3-302-e   过账302-t   打印302-p}
进货退货单                                                  3-303              {查看303-s   录入3-303-e   过账303-t   打印303-p}
进货换货单                                                  3-304              {查看304-s   录入3-304-e   过账304-t   打印304-p}
进货报表3-41
	商品进货统计                                3-41-305           {查看3-41-305-s      打印3-41-305-p}
	单位进货统计                                3-41-306           {查看3-41-306-s      打印3-41-306-p}
	商品进货退货统计                        3-41-307           {查看3-41-307-s      打印3-41-307-p}
	单位进货退货统计                        3-41-308           {查看3-41-308-s      打印3-41-308-p}
	进货优惠统计                                3-41-309           {查看3-41-309-s      打印3-41-309-p}
	单位进货分布                                3-41-310           {查看3-41-310-s      打印3-41-310-p}
	仓库进货分布                                3-41-311           {查看3-41-311-s      打印3-41-311-p}
进货订单报表3-42
	进货订单查询                               3-42-312           {查看3-42-312-s      打印3-42-312-p}
	进货订单统计                               3-42-313           {查看3-42-313-s      打印3-42-313-p}
	进货订单明细查询                       3-42-314           {查看3-42-314-s      打印3-42-314-p}
*/ 


##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`printUrl`) values ('进货订单',',1,2,','301-s','3-301-e','301-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('进货单',',1,2,','302-s','3-302-e','302-t','302-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('进货退货单',',1,2,','303-s','3-303-e','303-t','303-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('进货换货单',',1,2,','304-s','3-304-e','304-t','304-p');
##进货报表3-41
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品进货统计',',10,11,','3-41-305-s','3-41-305-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位进货统计',',10,11,','3-41-306-s','3-41-306-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品进货退货统计',',10,11,','3-41-307-s','3-41-307-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位进货退货统计',',10,11,','3-41-308-s','3-41-308-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('进货优惠统计',',10,11,','3-41-309-s','3-41-309-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位进货分布',',10,11,','3-41-310-s','3-41-310-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('仓库进货分布',',10,11,','3-41-311-s','3-41-311-p');
##进货订单报表3-42
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('进货订单查询',',10,11,','3-42-312-s','3-42-312-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('进货订单统计',',10,11,','3-42-313-s','3-42-313-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('进货订单明细查询',',10,11,','3-42-314-s','3-42-314-p');




##-----------------------------------------------------------------------------------------------------
/**
销售管理4
二级菜单(60-80)
400-500
   销售订单                                            4-401              {查看401-s   录入4-401-e   过账                           打印401-p}
   销售单                                                4-402              {查看402-s   录入4-402-e   过账402-t   打印402-p}
   销售退货单                                        4-403              {查看403-s   录入4-403-e   过账403-t   打印403-p}
   销售换货单                                        4-404              {查看404-s   录入4-404-e   过账404-t   打印404-p}
   销售报表4-61              
	       商品销售统计                   4-61-405           {查看4-61-405-s      打印4-61-405-p}
	       单位销售统计                   4-61-406           {查看4-61-406-s      打印4-61-406-p} 
	       商品销售明细汇总        4-61-406.5          {查看4-61-406.5-s      打印4-61-406.5-p} 
	       商品销售退货统计           4-61-407           {查看4-61-407-s      打印4-61-407-p}
	       单位销售退货统计           4-61-408           {查看4-61-408-s      打印4-61-408-p}
	       销售优惠统计                   4-61-409           {查看4-61-409-s      打印4-61-409-p}
	       单位销售分布                   4-61-410           {查看4-61-410-s      打印4-61-410-p}
	       仓库销售分布                   4-61-411           {查看4-61-411-s      打印4-61-411-p}
	       商品销售毛利表               4-61-412           {查看4-61-412-s      打印4-61-412-p}
   销售排行榜4-62
	       商品销售排行榜               4-62-413           {查看4-62-413-s      打印4-62-413-p}    
	       客户销售排行榜               4-62-414           {查看4-62-414-s      打印4-62-414-p}    
	       职员销售排行榜               4-62-415           {查看4-62-415-s      打印4-62-415-p}    
	       部门销售排行榜               4-62-416           {查看4-62-416-s      打印4-62-416-p}    
	       仓库销售排行榜               4-62-417           {查看4-62-417-s      打印4-62-417-p}    
	       地区销售排行榜               4-62-418           {查看4-62-418-s      打印4-62-418-p}    
   销售订单统计4-63                                         
	       销售订单查询                   4-63-419           {查看4-63-419-s      打印4-63-419-p}      
	       销售订单统计                   4-63-420           {查看4-63-420-s      打印4-63-420-p}      
	       销售订单明细查询           4-63-421           {查看4-63-421-s      打印4-63-421-p}      
*/ 


##sql 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`printUrl`) values ('销售订单',',1,3,','401-s','4-401-e','401-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('销售单',',1,3,','402-s','4-402-e','402-t','402-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('销售退货单',',1,3,','403-s','4-403-e','403-t','403-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('销售换货单',',1,3,','404-s','4-404-e','404-t','404-p');
##销售报表4-61
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品销售统计',',10,12,','4-61-405-s','4-61-405-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位销售统计',',10,12,','4-61-406-s','4-61-406-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品销售明细汇总',',10,12,','4-61-406.5-s','4-61-406.5-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品销售退货统计',',10,12,','4-61-407-s','4-61-407-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位销售退货统计',',10,12,','4-61-408-s','4-61-408-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('销售优惠统计',',10,12,','4-61-409-s','4-61-409-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位销售分布',',10,12,','4-61-410-s','4-61-410-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('仓库销售分布',',10,12,','4-61-411-s','4-61-411-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品销售毛利表',',10,12,','4-61-412-s','4-61-412-p');
##销售排行榜4-62
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品销售排行榜',',10,12,','4-62-413-s','4-62-413-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('客户销售排行榜',',10,12,','4-62-414-s','4-62-414-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('职员销售排行榜',',10,12,','4-62-415-s','4-62-415-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('部门销售排行榜',',10,12,','4-62-416-s','4-62-416-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('仓库销售排行榜',',10,12,','4-62-417-s','4-62-417-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('地区销售排行榜',',10,12,','4-62-418-s','4-62-418-p');
##销售订单统计4-63 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('销售订单查询',',10,12,','4-63-419-s','4-63-419-p');   
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('销售订单统计',',10,12,','4-63-420-s','4-63-420-p');   
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('销售订单明细查询',',10,12,','4-63-421-s','4-63-421-p');   
  
##-----------------------------------------------------------------------------------------------------
/**
5库存管理  
二级菜单(80-100)
500-600 
盘点管理        5-80			  
库存盘点单                                        5-80-502           {查看 5-80-502-s    	         录入 5-80-502-e         打印 5-80-502-p}
盘点处理                                            5-80-503           {查看 5-80-503-s    	         录入 5-80-503-e         打印 5-80-503-p}
报损单                                                5-80-504           {查看 5-80-504-s    	         录入 5-80-504-e     	 过账 5-80-504-t         打印 5-80-504-p}
报溢单                                                5-80-505           {查看 5-80-505-s    	         录入 5-80-505-e     	 过账 5-80-505-t         打印 5-80-505-p}

其它入库单                                        5-506              {查看5-506-s    录入5-5-506-e   过账5-506-t         打印5-506-p}
其它出库单                                        5-507              {查看5-507-s    录入5-5-507-e   过账5-507-t         打印5-507-p}
同价调拨单                                        5-508              {查看5-508-s    录入5-5-508-e   过账5-508-t         打印5-508-p}
变价调拨单                                        5-509              {查看5-509-s    录入5-509-e   	      过账5-509-t         打印5-509-p}
生产拆装单                                        5-510              {查看5-510-s    录入5-5-510-e   过账5-510-t         打印5-510-p}
成本调价单                                        5-511              {查看5-511-s   录入5-511-e   过账5-511-t   打印5-511-p}  
库存报表5-81
	库存状况                              5-81-511           {查看5-81-511-s   打印5-81-511-p}
	虚拟库存状况	      5-81-512           {查看5-81-512-s   打印5-81-512-p}
	库存状况分布表                  5-81-513           {查看5-81-513-s   打印5-81-513-p}
	商品保质期查询                  5-81-514           {查看5-81-514-s   打印5-81-514-p}
	商品批次跟踪                      5-81-515           {查看5-81-515-s   打印5-81-515-p}
	库存上下限报警设置          5-81-516           {查看5-81-516-s   打印5-81-516-p}
	库存商品上限报警              5-81-517           {查看5-81-517-s   打印5-81-517-p}
	库存商品下限报警              5-81-518           {查看5-81-518-s   打印5-81-518-p}
单据统计5-82
	其它入库单统计                  5-82-519           {查看5-82-519-s   打印5-82-519-p}
	其它出库单统计                  5-82-520           {查看5-82-520-s   打印5-82-520-p}
	报损单统计                          5-82-521           {查看5-82-521-s   打印5-82-521-p}
	报溢单统计                          5-82-522           {查看5-82-522-s   打印5-82-522-p}
	同价调拨单统计                  5-82-523           {查看5-82-523-s   打印5-82-523-p}
	变价调拨单统计                  5-82-524           {查看5-82-524-s   打印5-82-524-p}
	生产拆装单统计                  5-82-525           {查看5-82-525-s   打印5-82-525-p}
	成本调价单统计                  5-82-527           {查看5-82-527-s   打印5-82-527-p}
全能进销存变动报表	      5-526              {查看526-s        打印526-p}
*/ 
 
	
##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`printUrl`) values ('库存盘点单',',1,4,','5-80-502-s','5-80-502-e','5-80-502-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`printUrl`) values ('盘点处理',',1,4,','5-80-503-s','5-80-503-e','5-80-503-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('报损单',',1,4,','5-80-504-s','5-80-504-e','5-80-504-t','5-80-504-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('报溢单',',1,4,','5-80-505-s','5-80-505-e','5-80-505-t','5-80-505-p');

insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('其它入库单',',1,4,','5-506-s','5-506-e','5-506-t','5-506-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('其它出库单 ',',1,4,','5-507-s','5-507-e','5-507-t','5-507-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('同价调拨单',',1,4,','5-508-s','5-508-e','5-508-t','5-508-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('变价调拨单',',1,4,','5-509-s','5-509-e','5-509-t','5-509-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('生产拆装单',',1,4,','5-510-s','5-510-e','5-510-t','5-510-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('成本调价单',',1,4,','5-511-s','5-511-e','5-511-t','5-511-p');

##库存报表5-81	
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('库存状况',',10,13,','5-81-511-s','5-81-511-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('虚拟库存状况',',10,13,','5-81-512-s','5-81-512-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('库存状况分布表',',10,13,','5-81-513-s','5-81-513-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品保质期查询',',10,13,','5-81-514-s','5-81-514-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('商品批次跟踪',',10,13,','5-81-515-s','5-81-515-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('库存上下限报警设置',',10,13,','5-81-516-s','5-81-516-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('库存商品上限报警',',10,13,','5-81-517-s','5-81-517-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('库存商品下限报警',',10,13,','5-81-518-s','5-81-518-p'); 
##单据统计5-82
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('其它入库单统计',',10,13,','5-82-519-s','5-82-519-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('其它出库单统计',',10,13,','5-82-520-s','5-82-520-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('报损单统计',',10,13,','5-82-521-s','5-82-521-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('报溢单统计',',10,13,','5-82-522-s','5-82-522-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('同价调拨单统计',',10,13,','5-82-523-s','5-82-523-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('变价调拨单统计',',10,13,','5-82-524-s','5-82-524-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('生产拆装单统计',',10,13,','5-82-525-s','5-82-525-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('成本调价单统计',',10,13,','5-82-527-s','5-82-527-p'); 
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('全能进销存变动报表',',10,13,','526-s','526-p');

    
##-----------------------------------------------------------------------------------------------------
/**
6代财务管理
二级菜单(100-120)
600-700 
收款单                                                                        6-601                 {查看601-s   录入6-601-e   过账601-t   打印601-p}                                   
付款单                                                                        6-602                 {查看602-s   录入6-602-e   过账602-t   打印602-p}                                   
费用单                                                                        6-603                 {查看603-s   录入6-603-e   过账603-t   打印603-p}                                   
其它收入单                                                                6-604                 {查看604-s   录入6-604-e   过账604-t   打印604-p}                                   
内部转款单                                                                6-605                 {查看605-s   录入6-605-e   过账605-t   打印605-p}  
固定资产管理6-101
	固定资产购置                                              6-101-607              {查看607-s   录入6-101-607-e   过账607-t   打印607-p}
	固定资产折旧                                              6-101-608              {查看608-s   录入6-101-608-e   过账608-t   打印608-p}
	固定资产变卖                                              6-101-609              {查看609-s   录入6-101-609-e   过账609-t   打印609-p}
调账业务6-102
	应收减少                                                      6-102-610              {查看610-s   录入6-102-610-e   过账610-t   打印610-p}
	应收增加                                                      6-102-611              {查看611-s   录入6-102-611-e   过账611-t   打印611-p}
	应付减少                                                      6-102-612              {查看612-s   录入6-102-612-e   过账612-t   打印612-p}
	应付增加                                                      6-102-613              {查看613-s   录入6-102-613-e   过账613-t   打印613-p}
	资金增加                                                      6-102-614              {查看614-s   录入6-102-614-e   过账614-t   打印614-p}
	资金减少                                                      6-102-615              {查看615-s   录入6-102-615-e   过账615-t   打印615-p}
会计凭证                                                                   6-616              {查看616-s   录入6-616-e       过账616-t   打印616-p}
往来报表6-103
	应收应付6-103-104
		单位应收应付                                6-103-104-617          {查看6-103-104-617-s   打印6-103-104-617-p}
		职员应收应付                                6-103-104-618          {查看6-103-104-618-s   打印6-103-104-618-p}
		部门应收应付                                6-103-104-619          {查看6-103-104-619-s   打印6-103-104-619-p}
		地区应收应付                                6-103-104-620          {查看6-103-104-620-s   打印6-103-104-620-p}
	往来分析                                                      6-103-621              {查看6-103-621-s   打印6-103-621-p}
	往来对账                                                      6-103-622              {查看6-103-622-s   打印6-103-622-p}
	账龄分析                                                      6-103-623              {查看6-103-623-s   打印6-103-623-p}
	按单结算查询                                              6-103-624              {查看6-103-624-s   打印6-103-624-p}
	超期应收款                                                  6-103-625              {查看6-103-625-s   打印6-103-625-p}
	超期应付款                                                  6-103-626              {查看6-103-626-s   打印6-103-626-p}
	回款统计6-103-105
		经手人回款统计                            6-103-105-627          {查看6-103-105-627-s   打印6-103-105-627-p}
		单位回款统计                                6-103-105-628          {查看6-103-105-628-s   打印6-103-105-628-p}
		部门回款统计                                6-103-105-629          {查看6-103-105-629-s   打印6-103-105-629-p}
		地区回款统计                                6-103-105-630          {查看6-103-105-630-s   打印6-103-105-630-p}
		制单人收款统计                              6-103-105-630.1        {查看6-103-105-630.1-s   打印6-103-105-630.1-p}
	业务统计6-103-106
		单位业务统计                                6-103-106-631          {查看6-103-106-631-s   打印6-103-106-631-p}
		职员业务统计                                6-103-106-632          {查看6-103-106-632-s   打印6-103-106-632-p}
		部门业务统计                                6-103-106-633          {查看6-103-106-633-s   打印6-103-106-633-p}
		地区业务统计                                6-103-106-634          {查看6-103-106-634-s   打印6-103-106-634-p}
财务报表6-107
         
	现金银行                                                     6-107-635              {查看6-107-635-s   打印6-107-635-p}
	其它收入                                                     6-107-636              {查看6-107-636-s   打印6-107-636-p}
	固定资产                                                     6-107-637              {查看6-107-637-s   打印6-107-637-p}
	费用合计统计                                             6-107-638              {查看6-107-638-s   打印6-107-638-p}
	年度费用报表                                             6-107-639              {查看6-107-639-s   打印6-107-639-p}
	费用分布6-107-108                                                     
		部门费用分布                               6-107-108-640.1        {查看6-107-108-640.1-s   打印6-107-108-640.1-p}  
		职员费用分布                               6-107-108-640.2        {查看6-107-108-640.2-s   打印6-107-108-640.2-p}
		地区费用分布                               6-107-108-640.3        {查看6-107-108-640.3-s   打印6-107-108-640.3-p}
		单位费用分布                               6-107-108-640.4        {查看6-107-108-640.4-s   打印6-107-108-640.4-p}
	利润表        6-107-641              {查看6-107-641-s   打印6-107-641-p}
	资产负债表        6-107-642              {查看6-107-642-s   打印6-107-642-p}
	##资产负债表(树形表)         6-107-643              {查看6-107-643-s   打印6-107-643-p}
	年度利润表比较                                         6-107-644              {查看6-107-644-s   打印6-107-644-p}
	年度资产负债表比较                                 6-107-645              {查看6-107-645-s   打印6-107-645-p}
*/ 

##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('收款单',',1,3,','601-s','6-601-e','601-t','601-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('付款单',',1,2,','602-s','6-602-e','602-t','602-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('费用单',',1,5,','603-s','6-603-e','603-t','603-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('其它收入单',',1,5,','604-s','6-604-e','604-t','604-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('内部转款单',',1,5,','605-s','6-605-e','605-t','605-p');
##固定资产管理6-101
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('固定资产购置',',1,5,','607-s','6-101-607-e','607-t','607-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('固定资产折旧',',1,5,','608-s','6-101-608-e','608-t','608-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('固定资产变卖',',1,5,','609-s','6-101-609-e','609-t','609-p');
##调账业务6-102
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('应收减少',',1,5,','610-s','6-102-610-e','610-t','610-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('应收增加',',1,5,','611-s','6-102-611-e','611-t','611-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('应付减少',',1,5,','612-s','6-102-612-e','612-t','612-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('应付增加',',1,5,','613-s','6-102-613-e','613-t','613-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('资金增加',',1,5,','614-s','6-102-614-e','614-t','614-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('资金减少',',1,5,','615-s','6-102-615-e','615-t','615-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`entUrl`,`tranUrl`,`printUrl`) values ('会计凭证',',1,5,','616-s','6-616-e','616-t','616-p'); 
##往来报表6-103	
##应收应付6-103-104
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位应收应付',',10,14,','6-103-104-617-s','6-103-104-617-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('职员应收应付',',10,14,','6-103-104-618-s','6-103-104-618-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('部门应收应付',',10,14,','6-103-104-619-s','6-103-104-619-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('地区应收应付',',10,14,','6-103-104-620-s','6-103-104-620-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('往来分析',',10,14,','6-103-621-s','6-103-621-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('往来对账',',10,14,','6-103-622-s','6-103-622-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('账龄分析 ',',10,14,','6-103-623-s','6-103-623-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('按单结算查询',',10,14,','6-103-624-s','6-103-624-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('超期应收款',',10,14,','6-103-625-s','6-103-625-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('超期应付款',',10,14,','6-103-626-s','6-103-626-p');
##回款统计6-103-105
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('经手人回款统计 ',',10,14,','6-103-105-627-s','6-103-105-627-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位回款统计',',10,14,','6-103-105-628-s','6-103-105-628-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('部门回款统计',',10,14,','6-103-105-629-s','6-103-105-629-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('地区回款统计',',10,14,','6-103-105-630-s','6-103-105-630-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('制单人收款统计',',10,14,','6-103-105-630.1-s','6-103-105-630.1-p');
##业务统计6-103-106
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位业务统计',',10,14,','6-103-106-631-s','6-103-106-631-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('职员业务统计',',10,14,','6-103-106-632-s','6-103-106-632-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('部门业务统计',',10,14,','6-103-106-633-s','6-103-106-633-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('地区业务统计',',10,14,','6-103-106-634-s','6-103-106-634-p');
##财务报表6-107
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('现金银行',',10,15,','6-107-635-s','6-107-635-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('其它收入',',10,15,','6-107-636-s','6-107-636-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('固定资产',',10,15,','6-107-637-s','6-107-637-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('费用合计统计',',10,15,','6-107-638-s','6-107-638-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('年度费用报表',',10,15,','6-107-639-s','6-107-639-p');
##费用分布
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('部门费用分布 ',',10,15,','6-107-108-640.1-s','6-107-108-640.1-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('职员费用分布',',10,15,','6-107-108-640.2-s','6-107-108-640.2-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('地区费用分布',',10,15,','6-107-108-640.3-s','6-107-108-640.3-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('单位费用分布',',10,15,','6-107-108-640.4-s','6-107-108-640.4-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('利润表',',10,15,','6-107-641-s','6-107-641-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('资产负债表',',10,15,','6-107-642-s','6-107-642-p');
/**insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('资产负债表(树形表)',',10,15,','6-107-643-s','6-107-643-p');*/
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('年度利润表比较',',10,15,','6-107-644-s','6-107-644-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('年度资产负债表比较',',10,15,','6-107-645-s','6-107-645-p');
##-----------------------------------------------------------------------------------------------------


/**
7单据中心
二级菜单(120-140)
700-800 
*/ 

   
##-----------------------------------------------------------------------------------------------------
/**
8辅助功能  
二级菜单(140-160)
800-900 
物价管理                                               8-800              {查看执行8-800-s      修改删除8-800-u   打印8-800-p}
生产模板			   8-800.1            {查看执行8-800.1-s    新增8-800.1-a   修改删除8-800.1-u  打印8-800.1-p}  
单据编号设置			   8-801              {查看执行8-801-s}
单据格式配置                                       8-802              {查看执行8-802-s}
导入基本信息(Excel)     8-803              {查看执行8-803-s}
报警中心                                     	   8-807              {查看执行8-807-s        打印8-807-p} 
基本信息数据搬移                               8-808				  {查看执行8-808-s}
价格折扣跟踪                                       8-804              {查看执行8-804-s      新增8-804-a     修改删除8-804-u     打印8-804-p}     
OM订单配置8-141
	OM接口用户                          8-141-805              {查看执行8-141-805-s      新增8-141-805-a     修改删除8-141-805-u     打印8-141-805-p}    
	OM接口配置                          8-141-806              {查看执行8-141-806-s      新增8-141-806-a     修改删除8-141-806-u     打印8-141-806-p}  
*/ 


##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('物价管理',',18,21,','8-800-s','8-800-u','8-800-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('生产模板',',18,21,','8-800.1-s','8-800.1-a','8-800.1-u','8-800.1-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('单据编号设置',',18,21,','8-801-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('单据格式配置',',18,21,','8-802-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('导入基本信息(Excel)',',18,21,','8-803-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('报警中心',',18,21,','8-807-s','8-807-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('基本信息数据搬移 ',',18,21,','8-808-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('价格折扣跟踪',',18,21,','8-804-s','8-804-a','8-804-u','8-804-p');
##OM订单配置8-141
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('OM接口用户',',18,21,','8-141-805-s','8-141-805-a','8-141-805-u','8-141-805-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`,`printUrl`) values ('OM接口配置',',18,21,','8-141-806-s','8-141-806-a','8-141-806-u','8-141-806-p');


 
##-----------------------------------------------------------------------------------------------------
/**
9系统维护
二级菜单(160-180)
900-1000 
修改密码                                       9-901              {查看执行9-901-s      }  
用户及权限设置
基本信息授权
数据备份                                      9-904              {查看执行9-904-s      }  
数据恢复                                      9-905              {查看执行9-905-s      } 
系统重建                                      9-906              {查看执行9-906-s      } 
成本重算                                      9-906.5            {查看执行 9-906.5-s      } 
月结存                                          9-907              {查看执行9-907-s      } 
年结存                                          9-908              {查看执行9-908-s      } 
月结存信息表                              9-909              {查看执行9-909-s      打印9-901-p} 
年结存信息表                              9-910              {查看执行9-910-s      修改删除9-910-u     打印9-910-p} 
用户及权限设置                             9-911              {查看执行9-911-s      新增复制9-911-a     修改删除9-911-u} 
基本信息授权                               9-912              {查看执行9-912-s  } 
*/ 

##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('修改密码',',18,22,','9-901-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('数据备份',',18,22,','9-904-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('数据恢复',',18,22,','9-905-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('系统重建',',18,22,','9-906-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('成本重算 ',',18,22,','9-906.5-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('月结存',',18,22,','9-907-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('年结存',',18,22,','9-908-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('月结存信息表',',18,22,','9-909-s','9-901-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('年结存信息表',',18,22,','9-910-s','9-910-u','9-910-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`addUrl`,`editUrl`) values ('用户及权限设置',',18,22,','9-911-s','9-911-a','9-911-u');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('基本信息授权',',18,22,','9-912-s');

##-----------------------------------------------------------------------------------------------------
/**
10帮助中心
二级菜单(180-200)
1000-1100  
*/ 

##-----------------------------------------------------------------------------------------------------
/**
全部权限-》单据其它权限
1100-1200

成本查看权限 				1101              {查看执行1101-s} 
红冲自己单据权限 			1102              {查看执行1102-s} 
红冲他人单据权限 			1103              {查看执行1103-s} 
业务草稿				1104              {查看执行1104-s		打印1104-p} 
修改业务草稿                                           1105              {查看执行1105-s} 
删除业务草稿                                           1106              {查看执行1106-s} 
查看他人草稿                                           1107              {查看执行1107-s} 
修改他人草稿                                           1108              {查看执行1108-s} 
删除他人草稿                                           1109              {查看执行1109-s} 
经营历程 				1110              {查看执行1110-s		修改删除1110-u     打印1110-p} 
查看他人经营历程单据                           1111              {查看执行1111-s} 
*/

##sql

insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('成本查看权限',',18,23,','1101-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('红冲自己单据权限',',18,23,','1102-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('红冲他人单据权限',',18,23,','1103-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) values ('业务草稿',',18,23,','1104-s','1104-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('修改业务草稿',',18,23,','1105-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('删除业务草稿',',18,23,','1106-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('查看他人草稿',',18,23,','1107-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('修改他人草稿',',18,23,','1108-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('删除他人草稿',',18,23,','1109-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`editUrl`,`printUrl`) values ('经营历程',',18,23,','1110-s','1110-u','1110-p');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('查看他人经营历程单据',',18,23,','1111-s');






























##------------------------------------暂时不做-------------数据过滤----------------------------------------------------



##-----------------------------------------------------------------------------------------------------
/*全部基本信息字段*/
/**
科目
1200-1250
科目全名 				1201              {查看执行1201-s} 
科目编号				1202              {查看执行1202-s}  			
科目简名				1203              {查看执行1203-s} 
科目拼音码				1204              {查看执行1204-s} 
科目备注				1205              {查看执行1205-s} 
*/

/**
##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('科目全名',',24,25,','1201-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('科目编号',',24,25,','1202-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('科目简名',',24,25,','1203-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('科目拼音码',',24,25,','1204-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('科目备注',',24,25,','1205-s');
*/

/**
商品
1250-1300
商品全名 				1251              {查看执行1251-s} 
商品编号                                                   1252              {查看执行1252-s} 
商品简名                                                   1253              {查看执行1253-s} 
商品拼音码                                               1254              {查看执行1254-s} 
基本单位                                                   1255              {查看执行1255-s} 
规格                                                           1256              {查看执行1256-s} 
型号                                                           1257              {查看执行1257-s} 
产地                                                           1258              {查看执行1258-s} 
有效期(天)             1259              {查看执行1259-s} 
商品备注                                                  1260              {查看执行1260-s} 
成本算法                                                  1261              {查看执行1261-s} 
基本条码                                                  1262              {查看执行1262-s} 
商品自定义一                                          1263              {查看执行1263-s} 
*/

/**
##sql
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品全名',',24,26,','1251-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品编号',',24,26,','1252-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品简名',',24,26,','1253-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品拼音码 ',',24,26,','1254-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('基本单位 ',',24,26,','1255-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('规格',',24,26,','1256-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('型号',',24,26,','1257-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('产地',',24,26,','1258-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('有效期(天)',',24,26,','1259-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品备注',',24,26,','1260-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('成本算法',',24,26,','1261-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('基本条码',',24,26,','1262-s');
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('商品自定义一',',24,26,','1263-s');
*/


/**
单位
1300-1350
单位全名 				1301              {查看执行1301-s} 
单位编号                                                   1301              {查看执行1301-s} 
单位简名                                                   1301              {查看执行1301-s} 
单位拼音码                                               1301              {查看执行1301-s} 
单位地址                                                   1301              {查看执行1301-s} 
单位电话                                                   1301              {查看执行1301-s} 
传真                                                           1301              {查看执行1301-s} 
电子邮件                                                   1301              {查看执行1301-s} 
邮政编码                                                   1301              {查看执行1301-s} 
开户银行                                                   1301              {查看执行1301-s} 
银行账号                                                   1301              {查看执行1301-s} 
联系人一                                                   1301              {查看执行1301-s} 
税号                                                           1301              {查看执行1301-s} 
单位备注                                                   1301              {查看执行1301-s} 
单位自定义一                                           1301              {查看执行1301-s} 
*/

#sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('单位全名',',24,27,','1301-s');


/**
职员
1350-1400
职员全名 				1351              {查看执行1351-s} 
职员编号
职员拼音码
生日
职员部门
职员电话
职员地址
职员备注
职员自定义一

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('职员全名',',24,28,','1351-s');

*/



/**
仓库
1400-1450
仓库全名 				1401              {查看执行1401-s} 
仓库编号
仓库简名
仓库拼音码
仓库备注
仓库自定义一

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('仓库全名',',24,29,','1401-s');

*/

/**
部门
1450-1500
部门全名 				1451              {查看执行1451-s} 
部门编号
部门拼音码
部门备注
部门自定义一

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('部门全名',',24,30,','1451-s');

*/


/**
地区
1500-1550
地区全名 				1501              {查看执行1501-s} 
地区编号
地区拼音码
地区备注
地区自定义一

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('地区全名',',24,31,','1501-s');

*/


/**
单据类型
1550-1600
单据类型 				1551              {查看执行1551-s} 
单据字头

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('单据类型',',24,32,','1551-s');

*/


#-----------------------------------------------------------------------------------------
/*其它字段*/

/**
经营利润表
1600-1650

本月发生额				1601              {查看执行1601-s} 
累计金额 

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('本月发生额',',48,','1601-s');

*/

/**
资产负债表
1650-1700

资产类					1651              {查看执行1651-s} 
累计金额(资产类) 
负债及权益类
累计金额(负债及权益类)

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('资产类',',49,','1651-s');

*/

/**
资产负债表(树形表)
1700-1750

本月发生额				1701              {查看执行1701-s} 

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('本月发生额',',58,','1701-s');
*/

/**
单位信息
1750-1800

适用价格				1751              {查看执行1751-s} 
状态

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('适用价格',',38,','1751-s');
*/

/**
商品信息
1800-1850

单位关系				1801              {查看执行1801-s} 
零信息价
预设售价1
预设售价2
预设售价3
状态
有无图片

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('单位关系',',36,','1801-s');

*/

/**
仓库信息
1850-1900

状态					1851              {查看执行1851-s} 

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('状态',',40,','1851-s');

*/

/**
职员信息
1900-1950

职员职务				1901              {查看执行1901-s} 
每单优惠限额
有无图片
状态

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('职员职务',',39,','1901-s');

*/

/**
数据恢复
1950-2000

账套名称				1951              {查看执行1951-s} 
数据名称
备份时间
备份文件名

##sql
##insert  into `sys_permission`(`name`,`pids`,`seeUrl`) values ('账套名称',',39,','1951-s');

*/

/**
商品销售明细汇总表
2000-2050

单据日期				1551              {查看执行1551-s} 
单据编号
销售数量
辅助数量
单价
金额
折扣
折后单价
折后金额
税额
含税单价
含税金额
成本金额
附加说明
单据备注
自定义1
附加信息一

##sql

*/


/**
商品销售统计
2050-2100

辅助销售数量				1551              {查看执行1551-s} 
销售数量
折后金额
税额
价税合计
赠品金额
赠品数量

#sql

*/


/**
单位应收应付
2100-2150

应收余额				1551              {查看执行1551-s} 
应付余额

##sql

*/



/**
单据分析
2150-2200

分析项目				1551              {查看执行1551-s} 
全年合计
一月
二月
三月
四月
五月
六月
七月
八月
九月
十月
十一月
十二月

##sql

*/


/**
经营历程
2200-2250

单据编号				1551              {查看执行1551-s} 
日期
过账时间
摘要
附加说明
制单人
过账人
打印次数
金额

##sql

*/


/**
月结存信息表
2250-2300

账龄					1551              {查看执行1551-s} 
起始日期
结束日期
本月单据数量
月结期间

##sql

*/


/**
年结存信息表
2300-2350

年份					1551              {查看执行1551-s} 
起始日期
结束日期
年度单据数量

##sql

*/
##---------------------------------------------end----数据过滤----------------------------------------------------

##--------主数据库有改动维护mianDatabase_sql_log.sql文件----------------
  