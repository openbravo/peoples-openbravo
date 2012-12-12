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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.utils.FormatUtilities;

public class InitializeAcctDimensionsInClient extends ModuleScript {


  @Override
  public void execute() {

    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean isInitialized= InitializeAcctDimensionsInClientData.isExecuted(cp);
      if (!isInitialized){
        for (InitializeAcctDimensionsInClientData client : InitializeAcctDimensionsInClientData
            .getClients(cp)) {
      InitializeAcctDimensionsInClientData.updateDimClient(cp);  
      InitializeAcctDimensionsInClientData.updatebpari(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebparirm(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpesh(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpmmr(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpsoo(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpmms(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebparr(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpapc(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpfat(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpapp(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebparf(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebparc(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpbgt(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpamz(cp, client.adClientId);  
      InitializeAcctDimensionsInClientData.updatebpapi(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatebpglj(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatepresh(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprarirm(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprapi(cp, client.adClientId);  
      InitializeAcctDimensionsInClientData.updateprglj(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updatepramz(cp, client.adClientId);      
      InitializeAcctDimensionsInClientData.updateprarc(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprarf(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprbgt(cp, client.adClientId); 
      InitializeAcctDimensionsInClientData.updateprapp(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprfat(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprapc(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprpoo(cp, client.adClientId);   
      InitializeAcctDimensionsInClientData.updateprmms(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprarr(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprsoo(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateprmmr(cp, client.adClientId);   
      InitializeAcctDimensionsInClientData.updateprmmi(cp, client.adClientId);    
      InitializeAcctDimensionsInClientData.updateooamz(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooapc(cp, client.adClientId);      
      InitializeAcctDimensionsInClientData.updateooapi(cp, client.adClientId);     
      InitializeAcctDimensionsInClientData.updateooapp(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooarc(cp, client.adClientId); 
      InitializeAcctDimensionsInClientData.updateooarf(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooari(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooarirm(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooarr(cp, client.adClientId);        
      InitializeAcctDimensionsInClientData.updateoobgt(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooesh(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateooglj(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateoommi(cp, client.adClientId);   
      InitializeAcctDimensionsInClientData.updateoommm(cp, client.adClientId);      
      InitializeAcctDimensionsInClientData.updateoommr(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateoomms(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateoopoo(cp, client.adClientId);
      InitializeAcctDimensionsInClientData.updateoorec(cp, client.adClientId);   
      InitializeAcctDimensionsInClientData.updateoosoo(cp, client.adClientId); 
      
      InitializeAcctDimensionsInClientData.createPreference(cp,client.adClientId);
        }
      }
     
    } catch (Exception e) {
      handleError(e);
    }
  }
}
