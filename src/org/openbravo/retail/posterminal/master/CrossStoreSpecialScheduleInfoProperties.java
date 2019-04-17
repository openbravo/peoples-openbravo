/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(CrossStoreInfo.crossStoreSpecialScheduleInfoPropertyExtension)
public class CrossStoreSpecialScheduleInfoProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    final List<HQLProperty> list = new ArrayList<>();

    list.add(new HQLProperty("oss.specialdate", "specialdate"));
    list.add(new HQLProperty("oss.open", "open"));
    list.add(new HQLProperty("oss.startingTime", "startingTime"));
    list.add(new HQLProperty("oss.endingTime", "endingTime"));

    return list;

  }
}
