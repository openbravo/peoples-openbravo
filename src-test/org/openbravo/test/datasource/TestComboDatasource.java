package org.openbravo.test.datasource;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;

public class TestComboDatasource extends BaseDataSourceTestNoDal {

  public void test() throws Exception {

    // Test for organization drop down in Sales order
    Map<String, String> params = new HashMap<String, String>();
    params.put("fieldId", "1127");
    params.put("columnValue", "2221");
    params.put("_operationType", "fetch");
    params.put("_startRow", "1");
    params.put("_endRow", "2");

    String response = doRequest("/org.openbravo.service.datasource/ComboTableDatasourceService",
        params, 200, "POST");
    JSONObject jsonResponse = new JSONObject(response);
    System.out.println("Response " + jsonResponse.toString());
  }
}