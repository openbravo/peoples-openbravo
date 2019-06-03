package org.openbravo.service.centralrepository;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class SimpleModule {
  private static final long serialVersionUID = 1L;

  private String author;
  private String description;
  private String help;
  private String licenseAgreement;
  private String licenseType;
  private String moduleID;
  private String moduleVersionID;
  private String name;
  private String type;
  private String updateDescription;
  private String url;
  private String versionNo;
  private boolean isCommercial;
  private Map<String, Object> additionalInfo;

  public SimpleModule(String author, String description, String help, String licenseAgreement,
      String licenseType, String moduleID, String moduleVersionID, String name, String type,
      String updateDescription, String url, String versionNo, boolean isCommercial,
      Map<String, Object> additionalInfo) {
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
      Map<String, Object> additionalInfo = new HashMap<>();
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

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getHelp() {
    return help;
  }

  public void setHelp(String help) {
    this.help = help;
  }

  public String getLicenseAgreement() {
    return licenseAgreement;
  }

  public void setLicenseAgreement(String licenseAgreement) {
    this.licenseAgreement = licenseAgreement;
  }

  public String getLicenseType() {
    return licenseType;
  }

  public void setLicenseType(String licenseType) {
    this.licenseType = licenseType;
  }

  public String getModuleID() {
    return moduleID;
  }

  public void setModuleID(String moduleID) {
    this.moduleID = moduleID;
  }

  public String getModuleVersionID() {
    return moduleVersionID;
  }

  public void setModuleVersionID(String moduleVersionID) {
    this.moduleVersionID = moduleVersionID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUpdateDescription() {
    return updateDescription;
  }

  public void setUpdateDescription(String updateDescription) {
    this.updateDescription = updateDescription;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(String versionNo) {
    this.versionNo = versionNo;
  }

  public boolean isCommercial() {
    return isCommercial;
  }

  public void setCommercial(boolean isCommercial) {
    this.isCommercial = isCommercial;
  }

  public Map<String, Object> getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(Map<String, Object> additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

}
