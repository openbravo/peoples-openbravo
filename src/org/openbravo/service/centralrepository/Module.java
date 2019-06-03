package org.openbravo.service.centralrepository;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

@SuppressWarnings("serial")
public class Module extends org.openbravo.services.webservice.Module {
  public Module(String author, String dbPrefix,
      org.openbravo.services.webservice.ModuleDependency[] dependencies, String description,
      String help, org.openbravo.services.webservice.ModuleDependency[] includes,
      String licenseAgreement, String licenseType, String moduleID, String moduleVersionID,
      String name, String packageName, String type, String updateDescription, String url,
      String versionNo, boolean isCommercial, HashMap additionalInfo) {
    super(author, dbPrefix, dependencies, description, help, includes, licenseAgreement,
        licenseType, moduleID, moduleVersionID, name, packageName, type, updateDescription, url,
        versionNo, isCommercial, additionalInfo);
  }

  public static Module fromJson(JSONObject jsonModule) {
    try {
      String author = jsonModule.getString("author");
      String dbPrefix = jsonModule.getString("dbPrefix");
      String description = jsonModule.getString("description");
      String help = jsonModule.getString("help");
      String licenseAgreement = jsonModule.getString("licenseAgreement");
      String licenseType = jsonModule.getString("licenseType");
      String moduleID = jsonModule.getString("moduleID");
      String moduleVersionID = jsonModule.getString("moduleVersionID");
      String name = jsonModule.getString("name");
      String packageName = jsonModule.getString("packageName");
      String type = jsonModule.getString("type");
      String updateDescription = jsonModule.getString("updateDescription");
      String url = jsonModule.getString("url");
      String versionNo = jsonModule.getString("versionNo");
      boolean isCommercial = jsonModule.getBoolean("isCommercial");

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
      org.openbravo.services.webservice.ModuleDependency[] dependencies = ModuleDependency
          .fromJson(jsonModule.getJSONArray("dependencies"));
      org.openbravo.services.webservice.ModuleDependency[] includes = ModuleDependency
          .fromJson(jsonModule.getJSONArray("includes"));

      return new Module(author, dbPrefix, dependencies, description, help, includes,
          licenseAgreement, licenseType, moduleID, moduleVersionID, name, packageName, type,
          updateDescription, url, versionNo, isCommercial, additionalInfo);
    } catch (JSONException e) {
      throw new OBException(e);
    }
  }

  public static Module[] fromJson(JSONArray jsonModules) {
    Module[] modules = new Module[jsonModules.length()];
    try {
      for (int i = 0; i < jsonModules.length(); i++) {
        modules[i] = fromJson(jsonModules.getJSONObject(i));
      }
    } catch (JSONException e) {
      throw new OBException(e);
    }
    return modules;
  }
}
