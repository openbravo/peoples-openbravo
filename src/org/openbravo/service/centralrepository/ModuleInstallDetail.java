package org.openbravo.service.centralrepository;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

public class ModuleInstallDetail {
  private String[] dependencyErrors;
  private Module[] modulesToInstall;
  private Module[] modulesToUpdate;
  private boolean validConfiguration;

  public ModuleInstallDetail(String[] dependencyErrors, Module[] modulesToInstall,
      Module[] modulesToUpdate, boolean validConfiguration) {
    super();
    this.dependencyErrors = dependencyErrors;
    this.modulesToInstall = modulesToInstall;
    this.modulesToUpdate = modulesToUpdate;
    this.validConfiguration = validConfiguration;
  }

  public static ModuleInstallDetail fromJson(JSONObject jsonResponse) {
    try {
      JSONObject jsonDetail = jsonResponse.getJSONObject("response");

      JSONArray errors = jsonDetail.getJSONArray("dependencyErrors");
      String[] dependencyErrors = new String[errors.length()];
      for (int i = 0; i < errors.length(); i++) {
        dependencyErrors[i] = errors.getString(i);
      }

      Module[] modulesToInstall = Module.fromJson(jsonDetail.getJSONArray("modulesToInstall"));
      Module[] modulesToUpdate = Module.fromJson(jsonDetail.getJSONArray("modulesToUpdate"));

      boolean validConfing = jsonDetail.getBoolean("validConfiguration");
      return new ModuleInstallDetail(dependencyErrors, modulesToInstall, modulesToUpdate,
          validConfing);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  public String[] getDependencyErrors() {
    return dependencyErrors;
  }

  public void setDependencyErrors(String[] dependencyErrors) {
    this.dependencyErrors = dependencyErrors;
  }

  public Module[] getModulesToInstall() {
    return modulesToInstall;
  }

  public void setModulesToInstall(Module[] modulesToInstall) {
    this.modulesToInstall = modulesToInstall;
  }

  public Module[] getModulesToUpdate() {
    return modulesToUpdate;
  }

  public void setModulesToUpdate(Module[] modulesToUpdate) {
    this.modulesToUpdate = modulesToUpdate;
  }

  public boolean isValidConfiguration() {
    return validConfiguration;
  }

  public void setValidConfiguration(boolean validConfiguration) {
    this.validConfiguration = validConfiguration;
  }
}
