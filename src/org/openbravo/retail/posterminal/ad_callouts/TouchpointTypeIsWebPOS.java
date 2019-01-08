/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.retail.posterminal.TerminalType;

public class TouchpointTypeIsWebPOS extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    String strTouchpointTypeId = info.getStringParameter("inpobposTerminaltypeId",
        IsIDFilter.instance);
    TerminalType touchpointType = OBDal.getInstance().get(TerminalType.class, strTouchpointTypeId);
    if (touchpointType != null) {
      info.addResult("ISWEBPOS", "OBPOS".equals(touchpointType.getApplication()));
    }
  }
}