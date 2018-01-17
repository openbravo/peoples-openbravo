/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.process;

import java.util.Date;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.JSONProcessSimple;
import org.openbravo.retail.posterminal.OBPOSApplications;
import org.openbravo.service.json.JsonConstants;

public class LastTerminalStatusTimestamps extends JSONProcessSimple {

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    OBContext.setAdminMode(true);
    JSONObject result = new JSONObject();
    try {
      String posterminalID = jsonsent.getString("posterminalId");
      Date lastFullRefresh = null;
      Date lastIncRefresh = null;
      Date lastCacheGeneration = null;
      Date lastJSGeneration = null;

      if (jsonsent.has("lastFullRefresh") && !jsonsent.isNull("lastFullRefresh")) {
        Long lastFullRefreshUnix = jsonsent.getLong("lastFullRefresh");
        lastFullRefresh = new Date(lastFullRefreshUnix);
      }
      if (jsonsent.has("lastIncRefresh") && !jsonsent.isNull("lastIncRefresh")) {
        Long lastIncRefreshUnix = jsonsent.getLong("lastIncRefresh");
        lastIncRefresh = new Date(lastIncRefreshUnix);
      }
      if (jsonsent.has("lastCacheGeneration") && !jsonsent.isNull("lastCacheGeneration")) {
        Long lastCacheGenerationUnix = jsonsent.getLong("lastCacheGeneration");
        lastCacheGeneration = new Date(lastCacheGenerationUnix);
      }
      if (jsonsent.has("lastJSGeneration") && !jsonsent.isNull("lastJSGeneration")) {
        Long lastJSGenerationUnix = jsonsent.getLong("lastJSGeneration");
        lastJSGeneration = new Date(lastJSGenerationUnix);
      }
      OBPOSApplications posterminal = OBDal.getInstance().get(OBPOSApplications.class,
          posterminalID);

      // Set last full refresh
      if (posterminal.getTerminalLastfullrefresh() == null && lastFullRefresh != null) {
        posterminal.setTerminalLastfullrefresh(lastFullRefresh);
      } else if (lastFullRefresh != null && posterminal.getTerminalLastfullrefresh() != null
          && lastFullRefresh.getTime() > posterminal.getTerminalLastfullrefresh().getTime()) {
        posterminal.setTerminalLastfullrefresh(lastFullRefresh);
      }

      // Set last incremental refresh
      if (posterminal.getTerminalLastincrefresh() == null && lastIncRefresh != null) {
        posterminal.setTerminalLastincrefresh(lastIncRefresh);
      } else if (lastIncRefresh != null && posterminal.getTerminalLastincrefresh() != null
          && lastIncRefresh.getTime() > posterminal.getTerminalLastincrefresh().getTime()) {
        posterminal.setTerminalLastincrefresh(lastIncRefresh);
      }

      // Set last cache generation
      if (posterminal.getTerminalLastcachegeneration() == null && lastCacheGeneration != null) {
        posterminal.setTerminalLastcachegeneration(lastCacheGeneration);
      } else if (lastCacheGeneration != null
          && posterminal.getTerminalLastcachegeneration() != null
          && lastCacheGeneration.getTime() > posterminal.getTerminalLastcachegeneration().getTime()) {
        posterminal.setTerminalLastcachegeneration(lastCacheGeneration);
      }

      // Set last js generation
      if (posterminal.getTerminalLastjsgeneration() == null && lastJSGeneration != null) {
        posterminal.setTerminalLastjsgeneration(lastJSGeneration);
      } else if (lastJSGeneration != null && posterminal.getTerminalLastjsgeneration() != null
          && lastJSGeneration.getTime() > posterminal.getTerminalLastjsgeneration().getTime()) {
        posterminal.setTerminalLastjsgeneration(lastJSGeneration);
      }

      OBDal.getInstance().flush();
      result.put("status", JsonConstants.RPCREQUEST_STATUS_SUCCESS);
    } catch (Exception e) {
      result.put("status", JsonConstants.RPCREQUEST_STATUS_FAILURE);
    } finally {
      OBContext.restorePreviousMode();
    }

    return result;
  }

}
