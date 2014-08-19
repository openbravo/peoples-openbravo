package org.openbravo.advpaymentmngt.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.algorithm.StandardMatchingCandidatesAlgorithm;
import org.openbravo.advpaymentmngt.utility.FIN_CandidateRecord;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.payment.FIN_BankStatementLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.DataToJsonConverter;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;

public class CandidatesDatasource extends ReadOnlyDataSourceService {

  // Table ID of the datasource based table defined in the application dictionary
  private static final String AD_TABLE_ID = "85DA470A6BB3479E955D88C136EEC8A3";

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public String fetch(Map<String, String> parameters) {
    int startRow = 0;
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }

    final List<JSONObject> jsonObjects = fetchJSONObject(parameters);

    final JSONObject jsonResult = new JSONObject();
    final JSONObject jsonResponse = new JSONObject();
    try {
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, jsonObjects.size() + startRow - 1);
      // jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, getCount(parameters));
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
    } catch (JSONException e) {
    }

    return jsonResult.toString();
  }

  private List<JSONObject> fetchJSONObject(Map<String, String> parameters) {
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);
    int startRow = -1;
    int endRow = -1;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
    }
    if (endRowStr != null) {
      endRow = Integer.parseInt(endRowStr);
    }
    final List<Map<String, Object>> data = getData(parameters, startRow, endRow);
    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
    return toJsonConverter.convertToJsonObjects(data);
  }

  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    final String bankStatementLineId = parameters.get("bankStatementLineId");

    int offset = startRow;
    int nRows = endRow - startRow + 1;

    // String fetchType = null;
    // SQLQuery qry = null;
    // if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null) {
    // // FETCHTYPE = (STRING) PARAMETERS.GET(JSONCONSTANTS.DISTINCT_PARAMETER);
    // // QRY = OBDAL.GETINSTANCE().GETSESSION()
    // // .CREATESQLQuery(getDistinctProductQuery(parameters, offset, nRows));
    // } else {
    // fetchType = "grid";
    // qry = OBDal.getInstance().getSession().createSQLQuery(getSQLQuery(parameters, offset,
    // nRows));
    // }

    // TODO recover bank line
    final FIN_BankStatementLine line = OBDal.getInstance().get(FIN_BankStatementLine.class,
        bankStatementLineId);
    StandardMatchingCandidatesAlgorithm smc = new StandardMatchingCandidatesAlgorithm();
    List<FIN_CandidateRecord> transactionCandidates = smc.getTransactionCandidates(line,
        new ArrayList<FIN_FinaccTransaction>());
    transactionCandidates.addAll(smc.getPaymentCandidates(line, new ArrayList<FIN_Payment>()));
    transactionCandidates.addAll(smc.getInvoiceCandidates(line, new ArrayList<Invoice>()));
    transactionCandidates.addAll(smc.getOrderCandidates(line, new ArrayList<Order>()));

    for (FIN_CandidateRecord transactionCandidate : transactionCandidates) {
      result.add(transactionCandidate.toMap());
    }

    return result;
  }

  @Override
  protected int getCount(Map<String, String> parameters) {
    // TODO Auto-generated method stub
    return 1;
  }

}
