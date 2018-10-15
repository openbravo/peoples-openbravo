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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.datasource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

/**
 * Test that verifies the Log datasource works properly
 *
 * @author jarmendariz
 */
public class LogDatasourceTest extends BaseDataSourceTestNoDal {

  @Test
  public void testReturnsMoreEntriesThanRequestedForPagination() throws Exception {
    Map<String, String> params = new ParamBuilder().build();

    assertThat("number of loggers returned", getNumberOfLoggersReturned(requestLoggers(params)),
        is(greaterThan(1)));
  }

  @Test
  public void testFilteringReducesTheNumberOfTotalEntries() throws Exception {
    Map<String, String> paramsFull = new ParamBuilder().build();

    int totalRowsFull = getNumberOfTotalRows(requestLoggers(paramsFull));

    Map<String, String> paramsFiltered = new ParamBuilder() //
        .setSearchLogger("org.hibernate") //
        .build();

    int totalRowsFiltered = getNumberOfTotalRows(requestLoggers(paramsFiltered));

    assertThat("number of total rows", totalRowsFull, is(greaterThan(totalRowsFiltered)));
  }

  @Test
  public void datasourceIsSortedByLoggerByDefault() throws Exception {
    Map<String, String> params = new ParamBuilder().build();

    String firstLoggerOriginal = getFirstLoggerName(requestLoggers(params));

    Map<String, String> paramsSorted = new ParamBuilder().sortByLogger().build();

    String firstLoggerSorted = getFirstLoggerName(requestLoggers(paramsSorted));

    assertThat("first logger returned", firstLoggerOriginal, is(firstLoggerSorted));
  }

  @Test
  public void testOrderingByLoggerReversedChangesTheFirstLoggerReturned() throws Exception {
    Map<String, String> params = new ParamBuilder().build();

    String firstLoggerOriginal = getFirstLoggerName(requestLoggers(params));

    Map<String, String> paramsSorted = new ParamBuilder().sortByLoggerReversed().build();

    String firstLoggerSorted = getFirstLoggerName(requestLoggers(paramsSorted));

    assertThat("first logger returned", firstLoggerOriginal, is(not(firstLoggerSorted)));
  }

  private JSONObject requestLoggers(Map<String, String> parameters) throws Exception {
    String res = doRequest("/org.openbravo.service.datasource/F6DCA62BC0694DACA9CC84748C119FC5",
        parameters, 200, "POST");

    return getResponse(res);
  }

  private String getFirstLoggerName(JSONObject response) throws JSONException {
    return response.getJSONArray("data").getJSONObject(0).getString("logger");
  }

  private int getNumberOfLoggersReturned(JSONObject response) throws JSONException {
    return response.getJSONArray("data").length();
  }

  private int getNumberOfTotalRows(JSONObject response) throws JSONException {
    return response.getInt("totalRows");
  }

  private JSONObject getResponse(String res) throws JSONException {
    JSONObject result = new JSONObject(res);
    return result.getJSONObject("response");
  }

  private class ParamBuilder {
    private String searchLogger = null;
    private String sortBy = null;

    public ParamBuilder setSearchLogger(String searchTerm) {
      searchLogger = searchTerm;
      return this;
    }

    public ParamBuilder sortByLoggerReversed() {
      sortBy = "-logger";
      return this;
    }

    public ParamBuilder sortByLogger() {
      sortBy = "logger";
      return this;
    }

    public Map<String, String> build() {
      Map<String, String> params = new HashMap<>();
      params.put("_operationType", "fetch");
      params.put("_startRow", "0");
      params.put("_endRow", "1");

      if (searchLogger != null) {
        params.put("criteria", "{\"fieldName\":\"logger\",\"operator\":\"iContains\",\"value\":\""
            + searchLogger + "\",\"_constructor\":\"AdvancedCriteria\"}");
      }

      if (sortBy != null) {
        params.put("_sortBy", sortBy);
      }

      return params;
    }
  }
}
