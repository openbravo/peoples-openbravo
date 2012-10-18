/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;

public class ReservationUtils {

  public Reservation createReserveFromSalesOrderLine(OrderLine soLine, boolean doProcess)
      throws OBException {
    if (!soLine.getSalesOrder().isSalesTransaction()) {
      throw new OBException("@cannotReservePurchaseOrder@");
    }
    if (soLine.getOrderedQuantity().subtract(soLine.getDeliveredQuantity())
        .compareTo(BigDecimal.ZERO) == 0) {
      throw new OBException("@cannotReserveDeliveredSalesOrderLine@");
    }

    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(soLine.getId());
    parameters.add(doProcess ? "Y" : "N");
    parameters.add(DalUtil.getId(OBContext.getOBContext().getUser()));
    Reservation reservation = (Reservation) CallStoredProcedure.getInstance().call(
        "M_CREATE_RESERVE_FROM_SOL", parameters, null);

    return reservation;
  }

  public OBError createReserveStock(Reservation reservation) throws OBException {

    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(reservation.getId());
    String message = (String) CallStoredProcedure.getInstance().call("M_CREATE_RESERVE_STOCK",
        parameters, null);

    OBError obmessage = new OBError();
    obmessage.setType("SUCCESS");
    obmessage.setMessage(message);
    return obmessage;
  }

  public OBError addReserveStock(Reservation reservation, StorageDetail stock, OrderLine poLine,
      BigDecimal quantity) throws OBException {

    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(reservation.getId());
    parameters.add(stock.getId());
    parameters.add(poLine.getId());
    parameters.add(quantity);
    String message = (String) CallStoredProcedure.getInstance().call("M_ADD_RESERVED_STOCK",
        parameters, null);

    OBError obmessage = new OBError();
    obmessage.setType("SUCCESS");
    obmessage.setMessage(message);
    return obmessage;
  }

  public OBError processReserve(Reservation reservation, String action) throws OBException {

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "");
    } finally {
      OBContext.restorePreviousMode();
    }

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("action", action);

    final ProcessInstance pinstance = CallProcess.getInstance().call(process, reservation.getId(),
        parameters);

    return OBMessageUtils.getProcessInstanceMessage(pinstance);
  }

}
