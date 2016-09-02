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
 * All portions are Copyright (C) 2010-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RegexFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.window.servlet.CalloutServletConfig;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

/**
 * This class is used to implement Openbravo ERP servlet callouts in a simple manner.
 * <p>
 * To develop a new servlet callout based on this class you only have to create a new java class
 * that extends the method:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * protected void execute(CalloutInfo info) throws ServletException;
 * </pre>
 * 
 * </blockquote>
 * <p>
 * In this method you can develop the logic of the callout and use the info object of class
 * <code>CalloutInfo<code/> to access window fields,
 * database and other methods
 * 
 * @author aro
 */
public abstract class SimpleCallout extends DelegateConnectionProvider {

  private static Logger log = Logger.getLogger(SimpleCallout.class);

  /**
   * Overwrite this method to implement a new servlet callout based in <code>SimlpleCallout</code>
   * 
   * @param info
   *          The {@code CalloutInfo} that contains all helper data to access callout information
   *          and servlet information
   * @throws ServletException
   */
  protected abstract void execute(CalloutInfo info) throws ServletException;

  @Override
  public void init(CalloutServletConfig config) {
    super.init(config);
  }

  /**
   * This method execute the SimpleCallout operations.
   * 
   * @return JSONObject with the values updated by the SimpleCallout.
   */
  public JSONObject executeSimpleCallout(RequestContext request) throws ServletException,
      JSONException {
    // prepare values for callout
    VariablesSecureApp vars = new VariablesSecureApp(request.getRequest());
    CalloutInfo info = new CalloutInfo(vars);

    try {
      // execute the callout
      execute(info);
    } catch (ServletException ex) {
      // Error in current SimpleCallout, continue with following callout.
    }

    return info.getJSONObjectResult();
  }

  /**
   * Helper class that contains all data to access callout information and servlet information
   */
  protected static class CalloutInfo {

    private JSONObject result;
    private String currentElement;
    private ArrayList<JSONObject> currentSelectResult;

    /**
     * Provides the coder friendly methods to retrieve certain environment, session and servlet call
     * variables.
     */
    public VariablesSecureApp vars;

    private CalloutInfo(VariablesSecureApp vars) {
      this.vars = vars;
      result = new JSONObject();
      currentElement = null;
      currentSelectResult = new ArrayList<JSONObject>();
    }

    /**
     * 
     * Invokes another SimpleCallout. This method allows to divide callouts functionality into
     * several callout classes
     * 
     * @param callout
     *          SimpleCallout instance to invoke
     */
    public void executeCallout(SimpleCallout callout) throws ServletException {
      callout.execute(this);
    }

    /**
     * 
     * @return The name of field that triggered the callout.
     */
    public String getLastFieldChanged() {
      return vars.getStringParameter("inpLastFieldChanged");
    }

    /**
     * 
     * @return The Tab Id that triggered the callout.
     */
    public String getTabId() {
      return vars.getStringParameter("inpTabId", IsIDFilter.instance);
    }

    /**
     * 
     * @return The Window Id that triggered the callout.
     */
    public String getWindowId() {
      return vars.getStringParameter("inpwindowId", IsIDFilter.instance);
    }

    /**
     * 
     * @param param
     *          The name of the field to get the value.
     * @param filter
     *          Filter used to validate the input against list of allowed inputs.
     * @return The value of a field named param as an {@code String}. If value is modified
     *         previously by a parent callout, updated value is returned.
     */
    public String getStringParameter(String param, RequestFilter filter) {
      String value = "";
      try {
        // if a parent callout modified any value, updated value is returned.
        if (result.has(param)) {
          value = result.getJSONObject(param).get(CalloutConstants.CLASSIC_VALUE).toString();
        } else {
          value = vars.getStringParameter(param, filter);
        }
      } catch (JSONException e) {
        log.error("Error parsing JSON Object.", e);
      }
      return value;
    }

    public String getStringParameter(String param) {
      return getStringParameter(param, null);
    }

    /**
     * 
     * @param param
     *          The name of the field to get the value.
     * @return The value of a field named param as a {@code BigDecimal}.
     * @throws ServletException
     */
    public BigDecimal getBigDecimalParameter(String param) throws ServletException {
      return new BigDecimal(vars.getNumericParameter(param, "0"));
    }

    /**
     * Starts the inclusion of values of a field named param of type select.
     * 
     * @param param
     *          The name of the select field to set the values.
     */
    public void addSelect(String param) {
      currentSelectResult = new ArrayList<JSONObject>();
      currentElement = param;
    }

    /**
     * Adds an entry to the select field and marks it as unselected.
     * 
     * @param name
     *          The entry name to add.
     * @param value
     *          The entry value to add.
     */
    public void addSelectResult(String name, String value) {
      addSelectResult(name, value, false);
    }

    /**
     * Adds an entry value to the select field.
     * 
     * @param name
     *          The entry name to add.
     * @param value
     *          The entry value to add.
     * @param selected
     *          Whether this entry field is selected or not.
     */
    public void addSelectResult(String name, String value, boolean selected) {
      JSONObject entry = new JSONObject();
      try {
        entry.put(JsonConstants.ID, name);
        entry.put(JsonConstants.IDENTIFIER, value);
        currentSelectResult.add(entry);

        if (selected) {
          // If value of combo is selected
          JSONObject valueSelected = new JSONObject();
          valueSelected.put(CalloutConstants.VALUE, name.toString());
          valueSelected.put(CalloutConstants.CLASSIC_VALUE, name.toString());
          result.put(currentElement, valueSelected);
        }
      } catch (JSONException e) {
        log.error("Error parsing JSON Object.", e);
      }
    }

    /**
     * Finish the inclusion of values to the select field and destroy data.
     */
    public void endSelect() {
      try {
        // Added an initial blank element
        if (result.isNull(currentElement)) {
          ArrayList<JSONObject> blankElement = new ArrayList<JSONObject>();
          blankElement.add(new JSONObject());
          blankElement.addAll(currentSelectResult);
          currentSelectResult = blankElement;

          JSONObject jsonobject = new JSONObject();
          jsonobject.put(CalloutConstants.ENTRIES, new JSONArray(currentSelectResult));
          result.put(currentElement, jsonobject);
        } else {
          result.getJSONObject(currentElement).accumulate(CalloutConstants.ENTRIES,
              new JSONArray(currentSelectResult));
        }
      } catch (JSONException e) {
        log.error("Error parsing JSON Object.", e);
      }
      // reset current elements
      currentElement = null;
      currentSelectResult = null;
    }

    /**
     * Sets the value of a field named param with the value indicated.
     * 
     * @param param
     *          The name of the field to get the value.
     * @param value
     *          The value to assign to the field.
     * @throws JSONException
     */
    public void addResult(String param, Object value) {
      JSONObject columnValue = new JSONObject();
      Object strValue = value == null ? "null" : value;

      // handle case when callouts are sending us "\"\"" string.
      if ("\"\"".equals(strValue)) {
        strValue = "";
      }
      try {
        columnValue.put(CalloutConstants.VALUE, strValue);
        columnValue.put(CalloutConstants.CLASSIC_VALUE, strValue);
        result.put(param, columnValue);
      } catch (JSONException e) {
        log.error("Error parsing JSON Object.", e);
      }
    }

    /**
     * Sets the value of a field named param with the value indicated. This method is useful to set
     * numbers like {@code BigDecimal} objects.
     * 
     * @param param
     *          The name of the field to get the value.
     * @param value
     *          The value to assign to the field.
     * @throws JSONException
     */
    public void addResult(String param, String value) {
      addResult(param, (Object) (value == null ? null : value));
    }

    /**
     * Adds a default document number to the result.
     */
    void addDocumentNo() {
      String strTableNameId = getStringParameter("inpkeyColumnId", new RegexFilter(
          "[a-zA-Z0-9_]*_ID"));
      String strDocType_Id = getStringParameter("inpcDoctypeId", IsIDFilter.instance);
      String strTableName = strTableNameId.substring(0, strTableNameId.length() - 3);
      String strDocumentNo = Utility.getDocumentNo(new DalConnectionProvider(false), vars,
          getWindowId(), strTableName, strDocType_Id, strDocType_Id, false, false);
      addResult("inpdocumentno", "<" + strDocumentNo + ">");
    }

    /**
     * Shows a message in the browser with the value indicated.
     * 
     * @param value
     *          The message to display in the browser.
     */
    protected void showMessage(String value) {
      addResult("MESSAGE", value);
    }

    /**
     * Shows an error message in the browser with the value indicated.
     * 
     * @param value
     *          The error message to display in the browser.
     */
    protected void showError(String value) {
      addResult("ERROR", value);
    }

    /**
     * Shows a warning message in the browser with the value indicated.
     * 
     * @param value
     *          The warning message to display in the browser.
     */
    protected void showWarning(String value) {
      addResult("WARNING", value);
    }

    /**
     * Shows an information message in the browser with the value indicated.
     * 
     * @param value
     *          The information message to display in the browser.
     */
    protected void showInformation(String value) {
      addResult("INFO", value);
    }

    /**
     * Shows a success message in the browser with the value indicated.
     * 
     * @param value
     *          The success message to display in the browser.
     */
    protected void showSuccess(String value) {
      addResult("SUCCESS", value);
    }

    /**
     * Executes the javascript code indicated in the value in the browser.
     * 
     * @param value
     *          The javascript code to execute in the browser.
     */
    protected void executeCodeInBrowser(String value) {
      addResult("JSEXECUTE", value);
    }

    /**
     * Returns the value of the result variable as a String
     */
    public String getResult() {
      return result.toString();
    }

    /**
     * Returns the value of the result variable
     */
    public JSONObject getJSONObjectResult() {
      return result;
    }

  }
}
