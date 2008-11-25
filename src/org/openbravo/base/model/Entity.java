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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2008 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.OBClassLoader;
import org.openbravo.base.validation.AccessLevelChecker;
import org.openbravo.base.validation.EntityValidator;

/**
 * Models the business object type. The Entity is the main part of the in-memory
 * model. An entity corresponds mostly to a table in the database. An Entity has
 * properties which are primitive typed, references or lists of children.
 * 
 * @author iperdomo
 */

public class Entity {

    private List<UniqueConstraint> uniqueConstraints = new ArrayList<UniqueConstraint>();

    private List<Property> properties;
    private Map<String, Property> propertiesByName;
    private Map<String, Property> propertiesByColumnName;
    private List<Property> idProperties;
    private List<Property> identifierProperties;
    private List<Property> parentProperties;
    private List<Property> orderByProperties;

    private String name = null;
    private String tableName;
    private String tableId;
    private Class<?> mappingClass = null;
    private String className;
    private boolean isTraceable;
    private boolean isActiveEnabled;
    private boolean isOrganisationEnabled;
    private boolean isClientEnabled;
    private boolean isMutable;
    private boolean isDeletable;

    private EntityValidator entityValidator;
    private AccessLevelChecker accessLevelChecker;
    private AccessLevel accessLevel;

    // translates the columns of the table to a set of attributes
    public void initialize(Table t) {
        t.setEntity(this);
        setTableName(t.getTableName());
        setTableId(t.getId());
        setClassName(t.getPackageName() + "." + t.getNotNullClassName());
        setName(t.getName());
        setDeletable(t.isDeletable());
        setMutable(!t.isView());

        properties = new ArrayList<Property>();
        idProperties = new ArrayList<Property>();
        identifierProperties = new ArrayList<Property>();
        parentProperties = new ArrayList<Property>();
        orderByProperties = new ArrayList<Property>();
        propertiesByName = new HashMap<String, Property>();
        propertiesByColumnName = new HashMap<String, Property>();

        for (final Column c : t.getColumns()) {
            // non active columns are not mapped!
            // if (!c.isActive()) {
            // continue;
            // }

            final Property p = new Property();
            p.setEntity(this);
            p.initializeFromColumn(c);
            properties.add(p);
            propertiesByName.put(p.getName(), p);
            if (p.getColumnName() != null) {
                propertiesByColumnName.put(p.getColumnName(), p);
            }
            if (p.isId()) {
                idProperties.add(p);
            }
            if (p.isIdentifier()) {
                identifierProperties.add(p);
            }
            if (p.isParent()) {
                parentProperties.add(p);
            }
            if (p.isOrderByProperty())
                orderByProperties.add(p);
        }

        entityValidator = new EntityValidator();
        entityValidator.setEntity(this);
        entityValidator.initialize();

        if (t.getAccessLevel().equals("1")) {
            accessLevelChecker = AccessLevelChecker.ORGANIZATION;
            setAccessLevel(AccessLevel.ORGANIZATION);
        } else if (t.getAccessLevel().equals("3")) {
            accessLevelChecker = AccessLevelChecker.CLIENT_ORGANIZATION;
            setAccessLevel(AccessLevel.CLIENT_ORGANIZATION);
        } else if (t.getAccessLevel().equals("4")) {
            setAccessLevel(AccessLevel.SYSTEM);
            accessLevelChecker = AccessLevelChecker.SYSTEM;
        } else if (t.getAccessLevel().equals("6")) {
            accessLevelChecker = AccessLevelChecker.SYSTEM_CLIENT;
            setAccessLevel(AccessLevel.SYSTEM_CLIENT);
        } else if (t.getAccessLevel().equals("7")) {
            accessLevelChecker = AccessLevelChecker.ALL;
            setAccessLevel(AccessLevel.ALL);
        } else {
            Check.fail("Access level " + t.getAccessLevel() + " for table "
                    + t.getName() + " is not supported");
        }
    }

    public void addProperty(Property p) {
        getProperties().add(p);
        if (p.getColumnName() != null) {
            propertiesByColumnName.put(p.getColumnName(), p);
        }
        if (p.isIdentifier()) {
            getIdentifierProperties().add(p);
        }
        if (p.isId()) {
            getIdProperties().add(p);
        }
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void checkAccessLevel(String clientId, String orgId) {
        accessLevelChecker.checkAccessLevel(getName(), clientId, orgId);
    }

    public void validate(Object o) {
        entityValidator.validate(o);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    protected void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getMappingClass() {
        if (mappingClass == null) {
            try {
                // the context class loader is the safest one
                mappingClass = OBClassLoader.getInstance().loadClass(
                        getClassName());
            } catch (final Exception e) {
                throw new OBException(e);
            }
        }
        return mappingClass;
    }

    public void setTraceable(boolean isTraceable) {
        this.isTraceable = isTraceable;
    }

    public void setActiveEnabled(boolean isActiveEnabled) {
        this.isActiveEnabled = isActiveEnabled;
    }

    public void setOrganisationEnabled(boolean isOrganisationEnabled) {
        this.isOrganisationEnabled = isOrganisationEnabled;
    }

    public void setClientEnabled(boolean isClientEnabled) {
        this.isClientEnabled = isClientEnabled;
    }

    public String getImplementsStatement() {

        // NOTE not using the direct reference to the class for the interface
        // names
        // to prevent binary dependency
        final StringBuilder sb = new StringBuilder();
        if (isTraceable()) {
            sb.append("org.openbravo.base.structure.Traceable");
        }
        if (isClientEnabled()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("org.openbravo.base.structure.ClientEnabled");
        }
        if (isOrganisationEnabled()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("org.openbravo.base.structure.OrganizationEnabled");
        }
        if (isActiveEnabled()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("org.openbravo.base.structure.ActiveEnabled");
        }
        if (sb.length() == 0) {
            return "";
        }
        return "implements " + sb.toString();
    }

    // checks for a certain attribute
    // todo: it is saver to also check for the type!
    public boolean hasProperty(String checkMappingName) {
        for (final Property p : getProperties()) {
            if (p.getName().compareTo(checkMappingName) == 0) {
                return true;
            }
        }
        return false;
    }

    public void checkIsValidProperty(String propName) {
        if (propertiesByName.get(propName) == null) {
            throw new OBException("Property " + propName
                    + " not defined for entity " + this);
        }
    }

    public void checkValidPropertyAndValue(String propName, Object value) {
        Property p;
        if ((p = propertiesByName.get(propName)) == null) {
            throw new OBException("Property " + propName
                    + " not defined for entity " + this);
        }
        p.checkIsValidValue(value);
    }

    public void addPropertyByName(Property p) {
        propertiesByName.put(p.getName(), p);
    }

    public Property getProperty(String propName) {
        final Property prop = propertiesByName.get(propName);
        Check.isNotNull(prop, "Property " + propName
                + " does not exist for entity " + this);
        return prop;
    }

    public Property getPropertyByColumnName(String columnName) {
        final Property prop = propertiesByColumnName.get(columnName);
        Check.isNotNull(prop, "Property with " + columnName
                + " does not exist for entity " + this);
        return prop;
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
        return isTraceable;
    }

    public boolean isActiveEnabled() {
        return isActiveEnabled;
    }

    public boolean isOrganisationEnabled() {
        return isOrganisationEnabled;
    }

    public boolean isClientEnabled() {
        return isClientEnabled;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<Property> getIdentifierProperties() {
        return identifierProperties;
    }

    public void setIdentifierProperties(List<Property> identifierProperties) {
        this.identifierProperties = identifierProperties;
    }

    public List<Property> getParentProperties() {
        return parentProperties;
    }

    public void setParentProperties(List<Property> parentProperties) {
        this.parentProperties = parentProperties;
    }

    public List<Property> getOrderByProperties() {
        return this.orderByProperties;
    }

    public void setOrderByProperties(List<Property> orderByProperties) {
        this.orderByProperties = orderByProperties;
    }

    public List<Property> getIdProperties() {
        return idProperties;
    }

    public void setIdProperties(List<Property> idProperties) {
        this.idProperties = idProperties;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isMutable() {
        return isMutable;
    }

    public void setMutable(boolean isMutable) {
        this.isMutable = isMutable;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    public boolean hasCompositeId() {
        return getIdProperties().size() == 1
                && getIdProperties().get(0).isCompositeId();
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }
}
