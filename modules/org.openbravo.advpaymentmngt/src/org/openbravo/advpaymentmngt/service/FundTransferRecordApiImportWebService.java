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
import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.APRM_FundTransferRec;
import org.openbravo.advpaymentmngt.utility.FundsTransferUtility;
import org.openbravo.api.service.JSONWebService;
import org.openbravo.api.service.JSONWebServiceResult;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.service.json.JsonUtils;

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
      JSONObject jsonData = rawData.getJSONObject(0);

      // Format Date
      String strDate = jsonData.optString("date");
      Date date = "".equals(strDate) ? null : JsonUtils.createDateFormat().parse(strDate);

      // Account from
      String strAccountFrom = jsonData.getString("accountFrom");
      FIN_FinancialAccount accountFrom = this.getFinancialAccountByName(strAccountFrom);

      // Account to
      String strAccountTo = jsonData.getString("accountTo");
      FIN_FinancialAccount accountTo = this.getFinancialAccountByName(strAccountTo);

      // GL item
      String strGlItem = jsonData.getString("glItem");
      GLItem glItem = this.getGlItemByName(strGlItem);

      // Amount
      BigDecimal amount = new BigDecimal(jsonData.getString("amount"));

      // Description
      String description = jsonData.getString("description");

      APRM_FundTransferRec fundTransferRecCreated = FundsTransferUtility.createTransfer(date,
          accountFrom, accountTo, glItem, amount, null, null, null, description);

      responseJSON
          .add((new JSONObject()).put("documentNo", fundTransferRecCreated.getDocumentNo()));

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

  private FIN_FinancialAccount getFinancialAccountByName(String name) {
    OBCriteria<FIN_FinancialAccount> fAccountQuery = OBDal.getInstance()
        .createCriteria(FIN_FinancialAccount.class);
    fAccountQuery.add(Restrictions.eq(FIN_FinancialAccount.PROPERTY_NAME, name));
    fAccountQuery.setMaxResults(1);
    return (FIN_FinancialAccount) fAccountQuery.uniqueResult();
  }

  private GLItem getGlItemByName(String name) {
    OBCriteria<GLItem> glItemQuery = OBDal.getInstance().createCriteria(GLItem.class);
    glItemQuery.add(Restrictions.eq(GLItem.PROPERTY_NAME, name));
    glItemQuery.setMaxResults(1);
    return (GLItem) glItemQuery.uniqueResult();
  }
}
