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
import org.openbravo.base.expression.Evaluator;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.NamingUtil;
import org.openbravo.base.validation.PropertyValidator;
import org.openbravo.base.validation.ValidationException;

/**
 * Together with Entity, the Property is the main part of the in-memory model. A
 * property can be a primitive type, a reference or a list property.
 * 
 * TODO: consider subclasses for different types of properties
 * 
 * @author mtaal
 */

public class Property {
    private static final Logger log = Logger.getLogger(Property.class);

    private Column column;

    private boolean oneToOne;
    private boolean oneToMany;
    private Entity entity;
    private Entity targetEntity;
    private boolean id;
    private boolean primitive;
    private boolean isInactive;
    private Class<?> primitiveType;
    private Property referencedProperty;
    private String name;
    private String columnName;
    // note defaultValue contains the value as it exists in the db, for booleans
    // this for example Y or N
    private String defaultValue;
    private String minValue;
    private int fieldLength;
    private String maxValue;
    private boolean mandatory;
    private boolean identifier;
    private boolean parent;
    private boolean isUuid;
    private boolean isUpdatable;
    private Property idBasedOnProperty;
    private boolean isPartOfCompositeId;
    private boolean isOrderByProperty;
    private Set<String> allowedValues;
    private Boolean allowDerivedRead;
    private boolean isClientOrOrganization;

    private PropertyValidator validator;

    private boolean isCompositeId;
    private List<Property> idParts = new ArrayList<Property>();

    private boolean isAuditInfo;
    private boolean isTransient;

    private String transientCondition;

    public void initializeFromColumn(Column c) {
        setColumn(c);
        c.setProperty(this);
        setId(c.isKey());
        setPrimitive(c.isPrimitiveType());
        setPrimitiveType(c.getPrimitiveType());
        setIdentifier(c.isIdentifier());
        setParent(c.isParent());
        setColumnName(c.getColumnName());
        setDefaultValue(c.getDefaultValue());
        setMandatory(c.isMandatory());
        setMinValue(c.getValueMin());
        setMaxValue(c.getValueMax());
        setUuid(c.getReference().getName().equals("ID")
                && c.getReference().getId().equals("13"));
        setUpdatable(c.isUpdatable());
        setFieldLength(c.getFieldLength());
        setAllowedValues(c.getAllowedValues());
        final String columnname = c.getColumnName().toLowerCase();
        if (columnname.equals("line") || columnname.equals("seqno")
                || columnname.equals("lineno"))
            setOrderByProperty(true);
        else
            setOrderByProperty(false);

        setTransient(c.isTransient());
        setTransientCondition(c.getIsTransientCondition());

        setInactive(!c.isActive());
    }

    public void initializeName() {

        if (getName() == null) {
            setName(NamingUtil.getPropertyMappingName(this));
        }
        getEntity().addPropertyByName(this);

        if (getName().equals("created") && isPrimitive()
                && Date.class.isAssignableFrom(getPrimitiveType())) {
            setAuditInfo(true);
        } else if (getName().equals("updated") && isPrimitive()
                && Date.class.isAssignableFrom(getPrimitiveType())) {
            setAuditInfo(true);
        } else if (getName().toLowerCase().equals("updatedby")
                && !isPrimitive()) {
            setAuditInfo(true);
        } else if (getName().toLowerCase().equals("createdby")
                && !isPrimitive()) {
            setAuditInfo(true);
        } else {
            setAuditInfo(false);
        }
        if (getName().equals("client")) {
            setClientOrOrganization(true);
            getEntity().setClientEnabled(true);
        }
        if (getName().equals("organization")) {
            setClientOrOrganization(true);
            getEntity().setOrganisationEnabled(true);
        }
        if (getName().equalsIgnoreCase("isactive") && isPrimitive()) {
            getEntity().setActiveEnabled(true);
        }
    }

    public boolean isBoolean() {
        return isPrimitive()
                && (getPrimitiveType().getName().compareTo("boolean") == 0 || Boolean.class == getPrimitiveType());
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
        Check
                .isTrue(
                        isPrimitive() || isCompositeId() || isOneToMany(),
                        "Default value is only supported for composite ids, primitive types, and one-to-many properties: property "
                                + this);
        if (isCompositeId()) {
            return " new Id()";
        }

        if (isOneToMany()) {
            return " new java.util.ArrayList<Object>()";
        }

        if (defaultValue == null && isBoolean()) {
            if (getName().equalsIgnoreCase("isactive")) {
                log
                        .debug("Property "
                                + this
                                + " is probably the active column but does not have a default value set, supplying default value Y");
                defaultValue = "Y";
            } else {
                defaultValue = "N";
            }
        }

        if (defaultValue != null && isPrimitive()) {
            if (defaultValue.startsWith("@")) {
                return null;
            }
            if (defaultValue.toLowerCase().equals("sysdate")) {
                return " new java.util.Date()";
            }
            if (getPrimitiveType() == BigDecimal.class) {
                return " new java.math.BigDecimal(" + defaultValue + ")";
            }
            if (getPrimitiveType() == Float.class
                    || getPrimitiveType() == float.class) {
                return defaultValue + "f";
            }
            if (getPrimitiveType() == String.class) {
                if (defaultValue.length() > 1
                        && (defaultValue.startsWith("'") || defaultValue
                                .startsWith("\""))) {
                    defaultValue = defaultValue.substring(1);
                }
                if (defaultValue.length() > 1
                        && (defaultValue.endsWith("'") || defaultValue
                                .endsWith("\""))) {
                    defaultValue = defaultValue.substring(0, defaultValue
                            .length() - 1);
                }

                return "\"" + defaultValue + "\"";
            } else if (isBoolean()) {
                if (defaultValue.equals("Y")) {
                    return "true";
                } else if (defaultValue.equals("'Y'")) {
                    return "true";
                } else if (defaultValue.equals("'N'")) {
                    return "false";
                } else if (defaultValue.equals("N")) {
                    return "false";
                } else {
                    log.error("Illegal default value for boolean property "
                            + this + ", value should be Y or N and it is: "
                            + defaultValue);
                    return "false";
                }
            }
        }

        return defaultValue;
    }

    public Object getActualDefaultValue() {
        if (defaultValue == null && isBoolean()) {
            if (getName().equalsIgnoreCase("isactive")) {
                log
                        .debug("Property "
                                + this
                                + " is probably the active column but does not have a default value set, supplying default value Y");
                setDefaultValue("Y");
            } else {
                setDefaultValue("N");
            }
        }

        if (defaultValue != null && isPrimitive()) {
            if (defaultValue.startsWith("@")) {
                return null;
            }
            if (defaultValue.toLowerCase().equals("sysdate")) {
                return new Date();
            }
            if (getPrimitiveType() == BigDecimal.class) {
                return new BigDecimal(defaultValue);
            }
            if (getPrimitiveType() == Float.class
                    || getPrimitiveType() == float.class) {
                return new Float(defaultValue);
            }
            if (getPrimitiveType() == String.class) {
                return defaultValue;
            } else if (isBoolean()) {
                if (defaultValue.equals("Y")) {
                    return true;
                } else if (defaultValue.equals("N")) {
                    return false;
                } else {
                    log.error("Illegal default value for boolean property "
                            + this + ", value should be Y or N and it is: "
                            + defaultValue);
                    return false;
                }
            }
        }

        return null;
    }

    public boolean allowDerivedRead() {
        if (allowDerivedRead == null) {
            allowDerivedRead = isId() || isIdentifier()
                    || isClientOrOrganization();
        }
        return allowDerivedRead;
    }

    public boolean hasDefaultValue() {
        if (isCompositeId() || isOneToMany()) {
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

    public boolean isParent() {
        return parent;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
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
            System.err.println("ERROR NO REFERENCETYPE "
                    + getEntity().getName() + "." + getColumnName());
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
            return getPrimitiveObjectType().getName();
        } else {
            return getTypeName();
        }
    }

    public Class<?> getPrimitiveObjectType() {
        Check.isTrue(isPrimitive(), "Only primitive types supported here");
        final String typeName = getTypeName();
        if (typeName.indexOf('.') != -1) {
            return getPrimitiveType();
        }
        if ("boolean".equals(typeName)) {
            return Boolean.class;
        }
        if ("int".equals(typeName)) {
            return Integer.class;
        }
        if ("long".equals(typeName)) {
            return Long.class;
        }
        if ("byte".equals(typeName)) {
            return Byte.class;
        }
        if ("float".equals(typeName)) {
            return Float.class;
        }
        if ("double".equals(typeName)) {
            return Double.class;
        }
        Check.fail("Type " + typeName + " not supported as object type");
        // never gets here
        return null;
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

    // name stripped of is in case of boolean
    public String getGetterSetterName() {
        if (isBoolean() && getName().startsWith("is")) {
            return getName().substring(2);
        }
        return getName();
    }

    public void checkIsWritable() {
        if (isInactive()) {
            final ValidationException ve = new ValidationException();
            ve.addMessage(this, "Property " + this
                    + " is inactive and can therefore not be changed.");
            throw ve;
        }
    }

    // checks if the value of the object is of the correct type and it is not
    // null
    public void checkIsValidValue(Object value) {
        // note id's maybe set to null to force the creation of a new one
        // this assumes ofcourse that all ids are generated
        // also client and organization may be nullified as they are set
        // automatically
        if (value == null && isMandatory() && !isId()
                && !isClientOrOrganization()) {
            final ValidationException ve = new ValidationException();
            ve.addMessage(this, "Property " + this
                    + " is mandatory, null values are not allowed.");
            throw ve;
        }

        if (value == null) {
            return;
        }

        if (isOneToMany() && (value instanceof List)) {
            return;
        }

        if (!isPrimitive() && !(value instanceof BaseOBObjectDef)) {
            final ValidationException ve = new ValidationException();
            ve.addMessage(this, "Property " + this
                    + " only allows reference instances of type "
                    + BaseOBObjectDef.class.getName()
                    + " but the value is an instanceof "
                    + value.getClass().getName());
            throw ve;
        } else if (isPrimitive()) {
            if (!getPrimitiveObjectType().isInstance(value)) {
                final ValidationException ve = new ValidationException();
                ve.addMessage(this, "Property " + this
                        + " only allows instances of "
                        + getPrimitiveObjectType().getName()
                        + " but the value is an instanceof "
                        + value.getClass().getName());
                throw ve;
            }

            final PropertyValidator v = getValidator();
            if (v != null) {
                final String msg = v.validate(value);
                if (msg != null) {
                    final ValidationException ve = new ValidationException();
                    ve.addMessage(this, msg);
                    throw ve;
                }
            }
        }
    }

    public void setName(String name) {
        Check
                .isNotNull(name, "Name property can not be null, property "
                        + this);
        this.name = name;
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

    public boolean isOneToMany() {
        return oneToMany;
    }

    public void setOneToMany(boolean oneToMany) {
        this.oneToMany = oneToMany;
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
        for (final String s : allowedValues) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public boolean isTransient(BaseOBObjectDef bob) {
        if (isTransient()) {
            return true;
        }

        if (getTransientCondition() != null) {
            final Boolean result = Evaluator.getInstance().evaluateBoolean(bob,
                    getTransientCondition());
            return result;
        }
        return false;
    }

    public Set<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(Set<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public PropertyValidator getValidator() {
        return validator;
    }

    public void setValidator(PropertyValidator validator) {
        this.validator = validator;
    }

    public String getJavaName() {
        return NamingUtil.getSafeJavaName(getName());
    }

    public void setOrderByProperty(boolean isOrderByProperty) {
        this.isOrderByProperty = isOrderByProperty;
    }

    public boolean isOrderByProperty() {
        return isOrderByProperty;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

    public boolean isAuditInfo() {
        return isAuditInfo;
    }

    public void setAuditInfo(boolean isAuditInfo) {
        this.isAuditInfo = isAuditInfo;
        if (isAuditInfo) {
            getEntity().setTraceable(true);
        }
    }

    public String getTransientCondition() {
        return transientCondition;
    }

    public void setTransientCondition(String transientCondition) {
        this.transientCondition = transientCondition;
    }

    public boolean isClientOrOrganization() {
        return isClientOrOrganization;
    }

    public void setClientOrOrganization(boolean isClientOrOrganization) {
        this.isClientOrOrganization = isClientOrOrganization;
    }

    public boolean isInactive() {
        return isInactive;
    }

    public void setInactive(boolean isInactive) {
        this.isInactive = isInactive;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }
}
