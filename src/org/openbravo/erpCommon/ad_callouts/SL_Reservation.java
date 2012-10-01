package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.order.OrderLine;

public class SL_Reservation extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String lastChanged = info.getLastFieldChanged();

    if ("inpcOrderlineId".equals(lastChanged)) {
      final String strSalesOrderLineId = info.getStringParameter("inpcOrderlineId",
          IsIDFilter.instance);
      if (!"".equals(strSalesOrderLineId)) {
        final OrderLine soLine = OBDal.getInstance().get(OrderLine.class, strSalesOrderLineId);
        info.addResult("inpquantity", soLine.getOrderedQuantity());
        info.addResult("inppendingqty",
            soLine.getOrderedQuantity().subtract(soLine.getDeliveredQuantity()));
        info.addResult("inpmProductId", (String) DalUtil.getId(soLine.getProduct()));
      }
    }
  }

}
