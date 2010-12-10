/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Projections;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;

public class FIN_PaymentMonitorProcess extends DalBaseProcess {
  private ProcessLogger logger;

  public void doExecute(ProcessBundle bundle) throws Exception {
    logger = bundle.getLogger();
    // Extra check for PaymentMonitor-disabling switch, to build correct message for users
    try {
      try {
        Preferences.getPreferenceValue("PaymentMonitor", true, null, null, OBContext.getOBContext()
            .getUser(), null, null);

        if (isPreferenceOfModule("PaymentMonitor", "A918E3331C404B889D69AA9BFAFB23AC")) {
          logger
              .log("There is an extension module installed managing the Payment Monitor information.\n");
          logger.log("Starting Update Paid Amount for Invoices Background Process.\n");
        } else {
          logger.log("Payment Monitor active for other module.\n");
          logger.log("Core's background process is executed.\n");
          return;
        }

      } catch (PropertyNotFoundException e) {
        logger.log("Property not found \n");
        return;
      }
    } catch (PropertyException e) {
      logger.log("PropertyException, there is a conflict for PaymentMonitor property\n");
      return;
    }

    try {
      int counter = 0;
      final OBCriteria<Invoice> obc = OBDal.getInstance().createCriteria(Invoice.class);
      obc.add(Expression.eq(Invoice.PROPERTY_PROCESSED, true));
      obc.add(Expression.ne(Invoice.PROPERTY_OUTSTANDINGAMOUNT, BigDecimal.ZERO));
      obc.add(Expression.isNotEmpty(Invoice.PROPERTY_FINPAYMENTSCHEDULELIST));

      // For Background process execution at system level
      if (OBContext.getOBContext().isInAdministratorMode()) {
        obc.setFilterOnReadableClients(false);
        obc.setFilterOnReadableOrganization(false);
      }
      final List<Invoice> invoices = obc.list();
      for (Invoice invoice : invoices) {
        OBDal.getInstance().getSession().lock(Invoice.ENTITY_NAME, invoice, LockMode.NONE);
        updateInvoice(invoice);
        counter++;
        OBDal.getInstance().getSession().flush();
        OBDal.getInstance().getSession().clear();
        if (counter % 50 == 0) {
          logger.log("Invoices updated: " + counter + "\n");
        }
      }
      if (counter % 50 != 0)
        logger.log("Invoices updated: " + counter + "\n");
    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }
  }

  /**
   * Updates the days till due and last calculated on date fields of the invoice.
   * 
   * @param invoice
   * @throws OBException
   */
  public static void updateInvoice(Invoice invoice) throws OBException {
    final OBCriteria<FIN_PaymentSchedule> obc = OBDal.getInstance().createCriteria(
        FIN_PaymentSchedule.class);
    // For Background process execution at system level
    if (OBContext.getOBContext().isInAdministratorMode()) {
      obc.setFilterOnReadableClients(false);
      obc.setFilterOnReadableOrganization(false);
    }

    OBContext.setAdminMode();
    try {
      obc.add(Expression.eq(FIN_PaymentSchedule.PROPERTY_INVOICE, invoice));
      obc.add(Expression.ne(FIN_PaymentSchedule.PROPERTY_OUTSTANDINGAMOUNT, BigDecimal.ZERO));
      obc.setProjection(Projections.min(FIN_PaymentSchedule.PROPERTY_DUEDATE));
      Object o = obc.list().get(0);
      if (o != null) {
        System.err.println("updating invoice ='" + invoice.getId() + "'");
        invoice.setLastCalculatedOnDate(new Date());
        invoice.setDaysTillDue(FIN_Utility.getDaysToDue((Date) o));
      } else {
        System.err.println("select c_invoice_id, documentno from c_invoice where c_invoice_id ='"
            + invoice.getId() + "'");
        invoice.setDaysTillDue(0L);
      }

      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Checks if the module is implementing the specified property.
   * 
   * @param property
   *          Value of the property.
   * @param moduleId
   *          Module identifier.
   * @return true: only if there is one preference for the module or if there are several only one
   *         can be mark as selected. false: in other cases.
   */
  private static boolean isPreferenceOfModule(String property, String moduleId) {

    final OBCriteria<Preference> obcNotSel = OBDal.getInstance().createCriteria(Preference.class);
    obcNotSel.add(Expression.eq(Preference.PROPERTY_PROPERTY, property));
    obcNotSel.add(Expression.eq(Preference.PROPERTY_MODULE, OBDal.getInstance().get(Module.class,
        moduleId)));
    obcNotSel.add(Expression.eq(Preference.PROPERTY_SELECTED, false));
    obcNotSel.setFilterOnReadableClients(false);
    obcNotSel.setFilterOnReadableOrganization(false);

    final OBCriteria<Preference> obcSel = OBDal.getInstance().createCriteria(Preference.class);
    obcSel.add(Expression.eq(Preference.PROPERTY_PROPERTY, property));
    obcSel.add(Expression.eq(Preference.PROPERTY_MODULE, OBDal.getInstance().get(Module.class,
        moduleId)));
    obcSel.add(Expression.eq(Preference.PROPERTY_SELECTED, true));
    obcSel.setFilterOnReadableClients(false);
    obcSel.setFilterOnReadableOrganization(false);

    if (obcNotSel.list() != null && obcNotSel.list().size() > 0) {
      return (obcNotSel.list().size() == 1);
    } else {
      if (obcSel.list() != null && obcSel.list().size() > 0) {
        return (obcSel.list().size() == 1);
      } else {
        return false;
      }
    }
  }
}
