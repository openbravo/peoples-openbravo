package org.openbravo.service.centralrepository;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class SimpleModule extends org.openbravo.services.webservice.SimpleModule {

  private static final long serialVersionUID = 1L;

  public SimpleModule(String author, String description, String help, String licenseAgreement,
      String licenseType, String moduleID, String moduleVersionID, String name, String type,
      String updateDescription, String url, String versionNo, boolean isCommercial,
      HashMap additionalInfo) {
    super(author, description, help, licenseAgreement, licenseType, moduleID, moduleVersionID, name,
        type, updateDescription, url, versionNo, isCommercial, additionalInfo);
  }

  public static org.openbravo.services.webservice.SimpleModule fromJson(JSONObject jsonModule) {
    try {

      HashMap<String, String> additionalInfo = new HashMap<>();
      if (jsonModule.get("additionalInfo") instanceof JSONObject) {
        JSONObject jsonAdditionalInfo = jsonModule.getJSONObject("additionalInfo");
        JSONArray keys = jsonAdditionalInfo.names();
        additionalInfo = new HashMap<>(keys.length());

        for (int i = 0; i < keys.length(); i++) {
          String key = keys.getString(i);
          additionalInfo.put(key, jsonAdditionalInfo.getString(key));
        }
      }

      return new SimpleModule(jsonModule.getString("author"), jsonModule.getString("description"),
          jsonModule.getString("help"), jsonModule.getString("licenseAgreement"),
          jsonModule.getString("licenseType"), jsonModule.getString("moduleID"),
          jsonModule.getString("moduleVersionID"), jsonModule.getString("name"),
          jsonModule.getString("type"), jsonModule.getString("updateDescription"),
          jsonModule.getString("url"), jsonModule.getString("versionNo"),
          jsonModule.getBoolean("isCommercial"), additionalInfo);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

}
