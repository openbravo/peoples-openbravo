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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.client.kernel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used as a cache for the static resources (js and css) used in the application. It
 * keeps the information needed to make use of those resources without the need of generating them
 * again.
 */
@ApplicationScoped
public class StaticResourceProvider implements StaticResourceProviderMBean {
  final static private Logger log = LoggerFactory.getLogger(StaticResourceProvider.class);

  private ConcurrentHashMap<String, String> staticResources = new ConcurrentHashMap<>();

  /**
   * Returns the information stored for a particular static resource whose identifying name is
   * passed as parameter.
   * 
   * @param resourceName
   *          the identifying name of the static resource
   * 
   * @return a String with information of the static resource
   */
  public String getStaticResourceCachedInfo(String resourceName) {
    return staticResources.get(resourceName);
  }

  /**
   * Stores the information related to a particular static resource.
   * 
   * @param resourceName
   *          the identifying name of the static resource
   * @param content
   *          the information about the static resource to keep in cache
   */
  public void putStaticResourceCachedInfo(String resourceName, String content) {
    String value = staticResources.putIfAbsent(resourceName, content);
    if (value == null) {
      log.debug("Information of {} static resource stored in cache", resourceName);
    }
  }

  /**
   * @return a Map with the information about the cached static resources.
   */
  @Override
  public Map<String, String> getCachedStaticResources() {
    return staticResources;
  }

  /**
   * Removes the cached information related to a static resource whose identifying name is passed as
   * parameter.
   * 
   * @param resourceName
   *          the identifying name of the static resource
   */
  @Override
  public void removeStaticResourceCachedInfo(String resourceName) {
    if (staticResources.containsKey(resourceName)) {
      staticResources.remove(resourceName);
      log.debug("Information of {} static resource removed from cache", resourceName);
    }
  }
}
