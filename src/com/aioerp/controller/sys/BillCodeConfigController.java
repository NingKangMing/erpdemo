package com.aioerp.controller.sys;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.BillCodeConfig;
import com.aioerp.model.sys.BillCodeConfigFormat;
import com.aioerp.model.sys.BillCodeFlow;
import com.aioerp.model.sys.BillSort;
import com.aioerp.model.sys.BillType;
import com.aioerp.util.DateUtils;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.log.Log4jLogger;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class BillCodeConfigController
  extends BaseController
{
  private static Logger log = Log4jLogger.getLogger(BillCodeConfigController.class);
  
  public void index()
    throws Exception
  {
    String configName = loginConfigName();
    List<Model> typeList = BillSort.dao.getList(configName);
    Map<String, Object> pageMap = BillType.dao.getFormatPage(configName, "", AioConstants.START_PAGE, 20, Integer.valueOf(1));
    List<Model> pageList = (List)pageMap.get("pageList");
    String codeCustomChar = AioerpSys.dao.getValue1(configName, "codeCustomChar");
    String codeFlowLen = AioerpSys.dao.getValue1(configName, "codeFlowLen");
    int len = 0;
    if (StringUtils.isNotBlank(codeFlowLen)) {
      len = Integer.parseInt(codeFlowLen);
    }
    List<Model> newList = new ArrayList();
    if (pageList != null) {
      for (int i = 0; i < pageList.size(); i++)
      {
        Model model = (Model)pageList.get(i);
        String prefix = model.getStr("prefix");
        BillCodeConfig config = (BillCodeConfig)model.get("config");
        if ((config == null) || (config.get("id") == null))
        {
          newList.add(model);
        }
        else
        {
          String format = prefix;
          if (StringUtils.isNotBlank(codeCustomChar)) {
            format = format + "-" + codeCustomChar;
          }
          try
          {
            String formatStr = getFormatStr(configName, new String[] { config.getStr("format1"), config.getStr("format2"), config.getStr("format3"), config.getStr("format4"), config.getStr("format5"), config.getStr("format6"), config.getStr("format7"), config.getStr("format8"), config.getStr("format9"), config.getStr("format10") });
            if (StringUtils.isNotBlank(formatStr)) {
              format = format + "-" + formatStr;
            }
          }
          catch (ParseException e)
          {
            log.error(e.getMessage());
            e.printStackTrace();
          }
          format = format + "-" + getCodeFlow("1", len);
          
          model.put("preview", format);
          newList.add(model);
        }
      }
    }
    pageMap.remove("pageList");
    pageMap.put("pageList", newList);
    setAttr("typeList", typeList);
    setAttr("pageMap", pageMap);
    render("page.html");
  }
  
  public void list()
    throws Exception
  {
    String configName = loginConfigName();
    int sortId = getParaToInt(0, Integer.valueOf(1)).intValue();
    int pageNum = getParaToInt("pageNum", Integer.valueOf(AioConstants.START_PAGE)).intValue();
    int numPerPage = 20;
    numPerPage = getParaToInt("numPerPage", Integer.valueOf(20)).intValue();
    Map<String, Object> pageMap = BillType.dao.getFormatPage(configName, "billSortList", pageNum, numPerPage, Integer.valueOf(sortId));
    List<Model> pageList = (List)pageMap.get("pageList");
    String codeCustomChar = AioerpSys.dao.getValue1(configName, "codeCustomChar");
    String codeFlowLen = AioerpSys.dao.getValue1(configName, "codeFlowLen");
    int len = 0;
    if (StringUtils.isNotBlank(codeFlowLen)) {
      len = Integer.parseInt(codeFlowLen);
    }
    List<Model> newList = new ArrayList();
    if (pageList != null) {
      for (int i = 0; i < pageList.size(); i++)
      {
        Model model = (Model)pageList.get(i);
        String prefix = model.getStr("prefix");
        BillCodeConfig config = (BillCodeConfig)model.get("config");
        if ((config == null) || (config.get("id") == null))
        {
          newList.add(model);
        }
        else
        {
          String format = prefix;
          if (StringUtils.isNotBlank(codeCustomChar)) {
            format = format + "-" + codeCustomChar;
          }
          try
          {
            String formatStr = getFormatStr(configName, new String[] { config.getStr("format1"), config.getStr("format2"), config.getStr("format3"), config.getStr("format4"), config.getStr("format5"), config.getStr("format6"), config.getStr("format7"), config.getStr("format8"), config.getStr("format9"), config.getStr("format10") });
            if (StringUtils.isNotBlank(formatStr)) {
              format = format + "-" + formatStr;
            }
          }
          catch (ParseException e)
          {
            log.error(e.getMessage());
            e.printStackTrace();
          }
          format = format + "-" + getCodeFlow("1", len);
          
          model.put("preview", format);
          newList.add(model);
        }
      }
    }
    pageMap.remove("pageList");
    pageMap.put("pageList", newList);
    setAttr("sizeNum", Integer.valueOf(numPerPage));
    setAttr("pageMap", pageMap);
    setAttr("sortId", Integer.valueOf(sortId));
    render("list.html");
  }
  
  public void toConfig()
  {
    String configName = loginConfigName();
    int typeId = getParaToInt(0, Integer.valueOf(0)).intValue();
    BillType billType = (BillType)BillType.dao.findById(configName, Integer.valueOf(typeId));
    BillCodeConfig config = BillCodeConfig.dao.getObj(configName, Integer.valueOf(typeId));
    setAttr("billType", billType);
    setAttr("config", config);
    render("config.html");
  }
  
  public void config()
  {
    String configName = loginConfigName();
    Map<String, Object> result = new HashMap();
    Model model = (Model)getModel(BillCodeConfig.class);
    Integer id = model.getInt("id");
    String formatStr = "";
    
    String format1 = model.getStr("format1");
    String format2 = model.getStr("format2");
    String format3 = model.getStr("format3");
    String format4 = model.getStr("format4");
    String format5 = model.getStr("format5");
    String format6 = model.getStr("format6");
    String format7 = model.getStr("format7");
    String format8 = model.getStr("format8");
    String format9 = model.getStr("format9");
    String format10 = model.getStr("format10");
    if (StringUtils.isNotBlank(format1)) {
      formatStr = formatStr + format1;
    }
    if (StringUtils.isNotBlank(format2)) {
      formatStr = formatStr + format2;
    }
    if (StringUtils.isNotBlank(format3)) {
      formatStr = formatStr + format3;
    }
    if (StringUtils.isNotBlank(format4)) {
      formatStr = formatStr + format4;
    }
    if (StringUtils.isNotBlank(format5)) {
      formatStr = formatStr + format5;
    }
    if (StringUtils.isNotBlank(format6)) {
      formatStr = formatStr + format6;
    }
    if (StringUtils.isNotBlank(format7)) {
      formatStr = formatStr + format7;
    }
    if (StringUtils.isNotBlank(format8)) {
      formatStr = formatStr + format8;
    }
    if (StringUtils.isNotBlank(format9)) {
      formatStr = formatStr + format9;
    }
    if (StringUtils.isNotBlank(format10)) {
      formatStr = formatStr + format10;
    }
    if ((StringUtils.isBlank(format1)) && (StringUtils.isBlank(format2)) && (StringUtils.isBlank(format3)) && (StringUtils.isBlank(format4)) && (StringUtils.isBlank(format5)) && 
      (StringUtils.isBlank(format6)) && (StringUtils.isBlank(format7)) && (StringUtils.isBlank(format8)) && (StringUtils.isBlank(format9)) && (StringUtils.isBlank(format10)))
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "格式设置必填一项!");
      renderJson(result);
      return;
    }
    if (StringUtil.getWordLen(formatStr) >= 150)
    {
      result.put("statusCode", AioConstants.HTTP_RETURN300);
      result.put("message", "格式设置长度超出范围!");
      renderJson(result);
      return;
    }
    if (id == null) {
      model.save(configName);
    } else {
      model.update(configName);
    }
    result.put("statusCode", AioConstants.HTTP_RETURN200);
    result.put("rel", "billSortList");
    result.put("callbackType", "closeCurrent");
    renderJson(result);
  }
  
  public void toGlobal()
  {
    String configName = loginConfigName();
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    String codeCustomChar = AioerpSys.dao.getValue1(configName, "codeCustomChar");
    String codeDateSep = AioerpSys.dao.getValue1(configName, "codeDateSep");
    String codeIncreaseRule = AioerpSys.dao.getValue1(configName, "codeIncreaseRule");
    String codeSelfCreate = AioerpSys.dao.getValue1(configName, "codeSelfCreate");
    String codeAllowEdit = AioerpSys.dao.getValue1(configName, "codeAllowEdit");
    String codeCurrentFit = AioerpSys.dao.getValue1(configName, "codeCurrentFit");
    String codeAllowRep = AioerpSys.dao.getValue1(configName, "codeAllowRep");
    String codeFlowLen = AioerpSys.dao.getValue1(configName, "codeFlowLen");
    String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
    setAttr("codeCustomChar", codeCustomChar);
    setAttr("codeDateSep", codeDateSep);
    setAttr("codeIncreaseRule", codeIncreaseRule);
    setAttr("codeSelfCreate", codeSelfCreate);
    setAttr("codeAllowEdit", codeAllowEdit);
    setAttr("codeCurrentFit", codeCurrentFit);
    setAttr("codeAllowRep", codeAllowRep);
    setAttr("hasOpenAccount", hasOpenAccount);
    setAttr("codeFlowLen", codeFlowLen);
    setAttr("codeCreateRule", codeCreateRule);
    render("global.html");
  }
  
  @Before({Tx.class})
  public void global()
  {
    String configName = loginConfigName();
    String codeCustomChar = getPara("codeCustomChar");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeCustomChar", codeCustomChar, null);
    String codeDateSep = getPara("codeDateSep", "0");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeDateSep", codeDateSep, null);
    String codeIncreaseRule = getPara("codeIncreaseRule", "2");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeIncreaseRule", codeIncreaseRule, null);
    String codeSelfCreate = getPara("codeSelfCreate", "0");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeSelfCreate", codeSelfCreate, null);
    String codeAllowEdit = getPara("codeAllowEdit", "0");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeAllowEdit", codeAllowEdit, null);
    String codeCurrentFit = getPara("codeCurrentFit", "1");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeCurrentFit", codeCurrentFit, null);
    String codeAllowRep = getPara("codeAllowRep", "1");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeAllowRep", codeAllowRep, null);
    String codeFlowLen = getPara("codeFlowLen", "4");
    AioerpSys.dao.sysSaveOrUpdate(configName, "codeFlowLen", codeFlowLen, null);
    
    String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
    String newCodeCreateRule = getPara("codeCreateRule", "0");
    if ((("0".equals(codeCreateRule)) || ("2".equals(codeCreateRule))) && ("1".equals(newCodeCreateRule))) {
      BillCodeFlow.dao.batchUpdateZCodeNum(configName, codeIncreaseRule);
    }
    if (!codeCreateRule.equals(newCodeCreateRule)) {
      AioerpSys.dao.sysSaveOrUpdate(configName, "codeCreateRule", newCodeCreateRule, null);
    }
    Map<String, Object> result = new HashMap();
    result.put("statusCode", AioConstants.HTTP_RETURN200);
    result.put("rel", "billSortList");
    result.put("callbackType", "closeCurrent");
    renderJson(result);
  }
  
  public void toFormat()
  {
    String configName = loginConfigName();
    List<Model> formatList = BillCodeConfigFormat.dao.getList(configName);
    String codeDateSep = AioerpSys.dao.getValue1(configName, "codeDateSep");
    setAttr("codeDateSep", codeDateSep);
    setAttr("formatList", formatList);
    render("format.html");
  }
  
  public void preview()
  {
    String configName = loginConfigName();
    int billType = getParaToInt(0, Integer.valueOf(0)).intValue();
    String codeCustomChar = AioerpSys.dao.getValue1(configName, "codeCustomChar");
    String codeFlowLen = AioerpSys.dao.getValue1(configName, "codeFlowLen");
    String format1 = getPara("billCodeConfig.format1");
    String format2 = getPara("billCodeConfig.format2");
    String format3 = getPara("billCodeConfig.format3");
    String format4 = getPara("billCodeConfig.format4");
    String format5 = getPara("billCodeConfig.format5");
    String format6 = getPara("billCodeConfig.format6");
    String format7 = getPara("billCodeConfig.format7");
    String format8 = getPara("billCodeConfig.format8");
    String format9 = getPara("billCodeConfig.format9");
    String format10 = getPara("billCodeConfig.format10");
    int len = 0;
    if (StringUtils.isNotBlank(codeFlowLen)) {
      len = Integer.parseInt(codeFlowLen);
    }
    String format = "";
    BillType type = (BillType)BillType.dao.findById(configName, Integer.valueOf(billType));
    if (type != null) {
      format = format + type.getStr("prefix");
    }
    if (StringUtils.isNotBlank(codeCustomChar)) {
      format = format + "-" + codeCustomChar;
    }
    try
    {
      String formatStr = getFormatStr(configName, new String[] { format1, format2, format3, format4, format5, format6, format7, format8, format9, format10 });
      if (StringUtils.isNotBlank(formatStr)) {
        format = format + "-" + formatStr;
      }
    }
    catch (ParseException e)
    {
      log.error(e.getMessage());
      e.printStackTrace();
    }
    format = format + "-" + getCodeFlow("1", len);
    setAttr("format", format);
    render("preview.html");
  }
  
  public void clearOne()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    BillCodeConfig config = BillCodeConfig.dao.getObj(configName, Integer.valueOf(billId));
    if (config != null) {
      config.delete(configName);
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "billSortList");
    
    renderJson();
  }
  
  public void clearAll()
  {
    String configName = loginConfigName();
    BillCodeConfig.dao.delAll(configName);
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "billSortList");
    
    renderJson();
  }
  
  @Before({Tx.class})
  public void applyAll()
  {
    String configName = loginConfigName();
    int billId = getParaToInt(0, Integer.valueOf(0)).intValue();
    BillCodeConfig config = BillCodeConfig.dao.getObj(configName, Integer.valueOf(billId));
    if (config == null)
    {
      BillCodeConfig.dao.delAll(configName);
    }
    else
    {
      BillCodeConfig.dao.delAll(configName);
      List<Model> typeList = BillType.dao.getList(configName, "node", 1);
      BillCodeConfig newConfig = config;
      newConfig.set("billId", null);
      newConfig.set("id", null);
      for (Model model : typeList)
      {
        Integer typeId = model.getInt("id");
        BillCodeConfig codeConfig = new BillCodeConfig();
        codeConfig = newConfig;
        codeConfig.set("billId", typeId);
        codeConfig.set("id", null);
        codeConfig.save(configName);
      }
    }
    setAttr("statusCode", AioConstants.HTTP_RETURN200);
    setAttr("rel", "billSortList");
    
    renderJson();
  }
  
  public static String getCodeFlow(String CodeFlow, int len)
  {
    len -= CodeFlow.length();
    String subStr = "";
    for (int i = 0; i < len; i++) {
      subStr = subStr + "0";
    }
    return subStr + CodeFlow;
  }
  
  public static String getFormatStr(String configName, String... formats)
    throws ParseException
  {
    String formatStr = "";
    Date date = new Date();
    for (String format : formats) {
      if (StringUtils.isNotBlank(format))
      {
        BillCodeConfigFormat formatObj = BillCodeConfigFormat.dao.getObjByFormat(configName, format);
        if (formatObj != null) {
          formatStr = formatStr + DateUtils.format(date, format);
        } else {
          formatStr = formatStr + format;
        }
      }
    }
    return formatStr;
  }
  
  private static void updateCodeFlow(String configName, Integer billId, Integer codeIncrease, String createRule)
  {
    if ((createRule == null) && (codeIncrease == null)) {
      return;
    }
    String codeCreateRule = AioerpSys.dao.getValue1(configName, "codeCreateRule");
    String codeIncreaseRule = AioerpSys.dao.getValue1(configName, "codeIncreaseRule");
    BillCodeFlow codeFlow = BillCodeFlow.dao.getObj(configName, billId.intValue(), codeIncreaseRule);
    if ((!createRule.equals(codeCreateRule)) && (!createRule.equals("2"))) {
      return;
    }
    if (codeFlow != null)
    {
      codeFlow.set("updateDate", new Date());
      codeFlow.set("codeIncrease", codeIncrease);
      codeFlow.update(configName);
    }
    else
    {
      codeFlow = new BillCodeFlow();
      codeFlow.set("type", codeIncreaseRule);
      codeFlow.set("updateDate", new Date());
      codeFlow.set("codeIncrease", codeIncrease);
      codeFlow.set("billId", billId);
      codeFlow.save(configName);
    }
  }
  
  public static String getFormatCurrent(String configName, String codeDateSep, BillCodeConfig config)
    throws ParseException
  {
    String format1 = config.getStr("format1");
    String format2 = config.getStr("format2");
    String format3 = config.getStr("format3");
    String format4 = config.getStr("format4");
    String format5 = config.getStr("format5");
    String format6 = config.getStr("format6");
    String format7 = config.getStr("format7");
    String format8 = config.getStr("format8");
    String format9 = config.getStr("format9");
    String format10 = config.getStr("format10");
    
    String formatStr = "";
    if (StringUtils.isNotBlank(format1)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format1);
    }
    if (StringUtils.isNotBlank(format2)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format2);
    }
    if (StringUtils.isNotBlank(format3)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format3);
    }
    if (StringUtils.isNotBlank(format4)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format4);
    }
    if (StringUtils.isNotBlank(format5)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format5);
    }
    if (StringUtils.isNotBlank(format6)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format6);
    }
    if (StringUtils.isNotBlank(format7)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format7);
    }
    if (StringUtils.isNotBlank(format8)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format8);
    }
    if (StringUtils.isNotBlank(format9)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format9);
    }
    if (StringUtils.isNotBlank(format10)) {
      formatStr = formatStr + BillCodeConfigFormat.dao.getFormatStr(configName, codeDateSep, format10);
    }
    return formatStr;
  }
  
  public static String getFormatDefault(String codeDateSep)
    throws ParseException
  {
    String format = "yyyy-MM-dd";
    if ("1".equals(codeDateSep)) {
      format = "yyyy.MM.dd";
    } else if ("2".equals(codeDateSep)) {
      format = "yyyy年MM月dd日";
    } else if ("3".equals(codeDateSep)) {
      format = "yyyyMMdd";
    } else if ("3".equals(codeDateSep)) {
      format = "yyyy MM dd";
    }
    return DateUtils.format(new Date(), format);
  }
  
  public void reloadAutoCode()
  {
    int billTypeId = getParaToInt("billTypeId").intValue();
    setAttr("codeAllowEdit", "1");
    setAttr("codeIncrease", Integer.valueOf(-1));
    setAttr("code", "");
    billCodeAuto(billTypeId);
    renderJson();
  }
}
