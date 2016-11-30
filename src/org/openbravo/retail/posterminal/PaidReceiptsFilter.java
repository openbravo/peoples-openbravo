/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class PaidReceiptsFilter extends ProcessHQLQuery {
  public static final Logger log = Logger.getLogger(PaidReceiptsHeader.class);

  public static final String paidReceiptsFilterPropertyExtension = "PaidReceiptsFilter_Extension";

  @Inject
  @Any
  @Qualifier(paidReceiptsFilterPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    HQLPropertyList receiptsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);
    propertiesList.add(receiptsHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList receiptsHQLProperties = ModelExtensionUtils.getPropertyExtensions(extensions);

    String hqlPaidReceipts = "select"
        + receiptsHQLProperties.getHqlSelect()
        + "from Order as ord "
        + "where $filtersCriteria and $hqlCriteria and ord.$orgId"
        + " and ord.obposIsDeleted = false and ord.obposApplications is not null and ord.documentStatus <> 'CJ' "
        + " and ord.documentStatus <> 'CA'  "
        + " and exists(select 1 from ord.orderLineList where orderedQuantity != 0) "
        + " $orderByCriteria";

    return Arrays.asList(new String[] { hqlPaidReceipts });
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}