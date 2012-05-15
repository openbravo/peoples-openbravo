/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.retail.posterminal;

import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonToDataConverter;

public class ProcessHQLQuery implements JSONProcess {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {

    SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(jsonsent.getString("query"),
        jsonsent.optString("client"), jsonsent.optString("organization"));

    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(querybuilder.getHQLQuery());

    if (jsonsent.has("parameters")) {
      JSONObject jsonparams = jsonsent.getJSONObject("parameters");
      Iterator<?> it = jsonparams.keys();
      while (it.hasNext()) {
        String key = (String) it.next();
        Object value = jsonparams.get(key);
        if (value instanceof JSONObject) {
          JSONObject jsonvalue = (JSONObject) value;
          query.setParameter(
              key,
              JsonToDataConverter.convertJsonToPropertyValue(
                  PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
        } else {
          query.setParameter(key,
              JsonToDataConverter.convertJsonToPropertyValue(PropertyByType.infer(value), value));

        }
      }
    }

    List<?> listdata = query.list();
    String[] aliases = query.getReturnAliases();

    return JSONRowConverter.buildResponse(listdata, aliases);
  }
}
