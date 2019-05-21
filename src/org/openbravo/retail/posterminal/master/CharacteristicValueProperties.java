/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(CharacteristicValue.characteristicValuePropertyExtension)
public class CharacteristicValueProperties extends ModelExtension {

  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {
    boolean isRemote = getPreference("OBPOS_remote.product");
    final ArrayList<HQLProperty> list = new ArrayList<>();
    list.add(new HQLProperty("cv.id", "id"));
    list.add(new HQLProperty("cv.name", "name"));
    list.add(new HQLProperty("ch.id", "characteristic_id"));
    if (isRemote) {
      list.add(new HQLProperty("'0'", "parent"));
    } else {
      list.add(new HQLProperty("node.reportSet", "parent"));
    }
    list.add(new HQLProperty("cv.summaryLevel", "summaryLevel"));
    list.add(new HQLProperty("cv.name", "_identifier"));
    list.add(new HQLProperty("cv.active", "active"));
    list.add(new HQLProperty("ch.name", "characteristicName"));
    return list;
  }

  private boolean getPreference(final String preference) {
    OBContext.setAdminMode(false);
    boolean value;
    try {
      value = StringUtils.equals(Preferences.getPreferenceValue(preference, true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null), "Y");
    } catch (PropertyException e) {
      value = false;
    } finally {
      OBContext.restorePreviousMode();
    }
    return value;
  }

}
