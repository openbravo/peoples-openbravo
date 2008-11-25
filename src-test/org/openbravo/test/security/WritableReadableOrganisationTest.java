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
        for (final String s : readOrgs) {
            sb.append("," + s);
        }

        for (final String wo : writOrgs) {
            boolean found = false;
            for (final String s : readOrgs) {
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
        for (final String s : cs) {
            sb.append("," + s);
        }
        for (final String s : cs) {
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
        } catch (final OBSecurityException e) {
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
        } catch (final OBSecurityException e) {
            assertTrue("Invalid exception " + e.getMessage(), e.getMessage()
                    .indexOf("is not present  in OrganisationList") != -1);
        }

        setErrorOccured(false);
    }
}