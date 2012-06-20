package org.openbravo.retail.posterminal.term;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.retail.posterminal.JSONProcess;
import org.openbravo.retail.posterminal.JSONRowConverter;
import org.openbravo.service.json.JsonConstants;

public class RolePreferences implements JSONProcess {

  @Override
  public void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {

    // List of all permissions with its defaults in POS
    buildResponse(w, new Pref[] { new Pref("OBPOS_order.changePrice", false),
        new Pref("OBPOS_order.discount", false), new Pref("OBPOS_payment.cash", true),
        new Pref("OBPOS_payment.voucher", true), new Pref("OBPOS_payment.card", true) });
  }


  public void buildResponse(Writer w, Pref[] prefs)
      throws IOException {

    final int startRow = 0;
    int rows = 0;
    Throwable t = null;

    try {
      w.write("\"data\":[");
      while (rows < prefs.length) {
        if (rows > 0) {
          w.write(',');
        }
        JSONObject json = new JSONObject();
        json.put("key", prefs[rows].getKey());
        json.put("value", getPreferenceValue(prefs[rows].getKey(), prefs[rows].getDefault()));
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

  private boolean getPreferenceValue(String p, boolean def) {
    try {
      return "Y".equals(Preferences.getPreferenceValue(p, true, OBContext.getOBContext()
          .getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(), OBContext
          .getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      return def;
    }
  }

  private static class Pref {
    private String key;
    private boolean def;

    public Pref(String key, boolean def) {
      this.key = key;
      this.def = def;
    }

    public String getKey() {
      return key;
    }

    public boolean getDefault() {
      return def;
    }
  }
}
