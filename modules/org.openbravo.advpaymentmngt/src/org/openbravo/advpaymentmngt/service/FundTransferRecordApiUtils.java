/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.advpaymentmngt.service;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.erpCommon.utility.OBMessageUtils;

public final class FundTransferRecordApiUtils {

  public static final String PROPERTY_STATUS = "status";

  public static final String PROPERTY_TITLE = "title";

  public static final String PROPERTY_ERRORS = "errors";

  public static final String UNEXPECTED_DATA_FORMAT_ERROR_MESSAGE = "RETAPGC_UnexpectedDataFormat";

  /**
   * 
   * @param request
   *          - {@link HttpServletRequest} object which contains the body of the request
   * @return A {@link JSONArray} object with the content of the request in a string format
   * @throws OBException
   * @throws IOException
   */
  public static JSONArray getRequestContent(HttpServletRequest request)
      throws OBException, IOException {
    return toJSONArray(request.getReader().lines()).orElseThrow(
        () -> new OBException(OBMessageUtils.messageBD(UNEXPECTED_DATA_FORMAT_ERROR_MESSAGE)));
  }

  /**
   * Method that builds an error response
   * 
   * @param status
   *          - the status of the response
   * @param errorMessage
   *          - {@link String} that contains the error message
   * @return {@link JSONObject} object that contains the structure of the error response
   */

  public static JSONObject buildErrorResponse(int status, String errorMessage) {
    JSONObject errorResponse = new JSONObject();
    JSONArray errors = new JSONArray();
    JSONObject errorItem = new JSONObject();
    try {
      errorItem.put(PROPERTY_STATUS, status);
      errorItem.put(PROPERTY_TITLE, errorMessage);
      errors.put(errorItem);
      errorResponse.put(PROPERTY_ERRORS, errors);
    } catch (JSONException e) {
      throw new OBException(e.getMessage(), e);
    }
    return errorResponse;

  }

  /**
   * 
   * @param requestBody
   *          - a {@link Stream} object with the content of the HTTP request body.
   * @return {@link Optional} object with the JSON Array.
   */
  private static Optional<JSONArray> toJSONArray(Stream<String> requestBody) {
    String body = requestBody.collect(Collectors.joining(System.lineSeparator()));
    try {
      if (body.startsWith("{")) {
        // received a single JSONObject
        JSONObject json = new JSONObject(body);
        JSONArray array = new JSONArray();
        array.put(json);
        return Optional.of(array);
      }
      return Optional.of(new JSONArray(body));
    } catch (JSONException ex) {
      return Optional.empty();
    }
  }
}
