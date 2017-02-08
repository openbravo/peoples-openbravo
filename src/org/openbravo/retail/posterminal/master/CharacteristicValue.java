/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
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

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.config.OBRETCOProductList;
import org.openbravo.retail.posterminal.POSUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

/*
 * This class fills the m_ch_value table in WebSQL even if it is called productChValue.
 */
public class CharacteristicValue extends ProcessHQLQuery {
  public static final String characteristicValuePropertyExtension = "OBPOS_CharacteristicValueExtension";
  public static final Logger log = Logger.getLogger(CharacteristicValue.class);

  @Inject
  @Any
  @Qualifier(characteristicValuePropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<HQLPropertyList> getHqlProperties(JSONObject jsonsent) {
    // Get Product Properties
    List<HQLPropertyList> propertiesList = new ArrayList<HQLPropertyList>();
    Map<String, Object> args = new HashMap<String, Object>();
    HQLPropertyList characteristicsHQLProperties = ModelExtensionUtils.getPropertyExtensions(
        extensions, args);
    propertiesList.add(characteristicsHQLProperties);

    return propertiesList;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    List<String> hqlQueries = new ArrayList<String>();
    HQLPropertyList regularProductsChValueHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);
    String assortmentFilter = "";
    String orgId = OBContext.getOBContext().getCurrentOrganization().getId();
    final OBRETCOProductList productList = POSUtils.getProductListByOrgId(orgId);
    boolean isRemote = false;
    try {
      OBContext.setAdminMode(false);
      isRemote = "Y".equals(Preferences.getPreferenceValue("OBPOS_remote.product", true, OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBContext.getOBContext().getUser(), OBContext.getOBContext().getRole(), null));
    } catch (PropertyException e) {
      log.error("Error getting preference OBPOS_remote.product " + e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!isRemote) {
      assortmentFilter = "exists (select 1 from  ProductCharacteristicValue pcv, OBRETCO_Prol_Product assort  "
          + " where pcv.characteristicValue.id=cv.id "
          + " and pcv.product.id= assort.product.id "
          + " and assort.obretcoProductlist.id= '" + productList.getId() + "' ) and ";
    }
    hqlQueries
        .add("select"
            + regularProductsChValueHQLProperties.getHqlSelect()
            + "from CharacteristicValue cv, ADTreeNode node "
            + "where cv.characteristic.tree =  node.tree and cv.id = node.node and  $filtersCriteria AND $hqlCriteria "
            + "and cv.characteristic.obposUseonwebpos = true  and "
            + assortmentFilter
            + " cv.$naturalOrgCriteria and cv.$readableSimpleClientCriteria and (cv.$incrementalUpdateCriteria) "
            + "order by cv.name, cv.id");
    return hqlQueries;
  }
}
