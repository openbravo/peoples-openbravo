/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
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
    final List<HQLProperty> list = new ArrayList<>();

    list.add(new HQLProperty("financialMgmtTaxZone.id", "id"));
    list.add(new HQLProperty("financialMgmtTaxRate.id", "taxRateId"));
    list.add(new HQLProperty("financialMgmtTaxZone.fromCountry.id", "zoneCountry"));
    list.add(new HQLProperty("financialMgmtTaxZone.fromRegion.id", "zoneRegion"));
    list.add(
        new HQLProperty("financialMgmtTaxZone.destinationCountry.id", "zoneDestinationCountry"));
    list.add(new HQLProperty("financialMgmtTaxZone.destinationRegion.id", "zoneDestinationRegion"));
    list.add(new HQLProperty("financialMgmtTaxRate.name", "name"));
    list.add(new HQLProperty("financialMgmtTaxRate.rate", "rate"));
    list.add(new HQLProperty("financialMgmtTaxRate.taxCategory.id", "taxCategory"));
    list.add(new HQLProperty("financialMgmtTaxRate.summaryLevel", "summaryLevel"));
    list.add(new HQLProperty("financialMgmtTaxRate.businessPartnerTaxCategory.id",
        "businessPartnerTaxCategory"));
    return list;
  }

}
