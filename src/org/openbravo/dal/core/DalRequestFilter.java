/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.dal.core;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * The DalRequestFilter ensures that the request thread is handled inside of a
 * DalThreadHandler. In addition it initializes the sessionfactory. Although
 * this is not required (session factory initialization is done automatically
 * also), it is better for test/debug purposes.
 * 
 * The DalRequestFilter is enabled by setting it in the web.xml file:
 * 
 * <filter> <filter-name>dalFilter</filter-name>
 * <filter-class>org.openbravo.dal.core.DalRequestFilter</filter-class>
 * </filter>
 * 
 * <filter-mapping>
 * 
 * <filter-name>dalFilter</filter-name> <url-pattern>/*</url-pattern>
 * 
 * </filter-mapping>
 * 
 * Note the url-pattern can be defined more strictly if it is possible to
 * identify the pages which require a session/transaction.
 * 
 * @author mtaal
 */

public class DalRequestFilter implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {
        DalLayerInitializer.getInstance().initialize();
    }

    public void destroy() {
    }

    public void doFilter(final ServletRequest request,
            final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        final DalThreadHandler dth = new DalThreadHandler() {

            @Override
            public void doBefore() {
                OBContext.setOBContext((HttpServletRequest) request);
            }

            @Override
            protected void doAction() throws Exception {
                chain.doFilter(request, response);
            }

            // note OBContext is set to null in DalThreadHandler
        };

        dth.run();
    }
}