/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.model.domaintype.DatetimeDomainType;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.retail.posterminal.PaidReceipts;

@Qualifier(PaidReceipts.paidReceiptsLinesPropertyExtension)
public class PaidReceiptDeliveryProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    List<HQLProperty> props = new ArrayList<>();
    props.add(new HQLProperty("ordLine.obrdmDeliveryMode", "obrdmDeliveryMode"));
    props.add(new HQLProperty("ordLine.obrdmDeliveryDate", "obrdmDeliveryDate"));
    props.add(new HQLProperty("ordLine.obrdmDeliveryTime", "obrdmDeliveryTime", true,
        DatetimeDomainType.class));

    props.add(new HQLProperty("ordLine.obrdmAmttopayindelivery", "obrdmAmttopayindelivery"));
    return props;
  }
}
