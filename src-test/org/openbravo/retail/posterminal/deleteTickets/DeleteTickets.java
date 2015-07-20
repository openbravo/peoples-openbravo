/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.deleteTickets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.enterprise.inject.Instance;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.retail.posterminal.OBBaseRetailTest;
import org.openbravo.retail.posterminal.OrderLoader;
import org.openbravo.retail.posterminal.ProcessCashClose;

/**
 * A testcase of testing performance of order loading and cashups.
 * 
 * This testcase uses several json files (see same package). The json has been captured/printed in
 * the saveRecord methods of the {@link OrderLoader} and the {@link ProcessCashClose}. The json was
 * manually changed by replacing the order id and cashup id in the json files with the variables:
 * $orderId and $cashupId. Note the order json has both an order id as well as a cashup id.
 * 
 * @author mtaal
 */
public class DeleteTickets extends OBBaseRetailTest {

  @Test
  public void orderLoaderCreatingDelTickets() throws Exception {

    String standardOrder = IOUtils.toString(this.getClass()
        .getResourceAsStream("del_order_wt.json"));
    String standarOrderWDiscounts = IOUtils.toString(this.getClass().getResourceAsStream(
        "del_order_wt_wdis.json"));
    String standardOrderWTaxes = IOUtils.toString(this.getClass().getResourceAsStream(
        "del_order_wout.json"));
    String standarOrderWDiscountsWTaxes = IOUtils.toString(this.getClass().getResourceAsStream(
        "del_order_wout_wdis.json"));

    // warmup
    loadOneOrder(standardOrder);
    loadOneOrder(standarOrderWDiscounts);
    loadOneOrder(standardOrderWTaxes);
    loadOneOrder(standarOrderWDiscountsWTaxes);

    // Get documentno's of the orders
    ArrayList<String> documentno = new ArrayList<String>();
    documentno.add(getDocumentno(standardOrder));
    documentno.add(getDocumentno(standardOrder));
    documentno.add(getDocumentno(standardOrder));
    documentno.add(getDocumentno(standardOrder));

    final String sql = "select o.em_obpos_isdeleted headerDeleted, "
        + "ol.em_obpos_isdeleted lineDeleted, ol.em_obpos_qtydeleted, "
        + "o.grandtotal, o.totallines, o.docstatus, amtoffer "
        + "from c_order o, c_orderline ol left join (select sum(amtoffer) amtoffer,olo.c_orderline_id "
        + "from c_orderline_offer olo group by olo.c_orderline_id) as offer on ol.c_orderline_id = offer.c_orderline_id "
        + "where o.c_order_id = ol.c_order_id "
        + "and documentno = 'DocumentToBeReplaced' order by o.created desc limit 1";

    ConnectionProvider connectionProvider = getConnectionProvider();
    PreparedStatement st;
    ResultSet result;
    String query;
    HashMap<String, String> queryResult = null;
    for (String document : documentno) {
      query = sql.replace("DocumentToBeReplaced", document);
      st = connectionProvider.getPreparedStatement(query);
      result = st.executeQuery();
      if (result.next()) {
        queryResult = getResult(result);
      }
      verifyResult(queryResult);
      result.close();
    }

  }

  private void verifyResult(HashMap<String, String> queryResult) {
    Assert.assertEquals("Y", queryResult.get("headerDeleted"));
    Assert.assertEquals("Y", queryResult.get("lineDeleted"));
    Assert.assertEquals("1", queryResult.get("em_obpos_qtydeleted"));
    Assert.assertEquals("0", queryResult.get("grandtotal"));
    Assert.assertEquals("0", queryResult.get("totallines"));
    Assert.assertEquals("CL", queryResult.get("docstatus"));
    if (!queryResult.get("amtoffer").isEmpty()) {
      Assert.assertEquals("0", queryResult.get("amtoffer"));
    }
  }

  private HashMap<String, String> getResult(ResultSet result) {
    HashMap<String, String> arrayResult = new HashMap<String, String>();
    try {
      arrayResult.put("headerDeleted", UtilSql.getValue(result, "headerDeleted"));
      arrayResult.put("lineDeleted", UtilSql.getValue(result, "lineDeleted"));
      arrayResult.put("em_obpos_qtydeleted", UtilSql.getValue(result, "em_obpos_qtydeleted"));
      arrayResult.put("grandtotal", UtilSql.getValue(result, "grandtotal"));
      arrayResult.put("totallines", UtilSql.getValue(result, "totallines"));
      arrayResult.put("docstatus", UtilSql.getValue(result, "docstatus"));
      arrayResult.put("amtoffer", UtilSql.getValue(result, "amtoffer"));
    } catch (SQLException e) {
    }
    return arrayResult;
  }

  private String getDocumentno(String order) {
    JSONObject orderJS = null;
    String result = null;
    try {
      orderJS = new JSONObject(order);
      result = ((String) orderJS.get("documentNo")).replace("\\", "");
    } catch (JSONException e) {

    }
    return result;
  }

  private String getUUID() {
    return SequenceIdData.getUUID();
  }

  private void loadOneOrder(String orderJson) throws Exception {
    OBContext.setAdminMode(true);
    try {
      String orderJsonToLoad = orderJson.replace("$orderId", getUUID());
      getOrderLoader().saveRecord(new JSONObject(orderJsonToLoad));
      OBDal.getInstance().commitAndClose();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  protected OrderLoader getOrderLoader() {
    // override execute hooks as we Weld test environment needs to be updated to support
    // dependency injected hooks
    return new OrderLoader() {
      protected void executeHooks(Instance<? extends Object> hooks, JSONObject jsonorder,
          Order order, ShipmentInOut shipment, Invoice invoice) throws Exception {
      }
    };
  }
}
