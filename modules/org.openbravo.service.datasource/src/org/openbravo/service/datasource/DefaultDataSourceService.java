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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.service.json.DefaultJsonDataService;
import org.openbravo.service.json.JsonConstants;

/**
 * The default implementation of the {@link DataSourceService}. Supports data retrieval, update
 * operations as well as creation of the datasource in javascript.
 * 
 * Makes extensive use of the {@link DefaultJsonDataService}. Check the javadoc on that class for
 * more information.
 * 
 * @author mtaal
 */
public class DefaultDataSourceService extends BaseDataSourceService {
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#fetch(java.util.Map)
   */
  public String fetch(Map<String, String> parameters) {
    parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());

    if (getWhereClause() != null) {
      if (parameters.get(JsonConstants.WHERE_PARAMETER) != null) {
        final String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);
        parameters.put(JsonConstants.WHERE_PARAMETER, "(" + currentWhere + ") and ("
            + getWhereClause() + ")");
      } else {
        parameters.put(JsonConstants.WHERE_PARAMETER, getWhereClause());
      }
    }

    parameters.put(JsonConstants.USE_ALIAS, "true");

    // System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>" + new Date());
    // for (String key : parameters.keySet()) {
    // System.err.println(key + ": " + parameters.get(key));
    // }

    return DefaultJsonDataService.getInstance().fetch(parameters);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#remove(java.util.Map)
   */
  @Override
  public String remove(Map<String, String> parameters) {
    parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
    return DefaultJsonDataService.getInstance().remove(parameters);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#add(java.util.Map, java.lang.String)
   */
  @Override
  public String add(Map<String, String> parameters, String content) {
    parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
    return DefaultJsonDataService.getInstance().add(parameters, content);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSource#update(java.util.Map, java.lang.String)
   */
  @Override
  public String update(Map<String, String> parameters, String content) {
    parameters.put(JsonConstants.ENTITYNAME, getEntity().getName());
    return DefaultJsonDataService.getInstance().update(parameters, content);
  }

  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    final Entity entity = getEntity();
    final List<DataSourceProperty> dsProperties;
    if (entity == null) {
      dsProperties = super.getDataSourceProperties(parameters);
    } else {
      dsProperties = getInitialProperties(entity);
    }

    // now see if there are additional properties, these are often property paths
    final Object additionalPropParameter = parameters
        .get(JsonConstants.ADDITIONAL_PROPERTIES_PARAMETER);
    if (additionalPropParameter != null && getEntity() != null) {
      final String[] additionalProps = ((String) additionalPropParameter).split(",");

      // the additional properties are passed back using a different name
      // than the original property
      for (String additionalProp : additionalProps) {
        final Property property = DalUtil.getPropertyFromPath(entity, additionalProp);
        final DataSourceProperty dsProperty = DataSourceProperty.createFromProperty(property);
        dsProperty.setName(additionalProp);
        dsProperties.add(dsProperty);
      }
    }
    return dsProperties;
  }

  protected List<DataSourceProperty> getInitialProperties(Entity entity) {
    if (entity == null) {
      return Collections.emptyList();
    }
    final List<DataSourceProperty> result = new ArrayList<DataSourceProperty>();
    for (Property prop : entity.getProperties()) {
      if (prop.isOneToMany()) {
        continue;
      }
      result.add(DataSourceProperty.createFromProperty(prop));
    }
    return result;
  }
}
