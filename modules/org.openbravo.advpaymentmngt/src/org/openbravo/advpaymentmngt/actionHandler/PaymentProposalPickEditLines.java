/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2012 Openbravo S.L.U.
 ******************************************************************************
 */

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.service.db.DalConnectionProvider;

public class PaymentProposalPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(PaymentProposalPickEditLines.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      // When the focus is NOT in the tab of the button (i.e. any child tab) and the tab does not
      // contain any record, the inppaymentproposal parameter contains "null" string. Use
      // Fin_Payment_Proposal_ID
      // instead because it always contains the id of the selected order.
      // Issue 20585: https://issues.openbravo.com/view.php?id=20585
      final String strPaymentProposalId = jsonRequest.getString("Fin_Payment_Proposal_ID");
      FIN_PaymentProposal paymentProposal = OBDal.getInstance().get(FIN_PaymentProposal.class,
          strPaymentProposalId);

      if (cleanPaymentProposalDetails(paymentProposal)) {
        createPaymentProposalDetails(jsonRequest);
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text",
            Utility.messageBD(new DalConnectionProvider(), e.getMessage(), vars.getLanguage()));

        jsonRequest.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private boolean cleanPaymentProposalDetails(FIN_PaymentProposal paymentProposal) {
    if (paymentProposal == null) {
      return false;
    } else if (paymentProposal.getFINPaymentPropDetailList().isEmpty()) {
      // nothing to delete.
      return true;
    }
    try {
      paymentProposal.getFINPaymentPropDetailList().clear();
      paymentProposal.setAmount(BigDecimal.ZERO);
      paymentProposal.setWriteoffAmount(BigDecimal.ZERO);
      OBDal.getInstance().save(paymentProposal);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return false;
    }
    return true;
  }

  private void createPaymentProposalDetails(JSONObject jsonRequest) throws JSONException,
      OBException {

    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return;
    }
    final String strPaymentProposalId = jsonRequest.getString("Fin_Payment_Proposal_ID");
    FIN_PaymentProposal paymentProposal = OBDal.getInstance().get(FIN_PaymentProposal.class,
        strPaymentProposalId);
    BigDecimal totalAmount = BigDecimal.ZERO, totalWriteOff = BigDecimal.ZERO;
    for (int i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      BigDecimal paidAmount = new BigDecimal(selectedLine.getString("payment"));

      if (paidAmount.compareTo(BigDecimal.ZERO) != 0) {
        FIN_PaymentPropDetail newPPD = OBProvider.getInstance().get(FIN_PaymentPropDetail.class);
        newPPD.setOrganization(paymentProposal.getOrganization());
        newPPD.setClient(paymentProposal.getClient());
        newPPD.setCreatedBy(paymentProposal.getCreatedBy());
        newPPD.setUpdatedBy(paymentProposal.getUpdatedBy());
        newPPD.setFinPaymentProposal(paymentProposal);
        newPPD.setFINPaymentScheduledetail(OBDal.getInstance().get(FIN_PaymentScheduleDetail.class,
            selectedLine.getString("paymentScheduleDetail")));
        BigDecimal difference = new BigDecimal(selectedLine.getString("difference"));
        boolean writeOff = selectedLine.getString("writeoff").equals("true");
        newPPD.setAmount(paidAmount);
        totalAmount = totalAmount.add(paidAmount);
        if (difference.compareTo(BigDecimal.ZERO) != 0 && writeOff) {
          newPPD.setWriteoffAmount(difference);
          totalWriteOff = totalWriteOff.add(difference);
        }

        OBDal.getInstance().save(newPPD);
        OBDal.getInstance().save(paymentProposal);
        OBDal.getInstance().flush();
      }
    }

    paymentProposal.setAmount(totalAmount);
    paymentProposal.setWriteoffAmount(totalWriteOff);
    OBDal.getInstance().save(paymentProposal);
  }
}
