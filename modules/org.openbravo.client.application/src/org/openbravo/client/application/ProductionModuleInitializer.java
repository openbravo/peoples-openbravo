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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.SystemInformation;

@ApplicationScoped
public class ProductionModuleInitializer implements ApplicationInitializer {

  private static final String PURPOSE_PRODUCTION = "P";
  private static Logger log = Logger.getLogger(ProductionModuleInitializer.class);

  @Inject
  private ApplicationDictionaryCachedStructures cachedStructures;

  @Override
  public void initialize() {
    log.info("Checking instance purpose and In Development modules");

    String purpose = getInstancePurpose();
    if (isProductionInstance(purpose) && cachedStructures.isInDevelopment()) {
      removeDevelopmentFlagToAllModules();
    }
  }

  private boolean isProductionInstance(String purpose) {
    return purpose != null && purpose.equals(PURPOSE_PRODUCTION);
  }

  private String getInstancePurpose() {
    return (String) OBDal
        .getInstance()
        .getSession()
        .createQuery(
            "select " + SystemInformation.PROPERTY_INSTANCEPURPOSE + " from "
                + SystemInformation.ENTITY_NAME).uniqueResult();
  }

  private void removeDevelopmentFlagToAllModules() {
    OBDal
        .getInstance()
        .getSession()
        .createQuery(
            "update " + Module.ENTITY_NAME + " set " + Module.PROPERTY_INDEVELOPMENT + " = false")
        .executeUpdate();
  }
}
