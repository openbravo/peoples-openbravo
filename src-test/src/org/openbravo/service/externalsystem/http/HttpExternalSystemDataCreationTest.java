/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem.http;

import static org.junit.Assert.assertNotNull;
import static org.openbravo.test.base.TestConstants.Orgs.MAIN;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.externalsystem.ExternalSystemData;
import org.openbravo.service.externalsystem.HttpExternalSystemData;

/**
 * Tests for covering the creation of {@link HttpExternalSystemData} records
 */
public class HttpExternalSystemDataCreationTest extends WeldBaseTest {

  private static final String MAX_TIMEOUT_ERROR_MSG = "Timeout must be a value lower than 30 seconds";

  private ExternalSystemData externalSystemData;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void init() {
    setTestAdminContext();

    externalSystemData = OBProvider.getInstance().get(ExternalSystemData.class);
    externalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, MAIN));
    externalSystemData.setName("Test");
    externalSystemData.setProtocol("HTTP");
    OBDal.getInstance().save(externalSystemData);
  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }

  @Test
  public void createConfiguration() {
    HttpExternalSystemData configuration = createHttpConfiguration(5L);
    OBDal.getInstance().flush();
    assertNotNull(configuration.getId());
  }

  @Test
  public void cannotCreateConfigurationThatExceedsMaxTimeout() {
    exceptionRule.expect(OBException.class);
    exceptionRule.expectMessage(MAX_TIMEOUT_ERROR_MSG);

    createHttpConfiguration(HttpExternalSystem.MAX_TIMEOUT + 1L);
  }

  @Test
  public void cannotUpdateConfigurationToExceedMaxTimeout() {
    exceptionRule.expect(OBException.class);
    exceptionRule.expectMessage(MAX_TIMEOUT_ERROR_MSG);

    HttpExternalSystemData configuration = createHttpConfiguration(5L);
    OBDal.getInstance().flush();

    configuration.setTimeout(HttpExternalSystem.MAX_TIMEOUT + 1L);
    OBDal.getInstance().flush();
  }

  private HttpExternalSystemData createHttpConfiguration(long timeout) {
    HttpExternalSystemData httpExternalSystemData = OBProvider.getInstance()
        .get(HttpExternalSystemData.class);
    httpExternalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, MAIN));
    httpExternalSystemData.setURL("https://dummy");
    httpExternalSystemData.setAuthorizationType("NOAUTH");
    httpExternalSystemData.setExternalSystem(externalSystemData);
    httpExternalSystemData.setTimeout(timeout);
    externalSystemData.getHttpExternalSystemList().add(httpExternalSystemData);
    OBDal.getInstance().save(httpExternalSystemData);
    return httpExternalSystemData;
  }
}
