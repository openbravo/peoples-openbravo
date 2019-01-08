package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Brand.brandPropertyExtension)
public class BrandProperties extends ModelExtension {
  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    // TODO: Sandra replace the hgvol with brand reading from separate table
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    ArrayList<HQLProperty> list;
    boolean forceRemote = false;
    if (params != null) {
      @SuppressWarnings("unchecked")
      HashMap<String, Object> localParams = (HashMap<String, Object>) params;
      if (localParams.get("forceRemote") != null) {
        forceRemote = (Boolean) localParams.get("forceRemote");

      }
    }
    if (isRemote || forceRemote) {
      list = new ArrayList<HQLProperty>() {
        private static final long serialVersionUID = 1L;
        {
          add(new HQLProperty("brand.id", "id"));
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