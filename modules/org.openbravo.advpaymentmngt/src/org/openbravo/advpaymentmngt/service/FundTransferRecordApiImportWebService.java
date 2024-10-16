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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.APRM_FundTransferRec;
import org.openbravo.advpaymentmngt.utility.FundsTransferUtility;
import org.openbravo.api.service.JSONWebService;
import org.openbravo.api.service.JSONWebServiceResult;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.mobile.core.process.JSONPropertyToEntity;

/**
 * Create fund transfer record web service
 */
public class FundTransferRecordApiImportWebService extends JSONWebService {

  private static final Logger log = LogManager.getLogger();

  @Override
  protected JSONWebServiceResult doGet(String path, HttpServletRequest request) {
    throw new UnsupportedOperationException(
        OBMessageUtils.messageBD("RETAPCO_GetRequestsNotSupported"));
  }

  @Override
  protected JSONWebServiceResult doPut(String path, HttpServletRequest request) {
    throw new UnsupportedOperationException(
        OBMessageUtils.messageBD("RETAPCO_PutRequestsNotSupported"));
  }

  @Override
  protected JSONWebServiceResult doDelete(String path, HttpServletRequest request) {
    throw new UnsupportedOperationException(
        OBMessageUtils.messageBD("RETAPCO_DeleteRequestsNotSupported"));
  }

  @Override
  protected JSONWebServiceResult doPost(String path, HttpServletRequest request) {
    Stream.Builder<JSONObject> responseJSON = Stream.builder();
    try {
      // Get request data
      JSONArray rawData = FundTransferRecordApiUtils.getRequestContent(request);

      // Execute hook methods before consumption, passing the rawData
      executeFundTransferRecordApiHook(rawData);

      // Generate the import EDL request
      JSONObject data = rawData.getJSONObject(0);
      JSONObject result = WeldUtils
          .getInstanceFromStaticBeanManager(FundTransferRecordExtension.class)
          .exec(data);

      FundsTransferUtility.createTransfer(result.get("date"), result.get("accountFrom"),
          result.get("accountTo"), result.get("glItem"), result.get("amount"), null, null, null,
          result.get("description"));

      APRM_FundTransferRec fundTransferRecord = OBDal.getInstance()
          .get(APRM_FundTransferRec.class, result.get("id"));
      // Execute hook methods after consumption, passing the rawData
      executeFundTransferRecordApiPostHook(rawData, fundTransferRecord);

      setPropertiesFromJSON(fundTransferRecord, data);

      responseJSON.add(result);

    } catch (IOException ex) {
      throw new OBException("Could not retrieve request body", ex);
    } catch (OBException obEx) {
      // For custom validation exceptions (OBException), include the specific message
      log.error("Validation error: {}", obEx.getMessage(), obEx);
      responseJSON.add(FundTransferRecordApiUtils.buildErrorResponse(HttpServletResponse.SC_OK,
          obEx.getMessage()));
    } catch (Exception ex) {
      log.error(OBMessageUtils.messageBD("APRM_ERROR_CREATING_TRANSFER"), ex);
      throw new OBException(OBMessageUtils.messageBD("APRM_ERROR_CREATING_TRANSFER"), ex);
    }
    return new JSONWebServiceResult(responseJSON.build()).setMultiple(false)
        .setSuccessStatus(HttpServletResponse.SC_OK);
  }

  private void setPropertiesFromJSON(APRM_FundTransferRec fundTransfer, JSONObject data)
      throws JSONException {
    data.remove("client");
    data.remove("organization");
    JSONPropertyToEntity.fillBobFromJSON(fundTransfer.getEntity(), fundTransfer, data);

  }

  private void executeFundTransferRecordApiHook(JSONArray jsonFundTransfer) throws Exception {
    List<FundTransferRecordApiHook> fundTransferRecordHooks = WeldUtils
        .getInstances(FundTransferRecordApiHook.class);
    for (FundTransferRecordApiHook proc : fundTransferRecordHooks) {
      proc.exec(jsonFundTransfer);
    }
  }

  private void executeFundTransferRecordApiPostHook(JSONArray jsonFundTransfer,
      APRM_FundTransferRec fundTransfer) throws Exception {
    List<FundTransferRecordApiPostHook> fundTransferRecordHooks = WeldUtils
        .getInstances(FundTransferRecordApiPostHook.class);
    for (FundTransferRecordApiPostHook proc : fundTransferRecordHooks) {
      proc.exec(jsonFundTransfer, fundTransfer);
    }
  }
}
