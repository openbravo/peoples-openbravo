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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.openbravo.base.util.Check;

public class Property {
  private static final Logger log = Logger.getLogger(Property.class);
  
  private Column column;
  
  private boolean oneToOne;
  private Entity entity;
  private Entity targetEntity;
  private boolean id;
  private boolean primitive;
  private Class<?> primitiveType;
  private Property referencedProperty;
  private String name;
  private String columnName;
  private String defaultValue;
  private String minValue;
  private int fieldLength;
  private String maxValue;
  private boolean mandatory;
  private boolean identifier;
  private boolean isUuid;
  private boolean isUpdatable;
  private Property idBasedOnProperty;
  private boolean isPartOfCompositeId;
  private Set<String> allowedValues;
  
  private boolean isCompositeId;
  private List<Property> idParts = new ArrayList<Property>();
  
  // keeps track if this property should be exported/imported through
  // xml, forces the property to be non-mandatory in case of xml
  private boolean xmlTransient = false;
  private boolean xmlTransientSet = false;
  
  public boolean isXMLTransient() {
    if (!xmlTransientSet) {
      if (getName().equals("created") && isPrimitive() && Date.class.isAssignableFrom(getPrimitiveType())) {
        xmlTransient = true;
      } else if (getName().equals("updated") && isPrimitive() && Date.class.isAssignableFrom(getPrimitiveType())) {
        xmlTransient = true;
      } else if (getName().toLowerCase().equals("updatedby") && !isPrimitive() && getTargetEntity().getName().equals("ADUser")) {
        xmlTransient = true;
      } else if (getName().toLowerCase().equals("createdby") && !isPrimitive() && getTargetEntity().getName().equals("ADUser")) {
        xmlTransient = true;
      } else if (getName().equals("client") && !isPrimitive() && getTargetEntity().getName().equals("ADClient")) {
        xmlTransient = true;
      } else if (getName().equals("org") && !isPrimitive() && getTargetEntity().getName().equals("ADOrg")) {
        xmlTransient = true;
      } else {
        xmlTransient = false;
      }
      xmlTransientSet = true;
    }
    
    return xmlTransient;
  }
  
  public void initialize(Column c) {
    column = c;
    c.setProperty(this);
    setName(c.getMappingName());
    setId(c.isKey());
    setPrimitive(c.isPrimitiveType());
    setPrimitiveType(c.getPrimitiveType());
    setIdentifier(c.isIdentifier());
    setName(c.getMappingName());
    setColumnName(c.getColumnName());
    setDefaultValue(c.getDefaultValue());
    setMandatory(c.isMandatory());
    setMinValue(c.getValueMin());
    setMaxValue(c.getValueMax());
    setUuid(c.getReference().getName().equals("ID") && c.getReference().getId().equals("13"));
    setUpdatable(c.isUpdatable());
    setFieldLength(c.getFieldLength());
    setAllowedValues(c.getAllowedValues());
  }
  
  public boolean isBoolean() {
    return isPrimitive() && (getPrimitiveType().getName().compareTo("boolean") == 0 || Boolean.class == getPrimitiveType());
  }
  
  public Entity getEntity() {
    return entity;
  }
  
  public void setEntity(Entity entity) {
    this.entity = entity;
  }
  
  public boolean isId() {
    return id;
  }
  
  public void setId(boolean id) {
    this.id = id;
  }
  
  public boolean isPrimitive() {
    return primitive;
  }
  
  public void setPrimitive(boolean primitive) {
    this.primitive = primitive;
  }
  
  // returns null if there is no referenced property
  // this occurs in case of a reference to the primary key
  // of the referenced type
  public Property getReferencedProperty() {
    return referencedProperty;
  }
  
  public void setReferencedProperty(Property referencedProperty) {
    this.referencedProperty = referencedProperty;
    setTargetEntity(referencedProperty.getEntity());
  }
  
  public Entity getTargetEntity() {
    return targetEntity;
  }
  
  public void setTargetEntity(Entity targetEntity) {
    this.targetEntity = targetEntity;
  }
  
  public Class<?> getPrimitiveType() {
    return primitiveType;
  }
  
  public void setPrimitiveType(Class<?> primitiveType) {
    this.primitiveType = primitiveType;
  }
  
  public String getColumnName() {
    return columnName;
  }
  
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }
  
  public String getFormattedDefaultValue() {
    Check.isTrue(isPrimitive() || isCompositeId(), "Default value is only supported for composite ids and primitive types, property " + this);
    if (isCompositeId()) {
      return " new Id();";
    }
    
    if (defaultValue == null && isBoolean() && getName().equals("active")) {
      log.debug("Property " + this + " is probably the active column but does not have a default value set, supplying default value Y");
      setDefaultValue("Y");
    }
    
    if (defaultValue != null && isPrimitive()) {
      if (defaultValue.startsWith("@")) {
        return null;
      }
      if (defaultValue.toLowerCase().equals("sysdate")) {
        return " new java.util.Date();";
      }
      if (getPrimitiveType() == BigDecimal.class) {
        return " new java.math.BigDecimal(" + defaultValue + ");";
      }
      if (getPrimitiveType() == Float.class || getPrimitiveType() == float.class) {
        return defaultValue + "f";
      }
      if (getPrimitiveType() == String.class) {
        return "\"" + defaultValue + "\"";
      } else if (isBoolean()) {
        if (defaultValue.equals("Y")) {
          return "true";
        } else if (defaultValue.equals("N")) {
          return "false";
        } else {
          log.error("Illegal default value for boolean property " + this + ", value should be Y or N and it is: " + defaultValue);
          return "false";
        }
      }
    }
    
    return defaultValue;
  }
  
  public boolean hasDefaultValue() {
    if (isCompositeId()) {
      return true;
    }
    if (!isPrimitive()) {
      return false;
    }
    return getFormattedDefaultValue() != null;
  }
  
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
  
  public boolean isMandatory() {
    return mandatory;
  }
  
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }
  
  public boolean isIdentifier() {
    return identifier;
  }
  
  public void setIdentifier(boolean identifier) {
    this.identifier = identifier;
  }
  
  // returns the primitive type name or the class of the
  // referenced type
  public String getTypeName() {
    final String typeName;
    if (isCompositeId) {
      typeName = getEntity().getClassName() + ".Id";
    } else if (isPrimitive()) {
      typeName = getPrimitiveType().getName();
    } else if (getTargetEntity() == null) {
      System.err.println("ERROR NO REFERENCETYPE " + getEntity().getName() + "." + getColumnName());
      return "java.lang.Object";
    } else {
      typeName = getTargetEntity().getClassName();
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
    if (isPrimitive()) {
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
    if (!isPrimitive()) {
      return true;
    }
    return (getPrimitiveType().getName().indexOf('.') != -1);
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    Check.isNotNull(name, "Name property can not be null, property " + this);
    if (name.length() == 1) {
      this.name = name.toLowerCase();
    } else {
      this.name = name.substring(0, 1).toLowerCase() + name.substring(1);
    }
  }
  
  public String getMinValue() {
    return minValue;
  }
  
  public void setMinValue(String minValue) {
    this.minValue = minValue;
  }
  
  public String getMaxValue() {
    return maxValue;
  }
  
  public void setMaxValue(String maxValue) {
    this.maxValue = maxValue;
  }
  
  @Override
  public String toString() {
    if (getName() == null) {
      return getEntity() + "." + column;
    }
    return getEntity() + "." + getName();
  }
  
  public Property getIdBasedOnProperty() {
    return idBasedOnProperty;
  }
  
  public void setIdBasedOnProperty(Property idBasedOnProperty) {
    this.idBasedOnProperty = idBasedOnProperty;
  }
  
  public boolean isOneToOne() {
    return oneToOne;
  }
  
  public void setOneToOne(boolean oneToOne) {
    this.oneToOne = oneToOne;
  }
  
  public boolean isUuid() {
    return isUuid;
  }
  
  public void setUuid(boolean isUuid) {
    this.isUuid = isUuid;
  }
  
  public boolean isUpdatable() {
    return isUpdatable;
  }
  
  public void setUpdatable(boolean isUpdatable) {
    this.isUpdatable = isUpdatable;
  }
  
  public boolean isCompositeId() {
    return isCompositeId;
  }
  
  public void setCompositeId(boolean isCompositeId) {
    this.isCompositeId = isCompositeId;
  }
  
  public List<Property> getIdParts() {
    return idParts;
  }
  
  public boolean isPartOfCompositeId() {
    return isPartOfCompositeId;
  }
  
  public void setPartOfCompositeId(boolean isPartOfCompositeId) {
    this.isPartOfCompositeId = isPartOfCompositeId;
  }
  
  public int getFieldLength() {
    return fieldLength;
  }
  
  public void setFieldLength(int fieldLength) {
    this.fieldLength = fieldLength;
  }
  
  public boolean doCheckAllowedValue() {
    return allowedValues != null && allowedValues.size() > 0;
  }
  
  public boolean isAllowedValue(String value) {
    return allowedValues.contains(value);
  }
  
  public String concatenatedAllowedValues() {
    final StringBuffer sb = new StringBuffer();
    for (String s : allowedValues) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(s);
    }
    return sb.toString();
  }
  
  public Set<String> getAllowedValues() {
    return allowedValues;
  }
  
  public void setAllowedValues(Set<String> allowedValues) {
    this.allowedValues = allowedValues;
  }
}
