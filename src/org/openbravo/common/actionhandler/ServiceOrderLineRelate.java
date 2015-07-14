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
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.order.OrderlineServiceRelation;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceOrderLineRelate extends BaseProcessActionHandler {
  private static final Logger log = LoggerFactory.getLogger(ServiceOrderLineRelate.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode(true);
    JSONObject errorMessage = new JSONObject();
    try {
      jsonRequest = new JSONObject(content);
      log.debug("{}", jsonRequest);

      JSONArray selectedLines = jsonRequest.getJSONObject("_params").getJSONObject("grid")
          .getJSONArray("_selection");

      final Client serviceProductClient = (Client) OBDal.getInstance().getProxy(Client.ENTITY_NAME,
          jsonRequest.getString("inpadClientId"));
      final Organization serviceProductOrg = (Organization) OBDal.getInstance().getProxy(
          Organization.ENTITY_NAME, jsonRequest.getString("inpadOrgId"));
      OrderLine mainOrderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
          jsonRequest.getString("inpcOrderlineId"));

      mainOrderLine.getOrderlineServiceRelationList().removeAll(
          mainOrderLine.getOrderlineServiceRelationList());
      OBDal.getInstance().save(mainOrderLine);
      OBDal.getInstance().flush();
      mainOrderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
          jsonRequest.getString("inpcOrderlineId"));
      // Adding new rows
      for (int i = 0; i < selectedLines.length(); i++) {
        JSONObject selectedLine = selectedLines.getJSONObject(i);
        log.debug("{}", selectedLine);
        final OrderLine orderLine = (OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
            selectedLine.getString(OrderLine.PROPERTY_ID));

        BigDecimal lineAmount = new BigDecimal(selectedLine.getDouble("amount"));
        BigDecimal lineQuantity = new BigDecimal(selectedLine.getDouble("orderedQuantity"));

        OrderlineServiceRelation olsr = OBProvider.getInstance()
            .get(OrderlineServiceRelation.class);
        olsr.setClient(serviceProductClient);
        olsr.setOrganization(serviceProductOrg);
        olsr.setOrderlineRelated(orderLine);
        olsr.setSalesOrderLine(mainOrderLine);
        olsr.setAmount(lineAmount);
        olsr.setQuantity(lineQuantity);
        OBDal.getInstance().save(olsr);
        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
        }
      }

      errorMessage.put("severity", "success");
      errorMessage.put("title", OBMessageUtils.messageBD("Success"));
      jsonRequest.put("message", errorMessage);
    } catch (Exception e) {
      log.error("Error in ServiceOrderLineRelate Action Handler", e);
      OBDal.getInstance().rollbackAndClose();
      try {
        jsonRequest = new JSONObject();
        String message = OBMessageUtils.parseTranslation(new DalConnectionProvider(false),
            RequestContext.get().getVariablesSecureApp(), OBContext.getOBContext().getLanguage()
                .getLanguage(), e.getMessage());
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }
}
