package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ProductCharacteristic.productCharacteristicPropertyExtension)
public class ProductCharacteristicProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("pcv.id", "m_product_ch_id"));
        add(new HQLProperty("pcv.product.id", "m_product"));
        add(new HQLProperty("pcv.characteristic.id", "characteristic_id"));
        add(new HQLProperty("pcv.characteristic.name", "characteristic"));
        add(new HQLProperty("pcv.characteristicValue.id", "ch_value_id"));
        add(new HQLProperty("pcv.characteristicValue.name", "ch_value"));
        add(new HQLProperty("pcv.characteristic.name", "_identifier"));
      }
    };
    return list;
  }
}
