/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
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
import org.openbravo.mobile.core.model.HQLEntity;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

public class LoadedCustomer extends ProcessHQLQuery {
  public static final String businessPartnerPropertyExtension = "OBPOS_BusinessPartnerExtension";
  public static final String bpLocationPropertyExtension = "OBPOS_BPLocationExtension";

  @Inject
  @Any
  @Qualifier(businessPartnerPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Inject
  @Any
  @Qualifier(bpLocationPropertyExtension)
  private Instance<ModelExtension> extensionsLoc;

  @Override
  protected Map<String, Object> getParameterValues(JSONObject jsonsent) throws JSONException {
    try {
      OBContext.setAdminMode(true);
      Map<String, Object> paramValues = new HashMap<String, Object>();
      paramValues.put("businessPartnerId",
          jsonsent.getJSONObject("parameters").getJSONObject("bpartnerId").get("value"));
      paramValues.put("bplocId",
          jsonsent.getJSONObject("parameters").getJSONObject("bpLocationId").get("value"));
      if (jsonsent.getJSONObject("parameters").has("bpBillLocationId")) {
        paramValues.put("bpbilllocId",
            jsonsent.getJSONObject("parameters").getJSONObject("bpBillLocationId").get("value"));
      }

      return paramValues;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    boolean useGroupBy = false;
    String groupByExpression = "";
    List<String> customers = new ArrayList<String>();
    HQLPropertyList bpartnerHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    HQLPropertyList bpartnerLocHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensionsLoc);

    List<HQLEntity> entityExtensions = ModelExtensionUtils.getEntityExtensions(extensions);
    final String entitiesJoined = ModelExtensionUtils.getHQLEntitiesJoined(entityExtensions);
    for (HQLProperty property : bpartnerHQLProperties.getProperties()) {
      if (property.isIncludeInGroupBy()) {
        groupByExpression = bpartnerHQLProperties.getHqlGroupBy();
        useGroupBy = true;
        break;
      }
    }

    StringBuilder bpartnerHQLQuery = new StringBuilder();
    bpartnerHQLQuery.append("select ");
    bpartnerHQLQuery.append(bpartnerHQLProperties.getHqlSelect());
    bpartnerHQLQuery.append(" from BusinessPartnerLocation AS bpl");
    bpartnerHQLQuery.append(" join bpl.businessPartner as bp");
    bpartnerHQLQuery.append(" left outer join bp.aDUserList AS ulist");
    bpartnerHQLQuery.append(" left outer join bp.priceList AS plist");
    bpartnerHQLQuery.append(" left outer join bp.language AS lang ");
    bpartnerHQLQuery.append(" left outer join bp.greeting grt ");
    bpartnerHQLQuery.append(" left outer join bp.businessPartnerLocationList AS bpsl");
    bpartnerHQLQuery.append(entitiesJoined);
    bpartnerHQLQuery.append(" where bp.id = :businessPartnerId");
    bpartnerHQLQuery.append(" and bpl.id in (");
    bpartnerHQLQuery.append("   select max(bpls.id) as bpLocId");
    bpartnerHQLQuery.append("   from BusinessPartnerLocation AS bpls");
    bpartnerHQLQuery.append("   where bpls.businessPartner.id = bp.id");
    bpartnerHQLQuery.append("   and bpls.invoiceToAddress = true");
    bpartnerHQLQuery.append("   and bpls.$readableSimpleClientCriteria");
    bpartnerHQLQuery.append("   group by bpls.businessPartner.id");
    bpartnerHQLQuery.append(" )");
    bpartnerHQLQuery.append(" and (ulist is null");
    bpartnerHQLQuery.append(" or ulist.id in (");
    bpartnerHQLQuery.append("   select max(ulist2.id)");
    bpartnerHQLQuery.append("   from ADUser as ulist2");
    bpartnerHQLQuery.append("   where ulist2.businessPartner = bp");
    bpartnerHQLQuery.append("   group by ulist2.businessPartner");
    bpartnerHQLQuery.append(" ))");
    bpartnerHQLQuery.append(" and (bpsl is null");
    bpartnerHQLQuery.append(" or bpsl.id in (");
    bpartnerHQLQuery.append("   select max(bpls.id) as bpLocId");
    bpartnerHQLQuery.append("   from BusinessPartnerLocation AS bpls");
    bpartnerHQLQuery.append("   where bpls.shipToAddress = true");
    bpartnerHQLQuery.append("   and bpls.businessPartner.id = bp.id");
    bpartnerHQLQuery.append("   and bpls.$readableSimpleClientCriteria");
    bpartnerHQLQuery.append(" ))");
    if (useGroupBy) {
      bpartnerHQLQuery.append(" group by " + groupByExpression);
    }
    bpartnerHQLQuery.append("order by bp.name");
    customers.add(bpartnerHQLQuery.toString());

    final StringBuilder bpartnerShipLocHQLQuery = new StringBuilder();
    bpartnerShipLocHQLQuery.append("select ");
    bpartnerShipLocHQLQuery.append(bpartnerLocHQLProperties.getHqlSelect());
    bpartnerShipLocHQLQuery.append(" from BusinessPartnerLocation AS bploc");
    bpartnerShipLocHQLQuery.append(" join bploc.businessPartner AS bp");
    bpartnerShipLocHQLQuery.append(" left join bploc.locationAddress AS bplocAddress");
    bpartnerShipLocHQLQuery.append(" left join bplocAddress.region AS bplocRegion");
    bpartnerShipLocHQLQuery.append(" where bploc.id = :bplocId");
    bpartnerShipLocHQLQuery.append(" order by bploc.locationAddress.addressLine1");
    customers.add(bpartnerShipLocHQLQuery.toString());

    if (jsonsent.getJSONObject("parameters").has("bpBillLocationId")) {
      final StringBuilder bpartnerBillLocHQLQuery = new StringBuilder();
      bpartnerBillLocHQLQuery.append("select ");
      bpartnerBillLocHQLQuery.append(bpartnerLocHQLProperties.getHqlSelect());
      bpartnerBillLocHQLQuery.append(" from BusinessPartnerLocation AS bploc");
      bpartnerBillLocHQLQuery.append(" join bploc.businessPartner AS bp");
      bpartnerBillLocHQLQuery.append(" left join bploc.locationAddress AS bplocAddress");
      bpartnerBillLocHQLQuery.append(" left join bplocAddress.region AS bplocRegion");
      bpartnerBillLocHQLQuery.append(" where bploc.id = :bpbilllocId");
      bpartnerBillLocHQLQuery.append(" order by bploc.locationAddress.addressLine1");
      customers.add(bpartnerBillLocHQLQuery.toString());
    }

    return customers;
  }
}
