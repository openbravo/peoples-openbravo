/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.config.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.plm.Product;

public class AssortmentProduct extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String productId = info.getStringParameter("inpmProductId", null);
    Product product = OBDal.getInstance().get(Product.class, productId);
    if (product != null && (!product.isStocked() || !"I".equals(product.getProductType()))) {
      info.vars.setSessionValue(info.getWindowId() + "|DISPLAY_DISCONTINUED", "N");
    } else {
      info.vars.setSessionValue(info.getWindowId() + "|DISPLAY_DISCONTINUED", "Y");
    }
  }

}
