/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.StaticResourceComponent;

/**
 * This class replaces call to org.openbravo.client.kernel.StaticResourceComponent because the way
 * POS loads resources requires of $LAB
 * 
 * @author alostale
 * 
 */

@ApplicationScoped
public class OBPOSStaticResorcesComponent extends StaticResourceComponent {
  private static final String GEN_TARGET_LOCATION = "web/js/gen";

  @Override
  public String generate() {
    final String scriptPath = getContextUrl() + GEN_TARGET_LOCATION + "/"
        + getStaticResourceFileName() + ".js";
    return "$LAB.script('" + scriptPath + "');";
  }
}
