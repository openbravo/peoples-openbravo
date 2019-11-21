/*
 ************************************************************************************
 * Copyright (C) 2014-2019 Openbravo S.L.U.
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

/**
 * @author eduardobecerra
 * 
 */
@Qualifier(OfferPriceList.discFilterPriceListPropertyExtension)
public class OfferPriceListProperties extends ModelExtension {

  public static final Logger log = LogManager.getLogger();

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    final List<HQLProperty> list = new ArrayList<>();
    list.add(new HQLProperty("pl.id", "id"));
    list.add(new HQLProperty("pl.priceAdjustment.id", "priceAdjustment"));
    list.add(new HQLProperty("pl.priceList.id", "m_pricelist_id"));
    list.add(new HQLProperty("concat(pl.priceAdjustment.name, ' - ', pl.priceList.name)",
        "_identifier"));
    return list;
  }
}
