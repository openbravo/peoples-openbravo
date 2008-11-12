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

import java.util.Set;

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.OrganisationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.project.Project;
import org.openbravo.test.base.BaseTest;

/**
 * Tests computation of natural tree of an organisation.
 * 
 * @author mtaal
 */

public class AllowedOrganisationsTest extends BaseTest {

    public void testOrganisationTree() {
	setErrorOccured(true);
	setBigBazaarAdminContext();
	final OrganisationStructureProvider osp = new OrganisationStructureProvider();
	osp.setClientId("1000000");

	checkResult("1000001", osp, new String[] { "1000001" });
	checkResult("1000002", osp, new String[] { "1000003", "1000004",
		"1000000", "0", "1000002" });
	checkResult("1000003", osp, new String[] { "1000003", "1000000", "0",
		"1000002" });
	checkResult("1000004", osp, new String[] { "1000004", "1000000", "0",
		"1000002" });
	checkResult("1000005", osp, new String[] { "1000009", "1000006", "0",
		"1000000", "1000008", "1000005", "1000007" });
	checkResult("1000006", osp, new String[] { "1000009", "1000006", "0",
		"1000000", "1000008", "1000005" });
	checkResult("1000007", osp, new String[] { "1000000", "0", "1000005",
		"1000007" });
	checkResult("1000008", osp, new String[] { "1000000", "1000006", "0",
		"1000008", "1000005" });
	checkResult("1000009", osp, new String[] { "1000009", "1000006", "0",
		"1000000", "1000005" });
	setErrorOccured(false);
    }

    private void checkResult(String id, OrganisationStructureProvider osp,
	    String[] values) {
	final Set<String> result = osp.getNaturalTree(id);
	assertEquals(values.length, result.size());
	for (String value : values) {
	    assertTrue(result.contains(value));
	}
    }

    public void testProjectUpdate() {
	setErrorOccured(true);
	setUserContext("1000001");
	final Project p = OBDal.getInstance().get(Project.class, "1000001");
	p.setName(p.getName() + "A");
	setErrorOccured(false);
    }

    public void testOrganisationCheck() {
	setErrorOccured(true);
	setUserContext("0");
	OBContext.getOBContext().getOrganisationStructureProvider()
		.reInitialize();

	final Project p = OBDal.getInstance().get(Project.class, "1000001");
	final Organization o5 = OBDal.getInstance().get(Organization.class,
		"1000005");
	final Organization o3 = OBDal.getInstance().get(Organization.class,
		"1000001");
	p.setOrganization(o3);
	p.getBusinessPartner().setOrganization(o5);

	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail();
	} catch (OBSecurityException e) {
	    assertTrue("Invalid exception " + e.getMessage(), e.getMessage()
		    .indexOf("which is not part of the natural tree of") != -1);
	    // no fail!
	    SessionHandler.getInstance().rollback();
	}
	setErrorOccured(false);
    }
}