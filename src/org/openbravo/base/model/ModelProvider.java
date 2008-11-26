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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.session.UniqueConstraintColumn;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.CheckException;

/**
 * Builds the Runtime model base on the data model (application dictionary:
 * table, column, reference, etc). Makes the runtime model (Entity and Property)
 * available to the rest of the system.
 * 
 * @author iperdomo
 * @author mtaal
 */

public class ModelProvider implements OBSingleton {
    private static final Logger log = Logger.getLogger(ModelProvider.class);

    private static ModelProvider instance;
    private List<Entity> model = null;
    private List<Table> tables = null;
    private HashMap<String, Table> tablesByTableName = null;
    private List<RefTable> refTable = null;
    private List<RefSearch> refSearch = null;
    private Map<String, RefTable> refTableMap = new HashMap<String, RefTable>();
    private Map<String, RefSearch> refSearchMap = new HashMap<String, RefSearch>();
    private List<RefList> refList = null;
    private HashMap<String, Entity> entitiesByName = null;
    private HashMap<String, Entity> entitiesByClassName = null;
    private HashMap<String, Entity> entitiesByTableName = null;
    private HashMap<String, Entity> entitiesByTableId = null;
    private List<Module> modules;

    public static ModelProvider getInstance() {
        // set in a localInstance to prevent threading issues when
        // reseting it in setInstance()
        ModelProvider localInstance = instance;
        if (localInstance == null) {
            localInstance = OBProvider.getInstance().get(ModelProvider.class);
            instance = localInstance;
        }
        return localInstance;
    }

    public static void setInstance(ModelProvider instance) {
        ModelProvider.instance = instance;
    }

    public List<Table> getTables() {
        getModel();
        return tables;
    }

    public List<Entity> getModel() {
        if (model == null) {
            log.info("Building runtime model");

            // Caching model (tables, table-references, search-references,
            // list-references)
            // Changed to use the SessionHandler directly because the dal
            // layer uses the ModelProvider, so otherwise there will be a
            // cyclic relation.
            final SessionFactoryController sessionFactoryController = new ModelSessionFactoryController();
            final Session session = sessionFactoryController
                    .getSessionFactory().openSession();
            final Transaction tx = session.beginTransaction();
            try {
                log.debug("Read model from db");
                tables = list(session, Table.class);
                // read the columns in one query and assign them to the table
                final List<Column> cols = readColumns(session);
                assignColumnsToTable(cols);

                refTable = list(session, RefTable.class);
                refSearch = list(session, RefSearch.class);
                refList = list(session, RefList.class);
                modules = retrieveModules(session);

                tables = removeInactiveInvalidTables(tables);

                for (final RefTable rt : refTable) {
                    refTableMap.put(rt.getId(), rt);
                }
                for (final RefSearch rs : refSearch) {
                    // note mapped by reference id
                    refSearchMap.put(rs.getReference(), rs);
                }

                // this map stores the mapped tables
                tablesByTableName = new HashMap<String, Table>();
                for (final Table t : tables) {
                    // tables are stored case insensitive!
                    tablesByTableName.put(t.getTableName().toLowerCase(), t);
                }

                log.debug("Setting referencetypes for columns");
                for (final Table t : tablesByTableName.values()) {
                    t.setReferenceTypes(ModelProvider.instance);
                }

                log.debug("Setting List Values for columns");
                for (final RefList rl : refList) {
                    rl.setAllowedValue();
                }

                model = new ArrayList<Entity>();
                entitiesByName = new HashMap<String, Entity>();
                entitiesByClassName = new HashMap<String, Entity>();
                entitiesByTableName = new HashMap<String, Entity>();
                entitiesByTableId = new HashMap<String, Entity>();
                for (final Table t : tablesByTableName.values()) {
                    log.debug("Building model for table " + t.getTableName());
                    final Entity e = new Entity();
                    e.initialize(t);
                    model.add(e);
                    entitiesByClassName.put(e.getClassName(), e);
                    entitiesByName.put(e.getName(), e);
                    entitiesByTableName.put(t.getTableName().toUpperCase(), e);
                    entitiesByTableId.put(t.getId(), e);
                }

                // in the second pass set all the referenceProperties
                // and targetEntities
                // uses global member tablesByTableName
                setReferenceProperties();

                // add virtual property for the case that the
                // id property is also a reference (a foreign key)
                // In this case hibernate requires two mappings
                // one for the id (a string) and for the reference
                // in addition the id generation strategy should be set
                // to foreign.
                log.debug("Setting virtual property for many-to-one id's");
                setVirtualPropertiesForReferenceId();

                buildUniqueConstraints(session, sessionFactoryController);
            } finally {
                log
                        .debug("Closing session and sessionfactory used during model read");
                tx.commit();
                session.close();
                sessionFactoryController.getSessionFactory().close();
            }
        }

        // now initialize the names of the properties
        for (final Entity e : model) {
            for (final Property p : e.getProperties()) {
                p.initializeName();
            }
        }

        return model;
    }

    @SuppressWarnings("unchecked")
    private List<Column> readColumns(Session session) {
        final Criteria c = session.createCriteria(Column.class);
        c.addOrder(Order.asc("position"));
        return c.list();
    }

    private void assignColumnsToTable(List<Column> cols) {
        for (final Column column : cols) {
            final Table table = column.getTable();
            table.getColumns().add(column);
        }
    }

    private void setVirtualPropertiesForReferenceId() {

        for (final Entity e : entitiesByName.values()) {
            if (e.getIdProperties().size() == 1
                    && !e.getIdProperties().get(0).isPrimitive()) {
                createIdReferenceProperty(e);
            } else if (e.getIdProperties().size() > 1) {
                createCompositeId(e);
            }
            // add virtual property in the parent table based on
            // isParent columns
            if (e.getParentProperties().size() > 0) {
                createPropertyInParentEntity(e);
            }
        }
    }

    private void setReferenceProperties() {
        log.debug("Setting reference property");
        // uses global member tablesByTableName
        for (final Table t : tablesByTableName.values()) {
            for (final Column c : t.getColumns()) {
                if (!c.isPrimitiveType()) {
                    final Property thisProp = c.getProperty();
                    log
                            .debug("Setting targetEntity and reference Property for "
                                    + thisProp);
                    final Column thatColumn = c.getReferenceType();
                    if (thatColumn == null) {
                        log
                                .error("Property "
                                        + thisProp
                                        + " is mapped incorrectly, there is no reference column for it, removing from the mapping");
                        thisProp.getEntity().getProperties().remove(thisProp);
                        if (thisProp.getEntity().getIdProperties().remove(
                                thisProp)) {
                            Check
                                    .fail("Incorrect mapping for property "
                                            + thisProp
                                            + " which is an id, mapping fails, stopping here");
                        }
                        thisProp.getEntity().getIdentifierProperties().remove(
                                thisProp);
                        continue;
                    }
                    // targetentity is set within setReferencedProperty
                    final Property thatProperty = thatColumn.getProperty();
                    thisProp.setReferencedProperty(thatProperty);
                }
            }
        }

    }

    private List<Table> removeInactiveInvalidTables(List<Table> allTables) {
        final List<Table> toRemove = new ArrayList<Table>();
        final List<Table> localTables = allTables;
        for (final Table t : localTables) {
            if (!t.isActive()) {
                log
                        .debug("Table " + t.getName()
                                + " is not active ignoring it");
                toRemove.add(t);
                continue;
            }

            if (t.getPrimaryKeyColumns().size() == 0) {
                log.debug("Ignoring table " + t.getName()
                        + " because it has no primary key columns");
                toRemove.add(t);
                continue;
            }
        }
        allTables.removeAll(toRemove);
        return tables;
    }

    // Build unique constraints
    private void buildUniqueConstraints(Session session,
            SessionFactoryController sessionFactoryController) {
        final List<UniqueConstraintColumn> uniqueConstraintColumns = getUniqueConstraintColumns(
                session, sessionFactoryController);
        Entity entity = null;
        UniqueConstraint uniqueConstraint = null;
        for (final UniqueConstraintColumn uniqueConstraintColumn : uniqueConstraintColumns) {
            // get the entity
            if (entity == null
                    || !entity.getTableName().equalsIgnoreCase(
                            uniqueConstraintColumn.getTableName())) {
                entity = getEntityByTableName(uniqueConstraintColumn
                        .getTableName());
                uniqueConstraint = null;
            }
            if (entity == null) {
                log.warn("No entity found for "
                        + uniqueConstraintColumn.getTableName()
                        + " table, for uniqueconstraint computation");
                continue;
            }

            // the uniqueconstraint
            if (uniqueConstraint == null
                    || !uniqueConstraint.getName().equalsIgnoreCase(
                            uniqueConstraintColumn.getUniqueConstraintName())) {
                // note uniqueconstraint should be set to null, because the
                // for loop my not find another one
                uniqueConstraint = null;
                // get a new one, walk through all of them of the entity
                for (final UniqueConstraint uc : entity.getUniqueConstraints()) {
                    if (uc.getName().equalsIgnoreCase(
                            uniqueConstraintColumn.getUniqueConstraintName())) {
                        uniqueConstraint = uc;
                        break;
                    }
                }
            }
            if (uniqueConstraint == null) {
                uniqueConstraint = new UniqueConstraint();
                uniqueConstraint.setEntity(entity);
                uniqueConstraint.setName(uniqueConstraintColumn
                        .getUniqueConstraintName());
                entity.getUniqueConstraints().add(uniqueConstraint);
            }
            uniqueConstraint.addPropertyForColumn(uniqueConstraintColumn
                    .getColumnName());
        }

        // dumpUniqueConstraints();
    }

    // returns a list of uniqueconstraint columns containing all
    // uniqueconstraints
    // from the database
    public List<UniqueConstraintColumn> getUniqueConstraintColumns(
            Session session, SessionFactoryController sessionFactoryController) {
        final List<UniqueConstraintColumn> result = new ArrayList<UniqueConstraintColumn>();
        final SQLQuery sqlQuery = session
                .createSQLQuery(sessionFactoryController
                        .getUniqueConstraintQuery());
        for (final Object row : sqlQuery.list()) {
            // cast to an array of strings!
            // 0: tablename
            // 1: columnname
            // 2: uniqueconstraintname
            final Object[] values = (Object[]) row;
            Check.isTrue(values.length == 3,
                    "Unexpected value length for constraint query, should be 3, but is "
                            + values.length);
            final UniqueConstraintColumn uniqueConstraintColumn = new UniqueConstraintColumn();
            uniqueConstraintColumn.setTableName((String) values[0]);
            uniqueConstraintColumn.setColumnName((String) values[1]);
            uniqueConstraintColumn.setUniqueConstraintName((String) values[2]);
            result.add(uniqueConstraintColumn);
        }
        return result;
    }

    // expects that there is only one property
    private void createIdReferenceProperty(Entity e) {
        Check
                .isTrue(e.getIdProperties().size() == 1
                        && !e.getIdProperties().get(0).isPrimitive(),
                        "Expect one id property for the entity and it should be a reference type");
        final Property p = e.getIdProperties().get(0);
        log.debug("Handling many-to-one reference for " + p);
        Check.isTrue(e.getIdProperties().size() == 1,
                "Foreign-key id-properties are only handled if there is one in an entity "
                        + e.getName());
        // create a reference property
        final Property newProp = new Property();
        newProp.setEntity(e);
        newProp.setId(false);
        newProp.setIdentifier(p.isIdentifier());
        newProp.setMandatory(true);
        newProp.setPrimitive(false);
        newProp.setTargetEntity(p.getTargetEntity());
        newProp.setOneToOne(true);

        // the name is the name of the class of the target without
        // the package part and with the first character lowercased
        final String propName = p.getSimpleTypeName().substring(0, 1)
                .toLowerCase()
                + p.getSimpleTypeName().substring(1);
        newProp.setName(propName);
        e.addProperty(newProp);

        // and change the old id property to a primitive one
        final Property targetIdProp = p.getTargetEntity().getIdProperties()
                .get(0);
        Check
                .isTrue(
                        targetIdProp.isPrimitive(),
                        "Entity "
                                + e
                                + ", The ID property of the referenced class should be primitive, an other case is not supported");
        p.setPrimitive(true);
        p.setIdBasedOnProperty(newProp);
        p.setIdentifier(false);
        p.setTargetEntity(null);
        p.setPrimitiveType(targetIdProp.getPrimitiveType());
    }

    private void createCompositeId(Entity e) {
        Check.isTrue(e.getIdProperties().size() > 1, "Expect that entity " + e
                + " has more than one id property ");
        final Property compId = new Property();
        compId.setEntity(e);
        compId.setId(true);
        compId.setIdentifier(false);
        compId.setMandatory(true);
        compId.setPrimitive(false);
        compId.setCompositeId(true);
        compId.setName("id");
        // compId is added to the entity below

        final List<Property> toRemove = new ArrayList<Property>();
        for (final Property p : e.getIdProperties()) {
            compId.getIdParts().add(p);
            p.setPartOfCompositeId(true);
            p.setId(false);
            toRemove.add(p);
        }
        e.getIdProperties().removeAll(toRemove);
        Check.isTrue(e.getIdProperties().size() == 0,
                "There should not be any id properties (entity " + e
                        + ") at this point");

        // and now add the id property again
        e.addProperty(compId);
    }

    private void createPropertyInParentEntity(Entity e) {
        for (final Property p : e.getParentProperties()) {
            if (p.getReferencedProperty() == null) {
                continue;
            }
            final Entity parent = p.getReferencedProperty().getEntity();
            createChildProperty(parent, p);
        }
    }

    private void createChildProperty(Entity parentEntity, Property childProperty) {
        final Property newProp = new Property();

        newProp.setEntity(parentEntity);
        newProp.setId(false);
        newProp.setIdentifier(false);
        newProp.setMandatory(false);
        newProp.setPrimitive(false);
        newProp.setTargetEntity(childProperty.getEntity());
        newProp.setReferencedProperty(childProperty);
        newProp.setOneToOne(false);
        newProp.setOneToMany(true);

        parentEntity.addProperty(newProp);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> list(Session s, Class<T> clazz) {
        final Criteria c = s.createCriteria(clazz);
        return c.list();
    }

    public List<Module> getModules() {
        return modules;
    }

    @SuppressWarnings("unchecked")
    private List<Module> retrieveModules(Session s) {
        final Criteria c = s.createCriteria(Module.class);
        c.addOrder(Order.asc("seqno"));
        c.add(Expression.eq("active", true));
        return c.list();
    }

    public Table getTable(String tableName) throws CheckException {
        if (tablesByTableName == null)
            getModel();
        // search case insensitive!
        final Table table = tablesByTableName.get(tableName.toLowerCase());
        if (table == null)
            Check.fail("Table: " + tableName
                    + " not found in runtime model, is it maybe inactive?");
        return table;
    }

    public Entity getEntity(String entityName) throws CheckException {
        if (model == null)
            getModel();
        final Entity entity = entitiesByName.get(entityName);
        if (entity == null)
            Check.fail("Mapping name: " + entityName
                    + " not found in runtime model");
        return entity;
    }

    public Entity getEntityByTableName(String tableName) {
        if (model == null)
            getModel();
        final Entity entity = entitiesByTableName.get(tableName.toUpperCase());
        if (entity == null) {
            log
                    .warn("Table name: " + tableName
                            + " not found in runtime model");
        }
        return entity;
    }

    public Entity getEntity(Class<?> clz) throws CheckException {
        if (model == null)
            getModel();
        // TODO: handle subclasses, so if not found then try to find superclass!
        final Entity entity = entitiesByClassName.get(clz.getName());
        if (entity == null)
            Check.fail("Class name: " + clz.getName()
                    + " not found in runtime model");
        return entity;
    }

    public Column getColumnByReference(String reference, String referenceValue,
            char validationType, String columnName) throws CheckException {
        Column c = null;

        if (tablesByTableName == null)
            getModel();

        if (reference.equals(Reference.TABLEDIR)
                || (reference.equals(Reference.SEARCH) && referenceValue
                        .equals(Reference.NO_REFERENCE))
                || reference.equals(Reference.IMAGE)
                || reference.equals(Reference.PRODUCT_ATTRIBUTE)
                || reference.equals(Reference.RESOURCE_ASSIGNMENT)) {

            // Removing _ID from tableName based on Openbravo's naming
            // convention
            String sTable = columnName.substring(0, columnName.length() - 3);

            // TODO: solve references in the application dictionary
            // Special Cases
            if (sTable.equals("Ref_OrderLine"))
                sTable = "C_OrderLine";

            if (columnName.equals("C_Settlement_Cancel_ID")
                    || columnName.equals("C_Settlement_Generate_ID"))
                sTable = "C_Settlement";

            if (columnName.equals("Fact_Acct_Ref_ID"))
                sTable = "Fact_Acct";

            if (columnName.equals("Account_ID"))
                sTable = "C_ElementValue";

            if (columnName.equalsIgnoreCase("CreatedBy")
                    || columnName.equalsIgnoreCase("UpdatedBy"))
                sTable = "AD_User";

            try {
                c = getTable(sTable).getPrimaryKeyColumns().get(0);
            } catch (final Exception e) {
                e.printStackTrace();
            }

        } else if (reference.equals(Reference.TABLE)) {
            if (validationType == Reference.TABLE_VALIDATION) {
                final RefTable rt = refTableMap.get(referenceValue);
                if (rt != null) {
                    c = rt.getColumn();
                }
            }
        } else if (reference.equals(Reference.SEARCH)
                && !referenceValue.equals(Reference.NO_REFERENCE)) {
            if (validationType == Reference.SEARCH_VALIDATION) {
                final RefSearch rs = refSearchMap.get(referenceValue);
                if (rs != null) {
                    c = rs.getColumn();
                }
            }
        }
        if (c == null)
            Check.fail("Reference column for " + columnName
                    + " not found in runtime model [ref: " + reference
                    + ", refval: " + referenceValue + "]");
        return c;
    }
}
