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

    list.add(new HQLProperty("tr.id", "id"));
    list.add(new HQLProperty("tr.name", "name"));
    list.add(new HQLProperty("tr.description", "description"));
    list.add(new HQLProperty("tr.taxSearchKey", "taxSearchKey"));
    list.add(new HQLProperty("tr.summaryLevel", "summaryLevel"));
    list.add(new HQLProperty("tr.rate", "rate"));
    list.add(new HQLProperty("tr.parentTaxRate.id", "parentTaxRate"));
    list.add(new HQLProperty("tr.lineNo", "lineNo"));
    list.add(new HQLProperty("tr.cascade", "cascade"));
    list.add(new HQLProperty("tr.baseAmount", "baseAmount"));
    list.add(new HQLProperty("tr.taxBase.id", "taxBase"));
    list.add(new HQLProperty("tr.originalRate", "originalRate"));
    list.add(new HQLProperty("tr.deductableRate", "deductableRate"));

    list.add(new HQLProperty("coalesce(ptr.validFromDate, tr.validFromDate)", "validFromDate"));
    list.add(new HQLProperty("coalesce(ptr.taxCategory.id, tr.taxCategory.id)", "taxCategory"));
    list.add(new HQLProperty(
        "coalesce(ptr.businessPartnerTaxCategory.id, tr.businessPartnerTaxCategory.id)",
        "businessPartnerTaxCategory"));
    list.add(new HQLProperty("coalesce(ptr.salesPurchaseType, tr.salesPurchaseType)",
        "salesPurchaseType"));
    list.add(new HQLProperty("coalesce(ptr.docTaxAmount, tr.docTaxAmount)", "docTaxAmount"));
    list.add(new HQLProperty("coalesce(ptr.country.id, tr.country.id)", "country"));
    list.add(new HQLProperty("coalesce(ptr.region.id, tr.region.id)", "region"));
    list.add(new HQLProperty("coalesce(ptr.destinationCountry.id, tr.destinationCountry.id)",
        "destinationCountry"));
    list.add(new HQLProperty("coalesce(ptr.destinationRegion.id, tr.destinationRegion.id)",
        "destinationRegion"));

    list.add(new HQLProperty("coalesce(ptr.default, tr.default)", "default"));
    list.add(new HQLProperty("coalesce(ptr.taxExempt, tr.taxExempt)", "taxExempt"));
    list.add(new HQLProperty("coalesce(ptr.withholdingTax, tr.withholdingTax)", "withholdingTax"));
    list.add(new HQLProperty("coalesce(ptr.notTaxable, tr.notTaxable)", "notTaxable"));
    list.add(
        new HQLProperty("coalesce(ptr.notTaxdeductable, tr.notTaxdeductable)", "notTaxdeductable"));
    list.add(
        new HQLProperty("coalesce(ptr.istaxdeductable, tr.istaxdeductable)", "istaxdeductable"));
    list.add(new HQLProperty("coalesce(ptr.noVAT, tr.noVAT)", "noVAT"));
    list.add(new HQLProperty("coalesce(ptr.isCashVAT, tr.isCashVAT)", "isCashVAT"));

    return list;
  }

}
