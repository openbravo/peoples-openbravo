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

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.retail.posterminal.ProcessHQLQuery;

/*
 * This class fills the m_ch_value table in WebSQL even if it is called productChValue.
 */
public class CharacteristicValue extends ProcessHQLQuery {
  public static final String characteristicValuePropertyExtension = "OBPOS_CharacteristicValueExtension";

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

    hqlQueries
        .add("select"
            + regularProductsChValueHQLProperties.getHqlSelect()
            + "from CharacteristicValue cv, ADTreeNode node "
            + "where cv.characteristic.tree =  node.tree and cv.id = node.node and  $filtersCriteria AND $hqlCriteria "
            + "and cv.characteristic.obposUseonwebpos = true "
            + "and cv.$naturalOrgCriteria and cv.$readableSimpleClientCriteria and (cv.$incrementalUpdateCriteria) "
            + "order by cv.name");

    return hqlQueries;
  }
}
