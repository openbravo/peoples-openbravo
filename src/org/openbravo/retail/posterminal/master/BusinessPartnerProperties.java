/*
 ************************************************************************************
 * Copyright (C) 2015-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(BusinessPartner.businessPartnerPropertyExtension)
public class BusinessPartnerProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bp.id", "id"));
        add(new HQLProperty("bp.organization.id", "organization"));
        add(new HQLProperty("bp.name", "name"));
        add(new HQLProperty("bp.name", "_identifier"));
        add(new HQLProperty("bp.searchKey", "searchKey"));
        add(new HQLProperty("bp.description", "description"));
        add(new HQLProperty("bp.taxID", "taxID"));
        add(new HQLProperty("bp.sOBPTaxCategory.id", "taxCategory"));
        add(new HQLProperty("bp.priceList.id", "priceList"));
        add(new HQLProperty("bp.paymentMethod.id", "paymentMethod"));
        add(new HQLProperty("bp.paymentTerms.id", "paymentTerms"));
        add(new HQLProperty("bp.invoiceTerms", "invoiceTerms"));
        add(new HQLProperty("bpl.id", "locId"));
        add(new HQLProperty("bpsl.id", "shipLocId"));
        add(new HQLProperty("bpl.locationAddress.addressLine1", "locName"));
        add(new HQLProperty(
            "coalesce(bpsl.locationAddress.addressLine1, bpsl.locationAddress.addressLine2, bpsl.locationAddress.postalCode, bpsl.locationAddress.cityName)",
            "shipLocName"));
        add(new HQLProperty("ulist.email", "email"));
        add(new HQLProperty("ulist.id", "contactId"));
        add(new HQLProperty("ulist.phone", "phone"));
        add(new HQLProperty("ulist.firstName", "firstName"));
        add(new HQLProperty("ulist.lastName", "lastName"));
        add(new HQLProperty("plist.priceIncludesTax", "priceIncludesTax"));
        add(new HQLProperty("plist.name", "priceListName"));
        add(new HQLProperty("bpl.locationAddress.cityName", "cityName"));
        add(new HQLProperty("bpsl.locationAddress.cityName", "shipCityName"));
        add(new HQLProperty("bpl.locationAddress.postalCode", "postalCode"));
        add(new HQLProperty("bpsl.locationAddress.postalCode", "shipPostalCode"));
        add(new HQLProperty("bpl.locationAddress.region.id", "regionId"));
        add(new HQLProperty("bpsl.locationAddress.region.id", "shipRegionId"));
        add(new HQLProperty("bpl.locationAddress.country.id", "countryId"));
        add(new HQLProperty("bpsl.locationAddress.country.id", "shipCountryId"));
        add(new HQLProperty("bpl.locationAddress.country.name", "countryName"));
        add(new HQLProperty("bp.businessPartnerCategory.id", "businessPartnerCategory"));
        add(new HQLProperty("bp.businessPartnerCategory.name", "businessPartnerCategory_name"));
        add(new HQLProperty("bp.creditLimit", "creditLimit"));
        add(new HQLProperty("bp.creditUsed", "creditUsed"));
        add(new HQLProperty("bp.taxExempt", "taxExempt"));
        add(new HQLProperty("bp.customerBlocking", "customerBlocking"));
        add(new HQLProperty("bp.salesOrder", "salesOrderBlocking"));
        add(new HQLProperty("bp.birthDay", "birthDay"));
        add(new HQLProperty("bp.birthPlace", "birthPlace"));
        add(new HQLProperty(
            "(case when bpl.active = 'Y' and bp.active = 'Y' then true else false end)", "active"));
        String curDbms = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("bbdd.rdbms");
        if (curDbms.equals("POSTGRE")) {
          add(new HQLProperty("now()", "loaded"));
        } else if (curDbms.equals("ORACLE")) {
          add(new HQLProperty("TO_CHAR(SYSTIMESTAMP, 'MM-DD-YYYY HH24:MI:SS')", "loaded"));
        }

      }
    };
    return list;
  }

}