package org.openbravo.service.centralrepository;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ModuleDependency {
  private String moduleID;
  private String moduleName;
  private String moduleVersionDependencyID;
  private String versionEnd;
  private String versionStart;

  public ModuleDependency() {
  }

  public ModuleDependency(String moduleID, String moduleName, String moduleVersionDependencyID,
      String versionEnd, String versionStart) {
    this.moduleID = moduleID;
    this.moduleName = moduleName;
    this.moduleVersionDependencyID = moduleVersionDependencyID;
    this.versionEnd = versionEnd;
    this.versionStart = versionStart;
  }

  public static ModuleDependency fromJson(JSONObject jsonDep) {
    try {
      String moduleID = jsonDep.getString("moduleID");
      String moduleName = jsonDep.getString("moduleName");
      String moduleVersionDependencyID = jsonDep.getString("moduleVersionDependencyID");
      String versionEnd = jsonDep.getString("versionEnd");
      String versionStart = jsonDep.getString("versionStart");
      return new ModuleDependency(moduleID, moduleName, moduleVersionDependencyID, versionEnd,
          versionStart);
    } catch (JSONException e) {
      return null;
    }
  }

  public static ModuleDependency[] fromJson(JSONArray jsonArray) {
    ModuleDependency[] deps = new ModuleDependency[jsonArray.length()];
    try {
      for (int i = 0; i < jsonArray.length(); i++) {
        deps[i] = fromJson(jsonArray.getJSONObject(i));
      }
      return deps;
    } catch (JSONException e) {
      return null;
    }
  }

  public String getModuleID() {
    return moduleID;
  }

  public void setModuleID(String moduleID) {
    this.moduleID = moduleID;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public String getModuleVersionDependencyID() {
    return moduleVersionDependencyID;
  }

  public void setModuleVersionDependencyID(String moduleVersionDependencyID) {
    this.moduleVersionDependencyID = moduleVersionDependencyID;
  }

  public String getVersionEnd() {
    return versionEnd;
  }

  public void setVersionEnd(String versionEnd) {
    this.versionEnd = versionEnd;
  }

  public String getVersionStart() {
    return versionStart;
  }

  public void setVersionStart(String versionStart) {
    this.versionStart = versionStart;
  }

}
