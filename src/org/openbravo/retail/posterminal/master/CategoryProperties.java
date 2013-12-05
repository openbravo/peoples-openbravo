/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
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

@Qualifier(Category.productCategoryPropertyExtension)
public class CategoryProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    String nameTrl;
    if (OBContext.hasTranslationInstalled()) {
      nameTrl = "coalesce((select t.name from ProductCategoryTrl AS t where t.language='"
          + OBContext.getOBContext().getLanguage().getLanguage()
          + "'  and t.productCategory=pCat), pCat.name)";
    } else {
      nameTrl = "pCat.name";
    }
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("pCat.id", "id"));
    list.add(new HQLProperty("pCat.searchKey", "bpartner"));
    list.add(new HQLProperty(nameTrl, "name"));
    list.add(new HQLProperty(nameTrl, "_identifier"));
    list.add(new HQLProperty("img.bindaryData", "img"));
    return list;
  }

}