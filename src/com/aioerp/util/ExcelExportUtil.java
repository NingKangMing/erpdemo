package com.aioerp.util;

import com.jfinal.plugin.activerecord.Model;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddressList;

@SuppressWarnings("unchecked")
public class ExcelExportUtil
{
  public static <M extends Model<M>> void exportByRecord(HttpServletResponse response, HttpServletRequest request, String filename, List<Pair> titles, List<M> records)
  {
    exportByRecord(response, request, filename, new SheetData[] { new SheetData(titles, records) });
  }
  
  public static <M extends Model<M>> void exportByRecord(HttpServletResponse response, HttpServletRequest request, String filename, SheetData<M>... sheetDatas)
  {
    HSSFWorkbook wb = new HSSFWorkbook();
    

    CellStyle titleCellStyle = wb.createCellStyle();
    titleCellStyle.setAlignment((short)2);
    titleCellStyle.setWrapText(true);
    Font font = wb.createFont();
    font.setBoldweight((short)700);
    font.setFontName("微软雅黑");
    titleCellStyle.setFont(font);
    

    CellStyle cellStyle = wb.createCellStyle();
    cellStyle.setAlignment((short)2);
    cellStyle.setVerticalAlignment((short)1);
    cellStyle.setWrapText(true);
    Font font2 = wb.createFont();
    font2.setFontName("微软雅黑");
    cellStyle.setFont(font2);
    for (SheetData<M> sheetData : sheetDatas)
    {
     
	List<Pair> titles = sheetData.titles;
      
      List<M> records = sheetData.records;
      
      HSSFSheet sheet = wb.createSheet();
      
      int rowIndex = 0;int cellIndex = 0;
      
      HSSFRow row = null;
      HSSFCell cell = null;
      

      row = sheet.createRow(rowIndex);
      row.setHeight((short)450);
      rowIndex++;
      for (Pair pair : titles)
      {
        sheet.setColumnWidth(cellIndex, 6000);
        
        cell = row.createCell(cellIndex);
        cell.setCellStyle(titleCellStyle);
        cellIndex++;
        
        cell.setCellValue(pair.title);
      }
      Iterator localIterator2=records.iterator();
      while(localIterator2.hasNext())
      {
        
		M record = (M)localIterator2.next();
        
        row = sheet.createRow(rowIndex);
        row.setHeight((short)450);
        rowIndex++;
        cellIndex = 0;
        

        localIterator2 = titles.iterator();
        Pair pair = (Pair)localIterator2.next();
        
        cell = row.createCell(cellIndex);
        cell.setCellStyle(cellStyle);
        cellIndex++;
        
        Object value = record.get(pair.column);
        if (value != null) {
          cell.setCellValue(value.toString());
        }
      }
    }
    writeStream(filename, wb, response, request);
  }
  
  public static void writeStream(String filename, HSSFWorkbook wb, HttpServletResponse response, HttpServletRequest request)
  {
    try
    {
      String agent = request.getHeader("USER-AGENT");
      
      filename = filename + ".xls";
      
      filename.replaceAll("/", "-");
      if (agent.toLowerCase().indexOf("firefox") > 0) {
        filename = new String(filename.getBytes("utf-8"), "iso-8859-1");
      } else {
        filename = URLEncoder.encode(filename, "UTF-8");
      }
      response.reset();
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Content-Disposition", "attachment; filename=" + filename);
      response.setContentType("application/octet-stream;charset=UTF-8");
      OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
      wb.write(outputStream);
      outputStream.flush();
      outputStream.close();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  public static HSSFSheet setHSSFValidation(HSSFSheet sheet, String promptTitle, String promptContent, String[] textlist, int firstRow, int endRow, int firstCol, int endCol)
  {
    DVConstraint constraint = DVConstraint.createExplicitListConstraint(textlist);
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
    data_validation_list.createPromptBox(promptTitle, promptContent);
    sheet.addValidationData(data_validation_list);
    return sheet;
  }
  
  public static HSSFDataValidation setHSSFPrompt(String promptTitle, String promptContent, int firstRow, int endRow, int firstCol, int endCol)
  {
    DVConstraint constraint = DVConstraint.createCustomFormulaConstraint("BB1");
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox(promptTitle, promptContent);
    return ret;
  }
  
  public static HSSFDataValidation intValidate(int firstRow, int endRow, int firstCol, int endCol, String bgNum, String endNum)
  {
    DVConstraint constraint = DVConstraint.createNumericConstraint(
      1, 
      0, bgNum, endNum);
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox("提示：", "只能输入" + bgNum + "到" + endNum + "的整数");
    return ret;
  }
  
  public static HSSFDataValidation numValidate(int firstRow, int endRow, int firstCol, int endCol, String bgNum, String endNum)
  {
    DVConstraint constraint = DVConstraint.createNumericConstraint(
      2, 
      0, bgNum, endNum);
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox("提示：", "请输入" + bgNum + "到" + endNum + "的数字");
    return ret;
  }
  
  public static HSSFDataValidation lenValidate(int firstRow, int endRow, int firstCol, int endCol, String lenNum, String txt)
  {
    DVConstraint constraint = DVConstraint.createNumericConstraint(
      6, 
      0, "0", lenNum);
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox("提示：", txt);
    return ret;
  }
  
  public static HSSFDataValidation dateValidate(int firstRow, int endRow, int firstCol, int endCol, String minDate, String maxDate)
  {
    DVConstraint constraint = DVConstraint.createDateConstraint(0, minDate, maxDate, "yyyy-mm-dd");
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox("提示：", "请输入大于" + minDate + "小于" + maxDate + "的日期！");
    return ret;
  }
  
  public static HSSFDataValidation notNullValidate(int firstRow, int endRow, int firstCol, int endCol, String txt)
  {
    DVConstraint constraint = DVConstraint.createNumericConstraint(
      6, 
      0, "1", "10000");
    
    CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
    
    HSSFDataValidation ret = new HSSFDataValidation(regions, constraint);
    ret.createPromptBox("提示：", txt);
    return ret;
  }
  
  public static boolean isBlankRow(HSSFSheet sheet, HSSFRow row)
  {
    if (row == null) {
      return true;
    }
    boolean r = true;
    for (int i = 0; i < row.getLastCellNum(); i++)
    {
      HSSFCell titleCell = sheet.getRow(0).getCell(i);
      HSSFCell cell = row.getCell(i);
      CellStyle style = titleCell.getCellStyle();
      if (style.getLocked())
      {
        String value = "";
        if (cell == null) {
          break;
        }
        switch (cell.getCellType())
        {
        case 1: 
          value = cell.getStringCellValue();
          break;
        case 0: 
          value = String.valueOf((int)cell.getNumericCellValue());
          break;
        case 4: 
          value = String.valueOf(cell.getBooleanCellValue());
          break;
        case 2: 
          value = String.valueOf(cell.getCellFormula());
          break;
        }
        if (!value.trim().equals(""))
        {
          r = false;
          break;
        }
      }
    }
    return r;
  }
  
  public static Object getCellVal(HSSFCell cell)
  {
    Object val = null;
    if (cell != null)
    {
      int t = cell.getCellType();
      if ((t == 0) && 
        (HSSFDateUtil.isCellDateFormatted(cell)))
      {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue())).toString();
      }
      cell.setCellType(1);
      switch (cell.getCellType())
      {
      case 0: 
        val = Double.valueOf(cell.getNumericCellValue());
        break;
      case 1: 
        val = cell.getStringCellValue();
        String str = val.toString();
        str = str.replaceAll("\n", "");
        val = str.replaceAll("\r", "");
        if (StringUtil.isNull(str)) {
          val = null;
        }
        break;
      case 4: 
        val = Boolean.valueOf(cell.getBooleanCellValue());
        break;
      case 3: 
        val = null;
        break;
      case 2: 
      default: 
        System.out.println("未知类型");
      }
    }
    return val;
  }
  
  public static String numConvertLetter(int num)
  {
    String[] letter = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "G", 
      "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", 
      "W", "X", "Y", "Z" };
    if (num > 26)
    {
      int firstLetter = num / 26;
      String str1 = letter[(firstLetter - 1)];
    }
    String let = letter[(num % 26 - 1)];
    return let;
  }
  
  public static class Coord
  {
    public String code;
    public String title;
    public int row;
    public String cell;
    
    public Coord(String code, String title, int row, int cell)
    {
      this.code = code;
      this.title = title;
      this.row = row;
      this.cell = ExcelExportUtil.numConvertLetter(cell);
    }
  }
  
  public static class Pair
  {
    public String column;
    public String title;
    
    public Pair(String column, String title)
    {
      this.column = column;
      
      this.title = title;
    }
  }
  
  public static class TmpPair
  {
    public Boolean isStr;
    public String column;
    public String title;
    public CellStyle style;
    
    public TmpPair(Boolean isStr, String column, String title, CellStyle style)
    {
      this.isStr = isStr;
      this.column = column;
      this.title = title;
      this.style = style;
    }
  }
  
  public static class SheetData<M extends Model<M>>
  {
    public List<ExcelExportUtil.Pair> titles;
    public List<M> records;
    
    public SheetData(List<ExcelExportUtil.Pair> titles, List<M> records)
    {
      this.titles = titles;
      
      this.records = records;
    }
  }
}
