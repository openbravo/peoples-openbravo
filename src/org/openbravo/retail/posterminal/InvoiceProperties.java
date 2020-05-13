/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Invoices.invoicesPropertyExtension)
public class InvoiceProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("inv.documentNo", "documentNo"));
        add(new HQLProperty("inv.invoiceDate", "orderDate"));
        add(new HQLProperty("inv.creationDate", "creationDate"));
        add(new HQLProperty("inv.businessPartner.id", "bp"));
        add(new HQLProperty("inv.partnerAddress.id", "bpLocId"));
        add(new HQLProperty("inv.grandTotalAmount", "totalamount"));
        add(new HQLProperty("inv.description", "description"));
        add(new HQLProperty("inv.currency.iSOCode", "currency$_identifier"));
        add(new HQLProperty("inv.businessPartner.name", "businessPartner$_identifier"));
        add(new HQLProperty("inv.currency.id", "currency"));
        add(new HQLProperty("inv.priceList.id", "priceList"));
        add(new HQLProperty("inv.organization.id", "organization"));
        add(new HQLProperty("inv.client.id", "client"));
        add(new HQLProperty("inv.summedLineAmount", "totalNetAmount"));
        add(new HQLProperty("inv.priceList.priceIncludesTax", "priceIncludesTax"));
        add(new HQLProperty(
            "(case when inv.obposSequencename in ('fullinvoiceslastassignednum', 'fullreturninvoiceslastassignednum') then true else false end)",
            "fullInvoice"));

        add(new HQLProperty("salesRepresentative.id", "salesRepresentative"));
        add(new HQLProperty("salesRepresentative.name", "salesRepresentative$_identifier"));

        add(new HQLProperty("coalesce(ord.documentType.id,inv.documentType.id)", "documentType"));
        add(new HQLProperty("ord.warehouse.id", "warehouse"));

        add(new HQLProperty("pos.id", "posTerminal"));
        add(new HQLProperty("pos.name", "posTerminal$_identifier"));
      }
    };

    return list;
  }
}
