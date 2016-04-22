/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.model.Entity;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.UserRoles;
import org.openbravo.model.ad.access.WindowAccess;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.test.base.OBBaseTest;

/**
 * Test cases to ensure that initialize method of EntityAccessChecker is working properly.
 *
 * @author inigo.sanchez
 *
 *         BaseDataSourceTestDal
 */
@RunWith(Parameterized.class)
public class TestInitializeAccess extends OBBaseTest {
  private static final String ASTERISK_ORG_ID = "0";
  private static final String CONTEXT_USER = "100";
  private static final String LANGUAGE_ID = "192";
  private static final String WAREHOUSE_ID = "B2D40D8A5D644DD89E329DC297309055";
  private static final String ROLE_INTERNATIONAL_ADMIN = "42D0EEB1C66F497A90DD526DC597E6F0";
  private static final String ROLE_ACCESS_FINANCIAL_ACCOUNT = "1";
  private static final String ROLE_SYSTEM_ADMIN = "0";
  private static final String ESP_ORG = "E443A31992CB4635AFCAEABE7183CE85";
  private static final String FINANCIAL_ACCOUNT_WINDOW = "94EAA455D2644E04AB25D93BE5157B6D";
  private static final String CLIENT_ID_INT = "23C59575B9CF467C9620760EB255B389";

  private static final String TABLE_WINDOWS_TABS_FIELDS_ID = "105";
  private static final String RECORD_OF_WINDOWS_TABS_FIELDS_ID = "283";

  private RoleType role;
  private int expectedResponseStatus;

  private enum RoleType {
    FIN_ACC_ROLE(ROLE_ACCESS_FINANCIAL_ACCOUNT, ESP_ORG);

    private String roleId;
    private String orgId;

    private RoleType(String roleId, String orgId) {
      this.roleId = roleId;
      this.orgId = orgId;
    }
  }

  @SuppressWarnings("serial")
  private HashMap<String, String> expectedPermissions = new HashMap<String, String>() {
    {

      put("APRM_FinAcc_Transaction_acct_v", "Readable");
      put("APRM_Finacc_Transaction_v", "Readable");
      put("APRM_Finacc_Trx_Full_Acct_V", "Readable");
      put("APRM_Reconciliation", "Readable");
      put("Aprm_Credit_To_Use", "Readable");
      put("CurrencyConversionRateDoc", "Readable");
      put("FIN_BankStatement", "Readable");
      put("FIN_BankStatementLine", "Readable");
      put("FIN_Finacc_Transaction", "Readable");
      put("FIN_Financial_Account", "Readable");
      put("FIN_Financial_Account_Acct", "Readable");
      put("FIN_Payment", "Readable");
      put("FIN_Reconciliation", "Readable");
      put("FIN_ReconciliationLine_v", "Readable");
      put("FinancialMgmtFinAccPaymentMethod", "Readable");
      put("aprm_gl_item", "Readable");
      put("aprm_matchstatement", "Readable");
      put("aprm_orderinvoice", "Readable");
      put("aprm_transactiontomatch", "Readable");

      put("ADClient", "DerivedReadable");
      put("ADTable", "DerivedReadable");
      put("ADUser", "DerivedReadable");
      put("BusinessPartner", "DerivedReadable");
      put("Costcenter", "DerivedReadable");
      put("Country", "DerivedReadable");
      put("Currency", "DerivedReadable");
      put("DocumentType", "DerivedReadable");
      // put("FIN_Payment1", "DerivedReadable");
      put("FIN_PaymentMethod", "DerivedReadable");
      put("FIN_Payment_Detail_V", "DerivedReadable");
      put("FIN_Payment_Proposal", "DerivedReadable");
      put("FIN_Payment_Sched_Inv_V", "DerivedReadable");
      put("FIN_Payment_Sched_Ord_V", "DerivedReadable");
      put("FIN_ReconciliationLineTemp", "DerivedReadable");
      put("FinancialMgmtAccountingCombination", "DerivedReadable");
      put("FinancialMgmtAcctSchema", "DerivedReadable");
      put("FinancialMgmtAsset", "DerivedReadable");
      put("FinancialMgmtBankFileException", "DerivedReadable");
      put("FinancialMgmtBankFileFormat", "DerivedReadable");
      put("FinancialMgmtElementValue", "DerivedReadable");
      put("FinancialMgmtGLCategory", "DerivedReadable");
      put("FinancialMgmtGLItem", "DerivedReadable");
      put("FinancialMgmtGLJournal", "DerivedReadable");
      put("FinancialMgmtGLJournalLine", "DerivedReadable");
      put("FinancialMgmtMatchingAlgorithm", "DerivedReadable");
      put("FinancialMgmtPaymentExecutionProcess", "DerivedReadable");
      put("FinancialMgmtPeriod", "DerivedReadable");
      put("FinancialMgmtTaxCategory", "DerivedReadable");
      put("FinancialMgmtTaxRate", "DerivedReadable");
      put("FinancialMgmtWithholding", "DerivedReadable");
      put("Invoice", "DerivedReadable");
      put("Location", "DerivedReadable");
      put("Locator", "DerivedReadable");
      put("MarketingCampaign", "DerivedReadable");
      put("MaterialMgmtABCActivity", "DerivedReadable");
      put("Order", "DerivedReadable");
      put("Organization", "DerivedReadable");
      put("Product", "DerivedReadable");
      put("Project", "DerivedReadable");
      put("Region", "DerivedReadable");
      put("SalesRegion", "DerivedReadable");
      put("UOM", "DerivedReadable");
      put("UserDimension1", "DerivedReadable");
      put("UserDimension2", "DerivedReadable");

      put("ADList", "DerivedProcess");

      /*
       * 2120369 [http-8080-3] INFO org.openbravo.dal.security.EntityAccessChecker - Add Multiple
       * Payments 2120369 [http-8080-3] INFO org.openbravo.dal.security.EntityAccessChecker - Add
       * Payment 2120369 [http-8080-3] INFO org.openbravo.dal.security.EntityAccessChecker - Add
       * Transaction 2120369 [http-8080-3] INFO org.openbravo.dal.security.EntityAccessChecker -
       * Find Transactions to Match 2120369 [http-8080-3] INFO
       * org.openbravo.dal.security.EntityAccessChecker - Match Statement
       */
    }
  };

  public TestInitializeAccess(RoleType role) {
    this.role = role;
  }

  @Parameters(name = "{0} - dataSource: {1}")
  public static Collection<Object[]> parameters() {
    List<Object[]> testCases = new ArrayList<Object[]>();

    // testing a problem detected in how properties are initialized.
    testCases.add(new Object[] { RoleType.FIN_ACC_ROLE });
    return testCases;
  }

  /** Creates dummy role with access to Financial Account window for testing purposes */
  @BeforeClass
  public static void createRoleWithAccesFinancialAccount() {
    OBContext.setOBContext(CONTEXT_USER);

    Role noAccessRole = OBProvider.getInstance().get(Role.class);
    noAccessRole.setId("1");
    noAccessRole.setNewOBObject(true);
    noAccessRole.setOrganization(OBDal.getInstance().get(Organization.class, ASTERISK_ORG_ID));
    noAccessRole.setName("Test Access FA");
    noAccessRole.setManual(true);
    noAccessRole.setUserLevel(" CO");
    noAccessRole.setClientList(OBContext.getOBContext().getCurrentClient().getId());
    noAccessRole.setOrganizationList(ASTERISK_ORG_ID);
    OBDal.getInstance().save(noAccessRole);

    RoleOrganization noAcessRoleOrg = OBProvider.getInstance().get(RoleOrganization.class);
    noAcessRoleOrg.setOrganization((Organization) OBDal.getInstance().getProxy(
        Organization.ENTITY_NAME, ESP_ORG));
    noAcessRoleOrg.setRole(noAccessRole);
    OBDal.getInstance().save(noAcessRoleOrg);

    UserRoles noAccessRoleUser = OBProvider.getInstance().get(UserRoles.class);
    noAccessRoleUser.setOrganization(noAccessRole.getOrganization());
    noAccessRoleUser.setUserContact(OBContext.getOBContext().getUser());
    noAccessRoleUser.setRole(noAccessRole);
    OBDal.getInstance().save(noAccessRoleUser);

    // ****
    final WindowAccess windowAccess = OBProvider.getInstance().get(WindowAccess.class);
    final OBCriteria<Window> obCriteria = OBDal.getInstance().createCriteria(Window.class);
    obCriteria.add(Restrictions.eq(Window.PROPERTY_ID, FINANCIAL_ACCOUNT_WINDOW));
    obCriteria.setMaxResults(1);
    windowAccess.setClient(OBContext.getOBContext().getCurrentClient());
    windowAccess.setOrganization(OBDal.getInstance().get(Organization.class, ASTERISK_ORG_ID));
    windowAccess.setRole(OBDal.getInstance().get(Role.class, ROLE_ACCESS_FINANCIAL_ACCOUNT));
    windowAccess.setWindow((Window) obCriteria.uniqueResult());
    windowAccess.setEditableField(true);
    OBDal.getInstance().save(windowAccess);
    OBDal.getInstance().flush();
    // ****
    // WindowAccess windowAccess = OBProvider.getInstance().get(WindowAccess.class);
    // windowAccess.setId(FINANCIAL_ACCOUNT_WINDOW);
    // windowAccess.setNewOBObject(true);
    // windowAccess.setWindow(OBDal.getInstance().get(Window.class, FINANCIAL_ACCOUNT_WINDOW));
    // windowAccess.setEditableField(true);
    // OBDal.getInstance().save(windowAccess);

    OBDal.getInstance().commitAndClose();
  }

  /** Tests datasource allows or denies fetch action based on role access */
  @Test
  public void calculatePermissionsForRole() throws Exception {
    OBContext.setOBContext(CONTEXT_USER, ROLE_ACCESS_FINANCIAL_ACCOUNT, CLIENT_ID_INT,
        ASTERISK_ORG_ID);

    final OBContext obContext = OBContext.getOBContext();
    // OBContext.setOBContext(CONTEXT_USER, role.roleId, "0", role.orgId);
    obContext.getEntityAccessChecker().dump();
    int numberOfWritable = obContext.getEntityAccessChecker().getWritableEntities().size();
    int numberOfReadable = obContext.getEntityAccessChecker().getReadableEntities().size();
    int numberOfDerivedReadable = obContext.getEntityAccessChecker().getDerivedReadableEntities()
        .size();
    int numberOfDerivedFromProcess = obContext.getEntityAccessChecker()
        .getDerivedEntitiesFromProcess().size();
    int totalPermissions = numberOfWritable + numberOfDerivedReadable + numberOfDerivedFromProcess;

    assertThat("Total of permissions", totalPermissions, is(expectedPermissions.size()));
    assertThat("Readable and Writable number of permissions", numberOfWritable,
        is(numberOfReadable));

    // Writable and Readable
    List<Entity> calculatedEntities = new ArrayList<Entity>(obContext.getEntityAccessChecker()
        .getWritableEntities());
    ArrayList<String> calculatedEntitiesList = new ArrayList<String>();

    Iterator<Entity> et = calculatedEntities.iterator();
    while (et.hasNext()) {
      calculatedEntitiesList.add(et.next().getName());
    }

    for (Map.Entry<String, String> entry : expectedPermissions.entrySet()) {
      if (entry.getValue().equals("Readable") && calculatedEntitiesList.contains(entry.getKey())) {
        numberOfWritable--;
      }
    }

    // Derived
    calculatedEntities = new ArrayList<Entity>(obContext.getEntityAccessChecker()
        .getDerivedReadableEntities());
    calculatedEntitiesList = new ArrayList<String>();

    et = calculatedEntities.iterator();
    while (et.hasNext()) {
      calculatedEntitiesList.add(et.next().getName());
    }
    for (Map.Entry<String, String> entry : expectedPermissions.entrySet()) {
      if (entry.getValue().equals("DerivedReadable")
          && calculatedEntitiesList.contains(entry.getKey())) {
        numberOfDerivedReadable--;
      }
    }

    // Derived from process
    calculatedEntities = new ArrayList<Entity>(obContext.getEntityAccessChecker()
        .getDerivedEntitiesFromProcess());
    calculatedEntitiesList = new ArrayList<String>();

    et = calculatedEntities.iterator();
    while (et.hasNext()) {
      calculatedEntitiesList.add(et.next().getName());
    }
    for (Map.Entry<String, String> entry : expectedPermissions.entrySet()) {
      if (entry.getValue().equals("DerivedProcess")
          && calculatedEntitiesList.contains(entry.getKey())) {
        numberOfDerivedFromProcess--;
      }
    }

    // If there is more permissions than 0 permissions are not calculated properly.
    assertThat("Readable and Writable permissions", numberOfWritable, is(0));
    assertThat("Derived permissions", numberOfDerivedReadable, is(0));
    assertThat("Derived from process permissions", numberOfDerivedFromProcess, is(0));

  }

  /** Deletes dummy testing role */
  @AfterClass
  public static void cleanUp() {
    OBContext.setOBContext(CONTEXT_USER);
    OBDal.getInstance().remove(OBDal.getInstance().get(Role.class, ROLE_ACCESS_FINANCIAL_ACCOUNT));
    OBDal.getInstance().commitAndClose();
  }
}
