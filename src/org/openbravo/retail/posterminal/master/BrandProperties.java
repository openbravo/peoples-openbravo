package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Brand.brandPropertyExtension)
public class BrandProperties extends ModelExtension {
  public static final Logger log = Logger.getLogger(BrandProperties.class);

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    // TODO: Sandra replace the hgvol with brand reading from separate table
    boolean isRemote = false;
    try {
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true,
          OBContext.getOBContext().getCurrentClient(), OBContext.getOBContext()
              .getCurrentOrganization(), OBContext.getOBContext().getUser(), OBContext
              .getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    ArrayList<HQLProperty> list;
    if (isRemote) {
      list = new ArrayList<HQLProperty>() {
        private static final long serialVersionUID = 1L;
        {
          add(new HQLProperty("brand", "id"));
          add(new HQLProperty("brand.name", "name"));
          add(new HQLProperty("brand.name", "_identifier"));
        }
      };
    } else {
      list = new ArrayList<HQLProperty>() {
        private static final long serialVersionUID = 1L;
        {
          add(new HQLProperty("distinct(product.brand.id)", "id"));
          add(new HQLProperty("product.brand.name", "name"));
          add(new HQLProperty("product.brand.name", "_identifier"));
        }
      };
    }
    return list;
  }

}