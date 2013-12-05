/*
 ************************************************************************************
 * Copyright (C) 2013 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.util.List;

/**
 * Implement this interface to provide resources to AppCache
 * 
 * @author alostale
 * 
 */
public interface POSAppCacheResourceProvider {
  /**
   * Returns list of resources to be included in AppCache
   */
  public List<String> getResources();
}
