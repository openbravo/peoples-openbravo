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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.costing;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelCostAdjustment extends BaseActionHandler {
  private static Logger log = LoggerFactory.getLogger(CancelCostAdjustment.class);
  final static String strCategoryCostAdj = "CAD";
  final static String strTableCostAdj = "M_CostAdjustment";

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      final JSONObject jsonData = new JSONObject(data);
      String caId = jsonData.getString("inpmCostadjustmentId");
      CostAdjustment costAdjustmentOrig = OBDal.getInstance().get(CostAdjustment.class, caId);
      CostAdjustment costAdjustmentCancel = (CostAdjustment) DalUtil.copy(costAdjustmentOrig, true);
      for (CostAdjustmentLine costAdjustmentline : costAdjustmentCancel.getCostAdjustmentLineList()) {
        costAdjustmentline.setSource(true);
        costAdjustmentline.setAdjustmentAmount(costAdjustmentline.getAdjustmentAmount().negate());
      }
      final DocumentType docType = FIN_Utility.getDocumentType(
          costAdjustmentOrig.getOrganization(), strCategoryCostAdj);
      final String docNo = FIN_Utility.getDocumentNo(docType, strTableCostAdj);
      costAdjustmentCancel.setDocumentNo(docNo);

      costAdjustmentOrig.setCostadjustmentCancel(costAdjustmentCancel);
      costAdjustmentOrig.setDocstatus("VO");
      OBDal.getInstance().save(costAdjustmentCancel);
      OBDal.getInstance().save(costAdjustmentOrig);

      String message = "Ok";
      errorMessage.put("severity", "success");
      errorMessage.put("text", message);
      result.put("message", errorMessage);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", "Error");
        errorMessage.put("text", message);
        result.put("message", errorMessage);
      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }
}