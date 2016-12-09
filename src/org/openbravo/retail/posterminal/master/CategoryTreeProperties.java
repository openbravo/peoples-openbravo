/*
 ************************************************************************************
 * Copyright (C) 2013-2016 Openbravo S.L.U.
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

@Qualifier(CategoryTree.productCategoryTreePropertyExtension)
public class CategoryTreeProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>();
    list.add(new HQLProperty("tn.id", "id"));
    list.add(new HQLProperty("tn.node", "categoryId"));
    list.add(new HQLProperty("tn.reportSet", "parentId"));
    list.add(new HQLProperty("tn.sequenceNumber", "seqNo"));
    list.add(new HQLProperty("pc.active", "active"));
    return list;
  }

}