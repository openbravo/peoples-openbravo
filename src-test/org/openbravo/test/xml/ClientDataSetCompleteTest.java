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

package org.openbravo.test.xml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.service.db.DataExportService;

/**
 * Tests if the client definition data set is complete.
 * 
 * @author mtaal
 */

public class ClientDataSetCompleteTest extends XMLBaseTest {

    public void testDataSetComplete() {
        setUserContext("0");
        final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
                DataSet.class);
        obc.add(Expression.eq("name", DataExportService.CLIENT_DATA_SET_NAME));
        if (obc.list().size() == 0) {
            throw new OBException("No dataset found with name "
                    + DataExportService.CLIENT_DATA_SET_NAME);
        }
        final DataSet dataSet = obc.list().get(0);
        final File dir = new File(
                "/home/mtaal/mydata/dev/workspaces/obtrunk/openbravo/src-db/database/sampledata");
        final Set<String> names = new HashSet<String>();
        for (final File child : dir.listFiles()) {
            if (!child.isDirectory()) {
                names.add(child.getName());
            }
        }
        for (final DataSetTable dst : dataSet.getDataSetTableList()) {
            names.remove(dst.getTable().getTableName().toUpperCase() + ".xml");
        }
        for (final String name : names) {
            System.err.println(name);
        }
    }
}