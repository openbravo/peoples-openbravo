package org.openbravo.erpCommon.ad_actionButton;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;

public class RMCreateInvoice implements org.openbravo.scheduling.Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    final String language = bundle.getContext().getLanguage();
    final ConnectionProvider conProvider = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();

    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", language));

    final String strOrderId = (String) bundle.getParams().get("C_Order_ID");

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "119");
    } finally {
      OBContext.restorePreviousMode();
    }
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("DateInvoiced", new Date());
    parameters.put("AD_Org_ID", null);
    parameters.put("C_Order_ID", strOrderId);
    parameters.put("C_BPartner_ID", null);
    parameters.put("InvoiceToDate", null);

    final ProcessInstance pinstance = CallProcess.getInstance().callProcess(process, strOrderId,
        parameters);

    if (pinstance.getResult() == 0L) {
      // Error
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(conProvider, "Error", language));
    }

    msg.setMessage(Utility.parseTranslation(conProvider, vars, language, pinstance.getErrorMsg()));
    bundle.setResult(msg);

  }
}
