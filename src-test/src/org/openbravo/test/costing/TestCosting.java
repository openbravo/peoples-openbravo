/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.costing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.junit.Test;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.costing.CostingBackground;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.datasource.BaseDataSourceTestDal;

/**
 * Test cases to verify Costing Adjustment Project
 * 
 * @author mdejuana
 */

public class TestCosting extends BaseDataSourceTestDal {

  @Test
  public void testCostingCCC() throws Exception {

    OBContext.setAdminMode(false);

    try {
      Order purchaseOrder = cloneOrder("E8D703003128490C80FFECBF893ABDDC");
      bookOrder(purchaseOrder);
      runCostingBackground();

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    OBContext.restorePreviousMode();
    OBDal.getInstance().commitAndClose();

  }

  private void runCostingBackground() throws Exception {
    VariablesSecureApp vars = null;
    vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(), OBContext
        .getOBContext().getCurrentClient().getId(), OBContext.getOBContext()
        .getCurrentOrganization().getId(), OBContext.getOBContext().getRole().getId(), OBContext
        .getOBContext().getLanguage().getLanguage());
    ConnectionProvider conn = new DalConnectionProvider(true);
    ProcessBundle pb = new ProcessBundle(CostingBackground.AD_PROCESS_ID, vars).init(conn);
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    pb.setParams(parameters);
    new CostingBackground().execute(pb);
  }

  private Order cloneOrder(String orderId) {
    try {
      User currentUser = OBContext.getOBContext().getUser();
      Order objOrder = OBDal.getInstance().get(Order.class, orderId);
      Order objCloneOrder = (Order) DalUtil.copy(objOrder, false);
      BigDecimal bLineNetAmt = getLineNetAmt(orderId);

      DocumentType docType = FIN_Utility.getDocumentType(OBContext.getOBContext()
          .getCurrentOrganization(), "POO");
      String docNo = FIN_Utility.getDocumentNo(docType, "C_Order");
      objCloneOrder.setDocumentAction("CO");
      objCloneOrder.setDocumentStatus("DR");
      objCloneOrder.setPosted("N");
      objCloneOrder.setProcessed(false);
      objCloneOrder.setDelivered(false);
      objCloneOrder.setSalesTransaction(true);
      objCloneOrder.setDocumentType(docType);
      objCloneOrder.setDocumentNo(docNo);
      objCloneOrder.setSalesTransaction(objOrder.isSalesTransaction());
      objCloneOrder.setCreationDate(new Date());
      objCloneOrder.setUpdated(new Date());
      objCloneOrder.setCreatedBy(currentUser);
      objCloneOrder.setUpdatedBy(currentUser);
      objCloneOrder.setReservationStatus(null);

      objCloneOrder.setOrderDate(new Date());
      objCloneOrder.setScheduledDeliveryDate(new Date());

      // save the cloned order object
      OBDal.getInstance().save(objCloneOrder);

      objCloneOrder.setSummedLineAmount(objCloneOrder.getSummedLineAmount().subtract(bLineNetAmt));
      objCloneOrder.setGrandTotalAmount(objCloneOrder.getGrandTotalAmount().subtract(bLineNetAmt));

      // get the lines associated with the order and clone them to the new
      // order line.
      for (OrderLine ordLine : objOrder.getOrderLineList()) {
        String strPriceVersionId = getPriceListVersion(objOrder.getPriceList().getId(), objOrder
            .getClient().getId());
        BigDecimal bdPriceList = getPriceList(ordLine.getProduct().getId(), strPriceVersionId);
        OrderLine objCloneOrdLine = (OrderLine) DalUtil.copy(ordLine, false);
        objCloneOrdLine.setReservedQuantity(new BigDecimal("0"));
        objCloneOrdLine.setDeliveredQuantity(new BigDecimal("0"));
        objCloneOrdLine.setInvoicedQuantity(new BigDecimal("0"));
        if (!"".equals(bdPriceList) || bdPriceList != null
            || !bdPriceList.equals(BigDecimal.ZERO.setScale(bdPriceList.scale()))) {
          objCloneOrdLine.setListPrice(bdPriceList);
        }
        objCloneOrdLine.setCreationDate(new Date());
        objCloneOrdLine.setUpdated(new Date());
        objCloneOrdLine.setCreatedBy(currentUser);
        objCloneOrdLine.setUpdatedBy(currentUser);
        objCloneOrdLine.setOrderDate(new Date());
        objCloneOrdLine.setScheduledDeliveryDate(new Date());
        objCloneOrder.getOrderLineList().add(objCloneOrdLine);
        objCloneOrdLine.setSalesOrder(objCloneOrder);
        objCloneOrdLine.setReservationStatus(null);
      }

      OBDal.getInstance().save(objCloneOrder);

      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(objCloneOrder);
      OBDal.getInstance().commitAndClose();
      return objCloneOrder;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private void bookOrder(Order order) {
    final List<Object> parameters = new ArrayList<Object>();
    parameters.add(null);
    parameters.add(order.getId());
    parameters.add("N");
    final String procedureName = "c_order_post1";
    CallStoredProcedure.getInstance().call(procedureName, parameters, null, true, false);
  }

  private String getPriceListVersion(String priceList, String clientId) {
    try {
      String whereClause = " as plv left outer join plv.priceList pl where plv.active='Y' and plv.active='Y' and "
          + " pl.id = :priceList and plv.client.id = :clientId order by plv.validFromDate desc";

      OBQuery<PriceListVersion> ppriceListVersion = OBDal.getInstance().createQuery(
          PriceListVersion.class, whereClause);
      ppriceListVersion.setNamedParameter("priceList", priceList);
      ppriceListVersion.setNamedParameter("clientId", clientId);

      if (!ppriceListVersion.list().isEmpty()) {
        return ppriceListVersion.list().get(0).getId();
      } else {
        return "0";
      }
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private BigDecimal getPriceList(String strProductID, String strPriceVersionId) {
    BigDecimal bdPriceList = null;
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(strProductID);
      parameters.add(strPriceVersionId);
      final String procedureName = "M_BOM_PriceList";
      bdPriceList = (BigDecimal) CallStoredProcedure.getInstance().call(procedureName, parameters,
          null);
    } catch (Exception e) {
      throw new OBException(e);
    }

    return (bdPriceList);
  }

  public static BigDecimal getLineNetAmt(String strOrderId) {

    BigDecimal bdLineNetAmt = new BigDecimal("0");
    final String readLineNetAmtHql = " select (coalesce(ol.lineNetAmount,0) + coalesce(ol.freightAmount,0) + coalesce(ol.chargeAmount,0)) as LineNetAmt from OrderLine ol where ol.salesOrder.id=?";
    final Query readLineNetAmtQry = OBDal.getInstance().getSession().createQuery(readLineNetAmtHql);
    readLineNetAmtQry.setString(0, strOrderId);

    for (int i = 0; i < readLineNetAmtQry.list().size(); i++) {
      bdLineNetAmt = bdLineNetAmt.add(((BigDecimal) readLineNetAmtQry.list().get(i)));
    }

    return bdLineNetAmt;
  }
}