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

package org.openbravo.common.actionhandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.process.GenericExporterActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.service.db.CallProcess;

public class ExportPhysicalInventoryToCSV extends GenericExporterActionHandler {
  final static private Logger log = LogManager.getLogger();

  @Override
  protected void verifyData(JSONObject data) {
  }

  @Override
  protected String generateTmpFile(Map<String, Object> parameters, JSONObject data) {
    JSONObject params;
    try {
      params = data.getJSONObject("_params");
      boolean generateLines = params.optBoolean("generateLines");
      String inventoryId = data.optString("M_Inventory_ID");

      InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, inventoryId);
      generateInventoryLines(generateLines, inventoryId, inventory);
      if (!inventory.getMaterialMgmtInventoryCountLineList().isEmpty()) {
        return createCSVFile(inventory).getName();
      } else {
        throw new OBException(OBMessageUtils.messageBD("No lines"));
      }
    } catch (Exception e) {
      log.error("Error generating tmp file: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  protected String getDownloadContentType() {
    return "text/csv";
  }

  @Override
  protected String getDownloadFileName(JSONObject data) {
    String inventoryId = data.optString("M_Inventory_ID");
    InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, inventoryId);
    return "STOCK_LEVEL_" + inventory.getOrganization().getSearchKey() + "_" + new Date() + ".csv";
  }

  private void generateInventoryLines(boolean generateLines, String inventoryId,
      InventoryCount inventory) {
    if (generateLines && inventory.getInventoryType().equals("T")) {
      // Delete lines, in order to replace and create again
      if (!inventory.getMaterialMgmtInventoryCountLineList().isEmpty()) {
        inventory.getMaterialMgmtInventoryCountLineList().clear();
        OBDal.getInstance().save(inventory);
        OBDal.getInstance().flush();
      }
      generateInventoryLinesProcess(inventoryId);
    }
    OBDal.getInstance().refresh(inventory);
  }

  private void generateInventoryLinesProcess(String inventoryId) {
    final String PROCESS_M_INVENTORY_LISTCREATE = "105";
    final org.openbravo.model.ad.ui.Process process = OBDal.getInstance()
        .get(org.openbravo.model.ad.ui.Process.class, PROCESS_M_INVENTORY_LISTCREATE);
    Map<String, String> parameters = new HashMap<>();
    parameters.put("M_Locator_ID", null);
    parameters.put("ProductValue", "%");
    parameters.put("M_Product_Category_ID", null);
    parameters.put("QtyRange", null);
    parameters.put("regularization", "N");
    parameters.put("ABC", null);

    final ProcessInstance pinstance = CallProcess.getInstance()
        .call(process, inventoryId, parameters);
    final OBError result = OBMessageUtils.getProcessInstanceMessage(pinstance);
    if (StringUtils.equals("Error", result.getType())) {
      throw new OBException(result.getMessage());
    }
  }

  private File createCSVFile(InventoryCount inventory) throws IOException {
    File file = Files.createTempFile("", ".csv").toFile();
    try (FileWriter outputfile = new FileWriter(file)) {
      // Comma as a separator and No Quote Character
      final Writer writer = new StringWriter();
      addPhysicalInventoryLinesToCsv(writer, inventory.getMaterialMgmtInventoryCountLineList());
      writer.close();
      outputfile.write(writer.toString());
    }
    return file;
  }

  private void addPhysicalInventoryLinesToCsv(final Writer writer,
      final List<InventoryCountLine> inventoryLineList) throws IOException {
    String fieldSeparator = getFieldSeparator();
    // adding header to csv
    writer.append(OBMessageUtils.messageBD("ProductUPCEAN") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("ProductSearchKey") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("Attributes") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("ExportedQty") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("QuantityCount") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("UOM") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("StorageBinSearchKey") + fieldSeparator);
    writer.append(OBMessageUtils.messageBD("GapReason") + fieldSeparator);
    writer.append("\n");
    // adding lines
    for (final InventoryCountLine inventoryLine : inventoryLineList) {
      writer.append(
          inventoryLine.getProduct().getUPCEAN() != null ? inventoryLine.getProduct().getUPCEAN()
              : "" + fieldSeparator);
      writer.append(inventoryLine.getProduct().getSearchKey() + fieldSeparator);
      writer
          .append((inventoryLine.getAttributeSetValue() != null
              ? inventoryLine.getAttributeSetValue().getDescription() != null
                  ? inventoryLine.getAttributeSetValue().getDescription()
                  : ""
              : "") + fieldSeparator);
      writer.append(inventoryLine.getBookQuantity() + fieldSeparator);
      writer.append("" + fieldSeparator);
      writer.append(inventoryLine.getUOM().getName() + fieldSeparator);
      writer.append(inventoryLine.getStorageBin().getSearchKey() + fieldSeparator);
      writer.append("" + fieldSeparator);
      writer.append("\n");
    }
  }

  private String getFieldSeparator() {
    String fieldSeparator = "";
    try {
      fieldSeparator = Preferences.getPreferenceValue("OBSERDS_CSVFieldSeparator", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
    } catch (PropertyNotFoundException e) {
      // There is no preference for the field separator. Using the default one.
      fieldSeparator = ",";
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }
    return fieldSeparator;
  }

}
