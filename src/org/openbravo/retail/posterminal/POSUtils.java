package org.openbravo.retail.posterminal;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posconfig.org.openbravo.retail.posconfig.OBRETCOProductList;
import org.openbravo.retail.posterminal.org.openbravo.retail.posterminal.OBPOSApplications;

/**
 * @author iperdomo
 * 
 */
public class POSUtils {

  public static final Logger log = Logger.getLogger(POSUtils.class);
  public static final String MODULE_JAVA_PACKAGE = "org.openbravo.retail.posterminal";
  public static final String APP_NAME = "WebPOS";

  public static boolean isModuleInDevelopment() {
    OBContext.setAdminMode(false);
    try {
      return KernelUtils.getInstance().getModule(MODULE_JAVA_PACKAGE).isInDevelopment();
    } catch (Exception e) {
      log.error("Error trying to get Module info: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return false;
  }

  public static OBPOSApplications getTerminal(String searchKey) {

    try {
      OBContext.setAdminMode();

      OBQuery<OBPOSApplications> obq = OBDal.getInstance().createQuery(OBPOSApplications.class,
          "searchKey = :value");
      obq.setNamedParameter("value", searchKey);

      List<OBPOSApplications> posApps = obq.list();

      if (posApps.isEmpty()) {
        return null;
      }

      return posApps.get(0);

    } catch (Exception e) {
      log.error("Error getting terminal id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static List<String> getStoreList(String searchKey) {
    try {
      OBContext.setAdminMode();

      OBPOSApplications terminal = getTerminal(searchKey);

      if (terminal == null) {
        throw new OBException("No terminal with searchKey: " + searchKey);
      }

      return OBContext.getOBContext().getOrganizationStructureProvider()
          .getParentList(terminal.getStore().getId(), true);

    } catch (Exception e) {
      log.error("Error getting store list: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static OBRETCOProductList getProductListFromRetailOrganizations(
      List<String> lstOrganizationsIds) {
    for (String curOrg : lstOrganizationsIds) {
      Organization org = OBDal.getInstance().get(Organization.class, curOrg);
      if ("S".equals(org.getOBRETCORetailOrgType()) || "G".equals(org.getOBRETCORetailOrgType())) {
        if (org.getObretcoProductlist() != null) {
          return org.getObretcoProductlist();
        }
      }
    }
    return null;
  }
}
