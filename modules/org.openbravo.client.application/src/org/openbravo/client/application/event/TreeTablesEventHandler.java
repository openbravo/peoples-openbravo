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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.application.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.kernel.event.EntityDeleteEvent;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.utility.ADTreeType;
import org.openbravo.service.datasource.DataSourceService;
import org.openbravo.service.datasource.DataSourceServiceProvider;

public class TreeTablesEventHandler extends EntityPersistenceEventObserver {

  private static Entity[] entities = getTreeTables();

  private static final String TREENODE_DATASOURCE = "90034CAE96E847D78FBEF6D38CB1930D";
  private static final String LINKTOPARENT_DATASOURCE = "610BEAE5E223447DBE6FF672B703F72F";

  private static final String TREENODE_STRUCTURE = "ADTree";
  private static final String LINKTOPARENT_STRUCTURE = "LinkToParent";
  // private static final String CUSTOM_STRUCTURE = "Custom";

  protected Logger logger = Logger.getLogger(this.getClass());

  @Inject
  private DataSourceServiceProvider dataSourceServiceProvider;

  @Override
  protected Entity[] getObservedEntities() {
    return entities;
  }

  public void onNew(@Observes
  EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    BaseOBObject bob = event.getTargetInstance();
    DataSourceService dataSource = getDataSource(bob.getEntity().getTableId());
    JSONObject jsonBob = this.fromBobToJSONObject(bob);
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("jsonBob", jsonBob.toString());
    dataSource.add(parameters, null);
  }

  public void onDelete(@Observes
  EntityDeleteEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    BaseOBObject bob = event.getTargetInstance();
    DataSourceService dataSource = getDataSource(bob.getEntity().getTableId());
    JSONObject jsonBob = this.fromBobToJSONObject(bob);
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("jsonBob", jsonBob.toString());
    dataSource.remove(parameters);
  }

  private DataSourceService getDataSource(String tableId) {
    Table table = OBDal.getInstance().get(Table.class, tableId);
    ADTreeType treeType = table.getTreeCategory();

    DataSourceService dataSource = null;
    if (TREENODE_STRUCTURE.equals(treeType.getTreeStructure())) {
      dataSource = dataSourceServiceProvider.getDataSource(TREENODE_DATASOURCE);
    } else if (LINKTOPARENT_STRUCTURE.equals(treeType.getTreeStructure())) {
      dataSource = dataSourceServiceProvider.getDataSource(LINKTOPARENT_DATASOURCE);
    }
    return dataSource;
  }

  public JSONObject fromBobToJSONObject(BaseOBObject bob) {
    Entity entity = bob.getEntity();
    List<Property> propertyList = entity.getProperties();
    JSONObject jsonBob = new JSONObject();
    try {
      for (Property property : propertyList) {
        if (property.isOneToMany()) {
          continue;
        }
        if (property.getReferencedProperty() != null) {
          BaseOBObject referencedbob = (BaseOBObject) bob.get(property.getName());
          if (referencedbob != null) {
            jsonBob.put(property.getName(), referencedbob.getId());
          } else {
            jsonBob.put(property.getName(), (Object) null);
          }
        } else {
          jsonBob.put(property.getName(), bob.get(property.getName()));
        }
      }
      jsonBob.put("_entity", entity.getName());
    } catch (JSONException e) {
      logger.error("Error while converting the BOB to JsonObject", e);
    }
    return jsonBob;
  }

  private static Entity[] getTreeTables() {
    OBCriteria<Table> treeTablesCriteria = OBDal.getInstance().createCriteria(Table.class);
    treeTablesCriteria.add(Restrictions.eq(Table.PROPERTY_ISTREE, true));
    List<Table> treeTableList = treeTablesCriteria.list();
    ArrayList<Entity> entityArray = new ArrayList<Entity>();
    for (Table treeTable : treeTableList) {
      entityArray.add(ModelProvider.getInstance().getEntityByTableId(treeTable.getId()));
    }
    return (Entity[]) entityArray.toArray(new Entity[entityArray.size()]);
  }
}
