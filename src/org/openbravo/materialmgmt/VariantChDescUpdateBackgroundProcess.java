package org.openbravo.materialmgmt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.GenericJDBCException;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.KillableProcess;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

public class VariantChDescUpdateBackgroundProcess extends DalBaseProcess
    implements KillableProcess {
  private static final Logger log4j = LogManager.getLogger();
  private boolean killProcess = false;
  private static final String ERROR_MSG_TYPE = "Error";

  @Override
  public void kill(ProcessBundle processBundle) throws Exception {
    this.killProcess = true;
  }

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
      if (!killProcess) {
        new VariantChDescUpdateProcess().update(null, null);
        bundle.setResult(msg);
      }
    } catch (GenericJDBCException ge) {
      log4j.error("Exception processing variant generation", ge);
      msg.setType(ERROR_MSG_TYPE);
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), ERROR_MSG_TYPE,
          bundle.getContext().getLanguage()));
      msg.setMessage(ge.getSQLException().getMessage().split("\n")[0]);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    } catch (final Exception e) {
      log4j.error("Exception processing variant generation", e);
      msg.setType(ERROR_MSG_TYPE);
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), ERROR_MSG_TYPE,
          bundle.getContext().getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }
  }

}
