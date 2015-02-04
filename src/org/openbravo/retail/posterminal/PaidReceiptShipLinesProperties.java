/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
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
 * Defines hql properties for the PaidReceipsShipLines Order
 * 
 * @author mdj
 * 
 */

@Qualifier(PaidReceipts.paidReceiptsShipLinesPropertyExtension)
public class PaidReceiptShipLinesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("id", "shipLineId"));
        add(new HQLProperty("shipmentReceipt.documentNo", "shipment"));
        add(new HQLProperty("lineNo", "shipmentlineNo"));
        add(new HQLProperty("movementQuantity", "qty"));
        add(new HQLProperty(
            "movementQuantity - (select coalesce(sum(abs(deliveredQuantity)),0) from OrderLine where goodsShipmentLine.id = m.id)",
            "remainingQty"));
      }
    };

    return list;
  }
}
