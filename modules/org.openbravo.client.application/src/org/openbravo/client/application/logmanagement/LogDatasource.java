package org.openbravo.client.application.logmanagement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.reference.UIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.service.datasource.DataSourceProperty;
import org.openbravo.service.datasource.ReadOnlyDataSourceService;

public class LogDatasource extends ReadOnlyDataSourceService {

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

    return filteredLoggers //
        .skip(startRow) //
        .limit(endRow) //
        .map(l -> {
          Map<String, Object> r = new HashMap<>(2);
          r.put("logger", l.getName());
          r.put("level", l.getLevel().toString());
          return r;
        }) //
        .collect(Collectors.toList());
  }

  private Stream<Logger> getFilteredStream(Map<String, String> parameters) {
    Optional<JSONObject> criteria = getCriteria(parameters);

    LoggerContext lm = (LoggerContext) LogManager.getContext(false);
    return lm.getLoggers() //
        .stream() //
        .filter(r -> filterRow(r, criteria));

  }

  private Optional<JSONObject> getCriteria(Map<String, String> parameters) {
    if (parameters.containsKey("criteria")) {
      try {
        return Optional.of(new JSONObject(parameters.get("criteria")));
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }

  private static boolean filterRow(Logger r, Optional<JSONObject> criteria) {
    if (!criteria.isPresent()) {
      return true;
    }
    JSONObject c = criteria.get();
    try {
      String field = c.getString("fieldName");
      String value = c.getString("value").toLowerCase();

      switch (field) {
      case "logger":
        return r.getName().toLowerCase().contains(value);
      case "level":
        List<String> values = convertJsonArrayToStringList(value);
        return values.contains(r.getLevel().toString().toLowerCase());
      }
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return true;
  }

  private static List<String> convertJsonArrayToStringList(String value) {
    List<String> result = new ArrayList<>();
    try {
      JSONArray array = new JSONArray(value);
      for (int i=0; i < array.length(); i++) {
        result.add(array.getString(i));
      }
    }
    catch(JSONException e) {
      e.printStackTrace();
    }

    return result;
  }

  @Override
  public List<DataSourceProperty> getDataSourceProperties(Map<String, Object> parameters) {
    List<DataSourceProperty> dataSourceProperties = new ArrayList<>();
    dataSourceProperties.add(getLoggerProperty());
    dataSourceProperties.add(getLevelProperty());

    return dataSourceProperties;
  }

  private DataSourceProperty getLoggerProperty() {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName("logger");

    Reference loggerReference = OBDal.getInstance().get(Reference.class, STRING_REFERENCE_ID);
    UIDefinition stringUiDefinition = UIDefinitionController.getInstance().getUIDefinition(loggerReference);
    dsProperty.setUIDefinition(stringUiDefinition);

    return dsProperty;
  }

  private DataSourceProperty getLevelProperty() {
    final DataSourceProperty dsProperty = new DataSourceProperty();
    dsProperty.setName("level");

    Reference logLevelReference = OBDal.getInstance().get(Reference.class, LOG_LEVEL_LIST_REFERENCE_ID);
    UIDefinition uiDefinition = UIDefinitionController.getInstance().getUIDefinition(logLevelReference);
    dsProperty.setUIDefinition(uiDefinition);

    Set<String> allowedValues = DataSourceProperty.getAllowedValues(logLevelReference);
    dsProperty.setAllowedValues(allowedValues);
    dsProperty.setValueMap(DataSourceProperty.createValueMap(allowedValues, LOG_LEVEL_LIST_REFERENCE_ID));

    return dsProperty;
  }
}
