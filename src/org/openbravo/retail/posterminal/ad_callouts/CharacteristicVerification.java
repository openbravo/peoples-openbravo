/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.pricing.priceadjustment.Characteristic;

public class CharacteristicVerification extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String strCharacteristicId = info.getStringParameter("inpmCharacteristicId",
        IsIDFilter.instance);
    String strUseonwebpos = info.getStringParameter("inpemObposUseonwebpos", null);
    String strOffers = getRelatedDiscounts(info.vars, strCharacteristicId);
    if (strUseonwebpos.equalsIgnoreCase("N") && !StringUtils.isEmpty(strOffers)) {
      Map<String, String> map = new HashMap<String, String>();
      map.put("product", strOffers);
      info.addResult("WARNING",
          OBMessageUtils.parseTranslation(OBMessageUtils.messageBD("OBPOS_CALLOUT_CHAR"), map));
    }

  }

  /**
   * 
   * @param vars
   *          Variables from the callout
   * @param strCharacteristicId
   *          The Id of the characteristic to search related Discounts and Promotions
   */
  private String getRelatedDiscounts(VariablesSecureApp vars, String strCharacteristicId) {

    final org.openbravo.model.common.plm.Characteristic characteristics = OBDal.getInstance().get(
        org.openbravo.model.common.plm.Characteristic.class, strCharacteristicId);

    String names = new String();
    for (Characteristic offercharactheristic : characteristics
        .getPricingAdjustmentCharacteristicList()) {
      names = names + " " + offercharactheristic.getOffer().getName() + ",";
    }
    names = names.replaceAll(",$", "");

    return names;
  }
}