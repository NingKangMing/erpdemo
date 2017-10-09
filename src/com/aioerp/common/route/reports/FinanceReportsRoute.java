package com.aioerp.common.route.reports;

import com.aioerp.controller.reports.finance.AccountsCostTotalController;
import com.aioerp.controller.reports.finance.AssetDebtTotalController;
import com.aioerp.controller.reports.finance.BusinessCountController;
import com.aioerp.controller.reports.finance.ChanceCostPriceCountController;
import com.aioerp.controller.reports.finance.CostDistributionController;
import com.aioerp.controller.reports.finance.CostTotalController;
import com.aioerp.controller.reports.finance.YearCostController;
import com.aioerp.controller.reports.finance.YearCostTotalController;
import com.aioerp.controller.reports.finance.arap.ArapAgeController;
import com.aioerp.controller.reports.finance.arap.ArapAnalysisController;
import com.aioerp.controller.reports.finance.arap.ArapMoneyCheckController;
import com.aioerp.controller.reports.finance.arap.ArapOverPayOrGetMoneyController;
import com.aioerp.controller.reports.finance.arap.ArapSettlementByOrderController;
import com.aioerp.controller.reports.finance.arap.AreaArapController;
import com.aioerp.controller.reports.finance.arap.DeptArapController;
import com.aioerp.controller.reports.finance.arap.StaffArapController;
import com.aioerp.controller.reports.finance.arap.UnitArapController;
import com.aioerp.controller.reports.retmoney.ReturnMoneyAreaController;
import com.aioerp.controller.reports.retmoney.ReturnMoneyDepmController;
import com.aioerp.controller.reports.retmoney.ReturnMoneyStaffController;
import com.aioerp.controller.reports.retmoney.ReturnMoneyUnitController;
import com.aioerp.controller.reports.retmoney.ReturnMoneyUserController;
import com.jfinal.config.Routes;

public class FinanceReportsRoute
  extends Routes
{
  public void config()
  {
    add("/reports/finance/arap/unitArap", UnitArapController.class);
    add("/reports/finance/arap/staffArap", StaffArapController.class);
    add("/reports/finance/arap/deptArap", DeptArapController.class);
    add("/reports/finance/arap/areaArap", AreaArapController.class);
    
    add("/reports/finance/businessCount", BusinessCountController.class);
    
    add("/reports/retmoney/staff", ReturnMoneyStaffController.class);
    add("/reports/retmoney/unit", ReturnMoneyUnitController.class);
    add("/reports/retmoney/depm", ReturnMoneyDepmController.class);
    add("/reports/retmoney/area", ReturnMoneyAreaController.class);
    add("/reports/retmoney/user", ReturnMoneyUserController.class);
    
    add("/reports/finance/arap/analysis", ArapAnalysisController.class);
    add("/reports/finance/arap/arapMoneyCheck", ArapMoneyCheckController.class);
    add("/reports/finance/arap/ageAnalysis", ArapAgeController.class);
    add("/reports/finance/arap/settlementByOrder", ArapSettlementByOrderController.class);
    add("/reports/finance/arap/overPayGetMoney", ArapOverPayOrGetMoneyController.class);
    
    add("/reports/finance/changeCostPrice", ChanceCostPriceCountController.class);
    
    add("/reports/finance/yearCost", YearCostController.class);
    add("/reports/finance/costTotal", CostTotalController.class);
    add("/reports/finance/costDistribution", CostDistributionController.class);
    add("/reports/finance/accounts/costTotal", AccountsCostTotalController.class);
    add("/reports/finance/accounts/assetDebtTotal", AssetDebtTotalController.class);
    add("/reports/finance/yearCostTotal", YearCostTotalController.class);
  }
}
