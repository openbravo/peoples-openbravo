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
 * All portions are Copyright (C) 2001-2008 Openbravo SLU
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

public class ImportTax extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportBPartner.class);

  private String m_AD_Process_ID = "";
  private String m_Record_ID = "";
  private boolean m_deleteOldImported;

  public ImportTax(ConnectionProvider conn, String AD_Process_ID, String recordId, boolean deleteOld) {
    super(conn);
    m_AD_Process_ID = AD_Process_ID;
    m_Record_ID = recordId;
    m_deleteOldImported = deleteOld;
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
        no = ImportTaxData.deleteOld(con, conn, getAD_Client_ID());
        if (log4j.isDebugEnabled())
          log4j.debug("Delete Old Imported = " + no);
      }
      // Set Client, Org, IaActive, Created/Updated
      no = ImportTaxData.updateRecords(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Reset = " + no);

      /*
       * This cannot be done like this, because it can be referred by data inside the file, it has
       * to be done line per line // Tax Category ID no = ImportTaxData.updateTaxCategoryId(con,
       * conn, getAD_Client_ID()); if (log4j.isDebugEnabled()) log4j.debug("Tax Category Id = " +
       * no);
       * 
       * // BP Tax Category ID no = ImportTaxData.updateBPTaxCategoryId(con, conn,
       * getAD_Client_ID()); if (log4j.isDebugEnabled()) log4j.debug("BP Tax Category Id = " + no);
       * 
       * // Tax ID no = ImportTaxData.updateTaxId(con, conn, getAD_Client_ID()); if
       * (log4j.isDebugEnabled()) log4j.debug("BP Tax Category Id = " + no);
       */

      // Set Country From
      no = ImportTaxData.updateCountryFromId(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Set Country From =" + no);

      no = ImportTaxData.updateCountryFromError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Country From =" + no);

      // Set Country To
      no = ImportTaxData.updateCountryToId(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Set Country to =" + no);

      no = ImportTaxData.updateCountryToError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Country to =" + no);

      // Set Region From
      no = ImportTaxData.updateRegionFromId(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Set Region From =" + no);

      no = ImportTaxData.updateRegionFromError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Region From =" + no);

      // Set Region From
      no = ImportTaxData.updateRegionToId(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Set Region To =" + no);

      no = ImportTaxData.updateRegionToError(con, conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Invalid Region To =" + no);

      //

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

    // till here, the edition of the I_ImportBPartner table
    // now, the insertion from I_ImportBPartner table in C_BPartner...

    int noTaxInsert = 0;
    int noTaxUpdate = 0;
    int noTCInsert = 0;
    int noTCUpdate = 0;
    int noBPTCInsert = 0;
    int noBPTCUpdate = 0;
    int noTaxError = 0;

    try {
      // Go through Records
      ImportTaxData[] data = ImportTaxData.select(conn, getAD_Client_ID());
      if (log4j.isDebugEnabled())
        log4j.debug("Going through " + data.length + " records");
      for (int i = 0; i < data.length; i++) {
        String I_Tax_ID = data[i].iTaxId;
        String C_TaxCategory_ID = data[i].cTaxcategoryId;
        String C_BPTaxCategory_ID = data[i].cBpTaxcategoryId;
        String C_Tax_ID = data[i].cTaxId;
        String ParentTax_ID = data[i].parentTaxId;
        boolean newTaxCategory = (C_TaxCategory_ID == null || C_TaxCategory_ID.equals(""));
        boolean newBPTaxCategory = (C_BPTaxCategory_ID == null || C_BPTaxCategory_ID.equals(""));
        boolean newTax = (C_Tax_ID == null || C_Tax_ID.equals(""));
        con = conn.getTransactionConnection();

        // create/update TaxCategory
        if (!data[i].tcName.equals("") || !newTaxCategory) { // Inserting
          // taxes
          // this
          // shouldn't
          // be null,
          // but it
          // can be
          // if
          // inserting
          // only BP
          // Tax Cat
          if (newTaxCategory)
            C_TaxCategory_ID = ImportTaxData.selectTaxCategoryId(conn, data[i].tcName,
                getAD_Client_ID());
          newTaxCategory = (C_TaxCategory_ID == null || C_TaxCategory_ID.equals(""));
          if (log4j.isDebugEnabled())
            log4j.debug("TCId: " + C_TaxCategory_ID);
          if (newTaxCategory) { // Insert new TaxCategory
            C_TaxCategory_ID = SequenceIdData.getUUID();
            try {
              no = ImportTaxData.insertTaxCategory(con, conn, C_TaxCategory_ID, I_Tax_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Insert TaxCategory = " + no);
              noTCInsert++;
            } catch (ServletException ex) {
              String err = "Insert TaxCategory - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          } else { // Update existing TaxCategory
            try {
              no = ImportTaxData.updateTaxCategory(con, conn, I_Tax_ID, C_TaxCategory_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Update TaxCategory = " + no);
              noTCUpdate++;
            } catch (ServletException ex) {
              String err = "Update TaxCategory - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          }
        }

        // create/update BPTaxCategory
        if (!data[i].bptcName.equals("") || !newBPTaxCategory) {
          if (newBPTaxCategory)
            C_BPTaxCategory_ID = ImportTaxData.selectBPTaxCategoryId(conn, data[i].bptcName,
                getAD_Client_ID());
          newBPTaxCategory = (C_BPTaxCategory_ID == null || C_BPTaxCategory_ID.equals(""));
          if (newBPTaxCategory) { // Insert new BPTaxCategory
            C_BPTaxCategory_ID = SequenceIdData.getUUID();
            try {
              no = ImportTaxData.insertBPTaxCategory(con, conn, C_BPTaxCategory_ID, I_Tax_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Insert BPTaxCategory = " + no);
              noBPTCInsert++;
            } catch (ServletException ex) {
              String err = "Insert BPTaxCategory - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          } else { // Update existing BPTaxCategory
            try {
              no = ImportTaxData.updateBPTaxCategory(con, conn, I_Tax_ID, C_BPTaxCategory_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Update BPTaxCategory = " + no);
              noBPTCUpdate++;
            } catch (ServletException ex) {
              String err = "Update BPTaxCategory - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          }
        }

        if (!data[i].tName.equals("") || !newTax) { // Update Create Tax
          if (log4j.isDebugEnabled())
            log4j.debug("Tax:" + data[i].tName);

          if (!data[i].parentName.equals("")) { // Parent tax
            ParentTax_ID = ImportTaxData.selectTaxId(conn, "", data[i].parentName,
                getAD_Client_ID()); // Check
            // if
            // parent
            // tax
            // is
            // created,
            // if
            // not
            // create
            // with
            // default
            // values
            if (ParentTax_ID == null || ParentTax_ID.equals("")) { // Insert
              // parent
              // tax
              try {
                ParentTax_ID = SequenceIdData.getUUID();
                if (log4j.isDebugEnabled())
                  log4j.debug("Insert Parent Tax = " + no);
                no = ImportTaxData.insertDefaultTax(con, conn, ParentTax_ID, C_TaxCategory_ID,
                    I_Tax_ID);
                noTaxInsert++;
              } catch (ServletException ex) {
                String err = "Insert Parent Tax - " + ex.toString();
                if (log4j.isDebugEnabled())
                  log4j.debug(err);
                no = ImportTaxData.taxError(conn, err, I_Tax_ID);
                try {
                  conn.releaseRollbackConnection(con);
                } catch (Exception ignored) {
                }
                continue;
              }
            }
          }

          if (newTax)
            C_Tax_ID = ImportTaxData.selectTaxId(conn, data[i].parentName, data[i].tName,
                getAD_Client_ID()); // look for existing taxes
          // (a tax is unique by name
          // and parent)
          newTax = (C_Tax_ID == null || C_Tax_ID.equals(""));
          if (log4j.isDebugEnabled())
            log4j.debug("Tax:" + data[i].tName + " - new:" + newTax);
          if (newTax) { // Insert new Tax
            C_Tax_ID = SequenceIdData.getUUID();
            try {
              no = ImportTaxData.insertTax(con, conn, C_Tax_ID, C_TaxCategory_ID,
                  C_BPTaxCategory_ID, ParentTax_ID, I_Tax_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Insert Tax = " + no);
              noTaxInsert++;
            } catch (ServletException ex) {
              String err = "Insert Tax - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          } else { // Update existing Tax
            try {
              no = ImportTaxData.updateTax(con, conn, I_Tax_ID, C_TaxCategory_ID,
                  C_BPTaxCategory_ID, ParentTax_ID, C_Tax_ID);
              if (log4j.isDebugEnabled())
                log4j.debug("Update Tax = " + no);
              noTaxUpdate++;
            } catch (ServletException ex) {
              String err = "Update Tax - " + ex.toString();
              if (log4j.isDebugEnabled())
                log4j.debug(err);
              no = ImportTaxData.taxError(conn, err, I_Tax_ID);
              try {
                conn.releaseRollbackConnection(con);
              } catch (Exception ignored) {
              }
              continue;
            }
          }

          // Coutry-Region
          if ((!data[i].cCountryId.equals("") || !data[i].cRegionId.equals("")
              || !data[i].toCountryId.equals("0") || !data[i].toRegionId.equals("0"))
              && ImportTaxData.existsLocation(con, conn, I_Tax_ID, C_Tax_ID).equals("0")) {
            if (ImportTaxData.hasLocation(con, conn, C_Tax_ID).equals("0")) { // Update Location in
              // c_tax
              try {
                no = ImportTaxData.updateTaxRegion(con, conn, I_Tax_ID, C_Tax_ID);
                if (log4j.isDebugEnabled())
                  log4j.debug("Update Tax Location (in c_tax) = " + no);
              } catch (ServletException ex) {
                String err = "Update Tax Location (in c_tax)- " + ex.toString();
                if (log4j.isDebugEnabled())
                  log4j.debug(err);
                no = ImportTaxData.taxError(conn, err, I_Tax_ID);
                try {
                  conn.releaseRollbackConnection(con);
                } catch (Exception ignored) {
                }
                continue;
              }
            } else { // create Location in c_tax_zone
              try {
                String C_Tax_Zone_ID = SequenceIdData.getUUID();
                no = ImportTaxData.insertTaxZone(con, conn, C_Tax_Zone_ID, C_Tax_ID, I_Tax_ID);
                if (log4j.isDebugEnabled())
                  log4j.debug("Insert tax Location (in c_tax_zone) = " + no);
              } catch (ServletException ex) {
                String err = "Insert tax Location (in c_tax_zone)- " + ex.toString();
                if (log4j.isDebugEnabled())
                  log4j.debug(err);
                no = ImportTaxData.taxError(conn, err, I_Tax_ID);
                try {
                  conn.releaseRollbackConnection(con);
                } catch (Exception ignored) {
                }
                continue;
              }
            }
          }
        } // tax

        // Update I_Tax
        try {
          no = ImportTaxData.setImported(con, conn, C_TaxCategory_ID, I_Tax_ID);
          conn.releaseCommitConnection(con);
        } catch (Exception ex) {
          if (log4j.isDebugEnabled())
            log4j.debug("Update Imported - " + ex.toString());
          noTCInsert--;
          no = ImportTaxData.updateSetImportedError(conn, I_Tax_ID);
          try {
            conn.releaseRollbackConnection(con);
          } catch (Exception ignored) {
          }
          continue;
        }
      }

      // Set Error to indicator to not imported
      noTaxError = ImportTaxData.updateNotImported(conn, getAD_Client_ID());
    } catch (Exception se) {
      se.printStackTrace();
      addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
      return myError;
    }
    addLog(Utility.messageBD(conn, "Taxes not imported", vars.getLanguage()) + ": " + noTaxError
        + "; ");
    addLog("Tax inserted: " + noTaxInsert + "; ");
    addLog("Tax updated: " + noTaxUpdate + "; ");
    addLog("Tax Category inserted: " + noTCInsert + "; ");
    addLog("Tax Category updated: " + noTCUpdate + "; ");
    addLog("BPartner Tax Category inserted: " + noBPTCInsert + "; ");
    addLog("BPartner Tax Category updated: " + noBPTCUpdate);

    if (noTaxError == 0) {
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(conn, "Success", vars.getLanguage()));
    } else if (noTaxInsert > 0 || noTaxUpdate > 0 || noTCInsert > 0 || noTCUpdate > 0
        || noBPTCInsert > 0 || noBPTCUpdate > 0) {
      myError.setType("Warning");
      myError.setTitle(Utility.messageBD(conn, "Some taxes could not be imported", vars
          .getLanguage()));
    } else {
      myError.setType("Error");
      myError.setTitle(Utility.messageBD(conn, " No taxes could be imported", vars.getLanguage()));
    }
    myError.setMessage(Utility.messageBD(conn, getLog(), vars.getLanguage()));
    return myError;
  } // doIt
}
