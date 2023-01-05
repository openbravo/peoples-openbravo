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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This module script is used to upgrade the existing company logos to fit the new structure
 * introduced in https://issues.openbravo.com/view.php?id=51238
 *
 * The general criteria followed here is that we only migrate the logo used for documents,
 * and for the company logo we will prefer using a vectorial version, and in case there is
 * no vectorial image available, pick the largest image.
 *
 * This migration will only be done when logo images are found in the deprecated logo fields. If none,
 * new logo fields won't be updated
 */
public class MigrateExistingCompanyLogos extends ModuleScript {
  static Logger log4j = LogManager.getLogger();

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();

      migrateLogosInSystemInfo(cp);
      migrateLogosInClientInfo(cp);
      migrateLogosInOrgInfo(cp);
      clearOldLogos(cp);

    } catch (Exception e) {
      handleError(e);
    }
  }

  private void migrateLogosInSystemInfo(ConnectionProvider cp) throws Exception {
    // For docs, pick the image contained in your_company_document_image
    String logoForDocsSysInfo = MigrateExistingCompanyLogosForDocsData
        .selectLogoForDocumentsInSystemInfo(cp);

    // For the company logo we'll pick the first vectorial image, and if none is vectorial,
    // we'll pick the largest from the fields below by height:
    // - your_company_big_image,
    // - your_company_login_image,
    // - your_company_menu_image
    String companyLogoSysInfo = MigrateExistingCompanyLogosData
        .selectVectorialCompanyLogoInSystemInfo(cp);
    if (companyLogoSysInfo == null) {
      companyLogoSysInfo = MigrateExistingCompanyLogosData.selectLargestCompanyLogoInSystemInfo(cp);
    }

    // Update logos if found any
    if (logoExists(logoForDocsSysInfo) || logoExists(companyLogoSysInfo)) {
      log4j.warn("Updating logos in sys info " + logoForDocsSysInfo + " and " + companyLogoSysInfo);
      MigrateExistingCompanyLogosData.updateLogosInSystemInfo(cp, companyLogoSysInfo,
              logoForDocsSysInfo);
    }
    else {
      log4j.warn("skipping update in sysinfo");
    }
  }

  private void migrateLogosInClientInfo(ConnectionProvider cp) throws Exception {
    // From the list of all Client with clientInfo entries we pick your_company_document_image and set this
    // image as the new company_logo_for_docs.
    //
    // For the company logo we'll pick the first vectorial image, and if none is vectorial,
    // we'll pick the largest from the fields below by height:
    // - your_company_menu_image
    // - your_company_big_image
    MigrateExistingCompanyLogosForDocsData[] docLogosPerClient = MigrateExistingCompanyLogosForDocsData
        .selectLogoForDocumentsInClients(cp);
    for (MigrateExistingCompanyLogosForDocsData docLogo : docLogosPerClient) {
      String companyLogoForClient = MigrateExistingCompanyLogosData
          .selectVectorialCompanyLogoInClientInfo(cp, docLogo.adClientId);
      if (companyLogoForClient == null) {
        companyLogoForClient = MigrateExistingCompanyLogosData.selectLargestLogoInClientInfo(cp,
            docLogo.adClientId);
      }

      if (logoExists(docLogo.yourCompanyDocumentImage) || logoExists(companyLogoForClient)) {
        log4j.warn("Updating logos in client info " + docLogo.yourCompanyDocumentImage + " and " + companyLogoForClient);
        MigrateExistingCompanyLogosData.updateLogosInClientInfo(cp, companyLogoForClient,
                docLogo.yourCompanyDocumentImage, docLogo.adClientId);
      }
      else {
        log4j.warn("skipping update in client");
      }
    }
  }

  private void migrateLogosInOrgInfo(ConnectionProvider cp) throws Exception {
    // From the list of all organizations with orgInfo entry that has a document logo
    // we'll pick the image referenced in your_company_document_image to be the value of
    // company_logo_for_docs of the given organization
    MigrateExistingCompanyLogosForDocsData[] docLogosPerOrg = MigrateExistingCompanyLogosForDocsData
        .selectLogoForDocumentsInOrganizations(cp);
    for (MigrateExistingCompanyLogosForDocsData docLogo : docLogosPerOrg) {
      if (logoExists(docLogo.yourCompanyDocumentImage)) {
        MigrateExistingCompanyLogosData.updateLogosInOrgInfo(cp, docLogo.yourCompanyDocumentImage,
                docLogo.adOrgId);
      }
    }
  }

  private boolean logoExists(String logoId) {
    return logoId != null && !"".equals(logoId);
  }

  private void clearOldLogos(ConnectionProvider cp) throws Exception {
    MigrateExistingCompanyLogosData.clearOldLogosInSystem(cp);
    MigrateExistingCompanyLogosData.clearOldLogosInClient(cp);
    MigrateExistingCompanyLogosData.clearOldLogosInOrganization(cp);
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("0", null, new OpenbravoVersion(3, 0, 224901));
  }
}
