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

import javax.persistence.NoResultException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;
import org.openbravo.base.secureApp.LoginUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * Authenticates against the Openbravo user database.
 * 
 * @author mtaal
 */
@Name("obAuthenticator")
@Install(precedence = Install.FRAMEWORK)
public class OBAuthenticator {
  @In
  private Credentials credentials;

  @In
  private Identity identity;

  @In
  private OBUserContext obUserContext;

  public boolean authenticate() {

    try {
      final String userId = LoginUtils.getValidUserId(new DalConnectionProvider(), credentials
          .getUsername(), credentials.getPassword());
      if (userId == null) {
        return false;
      }

      // setup the user context
      obUserContext.setUserContext(userId, null);

      // a user can always only have one role
      if (OBContext.getOBContext().getRole() != null) {
        identity.addRole(OBContext.getOBContext().getRole().getName());
      }

      return true;
    } catch (NoResultException ex) {
      return false;
    }
  }

  @Observer("org.jboss.seam.security.loginSuccessful")
  public void updateUserStats() {
    // do something usefull here
  }

  @Observer("org.jboss.seam.security.loggedOut")
  public void afterLogOut() {
    OBContext.setOBContext((OBContext) null);
  }

}