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

package org.openbravo.test.modularity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetColumn;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.service.dataset.DataSetService;
import org.openbravo.service.db.DataExportService;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the Dataset Service Object
 * 
 * @author mtaal
 */

public class DatasetServiceTest extends BaseTest {

  public void testCheckQueries() {
    setErrorOccured(true);
    setUserContext("100");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ClientID", "0");

    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    final List<DataSet> dss = obc.list();
    setUserContext("0");
    for (final DataSet ds : dss) {
      for (final DataSetTable dt : ds.getDataSetTableList()) {
        try {
          // just test but do nothing with return value
          DataSetService.getInstance().getExportableObjects(dt, "0", parameters);
        } catch (final Exception e) {
          System.err.println(ds.getName() + ": " + dt.getEntityName() + ": " + e.getMessage());
        }
      }
    }
    setErrorOccured(false);
  }

  public void testExportAllDataSets() {
    setErrorOccured(true);
    setUserContext("100");
    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    final List<DataSet> dss = obc.list();
    setUserContext("0");

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ClientID", "0");

    for (final DataSet ds : dss) {
      final String xml = DataExportService.getInstance().exportDataSetToXML(ds, "0", parameters,
          true, true, OBDal.getInstance().get(Client.class, "0"), true);
      System.err.println("DataSet " + ds.getName() + " exported " + xml.length() + " characters");
    }
    setErrorOccured(false);
  }

  // test whereclause
  public void testDataSetTable() {
    setErrorOccured(true);
    setUserContext("100");
    final DataSetTable dst = OBProvider.getInstance().get(DataSetTable.class);
    final Table t = OBProvider.getInstance().get(Table.class);
    t.setName("ADTable");
    dst.setTable(t);
    dst.setWhereClause("(" + Table.PROPERTY_ISDELETEABLE + "='N' or " + Table.PROPERTY_ISVIEW
        + "='N') and client.id='0'");
    final List<BaseOBObject> l = DataSetService.getInstance().getExportableObjects(dst, "0");
    for (final BaseOBObject bob : l) {
      System.err.println(bob.getIdentifier());
    }
    setErrorOccured(false);
  }

  public void testReadAll() {
    setErrorOccured(true);
    setBigBazaarAdminContext();

    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("ClientID", "0");

    final DataSetService dss = DataSetService.getInstance();
    final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(DataSet.class);
    final List<DataSet> ds = obc.list();
    for (final DataSet d : ds) {
      System.err.println("Exporting DataSet: " + d.getName());
      System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
      final List<DataSetTable> dts = dss.getDataSetTables(d);
      for (final DataSetTable dt : dts) {
        System.err.println("Exporting DataSetTable: " + dt.getTable().getName());
        final List<DataSetColumn> dcs = dss.getDataSetColumns(dt);

        final List<BaseOBObject> bobs = dss.getExportableObjects(dt, "0", parameters);
        for (final BaseOBObject bob : bobs) {
          final List<Property> ps = dss.getExportableProperties(bob, dt, dcs);
          final StringBuilder sb = new StringBuilder();
          sb.append(bob.getIdentifier() + " has " + ps.size() + " properties to export");
          // . Values: ");
          // for (Property p : ps) {
          // final Object value = bob.get(p.getName());
          // sb.append(", " + p.getName() + ": ");
          // if (value instanceof BaseOBObject) {
          // sb.append(((BaseOBObject) value).getIdentifier());
          // } else {
          // sb.append(value);
          // }
          // System.err.println(sb.toString());
          // }
        }
      }
    }
    setErrorOccured(false);
  }

  public void psuedoCode() {
    final DataSetService dss = DataSetService.getInstance();
    final DataSet ds = dss.getDataSetByValue("My great DataSet");
    for (final DataSetTable dt : dss.getDataSetTables(ds)) {
      final List<BaseOBObject> bobs = dss.getExportableObjects(dt, "0");
      final List<DataSetColumn> dscs = dss.getDataSetColumns(dt);
      for (final BaseOBObject bob : bobs) {
        final List<Property> ps = dss.getExportableProperties(bob, dt, dscs);
        for (final Property p : ps) {
          final Object value = bob.get(p.getName());
          // handle references and export only their id's
          if (value instanceof BaseOBObject) {
            // this assumes that the id is a primitive type
            // and not itself a reference!
            exportProperty(p, (bob).getId());
          } else {
            exportProperty(p, value);
          }
        }

      }
    }
    // this needs to be done if the export service is not called
    // through a normal http request
    SessionHandler.getInstance().commitAndClose();
  }

  public void exportProperty(Property p, Object value) {
    System.err.println(p + ": " + value);
  }
}
