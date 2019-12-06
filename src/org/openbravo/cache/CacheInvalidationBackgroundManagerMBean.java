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
 * All portions are Copyright (C) 2008-2019 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  Mauricio Peccorini.
 ************************************************************************
 */

package org.openbravo.cache;

import javax.management.MBeanException;

public interface CacheInvalidationBackgroundManagerMBean {

  /*
   * This method determines if the caché invalidation background manager is being executed.
   */
  public boolean isStarted();

  /*
   * Start the caché invalidation background manager This will reload the caché control table, so
   * all cachés will be invalidated upon startup. The period with which the caché invalidation
   * control table is checked will also be reloaded.
   */
  public void start() throws MBeanException;

  /*
   * Force the invalidation of a cache
   */
  public void invalidateCache(String searchKey) throws MBeanException;
  
  /*
   * Stop the caché invalidation background manager
   */
  public void stop() throws MBeanException;

}
