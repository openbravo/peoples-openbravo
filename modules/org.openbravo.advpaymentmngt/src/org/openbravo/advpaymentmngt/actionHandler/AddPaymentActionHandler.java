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

package org.openbravo.advpaymentmngt.actionHandler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.process.FIN_PaymentProcess;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.UserDimension1;
import org.openbravo.model.financialmgmt.accounting.UserDimension2;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.marketing.Campaign;
import org.openbravo.model.materialmgmt.cost.ABCActivity;
import org.openbravo.model.project.Project;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.db.DbUtility;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddPaymentActionHandler extends BaseProcessActionHandler {
  private static Logger log = LoggerFactory.getLogger(AddPaymentActionHandler.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      // Get Params
      jsonRequest = new JSONObject(content);
      final String strOrgId = jsonRequest.getString("inpadOrgId");
      Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
      // String strTabId = jsonRequest.getString("inpTabId");
      final String strIssotrx = jsonRequest.getString("inpissotrx");
      boolean isReceipt = "Y".equals(strIssotrx);
      JSONObject jsonparams = jsonRequest.getJSONObject("_params");

      JSONObject orderInvoiceGrid = jsonparams.getJSONObject("order_invoice");
      JSONObject creditToUseGrid = jsonparams.getJSONObject("credit_to_use");
      JSONObject gLItemsGrid = jsonparams.getJSONObject("glitem");

      String strAction = (isReceipt ? "PRP" : "PPP");

      final String strCurrencyId = jsonparams.getString("c_currency_id");
      Currency currency = OBDal.getInstance().get(Currency.class, strCurrencyId);
      final String strBPartnerID = jsonparams.getString("received_from");
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class,
          strBPartnerID);
      String strActualPayment = jsonparams.getString("actual_payment");

      // Format Date
      String strPaymentDate = jsonparams.getString("payment_date");
      Date paymentDate = JsonUtils.createDateFormat().parse(strPaymentDate);
      String formattedDate = OBDateUtils.formatDate(paymentDate);

      // String formattedDate = new SimpleDateFormat("dd-MM-yyyy",
      // Locale.getDefault()).format(date);
      strPaymentDate = formattedDate;

      // TODO
      String strDifferenceAction = "";
      BigDecimal refundAmount = BigDecimal.ZERO;

      // Orders Invoices Grid
      JSONArray allselection = orderInvoiceGrid.getJSONArray("_selection");
      String strSelectedScheduledPaymentDetailIds = null;
      if (allselection.length() > 0) {
        strSelectedScheduledPaymentDetailIds = getSelectedRowIds(allselection);
      }

      BigDecimal exchangeRate = BigDecimal.ZERO;
      BigDecimal convertedAmount = BigDecimal.ZERO;
      String strCurrencyToId = jsonparams.getString("c_currency_to_id");
      if (jsonparams.get("conversion_rate") != JSONObject.NULL) {
        exchangeRate = new BigDecimal(jsonparams.getString("conversion_rate"));
      }
      if (jsonparams.get("converted_amount") != JSONObject.NULL) {
        convertedAmount = new BigDecimal(jsonparams.getString("converted_amount"));
      }

      List<FIN_PaymentScheduleDetail> selectedPaymentDetails = FIN_Utility.getOBObjectList(
          FIN_PaymentScheduleDetail.class, strSelectedScheduledPaymentDetailIds);
      HashMap<String, BigDecimal> selectedPaymentDetailAmounts = getSelectedPaymentDetailsAndAmount(
          allselection, strSelectedScheduledPaymentDetailIds);

      FIN_Payment payment = null;
      if (jsonparams.get("fin_payment_id") != JSONObject.NULL) {
        // Payment is already created. Load it.
        final String strFinPaymentID = jsonRequest.getString("finPaymentId");
        payment = OBDal.getInstance().get(FIN_Payment.class, strFinPaymentID);
      } else {
        try {
          payment = createNewPayment(jsonparams, isReceipt, org, businessPartner, paymentDate,
              currency, exchangeRate, convertedAmount, strActualPayment);
        } catch (OBException e) {
          JSONObject errorMessage = new JSONObject();
          errorMessage.put("severity", "error");
          errorMessage.put("text", e.getMessage());
          jsonRequest.put("message", errorMessage);
          return errorMessage;
        }
      }

      payment = FIN_AddPayment.savePayment(payment, isReceipt, null, null, null, null, null,
          strActualPayment, null, null, null, selectedPaymentDetails, selectedPaymentDetailAmounts,
          strDifferenceAction.equals("writeoff"), strDifferenceAction.equals("refund"), currency,
          exchangeRate, convertedAmount);

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
          description.append(String.format(OBMessageUtils.messageBD("APRM_CreditUsedPayment"),
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
      JSONArray addedGLITemsArray = gLItemsGrid.getJSONArray("_selection");
      addGLItems(payment, addedGLITemsArray);

      if (strAction.equals("PRP") || strAction.equals("PPP") || strAction.equals("PRD")
          || strAction.equals("PPW")) {

        OBError message = processPayment(payment, strAction, strDifferenceAction, refundAmount,
            exchangeRate);
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", message.getType().toLowerCase());
        errorMessage.put("title", message.getTitle());
        errorMessage.put("text", message.getMessage());
        jsonRequest.put("message", errorMessage);
      }

    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
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

  private FIN_Payment createNewPayment(JSONObject jsonparams, boolean isReceipt, Organization org,
      BusinessPartner bPartner, Date paymentDate, Currency currency, BigDecimal conversionRate,
      BigDecimal convertedAmt, String strActualPayment) throws OBException, JSONException {

    String strPaymentDocumentNo = jsonparams.getString("payment_documentno");
    String strReferenceNo = jsonparams.getString("reference_no");
    String strFinancialAccountId = jsonparams.getString("fin_financial_account_id");
    FIN_FinancialAccount finAccount = OBDal.getInstance().get(FIN_FinancialAccount.class,
        strFinancialAccountId);
    String strPaymentMethodId = jsonparams.getString("fin_paymentmethod_id");
    FIN_PaymentMethod paymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strPaymentMethodId);

    DocumentType documentType = FIN_Utility.getDocumentType(org, isReceipt ? "ARR" : "APP");
    boolean documentEnabled = true;
    String strDocBaseType = documentType.getDocumentCategory();

    String strAction = (isReceipt ? "PRP" : "PPP");

    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        OBContext.getOBContext().getCurrentClient().getId());
    boolean orgLegalWithAccounting = osp.getLegalEntityOrBusinessUnit(org).getOrganizationType()
        .isLegalEntityWithAccounting();

    boolean paymentDocumentEnabled = getDocumentConfirmation(finAccount, paymentMethod, isReceipt,
        strActualPayment, true);

    if ((strAction.equals("PRD") || strAction.equals("PPW") || FIN_Utility
        .isAutomaticDepositWithdrawn(finAccount, paymentMethod, isReceipt))
        && new BigDecimal(strActualPayment).compareTo(BigDecimal.ZERO) != 0) {
      documentEnabled = paymentDocumentEnabled
          || getDocumentConfirmation(finAccount, paymentMethod, isReceipt, strActualPayment, false);
    } else {
      documentEnabled = paymentDocumentEnabled;
    }

    if (documentEnabled
        && !FIN_Utility.isPeriodOpen(OBContext.getOBContext().getCurrentClient().getId(),
            strDocBaseType, org.getId(), OBDateUtils.formatDate(paymentDate))
        && orgLegalWithAccounting) {
      String messag = OBMessageUtils.translateError(OBMessageUtils.messageBD("PeriodNotAvailable"))
          .getMessage();
      throw new OBException(messag);
    }

    String strPaymentAmount = "0";
    if (strPaymentDocumentNo.startsWith("<")) {
      // get DocumentNo
      strPaymentDocumentNo = FIN_Utility.getDocumentNo(documentType, "FIN_Payment");
    }

    FIN_Payment payment = (new AdvPaymentMngtDao()).getNewPayment(isReceipt, org, documentType,
        strPaymentDocumentNo, bPartner, paymentMethod, finAccount, strPaymentAmount, paymentDate,
        strReferenceNo, currency, conversionRate, convertedAmt);
    return payment;
  }

  private void addGLItems(FIN_Payment payment, JSONArray addedGLITemsArray) throws JSONException,
      ServletException {
    boolean isReceipt = payment.isReceipt();
    for (int i = 0; i < addedGLITemsArray.length(); i++) {
      JSONObject glItem = addedGLITemsArray.getJSONObject(i);
      BigDecimal glItemOutAmt = BigDecimal.ZERO;
      BigDecimal glItemInAmt = BigDecimal.ZERO;

      if (glItem.has("paidOut") && glItem.get("paidOut") != JSONObject.NULL) {
        glItemOutAmt = new BigDecimal(glItem.getString("paidOut"));
      }
      if (glItem.has("receivedIn") && glItem.get("receivedIn") != JSONObject.NULL) {
        glItemInAmt = new BigDecimal(glItem.getString("receivedIn"));
      }

      BigDecimal glItemAmt = BigDecimal.ZERO;
      if (isReceipt) {
        glItemAmt = glItemInAmt.subtract(glItemOutAmt);
      } else {
        glItemAmt = glItemOutAmt.subtract(glItemInAmt);
      }
      String strGLItemId = null;
      if (glItem.has("gLItem") && glItem.get("gLItem") != JSONObject.NULL) {
        strGLItemId = glItem.getString("gLItem");
        checkID(strGLItemId);
      }

      // Accounting Dimensions
      BusinessPartner businessPartnerGLItem = null;
      if (glItem.has("businessPartner") && glItem.get("businessPartner") != JSONObject.NULL) {
        final String strElement_BP = glItem.getString("businessPartner");
        checkID(strElement_BP);
        businessPartnerGLItem = OBDal.getInstance().get(BusinessPartner.class, strElement_BP);
      }
      Product product = null;
      if (glItem.has("product") && glItem.get("product") != JSONObject.NULL) {
        final String strElement_PR = glItem.getString("product");
        checkID(strElement_PR);
        product = OBDal.getInstance().get(Product.class, strElement_PR);
      }
      Project project = null;
      if (glItem.has("project") && glItem.get("project") != JSONObject.NULL) {
        final String strElement_PJ = glItem.getString("project");
        checkID(strElement_PJ);
        project = OBDal.getInstance().get(Project.class, strElement_PJ);
      }
      ABCActivity activity = null;
      if (glItem.has("cActivityDim") && glItem.get("cActivityDim") != JSONObject.NULL) {
        final String strElement_AY = glItem.getString("cActivityDim");
        checkID(strElement_AY);
        activity = null;
      }
      Costcenter costCenter = null;
      if (glItem.has("costCenter") && glItem.get("costCenter") != JSONObject.NULL) {
        final String strElement_CC = glItem.getString("costCenter");
        checkID(strElement_CC);
        costCenter = OBDal.getInstance().get(Costcenter.class, strElement_CC);
      }
      Campaign campaign = null;
      if (glItem.has("cCampaignDim") && glItem.get("cCampaignDim") != JSONObject.NULL) {
        final String strElement_MC = glItem.getString("cCampaignDim");
        checkID(strElement_MC);
        campaign = null;
      }
      UserDimension1 user1 = null;
      if (glItem.has("stDimension") && glItem.get("stDimension") != JSONObject.NULL) {
        final String strElement_U1 = glItem.getString("stDimension");
        checkID(strElement_U1);
        user1 = OBDal.getInstance().get(UserDimension1.class, strElement_U1);
      }
      UserDimension2 user2 = null;
      if (glItem.has("ndDimension") && glItem.get("ndDimension") != JSONObject.NULL) {
        final String strElement_U2 = glItem.getString("ndDimension");
        checkID(strElement_U2);
        user2 = OBDal.getInstance().get(UserDimension2.class, strElement_U2);
      }
      FIN_AddPayment.saveGLItem(payment, glItemAmt,
          OBDal.getInstance().get(GLItem.class, strGLItemId), businessPartnerGLItem, product,
          project, campaign, activity, null, costCenter, user1, user2);
    }
  }

  private OBError processPayment(FIN_Payment payment, String strAction, String strDifferenceAction,
      BigDecimal refundAmount, BigDecimal exchangeRate) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider(true);
    VariablesSecureApp vars = RequestContext.get().getVariablesSecureApp();
    OBError message = FIN_AddPayment.processPayment(vars, conn,
        (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", payment);
    String strNewPaymentMessage = OBMessageUtils.parseTranslation("@PaymentCreated@" + " "
        + payment.getDocumentNo())
        + ".";
    if (!"Error".equalsIgnoreCase(message.getType())) {
      message.setMessage(strNewPaymentMessage + " " + message.getMessage());
      message.setType(message.getType().toLowerCase());
    }
    if (!strDifferenceAction.equals("refund")) {
      return message;
    }
    boolean newPayment = !payment.getFINPaymentDetailList().isEmpty();
    FIN_Payment refundPayment = FIN_AddPayment.createRefundPayment(conn, vars, payment,
        refundAmount.negate(), exchangeRate);
    OBError auxMessage = FIN_AddPayment.processPayment(vars, conn,
        (strAction.equals("PRP") || strAction.equals("PPP")) ? "P" : "D", refundPayment);
    if (newPayment && !"Error".equalsIgnoreCase(auxMessage.getType())) {
      final String strNewRefundPaymentMessage = OBMessageUtils
          .parseTranslation("@APRM_RefundPayment@" + ": " + refundPayment.getDocumentNo()) + ".";
      message.setMessage(strNewRefundPaymentMessage + " " + message.getMessage());
      if (payment.getGeneratedCredit().compareTo(BigDecimal.ZERO) != 0) {
        payment.setDescription(payment.getDescription() + strNewRefundPaymentMessage + "\n");
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
      }
    } else {
      message = auxMessage;
    }

    return message;
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
      return BigDecimal.ZERO;
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
   * @throws JSONException
   */
  private HashMap<String, BigDecimal> getSelectedCreditLinesAndAmount(JSONArray allselection,
      List<FIN_Payment> _selectedCreditPayments) throws JSONException {
    HashMap<String, BigDecimal> selectedCreditLinesAmounts = new HashMap<String, BigDecimal>();

    for (FIN_Payment creditPayment : _selectedCreditPayments) {
      for (int i = 0; i < allselection.length(); i++) {
        JSONObject selectedRow = allselection.getJSONObject(i);
        if (selectedRow.getString("id").equals(creditPayment.getId())) {
          selectedCreditLinesAmounts.put(creditPayment.getId(),
              new BigDecimal(selectedRow.getString("paymentAmount")));
        }
      }
    }
    return selectedCreditLinesAmounts;
  }

  private boolean getDocumentConfirmation(FIN_FinancialAccount finAccount,
      FIN_PaymentMethod finPaymentMethod, boolean isReceipt, String strPaymentAmount,
      boolean isPayment) {
    // Checks if this step is configured to generate accounting for the selected financial account
    boolean confirmation = false;
    OBContext.setAdminMode();
    try {
      OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance().createCriteria(
          FinAccPaymentMethod.class);
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAccount));
      obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, finPaymentMethod));
      obCriteria.setFilterOnReadableClients(false);
      obCriteria.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = obCriteria.list();
      List<FIN_FinancialAccountAccounting> accounts = finAccount.getFINFinancialAccountAcctList();
      String uponUse = "";
      if (isPayment) {
        if (isReceipt) {
          uponUse = lines.get(0).getUponReceiptUse();
        } else {
          uponUse = lines.get(0).getUponPaymentUse();
        }
      } else {
        if (isReceipt) {
          uponUse = lines.get(0).getUponDepositUse();
        } else {
          uponUse = lines.get(0).getUponWithdrawalUse();
        }
      }
      for (FIN_FinancialAccountAccounting account : accounts) {
        if (confirmation) {
          return confirmation;
        }
        if (isReceipt) {
          if (("INT").equals(uponUse) && account.getInTransitPaymentAccountIN() != null) {
            confirmation = true;
          } else if (("DEP").equals(uponUse) && account.getDepositAccount() != null) {
            confirmation = true;
          } else if (("CLE").equals(uponUse) && account.getClearedPaymentAccount() != null) {
            confirmation = true;
          }
        } else {
          if (("INT").equals(uponUse) && account.getFINOutIntransitAcct() != null) {
            confirmation = true;
          } else if (("WIT").equals(uponUse) && account.getWithdrawalAccount() != null) {
            confirmation = true;
          } else if (("CLE").equals(uponUse) && account.getClearedPaymentAccountOUT() != null) {
            confirmation = true;
          }
        }
        // For payments with Amount ZERO always create an entry as no transaction will be created
        if (isPayment) {
          BigDecimal amount = new BigDecimal(strPaymentAmount);
          if (amount.compareTo(BigDecimal.ZERO) == 0) {
            confirmation = true;
          }
        }
      }
    } catch (Exception e) {
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    return confirmation;
  }

}
