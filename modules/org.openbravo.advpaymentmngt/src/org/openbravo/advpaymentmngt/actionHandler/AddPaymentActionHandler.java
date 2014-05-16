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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.ad_actionbutton.AddPaymentFromInvoice;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.project.Project;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPaymentActionHandler extends BaseProcessActionHandler {
  private static Logger log = LoggerFactory.getLogger(AddPaymentActionHandler.class);
  private AdvPaymentMngtDao dao;

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    ConnectionProvider conn = new DalConnectionProvider(true);
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    try {
      // Get Params
      jsonRequest = new JSONObject(content);
      String strAction = null;
      OBError message = null;
      dao = new AdvPaymentMngtDao();

      JSONObject jsonparams = jsonRequest.getJSONObject("_params");
      String strOrgId = jsonRequest.getString("inpadOrgId");
      String strInvoiceId = jsonRequest.getString("inpcInvoiceId");
      String strTabId = jsonRequest.getString("inpTabId");
      String strIssotrx = jsonRequest.getString("inpissotrx");
      boolean isReceipt = "Y".equals(strIssotrx);
      JSONObject orderInvoiceGrid = jsonparams.getJSONObject("order_invoice");
      JSONObject creditToUseGrid = jsonparams.getJSONObject("credit_to_use");
      JSONObject strAddedGLItems = jsonparams.getJSONObject("glitem");

      strAction = (isReceipt ? "PRP" : "PPP");

      String strPaymentDocumentNo = jsonparams.getString("payment_documentno");
      String strReferenceNo = jsonparams.getString("reference_no");
      String strCurrencyId = jsonparams.getString("c_currency_id");
      String strCurrencyToId = jsonparams.getString("c_currency_to_id");
      String strReceivedFromId = jsonparams.getString("received_from");
      String strFinancialAccountId = jsonparams.getString("fin_financial_account_id");
      String strActualPayment = jsonparams.getString("actual_payment");
      String strConvertedAmount = jsonparams.getString("converted_amount");

      // Format Date
      String strPaymentDate = jsonparams.getString("payment_date");
      Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(strPaymentDate);
      String formattedDate = OBDateUtils.formatDate(date);

      // String formattedDate = new SimpleDateFormat("dd-MM-yyyy",
      // Locale.getDefault()).format(date);
      strPaymentDate = formattedDate;

      String strPaymentMethodId = jsonparams.getString("fin_paymentmethod_id");
      String strExpectedPayment = jsonparams.getString("expected_payment");
      String strConversionRate = jsonparams.getString("conversion_rate");

      // TODO
      String strDifferenceAction = "";
      BigDecimal refundAmount = BigDecimal.ZERO;

      // Orders Invoices Grid
      JSONArray allselection = orderInvoiceGrid.getJSONArray("_selection");
      String strSelectedScheduledPaymentDetailIds = null;
      if (allselection.length() > 0) {
        strSelectedScheduledPaymentDetailIds = getSelectedRowIds(allselection);
      }

      // GL Items
      JSONArray addedGLITemsArray = strAddedGLItems.getJSONArray("_selection");

      BigDecimal exchangeRate = BigDecimal.ZERO;
      BigDecimal convertedAmount = new BigDecimal(0);
      if (!"null".equals(strConversionRate)) {
        exchangeRate = new BigDecimal(strConversionRate);
      }
      if (!"null".equals(strConvertedAmount)) {
        convertedAmount = new BigDecimal(strConvertedAmount);
      }

      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          strReceivedFromId);
      PriceList priceList = isReceipt ? businessPartner.getPriceList() : businessPartner
          .getPurchasePricelist();
      FIN_FinancialAccount finAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
          strFinancialAccountId);
      FIN_PaymentMethod finPaymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          strPaymentMethodId);
      AddPaymentFromInvoice avd = new AddPaymentFromInvoice();
      boolean paymentDocumentEnabled = avd.getDocumentConfirmation(conn, finAccount,
          finPaymentMethod, isReceipt, strActualPayment, true);

      List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
          FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
      HashMap<String, BigDecimal> selectedPaymentDetailAmounts = getSelectedPaymentDetailsAndAmount(
          allselection, strSelectedScheduledPaymentDetailIds);

      // get DocumentNo
      final List<Object> params = new ArrayList<Object>();
      params.add(vars.getClient());
      params.add(strOrgId);
      params.add(isReceipt ? "ARR" : "APP");
      String strDocTypeId = (String) CallStoredProcedure.getInstance().call("AD_GET_DOCTYPE",
          params, null);
      DocumentType documentType = OBDal.getInstance().get(DocumentType.class, strDocTypeId);
      boolean documentEnabled = true;
      String strDocBaseType = documentType.getDocumentCategory();
      boolean orgLegalWithAccounting = FIN_Utility.periodControlOpened(Invoice.TABLE_NAME,
          strInvoiceId, Invoice.TABLE_NAME + "_ID", "LE");

      if ((strAction.equals("PRD") || strAction.equals("PPW") || FIN_Utility
          .isAutomaticDepositWithdrawn(finAccount, finPaymentMethod, isReceipt))
          && new BigDecimal(strActualPayment).compareTo(BigDecimal.ZERO) != 0) {
        documentEnabled = paymentDocumentEnabled
            || avd.getDocumentConfirmation(conn, finAccount, finPaymentMethod, isReceipt,
                strActualPayment, false);
      } else {
        documentEnabled = paymentDocumentEnabled;
      }

      if (documentEnabled
          && !FIN_Utility.isPeriodOpen(vars.getClient(), strDocBaseType, strOrgId, strPaymentDate)
          && orgLegalWithAccounting) {
        String messag = OBMessageUtils.translateError(conn, vars, vars.getLanguage(),
            Utility.messageBD(conn, "PeriodNotAvailable", vars.getLanguage())).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", messag);
        jsonRequest.put("message", errorMessage);
        return errorMessage;
      }

      if (strPaymentDocumentNo.startsWith("<")) {
        // get DocumentNo
        strPaymentDocumentNo = Utility.getDocumentNo(conn, vars, "AddPaymentFromInvoice",
            "FIN_Payment", strDocTypeId, strDocTypeId, false, true);
      }

      FIN_Payment payment = FIN_AddPayment.savePayment(null, isReceipt,
          dao.getObject(DocumentType.class, strDocTypeId), strPaymentDocumentNo,
          dao.getObject(BusinessPartner.class, strReceivedFromId),
          dao.getObject(FIN_PaymentMethod.class, strPaymentMethodId),
          dao.getObject(FIN_FinancialAccount.class, strFinancialAccountId), strActualPayment,
          FIN_Utility.getDate(strPaymentDate), dao.getObject(Organization.class, strOrgId),
          strReferenceNo, selectedPaymentDetails, selectedPaymentDetailAmounts,
          strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"),
          dao.getObject(Currency.class, strCurrencyId), exchangeRate, convertedAmount);

      // Credit to Use Grid
      JSONArray selectedCreditLines = creditToUseGrid.getJSONArray("_selection");
      String strSelectedCreditLinesIds = null;
      if (selectedCreditLines.length() > 0) {
        strSelectedCreditLinesIds = getSelectedCreditLinesIds(selectedCreditLines);
        List<FIN_Payment> selectedCreditPayment = FIN_Utility.getOBObjectList(FIN_Payment.class,
            strSelectedCreditLinesIds);
        HashMap<String, BigDecimal> selectedCreditPaymentAmounts = getSelectedCreditLinesAndAmount(
            selectedCreditLines, selectedCreditPayment);

        BigDecimal totalUsedCreditAmt = BigDecimal.ZERO;
        for (final FIN_Payment creditPayment : selectedCreditPayment) {
          // TODO: Añadir en la descripcion del payment de credito usado en que payment se usa
          final BigDecimal usedCreditAmt = selectedCreditPaymentAmounts.get(creditPayment.getId());
          final StringBuffer description = new StringBuffer();
          if (creditPayment.getDescription() != null && !creditPayment.getDescription().equals(""))
            description.append(creditPayment.getDescription()).append("\n");
          description.append(String.format(
              Utility.messageBD(conn, "APRM_CreditUsedPayment", vars.getLanguage()),
              payment.getDocumentNo()));
          String truncateDescription = (description.length() > 255) ? description.substring(0, 251)
              .concat("...").toString() : description.toString();
          creditPayment.setDescription(truncateDescription);
          // Set Used Credit = Amount + Previous used credit introduced by the user
          creditPayment.setUsedCredit(usedCreditAmt.add(creditPayment.getUsedCredit()));
          FIN_PaymentProcess.linkCreditPayment(payment, usedCreditAmt, creditPayment);
          OBDal.getInstance().save(creditPayment);
        }
      }

      // Add GL Item lines
      if (addedGLITemsArray.length() > 0) {
        for (int i = 0; i < addedGLITemsArray.length(); i++) {
          JSONObject glItem = addedGLITemsArray.getJSONObject(i);
          BigDecimal glItemOutAmt = BigDecimal.ZERO;
          BigDecimal glItemInAmt = BigDecimal.ZERO;

          if (glItem.has("paidOut")) {
            glItemOutAmt = new BigDecimal(glItem.getString("paidOut"));
          }
          if (glItem.has("receivedIn")) {
            glItemInAmt = new BigDecimal(glItem.getString("receivedIn"));
          }

          BigDecimal glItemAmt = BigDecimal.ZERO;
          if (isReceipt) {
            glItemAmt = glItemInAmt.subtract(glItemOutAmt);
          } else {
            glItemAmt = glItemOutAmt.subtract(glItemInAmt);
          }
          String strGLItemId = null;
          if (glItem.has("gLItem")) {
            strGLItemId = glItem.getString("gLItem");
            checkID(strGLItemId);
          }

          // Accounting Dimensions
          BusinessPartner businessPartnerGLItem = null;
          if (glItem.has("businessPartner")) {
            final String strElement_BP = glItem.getString("businessPartner");
            checkID(strElement_BP);
            businessPartnerGLItem = dao.getObject(BusinessPartner.class, strElement_BP);
          }
          Product product = null;
          if (glItem.has("product")) {
            final String strElement_PR = glItem.getString("product");
            checkID(strElement_PR);
            product = dao.getObject(Product.class, strElement_PR);
          }
          Project project = null;
          if (glItem.has("project")) {
            final String strElement_PJ = glItem.getString("project");
            checkID(strElement_PJ);
            project = dao.getObject(Project.class, strElement_PJ);
          }
          ABCActivity activity = null;
          if (glItem.has("cActivityDim")) {
            final String strElement_AY = glItem.getString("cActivityDim");
            checkID(strElement_AY);
            activity = null;
          }
          Costcenter costCenter = null;
          if (glItem.has("costCenter")) {
            final String strElement_CC = glItem.getString("costCenter");
            checkID(strElement_CC);
            costCenter = dao.getObject(Costcenter.class, strElement_CC);
          }
          Campaign campaign = null;
          if (glItem.has("cCampaignDim")) {
            final String strElement_MC = glItem.getString("cCampaignDim");
            checkID(strElement_MC);
            campaign = null;
          }
          UserDimension1 user1 = null;
          if (glItem.has("stDimension")) {
            final String strElement_U1 = glItem.getString("stDimension");
            checkID(strElement_U1);
            user1 = dao.getObject(UserDimension1.class, strElement_U1);
          }
          UserDimension2 user2 = null;
          if (glItem.has("ndDimension")) {
            final String strElement_U2 = glItem.getString("ndDimension");
            checkID(strElement_U2);
            user2 = dao.getObject(UserDimension2.class, strElement_U2);
          }
          FIN_AddPayment
              .saveGLItem(payment, glItemAmt, dao.getObject(GLItem.class, strGLItemId),
                  businessPartner, product, project, campaign, activity, null, costCenter, user1,
                  user2);
        }
      }

      if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
          || strAction.equals("PPW")) {

        message = FIN_AddPayment.processPayment(vars, conn,
            (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
        String strNewPaymentMessage = Utility.parseTranslation(conn, vars, vars.getLanguage(),
            "@PaymentCreated@" + " " + payment.getDocumentNo()) + ".";
        if (!"Error".equalsIgnoreCase(message.getType()))
          message.setMessage(strNewPaymentMessage + " " + message.getMessage());
        if (strDifferenceAction.equals("refund")) {
          Boolean newPayment = !payment.getFINPaymentDetailList().isEmpty();
          FIN_Payment refundPayment = FIN_AddPayment.createRefundPayment(conn, vars, payment,
              refundAmount.negate(), exchangeRate);
          OBError auxMessage = FIN_AddPayment.processPayment(vars, conn,
              (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
          if (newPayment && !"Error".equalsIgnoreCase(auxMessage.getType())) {
            final String strNewRefundPaymentMessage = Utility.parseTranslation(conn, vars,
                vars.getLanguage(), "@APRM_RefundPayment@" + ": " + refundPayment.getDocumentNo())
                + ".";
            message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage());
            if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
              payment.setDescription(payment.getDescription() + strNewRefundPaymentMessage + "\n");
              OBDal.getInstance().save(payment);
              OBDal.getInstance().flush();
            }
          } else {
            message = auxMessage;
          }
        }
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(new DalConnectionProvider(), vars,
            vars.getLanguage(), ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  /**
   * @param allselection
   *          : Selected Rows in Order Invoice grid
   * @return
   */
  private String getSelectedRowIds(JSONArray allselection) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    try {
      for (int i = 0; i < allselection.length(); i++) {
        JSONObject selectedRow = allselection.getJSONObject(i);
        sb.append("'" + selectedRow.getString("id") + "', ");
      }
      sb.replace(sb.lastIndexOf(","), sb.lastIndexOf(",") + 2, ")");
      return sb.toString();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * @param allselection
   *          : Selected Rows in Order Invoice grid
   * @return
   */
  private HashMap<String, BigDecimal> getSelectedPaymentDetailsAndAmount(JSONArray allselection,
      String _strSelectedScheduledPaymentDetailIds) {
    String strSelectedScheduledPaymentDetailIds = _strSelectedScheduledPaymentDetailIds;
    // Remove "(" ")"
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace("(", "");
    strSelectedScheduledPaymentDetailIds = strSelectedScheduledPaymentDetailIds.replace(")", "");
    HashMap<String, BigDecimal> selectedPaymentScheduleDetailsAmounts = new HashMap<String, BigDecimal>();
    // As selected items may contain records with multiple IDs we as well need the records list as
    // amounts are related to records
    StringTokenizer records = new StringTokenizer(strSelectedScheduledPaymentDetailIds, "'");
    Set<String> recordSet = new LinkedHashSet<String>();
    while (records.hasMoreTokens()) {
      recordSet.add(records.nextToken());
    }
    for (String record : recordSet) {
      if (", ".equals(record)) {
        continue;
      }
      Set<String> psdSet = new LinkedHashSet<String>();
      StringTokenizer psds = new StringTokenizer(record, ",");
      while (psds.hasMoreTokens()) {
        psdSet.add(psds.nextToken());
      }
      BigDecimal recordAmount = getAmountOfRow(allselection, record);
      HashMap<String, BigDecimal> recordsAmounts = calculateAmounts(recordAmount, psdSet);
      selectedPaymentScheduleDetailsAmounts.putAll(recordsAmounts);
    }
    return selectedPaymentScheduleDetailsAmounts;
  }

  /**
   * @param allselection
   * @param record
   * @return
   */
  private BigDecimal getAmountOfRow(JSONArray allselection, String record) {
    try {
      for (int i = 0; i < allselection.length(); i++) {
        JSONObject selectedRow = allselection.getJSONObject(i);
        if (record.equals(selectedRow.getString("id"))) {
          return new BigDecimal(selectedRow.getString("amount"));
        }
      }
      return new BigDecimal(0);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * This method returns a HashMap with pairs of UUID of payment schedule details and amounts
   * related to those ones.
   * 
   * @param recordAmount
   *          : amount to split among the set
   * @param psdSet
   *          : set of payment schedule details where to allocate the amount
   * @return
   */
  private HashMap<String, BigDecimal> calculateAmounts(BigDecimal recordAmount, Set<String> psdSet) {
    BigDecimal remainingAmount = recordAmount;
    HashMap<String, BigDecimal> recordsAmounts = new HashMap<String, BigDecimal>();
    // PSD needs to be properly ordered to ensure negative amounts are processed first
    List<FIN_PaymentScheduleDetail> psds = getOrderedPaymentScheduleDetails(psdSet);
    BigDecimal outstandingAmount = BigDecimal.ZERO;
    for (FIN_PaymentScheduleDetail paymentScheduleDetail : psds) {
      if (paymentScheduleDetail.getPaymentDetails() != null) {
        // This schedule detail comes from an edited payment so outstanding amount needs to be
        // properly calculated
        List<FIN_PaymentScheduleDetail> outStandingPSDs = FIN_AddPayment
            .getOutstandingPSDs(paymentScheduleDetail);
        if (outStandingPSDs.size() > 0) {
          outstandingAmount = paymentScheduleDetail.getAmount().add(
              outStandingPSDs.get(0).getAmount());
        } else {
          outstandingAmount = paymentScheduleDetail.getAmount();
        }
      } else {
        outstandingAmount = paymentScheduleDetail.getAmount();
      }
      // Manage negative amounts
      if ((remainingAmount.compareTo(BigDecimal.ZERO) > 0 && remainingAmount
          .compareTo(outstandingAmount) >= 0)
          || ((remainingAmount.compareTo(BigDecimal.ZERO) == -1 && outstandingAmount
              .compareTo(BigDecimal.ZERO) == -1) && (remainingAmount.compareTo(outstandingAmount) <= 0))) {
        recordsAmounts.put(paymentScheduleDetail.getId(), outstandingAmount);
        remainingAmount = remainingAmount.subtract(outstandingAmount);
      } else {
        recordsAmounts.put(paymentScheduleDetail.getId(), remainingAmount);
        remainingAmount = BigDecimal.ZERO;
      }

    }
    return recordsAmounts;
  }

  private List<FIN_PaymentScheduleDetail> getOrderedPaymentScheduleDetails(Set<String> psdSet) {
    OBCriteria<FIN_PaymentScheduleDetail> orderedPSDs = OBDal.getInstance().createCriteria(
        FIN_PaymentScheduleDetail.class);
    orderedPSDs.add(Restrictions.in(FIN_PaymentScheduleDetail.PROPERTY_ID, psdSet));
    orderedPSDs.addOrderBy(FIN_PaymentScheduleDetail.PROPERTY_AMOUNT, true);
    return orderedPSDs.list();
  }

  private void checkID(final String id) throws ServletException {
    if (!IsIDFilter.instance.accept(id)) {
      log.error("Input: " + id + " not accepted by filter: IsIDFilter");
      throw new ServletException("Input: " + id + " is not an accepted input");
    }
  }

  /**
   * @param allselection
   *          : Selected Rows in Order Invoice grid
   * @return
   */
  private String getSelectedCreditLinesIds(JSONArray allselection) {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    try {
      for (int i = 0; i < allselection.length(); i++) {
        JSONObject selectedRow = allselection.getJSONObject(i);
        sb.append(selectedRow.getString("id") + ",");
      }
      sb.replace(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1, ")");
      return sb.toString();
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * @param allselection
   * @param _selectedCreditPayments
   * @return
   */
  private HashMap<String, BigDecimal> getSelectedCreditLinesAndAmount(JSONArray allselection,
      List<FIN_Payment> _selectedCreditPayments) {
    HashMap<String, BigDecimal> selectedCreditLinesAmounts = new HashMap<String, BigDecimal>();

    try {
      for (FIN_Payment creditPayment : _selectedCreditPayments) {
        for (int i = 0; i < allselection.length(); i++) {
          JSONObject selectedRow = allselection.getJSONObject(i);
          if (selectedRow.getString("id").equals(creditPayment.getId())) {
            selectedCreditLinesAmounts.put(creditPayment.getId(),
                new BigDecimal(selectedRow.getString("paymentAmount")));
          }
        }
      }
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      return null;
    }
    return selectedCreditLinesAmounts;
  }
}
