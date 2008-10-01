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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.util.Check;
import org.openbravo.base.util.CheckException;

public class ModelProvider {
  private static final Logger log = Logger.getLogger(ModelProvider.class);
  
  private static ModelProvider instance = new ModelProvider();
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
  
  public static ModelProvider getInstance() {
    return instance;
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
      
      // Caching model (tables, table-references, search-references)
      // Changed to use the SessionHandler directly because the dal
      // layer uses the ModelProvider, so otherwise there will be a
      // cyclic relation.
      final SessionFactoryController sfc = new ModelSessionFactoryController();
      final Session s = sfc.getSessionFactory().openSession();
      final Transaction tx = s.beginTransaction();
      try {
        tables = list(s, Table.class);
        refTable = list(s, RefTable.class);
        refSearch = list(s, RefSearch.class);
        refList = list(s, RefList.class);
        
        for (RefTable rt : refTable) {
          refTableMap.put(rt.getId(), rt);
        }
        for (RefSearch rs : refSearch) {
          // note mapped by reference id
          refSearchMap.put(rs.getReference(), rs);
        }
        
        log.debug("Read model from db");
        
        model = new ArrayList<Entity>();
        entitiesByName = new HashMap<String, Entity>();
        entitiesByClassName = new HashMap<String, Entity>();
        entitiesByTableName = new HashMap<String, Entity>();
        
        // this map stores the mapped tables
        tablesByTableName = new HashMap<String, Table>();
        
        // the views have to be removed from the overall list
        // otherwise they reappear in the mapping
        final List<Table> toRemove = new ArrayList<Table>();
        
        for (Table t : tables) {
          if (!t.isActive()) {
            log.debug("Table " + t.getName() + " is not active ignoring it");
            toRemove.add(t);
            continue;
          }
          
          if (t.getPrimaryKeyColumns().size() == 0) {
            log.warn("Ignoring table " + t.getName() + " because it has no primary key columns");
            toRemove.add(t);
            continue;
          }
          // tables are stored case insensitive!
          tablesByTableName.put(t.getTableName().toLowerCase(), t);
        }
        
        tables.removeAll(toRemove);
        
        log.debug("Setting referencetypes for columns");
        for (Table t : tablesByTableName.values()) {
          t.setReferenceTypes(ModelProvider.instance);
        }
        
        log.debug("Setting List Values for columns");
        for (RefList rl : refList) {
          rl.setAllowedValue();
        }
        
        for (Table t : tablesByTableName.values()) {
          log.debug("Building model for table " + t.getTableName());
          final Entity e = new Entity();
          e.initialize(t);
          model.add(e);
          entitiesByClassName.put(e.getClassName(), e);
          entitiesByName.put(e.getName(), e);
          entitiesByTableName.put(t.getName(), e);
        }
        
        // in the second pass set all the referenceProperties
        // and targetEntities
        log.debug("Setting reference property");
        for (Table t : tablesByTableName.values()) {
          for (Column c : t.getColumns()) {
            if (!c.isActive()) {
              continue;
            }
            if (!c.isPrimitiveType()) {
              final Property thisProp = c.getProperty();
              log.debug("Setting targetEntity and reference Property for " + thisProp);
              final Column thatColumn = c.getReferenceType();
              if (thatColumn == null) {
                log.error("Property " + thisProp + " is mapped incorrectly, there is no reference column for it, removing from the mapping");
                thisProp.getEntity().getProperties().remove(thisProp);
                if (thisProp.getEntity().getIdProperties().remove(thisProp)) {
                  Check.fail("Incorrect mapping for property " + thisProp + " which is an id, mapping fails, stopping here");
                }
                thisProp.getEntity().getIdentifierProperties().remove(thisProp);
                continue;
              }
              // targetentity is set within setReferencedProperty
              final Property thatProperty = thatColumn.getProperty();
              thisProp.setReferencedProperty(thatProperty);
            }
          }
        }
        
        // add virtual property for the case that the
        // id property is also a reference (a foreign key)
        // In this case hibernate requires two mappings
        // one for the id (a string) and for the reference
        // in addition the id generation strategy should be set
        // to foreign.
        log.debug("Setting virtual property for many-to-one id's");
        for (Entity e : entitiesByName.values()) {
          if (e.getIdProperties().size() == 1 && !e.getIdProperties().get(0).isPrimitive()) {
            createIdReferenceProperty(e);
          } else if (e.getIdProperties().size() > 1) {
            createCompositeId(e);
          }
        }
      } finally {
        log.debug("Closing session and sessionfactory used during model read");
        tx.commit();
        s.close();
        sfc.getSessionFactory().close();
      }
    }
    return model;
  }
  
  // expects that there is only one property
  private void createIdReferenceProperty(Entity e) {
    Check.isTrue(e.getIdProperties().size() == 1 && !e.getIdProperties().get(0).isPrimitive(), "Expect one id property for the entity and it should be a reference type");
    final Property p = e.getIdProperties().get(0);
    log.debug("Handling many-to-one reference for " + p);
    Check.isTrue(e.getIdProperties().size() == 1, "Foreign-key id-properties are only handled if there is one in an entity " + e.getName());
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
    final String propName = p.getSimpleTypeName().substring(0, 1).toLowerCase() + p.getSimpleTypeName().substring(1);
    newProp.setName(propName);
    e.getProperties().add(newProp);
    if (newProp.isIdentifier()) {
      e.getIdentifierProperties().add(newProp);
    }
    
    // and change the old id property to a primitive one
    final Property targetIdProp = p.getTargetEntity().getIdProperties().get(0);
    Check.isTrue(targetIdProp.isPrimitive(), "Entity " + e + ", The ID property of the referenced class should be primitive, an other case is not supported");
    p.setPrimitive(true);
    p.setIdBasedOnProperty(newProp);
    p.setIdentifier(false);
    p.setTargetEntity(null);
    p.setPrimitiveType(targetIdProp.getPrimitiveType());
  }
  
  private void createCompositeId(Entity e) {
    Check.isTrue(e.getIdProperties().size() > 1, "Expect that entity " + e + " has more than one id property ");
    final Property compId = new Property();
    compId.setEntity(e);
    compId.setId(true);
    compId.setIdentifier(false);
    compId.setMandatory(true);
    compId.setPrimitive(false);
    compId.setCompositeId(true);
    compId.setName("id");
    e.getProperties().add(compId);
    
    final List<Property> toRemove = new ArrayList<Property>();
    for (Property p : e.getIdProperties()) {
      compId.getIdParts().add(p);
      p.setPartOfCompositeId(true);
      p.setId(false);
      toRemove.add(p);
    }
    e.getIdProperties().removeAll(toRemove);
    Check.isTrue(e.getIdProperties().size() == 0, "There should not be any id properties (entity " + e + ") at this point");
    e.getIdProperties().add(compId);
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Object> List<T> list(Session s, Class<T> clazz) {
    final Criteria c = s.createCriteria(clazz);
    return c.list();
  }
  
  public Table getTable(String tableName) throws CheckException {
    if (model == null)
      getModel();
    // search case insensitive!
    final Table table = tablesByTableName.get(tableName.toLowerCase());
    if (table == null)
      Check.fail("Table: " + tableName + " not found in runtime model, is it maybe inactive?");
    return table;
  }
  
  public Entity getEntity(String entityName) throws CheckException {
    if (model == null)
      getModel();
    final Entity entity = entitiesByName.get(entityName);
    if (entity == null)
      Check.fail("Mapping name: " + entityName + " not found in runtime model");
    return entity;
  }
  
  public Entity getEntityByTableName(String tableName) {
    if (model == null)
      getModel();
    final Entity entity = entitiesByTableName.get(tableName);
    if (entity == null) {
      log.warn("Table name: " + tableName + " not found in runtime model");
    }
    return entity;
  }
  
  public Entity getEntity(Class<?> clz) throws CheckException {
    if (model == null)
      getModel();
    // TODO: handle subclasses, so if not found then try to find superclass!
    final Entity entity = entitiesByClassName.get(clz.getName());
    if (entity == null)
      Check.fail("Class name: " + clz.getName() + " not found in runtime model");
    return entity;
  }
  
  public Column getColumnByReference(String reference, String referenceValue, char validationType, String columnName) throws CheckException {
    Column c = null;
    
    if (model == null)
      getModel();
    
    if (reference.equals(Reference.TABLEDIR) || (reference.equals(Reference.SEARCH) && referenceValue.equals(Reference.NO_REFERENCE)) || reference.equals(Reference.IMAGE) || reference.equals(Reference.PRODUCT_ATTRIBUTE) || reference.equals(Reference.RESOURCE_ASSIGNMENT)) {
      
      // Removing _ID from tableName based on Openbravo naming convention
      String sTable = columnName.substring(0, columnName.length() - 3);
      
      // TODO: solve references in the application dictionary
      // Special Cases
      if (sTable.equals("Ref_OrderLine"))
        sTable = "C_OrderLine";
      
      if (columnName.equals("C_Settlement_Cancel_ID") || columnName.equals("C_Settlement_Generate_ID"))
        sTable = "C_Settlement";
      
      if (columnName.equals("Fact_Acct_Ref_ID"))
        sTable = "Fact_Acct";
      
      if (columnName.equals("Account_ID"))
        sTable = "C_ElementValue";
      
      try {
        c = getTable(sTable).getPrimaryKeyColumns().get(0);
      } catch (Exception e) {
        e.printStackTrace();
      }
      
    } else if (reference.equals(Reference.TABLE)) {
      if (validationType == Reference.TABLE_VALIDATION) {
        final RefTable rt = refTableMap.get(referenceValue);
        if (rt != null) {
          c = rt.getColumn();
        }
      }
    } else if (reference.equals(Reference.SEARCH) && !referenceValue.equals(Reference.NO_REFERENCE)) {
      if (validationType == Reference.SEARCH_VALIDATION) {
        final RefSearch rs = refSearchMap.get(referenceValue);
        if (rs != null) {
          c = rs.getColumn();
        }
      }
    }
    if (c == null)
      Check.fail("Reference column for " + columnName + " not found in runtime model [ref: " + reference + ", refval: " + referenceValue + "]");
    return c;
  }
}
