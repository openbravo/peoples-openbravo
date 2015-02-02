/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;

public class ProcessCashCloseMaster extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    String masterterminal = jsonsent.getString("masterterminal");
    String cashUpId = jsonsent.getString("cashUpId");
    OBCriteria<OBPOSApplications> obCriteria = OBDal.getInstance().createCriteria(
        OBPOSApplications.class);
    obCriteria.add(Restrictions.eq(OBPOSApplications.PROPERTY_MASTERTERMINAL + ".id",
        masterterminal));
    List<OBPOSApplications> applications = obCriteria.list();
    JSONArray data = new JSONArray();
    for (OBPOSApplications application : applications) {
      JSONObject item = new JSONObject();
      item.put("id", application.getId());
      item.put("searchKey", application.getSearchKey());
      item.put("name", application.getName());
      item.put("finish", isCashUpFinish(application.getId(), cashUpId));
      data.put(item);
    }
    result.put("data", data);
    result.put("status", 0);
    return result;
  }

  private boolean isCashUpFinish(String posterminal, String parentCashUp) {
    OBCriteria<OBPOSAppCashup> obCriteria = OBDal.getInstance()
        .createCriteria(OBPOSAppCashup.class);
    obCriteria.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_POSTERMINAL + ".id", posterminal));
    obCriteria
        .add(Restrictions.eq(OBPOSAppCashup.PROPERTY_OBPOSPARENTCASHUP + ".id", parentCashUp));
    List<OBPOSAppCashup> cashUp = obCriteria.list();
    return cashUp.size() > 0 ? cashUp.get(0).isProcessed() && cashUp.get(0).isProcessedbo() : false;
  }

}
