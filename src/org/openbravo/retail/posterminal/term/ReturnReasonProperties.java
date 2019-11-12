/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ReturnReason.RETURNREASONPROPERTYEXTENSION)
public class ReturnReasonProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("reason.id", "id"));
        add(new HQLProperty("reason.searchKey", "searchKey"));
        add(new HQLProperty("reason.name", "name"));
        add(new HQLProperty("reason.name", "_identifier"));
        add(new HQLProperty("reason.active", "active"));
      }
    };
    return list;
  }
}
