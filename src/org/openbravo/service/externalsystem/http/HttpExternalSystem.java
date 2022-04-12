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
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
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
  private static final Logger log = LogManager.getLogger();
  public static final int MAX_TIMEOUT = 30;

  private String url;
  private String method;
  private int timeout;
  private HttpClient client;
  private HttpAuthorizationProvider authorizationProvider;

  @Inject
  @Any
  private Instance<HttpAuthorizationProvider> authorizationProviders;

  @Override
  public void configure(ExternalSystemData configuration) {
    super.configure(configuration);

    HttpExternalSystemData httpConfig = configuration.getExternalSystemHttpList()
        .stream()
        .filter(HttpExternalSystemData::isActive)
        .findFirst()
        .orElseThrow(() -> new ExternalSystemConfigurationError(
            "No HTTP configuration found for external system with ID " + configuration.getId()));

    url = httpConfig.getURL();
    method = httpConfig.getRequestMethod();
    timeout = getTimeoutValue(httpConfig);
    authorizationProvider = newHttpAuthorizationProvider(httpConfig);
    client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
  }

  private int getTimeoutValue(HttpExternalSystemData httpConfig) {
    Long configTimeout = httpConfig.getTimeout();
    if (configTimeout > MAX_TIMEOUT) {
      return MAX_TIMEOUT;
    }
    return configTimeout.intValue();
  }

  private HttpAuthorizationProvider newHttpAuthorizationProvider(
      HttpExternalSystemData httpConfig) {
    String authorizationType = httpConfig.getAuthorizationType();
    HttpAuthorizationProvider provider = authorizationProviders
        .select(new HttpAuthorizationMethodSelector(authorizationType))
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
          return list.get(0);
        }));
    provider.init(httpConfig);
    return provider;
  }

  @Override
  public CompletableFuture<ExternalSystemResponse> send(InputStream inputStream) {
    log.trace("Sending {} request to URL {} of external system {}", method, url, getName());
    if ("POST".equals(method)) {
      return post(url, inputStream);
    }
    throw new OBException("Unsupported HTTP request method " + method);
  }

  private CompletableFuture<ExternalSystemResponse> post(String postURL, InputStream inputStream) {
    HttpRequest.Builder request = HttpRequest.newBuilder()
        .uri(URI.create(postURL))
        .timeout(Duration.ofSeconds(timeout))
        // sent JSON content by default, if any other content type needs to be posted then this
        // should be moved to a new HTTP configuration setting
        .header("Content-Type", "application/json")
        .POST(BodyPublishers.ofInputStream(() -> inputStream));

    authorizationProvider.getHeaders()
        .entrySet()
        .stream()
        .forEach(entry -> request.header(entry.getKey(), entry.getValue()));

    long requestStartTime = System.currentTimeMillis();
    return client.sendAsync(request.build(), BodyHandlers.ofString())
        .thenApply(this::buildResponse)
        .orTimeout(timeout, TimeUnit.SECONDS)
        .exceptionally(this::buildErrorResponse)
        .whenComplete((response, action) -> log.trace("HTTP POST request to {} completed in {} ms",
            postURL, System.currentTimeMillis() - requestStartTime));
  }

  private ExternalSystemResponse buildResponse(HttpResponse<String> response) {
    long buildResponseStartTime = System.currentTimeMillis();
    boolean requestSuccess = response.statusCode() >= 200 && response.statusCode() <= 299;
    if (requestSuccess) {
      ExternalSystemResponse externalSystemResponse = ExternalSystemResponseBuilder.newBuilder()
          .withData(parseBody(response.body()))
          .withStatusCode(response.statusCode())
          .withType(Type.SUCCESS)
          .build();
      log.trace("HTTP successful response processed in {} ms",
          () -> (System.currentTimeMillis() - buildResponseStartTime));
      return externalSystemResponse;
    }
    Object error = parseBody(response.body());
    ExternalSystemResponse externalSystemResponse = ExternalSystemResponseBuilder.newBuilder()
        .withError(error != null ? error : "Response Status Code: " + response.statusCode())
        .withStatusCode(response.statusCode())
        .withType(Type.ERROR)
        .build();
    log.trace("HTTP error response processed in {} ms",
        () -> (System.currentTimeMillis() - buildResponseStartTime));
    return externalSystemResponse;
  }

  private Object parseBody(String body) {
    try {
      return new JSONObject(body);
    } catch (JSONException ex) {
      return body;
    }
  }

  private ExternalSystemResponse buildErrorResponse(Throwable error) {
    long buildErrorResponseStartTime = System.currentTimeMillis();
    String errorMessage = error.getMessage();
    if (errorMessage == null && error instanceof TimeoutException) {
      errorMessage = "Operation exceeded the maximum " + timeout + " seconds allowed";
    }
    ExternalSystemResponse externalSystemResponse = ExternalSystemResponseBuilder.newBuilder()
        .withError(errorMessage)
        .build();
    log.trace("HTTP error response processed in {} ms",
        () -> (System.currentTimeMillis() - buildErrorResponseStartTime));
    return externalSystemResponse;
  }

  /**
   * @return the URL that this HTTP external system communicates with
   */
  public String getURL() {
    return url;
  }
}
