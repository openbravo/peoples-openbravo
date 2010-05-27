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
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.openbravo.base.MultipartRequest;
import org.openbravo.base.VariablesBase;
import org.openbravo.data.FieldProvider;

/**
 * @author David Alsasua
 * 
 *         Chart Of Accounts (COA) Data class
 */
public class COAData extends MultipartRequest implements FieldProvider {
  static Logger log4j = Logger.getLogger(COAData.class);
  public String accountValue = "";
  public String accountName = "";
  public String accountDescription = "";
  public String accountType = "";
  public String accountSign = "";
  public String accountDocument = "";
  public String accountSummary = "";
  public String defaultAccount = "";
  public String accountParent = "";
  public String elementLevel = "";
  public String operands = "";
  public String balanceSheet = "";
  public String balanceSheetName = "";
  public String uS1120BalanceSheet = "";
  public String uS1120BalanceSheetName = "";
  public String profitAndLoss = "";
  public String profitAndLossName = "";
  public String uS1120IncomeStatement = "";
  public String uS1120IncomeStatementName = "";
  public String cashFlow = "";
  public String cashFlowName = "";
  public String cElementValueId = "";

  public COAData() {
  }

  public COAData(VariablesBase _vars, String _filename, boolean _firstLineHeads, String _format)
      throws IOException {
    super(_vars, _filename, _firstLineHeads, _format, null);
  }

  public COAData(VariablesBase _vars, InputStream _in, boolean _firstLineHeads, String _format)
      throws IOException {
    super(_vars, _in, _firstLineHeads, _format, null);
  }

  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("ACCOUNT_VALUE") || fieldName.equals("accountValue"))
      return accountValue;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_NAME") || fieldName.equals("accountName"))
      return accountName;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_DESCRIPTION")
        || fieldName.equals("accountDescription"))
      return accountDescription;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_TYPE") || fieldName.equals("accountType"))
      return accountType;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_SIGN") || fieldName.equals("accountSign"))
      return accountSign;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_DOCUMENT") || fieldName.equals("accountDocument"))
      return accountDocument;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_SUMMARY") || fieldName.equals("accountSummary"))
      return accountSummary;
    else if (fieldName.equalsIgnoreCase("DEFAULT_ACCOUNT") || fieldName.equals("defaultAccount"))
      return defaultAccount;
    else if (fieldName.equalsIgnoreCase("ACCOUNT_PARENT") || fieldName.equals("accountParent"))
      return accountParent;
    else if (fieldName.equalsIgnoreCase("ELEMENT_LEVEL") || fieldName.equals("elementLevel"))
      return elementLevel;
    else if (fieldName.equalsIgnoreCase("OPERANDS") || fieldName.equals("operands"))
      return operands.trim();
    else if (fieldName.equalsIgnoreCase("BALANCE_SHEET") || fieldName.equals("balanceSheet"))
      return balanceSheet;
    else if (fieldName.equalsIgnoreCase("BALANCE_SHEET_NAME")
        || fieldName.equals("balanceSheetName"))
      return balanceSheetName;
    else if (fieldName.equalsIgnoreCase("US_1120_BALANCE_SHEET")
        || fieldName.equals("uS1120BalanceSheet"))
      return uS1120BalanceSheet;
    else if (fieldName.equalsIgnoreCase("US_1120_BALANCE_SHEET_NAME")
        || fieldName.equals("uS1120BalanceSheetName"))
      return uS1120BalanceSheetName;
    else if (fieldName.equalsIgnoreCase("PROFIT_AND_LOSS") || fieldName.equals("profitAndLoss"))
      return profitAndLoss;
    else if (fieldName.equalsIgnoreCase("PROFIT_AND_LOSS_NAME")
        || fieldName.equals("profitAndLossName"))
      return profitAndLossName;
    else if (fieldName.equalsIgnoreCase("US_1120_INCOME_STATEMENT")
        || fieldName.equals("uS1120IncomeStatement"))
      return uS1120IncomeStatement;
    else if (fieldName.equalsIgnoreCase("US_1120_INCOME_STATEMENT_NAME")
        || fieldName.equals("uS1120IncomeStatementName"))
      return uS1120IncomeStatementName;
    else if (fieldName.equalsIgnoreCase("CASH_FLOW") || fieldName.equals("cashFlow"))
      return cashFlow;
    else if (fieldName.equalsIgnoreCase("CASH_FLOW_NAME") || fieldName.equals("cashFlowName"))
      return cashFlowName;
    else if (fieldName.equalsIgnoreCase("C_ELEMENT_VALUE_ID")
        || fieldName.equalsIgnoreCase("CELEMENTVALUEID"))
      return cElementValueId;
    else {
      if (log4j.isDebugEnabled())
        log4j.debug("COAData - getField - Field does not exist: " + fieldName);
      return null;
    }
  }

  public FieldProvider lineFixedSize(String linea) {
    return null;
  }

  public FieldProvider lineSeparatorFormated(String line) {
    if (line.length() < 1)
      return null;
    COAData coaData = new COAData();
    int next = 0;
    int previous = 0;
    String text = "";
    for (int i = 0; i < 21; i++) {
      if (next >= line.length())
        break;
      if ((previous + 1) < line.length() && line.substring(previous, previous + 1).equals("\"")) {
        int aux = line.indexOf("\"", previous + 1);
        if (aux != -1)
          next = aux;
      }
      next = line.indexOf(",", next + 1);
      if (next == -1)
        next = line.length();
      text = line.substring(previous, next);
      if (text.length() > 0) {
        if (text.charAt(0) == '"')
          text = text.substring(1);
        if (text.charAt(text.length() - 1) == '"')
          text = text.substring(0, text.length() - 1);
      }
      if (log4j.isDebugEnabled())
        log4j.debug("COAData - lineSeparatorFormated - i: " + i);
      if (log4j.isDebugEnabled())
        log4j.debug("COAData - lineSeparatorFormated - text: " + text);
      switch (i) {
      case 0:
        coaData.accountValue = text;
        break;
      case 1:
        coaData.accountName = text;
        break;
      case 2:
        coaData.accountDescription = text;
        break;
      case 3:
        coaData.accountType = text;
        break;
      case 4:
        coaData.accountSign = text;
        break;
      case 5:
        coaData.accountDocument = text;
        break;
      case 6:
        coaData.accountSummary = text;
        break;
      case 7:
        coaData.defaultAccount = text;
        break;
      case 8:
        coaData.accountParent = text;
        break;
      case 9:
        coaData.elementLevel = text;
        break;
      case 10:
        coaData.operands = text;
        break;
      case 11:
        coaData.balanceSheet = text;
        break;
      case 12:
        coaData.balanceSheetName = text;
        break;
      case 13:
        coaData.uS1120BalanceSheet = text;
        break;
      case 14:
        coaData.uS1120BalanceSheetName = text;
        break;
      case 15:
        coaData.profitAndLoss = text;
        break;
      case 16:
        coaData.profitAndLossName = text;
        break;
      case 17:
        coaData.uS1120IncomeStatement = text;
        break;
      case 18:
        coaData.uS1120IncomeStatementName = text;
        break;
      case 19:
        coaData.cashFlow = text;
        break;
      case 20:
        coaData.cashFlowName = text;
        break;
      }
      previous = next + 1;
    }
    return coaData;
  }
}
