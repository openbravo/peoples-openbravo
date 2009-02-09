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

import org.openbravo.base.model.Column;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.Reference;
import org.openbravo.base.model.Table;
import org.openbravo.test.base.BaseTest;

/**
 * 
 * @author iperdomo
 */

public class RuntimeModelTest extends BaseTest {

  // don't initialize dal layer for model tests
  @Override
  protected void setUp() throws Exception {
    setConfigPropertyFiles();
    super.setUp();
  }

  public void testDumpModel() {
    for (Entity e : ModelProvider.getInstance().getModel()) {
      System.err.println(">>>>>>>>>>>>>> " + e.getName() + " (" + e.getTableName()
          + ") <<<<<<<<<<<<<<<<<");
      for (Property p : e.getProperties()) {
        System.err.println(p.getName() + " (" + p.getColumnName() + ")");
      }
    }
  }

  public void testPK() {
    final ArrayList<Table> tablesWithoutPK = new ArrayList<Table>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (t.getPrimaryKeyColumns().size() == 0) {
        tablesWithoutPK.add(t);
      }
    }
    if (tablesWithoutPK.size() != 0) {
      System.err.println("Tables without primary keys defined:");
      for (final Table t2 : tablesWithoutPK) {
        System.err.println(t2);
      }
    }
    assertEquals(0, tablesWithoutPK.size());
  }

  public void testModelProvider() {
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      System.out.println("tablename: " + e.getTableName() + " -- classname: " + e.getClassName()
          + " -- mappingname: " + e.getName());
      for (final Property p : e.getProperties())
        System.out.println("property: " + p.getColumnName() + " -- mapping: " + p.getName());
    }
    assertNotNull(ModelProvider.getInstance().getModel());
  }

  public void testUniqueTableMapping() {
    final List<String> mappings = new ArrayList<String>();
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      if (mappings.contains(e.getName())) {
        System.err.println("Duplicated table mapping name: " + e.getName());
        duplicated = true;
        break;
      }
      mappings.add(e.getName());
    }
    assertFalse(duplicated);
  }

  public void testUniqueColumnMapping() {
    boolean duplicated = false;
    for (final Entity e : ModelProvider.getInstance().getModel()) {
      final List<String> propMappings = new ArrayList<String>();
      for (final Property p : e.getProperties()) {
        if (!p.isOneToMany() && propMappings.contains(p.getName())) {
          System.err.println("Duplicated column mapping name: " + p.getName() + " -- column: "
              + p.getColumnName() + " -- table: " + e.getTableName());
          duplicated = true;
        }
        propMappings.add(p.getName());
      }
    }
    assertFalse(duplicated);
  }

  public void testOnePK() {
    int total = 0;
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (!t.isView() && t.getPrimaryKeyColumns().size() > 1) {
        System.out.println("Table: " + t.getId() + " - " + t.getTableName());
        System.out.print("  Columns : ");
        for (final Column c : t.getColumns())
          System.out.print(c.getColumnName() + ", ");
        System.out.print("\n");
        System.out.print("    Keys: ");
        for (final Column c : t.getPrimaryKeyColumns())
          System.out.print(c.getColumnName() + ", ");
        System.out.print("\n");
        System.out.print("    Identifiers: ");
        for (final Column c : t.getIdentifierColumns())
          System.out.print(c.getColumnName() + ", ");
        System.out.print("\n");
        total++;
      }
    }
    if (total != 0)
      System.err.println(total + " tables with more than one primary key");
    assertEquals(0, total);
  }

  public void testIdentifiers() {
    final ArrayList<String> tables = new ArrayList<String>();
    for (final Table t : ModelProvider.getInstance().getTables()) {
      if (!t.isView() && t.isActive() && t.getIdentifierColumns().size() == 0)
        tables.add(t.getTableName());
    }
    if (tables.size() != 0) {
      System.err.println(tables.size() + " tables without Identifier columns");
      for (final String tableName : tables)
        System.err.println(tableName);
    }
    // assertEquals(0, tables.size());
  }

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
      System.err.println(columns.size() + " columns set as *isParent* errors (wrong reference): "
          + columns.toString());
    }
    assertEquals(0, columns.size());
  }

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
      System.err.println(columns.size() + " columns set as *isParent* and are *primitive type*: "
          + columns.toString());
    assertEquals(0, columns.size());
  }

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
      System.err.println(columns.size()
          + " columns set as *isParent* with reference *TABLE* and don't have table defined : "
          + columns.toString());
    assertEquals(0, columns.size());
  }

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
      System.err
          .println(columns.size()
              + " columns set as *isParent* with reference *TABLEDIR* and column name don't finish with _ID: "
              + columns.toString());
    assertEquals(0, columns.size());
  }
}
