/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(PaidReceiptsFilter.paidReceiptsFilterPropertyExtension)
public class PaidReceiptsFilterProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("ord.id", "id"));
        add(new HQLProperty("ord.documentType.id", "documentTypeId"));
        add(new HQLProperty("ord.documentStatus", "documentStatus"));
        add(new HQLProperty("ord.documentNo", "documentNo"));
        add(new HQLProperty("ord.creationDate", "creationDate"));
        add(new HQLProperty("ord.orderDate", "orderDate"));
        add(new HQLProperty("ord.orderDate", "orderDateFrom"));
        add(new HQLProperty("ord.orderDate", "orderDateTo"));
        add(new HQLProperty("ord.businessPartner.id", "businessPartner"));
        add(new HQLProperty("ord.businessPartner.name", "businessPartnerName"));
        add(new HQLProperty("ord.grandTotalAmount", "totalamount"));
        add(new HQLProperty("ord.grandTotalAmount", "totalamountFrom"));
        add(new HQLProperty("ord.grandTotalAmount", "totalamountTo"));
        String orderTypeFilter = PaidReceiptsFilter.getOrderTypeFilter((JSONObject) params);
        switch (orderTypeFilter) {
        case "ORD":
          add(new HQLProperty("to_char('ORD')", "orderType"));
          break;
        case "LAY":
          add(new HQLProperty("to_char('LAY')", "orderType"));
          break;
        case "QT":
          add(new HQLProperty("to_char('QT')", "orderType"));
          break;
        case "RET":
          add(new HQLProperty("to_char('RET')", "orderType"));
          break;
        default:
          add(new HQLProperty("(case when ord.documentType.return = true then 'RET'"
              + " when ord.documentType.sOSubType = 'OB' then 'QT'"
              + " when ord.obposIslayaway = true then 'LAY' else 'ORD' end)", "orderType"));

        }
      }
    };

    return list;
  }

}