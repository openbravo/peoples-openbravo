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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonToDataConverter;

public class ProcessOrder implements JSONProcess {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    Order order = OBProvider.getInstance().get(Order.class);

    JSONObject jsonorder = jsonsent.getJSONObject("order");

    order.setClient(OBDal.getInstance().get(Client.class, jsonorder.getString("client")));
    order.setOrganization(OBDal.getInstance().get(Organization.class,
        jsonorder.getString("organization")));
    order.setActive(true);

    order.setDocumentNo(jsonorder.getString("documentNo"));
    order.setDocumentType(OBDal.getInstance().get(DocumentType.class,
        jsonorder.getString("documentType")));
    order.setTransactionDocument(OBDal.getInstance().get(DocumentType.class,
        jsonorder.getString("documentType")));
    order.setOrderDate((Date) JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.DATE,
        jsonorder.getString("orderDate")));
    order.setAccountingDate((Date) JsonToDataConverter.convertJsonToPropertyValue(
        PropertyByType.DATE, jsonorder.getString("orderDate")));

    // hard-coded business partner
    order.setBusinessPartner(OBDal.getInstance().get(BusinessPartner.class,
        jsonorder.getJSONObject("bp").getString("id")));
    order.setPartnerAddress(OBDal.getInstance().get(Location.class,
        jsonorder.getJSONObject("bploc").getString("id")));
    order.setInvoiceAddress(OBDal.getInstance().get(Location.class,
        jsonorder.getJSONObject("bploc").getString("id")));
    order.setPaymentMethod(OBDal.getInstance().get(FIN_PaymentMethod.class,
        jsonorder.getJSONObject("bp").getString("paymentMethod")));
    order.setPaymentTerms(OBDal.getInstance().get(PaymentTerm.class,
        jsonorder.getJSONObject("bp").getString("paymentTerms")));
    order.setInvoiceTerms(jsonorder.getJSONObject("bp").getString("invoiceTerms"));

    order.setPriceList(OBDal.getInstance().get(PriceList.class, jsonorder.getString("priceList")));
    order.setCurrency(OBDal.getInstance().get(Currency.class, jsonorder.getString("currency")));
    order.setScheduledDeliveryDate((Date) JsonToDataConverter.convertJsonToPropertyValue(
        PropertyByType.DATE, jsonorder.getString("orderDate")));
    order.setWarehouse(OBDal.getInstance().get(Warehouse.class, jsonorder.getString("warehouse")));
    order.setSalesRepresentative(OBDal.getInstance().get(User.class,
        jsonorder.getString("salesRepresentative")));

    order.setSalesTransaction(true);
    order.setDocumentStatus("DR");
    order.setDocumentAction("CO");
    order.setProcessNow(false);
    
    JSONArray orderlines = jsonorder.getJSONArray("lines");
    for (int i = 0; i < orderlines.length(); i++) {
      OrderLine orderline = OBProvider.getInstance().get(OrderLine.class);
      orderline.setClient(OBDal.getInstance().get(Client.class, jsonorder.getString("client")));
      orderline.setOrganization(OBDal.getInstance().get(Organization.class,
          jsonorder.getString("organization")));
      orderline.setActive(true);
      orderline.setSalesOrder(order);
      
      orderline.setOrderDate((Date) JsonToDataConverter.convertJsonToPropertyValue(
          PropertyByType.DATE, jsonorder.getString("orderDate")));
      orderline.setWarehouse(OBDal.getInstance().get(Warehouse.class,
          jsonorder.getString("warehouse")));
      orderline.setBusinessPartner(OBDal.getInstance().get(BusinessPartner.class,
          jsonorder.getJSONObject("bp").getString("id")));
      orderline.setPartnerAddress(OBDal.getInstance().get(Location.class,
          jsonorder.getJSONObject("bploc").getString("id")));
      orderline.setCurrency(OBDal.getInstance()
          .get(Currency.class, jsonorder.getString("currency")));
      
      orderline.setLineNo((long) ((i + 1) * 10));
      orderline.setProduct(OBDal.getInstance().get(Product.class,
          orderlines.getJSONObject(i).getJSONObject("product").getJSONObject("product")
              .getString("id")));
      orderline.setUOM(OBDal.getInstance().get(UOM.class,
          orderlines.getJSONObject(i).getString("uOM")));
      orderline.setUnitPrice(new BigDecimal(orderlines.getJSONObject(i).getString("price")));    
      orderline.setOrderedQuantity(new BigDecimal(orderlines.getJSONObject(i).getString("qty")));
      orderline.setTax(OBDal.getInstance().get(TaxRate.class, "8142DAB7ACED4F23B5D1B4EF70AE1AE8")); // Exempt
      
      order.getOrderLineList().add(orderline);
    }

    OBDal.getInstance().save(order);
    OBDal.getInstance().flush();

    final JSONObject jsonResponse = bookOrder("C_Order Post", order.getId());

    OBDal.getInstance().commitAndClose();

    // final JSONObject jsonResponse = new JSONObject();
    //
    // jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    // jsonResponse.put("result", "0");
    // jsonResponse.put("data", jsonorder);

    return jsonResponse;
  }

  private JSONObject bookOrder(String processkey, String recordid) throws JSONException {

    ConnectionProvider dalconn = new DalConnectionProvider();
    OBContext cnx = OBContext.getOBContext();
    String user = cnx.getUser().getId();
    String client = cnx.getCurrentClient().getId();
    String organization = cnx.getCurrentOrganization().getId();
    String role = cnx.getRole().getId();
    VariablesSecureApp vars = new VariablesSecureApp(user, client, organization, role);

    OBCriteria<org.openbravo.model.ad.ui.Process> crProcess = OBDal.getInstance().createCriteria(
        org.openbravo.model.ad.ui.Process.class);
    crProcess.add(Restrictions.eq("searchKey", processkey));
    crProcess.setFilterOnReadableClients(true);
    crProcess.setFilterOnReadableOrganization(true);
    crProcess.setFilterOnActive(true);
    List<org.openbravo.model.ad.ui.Process> processes = crProcess.list();
    if (processes.size() != 1) {
      throw new JSONException(MessageFormat.format("Process \"{0}\" does not exist.", processkey));
    }
    org.openbravo.model.ad.ui.Process process = processes.get(0);

    // Calls the process
    final ProcessInstance pinstance = CallProcess.getInstance().callProcess(process, recordid,
        new HashMap<String, Object>());

    final JSONObject jsonResponse = new JSONObject();

    String message = pinstance.getErrorMsg();
    int i = message.indexOf("@ERROR=");
    if (i >= 0) {
      throw new JSONException(message.substring(i + 7));
    } else {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put("result", pinstance.getResult());
      jsonResponse.put("record", pinstance.getRecordID());
      jsonResponse.put("message",
          Utility.parseTranslation(dalconn, vars, vars.getLanguage(), message));
    }

    return jsonResponse;
  }

}
