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
import org.openbravo.base.model.Entity;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.model.ad.datamodel.Table;

/**
 * Test the DAL rest webservices in read-mode. The test cases here require that there is a running
 * Openbravo at http://localhost:8080/openbravo
 * 
 * @author mtaal
 */

public class WSReadTest extends BaseWSTest {

  /**
   * Tests retrieval of the XML Schema defining the REST webservice.
   * 
   * @throws Exception
   */
  public void testSchemaWebService() throws Exception {
    doTestGetRequest("/ws/dal/schema", "<xs:element name=\"Openbravo\">", 200, false);
  }

  /**
   * Tests a special web service which lists all Entities (types) in the system.
   * 
   * @throws Exception
   */
  public void testTypesWebService() throws Exception {
    doTestGetRequest("/ws/dal", "<Types>", 200, false);
  }

  /**
   * Queries for a few {@link Table} objects using a REST call with a whereclause.
   * 
   * @throws Exception
   */
  public void testWhereClause() throws Exception {
    String whereClause = "(table.id='104' or table.id='105') and isKey='Y'";
    whereClause = URLEncoder.encode(whereClause, "UTF-8");
    final String content = doTestGetRequest("/ws/dal/ADColumn?where=" + whereClause, "<ADColumn",
        200);

    // there should be two columns
    final int index1 = content.indexOf("<ADColumn");
    assertTrue(index1 != -1);
    final int index2 = content.indexOf("<ADColumn", index1 + 2);
    assertTrue(index2 != -1);
    final int index3 = content.indexOf("<ADColumn", index2 + 2);
    assertTrue(index3 == -1);
  }

  /**
   * Performs a number of paged queries.
   * 
   * @throws Exception
   */
  public void testPagedWhereClause() throws Exception {
    requestColumnPage(1, 10, 10);
    requestColumnPage(1, 5, 5);
    // note, that there are 32 rows in the query,
    // the test below may fail if the no. of columns changes
    requestColumnPage(30, 5, 2);
  }

  private void requestColumnPage(int firstResult, int maxResult, int expectedCount)
      throws Exception {
    String whereClause = "(table.id='104' or table.id='105')";
    whereClause = URLEncoder.encode(whereClause, "UTF-8");
    String content = doTestGetRequest("/ws/dal/ADColumn?where=" + whereClause + "&firstResult="
        + firstResult + "&maxResult=" + maxResult, "<ADColumn", 200);

    // count the columns
    int index = content.indexOf("<ADColumn");
    int cnt = 0;
    while (index != -1) {
      cnt++;
      index = content.indexOf("<ADColumn", index + 1);
    }
    assertEquals(expectedCount, cnt);
  }

  /**
   * Calls the webservice for every {@link Entity} in the system. The test can take some time to run
   * (about 5 minutes).
   */
  public void testAllToXML() {
    setBigBazaarAdminContext();
    final Configuration cfg = SessionFactoryController.getInstance().getConfiguration();

    for (final Iterator<?> it = cfg.getClassMappings(); it.hasNext();) {
      final PersistentClass pc = (PersistentClass) it.next();
      final String entityName = pc.getEntityName();
      doTestGetRequest("/ws/dal/" + entityName, "<ob:Openbravo", 200);
    }
  }

}