/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.process;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.OBPOSAppCashup;
import org.openbravo.retail.posterminal.OBPOSApplications;

public class ValidationUnlinkDeviceActionHandler extends BaseActionHandler {
  public static final Logger log = Logger.getLogger(ValidationUnlinkDeviceActionHandler.class);

        @Override protected JSONObject execute(Map<String, Object> parameters,String content){
          try {
            JSONObject result = new JSONObject();
            final JSONObject jsonData = new JSONObject(content);
            final String terminalId = jsonData.getString("id");
            final OBPOSApplications terminal = OBDal.getInstance().get(OBPOSApplications.class,
                terminalId);
            final OBCriteria<OBPOSAppCashup> qApp = OBDal.getInstance().createCriteria(
                OBPOSAppCashup.class);
            qApp.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_POSTERMINAL, terminal));
            qApp.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_ISPROCESSEDBO, false));
            qApp.add(Restrictions.eq(OBPOSAppCashup.PROPERTY_ISPROCESSED, false));

            final int num = qApp.count();

            if (num > 0) {
              result.put("hasNotClosedCashup","Y");
            }

            return result;
          } catch (Exception e) {
            throw new OBException(e);
          }
        }
}