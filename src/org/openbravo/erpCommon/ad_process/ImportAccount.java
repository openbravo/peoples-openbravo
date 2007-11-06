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
 * Contributions are Copyright (C) 2001-2006 Openbravo S.L.
 ******************************************************************************
*/

package org.openbravo.erpCommon.ad_process;

import org.openbravo.erpCommon.utility.*;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.exception.*;

import java.math.BigDecimal;
import java.sql.*;
import javax.servlet.*;
import java.util.StringTokenizer;
import org.apache.log4j.Logger ;

// imports for transactions
import org.openbravo.database.ConnectionProvider;
import java.sql.Connection;


public class ImportAccount extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportAccount.class);

  private String m_AD_Process_ID = "";
  private String m_C_Element_ID = "";
  private boolean m_deleteOldImported;
  private boolean m_updateDefaultAccounts;
  private boolean  m_createNewCombination;
	private static final int	UPDATE_ERROR = 0;
	private static final int	UPDATE_YES = 1;
	private static final int	UPDATE_SAME = 2;


  public ImportAccount(ConnectionProvider conn, String AD_Process_ID, boolean deleteOld, String C_Element_ID, boolean updateDefaultAccounts, boolean createNewCombination) {
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
    if (log4j.isDebugEnabled()) log4j.debug("Creating parameters");
  }

  protected boolean doIt(VariablesSecureApp vars) throws ServletException {
    int no = 0;
    ConnectionProvider conn = null;
    Connection con = null;
    try {
      conn = getConnection();
      con = conn.getTransactionConnection();
      if(m_deleteOldImported) {
        no = ImportAccountData.deleteOld(con, conn, getAD_Client_ID());
        if (log4j.isDebugEnabled()) log4j.debug("Delete Old Imported = " + no);
      }
      //  Set Client, Org, IaActive, Created/Updated
      no = ImportAccountData.updateRecords(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount Reset = " + no);
  
      // Set element
      if (!m_C_Element_ID.equals("0")) {
        no = ImportAccountData.updateElement(con, conn, m_C_Element_ID, getAD_Client_ID());
        if (log4j.isDebugEnabled()) log4j.debug("ImportAccount Element = " + no);
      }
      no = ImportAccountData.updateIdByName(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount ids by name = " + no);
      no = ImportAccountData.updateElementError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount element errors = " + no);
  
      // Set column
      no = ImportAccountData.updateColumn(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated columns = " + no);
      no = ImportAccountData.updateColumnError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount column errors = " + no);
  
      // Set default values for PostActual, PostBudget, PostStatistical, PostEncumbrance, IsSummary, IsDocControlled
      String[] yColumns = new String[] {"PostActual", "PostBudget", "PostStatistical", "PostEncumbrance"};
      for (int i = 0; i < yColumns.length; i++) {
        no = ImportAccountData.updateYColumns(con, conn, yColumns[i], getAD_Client_ID());
        if (log4j.isDebugEnabled()) log4j.debug("ImportAccount " + yColumns[i] + " errors = " + no);
      }
      no = ImportAccountData.updateSummary(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated summary = " + no);
      no = ImportAccountData.updateDocControlled(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated docControlled = " + no);
  
      // Check Account Type A (E) L M O R
      no = ImportAccountData.updateAccountType(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated AccountType = " + no);
      no = ImportAccountData.updateAccountTypeError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount AccountType errors = " + no);
  
      // Check Account Sign (N) C B
      no = ImportAccountData.updateAccountSign(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated AccountSign = " + no);
      no = ImportAccountData.updateAccountSignError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount AccountSign errors = " + no);
  
      //  Update ElementValue from existing
      no = ImportAccountData.updateCElementValueID(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated CElementValueID = " + no);
  
      // Disable triggers
      ImportAccountData.disableTriggers(con, conn);
      if (log4j.isDebugEnabled()) log4j.debug("Triggers in C_ValidCombination disabled");
  
      conn.releaseCommitConnection(con);
  
      // till here, the edition of the I_ElementValue table
      // now, the insertion from I_ElementValue table in C_ElementValue...
  
      int noInsert = 0;
      int noUpdate = 0;
  
      ImportAccountData[] records = ImportAccountData.selectRecords(conn, getAD_Client_ID());
      con = conn.getTransactionConnection();
      for (int i =0;i<records.length;i++) {
        String I_ElementValue_ID = records[i].iElementvalueId;
        String C_ElementValue_ID = records[i].cElementvalueId;
        if (log4j.isDebugEnabled()) log4j.debug("I_ElementValue_ID=" + I_ElementValue_ID + ", C_ElementValue_ID=" + C_ElementValue_ID);
        if (C_ElementValue_ID.equals("0") || C_ElementValue_ID == null || C_ElementValue_ID.equals("")) { // insert new
          try {
            C_ElementValue_ID = SequenceIdData.getSequence(conn, "C_ElementValue", vars.getClient());
            no = ImportAccountData.insertElementValue(con, conn, C_ElementValue_ID, I_ElementValue_ID);
            if (log4j.isDebugEnabled()) log4j.debug("Insert ElementValue = " + no);
            noInsert+=no;
            String [][] strOperand = operandProcess(ImportAccountData.selectOperands(conn, I_ElementValue_ID));
            String strSeqNo = "10";
            for(int j=0;strOperand!=null && j<strOperand.length;j++){
            	String C_ElementValue_Operand_ID = SequenceIdData.getSequence(conn, "C_ElementValue_Operand", vars.getClient());
            	String strAccount = ImportAccountData.selectAccount(con, conn, strOperand[j][0], vars.getClient());
            	if(strAccount!=null && !strAccount.equals("")){
            	  ImportAccountData.insertOperands(con, conn, C_ElementValue_Operand_ID, (strOperand[j][1].equals("+")?"1":"-1"), C_ElementValue_ID, strAccount, strSeqNo, vars.getClient(), vars.getUser());
                  strSeqNo = nextSeqNo(strSeqNo);
                }
            }
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled()) log4j.debug("Insert ElementValue - " + ex.toString());
            ImportAccountData.insertElementValueError(con, conn, ex.toString(), I_ElementValue_ID);
            continue;
          }
        } else { // update
          try {
            no = ImportAccountData.updateElementValue(con, conn, I_ElementValue_ID, C_ElementValue_ID);
            if (log4j.isDebugEnabled()) log4j.debug("Insert ElementValue = " + no);
            noUpdate+=no;
          } catch (ServletException ex) {
            if (log4j.isDebugEnabled()) log4j.debug("Update ElementValue - " + ex.toString());
            ImportAccountData.updateElementValueError(con, conn, ex.toString(), I_ElementValue_ID);
            continue;
          }
        }
        ImportAccountData.updateProcessing(con, conn, C_ElementValue_ID, I_ElementValue_ID);
      }
      no = ImportAccountData.updateNotImported(con, conn);
      if (log4j.isDebugEnabled()) log4j.debug("Errors: " + no);
      if (log4j.isDebugEnabled()) log4j.debug("Inserts: " + noInsert);
      if (log4j.isDebugEnabled()) log4j.debug("Updates: " + noUpdate);
      addLog(Utility.messageBD(conn, "Errors", vars.getLanguage()) + ": " + no + "\\n");
      addLog("Elements inserted: " + noInsert + "\\n");
      addLog("Elements updated: " + noUpdate + "\\n");
      conn.releaseCommitConnection(con);
  
      con = conn.getTransactionConnection();
      // Set parent
      no = ImportAccountData.setParent(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated parent = " + no);
      no = ImportAccountData.setParentError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated parent errors = " + no);
  
      int noParentUpdate = 0;
      ImportAccountData[] parents = ImportAccountData.selectParents(con, conn, getAD_Client_ID());
      for (int i=0;i<parents.length;i++) {
        no = ImportAccountData.updateTree(con, conn, parents[i].parentelementvalueId, parents[i].iElementvalueId, parents[i].adTreeId, parents[i].cElementvalueId);
        noParentUpdate += no;
      }
      addLog("Parent updates: " + noParentUpdate + "\\n");
      if (log4j.isDebugEnabled()) log4j.debug("Parent updates: " + noParentUpdate);
  
      // Reset Processing Flag
      if (m_updateDefaultAccounts) {
        no = ImportAccountData.updateProcessed(con, conn, getAD_Client_ID(), "clause");
        updateDefaults(con, conn);
      } else {
        no = ImportAccountData.updateProcessed(con, conn, getAD_Client_ID(), "");
      }
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated parent = " + no);
  
      // Re-enable triggers
      ImportAccountData.enableTriggers(con, conn);
      if (log4j.isDebugEnabled()) log4j.debug("Triggers in C_ValidCombination enabled");
      // Update Description
      no = ImportAccountData.updateDescription(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated description = " + no);
  
      conn.releaseCommitConnection(con);
    } catch (NoConnectionAvailableException ex) {
      throw new ServletException("@CODE=NoConnectionAvailable");
    } catch (SQLException ex2) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=" + Integer.toString(ex2.getErrorCode()) + "@" + ex2.getMessage());
    } catch (Exception ex3) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {}
      throw new ServletException("@CODE=@" + ex3.getMessage());
    }
    return true;
  }

  private void updateDefaults (Connection con, ConnectionProvider conn) {
    if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaults - CreateNewCombination= " + m_createNewCombination);
    int no = 0;
    try {
      ImportAccountData[] acctSchemas = ImportAccountData.selectAcctSchema(conn, m_C_Element_ID, getAD_Client_ID());
      for (int i =0;i<acctSchemas.length;i++)
        updateDefaultAccounts(con, conn, Integer.valueOf(acctSchemas[i].cAcctschemaId).intValue());
      no = ImportAccountData.updateDefaultAcct(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount updated default acct = " + no);
    } catch (ServletException e) {
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaults", e);
    }
  }  //  updateDefaults

  private void updateDefaultAccounts (Connection con, ConnectionProvider conn, int C_AcctSchema_ID) {
    try {
      int no = 0;
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccounts :: C_AcctSchema_ID=" + String.valueOf(C_AcctSchema_ID));
      if (!ImportAccountData.selectAcctSchemaAC(conn, String.valueOf(C_AcctSchema_ID)).equals(m_C_Element_ID)) {
        if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccounts :: C_Element_ID=" + m_C_Element_ID + " not in AcctSchema=" + String.valueOf(C_AcctSchema_ID));
        return;
      }
      ImportAccountData[] data = ImportAccountData.selectElementColumnTable(conn, m_C_Element_ID);
      for (int i =0;i<data.length;i++) {
        int u = updateDefaultAccount(con, conn, data[i].tablename, data[i].columnname, C_AcctSchema_ID, Integer.parseInt(data[i].cElementvalueId,10));
        if (u != 0) {
          no = ImportAccountData.updateProcessingN(con, conn, data[i].iElementvalueId);
          if (no != 1) if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccounts - Updated=" + no);
        }
      }
    } catch (ServletException se) {
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccounts", se);
    }
  }

  private int updateDefaultAccount (Connection con, ConnectionProvider conn, String TableName, String ColumnName, int C_AcctSchema_ID, int C_ElementValue_ID) {
    int no = 0;
    int retValue = UPDATE_ERROR;
    try {
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - "  + TableName + "." + ColumnName + " - " + C_ElementValue_ID);
      ImportAccountData[] data = ImportAccountData.selectValidCombination(conn, ColumnName, TableName, String.valueOf(C_AcctSchema_ID));
      if (data.length > 0) {
        if (data[0].accountId.equals(String.valueOf(C_ElementValue_ID))) {
          retValue = UPDATE_SAME;
          if (log4j.isDebugEnabled()) log4j.debug("Account_ID same as new value");
        } else { // update the account value
          if (m_createNewCombination) {
            ImportAccountData[] account = ImportAccountData.selectValidCombinationAll(conn, data[0].cValidcombinationId);
            ImportAccountData.updateAccountIdByVC(con, conn, String.valueOf(C_ElementValue_ID), data[0].cValidcombinationId);
            RespuestaCS respuestaCS = ImportAccountData.getCValidCombination(con, conn, account[0].adClientId, account[0].adOrgId, account[0].cAcctschemaId, String.valueOf(C_ElementValue_ID), data[0].cValidcombinationId, account[0].isfullyqualified, account[0].alias, account[0].createdby, account[0].mProductId, account[0].cBpartnerId, account[0].adOrgtrxId, account[0].cLocfromId, account[0].cLoctoId, account[0].cSalesregionId, account[0].cProjectId, account[0].cCampaignId, account[0].cActivityId, account[0].user1Id, account[0].user2Id);
            int newC_ValidCombination_ID = Integer.valueOf(respuestaCS.CValidCombinationId).intValue();
            if (!data[0].cValidcombinationId.equals(String.valueOf(newC_ValidCombination_ID))) {
              no = ImportAccountData.updateAbstract(con, conn, TableName, ColumnName, String.valueOf(newC_ValidCombination_ID), String.valueOf(C_AcctSchema_ID));
              if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - #" + no + " - " + TableName + "." + ColumnName + " - " + C_ElementValue_ID + " -- " + data[0].cValidcombinationId + " -> " + newC_ValidCombination_ID);
              if (no==1)
                retValue = UPDATE_YES;
            }
          } else {
            no = ImportAccountData.updateAccountIdByVC(con, conn, String.valueOf(C_ElementValue_ID), data[0].cValidcombinationId);
            if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - Replace #" + no + " - " + "C_ValidCombination_ID=" + data[0].cValidcombinationId + ", New Account_ID=" + C_ElementValue_ID);
            if (no == 1) {
              retValue = UPDATE_YES;
              no = ImportAccountData.updateAccountId(con, conn, String.valueOf(C_ElementValue_ID), data[0].accountId);
              if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - Replace VC #" + no + " - " + "Account_ID=" + data[0].accountId + ", New Account_ID=" + C_ElementValue_ID);
              no = ImportAccountData.updateFact(con, conn, String.valueOf(C_ElementValue_ID), data[0].accountId);
              if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - Replace Fact #" + no + " - " + "Account_ID=" + data[0].accountId + ", New Account_ID=" + C_ElementValue_ID);
            }
          }
        }
      } else {
        if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount - Account not found.");
      }
      return retValue;
    } catch (Exception e) {
      if (log4j.isDebugEnabled()) log4j.debug("ImportAccount.updateDefaultAccount " + e);
    }
    return retValue;
  }  //  updateDefaultAccount
  
  private String [][] operandProcess (String strOperand) {
     if(strOperand==null || strOperand.equals("")) return null;
     StringTokenizer st = new StringTokenizer(strOperand,"+-",true);
     StringTokenizer stNo = new StringTokenizer(strOperand,"+-",false);
     int no=stNo.countTokens();
     String [][] strResult = new String [no][2];
     no=0; //Token No
     int i=0; // Array position
     strResult[0][1] = "+";
     while (st.hasMoreTokens()) {
         if(i%2!=1){
             strResult[no][0] = st.nextToken();
             no++;
         }
         else strResult[no][1] = st.nextToken();
         i++;
     }
//     strResult = filterArray(strResult);
     return strResult;
  }  //  operandProcess
  
  public String nextSeqNo(String oldSeqNo){
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    String SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }
}
