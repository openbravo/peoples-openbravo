package org.openbravo.retail.posterminal;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;

/**
 * @author iperdomo
 * 
 */
public class POSUtils {

  public static final Logger log = Logger.getLogger(POSUtils.class);
  public static final String MODULE_JAVA_PACKAGE = "org.openbravo.retail.posterminal";
  public static final String APP_NAME = "WebPOS";
  public static final String WEB_POS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";

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

  public static List<String> getOrgListByTerminalId(String terminalId) {
    try {
      OBContext.setAdminMode();

      OBPOSApplications terminal = getTerminalById(terminalId);

      if (terminal == null) {
        throw new OBException("No terminal with id: " + terminalId);
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

  public static PriceList getPriceListByTerminalId(String terminalId) {
    try {
      OBContext.setAdminMode();

      final List<String> orgList = getOrgListByTerminalId(terminalId);

      for (String orgId : orgList) {
        final Organization org = OBDal.getInstance().get(Organization.class, orgId);
        if (org.getObretcoPricelist() != null) {
          return org.getObretcoPricelist();
        }
      }
    } catch (Exception e) {
      log.error("Error getting PriceList by Terminal id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  public static PriceListVersion getPriceListVersionForPriceList(String priceListId,
      Date terminalDate) {

    try {
      OBContext.setAdminMode(true);
      SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
      Query priceListVersionQuery = OBDal
          .getInstance()
          .getSession()
          .createQuery(
              "from PricingPriceListVersion AS plv "
                  + "where plv.priceList.id ='"
                  + priceListId
                  + "' and plv.validFromDate = (select max(pplv.validFromDate) "
                  + "from PricingPriceListVersion as pplv where pplv.active=true and pplv.priceList.id = '"
                  + priceListId + "' and pplv.validFromDate <= '" + format.format(terminalDate)
                  + "' )");
      for (Object plv : priceListVersionQuery.list()) {
        return (PriceListVersion) plv;
      }

    } catch (Exception e) {
      log.error("Error getting PriceList by Terminal id: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  public static PriceListVersion getPriceListVersionByTerminalId(String terminalId,
      Date terminalDate) {

    PriceList priceList = POSUtils.getPriceListByTerminalId(terminalId);
    String priceListId = (String) DalUtil.getId(priceList);
    return POSUtils.getPriceListVersionForPriceList(priceListId, terminalDate);
  }

  public static PriceListVersion getPriceListVersionByOrgId(String orgId, Date terminalDate) {

    PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String priceListId = (String) DalUtil.getId(priceList);
    return POSUtils.getPriceListVersionForPriceList(priceListId, terminalDate);
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

  public static int getLastDocumentNumberForPOS(String searchKey, List<String> documentTypeIds) {

    String curDbms = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("bbdd.rdbms");
    String sqlToExecute;
    String doctypeIds = "";
    for (String doctypeId : documentTypeIds) {
      if (!doctypeIds.equals("")) {
        doctypeIds += ",";
      }
      doctypeIds += "'" + doctypeId + "'";
    }

    if (curDbms.equals("POSTGRE")) {
      sqlToExecute = "select max(a.docno) from (select to_number(substring(documentno, '/([0-9]+)$')) docno from c_order where em_obpos_applications_id= (select obpos_applications_id from obpos_applications where value = ?) and c_doctype_id in ("
          + doctypeIds
          + ") and documentno like (select orderdocno_prefix from obpos_applications where value = ?)||'%') a";
    } else if (curDbms.equals("ORACLE")) {
      sqlToExecute = "select max(a.docno) from (select to_number(substr(REGEXP_SUBSTR(documentno, '/([0-9]+)$'), 2)) docno from c_order where em_obpos_applications_id= (select obpos_applications_id from obpos_applications where value = ?) and c_doctype_id in ("
          + doctypeIds
          + ") and documentno like (select orderdocno_prefix from obpos_applications where value = ?)||'%' ) a";
    } else {
      // unknow DBMS
      // shouldn't happen
      log.error("Error getting max documentNo because the DBMS is unknown.");
      return 0;
    }

    SQLQuery query = OBDal.getInstance().getSession().createSQLQuery(sqlToExecute);
    query.setString(0, searchKey);
    query.setString(1, searchKey);
    List result = query.list();
    if (result.size() == 0 || result.get(0) == null) {
      return 0;
    }
    if (curDbms.equals("POSTGRE")) {
      return ((BigDecimal) result.get(0)).intValue();
    } else if (curDbms.equals("ORACLE")) {
      return ((Long) result.get(0)).intValue();
    } else {
      return 0;
    }
  }

  public static int getLastDocumentNumberForPOS(String searchKey, String documentTypeId) {
    ArrayList<String> doctypeId = new ArrayList<String>();
    doctypeId.add(documentTypeId);
    return getLastDocumentNumberForPOS(searchKey, doctypeId);

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

  public static Warehouse getWarehouseForTerminal(OBPOSApplications pOSTerminal) {
    OBContext.setAdminMode(false);
    try {
      Organization org = pOSTerminal.getOrganization();
      OBCriteria<OrgWarehouse> warehouses = OBDal.getInstance().createCriteria(OrgWarehouse.class);
      warehouses.setFilterOnReadableClients(false);
      warehouses.setFilterOnReadableOrganization(false);
      warehouses.add(Restrictions.eq(OrgWarehouse.PROPERTY_ORGANIZATION, org));
      warehouses.addOrderBy(OrgWarehouse.PROPERTY_PRIORITY, true);
      warehouses.addOrderBy(OrgWarehouse.PROPERTY_ID, true);
      List<OrgWarehouse> warehouseList = warehouses.list();
      if (warehouseList.size() == 0) {
        return null;
      }
      return warehouseList.get(0).getWarehouse();
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  /**
   * This method returns a Date which corresponds to the current date, without hours, minutes, or
   * seconds
   * 
   * @return
   */
  public static Date getCurrentDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, 0);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.set(Calendar.MILLISECOND, 0);
    Date currentDate = cal.getTime();
    return currentDate;
  }

  public static String getComputedColumn(Class<?> clz, String computedColumnName) {
    Entity myEntity = ModelProvider.getInstance().getEntity(clz);
    Method methodToFind = null;
    try {
      methodToFind = myEntity.getClass().getMethod("hasComputedColumns", (Class<?>[]) null);
    } catch (SecurityException e) {
      return computedColumnName;
    } catch (NoSuchMethodException e) {
      return computedColumnName;
    }
    if (methodToFind != null) {
      return "_computedColumns." + computedColumnName;
    } else {
      return computedColumnName;
    }
  }
}
