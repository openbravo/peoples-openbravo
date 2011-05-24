package org.openbravo.advpaymentmngt.process;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatement;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.scheduling.ProcessBundle;

public class FIN_BankStatementProcess implements org.openbravo.scheduling.Process {
  private static AdvPaymentMngtDao dao;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    dao = new AdvPaymentMngtDao();
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(bundle.getConnection(), "Success", bundle.getContext()
        .getLanguage()));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("FIN_Bankstatement_ID");
      final FIN_BankStatement bankStatement = dao.getObject(FIN_BankStatement.class, recordID);
      final VariablesSecureApp vars = bundle.getContext().toVars();
      final ConnectionProvider conProvider = bundle.getConnection();

      bankStatement.setProcessNow(true);
      OBDal.getInstance().save(bankStatement);
      OBDal.getInstance().flush();

      if ("P".equals(strAction)) {
        // ***********************
        // Process Bank Statement
        // ***********************
        bankStatement.setProcessed(true);
        bankStatement.setAPRMProcessBankStatement("R");
        OBDal.getInstance().save(bankStatement);
        OBDal.getInstance().flush();
      } else if (strAction.equals("R")) {
        // *************************
        // Reactivate Bank Statement
        // *************************
        // Already Posted Document
        if ("Y".equals(bankStatement.getPosted())) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
              "@PostedDocument@" + ": " + bankStatement.getIdentifier()));
          bundle.setResult(msg);
          return;
        }
        // Already Reconciled
        for (FIN_BankStatementLine bsl : bankStatement.getFINBankStatementLineList()) {
          if (bsl.getFinancialAccountTransaction() != null) {
            msg.setType("Error");
            msg.setTitle(Utility.messageBD(conProvider, "Error", vars.getLanguage()));
            msg.setMessage(Utility.parseTranslation(conProvider, vars, vars.getLanguage(),
                "@APRM_BSLineReconciled@" + ": " + bsl.getLineNo().toString()));
            bundle.setResult(msg);
            return;
          }
        }

        bankStatement.setProcessed(false);
        bankStatement.setAPRMProcessBankStatement("P");
        OBDal.getInstance().save(bankStatement);
        OBDal.getInstance().flush();
      }

      bankStatement.setProcessNow(false);
      OBDal.getInstance().save(bankStatement);
      OBDal.getInstance().flush();
      bundle.setResult(msg);

    } catch (Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }

  }
}
