/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
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
        add(new HQLProperty("bpl.businessPartner.id", "id"));
        add(new HQLProperty("bpl.businessPartner.organization.id", "organization"));
        add(new HQLProperty("bpl.businessPartner.name", "name"));
        add(new HQLProperty("bpl.businessPartner.name", "_identifier"));
        add(new HQLProperty("bpl.businessPartner.searchKey", "searchKey"));
        add(new HQLProperty("bpl.businessPartner.description", "description"));
        add(new HQLProperty("bpl.businessPartner.taxID", "taxID"));
        add(new HQLProperty("bpl.businessPartner.sOBPTaxCategory.id", "taxCategory"));
        add(new HQLProperty("bpl.businessPartner.priceList.id", "priceList"));
        add(new HQLProperty("bpl.businessPartner.paymentMethod.id", "paymentMethod"));
        add(new HQLProperty("bpl.businessPartner.paymentTerms.id", "paymentTerms"));
        add(new HQLProperty("bpl.businessPartner.invoiceTerms", "invoiceTerms"));
        add(new HQLProperty(
            "(select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "locId"));
        add(new HQLProperty(
            "(select max(bpls.id) as bpLocId from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipLocId"));
        add(new HQLProperty(
            "(select max(COALESCE(bpls.locationAddress.addressLine1, bpls.locationAddress.addressLine2, bpls.locationAddress.postalCode, bpls.locationAddress.cityName)) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "locName"));
        add(new HQLProperty(
            "(select max(COALESCE(bpls.locationAddress.addressLine1, bpls.locationAddress.addressLine2, bpls.locationAddress.postalCode, bpls.locationAddress.cityName)) as nameShipTo from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipLocName"));
        add(new HQLProperty("ulist.email", "email"));
        add(new HQLProperty("ulist.id", "contactId"));
        add(new HQLProperty("ulist.phone", "phone"));
        add(new HQLProperty("ulist.firstName", "firstName"));
        add(new HQLProperty("ulist.lastName", "lastName"));
        add(new HQLProperty("plist.priceIncludesTax", "priceIncludesTax"));
        add(new HQLProperty("plist.name", "priceListName"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.cityName) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "cityName"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.cityName) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipCityName"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.postalCode) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.invoiceToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "postalCode"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.postalCode) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipPostalCode"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.region.id) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipRegionId"));
        add(new HQLProperty(
            "(select max(bpls.locationAddress.country.id) from BusinessPartnerLocation AS bpls where bpls.businessPartner.id=bpl.businessPartner.id and bpls.shipToAddress = true and bpls.$readableSimpleClientCriteria AND "
                + " bpls.$naturalOrgCriteria group by bpls.businessPartner.id)",
            "shipCountryId"));
        add(new HQLProperty("bpl.locationAddress.country.name", "countryName"));
        add(new HQLProperty("bpl.businessPartner.businessPartnerCategory.id",
            "businessPartnerCategory"));
        add(new HQLProperty("bpl.businessPartner.businessPartnerCategory.name",
            "businessPartnerCategory_name"));
        add(new HQLProperty("bpl.businessPartner.creditLimit", "creditLimit"));
        add(new HQLProperty("bpl.businessPartner.creditUsed", "creditUsed"));
        add(new HQLProperty("bpl.businessPartner.taxExempt", "taxExempt"));
        add(new HQLProperty("bpl.businessPartner.customerBlocking", "customerBlocking"));
        add(new HQLProperty("bpl.businessPartner.salesOrder", "salesOrderBlocking"));
        add(new HQLProperty("to_char(bpl.businessPartner.birthDay, 'yyyy-mm-dd')", "birthDay"));
        add(new HQLProperty("bpl.businessPartner.birthPlace", "birthPlace"));
        add(new HQLProperty(
            "(case when bpl.active = 'Y' and bpl.businessPartner.active = 'Y' then true else false end)",
            "active"));
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