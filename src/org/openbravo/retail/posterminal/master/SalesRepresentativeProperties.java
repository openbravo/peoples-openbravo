/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
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

@Qualifier(SalesRepresentative.salesRepresentativePropertyExtension)
public class SalesRepresentativeProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("user.id", "id"));
        add(new HQLProperty("user.name", "name"));
        add(new HQLProperty("user.username", "username"));
        add(new HQLProperty("user.name", "_identifier"));
        add(new HQLProperty("case when user.active='Y' and "
            + "user.businessPartner.active='Y' then true else false end", "active"));
      }
    };
    return list;
  }
}