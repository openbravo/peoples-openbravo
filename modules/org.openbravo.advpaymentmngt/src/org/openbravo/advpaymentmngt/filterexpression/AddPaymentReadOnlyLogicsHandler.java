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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.advpaymentmngt.filterexpression;

import java.util.Map;

import javax.enterprise.context.RequestScoped;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.ad.ui.Window;
import org.openbravo.scheduling.ProcessLogger;

@RequestScoped
abstract class AddPaymentReadOnlyLogicsHandler {

  private static ProcessLogger logger;

  abstract boolean getPaymentDocumentNoReadOnlyLogic(Map<String, String> requestMap)
      throws JSONException;

  abstract boolean getReceivedFromReadOnlyLogic(Map<String, String> requestMap)
      throws JSONException;

  abstract boolean getPaymentMethodReadOnlyLogic(Map<String, String> requestMap)
      throws JSONException;

  abstract boolean getActualPaymentReadOnlyLogic(Map<String, String> requestMap)
      throws JSONException;

  abstract boolean getPaymentDateReadOnlyLogic(Map<String, String> requestMap) throws JSONException;

  abstract boolean getFinancialAccountReadOnlyLogic(Map<String, String> requestMap)
      throws JSONException;

  abstract boolean getCurrencyReadOnlyLogic(Map<String, String> requestMap) throws JSONException;

  protected abstract long getSeq();

  boolean getConvertedAmountReadOnlyLogic(Map<String, String> requestMap) throws JSONException {
    OBContext.setAdminMode();
    JSONObject context = new JSONObject(requestMap.get("context"));
    String strWindow = context.getString("inpwindowId");
    Window window = OBDal.getInstance().get(Window.class, strWindow == null ? "" : strWindow);

    try {
      String value = org.openbravo.erpCommon.businessUtility.Preferences.getPreferenceValue(
          "NotAllowChangeExchange", true, OBContext.getOBContext().getCurrentClient(), OBContext
              .getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), window);
      if ("Y".equals(value)) {
        return true;
      } else {
        return false;
      }
    } catch (PropertyNotFoundException e) {
      // logger.log("Property not found \n");
      return false;
    } catch (PropertyException e) {
      // logger.log("PropertyException, there is a conflict for NotAllowChangeExchange property\n");
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  boolean getConversionRateReadOnlyLogic(Map<String, String> requestMap) throws JSONException {
    OBContext.setAdminMode();
    JSONObject context = new JSONObject(requestMap.get("context"));
    String strWindow = context.getString("inpwindowId");
    Window window = OBDal.getInstance().get(Window.class, strWindow == null ? "" : strWindow);

    try {
      String value = org.openbravo.erpCommon.businessUtility.Preferences.getPreferenceValue(
          "NotAllowChangeExchange", true, OBContext.getOBContext().getCurrentClient(), OBContext
              .getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), window);
      if ("Y".equals(value)) {
        return true;
      } else {
        return false;
      }
    } catch (PropertyNotFoundException e) {
      // logger.log("Property not found \n");
      return false;
    } catch (PropertyException e) {
      // logger.log("PropertyException, there is a conflict for NotAllowChangeExchange property\n");
      return false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
