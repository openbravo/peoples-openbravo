package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Brand.brandPropertyExtension)
public class BrandProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("distinct(product.brand.id)", "id"));
        add(new HQLProperty("product.brand.name", "name"));
        add(new HQLProperty("product.brand.name", "_identifier"));
      }
    };
    return list;
  }

}