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

package org.openbravo.service.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.service.OBServiceException;

/**
 * Catches all webservice servlets and dispatches the call to a real webservice.
 * 
 * @author mtaal
 */

public class WebServiceServlet extends BaseWebServiceServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException {
	try {
	    final String segment = WebServiceUtil.getInstance()
		    .getFirstSegment(request.getPathInfo());
	    final WebService ws = getWebService(segment);
	    if (ws != null) {
		ws.doGet(getRemainingPath(request.getPathInfo(), segment),
			request, response);
	    }
	} catch (OBException e) {
	    throw e;
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException {
	try {
	    final String segment = WebServiceUtil.getInstance()
		    .getFirstSegment(request.getPathInfo());
	    final WebService ws = getWebService(segment);
	    if (ws != null) {
		ws.doPost(getRemainingPath(request.getPathInfo(), segment),
			request, response);
	    }
	} catch (OBException e) {
	    throw e;
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }

    @Override
    public void doDelete(HttpServletRequest request,
	    HttpServletResponse response) throws IOException, ServletException {
	try {
	    final String segment = WebServiceUtil.getInstance()
		    .getFirstSegment(request.getPathInfo());
	    final WebService ws = getWebService(segment);
	    if (ws != null) {
		ws.doDelete(getRemainingPath(request.getPathInfo(), segment),
			request, response);
	    }
	} catch (OBException e) {
	    throw e;
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response)
	    throws IOException, ServletException {
	try {
	    final String segment = WebServiceUtil.getInstance()
		    .getFirstSegment(request.getPathInfo());
	    final WebService ws = getWebService(segment);
	    if (ws != null) {
		ws.doPut(getRemainingPath(request.getPathInfo(), segment),
			request, response);
	    }
	} catch (OBException e) {
	    throw e;
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }

    private WebService getWebService(String segment) {
	final Object o = OBProvider.getInstance().get(segment);
	if (o instanceof WebService) {
	    return (WebService) o;
	}
	throw new OBServiceException("No WebService found using the name "
		+ segment);
    }

    private String getRemainingPath(String pathInfo, String segment) {
	String localPathInfo = pathInfo;
	if (pathInfo.startsWith("/")) {
	    localPathInfo = pathInfo.substring(1);
	}
	if (localPathInfo.length() == segment.length()) {
	    return "";
	}
	return localPathInfo.substring(segment.length());
    }
}