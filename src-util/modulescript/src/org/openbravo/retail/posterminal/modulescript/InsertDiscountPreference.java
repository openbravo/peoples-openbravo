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
 * @author adrian
 * insert a new preference to versions updated lower than 1.2.750 version
 */
public class InsertDiscountPreference extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(InsertDiscountPreference.class);

  @Override
  public void execute() {

    try {
      ConnectionProvider cp = getConnectionProvider();

      String exists1 = InsertDiscountPreferenceData.selectExistsControlPreference(cp);
      String exists2 = InsertDiscountPreferenceData.selectIsNewInstance(cp);
      // if preference not exists and it is not a new instance then preference "discount to button" is inserted
      if (exists1.equals("0") && !exists2.equals("0")) {
    	int prefs = InsertDiscountPreferenceData.insert(cp);
        log4j.debug("Inserted " + prefs + " preference -open discount button-");  
      } else {
        log4j.debug("No need to insert preference -open discount button-");
      }
      // insert control preferente to avoid that the preference "discount to button"  is inserted in the future
      if (exists1.equals("0"))
      {
        InsertDiscountPreferenceData.insertControlPreference(cp);
      }

    } catch (Exception e) {
      handleError(e);
    }
  }

  public static void main(String[] args) {

    // This method is provided for testing purposes.

	InsertDiscountPreference t = new InsertDiscountPreference();
    t.execute();
  }
}
