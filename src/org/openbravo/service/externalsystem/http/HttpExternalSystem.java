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
package org.openbravo.service.externalsystem.http;

import java.io.InputStream;
import java.net.Authenticator;
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
import java.util.function.Supplier;
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
  private static final int MAX_RETRIES = 1;

  private String url;
  private String method;
  private int timeout;
  private HttpClient client;
  HttpAuthorizationProvider authorizationProvider;

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
    client = buildClient();
  }

  private int getTimeoutValue(HttpExternalSystemData httpConfig) {
    Long configTimeout = httpConfig.getTimeout();
    if (configTimeout > MAX_TIMEOUT) {
      return MAX_TIMEOUT;
    }
    return configTimeout.intValue();
  }

  private HttpClient buildClient() {
    HttpClient.Builder builder = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(timeout));

    if (authorizationProvider instanceof Authenticator) {
      builder.authenticator((Authenticator) authorizationProvider);
    }
    return builder.build();
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
  public CompletableFuture<ExternalSystemResponse> send(
      Supplier<? extends InputStream> inputStreamSupplier) {
    log.trace("Sending {} request to URL {} of external system {}", method, url, getName());
    if ("POST".equals(method)) {
      return sendRequest(getPOSTRequestSupplier(inputStreamSupplier));
    }
    throw new OBException("Unsupported HTTP request method " + method);
  }

  private CompletableFuture<ExternalSystemResponse> sendRequest(
      Supplier<HttpRequest> requestSupplier) {
    return sendRequestWithRetry(requestSupplier, MAX_RETRIES);
  }

  private CompletableFuture<ExternalSystemResponse> sendRequestWithRetry(
      Supplier<HttpRequest> requestSupplier, int remainingRetries) {
    HttpRequest request;
    try {
      request = requestSupplier.get();
    } catch (Exception ex) {
      log.error("Error building the HTTP request to {}", url, ex);
      return CompletableFuture.failedFuture(ex);
    }
    long requestStartTime = System.currentTimeMillis();
    return client.sendAsync(request, BodyHandlers.ofString()).thenCompose(response -> {
      boolean retry = false;
      if (!isSuccessfulResponse(response) && remainingRetries > 0) {
        retry = authorizationProvider.handleRequestRetry(response.statusCode());
      }
      if (retry) {
        return sendRequestWithRetry(requestSupplier, remainingRetries - 1);
      }
      return CompletableFuture.completedFuture(response)
          .thenApply(this::buildResponse)
          .orTimeout(timeout, TimeUnit.SECONDS);
    })
        .exceptionally(this::buildErrorResponse)
        .whenComplete((response, action) -> log.debug("{} request to {} completed in {} ms",
            request.method(), url, System.currentTimeMillis() - requestStartTime));
  }

  private Supplier<HttpRequest> getPOSTRequestSupplier(
      Supplier<? extends InputStream> inputStreamSupplier) {
    return () -> {
      HttpRequest.Builder request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .timeout(Duration.ofSeconds(timeout))
          // sent JSON content by default, if any other content type needs to be posted then this
          // should be moved to a new HTTP configuration setting
          .header("Content-Type", "application/json")
          .POST(BodyPublishers.ofInputStream(inputStreamSupplier));

      if (authorizationProvider instanceof HttpAuthorizationRequestHeaderProvider) {
        ((HttpAuthorizationRequestHeaderProvider) authorizationProvider).getHeaders()
            .entrySet()
            .stream()
            .forEach(entry -> request.header(entry.getKey(), entry.getValue()));
      }
      return request.build();
    };
  }

  private ExternalSystemResponse buildResponse(HttpResponse<String> response) {
    long buildResponseStartTime = System.currentTimeMillis();
    if (isSuccessfulResponse(response)) {
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

  private boolean isSuccessfulResponse(HttpResponse<String> response) {
    return response.statusCode() >= 200 && response.statusCode() <= 299;
  }

  private Object parseBody(String body) {
    try {
      return new JSONObject(body);
    } catch (JSONException ex) {
      return body;
    }
  }

  private ExternalSystemResponse buildErrorResponse(Throwable error) {
    String errorMessage = error.getMessage();
    if (errorMessage == null && error instanceof TimeoutException) {
      log.warn("Operation exceeded the maximum {} seconds allowed", timeout, error);
      errorMessage = "Operation exceeded the maximum " + timeout + " seconds allowed";
    }
    return ExternalSystemResponseBuilder.newBuilder().withError(errorMessage).build();
  }

  /**
   * @return the URL that this HTTP external system communicates with
   */
  public String getURL() {
    return url;
  }
}
