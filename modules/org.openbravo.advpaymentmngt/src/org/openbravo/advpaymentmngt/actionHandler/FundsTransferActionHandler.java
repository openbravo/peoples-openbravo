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
 * All portions are Copyright (C) 2016-2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FundsTransferUtility;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.application.process.ResponseActionsBuilder.MessageType;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.service.json.JsonUtils;

/**
 * This class implements the ability to transfer funds among financial accounts in a simple and
 * quick way. The idea is to have a button in the Financial Account window to transfer money.
 * 
 * @author Daniel Martins
 */
public class FundsTransferActionHandler extends BaseProcessActionHandler {
  private static final String ERROR_IN_PROCESS = "Error in process";
  private static final Logger log = LogManager.getLogger();

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {

    try {
      JSONObject request = new JSONObject(content);
      JSONObject jsonParams = request.getJSONObject("_params");

      // Format Date
      String strTrxDate = jsonParams.getString("trxdate");
      Date trxDate = JsonUtils.createDateFormat().parse(strTrxDate);

      // Account from
      String strAccountFrom = request.getString("inpfinFinancialAccountId");
      FIN_FinancialAccount accountFrom = OBDal.getInstance()
          .get(FIN_FinancialAccount.class, strAccountFrom);

      // Account to
      String strAccountTo = jsonParams.getString("fin_financial_account_id");
      FIN_FinancialAccount accountTo = OBDal.getInstance()
          .get(FIN_FinancialAccount.class, strAccountTo);

      // GL item
      String strGLItem = jsonParams.getString("glitem");
      GLItem glitem = OBDal.getInstance().get(GLItem.class, strGLItem);

      // Amount
      BigDecimal amount = new BigDecimal(jsonParams.getString("deposit_amount"));

      String description = jsonParams.getString("description");

      // Conversion Rate
      BigDecimal manualConversionRate = null;
      if (accountFrom.getCurrency().getId().equalsIgnoreCase(accountTo.getCurrency().getId())) {
        manualConversionRate = BigDecimal.ONE;
      } else if (!jsonParams.isNull("multiply_rate")) {
        manualConversionRate = new BigDecimal(jsonParams.getString("multiply_rate"));
      }

      // Fees
      BigDecimal bankFeeFrom = BigDecimal.ZERO;
      BigDecimal bankFeeTo = BigDecimal.ZERO;
      if (jsonParams.getBoolean("bank_fee")) {
        if (!jsonParams.isNull("bank_fee_from")) {
          bankFeeFrom = new BigDecimal(jsonParams.getString("bank_fee_from"));
        }
        if (!jsonParams.isNull("bank_fee_to")) {
          bankFeeTo = new BigDecimal(jsonParams.getString("bank_fee_to"));
        }
      }

      FundsTransferUtility.createTransfer(trxDate, accountFrom, accountTo, glitem, amount,
          manualConversionRate, bankFeeFrom, bankFeeTo, description);

    } catch (OBException e) {
      log.error(ERROR_IN_PROCESS, e);
      return getResponseBuilder()
          .showMsgInProcessView(MessageType.ERROR, OBMessageUtils.messageBD("error"),
              e.getMessage(), true)
          .retryExecution()
          .build();
    } catch (Exception e) {
      log.error(ERROR_IN_PROCESS, e);
      return getResponseBuilder()
          .showMsgInProcessView(MessageType.ERROR, OBMessageUtils.messageBD("error"),
              OBMessageUtils.messageBD("APRM_UnknownError"))
          .build();
    }
    return getResponseBuilder()
        .showMsgInProcessView(MessageType.SUCCESS, OBMessageUtils.messageBD("success"),
            OBMessageUtils.messageBD("APRM_TransferFundsSuccess"))
        .refreshGrid()
        .build();
  }

  // Deprecated: do not call to this function, call instead to FundsTransferUtility.createTransfer
  public static void createTransfer(Date date, FIN_FinancialAccount accountFrom,
      FIN_FinancialAccount accountTo, GLItem glitem, BigDecimal amount,
      BigDecimal manualConversionRate, BigDecimal bankFeeFrom, BigDecimal bankFeeTo,
      String description) {
    FundsTransferUtility.createTransfer(date, accountFrom, accountTo, glitem, amount,
        manualConversionRate, bankFeeFrom, bankFeeTo, description);
  }

}
