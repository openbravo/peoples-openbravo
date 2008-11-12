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

package org.openbravo.test.security;

import java.util.List;
import java.util.Set;

import org.hibernate.criterion.Expression;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.test.base.BaseTest;

/**
 * Tests check of the accesslevel of an entity
 * 
 * @author mtaal
 */

public class WritableReadableOrganisationTest extends BaseTest {

    public void testAccessLevelCO() {
	setErrorOccured(true);
	setUserContext("0");
	doCheckUser();
	setBigBazaarUserContext();
	doCheckUser();
	setErrorOccured(false);
    }

    private void doCheckUser() {
	final OBContext obContext = OBContext.getOBContext();
	final Set<String> writOrgs = obContext.getWritableOrganisations();
	final String[] readOrgs = obContext.getReadableOrganisations();
	final StringBuilder sb = new StringBuilder();
	for (String s : readOrgs) {
	    sb.append("," + s);
	}

	for (String wo : writOrgs) {
	    boolean found = false;
	    for (String s : readOrgs) {
		found = s.equals(wo);
		if (found) {
		    break;
		}
	    }
	    assertTrue("Org " + wo + " not present in readableOrglist "
		    + sb.toString(), found);
	}
    }

    public void testClient() {
	setErrorOccured(true);
	final OBContext obContext = OBContext.getOBContext();
	final String[] cs = obContext.getReadableClients();
	final String cid = obContext.getCurrentClient().getId();
	boolean found = false;
	final StringBuilder sb = new StringBuilder();
	for (String s : cs) {
	    sb.append("," + s);
	}
	for (String s : cs) {
	    found = s.equals(cid);
	    if (found) {
		break;
	    }
	}
	assertTrue("Current client " + cid + " not found in clienttlist "
		+ sb.toString(), found);
	setErrorOccured(false);
    }

    public void testUpdateCosting() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBCriteria<Costing> obc = OBDal.getInstance().createCriteria(
		Costing.class);
	obc.add(Expression.eq("id", "1000078"));
	final List<Costing> cs = obc.list();
	assertEquals(1, cs.size());
	final Costing c = cs.get(0);
	c.setCost(c.getCost() + 1);

	// switch usercontext to force eexception
	setUserContext("1000002");
	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail("Writable organisations not checked");
	} catch (OBSecurityException e) {
	    e.printStackTrace(System.err);
	    assertTrue("Invalid exception " + e.getMessage(), e.getMessage()
		    .indexOf(" is not writable by this user") != -1);
	}
	setErrorOccured(false);
    }

    public void testUpdateBPGroup() {
	setErrorOccured(true);
	setUserContext("1000001");
	final OBCriteria<Category> obc = OBDal.getInstance().createCriteria(
		Category.class);
	obc.add(Expression.eq("name", "Standard"));
	final List<Category> bogs = obc.list();
	assertEquals(1, bogs.size());
	final Category bp = bogs.get(0);
	bp.setDescription(bp.getDescription() + "A");
	try {
	    SessionHandler.getInstance().commitAndClose();
	} catch (OBSecurityException e) {
	    assertTrue("Invalid exception " + e.getMessage(), e.getMessage()
		    .indexOf("is not present  in OrganisationList") != -1);
	}

	setErrorOccured(false);
    }
}