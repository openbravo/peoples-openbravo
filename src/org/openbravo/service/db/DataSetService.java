/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.service.db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetColumn;
import org.openbravo.model.ad.utility.DataSetTable;

/**
 * Offers services around datasets including retrieving business objects etc.
 * 
 * @author Martin Taal
 */
public class DataSetService implements OBSingleton {
    private static final Logger log = Logger.getLogger(DataSetService.class);

    private static DataSetService instance;

    public static DataSetService getInstance() {
	if (instance == null) {
	    instance = OBProvider.getInstance().get(DataSetService.class);
	}
	return instance;
    }

    public static void setInstance(DataSetService instance) {
	DataSetService.instance = instance;
    }

    public DataSet getDataSetByValueModule(String value, String moduleId) {
	final Module module = OBDal.getInstance().get(Module.class, moduleId);
	final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
		DataSet.class);
	obc.add(Expression.eq(DataSet.PROPERTY_MODULE, module));
	obc.add(Expression.eq(DataSet.PROPERTY_VALUE, value));
	final List<?> list = obc.list();
	Check.isTrue(list.size() <= 1,
		"There is more than one dataset available when searching using the name/id "
			+ value + "/" + moduleId);
	if (list.size() == 0) {
	    return null;
	}
	return (DataSet) list.get(0);
    }

    // return a list of datasets on the basis of the moduleId
    public List<DataSet> getDataSetsByModuleID(String moduleId) {
	final Module module = OBDal.getInstance().get(Module.class, moduleId);
	final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
		DataSet.class);
	obc.add(Expression.eq(DataSet.PROPERTY_MODULE, module));
	return obc.list();
    }

    // get a DataSet using its name
    public DataSet getDataSetByValue(String value) {
	final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
		DataSet.class);
	obc.add(Expression.eq(DataSet.PROPERTY_VALUE, value));
	final List<DataSet> ds = obc.list();
	Check.isTrue(ds.size() > 0, "There is no DataSet with name " + value);
	if (ds.size() == 0) {
	    // TODO: throw an exception?
	    return null;
	}
	Check.isTrue(ds.size() == 1,
		"There is more than one DataSet with the name " + value
			+ ". The number of found DataSets is " + ds.size());
	return ds.get(0);
    }

    // return a list of DataSet tables instances on the basis of the
    // DataSet
    public List<DataSetTable> getDataSetTables(DataSet DataSet) {
	final OBCriteria<DataSetTable> obc = OBDal.getInstance()
		.createCriteria(DataSetTable.class);
	obc.add(Expression.eq(DataSetTable.PROPERTY_DATASET, DataSet));
	return obc.list();
    }

    // return the list of DataSet columns for a table
    public List<DataSetColumn> getDataSetColumns(DataSetTable DataSetTable) {
	final OBCriteria<DataSetColumn> obc = OBDal.getInstance()
		.createCriteria(DataSetColumn.class);
	obc.add(Expression
		.eq(DataSetColumn.PROPERTY_DATASETTABLE, DataSetTable));
	return obc.list();
    }

    // get the list of exportable objects, this assumes that the clause in
    // the DataSetTable can be used directly in hql.
    @SuppressWarnings("unchecked")
    public List<BaseOBObject> getExportableObjects(DataSetTable DataSetTable,
	    String moduleId) {

	final String entityName = DataSetTable.getTable().getName();
	final Entity entity = ModelProvider.getInstance().getEntity(entityName);

	if (entity == null) {
	    log.error("Entity not found using table name " + entityName);
	    return new ArrayList<BaseOBObject>();
	}

	String whereClause = DataSetTable.getWhereClause();
	if (moduleId != null && whereClause != null) {
	    while (whereClause.indexOf("@moduleid@") != -1) {
		whereClause = whereClause.replace("@moduleid@", "'" + moduleId
			+ "'");
	    }
	}

	final OBQuery<BaseOBObject> oq = OBDal.getInstance().createQuery(
		entity.getName(), whereClause);
	oq.setFilterOnActive(false);

	if (OBContext.getOBContext().getRole().getId().equals("0")
		&& OBContext.getOBContext().getCurrentClient().getId().equals(
			"0")) {
	    oq.setFilterOnAccessibleOrganisation(false);
	    oq.setFilterOnAccessibleClients(false);
	}

	final List<?> list = oq.list();
	Collections.sort(list, new BaseOBIDHexComparator());
	return (List<BaseOBObject>) list;
    }

    // this method will return all properties as defined by the DataSetcolumns
    // definition. It will return transient properties but not the auditinfo
    // if so excluded by the DataSet definition.
    public List<Property> getEntityProperties(BaseOBObject bob,
	    DataSetTable DataSetTable, List<DataSetColumn> DataSetColumns) {
	return getExportableProperties(bob, DataSetTable, DataSetColumns, true);
    }

    // this method will return the properties defined by the DataSetcolumns
    // definition
    // it will not export transients and it will not export the auditinfo if so
    // defined by the DataSet
    public List<Property> getExportableProperties(BaseOBObject bob,
	    DataSetTable DataSetTable, List<DataSetColumn> DataSetColumns) {
	return getExportableProperties(bob, DataSetTable, DataSetColumns, false);
    }

    // for the instance return the set of properties which need to be exported
    public List<Property> getExportableProperties(BaseOBObject bob,
	    DataSetTable DataSetTable, List<DataSetColumn> DataSetColumns,
	    boolean exportTransients) {

	final Entity entity = bob.getEntity();
	final List<Property> exportables;
	// check if all are included, except the excluded
	if (DataSetTable.isIncludeAllColumns()) {
	    exportables = new ArrayList<Property>(entity.getProperties());
	    // now remove the excluded
	    for (DataSetColumn dsc : DataSetColumns) {
		if (dsc.isExcluded()) {
		    exportables.remove(entity.getPropertyByColumnName(dsc
			    .getColumn().getColumnName()));
		}
	    }
	} else {
	    // not all included, go through the DataSetcolumns
	    // and add the not excluded
	    exportables = new ArrayList<Property>();
	    for (DataSetColumn dsc : DataSetColumns) {
		if (!dsc.isExcluded()) {
		    exportables.add(entity.getPropertyByColumnName(dsc
			    .getColumn().getColumnName()));
		}
	    }
	}
	// remove the transients
	if (!exportTransients) {
	    final List<Property> toRemove = new ArrayList<Property>();
	    for (Property p : exportables) {
		if (p.isTransient(bob)) {
		    toRemove.add(p);
		}
	    }
	    exportables.removeAll(toRemove);
	}

	// Remove the auditinfo
	if (DataSetTable.isExcludeAuditInfo()) {
	    final List<Property> toRemove = new ArrayList<Property>();
	    for (Property p : exportables) {
		if (p.isAuditInfo()) {
		    toRemove.add(p);
		}
	    }
	    exportables.removeAll(toRemove);
	}

	return exportables;
    }

    // compares the content of a list by converting the id to a hex
    public static class BaseOBIDHexComparator implements Comparator<Object> {

	public int compare(Object o1, Object o2) {
	    if (!(o1 instanceof BaseOBObject) || !(o2 instanceof BaseOBObject)) {
		return 0;
	    }
	    final BaseOBObject bob1 = (BaseOBObject) o1;
	    final BaseOBObject bob2 = (BaseOBObject) o2;
	    if (!(bob1.getId() instanceof String)
		    || !(bob2.getId() instanceof String)) {
		return 0;
	    }
	    try {
		BigInteger bd1 = new BigInteger(bob1.getId().toString(), 32);
		BigInteger bd2 = new BigInteger(bob2.getId().toString(), 32);
		return bd1.compareTo(bd2);
	    } catch (NumberFormatException n) {
		System.out.println("problem: " + n.getMessage());
		return 0;
	    }
	}
    }

}
