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

import org.openbravo.client.kernel.ComponentProvider;

@ComponentProvider.Qualifier("EB3C41F0973A4EDA91E475833792A6D4")
public class ProductSimpleSelectorTransformer extends PriceExceptionAbstractTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {

    String documentDate = getDocumentDate(requestParameters, "documentDate");
    String orgList = getOrganizationsList(requestParameters, "inpadOrgId");

    String transformedHql = hqlQuery.replace("@documentDate@", documentDate);
    transformedHql = transformedHql.replace("@orgList@", orgList);
    return transformedHql;
  }
}
