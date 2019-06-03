package org.openbravo.service.centralrepository;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class ModuleDependency extends org.openbravo.services.webservice.ModuleDependency {

  public ModuleDependency(java.lang.String moduleID, java.lang.String moduleName,
      java.lang.String moduleVersionDependencyID, java.lang.String versionEnd,
      java.lang.String versionStart) {
    super(moduleID, moduleName, moduleVersionDependencyID, versionEnd, versionStart);
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

  public static org.openbravo.services.webservice.ModuleDependency[] fromJson(JSONArray jsonArray) {
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

}
