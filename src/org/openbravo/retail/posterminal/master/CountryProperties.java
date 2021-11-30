/*
 ************************************************************************************
 * Copyright (C) 2016-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Country.countryPropertyExtension)
public class CountryProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    String nameTrl = "c.name";
    if (OBContext.hasTranslationInstalled()) {
      nameTrl = "coalesce ((select t.name from CountryTrl t where t.country=c and t.language.id= :languageId), c.name) ";
    }
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("c.id", "id"));
    list.add(new HQLProperty(nameTrl, "_identifier"));
    return list;
  }

}
