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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.sql.PreparedStatement;

import org.apache.log4j.Logger;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.ui.Tab;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

public class CalculatePromotions extends DalBaseProcess {
  final private static Logger log = Logger.getLogger(CalculatePromotions.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    OBContext.setAdminMode();
    PreparedStatement ps = null;
    try {
      final String tabId = (String) bundle.getParams().get("tabId");
      String tableName = OBDal.getInstance().get(Tab.class, tabId).getTable().getDBTableName();
      String type;
      if (tableName.equalsIgnoreCase("c_order")) {
        type = "O";
      } else {
        type = "I";
      }

      final String id = (String) bundle.getParams().get(tableName + "_ID");
      DalConnectionProvider conn = new DalConnectionProvider(false);
      String st;
      if (conn.getRDBMS().equals("ORACLE")) {
        st = "CALL ";
      } else {
        st = "SELECT ";
      }
      st += "M_PROMOTION_CALCULATE(?, ?, ?)";

      ps = conn.getPreparedStatement(st);
      ps.setString(1, type);
      ps.setString(2, id);
      ps.setString(3, OBContext.getOBContext().getUser().getId());

      ps.execute();

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle(OBMessageUtils.messageBD("Success"));
      bundle.setResult(msg);
    } catch (Exception e) {
      log.error("Error calculating promotions", e);

      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    } finally {
      if (ps != null) {
        ps.close();
      }
      OBContext.restorePreviousMode();
    }
  }

}
