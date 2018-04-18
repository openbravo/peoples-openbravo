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

import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.SystemInformation;

/**
 * This class centralizes the logic to update the "In Development" flag in all Modules
 *
 * @author jarmendariz
 */
@ApplicationScoped
public class ModuleDevelopmentStatusHelper {
  private static final String PURPOSE_PRODUCTION = "P";

  @Inject
  private ApplicationDictionaryCachedStructures cachedStructures;

  /**
   * Checks the current system instance purpose. If it is set as Production, all available modules
   * are set as not in development
   */
  public void updateDevelopmentStatusInAllModules() {
    String purpose = getInstancePurpose();
    updateDevelopmentStatusInAllModules(purpose);
  }

  /**
   * Sets all modules to not in development if the given purpose is "P" (Production) and there are
   * modules in development mode
   * 
   * @param purpose
   *          The instance purpose value
   */
  public void updateDevelopmentStatusInAllModules(String purpose) {
    if (PURPOSE_PRODUCTION.equals(purpose) && cachedStructures.isInDevelopment()) {
      removeDevelopmentFlagToAllModules();
    }
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
