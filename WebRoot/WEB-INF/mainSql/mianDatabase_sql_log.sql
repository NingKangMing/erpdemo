/*-------------总账套更新日志记录-----以##yyyymmdd##开头  以#@yyyymmdd@#结束 用于更新（按日期顺序）-----------*/


/**---------------------##20150209##-------------------------*/
/**权限表加版本字段*/
alter table `sys_permission` add column `version` INT(3) DEFAULT 1 COMMENT '版本号显示等级';
/**更新权限表显示等级*/
update `sys_permission` set version = 2
where name='期初应收应付' 
 or name='期初现金银行'
 or name='期初固定资产'
 or name='期初财务数据'
 or name='期初财务数据'
 or name='收款单'
 or name='付款单'
 or name='费用类型'
 or name='收入类型'
 or name='固定资产'
 or pids like '%,5,%'
 or pids like '%,14,%'
 or pids like '%,15,%';
/**更新成本调价单权限参数*/
update `sys_permission` 
	set 
	seeUrl = '5-511-s' , 
	entUrl = '5-511-e' , 
	tranUrl = '5-511-t' , 
	printUrl = '5-511-p' 
	where name='成本调价单';
/**更新成本调价单统计权限参数*/
update `sys_permission` 
	set
	pids = ',10,13,' , 
	seeUrl = '5-82-527-s' ,
	printUrl = '5-82-527-p'
	where name='成本调价单统计' ;
/**权限分类表加版本字段*/
alter table `sys_permission_type` add column `version` INT(3) DEFAULT 1 COMMENT '版本号显示等级';
update `sys_permission_type` set version = 2
where 
  id = 5
  or id = 14
  or id = 15;
/**---------------------#@20150209@#-------------------------*/


/*-------------##20150309## 增加成本重新权限-------------*/
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) 
select '成本重算 ',',18,22,','9-906.5-s' from dual 
where not exists(select `name` from `sys_permission` where `name`='成本重算');
/*-------------#@20150309@# 增加成本重新权限-------------*/


/*-------------##20150311##基本信息数据搬移-------------*/
insert  into `sys_permission`(`name`,`pids`,`seeUrl`) 
select '基本信息数据搬移 ',',18,22,','8-808-s' from dual 
where not exists(select `name` from `sys_permission` where `name`='基本信息数据搬移');
/*-------------#@20150311@#基本信息数据搬移-------------*/


/*-------------##20150325##lzm用户操作日志-----------------*/
DROP TABLE IF EXISTS sys_user_log;
CREATE TABLE `sys_user_log` (                                         
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户操作日志',  
    `whichDbName` varchar(200) DEFAULT NULL COMMENT '账套名称',     
    `username` varchar(50) DEFAULT NULL COMMENT '登录用户名',      
    `ip` varchar(50) DEFAULT NULL COMMENT 'ip',                         
    `actionType` varchar(200) DEFAULT NULL COMMENT '操作行为',         
    `updateTime` datetime DEFAULT NULL COMMENT '时间',                
    PRIMARY KEY (`id`)                                                  
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*-------------#@20150325@#lzm用户操作日志-----------------*/
  
/*-------------##20150425##cdc商品销售明细汇总表增加权限控制-----------------*/
insert  into `sys_permission`(`name`,`pids`,`seeUrl`,`printUrl`) 
select '商品销售明细汇总',',10,12,','4-61-406.5-s','4-61-406.5-p' from dual
where not exists(select 1 from `sys_permission` where `name`='商品销售明细汇总');
/*-------------#@20150425@#cdc商品销售明细汇总表增加权限控制-----------------*/





