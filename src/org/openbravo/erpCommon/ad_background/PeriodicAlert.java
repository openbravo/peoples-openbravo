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
 * Contributions are Copyright (C) 2001-2008 Openbravo S.L.
 ******************************************************************************
*/
package org.openbravo.erpCommon.ad_background;

import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.businessUtility.EMail;
import org.openbravo.erpCommon.utility.Utility;


public class PeriodicAlert implements BackgroundProcess {
  public String batchSize="50";
  static int counter = 0;  // This variable might be used in order to define how often an alert is going to be executed.


  private void processAlert(PeriodicAlertData alertRule, PeriodicBackground periodicBG) throws Exception{
    periodicBG.addLog("processing rule "+alertRule.name);
    PeriodicAlertData [] alert = null;
    if(!alertRule.sql.equals("")) {
	    try {
	      alert = PeriodicAlertData.selectAlert(periodicBG.conn, alertRule.sql);
	    } catch (Exception ex) {
	      periodicBG.addLog("Error processing: "+ex.toString());
	      return;
	    }
    }
    //insert
    if (alert!=null && alert.length!=0) {
      int insertions = 0;
      String msg = "";
      for (int i=0; i<alert.length; i++){
        if (PeriodicAlertData.existsReference(periodicBG.conn, alertRule.adAlertruleId, alert[i].referencekeyId).equals("0")) {
          String adAlertId = SequenceIdData.getSequence(periodicBG.conn, "AD_Alert", alert[i].adClientId);
          periodicBG.addLog("inserting alert "+adAlertId+" org:"+alert[i].adOrgId+" client:"+alert[i].adClientId+" reference key:"+alert[i].referencekeyId+" created"+alert[i].created);
          PeriodicAlertData.InsertAlert(periodicBG.conn, adAlertId, alert[i].adClientId, alert[i].adOrgId,
                                                         alert[i].created, alert[i].createdby,
                                                         alertRule.adAlertruleId, alert[i].recordId, alert[i].referencekeyId,
                                                         alert[i].description, alert[i].adUserId, alert[i].adRoleId);
          
         // periodicBG.addLog("alert inserted record:"+alert[i].record);
         insertions ++;
         msg += "\n\nAlert: "+alert[i].description+"\nRecord: "+alert[i].recordId;
        }
      }
      periodicBG.addLog("inserted alerts "+insertions);
      if (insertions>0) {
       //sendmail
        PeriodicAlertData[] mail =PeriodicAlertData.prepareMails(periodicBG.conn, alertRule.adAlertruleId);
        if (mail!=null) {
          for (int i=0; i<mail.length; i++) {
            String head = Utility.messageBD(periodicBG.conn, "AlertMailHead", mail[i].adLanguage)+"\n";
            EMail email = new EMail(null, mail[i].smtphost, mail[i].mailfrom, mail[i].mailto, "[OB Alert] "+alertRule.name, head+msg);
            email.setEMailUser(mail[i].requestuser, mail[i].requestuserpw);
            if("OK".equals(email.send())) periodicBG.addLog("mail sent ok");
            else periodicBG.addLog("error sending mail");
          }
        }
      }
    }
    
    //update
    if(!alertRule.sql.equals("")) {
	    try {
	      Integer count = PeriodicAlertData.updateAlert(periodicBG.conn, alertRule.adAlertruleId, alertRule.sql);
	      periodicBG.addLog("updated alerts: "+count);
	    } catch (Exception ex) {
	      periodicBG.addLog("Error updating: "+ex.toString());
	    }
    }
    
  }

  public void processPL(PeriodicBackground periodicBG, boolean directProcess) throws Exception {
    counter ++;
    periodicBG.addLog("Starting Alert Backgrouond Process. Loop "+counter);
    
    PeriodicAlertData [] alertRule = PeriodicAlertData.selectSQL(periodicBG.conn);
    //periodicBG.addLog("alertRule.length: "+alertRule.length);
    if (alertRule!=null && alertRule.length!=0) {
      for (int i=0; i<alertRule.length; i++) {
      //  periodicBG.addLog("sql: "+alertRule[i].sql);
        processAlert(alertRule[i], periodicBG);
        
        periodicBG.doPause();
      }
    }
   /* if (periodicBG.vars==null || periodicBG.adClientId.equals("")) {
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
        if (total==0) periodicBG.addLog("Contabilizando - " + acct.tableName, false);
        else periodicBG.addLog("Contabilizados " + total + " - " + acct.tableName, false);
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
          periodicBG.addLog("Table=" + acct.tableName + " - "  +acct.getInfo(periodicBG.vars));
          adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
          return;
        }
        total += Integer.valueOf(batchSize).intValue();
      }
      periodicBG.addLog("Table=" + acct.tableName + " - " + acct.getInfo(periodicBG.vars));
      adNoteId = periodicBG.saveLog(adNoteId, periodicBG.adClientId);
    }*/
  }
}
