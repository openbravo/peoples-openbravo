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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.UtilSql;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.FormTrl;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the following issues:
 * 
 * - https://issues.openbravo.com/view.php?id=11461 When saving business object in S/C data level
 * then access level exception is thrown for the child object
 * 
 * 
 * @author mtaal
 * @author iperdomo
 */

public class IssuesTest extends BaseTest {
  private static final Logger log = Logger.getLogger(IssuesTest.class);

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=11461
   */
  public void test11461() {
    setSystemAdministratorContext();

    Module module = OBDal.getInstance().createCriteria(Module.class).list().get(0);
    module.setInDevelopment(true);
    OBDal.getInstance().save(module);
    OBDal.getInstance().flush();

    Form form = OBProvider.getInstance().get(Form.class);
    form.setName("test");
    form.setDataAccessLevel("1");
    form.setDescription("description");
    form.setHelpComment("help");
    form.setModule(module);
    form.setJavaClassName("org.openbravo.test");

    FormTrl formTrl = OBProvider.getInstance().get(FormTrl.class);
    formTrl.setHelpComment("help");
    formTrl.setDescription("description");
    formTrl.setName("name");
    formTrl.setSpecialForm(form);
    formTrl.setLanguage(OBDal.getInstance().createCriteria(Language.class).list().get(0));

    form.getADFormTrlList().add(formTrl);
    OBDal.getInstance().save(form);
    OBDal.getInstance().flush();

    // if we get here then the issue is solved.

    // don't save anything
    OBDal.getInstance().rollbackAndClose();
  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=11681
   */
  public void test11681() {
    setSystemAdministratorContext();

    OBCriteria<Module> obc = OBDal.getInstance().createCriteria(Module.class);
    obc.add(Expression.eq(Module.PROPERTY_INDEVELOPMENT, false));

    if (obc.list().size() == 0) {
      // Can't test DAL's connection provider
      return;
    }

    Module module = obc.list().get(0);
    module.setInDevelopment(true);
    OBDal.getInstance().save(module);

    Connection con = OBDal.getInstance().getConnection();

    final String sql = "SELECT isindevelopment FROM ad_module where ad_module_id = ?";

    try {
      PreparedStatement st = con.prepareStatement(sql);
      st.setString(1, module.getId());
      ResultSet result = st.executeQuery();
      result.next();

      String isInDev = UtilSql.getValue(result, "isindevelopment");
      assertTrue(isInDev.equals("Y"));

      result = null;
      st = null;
      con.close();
    } catch (SQLException e) {
      log.error("Error " + e.getMessage(), e);
    }
    OBDal.getInstance().rollbackAndClose();
  }
}