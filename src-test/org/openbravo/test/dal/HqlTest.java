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

import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.test.base.BaseTest;

/**
 * Allows testing of hql
 * 
 * @author mtaal
 */

public class HqlTest extends BaseTest {
    // creates a new BPGroup, test simple save, BPGroup is removed in next test
    public void testDalWhereClause() {
	setErrorOccured(true);
	setUserContext("100");
	// final String where =
	// " tree.id='10' and exists( from ADMenu as menu where menu.id = node_id and menu.module.id='0')"
	// ;
	final String where = "callout.module.id='0' or reference.module.id='0'";
	// or form.module.id='0' or process.module.id='0' or
	// workflow.module.id='0' or tab.module.id='0'";
	// (callout is not null and callout.module.id='0') or (reference is not
	// null and reference.module.id='0') or (form is not null and
	// form.module.id='0') or (process is not null and
	// process.module.id='0') or (workflow is not null and
	// workflow.module.id='0') or (tab is not null and tab.module.id='0')
	final OBQuery<ModelImplementation> obq = OBDal.getInstance()
		.createQuery(ModelImplementation.class, where);
	for (BaseOBObject o : obq.list()) {
	    System.err.println(o.getIdentifier());
	}
	setErrorOccured(false);
    }

    // query for the BPGroup again and remove it
    public void testHql() {
	setErrorOccured(true);
	setUserContext("100");

	final Session s = SessionHandler.getInstance().getSession();
	final Query q = s
		.createQuery("select mo from ADModelObject as mo left join mo.callout left join mo.reference where mo.callout.module.id='0' or mo.reference.module.id='0'");
	for (Object o : q.list()) {
	    System.err.println(((BaseOBObject) o).getIdentifier());
	}

	setErrorOccured(false);
    }
}