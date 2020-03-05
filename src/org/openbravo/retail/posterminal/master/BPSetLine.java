/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.common.businesspartner.BusinessPartnerSetLine;

@MasterDataModel("BPSetLine")
public class BPSetLine extends MasterDataProcessHQLQuery {
  public static final String bpSetLinePropertyExtension = "OBPOS_BusinessPartnerSetLineExtension";

  @Inject
  @Any
  @Qualifier(bpSetLinePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    List<HQLPropertyList> propertiesList = new ArrayList<>();
    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    propertiesList.add(regularCountryHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    HQLPropertyList regularCountryHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    String hql = "select" + regularCountryHQLProperties.getHqlSelect()
        + "from BusinessPartnerSetLine c where c.$incrementalUpdateCriteria "
        + "and c.$naturalOrgCriteria "
        + "and c.$readableSimpleClientCriteria and c.$activeCriteria";

    boolean fullRefresh = jsonsent.has("lastUpdated")
        && !jsonsent.get("lastUpdated").equals("undefined")
        && !jsonsent.get("lastUpdated").equals("null") ? false : true;

    // if full refresh then only retrieve the lines valid now and in the future
    if (fullRefresh) {
      hql += " and (" + BusinessPartnerSetLine.PROPERTY_ENDINGDATE + " is null or "
          + BusinessPartnerSetLine.PROPERTY_ENDINGDATE + ">=NOW()) ";
    }
    hql += " order by c.id asc";

    return Arrays.asList(hql);
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Qualifier(bpSetLinePropertyExtension)
  public static class BPSetLineProperties extends ModelExtension {

    @Override
    public List<HQLProperty> getHQLProperties(Object params) {
      ArrayList<HQLProperty> list = new ArrayList<>();
      list.add(new HQLProperty("c.id", "id"));
      list.add(new HQLProperty("c.bpSet.id", "bpSet"));
      list.add(new HQLProperty("c.businessPartner.id", "businessPartner"));
      list.add(new HQLProperty("c.startingDate", "startingDate"));
      list.add(new HQLProperty("c.endingDate", "endingDate"));
      return list;
    }

  }

  @Override
  public List<String> getMasterDataModelProperties() {
    return getPropertiesFrom(extensions);
  }

}
