package org.openbravo.base.filter;

import java.math.BigDecimal;

public class NumberFilter implements RequestFilter {

  public static final NumberFilter instance = new NumberFilter();

  @Override
  public boolean accept(String value) {
    try {
      new BigDecimal(value);
    } catch (Exception e) {
      return false;
    }
    return true;
  }
}
