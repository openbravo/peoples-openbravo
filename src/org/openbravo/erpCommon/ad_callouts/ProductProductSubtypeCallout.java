/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.ProductSubtype;

/**
 * Callout that sets certain field values depending on the product subtype configuration
 */

public class ProductProductSubtypeCallout extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strProductSubtype = info.getStringParameter("inpmProductSubtypeId");
    ProductSubtype productSubtype = OBDal.getInstance()
        .get(ProductSubtype.class, strProductSubtype);

    if (productSubtype != null) {
      info.addResult("inpislinkedtoproduct",
          Boolean.TRUE.equals(productSubtype.isLinkableToProducts()) ? "Y" : "N");
      info.addResult("inpquantityRule", productSubtype.getQuantityRule());
      info.addResult("inpispricerulebased",
          Boolean.TRUE.equals(productSubtype.isPriceRuleBased()) ? "Y" : "N");
    }

  }
}
