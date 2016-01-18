/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(BPartnerFilter.bPartnerFilterPropertyExtension)
public class BPartnerFilterProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    @SuppressWarnings("unchecked")
    final Boolean location = (Boolean) ((Map<String, Object>) params).get("location");
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        if (location) {
          add(new HQLProperty("bpl.id", "id"));
          add(new HQLProperty("bpl.businessPartner.id", "bpartnerId"));
          add(new HQLProperty("bpl.businessPartner.customerBlocking", "customerBlocking"));
          add(new HQLProperty("bpl.businessPartner.salesOrder", "salesOrderBlocking"));
          add(new HQLProperty("bpl.businessPartner.name", "bpName"));
          add(new HQLProperty("bpl.businessPartner.taxID", "taxID"));
          add(new HQLProperty("bpl.businessPartner.businessPartnerCategory.name", "bpCategory"));
          add(new HQLProperty("ulist.email", "email"));
          add(new HQLProperty("ulist.phone", "phone"));
          add(new HQLProperty("bpl.id", "bpLocactionId"));
          add(new HQLProperty("bpl.name", "locName"));
          add(new HQLProperty("bpl.locationAddress.postalCode", "postalCode"));
          add(new HQLProperty("bpl.locationAddress.cityName", "cityName"));
        } else {
          add(new HQLProperty("bp.id", "id"));
          add(new HQLProperty("bp.id", "bpartnerId"));
          add(new HQLProperty("bp.customerBlocking", "customerBlocking"));
          add(new HQLProperty("bp.salesOrder", "salesOrderBlocking"));
          add(new HQLProperty("bp.name", "bpName"));
          add(new HQLProperty("bp.taxID", "taxID"));
          add(new HQLProperty("bp.businessPartnerCategory.name", "bpCategory"));
          add(new HQLProperty("ulist.email", "email"));
          add(new HQLProperty("ulist.phone", "phone"));
          add(new HQLProperty("''", "bpLocactionId"));
          add(new HQLProperty("''", "locName"));
          add(new HQLProperty("''", "postalCode"));
          add(new HQLProperty("''", "cityName"));
        }
      }
    };
    return list;
  }

}