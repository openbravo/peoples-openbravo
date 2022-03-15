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

/**
 * Used to define the communication with an external system. Classes extending this class must be
 * annotated with {@link Protocol} to declare the communication protocol it uses.
 * 
 * The {@ExternalSystemProvider} class must be used to retrieve instances of this class.
 */
public abstract class ExternalSystem {

  /**
   * Configures the external system instance with the provided configuration
   * 
   * @param configuration
   *          Provides the configuration data of the external system
   * @throws ExternalSystemConfigurationError
   *           in case the external system can not be properly configured
   */
  protected void configure(ExternalSystemData configuration) {
  }

  /**
   * Sends information to the external system
   * 
   * @param inputStream
   *          The input stream with the data to be sent
   * 
   * @return a completable future with an ExternalSystemResponse containing the response data coming
   *         from the external system
   */
  public abstract CompletableFuture<ExternalSystemResponse> send(InputStream inputStream);
}
