/*
 ************************************************************************************
 * Copyright (C) 2012-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(TaxZone.taxZonePropertyExtension)
public class TaxZoneProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("financialMgmtTaxZone.id", "id"));
        add(new HQLProperty("tax.id", "tax"));
        add(new HQLProperty("tax.name", "name"));
        add(new HQLProperty("tax.rate", "rate"));
        add(new HQLProperty("tax.taxCategory.id", "taxCategory"));
        add(new HQLProperty("tax.summaryLevel", "summaryLevel"));
        add(new HQLProperty("tax.businessPartnerTaxCategory.id", "businessPartnerTaxCategory"));
        add(new HQLProperty("financialMgmtTaxZone.fromCountry.id", "fromCountry"));
        add(new HQLProperty("financialMgmtTaxZone.destinationCountry.id", "destinationCountry"));
        add(new HQLProperty("financialMgmtTaxZone.fromRegion.id", "fromRegion"));
        add(new HQLProperty("financialMgmtTaxZone.destinationRegion.id", "destinationRegion"));
        add(new HQLProperty("financialMgmtTaxZone.active", "active"));
      }
    };
    return list;
  }

}