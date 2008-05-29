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
import javax.servlet.*;
import org.apache.log4j.Logger ;

// imports for transactions
import org.openbravo.database.ConnectionProvider;
import java.sql.Connection;


public class ImportBPartner extends ImportProcess {
  static Logger log4j = Logger.getLogger(ImportBPartner.class);

  private String m_AD_Process_ID = "";
  private String m_Record_ID = "";
  private boolean m_deleteOldImported;

  public ImportBPartner(ConnectionProvider conn, String AD_Process_ID, String recordId, boolean deleteOld) {
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
    if (log4j.isDebugEnabled()) log4j.debug("Creating parameters");
  }

  protected OBError doIt(VariablesSecureApp vars) throws ServletException {
    int no = 0;
    ConnectionProvider conn = null;
    Connection con = null;
    OBError myError = new OBError();
    
		try {
            conn = getConnection();
			con = conn.getTransactionConnection();
			//  Set Client, Org, IaActive, Created/Updated, ProductType
			no = ImportBPartnerData.updateRecords(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner Reset = " + no);

			//  Set BPGroup
			no = ImportBPartnerData.updateBPGroupDefault(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner BPGroup = " + no);
			no = ImportBPartnerData.updateBPGroupId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner BPGroupId = " + no);
			no = ImportBPartnerData.updateGroupError(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("Invalid BPartner group = " + no);

			//  Country
			no = ImportBPartnerData.updateCountryCodeDefault(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner CountryCode = " + no);
			no = ImportBPartnerData.updateCountryId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner CountryId = " + no);
			no = ImportBPartnerData.updateCountryError(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("Invalid Country = " + no);

			//  Region
			no = ImportBPartnerData.updateRegionDefault(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner Region = " + no);
			no = ImportBPartnerData.updateRegionId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner RegionId = " + no);
			no = ImportBPartnerData.updateRegionError(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("Invalid Region = " + no);
			
			//  Greeting
			no = ImportBPartnerData.updateGreetingId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner GreetingId = " + no);
			no = ImportBPartnerData.updateGreetingError(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("Invalid Greeting = " + no);
			
			//  BPartner
			no = ImportBPartnerData.updateBPartnerId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner BPartnerId = " + no);
			
			//  ADUser
			no = ImportBPartnerData.updateADUserId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner ADUserId = " + no);

			//  Location
			no = ImportBPartnerData.updateLocationId(con, conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("ImportBPartner LocationId = " + no);

			conn.releaseCommitConnection(con);
		} catch (Exception se) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {}
			se.printStackTrace();
			addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
			myError.setType("Error");
			myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
			myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
			return myError;
		}

    // till here, the edition of the I_ImportBPartner table
    // now, the insertion from I_ImportBPartner table in C_BPartner...

    int noInsert = 0;
    int noUpdate = 0;
    int noBPartnerError = 0;

		try {
			//  Go through Records
			ImportBPartnerData[] data = ImportBPartnerData.select(conn, getAD_Client_ID());
			if (log4j.isDebugEnabled()) log4j.debug("Going through " + data.length + " records");
			for(int i=0;i<data.length;i++){
				String I_BPartner_ID = data[i].iBpartnerId;
				String C_BPartner_ID = data[i].cBpartnerId;
				boolean newBPartner = C_BPartner_ID == "";
				String C_BPartner_Location_ID = data[i].cBpartnerLocationId;
				boolean newLocation = data[i].addr != null;
				String AD_User_ID = data[i].adUserId;
				boolean newContact =  data[i].contactname != null;
				con = conn.getTransactionConnection();

				//	create/update BPartner
				if (newBPartner) {	//	Insert new BPartner
					C_BPartner_ID = SequenceIdData.getSequence(conn, "C_BPartner", vars.getClient());
					try {
						no = ImportBPartnerData.insertBPartner(con, conn, C_BPartner_ID, I_BPartner_ID);
						if (log4j.isDebugEnabled()) log4j.debug("Insert BPartner = " + no);
						noInsert++;
					} catch (ServletException ex)	{
						if (log4j.isDebugEnabled()) log4j.debug("Insert BPartner - " + ex.toString());
						no = ImportBPartnerData.insertBPartnerError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
				} else {			//	Update existing BPartner
					try	{
						no = ImportBPartnerData.updateBPartner(con, conn, I_BPartner_ID, C_BPartner_ID);
						if (log4j.isDebugEnabled()) log4j.debug("Update BPartner = " + no);
						noUpdate++;
					}	catch (ServletException ex)	{
						if (log4j.isDebugEnabled()) log4j.debug("Update BPartner - " + ex.toString());
						no = ImportBPartnerData.updateBPartnerError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
				}

				//	create/update BPartner Location
				if (C_BPartner_Location_ID != "")	{	//	Update Location
				}	else if (newLocation)	{				//	New Location
					String C_Location_ID = SequenceIdData.getSequence(conn, "C_Location", vars.getClient());
					try {
						no = ImportBPartnerData.insertLocation(con, conn, C_Location_ID, I_BPartner_ID);
						if (log4j.isDebugEnabled()) log4j.debug("Insert Location = " + no);
					} catch (ServletException ex) {
						if (log4j.isDebugEnabled()) log4j.debug("Insert Location - " + ex.toString());
						noInsert--;
						no = ImportBPartnerData.insertLocationError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
					C_BPartner_Location_ID = SequenceIdData.getSequence(conn, "C_BPartner_Location", vars.getClient());
					try	{
					  String locationName = parseAddressName(data[i]);
					  
						no = ImportBPartnerData.insertBPLocation(con, conn, C_BPartner_Location_ID, locationName, C_BPartner_ID, C_Location_ID, I_BPartner_ID);
						if (log4j.isDebugEnabled()) log4j.debug("Insert BP Location = " + no);
					}	catch (ServletException ex)	{
						if (log4j.isDebugEnabled()) log4j.debug("Insert BP Location - " + ex.toString());
						noInsert--;
						no = ImportBPartnerData.insertBPLocationError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
				}

				//	Create/Update Contact
				if (AD_User_ID != "") {
					try	{
						no = ImportBPartnerData.updateBPContact(con, conn, I_BPartner_ID, AD_User_ID);
						if (log4j.isDebugEnabled()) log4j.debug("Update BP Contact = " + no);
					}	catch (ServletException ex)	{
						if (log4j.isDebugEnabled()) log4j.debug("Update BP Contact - " + ex.toString());
						noInsert--;
						no = ImportBPartnerData.updateBPContactError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
				}	else if (newContact)	{				//	New Contact
					AD_User_ID = SequenceIdData.getSequence(conn, "AD_User", vars.getClient());
					try
					{
						if (data[i].contactname != null && data[i].contactname != "") {
							no = ImportBPartnerData.insertBPContact(con, conn, AD_User_ID, C_BPartner_ID, (Integer.valueOf(C_BPartner_Location_ID).intValue() == 0)?"NULL":C_BPartner_Location_ID, I_BPartner_ID);
							if (log4j.isDebugEnabled()) log4j.debug("Insert BP Contact = " + no);
						} else {
							AD_User_ID = "0";
						}
					} catch (ServletException ex)	{
						if (log4j.isDebugEnabled()) log4j.debug("Insert BP Contact - " + ex.toString());
						noInsert--;
						no = ImportBPartnerData.insertBPContactError(con, conn, I_BPartner_ID);
						conn.releaseRollbackConnection(con);
						continue;
					}
				}

				//	Update I_BPARTNER
				try {
					no = ImportBPartnerData.setImported(con, conn, C_BPartner_ID, (Integer.valueOf(C_BPartner_Location_ID).intValue() == 0)?"":C_BPartner_Location_ID, (Integer.valueOf(AD_User_ID).intValue() == 0)?"":AD_User_ID, I_BPartner_ID);
					conn.releaseCommitConnection(con);
				} catch (ServletException ex) {
					if (log4j.isDebugEnabled()) log4j.debug("Update Imported - " + ex.toString());
					noInsert--;
					no = ImportBPartnerData.updateSetImportedError(con, conn, I_BPartner_ID);
					conn.releaseRollbackConnection(con);
					continue;
				}
			}

			//  Set Error to indicator to not imported
			noBPartnerError = ImportBPartnerData.updateNotImported(conn, getAD_Client_ID());
			// Delete imported 
			if(m_deleteOldImported) {
				no = ImportBPartnerData.deleteOld(conn, getAD_Client_ID());
				if (log4j.isDebugEnabled()) log4j.debug("Delete Old Imported = " + no);
			}
		} catch (Exception se) {
			se.printStackTrace();
			addLog(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
			myError.setType("Error");
			myError.setTitle(Utility.messageBD(conn, "Error", vars.getLanguage()));
			myError.setMessage(Utility.messageBD(conn, "ProcessRunError", vars.getLanguage()));
			return myError;
		}
    addLog(Utility.messageBD(conn, "Errors", vars.getLanguage()) + ": " + noBPartnerError + "; ");
    addLog("BPartner inserted: " + noInsert + "; ");
    addLog("BPartner updated: " + noUpdate);
    myError.setType("Success");  
    myError.setTitle(Utility.messageBD(conn, "Success", vars.getLanguage()));
    myError.setMessage(Utility.messageBD(conn, getLog(), vars.getLanguage()));
    return myError;

  } // doIt
  
  private String parseAddressName(ImportBPartnerData addressData) {
    String result = ".";
    
    String locationNameA = "";
    if (!addressData.city.equals("") && !addressData.city.equals(null)) {
      locationNameA = addressData.city;
    } else if (!addressData.regionname.equals("") && !addressData.regionname.equals(null)) {
      locationNameA = addressData.regionname;
    } else if (!addressData.postal.equals("") && !addressData.postal.equals(null)) {
      locationNameA = addressData.postal;
    }

    String locationNameB = "";
    if (!addressData.address1.equals("") && !addressData.address1.equals(null)) locationNameB = addressData.address1;
    else if (!addressData.address2.equals("") && !addressData.address2.equals(null)) locationNameB = addressData.address2;
    
    StringBuffer locationName = new StringBuffer();
    if (!locationNameA.equals("")) {
      locationName.append(locationNameA);
      if (!locationNameB.equals("")) {
        locationName.append(", " + locationNameB);
        result =  locationName.toString();
      } else result = locationName.toString();
    } else if (!locationNameB.equals("")) {
        locationName.append(locationNameB);
        result = locationName.toString();
      }
    return result;
  }

}
