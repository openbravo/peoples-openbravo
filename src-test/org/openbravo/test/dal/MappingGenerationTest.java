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

package org.openbravo.test.dal;

import java.util.Iterator;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.Identifiable;
import org.openbravo.dal.core.DalMappingGenerator;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.test.base.BaseTest;

/**
 * Test generation of mappings
 * 
 * @author mtaal
 */

public class MappingGenerationTest extends BaseTest {

    public void _testMappingGeneration() {
	DalMappingGenerator.getInstance().generateMapping();
    }

    public void testAllPageReadAll() {
	setErrorOccured(true);
	setBigBazaarAdminContext();
	final Configuration cfg = SessionFactoryController.getInstance()
		.getConfiguration();
	for (Iterator<?> it = cfg.getClassMappings(); it.hasNext();) {
	    final PersistentClass pc = (PersistentClass) it.next();
	    final String entityName = pc.getEntityName();

	    // System.err.println("++++++++ Reading entity " + entityName +
	    // " +++++++++++");

	    // do ordering for some of the classes
	    boolean orderOnName = false;
	    try {
		if (pc.getProperty("name") != null) {
		    orderOnName = true;
		}
	    } catch (MappingException m) {
		// ignore on purpose
	    }
	    try {
		final int count = OBDal.getInstance()
			.createCriteria(entityName).count();
		final int pageSize = 5;
		int pageCount = 1 + (count / pageSize);
		if (pageCount > 25) {
		    // System.err.println("Pagecount " + pageCount +
		    // " setting to max 25 because of performance reasons");
		    pageCount = 25;
		}
		for (int i = 0; i < pageCount; i++) {
		    final OBCriteria<BaseOBObject> obc = OBDal.getInstance()
			    .createCriteria(entityName);
		    obc.setFirstResult(i * pageSize);
		    obc.setMaxResults(pageSize);
		    if (orderOnName) {
			obc.addOrderBy("name", true);
		    }

		    // System.err.println("PAGE>>> " + (1 + i));
		    for (Object o : obc.list()) {
			if (o instanceof Identifiable) {
			    // final Identifiable n = (Identifiable) o;
			    // System.err.println(entityName + ": " +
			    // n.getIdentifier());
			} else {
			    // System.err.println(entityName + ": " + o);
			}
		    }
		}
	    } catch (Exception e) {
		System.err.println("Exception for entity: " + entityName);
		e.printStackTrace(System.err);
	    }
	    SessionHandler.getInstance().commitAndClose();
	}
	setErrorOccured(false);
    }
}