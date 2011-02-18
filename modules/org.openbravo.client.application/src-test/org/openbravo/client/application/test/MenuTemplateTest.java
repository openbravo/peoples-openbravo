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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.test;

import org.openbravo.client.application.navigationbarcomponents.ApplicationMenuComponent;
import org.openbravo.client.kernel.JSCompressor;
import org.openbravo.client.kernel.JSLintChecker;
import org.openbravo.client.kernel.TemplateProcessorRegistry;
import org.openbravo.client.kernel.freemarker.FreemarkerTemplateProcessor;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the generating of the menu through a template
 * 
 * @author mtaal
 */
public class MenuTemplateTest extends BaseTest {

  public void setUp() throws Exception {
    super.setUp();
    TemplateProcessorRegistry.getInstance().registerTemplateProcessor(
        new FreemarkerTemplateProcessor());
  }

  public void testApplication() throws Exception {
    setBigBazaarAdminContext();
    final ApplicationMenuComponent appMenuComponent = new ApplicationMenuComponent();
    final String javascript = appMenuComponent.generate();
    System.err.println(javascript);

    // do jslint check
    JSLintChecker.getInstance().check(appMenuComponent.getId(), javascript);

    // compress
    final String compressed = JSCompressor.getInstance().compress(javascript);

    // should have compressed something
    assertTrue(compressed.length() < javascript.length());
    assertTrue(!compressed.equals(javascript));
    assertTrue(compressed.length() > 0);
    System.err.println(compressed);
  }
}
