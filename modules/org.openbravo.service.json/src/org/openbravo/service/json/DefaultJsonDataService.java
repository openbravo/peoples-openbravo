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
 * All portions are Copyright (C) 2009-2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.json;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollableResults;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
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
 * The main usage of this class is through the {@link #getInstance()} method (as a singleton). This
 * class can however also be extended and instantiated directly.
 * 
 * There are several methods to override/implement update and insert hooks, see the pre* and post*
 * methods.
 * 
 * @author mtaal
 */
public class DefaultJsonDataService implements JsonDataService {
  private static final Logger log = Logger.getLogger(DefaultJsonDataService.class);

  private static final long serialVersionUID = 1L;

  private static final String ADD_FLAG = "_doingAdd";

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

      String selectedProperties = parameters.get(JsonConstants.SELECTEDPROPERTIES_PARAMETER);

      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();
      List<BaseOBObject> bobs;
      final String id = parameters.get(JsonConstants.ID);
      // if the id is set that's a special case of one object being requested
      if (id != null) {
        bobs = new ArrayList<BaseOBObject>();
        final BaseOBObject bob = OBDal.getInstance().get(entityName, id);
        if (bob != null) {
          bobs.add(bob);
        }
      } else {
        final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
        final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);

        boolean preventCountOperation = !parameters.containsKey(JsonConstants.NOCOUNT_PARAMETER)
            || "true".equals(parameters.get(JsonConstants.NOCOUNT_PARAMETER));

        DataEntityQueryService queryService = createSetQueryService(parameters, true);
        queryService.setEntityName(entityName);

        // only do the count if a paging request is done and it has not been prevented
        // explicitly
        boolean doCount = false;
        int count = -1;
        int startRow = (startRowStr != null ? queryService.getFirstResult() : 0);
        int computedMaxResults = (queryService.getMaxResults() == null ? Integer.MAX_VALUE
            : queryService.getMaxResults());
        if (startRowStr != null) {
          doCount = true;
        }
        if (endRowStr != null) {
          // note computedmaxresults must be set before
          // endRow is increased by 1
          // increase by 1 to see if there are more results.
          if (preventCountOperation) {
            // set count here, is corrected in specific cases later
            count = queryService.getMaxResults();
          }
        } else {
          // can't do count if there is no endrow...
          preventCountOperation = false;
        }

        if (doCount && !preventCountOperation) {
          count = queryService.count();
        }

        if (parameters.containsKey(JsonConstants.ONLYCOUNT_PARAMETER)) {
          // stop here
          jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, count);
          return jsonResponse.toString();
        }

        queryService = createSetQueryService(parameters, false);
        queryService.setEntityName(entityName);

        if (parameters.containsKey(JsonConstants.SUMMARY_PARAMETER)) {
          final JSONObject singleResult = new JSONObject();
          if (queryService.getSummaryFields().size() == 1) {
            singleResult.put(queryService.getSummaryFields().get(0), queryService.buildOBQuery()
                .createQuery().uniqueResult());
          } else {
            final Object[] os = (Object[]) queryService.buildOBQuery().createQuery().uniqueResult();
            int i = 0;
            if (os != null && os.length > 0) {
              for (String key : queryService.getSummaryFields()) {
                singleResult.put(key, os[i++]);
              }
            }
          }
          singleResult.put("isGridSummary", true);

          jsonResponse.put(JsonConstants.RESPONSE_DATA,
              new JSONArray(Collections.singleton(singleResult)));
          jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
          jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
          jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
          jsonResponse.put(JsonConstants.RESPONSE_ENDROW, 1);
          jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 1);
          return jsonResult.toString();
        } else if (parameters.containsKey(JsonConstants.DISTINCT_PARAMETER)) {
          // TODO: BaseOBObjects created by this query are not valid, see issue #23705, when this is
          // fixed, IdentifierProvider should be revisited to remove code handling this
          // incorrectness

          // when distinct an array of values is returned
          // the first value is the BaseObObject the other values
          // are part of the order by and such and can be ignored
          final String distinct = parameters.get(JsonConstants.DISTINCT_PARAMETER);
          final Property distinctProperty = DalUtil.getPropertyFromPath(ModelProvider.getInstance()
              .getEntity(entityName), distinct);
          final Entity distinctEntity = distinctProperty.getTargetEntity();

          final List<Property> properties = new ArrayList<Property>();
          properties.addAll(distinctEntity.getIdProperties());
          properties.addAll(queryService.getDistinctDisplayProperties());

          // filter the json serialization later on
          final StringBuilder selectedSb = new StringBuilder();
          for (Property prop : properties) {
            if (selectedSb.length() > 0) {
              selectedSb.append(",");
            }
            if (prop.getTargetEntity() != null) {
              // go one level deeper
              final List<Property> nextIdentifierProps = JsonUtils.getIdentifierSet(prop);
              for (Property nextIdentifierProp : nextIdentifierProps) {
                selectedSb.append(prop.getName() + "." + nextIdentifierProp);
              }
            } else {
              selectedSb.append(prop.getName());
            }
          }
          if (selectedProperties == null) {
            selectedProperties = selectedSb.toString();
          } else {
            selectedProperties += "," + selectedSb.toString();
          }

          bobs = new ArrayList<BaseOBObject>();

          List<List<Property>> cache = new ArrayList<List<Property>>();
          for (Object o : queryService.buildOBQuery().createQuery().list()) {
            final Object[] os = (Object[]) o;
            if (os[0] == null) {
              // the null value is also returned, ignore those
              continue;
            }

            if (cache.size() == 0) {
              for (int i = 0; i < os.length; i++) {
                cache.add(null);
              }
            }

            // create a BaseOBObject and fill the id/identifier properties
            final BaseOBObject bob = (BaseOBObject) OBProvider.getInstance().get(
                distinctEntity.getName());
            int i = 0;
            for (Property property : properties) {
              // the query contains the identifier and other properties for
              // one level deeper!
              if (property.getTargetEntity() != null) {
                final BaseOBObject refBob = (BaseOBObject) OBProvider.getInstance().get(
                    property.getTargetEntity().getName());
                final List<Property> nextIdentifierProps;
                if (cache.get(i) != null) {
                  nextIdentifierProps = cache.get(i);
                } else {
                  nextIdentifierProps = JsonUtils.getIdentifierSet(property);
                  cache.set(i, nextIdentifierProps);
                }
                for (Property nextIdentifierProp : nextIdentifierProps) {
                  refBob.setValue(nextIdentifierProp.getName(), os[i++]);
                }
                bob.setValue(property.getName(), refBob);
              } else {
                bob.setValue(property.getName(), os[i++]);
              }
            }
            bobs.add(bob);
          }
        } else {
          bobs = queryService.list();
        }

        bobs = bobFetchTransformation(bobs, parameters);

        if (preventCountOperation) {
          count = bobs.size() + startRow;
          // computedMaxResults is one too much, if we got one to much then correct
          // the result and up the count so that the grid knows that there are more
          if (bobs.size() == computedMaxResults) {
            bobs = bobs.subList(0, bobs.size() - 1);
            count++;
          }
        }

        jsonResponse.put(JsonConstants.RESPONSE_STARTROW, startRow);
        jsonResponse.put(JsonConstants.RESPONSE_ENDROW, (bobs.size() > 0 ? bobs.size() + startRow
            - 1 : 0));
        // bobs can be empty and count > 0 if the order by forces a join without results
        if (bobs.isEmpty()) {
          if (startRow > 0) {
            // reload the startrow again from 0
            parameters.put(JsonConstants.STARTROW_PARAMETER, "0");
            parameters.put(JsonConstants.ENDROW_PARAMETER, computedMaxResults + "");
            return fetch(parameters);
          }
          jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, 0);
        } else if (doCount) {
          jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, count);
        }
      }

      final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
          DataToJsonConverter.class);
      toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
      toJsonConverter.setSelectedProperties(selectedProperties);
      final List<JSONObject> jsonObjects = toJsonConverter.toJsonObjects(bobs);

      addWritableAttribute(jsonObjects);

      jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

      return doPostAction(parameters, jsonResult.toString(), DataSourceAction.FETCH, null);
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
      return JsonUtils.convertExceptionToJson(t);
    }
  }

  public void fetch(Map<String, String> parameters, QueryResultWriter writer) {
    long t = System.currentTimeMillis();
    final String entityName = parameters.get(JsonConstants.ENTITYNAME);
    final DataEntityQueryService queryService = createSetQueryService(parameters, false);
    queryService.setEntityName(entityName);

    final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
        DataToJsonConverter.class);
    toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));

    final ScrollableResults scrollableResults = queryService.scroll();
    int i = 0;
    while (scrollableResults.next()) {
      final Object result = scrollableResults.get()[0];
      final JSONObject json = toJsonConverter.toJsonObject((BaseOBObject) result,
          DataResolvingMode.FULL);

      try {
        doPostFetch(parameters, json);
      } catch (JSONException e) {
        throw new OBException(e);
      }

      writer.write(json);

      i++;
      // Clear session every 1000 records to prevent huge memory consumption in case of big loops
      if (i % 1000 == 0) {
        OBDal.getInstance().getSession().clear();
        log.debug("clearing in record " + i + " elapsed time " + (System.currentTimeMillis() - t));
      }
    }
    log.debug("Fetch took " + (System.currentTimeMillis() - t) + " ms");
  }

  protected DataEntityQueryService createSetQueryService(Map<String, String> parameters,
      boolean forCountOperation) {
    final String entityName = parameters.get(JsonConstants.ENTITYNAME);
    final String startRowStr = parameters.get(JsonConstants.STARTROW_PARAMETER);
    final String endRowStr = parameters.get(JsonConstants.ENDROW_PARAMETER);

    final DataEntityQueryService queryService = OBProvider.getInstance().get(
        DataEntityQueryService.class);
    queryService.setEntityName(entityName);

    if (parameters.containsKey(JsonConstants.USE_ALIAS)) {
      queryService.setUseAlias();
    }
    boolean directNavigation = parameters.containsKey("_directNavigation")
        && "true".equals(parameters.get("_directNavigation"))
        && parameters.containsKey(JsonConstants.TARGETRECORDID_PARAMETER);

    if (!directNavigation) {
      // set the where/org filter parameters and the @ parameters
      for (String key : parameters.keySet()) {
        if (key.equals(JsonConstants.WHERE_PARAMETER)
            || key.equals(JsonConstants.IDENTIFIER)
            || key.equals(JsonConstants.ORG_PARAMETER)
            || key.equals(JsonConstants.TARGETRECORDID_PARAMETER)
            || (key.startsWith(DataEntityQueryService.PARAM_DELIMITER) && key
                .endsWith(DataEntityQueryService.PARAM_DELIMITER))) {
          queryService.addFilterParameter(key, parameters.get(key));
        }

      }
    }
    queryService.setCriteria(JsonUtils.buildCriteria(parameters));

    if (parameters.get(JsonConstants.NO_ACTIVE_FILTER) != null
        && parameters.get(JsonConstants.NO_ACTIVE_FILTER).equals("true")) {
      queryService.setFilterOnActive(false);
    }

    if (parameters.containsKey(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE)) {
      queryService.setTextMatching(parameters.get(JsonConstants.TEXTMATCH_PARAMETER_OVERRIDE));
    } else {
      queryService.setTextMatching(parameters.get(JsonConstants.TEXTMATCH_PARAMETER));
    }

    // only do the count if a paging request is done
    // note preventCountOperation variable is considered further below
    int startRow = 0;
    int computedMaxResults = Integer.MAX_VALUE;
    if (startRowStr != null) {
      startRow = Integer.parseInt(startRowStr);
      queryService.setFirstResult(startRow);
    }

    if (endRowStr != null) {
      int endRow = Integer.parseInt(endRowStr);
      computedMaxResults = endRow - startRow + 1;
      queryService.setMaxResults(computedMaxResults);
    }

    final String sortBy = parameters.get(JsonConstants.SORTBY_PARAMETER);
    String orderBy = "";
    if (sortBy != null) {
      orderBy = sortBy;
    } else if (parameters.get(JsonConstants.ORDERBY_PARAMETER) != null) {
      orderBy = parameters.get(JsonConstants.ORDERBY_PARAMETER);
    }

    if (parameters.get(JsonConstants.SUMMARY_PARAMETER) != null
        && parameters.get(JsonConstants.SUMMARY_PARAMETER).trim().length() > 0) {
      queryService.setSummarySettings(parameters.get(JsonConstants.SUMMARY_PARAMETER));
    } else if (parameters.get(JsonConstants.DISTINCT_PARAMETER) != null
        && parameters.get(JsonConstants.DISTINCT_PARAMETER).trim().length() > 0) {
      queryService.setDistinct(parameters.get(JsonConstants.DISTINCT_PARAMETER).trim());
      // sortby the distinct's identifier
      orderBy = getOrderByForDistinct(entityName, queryService);
    } else {
      // Always append id to the orderby to make a predictable sorting
      orderBy += (orderBy.isEmpty() ? "" : ",") + "id";
    }

    queryService.setOrderBy(orderBy);

    // compute a new startrow if the targetrecordid was passed in
    int targetRowNumber = -1;
    if (!forCountOperation && !directNavigation
        && parameters.containsKey(JsonConstants.TARGETRECORDID_PARAMETER)) {
      final String targetRecordId = parameters.get(JsonConstants.TARGETRECORDID_PARAMETER);
      targetRowNumber = queryService.getRowNumber(targetRecordId);
      if (targetRowNumber != -1) {
        startRow = targetRowNumber;
        // if the startrow is really low, then just read from 0
        // to make sure that we have a full page of data to display
        if (startRow < (computedMaxResults / 2)) {
          startRow = 0;
        } else {
          startRow -= 20;
        }
        queryService.setFirstResult(startRow);
      }
      queryService.clearCachedValues();
    }
    if (!forCountOperation) {
      queryService.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
      // joining associated entities actually proved to be slower than doing
      // individual queries for them... so disabling this functionality for now
      // queryService.setJoinAssociatedEntities(true);
    }
    return queryService;
  }

  private String getOrderByForDistinct(String entityName, DataEntityQueryService queryService) {
    final String localDistinct = queryService.getDistinct();
    final List<Property> properties = queryService.getDistinctDisplayProperties();
    final StringBuilder sb = new StringBuilder();
    for (Property identifierProp : properties) {
      if (identifierProp.getTargetEntity() != null) {
        // go one level deeper
        final List<Property> nextIdentifierProps = JsonUtils.getIdentifierSet(identifierProp);
        for (Property nextIdentifierProp : nextIdentifierProps) {
          sb.append(localDistinct + DalUtil.DOT + identifierProp.getName() + "."
              + nextIdentifierProp + ",");
        }
      } else {
        sb.append(localDistinct + DalUtil.DOT + identifierProp.getName() + ",");
      }
    }
    sb.append(localDistinct + DalUtil.DOT + JsonConstants.ID);
    return sb.toString();
  }

  private void addWritableAttribute(List<JSONObject> jsonObjects) throws JSONException {
    for (JSONObject jsonObject : jsonObjects) {
      if (!jsonObject.has("client") || !jsonObject.has("organization")) {
        continue;
      }
      final Object rowClient = jsonObject.get("client");
      final Object rowOrganization = jsonObject.get("organization");
      if (!(rowClient instanceof String) || !(rowOrganization instanceof String)) {
        continue;
      }
      final String currentClientId = OBContext.getOBContext().getCurrentClient().getId();
      if (!rowClient.equals(currentClientId)) {
        jsonObject.put("_readOnly", true);
      } else {
        boolean writable = false;
        for (String orgId : OBContext.getOBContext().getWritableOrganizations()) {
          if (orgId.equals(rowOrganization)) {
            writable = true;
            break;
          }
        }
        if (!writable) {
          jsonObject.put("_readOnly", true);
        }
      }
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
    BaseOBObject bob = OBDal.getInstance().get(entityName, id);
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
        OBDal.getInstance().commitAndClose();

        doPreRemove(parameters, jsonObjects.get(0));

        // now do the real delete in a separate transaction
        // to prevent side effects that a child can not be deleted
        // from its parent
        // https://issues.openbravo.com/view.php?id=21229
        bob = OBDal.getInstance().get(entityName, id);
        OBDal.getInstance().remove(bob);

        final String result = doPostAction(parameters, jsonResult.toString(),
            DataSourceAction.REMOVE, null);

        OBDal.getInstance().commitAndClose();

        return result;
      } catch (Throwable t) {
        return JsonUtils.convertExceptionToJson(t);
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
    parameters.put(ADD_FLAG, "true");
    return update(parameters, content);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.service.json.JsonDataService#update(java.util.Map, java.lang.String)
   */
  public String update(Map<String, String> parameters, String content) {
    try {
      final boolean sendOriginalIdBack = "true".equals(parameters
          .get(JsonConstants.SEND_ORIGINAL_ID_BACK));

      final JsonToDataConverter fromJsonConverter = OBProvider.getInstance().get(
          JsonToDataConverter.class);

      String localContent = content;
      if (parameters.containsKey(ADD_FLAG)) {
        localContent = doPreAction(parameters, content, DataSourceAction.ADD);
      } else {
        localContent = doPreAction(parameters, content, DataSourceAction.UPDATE);
      }

      final Object jsonContent = getContentAsJSON(localContent);
      final List<BaseOBObject> bobs;
      final List<JSONObject> originalData = new ArrayList<JSONObject>();
      if (jsonContent instanceof JSONArray) {
        bobs = fromJsonConverter.toBaseOBObjects((JSONArray) jsonContent);
        final JSONArray jsonArray = (JSONArray) jsonContent;
        for (int i = 0; i < jsonArray.length(); i++) {
          originalData.add(jsonArray.getJSONObject(i));
        }
      } else {
        final JSONObject jsonObject = (JSONObject) jsonContent;
        originalData.add(jsonObject);
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
        OBDal.getInstance().rollbackAndClose();
        // report the errors
        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS,
            JsonConstants.RPCREQUEST_STATUS_VALIDATION_ERROR);
        final JSONObject errorsObject = new JSONObject();
        for (JsonConversionError error : fromJsonConverter.getErrors()) {
          errorsObject.put(error.getProperty().getName(), error.getThrowable().getMessage());
        }
        jsonResponse.put(JsonConstants.RESPONSE_ERRORS, errorsObject);
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);
        return jsonResult.toString();
      } else {
        for (BaseOBObject bob : bobs) {
          OBDal.getInstance().save(bob);
        }
        OBDal.getInstance().flush();

        // business event handlers can change the data
        // flush again before refreshing, refreshing can
        // potentially remove any in-memory changes
        int countFlushes = 0;
        while (OBDal.getInstance().getSession().isDirty()) {
          OBDal.getInstance().flush();
          countFlushes++;
          // arbitrary point to give up...
          if (countFlushes > 100) {
            throw new OBException("Infinite loop in flushing when persisting json: " + content);
          }
        }

        // refresh the objects from the db as they can have changed
        for (BaseOBObject bob : bobs) {
          OBDal.getInstance().getSession().refresh(bob);
        }

        // almost successfull, now create the response
        // needs to be done before the close of the session
        final DataToJsonConverter toJsonConverter = OBProvider.getInstance().get(
            DataToJsonConverter.class);
        toJsonConverter.setAdditionalProperties(JsonUtils.getAdditionalProperties(parameters));
        final List<JSONObject> jsonObjects = toJsonConverter.toJsonObjects(bobs);

        if (sendOriginalIdBack) {
          // now it is assumed that the jsonObjects are the same size and the same location
          // in the array
          if (jsonObjects.size() != originalData.size()) {
            throw new OBException("Unequal sizes in json data processed " + jsonObjects.size()
                + " " + originalData.size());
          }

          // now add the old id back
          for (int i = 0; i < originalData.size(); i++) {
            final JSONObject original = originalData.get(i);
            final JSONObject ret = jsonObjects.get(i);
            if (original.has(JsonConstants.ID) && original.has(JsonConstants.NEW_INDICATOR)) {
              ret.put(JsonConstants.ORIGINAL_ID, original.get(JsonConstants.ID));
            }
          }
        }

        final JSONObject jsonResult = new JSONObject();
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
        jsonResponse.put(JsonConstants.RESPONSE_DATA, new JSONArray(jsonObjects));
        jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

        final String result;
        if (parameters.containsKey(ADD_FLAG)) {
          result = doPostAction(parameters, jsonResult.toString(), DataSourceAction.ADD, content);
        } else {
          result = doPostAction(parameters, jsonResult.toString(), DataSourceAction.UPDATE, content);
        }

        OBDal.getInstance().commitAndClose();

        return result;
      }
    } catch (Throwable t) {
      Throwable localThrowable = t;
      if (localThrowable.getCause() instanceof BatchUpdateException) {
        final BatchUpdateException batchException = (BatchUpdateException) localThrowable
            .getCause();
        localThrowable = batchException.getNextException();
      }
      log.error(localThrowable.getMessage(), localThrowable);
      return JsonUtils.convertExceptionToJson(localThrowable);
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

  public static abstract class QueryResultWriter {
    public abstract void write(JSONObject json);
  }

  protected List<BaseOBObject> bobFetchTransformation(List<BaseOBObject> bobs,
      Map<String, String> parameters) {
    // If is override, take into account:
    // * If the number of the returned bobs change, there could be problems because endRow and
    // totalRows parameters will be out-of-sync with that the requester expects, and some values can
    // be missing in the following fetches. If there is no pagination (all values are returned at
    // once), there is no problem.
    // * If any bob is modified, the original entity is being modified, so a good practice could be
    // clone the bob (using DalUtil.copy, for example) before modify it, and then return the clone.

    return bobs;
  }

  protected String doPreAction(Map<String, String> parameters, String content,
      DataSourceAction action) {
    try {
      final Object contentObject = getContentAsJSON(content);
      final boolean isArray = contentObject instanceof JSONArray;
      final JSONArray data;
      if (isArray) {
        data = (JSONArray) contentObject;
      } else {
        final JSONObject request = new JSONObject(content);
        data = new JSONArray(Collections.singleton(request.getJSONObject(JsonConstants.DATA)));
      }

      final JSONArray newData = new JSONArray();
      for (int i = 0; i < data.length(); i++) {
        final JSONObject dataElement = data.getJSONObject(i);

        // do the pre thing
        switch (action) {
        case UPDATE:
          doPreUpdate(parameters, dataElement);
          break;
        case ADD:
          doPreInsert(parameters, dataElement);
          break;
        case REMOVE:
          doPreRemove(parameters, dataElement);
          break;
        default:
          throw new OBException("Unsupported action " + action);
        }

        // and set it in the new array
        newData.put(dataElement);
      }

      // return the array directly
      if (isArray) {
        return newData.toString();
      }

      final JSONObject request = new JSONObject(content);
      request.put(JsonConstants.DATA, newData.getJSONObject(0));
      return request.toString();

    } catch (JSONException e) {
      throw new OBException(e);
    } finally {
      OBDal.getInstance().flush();
    }
  }

  protected String doPostAction(Map<String, String> parameters, String content,
      DataSourceAction action, String originalObject) {

    OBDal.getInstance().flush();

    try {
      // this gets the data before the insert, so that it can be used
      // for preprocessing, for example inserting an order
      final JSONObject json = new JSONObject(content);
      final JSONObject response = json.getJSONObject(JsonConstants.RESPONSE_RESPONSE);
      final JSONArray data = response.getJSONArray(JsonConstants.RESPONSE_DATA);
      final JSONArray newData = new JSONArray();
      for (int i = 0; i < data.length(); i++) {
        final JSONObject dataElement = data.getJSONObject(i);

        // do the pre thing
        switch (action) {
        case FETCH:
          doPostFetch(parameters, dataElement);
          break;
        case UPDATE:
          doPostUpdate(parameters, dataElement, originalObject);
          break;
        case ADD:
          doPostInsert(parameters, dataElement, originalObject);
          break;
        case REMOVE:
          doPostRemove(parameters, dataElement);
          break;
        default:
          throw new OBException("Unsupported action " + action);
        }

        // and set it in the new array
        newData.put(dataElement);
      }
      // update the response with the changes, make it a string
      response.put(JsonConstants.RESPONSE_DATA, newData);
      json.put(JsonConstants.RESPONSE_RESPONSE, response);
      return json.toString();
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  /**
   * Is called before the actual remove of the object. The toRemove object contains the id and
   * entity name of the to-be-deleted object.
   */
  protected void doPreRemove(Map<String, String> parameters, JSONObject toRemove)
      throws JSONException {

  }

  /**
   * Is called after the remove in the database but before the commit. The removed parameter object
   * can be changed, the changes are sent to the client. This method is called in the same
   * transaction as the remove action.
   */
  protected void doPostRemove(Map<String, String> parameters, JSONObject removed)
      throws JSONException {

  }

  /**
   * Is called after fetching an object before the result is sent to the client, the fetched
   * {@link JSONObject} can be changed. The changes are sent to the client. This method is called in
   * the same transaction as the main fetch operation.
   */
  protected void doPostFetch(Map<String, String> parameters, JSONObject fetched)
      throws JSONException {

  }

  /**
   * Is called before an object is inserted. The toInsert {@link JSONObject} can be changed, the
   * changes are persisted to the database. This method is called in the same transaction as the
   * insert.
   */
  protected void doPreInsert(Map<String, String> parameters, JSONObject toInsert)
      throws JSONException {

  }

  /**
   * Is called after the insert action in the same transaction as the insert. The inserted
   * {@link JSONObject} can be changed, the changes are sent to the client.
   * 
   * The originalToInsert contains the json object/array string as it was passed into the
   * doPreInsert method. The inserted JSONObject is the object read from the database after it was
   * inserted. So it contains the changes done by stored procedures.
   */
  protected void doPostInsert(Map<String, String> parameters, JSONObject inserted,
      String originalToInsert) throws JSONException {
    // final String id = inserted.getString(JsonConstants.ID);
    // final String entityName = inserted.getString(JsonConstants.ENTITYNAME);

  }

  /**
   * Called before the update of an object. Is called in the same transaction as the main update
   * operation. Changes to the toUpdate {@link JSONObject} are persisted in the database.
   */
  protected void doPreUpdate(Map<String, String> parameters, JSONObject toUpdate)
      throws JSONException {
  }

  /**
   * Called after the updates have been done, within the same transaction as the main update.
   * Changes to the updated {@link JSONObject} are sent to the client (but not persisted to the
   * database).
   * 
   * The originalToUpdate contains the json object/array string as it was passed into the
   * doPreUpdate method. The updated JSONObject is the object read from the database after it was
   * updated. So it contains all the changes done by stored procedures.
   */
  protected void doPostUpdate(Map<String, String> parameters, JSONObject updated,
      String originalToUpdate) throws JSONException {
  }

  protected enum DataSourceAction {
    FETCH, ADD, UPDATE, REMOVE
  }
}
