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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Entity;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.service.datasource.DataSourceProperty;
import org.openbravo.service.datasource.DefaultDataSourceService;

/**
 * An extension mechanism that allows to define a custom way for resolving additional properties
 * which cannot be resolved through the data model. This interface mainly allows to retrieve the
 * value of the additional property and to provide the data source properties required to filter and
 * sort in the client side by the property field linked to the additional property.
 */
public interface AdditionalPropertyResolver {

  /**
   * Resolves an additional property. If null or an empty map is returned, then the additional
   * property will be tried to be resolved with an {@code AdditionalPropertyResolver} with less
   * priority, if any. If there is no {@code AdditionalPropertyResolver} returning a map with
   * values, then the standard logic of the {@link DataToJsonConverter} will be used to resolve the
   * additional property.
   * 
   * @see DataToJsonConverter#toJsonObject
   * 
   * @param bob
   *          The source {@link BaseOBObject}
   * @param additionalProperty
   *          The path to the additional property to be resolved
   * 
   * @return a Map with the values resolved for the additional property where the keys are the
   *         property names and values are the property values
   */
  public Map<String, Object> resolve(BaseOBObject bob, String additionalProperty);

  /**
   * Provides the list of {@link DataSourceProperty} that must be included in the standard data
   * sources when the provided entity and additional property are requested. This is needed in order
   * to support filtering and sorting in the client side by the given additional property. If null
   * or an empty list is returned, then the properties will be tried to be retrieved with an
   * {@code AdditionalPropertyResolver} with less priority, if any. If there is no
   * {@code AdditionalPropertyResolver} returning a list with properties, then no data source
   * properties will be added for the given additional property.
   * 
   * @see DefaultDataSourceService#getDataSourceProperties
   * 
   * @param entity
   *          The base entity
   * @param additionalProperty
   *          The additional property path
   * 
   * @return the list of {@link DataSourceProperty} to be included in the data source
   */
  public List<DataSourceProperty> getDataSourceProperties(Entity entity, String additionalProperty);

  /**
   * @return an integer representing the priority of this resolver. Those with lower priority are
   *         taken first when executing the different methods. It returns 100 by default.
   */
  public default int getPriority() {
    return 100;
  }
}
