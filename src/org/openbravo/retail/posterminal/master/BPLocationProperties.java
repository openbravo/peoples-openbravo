package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(BPLocation.bpLocationPropertyExtension)
public class BPLocationProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("bploc.id", "id"));
        add(new HQLProperty("bploc.businessPartner.id", "bpartner"));
        add(new HQLProperty("bploc.locationAddress.addressLine1", "name"));
        add(new HQLProperty("bploc.locationAddress.postalCode", "postalCode"));
        add(new HQLProperty("bploc.locationAddress.cityName", "cityName"));
        add(new HQLProperty("bploc.locationAddress.addressLine1", "_identifier"));
      }
    };
    return list;
  }

}