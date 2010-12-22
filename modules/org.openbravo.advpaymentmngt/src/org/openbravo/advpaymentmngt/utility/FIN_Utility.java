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

package org.openbravo.advpaymentmngt.utility;

import java.sql.BatchUpdateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.ElementTrl;
import org.openbravo.model.ad.ui.Message;
import org.openbravo.model.ad.ui.MessageTrl;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.utils.Replace;

public class FIN_Utility {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(Utility.class);
  private static AdvPaymentMngtDao dao;

  /**
   * Parses the string to a date using the dateFormat.java property.
   * 
   * @param strDate
   *          String containing the date
   * @return the date
   */
  public static Date getDate(String strDate) {
    if (strDate.equals(""))
      return null;
    try {
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
          "dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      return (outputFormat.parse(strDate));
    } catch (ParseException e) {
      log4j.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Parses the string to a date with time using the dateTimeFormat defined in Openbravo.properties.
   * If the string parameter does not have time include it will add the current hours, minutes and
   * seconds.
   * 
   * @param strDate
   *          String date.
   * @return the date with time.
   */
  public static Date getDateTime(String strDate) {
    String dateTime = strDate;
    Calendar cal = Calendar.getInstance();
    if (!strDate.contains(":")) {
      dateTime = strDate + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
          + ":" + cal.get(Calendar.SECOND);
    }
    if (dateTime.equals(""))
      return null;
    try {
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
          "dateTimeFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      return (outputFormat.parse(dateTime));
    } catch (ParseException e) {
      log4j.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Parses the string of comma separated id's to return a List object of the given class
   * 
   * @param <T>
   * @param t
   *          class of the OBObject the id's belong to
   * @param _strSelectedIds
   *          String containing a comma separated list of id's
   * @return a List object containing the parsed OBObjects
   */
  public static <T extends BaseOBObject> List<T> getOBObjectList(Class<T> t, String _strSelectedIds) {
    dao = new AdvPaymentMngtDao();
    String strSelectedIds = _strSelectedIds;
    final List<T> OBObjectList = new ArrayList<T>();
    // selected scheduled payments list
    if (strSelectedIds.startsWith("("))
      strSelectedIds = strSelectedIds.substring(1, strSelectedIds.length() - 1);
    if (!strSelectedIds.equals("")) {
      strSelectedIds = Replace.replace(strSelectedIds, "'", "");
      StringTokenizer st = new StringTokenizer(strSelectedIds, ",", false);
      while (st.hasMoreTokens()) {
        String strScheduledPaymentId = st.nextToken().trim();
        OBObjectList.add(dao.getObject(t, strScheduledPaymentId));
      }
    }
    return OBObjectList;
  }

  /**
   * 
   * @param _strSelectedIds
   *          Identifiers string list with the following structure: ('ID', 'ID', 'ID')
   * @return Map<K,V> using the ID as key and value <ID,ID> for each identifier.
   */
  public static Map<String, String> getMapFromStringList(String _strSelectedIds) {
    String strSelectedIds = _strSelectedIds;
    final Map<String, String> map = new HashMap<String, String>();
    if (strSelectedIds.startsWith("("))
      strSelectedIds = strSelectedIds.substring(1, strSelectedIds.length() - 1);
    if (!strSelectedIds.equals("")) {
      strSelectedIds = Replace.replace(strSelectedIds, "'", "");
      StringTokenizer st = new StringTokenizer(strSelectedIds, ",", false);
      while (st.hasMoreTokens()) {
        String strItem = st.nextToken().trim();
        map.put(strItem, strItem);
      }
    }
    return map;
  }

  /**
   * Returns a FieldProvider object containing the Scheduled Payments.
   * 
   * @param vars
   * @param selectedScheduledPayments
   *          List of FIN_PaymentSchedule that need to be selected by default
   * @param filteredScheduledPayments
   *          List of FIN_PaymentSchedule that need to unselected by default
   */
  public static FieldProvider[] getShownScheduledPayments(VariablesSecureApp vars,
      List<FIN_PaymentSchedule> selectedScheduledPayments,
      List<FIN_PaymentSchedule> filteredScheduledPayments) {
    final List<FIN_PaymentSchedule> shownScheduledPayments = new ArrayList<FIN_PaymentSchedule>();
    shownScheduledPayments.addAll(selectedScheduledPayments);
    shownScheduledPayments.addAll(filteredScheduledPayments);
    FIN_PaymentSchedule[] FIN_PaymentSchedules = new FIN_PaymentSchedule[0];
    FIN_PaymentSchedules = shownScheduledPayments.toArray(FIN_PaymentSchedules);
    // FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(FIN_PaymentSchedules);

    // FieldProvider[] data = new FieldProviderFactory[selectedScheduledPayments.size()];
    FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(shownScheduledPayments);
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty(
        "dateFormat.java");
    SimpleDateFormat dateFormater = new SimpleDateFormat(dateFormat);
    // set in administrator mode to be able to access FIN_PaymentSchedule entity
    OBContext.setAdminMode();
    try {

      for (int i = 0; i < data.length; i++) {
        FieldProviderFactory.setField(data[i], "finSelectedPaymentId", (selectedScheduledPayments
            .contains(FIN_PaymentSchedules[i])) ? FIN_PaymentSchedules[i].getId() : "");
        FieldProviderFactory.setField(data[i], "finScheduledPaymentId", FIN_PaymentSchedules[i]
            .getId());
        if (FIN_PaymentSchedules[i].getOrder() != null)
          FieldProviderFactory.setField(data[i], "orderNr", FIN_PaymentSchedules[i].getOrder()
              .getDocumentNo());
        if (FIN_PaymentSchedules[i].getInvoice() != null) {
          FieldProviderFactory.setField(data[i], "invoiceNr", FIN_PaymentSchedules[i].getInvoice()
              .getDocumentNo());
          FieldProviderFactory.setField(data[i], "invoicedAmount", FIN_PaymentSchedules[i]
              .getInvoice().getGrandTotalAmount().toString());
        }
        FieldProviderFactory.setField(data[i], "dueDate", dateFormater.format(
            FIN_PaymentSchedules[i].getDueDate()).toString());
        FieldProviderFactory.setField(data[i], "expectedAmount", FIN_PaymentSchedules[i]
            .getAmount().toString());
        String strPaymentAmt = vars.getStringParameter("inpPaymentAmount"
            + FIN_PaymentSchedules[i].getId(), "");
        FieldProviderFactory.setField(data[i], "paymentAmount", strPaymentAmt);
        FieldProviderFactory.setField(data[i], "rownum", String.valueOf(i));

      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  /**
   * Creates a comma separated string with the Id's of the OBObjects included in the List.
   * 
   * @param <T>
   * @param obObjectList
   *          List of OBObjects
   * @return Comma separated string of Id's
   */
  public static <T extends BaseOBObject> String getInStrList(List<T> obObjectList) {
    StringBuilder strInList = new StringBuilder();
    for (T obObject : obObjectList) {
      if (strInList.length() == 0)
        strInList.append("'" + obObject.getId() + "'");
      else
        strInList.append(", '" + obObject.getId() + "'");
    }
    return strInList.toString();
  }

  /**
   * Returns the cause of a trigger exception (BatchupdateException).
   * 
   * Hibernate and JDBC will wrap the exception thrown by the trigger in another exception (the
   * java.sql.BatchUpdateException) and this exception is sometimes wrapped again. Also the
   * java.sql.BatchUpdateException stores the underlying trigger exception in the nextException and
   * not in the cause property.
   * 
   * @param t
   *          exception.
   * @return the underlying trigger message.
   */
  public static String getExceptionMessage(Throwable t) {
    if (t.getCause() instanceof BatchUpdateException
        && ((BatchUpdateException) t.getCause()).getNextException() != null) {
      final BatchUpdateException bue = (BatchUpdateException) t.getCause();
      return bue.getNextException().getMessage();
    }
    return t.getMessage();
  }

  /**
   * Returns the DocumentType defined for the Organization (or parent organization tree) and
   * document category.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @return the Document Type
   */
  public static DocumentType getDocumentType(Organization org, String docCategory) {
    DocumentType outDocType = null;
    OBCriteria<DocumentType> obcDoc = OBDal.getInstance().createCriteria(DocumentType.class);

    obcDoc.add(Expression.in("organization.id", OBContext.getOBContext()
        .getOrganizationStructureProvider().getParentTree(org.getId(), true)));
    obcDoc.add(Expression.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    obcDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    obcDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    List<DocumentType> docTypeList = obcDoc.list();
    if (docTypeList != null && docTypeList.size() > 0) {
      outDocType = docTypeList.get(0);
    }
    return outDocType;
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param docType
   *          Document type of the document
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Null if no sequence is found.
   */
  public static String getDocumentNo(DocumentType docType, String tableName) {
    String nextDocNumber = "";
    if (docType != null) {
      Sequence seq = docType.getDocumentSequence();
      if (seq == null && tableName != null) {
        OBCriteria<Sequence> obcSeq = OBDal.getInstance().createCriteria(Sequence.class);
        obcSeq.add(Expression.eq(Sequence.PROPERTY_NAME, tableName));
        if (obcSeq != null && obcSeq.list().size() > 0) {
          seq = obcSeq.list().get(0);
        }
      }
      if (seq != null) {
        if (seq.getPrefix() != null)
          nextDocNumber = seq.getPrefix();
        nextDocNumber += seq.getNextAssignedNumber().toString();
        if (seq.getSuffix() != null)
          nextDocNumber += seq.getSuffix();
        seq.setNextAssignedNumber(seq.getNextAssignedNumber() + seq.getIncrementBy());
        OBDal.getInstance().save(seq);
        OBDal.getInstance().flush();
      }
    }

    return nextDocNumber;
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @param tableName
   *          the name of the table from which the sequence will be taken if the Document Type does
   *          not have any sequence associated.
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Null if no sequence is found.
   */
  public static String getDocumentNo(Organization org, String docCategory, String tableName) {
    DocumentType outDocType = getDocumentType(org, docCategory);
    return getDocumentNo(outDocType, tableName);
  }

  /**
   * Gets the available Payment Methods and returns in a String the html code containing all the
   * Payment Methods in the natural tree of the given organization filtered by the Financial
   * Account.
   * 
   * @param strPaymentMethodId
   *          the Payment Method id that will be selected by default in case it is present in the
   *          list.
   * @param strFinancialAccountId
   *          optional Financial Account id to filter the Payment Methods.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param isMandatory
   *          boolean parameter to add an extra blank option if the drop-down is optional.
   * @param excludePaymentMethodWithoutAccount
   *          if the strPaymentMethodId is empty or null then depending on this parameter the list
   *          will include payment methods with no Financial Accounts associated or only show the
   *          Payment Methods that belongs to at least on Financial Account
   * @return a String with the html code with the options to fill the drop-down of Payment Methods.
   */
  public static String getPaymentMethodList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory,
      boolean excludePaymentMethodWithoutAccount) {
    dao = new AdvPaymentMngtDao();
    List<FIN_PaymentMethod> paymentMethods = dao.getFilteredPaymentMethods(strFinancialAccountId,
        strOrgId, excludePaymentMethodWithoutAccount);
    String options = getOptionsList(paymentMethods, strPaymentMethodId, isMandatory);
    return options;
  }

  /**
   * Gets the available Financial Accounts and returns in a String the html code containing all the
   * Financial Accounts in the natural tree of the given organization filtered by the Payment
   * Method.
   * 
   * @param strPaymentMethodId
   *          optional Payment Method id to filter the Financial Accounts.
   * @param strFinancialAccountId
   *          the Financial Account id that will be selected by default in case it is present in the
   *          list.
   * @param strOrgId
   *          the Organization id the record belongs to.
   * @param strCurrencyId
   *          optional Currency id to filter the Financial Accounts.
   * @return a String with the html code with the options to fill the drop-down of Financial
   *         Accounts.
   */
  public static String getFinancialAccountList(String strPaymentMethodId,
      String strFinancialAccountId, String strOrgId, boolean isMandatory, String strCurrencyId) {
    List<FIN_FinancialAccount> financialAccounts = dao.getFilteredFinancialAccounts(
        strPaymentMethodId, strOrgId, strCurrencyId);
    String options = getOptionsList(financialAccounts, strFinancialAccountId, isMandatory);
    return options;
  }

  /**
   * Returns a String containing the html code with the options based on the given List of
   * BaseOBObjects
   * 
   * @param <T>
   *          Class that extends BaseOBObject.
   * @param obObjectList
   *          List containing the values to be included in the options.
   * @param selectedValue
   *          value to set as selected by default.
   * @param isMandatory
   *          boolean to add a blank option in the options list.
   * @return a String containing the html code with the options. *
   */
  public static <T extends BaseOBObject> String getOptionsList(List<T> obObjectList,
      String selectedValue, boolean isMandatory) {
    StringBuilder strOptions = new StringBuilder();
    if (!isMandatory)
      strOptions.append("<option value=\"\"></option>");

    for (T obObject : obObjectList) {
      strOptions.append("<option value=\"").append(obObject.getId()).append("\"");
      if (obObject.getId().equals(selectedValue))
        strOptions.append(" selected=\"selected\"");
      strOptions.append(">");
      strOptions.append(escape(obObject.getIdentifier()));
      strOptions.append("</option>");
    }
    return strOptions.toString();
  }

  /**
   * Method to replace special characters to print properly in an html. Changes are: ">" to "&gt"
   * and "<" to "&lt"
   * 
   * @param toEscape
   *          String to be replaced.
   * @return the given String with the special characters replaced.
   */
  private static String escape(String toEscape) {
    String result = toEscape.replaceAll(">", "&gt;");
    result = result.replaceAll("<", "&lt;");
    return result;
  }

  /**
   * Method used to calculate the Day still due for the payment.
   * 
   * @param date
   *          . Due date of the payment.
   * @return dayStillDue. Calculated Day Still due.
   */
  public static Long getDaysToDue(Date date) {
    final Date now = DateUtils.truncate(new Date(), Calendar.DATE);
    final TimeZone tz = TimeZone.getDefault();
    final long nowDstOffset = (tz.inDaylightTime(now)) ? tz.getDSTSavings() : 0L;
    final long dateDstOffset = (tz.inDaylightTime(date)) ? tz.getDSTSavings() : 0L;
    return (date.getTime() + dateDstOffset - now.getTime() - nowDstOffset)
        / DateUtils.MILLIS_PER_DAY;
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_FinancialAccount account,
      FIN_PaymentMethod paymentMethod, boolean isReceipt) {
    FinAccPaymentMethod financialAccountPaymentMethod = new AdvPaymentMngtDao()
        .getFinancialAccountPaymentMethod(account, paymentMethod);
    if (financialAccountPaymentMethod == null)
      return false;
    return isReceipt ? financialAccountPaymentMethod.isAutomaticDeposit()
        : financialAccountPaymentMethod.isAutomaticWithdrawn();
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_Payment payment) {
    return isAutomaticDepositWithdrawn(payment.getAccount(), payment.getPaymentMethod(), payment
        .isReceipt());
  }

  public static boolean isAutomaticDepositWithdrawn(FIN_PaymentProposal paymentProposal) {
    return isAutomaticDepositWithdrawn(paymentProposal.getAccount(), paymentProposal
        .getPaymentMethod(), paymentProposal.isReceipt());
  }

  /**
   * Translate the given code into some message from the application dictionary. It searches first
   * in AD_Message table and if there are not matchings then in AD_Element table.
   * 
   * @param strCode
   *          String with the search key to search.
   * @param strLanguage
   *          String with the translation language.
   * @return String with the translated message.
   */
  public static String messageBD(String strCode) {
    String strMessage = "";

    OBContext.setAdminMode(true);
    try {
      Language language = OBContext.getOBContext().getLanguage();
      if ("en_US".equals(language.getLanguage())) {
        OBCriteria<Message> obc = OBDal.getInstance().createCriteria(Message.class);
        obc.add(Expression.eq(Message.PROPERTY_SEARCHKEY, strCode));
        strMessage = (obc.list() != null && obc.list().size() > 0) ? obc.list().get(0)
            .getMessageText() : null;

      } else {
        OBCriteria<MessageTrl> obcTrl = OBDal.getInstance().createCriteria(MessageTrl.class);
        obcTrl.add(Expression.eq(MessageTrl.PROPERTY_LANGUAGE, language));
        obcTrl.createAlias(MessageTrl.PROPERTY_MESSAGE, "msg");
        obcTrl.add(Restrictions.eq("msg.searchKey", strCode));
        strMessage = (obcTrl.list() != null && obcTrl.list().size() > 0) ? obcTrl.list().get(0)
            .getMessageText() : null;
      }

      if (strMessage == null || strMessage.equals("")) {
        if ("en_US".equals(language.getLanguage())) {
          OBCriteria<Element> obcCol = OBDal.getInstance().createCriteria(Element.class);
          obcCol.add(Expression.eq(Element.PROPERTY_DBCOLUMNNAME, strCode).ignoreCase());
          strMessage = (obcCol.list() != null && obcCol.list().size() > 0) ? obcCol.list().get(0)
              .getName() : null;

        } else {
          OBCriteria<ElementTrl> obcTrl = OBDal.getInstance().createCriteria(ElementTrl.class);
          obcTrl.add(Expression.eq(ElementTrl.PROPERTY_LANGUAGE, language));
          obcTrl.createAlias(ElementTrl.PROPERTY_APPLICATIONELEMENT, "ele");
          obcTrl.add(Restrictions.eq("ele.dBColumnName", strCode).ignoreCase());
          strMessage = (obcTrl.list() != null && obcTrl.list().size() > 0) ? obcTrl.list().get(0)
              .getName() : null;

        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    if (strMessage == null || strMessage.equals(""))
      strMessage = strCode;
    return Replace.replace(Replace.replace(strMessage, "\n", "\\n"), "\"", "&quot;");

  }

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param values
   *          Value. Property, value and operator.
   * @return All the records that satisfy the conditions.
   */
  public static <T extends BaseOBObject> List<T> getAllInstances(Class<T> clazz, Value... values) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Expression.ne(Client.PROPERTY_ID, "0"));
    for (Value value : values) {
      if (value.getValue() == null && "==".equals(value.getOperator())) {
        obc.add(Expression.isNull(value.getField()));
      } else if (value.getValue() == null && "!=".equals(value.getOperator())) {
        obc.add(Expression.isNotNull(value.getField()));
      } else if ("==".equals(value.getOperator())) {
        obc.add(Expression.eq(value.getField(), value.getValue()));
      } else if ("!=".equals(value.getOperator())) {
        obc.add(Expression.ne(value.getField(), value.getValue()));
      } else if ("<".equals(value.getOperator())) {
        obc.add(Expression.lt(value.getField(), value.getValue()));
      } else if (">".equals(value.getOperator())) {
        obc.add(Expression.gt(value.getField(), value.getValue()));
      } else if ("<=".equals(value.getOperator())) {
        obc.add(Expression.le(value.getField(), value.getValue()));
      } else if (">=".equals(value.getOperator())) {
        obc.add(Expression.ge(value.getField(), value.getValue()));
      } else {
        obc.add(Expression.eq(value.getField(), value.getValue()));
      }
    }
    return obc.list();
  }

  /**
   * Generic OBCriteria.
   * 
   * @param clazz
   *          Class (entity).
   * @param values
   *          Value. Property, value and operator.
   * @return One record that satisfies the conditions.
   */
  public static <T extends BaseOBObject> T getOneInstance(Class<T> clazz, Value... values) {
    OBCriteria<T> obc = OBDal.getInstance().createCriteria(clazz);
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Expression.ne(Client.PROPERTY_ID, "0"));
    for (Value value : values) {
      if (value.getValue() == null && "==".equals(value.getOperator())) {
        obc.add(Expression.isNull(value.getField()));
      } else if (value.getValue() == null && "!=".equals(value.getOperator())) {
        obc.add(Expression.isNotNull(value.getField()));
      } else if ("==".equals(value.getOperator())) {
        obc.add(Expression.eq(value.getField(), value.getValue()));
      } else if ("!=".equals(value.getOperator())) {
        obc.add(Expression.ne(value.getField(), value.getValue()));
      } else if ("<".equals(value.getOperator())) {
        obc.add(Expression.lt(value.getField(), value.getValue()));
      } else if (">".equals(value.getOperator())) {
        obc.add(Expression.gt(value.getField(), value.getValue()));
      } else if ("<=".equals(value.getOperator())) {
        obc.add(Expression.le(value.getField(), value.getValue()));
      } else if (">=".equals(value.getOperator())) {
        obc.add(Expression.ge(value.getField(), value.getValue()));
      } else {
        obc.add(Expression.eq(value.getField(), value.getValue()));
      }
    }

    final List<T> listt = obc.list();
    if (listt != null && listt.size() > 0) {
      return listt.get(0);
    } else {
      return null;
    }

  }

}
