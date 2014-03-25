package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.MobileDefaults;
import org.openbravo.model.ad.access.User;

@ApplicationScoped
@Qualifier(POSConstants.APP_NAME)
public class POSDefaults extends MobileDefaults {
  @Override
  public String getFormId() {
    return POSUtils.WEB_POS_FORM_ID;
  }

  @Override
  public String getAppName() {
    return "Openbravo Web POS";
  }

  @Override
  public String getDefaultRoleProperty() {
    return User.PROPERTY_OBPOSDEFAULTPOSROLE;
  }
}
