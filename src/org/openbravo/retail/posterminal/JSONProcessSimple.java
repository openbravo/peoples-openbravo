/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class JSONProcessSimple implements JSONProcess {

  public abstract JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException;

  public final void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {
    try {
      String s = exec(jsonsent).toString();
      if (s.startsWith("{") && s.endsWith("}")) {
        // write only the properties, brackets are written outside.
        w.write(s.substring(1, s.length() - 1));
      } else {
        throw new JSONException("Result is not a JSON object.");
      }
    } catch (JSONException e) {
      JSONRowConverter.addJSONExceptionFields(w, e);
    }
  }
}
