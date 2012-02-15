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
 * All portions are Copyright (C) 2010-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DynamicExpressionParserTest extends TestCase {

  private static Map<String, String> exprToJSMap;
  static {
    exprToJSMap = new HashMap<String, String>();
    exprToJSMap.put("'Y'", "true");
    exprToJSMap.put("'N'", "false");
  }

  private String transformValue(String value) {
    if (value == null) {
      return null;
    }
    String removeBracketsRegExp = "[\\[\\(]*(.*?)[\\)\\]]*";
    Pattern pattern = Pattern.compile(removeBracketsRegExp);
    Matcher matcher = pattern.matcher(value);
    String transformedValueWithBrackets = null;
    // It is always matched: zero or plus opening brackets, followed by any string, follow by zero
    // or plus closing brackets
    if (matcher.matches()) {
      // Extracts the value
      String valueWithoutBrackets = matcher.group(1);
      // Transforms the value
      String transformedValueWithoutBrackets = exprToJSMap.get(valueWithoutBrackets) != null ? exprToJSMap
          .get(valueWithoutBrackets) : valueWithoutBrackets;
      // Re-encloses the value
      transformedValueWithBrackets = value.replace(valueWithoutBrackets,
          transformedValueWithoutBrackets);
    }
    return transformedValueWithBrackets;
  }

  public void testRegularExpression() {
    String value = "'Y'";
    String transformedValue = transformValue(value);
    Assert.assertEquals(transformedValue, "true");

    value = "(['Y'";
    transformedValue = transformValue(value);
    Assert.assertEquals(transformedValue, "([true");

    value = "'Y')";
    transformedValue = transformValue(value);
    Assert.assertEquals(transformedValue, "true)");

    value = "(['Y'])";
    transformedValue = transformValue(value);
    Assert.assertEquals(transformedValue, "([true])");

    value = "('NotBoolean')";
    transformedValue = transformValue(value);
    Assert.assertEquals(transformedValue, "('NotBoolean')");
  }

}
