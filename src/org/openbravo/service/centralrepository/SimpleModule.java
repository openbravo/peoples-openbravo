package org.openbravo.service.centralrepository;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class SimpleModule {
  private static final long serialVersionUID = 1L;

  private java.lang.String author;
  private java.lang.String description;
  private java.lang.String help;
  private java.lang.String licenseAgreement;
  private java.lang.String licenseType;
  private java.lang.String moduleID;
  private java.lang.String moduleVersionID;
  private java.lang.String name;
  private java.lang.String type;
  private java.lang.String updateDescription;
  private java.lang.String url;
  private java.lang.String versionNo;
  private boolean isCommercial;
  private HashMap additionalInfo;

  public SimpleModule(String author, String description, String help, String licenseAgreement,
      String licenseType, String moduleID, String moduleVersionID, String name, String type,
      String updateDescription, String url, String versionNo, boolean isCommercial,
      HashMap additionalInfo) {
    this.author = author;
    this.description = description;
    this.help = help;
    this.licenseAgreement = licenseAgreement;
    this.licenseType = licenseType;
    this.moduleID = moduleID;
    this.moduleVersionID = moduleVersionID;
    this.name = name;
    this.type = type;
    this.updateDescription = updateDescription;
    this.url = url;
    this.versionNo = versionNo;
    this.isCommercial = isCommercial;
    this.additionalInfo = additionalInfo;
  }

  public static SimpleModule fromJson(JSONObject jsonModule) {
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

  public java.lang.String getAuthor() {
    return author;
  }

  public void setAuthor(java.lang.String author) {
    this.author = author;
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

  public boolean isCommercial() {
    return isCommercial;
  }

  public void setCommercial(boolean isCommercial) {
    this.isCommercial = isCommercial;
  }

  public HashMap getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(HashMap additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

}
