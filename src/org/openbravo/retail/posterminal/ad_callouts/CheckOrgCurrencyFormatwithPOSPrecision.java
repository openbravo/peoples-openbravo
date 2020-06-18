/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;

public class CheckOrgCurrencyFormatwithPOSPrecision extends SimpleCallout {

  @Override
  protected void execute(final CalloutInfo info) throws ServletException {
    final String currencyFormat = info.getStringParameter("inpemObposCurrencyFormat");
    if (!StringUtils.isBlank(currencyFormat)) {
      final Organization organization = OBDal.getInstance()
          .get(Organization.class, info.getStringParameter("inpadOrgId"));
      final Currency currency = organization.getCurrency();
      final long currencyPrecision = currency.getObposPosprecision() == null
          ? currency.getPricePrecision()
          : currency.getObposPosprecision();
      final String[] precisionFormat = currencyFormat.split("\\.");
      if ((precisionFormat.length == 1 && currencyPrecision > 0) || (precisionFormat.length > 1
          && precisionFormat[1].length() != (int) currencyPrecision)) {
        info.addResult("WARNING", Utility.messageBD(this, "OBPOS_OrgCurrencyFormatWithPrecision",
            info.vars.getLanguage()));
      }
    }
  }
}
