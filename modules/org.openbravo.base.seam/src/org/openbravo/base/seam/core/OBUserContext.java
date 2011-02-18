/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2009 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.base.seam.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.access.UserRoles;

/**
 * Is a wrapper around the {@link OBContext} class.
 * 
 * Provides additional convenience methods to get roles and set the current role of the user.
 * 
 * @author mtaal
 */
@Name("obUserContext")
@AutoCreate
@Scope(ScopeType.SESSION)
@Install(precedence = Install.FRAMEWORK)
public class OBUserContext implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * @return the instance of the user context in the session scope.
   */
  public static OBUserContext get() {
    return (OBUserContext) Component.getInstance(OBUserContext.class, ScopeType.SESSION);
  }

  @In
  private EntityManager entityManager;

  @In
  private Identity identity;

  /**
   * Return the current {@link OBContext} instance.
   * 
   * @return the OBContext instance.
   * @throws NotLoggedInException
   */
  public OBContext getOBContext() {
    if (OBContext.getOBContext() == null) {
      throw new NotLoggedInException();
    }

    return OBContext.getOBContext();
  }

  /**
   * Returns the available {@link Role} instances valid for the current user.
   * 
   * @return the list of roles
   */
  public List<Role> getRoles() {
    if (OBContext.getOBContext() == null) {
      throw new NotLoggedInException();
    }

    final String qryStr = "select ur from " + UserRoles.class.getName() + " ur where " + " ur."
        + UserRoles.PROPERTY_USERCONTACT + "." + User.PROPERTY_ID + "='"
        + OBContext.getOBContext().getUser().getId() + "' and ur." + UserRoles.PROPERTY_ACTIVE
        + "='Y' and ur." + UserRoles.PROPERTY_ROLE + "." + Role.PROPERTY_ACTIVE
        + "='Y' order by ur." + UserRoles.PROPERTY_ROLE + "." + Role.PROPERTY_NAME + " asc";
    @SuppressWarnings("unchecked")
    final List<UserRoles> userRoles = (List<UserRoles>) entityManager.createQuery(qryStr)
        .getResultList();
    final List<Role> roles = new ArrayList<Role>();
    for (UserRoles userRole : userRoles) {
      if (!roles.contains(userRole.getRole())) {
        roles.add(userRole.getRole());
      }
    }
    return roles;
  }

  /**
   * The id of the current {@link Role} ({@link OBContext#getRole()}.
   * 
   * @return the id of the current role.
   * @throws NotLoggedInException
   */
  public String getRoleId() {
    if (OBContext.getOBContext() == null) {
      throw new NotLoggedInException();
    }
    if (OBContext.getOBContext().getRole() != null) {
      return OBContext.getOBContext().getRole().getId();
    }
    return null;
  }

  /**
   * Resets the {@link OBContext if the role has changed from the current role (@link
   * OBContext#getRole()}.
   * 
   * If the role id is the same as the current role then nothing is done in this method.
   * 
   * If the current role changes this role is removed from the identify
   * {@link Identity#removeRole(String)} and the new role is set/added through the method
   * {@link Identity#addRole(String)}.
   * 
   * @param roleId
   *          the id of the role to set
   * @throws NotLoggedInException
   */
  public void setRoleId(String roleId) {

    if (OBContext.getOBContext() == null) {
      throw new NotLoggedInException();
    }

    // don't do nullify
    if (roleId == null) {
      return;
    }

    if (OBContext.getOBContext().getRole() != null) {
      final String theRoleId = OBContext.getOBContext().getRole().getId();
      if (theRoleId.equals(roleId)) {
        // nothing changed go away
        return;
      }
    }

    final Role currentRole = OBContext.getOBContext().getRole();
    identity.removeRole(currentRole.getName());

    final String userId = OBContext.getOBContext().getUser().getId();
    setUserContext(userId, roleId);

    if (OBContext.getOBContext().getRole() != null) {
      identity.addRole(OBContext.getOBContext().getRole().getName());
    }

  }

  /**
   * Creates a {@link OBContext} instance and sets it using the userId and roleId. The roleId maybe
   * null.
   * 
   * When the methods returns the OBContext can be retrieved through the {@link #getOBContext()}
   * method and it is available in the HttpSession.
   * 
   * @param userId
   *          the id of the user to set in the OBContext
   * @param roleId
   *          the id of the role to set, maybe null
   */
  public void setUserContext(String userId, String roleId) {
    if (roleId == null) {
      OBContext.setOBContext(userId);
    } else {
      OBContext.setOBContext(userId, roleId, null, null);
    }

    OBContext.setOBContextInSession((HttpServletRequest) FacesContext.getCurrentInstance()
        .getExternalContext().getRequest(), OBContext.getOBContext());
  }
}