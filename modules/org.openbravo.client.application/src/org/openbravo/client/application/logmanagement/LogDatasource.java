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
 * All portions are Copyright (C) 2018 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application.logmanagement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.service.datasource.DataSourceProperty;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;
import org.openbravo.service.json.JsonUtils;

public class LogDatasource extends ReadOnlyDataSourceService {
  private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();

  private static final String LOG_LEVEL_LIST_REFERENCE_ID = "CF8CB8C4E798423081CE42078CA6BD7C";
  private static final String STRING_REFERENCE_ID = "10";

  @Override
  protected int getCount(Map<String, String> parameters) {
    return (int) getFilteredStream(parameters).count();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {

    Stream<Logger> filteredLoggers = getFilteredStream(parameters);
    filteredLoggers = sortStream(filteredLoggers, parameters);

    return filteredLoggers //
        .skip(startRow) //
        .limit(endRow + 1) //
        .map(l -> {
          Map<String, Object> r = new HashMap<>(2);
          r.put("id", l.getName());
          r.put("logger", l.getName());
          r.put("level", l.getLevel().toString());
          return r;
        }) //
        .collect(Collectors.toList());
  }

  private Stream<Logger> sortStream(Stream<Logger> loggerStream, Map<String, String> parameters) {
    if (parameters.containsKey("_sortBy")) {
      String sortKey = parameters.get("_sortBy");
      boolean reversed = false;
      if (sortKey.startsWith("-")) {
        sortKey = sortKey.substring(1);
        reversed = true;
      }

      if (getComparatorForSortKey(sortKey).isPresent()) {
        Comparator<Logger> comparator = getComparatorForSortKey(sortKey).get();
        if (reversed) {
          return loggerStream.sorted(comparator.reversed());
        }

        return loggerStream.sorted(comparator);
      }
    }

    return loggerStream.sorted(getComparatorForSortKey("logger").get());
  }

  private Optional<Comparator<Logger>> getComparatorForSortKey(String sortKey) {
    switch (sortKey) {
    case "logger":
      return Optional.of(Comparator.comparing(AbstractLogger::getName));
    case "level":
      return Optional.of(Comparator.comparing(Logger::getLevel));
    default:
      return Optional.empty();
    }
  }

  private Stream<Logger> getFilteredStream(Map<String, String> parameters) {
    Optional<JSONArray> criteria = getCriteria(parameters);

    LoggerContext lm = (LoggerContext) LogManager.getContext(false);

    return lm.getLoggers() //
        .stream() //
        .filter(r -> filterRow(r, criteria));

  }

  private Optional<JSONArray> getCriteria(Map<String, String> parameters) {
    if (parameters.containsKey("criteria")) {
      try {
        return Optional.of((JSONArray) JsonUtils.buildCriteria(parameters).get("criteria"));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private static boolean filterRow(Logger r, Optional<JSONArray> criteria) {
    if (!criteria.isPresent()) {
      return true;
    }

    boolean meetsCriteria = true;

    JSONArray criteriaArray = criteria.get();
    try {
      for (int i = 0; i < criteriaArray.length(); i++) {
        JSONObject criterion = criteriaArray.getJSONObject(i);
        meetsCriteria &= loggerMeetsCriterion(r, criterion);
      }
    } catch (JSONException e) {
      log.error("Error matching criteria", e);
    }

    return meetsCriteria;
  }

  private static boolean loggerMeetsCriterion(Logger r, JSONObject criterion) throws JSONException {
    String field = criterion.getString("fieldName");
    String value = criterion.getString("value").toLowerCase();

    switch (field) {
    case "logger":
      return r.getName().toLowerCase().contains(value);
    case "level":
      List<String> values = convertJsonArrayToStringList(new JSONArray(value));
      return values.contains(r.getLevel().toString().toLowerCase());
    default:
      return true;
    }
  }

  private static List<String> convertJsonArrayToStringList(JSONArray array) {
    List<String> result = new ArrayList<>();
    try {
      for (int i = 0; i < array.length(); i++) {
        result.add(array.getString(i));
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    List<DataSourceProperty> dataSourceProperties = new ArrayList<>();
    dataSourceProperties.add(getIdProperty());
    dataSourceProperties.add(getLoggerProperty());
    dataSourceProperties.add(getLevelProperty());

    return dataSourceProperties;
  }

  private DataSourceProperty getLoggerProperty() {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName("logger");

    Reference loggerReference = OBDal.getInstance().get(Reference.class, STRING_REFERENCE_ID);
    UIDefinition stringUiDefinition = UIDefinitionController.getInstance().getUIDefinition(
        loggerReference);
    dsProperty.setUIDefinition(stringUiDefinition);

    return dsProperty;
  }

  private DataSourceProperty getIdProperty() {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName("id");
    dsProperty.setId(true);

    Reference loggerReference = OBDal.getInstance().get(Reference.class, STRING_REFERENCE_ID);
    UIDefinition stringUiDefinition = UIDefinitionController.getInstance().getUIDefinition(
        loggerReference);
    dsProperty.setUIDefinition(stringUiDefinition);

    return dsProperty;
  }

  private DataSourceProperty getLevelProperty() {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName("level");

    Reference logLevelReference = OBDal.getInstance().get(Reference.class,
        LOG_LEVEL_LIST_REFERENCE_ID);
    UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(
        logLevelReference);
    dsProperty.setUIDefinition(uiDefinition);

    Set<String> allowedValues = DataSourceProperty.getAllowedValues(logLevelReference);
    dsProperty.setAllowedValues(allowedValues);
    dsProperty.setValueMap(DataSourceProperty.createValueMap(allowedValues,
        LOG_LEVEL_LIST_REFERENCE_ID));

    return dsProperty;
  }
}
