/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */

package org.openbravo.base.secureApp;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.weld.WeldUtils;

/**
 * Provides/handles the domain checkers which determine if a specific cross domain request is
 * allowed on the OB server.
 * 
 * https://en.wikipedia.org/wiki/Cross-origin_resource_sharing
 * 
 * @author Martin Taal
 */
public class AllowedCrossDomainsHandler {

  private static final Logger log = Logger.getLogger(AllowedCrossDomainsHandler.class);

  private static AllowedCrossDomainsHandler instance = new AllowedCrossDomainsHandler();

  public static AllowedCrossDomainsHandler getInstance() {
    return instance;
  }

  public static void setInstance(AllowedCrossDomainsHandler instance) {
    AllowedCrossDomainsHandler.instance = instance;
  }

  private Collection<AllowedCrossDomainsChecker> checkers = null;

  /**
   * Returns true if the origin is allowed, in that case the cors headers can be set (
   * {@link #setCORSHeaders(HttpServletRequest, HttpServletResponse)}.
   * 
   * @param request
   * @param origin
   *          , the origin can be obtained from the request
   * @return
   */
  public boolean isAllowedOrigin(HttpServletRequest request, String origin) {
    for (AllowedCrossDomainsChecker checker : getCheckers()) {
      if (checker.isAllowedOrigin(request, origin)) {
        return true;
      }
    }
    return false;
  }

  private Collection<AllowedCrossDomainsChecker> getCheckers() {
    if (checkers == null) {
      setCheckers();
    }
    return checkers;
  }

  private synchronized void setCheckers() {
    if (checkers != null) {
      return;
    }
    final Collection<AllowedCrossDomainsChecker> localCheckers = new ArrayList<AllowedCrossDomainsChecker>();
    for (AllowedCrossDomainsChecker checker : WeldUtils
        .getInstances(AllowedCrossDomainsChecker.class)) {
      localCheckers.add(checker);
    }
    checkers = localCheckers;
  }

  /**
   * Utility method to set CORS headers on a request.
   */
  public void setCORSHeaders(HttpServletRequest request, HttpServletResponse response) {

    // don't do anything if no checkers anyway
    if (getCheckers().isEmpty()) {
      return;
    }

    try {
      final String origin = request.getHeader("Origin");

      if (origin != null && !origin.equals("")) {

        if (!isAllowedOrigin(request, origin)) {
          log.error("Origin " + origin + " is not allowed, request information: "
              + request.getRequestURL() + "-" + request.getQueryString());
          return;
        }

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
            "Content-Type, Origin, Accept, X-Requested-With, Access-Control-Allow-Credentials");

        response.setHeader("Access-Control-Max-Age", "10000");
      }
    } catch (Exception logIt) {
      // on purpose not stopping on this to retain some robustness
      log.error(
          "Error when setting cors headers " + logIt.getMessage() + " " + request.getRequestURL()
              + " " + request.getQueryString(), logIt);
    }
  }

  /**
   * Implementation provided by modules which determine if a request is coming from an allowed
   * origin.
   * 
   * @author mtaal
   */
  public static abstract class AllowedCrossDomainsChecker {

    public abstract boolean isAllowedOrigin(HttpServletRequest request, String origin);

  }

}
