/*
 *************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_reports;

import java.math.BigDecimal;

import org.openbravo.data.FieldProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SummaryProductCategory implements FieldProvider {
  static Logger log4j = LoggerFactory.getLogger(SummaryProductCategory.class);

  public String category;
  public BigDecimal cost;

  public SummaryProductCategory() {
    super();
  }

  public SummaryProductCategory(String category, BigDecimal cost) {
    super();
    this.category = category;
    this.cost = cost;
  }

  @Override
  public String getField(String fieldName) {
    if (fieldName.equalsIgnoreCase("category")) {
      return category;
    } else if (fieldName.equalsIgnoreCase("cost")) {
      return cost.toPlainString();
    } else {
      log4j.debug("Field does not exist: {}", fieldName);
      return null;
    }
  }

  public void addCost(BigDecimal bigDecimal) {
    this.cost = this.cost.add(bigDecimal);
  }

}
