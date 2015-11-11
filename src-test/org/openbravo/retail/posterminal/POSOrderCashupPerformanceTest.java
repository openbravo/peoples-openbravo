/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import javax.enterprise.inject.Instance;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hibernate.Query;
import org.junit.Test;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;

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
public class POSOrderCashupPerformanceTest extends OBBaseRetailTest {

  private final static int ORDER_LOADING_ORDER_CNT = 100;
  private final static int ORDER_CNT = 100;
  private final static int CASHUP_CNT = 10;

  @Test
  public void testOrderLoadingPerformance() throws Exception {

    String cashupId = getUUID();
    String order1Json = IOUtils.toString(this.getClass().getResourceAsStream("order1.json"));
    String order2Json = IOUtils.toString(this.getClass().getResourceAsStream("order2.json"));

    long orderCount = countOrders();

    // warmup
    loadOneOrder(order1Json, cashupId);
    loadOneOrder(order1Json, cashupId);
    loadOneOrder(order2Json, cashupId);
    loadOneOrder(order2Json, cashupId);

    // now start
    long timing = System.currentTimeMillis();
    for (int i = 0; i < ORDER_LOADING_ORDER_CNT; i++) {
      loadOneOrder(order1Json, cashupId);
      loadOneOrder(order2Json, cashupId);
    }

    timing = System.currentTimeMillis() - timing;

    // there should really be more orders...
    MatcherAssert.assertThat(orderCount + 4 + (2 * ORDER_LOADING_ORDER_CNT),
        Matchers.equalTo(countOrders()));

    long avg = timing / ORDER_LOADING_ORDER_CNT;

    // should remain under 750 average
    // locally it takes less than 500millis per order
    MatcherAssert.assertThat(avg, Matchers.lessThan(750l));

    System.err.println("Loading " + (2 * ORDER_LOADING_ORDER_CNT) + " orders took " + timing
        + " millis, is " + avg + " millis per order ");
  }

  @Test
  public void testCashupPerformanceWithYesGroupingYesInvoices() throws Exception {
    testCashupPerformance(true, true);
  }

  @Test
  public void testCashupPerformanceWithNoGroupingYesInvoices() throws Exception {
    testCashupPerformance(false, true);
  }

  @Test
  public void testCashupPerformanceWithNoGroupingNoInvoices() throws Exception {
    testCashupPerformance(false, false);
  }

  @Test
  public void testCashupPerformanceWithYesGroupingNoInvoices() throws Exception {
    testCashupPerformance(true, false);
  }

  private void testCashupPerformance(boolean groupOrders, boolean generateInvoices)
      throws Exception {

    OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
        TEST_POS_TERMINAL_ID);
    boolean oldGroupOrders = posTerminal.getObposTerminaltype().isGroupingOrders();
    boolean oldGenerateInvoices = posTerminal.getObposTerminaltype().isGenerateInvoice();
    posTerminal.getObposTerminaltype().setGenerateInvoice(generateInvoices);
    posTerminal.getObposTerminaltype().setGroupingOrders(groupOrders);
    OBDal.getInstance().save(posTerminal);

    try {
      String order1Json = IOUtils.toString(this.getClass().getResourceAsStream("order1.json"));
      String order2Json = IOUtils.toString(this.getClass().getResourceAsStream("order2.json"));
      String cashupJson = IOUtils.toString(this.getClass().getResourceAsStream("cashup.json"));

      long orderCount = countOrders();
      long cashupCount = countCashups();

      // warmup
      String cashupId = getUUID();
      loadOneOrder(order1Json, cashupId);
      loadOneOrder(order1Json, cashupId);
      loadOneOrder(order2Json, cashupId);
      loadOneOrder(order2Json, cashupId);
      doCashup(cashupJson, cashupId);

      // now start
      long timing = 0;
      for (int c = 0; c < CASHUP_CNT; c++) {
        cashupId = getUUID();
        for (int i = 0; i < ORDER_CNT; i++) {
          loadOneOrder(order1Json, cashupId);
          loadOneOrder(order2Json, cashupId);
        }
        timing += doCashup(cashupJson, cashupId);
      }

      // there should really be more orders...
      long newOrderCount = countOrders();
      MatcherAssert.assertThat((long) (orderCount + 4 + (2 * CASHUP_CNT * ORDER_CNT)),
          Matchers.equalTo(newOrderCount));
      // and more cashups...
      long newCashupCount = countCashups();
      MatcherAssert.assertThat(cashupCount + 1 + CASHUP_CNT, Matchers.equalTo(newCashupCount));

      long avg = timing / CASHUP_CNT;

      // should remain under 750 average
      // MatcherAssert.assertThat(avg, Matchers.lessThan(750l));

      System.err.println("Doing " + CASHUP_CNT + " cashups with each " + (ORDER_CNT * 2)
          + " orders, took " + timing + " millis, is " + avg
          + " millis per cashup, parameters: grouping: " + groupOrders
          + ", generate invoice for order: " + generateInvoices);
    } finally {
      OBPOSApplications posTerminal2 = OBDal.getInstance().get(OBPOSApplications.class,
          TEST_POS_TERMINAL_ID);
      posTerminal2.getObposTerminaltype().setGenerateInvoice(oldGenerateInvoices);
      posTerminal2.getObposTerminaltype().setGroupingOrders(oldGroupOrders);
      OBDal.getInstance().save(posTerminal2);
      OBDal.getInstance().commitAndClose();
    }
  }

  private long countOrders() {
    Query qry = OBDal.getInstance().getSession()
        .createQuery("select count(*) from " + Order.ENTITY_NAME);
    return (Long) qry.uniqueResult();
  }

  private long countCashups() {
    Query qry = OBDal.getInstance().getSession()
        .createQuery("select count(*) from " + OBPOSAppCashup.ENTITY_NAME);
    return (Long) qry.uniqueResult();
  }

  private String getUUID() {
    return SequenceIdData.getUUID();
  }

  private long doCashup(String cashupJson, String cashupId) throws Exception {
    OBContext.setAdminMode(true);
    try {
      long timing = System.currentTimeMillis();
      String cashupToLoad = cashupJson.replace("$cashupId", cashupId);
      getProcessCashClose().saveRecord(new JSONObject(cashupToLoad));
      OBDal.getInstance().commitAndClose();
      return System.currentTimeMillis() - timing;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ProcessCashClose getProcessCashClose() {
    return new ProcessCashClose() {
      protected CashCloseProcessor getCashCloseProcessor() {
        return new CashCloseProcessor() {
          protected String executeHooks(JSONArray messages, OBPOSApplications posTerminal,
              OBPOSAppCashup cashUp, JSONObject jsonCashup) throws Exception {
            return "";
          }
        };
      }
    };
  }

  private void loadOneOrder(String orderJson, String cashupId) throws Exception {
    OBContext.setAdminMode(true);
    try {
      String orderJsonToLoad = orderJson.replace("$orderId", getUUID());
      orderJsonToLoad = orderJsonToLoad.replace("$cashupId", cashupId);
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
