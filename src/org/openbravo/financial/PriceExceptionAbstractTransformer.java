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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 ************************************************************************
 */
package org.openbravo.financial;

import java.util.Map;

import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.json.JsonUtils;

public abstract class PriceExceptionAbstractTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // TODO Auto-generated method stub
    return null;
  }

  protected String getDocumentDate(Map<String, String> requestParameters, String key) {
    String documentDate = requestParameters.containsKey(key)
        ? "TO_DATE('" + requestParameters.get(key) + "','"
            + JsonUtils.createDateFormat().toPattern() + "')"
        : "null";
    return documentDate;
  }

  protected String getOrganizationsList(Map<String, String> requestParameters, String key) {
    return StringCollectionUtils.commaSeparated(
        new OrganizationStructureProvider().getParentList(requestParameters.get(key), true), true);
  }

}
