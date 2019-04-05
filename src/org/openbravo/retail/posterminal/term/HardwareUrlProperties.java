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
 * All portions are Copyright (C) 2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(HardwareURL.hardwareUrlPropertyExtension)
public class HardwareUrlProperties extends ModelExtension {
  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    final ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("p.id", "id"));
        add(new HQLProperty("p.obposHardwaremng.name", "_identifier"));
        add(new HQLProperty("p.obposHardwaremng.hardwareURL", "hardwareURL"));
        add(new HQLProperty("p.obposHardwaremng.hasReceiptPrinter", "hasReceiptPrinter"));
        add(new HQLProperty("p.obposHardwaremng.hasPDFPrinter", "hasPDFPrinter"));
      }
    };

    return list;
  }
}
