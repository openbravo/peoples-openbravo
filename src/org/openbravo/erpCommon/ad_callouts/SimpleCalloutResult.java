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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;

/**
 * This class is used to manage and switch values from FIC to SimpleCallout and vice versa.
 *
 * @author inigo.sanchez
 *
 */
public class SimpleCalloutResult {

  private Map<String, JSONObject> columnValues;
  private List<String> dynamicCols;
  private Map<String, Object> hiddenInputs;
  private List<String> overwrittenAuxiliaryInputs;
  private List<String> changedCols;
  private List<JSONObject> messages;
  private List<String> jsExecuteCode;

  private HashMap<String, Field> inpFields;
  private Tab tab;

  private List<Column> calloutsToFire;
  private JSONObject resultJson;
  private boolean createNewPreferenceForWindow;

  /**
   * This constructor receives all values except two atributes:
   * <p>
   * * resultJson JSONObject because it is used to retrieves result of SimpleCallout execution and
   * at this moment the callout has not been executed.
   * <p>
   * * calloutsToCall is used to storage list of callouts to fire
   * <p>
   * * createNewPreferenceForWindow is a flag used to show an error if a preference should be
   * created.
   */
  public SimpleCalloutResult(Map<String, JSONObject> columnValues, List<String> dynamicCols,
      Map<String, Object> hiddenInputs, List<String> overwrittenAuxiliaryInputs,
      List<String> changedCols, List<JSONObject> messages, List<String> jsExecuteCode,
      HashMap<String, Field> inpFields, Tab tab) {
    this.columnValues = columnValues;
    this.dynamicCols = dynamicCols;
    this.hiddenInputs = hiddenInputs;
    this.overwrittenAuxiliaryInputs = overwrittenAuxiliaryInputs;
    this.messages = messages;
    this.jsExecuteCode = jsExecuteCode;
    this.changedCols = changedCols;
    this.inpFields = inpFields;
    this.tab = tab;

    // When creating SimpleCalloutResult these fields should be empty.
    this.resultJson = new JSONObject();
    this.calloutsToFire = new ArrayList<Column>();

    this.createNewPreferenceForWindow = false;
  }

  public Map<String, JSONObject> getColumnValues() {
    return columnValues;
  }

  public void setColumnValues(Map<String, JSONObject> columnValues) {
    this.columnValues = columnValues;
  }

  public void addColumnValues(String col, JSONObject json) {
    this.columnValues.put(col, json);
  }

  public List<String> getDynamicCols() {
    return dynamicCols;
  }

  public void setDynamicCols(List<String> dynamicCols) {
    this.dynamicCols = dynamicCols;
  }

  public Map<String, Object> getHiddenInputs() {
    return hiddenInputs;
  }

  public void setHiddenInputs(Map<String, Object> hiddenInputs) {
    this.hiddenInputs = hiddenInputs;
  }

  public void addHiddenInputs(String key, Object hiddenInput) {
    this.hiddenInputs.put(key, hiddenInput);
  }

  public List<String> getOverwrittenAuxiliaryInputs() {
    return overwrittenAuxiliaryInputs;
  }

  public void setOverwrittenAuxiliaryInputs(List<String> overwrittenAuxiliaryInputs) {
    this.overwrittenAuxiliaryInputs = overwrittenAuxiliaryInputs;
  }

  public void addOverwrittenAuxiliaryInputs(String auxiliary) {
    this.overwrittenAuxiliaryInputs.add(auxiliary);
  }

  public List<String> getJsExecuteCode() {
    return jsExecuteCode;
  }

  public void setJsExecuteCode(List<String> jsExecuteCode) {
    this.jsExecuteCode = jsExecuteCode;
  }

  public void addJsExecuteCode(String code) {
    this.jsExecuteCode.add(code);
  }

  public JSONObject getResultJson() {
    return resultJson;
  }

  public void setResultJson(JSONObject resultJson) {
    this.resultJson = resultJson;
  }

  public List<JSONObject> getMessages() {
    return messages;
  }

  public void setMessages(List<JSONObject> changedCols) {
    this.messages = changedCols;
  }

  public void addMessages(JSONObject messageJson) {
    this.messages.add(messageJson);
  }

  public List<Column> getCalloutsToFire() {
    return calloutsToFire;
  }

  public void addCalloutsToFire(Column column) {
    this.calloutsToFire.add(column);
  }

  public List<String> getChangedCols() {
    return changedCols;
  }

  public void setChangedCols(List<String> changedCols) {
    this.changedCols = changedCols;
  }

  public void addChangedCols(String col) {
    this.changedCols.add(col);
  }

  public HashMap<String, Field> getInpFields() {
    return inpFields;
  }

  public void setInpFields(HashMap<String, Field> inpFields) {
    this.inpFields = inpFields;
  }

  public Tab getTab() {
    return tab;
  }

  public void setTab(Tab tab) {
    this.tab = tab;
  }

  public boolean isShouldCreateNewPreferenceForWindow() {
    return createNewPreferenceForWindow;
  }

  public void addWarningForWindow() {
    this.createNewPreferenceForWindow = true;
  }
}
