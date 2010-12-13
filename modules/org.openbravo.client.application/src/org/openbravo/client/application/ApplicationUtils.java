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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;

/**
 * Utility class for common operations
 * 
 * @author iperdomo
 */
public class ApplicationUtils {

  private static Logger log = Logger.getLogger(ApplicationUtils.class);

  public static boolean isClientAdmin() {
    return OBContext.getOBContext().getRole().isClientAdmin();
  }

  public static boolean isOrgAdmin() {
    return getAdminOrgs().size() > 0;
  }

  public static boolean isRoleAdmin() {
    return getAdminRoles().size() > 0;
  }

  public static List<RoleOrganization> getAdminOrgs() {
    final Role role = OBContext.getOBContext().getRole();
    try {
      OBContext.setAdminMode();

      final OBCriteria<RoleOrganization> roleOrgs = OBDal.getInstance().createCriteria(
          RoleOrganization.class);
      roleOrgs.add(Expression.eq(RoleOrganization.PROPERTY_ROLE, role));
      roleOrgs.add(Expression.eq(RoleOrganization.PROPERTY_ORGADMIN, true));

      return roleOrgs.list();

    } catch (Exception e) {
      log.error("Error checking Role is organization admin: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return Collections.emptyList();
  }

  public static List<UserRoles> getAdminRoles() {
    final User user = OBContext.getOBContext().getUser();
    try {
      OBContext.setAdminMode();

      final OBCriteria<UserRoles> userRoles = OBDal.getInstance().createCriteria(UserRoles.class);
      userRoles.add(Expression.eq(UserRoles.PROPERTY_USERCONTACT, user));
      userRoles.add(Expression.eq(UserRoles.PROPERTY_ROLEADMIN, true));

      return userRoles.list();

    } catch (Exception e) {
      log.error("Error checking if User is role admin: " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    return Collections.emptyList();
  }

  /**
   * Returns an Object with the Value of the Parameter Value. This object can be a String, a
   * java.util.Data or a BigDecimal.
   * 
   * @param parameterValue
   *          the Parameter Value we want to get the Value from.
   * @return the Value of the Parameter Value.
   */
  public static Object getParameterValue(ParameterValue parameterValue) {
    if (parameterValue.getValueDate() != null) {
      return parameterValue.getValueDate();
    } else if (parameterValue.getValueNumber() != null) {
      return parameterValue.getValueNumber();
    } else if (parameterValue.getValueString() != null) {
      return parameterValue.getValueString();
    }
    return null;
  }

  /**
   * Returns the Fixed value of the given parameter. If the value is a JS expression it returns the
   * result of the expression based on the parameters passed in from the request.
   * 
   * @param parameters
   *          the parameters passed in from the request
   * @param parameter
   *          the parameter we want to get the Fixed Value from
   * @return the Fixed Value of the parameter
   */
  public static String getParameterFixedValue(Map<String, String> parameters, Parameter parameter) {
    if (parameter.isEvaluateFixedValue()) {
      try {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("js");

        engine.put("OB", new OBBindings(OBContext.getOBContext(), parameters));

        return (String) engine.eval(parameter.getFixedValue());
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
      return null;
    } else {
      return parameter.getFixedValue();
    }
  }
}
