/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

@Qualifier(ProductCharacteristicAndConfiguration.productChAndConfExtension)
public class ProductCharacteristicAndConfigurationProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {

    List<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("pc.id", "m_product_ch_id"));
    list.add(new HQLProperty("pc.sequenceNumber", "m_product_ch_seqno"));
    list.add(new HQLProperty("pc.characteristic.id", "m_characteristic_id"));
    list.add(new HQLProperty("pc.characteristic.name", "m_characteristic_name"));
    list.add(new HQLProperty("pcc.id", "m_product_ch_conf_id"));
    list.add(new HQLProperty("pcc.characteristicValue.id", "m_ch_value_id"));
    list.add(new HQLProperty("pcc.characteristicValue.name", "m_ch_value_name"));

    return list;
  }
}
