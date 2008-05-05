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
package org.openbravo.erpCommon.ad_background;
import java.util.*;
import javax.servlet.*;
import java.io.*;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.base.secureApp.VariablesSecureApp;


public class PeriodicAcctServer implements BackgroundProcess {
  public String batchSize="50";
//  private final String[] TableIds = {"318","407","392","800019","319", "321", "323", "259", "224"};
 private String[] TableIds = null;

  public void processPL(PeriodicBackground periodicBG, boolean directProcess) throws Exception {
    String adNoteId = "";
    if(periodicBG.isDirectProcess())
    	periodicBG.addLog("@DL_STARTING@", false);
    else
    	periodicBG.addLog("Starting background process.");
    if (periodicBG.vars==null || periodicBG.adClientId.equals("")) {
      try {
        PeriodicAcctServerData[] dataOrg = PeriodicAcctServerData.selectUserOrg(periodicBG.conn, periodicBG.adProcessId);
        if (dataOrg==null || dataOrg.length==0) {
        	if(periodicBG.isDirectProcess())
        		periodicBG.addLog("@DL_LOAD_FAILED@", false);
        	else
        		periodicBG.addLog("User and Organization loading failed.");
          periodicBG.setProcessing(false);
          return;
        }
        periodicBG.vars = new VariablesSecureApp(dataOrg[0].adUserId, periodicBG.adClientId,dataOrg[0].adOrgId);
      } catch (ServletException ex) {
        ex.printStackTrace();
        return;
      }
    }

    try {
        PeriodicAcctServerData [] data = PeriodicAcctServerData.selectAcctTable(periodicBG.conn);
        ArrayList<Object> vTableIds = new ArrayList<Object>();
        for (int i=0;i<data.length;i++){
         vTableIds.add(data[i].adTableId);
        }
        TableIds = new String [vTableIds.size()];
        vTableIds.toArray(TableIds);
    } catch (ServletException ex) {
      ex.printStackTrace();
      return;
    }
    adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
    String[] tables=null;
    String strTable = "";
    if (directProcess) {
      strTable = PeriodicAcctServerData.selectTable(periodicBG.conn, periodicBG.adPinstanceId);
    }
    if (!strTable.equals("")) {
      tables = new String[1];
      tables[0] = new String(strTable);
    } else tables = TableIds;
    for (int i=0;i<tables.length;i++){
      periodicBG.doPause();
      AcctServer acct = AcctServer.get(tables[i], periodicBG.adClientId, periodicBG.conn);
      acct.setBatchSize(batchSize);
      int total = 0;
      while (acct.checkDocuments()) {
        periodicBG.doPause();
        if (total==0) {
        	if(periodicBG.isDirectProcess())
        		periodicBG.addLog("@DL_ACCOUNTING@ - "+ acct.tableName, false);
        	else
        		periodicBG.addLog("Accounting - " + acct.tableName, false);
        }
        else {
        	if(periodicBG.isDirectProcess())
        		periodicBG.addLog("@DL_COUNTED@ " + total + " - " + acct.tableName, false);
        	else
        		periodicBG.addLog("Counted " + total + " - " + acct.tableName, false);
        }
        try {
          acct.run(periodicBG.vars);
          periodicBG.doPause();
        } catch(IOException ex) {
          ex.printStackTrace();
          return;
        } catch(InterruptedException e) {
          return;
        }
        if (!periodicBG.canContinue(directProcess, periodicBG.adClientId)) {
        	if(periodicBG.isDirectProcess())
        		periodicBG.addLog("@DL_TABLE@ = "+acct.tableName + " - "  +acct.getInfo(periodicBG.vars), false);
        	else
        		periodicBG.addLog("Table = " + acct.tableName + " - "  +acct.getInfo(periodicBG.vars));
          adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
          return;
        }
        total += Integer.valueOf(batchSize).intValue();
      }
      if(periodicBG.isDirectProcess())
    	  periodicBG.addLog("@DL_TABLE@ = " + acct.tableName + " - " + acct.getInfo(periodicBG.vars), false);
      else
    	  periodicBG.addLog("Table = " + acct.tableName + " - " + acct.getInfo(periodicBG.vars));
      adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
    }
  }
}
