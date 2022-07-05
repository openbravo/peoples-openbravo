/*
 ************************************************************************************
 * Copyright (C) 2019-2022 Openbravo S.L.U.
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

@Qualifier(CrossStoreFilter.crossStorePropertyExtension)
public class CrossStoreFilterProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    final List<HQLProperty> list = new ArrayList<>();

    list.add(new HQLProperty("o.id", "orgId"));
    list.add(new HQLProperty("o.name", "orgName"));
    list.add(new HQLProperty("la.country.id", "countryId"));
    list.add(new HQLProperty("la.region.id", "regionId"));
    list.add(new HQLProperty("o.obposCDoctype.id", "documentTypeId"));
    list.add(new HQLProperty("o.obposCDoctypequot.id", "quotationDocumentTypeId"));
    list.add(new HQLProperty("w.id", "warehouseId"));
    list.add(new HQLProperty("w.name", "warehouseName"));
    list.add(new HQLProperty("pl.id", "standardPriceListId"));
    list.add(new HQLProperty("coalesce(ppe.standardPrice,pp.standardPrice)", "standardPrice"));
    list.add(
        new HQLProperty("coalesce(sum(sd.quantityOnHand - sd.reservedQty), 0)", "stock", false));

    return list;
  }
}
