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
      final HQLPropertyList crossStoreInfoHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensions);
      final HQLPropertyList crossStoreRegularScheduleInfoHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensionsRegularSchedule);
      final HQLPropertyList crossStoreSpecialScheduleInfoHQLProperties = ModelExtensionUtils
          .getPropertyExtensions(extensionsSpecialSchedule);

      final StringBuilder hql1 = new StringBuilder();
      hql1.append(" select" + crossStoreInfoHQLProperties.getHqlSelect());
      hql1.append(" from OrganizationInformation oi");
      hql1.append(" left join oi.locationAddress l");
      hql1.append(" left join l.region r");
      hql1.append(" left join l.country c");
      hql1.append(" left join oi.userContact u");
      hql1.append(" where oi.organization.id = :orgId");

      final StringBuilder hql2 = new StringBuilder();
      hql2.append(" select" + crossStoreRegularScheduleInfoHQLProperties.getHqlSelect());
      hql2.append(" from OBRETCO_Org_RegularSchedule ors");
      hql2.append(" join ors.obretcoSchedule s");
      hql2.append(" join s.oBRETCOScheduleLineList sl");
      hql2.append(" where ors.organization.id = :orgId");
      hql2.append(" and ors.validFromDate = (");
      hql2.append("   select max(ors2.validFromDate)");
      hql2.append("   from OBRETCO_Org_RegularSchedule ors2");
      hql2.append("   where ors2.organization.id = ors.organization.id");
      hql2.append("   and ors2.scheduletype = ors.scheduletype");
      hql2.append("   and ors2.validFromDate <= :terminalDate");
      hql2.append(" )");
      hql2.append(" order by ors.scheduletype, sl.weekday, sl.startingTime");

      final StringBuilder hql3 = new StringBuilder();
      hql3.append(" select" + crossStoreSpecialScheduleInfoHQLProperties.getHqlSelect());
      hql3.append(" from OBRETCO_Org_SpecialSchedule oss");
      hql3.append(" where oss.organization.id = :orgId");
      hql3.append(" and oss.specialdate >= :terminalDate");
      hql3.append(" order by oss.specialdate, oss.startingTime");

      return Arrays.asList(hql1.toString(), hql2.toString(), hql3.toString());
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
