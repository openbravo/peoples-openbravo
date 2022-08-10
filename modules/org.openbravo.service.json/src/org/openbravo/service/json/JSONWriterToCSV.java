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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.json;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.Property;
import org.openbravo.base.model.domaintype.EnumerateDomainType;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.application.window.OBViewUtil;
import org.openbravo.client.kernel.reference.DateTimeUIDefinition;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.datamodel.Column;
import org.openbravo.model.ad.ui.Element;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.model.ad.ui.FieldTrl;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.utils.Replace;

/**
 * Helper class to write JSON objects to generate a CSV file using the standard preferences.
 *
 */
public class JSONWriterToCSV extends DefaultJsonDataService.QueryResultWriter {

  private static final Logger log = LogManager.getLogger();

  private static final String[] CSV_FORMULA_PREFIXES = new String[] { "=", "+", "-", "@" };
  private static final String YES_NO_REFERENCE_ID = "20";

  private Writer writer;
  private String fieldSeparator;
  private String decimalSeparator;
  private String prefDecimalSeparator;
  private List<String> fieldProperties;
  private Map<String, String> niceFieldProperties = new HashMap<String, String>();
  private boolean propertiesWritten = false;
  private Map<String, Map<String, String>> refLists = new HashMap<String, Map<String, String>>();
  private List<String> refListCols = new ArrayList<String>();
  private List<String> dateCols = new ArrayList<String>();
  private List<String> dateTimeCols = new ArrayList<String>();
  private List<String> timeCols = new ArrayList<String>();
  private List<String> numericCols = new ArrayList<String>();
  private List<String> yesNoCols = new ArrayList<String>();
  private int clientUTCOffsetMiliseconds;
  private TimeZone clientTimeZone;
  private String translatedLabelYes;
  private String translatedLabelNo;

  public JSONWriterToCSV(HttpServletRequest request, HttpServletResponse response,
      Map<String, String> parameters, Entity entity) {
    try {
      OBContext.setAdminMode();
      response.setHeader("Content-Disposition", "attachment; filename=ExportedData.csv");
      writer = response.getWriter();
      VariablesSecureApp vars = new VariablesSecureApp(request);
      Window window = JsonUtils.isValueEmpty(parameters.get(JsonConstants.TAB_PARAMETER)) ? null
          : OBDal.getInstance()
              .get(Tab.class, parameters.get(JsonConstants.TAB_PARAMETER))
              .getWindow();
      try {
        prefDecimalSeparator = Preferences.getPreferenceValue("OBSERDS_CSVDecimalSeparator", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), window);
      } catch (PropertyNotFoundException e) {
        // There is no preference for the decimal separator.
      }
      decimalSeparator = vars.getSessionValue("#DecimalSeparator|generalQtyEdition")
          .substring(0, 1);
      try {
        fieldSeparator = Preferences.getPreferenceValue("OBSERDS_CSVFieldSeparator", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), window);
      } catch (PropertyNotFoundException e) {
        // There is no preference for the field separator. Using the default one.
        fieldSeparator = ",";
      }
      if ((prefDecimalSeparator != null && prefDecimalSeparator.equals(fieldSeparator))
          || (prefDecimalSeparator == null && decimalSeparator.equals(fieldSeparator))) {
        if (!fieldSeparator.equals(";")) {
          fieldSeparator = ";";
        } else {
          fieldSeparator = ",";
        }
        log.warn(
            "Warning: CSV Field separator is identical to the decimal separator. Changing the field separator to "
                + fieldSeparator + " to avoid generating a wrong CSV file");
      }
      if (parameters.get("_UTCOffsetMiliseconds").length() > 0) {
        clientUTCOffsetMiliseconds = Integer.parseInt(parameters.get("_UTCOffsetMiliseconds"));
      } else {
        clientUTCOffsetMiliseconds = 0;
      }

      clientTimeZone = null;
      try {
        String clientTimeZoneId = Preferences.getPreferenceValue("localTimeZoneID", true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), null);
        List<String> validTimeZoneIDs = Arrays.asList(TimeZone.getAvailableIDs());
        if (validTimeZoneIDs.contains(clientTimeZoneId)) {
          clientTimeZone = TimeZone.getTimeZone(clientTimeZoneId);
        } else {
          log.error(
              "{} is not a valid time zone identifier. For a list of all accepted identifiers check http://www.java2s.com/Tutorial/Java/0120__Development/GettingallthetimezonesIDs.htm",
              clientTimeZoneId);
        }
      } catch (PropertyException pe) {
        log.warn(
            "The local Local Timezone ID property is not defined. It can be defined in a preference. For a list of all accepted values check http://www.java2s.com/Tutorial/Java/0120__Development/GettingallthetimezonesIDs.htm");
      }

      fieldProperties = new ArrayList<String>();
      if (!JsonUtils.isValueEmpty(parameters.get("viewState"))) {
        String viewStateO = parameters.get("viewState");
        String viewStateWithoutParenthesis = viewStateO.substring(1, viewStateO.length() - 1);
        JSONObject viewState = new JSONObject(viewStateWithoutParenthesis);
        String fieldA = viewState.getString("field");
        JSONArray fields = new JSONArray(fieldA);
        for (int i = 0; i < fields.length(); i++) {
          JSONObject field = fields.getJSONObject(i);
          if (field.has("visible") && !field.getBoolean("visible")) {
            // The field is not visible. We should not export it
            continue;
          }
          if (field.getString("name").equals("_checkboxField")
              || field.getString("name").equals("_editLink")) {
            continue;
          }
          fieldProperties.add(field.getString("name"));
        }
      }

      // Now we calculate ref lists and nice property names
      final String userLanguageId = OBContext.getOBContext().getLanguage().getId();
      if (entity != null) {
        final Map<String, Property> properties = new HashMap<String, Property>();
        for (Property prop : entity.getProperties()) {
          if (!fieldProperties.contains(prop.getName())) {
            continue;
          }
          properties.put(prop.getName(), prop);
        }
        for (String fieldProperty : fieldProperties) {
          if (fieldProperty.contains(DalUtil.FIELDSEPARATOR)) {
            properties.put(fieldProperty, DalUtil.getPropertyFromPath(entity, fieldProperty));
          }
        }

        boolean preferenceCalculateFirst = true;
        boolean translateYesNoReferences = false;
        String formattedPropKey;
        for (String propKey : properties.keySet()) {
          final Property prop = properties.get(propKey);
          Column col = OBDal.getInstance().get(Column.class, prop.getColumnId());
          formattedPropKey = propKey.replace("$", ".");
          if (prop.isAuditInfo()) {
            Element element = null;
            if ("creationDate".equals(propKey)) {
              element = OBViewUtil.createdElement;
            } else if ("createdBy".equals(propKey)) {
              element = OBViewUtil.createdByElement;
            } else if ("updated".equals(propKey)) {
              element = OBViewUtil.updatedElement;
            } else if ("updatedBy".equals(propKey)) {
              element = OBViewUtil.updatedByElement;
            }
            if (element != null) {
              niceFieldProperties.put(propKey,
                  OBViewUtil.getLabel(element, element.getADElementTrlList()));
            } else {
              niceFieldProperties.put(propKey, col.getName());
            }
          } else if (parameters.get(JsonConstants.TAB_PARAMETER) != null
              && !parameters.get(JsonConstants.TAB_PARAMETER).equals("")) {
            Tab tab = OBDal.getInstance()
                .get(Tab.class, parameters.get(JsonConstants.TAB_PARAMETER));
            for (Field field : tab.getADFieldList()) {
              if (field.getProperty() != null && !formattedPropKey.equals(field.getProperty())) {
                continue;
              } else if (field.getColumn() == null
                  || !field.getColumn().getId().equals(col.getId())) {
                continue;
              }

              niceFieldProperties.put(propKey, field.getName());
              for (FieldTrl fieldTrl : field.getADFieldTrlList()) {
                if (fieldTrl.getLanguage().getId().equals(userLanguageId)) {
                  niceFieldProperties.put(propKey, fieldTrl.getName());
                }
              }
            }
          } else {
            niceFieldProperties.put(propKey, col.getName());
          }
          // We also store the date properties
          if (prop.isDate()) {
            dateCols.add(propKey);
          } else if (prop.isDatetime()) {
            dateTimeCols.add(propKey);
          } else if (prop.isTime()) {
            timeCols.add(propKey);
          } else if (prop.isPrimitive() && prop.isNumericType()) {
            numericCols.add(propKey);
          } else if (isYesNoReference(prop)) {
            // Calculate if it is needed translate YesNo reference in export to CSV.
            if (preferenceCalculateFirst) {
              translateYesNoReferences = translateYesNoReferencesInCsv(window);
              preferenceCalculateFirst = false;
            }
            if (translateYesNoReferences) {
              yesNoCols.add(propKey);
            }
          }

          if (!(prop.getDomainType() instanceof EnumerateDomainType)) {
            continue;
          }
          String referenceId = col.getReferenceSearchKey().getId();
          Map<String, String> reflists = new HashMap<>();
          final String hql = "select al.searchKey, al.name from ADList al where "
              + " al.reference.id=:referenceId and al.active=true";
          final Query<Object[]> qry = OBDal.getInstance()
              .getSession()
              .createQuery(hql, Object[].class);
          qry.setParameter("referenceId", referenceId);
          for (Object[] row : qry.list()) {
            reflists.put(row[0].toString(), row[1].toString());
          }
          final String hqltrl = "select al.searchKey, trl.name from ADList al, ADListTrl trl where "
              + " al.reference.id=:referenceId and trl.listReference=al and trl.language.id=:languageId"
              + " and al.active=true and trl.active=true";
          final Query<Object[]> qrytrl = OBDal.getInstance()
              .getSession()
              .createQuery(hqltrl, Object[].class);
          qrytrl.setParameter("referenceId", referenceId);
          qrytrl.setParameter("languageId", userLanguageId);
          for (Object[] row : qrytrl.list()) {
            reflists.put(row[0].toString(), row[1].toString());
          }
          refListCols.add(propKey);
          refLists.put(propKey, reflists);
        }
      }

      writeCSVHeaderNote(parameters);
      if (fieldProperties.size() > 0) {
        // If the request came with the view state information, we get the properties from there
        for (int i = 0; i < fieldProperties.size(); i++) {
          if (i > 0) {
            writer.append(fieldSeparator);
          }
          if (niceFieldProperties.get(fieldProperties.get(i)) != null) {
            writer.append("\"")
                .append(niceFieldProperties.get(fieldProperties.get(i)))
                .append("\"");
          }
        }
        propertiesWritten = true;
      }
    } catch (Exception e) {
      throw new OBException("Error while exporting a CSV file", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public List<String> getFieldProperties() {
    return fieldProperties;
  }

  private boolean isYesNoReference(Property prop) {
    final Column column = OBDal.getInstance().get(Column.class, prop.getColumnId());
    return YES_NO_REFERENCE_ID.equals(column.getReference().getId());
  }

  private boolean translateYesNoReferencesInCsv(Window windowToCsv) {
    boolean shouldCheck = false;
    try {
      shouldCheck = Preferences.YES
          .equals(Preferences.getPreferenceValue("OBSERDS_CSVExportTranslateYesNoReference", true,
              OBContext.getOBContext().getCurrentClient(),
              OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
              OBContext.getOBContext().getRole(), windowToCsv));
    } catch (PropertyException prefNotDefined) {
    }
    return shouldCheck;
  }

  private void writeJSONProperties(JSONObject row) {
    final Iterator<?> itKeysF = row.keys();
    Vector<String> keys = new Vector<String>();
    boolean isFirst = true;
    try {
      while (itKeysF.hasNext()) {
        String key = (String) itKeysF.next();
        if (key.endsWith(JsonConstants.IDENTIFIER)) {
          continue;
        }
        if (fieldProperties.size() > 0 && !fieldProperties.contains(key)) {
          // Field is not visible. We don't show it
          continue;
        }
        if (isFirst) {
          isFirst = false;
        } else {
          writer.append(fieldSeparator);
        }
        keys.add(key);
        writer.append("\"").append(key).append("\"");
      }
      propertiesWritten = true;
    } catch (Exception e) {
      throw new OBException("Error while writing column names when exporting a CSV file", e);
    }
  }

  @Override
  public void write(JSONObject json) {
    try {
      if (!propertiesWritten) {
        writeJSONProperties(json);
      }
      writer.append("\n");
      final Iterator<?> itKeys;
      if (fieldProperties.size() > 0) {
        itKeys = fieldProperties.iterator();
      } else {
        itKeys = json.keys();
      }

      boolean isFirst = true;
      while (itKeys.hasNext()) {
        String key = (String) itKeys.next();
        if (key.endsWith(JsonConstants.IDENTIFIER)) {
          continue;
        }
        if (fieldProperties.size() > 0 && !fieldProperties.contains(key)) {
          // Field is not visible. We don't show it
          continue;
        }
        if (isFirst) {
          isFirst = false;
        } else {
          writer.append(fieldSeparator);
        }
        if (!json.has(key)) {
          continue;
        }
        Object keyValue = json.has(key + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER)
            ? json.get(key + DalUtil.FIELDSEPARATOR + JsonConstants.IDENTIFIER)
            : json.get(key);
        boolean isNumeric = false;
        if (refListCols.contains(key)) {
          keyValue = refLists.get(key).get(keyValue);
        } else if (keyValue instanceof Number) {
          // if the CSV decimal separator property is defined, used it over the character
          // defined in Format.xml
          isNumeric = true;
          keyValue = keyValue.toString()
              .replace(".", prefDecimalSeparator != null ? prefDecimalSeparator : decimalSeparator);
        } else if (dateCols.contains(key) && keyValue != null
            && !keyValue.toString().equals("null")) {
          Date date = JsonUtils.createDateFormat().parse(keyValue.toString());
          keyValue = UIDefinitionController.DATE_UI_DEFINITION.convertToClassicString(date);
        } else if (dateTimeCols.contains(key) && keyValue != null
            && !keyValue.toString().equals("null")) {
          final String repairedString = JsonUtils.convertFromXSDToJavaFormat(keyValue.toString());
          Date localDate = JsonUtils.createDateTimeFormat().parse(repairedString);
          Date clientTimezoneDate = null;
          clientTimezoneDate = convertFromLocalToClientTimezone(localDate);
          keyValue = ((DateTimeUIDefinition) UIDefinitionController.DATETIME_UI_DEFINITION)
              .convertToClassicStringInLocalTime(clientTimezoneDate);
        } else if (timeCols.contains(key) && keyValue != null
            && !keyValue.toString().equals("null")) {
          Date UTCdate = JsonUtils.createTimeFormatWithoutGMTOffset().parse(keyValue.toString());
          Date clientTimezoneDate = null;
          clientTimezoneDate = convertFromUTCToClientTimezone(UTCdate);
          SimpleDateFormat timeFormat = JsonUtils.createTimeFormatWithoutGMTOffset();
          timeFormat.setLenient(true);
          keyValue = timeFormat.format(clientTimezoneDate);
        } else if (yesNoCols.contains(key) && keyValue != null) {
          keyValue = (Boolean) keyValue ? getTranslatedLabelYes() : getTranslatedLabelNo();
        }

        String outputValue;
        if (keyValue != null && !keyValue.toString().equals("null")) {
          outputValue = keyValue.toString().replace("\"", "\"\"");
          if (!isNumeric && StringUtils.startsWithAny(outputValue, CSV_FORMULA_PREFIXES)) {
            // escape formulas
            outputValue = "\t" + outputValue;
          }
        } else {
          outputValue = "";
        }

        if (!numericCols.contains(key)) {
          outputValue = "\"" + outputValue + "\"";
        }
        writer.append(outputValue);
      }
    } catch (Exception e) {
      throw new OBException("Error while exporting CSV information", e);
    }
  }

  private String getTranslatedLabelYes() {
    if (translatedLabelYes == null) {
      translatedLabelYes = getTranslatedLabel("OBUISC_Yes");
    }
    return translatedLabelYes;
  }

  private String getTranslatedLabelNo() {
    if (translatedLabelNo == null) {
      translatedLabelNo = getTranslatedLabel("OBUISC_No");
    }
    return translatedLabelNo;
  }

  private String getTranslatedLabel(String label) {
    String userLanguage = OBContext.getOBContext().getLanguage().getLanguage();
    return Utility.messageBD(new DalConnectionProvider(false), label, userLanguage);
  }

  private Date convertFromLocalToClientTimezone(Date localDate) {

    Date UTCDate = convertFromLocalToUTCTimezone(localDate);
    Date clientDate = convertFromUTCToClientTimezone(UTCDate);

    return clientDate;
  }

  private Date convertFromUTCToClientTimezone(Date UTCdate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(UTCdate);
    if (clientTimeZone != null) {
      calendar = Calendar.getInstance(clientTimeZone);
      calendar.setTime(UTCdate);
      int gmtMillisecondOffset = (calendar.get(Calendar.ZONE_OFFSET)
          + calendar.get(Calendar.DST_OFFSET));
      calendar.add(Calendar.MILLISECOND, gmtMillisecondOffset);
    } else {
      calendar = Calendar.getInstance();
      calendar.setTime(UTCdate);
      calendar.add(Calendar.MILLISECOND, clientUTCOffsetMiliseconds);
    }
    return calendar.getTime();
  }

  private Date convertFromLocalToUTCTimezone(Date localDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(localDate);

    int gmtMillisecondOffset = (calendar.get(Calendar.ZONE_OFFSET)
        + calendar.get(Calendar.DST_OFFSET));
    calendar.add(Calendar.MILLISECOND, -gmtMillisecondOffset);

    return calendar.getTime();
  }

  private void writeCSVHeaderNote(Map<String, String> parameters)
      throws IOException, PropertyException {
    final String csvHeaderMsg = getMessage(parameters, "OBSERDS_CSVHeaderMessage");

    if (StringUtils.isNotBlank(csvHeaderMsg)) {
      writer.append("\"").append(csvHeaderMsg).append("\"");
      fillEmptyColumns();
      writer.append("\n");
    }
  }

  public void writeCSVFooterNote(Map<String, String> parameters)
      throws IOException, PropertyException {
    final String csvFooterMsg = getMessage(parameters, "OBSERDS_CSVFooterMessage");

    if (StringUtils.isNotBlank(csvFooterMsg)) {
      writer.append("\n").append("\"").append(csvFooterMsg).append("\"");
      fillEmptyColumns();
    }
  }

  private String getMessage(final Map<String, String> parameters, final String property)
      throws PropertyException {
    OBContext.setAdminMode(true);
    try {
      String csvMessage = null;
      try {
        Window window = JsonUtils.isValueEmpty(parameters.get(JsonConstants.TAB_PARAMETER)) ? null
            : OBDal.getInstance()
                .get(Tab.class, parameters.get(JsonConstants.TAB_PARAMETER))
                .getWindow();
        csvMessage = Preferences.getPreferenceValue(property, true,
            OBContext.getOBContext().getCurrentClient(),
            OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
            OBContext.getOBContext().getRole(), window);
      } catch (PropertyNotFoundException e) {
        // There is no preference defined
        csvMessage = null;
      }

      if (StringUtils.isNotBlank(csvMessage)) {
        csvMessage = Replace.replace(
            Replace.replace(Replace.replace(OBMessageUtils.messageBD(csvMessage), "\\n", "\n"),
                "&quot;", "\""),
            "\"", "\"\"");
      }

      return csvMessage;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void fillEmptyColumns() throws IOException {
    for (int i = 1; i < fieldProperties.size(); i++) {
      writer.append(fieldSeparator);
    }
  }
}
