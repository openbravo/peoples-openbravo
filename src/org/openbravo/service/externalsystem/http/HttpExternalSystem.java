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
package org.openbravo.service.externalsystem.http;

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
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.openbravo.base.exception.OBException;
import org.openbravo.service.externalsystem.ExternalSystem;
import org.openbravo.service.externalsystem.ExternalSystemConfigurationError;
import org.openbravo.service.externalsystem.ExternalSystemData;
import org.openbravo.service.externalsystem.ExternalSystemResponse;
import org.openbravo.service.externalsystem.ExternalSystemResponse.Type;
import org.openbravo.service.externalsystem.ExternalSystemResponseBuilder;
import org.openbravo.service.externalsystem.HttpExternalSystemData;
import org.openbravo.service.externalsystem.Protocol;

/**
 * Allows to communicate with an external system through HTTP requests
 */
@Protocol("HTTP")
public class HttpExternalSystem extends ExternalSystem {

  // Fixed timeout, this can be moved to an HTTP configuration setting if needed
  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  private String url;
  private String testURL;
  private HttpClient client;
  private HttpAuthorizationProvider authorizationProvider;
  private String method;

  @Inject
  @Any
  private Instance<HttpAuthorizationProvider> authorizationProviders;

  @Override
  public void configure(ExternalSystemData configuration) {
    HttpExternalSystemData httpConfig = configuration.getHttpExternalSystemList()
        .stream()
        .filter(HttpExternalSystemData::isActive)
        .findFirst()
        .orElseThrow(() -> new ExternalSystemConfigurationError(
            "No HTTP configuration found for external system with ID " + configuration.getId()));

    url = httpConfig.getURL();
    testURL = httpConfig.getTestURL();
    method = httpConfig.getRequestMethod();
    authorizationProvider = getHttpAuthorizationProvider(httpConfig);
    client = HttpClient.newBuilder().version(Version.HTTP_1_1).connectTimeout(TIMEOUT).build();
  }

  private HttpAuthorizationProvider getHttpAuthorizationProvider(
      HttpExternalSystemData httpConfig) {
    String authorizationType = httpConfig.getAuthorizationType();
    return authorizationProviders.select(new HttpAuthorizationMethodSelector(authorizationType))
        .stream()
        .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
          if (list.isEmpty()) {
            throw new ExternalSystemConfigurationError(
                "No HTTP authorization provider found for method " + authorizationType);
          }
          if (list.size() > 1) {
            // For the moment it is only supported to have one HttpAuthorizationProvider instance
            // per authorization type
            throw new ExternalSystemConfigurationError(
                "Found multiple HTTP authorization providers for method " + authorizationType);
          }
          HttpAuthorizationProvider provider = list.get(0);
          provider.init(httpConfig);
          return provider;
        }));
  }

  @Override
  public CompletableFuture<ExternalSystemResponse> send(InputStream inputStream) {
    if ("POST".equals(method)) {
      return post(url, inputStream);
    }
    throw new OBException("Unsupported HTTP request method " + method);
  }

  @Override
  public CompletableFuture<ExternalSystemResponse> test(InputStream inputStream) {
    if ("POST".equals(method)) {
      return post(testURL, inputStream);
    }
    throw new OBException("Unsupported HTTP request method " + method);
  }

  private CompletableFuture<ExternalSystemResponse> post(String postURL, InputStream inputStream) {
    HttpRequest.Builder request = HttpRequest.newBuilder()
        .uri(URI.create(postURL))
        .timeout(TIMEOUT)
        // sent JSON content by default, if any other content type needs to be posted then this
        // should be moved to a new HTTP configuration setting
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofInputStream(() -> inputStream));

    authorizationProvider.getHeaders()
        .entrySet()
        .stream()
        .forEach(entry -> request.header(entry.getKey(), entry.getValue()));

    return client.sendAsync(request.build(), BodyHandlers.ofString())
        .thenApply(this::buildResponse)
        .exceptionally(this::buildErrorResponse);
  }

  private ExternalSystemResponse buildResponse(HttpResponse<String> response) {
    boolean requestSuccess = response.statusCode() >= 200 && response.statusCode() <= 299;
    if (requestSuccess) {
      return ExternalSystemResponseBuilder.newBuilder()
          .withData(response.body())
          .withStatusCode(response.statusCode())
          .withType(Type.SUCESS)
          .build();
    }
    String error = response.body();
    return ExternalSystemResponseBuilder.newBuilder()
        .withError(error != null ? error : "Response Status Code: " + response.statusCode())
        .withStatusCode(response.statusCode())
        .withType(Type.ERROR)
        .build();
  }

  private ExternalSystemResponse buildErrorResponse(Throwable error) {
    return ExternalSystemResponseBuilder.newBuilder().withError(error.getMessage()).build();
  }
}
