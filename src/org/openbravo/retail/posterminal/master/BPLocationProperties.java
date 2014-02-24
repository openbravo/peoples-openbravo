/*
 ************************************************************************************
 * Copyright (C) 2013-2014 Openbravo S.L.U.
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

@Qualifier(BPLocation.bpLocationPropertyExtension)
public class BPLocationProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bploc.id", "id"));
        add(new HQLProperty("bploc.businessPartner.id", "bpartner"));
        add(new HQLProperty(
            "COALESCE(bploc.locationAddress.addressLine1, bploc.locationAddress.addressLine2, bploc.locationAddress.postalCode, bploc.locationAddress.cityName)",
            "name"));
        add(new HQLProperty("bploc.locationAddress.postalCode", "postalCode"));
        add(new HQLProperty("bploc.locationAddress.cityName", "cityName"));
        add(new HQLProperty("bploc.locationAddress.country.name", "countryName"));
        add(new HQLProperty(
            "COALESCE(bploc.locationAddress.addressLine1, bploc.locationAddress.addressLine2, bploc.locationAddress.postalCode, bploc.locationAddress.cityName)",
            "_identifier"));
        add(new HQLProperty("bploc.locationAddress.country.id", "countryId"));
        add(new HQLProperty(
            "COALESCE(bploc.locationAddress.addressLine1, bploc.locationAddress.addressLine2, bploc.locationAddress.postalCode, bploc.locationAddress.cityName)",
            "_identifier"));
      }
    };
    return list;
  }

}