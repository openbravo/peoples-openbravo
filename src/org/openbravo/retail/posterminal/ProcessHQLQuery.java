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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonToDataConverter;

public abstract class ProcessHQLQuery implements JSONProcess {

  protected abstract String getQuery(JSONObject jsonsent) throws JSONException;

  protected boolean isAdminMode() {
    return false;
  }

  @Override
  public final void exec(Writer w, JSONObject jsonsent) throws IOException, ServletException {

    try {

      if (isAdminMode()) {
        OBContext.setAdminMode();
      }

      Long lastUpdated = jsonsent.has("lastUpdated")
          && !jsonsent.get("lastUpdated").equals("undefined") ? jsonsent.getLong("lastUpdated")
          : null;

      SimpleQueryBuilder querybuilder = new SimpleQueryBuilder(getQuery(jsonsent),
          jsonsent.optString("client"), jsonsent.optString("organization"),
          lastUpdated != null ? new Date(lastUpdated) : null);

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(querybuilder.getHQLQuery());

      List<String> queryParams = new ArrayList<String>(Arrays.asList(query.getNamedParameters()));

      if (jsonsent.has("parameters")) {
        JSONObject jsonparams = jsonsent.getJSONObject("parameters");
        Iterator<?> it = jsonparams.keys();
        while (it.hasNext()) {
          String key = (String) it.next();
          if (!queryParams.contains(key)) {
            continue;
          }
          queryParams.remove(key);

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

      // XXX: for standard params (client, org, pos), no need to add as extra
      if (!queryParams.isEmpty()) {
        for (String param : queryParams) {
          if (jsonsent.has(param)) {
            Object value = jsonsent.get(param);
            if (value instanceof JSONObject) {
              JSONObject jsonvalue = (JSONObject) value;
              query.setParameter(
                  param,
                  JsonToDataConverter.convertJsonToPropertyValue(
                      PropertyByType.get(jsonvalue.getString("type")), jsonvalue.get("value")));
            } else {
              query.setParameter(param, JsonToDataConverter.convertJsonToPropertyValue(
                  PropertyByType.infer(value), value));
            }
          }
        }
      }

      ScrollableResults listdata = query.scroll(ScrollMode.FORWARD_ONLY);
      String[] aliases = query.getReturnAliases();

      JSONRowConverter.buildResponse(w, Scroll.create(listdata), aliases);
    } catch (QueryException e) {
      JSONRowConverter.addJSONExceptionFields(w, e);
    } catch (JSONException e) {
      JSONRowConverter.addJSONExceptionFields(w, e);
    } catch (Exception e) {
      JSONRowConverter.addJSONExceptionFields(w, e);
    } finally {
      if (isAdminMode()) {
        OBContext.restorePreviousMode();
      }
    }
  }
}
