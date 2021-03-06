package com.aioerp.common;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AioConstants
{
  public static final int V = 2;
  public static String AIOERP_VERSION = "V3.2.001";
  public static final String LASTDBUPDATETIME = "20150611";
  public static boolean IO_OVER = false;
  public static Map<String, Integer> PROJECT_STATUS_MAP = new HashMap();
  public static int PROJECT_STATUS0 = 0;
  public static int PROJECT_STATUS1 = 1;
  public static int PROJECT_STATUS2 = 2;
  public static int PROJECT_STATUS3 = 3;
  public static int PROJECT_STATUS4 = 4;
  public static Map<Serializable, String> PROJECT_SESSION_MAP = new HashMap();
  public static List<String> CONFIGSESSIONNAMELIST = new ArrayList();
  public static Map<String, Map<String, Map<String, Record>>> SYS_PARAM = new HashMap();
  public static Map<String, Integer> WHICHDBID_STATUS = new HashMap();
  public static boolean PROJECT_IS_OK = false;
  public static boolean BACKUP_BY_CMD = false;
  public static boolean CREATE_PROC = false;
  public static boolean CALL_PROC = false;
  public static boolean INSERT_BILL = false;
  public static boolean BILL_COUNT_IS_OK = false;
  public static boolean RELOAD_RCW = true;
  public static boolean ISOPENUSERLOG = true;
  public static String RELOAD_PWD = "admin";
  public static String ENCODING = "UTF-8";
  public static int GRAP_TIME = 43200000;
  public static int TEST_BILL_COUNT = 100;
  public static int TEST_DAY = 30000000;
  public static Map<String, Integer> accountBillCount = new HashMap();
  public static final int AUTO_SPTIME = 1000;
  public static int UNIT_DISPLAY_COUNT = 20;
  public static int STORAGE_DISPLAY_COUNT = 20;
  public static String HAS_OPEN_ACCOUNT0 = "0";
  public static String HAS_OPEN_ACCOUNT1 = "1";
  public static int VERSION = 2;
  public static String IS_FREE_VERSION = "yes";
  public static String LOG_VERSION = "标准版";
  public static String SETFILEPATH = "web.config";
  public static String CONFIG_NAME = "main";
  public static int TOTAL_ACCOUNTSET = 5;
  public static String LOGIN_USERNAME = "admin";
  public static String LOGIN_PWD = "admin";
  public static String ALL_PRIVS = "*";
  public static final int PAGE_SIZE = 20;
  public static final int PAGE_SIZE_MAX = 9999999;
  public static final int DIALOG_PAGE_SIZE = 10;
  public static int START_PAGE = 1;
  public static int STATUS_DELETE = 0;
  public static int STATUS_DISABLE = 1;
  public static int STATUS_ENABLE = 2;
  public static int STATUS_FORCE = 3;
  public static int STATUS_NULL = 3;
  public static int BILL_STATUS0 = 0;
  public static int BILL_STATUS1 = 1;
  public static int BILL_STATUS2 = 2;
  public static int BILL_STATUS3 = 3;
  public static int RCW_NO = 0;
  public static int RCW_BY = 1;
  public static int RCW_VS = 2;
  public static int IS_DRAFT_NO = 0;
  public static int IS_DRAFT = 1;
  public static int NODE_1 = 1;
  public static int NODE_2 = 2;
  public static int PRD_COST_PRICE1 = 1;
  public static int PRD_COST_PRICE4 = 4;
  public static String HTTP_RETURN200 = "200";
  public static String HTTP_RETURN300 = "300";
  public static String HTTP_RETURN301 = "301";
  public static int BILL_TYPE_0 = 0;
  public static int BILL_TYPE_1 = 1;
  public static int BILL_TYPE_3 = 3;
  public static String CODE_CRATE_RULE0 = "0";
  public static String CODE_CRATE_RULE1 = "1";
  public static String CODE_CRATE_RULE2 = "2";
  public static String CODE_CRATE_RULE3 = "3";
  public static int NUMADD0 = 0;
  public static int NUMNOADD1 = 1;
  public static int FILE_TYPE1 = 1;
  public static int FILE_TYPE2 = 2;
  public static int TABLE_1 = -1;
  public static int TABLE_2 = -2;
  public static int GET_MONEY = 0;
  public static int OUT_MONEY = 1;
  public static int OVERGETPAY_WARN0 = 0;
  public static int OVERGETPAY_WARN1 = 1;
  public static int ISARAP0 = 0;
  public static int ISARAP1 = 1;
  public static int ARAPACCOUNT0 = 0;
  public static int ARAPACCOUNT1 = 1;
  public static int BILL_ROW_TYPE_1 = -1;
  public static int BILL_ROW_TYPE_2 = -2;
  public static int BILL_ROW_TYPE0 = 0;
  public static int BILL_ROW_TYPE1 = 1;
  public static int BILL_ROW_TYPE2 = 2;
  public static int BILL_ROW_TYPE3 = 3;
  public static int BILL_ROW_TYPE4 = 4;
  public static int BILL_ROW_TYPE5 = 5;
  public static int BILL_ROW_TYPE6 = 6;
  public static int BILL_ROW_TYPE7 = 7;
  public static int BILL_ROW_TYPE8 = 8;
  public static int BILL_ROW_TYPE9 = 9;
  public static int BILL_ROW_TYPE10 = 10;
  public static int BILL_ROW_TYPE11 = 11;
  public static int BILL_ROW_TYPE12 = 12;
  public static int BILL_ROW_TYPE13 = 13;
  public static int BILL_ROW_TYPE14 = 14;
  public static int BILL_ROW_TYPE15 = 15;
  public static int BILL_ROW_TYPE16 = 16;
  public static int BILL_ROW_TYPE17 = 17;
  public static int BILL_ROW_TYPE18 = 18;
  public static int BILL_ROW_TYPE19 = 19;
  public static int BILL_ROW_TYPE20 = 20;
  public static int BILL_ROW_TYPE21 = 21;
  public static int BILL_ROW_TYPE22 = 22;
  public static int BILL_ROW_TYPE23 = 23;
  public static int BILL_ROW_TYPE24 = 24;
  public static int BILL_ROW_TYPE25 = 25;
  public static int BILL_ROW_TYPE26 = 26;
  public static int BILL_ROW_TYPE27 = 27;
  public static int BILL_ROW_TYPE28 = 28;
  public static int BILL_ROW_TYPE29 = 29;
  public static int BILL_ROW_TYPE30 = 30;
  public static int BILL_ROW_TYPE31 = 31;
  public static int BILL_ROW_TYPE32 = 32;
  public static int BILL_ROW_TYPE33 = 33;
  public static int BILL_ROW_TYPE34 = 34;
  public static int BILL_ROW_TYPE500 = 500;
  public static int BILL_ROW_TYPE501 = 501;
  public static int BILL_ROW_TYPE2000 = 2000;
  public static int BILL_ROW_TYPE2001 = 2001;
  public static int BILL_ROW_TYPE2002 = 2002;
  public static String BILL_SORT0 = "isBusiness";
  public static String BILL_SORT1 = "isArap";
  public static String BILL_SORT2 = "isFlow";
  public static String OPERTE_ADD = "add";
  public static String OPERTE_RCW = "rcw";
  public static String OPERTE_DEL = "del";
  public static int PAY_TYLE0 = 0;
  public static int PAY_TYLE1 = 1;
  public static int PAY_TYLE2 = 2;
  public static int PAY_TYLE3 = 3;
  public static int WAY_PAY = -1;
  public static int WAY_GET = 1;
  public static int ACCOUNT_MONEY0 = 0;
  public static int ACCOUNT_MONEY1 = 1;
  public static int ACCOUNT_TYPE0 = 0;
  public static int ACCOUNT_TYPE1 = 1;
  public static int ACCOUNT_TYPE2 = 2;
  public static int ACCOUNT_TYPE3 = 3;
  public static int ACCOUNT_TYPE4 = 4;
  public static String DATE_TYPE = "yyyy-MM-dd HH:mm:ss";
  public static final String GET_MONEY_TYPE = "order";
  public static final boolean NEGATIVE_STOCK11 = true;
  public static final boolean V_NEGATIVE_STOCK_SHOW11 = true;
  public static final boolean V_NEGATIVE_STOCK_PASS11 = false;
  public static final String UNIT_COST_PRICE11 = "yes";
  public static final String UNIT_SELL_PRICE11 = "yes";
  public static final String UNIT_COST_DISCOUNT11 = "yes";
  public static final String UNIT_SELL_DISCOUNT11 = "yes";
  public static final int SYS_CONFIG1 = 1;
  public static final int SYS_CONFIG2 = 2;
  public static final int SYS_CONFIG3 = 3;
  public static final int SYS_CONFIG4 = 4;
  public static final int SYS_CONFIG5 = 5;
  public static final int SYS_CONFIG6 = 6;
  public static final int SYS_CONFIG7 = 7;
  public static final int SYS_CONFIG8 = 8;
  public static final int SYS_CONFIG9 = 9;
  public static final int SYS_CONFIG10 = 10;
  public static final int SYS_CONFIG11 = 11;
  public static final int SYS_CONFIG12 = 12;
  public static final int SYS_CONFIG13 = 13;
  public static final int SYS_CONFIG14 = 14;
  public static final int SYS_CONFIG15 = 15;
  public static final int DETAIL_TYPE_IN = 1;
  public static final int DETAIL_TYPE_OUT = 2;
  public static final int BILL_SORT_XS = 1;
  public static final int BILL_SORT_CG = 2;
  public static final int BILL_SORT_CC = 3;
  public static final int BILL_SORT_WX = 4;
  public static final int BILL_SORT_WL = 5;
  public static final int BILL_SORT_CW = 6;
  public static final int BILL_SORT_QT = 7;
  public static final int USER_GRADE_COM = 1;
  public static final int USER_GRADE_ADV = 2;
  public static final int USER_GRADE_SUP = 3;
  public static int AHEAD_DAY = 0;
  public static int PAY_AHEAD_DAY = 0;
  public static int GET_AHEAD_DAY = 0;
  public static int BOUGHT_AHEAD_DAY = 0;
  public static int SELL_AHEAD_DAY = 0;
  public static int PRIVS_STATUS_NO = 1;
  public static int PRIVS_STATUS_PART = 2;
  public static int PRIVS_STATUS_All = 3;
  public static int BILL_TYPE_JH = 1;
  public static int BILL_TYPE_XS = 2;
  public static final String DEFAULT_BACKUP_STAT_TIME = "18:00:00";
  public static List<Model> permissionList = new ArrayList();
  public static final String TOTALROW = "totalRow";
  public static final String TOTALPAGE = "totalPage";
  public static final String PAGEINDEX = "pageIndex";
  public static final String PAGESIZE = "pageSize";
}
