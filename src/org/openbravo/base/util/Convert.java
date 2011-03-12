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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Enterprise Intelligence Systems (http://www.eintel.com.au).
 ************************************************************************
 */
package org.openbravo.base.util;

import org.apache.log4j.Logger;
import org.openbravo.base.session.OBPropertiesProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Collection of static methods for converting data from one form to another
 *
 * @author eintelau (ben.sommerville@eintel.com.au)
 */
public class Convert {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(Convert.class);


  /**
   * Convert a string to a Date object using the standard java date format
   * @param strDate
   *          String with date in java date format
   * @return valid Date object, or null if string cannot be parsed into a date
   */
  public static Date toDate(String strDate) {
    if (strDate == null || strDate.isEmpty())
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
   * Convert a string to an amount (BigDecimal) object
   * @param strAmount
   *          String with amount
   * @return valid BigDecimal object, or null if string cannot be parsed into a BigDecimal
   */
  public static BigDecimal toAmount(String strAmount) {
    return toBigDecimal(strAmount);
  }

  /**
   * Convert a string to a General Quantity (BigDecimal) object
   * @param strQuantity
   *          String with amount
   * @return valid BigDecimal object, or null if string cannot be parsed into a BigDecimal
   */
  public static BigDecimal toGeneralQuantity(String strQuantity) {
    return toBigDecimal(strQuantity);
  }

  /**
   * Convert a string to a BigDecimal object
   * @param strNumber
   *          String with number to be converted
   * @return valid BigDecimal object, or null if string cannot be parsed into a BigDecimal
   */
  public static BigDecimal toBigDecimal(String strNumber) {
    if(strNumber == null || strNumber.isEmpty()) {
      return null;
    }
    try {
      return new BigDecimal(strNumber);
    } catch (NumberFormatException e) {
      log4j.error(e.getMessage(), e);
      return null;
    }
  }

  /**
   * Convert a BigDecimal to a string
   * @param in
   *          BigDecimal to convert to a string
   * @return String representation of input
   */
  public static String toString(BigDecimal in) {
    if( in == null ) {
      return "";
    }
    return in.toPlainString();
  }


  /**
   * Convert a BigDecimal to a string with a specified number of decimal places
   * @param in
   *          BigDecimal to convert to a string
   * @param precision
   *          (Max) Number of decimal places to output
   *          Note: precision generally refers to the total number of digits in a number, but within
   *                Openbravo it refers to the number of significant digits after the decimal point.
   * @return String representation of input
   */
  public static String toStringWithPrecision(BigDecimal in, long precision) {
    if( in == null ) {
      return "";
    }
    try {
      return in.setScale((int)precision, RoundingMode.HALF_UP).toPlainString();
    } catch (ArithmeticException e) {
      log4j.error(e.getMessage(), e);
      return null;
    }
  }

}
