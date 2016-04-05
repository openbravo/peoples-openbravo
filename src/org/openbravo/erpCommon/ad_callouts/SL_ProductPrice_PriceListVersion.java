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
 * All portions are Copyright (C) 2012-2016 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.xmlEngine.XmlDocument;

/* Replaced by {@link org.openbravo.event.ProductPriceObserver.ProductPriceObserver},
 * which always overrides the organization by the price list version one. Note that the
 * UI has a validation to display only Price List Versions belonging to organizations
 * with access for the role
 */
@Deprecated
public class SL_ProductPrice_PriceListVersion extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strPriceListV = vars.getStringParameter("inpmPricelistVersionId");
      String strOrg = vars.getStringParameter("inpadOrgId");
      printPage(response, vars, strPriceListV, strOrg);
    }
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars,
      String strPriceListV, String strOrg) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    // If the role has access to the Price List Version Organization, we set this organization to
    // the record.
    PriceListVersion plv = OBDal.getInstance().get(PriceListVersion.class, strPriceListV);
    final String plvOrgId = plv.getOrganization().getId();
    Role role = OBDal.getInstance().get(Role.class, vars.getRole());
    boolean hasAccessTo = hasRoleOrganizationAccess(role.getId(), plvOrgId)
        || (StringUtils.contains(role.getUserLevel(), "C") && StringUtils.equals(plvOrgId, "0"));

    StringBuilder resultado = new StringBuilder();
    resultado.append("var calloutName='SL_ProductPrice_PriceListVersion';\n\n");
    resultado.append("var respuesta = new Array(");
    resultado
        .append("new Array(\"inpadOrgId\", \"" + ((hasAccessTo) ? plvOrgId : strOrg) + "\"));");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private boolean hasRoleOrganizationAccess(String roleId, String orgId) {
    try {
      OBContext.setAdminMode(false);
      StringBuffer hqlString = new StringBuffer();
      hqlString.append(" select " + RoleOrganization.PROPERTY_ORGANIZATION + ".id");
      hqlString.append(" from " + RoleOrganization.ENTITY_NAME);
      hqlString.append(" where " + RoleOrganization.PROPERTY_ROLE + ".id = :roleId");
      hqlString.append(" and " + RoleOrganization.PROPERTY_ORGANIZATION + ".id = :orgId");
      Query query = OBDal.getInstance().getSession().createQuery(hqlString.toString());
      query.setParameter("roleId", roleId);
      query.setParameter("orgId", orgId);
      query.setMaxResults(1);
      return query.uniqueResult() != null;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
