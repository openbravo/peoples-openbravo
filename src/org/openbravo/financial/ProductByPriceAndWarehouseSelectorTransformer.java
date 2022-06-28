package org.openbravo.financial;

import java.util.Map;

import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.json.JsonUtils;

@ComponentProvider.Qualifier("2E64F551C7C4470C80C29DBA24B34A5F")
public class ProductByPriceAndWarehouseSelectorTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {

    String clientId = requestParameters.get("inpadClientId");
    String organizationId = requestParameters.get("inpadOrgId");
    String documentDate = getDocumentDate(requestParameters);
    String orgList = getOrganizationsList(requestParameters, organizationId);

    String transformedHql = hqlQuery.replace("@documentDate@", documentDate);
    transformedHql = transformedHql.replace("@orgList@", orgList);
    transformedHql = transformedHql.replace("@AD_Client_Id@", "'" + clientId + "'");
    transformedHql = transformedHql.replace("@AD_Org_Id@", "'" + organizationId + "'");

    return transformedHql;
  }

  private String getDocumentDate(Map<String, String> requestParameters) {
    String documentDate = requestParameters.containsKey("documentDate")
        ? "TO_DATE('" + requestParameters.get("documentDate") + "','"
            + JsonUtils.createDateFormat().toPattern() + "')"
        : "null";
    return documentDate;
  }

  private String getOrganizationsList(Map<String, String> requestParameters,
      String organizationId) {
    return StringCollectionUtils.commaSeparated(
        new OrganizationStructureProvider().getParentList(organizationId, true), true);
  }

}
