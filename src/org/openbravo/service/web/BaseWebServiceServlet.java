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
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.util.Check;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.model.ad.access.User;
import org.openbravo.utils.CryptoSHA1BASE64;

/**
 * This servlet takes care of authenticating and it ensures that the correct
 * user context is set for the rest of the servlet to operate. It supports
 * basicauthentication as well as url parameter based authentication. It also
 * ensures that the correct http response code is returned.
 * 
 * @author mtaal
 */

public class BaseWebServiceServlet extends HttpServlet {

    public static final String LOGIN_PARAM = "l";
    public static final String PASSWORD_PARAM = "p";

    private static final long serialVersionUID = 1L;

    @Override
    protected void service(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {

	// already logged in?
	if (OBContext.getOBContext() != null) {
	    doService(request, response);
	    // do the login action
	} else if (isLoggedIn(request, response)) {
	    doService(request, response);
	} else {
	    response.setHeader("WWW-Authenticate", "Basic realm=\"Openbravo\"");
	    response.setStatus(401);
	}
    }

    private void doService(HttpServletRequest request,
	    HttpServletResponse response) throws ServletException, IOException {
	try {
	    super.service(request, response);
	    response.setStatus(200);
	} catch (InvalidRequestException e) {
	    SessionHandler.getInstance().setDoRollback(true);
	    e.printStackTrace(System.err);
	    response.setStatus(400);
	    final Writer w = response.getWriter();
	    w.write(WebServiceUtil.getInstance().createErrorXML(e));
	    w.close();
	} catch (InvalidContentException e) {
	    SessionHandler.getInstance().setDoRollback(true);
	    e.printStackTrace(System.err);
	    response.setStatus(409);
	    final Writer w = response.getWriter();
	    w.write(WebServiceUtil.getInstance().createErrorXML(e));
	    w.close();
	} catch (ResourceNotFoundException e) {
	    SessionHandler.getInstance().setDoRollback(true);
	    e.printStackTrace(System.err);
	    response.setStatus(404);
	    final Writer w = response.getWriter();
	    w.write(WebServiceUtil.getInstance().createErrorXML(e));
	    w.close();
	} catch (OBSecurityException e) {
	    SessionHandler.getInstance().setDoRollback(true);
	    e.printStackTrace(System.err);
	    response.setStatus(401);
	    final Writer w = response.getWriter();
	    w.write(WebServiceUtil.getInstance().createErrorXML(e));
	    w.close();
	} catch (Throwable t) {
	    SessionHandler.getInstance().setDoRollback(true);
	    t.printStackTrace(System.err);
	    response.setStatus(500);
	    final Writer w = response.getWriter();
	    w.write(WebServiceUtil.getInstance().createErrorXML(t));
	    w.close();
	}
    }

    private boolean isLoggedIn(HttpServletRequest request,
	    HttpServletResponse response) {
	String login = request.getParameter(LOGIN_PARAM);
	String password = request.getParameter(PASSWORD_PARAM);
	String userId = null;
	if (login != null && password != null) {
	    userId = getValidUserId(login, password);
	} else { // use basic authentication
	    userId = doBasicAuthentication(request);
	}
	if (userId != null) {
	    OBContext.setOBContext(UserContextCache.getInstance()
		    .getCreateOBContext(userId));
	    return true;
	} else {
	    return false;
	}
    }

    private String doBasicAuthentication(HttpServletRequest request) {
	try {
	    final String auth = request.getHeader("Authorization");
	    if (auth == null) {
		return null;
	    }
	    if (!auth.toUpperCase().startsWith("BASIC ")) {
		return null; // only BASIC supported
	    }

	    // user and password come after BASIC
	    final String userpassEncoded = auth.substring(6);

	    // Decode it, using any base 64 decoder
	    sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
	    final String decodedUserPass = new String(dec
		    .decodeBuffer(userpassEncoded));
	    final int index = decodedUserPass.indexOf(":");
	    if (index == -1) {
		return null;
	    }
	    final String login = decodedUserPass.substring(0, index);
	    final String password = decodedUserPass.substring(index + 1);
	    return getValidUserId(login, password);
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }

    private String getValidUserId(String login, String password) {
	try {
	    final String encodedPwd = CryptoSHA1BASE64.hash(password);
	    // now search with the login and password
	    final Query qry = SessionHandler.getInstance().createQuery(
		    "select id from " + User.class.getName()
			    + " where username=? and password=?");
	    qry.setParameter(0, login);
	    qry.setParameter(1, encodedPwd);
	    final List<?> list = qry.list();
	    Check.isTrue(list.size() == 0 || list.size() == 1,
		    "Zero or one user expected for login " + login
			    + " but found " + list.size() + " users ");
	    if (list.size() == 0) {
		return null;
	    }
	    return (String) list.get(0);
	} catch (Exception e) {
	    throw new OBException(e);
	}
    }
}