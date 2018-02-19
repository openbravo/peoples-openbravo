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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.filterexpression;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FundsTransferDefaultValuesExpression implements FilterExpression {
  private static final Logger log = LoggerFactory
      .getLogger(FundsTransferDefaultValuesExpression.class);

  @Override
  public String getExpression(Map<String, String> requestMap) {
    String strCurrentParam = requestMap.get("currentParam");
    Parameters param = Parameters.getParameter(strCurrentParam);
    try {

      switch (param) {
      case GLItem:
        return getOrgGLItem(requestMap);
      case Description:
        return getDefaultDescription();
      }
    } catch (Exception e) {
      log.error("Error trying to get default value of " + strCurrentParam + " " + e.getMessage(), e);
      return null;
    }
    log.error("No default value found for param: ");
    return null;
  }

  private enum Parameters {
    GLItem("glitem"), Description("description");

    private String columnname;

    Parameters(String columnname) {
      this.columnname = columnname;
    }

    public String getColumnName() {
      return this.columnname;

    }

    static Parameters getParameter(String strColumnName) {
      for (Parameters parameter : Parameters.values()) {
        if (strColumnName.equals(parameter.getColumnName())) {
          return parameter;
        }
      }
      return null;
    }
  }

  private String getDefaultDescription() {
    return OBMessageUtils.messageBD("FundsTransfer");
  }

  private String getOrgGLItem(Map<String, String> requestMap) {
    String orgId = "";
    try {
      JSONObject context = new JSONObject(requestMap.get("context"));
      if (context.has("ad_org_id") && context.get("ad_org_id") != JSONObject.NULL
          && StringUtils.isNotEmpty(context.getString("ad_org_id"))) {
        orgId = context.getString("ad_org_id");
      }
      if (StringUtils.isEmpty(orgId) && context.has("inpadOrgId")
          && context.get("inpadOrgId") != JSONObject.NULL
          && StringUtils.isNotEmpty(context.getString("inpadOrgId"))) {
        orgId = context.getString("inpadOrgId");
      }
      OBContext.setAdminMode(true);
      if (StringUtils.isBlank(orgId)) {
        // No organization
        return null;
      }

      final Organization org = OBDal.getInstance().get(Organization.class, orgId);
      if (org == null) {
        // No organization
        return null;
      }

      String cGLItemId = getOrgGLItemRecursive(org);
      if (StringUtils.isNotEmpty(cGLItemId)) {
        // Get default GL Item of organization for Funds Transfer from organization tree
        return cGLItemId;
      }

      return null;
    } catch (Exception e) {
      log.error("Impossible to get default GL Item for Funds Transfer\n" + " of organization id "
          + orgId, e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getOrgGLItemRecursive(Organization org) {

    if (org.getAPRMGlitem() != null) {
      // Get default GL Item of organization for Funds Transfer
      return org.getAPRMGlitem().getId();
    }

    if (StringUtils.equals(org.getId(), "0")) {
      // * organization doesn't have parents
      return null;
    }

    // Loop through parent organization list
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        org.getClient().getId());
    List<String> parentOrgIds = osp.getParentList(org.getId(), false);
    for (String orgId : parentOrgIds) {
      Organization parentOrg = OBDal.getInstance().get(Organization.class, orgId);
      if (parentOrg.getAPRMGlitem() != null) {
        return parentOrg.getAPRMGlitem().getId();
      }
    }
    return null;
  }

}
