package org.openbravo.erpCommon.ad_process;

import java.util.List;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;

public class PaymentMonitorProcess extends DalBaseProcess {

  private ProcessLogger logger;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();

    logger.log("Starting Update Paid Amount for Invoices Backgrouond Process.\n");
    try {
      int counter = 0;
      String whereClause = " as inv where inv.totalpaid <> inv.grandTotalAmount and inv.processed=true";

      final OBQuery<Invoice> obqParameters = OBDal.getInstance().createQuery(Invoice.class,
          whereClause);
      // For Background process execution at system level
      if (OBContext.getOBContext().isInAdministratorMode()) {
        obqParameters.setFilterOnReadableClients(false);
        obqParameters.setFilterOnReadableOrganization(false);
      }
      final List<Invoice> invoices = obqParameters.list();
      for (Invoice invoice : invoices) {
        PaymentMonitor.updateInvoice(invoice);
        counter++;
      }
      logger.log("Invoices updated: " + counter + "\n");
    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }

  }

}