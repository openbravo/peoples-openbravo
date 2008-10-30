package org.openbravo.erpCommon.ad_process;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.EMail;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.utils.FormatUtilities;
import org.quartz.JobExecutionException;

public class AlertProcess implements Process {
  
  static int counter = 0;
  
  private ConnectionProvider connection;
  private ProcessLogger logger;
  
  public void initialize(ProcessBundle bundle) {
    logger = bundle.getLogger();
    connection = bundle.getConnection();
  }
  
  public void execute(ProcessBundle bundle) throws Exception {
    logger.log("Starting Alert Backgrouond Process. Loop "+ counter + "\n");
    
    try {
      AlertProcessData [] alertRule = AlertProcessData.selectSQL(connection);
      if (alertRule != null && alertRule.length != 0) {
      
        for (int i = 0; i < alertRule.length; i++) {
          processAlert(alertRule[i],connection);
        }
      }
    } catch (Exception e) {
      throw new JobExecutionException(e.getMessage(), e);
    }
  }
  
  /**
   * @param alertRule
   * @param connection
   * @throws Exception
   */
  private void processAlert(AlertProcessData alertRule, ConnectionProvider connection) 
      throws Exception{
    logger.log("Processing rule "+ alertRule.name + "\n");
    
    AlertProcessData [] alert = null;
    
    if(!alertRule.sql.equals("")) {
      try {
        alert = AlertProcessData.selectAlert(connection, alertRule.sql);
      } catch (Exception ex) {
        logger.log("Error processing: " + ex.getMessage() + "\n");
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      int insertions = 0;
      StringBuilder msg = new StringBuilder();;
      
      for (int i = 0; i < alert.length; i++){
        if (AlertProcessData.existsReference(connection, 
            alertRule.adAlertruleId, alert[i].referencekeyId).equals("0")) {
          
          String adAlertId = 
            SequenceIdData.getUUID();
          
          logger.log("Inserting alert " + adAlertId + " org:" + alert[i].adOrgId + 
              " client:" + alert[i].adClientId + " reference key: " + 
              alert[i].referencekeyId + " created" + alert[i].created + "\n");
          
          AlertProcessData.InsertAlert(connection, adAlertId, 
              alert[i].adClientId, alert[i].adOrgId, alert[i].created, 
              alert[i].createdby, alertRule.adAlertruleId, alert[i].recordId, 
              alert[i].referencekeyId, alert[i].description, alert[i].adUserId, 
              alert[i].adRoleId);
         insertions ++;
         
         msg.append("\n\nAlert: " + alert[i].description + "\nRecord: " + 
             alert[i].recordId);
        }
      }
     
      if (insertions > 0) {
        //Send mail
        AlertProcessData[] mail = 
          AlertProcessData.prepareMails(connection, alertRule.adAlertruleId);
        
        if (mail!=null) {
          for (int i=0; i<mail.length; i++) {
            String head = Utility.messageBD(
                connection, "AlertMailHead", mail[i].adLanguage) + "\n";
            EMail email = new EMail(null, mail[i].smtphost, mail[i].mailfrom, 
                mail[i].mailto, "[OB Alert] " + alertRule.name, head + msg);
            String pwd = 
              FormatUtilities.encryptDecrypt(mail[i].requestuserpw, false);
            email.setEMailUser(mail[i].requestuser, pwd);
            if("OK".equals(email.send())) {
              logger.log("Mail sent ok.");
            }
            else {
              logger.log("Error sending mail.");
            }
          }
        }
      }
    }
    
    // Update
    if(!alertRule.sql.equals("")) {
      try {
        Integer count = AlertProcessData.updateAlert(
            connection, alertRule.adAlertruleId, alertRule.sql);
        logger.log("updated alerts: " + count + "\n");
      
      } catch (Exception ex) {
        logger.log("Error updating: " + ex.toString()  + "\n");
      }
    }
  }

}