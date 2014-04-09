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
 * All portions are Copyright (C) 2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;

/**
 * The implementation of the combo table reference datasource.
 * 
 * 
 * @author Shankar Balachandran
 */
public class ComboTableDatasourceService extends BaseDataSourceService {
  private static final long serialVersionUID = 1L;
  private static final Logger log4j = Logger.getLogger(ComboTableDatasourceService.class);

  /**
   * Retrieve tabledir and table reference values.
   */
  @Override
  public String fetch(Map<String, String> parameters) {
    List<BaseOBObject> bobs = null;
    try {
      OBContext.setAdminMode();
      String fieldId = parameters.get("fieldId");
      String startRow = parameters.get("_startRow");
      String endRow = parameters.get("_endRow");
      Field field = OBDal.getInstance().get(Field.class, fieldId);
      Boolean getValueFromSession = Boolean.getBoolean(parameters.get("getValueFromSession"));
      String columnValue = parameters.get("columnValue");
      RequestContext rq = RequestContext.get();
      VariablesSecureApp vars = rq.getVariablesSecureApp();
      boolean comboreload = rq.getRequestParameter("donotaddcurrentelement") != null
          && rq.getRequestParameter("donotaddcurrentelement").equals("true");
      String ref = field.getColumn().getReference().getId();
      String objectReference = "";
      if (field.getColumn().getReferenceSearchKey() != null) {
        objectReference = field.getColumn().getReferenceSearchKey().getId();
      }
      String validation = "";
      if (field.getColumn().getValidation() != null) {
        validation = field.getColumn().getValidation().getId();
      }

      String orgList = Utility.getReferenceableOrg(vars, vars.getStringParameter("inpadOrgId"));
      String clientList = Utility.getContext(new DalConnectionProvider(false), vars,
          "#User_Client", field.getTab().getWindow().getId());
      if (field.getColumn().getDBColumnName().equalsIgnoreCase("AD_CLIENT_ID")) {
        clientList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Client",
            field.getTab().getWindow().getId(),
            Integer.parseInt(field.getTab().getTable().getDataAccessLevel()));
        clientList = vars.getSessionValue("#User_Client");
        orgList = null;
      }
      if (field.getColumn().getDBColumnName().equalsIgnoreCase("AD_ORG_ID")) {
        orgList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Org", field
            .getTab().getWindow().getId(),
            Integer.parseInt(field.getTab().getTable().getDataAccessLevel()));
      }

      ApplicationDictionaryCachedStructures cachedStructures = WeldUtils
          .getInstanceFromStaticBeanManager(ApplicationDictionaryCachedStructures.class);
      ComboTableData comboTableData = cachedStructures.getComboTableData(vars, ref, field
          .getColumn().getDBColumnName(), objectReference, validation, orgList, clientList);
      Map<String, String> newParameters = null;

      FieldProvider tabData = UIDefinition.generateTabData(field.getTab().getADFieldList(), field,
          columnValue);
      newParameters = comboTableData.fillSQLParametersIntoMap(new DalConnectionProvider(false),
          vars, tabData, field.getTab().getWindow().getId(),
          (getValueFromSession && !comboreload) ? columnValue : "");

      FieldProvider[] fps = comboTableData.select(new DalConnectionProvider(false), newParameters,
          getValueFromSession && !comboreload, startRow, endRow);
      ArrayList<FieldProvider> values = new ArrayList<FieldProvider>();
      values.addAll(Arrays.asList(fps));
      ArrayList<JSONObject> comboEntries = new ArrayList<JSONObject>();
      ArrayList<String> possibleIds = new ArrayList<String>();
      // If column is mandatory we add an initial blank value
      if (!field.getColumn().isMandatory()) {
        possibleIds.add("");
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, (String) null);
        entry.put(JsonConstants.IDENTIFIER, (String) null);
        comboEntries.add(entry);
      }
      for (FieldProvider fp : values) {
        possibleIds.add(fp.getField("ID"));
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, fp.getField("ID"));
        entry.put(JsonConstants.IDENTIFIER, fp.getField("NAME"));
        comboEntries.add(entry);
      }
      JSONObject fieldProps = new JSONObject();
      if (getValueFromSession && !comboreload) {
        fieldProps.put("value", columnValue);
        fieldProps.put("classicValue", columnValue);
      } else {
        if (possibleIds.contains(columnValue)) {
          fieldProps.put("value", columnValue);
          fieldProps.put("classicValue", columnValue);
        } else {
          // In case the default value doesn't exist in the combo values, we choose the first one
          if (comboEntries.size() > 0) {
            if (comboEntries.get(0).has(JsonConstants.ID)) {
              fieldProps.put("value", comboEntries.get(0).get(JsonConstants.ID));
              fieldProps.put("classicValue", comboEntries.get(0).get(JsonConstants.ID));
            } else {
              fieldProps.put("value", (String) null);
              fieldProps.put("classicValue", (String) null);
            }
          } else {
            fieldProps.put("value", "");
            fieldProps.put("classicValue", "");
          }
        }
      }
      fieldProps.put("entries", new JSONArray(comboEntries));
      // comboValues.put(fieldIndex, values);
      // columnValues.put(fieldIndex, fixComboValue(columnValues.get(fieldIndex), fps));
      return fieldProps.toString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return null;
  }

  @Override
  public String remove(Map<String, String> parameters) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String add(Map<String, String> parameters, String content) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String update(Map<String, String> parameters, String content) {
    // TODO Auto-generated method stub
    return null;
  }

}