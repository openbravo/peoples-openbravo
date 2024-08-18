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

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;

/**
 * Filter in charge if include some security headers in the servlet response
 */
public class HttpResponseSecurityFilter implements Filter {

  private static final String DEFAULT_CSP_POLICY = "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-src 'self'";

  @Override
  public void init(FilterConfig config) {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletResponse httpResp = (HttpServletResponse) response;

    addXContentTypeOptionsHeader(httpResp);
    addCSPReportOnlyHeader(httpResp);

    chain.doFilter(request, response);
  }

  private void addCSPReportOnlyHeader(HttpServletResponse httpResp) {
    if (OBContext.getOBContext() == null) {
      return;
    }
    Properties obProperties = OBPropertiesProvider.getInstance().getOpenbravoProperties();
    String contextName = obProperties.getProperty("context.name");
    String policyProperty = obProperties.getProperty("csp.policy");
    String cspPolicy = StringUtils.isBlank(policyProperty) ? DEFAULT_CSP_POLICY : policyProperty;
    String reportUri = "report-uri /" + contextName + "/csp";
    httpResp.setHeader("Content-Security-Policy-Report-Only", cspPolicy + " " + reportUri + ";");
  }

  private void addXContentTypeOptionsHeader(HttpServletResponse httpResp) {
    httpResp.setHeader("X-Content-Type-Options", "nosniff");
  }

  @Override
  public void destroy() {
  }

}
