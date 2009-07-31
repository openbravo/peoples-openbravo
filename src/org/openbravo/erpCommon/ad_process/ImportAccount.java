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
 * Contributor(s): Openbravo SL
 * Contributions are Copyright (C) 2001-2009 Openbravo S.L.
 ******************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.exception.NoConnectionAvailableException;

public class ImportAccount extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportAccount.class);

  private String m_AD_Process_ID = "";
  private String m_C_Element_ID = "";
  private boolean m_deleteOldImported;
  private boolean m_updateDefaultAccounts;
  private boolean m_createNewCombination;
  private static final int UPDATE_ERROR = 0;
  private static final int UPDATE_YES = 1;
  private static final int UPDATE_SAME = 2;

  public ImportAccount(ConnectionProvider conn, String AD_Process_ID, boolean deleteOld,
      String C_Element_ID, boolean updateDefaultAccounts, boolean createNewCombination) {
    super(conn);
    m_AD_Process_ID = AD_Process_ID;
    m_deleteOldImported = deleteOld;
    m_C_Element_ID = C_Element_ID;
    m_updateDefaultAccounts = updateDefaultAccounts;
    m_createNewCombination = createNewCombination;
  }

  protected String getAD_Process_ID() {
    return m_AD_Process_ID;
  }

  protected String getRecord_ID() {
    return "0";
  }

  protected void createInstanceParams(VariablesSecureApp vars) throws ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Creating parameters");
  }

  protected OBError doIt(VariablesSecureApp vars) throws ServletException {
    int no = 0;
    ConnectionProvider conn = null;
    Connection con = null;
    OBError myError = new OBError();

    try {
      conn = getConnection();
      con = conn.getTransactionConnection();
      if (m_deleteOldImported) {
        no = ImportAccountData.deleteOld(con, conn, getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("Delete Old Imported = " + no);
      }
      // Set Client, Org, IaActive, Created/Updated
      no = ImportAccountData.updateRecords(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount Reset = " + no);

      // Set element
      if (!m_C_Element_ID.equals("0")) {
        no = ImportAccountData.updateElement(con, conn, m_C_Element_ID, getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("ImportAccount Element = " + no);
      }
      no = ImportAccountData.updateIdByName(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount ids by name = " + no);
      no = ImportAccountData.updateElementError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount element errors = " + no);

      // Set column
      no = ImportAccountData.updateColumn(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated columns = " + no);
      no = ImportAccountData.updateColumnError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount column errors = " + no);

      // Set default values for PostActual, PostBudget, PostStatistical,
      // PostEncumbrance, IsSummary, IsDocControlled
      String[] yColumns = new String[] { "PostActual", "PostBudget", "PostStatistical",
          "PostEncumbrance" };
      for (int i = 0; i < yColumns.length; i++) {
        no = ImportAccountData.updateYColumns(con, conn, yColumns[i], getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("ImportAccount " + yColumns[i] + " errors = " + no);
      }
      no = ImportAccountData.updateSummary(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated summary = " + no);
      no = ImportAccountData.updateDocControlled(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated docControlled = " + no);

      // Check Account Type A (E) L M O R
      no = ImportAccountData.updateAccountType(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated AccountType = " + no);
      no = ImportAccountData.updateAccountTypeError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount AccountType errors = " + no);

      // Check Account Sign (N) C B
      no = ImportAccountData.updateAccountSign(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated AccountSign = " + no);
      no = ImportAccountData.updateAccountSignError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount AccountSign errors = " + no);

      // Update ElementValue from existing
      no = ImportAccountData.updateCElementValueID(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated CElementValueID = " + no);

      // Disable triggers
      ImportAccountData.disableTriggers(con, conn);
      if (log4j.isDebugEnabled())
        log4j.debug("Triggers in C_ValidCombination disabled");

      conn.releaseCommitConnection(con);
      // Default accounts
      String[][] defaults = null;
      defaults = new String[73][2];

      defaults[0][0] = "PPVOFFSET_ACCT";
      defaults[1][0] = "USESUSPENSEBALANCING";// Never used
      defaults[2][0] = "SUSPENSEBALANCING_ACCT";
      defaults[3][0] = "USESUSPENSEERROR";// Never used
      defaults[4][0] = "SUSPENSEERROR_ACCT";
      defaults[5][0] = "USECURRENCYBALANCING";// Never used
      defaults[6][0] = "CURRENCYBALANCING_ACCT";
      defaults[7][0] = "RETAINEDEARNING_ACCT";
      defaults[8][0] = "INTERCOMPANYDUETO_ACCT";
      defaults[9][0] = "INTERCOMPANYDUEFROM_ACCT";
      defaults[10][0] = "CFS_ORDER_ACCT";
      defaults[11][0] = "W_INVENTORY_ACCT";
      defaults[12][0] = "W_INVACTUALADJUST_ACCT";
      defaults[13][0] = "W_DIFFERENCES_ACCT";
      defaults[14][0] = "W_REVALUATION_ACCT";
      defaults[15][0] = "P_REVENUE_ACCT";
      defaults[16][0] = "P_EXPENSE_ACCT";
      defaults[17][0] = "P_ASSET_ACCT";
      defaults[18][0] = "P_INVOICEPRICEVARIANCE_ACCT";
      defaults[19][0] = "P_TRADEDISCOUNTREC_ACCT";
      defaults[20][0] = "P_TRADEDISCOUNTGRANT_ACCT";
      defaults[21][0] = "P_COGS_ACCT";
      defaults[22][0] = "C_RECEIVABLE_ACCT";
      defaults[23][0] = "V_LIABILITY_ACCT";
      defaults[24][0] = "V_LIABILITY_SERVICES_ACCT";
      defaults[25][0] = "V_PREPAYMENT_ACCT";
      defaults[26][0] = "PAYDISCOUNT_EXP_ACCT";
      defaults[27][0] = "WRITEOFF_ACCT";
      defaults[28][0] = "PAYDISCOUNT_REV_ACCT";
      defaults[29][0] = "UNREALIZEDGAIN_ACCT";
      defaults[30][0] = "UNREALIZEDLOSS_ACCT";
      defaults[31][0] = "REALIZEDGAIN_ACCT";
      defaults[32][0] = "REALIZEDLOSS_ACCT";
      defaults[33][0] = "WITHHOLDING_ACCT";
      defaults[34][0] = "E_PREPAYMENT_ACCT";
      defaults[35][0] = "E_EXPENSE_ACCT";
      defaults[36][0] = "PJ_ASSET_ACCT";
      defaults[37][0] = "PJ_WIP_ACCT";
      defaults[38][0] = "T_EXPENSE_ACCT";
      defaults[39][0] = "T_LIABILITY_ACCT";
      defaults[40][0] = "T_RECEIVABLES_ACCT";
      defaults[41][0] = "T_DUE_ACCT";
      defaults[42][0] = "T_CREDIT_ACCT";
      defaults[43][0] = "B_INTRANSIT_ACCT";
      defaults[44][0] = "B_ASSET_ACCT";
      defaults[45][0] = "B_EXPENSE_ACCT";
      defaults[46][0] = "B_INTERESTEXP_ACCT";
      defaults[47][0] = "B_UNIDENTIFIED_ACCT";
      defaults[48][0] = "B_UNALLOCATEDCASH_ACCT";
      defaults[49][0] = "B_PAYMENTSELECT_ACCT";
      defaults[50][0] = "B_SETTLEMENTGAIN_ACCT";
      defaults[51][0] = "B_SETTLEMENTLOSS_ACCT";
      defaults[52][0] = "B_REVALUATIONGAIN_ACCT";
      defaults[53][0] = "B_REVALUATIONLOSS_ACCT";
      defaults[54][0] = "CH_EXPENSE_ACCT";
      defaults[55][0] = "CH_REVENUE_ACCT";
      defaults[56][0] = "UNEARNEDREVENUE_ACCT";
      defaults[57][0] = "NOTINVOICEDRECEIVABLES_ACCT";
      defaults[58][0] = "NOTINVOICEDREVENUE_ACCT";
      defaults[59][0] = "NOTINVOICEDRECEIPTS_ACCT";
      defaults[60][0] = "CB_ASSET_ACCT";
      defaults[61][0] = "CB_CASHTRANSFER_ACCT";
      defaults[62][0] = "CB_DIFFERENCES_ACCT";
      defaults[63][0] = "CB_RECEIPT_ACCT";
      defaults[64][0] = "A_ACCUMDEPRECIATION_ACCT";
      defaults[65][0] = "A_DEPRECIATION_ACCT";
      defaults[66][0] = "A_DISPOSAL_GAIN";
      defaults[67][0] = "INCOMESUMMARY_ACCT";
      defaults[68][0] = "P_PURCHASEPRICEVARIANCE_ACCT";
      defaults[69][0] = "C_PREPAYMENT_ACCT";
      defaults[70][0] = "B_INTERESTREV_ACCT";
      defaults[71][0] = "CB_EXPENSE_ACCT";
      defaults[72][0] = "A_DISPOSAL_LOSS";

      // till here, the edition of the I_ElementValue table
      // now, the insertion from I_ElementValue table in C_ElementValue...

      int noInsert = 0;
      int noUpdate = 0;
      int noAccountError = 0;

      con = conn.getTransactionConnection();
      ImportAccountData[] records = ImportAccountData.selectRecords(conn, getAD_Client_ID());
      for (int i = 0; i < records.length; i++) {
        String I_ElementValue_ID = records[i].iElementvalueId;
        String C_ElementValue_ID = records[i].cElementvalueId;

        if (log4j.isDebugEnabled())
          log4j.debug("I_ElementValue_ID=" + I_ElementValue_ID + ", C_ElementValue_ID="
              + C_ElementValue_ID);
        if (C_ElementValue_ID.equals("0") || C_ElementValue_ID == null
            || C_ElementValue_ID.equals("")) { // insert new
          try {
            C_ElementValue_ID = SequenceIdData.getUUID();
            records[i].cElementvalueId = C_ElementValue_ID;
            no = ImportAccountData.insertElementValue(con, conn, C_ElementValue_ID,
                I_ElementValue_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Insert ElementValue = " + no);
            noInsert += no;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Insert ElementValue - " + ex.toString());
            ImportAccountData.insertElementValueError(con, conn, ex.toString(), I_ElementValue_ID);
            continue;
          }
        } else { // update
          try {
            no = ImportAccountData.updateElementValue(con, conn, I_ElementValue_ID,
                C_ElementValue_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("Insert ElementValue = " + no);
            noUpdate += no;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled())
              log4j.debug("Update ElementValue - " + ex.toString());
            ImportAccountData.updateElementValueError(con, conn, ex.toString(), I_ElementValue_ID);
            continue;
          }
        }
        ImportAccountData.updateProcessing(con, conn, C_ElementValue_ID, I_ElementValue_ID);
      }
      // records = ImportAccountData.selectRecords(con, conn,
      // getAD_Client_ID());

      for (int i = 0; i < records.length; i++) {
        String I_ElementValue_ID = records[i].iElementvalueId;
        String elementValue = records[i].value;
        boolean found = false;
        for (int j = 0; j < 73 && !found && records[i].defaultAccount != null
            && !(records[i].defaultAccount).equals(""); j++) {
          if (defaults[j][0].equals(records[i].defaultAccount)) {
            defaults[j][1] = records[i].cElementvalueId;
            found = true;
          }
        }

        if (log4j.isDebugEnabled())
          log4j.debug("I_ElementValue_ID=" + I_ElementValue_ID + ", elementValue=" + elementValue);
        try {
          String[][] strOperand = operandProcess(ImportAccountData.selectOperands(con, conn,
              I_ElementValue_ID));
          String strSeqNo = "10";
          for (int j = 0; strOperand != null && j < strOperand.length; j++) {
            String C_ElementValue_Operand_ID = SequenceIdData.getUUID();
            String strAccount = ImportAccountData.selectAccount(con, conn, strOperand[j][0], vars
                .getClient(), m_C_Element_ID);
            String strElementValue = ImportAccountData.selectAccount(con, conn, elementValue, vars
                .getClient(), m_C_Element_ID);
            if (strAccount != null && !strAccount.equals("")) {
              ImportAccountData.insertOperands(con, conn, C_ElementValue_Operand_ID,
                  (strOperand[j][1].equals("+") ? "1" : "-1"), strElementValue, strAccount,
                  strSeqNo, vars.getClient(), vars.getUser());
              strSeqNo = nextSeqNo(strSeqNo);
            } else {
              if (log4j.isDebugEnabled())
                log4j.debug("Operand not inserted - Value = " + strOperand[j][0]);
            }
          }
        } catch (ServletException ex) {
          log4j.warn("ServletException I_ElementValue_ID=" + I_ElementValue_ID);
          ImportAccountData.insertElementValueError(con, conn, ex.toString(), I_ElementValue_ID);
          continue;
        }
      }
      ImportAccountData[] acctSchemas = ImportAccountData.selectAcctSchema(conn, m_C_Element_ID,
          getAD_Client_ID());
      for (int g = 0; g < acctSchemas.length; g++) {
        String defaultsRecordNo = ImportAccountData.selectDefaultsRecord(con, conn,
            acctSchemas[g].cAcctschemaId);
        if (defaultsRecordNo.equals("0")) {
          String C_AcctSchema_Default_ID = SequenceIdData.getUUID();
          ImportAccountData.insertDefaultsRecord(con, conn, acctSchemas[g].cAcctschemaId,
              acctSchemas[g].adClientId, acctSchemas[g].adOrgId, vars.getUser(),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[11][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[12][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[13][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[14][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[15][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[16][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[17][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[68][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[18][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[19][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[20][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[21][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[22][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[69][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[23][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[24][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[25][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[26][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[27][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[28][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[29][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[30][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[31][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[32][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[33][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[34][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[35][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[36][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[37][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[38][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[39][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[40][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[41][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[42][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[43][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[44][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[45][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[70][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[46][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[47][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[48][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[49][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[50][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[51][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[52][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[53][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[54][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[55][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[56][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[57][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[58][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[59][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[60][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[61][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[62][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[71][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[63][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[64][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[65][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[66][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[72][1], vars.getUser()),
              C_AcctSchema_Default_ID);
        }
        String gLRecordNo = ImportAccountData.selectGLRecord(con, conn,
            acctSchemas[g].cAcctschemaId);
        if (gLRecordNo.equals("0"))
          ImportAccountData.insertGLRecord(con, conn, acctSchemas[g].cAcctschemaId,
              acctSchemas[g].adClientId, acctSchemas[g].adOrgId, vars.getUser(),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[2][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[4][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[6][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[7][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[67][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[8][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[9][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[0][1], vars.getUser()),
              getValidCombination(con, conn, acctSchemas[g].adClientId, acctSchemas[g].adOrgId,
                  acctSchemas[g].cAcctschemaId, defaults[10][1], vars.getUser()));
      }
      // Set Error to indicator to not imported
      noAccountError = ImportAccountData.updateNotImported(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Errors: " + noAccountError);
      if (log4j.isDebugEnabled())
        log4j.debug("Inserts: " + noInsert);
      if (log4j.isDebugEnabled())
        log4j.debug("Updates: " + noUpdate);
      addLog(Utility.messageBD(conn, "Elements not imported", vars.getLanguage()) + ": "
          + noAccountError + "; ");
      addLog("Elements inserted: " + noInsert + "; ");
      addLog("Elements updated: " + noUpdate + "; ");

      if (noAccountError == 0) {
        myError.setType("Success");
        myError.setTitle(Utility.messageBD(conn, "Success", vars.getLanguage()));
      } else if (noInsert > 0 || noUpdate > 0) {
        myError.setType("Warning");
        myError.setTitle(Utility.messageBD(conn, "Some elements could not be imported", vars
            .getLanguage()));
      } else {
        myError.setType("Error");
        myError.setTitle(Utility.messageBD(conn, " No elements could be imported", vars
            .getLanguage()));
      }

      conn.releaseCommitConnection(con);

      con = conn.getTransactionConnection();
      // Set parent
      no = ImportAccountData.setParent(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated parent = " + no);
      no = ImportAccountData.setParentError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated parent errors = " + no);

      int noParentUpdate = 0;
      ImportAccountData[] parents = ImportAccountData.selectParents(con, conn, getAD_Client_ID());
      for (int i = 0; i < parents.length; i++) {
        no = ImportAccountData.updateTree(con, conn, parents[i].parentelementvalueId,
            parents[i].seqno, parents[i].adTreeId, parents[i].cElementvalueId);
        noParentUpdate += no;
      }
      addLog("Parent updates: " + noParentUpdate + " ");
      if (log4j.isDebugEnabled())
        log4j.debug("Parent updates: " + noParentUpdate);

      // Reset Processing Flag
      if (m_updateDefaultAccounts) {
        no = ImportAccountData.updateProcessed(con, conn, getAD_Client_ID(), "clause");
        updateDefaults(con, conn, vars);
      } else {
        no = ImportAccountData.updateProcessed(con, conn, getAD_Client_ID(), "");
      }
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated parent = " + no);

      // Re-enable triggers
      ImportAccountData.enableTriggers(con, conn);
      if (log4j.isDebugEnabled())
        log4j.debug("Triggers in C_ValidCombination enabled");
      // Update Description
      no = ImportAccountData.updateDescription(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated description = " + no);

      conn.releaseCommitConnection(con);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@"
          + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }

    myError.setMessage(Utility.messageBD(conn, getLog(), vars.getLanguage()));
    return myError;
  }

  private void updateDefaults(Connection con, ConnectionProvider conn, VariablesSecureApp vars) {
    if (log4j.isDebugEnabled())
      log4j.debug("ImportAccount.updateDefaults - CreateNewCombination= " + m_createNewCombination);
    int no = 0;
    try {
      ImportAccountData[] acctSchemas = ImportAccountData.selectAcctSchema(conn, m_C_Element_ID,
          getAD_Client_ID());
      for (int i = 0; i < acctSchemas.length; i++)
        updateDefaultAccounts(con, conn, vars, acctSchemas[i].cAcctschemaId);
      no = ImportAccountData.updateDefaultAcct(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount updated default acct = " + no);
    } catch (ServletException e) {
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount.updateDefaults", e);
    }
  } // updateDefaults

  private void updateDefaultAccounts(Connection con, ConnectionProvider conn,
      VariablesSecureApp vars, String C_AcctSchema_ID) {
    try {
      int no = 0;
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount.updateDefaultAccounts :: C_AcctSchema_ID="
            + String.valueOf(C_AcctSchema_ID));
      if (!ImportAccountData.selectAcctSchemaAC(conn, String.valueOf(C_AcctSchema_ID)).equals(
          m_C_Element_ID)) {
        if (log4j.isDebugEnabled())
          log4j.debug("ImportAccount.updateDefaultAccounts :: C_Element_ID=" + m_C_Element_ID
              + " not in AcctSchema=" + C_AcctSchema_ID);
        return;
      }
      ImportAccountData[] data = ImportAccountData.selectElementColumnTable(conn, m_C_Element_ID);
      for (int i = 0; i < data.length; i++) {
        int u = updateDefaultAccount(con, conn, vars, data[i].tablename, data[i].columnname,
            C_AcctSchema_ID, data[i].cElementvalueId);
        if (u != 0) {
          no = ImportAccountData.updateProcessingN(con, conn, data[i].iElementvalueId);
          if (no != 1)
            if (log4j.isDebugEnabled())
              log4j.debug("ImportAccount.updateDefaultAccounts - Updated=" + no);
        }
      }
    } catch (ServletException se) {
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount.updateDefaultAccounts", se);
    }
  }

  private int updateDefaultAccount(Connection con, ConnectionProvider conn,
      VariablesSecureApp vars, String TableName, String ColumnName, String C_AcctSchema_ID,
      String C_ElementValue_ID) {
    int no = 0;
    int retValue = UPDATE_ERROR;
    try {
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount.updateDefaultAccount - " + TableName + "." + ColumnName + " - "
            + C_ElementValue_ID);
      ImportAccountData[] data = ImportAccountData.selectValidCombination(conn, ColumnName,
          TableName, C_AcctSchema_ID);
      if (data.length == 0) {
        if (log4j.isDebugEnabled())
          log4j
              .debug("ImportAccount.updateDefaultAccount - Account not found. Creating a Valid Combination");
        ImportAccountData insertVC = ImportAccountData.insertValidCombination(con, conn, vars
            .getClient(), vars.getOrg(), C_AcctSchema_ID, C_ElementValue_ID, "", "",
            vars.getUser(), "", "", "", "");
        if (insertVC == null) {
          log4j
              .debug("ImportAccount.updateDefaultAccount - Account not found. Error creating Valid Combination for "
                  + ColumnName + " default.");
        } else {
          int num = ImportAccountData.addDefault(con, conn, TableName, ColumnName,
              insertVC.cValidcombinationId, C_AcctSchema_ID);
          if (num != 1)
            log4j
                .error("ImportAccount.updateDefaultAccount - Account not found. Error updating default value for "
                    + ColumnName + " default.");
        }
      }
      if (data[0].accountId.equals(C_ElementValue_ID)) {
        retValue = UPDATE_SAME;
        if (log4j.isDebugEnabled())
          log4j.debug("Account_ID same as new value");
      } else { // update the account value
        if (m_createNewCombination) {
          ImportAccountData[] account = ImportAccountData.selectValidCombinationAll(conn,
              data[0].cValidcombinationId);
          ImportAccountData.updateAccountIdByVC(con, conn, C_ElementValue_ID,
              data[0].cValidcombinationId);
          RespuestaCS respuestaCS = ImportAccountData.getCValidCombination(con, conn,
              account[0].adClientId, account[0].adOrgId, account[0].cAcctschemaId, String
                  .valueOf(C_ElementValue_ID), data[0].cValidcombinationId,
              account[0].isfullyqualified, account[0].alias, account[0].createdby,
              account[0].mProductId, account[0].cBpartnerId, account[0].adOrgtrxId,
              account[0].cLocfromId, account[0].cLoctoId, account[0].cSalesregionId,
              account[0].cProjectId, account[0].cCampaignId, account[0].cActivityId,
              account[0].user1Id, account[0].user2Id);
          int newC_ValidCombination_ID = Integer.valueOf(respuestaCS.CValidCombinationId)
              .intValue();
          if (!data[0].cValidcombinationId.equals(String.valueOf(newC_ValidCombination_ID))) {
            no = ImportAccountData.updateAbstract(con, conn, TableName, ColumnName, String
                .valueOf(newC_ValidCombination_ID), C_AcctSchema_ID);
            if (log4j.isDebugEnabled())
              log4j.debug("ImportAccount.updateDefaultAccount - #" + no + " - " + TableName + "."
                  + ColumnName + " - " + C_ElementValue_ID + " -- " + data[0].cValidcombinationId
                  + " -> " + newC_ValidCombination_ID);
            if (no == 1)
              retValue = UPDATE_YES;
          }
        } else {
          no = ImportAccountData.updateAccountIdByVC(con, conn, C_ElementValue_ID,
              data[0].cValidcombinationId);
          if (log4j.isDebugEnabled())
            log4j.debug("ImportAccount.updateDefaultAccount - Replace #" + no + " - "
                + "C_ValidCombination_ID=" + data[0].cValidcombinationId + ", New Account_ID="
                + C_ElementValue_ID);
          if (no == 1) {
            retValue = UPDATE_YES;
            no = ImportAccountData.updateAccountId(con, conn, C_ElementValue_ID, data[0].accountId);
            if (log4j.isDebugEnabled())
              log4j.debug("ImportAccount.updateDefaultAccount - Replace VC #" + no + " - "
                  + "Account_ID=" + data[0].accountId + ", New Account_ID=" + C_ElementValue_ID);
            no = ImportAccountData.updateFact(con, conn, C_ElementValue_ID, data[0].accountId);
            if (log4j.isDebugEnabled())
              log4j.debug("ImportAccount.updateDefaultAccount - Replace Fact #" + no + " - "
                  + "Account_ID=" + data[0].accountId + ", New Account_ID=" + C_ElementValue_ID);
          }
        }
      }
      return retValue;
    } catch (Exception e) {
      if (log4j.isDebugEnabled())
        log4j.debug("ImportAccount.updateDefaultAccount " + e);
    }
    return retValue;
  } // updateDefaultAccount

  private String[][] operandProcess(String strOperand) {
    if (strOperand == null || strOperand.equals(""))
      return null;
    StringTokenizer st = new StringTokenizer(strOperand, "+-", true);
    StringTokenizer stNo = new StringTokenizer(strOperand, "+-", false);
    int no = stNo.countTokens();
    String[][] strResult = new String[no][2];
    no = 0; // Token No
    int i = 0; // Array position
    strResult[0][1] = "+";
    while (st.hasMoreTokens()) {
      if (i % 2 != 1) {
        strResult[no][0] = st.nextToken();
        no++;
      } else
        strResult[no][1] = st.nextToken();
      i++;
    }
    // strResult = filterArray(strResult);
    return strResult;
  } // operandProcess

  private String getValidCombination(Connection con, ConnectionProvider conn, String AD_Client_ID,
      String AD_Org_ID, String C_AcctSchema_ID, String Account_ID, String AD_User_ID)
      throws ServletException {
    if (Account_ID == null || Account_ID.equals(""))
      return "";
    RespuestaCS respuestaCS;
    String C_ValidCombination_ID = "";
    respuestaCS = ImportAccountData.getCValidCombination(con, conn, AD_Client_ID, AD_Org_ID,
        C_AcctSchema_ID, Account_ID, "", "Y", "", AD_User_ID, "", "", "", "", "", "", "", "", "",
        "", "");
    if (respuestaCS != null)
      C_ValidCombination_ID = respuestaCS.CValidCombinationId;
    return C_ValidCombination_ID;
  }

  private String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    String SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }
}
