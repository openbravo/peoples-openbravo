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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.BaseTemplateComponent;
import org.openbravo.client.kernel.Component;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.client.kernel.ComponentProviderRegistry;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.datasource.DataSource;
import org.openbravo.service.datasource.DataSourceConstants;

/**
 * Represents a datasource to be rendered/created on the client.
 * 
 * @author mtaal
 */
public class AllDataSourcesComponent extends BaseTemplateComponent {

  @Override
  protected Template getComponentTemplate() {
    return OBDal.getInstance().get(Template.class, MainLayoutConstants.ALL_DS_TEMPLATE_ID);
  }

  public String getId() {
    return MainLayoutConstants.ALL_DATASOURCES_COMPONENT_ID;
  }

  public List<Component> getDataSources() {
    OBContext.setAdminMode();
    try {
      final ComponentProvider componentProvider = ComponentProviderRegistry.getInstance()
          .getComponentProvider(DataSourceConstants.DS_COMPONENT_TYPE);
      final Map<String, Object> dsParameters = new HashMap<String, Object>(getParameters());
      final List<Component> dataSources = new ArrayList<Component>();
      for (Entity entity : ModelProvider.getInstance().getModel()) {
        dataSources.add(componentProvider.getComponent(entity.getName(), dsParameters));
      }
      for (DataSource dataSource : OBDal.getInstance().createQuery(DataSource.class, "").list()) {
        dataSources.add(componentProvider.getComponent(dataSource.getId(), dsParameters));
      }
      return dataSources;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
