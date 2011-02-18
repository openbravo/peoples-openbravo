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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.HashSet;
import java.util.Set;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.DomainType;
import org.openbravo.base.model.domaintype.PrimitiveDomainType;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.model.ad.domain.Reference;

/**
 * A representation of the {@link DataSourceField} which can be read from the DataSourceField table
 * or created on the basis of an existing {@link Property} from the in-memory model.
 * 
 * DataSourceProperties are passed into the data source template to generate the data source
 * representation.
 * 
 * This class provides static factory methods for different ways of creating it.
 * 
 * @author mtaal
 */
public class DataSourceProperty {

  /**
   * Create a DataSourceProperty using a model property.
   * 
   * @param property
   *          the property to use to initialize the data source property
   * @return a new DataSourceProperty instance
   */
  public static DataSourceProperty createFromProperty(Property property) {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName(property.getName());
    dsProperty.setId(property.isId());
    dsProperty.setMandatory(property.isMandatory());
    dsProperty.setAuditInfo(property.isAuditInfo());
    dsProperty.setUpdatable(property.isUpdatable());
    dsProperty.setBoolean(property.isBoolean());
    dsProperty.setAllowedValues(property.getAllowedValues());
    dsProperty.setPrimitive(property.isPrimitive());
    dsProperty.setFieldLength(property.getFieldLength());
    if (dsProperty.isPrimitive()) {
      dsProperty.setPrimitiveObjectType(property.getPrimitiveObjectType());
      dsProperty.setNumericType(property.isNumericType());
      dsProperty.setDomainTypeName(property.getDomainType().getClass().getName());
    } else {
      dsProperty.setTargetEntity(property.getTargetEntity());
    }
    return dsProperty;
  }

  /**
   * Create a DataSourceProperty using a {@link DataSourceField} to initialize it.
   * 
   * @param dsField
   *          the data source field used to initialize the new data source property
   * @return a new DataSourceProperty instance
   */
  public static DataSourceProperty createFromDataSourceField(DatasourceField dsField) {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName(dsField.getName());
    dsProperty.setId(false);
    dsProperty.setMandatory(false);
    dsProperty.setAuditInfo(false);
    dsProperty.setUpdatable(false);

    // not supported by explicit data source fields
    // dsProperty.setAllowedValues(property.getAllowedValues());

    final DomainType domainType = getDomainType(dsField.getReference());
    if (domainType instanceof PrimitiveDomainType) {
      dsProperty.setDomainTypeName(domainType.getClass().getName());
      final PrimitiveDomainType primitiveDomainType = (PrimitiveDomainType) domainType;
      dsProperty.setBoolean(primitiveDomainType.getPrimitiveType() == Boolean.class);
      dsProperty.setPrimitive(true);
      // not supported by explicit data source fields yet
      // dsProperty.setFieldLength(property.getFieldLength());
      dsProperty.setPrimitiveObjectType(primitiveDomainType.getPrimitiveType());
      dsProperty.setNumericType(Number.class.isAssignableFrom(dsProperty.getPrimitiveObjectType()));
    } else {
      // TODO: make use of the column in the dsField
      Check.isTrue(dsField.getTable() != null,
          "Reference is a foreign key reference but the table is not set for this data source field "
              + dsField);
      final Entity targetEntity = ModelProvider.getInstance().getEntity(
          dsField.getTable().getName());
      dsProperty.setTargetEntity(targetEntity);
    }
    return dsProperty;
  }

  private static DomainType getDomainType(Reference reference) {
    try {
      final Class<?> clz = OBClassLoader.getInstance().loadClass(reference.getModelImpl());
      final DomainType domainType = (DomainType) clz.newInstance();
      // can't be set
      // note for our purpose this not need to be set
      // domainType.setReference(reference);
      return domainType;
    } catch (Exception e) {
      throw new OBException("Not able to create domain type for reference " + reference, e);
    }
  }

  private String name;
  private boolean id;
  private boolean mandatory;
  private boolean auditInfo;
  private boolean updatable;
  private boolean isBoolean;
  private Set<String> allowedValues = new HashSet<String>();
  private boolean primitive;
  private int fieldLength;
  private Class<?> primitiveObjectType;
  private Entity targetEntity;
  private boolean numericType;
  private String domainTypeName;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isId() {
    return id;
  }

  public void setId(boolean id) {
    this.id = id;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public boolean isAuditInfo() {
    return auditInfo;
  }

  public void setAuditInfo(boolean auditInfo) {
    this.auditInfo = auditInfo;
  }

  public boolean isUpdatable() {
    return updatable;
  }

  public void setUpdatable(boolean updatable) {
    this.updatable = updatable;
  }

  public boolean isBoolean() {
    return isBoolean;
  }

  public void setBoolean(boolean isBoolean) {
    this.isBoolean = isBoolean;
  }

  public Set<String> getAllowedValues() {
    return allowedValues;
  }

  public void setAllowedValues(Set<String> allowedValues) {
    this.allowedValues = allowedValues;
  }

  public boolean isPrimitive() {
    return primitive;
  }

  public void setPrimitive(boolean primitive) {
    this.primitive = primitive;
  }

  public int getFieldLength() {
    return fieldLength;
  }

  public void setFieldLength(int fieldLength) {
    this.fieldLength = fieldLength;
  }

  public Class<?> getPrimitiveObjectType() {
    return primitiveObjectType;
  }

  public void setPrimitiveObjectType(Class<?> primitiveObjectType) {
    this.primitiveObjectType = primitiveObjectType;
  }

  public Entity getTargetEntity() {
    return targetEntity;
  }

  public void setTargetEntity(Entity targetEntity) {
    this.targetEntity = targetEntity;
  }

  public boolean isNumericType() {
    return numericType;
  }

  public void setNumericType(boolean numericType) {
    this.numericType = numericType;
  }

  public String getDomainTypeName() {
    return domainTypeName;
  }

  public void setDomainTypeName(String domainTypeName) {
    this.domainTypeName = domainTypeName;
  }

}
