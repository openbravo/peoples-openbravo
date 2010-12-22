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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.window;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Expression;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.client.application.window.servlet.CalloutHttpServletResponse;
import org.openbravo.client.application.window.servlet.CalloutServletConfig;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.client.kernel.KernelUtils;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.reference.FKComboUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.Sqlc;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.domain.ReferencedTable;
import org.openbravo.model.ad.ui.AuxiliaryInput;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.service.db.DalConnectionProvider;

public class FormInitializationComponent extends BaseActionHandler {
  private static final Logger log = Logger.getLogger(FormInitializationComponent.class);

  private static final int MAX_CALLOUT_CALLS = 10;

  private HttpServletResponse response;

  public void doPost(HttpServletRequest localRequest, HttpServletResponse localResponse,
      ServletContext localContext) throws IOException {
    this.response = localResponse;
    HashMap<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("MODE", "NEW");
    parameters.put("TAB_ID", "186");
    parameters.put("ROW_ID", "1000019");
    JSONObject obj = execute(parameters, null);
    PrintWriter out = response.getWriter();
    out.print(obj.toString());
    out.flush();
    out.close();
  }

  // @Override
  protected JSONObject execute(Map<String, Object> parameters, String content) {
    OBContext.setAdminMode(true);
    long iniTime = System.currentTimeMillis();
    try {
      JSONObject jsContent = null;
      try {
        if (content == null) {
          jsContent = new JSONObject();
        } else {
          jsContent = new JSONObject(content);
        }
      } catch (JSONException e) {
        throw new OBException("Error while parsing content", e);
      }
      String mode = (String) parameters.get("MODE");
      String parentId = (String) parameters.get("PARENT_ID");
      String tabId = (String) parameters.get("TAB_ID");
      String rowId = (String) parameters.get("ROW_ID");
      String changedColumn = (String) parameters.get("CHANGED_COLUMN");
      Tab tab = OBDal.getInstance().get(Tab.class, tabId);
      List<Field> fields = tab.getADFieldList();
      BaseOBObject row = OBDal.getInstance().get(tab.getTable().getName(), rowId);
      Tab parentTab = null;
      BaseOBObject parentRecord = null;
      log.debug("TAB NAME: " + tab.getWindow().getName() + "." + tab.getName() + " Tab Id:"
          + tab.getId());

      // First the session variables for the parent records are set
      if (mode.equals("EDIT")) {
        parentRecord = KernelUtils.getInstance().getParentRecord(row, tab);
      }
      parentTab = KernelUtils.getInstance().getParentTab(tab);
      if (parentId != null && parentTab != null) {
        parentRecord = OBDal.getInstance().get(
            ModelProvider.getInstance().getEntityByTableName(parentTab.getTable().getDBTableName())
                .getName(), parentId);
      }
      if (parentTab != null && parentRecord != null) {
        setSessionValues(parentRecord, parentTab);
      }
      // We also need to set the current record values in the request
      if (mode.equals("EDIT")) {
        // In EDIT mode we get them from the database
        for (Field field : fields) {
          setValueOfColumnInRequest(row, field.getColumn().getDBColumnName());
        }
      } else if (mode.equals("CHANGE") || mode.equals("SETSESSION")) {
        // In CHANGE and SETSESSION we get them from the request
        for (Field field : fields) {
          String inpColName = "inp"
              + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName());
          try {
            if (jsContent.has(inpColName)) {
              String value;
              if (jsContent.get(inpColName) == null
                  || jsContent.get(inpColName).toString().equals("null")) {
                value = null;
              } else {
                value = jsContent.get(inpColName).toString();
              }
              RequestContext.get().setRequestParameter(inpColName, value);
            }
          } catch (Exception e) {
            log.error("Couldn't read column value from the request for column " + inpColName, e);
          }
        }
      }
      HashMap<String, JSONObject> columnValues = new HashMap<String, JSONObject>();
      HashMap<String, Field> columnsOfFields = new HashMap<String, Field>();
      ArrayList<String> allColumns = new ArrayList<String>();
      ArrayList<String> calloutsToCall = new ArrayList<String>();
      ArrayList<String> lastfieldChanged = new ArrayList<String>();
      List<String> changeEventCols = new ArrayList<String>();

      for (Field field : fields) {
        columnsOfFields.put(field.getColumn().getDBColumnName(), field);
      }

      // Calculation of validation dependencies
      HashMap<String, List<String>> columnsInValidation = new HashMap<String, List<String>>();
      computeListOfColumnsSortedByValidationDependencies(tab, allColumns, columnsInValidation,
          changeEventCols);

      // Computation of the Auxiliary Input values
      OBCriteria<AuxiliaryInput> auxInC = OBDal.getInstance().createCriteria(AuxiliaryInput.class);
      auxInC.add(Expression.eq(AuxiliaryInput.PROPERTY_TAB, tab));
      List<AuxiliaryInput> auxInputs = auxInC.list();
      for (AuxiliaryInput auxIn : auxInputs) {
        Object value = computeAuxiliaryInput(auxIn, tab.getWindow().getId());
        log.debug("Final Computed Value. Name: " + auxIn.getName() + " Value: " + value);
        JSONObject jsonObj = new JSONObject();
        try {
          jsonObj.put("value", value);
        } catch (JSONException e) {
          log.error("Error while computing auxiliary input " + auxIn.getName(), e);
        }
        columnValues.put("inp" + Sqlc.TransformaNombreColumna(auxIn.getName()), jsonObj);
        RequestContext.get().setRequestParameter(
            "inp" + Sqlc.TransformaNombreColumna(auxIn.getName()),
            value == null ? null : value.toString());
        // Now we insert session values for auxiliary inputs
        if (mode.equals("NEW") || mode.equals("EDIT") || mode.equals("SETSESSION")) {
          setSessionValue(tab.getWindow().getId() + "|" + auxIn.getName(), columnValues.get("inp"
              + Sqlc.TransformaNombreColumna(auxIn.getName())));
        }
      }

      // Column values are set in the RequestContext
      for (String col : allColumns) {
        Field field = columnsOfFields.get(col);
        try {
          String columnId = field.getColumn().getId();
          UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(columnId);
          String value = null;
          if (mode.equals("NEW")) {
            // On NEW mode, the values are computed through the UIDefinition (the defaults will be
            // used)
            value = uiDef.getFieldProperties(field, false);
          } else if (mode.equals("EDIT")
              || (mode.equals("CHANGE") && changeEventCols.contains(changedColumn))) {
            // On EDIT mode, the values are computed through the UIDefinition (the values have been
            // previously set in the RequestContext)
            // This is also done this way on CHANGE mode where a combo reload is needed
            value = uiDef.getFieldProperties(field, true);
          } else if (mode.equals("CHANGE") || mode.equals("SETSESSION")) {
            // On CHANGE and SETSESSION mode, the values are read from the request
            JSONObject jsCol = new JSONObject();
            String colName = "inp" + Sqlc.TransformaNombreColumna(col);
            if (!jsContent.has(colName) || jsContent.get(colName) == JSONObject.NULL) {
              continue;
            }
            jsCol.put("value", jsContent.get(colName));
            value = jsCol.toString();
          }
          JSONObject jsonobject = new JSONObject(value);
          columnValues.put("inp"
              + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()), jsonobject);
          // We need to fire callouts if the field value was changed, or if the field is a combo
          // (due to how ComboReloads worked, callouts were always called)
          if (mode.equals("NEW")
              && ((jsonobject.has("value") && !jsonobject.get("value").equals("")) || uiDef instanceof FKComboUIDefinition)) {
            if (field.getColumn().getCallout() != null) {
              addCalloutToList(field.getColumn(), calloutsToCall, lastfieldChanged);
            }
          }
          setRequestContextParameter(field, jsonobject);

          // We also set the session value for the column in Edit or SetSession mode
          if (mode.equals("EDIT") || mode.equals("SETSESSION")) {
            if (field.getColumn().isStoredInSession()) {
              setSessionValue(tab.getWindow().getId() + "|" + field.getColumn().getDBColumnName(),
                  jsonobject.has("value") ? uiDef.formatValueToSQL(jsonobject.get("value")
                      .toString()) : null);
            }
          }
        } catch (Exception e) {
          throw new OBException("Couldn't get data for column "
              + field.getColumn().getDBColumnName(), e);
        }
      }

      // List of the callouts that need to be called
      ArrayList<String> messages = new ArrayList<String>();
      if (mode.equals("NEW")) {
        for (Field field : fields) {
          if (field.getColumn().getCallout() != null) {
            Object value;
            try {
              JSONObject jsonCol = columnValues.get("inp"
                  + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()));
              value = jsonCol.has("value") ? jsonCol.get("value") : null;
              if (value != null && !value.toString().equals("")) {
                // There is a callout and the value for this field is set

                addCalloutToList(field.getColumn(), calloutsToCall, lastfieldChanged);
              }
            } catch (JSONException e) {
              log.error("Error reading value from parameter. Not executing callouts for column "
                  + field.getColumn().getDBColumnName(), e);
            }
          }
        }
      }

      // In CHANGE mode, we will add the initial callout call for the changed column, if there is
      // one
      if (mode.equals("CHANGE")) {
        if (changedColumn != null) {
          for (Column col : tab.getTable().getADColumnList()) {
            if (("inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName())).equals(changedColumn)) {
              if (col.getCallout() != null) {
                // The column has a callout. We will add the callout to the callout list
                addCalloutToList(col, calloutsToCall, lastfieldChanged);
              }
            }
          }
        }
      }

      ArrayList<String> calledCallouts = new ArrayList<String>();
      runCallouts(columnValues, fields, calledCallouts, calloutsToCall, lastfieldChanged, messages);

      JSONObject finalObject = new JSONObject();
      try {
        if (mode.equals("NEW") || mode.equals("CHANGE")) {
          JSONArray arrayMessages = new JSONArray(messages);
          finalObject.put("calloutMessages", arrayMessages);
        }
        if (mode.equals("NEW") || mode.equals("EDIT") || mode.equals("CHANGE")) {
          JSONObject jsonColumnValues = new JSONObject();
          for (Field field : fields) {
            jsonColumnValues.put(field.getColumn().getDBColumnName(), columnValues.get("inp"
                + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName())));
          }
          finalObject.put("columnValues", jsonColumnValues);
        }
        JSONObject jsonAuxiliaryInputValues = new JSONObject();
        for (AuxiliaryInput auxIn : auxInputs) {
          jsonAuxiliaryInputValues.put(auxIn.getName(), columnValues.get("inp"
              + Sqlc.TransformaNombreColumna(auxIn.getName())));
        }
        finalObject.put("auxiliaryInputValues", jsonAuxiliaryInputValues);

        if (mode.equals("NEW") || mode.equals("EDIT")) {
          // We also include information related to validation dependencies
          // and we add the columns which have a callout
          for (Field field : fields) {
            if (field.getColumn().getCallout() != null) {
              final String columnName = "inp"
                  + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName());
              if (!changeEventCols.contains(columnName)) {
                changeEventCols.add(columnName);
              }
            }
          }
          finalObject.put("dynamicCols", new JSONArray(changeEventCols));
        }
        log.debug(finalObject.toString(1));
        log.debug("Elapsed time: " + (System.currentTimeMillis() - iniTime));
        return finalObject;
      } catch (JSONException e) {
        log.error("Error while generating the final JSON object: ", e);
      }

      // }
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  private void computeListOfColumnsSortedByValidationDependencies(Tab tab,
      ArrayList<String> sortedColumns, HashMap<String, List<String>> columnsInValidation,
      List<String> changeEventCols) {
    List<Field> fields = tab.getADFieldList();
    ArrayList<String> columns = new ArrayList<String>();
    List<String> columnsWithValidation = new ArrayList<String>();
    HashMap<String, String> validations = new HashMap<String, String>();
    for (Field field : fields) {
      String columnName = field.getColumn().getDBColumnName();
      columns.add(columnName.toUpperCase());
      if (field.getColumn().getValidation() != null) {
        columnsWithValidation.add(field.getColumn().getDBColumnName());
        validations.put(field.getColumn().getDBColumnName(), getValidation(field));
      }
    }
    for (String column : columnsWithValidation) {
      columnsInValidation.put(column, parseValidation(column, validations.get(column), columns));
      String cols = "";
      for (String col : columnsInValidation.get(column)) {
        cols += col + ",";
      }
      log.debug("Column: " + column);
      log.debug("Validation: '" + validations.get(column) + "'");
      log.debug("Columns in validation: '" + cols + "'");
    }

    // Add client and org first to compute dependencies correctly
    for (Field field : fields) {
      String colName = field.getColumn().getDBColumnName();
      if (colName.equalsIgnoreCase("Ad_Client_Id")) {
        sortedColumns.add(colName);
      }
    }
    for (Field field : fields) {
      String colName = field.getColumn().getDBColumnName();
      if (colName.equalsIgnoreCase("Ad_Org_Id")) {
        sortedColumns.add(colName);
      }
    }
    // we add the columns not included in the sortedColumns
    // (the ones which don't have validations)
    for (Field field : fields) {
      String colName = field.getColumn().getDBColumnName();
      if (field.getColumn().getValidation() == null && !sortedColumns.contains(colName)) {
        sortedColumns.add(colName);
      }
    }
    String nonDepColumn = pickNonDependantColumn(sortedColumns, columnsWithValidation,
        columnsInValidation);
    while (nonDepColumn != null) {
      sortedColumns.add(nonDepColumn);
      nonDepColumn = pickNonDependantColumn(sortedColumns, columnsWithValidation,
          columnsInValidation);
    }

    String cycleCols = "";
    for (String col : columnsWithValidation) {
      if (!sortedColumns.contains(col)) {
        cycleCols += "," + col;
      }
    }
    if (!cycleCols.equals("")) {
      throw new OBException("Error. The columns " + cycleCols.substring(1)
          + " have validations which form a cycle.");
    }
    String finalCols = "";
    for (String col : sortedColumns) {
      finalCols += col + ",";
    }
    log.debug("Final order of column computation: " + finalCols);

    // We also fill the changeEventCols
    // These are the columns which should trigger a CHANGE request to the FIC (because either they
    // require a combo reload because they are used in a validation, or there is a callout
    // associated with them)
    for (Field field : fields) {
      String column = field.getColumn().getDBColumnName();
      if (columnsInValidation.get(column) != null && columnsInValidation.get(column).size() > 0) {
        for (String colInVal : columnsInValidation.get(column)) {
          final String columnName = "inp" + Sqlc.TransformaNombreColumna(colInVal);
          if (!changeEventCols.contains(columnName)) {
            changeEventCols.add(columnName);
          }
        }
      }
    }
  }

  private Object parseDateFromDAL(Object value) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    format.setLenient(true);
    Date date;
    try {
      date = format.parse(value.toString());
    } catch (ParseException e) {
      throw new OBException("Error while parsing date: " + value, e);
    }
    SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    outFormat.setLenient(true);
    return outFormat.format(date);
  }

  private void setValueOfColumnInRequest(BaseOBObject obj, String columnName) {
    Entity entity = obj.getEntity();
    Property prop = entity.getPropertyByColumnName(columnName);
    Object currentValue = obj.get(prop.getName());

    if (currentValue != null) {
      if (prop.isDate()) {
        currentValue = parseDateFromDAL(currentValue);
      } else if (currentValue instanceof BaseOBObject) {
        if (prop.getReferencedProperty() != null) {
          currentValue = ((BaseOBObject) currentValue).get(prop.getReferencedProperty().getName());
        } else {
          currentValue = ((BaseOBObject) currentValue).getId();
        }
      }
      RequestContext.get().setRequestParameter("inp" + Sqlc.TransformaNombreColumna(columnName),
          currentValue.toString());
    }
  }

  private void setSessionValues(BaseOBObject object, Tab tab) {
    for (Column col : tab.getTable().getADColumnList()) {
      if (col.isStoredInSession()) {
        Property prop = object.getEntity().getPropertyByColumnName(col.getDBColumnName());
        Object value = object.get(prop.getName());
        if (value != null) {
          if (prop.isDate()) {
            value = parseDateFromDAL(value);
          } else if (value instanceof BaseOBObject) {
            value = ((BaseOBObject) value).getId();
          } else {
            value = UIDefinitionController.getInstance().getUIDefinition(col.getId())
                .formatValueToSQL(value.toString());
          }
          setSessionValue(tab.getWindow().getId() + "|" + col.getName(), value);
        }
        // We also set the value of every column in the RequestContext so that it is available for
        // the Auxiliary Input computation
        setValueOfColumnInRequest(object, col.getDBColumnName());
      }
    }
    OBCriteria<AuxiliaryInput> auxInC = OBDal.getInstance().createCriteria(AuxiliaryInput.class);
    auxInC.add(Expression.eq(AuxiliaryInput.PROPERTY_TAB, tab));
    List<AuxiliaryInput> auxInputs = auxInC.list();
    for (AuxiliaryInput auxIn : auxInputs) {
      Object value = computeAuxiliaryInput(auxIn, tab.getWindow().getId());
      setSessionValue(tab.getWindow().getId() + "|" + auxIn.getName(), value);
    }
    Tab parentTab = KernelUtils.getInstance().getParentTab(tab);
    BaseOBObject parentRecord = KernelUtils.getInstance().getParentRecord(object, tab);
    if (parentTab != null && parentRecord != null) {
      setSessionValues(parentRecord, parentTab);
    }
  }

  private void setSessionValue(String key, Object value) {
    log.debug("Setting session value. Key: " + key + "  Value:" + value);
    RequestContext.get().setSessionAttribute(key, value);
  }

  private void setRequestContextParameter(Field field, JSONObject jsonObj) {
    try {
      String fieldId = "inp" + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName());
      RequestContext.get().setRequestParameter(fieldId,
          jsonObj.has("value") ? jsonObj.getString("value") : null);
    } catch (JSONException e) {
      log.error("Couldn't read JSON parameter for column " + field.getColumn().getDBColumnName());
    }
  }

  private void setRequestContextParameters(List<Field> fields,
      HashMap<String, JSONObject> columnValues) {
    for (Field field : fields) {
      String fieldId = "inp" + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName());
      JSONObject jsonObj = columnValues.get(fieldId);
      if (jsonObj != null) {
        setRequestContextParameter(field, jsonObj);
      }
    }

  }

  private HashMap<String, Field> buildInpField(List<Field> fields) {
    HashMap<String, Field> inpFields = new HashMap<String, Field>();
    for (Field field : fields) {
      inpFields.put("inp" + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()),
          field);
    }
    return inpFields;
  }

  // TODO: This method should probably be transformed into a utility class
  private void runCallouts(HashMap<String, JSONObject> columnValues, List<Field> fields,
      ArrayList<String> calledCallouts, ArrayList<String> calloutsToCall,
      ArrayList<String> lastfieldChangedList, ArrayList<String> messages) {

    HashMap<String, Field> inpFields = buildInpField(fields);

    while (!calloutsToCall.isEmpty() && calledCallouts.size() < MAX_CALLOUT_CALLS) {
      String calloutClassName = calloutsToCall.get(0);
      String lastFieldChanged = lastfieldChangedList.get(0);
      log.debug("Calling callout " + calloutClassName);
      try {
        Class<?> calloutClass = Class.forName(calloutClassName);
        calloutsToCall.remove(calloutClassName);
        lastfieldChangedList.remove(lastFieldChanged);
        Object calloutInstance = calloutClass.newInstance();
        Method method = null;
        Method init = null;
        Method service = null;
        for (Method m : calloutClass.getMethods()) {
          if (m.getName().equals("doPost")) {
            method = m;
          }
          if (m.getName().equals("init") && m.getParameterTypes().length == 1) {
            init = m;
          }
          if (m.getName().equals("service")) {
            service = m;
          }
        }

        if (method == null) {
          log.error("Couldn't find method doPost in Callout " + calloutClassName);
        } else {
          RequestContext rq = RequestContext.get();
          // We first prepare the data so that it's usable by the callout
          formatColumnValues(columnValues, fields);
          RequestContext.get().setRequestParameter("inpLastFieldChanged", lastFieldChanged);

          // We then execute the callout
          CalloutServletConfig config = new CalloutServletConfig(calloutClassName, RequestContext
              .getServletContext());
          Object[] initArgs = { config };
          init.invoke(calloutInstance, initArgs);
          CalloutHttpServletResponse fakeResponse = new CalloutHttpServletResponse(rq.getResponse());
          Object[] arguments = { rq.getRequest(), fakeResponse };
          service.invoke(calloutInstance, arguments);
          method.invoke(calloutInstance, arguments);
          String calloutResponse = fakeResponse.getOutputFromWriter();

          // Now we restore the request data so that it's compatible with the UIDefinition
          // computation
          setRequestContextParameters(fields, columnValues);
          // Now we parse the callout response and modify the stored values of the columns modified
          // by the callout
          ArrayList<NativeArray> returnedArray = new ArrayList<NativeArray>();
          String calloutNameJS = parseCalloutResponse(calloutResponse, returnedArray);
          if (calloutNameJS != null && calloutNameJS != "") {
            calledCallouts.add(calloutNameJS);
          }
          if (returnedArray.size() > 0) {
            for (NativeArray element : returnedArray) {
              String name = (String) element.get(0, null);
              if (name.equals("MESSAGE")) {
                log.debug("Callout message: " + element.get(1, null));
                messages.add(element.get(1, null).toString());
              } else {
                if (name.startsWith("inp")) {
                  boolean changed = false;
                  if (inpFields.containsKey(name)) {
                    Column col = inpFields.get(name).getColumn();
                    String colId = "inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName());
                    if (element.get(1, null) instanceof NativeArray) {
                      // Combo data
                      NativeArray subelements = (NativeArray) element.get(1, null);
                      for (int j = 0; j < subelements.getLength(); j++) {
                        NativeArray subelement = (NativeArray) subelements.get(j, null);
                        if (subelement.get(2, null).toString().equalsIgnoreCase("True")) {
                          String value = subelement.get(0, null).toString();
                          log.debug("Column: " + col.getDBColumnName() + "  Value: " + value);
                          rq.setRequestParameter(colId, value);
                          UIDefinition uiDef = UIDefinitionController.getInstance()
                              .getUIDefinition(col.getId());
                          JSONObject jsonobject = new JSONObject(uiDef.getFieldProperties(inpFields
                              .get(name), true));
                          columnValues.put(colId, jsonobject);
                          changed = true;
                        }
                      }
                    } else {
                      // Normal data
                      Object el = element.get(1, null);
                      String value;
                      if (el instanceof Double) {
                        value = ((Double) el).toString();
                      } else {
                        value = (String) el;
                      }
                      log.debug("Modified column: " + col.getDBColumnName() + "  Value: " + value);
                      rq.setRequestParameter(colId, value);
                      UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(
                          col.getId());
                      JSONObject jsonobj = new JSONObject(uiDef.getFieldProperties(inpFields
                          .get(name), true));
                      columnValues.put("inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName()),
                          jsonobj);
                      changed = true;
                    }
                    if (changed && col.getCallout() != null) {
                      // We need to fire this callout, as the column value was changed
                      addCalloutToList(col, calloutsToCall, lastfieldChangedList);
                    }
                  }
                }
              }
            }
          }
        }
      } catch (ClassNotFoundException e) {
        log.error("Couldn't find class " + calloutClassName, e);
      } catch (Exception e) {
        log.error("Couldn't execute callout (class " + calloutClassName + ")", e);
      }
    }
    if (calledCallouts.size() == MAX_CALLOUT_CALLS) {
      log.warn("Warning: maximum number of callout calls reached");
    }

  }

  private void addCalloutToList(Column col, ArrayList<String> listOfCallouts,
      ArrayList<String> lastFieldChangedList) {
    if (col.getCallout().getADModelImplementationList() == null
        || col.getCallout().getADModelImplementationList().size() == 0) {
      log.error("The callout of the column " + col.getDBColumnName()
          + " doesn't have a corresponding model object, and therefore cannot be executed.");
    } else {
      String calloutClassNameToCall = col.getCallout().getADModelImplementationList().get(0)
          .getJavaClassName();
      listOfCallouts.add(calloutClassNameToCall);
      lastFieldChangedList.add("inp" + Sqlc.TransformaNombreColumna(col.getDBColumnName()));
    }
  }

  private void formatColumnValues(HashMap<String, JSONObject> columnValues, List<Field> fields) {
    for (Field field : fields) {
      UIDefinition uiDef = UIDefinitionController.getInstance().getUIDefinition(
          field.getColumn().getId());
      JSONObject obj = columnValues.get("inp"
          + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()));
      try {
        if (obj != null) {
          String oldValue = obj.has("value") ? obj.getString("value") : null;
          String value = oldValue == null || oldValue.equals("") ? oldValue : uiDef
              .formatValueToSQL(oldValue.toString());
          RequestContext.get().setRequestParameter(
              "inp" + Sqlc.TransformaNombreColumna(field.getColumn().getDBColumnName()), value);
        }
      } catch (Exception e) {
        log.error("Error while formatting column " + field.getColumn().getDBColumnName(), e);
        throw new OBException("died");
      }
    }
  }

  private String parseCalloutResponse(String calloutResponse, ArrayList<NativeArray> returnedArray) {
    String initS = "id=\"paramArray\">";
    String resp = calloutResponse.substring(calloutResponse.indexOf(initS) + initS.length());
    resp = resp.substring(0, resp.indexOf("</")).trim();
    if (!resp.contains("new Array(")) {
      return null;
    }
    Context cx = Context.enter();
    Scriptable scope = cx.initStandardObjects();
    cx.evaluateString(scope, resp, "<cmd>", 1, null);
    try {
      NativeArray oresp = (NativeArray) scope.get("respuesta", scope);
      Object calloutName = scope.get("calloutName", scope);
      String calloutNameS = calloutName == null ? null : calloutName.toString();
      log.debug("Callout Name: " + calloutNameS);
      NativeArray array = (NativeArray) oresp;
      for (int i = 0; i < array.getLength(); i++) {
        returnedArray.add((NativeArray) array.get(i, null));
      }
      return calloutNameS;
    } catch (Exception e) {
      log.error("Couldn't parse callout response. The parsed response was: " + resp, e);
    }
    return null;
  }

  private String pickNonDependantColumn(List<String> sortedColumns, List<String> columns,
      HashMap<String, List<String>> columnsInValidation) {
    for (String col : columns) {
      if (sortedColumns.contains(col)) {
        continue;
      }
      if (columnsInValidation.get(col) == null || columnsInValidation.get(col).isEmpty()) {
        return col;
      }
      boolean allColsSorted = true;
      for (String depCol : columnsInValidation.get(col)) {
        if (!sortedColumns.contains(depCol))
          allColsSorted = false;
      }
      if (allColsSorted)
        return col;
    }

    return null;
  }

  private String getValidation(Field field) {
    Column c = field.getColumn();
    String val = c.getValidation().getValidationCode();
    if (c.getReference().getId().equals("18")) {
      if (c.getReferenceSearchKey() != null) {
        for (ReferencedTable t : c.getReferenceSearchKey().getADReferencedTableList()) {
          val += " AND " + t.getSQLWhereClause();
        }
      }
    }
    return val;

  }

  private ArrayList<String> parseValidation(String column, String validation,
      List<String> possibleColumns) {
    String token = validation;
    ArrayList<String> columns = new ArrayList<String>();
    int i = token.indexOf("@");
    while (i != -1) {
      token = token.substring(i + 1);
      if (!token.startsWith("SQL")) {
        i = token.indexOf("@");
        if (i != -1) {
          String strAux = token.substring(0, i);
          token = token.substring(i + 1);
          if (!columns.contains(strAux)) {
            if (!strAux.equalsIgnoreCase(column) && possibleColumns.contains(strAux.toUpperCase())) {
              columns.add(strAux);
            }
          }
        }
      }
      i = token.indexOf("@");
    }
    return columns;
  }

  private Object computeAuxiliaryInput(AuxiliaryInput auxIn, String windowId) {
    try {
      String code = auxIn.getValidationCode();
      log.debug("Auxiliary Input: " + auxIn.getName() + " Code:" + code);
      String fvalue = null;
      if (code.startsWith("@SQL=")) {
        ArrayList<String> params = new ArrayList<String>();
        String sql = UIDefinition.parseSQL(code, params);
        // final StringBuffer parametros = new StringBuffer();
        // for (final Enumeration<String> e = params.elements(); e.hasMoreElements();) {
        // String paramsElement = WadUtility.getWhereParameter(e.nextElement(), true);
        // parametros.append("\n" + paramsElement);
        // }
        log.debug("Transformed SQL code: " + sql);
        int indP = 1;
        PreparedStatement ps = OBDal.getInstance().getConnection().prepareStatement(sql);
        for (String parameter : params) {
          String value = "";
          if (parameter.substring(0, 1).equals("#")) {
            value = Utility.getContext(new DalConnectionProvider(false), RequestContext.get()
                .getVariablesSecureApp(), parameter, windowId);
          } else {
            String fieldId = "inp" + Sqlc.TransformaNombreColumna(parameter);
            value = RequestContext.get().getRequestParameter(fieldId);
          }
          log.debug("Parameter: " + parameter + ": Value " + value);
          ps.setObject(indP++, value);
        }
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
          fvalue = rs.getString(1);
        }
      } else {
        fvalue = Utility.getContext(new DalConnectionProvider(false), RequestContext.get()
            .getVariablesSecureApp(), code, windowId);
      }
      return fvalue;
    } catch (Exception e) {
      log.error("Error while computing auxiliary input parameter: " + auxIn.getName()
          + " from tab: " + auxIn.getTab().getName(), e);
    }
    return null;
  }

}
