/*
************************************************************************************
* Copyright (C) 2020 Openbravo S.L.U.
* Licensed under the Openbravo Commercial License version 1.0
* You may obtain a copy of the License at
http://www.openbravo.com/legal/obcl.html
* or in the legal folder of this module distribution.
************************************************************************************
*/
package org.openbravo.retail.posterminal.buildvalidation;

import java.util.ArrayList;
import java.util.List;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

/**
 * This validation is related to refactor product and services to be an indexDB model
 * 
 */
public class DuplicatedServicesPriceRuleVersion extends BuildValidation {

  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public List<String> execute() {
    try {
      List<String> errors = new ArrayList<String>();
      ConnectionProvider cp = getConnectionProvider();
      DuplicatedServicesPriceRuleVersionData[] servicePriceRuleVersionMax = DuplicatedServicesPriceRuleVersionData.duplicatedServicesPriceRuleMax(cp);
      for (int i = 0; i < servicePriceRuleVersionMax.length; i++) {
        String msg = "\nPlease, review the configuration of Services Price Rule maximun defined for product "
            + servicePriceRuleVersionMax[i].name
            + " . Only one service price rule is allowed for each product.";
        errors.add(msg);
      }
      DuplicatedServicesPriceRuleVersionData[] servicePriceRuleVersionMin = DuplicatedServicesPriceRuleVersionData.duplicatedServicesPriceRuleMin(cp);
      for (int i = 0; i < servicePriceRuleVersionMin.length; i++) {
        String msg = "\nPlease, review the configuration of Services Price Rule minimun defined for product "
            + servicePriceRuleVersionMin[i].name
            + " . Only one service price rule is allowed for each product.";
        errors.add(msg);
      }
     
      return errors;
    } catch (Exception e) {
      return handleError(e);
    }
  }

  @Override
  protected boolean executeOnInstall() {
    return false;
  }
  @Override
  protected ExecutionLimits getBuildValidationLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 5000));
  }
}