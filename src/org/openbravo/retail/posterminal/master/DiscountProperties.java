/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.service.json.JsonConstants;

@Qualifier(Discount.discountPropertyExtension)
public class DiscountProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    Entity discountEntity = ModelProvider.getInstance().getEntity(PriceAdjustment.ENTITY_NAME);
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
    List<String> identifier = new ArrayList<String>();
    for (Property property : discountEntity.getProperties()) {
      if (property.isOneToMany() || property.isAuditInfo()) {
        continue;
      }
      if (property.getTargetEntity() != null) {
        list.add(new HQLProperty("p." + property.getName() + ".id", property.getName()));
      } else {
        list.add(new HQLProperty("p." + property.getName(), property.getName()));
        if (property.isIdentifier()) {
          identifier.add("p." + property.getName());
        }
      }
    }
    list.add(new HQLProperty(String.join("|| ' - ' ||", identifier), JsonConstants.IDENTIFIER));
    return list;
  }
}
