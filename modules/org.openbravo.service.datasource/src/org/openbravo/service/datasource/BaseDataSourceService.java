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
 * All portions are Copyright (C) 2010-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.application.CachedPreference;
import org.openbravo.client.kernel.Template;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.json.JsonConstants;

/**
 * A base data source service which can be extended. It combines the common parts for data sources
 * which are based on an entity and full-computed data sources.
 * 
 * @author mtaal
 */
public abstract class BaseDataSourceService implements DataSourceService {
  private static final Logger log = Logger.getLogger(BaseDataSourceService.class);

  private String name;
  private Template template;

  // TODO: move this to a config parameter
  private String dataUrl = DataSourceServlet.getServletPathPart() + "/";

  private String whereClause = null;
  private Entity entity;
  private DataSource dataSource;
  private List<DataSourceProperty> dataSourceProperties = new ArrayList<DataSourceProperty>();
  private static final String ALLOW_WHERE_PREFERENCE = "OBSERDS_AllowWhereParameter";
  private static final String WARN_MESSAGE = "The '_where' parameter has been included in the request. The provided value will be used by the datasource because the OBSERDS_AllowWhereParameter preference is set to true.";

  @Inject
  private CachedPreference cachedPreference;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSourceService#getTemplate()
   */
  public Template getTemplate() {
    if (template == null) {
      template = OBDal.getInstance().get(Template.class, DataSourceConstants.DS_TEMPLATE_ID);
      if (template == null) {
        log.error("The default data source template with id " + DataSourceConstants.DS_TEMPLATE_ID
            + " is not present in the database. This is an error!");
      }
    }
    return template;
  }

  /**
   * @deprecated returned class {@link DataSourceJavaScriptCreator} is deprecated
   */
  @Deprecated
  protected DataSourceJavaScriptCreator getJavaScriptCreator() {
    return new DataSourceJavaScriptCreator();
  }

  public String getName() {
    return name;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public void setDataUrl(String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public String getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(String whereClause) {
    this.whereClause = whereClause;
  }

  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    return dataSourceProperties;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    setName(dataSource.getId());
    dataSourceProperties = new ArrayList<DataSourceProperty>();
    for (DatasourceField dsField : dataSource.getOBSERDSDatasourceFieldList()) {
      if (dsField.isActive()) {
        dataSourceProperties.add(DataSourceProperty.createFromDataSourceField(dsField));
      }
    }
    if (dataSource.getTable() != null) {
      setEntity(ModelProvider.getInstance().getEntity(dataSource.getTable().getName()));
    }
    setWhereClause(dataSource.getHQLWhereClause());
  }

  public Entity getEntity() {
    return entity;
  }

  public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * This method returns a String with the where and filter clauses that will be applied.
   *
   * @return A String with the value of the where and filter clause. It can be null when there is no
   *         filter clause nor where clause.
   */
  protected String getWhereAndFilterClause(Map<String, String> parameters, Entity ent) {
    String whereAndFilterClause = null;
    if (("Y".equals(cachedPreference.getPreferenceValue(ALLOW_WHERE_PREFERENCE)))
        && parameters.containsKey(JsonConstants.WHERE_PARAMETER)) {
      log.warn(WARN_MESSAGE);
      if (getWhereClause() != null) {
        if (parameters.get(JsonConstants.WHERE_PARAMETER) != null) {
          final String currentWhere = parameters.get(JsonConstants.WHERE_PARAMETER);
          whereAndFilterClause = "(" + currentWhere + ") and (" + getWhereClause() + ")";
        } else if (!"Y".equals(cachedPreference.getPreferenceValue(ALLOW_WHERE_PREFERENCE))) {
          whereAndFilterClause = getWhereClause();
        }
      }
    } else {
      String tabId = parameters.get(JsonConstants.TAB_PARAMETER);
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      String where = tab.getHqlwhereclause();
      if (isFilterApplied(parameters)) {
        String filterClause = getFilterClause(tab, ent);
        if (StringUtils.isNotBlank(where)) {
          whereAndFilterClause = " ((" + where + ") and (" + filterClause + "))";
        } else {
          whereAndFilterClause = filterClause;
        }
      } else {
        if (StringUtils.isNotBlank(where)) {
          whereAndFilterClause = where;
        } else {
          whereAndFilterClause = null;
        }
      }
    }
    return whereAndFilterClause;

  }

  private boolean isRootTab(Tab tab) {
    boolean isRootLevel;
    Long tabLevel = tab.getTabLevel();
    if (tabLevel == 0) {
      isRootLevel = true;
    } else {
      isRootLevel = false;
    }
    return isRootLevel;
  }

  private String getFilterClause(Tab tab, Entity ent) {
    boolean isTransactionalWindow = tab.getWindow().getWindowType().equals("T");
    if (tab.getHqlfilterclause() != null) {
      return addTransactionalFilter(tab.getHqlfilterclause(), tab, isTransactionalWindow, ent);
    }
    return addTransactionalFilter("", tab, isTransactionalWindow, ent);
  }

  private String addTransactionalFilter(String filterClause, Tab tab,
      boolean isTransactionalWindow, Entity ent) {
    if (!isTransactionalFilterApplied(isTransactionalWindow, tab)) {
      return filterClause;
    }
    String transactionalFilter = " e.updated > " + JsonConstants.QUERY_PARAM_TRANSACTIONAL_RANGE
        + " ";
    if (ent.hasProperty(Order.PROPERTY_PROCESSED)) {
      transactionalFilter += " or e.processed = 'N' ";
    }
    transactionalFilter = " (" + transactionalFilter + ") ";

    if (filterClause.length() > 0) {
      return " (" + transactionalFilter + " and (" + filterClause + ")) ";
    }
    return transactionalFilter;
  }

  private boolean isTransactionalFilterApplied(boolean isTransactionalWindow, Tab tab) {
    boolean applies;
    if (isTransactionalWindow && isRootTab(tab)) {
      applies = true;
    } else {
      applies = false;
    }
    return applies;
  }

  private boolean isFilterApplied(Map<String, String> parameters) {
    if ("true".equals(parameters.get(JsonConstants.FILTER_APPLIED_PARAMETER))) {
      return true;
    } else {
      return false;
    }
  }
}
