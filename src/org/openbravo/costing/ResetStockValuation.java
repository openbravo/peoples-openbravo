package org.openbravo.costing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.materialmgmt.cost.StockValuation;
import org.openbravo.service.db.CallStoredProcedure;

public class ResetStockValuation extends BaseProcessActionHandler {

  private static final Logger log = Logger.getLogger(ResetStockValuation.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    try {
      JSONObject result = new JSONObject();
      JSONObject msg = new JSONObject();
      boolean errorMessage = false;

      JSONObject request = new JSONObject(content);
      JSONObject params = request.getJSONObject("_params");

      // Do validations on param values
      String strOrgID = null;
      if (params.get("AD_Org_ID") != JSONObject.NULL) {
        strOrgID = (String) params.get("AD_Org_ID");
      }

      // delete existing records
      StringBuffer sql = new StringBuffer();
      sql.append("delete from");
      sql.append("\n " + StockValuation.ENTITY_NAME + " sv");
      sql.append("\n where sv." + StockValuation.PROPERTY_CLIENT + ".id = :client");
      if (strOrgID != null) {
        sql.append("\n and sv." + StockValuation.PROPERTY_ORGANIZATION + ".id = :org");
      }

      Query delQry = OBDal.getInstance().getSession().createQuery(sql.toString());
      delQry.setParameter("client", OBContext.getOBContext().getCurrentClient().getId());
      if (strOrgID != null) {
        delQry.setParameter("org", strOrgID);
      }
      delQry.executeUpdate();

      List<Object> storedProcedureParams = new ArrayList<Object>();
      storedProcedureParams.add(OBContext.getOBContext().getCurrentClient().getId());
      storedProcedureParams.add(strOrgID);
      storedProcedureParams.add(null);
      try {
        CallStoredProcedure.getInstance().call("M_INITIALIZE_STOCK_VALUATION",
            storedProcedureParams, null, true, false);
      } catch (Exception e) {
        errorMessage = true;
        msg.put("msgType", "error");
        msg.put("msgTitle", OBMessageUtils.messageBD("Error"));
        msg.put("msgText", OBMessageUtils.messageBD("ConversionRateCommandError"));
      }

      if (!errorMessage) {
        msg.put("msgType", "success");
        msg.put("msgTitle", OBMessageUtils.messageBD("Success"));
      }

      JSONArray actions = new JSONArray();
      JSONObject msgAction = new JSONObject();
      msgAction.put("showMsgInProcessView", msg);
      actions.put(msgAction);
      result.put("responseActions", actions);
      result.put("retryExecution", true);
      return result;

    } catch (JSONException e) {
      log.error("Error in process", e);
      return new JSONObject();
    }
  }
}
