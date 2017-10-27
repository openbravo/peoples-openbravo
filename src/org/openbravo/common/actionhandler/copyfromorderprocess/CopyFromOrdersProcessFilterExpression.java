package org.openbravo.common.actionhandler.copyfromorderprocess;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;

public class CopyFromOrdersProcessFilterExpression implements FilterExpression {

  @Override
  public String getExpression(Map<String, String> requestMap) {
    try {
      String context = requestMap.get("context");
      JSONObject contextJSON = new JSONObject(context);
      String organizationId = contextJSON.getString("inpadOrgId");
      Organization organization = OBDal.getInstance().get(Organization.class, organizationId);
      OrganizationStructureProvider osp = OBContext.getOBContext()
          .getOrganizationStructureProvider();
      Organization legalEntity = osp.getLegalEntity(organization);
      return legalEntity.getId();
    } catch (Exception e) {
      return null;
    }
  }
}
