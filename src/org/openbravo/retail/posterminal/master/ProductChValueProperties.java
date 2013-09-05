package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ProductChValue.productChValuePropertyExtension)
public class ProductChValueProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("cv.id", "id"));
        add(new HQLProperty("cv.name", "name"));
        add(new HQLProperty("cv.characteristic.id", "characteristic_id"));
        add(new HQLProperty("node.reportSet", "parent"));
        add(new HQLProperty("cv.name", "_identifier"));
      }
    };
    return list;
  }

}
