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

package org.openbravo.test.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Location;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.ImportResult;

/**
 * Test import of data with a business object, adding and removing childs
 * 
 * @author mtaal
 */

public class EntityXMLImportTestReference extends XMLBaseTest {

    public void _testPrintReadable() {
	setUserContext("1000019");
	System.err.println("User 1000019");
	OBContext.getOBContext().getEntityAccessChecker().dump();
    }

    // import greetings in 1000002
    public void test1Warehouse() {
	cleanRefDataLoaded();
	setErrorOccured(true);
	setUserContext("1000001");

	final String xml = getXML(Warehouse.class);
	// insert in org 1000002
	setUserContext("1000019");
	// System.err.println(xml);
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	} else {
	    assertEquals(4, ir.getInsertedObjects().size());
	    assertEquals(0, ir.getUpdatedObjects().size());
	}
	setErrorOccured(ir.hasErrorOccured());
    }

    public void test2Warehouse() {
	setErrorOccured(true);
	setUserContext("1000019");
	removeAll(Warehouse.class, 2, Expression.ne("id", "1000002"));
	setErrorOccured(false);
    }

    public void test3Warehouse() {
	setErrorOccured(true);
	setUserContext("1000001");

	final String xml = getXML(Warehouse.class);
	setUserContext("1000019");
	final ImportResult ir = DataImportService.getInstance()
		.importDataFromXML(
			OBDal.getInstance().get(Client.class, "1000001"),
			OBDal.getInstance().get(Organization.class, "1000001"),
			xml);
	if (ir.getException() != null) {
	    ir.getException().printStackTrace(System.err);
	    fail(ir.getException().getMessage());
	}
	assertEquals(2, ir.getInsertedObjects().size());
	for (BaseOBObject bob : ir.getInsertedObjects()) {
	    assertTrue(bob instanceof Warehouse);
	}
	assertEquals(0, ir.getUpdatedObjects().size());
	setErrorOccured(ir.hasErrorOccured());
    }

    public void test4Warehouse() {
	setErrorOccured(true);
	setUserContext("1000019");
	removeAll(Warehouse.class, 2, Expression.gt("created", new Date(System
		.currentTimeMillis() - 1000 * 3600 * 24)));
	removeAll(Location.class, 2, Expression.gt("created", new Date(System
		.currentTimeMillis() - 1000 * 3600 * 24)));
	setErrorOccured(false);
    }

    public <T extends BaseOBObject> void removeAll(Class<T> clz,
	    int expectCount, Criterion c) {
	try {
	    setErrorOccured(true);
	    setUserContext("1000019");
	    OBContext.getOBContext().setInAdministratorMode(true);

	    final Criteria criteria = SessionHandler.getInstance().getSession()
		    .createCriteria(clz);
	    if (c != null) {
		criteria.add(c);
	    }
	    criteria.add(Expression.eq("client.id", "1000001"));

	    @SuppressWarnings("unchecked")
	    final List<T> list = criteria.list();
	    assertEquals(expectCount, list.size());
	    for (T t : list) {
		SessionHandler.getInstance().getSession().delete(t);
	    }
	    setErrorOccured(false);
	} finally {
	    OBContext.getOBContext().restorePreviousAdminMode();
	}
    }

    public <T extends BaseOBObject> String getXML(Class<T> clz) {
	setErrorOccured(true);
	final OBCriteria<T> obc = OBDal.getInstance().createCriteria(clz);
	final EntityXMLConverter exc = EntityXMLConverter.newInstance();
	exc.setOptionIncludeReferenced(true);
	// exc.setOptionEmbedChildren(true);
	// exc.setOptionIncludeChildren(true);
	exc.setAddSystemAttributes(false);
	return exc.toXML(new ArrayList<BaseOBObject>(obc.list()));
    }
}