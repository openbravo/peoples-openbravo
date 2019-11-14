/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * 
 * @author iperdomo
 * 
 */
public class TestComponent extends BaseComponent {

  @Inject
  @Any
  private Instance<ComponentProvider> componentProviders;

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#generate()
   */
  @Override
  public String generate() {
    final StringBuilder sb = new StringBuilder();
    for (String resource : getTestResources()) {
      String testResource = resource;
      if (testResource.startsWith("/") && getContextUrl().length() > 0) {
        testResource = getContextUrl() + testResource.substring(1);
      } else {
        testResource = getContextUrl() + testResource;
      }
      sb.append("$LAB.script(\"" + testResource + "\");\n");
    }
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.openbravo.client.kernel.BaseComponent#getData()
   */
  @Override
  public Object getData() {
    return this;
  }

  public List<String> getTestResources() {
    return new ArrayList<>();
  }
}
