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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.buildvalidation;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.database.ConnectionProvider;

/**
 * This validation is related to this issue: https://issues.openbravo.com/view.php?id=12277 
 * A new check constrain was added to ensure that whenever a tax is defined usinga base amount 
 * which depends on a tax base, this tax base is not null
 */
public class CTaxTaxbase extends BuildValidation {

  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    ArrayList<String> errors = new ArrayList<String>();
    try {
      int a = Integer.parseInt(CTaxTaxbaseData.count(cp));
      if (a > 0) {
        errors
            .add("You can not apply this MP because your instance fails in a pre-validation: Some taxes are not properly configured. Please ensure taxe base field contains a value as it is mandatory for taxes which tax base amount is one of the following: Alternate Tax Base Amount + Tax Amount, Line Net Amount + Tax Amount or Tax Amount. To fix this problem in your instance, you can know the wrong entries by reviewing Alerts in your system (Alert Rule: Wrong tax rate definition). Once you find the wrong entries you should fix the wrong ones. Once it is fixed you should be able to apply this MP.");
        String alertRuleId = CTaxTaxbaseData.getUUID(cp);
        if (CTaxTaxbaseData.existsAlertRule(cp).equals("0")) {
          CTaxTaxbaseData.insertAlertRule(cp, alertRuleId);
          processAlert(alertRuleId, cp);
        }
      }
    } catch (Exception e) {
      return handleError(e);
    }
    return errors;
  }

  /**
   * @param alertRule
   * @param conn
   * @throws Exception
   */
  private void processAlert(String adAlertruleId, ConnectionProvider cp) throws Exception {
    CTaxTaxbaseData[] alertRule = CTaxTaxbaseData.select(cp, adAlertruleId);
    CTaxTaxbaseData[] alert = null;
    if (!alertRule[0].sql.equals("")) {
      try {
        alert = CTaxTaxbaseData.selectAlert(cp, alertRule[0].sql);
      } catch (Exception ex) {
        return;
      }
    }
    // Insert
    if (alert != null && alert.length != 0) {
      StringBuilder msg = new StringBuilder();
      ;

      for (int i = 0; i < alert.length; i++) {
        if (CTaxTaxbaseData.existsReference(cp, adAlertruleId, alert[i].referencekeyId).equals(
            "0")) {
          CTaxTaxbaseData.insertAlert(cp, alert[i].description, alertRule[0].adAlertruleId,
              alert[i].recordId, alert[i].referencekeyId);
        }
      }
    }
  }

}
