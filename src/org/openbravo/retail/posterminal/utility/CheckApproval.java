/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.utils.FormatUtilities;

public class CheckApproval extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    try {
      String username = jsonsent.getString("u");
      String password = jsonsent.getString("p");
      String type = jsonsent.getString("approvalType");

      JSONObject result = new JSONObject();

      OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
      qUser.add(Restrictions.eq(User.PROPERTY_USERNAME, username));
      qUser.add(Restrictions.eq(User.PROPERTY_PASSWORD, FormatUtilities.sha1Base64(password)));
      if (qUser.list().size() == 0) {
        System.out.println();
        result.put("status", 1);
        JSONObject jsonError = new JSONObject();
        jsonError.put("message", "invalid user/password");
        result.put("error", jsonError);
      } else {
        String whereClause = "as p" + //
            " where property = :type" + //
            "   and (userContact = :user" + //
            "        or exists (from ADUserRoles r" + //
            "                  where r.role = p.visibleAtRole" + //
            "                    and r.userContact = :user))";
        OBQuery<Preference> qPreference = OBDal.getInstance().createQuery(Preference.class,
            whereClause);
        qPreference.setNamedParameter("user", qUser.list().get(0));
        qPreference.setNamedParameter("type", type);
        if (qPreference.count() > 0) {
          result.put("status", 0);
          result.put("data", "ok");
        } else {
          result.put("status", 1);
          JSONObject jsonError = new JSONObject();
          jsonError.put("message", "missing pref");
          result.put("error", jsonError);
        }
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
