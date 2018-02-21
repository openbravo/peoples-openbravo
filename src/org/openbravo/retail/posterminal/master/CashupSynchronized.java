/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.master;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.mobile.core.servercontroller.MobileServerController;
import org.openbravo.mobile.core.servercontroller.MobileServerRequestExecutor;
import org.openbravo.mobile.core.servercontroller.MobileServerUtils;
import org.openbravo.mobile.core.servercontroller.SynchronizedServerProcessCaller;
import org.openbravo.model.common.order.Order;
import org.openbravo.retail.posterminal.OBPOSErrors;
import org.openbravo.service.importprocess.ImportEntry;

/**
 * A read cashup information class which is used in case of running in synchronized mode. First call
 * the central server to get the latest cashup information and return that if available.
 * 
 * @author mtaal
 */
public class CashupSynchronized extends Cashup {
  private static final Logger log = Logger.getLogger(CashupSynchronized.class);

  @Override
  public JSONObject exec(JSONObject json) throws JSONException, ServletException {
    if (MobileServerController.getInstance().isThisACentralServer()) {
      return executeLocal(json);
    } else if (MobileServerController.getInstance().isCentralServerOnline()) {
      log.debug("Central server is online, calling it to get latest cashup information");
      try {
        return MobileServerRequestExecutor.getInstance().executeCentralRequest(
            MobileServerUtils.OBWSPATH + this.getClass().getName(), json);
      } catch (Throwable t) {
        // something goes wrong on central, try local
        // should fail as there are errors in the import queue
        return executeLocal(json);
      }
    } else {
      return executeLocal(json);
    }
  }

  private JSONObject executeLocal(JSONObject json) throws JSONException, ServletException {
    if (isDataInQueue(json)) {
      throw new OBException(
          "Data on the server is in error/initial state, cashup information can not be retrieved");
    }
    return super.exec(json);
  }

  private boolean isDataInQueue(JSONObject json) throws JSONException {
    final String posId = json.getString("pos");
    {
      final Query qry = SessionHandler
          .getInstance()
          .getSession()
          .createQuery(
              "select count(*) from " + ImportEntry.ENTITY_NAME + " where ("
                  + ImportEntry.PROPERTY_IMPORTSTATUS + "='Error' or "
                  + ImportEntry.PROPERTY_IMPORTSTATUS + "='Initial') and "
                  + ImportEntry.PROPERTY_TYPEOFDATA + "='"
                  + SynchronizedServerProcessCaller.SYNCHRONIZED_DATA_TYPE + "' and "
                  + ImportEntry.PROPERTY_OBPOSPOSTERMINAL + "='" + posId + "'");
      if (((Number) qry.uniqueResult()).intValue() > 0) {
        return true;
      }
    }
    {
      final Query qry = SessionHandler
          .getInstance()
          .getSession()
          .createQuery(
              "select count(*) from " + OBPOSErrors.ENTITY_NAME + " where "
                  + OBPOSErrors.PROPERTY_ORDERSTATUS + "='N' and "
                  + OBPOSErrors.PROPERTY_TYPEOFDATA + "='" + Order.ENTITY_NAME + "' and "
                  + OBPOSErrors.PROPERTY_OBPOSAPPLICATIONS + "='" + posId + "'");
      if (((Number) qry.uniqueResult()).intValue() > 0) {
        return true;
      }
    }
    return false;
  }
}
