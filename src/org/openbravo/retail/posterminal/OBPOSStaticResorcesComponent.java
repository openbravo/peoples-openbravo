/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.client.kernel.BaseComponent;
import org.openbravo.client.kernel.StaticResourceComponent;

/**
 * This class replaces call to org.openbravo.client.kernel.StaticResourceComponent because the way
 * POS loads resources requires of $LAB
 * 
 * @author alostale
 * 
 */

public class OBPOSStaticResorcesComponent extends BaseComponent {
  private static final String GEN_TARGET_LOCATION = "web/js/gen";

  @Inject
  @Any
  private Instance<StaticResourceComponent> rc;

  public String generate() {
    StaticResourceComponent sr = rc.get();

    sr.setParameters(getParameters());
    final String scriptPath = getContextUrl() + GEN_TARGET_LOCATION + "/"
        + sr.getStaticResourceFileName() + ".js";
    return "$LAB.script('" + scriptPath + "');";
  }

  @Override
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }
}
