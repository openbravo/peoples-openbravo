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

package org.openbravo.test.xml;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.structure.ClientEnabled;
import org.openbravo.service.db.ClientImportProcessor;
import org.openbravo.service.db.DataExportService;
import org.openbravo.service.db.DataImportService;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.db.ImportResult;
import org.openbravo.service.db.ReferenceDataTask;

/**
 * Tests export and import of client dataset.
 * 
 * @author mtaal
 */

public class ClientExportImportTest extends XMLBaseTest {

  public void testImportReferenceData() {
    setErrorOccured(true);
    setUserContext("0");

    final String sourcePath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("source.path");
    final File importDir = new File(sourcePath, ReferenceDataTask.REFERENCE_DATA_DIRECTORY);

    for (final File importFile : importDir.listFiles()) {
      if (importFile.isDirectory()) {
        continue;
      }
      String xml = DbUtility.readFile(importFile);
      final ClientImportProcessor importProcessor = new ClientImportProcessor();
      importProcessor.setNewName(null);
      final ImportResult ir = DataImportService.getInstance().importClientData(xml,
          importProcessor, false);
      xml = null; // set to null to make debugging faster
      if (ir.hasErrorOccured()) {
        if (ir.getException() != null) {
          throw new OBException(ir.getException());
        }
        if (ir.getErrorMessages() != null) {
          throw new OBException(ir.getErrorMessages());
        }
      }
    }
    setErrorOccured(false);
  }

  public void _testExportImportClient1000000() {
    exportImport("1000000");
  }

  public void _testExportImportClient1000001() {
    exportImport("1000001");
  }

  private void exportImport(String clientId) {
    setErrorOccured(true);
    setUserContext("0");
    final Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put(DataExportService.CLIENT_ID_PARAMETER_NAME, clientId);
    String xml = DataExportService.getInstance().exportClientToXML(parameters, false);

    try {
      final File f = new File("/tmp/export.xml");
      if (f.exists()) {
        f.delete();
      }
      final FileWriter fw = new FileWriter(f);
      fw.write(xml);
      fw.close();
    } catch (final Exception e) {
      throw new OBException(e);
    }

    final ClientImportProcessor importProcessor = new ClientImportProcessor();
    importProcessor.setNewName("" + System.currentTimeMillis());
    try {
      final ImportResult ir = DataImportService.getInstance().importClientData(xml,
          importProcessor, false);
      xml = null;
      if (ir.getException() != null) {
        ir.getException().printStackTrace(System.err);
        throw new OBException(ir.getException());
      }
      if (ir.getErrorMessages() != null) {
        fail(ir.getErrorMessages());
      }
      // none should be updated!
      assertEquals(0, ir.getUpdatedObjects().size());

      // and never insert anything in client 0
      for (final BaseOBObject bob : ir.getInsertedObjects()) {
        if (bob instanceof ClientEnabled) {
          final ClientEnabled ce = (ClientEnabled) bob;
          assertNotNull(ce.getClient());
          assertTrue(!ce.getClient().getId().equals("0"));
        }
      }

      System.err.println(ir.getWarningMessages());
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      throw new OBException(e);
    }
    setErrorOccured(true);
  }
}