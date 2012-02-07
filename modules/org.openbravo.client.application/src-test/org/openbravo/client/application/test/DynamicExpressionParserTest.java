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
