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
      boolean execute= InitializeAcctDimensionsInClientData.isExecuted(cp);
      if (execute){
        System.out.println("in");
      //Business Partner
      String ismandatory = InitializeAcctDimensionsInClientData.selectbph(cp);
      InitializeAcctDimensionsInClientData.initializebph(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectbpl(cp);
      InitializeAcctDimensionsInClientData.initializebpl(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectbpbd(cp);
      InitializeAcctDimensionsInClientData.initializebpbd(cp,ismandatory);   
      
      //Product
      ismandatory = InitializeAcctDimensionsInClientData.selectprh(cp);
      InitializeAcctDimensionsInClientData.initializeprh(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectprl(cp);
      InitializeAcctDimensionsInClientData.initializeprl(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectprbd(cp);
      InitializeAcctDimensionsInClientData.initializeprbd(cp,ismandatory);  
      
      //Project
      ismandatory = InitializeAcctDimensionsInClientData.selectpjh(cp);
      InitializeAcctDimensionsInClientData.initializepjh(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectpjl(cp);
      InitializeAcctDimensionsInClientData.initializepjl(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectpjbd(cp);
      InitializeAcctDimensionsInClientData.initializepjbd(cp,ismandatory);  
     
      //Cost Center
      ismandatory = InitializeAcctDimensionsInClientData.selectcch(cp);
      InitializeAcctDimensionsInClientData.initializecch(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectccl(cp);
      InitializeAcctDimensionsInClientData.initializeccl(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectccbd(cp);
      InitializeAcctDimensionsInClientData.initializeccbd(cp,ismandatory);  
     
      //User 1
      ismandatory = InitializeAcctDimensionsInClientData.selectu1h(cp);
      InitializeAcctDimensionsInClientData.initializeu1h(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectu1l(cp);
      InitializeAcctDimensionsInClientData.initializeu1l(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectu1bd(cp);
      InitializeAcctDimensionsInClientData.initializeu1bd(cp,ismandatory);  
      
      //User 2
      ismandatory = InitializeAcctDimensionsInClientData.selectu2h(cp);
      InitializeAcctDimensionsInClientData.initializeu2h(cp,ismandatory);     
      ismandatory = InitializeAcctDimensionsInClientData.selectu2l(cp);
      InitializeAcctDimensionsInClientData.initializeu2l(cp,ismandatory);   
      ismandatory = InitializeAcctDimensionsInClientData.selectu2bd(cp);
      InitializeAcctDimensionsInClientData.initializeu2bd(cp,ismandatory);  
      
      int a = InitializeAcctDimensionsInClientData.updatePreferencetest(cp);
      System.out.println("updated: " + a);
      }
      
      
    } catch (Exception e) {
      handleError(e);
    }
  }
}
