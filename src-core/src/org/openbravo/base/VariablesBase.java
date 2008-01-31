/*
 ************************************************************************************
 * Copyright (C) 2001-2006 Openbravo S.L.
 * Licensed under the Apache Software License version 2.0
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to  in writing,  software  distributed
 * under the License is distributed  on  an  "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR
 * CONDITIONS OF ANY KIND, either  express  or  implied.  See  the  License  for  the
 * specific language governing permissions and limitations under the License.
 ************************************************************************************
*/
package org.openbravo.base;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.List;
import java.util.Iterator;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.Logger ;
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.*;
import org.apache.commons.fileupload.servlet.*;

public class VariablesBase {
  HttpSession session;
  HttpServletRequest httpRequest;
  public boolean isMultipart = false;
  List<FileItem> items;

  static Logger log4j = Logger.getLogger(VariablesBase.class);
  public VariablesBase() {
  }


@SuppressWarnings("unchecked")
public VariablesBase(HttpServletRequest request) {
    this.session = request.getSession(true);
    this.httpRequest = request;    
    this.isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(request));
    if (isMultipart) {
      DiskFileItemFactory factory = new DiskFileItemFactory();
      //factory.setSizeThreshold(yourMaxMemorySize);
      //factory.setRepositoryPath(yourTempDirectory);
      ServletFileUpload upload = new ServletFileUpload(factory);
      //upload.setSizeMax(yourMaxRequestSize);
      try {
        items = upload.parseRequest(request);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  /**
   * Overloaded constructor, used to prevent session removal in case of multipart
   * @param request
   * @param f dummy boolean, only for overload the constructor
   */
  public VariablesBase(HttpServletRequest request, boolean f) {
    this.session = request.getSession(true);
    this.httpRequest = request;
  }

  public String getGlobalVariable(String requestParameter, String sessionAttribute, boolean clearSession,
      boolean requestRequired, boolean sessionRequired, String defaultValue) throws ServletException {
    String auxStr = getStringParameter(requestParameter);
    if (log4j.isDebugEnabled()) log4j.debug("Request parameter: " + requestParameter + ":..." + auxStr);
    if (!(auxStr.equals(""))) {
      setSessionValue(sessionAttribute, auxStr);
    } else {
      if (requestRequired) {
        throw new ServletException("Request parameter required: " + requestParameter);
      } else {
        auxStr = getSessionValue(sessionAttribute);
        if (!sessionAttribute.equalsIgnoreCase("menuVertical") && log4j.isDebugEnabled()) log4j.debug("Session attribute: " + sessionAttribute + ":..." + auxStr);
        if (auxStr.equals("")) {
          if (sessionRequired) {
            throw new ServletException("Session attribute required: " + sessionAttribute);
          } else {
            auxStr = defaultValue;
            if (log4j.isDebugEnabled()) log4j.debug("Default value:..." + auxStr);
            setSessionValue(sessionAttribute, auxStr);
          }
        } else {
          if (clearSession) {
            auxStr = defaultValue;
            if (auxStr.equals("")) removeSessionValue(sessionAttribute);
            else setSessionValue(sessionAttribute, auxStr);
          }
        }
      }
    }
    return auxStr;
  }

  public String getInGlobalVariable(String requestParameter, String sessionAttribute, boolean clearSession,
      boolean requestRequired, boolean sessionRequired, String defaultValue) throws ServletException {
    String auxStr = getInStringParameter(requestParameter);
    if (log4j.isDebugEnabled()) log4j.debug("Request IN parameter: " + requestParameter + ":..." + auxStr);
    if (!(auxStr.equals(""))) {
      setSessionValue(sessionAttribute, auxStr);
    } else {
      if (requestRequired) {
        throw new ServletException("Request IN parameter required: " + requestParameter);
      } else {
        auxStr = getSessionValue(sessionAttribute);
        if (log4j.isDebugEnabled()) log4j.debug("Session IN attribute: " + sessionAttribute + ":..." + auxStr);
        if (auxStr.equals("")) {
          if (sessionRequired) {
            throw new ServletException("Session IN attribute required: " + sessionAttribute);
          } else {
            auxStr = defaultValue;
            if (log4j.isDebugEnabled()) log4j.debug("Default value:..." + auxStr);
            setSessionValue(sessionAttribute, auxStr);
          }
        } else {
          if (clearSession) {
            auxStr = "";
            removeSessionValue(sessionAttribute);
          }
        }
      }
    }
    return auxStr;
  }

  public String getGlobalVariable(String requestParameter, String sessionAttribute, String defaultValue) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false,false, false, defaultValue);
  }

  public String getGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, true, "");
  }

  public String getRequiredGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false,true, true, "");
  }

  public String getRequestGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true,false,false, "");
  }

  public String getRequiredInputGlobalVariable(String requestParameter, String sessionAttribute, String defaultStr) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true,false,false, defaultStr);
  }

  public String getInGlobalVariable(String requestParameter, String sessionAttribute, String defaultValue) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false,false, false, defaultValue);
  }

  public String getInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, true, "");
  }

  public String getRequiredInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false,true, true, "");
  }

  public String getRequestInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, true,false,false, "");
  }

  public String getStringParameter(String parameter, String defaultValue) {
    try {
      return getStringParameter(parameter, false, defaultValue);
    }
    catch (Exception e) {return null;}
  }

  public String getStringParameter(String parameter) {
    return getStringParameter(parameter, "");
  }

  public String getRequiredStringParameter(String parameter) throws ServletException {
    return getStringParameter(parameter, true, "");
  }


  public String getStringParameter(String parameter, boolean required, String defaultValue) throws ServletException {
    String auxStr = null;
    try {
      if (isMultipart) auxStr = getMultiParameter(parameter);
      else auxStr = httpRequest.getParameter(parameter);
    } catch (Exception e) {
      if (!(required)) {
        auxStr = defaultValue;
      }
    }
    if (auxStr==null || auxStr.trim().equals("")) {
      if (required) {
        throw new ServletException("Request parameter required: " + parameter);
      } else {
        auxStr = defaultValue;
      }
    }
    if (log4j.isDebugEnabled()) log4j.debug("Request parameter: " + parameter + ":..." + auxStr);
    return auxStr;
  }

  public String getInParameter(String parameter, String defaultValue) throws ServletException {
    return getInParameter(parameter, false, defaultValue);
  }

  public String getInParameter(String parameter) throws ServletException {
    return getInParameter(parameter, false, "");
  }

  public String getRequiredInParameter(String parameter) throws ServletException {
    return getInParameter(parameter, true, "");
  }

  public String getInParameter(String parameter, boolean required, String defaultValue) throws ServletException {
    String[] auxStr=null;
    StringBuffer strResultado = new StringBuffer();
    try {
      if (isMultipart) auxStr = getMultiParameters(parameter);
      else auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e){
      if (!(required)) {
        strResultado.append(defaultValue);
      }
    }

    if (auxStr==null || auxStr.length==0 || auxStr.equals("")) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResultado.append(defaultValue);
      }
      return strResultado.toString();
    }
    if (auxStr!=null && auxStr.length>0) {
      for (int i=0;i<auxStr.length;i++) {
        if (auxStr[i].length()>0) {
          if (strResultado.length()>0) strResultado.append(",");
          strResultado.append(auxStr[i]);
        }
      }
    }

    if (strResultado.toString().equals("")) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResultado.append(defaultValue);
      }
      return strResultado.toString();
    }

    if (log4j.isDebugEnabled()) log4j.debug("Request IN parameter: " + parameter + ":...(" + strResultado.toString() + ")");

    return "(" + strResultado.toString() + ")";
  }

  public String getInStringParameter(String parameter, String defaultValue) throws ServletException {
    return getInStringParameter(parameter, false, defaultValue);
  }

  public String getInStringParameter(String parameter) throws ServletException {
    return getInStringParameter(parameter, false, "");
  }

  public String getRequiredInStringParameter(String parameter) throws ServletException {
    return getInStringParameter(parameter, true, "");
  }

  public String getInStringParameter(String parameter, boolean required, String defaultValue) throws ServletException {
    String[] auxStr=null;
    StringBuffer strResultado = new StringBuffer();
    try {
      if (isMultipart) auxStr = getMultiParameters(parameter);
      else auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e){
      if (!(required)) {
        strResultado.append(defaultValue);
      }
    }

    if (auxStr==null || auxStr.length==0) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResultado.append(defaultValue);
      }
      return strResultado.toString();
    }

    strResultado.append("('");
    for (int i=0;i<auxStr.length;i++) {
      if (i>0) {
        strResultado.append("', '");
      }
      strResultado.append(auxStr[i]);
    }
    strResultado.append("')");

    if (log4j.isDebugEnabled()) log4j.debug("Request IN parameter: " + parameter + ":..." + strResultado.toString());

    return strResultado.toString();
  }

  public String getSessionValue(String sessionAttribute) {
    return getSessionValue(sessionAttribute, "");
  }

  public String getSessionValue(String sessionAttribute, String defaultValue) {
    String auxStr = null;
    try {
      auxStr = (String) session.getAttribute(sessionAttribute.toUpperCase());
      if (auxStr==null || auxStr.trim().equals(""))
        auxStr = defaultValue;
    }
    catch (Exception e) {
      auxStr = defaultValue;
    }
    if (!sessionAttribute.equalsIgnoreCase("menuVertical")) if (log4j.isDebugEnabled()) log4j.debug("Get session attribute: " + sessionAttribute + ":..." + auxStr);
    return auxStr;
  }

  public void setSessionValue(String attribute, String value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
      if (!attribute.equalsIgnoreCase("menuVertical")) if (log4j.isDebugEnabled()) log4j.debug("Set session attribute: " + attribute + ":..." + value.toString());
    }
    catch (Exception e) {
      log4j.error("setSessionValue error: " + attribute + ":..." + value);
    }
  }

  public void removeSessionValue(String attribute) {
    try {
      if (log4j.isDebugEnabled()) log4j.debug("Remove session attribute: " + attribute + ":..." + getSessionValue(attribute));
      session.removeAttribute(attribute.toUpperCase());

    }
    catch (Exception e) {
      log4j.error("removeSessionValue error: " + attribute);
    }
  }

  public Object getSessionObject(String sessionAttribute) {
    Object auxStr = null;
    try {
      auxStr = (Object) session.getAttribute(sessionAttribute.toUpperCase());
    } catch (Exception e) {
      auxStr = null;
    }
    return auxStr;
  }

  public void setSessionObject(String attribute, Object value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
    } catch (Exception e) {
      log4j.error("setSessionObject error: " + attribute + ":..." + e);
    }
  }

  public void clearSession(boolean all) {
    if (log4j.isDebugEnabled()) log4j.debug("...: removing session");
    String target="";
    try {
      String sessionName;
      Enumeration<?> e = session.getAttributeNames();
      while (e.hasMoreElements()) {
        sessionName = (String)e.nextElement();
        if (log4j.isDebugEnabled()) log4j.debug("  session name: " + sessionName);
        if (!all && sessionName.equalsIgnoreCase("target")) target = (String) session.getAttribute(sessionName);
        session.removeAttribute(sessionName);
        e = session.getAttributeNames();
      }
    } catch (Exception e) {
      log4j.error("clearSession error " + e);
    }
    if (!target.equals("")) session.setAttribute("TARGET", target);
  }

  public Vector<String> getListFromInString(String strDatos) {
    Vector<String> fields = new Vector<String>();
    if (strDatos==null || strDatos.length()==0) return fields;
    strDatos = strDatos.trim();
    if (strDatos.equals("")) return fields;
    if (strDatos.startsWith("(")) strDatos = strDatos.substring(1, strDatos.length()-1);
    strDatos = strDatos.trim();
    if (strDatos.equals("")) return fields;
    StringTokenizer datos = new StringTokenizer(strDatos, ",", false);
    while (datos.hasMoreTokens()) {
      String token = datos.nextToken();
      if (token.startsWith("'")) token = token.substring(1, token.length()-1);
      token = token.trim();
      if (!token.equals("")) fields.addElement(token);
    }
    return fields;
  }

  public String getMultiParameter(String parameter) {
    if (!isMultipart || items==null) return "";
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && item.getFieldName().equals(parameter)) {
        try {
          return (item.getString("UTF-8"));
        } catch (Exception ex) {
          ex.printStackTrace();
          return "";
        }
      }
    }
    return "";
  }

  public String[] getMultiParameters(String parameter) {
    if (!isMultipart || items==null) return null;
    Iterator<FileItem> iter = items.iterator();
    Vector<String> result = new Vector<String>();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && item.getFieldName().equals(parameter)) {
        try {
          result.addElement(item.getString());
        } catch (Exception ex) {}
      }
    }
    String[] strResult = new String[result.size()];
    result.copyInto(strResult);
    return strResult;
  }

  public FileItem getMultiFile(String parameter) {
    if (!isMultipart || items==null) return null;
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (!item.isFormField() && item.getFieldName().equals(parameter)) return item;
    }
    return null;
  }
}
