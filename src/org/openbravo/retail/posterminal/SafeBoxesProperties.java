package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(SafeBoxes.safeBoxesPropertyExtension)
public class SafeBoxesProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("sf.id", "safeBoxId"));
        add(new HQLProperty("sf.commercialName", "safeBoxName"));
        add(new HQLProperty("sf.searchKey", "safeBoxSearchKey"));
        add(new HQLProperty("sf.countOnRemove", "safeBoxCountOnRemove"));
        add(new HQLProperty("coalesce(u.id, null)", "safeBoxUserId"));
        add(new HQLProperty("coalesce(u.name, null)", "safeBoxUserName"));
      }
    };

    return list;
  }

}
