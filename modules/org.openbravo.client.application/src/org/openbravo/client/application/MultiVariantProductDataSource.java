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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.application;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.service.datasource.DefaultDataSourceService;

/**
 * Datasource used by the Purchase Order view "Model Mode" process
 * TODO: Requires further development
 */
public class MultiVariantProductDataSource extends DefaultDataSourceService {
  private static final Logger log = LogManager.getLogger();

  // M_PRODUCT_ID table id
  private static final String AD_TABLE_ID = "208";

  @Override
  public Entity getEntity() {
    return ModelProvider.getInstance().getEntityByTableId(AD_TABLE_ID);
  }

  @Override
  public void checkFetchDatasourceAccess(Map<String, String> parameter) {
    // Product datasource is accessible by all roles.
    // TODO: Might need rechecking
  }

  @Override
  protected String getWhereAndFilterClause(Map<String, String> parameters) {
    return "";
  }
}