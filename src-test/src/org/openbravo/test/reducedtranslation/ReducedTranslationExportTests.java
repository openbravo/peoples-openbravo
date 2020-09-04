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
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.test.reducedtranslation;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.TranslationManager;
import org.openbravo.model.ad.system.Language;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests that checks some examples of expected scenarios where the reduced translation version
 * should exclude or include some terms
 */
public class ReducedTranslationExportTests extends OBBaseTest {
  private static final String ATTACHFOLDER = OBPropertiesProvider.getInstance()
      .getOpenbravoProperties()
      .getProperty("attach.path");
  private static final File FULL_TRL_DIR = new File(ATTACHFOLDER + "/full");
  private static final File REDUCED_TRL_DIR = new File(ATTACHFOLDER + "/reduced");
  private static final String CLIENT_0 = "0";
  private static final String ES_ES_LANG_ID = "140";
  private static final String ES_ES = "es_ES";

  @BeforeClass
  public static void exportReducedTranslation() {
    doVerifyLanguageIfNotPreviouslyDoneBefore();
    exportReducedAndFullTranslations();
  }

  private static void doVerifyLanguageIfNotPreviouslyDoneBefore() {
    try {
      OBContext.setAdminMode(false);
      final Language esESLang = OBDal.getInstance().get(Language.class, ES_ES_LANG_ID);
      if (!esESLang.isSystemLanguage()) {
        esESLang.setSystemLanguage(true);
        CallStoredProcedure.getInstance()
            .call("AD_Language_Create", Arrays.asList((Object) null), null, true);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static void exportReducedAndFullTranslations() {
    REDUCED_TRL_DIR.mkdir();
    TranslationManager.exportTrl(new DalConnectionProvider(), REDUCED_TRL_DIR.getAbsolutePath(),
        ES_ES, CLIENT_0, ES_ES, true);
    FULL_TRL_DIR.mkdir();
    TranslationManager.exportTrl(new DalConnectionProvider(), FULL_TRL_DIR.getAbsolutePath(), ES_ES,
        CLIENT_0, ES_ES, false);
  }

  @AfterClass
  public static void removeTranslationFolders() throws IOException {
    FileUtils.deleteDirectory(REDUCED_TRL_DIR);
    FileUtils.deleteDirectory(FULL_TRL_DIR);
  }

  @Test
  public void testReducedTranslationSize() {
    assertThat("Reduced translation size", FileUtils.sizeOfDirectory(REDUCED_TRL_DIR),
        lessThan(FileUtils.sizeOfDirectory(FULL_TRL_DIR)));
  }

  @Test
  public void testAdTextInterfacesJrxml() throws IOException {
    testExistInFullAndNotExistsInReduced(
        "/lang/es_ES/org.openbravo.financial.paymentreport/AD_TEXTINTERFACES_TRL_es_ES.xml",
        "ACCS_ACCOUNT_ID_D");
  }

  @Test
  public void testAdElementDirectAccessInWindowExcludingTranslation() throws IOException {
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_ELEMENT_TRL_es_ES.xml", "\"Application\"");
  }

  @Test
  public void testAdElementIndirectAccessInWindowReferenceExcludingTranslation()
      throws IOException {
    testExistInFullAndNotExistsInReduced(
        "/lang/es_ES/org.openbravo.client.application/AD_ELEMENT_TRL_es_ES.xml", "\"Logger\"");
  }

  @Test
  public void testAdElementIndirectProcessDefinitionIncludedTranslation() throws IOException {
    testExistInBothTranslations("/lang/es_ES/AD_ELEMENT_TRL_es_ES.xml", "\"LC Costs\"");
  }

  @Test
  public void testAdElementDirectProcessIncludedTranslation() throws IOException {
    testExistInBothTranslations("/lang/es_ES/AD_ELEMENT_TRL_es_ES.xml", "\"Export Audit Info\"");
  }

  @Test
  public void testAdFieldExcludedTranslation() throws IOException {
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_FIELD_TRL_es_ES.xml",
        "\"Copy from Attribute\"");
  }

  @Test
  public void testAdProcessAdMenuExcludedTranslation() throws IOException {
    final String string = "\"Project Status Summary\"";
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_PROCESS_TRL_es_ES.xml", string);
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_MENU_TRL_es_ES.xml", string);
  }

  @Test
  public void testProcessDefinitionAdMenuAdWindowExcludedTranslation() throws IOException {
    final String string = "\"Log Management\"";
    testExistInFullAndNotExistsInReduced(
        "/lang/es_ES/org.openbravo.client.application/OBUIAPP_PROCESS_TRL_es_ES.xml", string);
    testExistInFullAndNotExistsInReduced(
        "/lang/es_ES/org.openbravo.client.application/AD_MENU_TRL_es_ES.xml", string);
    testExistInFullAndNotExistsInReduced(
        "/lang/es_ES/org.openbravo.client.application/AD_WINDOW_TRL_es_ES.xml", string);
  }

  @Test
  public void testAdProcessParaExcludedTranslation() throws IOException {
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_PROCESS_PARA_TRL_es_ES.xml",
        "Target Payment Rule");
  }

  @Test
  public void testAdWindowAdMenuExcludedTranslation() throws IOException {
    final String string = "\"Windows, Tabs, and Fields\"";
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_WINDOW_TRL_es_ES.xml", string);
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_MENU_TRL_es_ES.xml", string);
  }

  @Test
  public void testAdTabAdElementExcludedTranslation() throws IOException {
    final String string = "\"Window Translation\"";
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_TAB_TRL_es_ES.xml", string);
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_ELEMENT_TRL_es_ES.xml", string);
  }

  @Test
  public void testAdFieldGroupExcludedTranslation() throws IOException {
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_FIELDGROUP_TRL_es_ES.xml", "\"Assets\"");
  }

  @Test
  public void testAdRefListExcludedTranslation() throws IOException {
    testExistInFullAndNotExistsInReduced("/lang/es_ES/AD_REF_LIST_TRL_es_ES.xml", "\"Servlet\"");
  }

  @Test
  public void expectedEmptyTranslationFiles() {
    Stream
        .of("/lang/es_ES/AD_REFERENCE_TRL_es_ES.xml", "/lang/es_ES/OBUISEL_SELECTOR_TRL_es_ES.xml")
        .forEach(this::testExpectedEmptyTranslationFile);
  }

  private void testExistInFullAndNotExistsInReduced(final String fileRelativePath,
      final String string) throws IOException {
    assertThat("Full version " + fileRelativePath,
        FileUtils.readFileToString(new File(FULL_TRL_DIR.getAbsoluteFile() + fileRelativePath)),
        containsString(string));
    assertThat("Reduced version " + fileRelativePath,
        FileUtils.readFileToString(new File(REDUCED_TRL_DIR.getAbsoluteFile() + fileRelativePath)),
        not(containsString(string)));
  }

  private void testExistInBothTranslations(final String fileRelativePath, final String string)
      throws IOException {
    assertThat("Full version " + fileRelativePath,
        FileUtils.readFileToString(new File(FULL_TRL_DIR.getAbsoluteFile() + fileRelativePath)),
        containsString(string));
    assertThat("Reduced version " + fileRelativePath,
        FileUtils.readFileToString(new File(REDUCED_TRL_DIR.getAbsoluteFile() + fileRelativePath)),
        containsString(string));
  }

  private void testExpectedEmptyTranslationFile(final String fileRelativePath) {
    final long lineCountReduced = countLines(REDUCED_TRL_DIR, fileRelativePath);
    final long lineCountFull = countLines(FULL_TRL_DIR, fileRelativePath);

    final long XmlHeaderLinesOnly = 2l;
    assertThat("Reduced translation " + fileRelativePath, lineCountReduced,
        equalTo(XmlHeaderLinesOnly));
    assertThat("Reduced translation " + fileRelativePath, lineCountReduced,
        lessThan(lineCountFull));
  }

  private long countLines(final File translationDir, final String fileRelativePath) {
    try (final Stream<String> stream = Files
        .lines(new File(translationDir.getAbsoluteFile() + fileRelativePath).toPath())) {
      return stream.count();
    } catch (IOException e) {
      throw new OBException(e);
    }
  }

}
