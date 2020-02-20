/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
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

@Qualifier(TaxRate.taxRatePropertyExtension)
public class TaxRateProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    final List<HQLProperty> list = new ArrayList<>();

    list.add(new HQLProperty("financialMgmtTaxRate.id", "id"));
    list.add(new HQLProperty("financialMgmtTaxRate.name", "name"));
    list.add(new HQLProperty("financialMgmtTaxRate.description", "description"));
    list.add(new HQLProperty("financialMgmtTaxRate.taxSearchKey", "taxSearchKey"));
    list.add(new HQLProperty("financialMgmtTaxRate.validFromDate", "validFromDate"));
    list.add(new HQLProperty("financialMgmtTaxRate.summaryLevel", "summaryLevel"));
    list.add(new HQLProperty("financialMgmtTaxRate.rate", "rate"));
    list.add(new HQLProperty("financialMgmtTaxRate.parentTaxRate.id", "parentTaxRate"));
    list.add(new HQLProperty("financialMgmtTaxRate.country.id", "country"));
    list.add(new HQLProperty("financialMgmtTaxRate.region.id", "region"));
    list.add(new HQLProperty("financialMgmtTaxRate.destinationCountry.id", "destinationCountry"));
    list.add(new HQLProperty("financialMgmtTaxRate.destinationRegion.id", "destinationRegion"));
    list.add(new HQLProperty("financialMgmtTaxRate.taxCategory.id", "taxCategory"));
    list.add(new HQLProperty("financialMgmtTaxRate.default", "default"));
    list.add(new HQLProperty("financialMgmtTaxRate.taxExempt", "taxExempt"));
    list.add(new HQLProperty("financialMgmtTaxRate.salesPurchaseType", "salesPurchaseType"));
    list.add(new HQLProperty("financialMgmtTaxRate.cascade", "cascade"));
    list.add(new HQLProperty("financialMgmtTaxRate.businessPartnerTaxCategory.id",
        "businessPartnerTaxCategory"));
    list.add(new HQLProperty("financialMgmtTaxRate.lineNo", "lineNo"));
    list.add(new HQLProperty("financialMgmtTaxRate.withholdingTax", "withholdingTax"));
    list.add(new HQLProperty("financialMgmtTaxRate.notTaxable", "notTaxable"));
    list.add(new HQLProperty("financialMgmtTaxRate.deductableRate", "deductableRate"));
    list.add(new HQLProperty("financialMgmtTaxRate.originalRate", "originalRate"));
    list.add(new HQLProperty("financialMgmtTaxRate.notTaxdeductable", "notTaxdeductable"));
    list.add(new HQLProperty("financialMgmtTaxRate.istaxdeductable", "istaxdeductable"));
    list.add(new HQLProperty("financialMgmtTaxRate.noVAT", "noVAT"));
    list.add(new HQLProperty("financialMgmtTaxRate.baseAmount", "baseAmount"));
    list.add(new HQLProperty("financialMgmtTaxRate.taxBase.id", "taxBase"));
    list.add(new HQLProperty("financialMgmtTaxRate.docTaxAmount", "docTaxAmount"));
    list.add(new HQLProperty("financialMgmtTaxRate.isCashVAT", "isCashVAT"));

    return list;
  }

}
