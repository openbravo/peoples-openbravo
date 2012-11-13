package org.openbravo.retail.posterminal;

import java.math.BigDecimal;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;

public class PaymentProcessorEmpty implements PaymentProcessor {

  public void process(JSONObject payment, Order order, Invoice invoice, BigDecimal writeoff) {
    // Do nothing
  }
}
