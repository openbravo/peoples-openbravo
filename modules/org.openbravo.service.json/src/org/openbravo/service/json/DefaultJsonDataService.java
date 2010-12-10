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
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonToDataConverter.JsonConversionError;

/**
 * Implements generic data operations which have parameters and json as an input and return results
 * as json strings.
 * 
 * Note the parameters, json input and generated json follow the Smartclient specs. See the
 * Smartclient <a href="http://www.smartclient.com/docs/7.0rc2/a/b/c/go.html#class..RestDataSource">
 * RestDataSource</a> for more information.
 * 
 * This is a singleton class.
 * 
 * @author mtaal
 */
public class DefaultJsonDataService implements JsonDataService {
  private static final Logger log = Logger.getLogger(DefaultJsonDataService.class);

  private static final long serialVersionUID = 1L;

  private static DefaultJsonDataService instance = new DefaultJsonDataService();

  public static DefaultJsonDataService getInstance() {
    return instance;
  }

  public static void setInstance(DefaultJsonDataService instance) {
    DefaultJsonDataService.instance = instance;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.json.JsonDataService#fetch(java.util.Map)
   */
  public String fetch(Map<String, String> parameters) {
    try {
      final String entityName = parameters.get(JsonConstants.ENTITYNAME);
      Check.isNotNull(entityName, "The name of the service/entityname should not be null");
      Check.isNotNull(parameters, "The parameters should not be null");

      final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
      final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);

      final DataEntityQueryService queryService = OBProvider.getInstance().get(
          DataEntityQueryService.class);
      queryService.setEntityName(entityName);

      if (parameters.containsKey(JsonConstants.USE_ALIAS)) {
        queryService.setUseAlias();
      }
      // set the filter parameters
      for (String key : parameters.keySet()) {
        if (!key.startsWith("_")) {
          queryService.addFilterParameter(key, parameters.get(key));
        } else if (key.equals(JsonConstants.WHERE_PARAMETER)
            || key.equals(JsonConstants.IDENTIFIER) || key.equals(JsonConstants.ORG_PARAMETER)) {
          // the _where is used in a special way
          queryService.addFilterParameter(key, parameters.get(key));
        }
      }

      if (parameters.get(JsonConstants.OR_EXPRESSION_PARAMETER) != null) {
        queryService.setDoOrExpression();
      }

      if (parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE)) {
        queryService.setTextMatching(parameters.get(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE));
      } else {
        queryService.setTextMatching(parameters.get(JsonConstants.TEXTMATCH_PARAMETER));
      }

      boolean preventCountOperation = !parameters.containsKey(JsonConstants.NOCOUNT_PARAMETER)
          || "true".equals(parameters.get(JsonConstants.NOCOUNT_PARAMETER));

      // only do the count if a paging request is done
      // note preventCountOperation variable is considered further below
      boolean doCount = false;
      int count = -1;
      int startRow = 0;
      int computedMaxResults = Integer.MAX_VALUE;
      if (startRowStr != null) {
        startRow = Integer.parseInt(startRowStr);
        queryService.setFirstResult(startRow);
        doCount = true;
      }

      if (endRowStr != null && endRowStr != null) {
        int endRow = Integer.parseInt(endRowStr);
        computedMaxResults = endRow - startRow + 1;
        // note computedmaxresults must be set before
        // endRow is increased by 1
        // increase by 1 to see if there are more results.
        if (preventCountOperation) {
          endRow++;
          // set count here, is corrected in specific cases later
          count = endRow;
        }
        queryService.setMaxResults(computedMaxResults);
        doCount = true;
      } else {
        // can't do this if there is no endrow...
        preventCountOperation = false;
      }

      final String sortBy = parameters.get(JsonConstants.SORTBY_PARAMETER);
      if (sortBy != null) {
        queryService.setOrderBy(sortBy);
      } else if (parameters.get(JsonConstants.ORDERBY_PARAMETER) != null) {
        queryService.setOrderBy(parameters.get(JsonConstants.ORDERBY_PARAMETER));
      }

      // compute a new startrow if the targetrecordid was passed in
      int targetRowNumber = -1;
      if (parameters.containsKey(JsonConstants.TARGETRECORDID_PARAMETER)) {
        final String targetRecordId = parameters.get(JsonConstants.TARGETRECORDID_PARAMETER);
        targetRowNumber = queryService.getRowNumber(targetRecordId);
        if (targetRowNumber != -1) {
          queryService.setFirstResult(targetRowNumber);
          startRow = targetRowNumber;
        }
      }

      List<BaseOBObject> bobs = queryService.list();

      if (preventCountOperation) {
        // computedMaxResults is one too much, if we got one to much then correct
        // the result, the count is already correct
        if (bobs.size() == computedMaxResults) {
          bobs = bobs.subList(0, bobs.size() - 1);
        } else {
          // got less so correct the count
          count = bobs.size() + startRow;
        }
      }

      final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
          DataToJsonConverter.class);
      toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
      final List<JSONObject> jsonObjects = toJsonConverter.toJsonObjects(bobs);

      if (doCount && !preventCountOperation) {
        count = queryService.count();
      }

      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      if (targetRowNumber != -1) {
        jsonResponse.put(JsonConstants.RESPONSE_SCROLLTO, targetRowNumber);
      }
      jsonResponse.put(JsonConstants.RESPONSE_STARTROWS, startRow);
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, (bobs.size() > 0 ? bobs.size() + startRow - 1
          : 0));
      // bobs can be empty and count > 0 if the order by forces a join without results
      if (bobs.isEmpty()) {
        jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
      } else if (doCount) {
        jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, count);
      }
      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

      // if (jsonObjects.size() > 0) {
      // System.err.println(jsonObjects.get(0));
      // }
      return jsonResult.toString();

    } catch (Throwable t) {
      log.error(t.getMessage(), t);
      return JsonUtils.convertExceptionToJson(t);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.json.JsonDataService#remove(java.util.Map)
   */
  public String remove(Map<String, String> parameters) {
    final String id = parameters.get(JsonConstants.ID);
    if (id == null) {
      return JsonUtils.convertExceptionToJson(new IllegalStateException("No id parameter"));
    }
    final String entityName = parameters.get(JsonConstants.ENTITYNAME);
    if (entityName == null) {
      return JsonUtils.convertExceptionToJson(new IllegalStateException("No entityName parameter"));
    }
    final BaseOBObject bob = OBDal.getInstance().get(entityName, id);
    if (bob != null) {

      try {
        // create the result info before deleting to prevent Hibernate errors

        final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
            DataToJsonConverter.class);
        final List<JSONObject> jsonObjects = toJsonConverter.toJsonObjects(Collections
            .singletonList(bob));
        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
        jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

        OBDal.getInstance().remove(bob);
        OBDal.getInstance().commitAndClose();

        return jsonResult.toString();
      } catch (JSONException e) {
        return JsonUtils.convertExceptionToJson(e);
      }
    } else {
      return JsonUtils.convertExceptionToJson(new IllegalStateException("Object not found"));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.json.JsonDataService#add(java.util.Map, java.lang.String)
   */
  public String add(Map<String, String> parameters, String content) {
    return update(parameters, content);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.json.JsonDataService#update(java.util.Map, java.lang.String)
   */
  public String update(Map<String, String> parameters, String content) {
    try {
      final JsonToDataConverter fromJsonConverter = OBProvider.getInstance().get(
          JsonToDataConverter.class);

      final Object jsonContent = getContentAsJSON(content);
      final List<BaseOBObject> bobs;
      if (jsonContent instanceof JSONArray) {
        bobs = fromJsonConverter.toBaseOBObjects((JSONArray) jsonContent);
      } else {
        final JSONObject jsonObject = (JSONObject) jsonContent;
        // now set the id and entityname from the parameters if it was set
        if (!jsonObject.has(JsonConstants.ID) && parameters.containsKey(JsonConstants.ID)) {
          jsonObject.put(JsonConstants.ID, parameters.containsKey(JsonConstants.ID));
        }
        if (!jsonObject.has(JsonConstants.ENTITYNAME)
            && parameters.containsKey(JsonConstants.ENTITYNAME)) {
          jsonObject.put(JsonConstants.ENTITYNAME, parameters.get(JsonConstants.ENTITYNAME));
        }

        bobs = Collections
            .singletonList(fromJsonConverter.toBaseOBObject((JSONObject) jsonContent));
      }

      if (fromJsonConverter.hasErrors()) {
        // report the errors
        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS,
            JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR);
        final JSONObject errorsObject = new JSONObject();
        for (JsonConversionError error : fromJsonConverter.getErrors()) {
          final JSONObject errorMessageObject = new JSONObject();
          errorMessageObject.put(JsonConstants.RESPONSE_ERRORMESSAGE, error.getThrowable()
              .getMessage());
          if (error.getProperty().isPrimitive()) {
            errorsObject.put(error.getProperty().getName(), errorMessageObject);
          } else {
            errorsObject.put(error.getProperty().getName() + "." + JsonConstants.ID,
                errorMessageObject);
          }
        }
        jsonResponse.put(JsonConstants.RESPONSE_ERRORS, errorsObject);
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
        return jsonResult.toString();
      } else {
        for (BaseOBObject bob : bobs) {
          OBDal.getInstance().save(bob);
        }
        OBDal.getInstance().flush();

        // almost successfull, now create the response
        // needs to be done before the close of the session
        final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
            DataToJsonConverter.class);
        final List<JSONObject> jsonObjects = toJsonConverter.toJsonObjects(bobs);

        OBDal.getInstance().commitAndClose();

        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
        jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
        return jsonResult.toString();
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return JsonUtils.convertExceptionToJson(e);
    }

  }

  private Object getContentAsJSON(String content) throws JSONException {
    Check.isNotNull(content, "Content must be set");
    final Object jsonRepresentation;
    if (content.trim().startsWith("[")) {
      jsonRepresentation = new JSONArray(content);
    } else {
      final JSONObject jsonObject = new JSONObject(content);
      jsonRepresentation = jsonObject.get(JsonConstants.DATA);
    }
    return jsonRepresentation;
  }
}
