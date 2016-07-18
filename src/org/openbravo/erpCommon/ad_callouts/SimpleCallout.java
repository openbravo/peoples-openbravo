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
import java.util.Iterator;
import java.util.Map;

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
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
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
 * In this method you can develop the logic of the callout and use the infoobject of class
 * <code>CalloutInfo<code/> to access window fields,
 * database and other methods
 * 
 * @author aro
 */
public abstract class SimpleCallout extends DelegateConnectionProvider {

  private static final long serialVersionUID = 1L;
  private static Logger log = Logger.getLogger(SimpleCallout.class);

  public static final String SIMPLE_CALLOUT_NOT_SELECTED = "not_selected";

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

  public SimpleCalloutResult executeSimpleCallout(RequestContext request,
      SimpleCalloutResult valuesFromFIC) throws ServletException, JSONException {
    // prepare values for callout
    VariablesSecureApp vars = new VariablesSecureApp(request.getRequest());
    CalloutInfo info = new CalloutInfo(vars);

    // execute and parse response of the callout
    try {
      execute(info);
    } catch (ServletException ex) {
      // Error in current callout, continue with following callout
    }
    parseResponeSimpleCallout(request, info, valuesFromFIC);

    return valuesFromFIC;
  }

  private void parseResponeSimpleCallout(RequestContext request, CalloutInfo info,
      SimpleCalloutResult valuesFromFIC) throws JSONException {

    Map<String, Field> inpFields = valuesFromFIC.getInpFields();
    Tab tab = valuesFromFIC.getTab();
    JSONObject result = info.getJSONObjectResult();

    // parsing response and updated values in SimpleCalloutResult to retrieves a result.
    @SuppressWarnings("unchecked")
    Iterator<String> keys = result.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject element = result.getJSONObject(key);

      if (isMessageElement(key)) {
        // first check messages
        JSONObject message = parseMessage(key, element);
        valuesFromFIC.addMessages(message);

      } else if (SimpleCalloutConstants.SC_JSEXECUTE.equals(key)) {
        // The code on a JSEXECUTE command is sent directly to the client for eval()
        String code = element.getString(SimpleCalloutConstants.CLASSIC_VALUE);
        if (code != null) {
          valuesFromFIC.addJsExecuteCode(code);
        }

      } else if (SimpleCalloutConstants.SC_EXECUTE.equals(key)) {
        String js = element.getString(SimpleCalloutConstants.CLASSIC_VALUE) == null ? null
            : element.getString(SimpleCalloutConstants.CLASSIC_VALUE);
        if (js != null && !js.equals("")) {
          if (js.equals("displayLogic();")) {
            // We don't do anything, this is a harmless js response
          } else {
            JSONObject message = createMessage("ERROR", Utility.messageBD(
                new DalConnectionProvider(false), "OBUIAPP_ExecuteInCallout", RequestContext.get()
                    .getVariablesSecureApp().getLanguage()));
            valuesFromFIC.addMessages(message);
            valuesFromFIC.addWarningForWindow();
          }
        }
      } else if (inpFields.containsKey(key)) {
        Column col = inpFields.get(key).getColumn();
        String colID = col.getId();
        String oldValue = request.getRequestParameter(colID);
        Boolean changed = false;

        if (element.has(SimpleCalloutConstants.CLASSIC_VALUE)) {
          String newValue = element.getString(SimpleCalloutConstants.CLASSIC_VALUE);
          if ((oldValue == null && newValue != null) || (oldValue != null && newValue == null)
              || (oldValue != null && newValue != null && !oldValue.equals(newValue))) {
            valuesFromFIC.addColumnValues(
                "inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName()), element);
            // to check refire a callout
            changed = true;
            if (valuesFromFIC.getDynamicCols().contains(colID)) {
              valuesFromFIC.addChangedCols(col.getDBColumnName());
            }
            request.setRequestParameter(key,
                element.getString(SimpleCalloutConstants.CLASSIC_VALUE));
          }
          // If the column is mandatory and it is a combo
        } else if (element.has(SimpleCalloutConstants.ENTRIES) && col.isMandatory()) {
          // remove empty value and we choose the first value as selected
          JSONArray jsonArr = element.getJSONArray(SimpleCalloutConstants.ENTRIES);
          ArrayList<JSONObject> newJsonArr = new ArrayList<JSONObject>();
          JSONObject temporal = null;
          for (int i = 0; i < jsonArr.length(); i++) {
            temporal = jsonArr.getJSONObject(i);
            if (i == 0) {
              continue;
            } else {
              newJsonArr.add(temporal);
            }
          }

          // create element with new values
          String valueSelected = newJsonArr.get(0).getString(JsonConstants.ID);
          JSONObject temporalyElement = new JSONObject();
          temporalyElement.put(SimpleCalloutConstants.VALUE, valueSelected);
          temporalyElement.put(SimpleCalloutConstants.CLASSIC_VALUE, valueSelected);
          temporalyElement.put(SimpleCalloutConstants.ENTRIES, new JSONArray(newJsonArr));

          // added this new value and check refire a callout
          valuesFromFIC.addColumnValues(
              "inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName()), temporalyElement);
          changed = true;
          if (valuesFromFIC.getDynamicCols().contains(colID)) {
            valuesFromFIC.addChangedCols(col.getDBColumnName());
          }
          request.setRequestParameter(key,
              temporalyElement.getString(SimpleCalloutConstants.CLASSIC_VALUE));
        } else {
          log.debug("Column value didn't change. We do not attempt to execute any additional callout");
        }

        if (changed && col.getCallout() != null) {
          // We need to fire this callout, as the column value was changed
          // but only if the callout we are firing is different
          if (!getSimpleClassName().equals(
              col.getCallout().getADModelImplementationList().get(0).getJavaClassName())) {
            // add callouts to call and lastFieldChangedList in
            valuesFromFIC.addCalloutsToFire(col);
          }
        }

      } else {
        for (AuxiliaryInput aux : tab.getADAuxiliaryInputList()) {
          if (key.equalsIgnoreCase("inp" + Sqlc.TransformaNombreColumna(aux.getName()))) {
            valuesFromFIC.addColumnValues(key, element);
            // Add the auxiliary input to the list of auxiliary inputs modified by
            // callouts
            if (!valuesFromFIC.getOverwrittenAuxiliaryInputs().contains(aux.getName())) {
              valuesFromFIC.addOverwrittenAuxiliaryInputs(aux.getName());
            }
          }
        }
        if (!valuesFromFIC.getColumnValues().containsKey(key)) {
          // This returned value wasn't found to be either a column or an auxiliary
          // input. We assume it is a hidden input, which are used in places like
          // selectors
          String hiddenValue = element.getString(SimpleCalloutConstants.CLASSIC_VALUE);
          if (!hiddenValue.equals("null")) {
            valuesFromFIC.addHiddenInputs(key, hiddenValue);
            // We set the hidden fields in the request, so that subsequent callouts
            // can use them
            request.setRequestParameter(key, element.toString());
          }
        }
      }
    }
    // set result json in SimpleCalloutResult
    valuesFromFIC.setResultJson(result);
  }

  private boolean isMessageElement(String key) {
    return ("MESSAGE".equals(key) || "INFO".equals(key) || "WARNING".equals(key)
        || "ERROR".equals(key) || "SUCCESS".equals(key));
  }

  private JSONObject parseMessage(String key, JSONObject element) throws JSONException {
    String elementValue = element.getString(SimpleCalloutConstants.CLASSIC_VALUE);
    return createMessage(key, elementValue);
  }

  private JSONObject createMessage(String key, String elementValue) throws JSONException {
    JSONObject message = new JSONObject();
    message.put("text", elementValue);
    message.put("severity", "MESSAGE".equals(key) ? "TYPE_INFO" : "TYPE_" + key);
    return message;
  }

  private String getSimpleClassName() {
    String classname = getClass().getName();
    int i = classname.lastIndexOf(".");
    if (i < 0) {
      return classname;
    } else {
      return classname.substring(i + 1);
    }
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
     * @return The value of a field named param as an {@code String}.
     */
    public String getStringParameter(String param, RequestFilter filter) {
      return vars.getStringParameter(param, filter);
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
          valueSelected.put(SimpleCalloutConstants.VALUE, name.toString());
          valueSelected.put(SimpleCalloutConstants.CLASSIC_VALUE, name.toString());
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
          jsonobject.put(SimpleCalloutConstants.ENTRIES, new JSONArray(currentSelectResult));
          result.put(currentElement, jsonobject);
        } else {
          result.getJSONObject(currentElement).accumulate(SimpleCalloutConstants.ENTRIES,
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
      try {
        columnValue.put(SimpleCalloutConstants.VALUE, value == null ? "null" : value.toString());
        columnValue.put(SimpleCalloutConstants.CLASSIC_VALUE,
            value == null ? "null" : value.toString());
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
