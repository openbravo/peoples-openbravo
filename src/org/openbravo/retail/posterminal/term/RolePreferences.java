package org.openbravo.retail.posterminal.term;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.process.JSONProcess;
import org.openbravo.mobile.core.process.JSONRowConverter;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.service.json.JsonConstants;

public class RolePreferences implements JSONProcess {

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    OBContext.setAdminMode(true);
    try {
      String whereClause = "where reference.id = 'A26BA480E2014707B47257024C3CBFF7' and module.id in "
          + POSUtils.getRetailDependantModuleIds() + "";
      OBQuery<org.openbravo.model.ad.domain.List> refLists = OBDal.getInstance().createQuery(
          org.openbravo.model.ad.domain.List.class, whereClause);
      List<String> preferenceList = new ArrayList<String>();
      for (org.openbravo.model.ad.domain.List listRef : refLists.list()) {
        preferenceList.add(listRef.getSearchKey());
      }
      // List of all permissions with its defaults in POS
      buildResponse(w, preferenceList);

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public void buildResponse(Writer w, List<String> prefs) throws IOException {

    final int startRow = 0;
    int rows = 0;
    Throwable t = null;

    try {
      w.write("\"data\":[");
      while (rows < prefs.size()) {
        if (rows > 0) {
          w.write(',');
        }
        JSONObject json = new JSONObject();
        json.put("key", prefs.get(rows));
        json.put("value", getPreferenceValue(prefs.get(rows)));
        w.write(json.toString());
        rows++;
      }
    } catch (JSONException e) {
      t = e;
    } finally {
      w.write("],");
      if (t == null) {
        // Add success fields
        w.write("\"");
        w.write(JsonConstants.RESPONSE_STARTROW);
        w.write("\":");
        w.write(Integer.toString(startRow));
        w.write(",\"");
        w.write(JsonConstants.RESPONSE_ENDROW);
        w.write("\":");
        w.write(Integer.toString(rows > 0 ? rows + startRow - 1 : 0));
        w.write(",\"");
        if (rows == 0) {
          w.write(JsonConstants.RESPONSE_TOTALROWS);
          w.write("\":0,\"");
        }
        w.write(JsonConstants.RESPONSE_STATUS);
        w.write("\":");
        w.write(Integer.toString(JsonConstants.RPCREQUEST_STATUS_SUCCESS));
      } else {
        JSONRowConverter.addJSONExceptionFields(w, t);
      }
    }
  }

  private boolean getPreferenceValue(String p) {
    try {
      return "Y".equals(Preferences.getPreferenceValue(p, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      return false;
    }
  }

}
