package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ReturnReason.returnReasonPropertyExtension)
public class ReturnReasonProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("reason.id", "id"));
        add(new HQLProperty("reason.searchKey", "searchKey"));
        add(new HQLProperty("reason.name", "name"));
        add(new HQLProperty("reason.name", "_identifier"));
      }
    };
    return list;
  }
}