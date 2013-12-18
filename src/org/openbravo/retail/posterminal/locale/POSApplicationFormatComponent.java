/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.locale;

import org.apache.commons.lang.StringUtils;
import org.openbravo.client.kernel.ApplicationComponent;
import org.openbravo.dal.core.OBContext;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.retail.posterminal.POSUtils;

/**
 * Overrides methods in ApplicationComponent to get specific POS formats
 * 
 * @author alostale
 * 
 */
public class POSApplicationFormatComponent extends ApplicationComponent {
  @Override
  public String getDefaultDecimalSymbol() {
    String decimalSymbol = (String) POSUtils.getPropertyInOrgTree(OBContext.getOBContext()
        .getCurrentOrganization(), Organization.PROPERTY_OBPOSFORMATDECIMAL);
    if (StringUtils.isEmpty(decimalSymbol)) {
      return super.getDefaultDecimalSymbol();
    } else {
      return decimalSymbol;
    }
  }

  @Override
  public String getDefaultGroupingSymbol() {
    String groupSymbol = (String) POSUtils.getPropertyInOrgTree(OBContext.getOBContext()
        .getCurrentOrganization(), Organization.PROPERTY_OBPOSFORMATGROUP);
    if (StringUtils.isEmpty(groupSymbol)) {
      return super.getDefaultGroupingSymbol();
    } else {
      return groupSymbol;
    }
  }

  @Override
  public String getDateFormat() {
    String dateFormat = (String) POSUtils.getPropertyInOrgTree(OBContext.getOBContext()
        .getCurrentOrganization(), Organization.PROPERTY_OBPOSDATEFORMAT);
    if (StringUtils.isEmpty(dateFormat)) {
      return super.getDateFormat();
    } else {
      return dateFormat;
    }
  }
}
