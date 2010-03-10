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
 * All portions are Copyright (C) 2009-2010 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.IdentifierProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.dal.xml.EntityXMLException;
import org.openbravo.dal.xml.XMLTypeConverter;
import org.openbravo.data.UtilSql;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Form;
import org.openbravo.model.ad.ui.FormTrl;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.test.base.BaseTest;

/**
 * Tests the following issues:
 * 
 * - https://issues.openbravo.com/view.php?id=11461 When saving business object in S/C data level
 * then access level exception is thrown for the child object
 * 
 * - https://issues.openbravo.com/view.php?id=12202 OBQuery does not support list parameter
 * 
 * - https://issues.openbravo.com/view.php?id=12201 OBContext is not using system language as
 * default language
 * 
 * - https://issues.openbravo.com/view.php?id=12143 OBQuery class should add convenience method
 * uniqueResult similar to the OBCriteria class
 * 
 * - https://issues.openbravo.com/view.php?id=12497: Active property should have default value ==
 * true if no explicit default is defined
 * 
 * - https://issues.openbravo.com/view.php?id=12106: record identifier returned from dal uses ' ' as
 * separator of columns, but normal pl-version uses ' - '
 * 
 * @author mtaal
 * @author iperdomo
 */

public class IssuesTest extends BaseTest {
  private static final Logger log = Logger.getLogger(IssuesTest.class);

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=12106
   */
  public void test12106() {
    setSystemAdministratorContext();
    final List<Module> modules = OBDal.getInstance().createCriteria(Module.class).list();
    for (Module module : modules) {
      assertTrue(module.getIdentifier().contains(IdentifierProvider.SEPARATOR));
    }
  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=12202
   */
  public void test12202() {
    setSystemAdministratorContext();
    final List<Module> modules = OBDal.getInstance().createCriteria(Module.class).list();

    final OBQuery<Message> messages = OBDal.getInstance().createQuery(Message.class,
        "module in (:modules)");
    messages.setNamedParameter("modules", modules);
    assertFalse(messages.list().isEmpty());

  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=12201
   */
  public void test12201() {
    setSystemAdministratorContext();
    assertEquals("0", OBContext.getOBContext().getUser().getId());
    assertTrue(null == OBContext.getOBContext().getUser().getDefaultLanguage());
    assertTrue(OBContext.getOBContext().getLanguage().isSystemLanguage());
  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=12143
   */
  public void test12143() {
    setSystemAdministratorContext();
    final OBQuery<Message> messages = OBDal.getInstance().createQuery(Message.class, null);
    try {
      messages.uniqueResult();
      fail();
    } catch (Exception e) {
      // should fail as there is more than one result
    }
    final OBQuery<Organization> organizations = OBDal.getInstance().createQuery(Organization.class,
        "id='0'");
    final Organization organization = organizations.uniqueResult();
    assertNotNull(organization);
  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=11812
   */
  public void test11812() {
    assertTrue(24 == XMLTypeConverter.getInstance().fromXML(Long.class, "24.0"));
    try {
      XMLTypeConverter.getInstance().fromXML(Long.class, "24.5");
      fail("No exception on 24.5");
    } catch (EntityXMLException e) {
      // expected
    }
  }

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=11461
   */
  public void test11461() {
    setSystemAdministratorContext();

    Module module = OBDal.getInstance().get(Module.class, "0");
    module.setInDevelopment(true);
    OBDal.getInstance().save(module);
    OBDal.getInstance().flush();

    Form form = OBProvider.getInstance().get(Form.class);
    form.setName("test");
    form.setDataAccessLevel("1");
    form.setDescription("description");
    form.setHelpComment("help");
    form.setModule(module);
    form.setJavaClassName(module.getJavaPackage() + ".test");

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

  /**
   * Tests issue: https://issues.openbravo.com/view.php?id=12497
   */
  public void test12497() {
    final InvoiceLine invoiceLine = OBProvider.getInstance().get(InvoiceLine.class);
    assertTrue(invoiceLine.isActive());
    Location bpLoc = OBProvider.getInstance().get(Location.class);
    assertTrue(bpLoc.isActive());
  }
}