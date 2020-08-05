/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.authentication.AuthenticationManager;
import org.openbravo.base.exception.OBException;

@AuthenticationManager.Stateless
public class ExportOrder extends PaidReceipts {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = super.exec(jsonsent);
    return transform(result);
  }

  private JSONObject transform(JSONObject result) {
    try {
      JSONObject transformedResult = result;
      if (transformedResult.has("data")) {
        transformOrders(transformedResult.getJSONArray("data"));
      }
      return transformedResult;
    } catch (Exception e) {
      throw new OBException("Error transforming order for exporting", e);
    }
  }

  private void transformOrders(JSONArray data) throws JSONException {
    for (int i = 0; i < data.length(); i++) {
      JSONObject order = (JSONObject) data.get(i);
      renameStringPropertyInObject(order, "orderid", "id");
      transformLines(order);
      transformPayments(order);
      transformOrderTaxes(order);
    }
  }

  private void transformLines(JSONObject order) throws JSONException {
    if (order.has("receiptLines")) {
      renameArrayPropertyInObject(order, "receiptLines", "lines");
      JSONArray orderLines = order.getJSONArray("lines");
      for (int i = 0; i < orderLines.length(); i++) {
        JSONObject line = (JSONObject) orderLines.get(i);
        renameStringPropertyInObject(line, "id", "productId");
        renameStringPropertyInObject(line, "name", "product");
        renameStringPropertyInObject(line, "lineId", "id");
        renameNumericPropertyInObject(line, "quantity", "qty");
        renameNumericPropertyInObject(line, "lineGrossAmount", "grossAmount");
        renameNumericPropertyInObject(line, "linenetamount", "netAmount");
        transformOrderLineTaxes(line);
        transformOrderLinePromotions(line);
      }
    }
  }

  private void transformPayments(JSONObject order) throws JSONException {
    if (order.has("receiptPayments")) {
      renameArrayPropertyInObject(order, "receiptPayments", "payments");
      JSONArray orderPayments = order.getJSONArray("payments");
      for (int i = 0; i < orderPayments.length(); i++) {
        JSONObject payment = (JSONObject) orderPayments.get(i);
        renameNumericPropertyInObject(payment, "paymentAmount", "paidAmount");
      }
    }
  }

  private void transformOrderTaxes(JSONObject order) throws JSONException {
    if (order.has("receiptTaxes")) {
      JSONArray orderTaxes = order.getJSONArray("receiptTaxes");
      JSONObject outputOrderTaxes = new JSONObject();
      for (int i = 0; i < orderTaxes.length(); i++) {
        JSONObject tax = (JSONObject) orderTaxes.get(i);
        renameStringPropertyInObject(tax, "taxid", "id");
        renameNumericPropertyInObject(tax, "taxRate", "rate");
        addTaxAsProperty(outputOrderTaxes, tax);
      }
      order.remove("receiptTaxes");
      order.put("taxes", outputOrderTaxes);
    }
  }

  private void addTaxAsProperty(JSONObject outputOrderTaxes, JSONObject tax) throws JSONException {
    String taxName = tax.has("name") ? tax.getString("name") : tax.getString("identifier");
    tax.remove(tax.has("name") ? "name" : "identifier");
    outputOrderTaxes.put(taxName, tax);
  }

  private void transformOrderLineTaxes(JSONObject line) throws JSONException {
    if (line.has("taxes")) {
      JSONArray taxLines = line.getJSONArray("taxes");
      JSONObject outputOrderLineTaxes = new JSONObject();
      for (int i = 0; i < taxLines.length(); i++) {
        JSONObject lineTax = (JSONObject) taxLines.get(i);
        renameStringPropertyInObject(lineTax, "taxId", "id");
        renameNumericPropertyInObject(lineTax, "taxRate", "rate");
        addTaxAsProperty(outputOrderLineTaxes, lineTax);
      }
      line.remove("taxes");
      line.put("taxLines", outputOrderLineTaxes);
    }
  }

  private void transformOrderLinePromotions(JSONObject line) throws JSONException {
    if (line.has("promotions")) {
      JSONArray linePromotions = line.getJSONArray("promotions");
      for (int i = 0; i < linePromotions.length(); i++) {
        JSONObject linePromotion = (JSONObject) linePromotions.get(i);
        renameStringPropertyInObject(linePromotion, "ruleId", "discountRule");
        renameNumericPropertyInObject(linePromotion, "amt", "amount");
        renameNumericPropertyInObject(linePromotion, "qtyOffer", "quantity");
      }
    }
  }

  private void renameArrayPropertyInObject(JSONObject object, String propertyNameToReplace,
      String newPropertyName) throws JSONException {
    if (object.has(propertyNameToReplace)) {
      object.put(newPropertyName, object.getJSONArray(propertyNameToReplace));
      object.remove(propertyNameToReplace);
    }
  }

  private void renameStringPropertyInObject(JSONObject object, String propertyNameToReplace,
      String newPropertyName) throws JSONException {
    if (object.has(propertyNameToReplace)) {
      object.put(newPropertyName, object.getString(propertyNameToReplace));
      object.remove(propertyNameToReplace);
    }
  }

  private void renameNumericPropertyInObject(JSONObject object, String propertyNameToReplace,
      String newPropertyName) throws JSONException {
    if (object.has(propertyNameToReplace)) {
      object.put(newPropertyName, object.getDouble(propertyNameToReplace));
      object.remove(propertyNameToReplace);
    }
  }

}
