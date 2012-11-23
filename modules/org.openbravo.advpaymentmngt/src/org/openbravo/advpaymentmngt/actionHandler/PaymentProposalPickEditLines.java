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
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentPropDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;

public class PaymentProposalPickEditLines extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(PaymentProposalPickEditLines.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
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
        int cont = createPaymentProposalDetails(jsonRequest);
        jsonRequest = new JSONObject();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "success");
        errorMessage.put("text", OBMessageUtils.messageBD("Success"));
        jsonRequest.put("message", errorMessage);
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", OBMessageUtils.messageBD(e.getMessage()));

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

  private int createPaymentProposalDetails(JSONObject jsonRequest) throws JSONException,
      OBException {

    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return 0;
    }
    final String strPaymentProposalId = jsonRequest.getString("Fin_Payment_Proposal_ID");
    FIN_PaymentProposal paymentProposal = OBDal.getInstance().get(FIN_PaymentProposal.class,
        strPaymentProposalId);
    BigDecimal totalAmount = BigDecimal.ZERO, totalWriteOff = BigDecimal.ZERO;
    int cont = 0;
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
        cont++;
      }
    }

    paymentProposal.setAmount(totalAmount);
    paymentProposal.setWriteoffAmount(totalWriteOff);
    OBDal.getInstance().save(paymentProposal);
    return cont;
  }
}
