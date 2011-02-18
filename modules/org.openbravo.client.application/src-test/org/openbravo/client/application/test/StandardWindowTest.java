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

import org.openbravo.client.application.MainLayoutComponentProvider;
import org.openbravo.client.application.StandardWindowComponent;
import org.openbravo.client.freemarker.FreemarkerTemplateProcessor;
import org.openbravo.client.kernel.TemplateProcessorRegistry;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.test.base.BaseTest;

/**
 * Tests generation of the javascript for standard windows
 * 
 * @author iperdomo
 */
public class StandardWindowTest extends BaseTest {

  /**
   * Tests generating the javascript for a standard window.
   */
  public void testStandardViewGeneration() throws Exception {
    setSystemAdministratorContext();

    new MainLayoutComponentProvider().initialize();

    TemplateProcessorRegistry.getInstance().registerTemplateProcessor(
        new FreemarkerTemplateProcessor());

    for (Window window : OBDal.getInstance().createQuery(Window.class, "").list()) {
      System.err.println(window.getName());
      final StandardWindowComponent component = new StandardWindowComponent();
      component.setWindow(window);
      final String jsCode = component.generate();
      if (window.getId().equals("102")) {
        System.err.println(jsCode);
      }
    }
  }
}
