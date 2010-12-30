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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.dao.TransactionsDao;
import org.openbravo.advpaymentmngt.exception.NoExecutionProcessFoundException;
import org.openbravo.advpaymentmngt.utility.FIN_PaymentExecutionProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcess;
import org.openbravo.model.financialmgmt.payment.PaymentExecutionProcessParameter;
import org.openbravo.model.financialmgmt.payment.PaymentRun;
import org.openbravo.model.financialmgmt.payment.PaymentRunPayment;

public class FIN_ExecutePayment {
  private AdvPaymentMngtDao dao;
  private FIN_PaymentExecutionProcess paymentExecutionProcess = null;
  private PaymentExecutionProcess executionProcess;
  private HashMap<String, String> constantParameters;
  private HashMap<String, String> parameters;
  private PaymentRun paymentRun;

  public void init(String sourceType, PaymentExecutionProcess _executionProcess,
      List<FIN_Payment> payments, HashMap<String, String> _parameters, Organization organization)
      throws NoExecutionProcessFoundException {

    this.dao = new AdvPaymentMngtDao();
    this.executionProcess = _executionProcess;
    setConstantParameters();
    if (_parameters == null)
      setDefaultParameters();
    else
      this.parameters = _parameters;
    this.paymentRun = dao.getNewPaymentRun(sourceType, executionProcess, organization);
    for (FIN_Payment payment : payments)
      dao.getNewPaymentRunPayment(paymentRun, payment);
    final List<PaymentExecutionProcessParameter> allParameters = executionProcess
        .getFinancialMgmtPaymentExecutionProcessParameterList();
    for (PaymentExecutionProcessParameter parameter : allParameters)
      if ("IN".equals(parameter.getParameterType()))
        dao.getNewPaymentRunParameter(paymentRun, parameter, parameters.get(parameter
            .getSearchKey()));
      else if ("CONSTANT".equals(parameter.getParameterType()))
        dao.getNewPaymentRunParameter(paymentRun, parameter, parameter.getDefaultTextValue());
    try {
      this.paymentExecutionProcess = (FIN_PaymentExecutionProcess) Class.forName(
          executionProcess.getJavaClassName()).newInstance();
    } catch (InstantiationException e) {
      throw new NoExecutionProcessFoundException(e);
    } catch (IllegalAccessException e) {
      throw new NoExecutionProcessFoundException(e);
    } catch (ClassNotFoundException e) {
      throw new NoExecutionProcessFoundException(e);
    }

  }

  public OBError execute() {
    try {
      if (paymentExecutionProcess != null) {
        for (PaymentRunPayment paymentRunPayment : paymentRun
            .getFinancialMgmtPaymentRunPaymentList()) {
          if (dao.isPaymentBeingExecuted(paymentRunPayment.getPayment())) {
            paymentRunPayment.setResult("E");
            paymentRunPayment.setMessage("@APRM_PaymentInExecution@");
            OBDal.getInstance().save(paymentRunPayment);
            OBDal.getInstance().flush();
          } else
            dao.setPaymentExecuting(paymentRunPayment.getPayment(), true);
        }

        OBError result = paymentExecutionProcess.execute(paymentRun);

        for (PaymentRunPayment paymentRunPayment : paymentRun
            .getFinancialMgmtPaymentRunPaymentList()) {
          if (dao.isPaymentBeingExecuted(paymentRunPayment.getPayment())) {
            dao.setPaymentExecuting(paymentRunPayment.getPayment(), false);
            if ("S".equals(paymentRunPayment.getResult()))
              dao.removeFromExecutionPending(paymentRunPayment.getPayment());
          }

          if ("S".equals(paymentRunPayment.getResult())) {
            String paymentStatus = paymentRunPayment.getPayment().getStatus();
            if ("RPAE".equals(paymentStatus)) {
              paymentStatus = paymentRunPayment.getPayment().isReceipt() ? "RPR" : "PPM";
              paymentRunPayment.getPayment().setStatus(paymentStatus);
            }
            paymentRunPayment.getPayment().setPosted("N");
            if (("RPR".equals(paymentStatus) || "PPM".equals(paymentStatus))
                && FIN_Utility.isAutomaticDepositWithdrawn(paymentRunPayment.getPayment())
                && paymentRunPayment.getPayment().getAmount().compareTo(BigDecimal.ZERO) != 0) {
              FIN_FinaccTransaction transaction = TransactionsDao
                  .createFinAccTransaction(paymentRunPayment.getPayment());
              TransactionsDao.process(transaction);
            }
          }
          OBDal.getInstance().save(paymentRunPayment.getPayment());
        }
        OBDal.getInstance().flush();
        return result;
      } else
        throw new NoExecutionProcessFoundException(
            "No Execution Process has been found to execute the Payment");
    } catch (final Exception e) {
      e.printStackTrace(System.err);
      OBError error = new OBError();
      error.setType("Error");
      error.setMessage("@IssueOnExecutionProcess@");
      return error;
    }
  }

  private void setDefaultParameters() {
    final List<PaymentExecutionProcessParameter> allParameters = executionProcess
        .getFinancialMgmtPaymentExecutionProcessParameterList();
    for (PaymentExecutionProcessParameter parameter : allParameters)
      if ("IN".equals(parameter.getInputType()))
        if ("CHECK".equals(parameter.getParameterType()))
          constantParameters.put(parameter.getSearchKey(), parameter.getDefaultValueForFlag());
        else if ("TEXT".equals(parameter.getParameterType()))
          constantParameters.put(parameter.getSearchKey(), parameter.getDefaultTextValue());
  }

  private void setConstantParameters() {
    final List<PaymentExecutionProcessParameter> allParameters = executionProcess
        .getFinancialMgmtPaymentExecutionProcessParameterList();
    for (PaymentExecutionProcessParameter parameter : allParameters)
      if ("CONSTANT".equals(parameter.getInputType()))
        constantParameters.put(parameter.getSearchKey(), parameter.getDefaultTextValue());
  }
}
