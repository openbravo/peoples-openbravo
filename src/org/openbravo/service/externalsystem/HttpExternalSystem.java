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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Allows to communicate with an external HTTP system
 */
public class HttpExternalSystem extends ExternalSystem {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  @Override
  public CompletableFuture<ExternalSystemResponse> push(
      Supplier<? extends InputStream> inputStreamSupplier) {
    HttpClient client = HttpClient.newBuilder()
        .version(Version.HTTP_1_1)
        .connectTimeout(TIMEOUT)
        // .authenticator(Authenticator.getDefault())
        .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8000/log"))
        .timeout(TIMEOUT)
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofInputStream(inputStreamSupplier))
        .build();

    client.sendAsync(request, BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenApply(ExternalSystemResponse::new)
        .thenAccept(System.out::println);
    return null;
  }

  public static void main(String[] args) {

    HttpExternalSystem httpSystem = new HttpExternalSystem();
    JSONObject json = new JSONObject();
    try {
      json.put("kkk", "false");
    } catch (JSONException ex) {
      ex.printStackTrace();
    }
    httpSystem.push(() -> new ByteArrayInputStream(json.toString().getBytes()));
  }

}
