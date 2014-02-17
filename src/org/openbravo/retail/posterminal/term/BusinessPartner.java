/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class BusinessPartner extends ProcessHQLQuery {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    Organization org = OBDal.getInstance().get(Organization.class,
        OBContext.getOBContext().getCurrentOrganization().getId());
    OBPOSApplications terminal = POSUtils.getTerminalById(RequestContext.get()
        .getSessionAttribute("POSTerminal").toString());
    String strBPId = terminal.getDefaultCustomer() != null ? terminal.getDefaultCustomer().getId()
        : org.getObretcoCBpartner().getId();
    return Arrays.asList(new String[] { "SELECT id as id " + "FROM BusinessPartner as bp "
        + "WHERE bp.id = '" + strBPId + "'" });
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
