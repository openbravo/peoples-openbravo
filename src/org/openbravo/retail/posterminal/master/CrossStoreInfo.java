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
  public static final String crossStoreRegularScheduleInfoPropertyExtension = "OBPOS_CrossStoreRegularScheduleInfoExtension";
  public static final String crossStoreSpecialScheduleInfoPropertyExtension = "OBPOS_CrossStoreSpecialScheduleInfoExtension";

  @Inject
  @Any
  @Qualifier(crossStoreInfoPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(crossStoreRegularScheduleInfoPropertyExtension)
  private Instance<ModelExtension> extensionsRegularSchedule;

  @Inject
  @Any
  @Qualifier(crossStoreSpecialScheduleInfoPropertyExtension)
  private Instance<ModelExtension> extensionsSpecialSchedule;

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
      final String hql1 = getCrossStoreInfoQuery();
      final String hql2 = getCrossStoreRegularScheduleQuery();
      final String hql3 = getCrossStoreSpecialScheduleQuery();

      return Arrays.asList(hql1, hql2, hql3);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private String getCrossStoreInfoQuery() {
    final HQLPropertyList crossStoreInfoHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    final StringBuilder hql = new StringBuilder();
    hql.append(" select" + crossStoreInfoHQLProperties.getHqlSelect());
    hql.append(" from OrganizationInformation oi");
    hql.append(" left join oi.locationAddress l");
    hql.append(" left join l.region r");
    hql.append(" left join l.country c");
    hql.append(" left join oi.userContact u");
    hql.append(" where oi.organization.id = :orgId");
    return hql.toString();
  }

  private String getCrossStoreRegularScheduleQuery() {
    final HQLPropertyList crossStoreRegularScheduleHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsRegularSchedule);

    final StringBuilder hql = new StringBuilder();
    hql.append(" select" + crossStoreRegularScheduleHQLProperties.getHqlSelect());
    hql.append(" from OBRETCO_Org_RegularSchedule ors");
    hql.append(" join ors.obretcoSchedule s");
    hql.append(" join s.oBRETCOScheduleLineList sl");
    hql.append(" where ors.organization.id = :orgId");
    hql.append(" and ors.validFromDate = (");
    hql.append("   select max(ors2.validFromDate)");
    hql.append("   from OBRETCO_Org_RegularSchedule ors2");
    hql.append("   where ors2.organization.id = ors.organization.id");
    hql.append("   and ors2.scheduletype = ors.scheduletype");
    hql.append("   and ors2.validFromDate <= :terminalDate");
    hql.append(" )");
    hql.append(" order by ors.scheduletype, sl.weekday, sl.startingTime");
    return hql.toString();
  }

  private String getCrossStoreSpecialScheduleQuery() {
    final HQLPropertyList crossStoreSpecialScheduleHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsSpecialSchedule);

    final StringBuilder hql = new StringBuilder();
    hql.append(" select" + crossStoreSpecialScheduleHQLProperties.getHqlSelect());
    hql.append(" from OBRETCO_Org_SpecialSchedule oss");
    hql.append(" where oss.organization.id = :orgId");
    hql.append(" and oss.specialdate >= :terminalDate");
    hql.append(" order by oss.specialdate, oss.startingTime");
    return hql.toString();
  }

}
