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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.util.NamingUtil;

public class Column extends ModelObject {
  private static final Logger log = Logger.getLogger(Column.class);
  
  private Property property;
  private String columnName;
  private Table table;
  private Reference reference;
  private Reference referenceValue;
  private Column referenceType = null;
  private int fieldLength;
  private String defaultValue;
  private boolean key;
  private boolean secondaryKey;
  private boolean parent;
  private boolean mandatory;
  private boolean updatable;
  private boolean identifier;
  private String valueMin;
  private String valueMax;
  private String mappingName = null;
  private String developmentStatus;
  
  public boolean isBoolean() {
    return isPrimitiveType() && (getPrimitiveType().getName().compareTo("boolean") == 0 || Boolean.class == getPrimitiveType());
  }
  
  public String getColumnName() {
    return columnName;
  }
  
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }
  
  public Table getTable() {
    return table;
  }
  
  public void setTable(Table table) {
    this.table = table;
  }
  
  public Reference getReference() {
    return reference;
  }
  
  public void setReference(Reference reference) {
    this.reference = reference;
  }
  
  public Reference getReferenceValue() {
    return referenceValue;
  }
  
  public void setReferenceValue(Reference referenceValue) {
    this.referenceValue = referenceValue;
  }
  
  public int getFieldLength() {
    return fieldLength;
  }
  
  public void setFieldLength(int fieldLength) {
    this.fieldLength = fieldLength;
  }
  
  public String getDefaultValue() {
    return defaultValue;
  }
  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public boolean isKey() {
    return key;
  }
  
  public void setKey(Boolean key) {
    this.key = key;
  }
  
  public boolean isSecondaryKey() {
    return secondaryKey;
  }
  
  public void setSecondaryKey(boolean secondaryKey) {
    this.secondaryKey = secondaryKey;
  }
  
  public boolean isParent() {
    return parent;
  }
  
  public void setParent(Boolean parent) {
    this.parent = parent;
  }
  
  public boolean isMandatory() {
    return mandatory;
  }
  
  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }
  
  public boolean isUpdatable() {
    return updatable;
  }
  
  public void setUpdatable(Boolean updatable) {
    this.updatable = updatable;
  }
  
  public boolean isIdentifier() {
    return identifier;
  }
  
  public void setIdentifier(Boolean identifier) {
    this.identifier = identifier;
  }
  
  public String getValueMin() {
    return valueMin;
  }
  
  public void setValueMin(String valueMin) {
    this.valueMin = valueMin;
  }
  
  public String getValueMax() {
    return valueMax;
  }
  
  public void setValueMax(String valueMax) {
    this.valueMax = valueMax;
  }
  
  public String getDevelopmentStatus() {
    return developmentStatus;
  }
  
  public void setDevelopmentStatus(String developmentStatus) {
    this.developmentStatus = developmentStatus;
  }
  
  public String getMappingName() {
    // TODO: check the mappingname of an pk is always id
    if (isKey() && getTable().getPrimaryKeyColumns().size() == 1) {
      return "id";
    }
    if (mappingName == null)
      mappingName = NamingUtil.getColumnMappingName(this);
    return mappingName;
  }
  
  public boolean isPrimitiveType() {
    if (!reference.getId().equals(Reference.TABLE) && !reference.getId().equals(Reference.TABLEDIR) && !reference.getId().equals(Reference.SEARCH) && !reference.getId().equals(Reference.IMAGE) && !reference.getId().equals(Reference.PRODUCT_ATTRIBUTE) && !reference.getId().equals(Reference.RESOURCE_ASSIGNMENT))
      return true;
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public Class getPrimitiveType() {
    if (isPrimitiveType())
      return Reference.getPrimitiveType(reference.getId());
    return null;
  }
  
  public Column getReferenceType() {
    if (!isPrimitiveType())
      return referenceType;
    return null;
  }
  
  public void setReferenceType(Column column) {
    this.referenceType = column;
  }
  
  @Override
  public boolean isActive() {
    if (super.isActive() && !isPrimitiveType()) {
      final Column thatColumn = getReferenceType();
      
      if (thatColumn != null && (!thatColumn.isActive() || !thatColumn.getTable().isActive() || thatColumn.getTable().isView())) {
        log.error("Column " + this + " refers to a non active table or column or to a view" + thatColumn);
        System.err.println("Column " + this + " refers to a non active table or column or to a view" + thatColumn);
        return false;
      }
    }
    return super.isActive();
  }
  
  protected void setReferenceType(ModelProvider modelProvider) {
    
    // reference type does not need to be set
    if (isPrimitiveType()) {
      return;
    }
    
    try {
      String referenceId = reference.getId();
      String referenceValueId = (referenceValue != null ? referenceValue.getId() : Reference.NO_REFERENCE);
      char validationType = (referenceValue != null ? referenceValue.getValidationType() : reference.getValidationType());
      Column c = modelProvider.getColumnByReference(referenceId, referenceValueId, validationType, getColumnName());
      if (c != null)
        setReferenceType(c);
    } catch (Exception e) {
      System.out.println("Error >> tableName: " + table.getTableName() + " - columnName: " + getColumnName());
      e.printStackTrace();
    }
  }
  
  // returns the primitive type name or the class of the
  // referenced type
  public String getTypeName() {
    final String typeName;
    if (isPrimitiveType()) {
      typeName = getPrimitiveType().getName();
    } else if (getReferenceType() == null) {
      System.err.println("ERROR NO REFERENCETYPE " + getTable().getMappingName() + "." + getColumnName());
      return "java.lang.Object";
    } else {
      typeName = getReferenceType().getTable().getClassName();
    }
    return typeName;
  }
  
  // the last part of the class name
  public String getSimpleTypeName() {
    final String typeName = getTypeName();
    if (typeName.indexOf(".") == -1) {
      return typeName;
    }
    return typeName.substring(1 + typeName.lastIndexOf("."));
  }
  
  // returns the typename as an object variant
  public String getObjectTypeName() {
    if (isPrimitiveType()) {
      final String typeName = getTypeName();
      if (typeName.indexOf('.') != -1) {
        return typeName;
      }
      if ("boolean".equals(typeName)) {
        return Boolean.class.getName();
      }
      if ("int".equals(typeName)) {
        return Integer.class.getName();
      }
      if ("long".equals(typeName)) {
        return Long.class.getName();
      }
      if ("byte".equals(typeName)) {
        return Byte.class.getName();
      }
      if ("float".equals(typeName)) {
        return Float.class.getName();
      }
      if ("double".equals(typeName)) {
        return Double.class.getName();
      }
      // TODO: maybe throw an exception
      return typeName;
    } else {
      return getTypeName();
    }
  }
  
  // method added for oaw template
  public boolean allowNullValues() {
    if (!isPrimitiveType()) {
      return true;
    }
    return (getPrimitiveType().getName().indexOf('.') != -1);
  }
  
  public Property getProperty() {
    return property;
  }
  
  public void setProperty(Property property) {
    this.property = property;
  }
  
  @Override
  public String toString() {
    return getTable() + "." + getColumnName();
  }
  
  public Set<String> getAllowedValues() {
    if (getReferenceValue() != null) {
      return getReferenceValue().getAllowedValues();
    }
    return new HashSet<String>();
  }
}
