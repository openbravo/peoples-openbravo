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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.hibernate.criterion.Expression;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;

/**
 * Parses a dynamic expressions and extracts information, e.g. The expression is using a field or an
 * auxiliary input, etc. <br/>
 * The transformation of @Expression@ is the following:
 * <ul>
 * <li>@ColumnName@ are transformed into property name, e.g. @DocStatus@ into <b>documentStatus</b></li>
 * <li>@AuxiliarInput@ is transformed just removes the <b>@</b>, e.g. @FinancialManagementDep@ into
 * <b>FinancialManagementDep</b></li>
 * </ul>
 * 
 */
public class DynamicExpressionParser {

  private static final String[][] COMPARATIONS = { { "==", " === " }, { "=", " === " },
      { "!", " !== " }, { "^", " !== " }, { "-", " !== " } };

  private static final String[][] UNIONS = { { "|", " || " }, { "&", " && " } };

  private static final String TOKEN_PREFIX = "context.";
  private static Map<String, String> exprToJSMap;
  static {
    exprToJSMap = new HashMap<String, String>();
    exprToJSMap.put("'Y'", "true");
    exprToJSMap.put("'N'", "false");
  }

  private List<Field> fieldsInExpression = new ArrayList<Field>();
  private List<AuxiliaryInput> auxInputsInExpression = new ArrayList<AuxiliaryInput>();
  private List<String> sessionAttributesInExpression = new ArrayList<String>();

  private String code;
  private Tab tab;
  private StringBuffer jsCode;

  public DynamicExpressionParser(String code, Tab tab) {
    this.code = code;
    this.tab = tab;
    parse();
  }

  /*
   * Note: This method was partially copied from WadUtility.
   */
  public void parse() {
    StringTokenizer st = new StringTokenizer(code, "|&", true);
    String token, token2;
    String strAux;
    jsCode = new StringBuffer();
    while (st.hasMoreTokens()) {
      strAux = st.nextToken().trim();
      int i[] = getFirstElement(UNIONS, strAux);
      if (i[0] != -1) {
        strAux = strAux.substring(0, i[0]) + UNIONS[i[1]][1]
            + strAux.substring(i[0] + UNIONS[i[1]][0].length());
      }

      int pos[] = getFirstElement(COMPARATIONS, strAux);
      token = strAux;
      token2 = "";
      if (pos[0] >= 0) {
        token = strAux.substring(0, pos[0]);
        token2 = strAux.substring(pos[0] + COMPARATIONS[pos[1]][0].length(), strAux.length());
        strAux = strAux.substring(0, pos[0]) + COMPARATIONS[pos[1]][1]
            + strAux.substring(pos[0] + COMPARATIONS[pos[1]][0].length(), strAux.length());
      }

      String leftPart = getDisplayLogicText(token);
      jsCode.append(leftPart);

      if (pos[0] >= 0) {
        jsCode.append(COMPARATIONS[pos[1]][1]);
      }

      String rightPart = getDisplayLogicText(token2);
      jsCode.append(leftPart.contains("form.getValue") ? transformValue(rightPart) : rightPart);
    }
  }

  /**
   * Gets a JavaScript expression based on the dynamic expression, e.g @SomeColumn@!'Y' results in
   * form.getValue('someColumn') !== true.<br/>
   * Note: Field comparison with <b>'Y'</b> or <b>'N'</b> are transformed in <b>true</b> or
   * <b>false</b>
   * 
   * @return A JavaScript expression
   */
  public String getJSExpression() {
    return jsCode.toString();
  }

  /**
   * @see DynamicExpressionParser#getJSExpression()
   */
  public String toString() {
    return getJSExpression();
  }

  /**
   * Returns the list of Fields used in the dynamic expression
   * 
   */
  public List<Field> getFields() {
    return fieldsInExpression;
  }

  /**
   * Returns the list of session attribute names used in the dynamic expression
   * 
   */
  public List<String> getSessionAttributes() {
    return sessionAttributesInExpression;
  }

  /**
   * Transform values into JavaScript equivalent, e.g. <b>'Y'</b> into <b>true</b>, based in a
   * defined map. Often used in dynamic expression comparisons
   * 
   * @param value
   *          A string expression like <b>'Y'</b>
   * @return A equivalent value in JavaScript or the same string if has no mapping value
   */
  private String transformValue(String value) {
    return exprToJSMap.get(value) != null ? exprToJSMap.get(value) : value;
  }

  /*
   * This method was partially copied from WadUtility.
   */
  private String getDisplayLogicText(String token) {
    StringBuffer strOut = new StringBuffer();
    String localToken = token;
    int i = localToken.indexOf("@");
    while (i != -1) {
      strOut.append(localToken.substring(0, i));
      localToken = localToken.substring(i + 1);
      i = localToken.indexOf("@");
      if (i != -1) {
        String strAux = localToken.substring(0, i);
        localToken = localToken.substring(i + 1);
        String st = getDisplayLogicTextTranslate(strAux);
        strOut.append(st);
      }
      i = localToken.indexOf("@");
    }
    strOut.append(localToken);
    return strOut.toString();
  }

  /*
   * This method is a different reimplementation of an equivalent method in WadUtility
   */
  private String getDisplayLogicTextTranslate(String token) {
    if (token == null || token.trim().equals(""))
      return "";
    for (Field field : tab.getADFieldList()) {
      if (token.equalsIgnoreCase(field.getColumn().getDBColumnName())) {
        fieldsInExpression.add(field);
        final String fieldName = KernelUtils.getInstance().getPropertyFromColumn(field.getColumn())
            .getName();
        return "form.getValue('" + fieldName + "')";
      }
    }
    OBCriteria<AuxiliaryInput> auxInC = OBDal.getInstance().createCriteria(AuxiliaryInput.class);
    auxInC.add(Expression.eq(AuxiliaryInput.PROPERTY_TAB, tab));
    List<AuxiliaryInput> auxInputs = auxInC.list();
    for (AuxiliaryInput auxIn : auxInputs) {
      if (token.equalsIgnoreCase(auxIn.getName())) {
        auxInputsInExpression.add(auxIn);
        return TOKEN_PREFIX + auxIn.getName();
      }
    }
    sessionAttributesInExpression.add(token);
    return TOKEN_PREFIX + (token.startsWith("#") ? token.replace("#", "_") : token);
  }

  /*
   * This method was partially copied from WadUtility.
   */
  private static int[] getFirstElement(String[][] array, String token) {
    int min[] = { -1, -1 }, aux;
    for (int i = 0; i < array.length; i++) {
      aux = token.indexOf(array[i][0]);
      if (aux != -1 && (aux < min[0] || min[0] == -1)) {
        min[0] = aux;
        min[1] = i;
      }
    }
    return min;
  }

}
