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
 * All portions are Copyright (C) 2018-2024 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.costing;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.cost.CostingAlgorithm;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.test.costing.utils.TestCostingConstants;
import org.openbravo.test.costing.utils.TestCostingUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCostingBase extends WeldBaseTest {

  @Before
  public void setInitialConfiguration() {
    // FIXME: Change setInitialConfiguration to @BeforeClass and remove runBefore flag
    // once https://issues.openbravo.com/view.php?id=36326 is fixed
    if (TestCostingConstants.runBefore) {
      try {

        // Set System context
        OBContext.setOBContext(TestCostingConstants.USERADMIN_ID);
        OBContext.setAdminMode(true);

        // Set EUR currency costing precision
        Currency currrencyEur = OBDal.getInstance().get(Currency.class, EURO_ID);
        currrencyEur.setCostingPrecision(4L);
        OBDal.getInstance().save(currrencyEur);

        // Set USD currency costing precision
        Currency currrencyUsd = OBDal.getInstance().get(Currency.class, DOLLAR_ID);
        currrencyUsd.setCostingPrecision(4L);
        OBDal.getInstance().save(currrencyUsd);
        OBDal.getInstance().flush();

        // Set QA context
        OBContext.setOBContext(TestCostingConstants.OPENBRAVO_USER_ID,
            TestCostingConstants.QATESTING_ROLE_ID, TestCostingConstants.QATESTING_CLIENT_ID,
            TestCostingConstants.SPAIN_ORGANIZATION_ID);

        // Set Spain organization currency
        Organization organization = OBDal.getInstance()
            .get(Organization.class, TestCostingConstants.SPAIN_ORGANIZATION_ID);
        organization.setCurrency(OBDal.getInstance().get(Currency.class, EURO_ID));
        OBDal.getInstance().save(organization);
        OBDal.getInstance().flush();

        // Create costing rule
        CostingRule costingRule = OBProvider.getInstance().get(CostingRule.class);
        TestCostingUtils.setGeneralData(costingRule);
        costingRule.setCostingAlgorithm(OBDal.getInstance()
            .get(CostingAlgorithm.class, TestCostingConstants.AVERAGE_COSTINGALGORITHM_ID));
        costingRule.setWarehouseDimension(true);
        costingRule.setBackdatedTransactionsFixed(true);
        costingRule.setValidated(false);
        costingRule.setStartingDate(null);
        costingRule.setEndingDate(null);
        OBDal.getInstance().save(costingRule);
        OBDal.getInstance().flush();
        OBDal.getInstance().refresh(costingRule);
        TestCostingUtils.runCostingBackground();
        TestCostingUtils.validateCostingRule(costingRule.getId());

        OBDal.getInstance().commitAndClose();
      } catch (Exception e) {
        System.out.println(e.getMessage());
        throw new OBException(e);
      }

      finally {
        OBContext.restorePreviousMode();
        TestCostingConstants.runBefore = false;
      }
    }
  }

  @AfterClass
  public static void setFinalConfiguration() {
    try {
      // Set System context
      OBContext.setOBContext(TestCostingConstants.USERADMIN_ID);
      OBContext.setAdminMode(true);

      // Set EUR currency costing precision
      Currency currrencyEur = OBDal.getInstance().get(Currency.class, EURO_ID);
      currrencyEur.setCostingPrecision(2L);
      OBDal.getInstance().save(currrencyEur);

      // Set USD currency costing precision
      Currency currrencyUsd = OBDal.getInstance().get(Currency.class, DOLLAR_ID);
      currrencyUsd.setCostingPrecision(2L);
      OBDal.getInstance().save(currrencyUsd);
      OBDal.getInstance().flush();

      // Set QA context
      OBContext.setOBContext(TestCostingConstants.OPENBRAVO_USER_ID,
          TestCostingConstants.QATESTING_ROLE_ID, TestCostingConstants.QATESTING_CLIENT_ID,
          TestCostingConstants.SPAIN_ORGANIZATION_ID);

      // Set Spain organization currency
      Organization organization = OBDal.getInstance()
          .get(Organization.class, TestCostingConstants.SPAIN_ORGANIZATION_ID);
      organization.setCurrency(null);
      OBDal.getInstance().save(organization);

      OBDal.getInstance().flush();
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new OBException(e);
    }

    finally {
      OBContext.restorePreviousMode();
      TestCostingConstants.runBefore = true;
    }
  }

}
