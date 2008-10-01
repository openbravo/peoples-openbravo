/*
 * 
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SL All
 * portions are Copyright (C) 2008 Openbravo SL All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package org.openbravo.base.model;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.util.NamingUtil;

public class Table extends ModelObject {
  private Entity entity;
  private String tableName;
  private boolean view;
  private boolean isDeletable;
  private List<Column> columns;
  private List<Column> primaryKeyColumns = null;
  private List<Column> identifierColumns = null;
  private String mappingName = null;
  private String className = null;
  
  private boolean areEnabledMembersComputed = false;
  private boolean isTraceable;
  private boolean isActiveEnabled;
  private boolean isOrganisationEnabled;
  private boolean isClientEnabled;
  
  private String accessLevel;
  
  public String getTableName() {
    return tableName;
  }
  
  public void setTableName(String tableName) {
    this.tableName = tableName;
  }
  
  public List<Column> getColumns() {
    return columns;
  }
  
  public void setColumns(List<Column> columns) {
    this.columns = columns;
  }
  
  public List<Column> getPrimaryKeyColumns() {
    if (primaryKeyColumns == null) {
      primaryKeyColumns = new ArrayList<Column>();
      
      for (Column c : getColumns()) {
        if (c.isKey())
          primaryKeyColumns.add(c);
      }
    }
    return primaryKeyColumns;
  }
  
  public void setPrimaryKeyColumns(List<Column> primaryKeyColumns) {
    this.primaryKeyColumns = primaryKeyColumns;
  }
  
  public List<Column> getIdentifierColumns() {
    if (identifierColumns == null) {
      identifierColumns = new ArrayList<Column>();
      for (Column c : getColumns()) {
        if (c.isIdentifier())
          identifierColumns.add(c);
      }
    }
    return identifierColumns;
  }
  
  public void setIdentifierColumns(List<Column> identifierColumns) {
    this.identifierColumns = identifierColumns;
  }
  
  public void setView(boolean view) {
    this.view = view;
  }
  
  public boolean isView() {
    return view;
  }
  
  public String getMappingName() {
    if (mappingName == null)
      mappingName = NamingUtil.getMappingName(getName());
    return mappingName;
  }
  
  public String getClassName() {
    if (className == null)
      className = NamingUtil.getPackageName(getName()) + "." + NamingUtil.getEntityName(getName());
    return className;
  }
  
  public void setReferenceTypes(ModelProvider modelProvider) {
    for (Column c : columns) {
      if (!c.isPrimitiveType() && !c.getColumnName().equals("Node_ID")) // TODO:
        // AD_TreeNode
        // is a
        // special
        // case
        c.setReferenceType(modelProvider);
    }
  }
  
  private void setEnabledMembers() {
    if (areEnabledMembersComputed) {
      return;
    }
    isTraceable = hasColumn("createdby");
    isActiveEnabled = hasColumn("active");
    isOrganisationEnabled = hasColumn("org");
    isClientEnabled = hasColumn("client");
    areEnabledMembersComputed = true;
  }
  
  // checks for a certain column
  // todo: it is saver to also check for the type!
  private boolean hasColumn(String checkMappingName) {
    for (Column c : getColumns()) {
      if (!c.isKey() && c.getMappingName().compareTo(checkMappingName) == 0) {
        return true;
      }
    }
    return false;
  }
  
  public String getPackageName() {
    final int lastIndexOf = getClassName().lastIndexOf('.');
    return getClassName().substring(0, lastIndexOf);
  }
  
  public String getSimpleClassName() {
    final int lastIndexOf = getClassName().lastIndexOf('.');
    return getClassName().substring(1 + lastIndexOf);
  }
  
  public boolean isTraceable() {
    setEnabledMembers();
    return isTraceable;
  }
  
  public boolean isActiveEnabled() {
    setEnabledMembers();
    return isActiveEnabled;
  }
  
  public boolean isOrganisationEnabled() {
    setEnabledMembers();
    return isOrganisationEnabled;
  }
  
  public boolean isClientEnabled() {
    setEnabledMembers();
    return isClientEnabled;
  }
  
  public Entity getEntity() {
    return entity;
  }
  
  public void setEntity(Entity entity) {
    this.entity = entity;
  }
  
  @Override
  public String toString() {
    return getTableName();
  }
  
  public boolean isDeletable() {
    return isDeletable;
  }
  
  public void setDeletable(boolean isDeletable) {
    this.isDeletable = isDeletable;
  }
  
  public String getAccessLevel() {
    return accessLevel;
  }
  
  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }
}
