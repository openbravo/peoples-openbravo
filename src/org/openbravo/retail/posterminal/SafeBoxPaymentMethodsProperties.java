/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(SafeBoxes.safeBoxesPaymentMethodsPropertyExtension)
public class SafeBoxPaymentMethodsProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("sfpm.id", "safeBoxPaymentMethodId"));
        add(new HQLProperty("sfpm.fINFinancialaccount.id", "financialAccountId"));
        add(new HQLProperty("sfpm.paymentMethod.id", "paymentMethodId"));
        add(new HQLProperty("sfpm.fINFinancialaccount.currency.id", "currency"));
        add(new HQLProperty("sfpm.cash", "isCash"));
        add(new HQLProperty("sfpm.countCash", "countCash"));
        add(new HQLProperty("sfpm.automateMovementToOtherAccount",
            "automateMovementToOtherAccount"));
        add(new HQLProperty("sfpm.keepFixedAmount", "keepFixedAmount"));
        add(new HQLProperty("sfpm.amount", "amount"));
        add(new HQLProperty("sfpm.allowVariableAmount", "allowVariableAmount"));
        add(new HQLProperty("sfpm.allowNotToMove", "allowNotToMove"));
        add(new HQLProperty("sfpm.allowMoveEverything", "allowMoveEverything"));
        add(new HQLProperty("sfpm.countDifferenceLimit", "countDifferenceLimit"));
      }
    };

    return list;
  }

}
