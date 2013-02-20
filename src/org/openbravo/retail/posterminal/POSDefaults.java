package org.openbravo.retail.posterminal;

import org.openbravo.mobile.core.MobileDefaults;

public class POSDefaults extends MobileDefaults {
  @Override
  public String getFormId() {
    return POSUtils.WEB_POS_FORM_ID;
  }

  @Override
  public String getAppName() {
    return "Openbravo Web POS";
  }
}
