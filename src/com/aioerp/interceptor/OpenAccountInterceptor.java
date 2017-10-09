package com.aioerp.interceptor;

import com.aioerp.common.AioConstants;
import com.aioerp.model.sys.AioerpSys;
import com.aioerp.model.sys.User;
import com.aioerp.util.DateUtils;
import com.aioerp.util.Dog;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OpenAccountInterceptor
  implements Interceptor
{
  public void intercept(ActionInvocation ai)
  {
    Controller controller = ai.getController();
    Record user = (Record)controller.getSessionAttr("user");
    String actionKey = ai.getActionKey();
    System.out.println(actionKey);
    


    Map<String, String> actionKeyMap = new HashMap();
    
    actionKeyMap.put("/bought/purchase/add", "");
    actionKeyMap.put("/bought/return/add", "");
    actionKeyMap.put("/bought/barter/add", "");
    
    actionKeyMap.put("/sell/sell/add", "销售单添加");
    actionKeyMap.put("/sell/return/add", "销售退货单添加");
    actionKeyMap.put("/sell/barter/add", "销售换货单添加");
    
    actionKeyMap.put("/stock/takeStock/add", "");
    actionKeyMap.put("/stock/breakage/add", "");
    actionKeyMap.put("/stock/overflow/add", "");
    actionKeyMap.put("/stock/otherin/add", "其它入库单");
    actionKeyMap.put("/stock/otherout/add", "其它出库单");
    actionKeyMap.put("/stock/parityAllot/add", "");
    actionKeyMap.put("/stock/difftAllot/add", "");
    actionKeyMap.put("/stock/dismount/add", "");
    

    actionKeyMap.put("/finance/getMoney/add", "收款单");
    actionKeyMap.put("/finance/payMoney/add", "付款单");
    actionKeyMap.put("/finance/fee/add", "费用单");
    actionKeyMap.put("/finance/otherEarn/add", "其它收入单");
    actionKeyMap.put("/finance/transfer/add", "内部转款单");
    actionKeyMap.put("/finance/adjustCost/add", "成本调价单");
    actionKeyMap.put("/finance/addAssets/add", "固定资产购置单");
    actionKeyMap.put("/finance/deprAssets/add", "固定资产折旧单");
    actionKeyMap.put("/finance/subAssets/add", "固定资产变卖单");
    actionKeyMap.put("/finance/changeUnit/add", "应收应付  资金 变化");
    actionKeyMap.put("/finance/accountVoucher/add", "会计凭证");
    if (user != null)
    {
      String loginConfigId = user.getStr("loginConfigId") == null ? AioConstants.CONFIG_NAME : user.getStr("loginConfigId");
      boolean isTry = true;
      Map<String, String> result = new HashMap();
      boolean isFalse = true;
      try
      {
        if (("no".equals(AioConstants.IS_FREE_VERSION)) && (!"".equals(Dog.getID())) && (Dog.getVersion() == AioConstants.VERSION))
        {
          isTry = false;
          if (actionKeyMap.containsKey(actionKey)) {
            if ((!Dog.isRegister()) && (Dog.getLastDay() <= 0))
            {
              result.put("statusCode", "300");
              result.put("message", "加密狗未注册，请帮助中心注册！");
              isFalse = false;
            }
            else
            {
              int userCount = User.dao.getUserCount(user.getStr("loginConfigId"));
              if ((userCount > Dog.getUserCount()) && (actionKeyMap.containsKey(actionKey)))
              {
                result.put("statusCode", "300");
                result.put("message", "您购买的软件为" + Dog.getUserCount() + "用户版，请先删除多余用户！");
                isFalse = false;
              }
            }
          }
        }
      }
      catch (Error localError) {}
      if (AioConstants.IS_FREE_VERSION != "yes") {
        if ((isTry) && (actionKeyMap.containsKey(actionKey)))
        {
          Date currentDate = new Date();
          Date time = DateUtils.addDays(user.getDate("loginConfigCreatTime"), AioConstants.TEST_DAY);
          if (currentDate.after(time))
          {
            result.put("statusCode", "300");
            result.put("message", "试用版到期,请购买正式版本");
            isFalse = false;
          }
          else
          {
            Integer billCount = (Integer)AioConstants.accountBillCount.get(loginConfigId);
            if ((billCount != null) && (billCount.intValue() > AioConstants.TEST_BILL_COUNT))
            {
              result.put("statusCode", "300");
              result.put("message", "试用版到期,请购买正式版本");
              isFalse = false;
            }
          }
        }
      }
      if ((AioConstants.IO_OVER) && (actionKeyMap.containsKey(actionKey)))
      {
        result.put("statusCode", "300");
        result.put("message", "数据库被损坏或者中毒，请联系简易软件总部  020-87530321");
        isFalse = false;
      }
      if ((isTry) && (((Integer)AioConstants.PROJECT_STATUS_MAP.get(loginConfigId)).intValue() != AioConstants.PROJECT_STATUS0))
      {
        result.put("statusCode", "300");
        isFalse = false;
        if (((Integer)AioConstants.PROJECT_STATUS_MAP.get(loginConfigId)).intValue() == AioConstants.PROJECT_STATUS1) {
          result.put("message", "当前账套系统正在备份，请稍后再试...");
        } else if (((Integer)AioConstants.PROJECT_STATUS_MAP.get(loginConfigId)).intValue() == AioConstants.PROJECT_STATUS2) {
          result.put("message", "当前账套系统正在恢复 ，请稍后再试...");
        } else if (((Integer)AioConstants.PROJECT_STATUS_MAP.get(loginConfigId)).intValue() == AioConstants.PROJECT_STATUS3) {
          result.put("message", "当前账套系统正在重建，请稍后再试...");
        } else if (((Integer)AioConstants.PROJECT_STATUS_MAP.get(loginConfigId)).intValue() == AioConstants.PROJECT_STATUS4) {
          result.put("message", "当前账套系统正在成本重算，请稍后再试...");
        }
      }
      if ((user.getStr("loginConfigId") == null) || (user.getStr("loginConfigId").equals(AioConstants.CONFIG_NAME)))
      {
        if (isFalse) {
          ai.invoke();
        }
      }
      else
      {
        if ((loginConfigId != null) && (!loginConfigId.equals(AioConstants.CONFIG_NAME)))
        {
          Integer status = (Integer)AioConstants.WHICHDBID_STATUS.get(loginConfigId);
          if (status.intValue() == AioConstants.STATUS_DISABLE)
          {
            result.put("statusCode", "300");
            result.put("message", "账套已经停用，请退出");
            isFalse = false;
          }
        }
        String hasOpenAccount = AioerpSys.dao.getValue1(user.getStr("loginConfigId"), "hasOpenAccount");
        if ((hasOpenAccount.equals(AioConstants.HAS_OPEN_ACCOUNT0)) && 
          (actionKeyMap.containsKey(actionKey)))
        {
          result.put("statusCode", "300");
          result.put("message", "没有开账，请先开账");
          isFalse = false;
        }
      }
      if (!isFalse) {
        controller.renderJson(result);
      } else {
        ai.invoke();
      }
    }
    else
    {
      ai.invoke();
    }
  }
}
