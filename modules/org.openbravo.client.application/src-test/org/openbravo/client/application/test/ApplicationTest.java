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

import java.util.HashMap;

import org.openbravo.client.application.MainLayoutComponentProvider;
import org.openbravo.client.application.MainLayoutConstants;
import org.openbravo.client.freemarker.FreemarkerTemplateProcessor;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentGenerator;
import org.openbravo.client.kernel.TemplateProcessorRegistry;
import org.openbravo.test.base.BaseTest;

/**
 * 
 * @author iperdomo
 */
public class ApplicationTest extends BaseTest {

  /**
   * Tests retrieving and generating the application JS.
   */
  public void testMainLayoutComponentGeneration() throws Exception {
    setSystemAdministratorContext();

    // TODO: Initialize all component providers for navbar components
    // Uncomment line below if you want to test sample navbar component

    // new SampleButtonComponentProvider().initialize();

    new MainLayoutComponentProvider().initialize();

    TemplateProcessorRegistry.getInstance().registerTemplateProcessor(
        new FreemarkerTemplateProcessor());

    final Component component = new MainLayoutComponentProvider().getComponent(
        MainLayoutConstants.MAIN_LAYOUT_ID, new HashMap<String, Object>());

    final String output = ComponentGenerator.getInstance().generate(component);
    System.out.println(output);
  }
}
