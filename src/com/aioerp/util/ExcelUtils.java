package com.aioerp.util;

import java.util.Date;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.Region;

public class ExcelUtils
{
  public static HSSFCell createCell(HSSFRow row, short cellnum, HSSFCellStyle style, String value)
  {
    HSSFCell cell = row.createCell(cellnum++);
    
    cell.setCellStyle(style);
    cell.setCellType(1);
    cell.setCellValue(value);
    return cell;
  }
  
  public static HSSFCell createCell1(HSSFRow row, short cellnum, HSSFCellStyle style, String value)
  {
    HSSFCell cell = row.getCell(cellnum);
    if (cell == null) {
      cell = row.createCell(cellnum);
    }
    cell.setCellStyle(style);
    cell.setCellType(1);
    cell.setCellValue(value);
    return cell;
  }
  
  public static HSSFCell createCell(HSSFRow row, short cellnum, HSSFCellStyle style, int value)
  {
    HSSFCell cell = row.createCell(cellnum++);
    
    cell.setCellStyle(style);
    cell.setCellType(0);
    cell.setCellValue(value);
    return cell;
  }
  
  public static HSSFCell createCell1(HSSFRow row, short cellnum, HSSFCellStyle style, int value)
  {
    HSSFCell cell = row.getCell(cellnum);
    if (cell == null) {
      cell = row.createCell(cellnum);
    }
    cell.setCellStyle(style);
    cell.setCellType(0);
    cell.setCellValue(value);
    return cell;
  }
  
  public static HSSFCell createCell(HSSFRow row, short cellnum, HSSFCellStyle style, double value)
  {
    HSSFCell cell = row.createCell(cellnum++);
    
    cell.setCellStyle(style);
    cell.setCellType(0);
    cell.setCellValue(value);
    return cell;
  }
  
  public static HSSFCell createCell(HSSFRow row, short cellnum, HSSFCellStyle style, Date value)
  {
    HSSFCell cell = row.createCell(cellnum++);
    
    cell.setCellStyle(style);
    cell.setCellType(0);
    if (value != null) {
      cell.setCellValue(value);
    } else {
      cell.setCellValue("");
    }
    return cell;
  }
  
  public static HSSFCell createCellWithFormula(HSSFRow row, short cellnum, HSSFCellStyle style, String formula)
  {
    HSSFCell cell = row.createCell(cellnum++);
    
    cell.setCellStyle(style);
    cell.setCellType(2);
    cell.setCellFormula(formula);
    return cell;
  }
  
  public static Region createRegion(HSSFSheet sheet, short rowFrom, short columnFrom, short rowTo, short columnTo, HSSFCellStyle style, String value)
  {
    HSSFRow row = null;
    for (short i = rowFrom; i <= rowTo; i = (short)(i + 1))
    {
      row = sheet.getRow(i);
      if (row == null) {
        row = sheet.createRow(i);
      }
      for (short j = columnFrom; j <= columnTo; j = (short)(j + 1)) {
        createCell(row, j, style, value);
      }
    }
    Region region = new Region(rowFrom, columnFrom, rowTo, columnTo);
    sheet.addMergedRegion(region);
    return region;
  }
  
  public static HSSFCellStyle getStyle(HSSFWorkbook workbook, String fontName, short fontSize, short weight, short border, short align, short valign)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontName(fontName);
    f.setFontHeightInPoints(fontSize);
    f.setBoldweight(weight);
    style.setFont(f);
    style.setAlignment(align);
    style.setVerticalAlignment(valign);
    style.setBorderBottom(border);
    style.setBorderLeft(border);
    style.setBorderRight(border);
    style.setBorderTop(border);
    return style;
  }
  
  public static HSSFCellStyle getTitleStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontName("黑体");
    f.setFontHeightInPoints((short)20);
    f.setBoldweight((short)700);
    style.setFont(f);
    style.setAlignment((short)2);
    style.setVerticalAlignment((short)1);
    return style;
  }
  
  public static HSSFCellStyle getHeadStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)11);
    f.setBoldweight((short)700);
    style.setFont(f);
    style.setAlignment((short)2);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    style.setFillBackgroundColor((short)9);
    return style;
  }
  
  public static HSSFCellStyle getSmallHeadStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)11);
    f.setBoldweight((short)700);
    style.setFont(f);
    style.setAlignment((short)1);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    style.setFillBackgroundColor((short)9);
    return style;
  }
  
  public static HSSFCellStyle getBigHeadStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)15);
    f.setBoldweight((short)700);
    style.setFont(f);
    style.setAlignment((short)2);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    style.setFillBackgroundColor((short)9);
    return style;
  }
  
  public static HSSFCellStyle getTextStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)2);
    style.setVerticalAlignment((short)1);
    style.setWrapText(true);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    return style;
  }
  
  public static HSSFCellStyle getTextStyleAlignLeft(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)1);
    style.setVerticalAlignment((short)1);
    style.setWrapText(true);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    return style;
  }
  
  public static HSSFCellStyle getTextStyleNoBorder(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)2);
    style.setVerticalAlignment((short)1);
    return style;
  }
  
  public static HSSFCellStyle getTextStyleNoBorderLeft(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)11);
    style.setFont(f);
    style.setAlignment((short)1);
    style.setVerticalAlignment((short)1);
    return style;
  }
  
  public static HSSFCellStyle getTextStyleNoBorderRight(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)3);
    style.setVerticalAlignment((short)1);
    return style;
  }
  
  public static HSSFCellStyle getNumberStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)3);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    
    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
    return style;
  }
  
  public static HSSFCellStyle getPercentStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)3);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    
    style.setDataFormat(HSSFDataFormat.getBuiltinFormat("0.00%"));
    return style;
  }
  
  public static HSSFCellStyle getMoneyFormatStyle(HSSFWorkbook workbook)
  {
    HSSFCellStyle style = workbook.createCellStyle();
    HSSFFont f = workbook.createFont();
    f.setFontHeightInPoints((short)10);
    style.setFont(f);
    style.setAlignment((short)3);
    style.setVerticalAlignment((short)1);
    short type = 1;
    style.setBorderBottom(type);
    style.setBorderLeft(type);
    style.setBorderRight(type);
    style.setBorderTop(type);
    

    style.setDataFormat((short)4);
    return style;
  }
  
  public static String getCellStrValue(HSSFRow row, short cellNum)
  {
    String value = null;
    if (row != null)
    {
      HSSFCell cell = row.getCell(cellNum);
      if (cell != null)
      {
        try
        {
          value = cell.getStringCellValue();
        }
        catch (Exception e)
        {
          try
          {
            value = String.valueOf(cell.getNumericCellValue());
          }
          catch (Exception e2)
          {
            value = null;
          }
        }
        if (value != null) {
          value = value.trim();
        }
      }
    }
    return value;
  }
}
