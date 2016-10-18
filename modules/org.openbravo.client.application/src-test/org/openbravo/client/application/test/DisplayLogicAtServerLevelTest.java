/*************************************************************************
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

package org.openbravo.client.application.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.CachedPreference;
import org.openbravo.client.application.DynamicExpressionParser;
import org.openbravo.client.application.window.OBViewFieldHandler;
import org.openbravo.client.application.window.OBViewFieldHandler.OBViewField;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;

public class DisplayLogicAtServerLevelTest extends WeldBaseTest {

  CachedPreference cachedPreference;
  Tab tab;
  OBViewField field;

  /**
   * Initializes the global variables for the rest of the tests
   */
  @Before
  public void initializeTest() {
    setSystemAdministratorContext();

    cachedPreference = org.openbravo.base.weld.WeldUtils
        .getInstanceFromStaticBeanManager(CachedPreference.class);
    tab = OBDal.getInstance().get(Tab.class, "270");

    OBViewFieldHandler handler = new OBViewFieldHandler();
    handler.setTab(tab);
    field = handler.new OBViewField();
  }

  /**
   * Tests that the replacement of the DisplayLogic at Server level works correctly
   * 
   * @return True if the test is ok, false otherwise
   */
  @Test
  public void testReplaceSystemPreferencesInDisplayLogic() {

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");
    cachedPreference.addCachedPreference("uomManagement");
    cachedPreference.setPreferenceValue("uomManagement", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";
    String expectedTranslatedDisplayLogic = "'Y' = 'Y' & 'Y' = 'Y'";
    String translatedDisplayLogic = DynamicExpressionParser
        .replaceSystemPreferencesInDisplayLogic(displayLogicEvaluatedInServerExpression);

    assertThat(translatedDisplayLogic, equalTo(expectedTranslatedDisplayLogic));
  }

  /**
   * Tests that the replacement of the DisplayLogic at Server level works correctly containing null
   * values
   * 
   * @return True if the test is ok, false otherwise
   */
  @Test
  public void testReplaceSystemPreferencesInDisplayLogicWithNullValue() {

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'N'";
    String expectedTranslatedDisplayLogic = "'null' = 'Y' & 'Y' = 'N'";

    String translatedDisplayLogic = DynamicExpressionParser
        .replaceSystemPreferencesInDisplayLogic(displayLogicEvaluatedInServerExpression);

    assertThat(translatedDisplayLogic, equalTo(expectedTranslatedDisplayLogic));
  }

  /**
   * Tests that the evaluation of the DisplayLogic at Server level works correctly
   * 
   * @return True if the test is ok, false otherwise
   */
  @Test
  public void testEvaluatePreferencesInDisplayLogic() {

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");
    cachedPreference.addCachedPreference("uomManagement");
    cachedPreference.setPreferenceValue("uomManagement", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";

    boolean evaluatedDisplayLogic = field.evaluateDisplayLogicAtServerLevel(
        displayLogicEvaluatedInServerExpression, "0");
    boolean expectedEvaluatedDisplayLogic = true;

    assertThat(evaluatedDisplayLogic, equalTo(expectedEvaluatedDisplayLogic));

  }

  /**
   * Tests that the evaluation of the DisplayLogic at Server level works correctly containing null
   * values
   * 
   * @return True if the test is ok, false otherwise
   */
  @Test
  public void testEvaluatePreferencesInDisplayLogicWithNullValue() {

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "N");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";

    boolean evaluatedDisplayLogic = field.evaluateDisplayLogicAtServerLevel(
        displayLogicEvaluatedInServerExpression, "0");
    boolean expectedEvaluatedDisplayLogic = false;

    assertThat(evaluatedDisplayLogic, equalTo(expectedEvaluatedDisplayLogic));

  }

}
