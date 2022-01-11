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

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.jettison.json.JSONArray;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher to check whether a JSONArray contains all the objects of a list
 */
class HasItems extends TypeSafeMatcher<JSONArray> {

  private List<Object> items;

  HasItems(List<Object> items) {
    this.items = items;
  }

  @Override
  protected boolean matchesSafely(JSONArray item) {
    return JSONComparator.arrayContains(item, items);
  }

  @Override
  public void describeTo(Description description) {
    String itemsDesc = items.stream().map(Object::toString).collect(Collectors.joining(","));
    description.appendText("has items <[").appendText(itemsDesc).appendText("]>");
  }

  @Override
  public void describeMismatchSafely(JSONArray item, Description description) {
    description.appendText("missing items in ").appendValue(item);
  }
}
