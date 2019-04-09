/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.Arrays;
import java.util.Date;
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
import org.openbravo.mobile.core.utils.OBMOBCUtils;
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
      final Date terminalDate = OBMOBCUtils.calculateServerDate(
          jsonsent.getJSONObject("parameters").getString("terminalTime"),
          jsonsent.getJSONObject("parameters")
              .getJSONObject("terminalTimeOffset")
              .getLong("value"));

      final Map<String, Object> paramValues = new HashMap<>();
      paramValues.put("orgId", orgId);
      paramValues.put("terminalDate", terminalDate);

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

      final StringBuilder hql1 = new StringBuilder();
      hql1.append(" select" + regularProductStockHQLProperties.getHqlSelect());
      hql1.append(" from OrganizationInformation oi");
      hql1.append(" left join oi.locationAddress l");
      hql1.append(" left join l.region r");
      hql1.append(" left join l.country c");
      hql1.append(" left join oi.userContact u");
      hql1.append(" where oi.organization.id = :orgId");

      final StringBuilder hql2 = new StringBuilder();
      hql2.append(" select" + regularProductStockHQLProperties.getHqlSelect());
      hql2.append(" from OBRETCO_Org_Schedule os");
      hql2.append(" join os.obretcoSchedule s");
      hql2.append(" join s.oBRETCOScheduleLineList sl");
      hql2.append(" where os.organization.id = :orgId");
      hql2.append(" and os.validFromDate = (");
      hql2.append("   select max(os2.validFromDate)");
      hql2.append("   from OBRETCO_Org_Schedule os2");
      hql2.append("   where os2.organization.id = os.organization.id");
      hql2.append("   where os2.scheduletype = os.scheduletype");
      hql2.append("   and os2.validFromDate <= :terminalDate");
      hql2.append("   and os2.active = true");
      hql2.append(" )");
      hql2.append(" and os.active = true");
      hql2.append(" and s.active = true");
      hql2.append(" order by os.scheduletype, sl.weekday, sl.startingTime");

      return Arrays.asList(hql1.toString(), hql2.toString());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
