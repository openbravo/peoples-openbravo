package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;

public class SE_Payment_BPartner extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;
    String strcBpartnerId = vars.getStringParameter("inpcBpartnerId");
    String strisreceipt = vars.getStringParameter("inpisreceipt");
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnerId);
    boolean isReceipt = "Y".equals(strisreceipt);
    try {
      info.addResult("inpfinPaymentmethodId", isReceipt ? bpartner.getPaymentMethod().getId()
          : bpartner.getPOPaymentMethod().getId());
      info.addResult("inpfinFinancialAccountId", isReceipt ? bpartner.getAccount().getId()
          : bpartner.getPOFinancialAccount().getId());
    } catch (Exception e) {
      log4j.info("No default info for the selected business partner");
    }
  }
}
