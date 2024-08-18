/*
 ************************************************************************************
 * Copyright (C) 2023 Openbravo S.L.U.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
 */
package org.openbravo.erpCommon.security;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.service.web.BaseWebServiceServlet;

/**
 * Endpoint to receive reports for CSP violations
 */
public class CspReportHandler extends BaseWebServiceServlet {

  private static final Logger log = LogManager.getLogger();
  private static final long serialVersionUID = 1L;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    log.debug("CSP Violation: {}", () -> buildReport(request));
  }

  private String buildReport(HttpServletRequest request) {
    StringBuilder report = new StringBuilder();
    try (BufferedReader reader = request.getReader()) {
      String line;
      while ((line = reader.readLine()) != null) {
        report.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return report.toString();
  }
}
