/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class CrossStoreInfo extends ProcessHQLQuery {

  public static final String crossStoreInfoPropertyExtension = "OBPOS_CrossStoreInfoExtension";

  @Inject
  @Any
  @Qualifier(crossStoreInfoPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected Map<String, Object> getParameterValues(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final String orgId = jsonsent.getString("org");

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("orgId", orgId);
      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(final JSONObject jsonsent) throws JSONException {
    OBContext.setAdminMode(true);
    try {
      final HQLPropertyList regularProductStockHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);

      final StringBuilder hql = new StringBuilder();
      hql.append(" select" + regularProductStockHQLProperties.getHqlSelect());
      hql.append(" from OrganizationInformation oi");
      hql.append(" left join oi.locationAddress l");
      hql.append(" left join l.region r");
      hql.append(" left join l.country c");
      hql.append(" left join oi.userContact u");
      hql.append(" where oi.organization.id = :orgId");

      return Collections.singletonList(hql.toString());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
