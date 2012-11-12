package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
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

  public static Organization getOrganization(String orgId) {
    try {
      OBContext.setAdminMode();

      Organization org = OBDal.getInstance().get(Organization.class, orgId);

      return org;

    } catch (Exception e) {
      log.error("Error getting Organization by org id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static List<String> getOrgList(String searchKey) {
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

  public static List<String> getStoreList(String orgId) {
    return OBContext.getOBContext().getOrganizationStructureProvider().getParentList(orgId, true);
  }

  public static PriceList getPriceListByOrgId(String orgId) {
    try {
      OBContext.setAdminMode();

      final List<String> orgList = getStoreList(orgId);

      for (String currentOrgId : orgList) {
        final Organization org = OBDal.getInstance().get(Organization.class, currentOrgId);
        if (org.getObretcoPricelist() != null) {
          return org.getObretcoPricelist();
        }
      }
    } catch (Exception e) {
      log.error("Error getting PriceList by Org ID: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  public static PriceList getPriceListByTerminal(String searchKey) {
    try {
      OBContext.setAdminMode();

      final List<String> orgList = getOrgList(searchKey);

      for (String orgId : orgList) {
        final Organization org = OBDal.getInstance().get(Organization.class, orgId);
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

  public static OBRETCOProductList getProductListByOrgId(String orgId) {
    try {
      OBContext.setAdminMode();

      final List<String> orgList = getStoreList(orgId);

      for (String currentOrgId : orgList) {
        final Organization org = OBDal.getInstance().get(Organization.class, currentOrgId);
        if (org.getObretcoProductlist() != null) {
          return org.getObretcoProductlist();
        }
      }
    } catch (Exception e) {
      log.error("Error getting ProductList by Org ID: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static int getLastDocumentNumberForPOS(String searchKey, String documentTypeId) {
    OBPOSApplications POSTerminal = POSUtils.getTerminal(searchKey);

    OBQuery<Order> obqOrders = OBDal.getInstance().createQuery(Order.class,
        "em_obpos_applications_id = :value and documentType.id=:documentTypeId");
    obqOrders.setNamedParameter("value", POSTerminal.getId());
    obqOrders.setNamedParameter("documentTypeId", documentTypeId);

    ScrollableResults POSOrders = obqOrders.scroll(ScrollMode.FORWARD_ONLY);
    int maxNumber = 0;
    int i = 0;
    while (POSOrders.next()) {
      Order order = (Order) POSOrders.get(0);
      String documentNo = order.getDocumentNo();
      // documentNo = prefix + '/' + number
      String onlyNumber = documentNo.split("/")[1];
      try {
        int number = Integer.parseInt(onlyNumber);
        if (number > maxNumber) {
          maxNumber = number;
        }
      } catch (NumberFormatException e) {
        // If the parsed result is not a number (i.e. because the searchKey is a
        // prefix of another POS searchKey, the result is ignored
      }
      i++;
      if (i % 100 == 0) {
        OBDal.getInstance().getSession().clear();
      }
    }
    return maxNumber;
  }

  public static String getRetailDependantModuleIds() {
    StringBuffer ids = new StringBuffer();

    List<Module> dependantModules = new ArrayList<Module>();
    Module retailModule = OBDal.getInstance().get(Module.class, "FF808181326CC34901326D53DBCF0018");
    OBCriteria<ModuleDependency> totalDeps = OBDal.getInstance().createCriteria(
        ModuleDependency.class);
    dependantModules.add(retailModule);
    getRetailDependantModules(retailModule, dependantModules, totalDeps.list());
    int n = 0;
    ids.append("(");
    for (Module mod : dependantModules) {
      if (n > 0) {
        ids.append(",");
      }
      ids.append("'" + mod.getId() + "'");
      n++;
    }
    ids.append(")");
    return ids.toString();
  }

  public static void getRetailDependantModules(Module module, List<Module> moduleList,
      List<ModuleDependency> list) {
    for (ModuleDependency depModule : list) {
      if (depModule.getDependentModule().equals(module)
          && !moduleList.contains(depModule.getModule())) {
        moduleList.add(depModule.getModule());
        getRetailDependantModules(depModule.getModule(), moduleList, list);
      }
    }
  }
}
