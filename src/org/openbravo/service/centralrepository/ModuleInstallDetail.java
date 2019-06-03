package org.openbravo.service.centralrepository;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;

@SuppressWarnings("serial")
public class ModuleInstallDetail extends org.openbravo.services.webservice.ModuleInstallDetail {
  public ModuleInstallDetail(java.lang.String[] dependencyErrors,
      org.openbravo.services.webservice.Module[] modulesToInstall,
      org.openbravo.services.webservice.Module[] modulesToUpdate, boolean validConfiguration) {
    super(dependencyErrors, modulesToInstall, modulesToUpdate, validConfiguration);
  }

  public static ModuleInstallDetail fromJson(JSONObject jsonResponse) {
    try {
      JSONObject jsonDetail = jsonResponse.getJSONObject("response");

      JSONArray errors = jsonDetail.getJSONArray("dependencyErrors");
      String[] dependencyErrors = new String[errors.length()];
      for (int i = 0; i < errors.length(); i++) {
        dependencyErrors[i] = errors.getString(i);
      }

      org.openbravo.services.webservice.Module[] modulesToInstall = Module
          .fromJson(jsonDetail.getJSONArray("modulesToInstall"));
      org.openbravo.services.webservice.Module[] modulesToUpdate = Module
          .fromJson(jsonDetail.getJSONArray("modulesToUpdate"));

      boolean validConfing = jsonDetail.getBoolean("validConfiguration");
      return new ModuleInstallDetail(dependencyErrors, modulesToInstall, modulesToUpdate,
          validConfing);
    } catch (Exception e) {
      throw new OBException(e);
    }
  }
}
