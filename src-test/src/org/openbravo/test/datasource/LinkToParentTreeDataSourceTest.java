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
 * All portions are Copyright (C) 2010-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.datasource;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test case for LinkToParent tree Datasource
 *
 * @author jarmendariz
 */
public class LinkToParentTreeDataSourceTest extends BaseDataSourceTestDal {

  private static final Logger log = LoggerFactory.getLogger(LinkToParentTreeDataSourceTest.class);

  private static final int STATUS_OK = 0;
  private static final int NUMBER_OF_COST_ADJUSTMENT_LINES = 2;
  private CostAdjustmentTestDataHelper dataHelper = new CostAdjustmentTestDataHelper();

  private String costAdjustmentId;

  @Before
  public void setUpCostAdjustmentData() {
    CostAdjustment costAdjustment = this.dataHelper
        .createCostAdjustmentWithActiveAndNonActiveLines();
    this.costAdjustmentId = costAdjustment.getId();
  }

  /**
   * Ensure that fetching cost adjustment lines both active and non-active lines are retrieved
   */
  @Test
  public void fetchIncludeNonActiveFields() {
    assertEquals(NUMBER_OF_COST_ADJUSTMENT_LINES, this.getNumberOfCostAdjustmentLines());
  }

  @After
  public void tearDownLinkToParentData() {
    this.dataHelper.removeCostAdjustment(this.costAdjustmentId);
    this.costAdjustmentId = null;
  }

  private int getNumberOfCostAdjustmentLines() {
    try {
      JSONObject response = this.requestCostAdjustmentLines();
      if (this.isResponseOk(response)) {
        return this.getNumberOfDataItems(response);
      } else {
        log.error("DataSource response has no items");
        return 0;
      }
    } catch (Exception exception) {
      log.error("Cost Adjustment request from DataSource failed", exception);
      return 0;
    }
  }

  private boolean isResponseOk(JSONObject response) throws JSONException {
    return response.getInt("status") == STATUS_OK;
  }

  private int getNumberOfDataItems(JSONObject response) throws JSONException {
    return response.getJSONArray("data").length();
  }

  private JSONObject requestCostAdjustmentLines() throws Exception {
    Map<String, String> params = this.generateCostAdjustmentLinesParams(this.costAdjustmentId);

    return new JSONObject(this.doRequest(
        "/org.openbravo.service.datasource/610BEAE5E223447DBE6FF672B703F72F", params, 200, "POST"))
        .getJSONObject("response");
  }

  private Map<String, String> generateCostAdjustmentLinesParams(String id) {
    Map<String, String> params = new HashMap<>();
    params.put("_operationType", "fetch");
    params.put("_startRow", "0");
    params.put("_endRow", "200");
    params.put("referencedTableId", "34E79323CEC847C2A9ED2C8430AC73D1");
    params.put("parentRecordId", id);
    params.put("tabId", "06DCB72BB6D24F82BCDA5FFF8EA0425C");
    params.put("@CostAdjustment.id@", id);
    params
        .put(
            "criteria",
            "{\"_constructor\":\"AdvancedCriteria\",\"fieldName\":\"parentId\",\"value\":\"-1\",\"operator\":\"equals\"}");

    return params;
  }

  private class CostAdjustmentTestDataHelper {

    private static final String DOCUMENT_TYPE_ID = "82000D718BDA40C38F83FA1A5FFF6419";
    private static final String SOURCE_PROCESS = "MCC";
    private static final String DOCUMENT_NO = "::DOCUMENT-NO::";

    public CostAdjustment createCostAdjustmentWithActiveAndNonActiveLines() {
      OBContext.setOBContext(TEST_USER_ID);
      OBDal obdal = OBDal.getInstance();

      CostAdjustment costAdjustment = OBProvider.getInstance().get(CostAdjustment.class);
      costAdjustment.setDocumentType(obdal.get(DocumentType.class, DOCUMENT_TYPE_ID));
      costAdjustment.setDocumentNo(DOCUMENT_NO);
      costAdjustment.setSourceProcess(SOURCE_PROCESS);

      obdal.save(costAdjustment);

      this.createActiveCostAdjustmentLine(costAdjustment);
      this.createNonActiveCostAdjustmentLine(costAdjustment);

      OBDal.getInstance().commitAndClose();

      return costAdjustment;
    }

    public void removeCostAdjustment(String id) {
      OBDal obdal = OBDal.getInstance();
      obdal.remove(obdal.getProxy(CostAdjustment.class, id));
      obdal.commitAndClose();
    }

    private CostAdjustmentLine createActiveCostAdjustmentLine(CostAdjustment costAdjustment) {
      return this.createCostAdjustmentLine(costAdjustment, 100L, true);
    }

    private CostAdjustmentLine createNonActiveCostAdjustmentLine(CostAdjustment costAdjustment) {
      return this.createCostAdjustmentLine(costAdjustment, 200L, false);
    }

    private CostAdjustmentLine createCostAdjustmentLine(CostAdjustment costAdjustment, Long lineNo,
        boolean isActive) {
      OBDal obdal = OBDal.getInstance();

      CostAdjustmentLine line = OBProvider.getInstance().get(CostAdjustmentLine.class);
      line.setLineNo(lineNo);
      line.setCostAdjustment(costAdjustment);
      line.setCurrency(obdal.getProxy(Currency.class, EURO_ID));
      line.setActive(isActive);

      obdal.save(line);

      return line;
    }

  }
}
