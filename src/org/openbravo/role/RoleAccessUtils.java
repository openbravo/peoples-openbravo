package org.openbravo.role;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.query.Query;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.access.Role;

public class RoleAccessUtils {

  public static boolean isAutoRole(String role) {
    // @formatter:off
    final String roleQryStr = "select r.manual"
    + " from ADRole r"
    + " where r.id= :targetRoleId"
    + " and r.active= 'Y'";
    // @formatter:on
    final Query<Boolean> qry = SessionHandler.getInstance()
        .createQuery(roleQryStr, Boolean.class)
        .setParameter("targetRoleId", role);
    return !qry.uniqueResult();
  }

  public static String getUserLevel(String role) {
    // @formatter:off
    final String roleQryStr = "select r.userLevel"
    + " from ADRole r"
    + " where r.id= :targetRoleId"
    + " and r.active= 'Y'";
    // @formatter:on
    final Query<String> qry = SessionHandler.getInstance()
        .createQuery(roleQryStr, String.class)
        .setParameter("targetRoleId", role);
    return qry.uniqueResult();
  }

  public static List<String> getOrganizationsForAutoRoleByClient(Role role) {
    return getOrganizationsForAutoRoleByClient(role.getClient().getId(), role.getId());
  }

  public static List<String> getOrganizationsForAutoRoleByClient(String clientId, String roleId) {
    String userLevel = getUserLevel(roleId);
    List<String> organizations = new ArrayList<>();

    // " CO" Client/Organization level: *, other Orgs (but *)
    // " O" Organization level: Orgs (but *) [isOrgAdmin=Y]
    if (StringUtils.equals(userLevel, " CO") || StringUtils.equals(userLevel, "  O")) {
      // @formatter:off
      final String orgsQryStr = "select o.id"
          + " from Organization o"
          + " where o.client.id= :clientId"
          + "   and o.id <>'0'"
          + "   and o.active= 'Y' "
          + "   and not exists ( select 1 "
          + "   from ADRoleOrganization roa where (o.id=roa.organization.id)"
          + "   and roa.role.id= :roleId"
          + "   and roa.active= 'N')"
          + " order by o.id desc";
      // @formatter:on
      final Query<String> qry = SessionHandler.getInstance()
          .createQuery(orgsQryStr, String.class)
          .setParameter("clientId", clientId)
          .setParameter("roleId", roleId);
      organizations.addAll(qry.list());
    }

    // Client or System level: Only *
    if (StringUtils.equals(userLevel, " C") || StringUtils.equals(userLevel, "S")
        || StringUtils.equals(userLevel, " CO")) {
      organizations.add("0");
    }
    return organizations;
  }
}
