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

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.type.Type;
import org.openbravo.base.session.SessionFactoryController;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalSessionFactoryController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.xml.EntityXMLConverter;
import org.openbravo.test.base.BaseTest;

/**
 * Test for updates which can happen behind the scenes (but should not happen)
 * if properties are accidentally changed.
 * 
 * Note the testcases assume that they are run in the order defined in this
 * class.
 * 
 * @author mtaal
 */

public class HiddenUpdateTest extends BaseTest {

    // Test for hidden updates, these are not allowed!
    // Hidden updates can occur when a load/read of an entity also
    // changes the state, or that hibernate detects dirty in another way
    public void testHiddenUpdates() {
	setErrorOccured(true);
	setUserContext("0");

	final SessionFactoryController currentSFC = SessionFactoryController
		.getInstance();
	OBContext.getOBContext().setInAdministratorMode(true);
	try {
	    final SessionFactoryController newSFC = new LocalSessionFactoryController();
	    SessionFactoryController.setInstance(newSFC);
	    SessionFactoryController.getInstance().reInitialize();
	    SessionHandler.getInstance().commitAndClose();

	    // System.err.println(SessionFactoryController.getInstance().
	    // getMapping());

	    final Configuration cfg = DalSessionFactoryController.getInstance()
		    .getConfiguration();

	    for (Iterator<?> it = cfg.getClassMappings(); it.hasNext();) {
		final PersistentClass pc = (PersistentClass) it.next();
		final String entityName = pc.getEntityName();

		System.err.println("++++++++ Reading entity " + entityName
			+ " +++++++++++");
		for (Object o : OBDal.getInstance().createCriteria(entityName)
			.list()) {
		    if (o == null) {
			// can occur when reading views which have nullable
			// columns in a
			// multi-column pk
			continue;
		    }
		    EntityXMLConverter.newInstance().toXML((BaseOBObject) o);
		}
		SessionHandler.getInstance().commitAndClose();
	    }
	} finally {
	    SessionFactoryController.setInstance(currentSFC);
	    OBContext.getOBContext().setInAdministratorMode(false);
	}
	setErrorOccured(false);
    }

    class LocalSessionFactoryController extends DalSessionFactoryController {
	@Override
	protected void setInterceptor(Configuration configuration) {
	    configuration.setInterceptor(new LocalInterceptor());
	}
    }

    class LocalInterceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean onLoad(Object entity, Serializable id, Object[] state,
		String[] propertyNames, Type[] types) {
	    return false;
	}

	@Override
	public void onDelete(Object entity, Serializable id, Object[] state,
		String[] propertyNames, Type[] types) {
	    fail();
	}

	@Override
	public boolean onFlushDirty(Object entity, Serializable id,
		Object[] currentState, Object[] previousState,
		String[] propertyNames, Type[] types) {
	    fail();
	    return false;
	}

	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state,
		String[] propertyNames, Type[] types) {
	    fail();
	    return false;
	}

	@Override
	public void onCollectionRemove(Object collection, Serializable key)
		throws CallbackException {
	    fail();
	}

	@Override
	public void onCollectionRecreate(Object collection, Serializable key)
		throws CallbackException {
	    fail();
	}

	@Override
	public void onCollectionUpdate(Object collection, Serializable key)
		throws CallbackException {
	    fail();
	}
    }
}