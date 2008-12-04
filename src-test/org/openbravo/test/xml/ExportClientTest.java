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

import java.util.HashMap;
import java.util.Map;

import org.hibernate.criterion.Expression;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.service.db.DataExportService;

/**
 * Tests export of client dataset.
 * 
 * @author mtaal
 */

public class ExportClientTest extends XMLBaseTest {
    public void testExportClient() {
        setErrorOccured(true);
        setUserContext("1000000");
        final OBCriteria<DataSet> obc = OBDal.getInstance().createCriteria(
                DataSet.class);
        obc.add(Expression.eq("name", "Client Definition"));
        assertTrue(obc.list().size() == 1);
        final DataSet dataSet = obc.list().get(0);
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("ClientID", "1000000");
        final String xml = DataExportService.getInstance().exportClientToXML(
                dataSet, null, null);
        System.err.println(xml);
    }
}