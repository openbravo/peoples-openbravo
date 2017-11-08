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
 * All portions are Copyright (C) 2009-2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel.freemarker.test;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.MockServletContext;
import org.junit.Test;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.client.kernel.BaseComponentProvider.ComponentResource;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentGenerator;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.KernelComponentProvider;
import org.openbravo.client.kernel.KernelConstants;
import org.openbravo.client.kernel.SessionDynamicTemplateComponent;
import org.openbravo.dal.core.DalContextListener;

/**
 * Test the generation of several kernel components.
 * 
 * @author mtaal
 */

public class GenerateComponentTest extends WeldBaseTest {

  @Inject
  @ComponentProvider.Qualifier(KernelComponentProvider.QUALIFIER)
  private ComponentProvider kernelComponentProvider;

  @Inject
  @Any
  private Instance<SessionDynamicTemplateComponent> components;

  @Test
  public void testApplication() throws Exception {
    generateComponent(KernelConstants.APPLICATION_COMPONENT_ID, null);
  }

  @SuppressWarnings("serial")
  @Test
  public void testStaticResources() throws Exception {
    final MockServletContext mockContext = new MockServletContext(OBPropertiesProvider
        .getInstance().getOpenbravoProperties().getProperty("source.path")
        + "/WebContent");
    DalContextListener.setServletContext(mockContext);
    generateComponent(KernelConstants.RESOURCE_COMPONENT_ID, new HashMap<String, Object>() {
      {
        put(KernelConstants.APP_NAME_PARAMETER, ComponentResource.APP_OB3);
        put(KernelConstants.SERVLET_CONTEXT, mockContext);
      }
    });
  }

  @Test
  public void testLabels() throws Exception {
    generateDynamicComponent(KernelConstants.LABELS_COMPONENT_ID);
  }

  protected void generateComponent(String componentID, Map<String, Object> params) {
    final Component component = kernelComponentProvider.getComponent(componentID,
        params == null ? new HashMap<String, Object>() : params);

    final String output = ComponentGenerator.getInstance().generate(component);
    System.err.println(output);
  }

  private void generateDynamicComponent(String componentId) {
    for (SessionDynamicTemplateComponent component : components) {
      if (component.getId().equals(componentId)) {
        final String output = ComponentGenerator.getInstance().generate(component);
        System.err.println(output);
        return;
      }
    }
  }

}