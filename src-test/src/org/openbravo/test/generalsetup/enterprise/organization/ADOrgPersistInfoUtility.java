/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.test.generalsetup.enterprise.organization;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.InitialOrgSetup;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.OrganizationAcctSchema;
import org.openbravo.model.common.enterprise.OrganizationType;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADOrgPersistInfoUtility {

  private static final Logger log = LoggerFactory.getLogger(ADOrgPersistInfoUtility.class);

  /**
   * Set Context for FnB International Group Client Admin
   */

  public static void setTestContextFB() {
    OBContext.setOBContext(ADOrgPersistInfoConstants.OPENBRAVO_USER_ID,
        ADOrgPersistInfoConstants.FB_GROUP_ADMIN_ROLE_ID, ADOrgPersistInfoConstants.CLIENT_FB,
        ADOrgPersistInfoConstants.ORG_FB_FBGROUP);
  }

  /**
   * Set Context for QA Testing Client Admin
   */

  public static void setTestContextQA() {
    OBContext.setOBContext(ADOrgPersistInfoConstants.OPENBRAVO_USER_ID,
        ADOrgPersistInfoConstants.QA_TESTING_ADMIN_ROLE_ID, ADOrgPersistInfoConstants.CLIENT_QA,
        ADOrgPersistInfoConstants.ORG_QA_SPAIN);
  }

  /**
   * Create organization with type orgType under strParentOrg
   * 
   * @param newOrgType
   * @param strParentOrg
   * @param summary
   * @param currencyId
   * @return
   */
  public static String createOrganization(String newOrgType, String strParentOrg, boolean summary,
      String currencyId) {
    long number = System.currentTimeMillis();
    Properties properties = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    String strSourcePath = properties.getProperty("source.path");
    InitialOrgSetup initialOrg = new InitialOrgSetup(OBContext.getOBContext().getCurrentClient());
    initialOrg.createOrganization("Test_" + number, "", newOrgType, strParentOrg, "", "", "", false,
        null, "", false, false, false, false, false, strSourcePath);
    Organization org = OBDal.getInstance().get(Organization.class, initialOrg.getOrgId());
    org.setSummaryLevel(summary);
    if (StringUtils.equals(newOrgType, ADOrgPersistInfoConstants.ORGTYPE_LEGALWITHACCOUNTING)) {
      org.setCurrency(OBDal.getInstance().get(Currency.class, currencyId));
      org.setAllowPeriodControl(true);
      org.setCalendar(ADOrgPersistInfoUtility.createCalendar(org));
      org.setGeneralLedger(ADOrgPersistInfoUtility.createAcctSchema(org, currencyId));
      OBDal.getInstance().commitAndClose();
      ADOrgPersistInfoUtility.setAcctSchema(org);
    }
    return org.getId();
  }

  /**
   * Sets organization as ready
   * 
   * @param orgId
   * @param isCascade
   */
  public static void setAsReady(final String orgId, final String isCascade) {
    final Map<String, String> parameters = new HashMap<String, String>(1);
    parameters.put("Cascade", isCascade);
    final ProcessInstance pinstance = CallProcess.getInstance().call("AD_Org_Ready", orgId,
        parameters);
    if (pinstance.getResult() == 0L) {
      throw new RuntimeException(pinstance.getErrorMsg());
    }
    OBDal.getInstance().commitAndClose();
  }

  public static Organization getCalendarOrganization(String orgId) {
    Organization calOrg = null;
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider();
    for (String org : osp.getParentList(orgId, true)) {
      calOrg = OBDal.getInstance().get(Organization.class, org);
      if (calOrg.getCalendar() != null) {
        break;
      } else {
        calOrg = null;
      }
    }
    return calOrg;
  }

  public static Organization getBusinessUnitOrganization(String orgId) {
    Organization businessUnitOrg = null;
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider();
    for (String org : osp.getParentList(orgId, true)) {
      businessUnitOrg = OBDal.getInstance().get(Organization.class, org);
      if (businessUnitOrg.getOrganizationType().isBusinessUnit()) {
        break;
      } else {
        businessUnitOrg = null;
      }
    }
    return businessUnitOrg;
  }

  public static AcctSchema createAcctSchema(final Organization org, final String currencyId) {
    AcctSchema acctSchema = null;
    try {
      acctSchema = OBProvider.getInstance().get(AcctSchema.class);
      acctSchema.setOrganization(org);
      acctSchema.setName(org.getName() + " GL");
      acctSchema.setClient(org.getClient());
      acctSchema.setCurrency(OBDal.getInstance().get(Currency.class, currencyId));
      OBDal.getInstance().save(acctSchema);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error("Error in createAcctSchema", e);
    }
    return acctSchema;
  }

  public static void setAcctSchema(final Organization org) {
    OrganizationAcctSchema orgAcctSchema = null;
    try {
      orgAcctSchema = OBProvider.getInstance().get(OrganizationAcctSchema.class);
      orgAcctSchema.setOrganization(org);
      orgAcctSchema.setClient(org.getClient());
      orgAcctSchema.setAccountingSchema(org.getGeneralLedger());
      OBDal.getInstance().save(orgAcctSchema);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error("Error in setAcctSchema", e);
    }
  }

  public static Calendar createCalendar(final Organization org) {
    Calendar calendar = null;
    try {
      calendar = OBProvider.getInstance().get(Calendar.class);
      calendar.setName(org.getName() + " Calendar");
      calendar.setOrganization(org);
      calendar.setClient(org.getClient());
      OBDal.getInstance().save(calendar);
      OBDal.getInstance().flush();
    } catch (Exception e) {
      log.error("Error in createCalendar", e);
    }
    return calendar;
  }

  public static String getBusinessUnitOrgType() {
    String businessUnitOrgType = null;
    try {
      OBContext.setAdminMode(false);
      Client client = OBDal.getInstance().get(Client.class, ADOrgPersistInfoConstants.CLIENT_0);
      Organization org0 = OBDal.getInstance().get(Organization.class,
          ADOrgPersistInfoConstants.ORG_0);
      final OBCriteria<OrganizationType> criteria = OBDal.getInstance()
          .createCriteria(OrganizationType.class);
      criteria.add(Restrictions.eq(OrganizationType.PROPERTY_BUSINESSUNIT, true));
      criteria.add(Restrictions.eq(OrganizationType.PROPERTY_ACTIVE, true));
      criteria.add(Restrictions.eq(OrganizationType.PROPERTY_CLIENT, client));
      criteria.add(Restrictions.eq(OrganizationType.PROPERTY_ORGANIZATION, org0));
      criteria.setMaxResults(1);
      if (criteria.uniqueResult() != null) {
        businessUnitOrgType = ((OrganizationType) criteria.uniqueResult()).getId();
      } else {
        OrganizationType orgType = OBProvider.getInstance().get(OrganizationType.class);
        orgType.setName("Business Unit");
        orgType.setClient(client);
        orgType.setOrganization(org0);
        orgType.setActive(true);
        orgType.setBusinessUnit(true);
        OBDal.getInstance().save(orgType);
        OBDal.getInstance().flush();
        OBDal.getInstance().commitAndClose();
        businessUnitOrgType = orgType.getId();
      }
    } catch (Exception e) {
      log.error("Error in getBusinessUnitOrgType", e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return businessUnitOrgType;
  }

  public static String getPersistOrgInfo(final String functionName, final String orgId) {
    String returnValue = "";
    try {
      final ArrayList<Object> parameters = new ArrayList<Object>();
      parameters.add(orgId);
      returnValue = (String) CallStoredProcedure.getInstance().call(functionName, parameters, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return returnValue;
  }

  public static String getLegalEntityOrBusinessUnitOrg(final String functionName,
      final String orgId, final String ptype) {
    String returnValue = "";
    try {
      final ArrayList<Object> parameters = new ArrayList<Object>();
      parameters.add(orgId);
      parameters.add(ptype);
      returnValue = (String) CallStoredProcedure.getInstance().call(functionName, parameters, null);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return returnValue;
  }

  /**
   * Validates persist organization information set by AD_Org_Ready DB procedure against the
   * information provided by OrganizationStructureProvider
   * 
   * @param orgId
   */
  public static void assertPersistOrgInfo(String orgId) {
    if (StringUtils.isNotEmpty(orgId)) {
      Organization org = OBDal.getInstance().get(Organization.class, orgId);
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider();
      osp.reInitialize();
      assertEquals("Failed to match Legal Entity of Organization", osp.getLegalEntity(org),
          org.getLegalEntityOrganization());
      assertEquals("Failed to match Period Control Allowed Organization of Organization",
          osp.getPeriodControlAllowedOrganization(org), org.getPeriodControlAllowedOrganization());
      Organization calOrg = ADOrgPersistInfoUtility.getCalendarOrganization(orgId);
      assertEquals("Failed to match Calendar Owner Organization of organization", calOrg,
          org.getCalendarOwnerOrganization());
      if (calOrg != null) {
        assertEquals("Failed to match Calendar of Organization", calOrg.getCalendar(),
            org.getInheritedCalendar());
      }
      assertEquals("Failed to match Business Unit Organization of Organization",
          ADOrgPersistInfoUtility.getBusinessUnitOrganization(orgId),
          org.getBusinessUnitOrganization());
    }
  }
}
