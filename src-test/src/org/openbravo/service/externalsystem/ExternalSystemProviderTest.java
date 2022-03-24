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
package org.openbravo.service.externalsystem;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.externalsystem.http.HttpExternalSystem;

/**
 * Tests for the {@link ExternalSystemProvider} class
 */
public class ExternalSystemProviderTest extends WeldBaseTest {

  @Inject
  private ExternalSystemProvider externalSystemProvider;

  private ExternalSystemData externalSystemData;

  @Before
  public void initExternalSystemData() {
    externalSystemData = OBProvider.getInstance().get(ExternalSystemData.class);
    externalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
    externalSystemData.setProtocol("HTTP");
    OBDal.getInstance().save(externalSystemData);
  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }

  @Test
  public void getExternalSystemForHttpProtocol() {
    addHttpConfig(true);

    ExternalSystem externalSystem = externalSystemProvider.getExternalSystem(externalSystemData)
        .orElseThrow();

    assertThat(externalSystem, instanceOf(HttpExternalSystem.class));
  }

  @Test
  public void cannotGetExternalSystemWithoutActiveConfigurations() {
    addHttpConfig(false);

    Optional<ExternalSystem> externalSystem = externalSystemProvider
        .getExternalSystem(externalSystemData);

    assertThat(externalSystem.isPresent(), equalTo(false));
  }

  @Test
  public void cannotGetExternalSystemForIncompleteConfiguration() {
    Optional<ExternalSystem> externalSystem = externalSystemProvider
        .getExternalSystem(externalSystemData);

    assertThat(externalSystem.isPresent(), equalTo(false));
  }

  private void addHttpConfig(boolean isActive) {
    HttpExternalSystemData httpExternalSystemData = OBProvider.getInstance()
        .get(HttpExternalSystemData.class);
    httpExternalSystemData.setOrganization(OBDal.getInstance().getProxy(Organization.class, "0"));
    httpExternalSystemData.setAuthorizationType("NOAUTH");
    httpExternalSystemData.setExternalSystem(externalSystemData);
    httpExternalSystemData.setActive(isActive);
    externalSystemData.getHttpExternalSystemList().add(httpExternalSystemData);
    OBDal.getInstance().save(httpExternalSystemData);
  }
}
