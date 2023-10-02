package org.openbravo.role;

import org.hibernate.query.Query;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.access.Role;

public class RoleAccessUtils {

  public static boolean isAutoRole(Role role) {
    // @formatter:off
    final String roleQryStr = "select r.manual"
    + " from ADRole r"
    + " where r.id= :targetRoleId"
    + " and r.active= 'Y'";
    // @formatter:on
    final Query<Boolean> qry = SessionHandler.getInstance()
        .createQuery(roleQryStr, Boolean.class)
        .setParameter("targetRoleId", role.getId());
    return !qry.uniqueResult();
  }
}
