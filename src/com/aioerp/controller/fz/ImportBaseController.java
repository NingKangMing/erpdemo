package com.aioerp.controller.fz;

import com.aioerp.common.AioConstants;
import com.aioerp.controller.BaseController;
import com.aioerp.model.base.Product;
import com.aioerp.model.base.ProductUnit;
import com.aioerp.model.base.Storage;
import com.aioerp.model.base.Unit;
import com.aioerp.model.stock.StockInit;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.SysConfig;
import com.aioerp.util.BigDecimalUtils;
import com.aioerp.util.ExcelExportUtil;
import com.aioerp.util.ExcelExportUtil.TmpPair;
import com.aioerp.util.IO;
import com.aioerp.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;

public class ImportBaseController
  extends BaseController
{
  public void index()
  {
    render("page.html");
  }
  
  public <M extends Model<M>> void exportTmpExcel()
  {
    expExcel("简易基本信息模板", Boolean.valueOf(false), 1000, null, null, null, null);
    renderNull();
  }
  
  String[] proColums = { "父类编号", "商品编号", "商品全名", "简名", "型号", "规格", "产地", "成本算法", "有效期(天)", "备注", "基本单位", "条码", "零售价", "预设售价1", "预设售价2", "预设售价3" };
  String[] proCodes = { "supCode", "code", "fullName", "smallName", "model", "standard", "field", "costArith", "validity", "memo", "calculateUnit1", "barCode1", "retailPrice1", "defaultPrice11", "defaultPrice12", "defaultPrice13" };
  String[] sgeColums = { "父类编号", "仓库编号", "仓库全名", "仓库简称", "备注", "仓库地址", "仓库负责人", "负责人电话" };
  String[] sgeCodes = { "supCode", "code", "fullName", "smallName", "memo", "stoAddress", "functionary", "phone" };
  String[] unitColums = { "父类编号", "单位编号", "单位全名", "单位简名", "单位地址", "单位电话", "手机一", "手机二", "传真", "邮编", "联系人一", "联系人二", "开户银行", "银行账号", "税号", "期初应收款", "期初应付款", "应收款上限", "应付款上限", "结算期限(天)", "备注", "换货期限(天)", "换货比例(%)", "电子邮件" };
  String[] unitCodes = { "supCode", "code", "fullName", "smallName", "address", "phone", "mobile1", "mobile2", "fax", "zipCode", "contact1", "contact2", "bank", "bankAccount", "tariff", "beginGetMoney", "beginPayMoney", "getMoneyLimit", "payMoneyLimit", "settlePeriod", "memo", "replacePrdPeriod", "replacePrdPercentage", "email" };
  String[] sckColums = { "商品编号", "商品全名", "仓库编号", "仓库全名", "数量", "金额", "批号", "生产日期" };
  String[] sckCodes = { "proCode", "proName", "sgeCode", "sgeName", "amount", "money", "batch", "produceDate" };
  
  @Before({Tx.class})
  public void importExcel()
    throws SQLException
  {
    int allowRow = 1000;
    
    String configName = loginConfigName();
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    
    UploadFile excel = getFile("impExcel", "upLoadFile/impExcel", Integer.valueOf(1048576), "utf-8");
    if (excel != null)
    {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      String fileName = sdf.format(new Date());
      String filePatch = IO.getWebrootPath() + "/upload/upLoadFile/impExcel/" + fileName + ".xls";
      File file = new File(filePatch);
      excel.getFile().renameTo(file);
      
      File file2 = new File(filePatch);
      
      List<Product> proList = new ArrayList();
      List<Product> eProList = new ArrayList();
      
      List<Storage> sgeList = new ArrayList();
      List<Storage> eSgeList = new ArrayList();
      
      List<Unit> unitList = new ArrayList();
      List<Unit> eUnitList = new ArrayList();
      
      List<StockInit> sckList = new ArrayList();
      List<StockInit> eSckList = new ArrayList();
      try
      {
        InputStream is = new FileInputStream(filePatch);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);
        for (int i = 0; i < hssfWorkbook.getNumberOfSheets(); i++)
        {
          HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(i);
          String sheetName = hssfSheet.getSheetName();
          if (sheetName.equals("商品"))
          {
            int minCell = 16;
            int cellCount = minCell;
            
            int rowCount = hssfSheet.getPhysicalNumberOfRows();
            if (rowCount > allowRow + 1)
            {
              file2.delete();
              


              renderText("[" + AioConstants.HTTP_RETURN300 + "]商品数据量超过定值" + allowRow + ",文件导入失败，请重试！");
              return;
            }
            for (int r = 0; r <= rowCount; r++)
            {
              HSSFRow row = hssfSheet.getRow(r);
              if ((r != 0) && 
                (!ExcelExportUtil.isBlankRow(hssfSheet, row)))
              {
                Product pro = new Product();
                Boolean isValid = Boolean.valueOf(true);
                for (int c = 0; c < cellCount; c++)
                {
                  HSSFCell cell = row.getCell(c);
                  String title = hssfSheet.getRow(0).getCell(c).getStringCellValue();
                  Object val = ExcelExportUtil.getCellVal(cell);
                  if (!isValid.booleanValue()) {
                    break;
                  }
                  for (int p = 0; p < this.proColums.length; p++) {
                    if (title.equals(this.proColums[p]))
                    {
                      if (this.proColums[p].equals("父类编号"))
                      {
                        pro.put(this.proCodes[p], val == null ? null : val.toString());
                        break;
                      }
                      if (this.proColums[p].equals("商品编号"))
                      {
                        if ((val == null) || (StringUtil.isNull(val.toString())))
                        {
                          isValid = Boolean.valueOf(false);
                          break;
                        }
                        pro.set(this.proCodes[p], val == null ? null : val.toString());
                        break;
                      }
                      if (this.proColums[p].equals("成本算法"))
                      {
                        if (val != null)
                        {
                          if (val.equals("手工指定"))
                          {
                            pro.set(this.proCodes[p], "4");
                            break;
                          }
                          pro.set(this.proCodes[p], "1");
                          
                          break;
                        }
                        pro.set(this.proCodes[p], Integer.valueOf(1));
                        
                        break;
                      }
                      if (this.proColums[p].equals("有效期(天)"))
                      {
                        if (val != null)
                        {
                          pro.set(this.proCodes[p], val.toString());
                          break;
                        }
                        pro.set(this.proCodes[p], "0");
                        
                        break;
                      }
                      pro.set(this.proCodes[p], val == null ? null : val.toString());
                      
                      break;
                    }
                  }
                }
                if (isValid.booleanValue()) {
                  proList.add(pro);
                }
              }
            }
          }
          else if (sheetName.equals("仓库"))
          {
            int minCell = 8;
            int cellCount = minCell;
            int rowCount = hssfSheet.getPhysicalNumberOfRows();
            if (rowCount > allowRow + 1)
            {
              file2.delete();
              


              renderText("[" + AioConstants.HTTP_RETURN300 + "]仓库数据量超过定值" + allowRow + ",文件导入失败，请重试！");
              return;
            }
            for (int r = 0; r <= rowCount; r++)
            {
              HSSFRow row = hssfSheet.getRow(r);
              if ((r != 0) && 
                (!ExcelExportUtil.isBlankRow(hssfSheet, row)))
              {
                Storage sge = new Storage();
                Boolean isValid = Boolean.valueOf(true);
                for (int c = 0; c < cellCount; c++)
                {
                  HSSFCell cell = row.getCell(c);
                  String title = hssfSheet.getRow(0).getCell(c).getStringCellValue();
                  Object val = ExcelExportUtil.getCellVal(cell);
                  if (!isValid.booleanValue()) {
                    break;
                  }
                  for (int s = 0; s < this.sgeColums.length; s++) {
                    if (title.equals(this.sgeColums[s]))
                    {
                      if (this.sgeColums[s].equals("父类编号"))
                      {
                        sge.put(this.sgeCodes[s], val == null ? null : val.toString());
                        break;
                      }
                      if (this.sgeColums[s].equals("仓库编号"))
                      {
                        if ((val == null) || (StringUtil.isNull(val.toString())))
                        {
                          isValid = Boolean.valueOf(false);
                          break;
                        }
                        sge.set(this.sgeCodes[s], val == null ? null : val.toString());
                        break;
                      }
                      sge.set(this.sgeCodes[s], val == null ? null : val.toString());
                      
                      break;
                    }
                  }
                }
                if (isValid.booleanValue()) {
                  sgeList.add(sge);
                }
              }
            }
          }
          else if (sheetName.equals("单位"))
          {
            int minCell = 24;
            if ("1".equals(hasOpenAccount)) {
              minCell = 22;
            }
            int cellCount = minCell;
            
            int rowCount = hssfSheet.getPhysicalNumberOfRows();
            if (rowCount > allowRow + 1)
            {
              file2.delete();
              


              renderText("[" + AioConstants.HTTP_RETURN300 + "]单位数据量超过定值" + allowRow + ",文件导入失败，请重试！");
              return;
            }
            for (int r = 0; r <= rowCount; r++)
            {
              HSSFRow row = hssfSheet.getRow(r);
              if ((r != 0) && 
                (!ExcelExportUtil.isBlankRow(hssfSheet, row)))
              {
                Unit unit = new Unit();
                Boolean isValid = Boolean.valueOf(true);
                for (int c = 0; c < cellCount; c++)
                {
                  HSSFCell cell = row.getCell(c);
                  String title = hssfSheet.getRow(0).getCell(c).getStringCellValue();
                  Object val = ExcelExportUtil.getCellVal(cell);
                  if (!isValid.booleanValue()) {
                    break;
                  }
                  for (int u = 0; u < this.unitColums.length; u++) {
                    if (title.equals(this.unitColums[u]))
                    {
                      if (this.unitColums[u].equals("父类编号"))
                      {
                        unit.put(this.unitCodes[u], val == null ? null : val.toString());
                        break;
                      }
                      if (this.unitColums[u].equals("单位编号"))
                      {
                        if ((val == null) || (StringUtil.isNull(val.toString())))
                        {
                          isValid = Boolean.valueOf(false);
                          break;
                        }
                        unit.set(this.unitCodes[u], val == null ? null : val.toString());
                        break;
                      }
                      if ((this.unitColums[u].equals("期初应收款")) || (this.unitColums[u].equals("期初应付款")) || 
                        (this.unitColums[u].equals("应收款上限")) || (this.unitColums[u].equals("应付款上限")) || 
                        (this.unitColums[u].equals("结算期限(天)")) || (this.unitColums[u].equals("换货期限(天)")))
                      {
                        if (val != null)
                        {
                          unit.set(this.unitCodes[u], val.toString());
                          break;
                        }
                        unit.set(this.unitCodes[u], "0");
                        
                        break;
                      }
                      if (this.unitColums[u].equals("换货比例(%)"))
                      {
                        if (val != null)
                        {
                          unit.set(this.unitCodes[u], val.toString());
                          break;
                        }
                        unit.set(this.unitCodes[u], "100");
                        
                        break;
                      }
                      unit.set(this.unitCodes[u], val == null ? null : val.toString());
                      
                      break;
                    }
                  }
                }
                if (isValid.booleanValue()) {
                  unitList.add(unit);
                }
              }
            }
          }
          else if ((hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT0)) && (sheetName.equals("库存信息")))
          {
            int minCell = 8;
            int cellCount = minCell;
            
            int rowCount = hssfSheet.getPhysicalNumberOfRows();
            if (rowCount > allowRow + 1)
            {
              file2.delete();
              


              renderText("[" + AioConstants.HTTP_RETURN300 + "]期初库存数据量超过定值" + allowRow + ",文件导入失败，请重试！");
              return;
            }
            for (int r = 0; r <= rowCount; r++)
            {
              HSSFRow row = hssfSheet.getRow(r);
              if ((r != 0) && 
                (!ExcelExportUtil.isBlankRow(hssfSheet, row)))
              {
                StockInit sck = new StockInit();
                Boolean isValid = Boolean.valueOf(true);
                for (int c = 0; c < cellCount; c++)
                {
                  HSSFCell cell = row.getCell(c);
                  String title = hssfSheet.getRow(0).getCell(c).getStringCellValue();
                  Object val = ExcelExportUtil.getCellVal(cell);
                  if (!isValid.booleanValue()) {
                    break;
                  }
                  for (int k = 0; k < this.sckColums.length; k++) {
                    if (title.equals(this.sckColums[k]))
                    {
                      if ((this.sckColums[k].equals("商品编号")) || (this.sckColums[k].equals("仓库编号")))
                      {
                        if ((val == null) || (StringUtil.isNull(val.toString())))
                        {
                          isValid = Boolean.valueOf(false);
                          break;
                        }
                        sck.put(this.sckCodes[k], val == null ? null : val.toString());
                        break;
                      }
                      if ((this.sckColums[k].equals("商品全名")) || (this.sckColums[k].equals("仓库全名")))
                      {
                        sck.put(this.sckCodes[k], val == null ? null : val.toString());
                        break;
                      }
                      sck.set(this.sckCodes[k], val == null ? null : val.toString());
                      
                      break;
                    }
                  }
                }
                if (isValid.booleanValue()) {
                  sckList.add(sck);
                }
              }
            }
          }
        }
        for (int j = 0; j < proList.size(); j++)
        {
          Product pro = (Product)proList.get(j);
          String supCode = pro.getStr("supCode");
          String code1 = pro.getStr("code");
          String name = pro.getStr("fullName");
          try
          {
            pro.set("validity", Integer.valueOf(Integer.parseInt(pro.getStr("validity"))));
          }
          catch (Exception e)
          {
            pro.put("error", "整数格式错误：有效期");
            eProList.add(pro);
            continue;
          }
          try
          {
            if (!StringUtil.isNull(pro.getStr("retailPrice1"))) {
              pro.set("retailPrice1", new BigDecimal(pro.getStr("retailPrice1")));
            }
            if (!StringUtil.isNull(pro.getStr("defaultPrice11"))) {
              pro.set("defaultPrice11", new BigDecimal(pro.getStr("defaultPrice11")));
            }
            if (!StringUtil.isNull(pro.getStr("defaultPrice12"))) {
              pro.set("defaultPrice12", new BigDecimal(pro.getStr("defaultPrice12")));
            }
            if (!StringUtil.isNull(pro.getStr("defaultPrice13"))) {
              pro.set("defaultPrice13", new BigDecimal(pro.getStr("defaultPrice13")));
            }
          }
          catch (Exception e)
          {
            pro.put("error", "数字格式错误：价格");
            eProList.add(pro);
            continue;
          }
          if (Product.dao.existObjectByNum(configName, 0, code1))
          {
            pro.put("error", "商品编号已存在");
            eProList.add(pro);
          }
          else if (StringUtil.isNull(name))
          {
            pro.put("error", "商品全名不能为空");
            eProList.add(pro);
          }
          else
          {
            String supPids;
         
            if (StringUtil.isNull(supCode))
            {
              pro.set("supId", Integer.valueOf(0));
              supPids = "{0}";
            }
            else
            {
              Model supModel = Product.dao.getRecordByCode(configName, supCode);
              if (supModel == null)
              {
                pro.put("error", "父类编号不存在");
                eProList.add(pro);
                continue;
              }
              pro.set("supId", supModel.getInt("id"));
              supPids = supModel.getStr("pids");
              supModel.set("node", Integer.valueOf(2));
              supModel.update(configName);
            }
            String barCode1 = pro.getStr("barCode1");
            if ((!SysConfig.getConfig(configName, 8).booleanValue()) && 
              (Product.dao.existBarCode(configName, barCode1, Integer.valueOf(0))))
            {
              pro.put("error", "商品条码不能重复");
              eProList.add(pro);
            }
            else
            {
              pro.set("unitRelation1", Integer.valueOf(1));
              pro.set("node", Integer.valueOf(1));
              pro.set("rank", Integer.valueOf(Product.dao.getMaxRank(configName)));
              pro.set("createTime", new Date());
              pro.set("status", Integer.valueOf(2));
              pro.save(configName);
              int id = pro.getInt("id").intValue();
              pro.set("pids", supPids + "{" + id + "}");
              pro.update(configName);
              
              ProductUnit.dao.addProductUnit(configName, pro, 1, 1, 1);
            }
          }
        }
        for (int j = 0; j < sgeList.size(); j++)
        {
          Storage sge = (Storage)sgeList.get(j);
          String supCode = sge.getStr("supCode");
          String code1 = sge.getStr("code");
          String name = sge.getStr("fullName");
          if (Storage.dao.codeIsExist(configName, code1, 0).booleanValue())
          {
            sge.put("error", "仓库编号已存在");
            eSgeList.add(sge);
          }
          else if (StringUtil.isNull(name))
          {
            sge.put("error", "仓库全名不能为空");
            eSgeList.add(sge);
          }
          else
          {
            String supPids;
          
            if (StringUtil.isNull(supCode))
            {
              sge.set("supId", Integer.valueOf(0));
              supPids = "{0}";
            }
            else
            {
              Model supModel = Storage.dao.getRecordByCode(configName, supCode);
              if (supModel == null)
              {
                sge.put("error", "父类编号不存在");
                eSgeList.add(sge);
                continue;
              }
              sge.set("supId", supModel.getInt("id"));
              supPids = supModel.getStr("pids");
              supModel.set("node", Integer.valueOf(2));
              supModel.update(configName);
            }
            sge.set("node", Integer.valueOf(1));
            sge.set("rank", Integer.valueOf(Storage.dao.getMaxRank(configName)));
            sge.set("createTime", new Date());
            sge.set("status", Integer.valueOf(2));
            sge.save(configName);
            int id = sge.getInt("id").intValue();
            sge.set("pids", supPids + "{" + id + "}");
            sge.update(configName);
          }
        }
        for (int j = 0; j < unitList.size(); j++)
        {
          Unit unit = (Unit)unitList.get(j);
          String supCode = unit.getStr("supCode");
          String code1 = unit.getStr("code");
          String name = unit.getStr("fullName");
          try
          {
            if (!StringUtil.isNull(unit.getStr("beginGetMoney"))) {
              unit.set("beginGetMoney", new BigDecimal(unit.getStr("beginGetMoney")));
            }
            if (!StringUtil.isNull(unit.getStr("beginPayMoney"))) {
              unit.set("beginPayMoney", new BigDecimal(unit.getStr("beginPayMoney")));
            }
            if (!StringUtil.isNull(unit.getStr("getMoneyLimit"))) {
              unit.set("getMoneyLimit", new BigDecimal(unit.getStr("getMoneyLimit")));
            }
            if (!StringUtil.isNull(unit.getStr("payMoneyLimit"))) {
              unit.set("payMoneyLimit", new BigDecimal(unit.getStr("payMoneyLimit")));
            }
          }
          catch (Exception e)
          {
            unit.put("error", "数字格式错误：应收应付款");
            eUnitList.add(unit);
            continue;
          }
          try
          {
            unit.set("settlePeriod", Integer.valueOf(Integer.parseInt(unit.getStr("settlePeriod"))));
            unit.set("replacePrdPeriod", Integer.valueOf(Integer.parseInt(unit.getStr("replacePrdPeriod"))));
            int replacePrdPercentage = Integer.parseInt(unit.getStr("replacePrdPercentage"));
            if (replacePrdPercentage > 100)
            {
              unit.put("error", "换货比例(%)必须小于100");
              eUnitList.add(unit);
              continue;
            }
            unit.set("replacePrdPercentage", Integer.valueOf(replacePrdPercentage));
          }
          catch (Exception e)
          {
            unit.put("error", "整数格式错误：期限");
            eUnitList.add(unit);
            continue;
          }
          BigDecimal beginGetMoney = unit.getBigDecimal("beginGetMoney");
          BigDecimal beginPayMoney = unit.getBigDecimal("beginPayMoney");
          if (hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT1))
          {
            unit.remove("beginGetMoney");
            unit.remove("beginPayMoney");
          }
          if ((beginGetMoney != null) && (BigDecimalUtils.compare(beginGetMoney, BigDecimal.ZERO) != 0) && 
            (beginPayMoney != null) && (BigDecimalUtils.compare(beginPayMoney, BigDecimal.ZERO) != 0))
          {
            unit.put("error", "应收应付不能同时有值");
            eUnitList.add(unit);
          }
          else if (Unit.dao.existObjectByNum(configName, 0, code1))
          {
            unit.put("error", "单位编号已存在");
            eUnitList.add(unit);
          }
          else if (StringUtil.isNull(name))
          {
            unit.put("error", "单位全名不能为空");
            eUnitList.add(unit);
          }
          else
          {
            String supPids;
            
            if (StringUtil.isNull(supCode))
            {
              unit.set("supId", Integer.valueOf(0));
              supPids = "{0}";
            }
            else
            {
              Model supModel = Unit.dao.getRecordByCode(configName, supCode);
              if (supModel == null)
              {
                unit.put("error", "父类编号不存在");
                eUnitList.add(unit);
                continue;
              }
              unit.set("supId", supModel.getInt("id"));
              supPids = supModel.getStr("pids");
              supModel.set("node", Integer.valueOf(2));
              supModel.update(configName);
            }
            unit.set("node", Integer.valueOf(1));
            unit.set("rank", Integer.valueOf(Unit.dao.getMaxRank(configName)));
            unit.set("createTime", new Date());
            unit.set("status", Integer.valueOf(2));
            unit.save(configName);
            int id = unit.getInt("id").intValue();
            unit.set("pids", supPids + "{" + id + "}");
            unit.update(configName);
          }
        }
        if (hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT0)) {
          for (int j = 0; j < sckList.size(); j++)
          {
            StockInit sck = (StockInit)sckList.get(j);
            String proCode = sck.getStr("proCode");
            String proName = sck.getStr("proName");
            String sgeCode = sck.getStr("sgeCode");
            String sgeName = sck.getStr("sgeName");
            if (StringUtil.isNull(sck.getStr("amount")))
            {
              sck.put("error", "库存数量不能为空");
              eSckList.add(sck);
            }
            else
            {
              if (StringUtil.isNull(sck.getStr("money"))) {
                sck.set("money", "0");
              }
              try
              {
                if (!StringUtil.isNull(sck.getStr("amount"))) {
                  sck.set("amount", new BigDecimal(sck.getStr("amount")));
                }
                if (!StringUtil.isNull(sck.getStr("money"))) {
                  sck.set("money", new BigDecimal(sck.getStr("money")));
                }
              }
              catch (Exception e)
              {
                sck.put("error", "数字格式错误：数量或金额");
                eSckList.add(sck);
                continue;
              }
              Date produceDate = null;
              try
              {
                String strd = sck.getStr("produceDate");
                if (!StringUtil.isNull(strd))
                {
                  SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                  produceDate = sdf2.parse(strd);
                }
              }
              catch (Exception e)
              {
                sck.put("error", "日期格式错误");
                eSckList.add(sck);
                continue;
              }
              Model proModel = Product.dao.getRecordByCode(configName, proCode);
              if (proModel == null)
              {
                sck.put("error", "商品编号不存在");
                eSckList.add(sck);
              }
              else
              {
                Integer node = proModel.getInt("node");
                if (AioConstants.NODE_2 == node.intValue())
                {
                  sck.put("error", "商品不能为一类");
                  eSckList.add(sck);
                }
                else
                {
                  String name = proModel.getStr("fullName");
                  if (!name.equals(proName))
                  {
                    sck.put("error", "商品全名不匹配");
                    eSckList.add(sck);
                  }
                  else
                  {
                    Model sgeModel = Storage.dao.getRecordByCode(configName, sgeCode);
                    if (sgeModel == null)
                    {
                      sck.put("error", "仓库编号不存在");
                      eSckList.add(sck);
                    }
                    else
                    {
                       node = sgeModel.getInt("node");
                      if (AioConstants.NODE_2 == node.intValue())
                      {
                        sck.put("error", "仓库不能为一类");
                        eSckList.add(sck);
                      }
                      else
                      {
                         name = sgeModel.getStr("fullName");
                        if (!name.equals(sgeName))
                        {
                          sck.put("error", "仓库全名不匹配");
                          eSckList.add(sck);
                        }
                        else
                        {
                          int proId = proModel.getInt("id").intValue();
                          int sgeId = sgeModel.getInt("id").intValue();
                          
                          BigDecimal amount = sck.getBigDecimal("amount");
                          BigDecimal money = sck.getBigDecimal("money");
                          BigDecimal price = BigDecimalUtils.div(money, amount);
                          price = price.setScale(4, 4);
                          
                          StockInit s = StockInit.dao.getStockInit(configName, Integer.valueOf(sgeId), Integer.valueOf(proId), price, sck.getStr("batch"), produceDate);
                          if (s == null)
                          {
                            sck.set("productId", Integer.valueOf(proId));
                            sck.set("price", price);
                            sck.set("storageId", Integer.valueOf(sgeId));
                            sck.set("updateTime", new Date());
                            sck.set("produceDate", produceDate);
                            sck.save(configName);
                          }
                          else
                          {
                            sck.put("error", "该仓库已存在商品库存");
                            eSckList.add(sck);
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        file2.delete();
        if ((eProList.size() > 0) || (eSgeList.size() > 0) || (eUnitList.size() > 0) || (eSckList.size() > 0))
        {
          expExcel("错误数据改正重新导入", Boolean.valueOf(true), allowRow, eProList, eSgeList, eUnitList, eSckList);
          renderNull();
          return;
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        commonRollBack();
        


        file2.delete();
        


        renderText("[" + AioConstants.HTTP_RETURN300 + "]请勿改变模板格式,文件导入失败，请重试！"); return;
      }
      InputStream is;
      renderText("[" + AioConstants.HTTP_RETURN200 + "]导入数据成功！");
    }
    else
    {
      renderText("[" + AioConstants.HTTP_RETURN300 + "]请选择Excel文件！");
    }
  }
  
  public void expExcel(String excName, Boolean hasData, int dataCount, List<Product> eProList, List<Storage> eSgeList, List<Unit> eUnitList, List<StockInit> eSckList)
  {
    String configName = loginConfigName();
    String hasOpenAccount = AioerpSys.dao.getValue1(configName, "hasOpenAccount");
    
    HSSFWorkbook wb = new HSSFWorkbook();
    
    CellStyle css = wb.createCellStyle();
    DataFormat format = wb.createDataFormat();
    css.setDataFormat(format.getFormat("@"));
    

    CellStyle reqStyle = wb.createCellStyle();
    reqStyle.setDataFormat(format.getFormat("@"));
    reqStyle.setAlignment((short)2);
    reqStyle.setWrapText(true);
    Font font2 = wb.createFont();
    font2.setBoldweight((short)700);
    font2.setFontName("微软雅黑");
    font2.setColor((short)10);
    reqStyle.setFont(font2);
    reqStyle.setLocked(true);
    

    CellStyle titleCellStyle = wb.createCellStyle();
    titleCellStyle.setDataFormat(format.getFormat("@"));
    titleCellStyle.setAlignment((short)2);
    titleCellStyle.setWrapText(true);
    Font font = wb.createFont();
    font.setBoldweight((short)700);
    font.setFontName("微软雅黑");
    titleCellStyle.setFont(font);
    titleCellStyle.setLocked(false);
    


    List<ExcelExportUtil.TmpPair> proPair = new ArrayList();
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[0], this.proColums[0], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[1], this.proColums[1], reqStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[2], this.proColums[2], reqStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[3], this.proColums[3], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[4], this.proColums[4], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[5], this.proColums[5], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[6], this.proColums[6], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[7], this.proColums[7], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.proCodes[8], this.proColums[8], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[9], this.proColums[9], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[10], this.proColums[10], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.proCodes[11], this.proColums[11], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.proCodes[12], this.proColums[12], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.proCodes[13], this.proColums[13], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.proCodes[14], this.proColums[14], titleCellStyle));
    proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.proCodes[15], this.proColums[15], titleCellStyle));
    if (hasData.booleanValue()) {
      proPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), "error", "错误信息", titleCellStyle));
    }
    List<ExcelExportUtil.TmpPair> sgePair = new ArrayList();
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[0], this.sgeColums[0], titleCellStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[1], this.sgeColums[1], reqStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[2], this.sgeColums[2], reqStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[3], this.sgeColums[3], titleCellStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[4], this.sgeColums[4], titleCellStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[5], this.sgeColums[5], titleCellStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[6], this.sgeColums[6], titleCellStyle));
    sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sgeCodes[7], this.sgeColums[7], titleCellStyle));
    if (hasData.booleanValue()) {
      sgePair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), "error", "错误信息", titleCellStyle));
    }
    List<ExcelExportUtil.TmpPair> unitPair = new ArrayList();
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[0], this.unitColums[0], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[1], this.unitColums[1], reqStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[2], this.unitColums[2], reqStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[3], this.unitColums[3], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[4], this.unitColums[4], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[5], this.unitColums[5], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[6], this.unitColums[6], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[7], this.unitColums[7], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[8], this.unitColums[8], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[9], this.unitColums[9], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[10], this.unitColums[10], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[11], this.unitColums[11], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[12], this.unitColums[12], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[13], this.unitColums[13], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[14], this.unitColums[14], titleCellStyle));
    if (hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT0))
    {
      unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[15], this.unitColums[15], titleCellStyle));
      unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[16], this.unitColums[16], titleCellStyle));
    }
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[17], this.unitColums[17], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[18], this.unitColums[18], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[19], this.unitColums[19], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[20], this.unitColums[20], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[21], this.unitColums[21], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.unitCodes[22], this.unitColums[22], titleCellStyle));
    unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.unitCodes[23], this.unitColums[23], titleCellStyle));
    if (hasData.booleanValue()) {
      unitPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), "error", "错误信息", titleCellStyle));
    }
    List<ExcelExportUtil.TmpPair> sckPair = new ArrayList();
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sckCodes[0], this.sckColums[0], reqStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sckCodes[1], this.sckColums[1], reqStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sckCodes[2], this.sckColums[2], reqStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sckCodes[3], this.sckColums[3], reqStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.sckCodes[4], this.sckColums[4], titleCellStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.sckCodes[5], this.sckColums[5], titleCellStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), this.sckCodes[6], this.sckColums[6], titleCellStyle));
    sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(false), this.sckCodes[7], this.sckColums[7], titleCellStyle));
    if (hasData.booleanValue()) {
      sckPair.add(new ExcelExportUtil.TmpPair(Boolean.valueOf(true), "error", "错误信息", titleCellStyle));
    }
    List<Map<String, Object>> sheetDatas = new ArrayList();
    
    Map<String, Object> map = new HashMap();
    map.put("sheetCode", "pro");
    map.put("sheetName", "商品");
    map.put("titles", proPair);
    map.put("records", eProList);
    sheetDatas.add(map);
    
    map = new HashMap();
    map.put("sheetCode", "sge");
    map.put("sheetName", "仓库");
    map.put("titles", sgePair);
    map.put("records", eSgeList);
    sheetDatas.add(map);
    
    map = new HashMap();
    map.put("sheetCode", "unit");
    map.put("sheetName", "单位");
    map.put("titles", unitPair);
    map.put("records", eUnitList);
    sheetDatas.add(map);
    
    map = new HashMap();
    map.put("sheetCode", "sck");
    map.put("sheetName", "库存信息");
    map.put("titles", sckPair);
    map.put("records", eSckList);
    if (hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT0)) {
      sheetDatas.add(map);
    }
    String[] textlist = { "移动加权平均", "手工指定" };
    String promptTitle = "提示：";
    String promptContent = "请在下拉框中选择成本算法，不填默认为移动加权平均！";
    for (int i = 0; i < sheetDatas.size(); i++)
    {
      Map<String, Object> data = (Map)sheetDatas.get(i);
      String sheetCode = data.get("sheetCode").toString();
      String sheetName = data.get("sheetName").toString();
      HSSFSheet sheet = wb.createSheet(sheetName);
      


      int rowCount = dataCount + 2;
      if (sheetCode.equals("pro"))
      {
        HSSFDataValidation validate = ExcelExportUtil.notNullValidate(1, rowCount, 1, 2, "该列为必填项");
        sheet.addValidationData(validate);
        sheet = ExcelExportUtil.setHSSFValidation(sheet, promptTitle, promptContent, textlist, 1, rowCount, 7, 7);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-10000的整数", 1, rowCount, 8, 8);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.lenValidate(1, rowCount, 11, 11, "30", "一个条码最长为30个字符");
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-999999999的数字", 1, rowCount, 12, 15);
        sheet.addValidationData(validate);
      }
      else if (sheetCode.equals("sge"))
      {
        HSSFDataValidation validate = ExcelExportUtil.notNullValidate(1, rowCount, 1, 2, "该列为必填项");
        sheet.addValidationData(validate);
      }
      else if (sheetCode.equals("unit"))
      {
        HSSFDataValidation validate = ExcelExportUtil.notNullValidate(1, rowCount, 1, 2, "该列为必填项");
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-999999999的数字", 1, rowCount, 15, 18);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-9999的整数", 1, rowCount, 19, 19);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-9999的整数", 1, rowCount, 21, 21);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-100的整数", 1, rowCount, 22, 22);
        sheet.addValidationData(validate);
      }
      else if (sheetCode.equals("sck"))
      {
        HSSFDataValidation validate = ExcelExportUtil.notNullValidate(1, rowCount, 0, 3, "该列为必填项");
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-99999的数字", 1, rowCount, 4, 4);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入0-9999999999999的数字", 1, rowCount, 5, 5);
        sheet.addValidationData(validate);
        validate = ExcelExportUtil.setHSSFPrompt(promptTitle, "请输入大于1980-1-1小于2050-1-1的日期", 1, rowCount, 7, 7);
        sheet.addValidationData(validate);
      }
      int rowIndex = 0;int cellIndex = 0;
      HSSFRow row = null;
      HSSFCell cell = null;
      

      row = sheet.createRow(rowIndex);
      row.setHeight((short)450);
      rowIndex++;
      
      List<ExcelExportUtil.TmpPair> pairs = (List)data.get("titles");
      List<Model> records = (List)data.get("records");
      for (ExcelExportUtil.TmpPair pair : pairs)
      {
        sheet.setColumnWidth(cellIndex, 4000);
        cell = row.createCell(cellIndex);
        cell.setCellStyle(pair.style);
        cell.setCellValue(pair.title);
        
        sheet.setDefaultColumnStyle(cellIndex, css);
        
        cellIndex++;
      }
/*      if (records != null)
      {
        Iterator localIterator2;
        for (localIterator2<Model> = records.iterator(); localIterator2.hasNext())
        {
          Model record = (Model)localIterator2.next();
          row = sheet.createRow(rowIndex);
          
          rowIndex++;
          cellIndex = 0;
          
          localIterator2 = pairs.iterator(); continue;ExcelExportUtil.TmpPair pair = (ExcelExportUtil.TmpPair)localIterator2.next();
          cell = row.createCell(cellIndex);
          
          cellIndex++;
          
          String colum = pair.column;
          Object value = record.get(colum);
          if (colum.equals("costArith")) {
            if (Integer.parseInt(value.toString()) == 1) {
              value = "移动加权平均";
            } else {
              value = "手工指定";
            }
          }
          if (value != null) {
            cell.setCellValue(value.toString());
          }
        }
      }*/
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    String filename = excName + sdf.format(new Date());
    ExcelExportUtil.writeStream(filename, wb, getResponse(), getRequest());
  }
  
  public Boolean verifyCodeAqlike(List<Model> list, File file)
  {
    Boolean flag = Boolean.valueOf(false);
    int row1 = 0;
    int row2 = 0;
    Model temp = null;
    for (int i = 0; i < list.size(); i++)
    {
      Model m1 = (Model)list.get(i);
      if ((temp != null) && (m1.getStr("code").equals(temp.getStr("code"))))
      {
        row1 = i + 2;
        break;
      }
      for (int j = 0; j < list.size(); j++)
      {
        Model m2 = (Model)list.get(j);
        if ((i != j) && (m1.getStr("code").equals(m2.getStr("code"))))
        {
          flag = Boolean.valueOf(true);
          row2 = j + 2;
          temp = m2;
          break;
        }
      }
    }
    if (flag.booleanValue())
    {
      file.delete();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("msg", "第" + row1 + "行和" + row2 + "行的编号一致，文件导入失败，请改正重试！");
      renderJson();
      return Boolean.valueOf(false);
    }
    return Boolean.valueOf(true);
  }
  
  public Boolean verifyCode(List<Model> list, File file)
  {
    Boolean flag = Boolean.valueOf(false);
    int row = 0;
    for (int i = 0; i < list.size(); i++)
    {
      Model m = (Model)list.get(i);
      String supCode = m.getStr("supCode");
      String code = m.getStr("code");
      String name = m.getStr("fullName");
      if ((StringUtil.isNull(code)) || (StringUtil.isNull(name)) || (supCode.equals(code)))
      {
        flag = Boolean.valueOf(true);
        row = i + 2;
        break;
      }
    }
    if (flag.booleanValue())
    {
      file.delete();
      setAttr("statusCode", AioConstants.HTTP_RETURN300);
      setAttr("msg", "第" + row + "行父类编号和编号一致或必填项为空，文件导入失败，请改正重试！");
      renderJson();
      return Boolean.valueOf(false);
    }
    return Boolean.valueOf(true);
  }
  
  public List<Model> orderListByCode(List<Model> list)
  {
    for (int i = 0; i < list.size(); i++)
    {
      Model m1 = (Model)list.get(i);
      for (int j = 0; j < list.size(); j++)
      {
        Model m2 = (Model)list.get(j);
        if (m1.getStr("supCode").equals(m2.getStr("code")))
        {
          list.set(i, m2);
          list.set(j, m1);
          i = 0;
        }
      }
    }
    return list;
  }
}
