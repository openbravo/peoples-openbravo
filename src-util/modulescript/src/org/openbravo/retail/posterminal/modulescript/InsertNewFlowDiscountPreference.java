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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.apache.log4j.Logger;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * 
 * @author malsasua
 * insert a new preference to versions updated lower than RR14Q3 version
 */
public class InsertNewFlowDiscountPreference extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(InsertDiscountPreference.class);

  @Override
  public void execute() {

    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists1 = InsertNewFlowDiscountPreferenceData.selectExistsControlPreference(cp);
      String exists2 = InsertNewFlowDiscountPreferenceData.selectIsNewInstance(cp);
      // if preference not exists and it is not a new instance then preference with value N is inserted
      // if preference not exists and it is a new instance then preference with value Y is inserted
      if (exists1.equals("0")) {
        if (exists2.equals("0")) {
    	  int prefs = InsertNewFlowDiscountPreferenceData.insert(cp, "Y");
          log4j.debug("Inserted " + prefs + " preference -Enable New Flow Discount: value Y-");
        } else {
          int prefs = InsertNewFlowDiscountPreferenceData.insert(cp, "N");
          log4j.debug("Inserted " + prefs + " preference -Enable New Flow Discount: value N-");         
        }
      } else {
        log4j.debug("No need to insert preference -open discount button-");
      }
      // insert control preferente to avoid that the preference "New Flow Discount module script executed"  is inserted in the future
      if (exists1.equals("0"))
      {
        InsertNewFlowDiscountPreferenceData.insertControlPreference(cp);
      }

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {

    // This method is provided for testing purposes.

    InsertNewFlowDiscountPreference t = new InsertNewFlowDiscountPreference();
    t.execute();
  }
}
