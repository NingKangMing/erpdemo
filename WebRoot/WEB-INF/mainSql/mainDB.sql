/*
SQLyog Community v11.31 (32 bit)
MySQL - 5.1.73 : Database - aioerp
*********************************************************************
*/


/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`aioerp` /*!40100 DEFAULT CHARACTER SET utf8 */;

USE `aioerp`;

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

/*Table structure for table `aioerp_sys_backup` */

DROP TABLE IF EXISTS `aioerp_sys_backup`;

CREATE TABLE `aioerp_sys_backup` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `dataBaseName` varchar(225) DEFAULT NULL COMMENT '数据库名',
  `whichDbName` varchar(225) DEFAULT NULL COMMENT '账套名称',
  `staff_id` int(11) DEFAULT NULL COMMENT '操作人ID',
  `staff_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `file_name` varchar(100) DEFAULT NULL COMMENT '文件名称',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aioerp_sys_backup_plan` */

DROP TABLE IF EXISTS `aioerp_sys_backup_plan`;

CREATE TABLE `aioerp_sys_backup_plan` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(225) DEFAULT NULL COMMENT '自动备份名称',
  `databaseIds` varchar(225) DEFAULT NULL COMMENT '自动备份数据ID集合',
  `startTime` varchar(225) DEFAULT NULL COMMENT '每天开始时间',
  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `aioerp_which_db` */

DROP TABLE IF EXISTS `aioerp_which_db`;

CREATE TABLE `aioerp_which_db` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `dataBaseName` VARCHAR(225) DEFAULT NULL COMMENT '数据库名',
  `whichDbCode` VARCHAR(225) DEFAULT NULL COMMENT '账套编号',
  `whichDbName` VARCHAR(225) DEFAULT NULL COMMENT '账套名称',
  `aioerpVersion` VARCHAR(225) DEFAULT NULL COMMENT '版本号',
  `status` INT(1) DEFAULT NULL COMMENT '1.停用  2.启动',
  `hasOm` INT(1) DEFAULT 1 COMMENT '1.停用  2.启用',
  `rank` INT(11) DEFAULT NULL COMMENT '排序',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `userId` INT(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

/*Table structure for table `sys_permission` */

DROP TABLE IF EXISTS `sys_permission`;

CREATE TABLE `sys_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限列表Id',
  `name` varchar(200) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '名称',
  `url` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '访问路径',
  `pids` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '父级ID集合',
  `seeUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '查看的url',
  `entUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '录入的url',
  `tranUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '过账url',
  `printUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '打印的url',
  `addUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '新增复制url',
  `editUrl` varchar(500) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '修改删除url',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='权限列表';

/*Table structure for table `sys_permission_type` */

DROP TABLE IF EXISTS `sys_permission_type`;

CREATE TABLE `sys_permission_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '权限类型',
  `name` varchar(200) COLLATE utf8_czech_ci DEFAULT NULL COMMENT '名称',
  `pid` int(11) DEFAULT NULL COMMENT '父类ID',
  `rank` int(11) DEFAULT NULL COMMENT '排序',
  `userId` int(11) DEFAULT NULL COMMENT '用户Id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='权限类型表';

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

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

/*主数据库有改动维护mianDatabase_sql_log.sql文件*/;