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
 * All portions are Copyright (C) 2022-2023 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.service.externalsystem;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Used to define the communication with an external system. Classes extending this class must be
 * annotated with {@link Protocol} to declare the communication protocol it uses.
 * 
 * The {@ExternalSystemProvider} class must be used to retrieve instances of this class.
 */
public abstract class ExternalSystem {

  private String name;

  /**
   * Sends information to the external system
   *
   * @param inputStreamSupplier
   *          A supplier of the input stream with the data to be sent
   *
   * @return a CompletableFuture<ExternalSystemResponse> containing the response data coming from
   *         the external system
   */
  public abstract CompletableFuture<ExternalSystemResponse> send(
      Supplier<? extends InputStream> inputStreamSupplier);

  /**
   * Sends information to the external system
   *
   * @param method
   *          Identifies the method or type of the send operation
   * @param inputStreamSupplier
   *          A supplier of the input stream with the data to be sent
   * @param payload
   *          Additional information used to configure the send operation
   *
   * @return a CompletableFuture<ExternalSystemResponse> containing the response data coming from
   *         the external system
   */
  public abstract CompletableFuture<ExternalSystemResponse> send(String method,
      Supplier<? extends InputStream> inputStreamSupplier, Map<String, Object> payload);

  /**
   * Sends information to the external system using the provided method and payload but without
   * using a supplier of data to be sent.
   *
   * @see #send(String, Supplier, Map)
   */
  public CompletableFuture<ExternalSystemResponse> send(String method,
      Map<String, Object> payload) {
    return send(method, null, payload);
  }

  /**
   * Sends information to the external system using the provided method but without using a supplier
   * of data to be sent nor a payload.
   *
   * @see #send(String, Supplier, Map)
   */
  public CompletableFuture<ExternalSystemResponse> send(String method) {
    return send(method, null, Collections.emptyMap());
  }

  /**
   * Configures the external system instance with the provided configuration. The extensions of this
   * class must use this method to initialize their own configuration fields.
   * 
   * @param configuration
   *          Provides the configuration data of the external system
   * @throws ExternalSystemConfigurationError
   *           in case the external system cannot be properly configured
   */
  protected void configure(ExternalSystemData configuration) {
    name = configuration.getName();
  }

  /**
   * @return the name of the external system
   */
  protected String getName() {
    return name;
  }
}
