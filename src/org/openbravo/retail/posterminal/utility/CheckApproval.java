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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.domain.Preference;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.utils.FormatUtilities;

public class CheckApproval extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(false);
    try {
      JSONArray approvalType = new JSONArray();
      String username = jsonsent.getString("u");
      String password = jsonsent.getString("p");
      if (jsonsent.getString("approvalType") != null) {
        approvalType = new JSONArray(jsonsent.getString("approvalType"));
      }
      JSONObject result = new JSONObject();

      OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
      qUser.add(Restrictions.eq(User.PROPERTY_USERNAME, username));
      qUser.add(Restrictions.eq(User.PROPERTY_PASSWORD, FormatUtilities.sha1Base64(password)));
      if (qUser.list().size() == 0) {
        System.out.println();
        result.put("status", 1);
        JSONObject jsonError = new JSONObject();
        jsonError.put("message", OBMessageUtils.getI18NMessage("OBPOS_InvalidUserPassword", null));
        result.put("error", jsonError);
      } else {
        String approvals = "'" + approvalType.getString(0) + "'";
        for (int i = 1; i < approvalType.length(); i++) {
          approvals = approvals + ",'" + approvalType.getString(i) + "'";
        }

        String whereClause = "as p" + //
            " where property in (" + approvals + ")" + //
            "   and active = true" + //
            "   and searchKey = 'Y'" + //
            "   and (userContact = :user" + //
            "        or exists (from ADUserRoles r" + //
            "                  where r.role = p.visibleAtRole" + //
            "                    and r.userContact = :user))";
        OBQuery<Preference> qPreference = OBDal.getInstance().createQuery(Preference.class,
            whereClause);
        qPreference.setNamedParameter("user", qUser.list().get(0));

        result.put("status", 0);
        JSONObject jsonData = new JSONObject();
        jsonData.put("userId", qUser.list().get(0).getId());
        jsonData.put("canApprove", qPreference.count() == approvalType.length());
        result.put("data", jsonData);
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
