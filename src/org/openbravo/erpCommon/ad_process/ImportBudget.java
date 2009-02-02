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
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2001-2008 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_process;

import java.sql.Connection;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;

public class ImportBudget extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportBudget.class);

  private String m_AD_Process_ID = "";
  private String m_Record_ID = "";
  private String m_Budget_ID = "";
  private boolean m_deleteOldImported;

  public ImportBudget(ConnectionProvider conn, String AD_Process_ID, String recordId,
      String strBudget, boolean deleteOld) {
    super(conn);
    m_AD_Process_ID = AD_Process_ID;
    m_Record_ID = recordId;
    m_deleteOldImported = deleteOld;
    m_Budget_ID = strBudget;
  }

  protected String getAD_Process_ID() {
    return m_AD_Process_ID;
  }

  protected String getRecord_ID() {
    return m_Record_ID;
  }

  protected void createInstanceParams(VariablesSecureApp vars) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Creating parameters");
    addParameterString("DeleteOldImported", "20", m_deleteOldImported ? "Y" : "N");
    addParameterNumber("C_Budget_ID", "10", m_Budget_ID);
  }

  protected OBError doIt(VariablesSecureApp vars) throws ServletException {
    int no = 0;
    OBError myError = new OBError();
    ConnectionProvider conn = null;
    Connection con = null;
    try {
      conn = getConnection();
      con = conn.getTransactionConnection();
      if (m_deleteOldImported) {
        no = ImportBudgetData.deleteOld(con, conn, getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("Delete Old Imported = " + no);
      }
      // Set Client, Org, IaActive, Created/Updated, ProductType
      no = ImportBudgetData.updateRecords(con, conn, getAD_Client_ID(), m_Budget_ID);
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Reset = " + no);

      // BPartner
      no = ImportBudgetData.updateBPartnerId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget BPartnerId = " + no);
      no = ImportBudgetData.updatePartnerError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Partner = " + no);

      // Set BPGroup
      no = ImportBudgetData.updateBPGroupId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget BPGroupId = " + no);
      no = ImportBudgetData.updateGroupError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid BPartner group = " + no);

      // Product
      no = ImportBudgetData.updateProductId(con, conn, vars.getLanguage(), getAD_Client_ID());
      ImportBudgetData.updateUomId(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget ProductId = " + no);
      no = ImportBudgetData.updateProductError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Product = " + no);

      // Product Category
      no = ImportBudgetData.updateProdCatId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Product Category = " + no);
      no = ImportBudgetData.updateProdCatError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Product Category= " + no);

      // Trx Organization
      no = ImportBudgetData.updateTrxOrgId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Trx Organization = " + no);
      no = ImportBudgetData.updateTrxOrgError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Trx Organization = " + no);

      // Sales Region
      no = ImportBudgetData.updateSalesRegionId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Sales Region = " + no);
      no = ImportBudgetData.updateSalesRegionError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Sales Region = " + no);

      // Project
      no = ImportBudgetData.updateProjectId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Project = " + no);
      no = ImportBudgetData.updateProjectError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Project = " + no);

      // Campaign
      no = ImportBudgetData.updateCampaignId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Campaign = " + no);
      no = ImportBudgetData.updateCampaignError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Campaign = " + no);

      // Activity
      no = ImportBudgetData.updateActivityId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Activity = " + no);
      no = ImportBudgetData.updateActivityError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Activity = " + no);

      // Account ID
      no = ImportBudgetData.updateAccountId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget AccountID = " + no);
      no = ImportBudgetData.updateAccountIdError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid AccountID = " + no);

      // Account Schema
      no = ImportBudgetData.updateAcctSchemaId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Account Schema = " + no);
      no = ImportBudgetData.updateAcctSchemaError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Account Schema = " + no);

      // Period
      no = ImportBudgetData.updatePeriodId(con, conn, m_Budget_ID, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Period = " + no);
      no = ImportBudgetData.updatePeriodError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Period = " + no);

      // Currency
      no = ImportBudgetData.updateCurrencyId(con, conn, vars.getLanguage(), getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget Currency = " + no);
      no = ImportBudgetData.updateCurrencyError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Currency = " + no);

      // Budget Line ¿Exist?
      no = ImportBudgetData.updateBudgetLineId(con, conn, m_Budget_ID, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportBudget BudgetLine = " + no);

      conn.releaseCommitConnection(con);
    } catch (Exception se) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      se.printStackTrace();
      addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      return myError;
    }

    // till here, the edition of the I_BUDGETLINE table
    // now, the insertion from I_BUDGETLINE table in C_BPartner...

    int noInsert = 0;
    int noUpdate = 0;
    int seqNo = 0;
    int noBudgetError = 0;

    try {
      // Go through Records
      ImportBudgetData[] data = ImportBudgetData.select(conn, getAD_Client_ID());
      seqNo = Integer.parseInt(ImportBudgetData.selectSeqNo(conn, m_Budget_ID));
      if (log4j.isDebugEnabled())
        log4j.debug("Going through " + data.length + " records");
      for (int i = 0; i < data.length; i++) {
        String I_BudgetLine_ID = data[i].iBudgetlineId;
        String C_BudgetLine_ID = data[i].cBudgetlineId;
        boolean newBudgetLine = (C_BudgetLine_ID == null || C_BudgetLine_ID.equals(""));

        con = conn.getTransactionConnection();

        // create/update Budget
        if (newBudgetLine) { // Insert new BudgetLine
          C_BudgetLine_ID = SequenceIdData.getUUID();
          try {
            no = ImportBudgetData.insertBudgetLine(con, conn, C_BudgetLine_ID, Integer
                .toString(seqNo), I_BudgetLine_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Insert BudgetLine = " + no);
            noInsert++;
            seqNo = seqNo + 10;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Insert BudgetLine - " + ex.toString());
            conn.releaseRollbackConnection(con);
            no = ImportBudgetData.insertBudgetLineError(conn, ex.toString(), I_BudgetLine_ID);
            continue;
          }
        } else { // Update existing BudgetLine
          try {
            no = ImportBudgetData.updateBudgetLine(con, conn, I_BudgetLine_ID, C_BudgetLine_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Update BudgetLine = " + no);
            noUpdate++;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Update BudgetLine - " + ex.toString());
            conn.releaseRollbackConnection(con);
            no = ImportBudgetData.updateBudgetLineError(conn, ex.toString(), I_BudgetLine_ID);
            continue;
          }
        }

        // Update I_BudgetLine
        try {
          no = ImportBudgetData.setImported(con, conn, C_BudgetLine_ID, I_BudgetLine_ID);
          conn.releaseCommitConnection(con);
        } catch (ServletException ex) {
          if (log4j.isDebugEnabled())
            log4j.debug("Update Imported - " + ex.toString());
          noInsert--;
          conn.releaseRollbackConnection(con);
          no = ImportBudgetData.updateSetImportedError(conn, ex.toString(), I_BudgetLine_ID);
          continue;
        }
      }

      // Set Error to indicator to not imported
      noBudgetError = ImportBudgetData.updateNotImported(conn, getAD_Client_ID());
    } catch (Exception se) {
      se.printStackTrace();
      addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      return myError;
    }
    addLog(Utility.messageBD(conn, "Budgets not imported", vars.getLanguage()) + ": "
        + noBudgetError + "; ");
    addLog("BudgetLine inserted: " + noInsert + "; ");
    addLog("BudgetLine updated: " + noUpdate);

    if (noBudgetError == 0) {
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(conn, "Success", vars.getLanguage()));
    } else if (noInsert > 0 || noUpdate > 0) {
      myError.setType("Warning");
      myError.setTitle(Utility.messageBD(conn, "Some budgets could not be imported", vars
          .getLanguage()));
    } else {
      myError.setType("Error");
      myError
          .setTitle(Utility.messageBD(conn, " No budgets could be imported", vars.getLanguage()));
    }
    myError.setMessage(Utility.messageBD(conn, getLog(), vars.getLanguage()));
    return myError;

  } // doIt
}
