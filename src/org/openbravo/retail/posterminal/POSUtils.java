/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.mobile.core.MobileUiConfiguration;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.module.ModuleDependency;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.OrgWarehouse;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.importprocess.ImportEntry;

/**
 * @author iperdomo
 * 
 */
public class POSUtils {

  public static final Logger log = LogManager.getLogger();
  public static final String MODULE_JAVA_PACKAGE = "org.openbravo.retail.posterminal";
  public static final String APP_NAME = "WebPOS";
  public static final String BUSINESSLOGIC_NAME = "WebPOS_BusinessLogic";
  public static final String WEB_POS_FORM_ID = "B7B7675269CD4D44B628A2C6CF01244F";
  public static final DateFormat dateFormatUTC = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
      OBContext.setAdminMode(false);

      OBQuery<OBPOSApplications> obq = OBDal.getInstance()
          .createQuery(OBPOSApplications.class, "searchKey = :value");
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
      OBContext.setAdminMode(false);

      OBPOSApplications posTerminal = OBDal.getInstance()
          .get(OBPOSApplications.class, posTerminalId);

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
      OBContext.setAdminMode(false);

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
      OBContext.setAdminMode(false);

      OBPOSApplications terminal = getTerminal(searchKey);

      if (terminal == null) {
        throw new OBException("No terminal with searchKey: " + searchKey);
      }

      return OBContext.getOBContext()
          .getOrganizationStructureProvider()
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
      OBContext.setAdminMode(false);

      OBPOSApplications terminal = getTerminalById(terminalId);

      if (terminal == null) {
        throw new OBException("No terminal with id: " + terminalId);
      }

      return OBContext.getOBContext()
          .getOrganizationStructureProvider()
          .getParentList(terminal.getOrganization().getId(), true);

    } catch (Exception e) {
      log.error("Error getting store list: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  /**
   * Retrieves the list of stores sharing the given Cross Store Organization.
   */
  @SuppressWarnings("unchecked")
  public static List<String> getOrgListByCrossStoreId(final String crossStoreId) {
    OBContext.setAdminMode(false);
    try {
      final String select = "select o.id from Organization as o where o.oBRETCOCrossStoreOrganization.id = :crossStoreId";

      final Query<String> query = OBDal.getInstance().getSession().createQuery(select);
      query.setParameter("crossStoreId", crossStoreId);

      return query.list();
    } catch (Exception e) {
      log.error("Error getting store list: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  /**
   * If isCrossStore is true, retrieves the list of cross stores for given posTerminal store,
   * including posTerminal store. If isCrossStore is false, retrieves given posTerminal store.
   */
  public static List<String> getOrgListCrossStore(final String posterminalId,
      final boolean isCrossStore) {
    final OBPOSApplications posterminal = getTerminalById(posterminalId);

    if (isCrossStore) {
      final Organization crossStore = posterminal.getOrganization()
          .getOBRETCOCrossStoreOrganization();
      return getOrgListByCrossStoreId(crossStore.getId());
    }

    return Collections.singletonList(posterminal.getOrganization().getId());
  }

  public static List<String> getStoreList(String orgId) {
    return OBContext.getOBContext().getOrganizationStructureProvider().getParentList(orgId, true);
  }

  public static PriceList getPriceListByOrgId(String orgId) {
    try {
      OBContext.setAdminMode(false);

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
      OBContext.setAdminMode(false);

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
      OBContext.setAdminMode(false);

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
      Query<PriceListVersion> priceListVersionQuery = OBDal.getInstance()
          .getSession()
          .createQuery("from PricingPriceListVersion AS plv "
              + "where plv.priceList.id = :priceList and plv.active=true "
              + "and plv.validFromDate = (select max(pplv.validFromDate) "
              + "from PricingPriceListVersion as pplv where pplv.active=true "
              + "and pplv.priceList.id = :priceList " //
              + "and pplv.validFromDate <= :terminalDate)", PriceListVersion.class);
      priceListVersionQuery.setParameter("priceList", priceListId);
      priceListVersionQuery.setParameter("terminalDate", terminalDate);
      for (PriceListVersion plv : priceListVersionQuery.list()) {
        return plv;
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

    if (priceList == null) {
      throw new OBException(
          Utility.messageBD(new DalConnectionProvider(false), "OBPOS_errorLoadingPriceList",
              RequestContext.get().getVariablesSecureApp().getLanguage()));
    }

    String priceListId = priceList.getId();
    return POSUtils.getPriceListVersionForPriceList(priceListId, terminalDate);
  }

  public static PriceListVersion getPriceListVersionByOrgId(String orgId, Date terminalDate) {
    PriceList priceList = POSUtils.getPriceListByOrgId(orgId);
    String priceListId = priceList.getId();
    return POSUtils.getPriceListVersionForPriceList(priceListId, terminalDate);
  }

  public static OBRETCOProductList getProductListByPosterminalId(String posterminalId) {
    try {
      OBContext.setAdminMode(false);
      OBPOSApplications posterminal = getTerminalById(posterminalId);
      TerminalType terminalType = OBDal.getInstance()
          .get(TerminalType.class, posterminal.getObposTerminaltype().getId());
      if (terminalType.getObretcoProductlist() != null) {
        return terminalType.getObretcoProductlist();
      } else {
        final List<String> orgList = getStoreList(posterminal.getOrganization().getId());

        for (String currentOrgId : orgList) {
          final Organization org = OBDal.getInstance().get(Organization.class, currentOrgId);
          if (org.getObretcoProductlist() != null) {
            return org.getObretcoProductlist();
          }
        }
      }
    } catch (Exception e) {
      log.error("Error getting ProductList by Org ID: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  public static MobileUiConfiguration getUiConfigurationByTerminalId(String posterminalId) {
    try {
      OBContext.setAdminMode(false);
      OBPOSApplications posterminal = getTerminalById(posterminalId);
      TerminalType terminalType = posterminal.getObposTerminaltype();
      return terminalType.getMobileUIConfiguration();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  /**
   * If isCrossStore is true, retrieves the list of cross assortments for given posTerminal store,
   * including posTerminal assortment. If isCrossStore is false, retrieves given posTerminal
   * assortment.
   */
  @SuppressWarnings("unchecked")
  public static Set<String> getProductListCrossStore(final String posterminalId,
      final boolean isCrossStore) {
    OBContext.setAdminMode(false);
    try {
      Set<String> productList = new HashSet<>();
      final OBPOSApplications posterminal = getTerminalById(posterminalId);

      if (isCrossStore) {
        final Organization crossStore = posterminal.getOrganization()
            .getOBRETCOCrossStoreOrganization();

        final String select = " select o.obretcoProductlist.id from Organization o "
            + " where o.oBRETCOCrossStoreOrganization.id = :crossStoreId "
            + " and o.obretcoProductlist is not null group by o.obretcoProductlist.id";

        final Query<String> query = OBDal.getInstance().getSession().createQuery(select);
        query.setParameter("crossStoreId", crossStore.getId());
        productList.addAll(query.list());
      }

      productList.add(getProductListByPosterminalId(posterminalId).getId());
      return productList;
    } catch (Exception e) {
      log.error("Error getting ProductList by Cross Store ID: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return Collections.emptySet();
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
      OBQuery<OrgWarehouse> warehouses = OBDal.getInstance()
          .createQuery(OrgWarehouse.class,
              " e where e.organization=:org and e.warehouse.active=true order by priority, id");
      warehouses.setNamedParameter("org", org);
      List<OrgWarehouse> warehouseList = warehouses.list();
      if (warehouseList.size() == 0) {
        return null;
      }
      return warehouseList.get(0).getWarehouse();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static List<Warehouse> getWarehousesForTerminal(OBPOSApplications pOSTerminal) {
    ArrayList<Warehouse> lstWarehouses = new ArrayList<Warehouse>();
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
      for (OrgWarehouse orgWarehouse : warehouseList) {
        lstWarehouses.add(orgWarehouse.getWarehouse());
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return lstWarehouses;
  }

  public static Locator getBinForReturns(OBPOSApplications pOSTerminal) {
    List<Warehouse> lstWarehouses = getWarehousesForTerminal(pOSTerminal);
    if (lstWarehouses.size() > 0) {
      for (Warehouse warehouse : lstWarehouses) {
        if (warehouse.getReturnlocator() != null) {
          return warehouse.getReturnlocator();
        }
      }
      // We haven't found a warehouse with a return bin
      // We are going to select the bin with greater priority
      // of the warehouse of greater priority
      OBCriteria<Locator> locatorCriteria = OBDal.getInstance().createCriteria(Locator.class);
      locatorCriteria.add(Restrictions.eq(Locator.PROPERTY_WAREHOUSE, lstWarehouses.get(0)));
      locatorCriteria.add(Restrictions.eqOrIsNull(Locator.PROPERTY_ISVIRTUAL, false));
      locatorCriteria.addOrderBy(Locator.PROPERTY_RELATIVEPRIORITY, true);
      locatorCriteria.setMaxResults(1);

      List<Locator> lstLocators = locatorCriteria.list();
      if (lstLocators.size() > 0) {
        return lstLocators.get(0);
      } else {
        throw new OBException("Warehouse" + lstWarehouses.get(0) + " doesn't have bins");
      }
    } else {
      throw new OBException("Warehouse are not correctly configured for "
          + pOSTerminal.getIdentifier() + " terminal");
    }
  }

  /**
   * This method returns a Date which corresponds to the current date, without hours, minutes, or
   * seconds
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

  /**
   * Gets the value of a given property in Organization entity looking the the org tree and getting
   * the first not null value
   */
  public static Object getPropertyInOrgTree(Organization org, String propertyName) {
    for (String orgId : OBContext.getOBContext()
        .getOrganizationStructureProvider()
        .getParentList(org.getId(), true)) {
      Organization orgInTree = OBDal.getInstance().get(Organization.class, orgId);
      if (orgInTree.get(propertyName) != null) {
        return orgInTree.get(propertyName);
      }
    }
    return null;
  }

  public static Boolean hasCurrencyRate(String posTerminalId) {
    try {
      OBContext.setAdminMode(true);
      @SuppressWarnings("rawtypes")
      Query currencyRateQuery = OBDal.getInstance()
          .getSession()
          .createQuery("select obpos_currency_rate(coalesce(c, p.paymentMethod.currency), "
              + "p.obposApplications.organization.currency,"
              + " null, null, p.obposApplications.client.id, "
              + "p.obposApplications.organization.id) as rate, obpos_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate"
              + " from OBPOS_App_Payment as p left join p.financialAccount as f "
              + "left join f.currency as c where p.obposApplications.id = :terminalId");

      currencyRateQuery.setParameter("terminalId", posTerminalId);
      currencyRateQuery.list(); // No need to get the result, just execute the query

      // The query succeeded, then the check is valid.
      return true;
    } catch (Exception e) {
      log.error("Error getting Currency Rate: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    // The query failed, then the check is not valid.
    return false;
  }

  /**
   * Method to calculate the default payment method and financial account of an order
   * 
   * @param jsonorder
   *          JSONObject with the information sent from the Web POS
   * @param order
   *          The order to obtain data
   */
  public static void setDefaultPaymentType(JSONObject jsonorder, Order order) {
    try {
      OBQuery<OBPOSAppPayment> paymentQuery = OBDal.getInstance()
          .createQuery(OBPOSAppPayment.class,
              "as e where e.obposApplications.organization = :organization and e.obposApplications.id = :terminal and e.financialAccount.currency = :currency order by e.id");
      paymentQuery.setNamedParameter("organization",
          order.getObposApplications().getOrganization());
      paymentQuery.setNamedParameter("terminal", order.getObposApplications().getId());
      paymentQuery.setNamedParameter("currency", order.getOrganization().getCurrency());
      paymentQuery.setFilterOnReadableOrganization(false);
      paymentQuery.setMaxResult(1);
      OBPOSAppPayment defaultPaymentType = paymentQuery.uniqueResult();

      if (defaultPaymentType != null) {
        JSONObject paymentTypeValues = new JSONObject();
        paymentTypeValues.put("paymentMethodId",
            defaultPaymentType.getPaymentMethod().getPaymentMethod().getId());
        paymentTypeValues.put("financialAccountId",
            defaultPaymentType.getFinancialAccount().getId());
        jsonorder.put("defaultPaymentType", paymentTypeValues);
      } else {
        throw new OBException(OBMessageUtils.messageBD("OBPOS_NoPaymentMethodInStore"));
      }
    } catch (JSONException e) {
      log.error("Error setting default payment type to order" + order, e);
    } catch (OBException e) {
      log.error("Error setting default payment type to order" + order, e);
      throw new OBException(OBMessageUtils.messageBD(e.getMessage()));
    }
  }

  /**
   * Method to calculate the number of characteristics marked as "Filter on Web POS"
   */
  public static long getNumberOfCharacteristicsToFilterInWebPos() {
    long result = -1;
    try {
      Query<Long> queryNumberOfChToFilterInWebPos = OBDal.getInstance()
          .getSession()
          .createQuery("select count(ch.id) " //
              + "from Characteristic as ch " //
              + "where ch.obposFilteronwebpos ='Y' and ch.client.id = :client ", Long.class);

      queryNumberOfChToFilterInWebPos.setParameter("client",
          OBContext.getOBContext().getCurrentClient().getId());

      result = queryNumberOfChToFilterInWebPos.uniqueResult();
    } catch (Exception e) {
      String errorMsg = "Error getting the number of characteristic which are used to filter in Web POS: "
          + e.getMessage();
      log.error(errorMsg, e);
      throw new OBException(errorMsg);
    }
    return result;
  }

  public static boolean isPaidStatus(FIN_Payment payment) {
    return FIN_Utility.seqnumberpaymentstatus(payment.getStatus()) >= FIN_Utility
        .seqnumberpaymentstatus(FIN_Utility.invoicePaymentStatus(payment));
  }

  public static boolean cashupErrorsExistInTerminal(String posId) {
    OBPOSApplications terminal = OBDal.getInstance().getProxy(OBPOSApplications.class, posId);
    OBCriteria<OBPOSErrors> errorsInPOSWindow = OBDal.getInstance()
        .createCriteria(OBPOSErrors.class);
    errorsInPOSWindow.add(Restrictions.eq(OBPOSErrors.PROPERTY_OBPOSAPPLICATIONS, terminal));
    errorsInPOSWindow.add(Restrictions.eq(OBPOSErrors.PROPERTY_TYPEOFDATA, "OBPOS_App_Cashup"));
    errorsInPOSWindow.add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"));
    errorsInPOSWindow.setMaxResults(1);
    if (errorsInPOSWindow.list().size() > 0) {
      return true;
    }
    OBCriteria<ImportEntry> errorsInImportEntry = OBDal.getInstance()
        .createCriteria(ImportEntry.class);
    errorsInImportEntry.add(Restrictions.eq(ImportEntry.PROPERTY_OBPOSPOSTERMINAL, terminal));
    errorsInImportEntry.add(Restrictions.eq(ImportEntry.PROPERTY_TYPEOFDATA, "OBPOS_App_Cashup"));
    errorsInImportEntry.add(Restrictions.eq(ImportEntry.PROPERTY_IMPORTSTATUS, "Error"));
    errorsInImportEntry.setMaxResults(1);
    if (errorsInImportEntry.list().size() > 0) {
      return true;
    }

    return false;
  }

  public static boolean getPreference(final String preference) {
    OBContext.setAdminMode(false);
    boolean value;
    try {
      value = StringUtils.equals(Preferences.getPreferenceValue(preference, true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null), "Y");
    } catch (PropertyException e) {
      value = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return value;
  }

  public static boolean isSynchronizedModeEnabled() {

    boolean isSynchronizeModeActive;
    try {
      isSynchronizeModeActive = "Y".equals(Preferences.getPreferenceValue("OBMOBC_SynchronizedMode",
          true, OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null));
    } catch (PropertyNotFoundException prop) {
      isSynchronizeModeActive = false;
    } catch (PropertyException e) {
      throw new OBException("Error while reading synchronized preference", e);
    }
    return isSynchronizeModeActive;
  }

  /**
   * Returns true if Cross Store functionality is enabled for this posTerminal
   */
  public static boolean isCrossStoreEnabled(final OBPOSApplications posTerminal) {
    final Organization crossOrganization;
    OBContext.setAdminMode(true);
    try {
      crossOrganization = posTerminal.getOrganization().getOBRETCOCrossStoreOrganization();
    } finally {
      OBContext.restorePreviousMode();
    }
    return crossOrganization != null;
  }

  /**
   * Returns true if order store is different than terminal store
   */
  public static boolean isCrossStore(final Order order, final OBPOSApplications posTerminal) {
    if (isCrossStoreEnabled(posTerminal)) {
      if (!StringUtils.equals(order.getOrganization().getId(),
          order.getObposApplications().getOrganization().getId())) {
        return true;
      }

      if (!StringUtils.equals(order.getOrganization().getId(),
          posTerminal.getOrganization().getId())) {
        return true;
      }
    }

    return false;
  }

  /**
   * Updates the last number of the terminal sequence defined with the given name.
   */
  public static void updateTerminalDocumentSequence(final OBPOSApplications posTerminal,
      final String sequenceName, final long sequenceNumber) {
    if (posTerminal.getEntity().hasProperty(sequenceName)) {
      posTerminal.set(sequenceName, Long.max(
          (Long) ObjectUtils.defaultIfNull(posTerminal.get(sequenceName), 0L), sequenceNumber));
      OBDal.getInstance().save(posTerminal);
    }
  }

  /**
   * Reads the last number of the terminal sequence defined with the given name. This number will be
   * compared against the maximum number in the failed orders.
   */
  public static Long getLastTerminalDocumentSequence(final OBPOSApplications posTerminal,
      final String sequenceName, final boolean isInvoiceSequence) {

    final long maxNumberInTerminal = posTerminal.getEntity().hasProperty(sequenceName)
        ? (Long) ObjectUtils.defaultIfNull(posTerminal.get(sequenceName), 0L)
        : 0;

    final List<OBPOSErrors> errors = OBDal.getInstance()
        .createCriteria(OBPOSErrors.class)
        .add(Restrictions.eq(OBPOSErrors.PROPERTY_OBPOSAPPLICATIONS, posTerminal))
        .add(Restrictions.eq(OBPOSErrors.PROPERTY_TYPEOFDATA, "Order"))
        .add(Restrictions.eq(OBPOSErrors.PROPERTY_ORDERSTATUS, "N"))
        .list();

    long maxNumberInErrors = errors.stream().mapToLong(error -> {
      try {
        JSONObject jsonError = new JSONObject(error.getJsoninfo());
        if (isInvoiceSequence && jsonError.has("calculatedInvoice")) {
          jsonError = jsonError.getJSONObject("calculatedInvoice");
        }
        if (jsonError.has("obposSequencename") && jsonError.has("obposSequencenumber")
            && jsonError.getString("obposSequencename").equals(sequenceName)) {
          return jsonError.getLong("obposSequencenumber");
        }
        return 0;
      } catch (Exception e) {
        return 0;
      }
    }).max().orElse(0);

    return Math.max(maxNumberInTerminal, maxNumberInErrors);
  }
}
