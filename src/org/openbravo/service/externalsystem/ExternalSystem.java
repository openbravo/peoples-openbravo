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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Used to define the communication with an external system
 */
public abstract class ExternalSystem {

  public ExternalSystem() {
    // TODO: set config from DB model
  }

  /**
   * Push information into the external system
   * 
   * @param inputStreamSupplier
   *          Supplies the input stream with the data to be pushed
   * 
   * @return a completable future with an ExternalSystemResponse containing the response data coming
   *         from the external system
   */
  public abstract CompletableFuture<ExternalSystemResponse> push(
      Supplier<? extends InputStream> inputStreamSupplier);
}
