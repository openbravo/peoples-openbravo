package org.openbravo.client.application.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.application.CachedPreference;
import org.openbravo.client.application.DynamicExpressionParser;
import org.openbravo.client.application.window.OBViewFieldHandler;
import org.openbravo.client.application.window.OBViewFieldHandler.OBViewField;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;

public class DisplayLogicAtServerLevelTest extends WeldBaseTest {

  @Test
  public void testReplaceSystemPreferencesInDisplayLogic() {

    setSystemAdministratorContext();

    CachedPreference cachedPreference = org.openbravo.base.weld.WeldUtils
        .getInstanceFromStaticBeanManager(CachedPreference.class);
    Tab tab = OBDal.getInstance().get(Tab.class, "270");

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");
    cachedPreference.addCachedPreference("uomManagement");
    cachedPreference.setPreferenceValue("uomManagement", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";
    String expectedTranslatedDisplayLogic = "'Y' = 'Y' & 'Y' = 'Y'";

    DynamicExpressionParser parser = new DynamicExpressionParser(
        displayLogicEvaluatedInServerExpression, tab);

    assertEquals(expectedTranslatedDisplayLogic, parser.replaceSystemPreferencesInDisplayLogic());
  }

  @Test
  public void testReplaceSystemPreferencesInDisplayLogicWithNullValue() {

    setSystemAdministratorContext();

    CachedPreference cachedPreference = org.openbravo.base.weld.WeldUtils
        .getInstanceFromStaticBeanManager(CachedPreference.class);
    Tab tab = OBDal.getInstance().get(Tab.class, "270");

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'N'";
    String expectedTranslatedDisplayLogic = "'null' = 'Y' & 'Y' = 'N'";

    DynamicExpressionParser parser = new DynamicExpressionParser(
        displayLogicEvaluatedInServerExpression, tab);

    assertEquals(expectedTranslatedDisplayLogic, parser.replaceSystemPreferencesInDisplayLogic());
  }

  @Test
  public void testEvaluatePreferencesInDisplayLogic() {

    setSystemAdministratorContext();

    CachedPreference cachedPreference = org.openbravo.base.weld.WeldUtils
        .getInstanceFromStaticBeanManager(CachedPreference.class);

    Tab tab = OBDal.getInstance().get(Tab.class, "270");
    OBViewFieldHandler handler = new OBViewFieldHandler();
    handler.setTab(tab);
    OBViewField field = handler.new OBViewField();

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "Y");
    cachedPreference.addCachedPreference("uomManagement");
    cachedPreference.setPreferenceValue("uomManagement", "Y");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";

    boolean evaluatedDisplayLogic = field
        .evaluateDisplayLogicAtServerLevel(displayLogicEvaluatedInServerExpression);
    boolean expectedEvaluatedDisplayLogic = true;

    assertEquals(expectedEvaluatedDisplayLogic, evaluatedDisplayLogic);

  }

  @Test
  public void testEvaluatePreferencesInDisplayLogicWithNullValue() {

    setSystemAdministratorContext();

    CachedPreference cachedPreference = org.openbravo.base.weld.WeldUtils
        .getInstanceFromStaticBeanManager(CachedPreference.class);

    Tab tab = OBDal.getInstance().get(Tab.class, "270");
    OBViewFieldHandler handler = new OBViewFieldHandler();
    handler.setTab(tab);
    OBViewField field = handler.new OBViewField();

    cachedPreference.addCachedPreference("enableNegativeStockCorrections");
    cachedPreference.setPreferenceValue("enableNegativeStockCorrections", "N");

    String displayLogicEvaluatedInServerExpression = "@uomManagement@ = 'Y' & @enableNegativeStockCorrections@ = 'Y'";

    boolean evaluatedDisplayLogic = field
        .evaluateDisplayLogicAtServerLevel(displayLogicEvaluatedInServerExpression);
    boolean expectedEvaluatedDisplayLogic = false;

    assertEquals(expectedEvaluatedDisplayLogic, evaluatedDisplayLogic);

  }

}
