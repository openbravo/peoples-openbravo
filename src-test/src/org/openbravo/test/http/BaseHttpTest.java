package org.openbravo.test.http;

import java.io.IOException;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.openbravo.base.exception.OBException;
import org.openbravo.test.base.OBBaseTest;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class BaseHttpTest extends OBBaseTest {
  private MockWebServer server = new MockWebServer();
  private String serverURL;

  @Before
  public void start() throws IOException {
    // Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);
    server.start();
    HttpUrl url = server.url("/obmockserver/");
    serverURL = url.toString();
  }

  @After
  public void shutdown() throws IOException {
    server.shutdown();
  }

  protected String getServerURL() {
    return serverURL;
  }

  protected void enqueueResponse(Map<String, Object> headers, int responseCode, String body) {
    MockResponse response = new MockResponse().setBody(body).setResponseCode(responseCode);
    headers.entrySet().forEach(entry -> response.setHeader(entry.getKey(), entry.getValue()));
    server.enqueue(response);
  }

  // TODO
  protected void getRequestData() {
    try {
      RecordedRequest request = server.takeRequest();
    } catch (InterruptedException ex) {
      throw new OBException("Could not take the request information", ex);
    }
    // request.get
    // assertEquals("POST /v1/chat/send HTTP/1.1", request.getRequestLine());
    // assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
    // assertEquals("{}", request.getBody().readUtf8());
  }
}
