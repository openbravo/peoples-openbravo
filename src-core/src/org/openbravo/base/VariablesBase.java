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
import org.openbravo.utils.FormatUtilities;

/**
 * This class is used to manage parameters passed to the servlets either 
 * using the URL with HTTP GET method or through the HTTP POST method. 
 * 
 * @author Openbravo
 *
 */
public class VariablesBase {
  HttpSession session;
  HttpServletRequest httpRequest;
  public boolean isMultipart = false;
  List<FileItem> items;

  static Logger log4j = Logger.getLogger(VariablesBase.class);

  /**
   * Default empty constructor
   */
  public VariablesBase() {
  }

  /**
   * Basic constructor that takes the request object and saves it to be used 
   * by the subsequent methods
   * 
   * @param request HttpServletRequest object originating from the user request.
   */
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
   * Overloaded constructor, used to prevent session removal in case of 
   * multipart requests.
   * 
   * @param request HttpServletRequest object originating from the user request.
   * @param f       Dummy boolean only used for overloading the constructor.
   */
  public VariablesBase(HttpServletRequest request, boolean f) {
    this.session = request.getSession(true);
    this.httpRequest = request;
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter.
   * Before returning the final value it also saves it to the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method 
   * @param sessionAttribute  the name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found 
   * @param clearSession      If true, then the value found within the
   *                          session variable specified by the sessionAttribute
   *                          parameter is cleared and reset to the value 
   *                          specified by the defaultValue parameter. This will
   *                          consequently also be the value returned then.
   * @param requestRequired   If true, method throws an exception in 
   *                          case the parameter is not found among the ones
   *                          passed to the servlet by the HTTP GET/POST
   *                          method.
   * @param sessionRequired   If true, method throws an exception in 
   *                          case the parameter is not found among the ones
   *                          passed to the servlet by the HTTP GET/POST
   *                          method or the ones stored in the session.
   * @param defaultValue      If requestRequired or sessionRequired are false 
   *                          then the value returned by the method will take 
   *                          this value in case the parameter value is not 
   *                          found within the sought locations.
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
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

  /**
   * Returns the set of comma separated values passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter.
   * Before returning the final value it also saves it to the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found.
   * @param clearSession      If true, then the value of the session variable
   *                          specified by the sessionAttribute parameter is 
   *                          cleared. Consequently, an empty string is returned
   *                          in case requestParameter was not found previously.
   * @param requestRequired   If true, method throws an exception in 
   *                          case the parameter is not found among the ones
   *                          passed to the servlet by the HTTP GET/POST
   *                          method.
   * @param sessionRequired   If true, method throws an exception in 
   *                          case the parameter is not found among the ones
   *                          passed to the servlet by the HTTP GET/POST
   *                          method or the ones stored in the session.
   * @param defaultValue      If requestRequired or sessionRequired are false 
   *                          then the value returned by the method will take 
   *                          this value in case the parameter value is not 
   *                          found within the sought locations.
   * @return                  String containing the set of comma separated values
   *                          within parentheses. For example ('value1', 'value2').
   * @throws ServletException
   */
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

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter. If none are found
   * the defaultValue is returned.
   * Before returning the final value it also saves it to the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found. 
   * @param defaultValue      The value returned by the method will take 
   *                          this value in case the parameter value is not 
   *                          found within the sought locations.
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute, String defaultValue) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, false, defaultValue);
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter. If none are found, an 
   * Exception is thrown.
   * If one of the values is found, it is also stored into the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found. 
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
  public String getGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, false, true, "");
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, an exception is thrown. Otherwise the value
   * is also stored into the session variable with the name specified by 
   * sessionAtribute parameter before being returned. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method
   * @param sessionAttribute  The name of the session variable where the value
   *                          of the parameter specified by the requestParameter
   *                          should be stored.
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, false, true, true, "");
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter. If none are found
   * an empty string is returned.
   * Before returning the final value it also clears the session variable 
   * with the name specified by sessionAtribute parameter and sets its value to
   * whatever value is being returned.
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found.
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequestGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true,false,false, "");
  }

  /**
   * Returns the value of the requestParameter passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter. If none are found
   * the value of defaultStr parameter is returned.
   * Before returning the final value it also clears the session variable 
   * with the name specified by sessionAtribute parameter and sets its value to
   * whatever value is being returned.
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found.
   * @param defaultStr        The value returned by the method will take 
   *                          this value in case the parameter value is not 
   *                          found within the sought locations.
   * @return                  String containing the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredInputGlobalVariable(String requestParameter, String sessionAttribute, String defaultStr) throws ServletException {
    return getGlobalVariable(requestParameter, sessionAttribute, true,false,false, defaultStr);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter.
   * Before returning the final value it also saves it to the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found.
   * @param defaultValue      The value returned by the method will take 
   *                          this value in case the parameter value is not 
   *                          found within the sought locations.
   * @return                  String containing the set of comma separated values
   *                          within parentheses. For example ('value1', 'value2').
   * @throws ServletException
   */  
  public String getInGlobalVariable(String requestParameter, String sessionAttribute, String defaultValue) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, false, defaultValue);
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the  
   * HTTP GET/POST method. If this parameter is not found among the ones 
   * submitted to the servlet, it then tries to return the session value 
   * specified by the sessionAttribute parameter. If not found here, it 
   * throws an exception.
   * If successful, it also saves the return to the session variable 
   * with the name specified by sessionAtribute parameter. 
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the parameter to be retrieved from the 
   *                          session variable in case the requestParameter is 
   *                          not found.
   * @return                  String containing the set of comma separated values
   *                          within parentheses. For example ('value1', 'value2').
   * @throws ServletException
   */  
  public String getInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, false, true, "");
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the  
   * HTTP GET/POST method. If not found, it throws an exception.
   * If successful, it also saves the return to the session variable 
   * with the name specified by sessionAtribute parameter.
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the session variable to set the value
   *                          to in case the parameter value is successfully 
   *                          found. 
   * @return                  String containing the set of comma separated values
   *                          within parentheses. For example ('value1', 'value2').
   * @throws ServletException
   */  
  public String getRequiredInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, false, true, true, "");
  }

  /**
   * Returns the set of comma separated values passed to the servlet by the  
   * HTTP GET/POST method. If found, it saves the value into a session variable
   * specified by the sessionAttribute parameter adn returns it. If not found, 
   * it clears the session variable specified by the sessionAttribute parameter 
   * and returns an empty string.
   * 
   * @param requestParameter  The name of the parameter to be retrieved from the
   *                          parameters passed to the servlet by the 
   *                          HTTP GET/POST method. 
   * @param sessionAttribute  The name of the session variable to clear in case
   *                          the above parameter does not exist.
   * @return                  String containing the set of comma separated values
   *                          within parentheses. For example ('value1', 'value2').
   * @throws ServletException
   */  
  public String getRequestInGlobalVariable(String requestParameter, String sessionAttribute) throws ServletException {
    return getInGlobalVariable(requestParameter, sessionAttribute, true, false, false, "");
  }

  /**
   * Retrieves a single parameter value passed to the servlet by the 
   * HTTP GET/POST method. If value is not found among submitted data, the 
   * defaultValue will be returned.
   * 
   * @param parameter    The name of the parameter that needs to be retrieved.
   * @param defaultValue Value to be returned in case the parameter was not 
   *                     passed to the servlet.
   * @return             String containing the value of the parameter passed
   *                     to the servlet by the HTTP GET/POST method.
   */
  public String getStringParameter(String parameter, String defaultValue) {
    try {
      return getStringParameter(parameter, false, defaultValue);
    }
    catch (Exception e) {return null;}
  }

  /**
   * Retrieves a single parameter value passed to the servlet by the 
   * HTTP GET/POST method. If value is not found among submitted data, an empty 
   * string is returned.
   * 
   * @param  parameter The name of the parameter to be retrieved.
   * @return           String with the value of the parameter. If value is not
   *                   found among submitted data, an empty string is returned.
   */
  public String getStringParameter(String parameter) {
    return getStringParameter(parameter, "");
  }

  /**
   * Retrieves the single parameter value passed to the servlet by the 
   * HTTP GET/POST method. If value is not found among submitted data, a 
   * ServletException is thrown.
   * 
   * @param  parameter        The name of the parameter to be retrieved.
   * @return                  String with the value of the parameter.
   * @throws ServletException
   */
  public String getRequiredStringParameter(String parameter) throws ServletException {
    return getStringParameter(parameter, true, "");
  }

  /**
   * Retrieves the single parameter value passed to the servlet by the 
   * HTTP GET/POST method.
   * 
   * @param  parameter        The name of the parameter to be retrieved.
   * @param  required         If true, method throws an exception if the value
   *                          of the parameter is not found in the submitted data.
   * @param  defaultValue     If parameter is not required, this will be the 
   *                          default value the parameter will take if not 
   *                          found in the submitted data. 
   * @return                  String with the value of the parameter.
   * @throws ServletException 
   */
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
    
    auxStr = FormatUtilities.sanitizeInput(auxStr);
    
    if (log4j.isDebugEnabled()) log4j.debug("Request parameter: " + parameter + ":..." + auxStr);
    return auxStr;
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as 
   * passed to the servlet by the HTTP POST method.
   * String returned is in the form (value1,value2,...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * as they are, no quotation marks.
   * If there is no such parameter for this entry, the defaultValue is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @param defaultValue      The value that will be returned in case the 
   *                          parameter is not found among the data submitted 
   *                          to the servlet.
   * @return                  String containing the set of values in the form
   *                          (value1,value2,...).
   * @throws ServletException
   */
  public String getInParameter(String parameter, String defaultValue) throws ServletException {
    return getInParameter(parameter, false, defaultValue);
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as 
   * passed to the servlet by the HTTP POST method.
   * String returned is in the form (value1,value2,...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * as they are, no quotation marks.
   * If there is no such parameter for this entry, an empty string is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @return                  String containing the set of values in the form
   *                          (value1,value2,...).
   * @throws ServletException
   */
  public String getInParameter(String parameter) throws ServletException {
    return getInParameter(parameter, false, "");
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as 
   * passed to the servlet by the HTTP POST method.
   * String returned is in the form (value1,value2,...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * as they are, no quotation marks.
   * If there is no such parameter for this entry an exception is thrown.
   * 
   * @param parameter         Name of the parameter to be retrieved.        
   * @return                  String containing the set of values in the form
   *                          (value1,value2,...).
   * @throws ServletException
   */
  public String getRequiredInParameter(String parameter) throws ServletException {
    return getInParameter(parameter, true, "");
  }

  /**
   * Retrieves the set of values for the parameter with the specified name as 
   * passed to the servlet by the HTTP POST method.
   * String returned is in the form (value1,value2,...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * as they are, no quotation marks.
   * If there is no such parameter for this entry, then if it is required, an
   * exception is thrown, or if not the defaultValue is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @param required          If true, an exception is thrown if the parameter 
   *                          is not among the data submitted to the servlet.
   * @param defaultValue      If not required, this is the value that 
   *                          will be returned in case the parameter is not 
   *                          found among the data submitted to the servlet.
   * @return                  String containing the set of values in the form
   *                          (value1,value2,...).
   * @throws ServletException
   */
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
    
    auxStr = FormatUtilities.sanitizeInput(auxStr);
    
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

  /**
   * Retrieves the set of string values for the parameter with the specified 
   * name as passed to the servlet by the HTTP POST method.
   * String returned is in the form ('value1', 'value2',...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * within single quotation marks.
   * If there is no such parameter for this entry, the defaultValue is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @param defaultValue      In case the parameter is not found among the data 
   *                          submitted to the servlet, this value is returned
   * @return                  String containing the set of values in the form
   *                          ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getInStringParameter(String parameter, String defaultValue) throws ServletException {
    return getInStringParameter(parameter, false, defaultValue);
  }

  /**
   * Retrieves the set of string values for the parameter with the specified 
   * name as passed to the servlet by the HTTP POST method.
   * String returned is in the form ('value1', 'value2',...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * within single quotation marks.
   * If there is no such parameter for this entry, an empty string is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @return                  String containing the set of values in the form
   *                          ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getInStringParameter(String parameter) throws ServletException {
    return getInStringParameter(parameter, false, "");
  }

  /**
   * Retrieves the set of string values for the parameter with the specified 
   * name as passed to the servlet by the HTTP POST method.
   * String returned is in the form ('value1', 'value2',...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * within single quotation marks.
   * If there is no such parameter for this entry, an exception is thrown.
   * 
   * @param parameter         Name of the parameter to be retrieved.         
   * @return                  String containing the set of values in the form
   *                          ('value1', 'value2',...).
   * @throws ServletException
   */
  public String getRequiredInStringParameter(String parameter) throws ServletException {
    return getInStringParameter(parameter, true, "");
  }

  /**
   * Retrieves the set of string values for the parameter with the specified 
   * name as passed to the servlet by the HTTP POST method.
   * String returned is in the form ('value1', 'value2',...) and can be used 
   * within SQL statements as part of the 'WHERE columnName IN' filter which
   * is the main purpose of this method. Note that the values are specified
   * within single quotation marks.
   * If there is no such parameter for this entry, then if it is required, an
   * exception is thrown, or if not the defaultValue is returned.
   * 
   * @param parameter         Name of the parameter to be retrieved         
   * @param required          If true, an exception is thrown if the parameter 
   *                          is not among the data submitted to the servlet.
   * @param defaultValue      If not required, this is the value that 
   *                          will be returned in case the parameter is not 
   *                          found among the data submitted to the servlet.
   * @return                  String containing the set of values in the form
   *                          ('value1', 'value2',...)
   * @throws ServletException
   */
  public String getInStringParameter(String parameter, boolean required, String defaultValue) throws ServletException {
    String[] auxStr=null;
    StringBuffer strResult = new StringBuffer();
    try {
      if (isMultipart) auxStr = getMultiParameters(parameter);
      else auxStr = httpRequest.getParameterValues(parameter);
    } catch (Exception e){
      if (!(required)) {
        strResult.append(defaultValue);
      }
    }

    if (auxStr==null || auxStr.length==0) {
      if (required) {
        throw new ServletException("Request IN parameter required: " + parameter);
      } else {
        strResult.append(defaultValue);
      }
      return strResult.toString();
    }

    auxStr = FormatUtilities.sanitizeInput(auxStr);
    
    strResult.append("('");
    for (int i=0;i<auxStr.length;i++) {
      if (i>0) {
        strResult.append("', '");
      }
      strResult.append(auxStr[i]);
    }
    strResult.append("')");

    if (log4j.isDebugEnabled()) log4j.debug("Request IN parameter: " + parameter + ":..." + strResult.toString());

    return strResult.toString();
  }

  /**
   * Retrieve a value specified by the sessionAttribute parameter from the
   * session variables. If not found, an empty string is returned.
   * 
   * @param sessionAttribute The name of the session variable to be retrieved.
   * @return                 String with the value of the session variable.
   */
  public String getSessionValue(String sessionAttribute) {
    return getSessionValue(sessionAttribute, "");
  }

  /**
   * Retrieve a value specified by the sessionAttribute parameter from the
   * session variables. If not found, the defaultValue is returned.
   * 
   * @param sessionAttribute The name of the session variable to be retrieved.
   * @param defaultValue     The value to be returned in case the session 
   *                         variable does not exist.
   * @return                 String with the value of the session variable.
   */
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

  /**
   * Store a variable and its value into the session as specified by the 
   * parameters. 
   * 
   * @param attribute The name of the session variable to be set.
   * @param value     The value of the session variable to set.
   */
  public void setSessionValue(String attribute, String value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
      if (!attribute.equalsIgnoreCase("menuVertical")) if (log4j.isDebugEnabled()) log4j.debug("Set session attribute: " + attribute + ":..." + value.toString());
    }
    catch (Exception e) {
      log4j.error("setSessionValue error: " + attribute + ":..." + value);
    }
  }

  /**
   * Remove a variable and its value from the session.
   *  
   * @param attribute The name of the session variable to remove 
   */
  public void removeSessionValue(String attribute) {
    try {
      if (log4j.isDebugEnabled()) log4j.debug("Remove session attribute: " + attribute + ":..." + getSessionValue(attribute));
      session.removeAttribute(attribute.toUpperCase());

    }
    catch (Exception e) {
      log4j.error("removeSessionValue error: " + attribute);
    }
  }

  /**
   * Retrieve an object from the session.
   * 
   * @param sessionAttribute The name of the object to retrieve.
   * @return                 Object containing the requested object.
   */
  public Object getSessionObject(String sessionAttribute) {
    Object auxStr = null;
    try {
      auxStr = (Object) session.getAttribute(sessionAttribute.toUpperCase());
    } catch (Exception e) {
      auxStr = null;
    }
    return auxStr;
  }

  /**
   * Save an object and its value to the session.
   * 
   * @param attribute The name of the object to be stored.
   * @param value     The value of the object to store.
   */
  public void setSessionObject(String attribute, Object value) {
    try {
      session.setAttribute(attribute.toUpperCase(), value);
    } catch (Exception e) {
      log4j.error("setSessionObject error: " + attribute + ":..." + e);
    }
  }
  
  /**
   * Clear session variables.
   * 
   * @param all If false, one session variable named TARGET is kept
   *        otherwise all session variables are erased.
   */
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

  /**
   * Convert a list specified as a comma separated list within parentheses
   * into a Vector of individual values. Strips parentheses and single
   * quotes. For example, a string like ('1000022', '1000344') becomes a 
   * Vector with two values: 1000022 and 1000344.
   * 
   * @param strList String representing a comma separated list of values.
   * @return        A Vector of individual values.
   */
  public Vector<String> getListFromInString(String strList) {
    Vector<String> fields = new Vector<String>();
    if (strList==null || strList.length()==0) return fields;
    strList = strList.trim();
    if (strList.equals("")) return fields;
    if (strList.startsWith("(")) strList = strList.substring(1, strList.length()-1);
    strList = strList.trim();
    if (strList.equals("")) return fields;
    StringTokenizer datos = new StringTokenizer(strList, ",", false);
    while (datos.hasMoreTokens()) {
      String token = datos.nextToken();
      if (token.startsWith("'")) token = token.substring(1, token.length()-1);
      token = token.trim();
      if (!token.equals("")) fields.addElement(token);
    }
    return fields;
  }

  /**
   * Retrieve a parameter passed to the servlet as part of a multi part content.
   * 
   * @param parameter the name of the parameter to be retrieved
   * @return          String containing the value of the parameter. Empty 
   *                  string if the content is not multipart or the parameter
   *                  is not found.
   */
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

  /**
   * Retrieve a set of values belonging to a parameter passed to the servlet 
   * as part of a multi part content.
   * 
   * @param parameter The name of the parameter to be retrieved.
   * @return          String array containing the values of the parameter. Empty 
   *                  string if the content is not multipart.
   */
  public String[] getMultiParameters(String parameter) {
    if (!isMultipart || items==null) return null;
    Iterator<FileItem> iter = items.iterator();
    Vector<String> result = new Vector<String>();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && item.getFieldName().equals(parameter)) {
        try {
          result.addElement(item.getString("UTF-8"));
        } catch (Exception ex) {}
      }
    }
    String[] strResult = new String[result.size()];
    result.copyInto(strResult);
    return strResult;
  }

  /**
   * Retrieve a deserialized file passed to the servlet as a parameter 
   * and part of a multi part content.
   * 
   * @param parameter The name of the parameter that contains the file
   * @return          FileItem object containing the file content 
   */
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
