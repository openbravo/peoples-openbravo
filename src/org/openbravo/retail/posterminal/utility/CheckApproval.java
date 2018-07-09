/*
 ************************************************************************************
 * Copyright (C) 2013-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.util.List;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
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
      final String organization = jsonsent.getString("organization");
      final String client = jsonsent.getString("client");
      if (jsonsent.getString("approvalType") != null) {
        approvalType = new JSONArray(jsonsent.getString("approvalType"));
      }
      JSONObject result = new JSONObject();

      OBCriteria<User> qUser = OBDal.getInstance().createCriteria(User.class);
      qUser.add(Restrictions.eq(User.PROPERTY_USERNAME, username));
      qUser.add(Restrictions.eq(User.PROPERTY_PASSWORD, FormatUtilities.sha1Base64(password)));
      List<User> qUserList = qUser.list();

      if (qUserList.size() == 0) {
        result.put("status", 1);
        JSONObject jsonError = new JSONObject();
        jsonError.put("message", OBMessageUtils.getI18NMessage("OBPOS_InvalidUserPassword", null));
        result.put("error", jsonError);
      } else {
        String approvals = "'" + approvalType.getString(0) + "'";
        for (int i = 1; i < approvalType.length(); i++) {
          approvals = approvals + ",'" + approvalType.getString(i) + "'";
        }

        String naturalTreeOrgList = Utility.getInStrSet(OBContext.getOBContext()
            .getOrganizationStructureProvider(client).getNaturalTree(organization));

        String hqlQuery = "select p.property from ADPreference as p"
            + " where property IS NOT NULL "
            + "   and active = true" //
            + "   and (case when length(searchKey)<>1 then 'X' else to_char(searchKey) end) = 'Y'" //
            + "   and (userContact.id = :user" //
            + "        or exists (from ADUserRoles r"
            + "                  where r.role = p.visibleAtRole"
            + "                    and r.userContact.id = :user"
            + "                    and r.active=true))"
            + "   and (p.visibleAtOrganization.id = :org " //
            + "   or p.visibleAtOrganization.id in (:orgList) "
            + "   or p.visibleAtOrganization is null) group by p.property";
        Query<String> preferenceQuery = OBDal.getInstance().getSession()
            .createQuery(hqlQuery, String.class);
        preferenceQuery.setParameter("user", qUserList.get(0).getId());
        preferenceQuery.setParameter("org", organization);
        preferenceQuery.setParameter("orgList", naturalTreeOrgList);

        List<String> preferenceList = preferenceQuery.list();
        if (preferenceList.size() == 0) {
          result.put("status", 1);
          JSONObject jsonError = new JSONObject();
          jsonError.put("message",
              OBMessageUtils.getI18NMessage("OBPOS_UserCannotApprove", new String[] { username }));
          result.put("error", jsonError);
        } else {
          result.put("status", 0);
          JSONObject jsonData = new JSONObject();
          JSONObject jsonPreference = new JSONObject();
          Integer c = 0;
          for (String preference : preferenceList) {
            jsonPreference.put(preference, preference);
            if (approvals.contains(preference)) {
              c++;
            }
          }
          jsonData.put("userId", qUserList.get(0).getId());
          jsonData.put("canApprove", c >= approvalType.length());
          jsonData.put("preference", jsonPreference);
          result.put("data", jsonData);
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
