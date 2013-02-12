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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.mobile.core.process.JSONProcessSimple;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonToDataConverter;

public class ProcessSavePayments extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    
    // Get context
    ConnectionProvider conn = new DalConnectionProvider();
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();

    // Get parameters.
    Order order = OBDal.getInstance().get(Order.class, jsonsent.getString("order"));
    Organization org = order.getOrganization();
    BusinessPartner bp = order.getBusinessPartner();
    DocumentType docType = OBDal.getInstance().get(DocumentType.class,
        jsonsent.getString("docType"));
    String documentNo = "xx"; // Utility.getDocumentNoByDocumentType(conn, vars.getClient(),
                              // jsonsent.getString("docType"), true);
    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class, jsonsent.getString("paymentMethod"));
    FIN_FinancialAccount account = OBDal.getInstance().get(FIN_FinancialAccount.class,
        jsonsent.getString("account"));
    String amount = Double.toString(jsonsent.getDouble("amount"));
    Date paymentDate = (Date) JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.DATE,
        jsonsent.getString("paymentDate"));

    // Payment Scheduled
    OBCriteria<FIN_PaymentSchedule> crPaymentSched = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    crPaymentSched.add(Restrictions.eq("order", order));
    List<FIN_PaymentSchedule> paymentScheds = crPaymentSched.list();
    if (paymentScheds.size() != 1) {
      throw new ServletException("Not found payment scheduled for order.");
    }
    FIN_PaymentSchedule paymentSched = paymentScheds.get(0);
    
    // Payments Scheduled Details
    OBCriteria<FIN_PaymentScheduleDetail> crProcess = OBDal.getInstance().createCriteria(
        FIN_PaymentScheduleDetail.class);
    crProcess.add(Restrictions.eq("orderPaymentSchedule", paymentSched));
    crProcess.add(Restrictions.isNull("paymentDetails"));

    List<FIN_PaymentScheduleDetail> paymentSchedDetails = crProcess.list();
    if (paymentSchedDetails.size() != 1) {
      throw new ServletException("Not found detail for payment scheduled.");
    }

    HashMap<String, BigDecimal> paymentAmount = new HashMap<String, BigDecimal>();
    paymentAmount.put(paymentSchedDetails.get(0).getId(), new BigDecimal(amount));

    // Save Payment
    FIN_Payment payment = FIN_AddPayment.savePayment(null, true, docType, documentNo, bp,
        paymentMethod, account, amount, paymentDate, org, null, paymentSchedDetails, paymentAmount,
        false, false);
    
    // Process Payment
    try {
      FIN_AddPayment.processPayment(vars, conn, "D", payment);
    } catch (Exception e) {
      throw new ServletException(e);
    }
    
    // Return processed payment.
    List<Object> data = new ArrayList<Object>();
    data.add(payment);

    // return JSONRowConverter.buildResponse(data, null);
    return null;
  }
}
