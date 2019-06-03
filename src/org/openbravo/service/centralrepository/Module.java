package org.openbravo.service.centralrepository;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class Module {
  private java.lang.String author;
  private java.lang.String dbPrefix;
  private ModuleDependency[] dependencies;
  private java.lang.String description;
  private java.lang.String help;
  private ModuleDependency[] includes;
  private java.lang.String licenseAgreement;
  private java.lang.String licenseType;
  private java.lang.String moduleID;
  private java.lang.String moduleVersionID;
  private java.lang.String name;
  private java.lang.String packageName;
  private java.lang.String type;
  private java.lang.String updateDescription;
  private java.lang.String url;
  private java.lang.String versionNo;
  private boolean isCommercial;
  private HashMap additionalInfo;

  public Module() {
  }

  public Module(String author, String dbPrefix, ModuleDependency[] dependencies, String description,
      String help, ModuleDependency[] includes, String licenseAgreement, String licenseType,
      String moduleID, String moduleVersionID, String name, String packageName, String type,
      String updateDescription, String url, String versionNo, boolean isCommercial,
      HashMap additionalInfo) {
    this.author = author;
    this.dbPrefix = dbPrefix;
    this.dependencies = dependencies;
    this.description = description;
    this.help = help;
    this.includes = includes;
    this.licenseAgreement = licenseAgreement;
    this.licenseType = licenseType;
    this.moduleID = moduleID;
    this.moduleVersionID = moduleVersionID;
    this.name = name;
    this.packageName = packageName;
    this.type = type;
    this.updateDescription = updateDescription;
    this.url = url;
    this.versionNo = versionNo;
    this.isCommercial = isCommercial;
    this.additionalInfo = additionalInfo;
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
      ModuleDependency[] dependencies = ModuleDependency
          .fromJson(jsonModule.getJSONArray("dependencies"));
      ModuleDependency[] includes = ModuleDependency.fromJson(jsonModule.getJSONArray("includes"));

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

  public java.lang.String getAuthor() {
    return author;
  }

  public void setAuthor(java.lang.String author) {
    this.author = author;
  }

  public java.lang.String getDbPrefix() {
    return dbPrefix;
  }

  public void setDbPrefix(java.lang.String dbPrefix) {
    this.dbPrefix = dbPrefix;
  }

  public ModuleDependency[] getDependencies() {
    return dependencies;
  }

  public void setDependencies(ModuleDependency[] dependencies) {
    this.dependencies = dependencies;
  }

  public java.lang.String getDescription() {
    return description;
  }

  public void setDescription(java.lang.String description) {
    this.description = description;
  }

  public java.lang.String getHelp() {
    return help;
  }

  public void setHelp(java.lang.String help) {
    this.help = help;
  }

  public ModuleDependency[] getIncludes() {
    return includes;
  }

  public void setIncludes(ModuleDependency[] includes) {
    this.includes = includes;
  }

  public java.lang.String getLicenseAgreement() {
    return licenseAgreement;
  }

  public void setLicenseAgreement(java.lang.String licenseAgreement) {
    this.licenseAgreement = licenseAgreement;
  }

  public java.lang.String getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(java.lang.String licenseType) {
    this.licenseType = licenseType;
  }

  public java.lang.String getModuleID() {
    return moduleID;
  }

  public void setModuleID(java.lang.String moduleID) {
    this.moduleID = moduleID;
  }

  public java.lang.String getModuleVersionID() {
    return moduleVersionID;
  }

  public void setModuleVersionID(java.lang.String moduleVersionID) {
    this.moduleVersionID = moduleVersionID;
  }

  public java.lang.String getName() {
    return name;
  }

  public void setName(java.lang.String name) {
    this.name = name;
  }

  public java.lang.String getPackageName() {
    return packageName;
  }

  public void setPackageName(java.lang.String packageName) {
    this.packageName = packageName;
  }

  public java.lang.String getType() {
    return type;
  }

  public void setType(java.lang.String type) {
    this.type = type;
  }

  public java.lang.String getUpdateDescription() {
    return updateDescription;
  }

  public void setUpdateDescription(java.lang.String updateDescription) {
    this.updateDescription = updateDescription;
  }

  public java.lang.String getUrl() {
    return url;
  }

  public void setUrl(java.lang.String url) {
    this.url = url;
  }

  public java.lang.String getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(java.lang.String versionNo) {
    this.versionNo = versionNo;
  }

  public boolean isIsCommercial() {
    return isCommercial;
  }

  public void setIsCommercial(boolean isCommercial) {
    this.isCommercial = isCommercial;
  }

  public HashMap getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(HashMap additionalInfo) {
    this.additionalInfo = additionalInfo;
  }
}
