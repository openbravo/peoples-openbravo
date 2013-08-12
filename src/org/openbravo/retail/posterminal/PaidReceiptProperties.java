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
import org.openbravo.model.common.order.Order;

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
        add(new HQLProperty("ord.businessPartner.id", "businessPartner"));
        add(new HQLProperty("ord.grandTotalAmount", "totalamount"));
        add(new HQLProperty("ord.salesRepresentative.name", "salesRepresentative$_identifier"));
        add(new HQLProperty("ord.documentType.name", "documentType"));
        add(new HQLProperty("ord.warehouse.id", "warehouse"));
        add(new HQLProperty("ord.currency.iSOCode", "currency$_identifier"));
        add(new HQLProperty("ord.obposApplications.id", "posTerminal"));
        add(new HQLProperty("ord.obposApplications.name", "posTerminal$_identifier"));
        add(new HQLProperty("ord.businessPartner.name", "businessPartner$_identifier"));
        add(new HQLProperty("ord.currency.id", "currency"));
        add(new HQLProperty("ord.priceList.id", "priceList"));
        add(new HQLProperty("ord.salesRepresentative.id", "salesRepresentative"));
        add(new HQLProperty("ord.organization.id", "organization"));
        add(new HQLProperty("ord.client.id", "client"));
        add(new HQLProperty(
            "(case when ord.documentType.id =  ord.obposApplications.obposTerminaltype.documentTypeForQuotations.id then true else false end)",
            "isQuotation"));
        add(new HQLProperty("ord.summedLineAmount", "totalNetAmount"));
        add(new HQLProperty("(case when "
            + "ord.".concat(POSUtils.getComputedColumn(Order.class, "deliveryStatus"))
            + " = 0 then true else false end)", "isLayaway")); // TODO: computed column, it should
                                                               // be refactored
        add(new HQLProperty("ord.priceList.priceIncludesTax", "priceIncludesTax"));
      }
    };

    return list;
  }
}
