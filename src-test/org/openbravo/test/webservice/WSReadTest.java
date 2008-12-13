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

package org.openbravo.test.webservice;

import java.net.URLEncoder;
import java.util.Iterator;

import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Test webservice. Note some of the test cases here require a running Openbravo
 * at http://localhost:8080/openbravo
 * 
 * @author mtaal
 */

public class WSReadTest extends BaseWSTest {

    public void testSchemaWebService() throws Exception {
        doTestGetRequest("/ws/dal/schema", "<element name=\"Openbravo\">", 200);
    }

    public void testTypesWebService() throws Exception {
        doTestGetRequest("/ws/dal", "<Types>", 200);
    }

    public void testWhereClause() throws Exception {
        String whereClause = "(table.id='104' or table.id='105') and isKey='Y'";
        whereClause = URLEncoder.encode(whereClause, "UTF-8");
        final String content = doTestGetRequest("/ws/dal/ADColumn?where="
                + whereClause, "<ADColumn", 200);
        // there should be two columns
        final int index1 = content.indexOf("<ADColumn");
        assertTrue(index1 != -1);
        final int index2 = content.indexOf("<ADColumn", index1 + 2);
        assertTrue(index2 != -1);
        final int index3 = content.indexOf("<ADColumn", index2 + 2);
        assertTrue(index3 == -1);
    }

    public void testAllToXML() {
        setErrorOccured(true);
        setBigBazaarAdminContext();
        final Configuration cfg = SessionFactoryController.getInstance()
                .getConfiguration();

        for (final Iterator<?> it = cfg.getClassMappings(); it.hasNext();) {
            final PersistentClass pc = (PersistentClass) it.next();
            final String entityName = pc.getEntityName();
            doTestGetRequest("/ws/dal/" + entityName, "<ob:Openbravo", 200);
        }
        setErrorOccured(false);
    }

}