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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.materialmgmt;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.access.Role;
import org.openbravo.model.ad.access.RoleOrganization;
import org.openbravo.service.datasource.DefaultDataSourceService;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manual datasource that creates a tree of characteristics with their values. Intended to be used
 * from characteristics filter.
 * 
 * The tree is generated in a single request without pagination or extra requests for child nodes as
 * volumes in it are expected to be small.
 * 
 * 
 * @author alostale
 * 
 */
public class ProductCharacteristicsDS extends DefaultDataSourceService {
  final static Logger log = LoggerFactory.getLogger(ProductCharacteristicsDS.class);

  final static int CHAR_ID = 0;
  final static int CHAR_NAME = 1;
  final static int VAL_ID = 2;
  final static int VAL_NAME = 3;
  final static int VAL_PARENT = 4;

  @Override
  public String fetch(Map<String, String> parameters) {
    OBContext.setAdminMode(true);
    try {

      StringBuilder hqlBuilder = new StringBuilder();
      hqlBuilder.append(" select c.id, c.name, v.id, v.name, tn.reportSet ");
      hqlBuilder.append(" from ADTreeNode tn, ");
      hqlBuilder.append("      CharacteristicValue v, ");
      hqlBuilder.append("      Characteristic c ");
      hqlBuilder.append(" where tn.tree.typeArea ='CH'");
      hqlBuilder.append(" and tn.node = v.id");
      hqlBuilder.append(" and v.characteristic = c");
      hqlBuilder.append(this.getClientOrgFilter());
      hqlBuilder.append(" order by c.name, ");
      hqlBuilder.append("          coalesce(tn.reportSet, '-1'), ");
      hqlBuilder.append("          tn.sequenceNumber ");

      String hql = hqlBuilder.toString();

      Query qTree = OBDal.getInstance().getSession().createQuery(hql);

      String currentCharId = null;
      JSONArray responseData = new JSONArray();
      for (Object rawNode : qTree.list()) {
        Object[] node = (Object[]) rawNode;
        String charId = (String) node[CHAR_ID];

        if (!charId.equals(currentCharId)) {
          currentCharId = charId;
          // new characteristic
          JSONObject characteristic = new JSONObject();
          characteristic.put("id", charId);
          characteristic.put("_identifier", node[CHAR_NAME]);
          characteristic.put("showOpenIcon", true);
          characteristic.put("isCharacteristic", true);
          characteristic
              .put(
                  "icon",
                  "../web/org.openbravo.userinterface.smartclient/openbravo/skins/Default/org.openbravo.client.application/images/form/sectionItem-ico.png");
          // TODO: skinnable icon
          responseData.put(characteristic);
        }

        JSONObject value = new JSONObject();
        String parentId = (String) node[VAL_PARENT];
        parentId = "0".equals(parentId) ? charId : parentId;
        value.put("id", node[VAL_ID]);
        value.put("_identifier", node[VAL_NAME]);
        value.put("parentId", parentId);
        value.put("characteristic", charId);
        value.put("characteristic$_identifier", node[CHAR_NAME]);

        responseData.put(value);
      }

      final JSONObject jsonResult = new JSONObject();
      final JSONObject jsonResponse = new JSONObject();

      jsonResponse.put(JsonConstants.RESPONSE_DATA, responseData);
      jsonResponse.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      jsonResponse.put(JsonConstants.RESPONSE_TOTALROWS, responseData.length());
      jsonResponse.put(JsonConstants.RESPONSE_STARTROW, 0);
      jsonResponse.put(JsonConstants.RESPONSE_ENDROW, responseData.length() - 1);
      jsonResult.put(JsonConstants.RESPONSE_RESPONSE, jsonResponse);

      return jsonResult.toString();
    } catch (Throwable t) {
      log.error("Error building characteristics tree", t);
      return JsonUtils.convertExceptionToJson(t);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getClientOrgFilter() {
    String clientId = OBContext.getOBContext().getCurrentClient().getId();
    final Set<String> orgs = new HashSet<String>();
    OrganizationStructureProvider orgStructure = OBContext.getOBContext()
        .getOrganizationStructureProvider();

    // Role in OBContext has not organization list initialized, force reload to attach to current
    // DAL's session
    Role currentRole = OBDal.getInstance().get(Role.class,
        OBContext.getOBContext().getRole().getId());

    // Adding organizations in the trees of all granted ones
    for (RoleOrganization org : currentRole.getADRoleOrganizationList()) {
      orgs.addAll(orgStructure.getNaturalTree((String) DalUtil.getId(org.getOrganization())));
    }

    StringBuilder hqlBuilder = new StringBuilder();
    hqlBuilder.append(" and c.client.id = '" + clientId + "' ");
    hqlBuilder.append(" and c.organization.id in (");
    boolean addComma = false;
    for (String org : orgs) {
      if (addComma) {
        hqlBuilder.append(",");
      }
      hqlBuilder.append("'" + org + "'");
      addComma = true;
    }
    hqlBuilder.append(") ");
    return hqlBuilder.toString();
  }
}
