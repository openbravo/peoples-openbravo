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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.authentication.oauth2;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.cache.TimeInvalidatedCache;
import org.openbravo.model.authentication.OAuth2LoginProvider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * Provides information about the user making an authentication request from the value of an OpenID
 * token in the JSON Web Token (JWT) format.
 */
@ApplicationScoped
class OpenIDTokenDataProvider {
  private static final Logger log = LogManager.getLogger();

  private TimeInvalidatedCache<String, JSONArray> keys = TimeInvalidatedCache.newBuilder()
      .name("Open ID Certificates")
      .expireAfterDuration(Duration.ofMinutes(10))
      .build(this::requestKeys);

  private JSONArray requestKeys(String url) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Content-Type", "application/json")
          .timeout(Duration.ofSeconds(30))
          .GET()
          .build();
      HttpResponse<String> response = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(30))
          .build()
          .send(request, HttpResponse.BodyHandlers.ofString());
      JSONObject certificates = new JSONObject(response.body());
      return certificates.getJSONArray("keys");
    } catch (Exception ex) {
      return new JSONArray();
    }
  }

  /**
   * Extracts the authentication information from the given OpenID token. For the moment the
   * returned map just contains the user email.
   *
   * @param token
   *          An OpenID token
   * @param configuration
   *          the OAuth 2.0 configuration with information that can be used to verify the token like
   *          the URL to get the public keys required by the algorithm used for encrypting the token
   *          data.
   *
   * @return the authentication information extracted from the given OpenID token
   *
   * @throws OAuth2TokenVerificationException
   *           if it is not possible to verify the token or extract the authentication data
   */
  Map<String, Object> getData(String token, OAuth2LoginProvider config)
      throws OAuth2TokenVerificationException {
    try {
      DecodedJWT decodedJWT = JWT.decode(token);
      Algorithm algorithm = getAlgorithm(decodedJWT, config.getCertificateURL());
      JWTVerifier verifier = JWT.require(algorithm).build();
      DecodedJWT verifiedJWT = verifier.verify(token);
      Map<String, Claim> claims = verifiedJWT.getClaims();
      if (claims.containsKey("email")) {
        return Map.of("email", claims.get("email").asString());
      }
      return Collections.emptyMap();
    } catch (NoSuchAlgorithmException ex) {
      throw new OAuth2TokenVerificationException("Could not retrieve data from OpenID token", ex);
    }
  }

  private Algorithm getAlgorithm(DecodedJWT decodedJWT, String certificateURL)
      throws NoSuchAlgorithmException, OAuth2TokenVerificationException {
    String algorithm = decodedJWT.getAlgorithm();
    String keyId = decodedJWT.getKeyId();
    if ("RS256".equals(algorithm)) {
      return getKey(certificateURL, keyId).map(this::getRS256Algorithm)
          .orElseThrow(
              () -> new OAuth2TokenVerificationException("Could not get algorithm " + algorithm));
    }
    throw new NoSuchAlgorithmException("Unsupported algorithm: " + algorithm);
  }

  private Algorithm getRS256Algorithm(JSONObject key) {
    try {
      byte[] modulusBytes = Base64.getUrlDecoder().decode(key.getString("n"));
      byte[] exponentBytes = Base64.getUrlDecoder().decode(key.getString("e"));
      RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(1, modulusBytes),
          new BigInteger(1, exponentBytes));
      PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
      return Algorithm.RSA256((RSAPublicKey) publicKey);
    } catch (JSONException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
      log.error("Error getting RS256 algorithm", ex);
      return null;
    }
  }

  private Optional<JSONObject> getKey(String url, String keyId) {
    try {
      JSONArray array = keys.get(url);
      for (int i = 0; i < array.length(); i += 1) {
        JSONObject key = array.getJSONObject(i);
        if (keyId.equals(key.getString("kid"))) {
          return Optional.of(key);
        }
      }
      return Optional.empty();
    } catch (JSONException ex) {
      log.error("Error getting key from URL {}", url, ex);
      return Optional.empty();
    }
  }
}
