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
 * All portions are Copyright (C) 2017 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.costing;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;

public class TestCostingUtils {

  private static final String ENABLE_AUTOMATIC_PRICE_CORRECTION_TRXS = "enableAutomaticPriceCorrectionTrxs";
  public static final Organization ALL_ORGANIZATIONS = OBDal.getInstance().get(Organization.class,
      "0");

  public static void enableAutomaticPriceDifferenceCorrectionPreference() {
    if (enableAutomaticPriceDifferenceCorrectionPreferenceHasNotBeenSet()) {
      setEnableAutomaticPriceDifferenceCorrectionPreference();
    }
  }

  public static void disableAutomaticPriceDifferenceCorrectionPreference() {
    Preference preference = getEnableAutomaticPriceDifferenceCorrectionsPreference();
    if (preference != null) {
      unsetEnableAutomaticPriceDifferenceCorrectionPreference(preference);
    }
  }

  private static void setEnableAutomaticPriceDifferenceCorrectionPreference() {
    Preference preference = OBProvider.getInstance().get(Preference.class);
    preference.setOrganization(ALL_ORGANIZATIONS);
    preference.setProperty(ENABLE_AUTOMATIC_PRICE_CORRECTION_TRXS);
    preference.setSearchKey("Y");
    preference.setVisibleAtClient(null);
    preference.setVisibleAtOrganization(null);
    preference.setVisibleAtRole(null);
    OBDal.getInstance().save(preference);
    OBDal.getInstance().flush();
  }

  private static void unsetEnableAutomaticPriceDifferenceCorrectionPreference(Preference preference) {
    OBDal.getInstance().remove(preference);
    OBDal.getInstance().flush();
  }

  private static boolean enableAutomaticPriceDifferenceCorrectionPreferenceHasNotBeenSet() {
    Preference preference = getEnableAutomaticPriceDifferenceCorrectionsPreference();
    return preference == null;
  }

  private static Preference getEnableAutomaticPriceDifferenceCorrectionsPreference() {
    List<Preference> preferenceList = Preferences.getPreferences(
        ENABLE_AUTOMATIC_PRICE_CORRECTION_TRXS, true, null, null, null, null, null);
    return (preferenceList.size() == 0) ? null : preferenceList.get(0);
  }

  public static void assertOriginalTotalAndUnitCostOfProductTransaction(Product costingProduct,
      int originalTransactionCost, int totalTransactionCost, int unitTransactionCost) {
    assertTrue(costingProduct.getMaterialMgmtMaterialTransactionList().size() == 1);
    assertTrue(costingProduct.getMaterialMgmtMaterialTransactionList().get(0).getTransactionCost()
        .intValue() == originalTransactionCost);
    assertTrue(costingProduct.getMaterialMgmtMaterialTransactionList().get(0).getTotalCost()
        .intValue() == totalTransactionCost);
    assertTrue(costingProduct.getMaterialMgmtMaterialTransactionList().get(0).getUnitCost()
        .intValue() == unitTransactionCost);
  }
}
