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
package org.openbravo.erpCommon.ad_actionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.Vector;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.materialmgmt.transaction.ProductionPlan;
import org.openbravo.model.materialmgmt.transaction.ProductionTransaction;
import org.openbravo.scheduling.ProcessBundle;

public class ValidateWorkEffort_ProductionRun implements org.openbravo.scheduling.Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    try {

      final String strMProductionPlanID = (String) bundle.getParams().get("M_ProductionPlan_ID");
      final ConnectionProvider conn = bundle.getConnection();
      ProductionPlan productionPlan = OBDal.getInstance().get(ProductionPlan.class,
          strMProductionPlanID);
      ProductionTransaction production = productionPlan.getProduction();

      if (production.getMaterialMgmtProductionPlanList().size() > 1)
        throw new OBException(Utility.messageBD(conn, "MoreThanOneProductionPlanError", bundle
            .getContext().getLanguage()));

      if (productionPlan.getProductionplandate() != null) {
        production.setMovementDate(productionPlan.getProductionplandate());
        OBDal.getInstance().save(production);
        OBDal.getInstance().flush();
      }

      validateWorkEffort(production, conn, bundle.getContext().toVars());
      OBDal.getInstance().save(production);
      OBDal.getInstance().flush();

      final OBError msg = new OBError();

      msg.setType("Success");
      msg.setTitle(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      msg.setMessage(Utility.messageBD(conn, "Success", bundle.getContext().getLanguage()));
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();
      e.printStackTrace(System.err);
      final OBError msg = new OBError();
      msg.setType("Error");
      if (e instanceof org.hibernate.exception.GenericJDBCException) {
        msg.setMessage(((org.hibernate.exception.GenericJDBCException) e).getSQLException()
            .getNextException().getMessage());
      } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
        msg.setMessage(((org.hibernate.exception.ConstraintViolationException) e).getSQLException()
            .getNextException().getMessage());
      } else {
        msg.setMessage(e.getMessage());
      }
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }

  private void validateWorkEffort(ProductionTransaction production, ConnectionProvider conn,
      VariablesSecureApp vars) throws Exception {
    try {
      OBContext.setAdminMode();

      org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, "800106");

      final ProcessInstance pInstance = OBProvider.getInstance().get(ProcessInstance.class);
      pInstance.setProcess(process);
      pInstance.setActive(true);
      pInstance.setRecordID(production.getId());
      pInstance.setUserContact(OBContext.getOBContext().getUser());

      OBDal.getInstance().save(pInstance);
      OBDal.getInstance().flush();

      try {
        final Connection connection = OBDal.getInstance().getConnection();
        PreparedStatement ps = null;
        final Properties obProps = OBPropertiesProvider.getInstance().getOpenbravoProperties();
        if (obProps.getProperty("bbdd.rdbms") != null
            && obProps.getProperty("bbdd.rdbms").equals("POSTGRE")) {
          ps = connection.prepareStatement("SELECT * FROM ma_workeffort_validate(?)");
        } else {
          ps = connection.prepareStatement("CALL ma_workeffort_validate(?)");
        }
        ps.setString(1, pInstance.getId());
        ps.execute();

      } catch (Exception e) {
        throw new IllegalStateException(e);
      }

      OBDal.getInstance().getSession().refresh(pInstance);

      if (pInstance.getResult() == 0) {
        // Error Processing
        OBError myMessage = Utility.getProcessInstanceMessage(conn, vars,
            getPInstanceData(pInstance));
        throw new OBException("ERROR: " + myMessage.getMessage());
      }
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private PInstanceProcessData[] getPInstanceData(ProcessInstance pInstance) throws Exception {
    Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);
    PInstanceProcessData objectPInstanceProcessData = new PInstanceProcessData();
    objectPInstanceProcessData.result = pInstance.getResult().toString();
    objectPInstanceProcessData.errormsg = pInstance.getErrorMsg();
    objectPInstanceProcessData.pMsg = "";
    vector.addElement(objectPInstanceProcessData);
    PInstanceProcessData pinstanceData[] = new PInstanceProcessData[1];
    vector.copyInto(pinstanceData);
    return pinstanceData;
  }
}
