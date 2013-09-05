package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Category.productCategoryPropertyExtension)
public class CategoryProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("pCat.id", "id"));
        add(new HQLProperty("pCat.searchKey", "bpartner"));
        add(new HQLProperty("pCat.name", "name"));
        add(new HQLProperty("pCat.name", "_identifier"));
        add(new HQLProperty("img.bindaryData", "img"));
      }
    };
    return list;
  }

}