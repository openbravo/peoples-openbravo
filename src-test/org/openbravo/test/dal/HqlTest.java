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

package org.openbravo.test.dal;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.domain.ModelImplementation;
import org.openbravo.test.base.BaseTest;

/**
 * Tests hql and the DAL.
 * 
 * NOTE: simple test class only used for testing queries manually, should not be part of a test
 * suite.
 * 
 * @author mtaal
 */

public class HqlTest extends BaseTest {
  private static final Logger log = Logger.getLogger(HqlTest.class);

  public void testDalWhereClause() {
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
    final OBQuery<ModelImplementation> obq = OBDal.getInstance().createQuery(
        ModelImplementation.class, where);
    for (final BaseOBObject o : obq.list()) {
      log.debug(o.getIdentifier());
    }
  }

  // query for the BPGroup again and remove it
  public void testHql() {
    setUserContext("100");

    final Session s = SessionHandler.getInstance().getSession();
    final Query q = s
        .createQuery("select mo from "
            + ModelImplementation.ENTITY_NAME
            + " as mo left join mo.callout left join mo.reference where mo.callout.module.id='0' or mo.reference.module.id='0'");
    for (final Object o : q.list()) {
      log.debug(((BaseOBObject) o).getIdentifier());
    }

  }
}