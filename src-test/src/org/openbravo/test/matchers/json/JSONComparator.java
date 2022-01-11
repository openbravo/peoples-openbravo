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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.matchers.json;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hamcrest.Matcher;

/**
 * Contains the base methods for comparing JSONObjects and JSONArrays
 */
class JSONComparator {

  private JSONComparator() {
  }

  static boolean objectsAreEquivalent(Object obj1, Object obj2) {
    if (!(obj1 instanceof JSONObject) || !(obj2 instanceof JSONObject)) {
      return obj1.equals(obj2);
    }
    JSONObject json1 = (JSONObject) obj1;
    JSONObject json2 = (JSONObject) obj2;
    if (json1.length() != json2.length()) {
      return false;
    }
    return allMatch(json1, key -> propertiesAreEqual(json1.opt(key), json2.opt(key)));
  }

  static boolean objectsMatch(JSONObject json, JSONObject subset) {
    if (subset.length() > json.length()) {
      return false;
    }

    JSONObject common = getCommonProperties(json, subset);

    if (common.length() == 0) {
      // not any common property
      return false;
    }

    return allMatch(common, key -> propertiesAreEqual(common.opt(key), subset.opt(key)));
  }

  static boolean arrayContains(JSONArray array, List<Object> objects) {
    return objects.stream().allMatch(json -> hasEquivalentObject(array, json));
  }

  private static JSONObject getCommonProperties(JSONObject json1, JSONObject json2) {
    @SuppressWarnings("unchecked")
    Stream<String> stream = asStream(json1.keys());
    return stream.filter(json2::has).collect(Collector.of(JSONObject::new, (result, key) -> {
      try {
        result.put(key, json1.get(key));
      } catch (JSONException ignore) {
        // should not fail
      }
    }, (object1, object2) -> {
      throw new UnsupportedOperationException(
          "This JSONObject collector does not support combine operation");
    }));
  }

  private static boolean allMatch(JSONObject json, Predicate<String> keyPredicate) {
    @SuppressWarnings("unchecked")
    Stream<String> stream = asStream(json.keys());
    return stream.allMatch(keyPredicate::test);
  }

  private static boolean propertiesAreEqual(Object prop1, Object prop2) {
    if (prop1 == null || prop2 == null) {
      return prop1 == null && prop2 == null;
    }
    if (prop1.getClass() != prop2.getClass()) {
      if (prop1 instanceof Number && prop2 instanceof Number) {
        return areEqualNumericValues((Number) prop1, (Number) prop2);
      } else if (canCompareTimestampValues(prop1, prop2)) {
        return areEqualStringValues(prop1, prop2);
      } else if (prop2 instanceof Matcher<?>) {
        return ((Matcher<?>) prop2).matches(prop1);
      }
      return false;
    }
    if (prop1 instanceof JSONObject) {
      return objectsAreEquivalent(prop1, prop2);
    }
    if (prop1 instanceof JSONArray) {
      return arraysAreEquivalent((JSONArray) prop1, (JSONArray) prop2);
    }
    return prop1.equals(prop2);
  }

  private static boolean areEqualNumericValues(Number number1, Number number2) {
    return new BigDecimal(number1.toString()).compareTo(new BigDecimal(number2.toString())) == 0;
  }

  private static boolean canCompareTimestampValues(Object object1, Object object2) {
    if (object1 instanceof Timestamp) {
      return object2 instanceof Timestamp || object2 instanceof String;
    }
    return object1 instanceof String && object2 instanceof Timestamp;
  }

  private static boolean areEqualStringValues(Object object1, Object object2) {
    return object1.toString().equals(object2.toString());
  }

  private static boolean hasEquivalentObject(JSONArray array, Object object) {
    return asStream(array).anyMatch(json -> objectsAreEquivalent(json, object));
  }

  private static boolean arraysAreEquivalent(JSONArray array1, JSONArray array2) {
    if (array1.length() != array2.length()) {
      return false;
    }
    return arrayContains(array1, asStream(array2).collect(Collectors.toList()));
  }

  private static Stream<String> asStream(Iterator<String> sourceIterator) {
    Iterable<String> iterable = () -> sourceIterator;
    return StreamSupport.stream(iterable.spliterator(), false);
  }

  private static Stream<Object> asStream(JSONArray array) {
    return IntStream.range(0, array.length()).mapToObj(array::opt);
  }
}
