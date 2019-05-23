/*
 ************************************************************************************
 * Copyright (C) 2013-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(Brand.brandPropertyExtension)
public class BrandProperties extends ModelExtension {
  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {
    final ArrayList<HQLProperty> list = new ArrayList<>();
    list.add(new HQLProperty("brand.id", "id"));
    list.add(new HQLProperty("brand.name", "name"));
    list.add(new HQLProperty("brand.name", "_identifier"));
    return list;
  }

}
