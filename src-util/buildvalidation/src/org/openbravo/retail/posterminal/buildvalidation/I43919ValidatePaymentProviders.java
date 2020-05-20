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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.base.ExecutionLimits;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.OpenbravoVersion;

public class I43919ValidatePaymentProviders extends BuildValidation {

  private static final String RETAIL_POSTERMINAL_MODULE_ID = "FF808181326CC34901326D53DBCF0018";

  @Override
  public List<String> execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      Collection<String> errors;
      if (I43919ValidatePaymentProvidersData.existsWrongPaymentMethod(cp)) {
        errors = Arrays.asList("There are Payment Methods in the Channel - Touchpoint Type with a Payment Provider but no Payment Method Type. "
            + "All Payment Methods with a Payment Provider must have defined a corresponding Payment Method Type. "
            + "Consider reseting the configuration with the following SQL command: "
            + "UPDATE OBPOS_APP_PAYMENT_TYPE SET OBPOS_PAYMENTGROUP_ID = NULL WHERE NOT(OBPOS_PAYMENTGROUP_ID IS NULL OR OBPOS_PAYMENTGROUP_ID = '0' OR OBPOS_PAYMENTMETHOD_TYPE_ID IS NOT NULL)");
      } else {
        errors = Collections.emptyList();
      }
      
      return new ArrayList<String>(errors);
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
    // From RR18Q3 to RR20Q2
    return new ExecutionLimits(RETAIL_POSTERMINAL_MODULE_ID, new OpenbravoVersion(1, 2, 5501), new OpenbravoVersion(1, 2, 6901));
  }
}