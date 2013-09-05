package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(BPCategory.bpcategoryPropertyExtension)
public class BPCategoryProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bpcat.id", "id"));
        add(new HQLProperty("bpcat.searchKey", "searchKey"));
        add(new HQLProperty("bpcat.name", "name"));
        add(new HQLProperty("bpcat.name", "_identifier"));
      }
    };
    return list;
  }

}
