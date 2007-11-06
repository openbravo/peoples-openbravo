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
    periodicBG.addLog("Iniciando proceso contable background.");
    if (periodicBG.vars==null || periodicBG.adClientId.equals("")) {
      try {
        PeriodicAcctServerData[] dataOrg = PeriodicAcctServerData.selectUserOrg(periodicBG.conn, periodicBG.adProcessId);
        if (dataOrg==null || dataOrg.length==0) {
          periodicBG.addLog("La carga de organización y usuario a fallado.");
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
      AcctServer acct = AcctServer.get(tables[i], periodicBG.adClientId);
      acct.setBatchSize(batchSize);
      int total = 0;
      while (acct.checkDocuments()) {
        periodicBG.doPause();
        if (total==0) periodicBG.addLog("Contabilizando - " + AcctServer.tableName, false);
        else periodicBG.addLog("Contabilizados " + total + " - " + AcctServer.tableName, false);
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
          periodicBG.addLog("Table=" + AcctServer.tableName + " - "  +acct.getInfo(periodicBG.vars));
          adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
          return;
        }
        total += Integer.valueOf(batchSize).intValue();
      }
      periodicBG.addLog("Table=" + AcctServer.tableName + " - " + acct.getInfo(periodicBG.vars));
      adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
    }
  }
}
