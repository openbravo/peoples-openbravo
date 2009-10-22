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

package org.openbravo.test.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.base.model.Column;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.model.Table;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the in-memory runtime model provided by the {@link ModelProvider}.
 * 
 * @see Entity
 * @see Property
 * @author iperdomo
 */

public class RuntimeModelTest extends BaseTest {

  private static final Logger log = Logger.getLogger(RuntimeModelTest.class);

  // don't initialize dal layer for model tests
  @Override
  protected void setUp() throws Exception {
    setConfigPropertyFiles();
    super.setUp();
  }

  /**
   * Iterates over the model and prints it to the log.
   */
  public void testDumpModel() {
    for (Entity e : ModelProvider.getInstance().getModel()) {
      log.debug(">>>>>>>>>>>>>> " + e.getName() + " (" + e.getTableName() + ") <<<<<<<<<<<<<<<<<");
      for (Property p : e.getProperties()) {
        log.debug(p.getName() + " (" + p.getColumnName() + ")");
      }
    }
  }

  /**
   * Checks if there are tables without a PK in the model.
   */
  public void testPK() {
    final ArrayList<Table> tablesWithoutPK = new ArrayList<Table>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (!t.isView() && t.getPrimaryKeyColumns().size() == 0) {
        tablesWithoutPK.add(t);
      }
    }
    if (tablesWithoutPK.size() != 0) {
      log.debug("Tables without primary keys defined:");
      for (final Table t2 : tablesWithoutPK) {
        log.debug(t2);
      }
    }
    assertEquals(0, tablesWithoutPK.size());
  }

  /**
   * Just checks that there is a {@link ModelProvider} and that it returns a model (a list of
   * {@link Entity} objects).
   */
  public void testModelProvider() {
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      log.debug("tablename: " + e.getTableName() + " -- classname: " + e.getClassName()
          + " -- mappingname: " + e.getName());
      for (final Property p : e.getProperties())
        log.debug("property: " + p.getColumnName() + " -- mapping: " + p.getName());
    }
    assertNotNull(ModelProvider.getInstance().getModel());
  }

  /**
   * Checks that entities have a unique name.
   * 
   * @see Entity#getName()
   */
  public void testUniqueTableMapping() {
    final List<String> mappings = new ArrayList<String>();
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (mappings.contains(e.getName())) {
        log.debug("Duplicated table mapping name: " + e.getName());
        duplicated = true;
        break;
      }
      mappings.add(e.getName());
    }
    assertFalse(duplicated);
  }

  /**
   * Checks that all names of properties are unique within an Entity.
   * 
   * @see Property#getName()
   */
  public void testUniqueColumnMapping() {
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      final List<String> propMappings = new ArrayList<String>();
      for (final Property p : e.getProperties()) {
        if (!p.isOneToMany() && propMappings.contains(p.getName())) {
          log.debug("Duplicated column mapping name: " + p.getName() + " -- column: "
              + p.getColumnName() + " -- table: " + e.getTableName());
          duplicated = true;
        }
        propMappings.add(p.getName());
      }
    }
    assertFalse(duplicated);
  }

  /**
   * Tests that each entity/table has only one PK.
   */
  public void testOnePK() {
    int total = 0;
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (!t.isView() && t.getPrimaryKeyColumns().size() > 1) {
        log.debug("Table: " + t.getId() + " - " + t.getTableName());
        log.debug("  Columns : ");
        for (final Column c : t.getColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        log.debug("    Keys: ");
        for (final Column c : t.getPrimaryKeyColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        log.debug("    Identifiers: ");
        for (final Column c : t.getIdentifierColumns())
          log.debug(c.getColumnName() + ", ");
        log.debug("\n");
        total++;
      }
    }
    if (total != 0)
      log.debug(total + " tables with more than one primary key");
    assertEquals(0, total);
  }

  public void testIdentifiers() {
    final ArrayList<String> tables = new ArrayList<String>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (!t.isView() && t.isActive() && t.getIdentifierColumns().size() == 0)
        tables.add(t.getTableName());
    }
    if (tables.size() != 0) {
      log.debug(tables.size() + " tables without Identifier columns");
      for (final String tableName : tables)
        log.debug(tableName);
    }
    assertEquals(0, tables.size());
  }

  /**
   * Tests that parent references are only allowed for specific reference types.
   * 
   * @see Column#getReference()
   * @see Reference
   */
  public void testIsParent() {
    final ArrayList<String> columns = new ArrayList<String>();

    for (final Table t : ModelProvider.getInstance().getTables()) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && !c.getReference().getId().equals(Reference.TABLE)
            && !c.getReference().getId().equals(Reference.TABLEDIR)
            && !c.getReference().getId().equals(Reference.SEARCH)
            && !c.getReference().getId().equals(Reference.PRODUCT_ATTRIBUTE)) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0) {
      log.debug(columns.size() + " columns set as *isParent* errors (wrong reference): "
          + columns.toString());
    }
    assertEquals(0, columns.size());
  }

  /**
   * Tests that columns that has {@link Column#isParent()} on true are not of a primitive type.
   */
  public void testIsParent2() {
    final ArrayList<String> columns = new ArrayList<String>();

    for (final Table t : ModelProvider.getInstance().getTables()) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.isPrimitiveType()) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log.debug(columns.size() + " columns set as *isParent* and are *primitive type*: "
          + columns.toString());
    assertEquals(0, columns.size());
  }

  /**
   * Checks that a column that has {@link Column#isParent()} on true has a table defined.
   */
  public void testIsParent3() {
    final ArrayList<String> columns = new ArrayList<String>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.getReference().getId().equals(Reference.TABLE)
            && c.getReferenceValue() == null) {
          columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log.debug(columns.size()
          + " columns set as *isParent* with reference *TABLE* and don't have table defined : "
          + columns.toString());
    assertEquals(0, columns.size());
  }

  /**
   * Checks that a column that has {@link Column#isParent()} finishes on _ID.
   */
  public void testIsParent4() {
    final ArrayList<String> columns = new ArrayList<String>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      for (final Column c : t.getColumns()) {
        if (c.isParent() && c.getReference().getId().equals(Reference.TABLEDIR)) {
          final String obNamingConvention = c.getColumnName().substring(
              c.getColumnName().length() - 3);
          if (!obNamingConvention.equals("_ID"))
            columns.add(t.getTableName() + " - " + c.getColumnName());
        }
      }
    }

    if (columns.size() != 0)
      log
          .debug(columns.size()
              + " columns set as *isParent* with reference *TABLEDIR* and column name don't finish with _ID: "
              + columns.toString());
    assertEquals(0, columns.size());
  }
}
