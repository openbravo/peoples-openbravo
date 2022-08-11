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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;
import org.openbravo.service.json.JsonUtils;

/**
 * Defines same methods used in transformers price exception.
 */
public abstract class PriceExceptionAbstractTransformer extends HqlQueryTransformer {
  private static final Logger log = LogManager.getLogger();

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Get document date.
   * 
   * @param requestParameters
   *          map of all parameters.
   * @param key
   *          name of the parameter to capture in requestParameters.
   *
   * @return document date or null as a string format.
   */
  protected String getDocumentDate(Map<String, String> requestParameters, String key) {
    SimpleDateFormat formatter = JsonUtils.createDateFormat();
    String documentDate = "null";
    try {
      documentDate = requestParameters.containsKey(key)
          ? "TO_DATE('" + formatter.format(formatter.parse(requestParameters.get(key))) + "','"
              + formatter.toPattern().toUpperCase() + "')"
          : "null";
    } catch (ParseException e) {
      log.error("Couldn't transform date", e);
    }
    return documentDate;
  }

  /**
   * Get parent organization list separated by comma.
   * 
   * @param requestParameters
   *          map of all parameters.
   * @param key
   *          name of the parameter to capture in requestParameters.
   *
   * @return collection of organizations id separated by comma as a string format.
   */
  protected String getOrganizationsList(Map<String, String> requestParameters, String key) {
    return StringCollectionUtils.commaSeparated(
        new OrganizationStructureProvider().getParentList(requestParameters.get(key), true), true);
  }

}
