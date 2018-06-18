/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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

/**
 * Defines hql properties for the PaidReceips Order header
 * 
 * @author alostale
 * 
 */

@Qualifier(PaidReceipts.paidReceiptsPropertyExtension)
public class PaidReceiptProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("ord.documentNo", "documentNo"));
        add(new HQLProperty("ord.orderDate", "orderDate"));
        add(new HQLProperty("ord.creationDate", "creationDate"));
        add(new HQLProperty("ord.businessPartner.id", "bp"));
        add(new HQLProperty("ord.partnerAddress.id", "bpLocId"));
        add(new HQLProperty("ord.invoiceAddress.id", "bpBillLocId"));
        add(new HQLProperty("ord.grandTotalAmount", "totalamount"));
        add(new HQLProperty("salesRepresentative.name", "salesRepresentative$_identifier"));
        add(new HQLProperty("ord.documentType.id", "documentType"));
        add(new HQLProperty("ord.warehouse.id", "warehouse"));
        add(new HQLProperty("ord.description", "description"));
        add(new HQLProperty("ord.currency.iSOCode", "currency$_identifier"));
        add(new HQLProperty("pos.id", "posTerminal"));
        add(new HQLProperty("pos.name", "posTerminal$_identifier"));
        add(new HQLProperty("ord.businessPartner.name", "businessPartner$_identifier"));
        add(new HQLProperty("ord.currency.id", "currency"));
        add(new HQLProperty("ord.priceList.id", "priceList"));
        add(new HQLProperty("salesRepresentative.id", "salesRepresentative"));
        add(new HQLProperty("ord.organization.id", "organization"));
        add(new HQLProperty("ord.client.id", "client"));
        add(new HQLProperty(
            "(case when ord.documentType.sOSubType = 'OB' then true else false end)", "isQuotation"));
        add(new HQLProperty("ord.summedLineAmount", "totalNetAmount"));
        add(new HQLProperty(
            "(case when (select sum(abs(deliveredQuantity)) from ord.orderLineList)=0 and ord.documentType.sOSubType<>'OB' then true else false end)",
            "isLayaway")); // TODO: computed column, it should be refactored
        add(new HQLProperty("ord.priceList.priceIncludesTax", "priceIncludesTax"));
        add(new HQLProperty("replacedOrder.documentNo", "replacedorder_documentNo"));
        add(new HQLProperty("replacedOrder.id", "replacedorder"));
        add(new HQLProperty("ord.iscancelled", "iscancelled"));
        add(new HQLProperty("'false'", "isModified"));
        add(new HQLProperty("ord.updated", "loaded"));
      }
    };

    return list;
  }
}