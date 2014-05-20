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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.window.ApplicationDictionaryCachedStructures;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.service.json.JsonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the combo table reference datasource.
 * 
 * @author Shankar Balachandran
 */

public class ComboTableDatasourceService extends BaseDataSourceService {
  private static final Logger log = LoggerFactory.getLogger(ComboTableDatasourceService.class);

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.datasource.DataSourceService#fetch(java.util.Map)
   */
  @Override
  public String fetch(Map<String, String> parameters) {
    long init = System.currentTimeMillis();
    Field field = null;
    Column column = null;
    FieldProvider[] fps = null;
    String fieldId = parameters.get("fieldId");
    String windowId = parameters.get("windowId");
    Entity targetEntity = null;
    String filterString = null;

    int startRow = -1, endRow = -1;
    try {
      field = OBDal.getInstance().get(Field.class, fieldId);
      column = field.getColumn();
      targetEntity = ModelProvider.getInstance().getEntityByTableId(
          (String) DalUtil.getId(column.getTable()));
      OBContext.getOBContext().getEntityAccessChecker().checkReadable(targetEntity);
    } catch (Exception e1) {
      throw new OBException(e1);
    }
    OBContext.setAdminMode();
    try {
      if (!StringUtils.isEmpty(parameters.get("criteria"))) {
        String criteria = parameters.get("criteria");
        for (String criterion : criteria.split(JsonConstants.IN_PARAMETER_SEPARATOR)) {
          try {
            JSONObject jsonCriterion = new JSONObject(criterion);
            if (jsonCriterion.getString("fieldName").equals(JsonConstants.IDENTIFIER)) {
              filterString = jsonCriterion.getString("value");
            }
          } catch (JSONException e) {
            log.error("Error getting criteria for ComboTableDataSoruce - field: " + fieldId
                + " - criteria: " + criteria, e);
          }
        }
      }

      if (parameters.get(JsonConstants.STARTROW_PARAMETER) != null) {
        startRow = Integer.parseInt(parameters.get(JsonConstants.STARTROW_PARAMETER));
      }
      if (parameters.get(JsonConstants.ENDROW_PARAMETER) != null) {
        endRow = Integer.parseInt(parameters.get(JsonConstants.ENDROW_PARAMETER));
      }
      boolean applyLimits = startRow != -1 && endRow != -1;
      if (!applyLimits) {
        throw new OBException(JsonConstants.STARTROW_PARAMETER + " and "
            + JsonConstants.ENDROW_PARAMETER + " not present");
      } else {
        if (endRow - startRow > 500) {
          throw new OBException("trying to retrieve more than 500 records");
        }
      }

      String columnValue = parameters.get("columnValue");
      RequestContext rq = RequestContext.get();
      VariablesSecureApp vars = rq.getVariablesSecureApp();
      String ref = (String) DalUtil.getId(column.getReference());
      String objectReference = "";

      if (column.getReferenceSearchKey() != null) {
        objectReference = (String) DalUtil.getId(column.getReferenceSearchKey());
      }
      String validation = "";
      if (column.getValidation() != null) {
        validation = (String) DalUtil.getId(column.getValidation());
      }

      String orgList = Utility.getReferenceableOrg(vars, vars.getStringParameter("inpadOrgId"));
      String clientList = Utility.getContext(new DalConnectionProvider(false), vars,
          "#User_Client", windowId);
      int accessLevel = targetEntity.getAccessLevel().getDbValue();
      if (column.getDBColumnName().equalsIgnoreCase("AD_CLIENT_ID")) {
        clientList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Client",
            windowId, accessLevel);
        clientList = vars.getSessionValue("#User_Client");
        orgList = null;
      }

      if (column.getDBColumnName().equalsIgnoreCase("AD_ORG_ID")) {
        orgList = Utility.getContext(new DalConnectionProvider(false), vars, "#User_Org", windowId,
            accessLevel);
      }

      ApplicationDictionaryCachedStructures cachedStructures = WeldUtils
          .getInstanceFromStaticBeanManager(ApplicationDictionaryCachedStructures.class);
      ComboTableData comboTableData = cachedStructures.getComboTableData(vars, ref,
          column.getDBColumnName(), objectReference, validation, orgList, clientList);
      Map<String, String> newParameters = null;
      if (StringUtils.isNotEmpty(filterString)) {
        columnValue = filterString;
      }

      newParameters = comboTableData.fillSQLParametersIntoMap(new DalConnectionProvider(false),
          vars, new FieldProviderFactory(parameters), windowId, columnValue);

      if (parameters.get("_currentValue") != null) {
        newParameters.put("@ACTUAL_VALUE@", parameters.get("_currentValue"));
      }

      if (!StringUtils.isEmpty(filterString)) {
        newParameters.put("FILTER_VALUE", filterString);
      }
      fps = comboTableData.select(new DalConnectionProvider(false), newParameters, true, startRow,
          endRow);

      ArrayList<JSONObject> comboEntries = new ArrayList<JSONObject>();
      ArrayList<String> possibleIds = new ArrayList<String>();
      // If column is mandatory we add an initial blank value in the first page if not filtered
      if (!column.isMandatory() && startRow == 0 && StringUtils.isEmpty(filterString)) {
        possibleIds.add("");
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, (String) null);
        entry.put(JsonConstants.IDENTIFIER, (String) null);
        comboEntries.add(entry);
      }
      int maxRows = endRow - startRow;

      boolean hasMoreRows = false;
      for (FieldProvider fp : fps) {
        if (comboEntries.size() > maxRows && applyLimits) {
          hasMoreRows = true;
          break;
        }
        possibleIds.add(fp.getField("ID"));
        JSONObject entry = new JSONObject();
        entry.put(JsonConstants.ID, fp.getField("ID"));
        entry.put(JsonConstants.IDENTIFIER, fp.getField("NAME"));
        comboEntries.add(entry);
      }

      // now jsonfy the data
      try {
        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
        jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
        jsonResponse.put(JsonConstants.RESPONSE_ENDROW, comboEntries.size() + startRow - 1);

        jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, comboEntries.size() + startRow
            + (hasMoreRows ? 1 : 0));

        jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(comboEntries));
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

        return jsonResult.toString();
      } catch (JSONException e) {
        throw new OBException(e);
      }
    } catch (Exception e) {
      log.error("Error in DS for combos - Field: " + fieldId + " - Filter: " + filterString, e);
      throw new OBException(e);
    } finally {
      log.debug("fetch ComboTableDatasourceService took: {} ms. Field: {}, filter: {}",
          new Object[] { (System.currentTimeMillis() - init), fieldId, filterString });
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public String remove(Map<String, String> parameters) {
    throw new OBException("Method not implemented");
  }

  @Override
  public String add(Map<String, String> parameters, String content) {
    throw new OBException("Method not implemented");
  }

  @Override
  public String update(Map<String, String> parameters, String content) {
    throw new OBException("Method not implemented");
  }
}