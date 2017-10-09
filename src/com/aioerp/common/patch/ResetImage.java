package com.aioerp.common.patch;

import com.aioerp.common.AioConstants;
import com.aioerp.model.base.Product;
import com.aioerp.model.sys.AioerpFile;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.util.IO;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import java.io.File;
import java.util.List;

public class ResetImage
{
  public static void reset(String configName)
  {
    Record reload = AioerpSys.dao.getObj(configName, "resetImage");
    if ((reload != null) && (reload.getStr("value1").equals("1"))) {
      return;
    }
    String projectPath = IO.getWebrootPath();
    
    String oldFolderPath = projectPath + "upLoadImg" + File.separator;
    

    String productFolder = oldFolderPath + "base" + File.separator + configName + File.separator + "product" + File.separator;
    String staffFolder = oldFolderPath + "base" + File.separator + configName + File.separator + "staff" + File.separator;
    String orderFolder = oldFolderPath + "base" + File.separator + configName + File.separator + "order" + File.separator;
    String savePath="";
    IO.existFolder(productFolder);
    IO.existFolder(staffFolder);
    IO.existFolder(orderFolder);
    File oldFile = null;
    
    List<Model> attachments = AioerpFile.dao.find(configName, "select * from aioerp_file");
    Model product;
    for (Model attach : attachments)
    {
      Integer tableId = attach.getInt("tableId");
      savePath = attach.getStr("savePath");
      oldFile = new File(oldFolderPath + savePath);
      if (oldFile.exists())
      {
        if (tableId.intValue() == AioConstants.TABLE_1)
        {
          File newFile = new File(productFolder + oldFile.getName());
          IO.fileChannelCopy(oldFile, newFile);
          savePath = "base/" + configName + "/product/" + oldFile.getName();
          attach.set("savePath", savePath).update(configName);
          oldFile.delete();
          product = Product.dao.findFirst(configName, "select * from b_product where sysImgId = " + attach.getInt("id"));
          if (product != null) {
            product.set("savePath", savePath).update(configName);
          }
        }
        else if (tableId.intValue() == AioConstants.TABLE_2)
        {
          File newFile = new File(staffFolder + oldFile.getName());
          IO.fileChannelCopy(oldFile, newFile);
          savePath = "base/" + configName + "/staff/" + oldFile.getName();
          attach.set("savePath", savePath).update(configName);
          oldFile.delete();
        }
        else
        {
          File newFile = new File(orderFolder + oldFile.getName());
          IO.fileChannelCopy(oldFile, newFile);
          savePath = "base/" + configName + "/order/" + oldFile.getName();
          attach.set("savePath", savePath).update(configName);
          oldFile.delete();
        }
      }
      else {
        attach.delete(configName);
      }
    }
    String oldPath = oldFolderPath + "base";
    File[] waitDeleteFiles = new File(oldPath).listFiles();
   /* File newFile = (product = waitDeleteFiles).length;
    for (String savePath = 0; savePath < newFile; savePath++)
    {
      File file = product[savePath];
      String dirName = file.getName();
      if ((dirName.equals("order")) || (dirName.equals("staff")) || (dirName.equals("product"))) {
        IO.deleteFile(file);
      }
    }*/
    AioerpSys.dao.sysSaveOrUpdate(configName, "resetImage", "1", "系统单据图片转移     0未   1已转移");
  }
}
