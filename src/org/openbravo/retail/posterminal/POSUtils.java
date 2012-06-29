package org.openbravo.retail.posterminal;

import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.retail.config.OBRETCOProductList;

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

  public static OBPOSApplications getTerminalById(String posTerminalId) {
    try {
      OBContext.setAdminMode();

      OBPOSApplications posTerminal = OBDal.getInstance().get(OBPOSApplications.class,
          posTerminalId);

      return posTerminal;

    } catch (Exception e) {
      log.error("Error getting terminal by id: " + e.getMessage(), e);
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
          .getParentList(terminal.getOrganization().getId(), true);

    } catch (Exception e) {
      log.error("Error getting store list: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static List<String> getStoreListByTerminalId(String posTerminalId) {
    try {
      OBContext.setAdminMode();

      OBPOSApplications terminal = getTerminalById(posTerminalId);

      if (terminal == null) {
        throw new OBException("No terminal with ID: " + posTerminalId);
      }

      return OBContext.getOBContext().getOrganizationStructureProvider()
          .getParentList(terminal.getOrganization().getId(), true);

    } catch (Exception e) {
      log.error("Error getting store list by terminal id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static PriceList getPriceListByTerminalId(String terminalId) {
    try {
      OBContext.setAdminMode();

      final List<String> storeList = getStoreListByTerminalId(terminalId);

      for (String storeId : storeList) {
        final Organization org = OBDal.getInstance().get(Organization.class, storeId);
        if (org.getObretcoPricelist() != null) {
          return org.getObretcoPricelist();
        }
      }
    } catch (Exception e) {
      log.error("Error getting PriceList by Terminal ID: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  public static PriceList getPriceListByTerminal(String searchKey) {
    try {
      OBContext.setAdminMode();

      final List<String> storeList = getStoreList(searchKey);

      for (String storeId : storeList) {
        final Organization org = OBDal.getInstance().get(Organization.class, storeId);
        if (org.getObretcoPricelist() != null) {
          return org.getObretcoPricelist();
        }
      }
    } catch (Exception e) {
      log.error("Error getting PriceList by Terminal value: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  public static OBRETCOProductList getProductListByTerminalId(String terminalId) {
    try {
      OBContext.setAdminMode();

      final List<String> storeList = getStoreListByTerminalId(terminalId);

      for (String orgId : storeList) {
        final Organization org = OBDal.getInstance().get(Organization.class, orgId);
        if ("S".equals(org.getOBRETCORetailOrgType()) || "G".equals(org.getOBRETCORetailOrgType())) {
          if (org.getObretcoProductlist() != null) {
            return org.getObretcoProductlist();
          }
        }
      }
    } catch (Exception e) {
      log.error("Error getting ProductList by Terminal value: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }
}
