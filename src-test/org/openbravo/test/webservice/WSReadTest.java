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
	doTestGetRequest("/ws/dal/schema", "<element name=\"OpenBravo\">", 200);
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

	for (Iterator<?> it = cfg.getClassMappings(); it.hasNext();) {
	    final PersistentClass pc = (PersistentClass) it.next();
	    final String entityName = pc.getEntityName();
	    doTestGetRequest("/ws/dal/" + entityName, "<ob:OpenBravo", 200);
	}
	setErrorOccured(false);
    }

}