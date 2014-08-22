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
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.costing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceDifferenceByDateProcess extends BaseProcessActionHandler {
  final static private Logger log = LoggerFactory.getLogger(PriceDifferenceByDateProcess.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    String message = new String();
    OBContext.setAdminMode(true);
    try {
      jsonRequest = new JSONObject(content);
      JSONObject params = jsonRequest.getJSONObject("_params");
      log.debug("{}", jsonRequest);
      JSONArray productIds = params.getJSONArray("M_Product_ID");
      String mvdate = params.getString("movementdate");
      String orgId = params.getString("ad_org_id");

      Date movementdate = JsonUtils.createDateFormat().parse(mvdate);
      doChecks(movementdate, message);

      String strUpdate = "UPDATE MaterialMgmtMaterialTransaction trx"
          + " SET checkpricedifference = 'Y'"
          + " WHERE exists ("
          + " SELECT 1"
          + " FROM  ProcurementReceiptInvoiceMatch mpo"
          + " WHERE trx.isCostCalculated = 'Y' and mpo.goodsShipmentLine.id = trx.goodsShipmentLine.id  "
          + " AND trx.movementDate >= :date and trx.organization.id in (:orgIds))";
      for (int i = 0; i < productIds.length(); i++) {
        final String strProductId = productIds.getString(i);
        if (i == 0) {
          strUpdate += " AND (product.id = '" + strProductId + "' ";
        } else {
          strUpdate += " OR product.id = '" + strProductId + "' ";
        }
        if (i == productIds.length() - 1) {
          strUpdate += ")";
        }
      }

      Query update = OBDal.getInstance().getSession().createQuery(strUpdate);
      update.setParameterList("orgIds",
          new OrganizationStructureProvider().getChildTree(orgId, true));
      update.setDate("date", movementdate);

      int updated = update.executeUpdate();

      Map<String, String> map = new HashMap<String, String>();
      map.put("trxsNumber", Integer.toString(updated));

      String messageText = OBMessageUtils.messageBD("PriceDifferenceChecked");
      JSONObject msg = new JSONObject();
      msg.put("severity", "success");
      msg.put("text", OBMessageUtils.parseTranslation(messageText, map));
      jsonRequest.put("message", msg);

    } catch (Exception e) {
      log.error("Error Process Price Correction", e);

      try {
        jsonRequest = new JSONObject();
        jsonRequest.put("retryExecution", true);
        if (message.isEmpty()) {
          Throwable ex = DbUtility.getUnderlyingSQLException(e);
          message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        }
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);
        return jsonRequest;
      } catch (Exception ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void doChecks(Date movementdate, String message) {
    // FIXME: The date is in a open period (and following periods are opened too)
    // throw new OBException("");
  }
}