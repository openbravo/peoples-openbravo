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
    protected void setUp() {
	setConfigPropertyFiles();
    }

    public void testModelProvider() {
	for (Entity e : ModelProvider.getInstance().getModel()) {
	    System.out.println("tablename: " + e.getTableName()
		    + " -- classname: " + e.getClassName()
		    + " -- mappingname: " + e.getName());
	    for (Property p : e.getProperties())
		System.out.println("property: " + p.getColumnName()
			+ " -- mapping: " + p.getName());
	}
	assertNotNull(ModelProvider.getInstance().getModel());
    }

    public void testUniqueTableMapping() {
	List<String> mappings = new ArrayList<String>();
	boolean duplicated = false;
	for (Entity e : ModelProvider.getInstance().getModel()) {
	    if (mappings.contains(e.getName())) {
		System.err.println("Duplicated table mapping name: "
			+ e.getName());
		duplicated = true;
		break;
	    }
	    mappings.add(e.getName());
	}
	assertFalse(duplicated);
    }

    public void testUniqueColumnMapping() {
	boolean duplicated = false;
	for (Entity e : ModelProvider.getInstance().getModel()) {
	    List<String> propMappings = new ArrayList<String>();
	    for (Property p : e.getProperties()) {
		if (!p.isOneToMany() && propMappings.contains(p.getName())) {
		    System.err.println("Duplicated column mapping name: "
			    + p.getName() + " -- column: " + p.getColumnName()
			    + " -- table: " + e.getTableName());
		    duplicated = true;
		}
		propMappings.add(p.getName());
	    }
	}
	assertFalse(duplicated);
    }

    public void testOnePK() {
	int total = 0;
	for (Table t : ModelProvider.getInstance().getTables()) {
	    if (!t.isView() && t.getPrimaryKeyColumns().size() > 1) {
		System.out.println("Table: " + t.getId() + " - "
			+ t.getTableName());
		System.out.print("  Columns : ");
		for (Column c : t.getColumns())
		    System.out.print(c.getColumnName() + ", ");
		System.out.print("\n");
		System.out.print("    Keys: ");
		for (Column c : t.getPrimaryKeyColumns())
		    System.out.print(c.getColumnName() + ", ");
		System.out.print("\n");
		System.out.print("    Identifiers: ");
		for (Column c : t.getIdentifierColumns())
		    System.out.print(c.getColumnName() + ", ");
		System.out.print("\n");
		total++;
	    }
	}
	if (total != 0)
	    System.err
		    .println(total + " tables with more than one primary key");
	assertEquals(0, total);
    }

    public void testIdentifiers() {
	ArrayList<String> tables = new ArrayList<String>();
	for (Table t : ModelProvider.getInstance().getTables()) {
	    if (!t.isView() && t.isActive()
		    && t.getIdentifierColumns().size() == 0)
		tables.add(t.getTableName());
	}
	if (tables.size() != 0) {
	    System.err.println(tables.size()
		    + " tables without Identifier columns");
	    for (String tableName : tables)
		System.err.println(tableName);
	}
	// assertEquals(0, tables.size());
    }

    public void testIsParent() {
	ArrayList<String> columns = new ArrayList<String>();

	for (Table t : ModelProvider.getInstance().getTables()) {
	    for (Column c : t.getColumns()) {
		if (c.isParent()
			&& !c.getReference().getId().equals(Reference.TABLE)
			&& !c.getReference().getId().equals(Reference.TABLEDIR)
			&& !c.getReference().getId().equals(Reference.SEARCH)
			&& !c.getReference().getId().equals(
				Reference.PRODUCT_ATTRIBUTE)) {
		    columns.add(t.getTableName() + " - " + c.getColumnName());
		}
	    }
	}

	if (columns.size() != 0) {
	    System.err.println(columns.size()
		    + " columns set as *isParent* errors (wrong reference): "
		    + columns.toString());
	}
	assertEquals(0, columns.size());
    }

    public void testIsParent2() {
	ArrayList<String> columns = new ArrayList<String>();

	for (Table t : ModelProvider.getInstance().getTables()) {
	    for (Column c : t.getColumns()) {
		if (c.isParent() && c.isPrimitiveType()) {
		    columns.add(t.getTableName() + " - " + c.getColumnName());
		}
	    }
	}

	if (columns.size() != 0)
	    System.err.println(columns.size()
		    + " columns set as *isParent* and are *primitive type*: "
		    + columns.toString());
	assertEquals(0, columns.size());
    }

    public void testIsParent3() {
	ArrayList<String> columns = new ArrayList<String>();
	for (Table t : ModelProvider.getInstance().getTables()) {
	    for (Column c : t.getColumns()) {
		if (c.isParent()
			&& c.getReference().getId().equals(Reference.TABLE)
			&& c.getReferenceValue() == null) {
		    columns.add(t.getTableName() + " - " + c.getColumnName());
		}
	    }
	}

	if (columns.size() != 0)
	    System.err
		    .println(columns.size()
			    + " columns set as *isParent* with reference *TABLE* and don't have table defined : "
			    + columns.toString());
	assertEquals(0, columns.size());
    }

    public void testIsParent4() {
	ArrayList<String> columns = new ArrayList<String>();
	for (Table t : ModelProvider.getInstance().getTables()) {
	    for (Column c : t.getColumns()) {
		if (c.isParent()
			&& c.getReference().getId().equals(Reference.TABLEDIR)) {
		    String obNamingConvention = c.getColumnName().substring(
			    c.getColumnName().length() - 3);
		    if (!obNamingConvention.equals("_ID"))
			columns.add(t.getTableName() + " - "
				+ c.getColumnName());
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
