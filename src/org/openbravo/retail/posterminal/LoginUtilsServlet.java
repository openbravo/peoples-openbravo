package org.openbravo.retail.posterminal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.json.JsonUtils;

public class LoginUtilsServlet extends WebServiceAbstractServlet {

  private static final Logger log = Logger.getLogger(LoginUtilsServlet.class);

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {

    OBContext.setOBContext("0", "0", "0", "0");

    JSONObject result = new JSONObject();
    JSONObject resp = new JSONObject();
    JSONArray data = new JSONArray();
    JSONObject item;
    try {
      item = new JSONObject();
      item.put("user", "Openbravo");
      item.put("userName", "Openbravo");
      item.put("image", "none");
      item.put("connected", "true");
      data.put(item);

      item = new JSONObject();
      item.put("user", "test");
      item.put("userName", "Test User");
      item.put(
          "image",
          "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAAGXcA1uAAAAB3RJTUUH2wgCAAoreAfUzAAAAAlwSFlzAABOIAAATiABFn2Z3gAAAARnQU1BAACxjwv8YQUAAAKfSURBVHjapVQ9b9pAGLYRzAUlY5WyVBkYKFs6oJiOzVDWKEP5CckviPsL2v6C0CFpIqUqVApqUIUdMZBKSDEDQzMQkg4MSNgSg5EYrs/r3tHzB9RVX+nx+e699/OeO0VZJip9Ph4fm483NrbD6tFoxGhM0Baz1bryaV3XNYH0Uu/9ft8zT4ooQpHL5dQv9Tpbakm7GY+RBsI7KTWRns9CDiyC10VwCtxut1mxWFQTmL+SrE17MvE1pYaBirN29/b2aY0x5tBcVVVNeIROS3AnC2+2bQ8B5jhODQh1NiH960Dta6NRzmQyKoxMQBkMBhrX6bJBQTJMIwXte6djTSaTN4DxdHNzoUzy8Qb5qRH9tZqXlxTBWFtfLylx5ez0TIu9OSgLrom2oZ2COzaQOT05oWKfAQ705ZAHEMERo/iXjz4ZjDCbzR6hnTUasXQP3TseIVrAKF0wCwgVK2pYKBDFi4YD81LAedAZUV3Gktb/FlC0QjQFtKgaQqFB3Wq32z0izzyTQqitAbFwrSmV10AJnI/aExZQgxHEPLkighcF7b3H+CSWdxLwKB17c1BAmyygA+W4NuI6iDbLlCeuWfP5vIL/bCqV0nG9h5iT7jMf32N9H7bEHDnz8BnLz5As0+mUceh8npbWIm1kX0lpvRDgh1cBDuEA/2+Bw/F4fIi57OuAOwxWUIgKoAc2kaHyrdmkl1D9+fBAOnJUVf6cOp0FXa6K4r9gDtf9vUVRcvvjdggwDjNui1bxVRbLnbmU7RCgJ2K71+uZ+Xxei/KhxnAYKded6yyGO2npauv5lhbctwgQQdNV4hHAaBlUyY0cpPSi5AviC7DyfVoijYuLCoYjaenDy52dipisommsSuCs+un8nBhGVKZ3xfzXJP9LfgFzgJUOj4I2DQAAAABJRU5ErkJggg==");
      item.put("connected", "false");
      data.put(item);

      resp.put("startRow", 0);
      resp.put("endRow", 1);
      resp.put("totalRows", 2);
      resp.put("data", data);
      result.append("response", resp);

    } catch (JSONException e) {
      log.error(e.getMessage(), e);
      writeResult(response, JsonUtils.convertExceptionToJson(e));
    }

    writeResult(response, result.toString());
  }
}