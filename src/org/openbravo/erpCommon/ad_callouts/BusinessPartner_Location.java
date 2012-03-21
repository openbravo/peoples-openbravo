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
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.geography.Location;
import org.openbravo.service.db.CallStoredProcedure;

public class BusinessPartner_Location extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    VariablesSecureApp vars = info.vars;

    String strcLocationId = vars.getStringParameter("inpcLocationId");
    String strname = vars.getStringParameter("inpname");
    final Location loc = OBDal.getInstance().get(org.openbravo.model.common.geography.Location.class, strcLocationId);
    
    final List<Object> parameters = new ArrayList<Object>();

    parameters.add(loc.getId());
    
 // the procedure name
    final String procedureName = "C_Location_Name";

    try {
      // calling the procedure and getting the result

      // STORED PROCEDURE
    	
    	String substr = strname.substring(0, 1);
    	
      if (substr.equals(".")) {
    	  String locationName = (String) CallStoredProcedure.getInstance().call(procedureName, parameters, null);
          info.addResult("inpname", "." + locationName);
      }

      // STORED PROCEDURE

    } catch (final Exception e) {
      throw new OBException(e);
    }
  }
}