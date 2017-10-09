/*
SQLyog 企业版 - MySQL GUI v7.14 
MySQL - 5.1.73-community : Database - lzmdatabasename
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

CREATE DATABASE /*!32312 IF NOT EXISTS*/`lzmdatabasename` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `lzmdatabasename`;

/*Table structure for table `aioerp_file` */

DROP TABLE IF EXISTS `aioerp_file`;

CREATE TABLE `aioerp_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号，主键',
  `sourceName` varchar(100) DEFAULT NULL COMMENT '原始文件名',
  `saveName` varchar(100) DEFAULT NULL COMMENT '保存文件名',
  `fileType` varchar(20) DEFAULT NULL COMMENT '文件格式',
  `savePath` varchar(200) DEFAULT NULL COMMENT '保存路径',
  `typeFile` int(11) DEFAULT NULL COMMENT '文件格式     常量类定义  图片  视频',
  `tableId` int(11) DEFAULT NULL COMMENT '对应那张表    常量类定义',
  `recordId` int(11) DEFAULT NULL COMMENT '对应表的记录ID',
  `status` int(11) DEFAULT NULL COMMENT '状态        常量类定义   0=删除  1=停用  2=启用',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aioerp_sys` */

DROP TABLE IF EXISTS `aioerp_sys`;

CREATE TABLE `aioerp_sys` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key1` varchar(50) DEFAULT NULL,
  `value1` varchar(200) DEFAULT NULL,
  `memo` varchar(200) CHARACTER SET utf8 COLLATE utf8_czech_ci DEFAULT NULL,
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aioerp_sys_config` */

DROP TABLE IF EXISTS `aioerp_sys_config`;

CREATE TABLE `aioerp_sys_config` (
  `id` int(6) NOT NULL AUTO_INCREMENT COMMENT '系统配置表',
  `code` varchar(50) DEFAULT NULL COMMENT '编号',
  `memo` varchar(300) DEFAULT NULL COMMENT '配置类名',
  `isAllow` int(1) DEFAULT NULL COMMENT '是否允许 1:是 0:否',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aioerp_sys_user` */

DROP TABLE IF EXISTS `aioerp_sys_user`;

CREATE TABLE `aioerp_sys_user` (
  `id` int(5) NOT NULL AUTO_INCREMENT COMMENT '系统单位用户',
  `fullName` varchar(200) DEFAULT NULL COMMENT '单位全名',
  `smallName` varchar(200) DEFAULT NULL COMMENT '单位简称',
  `phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `address` varchar(300) DEFAULT NULL COMMENT '地址',
  `fax` varchar(50) DEFAULT NULL COMMENT '传真号码',
  `zipCode` varchar(50) DEFAULT NULL COMMENT '邮政编码',
  `corp` varchar(50) DEFAULT NULL COMMENT '法人代表',
  `taxNum` varchar(100) DEFAULT NULL COMMENT '税务登记号',
  `bank` varchar(500) DEFAULT NULL COMMENT '开户银行及账号',
  `home` varchar(100) DEFAULT NULL COMMENT '主页地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_accounts` */

DROP TABLE IF EXISTS `b_accounts`;

CREATE TABLE `b_accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code` varchar(225) NOT NULL COMMENT '编号',
  `fullName` varchar(225) DEFAULT NULL COMMENT '全称',
  `smallName` varchar(225) DEFAULT NULL COMMENT '简称',
  `spell` varchar(225) DEFAULT NULL COMMENT '拼音码',
  `type` varchar(20) DEFAULT NULL COMMENT '类型   eg:固定行类型',
  `memo` text COMMENT '备注',
  `node` int(11) DEFAULT NULL COMMENT '1-叶子节点，2-分类',
  `supId` int(11) DEFAULT NULL COMMENT '父类ID，从1开始',
  `rank` int(11) DEFAULT NULL COMMENT '排序，大的在前面',
  `pids` varchar(255) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `pFullNames` varchar(1000) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT NULL COMMENT '状态 0=删除  1=停用  2=启用',
  `isSysRow` int(1) DEFAULT NULL COMMENT '是不是系统行   0不是   1是（提示不能删除）',
  `money` decimal(30,4) DEFAULT NULL COMMENT '账户资金',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_area` */

DROP TABLE IF EXISTS `b_area`;

CREATE TABLE `b_area` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID主键',
  `fullName` varchar(255) NOT NULL COMMENT '地区全名',
  `code` varchar(255) DEFAULT NULL COMMENT '地区编号，唯一约束',
  `spell` varchar(255) DEFAULT NULL COMMENT '地区拼音码',
  `memo` varchar(500) DEFAULT NULL COMMENT '备注',
  `smallName` varchar(50) DEFAULT NULL COMMENT '简称',
  `handler` varchar(50) DEFAULT NULL COMMENT '负责人',
  `handlerTel` varchar(50) DEFAULT NULL COMMENT '负责人电话',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT '2' COMMENT '状态 1=停用  2=启用',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `supId` int(11) DEFAULT '0' COMMENT '父类ID，默认0',
  `node` int(1) DEFAULT '1' COMMENT '1-叶子节点，2-分类',
  `pids` varchar(500) DEFAULT '{0}' COMMENT '{1},{2}此类型保存父级分类',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_department` */

DROP TABLE IF EXISTS `b_department`;

CREATE TABLE `b_department` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号，主键',
  `fullName` varchar(255) NOT NULL COMMENT '部门名称',
  `code` varchar(255) DEFAULT NULL COMMENT '部门编号，唯一约束',
  `spell` varchar(255) DEFAULT NULL COMMENT '部门拼音码',
  `memo` varchar(500) DEFAULT NULL COMMENT '备注',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT '2' COMMENT '状态 1=停用  2=启用',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `supId` int(11) DEFAULT '0' COMMENT '父类ID，默认0',
  `node` int(1) DEFAULT '1' COMMENT '1-叶子节点，2-分类',
  `pids` varchar(500) DEFAULT '{0}' COMMENT '{1},{2}此类型保存父级分类',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_product` */

DROP TABLE IF EXISTS `b_product`;

CREATE TABLE `b_product` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号，主键',
  `fullName` varchar(255) NOT NULL COMMENT '商品全名',
  `code` varchar(255) DEFAULT NULL COMMENT '商品编号，唯一约束',
  `smallName` varchar(255) DEFAULT NULL COMMENT '商品简称',
  `spell` varchar(255) DEFAULT NULL COMMENT '商品拼音码',
  `standard` varchar(255) DEFAULT NULL COMMENT '商品规格',
  `model` varchar(255) DEFAULT NULL COMMENT '型号',
  `field` varchar(255) DEFAULT NULL COMMENT '产地',
  `costArith` int(11) DEFAULT NULL COMMENT '成本算法 1.移动加权平均,2.先进先出法,3.后进先出法,4.手工指定法',
  `validity` int(11) DEFAULT NULL COMMENT '有效期(天数)',
  `sysImgId` int(11) DEFAULT NULL COMMENT '外键   图片ID',
  `savePath` varchar(225) DEFAULT NULL COMMENT '图片存放路径',
  `memo` text COMMENT '备注',
  `calculateUnit1` varchar(50) DEFAULT NULL COMMENT '单位名称1',
  `calculateUnit2` varchar(50) DEFAULT NULL COMMENT '单位名称2',
  `calculateUnit3` varchar(50) DEFAULT NULL COMMENT '单位名称3',
  `unitRelation1` int(11) DEFAULT '1' COMMENT '单位关系1',
  `unitRelation2` decimal(30,4) DEFAULT NULL COMMENT '单位关系2',
  `unitRelation3` decimal(30,4) DEFAULT NULL COMMENT '单位关系3',
  `barCode1` varchar(255) DEFAULT NULL COMMENT '基本条码',
  `barCode2` varchar(255) DEFAULT NULL COMMENT '条码2',
  `barCode3` varchar(255) DEFAULT NULL COMMENT '条码3',
  `retailPrice1` decimal(30,4) DEFAULT NULL COMMENT '零售价1',
  `retailPrice2` decimal(30,4) DEFAULT NULL COMMENT '零售价2',
  `retailPrice3` decimal(30,4) DEFAULT NULL COMMENT '零售价3',
  `defaultPrice11` decimal(30,4) DEFAULT NULL COMMENT '预设售价11',
  `defaultPrice12` decimal(30,4) DEFAULT NULL COMMENT '预设售价12',
  `defaultPrice13` decimal(30,4) DEFAULT NULL COMMENT '预设售价13',
  `defaultPrice21` decimal(30,4) DEFAULT NULL COMMENT '预设售价21',
  `defaultPrice22` decimal(30,4) DEFAULT NULL COMMENT '预设售价22',
  `defaultPrice23` decimal(30,4) DEFAULT NULL COMMENT '预设售价23',
  `defaultPrice31` decimal(30,4) DEFAULT NULL COMMENT '预设售价31',
  `defaultPrice32` decimal(30,4) DEFAULT NULL COMMENT '预设售价32',
  `defaultPrice33` decimal(30,4) DEFAULT NULL COMMENT '预设售价33',
  `assistUnit` varchar(255) DEFAULT NULL COMMENT '辅助单位，方便查询显示',
  `node` int(11) DEFAULT NULL COMMENT '1-叶子节点，2-分类',
  `supId` int(11) DEFAULT NULL COMMENT '父类ID，从1开始',
  `rank` int(11) DEFAULT NULL COMMENT '排序，大的在前面',
  `pids` varchar(255) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT NULL COMMENT '状态 0=删除  1=停用  2=启用',
  `unitId1` int(1) DEFAULT NULL,
  `unitId2` int(1) DEFAULT NULL,
  `unitId3` int(1) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_product_unit` */

DROP TABLE IF EXISTS `b_product_unit`;

CREATE TABLE `b_product_unit` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品信息_单位，主键',
  `productId` int(11) NOT NULL COMMENT '商品信息Id，外键',
  `selectUnitId` int(1) NOT NULL COMMENT '选择的那个单位',
  `calculateUnit` varchar(50) DEFAULT NULL COMMENT '单位名称',
  `unitRelation` int(11) DEFAULT NULL COMMENT '单位关系1',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价1',
  `defaultPrice1` decimal(30,4) DEFAULT NULL COMMENT '预设售价1',
  `defaultPrice2` decimal(30,4) DEFAULT NULL COMMENT '预设售价2',
  `defaultPrice3` decimal(30,4) DEFAULT NULL COMMENT '预设售价3',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT NULL COMMENT '状态0=删除  1=停用  2=启用 ',
  `barCode` varchar(200) CHARACTER SET utf8 COLLATE utf8_czech_ci DEFAULT NULL COMMENT '条码',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_staff` */

DROP TABLE IF EXISTS `b_staff`;

CREATE TABLE `b_staff` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fullName` varchar(255) DEFAULT NULL COMMENT '##用户姓名   代替上下面的name 确定与基础数据其它模块一样',
  `name` varchar(255) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `spell` varchar(255) DEFAULT NULL,
  `depmId` int(11) DEFAULT NULL,
  `birth` date DEFAULT NULL,
  `role` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `favoriteLimit` double DEFAULT NULL,
  `memo` text,
  `imageName` varchar(255) DEFAULT NULL,
  `imageAddress` varchar(255) DEFAULT NULL,
  `supId` int(11) NOT NULL,
  `idCard` varchar(255) DEFAULT NULL,
  `createTime` date DEFAULT NULL,
  `updateTime` timestamp NULL DEFAULT NULL,
  `status` int(1) DEFAULT NULL,
  `sysImgId` int(11) DEFAULT NULL,
  `node` int(1) DEFAULT '1' COMMENT '1-叶子节点，2-分类',
  `pids` varchar(255) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_storage` */

DROP TABLE IF EXISTS `b_storage`;

CREATE TABLE `b_storage` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '序号，主键',
  `fullName` varchar(255) NOT NULL COMMENT '仓库全名',
  `code` varchar(255) DEFAULT NULL COMMENT '仓库编号，唯一约束',
  `spell` varchar(255) DEFAULT NULL COMMENT '仓库拼音',
  `smallName` varchar(255) DEFAULT NULL COMMENT '仓库简称',
  `stoAddress` varchar(255) DEFAULT NULL COMMENT '仓库地址',
  `functionary` varchar(255) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(255) DEFAULT NULL COMMENT '负责人电话',
  `memo` varchar(500) DEFAULT NULL COMMENT '备注',
  `supId` int(11) NOT NULL COMMENT '父类ID，默认0',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT '2' COMMENT '状态 1=停用  2=启用',
  `node` int(1) DEFAULT '1' COMMENT '1-叶子节点，2-分类',
  `pids` varchar(255) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `b_unit` */

DROP TABLE IF EXISTS `b_unit`;

CREATE TABLE `b_unit` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(50) DEFAULT NULL COMMENT '单位编号',
  `fullName` varchar(200) DEFAULT NULL COMMENT '单位全名',
  `smallName` varchar(200) DEFAULT NULL COMMENT '单位简称',
  `spell` varchar(100) DEFAULT NULL COMMENT '科目拼音码',
  `address` varchar(250) DEFAULT NULL COMMENT '单位地址',
  `phone` varchar(50) DEFAULT NULL COMMENT '单位电话',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮件',
  `contact1` varchar(50) DEFAULT NULL COMMENT '联系人一',
  `mobile1` varchar(50) DEFAULT NULL COMMENT '手机一',
  `contact2` varchar(50) DEFAULT NULL COMMENT '联系人二',
  `mobile2` varchar(50) DEFAULT NULL COMMENT '手机二',
  `staffId` int(11) DEFAULT NULL COMMENT '默认经手人id',
  `staffName` varchar(50) DEFAULT NULL COMMENT '默认经手人姓名',
  `bank` varchar(50) DEFAULT NULL COMMENT '开户银行',
  `bankAccount` varchar(50) DEFAULT NULL COMMENT '银行账号',
  `zipCode` varchar(20) DEFAULT NULL COMMENT '邮政编码',
  `beginGetMoney` decimal(30,4) DEFAULT '0' COMMENT '期初应收款',
  `beginPayMoney` decimal(30,4) DEFAULT '0' COMMENT '期初应付款',
  `fax` varchar(50) DEFAULT NULL COMMENT '传真',
  `beginPreGetMoney` decimal(30,4) DEFAULT NULL COMMENT '期初预收款',
  `beginPrePayMoney` decimal(30,4) DEFAULT NULL COMMENT '期初预付款',
  `tariff` varchar(50) DEFAULT NULL COMMENT '税号',
  `getMoneyLimit` decimal(30,4) DEFAULT NULL COMMENT '应收款上限',
  `payMoneyLimit` decimal(30,4) DEFAULT NULL COMMENT '应付款上限',
  `areaId` int(11) DEFAULT NULL COMMENT '地区id',
  `areaFullName` varchar(50) DEFAULT NULL COMMENT '地区全名',
  `replacePrdPeriod` int(11) DEFAULT NULL COMMENT '换货期限',
  `replacePrdPercentage` decimal(30,4) DEFAULT NULL COMMENT '换货比例(%)',
  `settlePeriod` int(11) DEFAULT NULL COMMENT '结算期限(天)',
  `totalGet` decimal(30,4) DEFAULT NULL COMMENT '累计应收',
  `totalPay` decimal(30,4) DEFAULT NULL COMMENT '累计应付',
  `fitPrice` int(1) DEFAULT '0' COMMENT '适用价格',
  `totalPreGet` decimal(30,4) DEFAULT NULL COMMENT '累计预收',
  `totalPrePay` decimal(30,4) DEFAULT NULL COMMENT '累计预付',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `node` int(11) DEFAULT NULL COMMENT '1-叶子节点，2-分类',
  `supId` int(11) DEFAULT NULL COMMENT '父类ID，从1开始',
  `rank` int(11) DEFAULT NULL COMMENT '排序，大的在前面',
  `pids` varchar(255) DEFAULT NULL COMMENT '{1},{2}此类型保存父级分类',
  `createTime` date DEFAULT NULL COMMENT '创建时间',
  `updateTime` date DEFAULT NULL COMMENT '修改时间',
  `status` int(1) DEFAULT NULL COMMENT '状态 0=删除  1=停用  2=启用',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `bb_billhistory` */

DROP TABLE IF EXISTS `bb_billhistory`;

CREATE TABLE `bb_billhistory` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID经营历程',
  `billId` int(11) DEFAULT NULL COMMENT '单据ID外键',
  `billCode` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `isRCW` int(11) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `postTime` datetime DEFAULT NULL COMMENT '过账时间(存盘时间)',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `producerId` int(11) DEFAULT NULL COMMENT '外键制单人',
  `postUsreId` int(11) DEFAULT NULL COMMENT '外键过账人',
  `printAmout` int(11) DEFAULT NULL COMMENT '打印次数',
  `money` decimal(30,4) DEFAULT NULL COMMENT '单据发生金额',
  `billTypeId` int(11) DEFAULT NULL COMMENT '外键单据类型',
  `unitId` int(11) DEFAULT NULL COMMENT '外键往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '外键职员',
  `departmentId` int(11) DEFAULT NULL COMMENT '外键外键部门',
  `inStorageId` int(11) DEFAULT NULL COMMENT '外键头部入库ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '外键头部出库ID',
  `accountsId` int(11) DEFAULT NULL COMMENT '外键会计科目ID',
  `accountsIds` varchar(100) DEFAULT NULL COMMENT '收付款账户ID  用 ,拼接',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `bb_businessdraft` */

DROP TABLE IF EXISTS `bb_businessdraft`;

CREATE TABLE `bb_businessdraft` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID业务草稿',
  `billId` int(11) DEFAULT NULL COMMENT '单据ID外键',
  `billCode` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `isRCW` int(11) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `postTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `producerId` int(11) DEFAULT NULL COMMENT '外键制单人',
  `postUsreId` int(11) DEFAULT NULL COMMENT '外键过账人',
  `printAmout` int(11) DEFAULT NULL COMMENT '打印次数',
  `money` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `billTypeId` int(11) DEFAULT NULL COMMENT '外键单据类型',
  `unitId` int(11) DEFAULT NULL COMMENT '外键往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '外键职员',
  `departmentId` int(11) DEFAULT NULL COMMENT '外键部门',
  `inStorageId` int(11) DEFAULT NULL COMMENT '外键头部入库ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '外键头部出库ID',
  `accountsId` int(11) DEFAULT NULL COMMENT '外键会计科目ID',
  `accountsIds` varchar(100) DEFAULT NULL COMMENT '收付款账户ID  用 ,拼接',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_adjust_cost_bill` */

DROP TABLE IF EXISTS `cc_adjust_cost_bill`;

CREATE TABLE `cc_adjust_cost_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '成本调价单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `lastMoneys` decimal(30,4) DEFAULT NULL COMMENT '调整后金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '调整前金额',
  `adjustMoneys` decimal(30,4) DEFAULT NULL COMMENT '调整金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_adjust_cost_detail` */

DROP TABLE IF EXISTS `cc_adjust_cost_detail`;

CREATE TABLE `cc_adjust_cost_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '成本调价单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `lastPrice` decimal(30,4) DEFAULT NULL COMMENT '调价后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `lastMoney` decimal(30,4) DEFAULT NULL COMMENT '调价后金额',
  `adjustMoney` decimal(30,4) DEFAULT NULL COMMENT '调整金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '进货时平均成本价',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_adjust_cost_draft_bill` */

DROP TABLE IF EXISTS `cc_adjust_cost_draft_bill`;

CREATE TABLE `cc_adjust_cost_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '成本调价单草稿',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `lastMoneys` decimal(30,4) DEFAULT NULL COMMENT '调整后金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '调整前金额',
  `adjustMoneys` decimal(30,4) DEFAULT NULL COMMENT '调整金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_adjust_cost_draft_detail` */

DROP TABLE IF EXISTS `cc_adjust_cost_draft_detail`;

CREATE TABLE `cc_adjust_cost_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '成本调价单明细草稿',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `lastPrice` decimal(30,4) DEFAULT NULL COMMENT '调价后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `lastMoney` decimal(30,4) DEFAULT NULL COMMENT '调价后金额',
  `adjustMoney` decimal(30,4) DEFAULT NULL COMMENT '调整金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '进货时平均成本价',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_breakage_bill` */

DROP TABLE IF EXISTS `cc_breakage_bill`;

CREATE TABLE `cc_breakage_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报损_单据',
  `code` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `isRCW` int(1) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_breakage_detail` */

DROP TABLE IF EXISTS `cc_breakage_detail`;

CREATE TABLE `cc_breakage_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报损单_明细',
  `billId` int(11) DEFAULT NULL COMMENT '报损单据ID，外键',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位(1,2,3)',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_breakage_draft_bill` */

DROP TABLE IF EXISTS `cc_breakage_draft_bill`;

CREATE TABLE `cc_breakage_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报损_单据',
  `code` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `isRCW` int(1) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_breakage_draft_detail` */

DROP TABLE IF EXISTS `cc_breakage_draft_detail`;

CREATE TABLE `cc_breakage_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报损单_明细',
  `billId` int(11) DEFAULT NULL COMMENT '报损单据ID，外键',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位(1,2,3)',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_difftallot_bill` */

DROP TABLE IF EXISTS `cc_difftallot_bill`;

CREATE TABLE `cc_difftallot_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '变价调拨',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `outStorageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT '1' COMMENT '状态:',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据总金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_difftallot_detail` */

DROP TABLE IF EXISTS `cc_difftallot_detail`;

CREATE TABLE `cc_difftallot_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '变价调拨单明细',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `inStorageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折后单价',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '折后金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_difftallot_draft_bill` */

DROP TABLE IF EXISTS `cc_difftallot_draft_bill`;

CREATE TABLE `cc_difftallot_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '变价调拨',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `outStorageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT '1' COMMENT '状态:',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据总金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_difftallot_draft_detail` */

DROP TABLE IF EXISTS `cc_difftallot_draft_detail`;

CREATE TABLE `cc_difftallot_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '变价调拨单明细',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `inStorageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折后单价',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '折后金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_dismount_bill` */

DROP TABLE IF EXISTS `cc_dismount_bill`;

CREATE TABLE `cc_dismount_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '拆装单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '入库数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '入库金额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '出库数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '出库金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_dismount_detail` */

DROP TABLE IF EXISTS `cc_dismount_detail`;

CREATE TABLE `cc_dismount_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '拆装单明细',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_dismount_draft_bill` */

DROP TABLE IF EXISTS `cc_dismount_draft_bill`;

CREATE TABLE `cc_dismount_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '拆装单草稿',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '入库数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '入库金额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '出库数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '出库金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_dismount_draft_detail` */

DROP TABLE IF EXISTS `cc_dismount_draft_detail`;

CREATE TABLE `cc_dismount_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '拆装单明细草稿',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherin_bill` */

DROP TABLE IF EXISTS `cc_otherin_bill`;

CREATE TABLE `cc_otherin_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它入库单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `accountsId` varchar(100) DEFAULT NULL COMMENT '会计科目ID',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherin_detail` */

DROP TABLE IF EXISTS `cc_otherin_detail`;

CREATE TABLE `cc_otherin_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它入库单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cc_otherin_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherin_draft_bill` */

DROP TABLE IF EXISTS `cc_otherin_draft_bill`;

CREATE TABLE `cc_otherin_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它入库单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `accountsId` varchar(100) DEFAULT NULL COMMENT '会计科目ID',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherin_draft_detail` */

DROP TABLE IF EXISTS `cc_otherin_draft_detail`;

CREATE TABLE `cc_otherin_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它入库单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cc_otherin_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherout_bill` */

DROP TABLE IF EXISTS `cc_otherout_bill`;

CREATE TABLE `cc_otherout_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它出库单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `accountsId` varchar(100) DEFAULT NULL COMMENT '会计科目ID',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherout_detail` */

DROP TABLE IF EXISTS `cc_otherout_detail`;

CREATE TABLE `cc_otherout_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它出库单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cc_otherout_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherout_draft_bill` */

DROP TABLE IF EXISTS `cc_otherout_draft_bill`;

CREATE TABLE `cc_otherout_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它出库单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `accountsId` varchar(100) DEFAULT NULL COMMENT '会计科目ID',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_otherout_draft_detail` */

DROP TABLE IF EXISTS `cc_otherout_draft_detail`;

CREATE TABLE `cc_otherout_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它出库单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cc_otherout_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_overflow_bill` */

DROP TABLE IF EXISTS `cc_overflow_bill`;

CREATE TABLE `cc_overflow_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报溢_单据',
  `code` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `isRCW` int(1) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_overflow_detail` */

DROP TABLE IF EXISTS `cc_overflow_detail`;

CREATE TABLE `cc_overflow_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报溢单_明细',
  `billId` int(11) DEFAULT NULL COMMENT '报溢单据ID，外键',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位(1,2,3)',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `batch` varchar(100) DEFAULT NULL COMMENT '生产日期',
  `produceDate` date DEFAULT NULL COMMENT '当前商品总金额',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_overflow_draft_bill` */

DROP TABLE IF EXISTS `cc_overflow_draft_bill`;

CREATE TABLE `cc_overflow_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报溢_单据',
  `code` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单时间',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `isRCW` int(1) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_overflow_draft_detail` */

DROP TABLE IF EXISTS `cc_overflow_draft_detail`;

CREATE TABLE `cc_overflow_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_报溢单_明细',
  `billId` int(11) DEFAULT NULL COMMENT '报溢单据ID，外键',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位(1,2,3)',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `batch` varchar(100) DEFAULT NULL COMMENT '生产日期',
  `produceDate` date DEFAULT NULL COMMENT '当前商品总金额',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_parityallot_bill` */

DROP TABLE IF EXISTS `cc_parityallot_bill`;

CREATE TABLE `cc_parityallot_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '同价调拨',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `outStorageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_parityallot_detail` */

DROP TABLE IF EXISTS `cc_parityallot_detail`;

CREATE TABLE `cc_parityallot_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '同价调拨单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `inStorageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价    废除字段',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_parityallot_draft_bill` */

DROP TABLE IF EXISTS `cc_parityallot_draft_bill`;

CREATE TABLE `cc_parityallot_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '同价调拨草稿',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `outStorageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `inStorageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0没红冲，1被红冲，2反红冲',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_parityallot_draft_detail` */

DROP TABLE IF EXISTS `cc_parityallot_draft_detail`;

CREATE TABLE `cc_parityallot_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '同价调拨单草稿明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `outStorageId` int(11) DEFAULT NULL COMMENT '发货仓库ID',
  `inStorageId` int(11) DEFAULT NULL COMMENT '收货仓库ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_stock` */

DROP TABLE IF EXISTS `cc_stock`;

CREATE TABLE `cc_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_库存',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '库存数量',
  `draftAmount` decimal(30,4) DEFAULT NULL COMMENT '草稿数量    废除字段',
  `virtualAmount` decimal(30,4) DEFAULT NULL COMMENT '虚拟库存数量       废除字段',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `batchNum` varchar(255) DEFAULT NULL COMMENT '批号',
  `storageId` int(11) DEFAULT NULL COMMENT '所属仓库',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_stock_bound` */

DROP TABLE IF EXISTS `cc_stock_bound`;

CREATE TABLE `cc_stock_bound` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '库存上下限',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `max` decimal(30,4) DEFAULT NULL COMMENT '库存上限',
  `min` decimal(30,4) DEFAULT NULL COMMENT '库存下限',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_stock_init` */

DROP TABLE IF EXISTS `cc_stock_init`;

CREATE TABLE `cc_stock_init` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_期初商品库存',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '期初数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '期初成本单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '期初金额',
  `batch` varchar(255) DEFAULT NULL COMMENT '批号',
  `storageId` int(11) DEFAULT NULL COMMENT '所属仓库',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_stock_records` */

DROP TABLE IF EXISTS `cc_stock_records`;

CREATE TABLE `cc_stock_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品库存记录表',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间    单据头表创建时间',
  `billTypeId` int(11) DEFAULT NULL COMMENT '单据类型ID',
  `billId` int(11) DEFAULT NULL COMMENT '单据头表ID',
  `billCode` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeTime` datetime DEFAULT NULL COMMENT '单据录单时间',
  `billAbstract` varchar(500) DEFAULT NULL COMMENT '单据摘要',
  `isRCW` int(1) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位ID',
  `storageId` int(11) DEFAULT NULL COMMENT '操作的仓库ID',
  `batch` varchar(200) DEFAULT NULL COMMENT '批号',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `price` decimal(30,4) DEFAULT NULL COMMENT '成本单价  最小单位',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '税后单价',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '税后总金额',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `selectUnitId` int(11) DEFAULT NULL COMMENT '计量单位',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '入库数量   最小单位',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '出库数量   最小单位',
  `remainAmount` decimal(30,4) DEFAULT NULL COMMENT '库存余量     废除字段',
  `remainMoney` decimal(30,4) DEFAULT NULL COMMENT '库存余额     废除字段',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_stock_records_draft` */

DROP TABLE IF EXISTS `cc_stock_records_draft`;

CREATE TABLE `cc_stock_records_draft` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '草稿商品库存记录表',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `billTypeId` int(11) DEFAULT NULL COMMENT '单据类型ID',
  `billId` int(11) DEFAULT NULL COMMENT '单据头表ID',
  `billCode` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeTime` datetime DEFAULT NULL COMMENT '单据录单时间',
  `billAbstract` varchar(500) DEFAULT NULL COMMENT '单据摘要',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位ID',
  `storageId` int(11) DEFAULT NULL COMMENT '操作的仓库ID',
  `batch` varchar(200) DEFAULT NULL COMMENT '批号',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `price` decimal(30,4) DEFAULT NULL COMMENT '成本单价  最小单位',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '税后单价',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '税后总金额',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `selectUnitId` int(11) DEFAULT NULL COMMENT '计量单位',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '入库数量   最小单位',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '出库数量   最小单位',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_takestock_bill` */

DROP TABLE IF EXISTS `cc_takestock_bill`;

CREATE TABLE `cc_takestock_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_库存盘点_单据',
  `code` varchar(255) DEFAULT NULL COMMENT '单据编号',
  `createTime` datetime DEFAULT NULL COMMENT '录单时间',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `storageId` int(11) DEFAULT NULL COMMENT '盘点仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '合计盘点数量',
  `sckAmounts` decimal(30,4) DEFAULT NULL COMMENT '合计库存量',
  `sckMoneys` decimal(30,4) DEFAULT NULL COMMENT '合计库存金额',
  `gainAndLossAmounts` decimal(30,4) DEFAULT NULL COMMENT '合计盈亏数量',
  `gainAndLossMoneys` decimal(30,4) DEFAULT NULL COMMENT '合计盈亏金额',
  `sckRetailMoney` decimal(30,4) DEFAULT NULL COMMENT '合计零售金额',
  `status` int(1) DEFAULT '1' COMMENT '状态(1,未处理;2已处理)',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cc_takestock_detail` */

DROP TABLE IF EXISTS `cc_takestock_detail`;

CREATE TABLE `cc_takestock_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '仓储_库存盘点单_明细',
  `billId` int(11) DEFAULT NULL COMMENT '盘点单据ID，外键',
  `productId` int(11) DEFAULT NULL COMMENT '库存商品，外键',
  `takeStockAmount` decimal(30,4) DEFAULT NULL COMMENT '盘点数量',
  `stockAmount` decimal(30,4) DEFAULT NULL COMMENT '库存数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `gainAndLossAmount` decimal(30,4) DEFAULT NULL COMMENT '盈亏数量',
  `gainAndLossMoney` decimal(30,4) DEFAULT NULL COMMENT '盈亏金额',
  `batch` varchar(255) DEFAULT NULL COMMENT '批号',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_barter_bill` */

DROP TABLE IF EXISTS `cg_barter_bill`;

CREATE TABLE `cg_barter_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货换货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `type` int(1) DEFAULT NULL COMMENT '换货类型(0正常换货，1坏损换货)',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '换入数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '换入金额',
  `gapMoney` decimal(30,4) DEFAULT NULL COMMENT '换货差额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '换出数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '换出金额',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_barter_detail` */

DROP TABLE IF EXISTS `cg_barter_detail`;

CREATE TABLE `cg_barter_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货换货单明细',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `status` int(1) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_barter_draft_bill` */

DROP TABLE IF EXISTS `cg_barter_draft_bill`;

CREATE TABLE `cg_barter_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货换货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `type` int(1) DEFAULT NULL COMMENT '换货类型(0正常换货，1坏损换货)',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '换入数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '换入金额',
  `gapMoney` decimal(30,4) DEFAULT NULL COMMENT '换货差额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '换出数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '换出金额',
  `status` int(1) DEFAULT NULL COMMENT '状态',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_barter_draft_detail` */

DROP TABLE IF EXISTS `cg_barter_draft_detail`;

CREATE TABLE `cg_barter_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货换货单明细',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `status` int(1) DEFAULT NULL,
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_bought_bill` */

DROP TABLE IF EXISTS `cg_bought_bill`;

CREATE TABLE `cg_bought_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '采购订单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '供货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `deliveryDate` date DEFAULT NULL COMMENT '交货日期',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前后金额',
  `status` int(1) DEFAULT '1' COMMENT '状态:1未完成，2完成',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(30,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `arrivalAmounts` decimal(30,4) DEFAULT NULL COMMENT '到货数量',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_bought_detail` */

DROP TABLE IF EXISTS `cg_bought_detail`;

CREATE TABLE `cg_bought_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '采购订单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(30,4) DEFAULT NULL COMMENT '到货数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（bill）',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号',
  `untreatedAmount` decimal(30,4) DEFAULT NULL COMMENT '未完成数量',
  `replenishAmount` decimal(30,4) DEFAULT NULL COMMENT '补充数量',
  `forceAmount` decimal(30,4) DEFAULT NULL COMMENT '强制完成数',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `bargainMoney` decimal(30,4) DEFAULT NULL COMMENT '订单金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_purchase_bill` */

DROP TABLE IF EXISTS `cg_purchase_bill`;

CREATE TABLE `cg_purchase_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '供货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `payDate` date DEFAULT NULL COMMENT '付款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(30,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `delayDeliveryDate` date DEFAULT NULL COMMENT '延期付款日期',
  `isWarn` int(1) DEFAULT NULL COMMENT '是否取消超期应付款报警   0.启动     1.取消',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_purchase_detail` */

DROP TABLE IF EXISTS `cg_purchase_detail`;

CREATE TABLE `cg_purchase_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `detailId` int(11) DEFAULT NULL COMMENT '订单明细id',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `returnAmount` decimal(30,4) DEFAULT NULL COMMENT '退回数量',
  `untreatedAmount` decimal(30,4) DEFAULT NULL COMMENT '未处理数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '进货时平均成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_purchase_draft_bill` */

DROP TABLE IF EXISTS `cg_purchase_draft_bill`;

CREATE TABLE `cg_purchase_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '供货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `payDate` date DEFAULT NULL COMMENT '付款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(30,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `delayDeliveryDate` date DEFAULT NULL COMMENT '延期付款日期',
  `isWarn` int(1) DEFAULT NULL COMMENT '是否取消超期应付款报警   0.启动     1.取消',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_purchase_draft_detail` */

DROP TABLE IF EXISTS `cg_purchase_draft_detail`;

CREATE TABLE `cg_purchase_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `detailId` int(11) DEFAULT NULL COMMENT '订单明细id',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `returnAmount` decimal(30,4) DEFAULT NULL COMMENT '退回数量',
  `untreatedAmount` decimal(30,4) DEFAULT NULL COMMENT '未处理数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '进货时平均成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_return_bill` */

DROP TABLE IF EXISTS `cg_return_bill`;

CREATE TABLE `cg_return_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货退货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '收货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(30,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲，1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_return_detail` */

DROP TABLE IF EXISTS `cg_return_detail`;

CREATE TABLE `cg_return_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货退货单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(30,4) DEFAULT NULL COMMENT '处理数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `detailId` int(11) DEFAULT NULL COMMENT '进货单明细id',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本单价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_return_draft_bill` */

DROP TABLE IF EXISTS `cg_return_draft_bill`;

CREATE TABLE `cg_return_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货退货草稿单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '收货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `storageId` int(11) DEFAULT NULL COMMENT '收货仓库',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(30,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(30,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(30,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(30,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲，1被红冲，2反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cg_return_draft_detail` */

DROP TABLE IF EXISTS `cg_return_draft_detail`;

CREATE TABLE `cg_return_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '进货退货草稿单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(30,4) DEFAULT NULL COMMENT '处理数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(30,4) DEFAULT NULL COMMENT '开票金额',
  `detailId` int(11) DEFAULT NULL COMMENT '进货单明细id',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本单价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_accounts_init` */

DROP TABLE IF EXISTS `cw_accounts_init`;

CREATE TABLE `cw_accounts_init` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '财务期初数据',
  `unitId` int(11) DEFAULT NULL COMMENT '单位ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目Id',
  `money` decimal(30,4) DEFAULT NULL COMMENT '期初金额',
  `time` varchar(20) CHARACTER SET utf8 COLLATE utf8_czech_ci DEFAULT NULL COMMENT '跟开账时间相等',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_accountvoucher_bill` */

DROP TABLE IF EXISTS `cw_accountvoucher_bill`;

CREATE TABLE `cw_accountvoucher_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会计凭证',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `debitMoneys` decimal(30,4) DEFAULT NULL COMMENT '借方金额',
  `lendMoneys` decimal(30,4) DEFAULT NULL COMMENT '贷方金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_accountvoucher_detail` */

DROP TABLE IF EXISTS `cw_accountvoucher_detail`;

CREATE TABLE `cw_accountvoucher_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会计凭证明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '总会计科目ID',
  `accountsDetail` char(200) DEFAULT NULL COMMENT '明细会计科目',
  `debitMoney` decimal(30,4) DEFAULT NULL COMMENT '借方金额',
  `lendMoney` decimal(30,4) DEFAULT NULL COMMENT '贷方金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  `detailId` int(11) DEFAULT NULL COMMENT '明细会计科目Id',
  `unitId` int(11) DEFAULT NULL COMMENT '单位Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_accountvoucher_draft_bill` */

DROP TABLE IF EXISTS `cw_accountvoucher_draft_bill`;

CREATE TABLE `cw_accountvoucher_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会计凭证草稿',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `debitMoneys` decimal(30,4) DEFAULT NULL COMMENT '借方金额',
  `lendMoneys` decimal(30,4) DEFAULT NULL COMMENT '贷方金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_accountvoucher_draft_detail` */

DROP TABLE IF EXISTS `cw_accountvoucher_draft_detail`;

CREATE TABLE `cw_accountvoucher_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会计凭证明细草稿',
  `accountsId` int(11) DEFAULT NULL COMMENT '总会计科目ID',
  `accountsDetail` char(200) DEFAULT NULL COMMENT '明细会计科目',
  `debitMoney` decimal(30,4) DEFAULT NULL COMMENT '借方金额',
  `lendMoney` decimal(30,4) DEFAULT NULL COMMENT '贷方金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  `detailId` int(11) DEFAULT NULL COMMENT '明细会计科目Id',
  `unitId` int(11) DEFAULT NULL COMMENT '单位Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_addassets_bill` */

DROP TABLE IF EXISTS `cw_addassets_bill`;

CREATE TABLE `cw_addassets_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产购置单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '供货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `payAccountId` int(11) DEFAULT NULL COMMENT '付款账户',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '实付金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_addassets_detail` */

DROP TABLE IF EXISTS `cw_addassets_detail`;

CREATE TABLE `cw_addassets_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产购置单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_addassets_draft_bill` */

DROP TABLE IF EXISTS `cw_addassets_draft_bill`;

CREATE TABLE `cw_addassets_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产购置单草稿',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '供货单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `payAccountId` int(11) DEFAULT NULL COMMENT '付款账户',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '实付金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_addassets_draft_detail` */

DROP TABLE IF EXISTS `cw_addassets_draft_detail`;

CREATE TABLE `cw_addassets_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产购置单明细草稿',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_arap_records` */

DROP TABLE IF EXISTS `cw_arap_records`;

CREATE TABLE `cw_arap_records` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '应收应付记录表',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  `billTypeId` int(11) DEFAULT NULL COMMENT '单据类型ID',
  `billId` int(11) DEFAULT NULL COMMENT '单据头表ID',
  `billCode` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeTime` datetime DEFAULT NULL COMMENT '单据录单时间',
  `billAbstract` varchar(500) DEFAULT NULL COMMENT '单据摘要',
  `isRCW` int(11) DEFAULT '0' COMMENT '是否红冲(0:否，1：被红冲，2：红字反冲)',
  `areaId` int(11) DEFAULT NULL COMMENT '地区ID',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位ID',
  `staffId` int(11) DEFAULT NULL COMMENT '职员ID',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门ID',
  `addMoney` decimal(30,4) DEFAULT NULL COMMENT '增加金额',
  `subMoney` decimal(30,4) DEFAULT NULL COMMENT '减少金额',
  `addPrepay` decimal(30,4) DEFAULT NULL COMMENT '预付增加',
  `subPrepay` decimal(30,4) DEFAULT NULL COMMENT '预付减少',
  `getMoney` decimal(30,4) DEFAULT NULL COMMENT '收款金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_money_bill` */

DROP TABLE IF EXISTS `cw_c_money_bill`;

CREATE TABLE `cw_c_money_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `mergeType` int(11) DEFAULT NULL COMMENT '0资金减少    1资金增加  ',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_money_detail` */

DROP TABLE IF EXISTS `cw_c_money_detail`;

CREATE TABLE `cw_c_money_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_money_draft_bill` */

DROP TABLE IF EXISTS `cw_c_money_draft_bill`;

CREATE TABLE `cw_c_money_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `mergeType` int(11) DEFAULT NULL COMMENT '0资金减少    1资金增加  ',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_money_draft_detail` */

DROP TABLE IF EXISTS `cw_c_money_draft_detail`;

CREATE TABLE `cw_c_money_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_unitgetorpay_bill` */

DROP TABLE IF EXISTS `cw_c_unitgetorpay_bill`;

CREATE TABLE `cw_c_unitgetorpay_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '调账-单位应收应付',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `mergeType` int(11) DEFAULT NULL COMMENT '0应收减少    1应收增加    2应付减少   3 应付增加',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_unitgetorpay_detail` */

DROP TABLE IF EXISTS `cw_c_unitgetorpay_detail`;

CREATE TABLE `cw_c_unitgetorpay_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '调账-单位应收应付详情',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_unitgetorpay_draft_bill` */

DROP TABLE IF EXISTS `cw_c_unitgetorpay_draft_bill`;

CREATE TABLE `cw_c_unitgetorpay_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '调账-单位应收应付-草稿',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `mergeType` int(11) DEFAULT NULL COMMENT '0应收减少    1应收增加    2应付减少   3 应付增加',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_c_unitgetorpay_draft_detail` */

DROP TABLE IF EXISTS `cw_c_unitgetorpay_draft_detail`;

CREATE TABLE `cw_c_unitgetorpay_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_deprassets_bill` */

DROP TABLE IF EXISTS `cw_deprassets_bill`;

CREATE TABLE `cw_deprassets_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产折旧单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_deprassets_detail` */

DROP TABLE IF EXISTS `cw_deprassets_detail`;

CREATE TABLE `cw_deprassets_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产折旧单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_deprassets_draft_bill` */

DROP TABLE IF EXISTS `cw_deprassets_draft_bill`;

CREATE TABLE `cw_deprassets_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产折旧单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_deprassets_draft_detail` */

DROP TABLE IF EXISTS `cw_deprassets_draft_detail`;

CREATE TABLE `cw_deprassets_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产折旧单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_fee_bill` */

DROP TABLE IF EXISTS `cw_fee_bill`;

CREATE TABLE `cw_fee_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_fee_detail` */

DROP TABLE IF EXISTS `cw_fee_detail`;

CREATE TABLE `cw_fee_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_fee_draft_bill` */

DROP TABLE IF EXISTS `cw_fee_draft_bill`;

CREATE TABLE `cw_fee_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_fee_draft_detail` */

DROP TABLE IF EXISTS `cw_fee_draft_detail`;

CREATE TABLE `cw_fee_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '费用单',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_freight` */

DROP TABLE IF EXISTS `cw_freight`;

CREATE TABLE `cw_freight` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '运费',
  `unitId` int(8) DEFAULT NULL COMMENT '运费单位',
  `billId` int(8) DEFAULT NULL COMMENT '单据ID',
  `payBankIds` varchar(500) DEFAULT NULL COMMENT '付款收款类型集合   eg:1,2',
  `payMoneys` varchar(500) DEFAULT NULL COMMENT '付款收款金额集合   eg:10.2,84',
  `totalMoney` decimal(30,4) DEFAULT NULL COMMENT '总金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_getmoney_bill` */

DROP TABLE IF EXISTS `cw_getmoney_bill`;

CREATE TABLE `cw_getmoney_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '收款单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `preGetMoney` decimal(20,4) DEFAULT NULL COMMENT '预收账款',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `canAssignMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_getmoney_detail` */

DROP TABLE IF EXISTS `cw_getmoney_detail`;

CREATE TABLE `cw_getmoney_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '收款单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cw_getmoney_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_getmoney_draft_bill` */

DROP TABLE IF EXISTS `cw_getmoney_draft_bill`;

CREATE TABLE `cw_getmoney_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '收款单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `preGetMoney` decimal(20,4) DEFAULT NULL COMMENT '预收账款',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `canAssignMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_getmoney_draft_detail` */

DROP TABLE IF EXISTS `cw_getmoney_draft_detail`;

CREATE TABLE `cw_getmoney_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '收款单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cw_getmoney_draft_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_otherearn_bill` */

DROP TABLE IF EXISTS `cw_otherearn_bill`;

CREATE TABLE `cw_otherearn_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它收入单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_otherearn_detail` */

DROP TABLE IF EXISTS `cw_otherearn_detail`;

CREATE TABLE `cw_otherearn_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其他收入单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_otherearn_draft_bill` */

DROP TABLE IF EXISTS `cw_otherearn_draft_bill`;

CREATE TABLE `cw_otherearn_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其它收入单草稿',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '付款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `getMoney` decimal(20,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲:1单据被红冲   2单据反红冲',
  `accountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_otherearn_draft_detail` */

DROP TABLE IF EXISTS `cw_otherearn_draft_detail`;

CREATE TABLE `cw_otherearn_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '其他收入单明细草稿',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_pay_by_draft_order` */

DROP TABLE IF EXISTS `cw_pay_by_draft_order`;

CREATE TABLE `cw_pay_by_draft_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '支付  收款',
  `unitId` int(8) DEFAULT NULL COMMENT '单位ID',
  `type` int(1) DEFAULT NULL COMMENT '0收款单，1付款单',
  `billTypeId` int(3) DEFAULT NULL COMMENT '结算单据类型',
  `billId` int(8) DEFAULT NULL COMMENT '结算单据ID',
  `cwBillTypeId` int(8) DEFAULT NULL COMMENT 'eg：收款单单据类型',
  `cwBillId` int(8) DEFAULT NULL COMMENT 'eg：收款单头表ID',
  `lastMoney` decimal(30,4) DEFAULT NULL COMMENT '余额',
  `settlementAmount` decimal(30,4) DEFAULT NULL COMMENT '结算金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_pay_by_order` */

DROP TABLE IF EXISTS `cw_pay_by_order`;

CREATE TABLE `cw_pay_by_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '支付  收款',
  `unitId` int(8) DEFAULT NULL COMMENT '单位ID',
  `type` int(1) DEFAULT NULL COMMENT '0收款单，1付款单',
  `billTypeId` int(3) DEFAULT NULL COMMENT '结算单据类型',
  `billId` int(8) DEFAULT NULL COMMENT '结算单据ID',
  `cwBillTypeId` int(8) DEFAULT NULL COMMENT 'eg：收款单单据类型',
  `cwBillId` int(8) DEFAULT NULL COMMENT 'eg：收款单头表ID',
  `lastMoney` decimal(30,4) DEFAULT NULL COMMENT '余额',
  `settlementAmount` decimal(30,4) DEFAULT NULL COMMENT '结算金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_pay_draft` */

DROP TABLE IF EXISTS `cw_pay_draft`;

CREATE TABLE `cw_pay_draft` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '草稿会计科目历史记录',
  `billTypeId` int(11) DEFAULT NULL COMMENT '单据类型',
  `billId` int(11) DEFAULT NULL COMMENT '单据ID',
  `unitId` int(11) DEFAULT NULL COMMENT '供应商  客户',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门ID',
  `staffId` int(11) DEFAULT NULL COMMENT '职员ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `hasAccount` int(11) DEFAULT NULL COMMENT '是否是发生往来科目 0没有   1有',
  `type` int(1) DEFAULT NULL COMMENT '0正数/1负数   相对于会计科目所在的类型  如优惠    进货优惠为负，销售优惠为正',
  `accountId` int(11) DEFAULT NULL COMMENT '付款收款账户ID',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款收款金额',
  `accountType` int(11) DEFAULT NULL COMMENT '0.单据表体  1.收付款  2.优惠金额,3.往来应收/付,4.系统科目(eg:未分配利润)',
  `payTime` timestamp NULL DEFAULT NULL COMMENT '操作时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_pay_type` */

DROP TABLE IF EXISTS `cw_pay_type`;

CREATE TABLE `cw_pay_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '会计科目历史记录',
  `billTypeId` int(11) DEFAULT NULL COMMENT '单据类型',
  `billId` int(11) DEFAULT NULL COMMENT '单据ID',
  `unitId` int(11) DEFAULT NULL COMMENT '供应商  客户',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门ID',
  `staffId` int(11) DEFAULT NULL COMMENT '职员ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `hasAccount` int(11) DEFAULT NULL COMMENT '是否是发生往来科目 0没有   1有',
  `type` int(1) DEFAULT NULL COMMENT '0正数/1负数      ',
  `accountId` int(11) DEFAULT NULL COMMENT '付款收款账户ID',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款收款金额',
  `accountType` int(11) DEFAULT NULL COMMENT '0.单据表体  1.收付款  2.优惠金额,3.往来应收/付,4.系统科目(eg:未分配利润)',
  `payTime` timestamp NULL DEFAULT NULL COMMENT '操作时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_paymoney_bill` */

DROP TABLE IF EXISTS `cw_paymoney_bill`;

CREATE TABLE `cw_paymoney_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '付款单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '收款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据金额',
  `preGetMoney` decimal(30,4) DEFAULT NULL COMMENT '预付账款',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `canAssignMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_paymoney_detail` */

DROP TABLE IF EXISTS `cw_paymoney_detail`;

CREATE TABLE `cw_paymoney_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '付款单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cw_paymoney_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_paymoney_draft_bill` */

DROP TABLE IF EXISTS `cw_paymoney_draft_bill`;

CREATE TABLE `cw_paymoney_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '付款单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '收款单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '单据金额',
  `preGetMoney` decimal(30,4) DEFAULT NULL COMMENT '预付账款',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `canAssignMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '可分配优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_paymoney_draft_detail` */

DROP TABLE IF EXISTS `cw_paymoney_draft_detail`;

CREATE TABLE `cw_paymoney_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '付款单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（cw_paymoney_bill）',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_subassets_bill` */

DROP TABLE IF EXISTS `cw_subassets_bill`;

CREATE TABLE `cw_subassets_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产变卖单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '收购单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `getAccountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `getMoney` decimal(30,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_subassets_detail` */

DROP TABLE IF EXISTS `cw_subassets_detail`;

CREATE TABLE `cw_subassets_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产变卖单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_subassets_draft_bill` */

DROP TABLE IF EXISTS `cw_subassets_draft_bill`;

CREATE TABLE `cw_subassets_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产变卖单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '收购单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `getAccountId` int(11) DEFAULT NULL COMMENT '收款账户',
  `getMoney` decimal(30,4) DEFAULT NULL COMMENT '实收金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_subassets_draft_detail` */

DROP TABLE IF EXISTS `cw_subassets_draft_detail`;

CREATE TABLE `cw_subassets_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '固定资产变卖单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_transfer_bill` */

DROP TABLE IF EXISTS `cw_transfer_bill`;

CREATE TABLE `cw_transfer_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '内部转款单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `payAccountId` int(11) DEFAULT NULL COMMENT '付款账户',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '实付金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_transfer_detail` */

DROP TABLE IF EXISTS `cw_transfer_detail`;

CREATE TABLE `cw_transfer_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '内部转账单明细',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_transfer_draft_bill` */

DROP TABLE IF EXISTS `cw_transfer_draft_bill`;

CREATE TABLE `cw_transfer_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '内部转款单草稿',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门Id',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `moneys` decimal(30,4) DEFAULT NULL COMMENT '金额',
  `payAccountId` int(11) DEFAULT NULL COMMENT '付款账户',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '实付金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `isRCW` int(1) DEFAULT NULL COMMENT '0单据没红冲:1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `cw_transfer_draft_detail` */

DROP TABLE IF EXISTS `cw_transfer_draft_detail`;

CREATE TABLE `cw_transfer_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '内部转账单明细草稿',
  `accountsId` int(11) DEFAULT NULL COMMENT '会计科目ID',
  `money` decimal(20,4) DEFAULT NULL COMMENT '金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '单据备注 ',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `delivery_company` */

DROP TABLE IF EXISTS `delivery_company`;

CREATE TABLE `delivery_company` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '快递公司',
  `name` char(200) DEFAULT NULL COMMENT '名称',
  `code` char(100) DEFAULT NULL COMMENT '编号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `fz_formula` */

DROP TABLE IF EXISTS `fz_formula`;

CREATE TABLE `fz_formula` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '计算公式表 eq设置上下限公式',
  `billTypeId` int(3) DEFAULT NULL COMMENT '单据类型',
  `type` int(1) DEFAULT NULL COMMENT '某条公式      eq上限:1，下限:-1',
  `item` int(3) DEFAULT NULL COMMENT '运行符前面参数     项目,负数为销售，正数为进货',
  `operate` varchar(5) DEFAULT NULL COMMENT '运算符',
  `param` decimal(30,4) DEFAULT NULL COMMENT '运行符后面参数',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `fz_minsell_price` */

DROP TABLE IF EXISTS `fz_minsell_price`;

CREATE TABLE `fz_minsell_price` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '最低销售表',
  `storageId` int(11) DEFAULT NULL COMMENT '外键：仓库ID',
  `minSellPrice` decimal(30,4) DEFAULT NULL COMMENT '最低销售价',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC;

/*Table structure for table `fz_pricediscount_track` */

DROP TABLE IF EXISTS `fz_pricediscount_track`;

CREATE TABLE `fz_pricediscount_track` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID经营历程',
  `unitId` int(11) DEFAULT NULL COMMENT '单位ID外键',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID外键',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `lastCostPrice` decimal(20,4) DEFAULT NULL COMMENT '最近进价',
  `lastSellPrice` decimal(20,4) DEFAULT NULL COMMENT '最近售价',
  `lastCostDiscouont` decimal(20,4) DEFAULT '1.0000' COMMENT '最近进货折扣',
  `lastSellDiscouont` decimal(20,4) DEFAULT '1.0000' COMMENT '最近销售折扣',
  `lastCostDate` datetime DEFAULT NULL COMMENT '最近进货时间',
  `lastSellDate` datetime DEFAULT NULL COMMENT '最近销售时间',
  `isMark` int(1) DEFAULT '0' COMMENT '是否标记 0没有   1有',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `fz_produce_template` */

DROP TABLE IF EXISTS `fz_produce_template`;

CREATE TABLE `fz_produce_template` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '辅助表_生产模板',
  `tmpName` varchar(200) DEFAULT NULL COMMENT '模板名称',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `fz_produce_template_detail` */

DROP TABLE IF EXISTS `fz_produce_template_detail`;

CREATE TABLE `fz_produce_template_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '商品生产模板明细',
  `tmpId` int(11) DEFAULT NULL COMMENT '模板ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `assortAmount` decimal(30,4) DEFAULT NULL COMMENT '配套数量',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `fz_report_row` */

DROP TABLE IF EXISTS `fz_report_row`;

CREATE TABLE `fz_report_row` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '报表行列表',
  `name` varchar(100) NOT NULL COMMENT '默认列名',
  `showName` varchar(100) DEFAULT NULL COMMENT '显示列名',
  `code` varchar(100) DEFAULT NULL COMMENT '列编号',
  `reportType` varchar(100) DEFAULT NULL COMMENT '报表行分类（前缀加数字，数字从500-1000）',
  `status` int(1) DEFAULT '1' COMMENT '状态：1不显示，2显示',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `width` int(11) DEFAULT NULL COMMENT '列宽度',
  `isOrder` int(1) DEFAULT '2' COMMENT 'order by：1不过滤，2过滤',
  `isCount` int(1) DEFAULT '1' COMMENT '1没有合计，2有合计',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `om_port_record` */

DROP TABLE IF EXISTS `om_port_record`;

CREATE TABLE `om_port_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `unitId` int(11) DEFAULT NULL COMMENT '简易单位ID',
  `sellbookId` int(11) DEFAULT NULL COMMENT '销售订单ID',
  `omOrderCode` varchar(100) DEFAULT NULL COMMENT 'om订单系统订单ID',
  `name` varchar(100) DEFAULT NULL COMMENT '订单系统收货客户公司',
  `linkman` varchar(50) DEFAULT NULL COMMENT '订单系统收货客户名称',
  `phone` varchar(50) DEFAULT NULL COMMENT '订单系统收货客户电话',
  `address` varchar(250) DEFAULT NULL COMMENT '订单系统收货客户地址',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `om_port_record_detail` */

DROP TABLE IF EXISTS `om_port_record_detail`;

CREATE TABLE `om_port_record_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sellbookDetaillId` int(11) DEFAULT NULL COMMENT '销售订单明细ID',
  `omOrderId` int(11) DEFAULT NULL COMMENT 'om订单系统订单ID',
  `hasSentAmount` decimal(20,4) DEFAULT NULL COMMENT '发货数量',
  `billId` int(11) DEFAULT NULL COMMENT '头表ID',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `om_port_user` */

DROP TABLE IF EXISTS `om_port_user`;

CREATE TABLE `om_port_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `unitId` int(11) DEFAULT NULL COMMENT '简易单位ID',
  `storageIds` varchar(250) DEFAULT NULL COMMENT '简易单位所属那些仓库',
  `loginName` varchar(50) DEFAULT NULL COMMENT 'om订单系统 登录用户名',
  `loginPwd` varchar(50) DEFAULT NULL COMMENT 'om订单系统 登录密码',
  `portKey` varchar(250) DEFAULT NULL COMMENT 'om订单系统 调用接口密钥',
  `loginPwdInfo` varchar(250) DEFAULT NULL COMMENT '密码提示',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `status` int(11) DEFAULT NULL COMMENT '登录状态 0已删除  1停用 2正常  启用 ',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_bill_row` */

DROP TABLE IF EXISTS `sys_bill_row`;

CREATE TABLE `sys_bill_row` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '单据行列表',
  `name` varchar(100) NOT NULL COMMENT '默认列名',
  `showName` varchar(100) DEFAULT NULL COMMENT '显示列名',
  `code` varchar(100) DEFAULT NULL COMMENT '列编号',
  `billId` int(11) DEFAULT NULL COMMENT '单据类型表Id关联（sys_billType）',
  `status` int(1) DEFAULT '1' COMMENT '状态：1不显示，2显示',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `width` int(11) DEFAULT NULL COMMENT '列宽度',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  `isSys` int(1) DEFAULT '0' COMMENT '是系统不能隐藏：0否，1是',
  `linkage` varchar(100) DEFAULT NULL COMMENT '联动',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_billcode_flow` */

DROP TABLE IF EXISTS `sys_billcode_flow`;

CREATE TABLE `sys_billcode_flow` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '单据流水号Id',
  `codeIncrease` int(11) DEFAULT '0' COMMENT '流水号',
  `type` char(1) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '类型：0：每日递增，1：每月递增，2：每年递增',
  `updateDate` date DEFAULT NULL COMMENT '日期',
  `billId` int(11) DEFAULT NULL COMMENT '单据类型',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC;

/*Table structure for table `sys_billcodeconfig` */

DROP TABLE IF EXISTS `sys_billcodeconfig`;

CREATE TABLE `sys_billcodeconfig` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '单据编号格式配置',
  `billId` int(11) DEFAULT NULL COMMENT '单据类型ID',
  `format1` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式1',
  `format2` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式2',
  `format3` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式3',
  `format4` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式4',
  `format5` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式5',
  `format6` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式6',
  `format7` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式7',
  `format8` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式8',
  `format9` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式9',
  `format10` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '格式10',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC;

/*Table structure for table `sys_billcodeconfig_format` */

DROP TABLE IF EXISTS `sys_billcodeconfig_format`;

CREATE TABLE `sys_billcodeconfig_format` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '格式定义表',
  `formatCh` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '中文格式',
  `formatHg` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '横杠格式',
  `formatDd` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '点格式',
  `formatNo` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '没有格式',
  `formatKg` char(50) CHARACTER SET utf8 COLLATE utf8_danish_ci DEFAULT NULL COMMENT '空格格式',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC;

/*Table structure for table `sys_billsort` */

DROP TABLE IF EXISTS `sys_billsort`;

CREATE TABLE `sys_billsort` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID,单据类别',
  `name` varchar(255) DEFAULT NULL COMMENT '单据类别名称',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_billtype` */

DROP TABLE IF EXISTS `sys_billtype`;

CREATE TABLE `sys_billtype` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID，单据类型',
  `name` varchar(255) DEFAULT NULL COMMENT '单据类型名称',
  `sortId` int(11) DEFAULT NULL COMMENT '单据类别ID',
  `prefix` varchar(50) DEFAULT NULL COMMENT '单据类型字头',
  `biillTableName` varchar(255) DEFAULT NULL COMMENT '对应的表名字',
  `isBusiness` int(1) DEFAULT '1' COMMENT '是否是业务单据，0:无影响的辅助单据，1:业务单据',
  `isArap` int(1) DEFAULT NULL COMMENT '是否有往来  0无  1有',
  `supId` int(1) DEFAULT NULL COMMENT '父级ID',
  `mergeType` int(1) DEFAULT NULL COMMENT '多个业务共用一张表（mergeType原表区分）   各自的取值对照自己的业务',
  `node` int(1) DEFAULT NULL COMMENT '节点 1节点   2分类',
  `itemNum` varchar(255) DEFAULT NULL COMMENT '单据特有明细字段(0.会员卡情况1.优惠情况2.单价 3.折扣 4.税率)eg:销售单01234',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_monthend` */

DROP TABLE IF EXISTS `sys_monthend`;

CREATE TABLE `sys_monthend` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '月结存',
  `monthsName` varchar(255) DEFAULT NULL COMMENT '账龄',
  `startDate` date DEFAULT NULL COMMENT '起始日期',
  `endDate` date DEFAULT NULL COMMENT '结束日期',
  `billCount` int(11) DEFAULT NULL COMMENT '本月单数',
  `monthCount` int(1) DEFAULT NULL COMMENT '月结期间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(220) CHARACTER SET utf8 DEFAULT NULL COMMENT '用户名',
  `password` varchar(500) CHARACTER SET utf8 DEFAULT NULL COMMENT '密码',
  `isOnline` int(1) DEFAULT NULL COMMENT '1、离线，2、在线',
  `lastTime` timestamp NULL DEFAULT NULL COMMENT '上次登录时间',
  `lastIp` char(50) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '上次登录Ip',
  `addTime` timestamp NULL DEFAULT NULL COMMENT '添加时间',
  `status` int(1) DEFAULT '1' COMMENT '1、停用，2、启用',
  `privs` longtext COLLATE utf8_czech_ci COMMENT '系统权限',
  `staffId` int(11) DEFAULT NULL COMMENT '关联职员',
  `grade` int(1) DEFAULT NULL COMMENT '1、普通用户，2、高级，3、超级管理员',
  `productPrivs` longtext COLLATE utf8_czech_ci COMMENT '商品权限',
  `unitPrivs` longtext COLLATE utf8_czech_ci COMMENT '单位权限',
  `storagePrivs` longtext COLLATE utf8_czech_ci COMMENT '仓库权限',
  `accountPrivs` longtext COLLATE utf8_czech_ci COMMENT '会计科目权限',
  `areaPrivs` longtext COLLATE utf8_czech_ci COMMENT '地区权限',
  `staffPrivs` longtext COLLATE utf8_czech_ci COMMENT '职员权限',
  `departmentPrivs` longtext COLLATE utf8_czech_ci COMMENT '部门权限',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='用户表';

/*Table structure for table `sys_yearend` */

DROP TABLE IF EXISTS `sys_yearend`;

CREATE TABLE `sys_yearend` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '年结存',
  `yearsName` varchar(255) DEFAULT NULL COMMENT '年份',
  `startDate` date DEFAULT NULL COMMENT '起始日期',
  `endDate` date DEFAULT NULL COMMENT '结束日期',
  `billCount` int(11) DEFAULT NULL COMMENT '年度单据单数',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_barter_bill` */

DROP TABLE IF EXISTS `xs_barter_bill`;

CREATE TABLE `xs_barter_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售换货单',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `type` int(1) DEFAULT NULL COMMENT '换货类型(正常换货，坏损换货)',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `payDate` date DEFAULT NULL COMMENT '付款日期',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '换入数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '换入金额',
  `gapMoney` decimal(30,4) DEFAULT NULL COMMENT '换货差额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '换出数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '换出金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `inTaxes` decimal(30,4) DEFAULT NULL COMMENT '换入总税额',
  `outTaxes` decimal(30,4) DEFAULT NULL COMMENT '换出总税额',
  `inDiscountMoneys` decimal(30,4) DEFAULT NULL COMMENT '换入折后总金额',
  `outDiscountMoneys` decimal(30,4) DEFAULT NULL COMMENT '换出折后总金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_barter_detail` */

DROP TABLE IF EXISTS `xs_barter_detail`;

CREATE TABLE `xs_barter_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售换货单明细',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `costMoneys` decimal(30,4) DEFAULT NULL COMMENT '成本总金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_barter_draft_bill` */

DROP TABLE IF EXISTS `xs_barter_draft_bill`;

CREATE TABLE `xs_barter_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售换货单草稿',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `unitId` int(11) DEFAULT NULL COMMENT '往来单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(11) DEFAULT NULL COMMENT '部门',
  `outStorageId` int(11) DEFAULT NULL COMMENT '换出仓库',
  `type` int(1) DEFAULT NULL COMMENT '换货类型(正常换货，坏损换货)',
  `inStorageId` int(11) DEFAULT NULL COMMENT '换入仓库',
  `privilege` decimal(30,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(30,4) DEFAULT NULL COMMENT '优惠后金额',
  `payDate` date DEFAULT NULL COMMENT '付款日期',
  `remark` varchar(1000) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(1000) DEFAULT NULL COMMENT '附加说明',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `settlementAmount` decimal(20,4) DEFAULT NULL COMMENT '结算金额   按单结算 ',
  `payMoney` decimal(30,4) DEFAULT NULL COMMENT '付款金额',
  `status` int(1) DEFAULT '1' COMMENT '状态：1未完成，2完成',
  `isRCW` int(1) DEFAULT '0' COMMENT '红冲：0未红冲,1被红冲，2反红冲',
  `inAmount` decimal(30,4) DEFAULT NULL COMMENT '换入数量',
  `inMoney` decimal(30,4) DEFAULT NULL COMMENT '换入金额',
  `gapMoney` decimal(30,4) DEFAULT NULL COMMENT '换货差额',
  `outAmount` decimal(30,4) DEFAULT NULL COMMENT '换出数量',
  `outMoney` decimal(30,4) DEFAULT NULL COMMENT '换出金额',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `inTaxes` decimal(30,4) DEFAULT NULL COMMENT '换入总税额',
  `outTaxes` decimal(30,4) DEFAULT NULL COMMENT '换出总税额',
  `inDiscountMoneys` decimal(30,4) DEFAULT NULL COMMENT '换入折后总金额',
  `outDiscountMoneys` decimal(30,4) DEFAULT NULL COMMENT '换出折后总金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_barter_draft_detail` */

DROP TABLE IF EXISTS `xs_barter_draft_detail`;

CREATE TABLE `xs_barter_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售换货单明细草稿',
  `type` int(1) DEFAULT '1' COMMENT '状态：1换入，2换出',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品引用单位',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '商品数量',
  `price` decimal(30,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(30,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(30,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(30,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(30,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `taxRate` decimal(30,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(30,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(30,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(30,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(30,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `updateTime` datetime DEFAULT NULL COMMENT '修改时间',
  `baseAmount` decimal(30,4) DEFAULT NULL COMMENT '基本数量',
  `basePrice` decimal(30,4) DEFAULT NULL COMMENT '基本单价',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `giftAmount` decimal(30,4) DEFAULT NULL COMMENT '赠品数量',
  `costMoneys` decimal(30,4) DEFAULT NULL COMMENT '成本金额',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_return_bill` */

DROP TABLE IF EXISTS `xs_return_bill`;

CREATE TABLE `xs_return_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售退货单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `deliveryDate` date DEFAULT NULL COMMENT '收款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(20,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(20,4) DEFAULT NULL COMMENT '含税总额',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `bankPay` varchar(20) DEFAULT NULL COMMENT '付款账户   废除',
  `payMoney` decimal(20,4) DEFAULT NULL COMMENT '收款金额  废除',
  `payTypeIds` varchar(100) DEFAULT NULL COMMENT '付款账户ID  用 ,拼接  废除',
  `payTypeMoneys` varchar(100) DEFAULT NULL COMMENT '付款账户ID的金额  用 ,拼接   废除',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.红冲/删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '是否红冲   0没有    1红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_return_detail` */

DROP TABLE IF EXISTS `xs_return_detail`;

CREATE TABLE `xs_return_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售退货单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(20,4) DEFAULT NULL COMMENT '到货数量',
  `untreatedAmount` decimal(20,4) DEFAULT NULL COMMENT '未完成数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `costMoneys` decimal(20,4) DEFAULT NULL COMMENT '当前成本单价*数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(20,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(20,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(20,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `taxRate` decimal(20,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(20,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(20,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `detailId` int(11) DEFAULT NULL COMMENT '销售订单明细id',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_return_draft_bill` */

DROP TABLE IF EXISTS `xs_return_draft_bill`;

CREATE TABLE `xs_return_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售退货单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `deliveryDate` date DEFAULT NULL COMMENT '收款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(20,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(20,4) DEFAULT NULL COMMENT '含税总额',
  `retailMoneys` decimal(30,4) DEFAULT NULL COMMENT '零售金额',
  `bankPay` varchar(20) DEFAULT NULL COMMENT '付款账户',
  `payMoney` decimal(20,4) DEFAULT NULL COMMENT '收款金额',
  `payTypeIds` varchar(100) DEFAULT NULL COMMENT '付款账户ID  用 ,拼接',
  `payTypeMoneys` varchar(100) DEFAULT NULL COMMENT '付款账户ID的金额  用 ,拼接',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.红冲/删除, 1.审核前   2.审核中 3.审核后',
  `isRCW` int(1) DEFAULT NULL COMMENT '是否红冲   0没有    1红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_return_draft_detail` */

DROP TABLE IF EXISTS `xs_return_draft_detail`;

CREATE TABLE `xs_return_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售退货单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(20,4) DEFAULT NULL COMMENT '到货数量',
  `untreatedAmount` decimal(20,4) DEFAULT NULL COMMENT '未完成数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `costMoneys` decimal(20,4) DEFAULT NULL COMMENT '当前成本单价*数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(20,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(20,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(20,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `taxRate` decimal(20,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(20,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(20,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `detailId` int(11) DEFAULT NULL COMMENT '销售订单明细id',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sell_bill` */

DROP TABLE IF EXISTS `xs_sell_bill`;

CREATE TABLE `xs_sell_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `deliveryDate` date DEFAULT NULL COMMENT '收款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(20,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(20,4) DEFAULT NULL COMMENT '含税总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(20,4) DEFAULT NULL COMMENT '开票金额',
  `bankPay` varchar(20) DEFAULT NULL COMMENT '付款账户    废除',
  `payMoney` decimal(20,4) DEFAULT NULL COMMENT '收款金额   废除',
  `payTypeIds` varchar(100) DEFAULT NULL COMMENT '付款账户ID  用 ,拼接     废除',
  `payTypeMoneys` varchar(100) DEFAULT NULL COMMENT '付款账户ID的金额  用 ,拼接    废除',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `waybillnumber` varchar(100) DEFAULT NULL COMMENT '运单号',
  `deliveryCompanyCode` varchar(100) DEFAULT NULL COMMENT '快递公司编号',
  `deliveryCompany` varchar(100) DEFAULT NULL COMMENT '快递公司',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.红冲/删除, 1.审核前   2.审核中 3.审核后',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `delayDeliveryDate` date DEFAULT NULL COMMENT '延期收款日期',
  `isWarn` int(1) DEFAULT NULL COMMENT '是否取消超期应收款报警   0.启动     1.取消',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sell_detail` */

DROP TABLE IF EXISTS `xs_sell_detail`;

CREATE TABLE `xs_sell_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(20,4) DEFAULT NULL COMMENT '到货数量',
  `untreatedAmount` decimal(20,4) DEFAULT NULL COMMENT '未完成数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `costMoneys` decimal(20,4) DEFAULT NULL COMMENT '当前成本单价*数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(20,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(20,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(20,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `taxRate` decimal(20,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(20,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(20,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(20,4) DEFAULT NULL COMMENT '开票金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（xs_sell）',
  `detailId` int(11) DEFAULT NULL COMMENT '销售订单明细id',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成  代表销售退货单引用销售单',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sell_draft_bill` */

DROP TABLE IF EXISTS `xs_sell_draft_bill`;

CREATE TABLE `xs_sell_draft_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(200) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `departmentId` int(5) DEFAULT NULL COMMENT '部门Id',
  `deliveryDate` date DEFAULT NULL COMMENT '收款日期',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(20,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(20,4) DEFAULT NULL COMMENT '含税总额',
  `retailMoneys` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoneys` decimal(20,4) DEFAULT NULL COMMENT '开票金额',
  `bankPay` varchar(20) DEFAULT NULL COMMENT '付款账户    废除',
  `payMoney` decimal(20,4) DEFAULT NULL COMMENT '收款金额',
  `payTypeIds` varchar(100) DEFAULT NULL COMMENT '付款账户ID  用 ,拼接    废除',
  `payTypeMoneys` varchar(100) DEFAULT NULL COMMENT '付款账户ID的金额  用 ,拼接    废除',
  `privilege` decimal(20,4) DEFAULT NULL COMMENT '优惠金额',
  `privilegeMoney` decimal(20,4) DEFAULT NULL COMMENT '优惠后金额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `waybillnumber` varchar(100) DEFAULT NULL COMMENT '运单号',
  `deliveryCompanyCode` varchar(100) DEFAULT NULL COMMENT '快递公司编号',
  `deliveryCompany` varchar(100) DEFAULT NULL COMMENT '快递公司',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.红冲/删除, 1.审核前   2.审核中 3.审核后',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '   0单据没红冲  1单据被红冲   2单据反红冲',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `delayDeliveryDate` date DEFAULT NULL COMMENT '延期收款日期',
  `isWarn` int(1) DEFAULT NULL COMMENT '是否取消超期应收款报警   0.启动     1.取消',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sell_draft_detail` */

DROP TABLE IF EXISTS `xs_sell_draft_detail`;

CREATE TABLE `xs_sell_draft_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售单明细',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(20,4) DEFAULT NULL COMMENT '到货数量',
  `untreatedAmount` decimal(20,4) DEFAULT NULL COMMENT '未完成数量',
  `costPrice` decimal(30,4) DEFAULT NULL COMMENT '当前成本价',
  `costMoneys` decimal(20,4) DEFAULT NULL COMMENT '当前成本单价*数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(20,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(20,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(20,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `taxRate` decimal(20,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(20,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(20,4) DEFAULT NULL COMMENT '含税金额',
  `retailPrice` decimal(20,4) DEFAULT NULL COMMENT '零售价',
  `retailMoney` decimal(20,4) DEFAULT NULL COMMENT '零售金额',
  `invoiceMoney` decimal(20,4) DEFAULT NULL COMMENT '开票金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（xs_sell）',
  `detailId` int(11) DEFAULT NULL COMMENT '销售订单明细id',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成  代表销售退货单引用销售单',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sellbook_bill` */

DROP TABLE IF EXISTS `xs_sellbook_bill`;

CREATE TABLE `xs_sellbook_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售订单',
  `recodeDate` datetime DEFAULT NULL COMMENT '录单日期',
  `code` varchar(100) DEFAULT NULL COMMENT '单据编号',
  `unitId` int(11) DEFAULT NULL COMMENT '购买单位',
  `staffId` int(11) DEFAULT NULL COMMENT '经手人',
  `deliveryDate` date DEFAULT NULL COMMENT '交货日期',
  `storageId` int(11) DEFAULT NULL COMMENT '发货仓库',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要',
  `memo` varchar(500) DEFAULT NULL COMMENT '附加说明',
  `discounts` decimal(20,4) DEFAULT NULL COMMENT '整单折扣',
  `amounts` decimal(20,4) DEFAULT NULL COMMENT '货品总数量',
  `moneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前总金额',
  `discountMoneys` decimal(20,4) DEFAULT NULL COMMENT '折扣前后金额',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '总税额',
  `taxMoneys` decimal(20,4) DEFAULT NULL COMMENT '含税总额',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `printNum` int(11) DEFAULT NULL COMMENT '打印次数',
  `status` int(1) DEFAULT NULL COMMENT '状态   0.红冲/删除, 1.审核前   2.审核中 3.审核后',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成,3强制完成',
  `isRCW` int(1) DEFAULT NULL COMMENT '是否红冲   0没有    1红冲     废除字段',
  `codeIncrease` int(11) DEFAULT NULL COMMENT '单据流水号',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `xs_sellbook_detail` */

DROP TABLE IF EXISTS `xs_sellbook_detail`;

CREATE TABLE `xs_sellbook_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '销售订单明细',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `selectUnitId` int(1) DEFAULT NULL COMMENT '商品单位编号 ',
  `produceDate` date DEFAULT NULL COMMENT '生产日期',
  `batch` varchar(100) DEFAULT NULL COMMENT '批号',
  `baseAmount` decimal(20,4) DEFAULT NULL COMMENT '最小单位数量',
  `amount` decimal(20,4) DEFAULT NULL COMMENT '商品数量',
  `arrivalAmount` decimal(20,4) DEFAULT NULL COMMENT '到货数量',
  `untreatedAmount` decimal(20,4) DEFAULT NULL COMMENT '未完成数量',
  `basePrice` decimal(20,4) DEFAULT NULL COMMENT '最小单位价格(三个价格的其中一个)',
  `price` decimal(20,4) DEFAULT NULL COMMENT '单价',
  `discount` decimal(20,4) DEFAULT NULL COMMENT '折扣',
  `discountPrice` decimal(20,4) DEFAULT NULL COMMENT '折扣后单价',
  `money` decimal(20,4) DEFAULT NULL COMMENT '当前商品总金额',
  `discountMoney` decimal(20,4) DEFAULT NULL COMMENT '当前商品折扣总金额',
  `taxRate` decimal(20,4) DEFAULT NULL COMMENT '税率',
  `taxPrice` decimal(20,4) DEFAULT NULL COMMENT '含税单价',
  `taxes` decimal(20,4) DEFAULT NULL COMMENT '税额',
  `taxMoney` decimal(20,4) DEFAULT NULL COMMENT '含税金额',
  `memo` varchar(1000) DEFAULT NULL COMMENT '备注',
  `message` varchar(200) DEFAULT NULL COMMENT '附加信息',
  `updateTime` datetime DEFAULT NULL COMMENT '存盘时间',
  `replenishAmount` decimal(30,4) DEFAULT NULL COMMENT '补充数量',
  `forceAmount` decimal(30,4) DEFAULT NULL COMMENT '强制完成数',
  `billId` int(11) DEFAULT NULL COMMENT '对应的父表（xs_sellbook）',
  `relStatus` int(1) DEFAULT NULL COMMENT '状态 1未完成，2完成  代表销售单引用销售订单',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `zj_product_avgprice` */

DROP TABLE IF EXISTS `zj_product_avgprice`;

CREATE TABLE `zj_product_avgprice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '对应（仓库-商品）平均成本价',
  `storageId` int(11) DEFAULT NULL COMMENT '仓库ID(为null时只统计商品)',
  `productId` int(11) DEFAULT NULL COMMENT '商品ID',
  `avgPrice` decimal(30,4) DEFAULT NULL COMMENT '平均成本价格',
  `costMoneys` decimal(30,4) DEFAULT NULL COMMENT '成本总金额',
  `amount` decimal(30,4) DEFAULT NULL COMMENT '总数量',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
