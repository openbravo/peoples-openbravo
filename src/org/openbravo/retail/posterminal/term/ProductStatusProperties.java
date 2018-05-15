/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(ProductStatus.productStatusPropertyExtension)
public class ProductStatusProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(final Object params) {

    final ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("ps.id", "id"));
        add(new HQLProperty(
            "COALESCE(SELECT pst.name FROM ProductStatusTrl AS pst WHERE pst.language = '"
                + OBContext.getOBContext().getLanguage().getLanguage()
                + "' AND pst.productStatus.id = ps.id), ps.name)", "name"));
        add(new HQLProperty("ps.restrictsalefrompos", "restrictsalefrompos"));
        add(new HQLProperty("ps.restrictsaleoutofstock", "restrictsaleoutofstock"));
      }
    };

    return list;
  }

}
