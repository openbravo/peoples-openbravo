/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.json.JsonConstants;

public class DeliveryPaymentMode extends JSONTerminalProperty {

  final String DEFAULTDELIVERYPAYMENTMODE = "PT";

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);

    try {
      Organization currentOrg = OBContext.getOBContext().getCurrentOrganization();
      String deliveryPaymentMode = currentOrg.getObrdmDeliverypaymentmode();

      if (deliveryPaymentMode == null) {
        OrganizationStructureProvider osp = OBContext.getOBContext()
            .getOrganizationStructureProvider(OBContext.getOBContext().getCurrentClient().getId());
        String parentOrgId = osp.getParentOrg(currentOrg.getId());
        Organization parentOrg;
        while (parentOrgId != null && deliveryPaymentMode == null) {
          parentOrg = OBDal.getInstance().get(Organization.class, parentOrgId);
          deliveryPaymentMode = parentOrg.getObrdmDeliverypaymentmode();
          parentOrgId = osp.getParentOrg(parentOrgId);
        }
      }

      result.put(JsonConstants.RESPONSE_DATA,
          deliveryPaymentMode != null ? deliveryPaymentMode : DEFAULTDELIVERYPAYMENTMODE);
      result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);

      return result;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public String getProperty() {
    return "deliveryPaymentMode";
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

}
