/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.locale;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.ad.access.Role;

/**
 * After selecting default POS role for a user, default POS language is set to that user if the role
 * has it defined
 * 
 * @author alostale
 * 
 */
public class DefaultPOSLanguageCallout extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    OBContext.setAdminMode(true);
    try {
      String roleID = info.getStringParameter("inpemObposDefaultPosRole", null);
      if (!StringUtils.isEmpty(roleID)) {
        Role role = OBDal.getInstance().get(Role.class, roleID);
        if (role.getOBPOSDefaultPosLanguage() != null) {
          info.addResult("inpdefaultAdLanguage", role.getOBPOSDefaultPosLanguage().getLanguage());
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
