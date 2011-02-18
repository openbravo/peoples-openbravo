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
package org.openbravo.service.datasource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.criterion.Expression;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

/**
 * Provides {@link DataSourceService} instances and caches them in a global cache.
 * 
 * @author mtaal
 */
public class DataSourceServiceProvider {

  private static final long serialVersionUID = 1L;

  private static DataSourceServiceProvider instance = new DataSourceServiceProvider();

  public static synchronized DataSourceServiceProvider getInstance() {
    return instance;
  }

  public static synchronized void setInstance(DataSourceServiceProvider instance) {
    DataSourceServiceProvider.instance = instance;
  }

  private Map<String, DataSourceService> dataSources = new ConcurrentHashMap<String, DataSourceService>();

  /**
   * Checks the internal cache for a datasource with the requested name and returns it if found. If
   * not found a new one is created, which is cached and then returned.
   * 
   * @param name
   *          the name by which to search and identify the data source.
   * @return a {@link DataSourceService} object
   */
  public DataSourceService getDataSource(String name) {
    // TODO: if a module is in development then it should be cached
    DataSourceService ds = dataSources.get(name);
    if (ds == null) {
      OBContext.setAdminMode();
      try {
        DataSource dataSource = OBDal.getInstance().get(DataSource.class, name);
        if (dataSource == null) {

          final OBCriteria<DataSource> obCriteria = OBDal.getInstance().createCriteria(
              DataSource.class);
          obCriteria.add(Expression.eq(DataSource.PROPERTY_NAME, name));
          if (!obCriteria.list().isEmpty()) {
            dataSource = obCriteria.list().get(0);
          }
        }
        if (dataSource == null) {
          ds = new DefaultDataSourceService();
          ds.setName(name);
          ds.setEntity(ModelProvider.getInstance().getEntity(name));
          dataSources.put(name, ds);
        } else {
          if (dataSource.getJavaClassName() != null) {
            try {
              ds = (DataSourceService) this.getClass().getClassLoader().loadClass(
                  dataSource.getJavaClassName()).newInstance();
            } catch (Exception e) {
              throw new IllegalArgumentException(
                  "Exception when instantiating datasource class for datasource " + dataSource, e);
            }
          } else {
            ds = new DefaultDataSourceService();
          }
          ds.setDataSource(dataSource);
          dataSources.put(name, ds);
        }
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    return ds;
  }
}
