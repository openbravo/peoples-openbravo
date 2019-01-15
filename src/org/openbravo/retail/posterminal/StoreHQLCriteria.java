/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang.StringUtils;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.erpCommon.utility.StringCollectionUtils;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("Store")
public class StoreHQLCriteria extends HQLCriteriaProcess {

  private static final String SUFIX = "[\"all_";

  @Override
  public String getHQLFilter(String params) {
    if (StringUtils.startsWith(params, SUFIX)) {

      String crossStoreOrgId = params.substring(SUFIX.length(), params.length() - 2);

      String crossStoreOrg = StringCollectionUtils.commaSeparated(
          POSUtils.getOrgListByCrossStoreId(crossStoreOrgId), true);

      final StringBuilder orgFilter = new StringBuilder();
      orgFilter.append(" ord.organization.id in (");
      orgFilter.append(crossStoreOrg);
      orgFilter.append(")");
      return orgFilter.toString();
    }
    return " ord.organization.id = $1 ";
  }
}