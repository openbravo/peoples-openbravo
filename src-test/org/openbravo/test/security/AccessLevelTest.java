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

import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.cashmgmt.CashJournal;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.test.base.BaseTest;

/**
 * Tests check of the accesslevel of an entity
 * 
 * @author mtaal
 */

public class AccessLevelTest extends BaseTest {

    public void testAccessLevelCO() {
	setErrorOccured(true);
	setBigBazaarAdminContext();
	final Client c = OBDal.getInstance().get(Client.class, "0");

	final BusinessPartner bp = OBDal.getInstance().get(
		BusinessPartner.class, "1000005");
	bp.setClient(c);
	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail();
	} catch (OBSecurityException e) {
	    // no fail!
	    assertTrue(e.getMessage().indexOf(
		    "may not have instances with client 0") != -1);
	    SessionHandler.getInstance().rollback();
	}
	setErrorOccured(false);
    }

    public void testAccessLevelSystem() {
	setErrorOccured(true);
	setUserContext("0");
	final Organization o = OBDal.getInstance().get(Organization.class,
		"1000002");
	final Table t = OBDal.getInstance().get(Table.class, "100");
	t.setOrganization(o);

	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail();
	} catch (OBSecurityException e) {
	    // no fail!
	    assertTrue(
		    "Invalid exception: " + e.getMessage(),
		    e.getMessage().indexOf(
			    " may only have instances with organisation *") != -1);
	    SessionHandler.getInstance().rollback();
	}
	setErrorOccured(false);
    }

    public void testAccessLevelOrganisation() {
	setErrorOccured(true);
	setUserContext("0");
	final Organization o = OBDal.getInstance().get(Organization.class, "0");
	final CashJournal c = OBDal.getInstance().get(CashJournal.class,
		"1000000");
	c.setOrganization(o);

	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail();
	} catch (OBSecurityException e) {
	    // no fail!
	    assertTrue(
		    "Invalid exception " + e.getMessage(),
		    e.getMessage().indexOf(
			    " may not have instances with organisation *") != -1);
	    SessionHandler.getInstance().rollback();
	}
	setErrorOccured(false);
    }

    public void testAccessLevelSC() {
	setErrorOccured(true);
	setUserContext("0");
	final Organization o = OBDal.getInstance().get(Organization.class,
		"1000001");
	final TaxRate t = OBDal.getInstance().get(TaxRate.class, "1000000");
	t.setOrganization(o);

	try {
	    SessionHandler.getInstance().commitAndClose();
	    fail();
	} catch (OBSecurityException e) {
	    // no fail!
	    assertTrue(
		    "Invalid exception " + e.getMessage(),
		    e.getMessage().indexOf(
			    "may only have instances with organisation *") != -1);
	    SessionHandler.getInstance().rollback();
	}
	setErrorOccured(false);
    }

}