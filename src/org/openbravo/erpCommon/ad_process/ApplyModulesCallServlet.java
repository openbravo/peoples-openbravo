package org.openbravo.erpCommon.ad_process;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.HttpBaseServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.system.ReloadContext;
import org.openbravo.service.system.RestartTomcat;
import org.openbravo.xmlEngine.XmlDocument;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

public class ApplyModulesCallServlet extends HttpBaseServlet {
  private static final long serialVersionUID = 1L;

  /**
     * 
     */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("TOMCAT")) {
      printPageTomcat(request, response, vars);
    } else if (vars.commandIn("UPDATESTATUS")) {
      update(response, vars);
    } else if (vars.commandIn("REQUESTERRORSTATE")) {
      requesterrorstate(response, vars);
    } else if (vars.commandIn("GETERR")) {
      getError(response, vars);
    } else if (vars.commandIn("RESTART")) {
      restartApplicationServer(response, vars);
    } else if (vars.commandIn("RELOAD")) {
      reloadContext(response, vars);
    }
  }

  /**
   * Prints a page that only allows the user to restart or reload Tomcat
   */
  private void printPageTomcat(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars) throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartTomcat").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Reloads the application context to after building the application to apply modules.
   * 
   * @param response
   *          the HttpServletResponse to write to
   * @param vars
   *          the application variables
   * @throws IOException
   * @throws ServletException
   */
  private void reloadContext(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartingContext").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String message = Utility.messageBD(this, "CONTEXT_RELOAD", vars.getLanguage());
    xmlDocument.setParameter("message", Utility.formatMessageBDToHtml(message));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    response.flushBuffer();

    ReloadContext.reload();
  }

  /**
   * Restarts the application server after building the application to apply modules.
   * 
   * @param response
   *          the HttpServletResponse to write to
   * @param vars
   *          the application variables
   * @throws IOException
   * @throws ServletException
   */
  private void restartApplicationServer(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException, ServletException {

    final XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_process/RestartingContext").createXmlDocument();
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String message = Utility.messageBD(this, "TOMCAT_RESTART", vars.getLanguage());
    xmlDocument.setParameter("message", Utility.formatMessageBDToHtml(message));

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    response.flushBuffer();

    RestartTomcat.restart();
  }

  /**
   * This method returns an ApplyModulesResponse object, that later is transformed into a JSON
   * object and resend to the rebuild window.
   */
  private ApplyModulesResponse fillResponse(VariablesSecureApp vars, String state,
      String defaultState) {
    String ln = vars.getSessionValue("ApplyModules|Last_Line_Number_Log");
    int lastlinenumber;
    if (ln == null || ln.equals("")) {
      lastlinenumber = 0;
    } else {
      lastlinenumber = Integer.parseInt(ln);
    }
    ApplyModulesResponse resp = new ApplyModulesResponse();
    resp.setState(Integer.parseInt(state.replace("RB", "")));
    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    PreparedStatement ps3 = null;
    boolean warning = false;
    boolean error = false;
    int newlinenumber = lastlinenumber;
    try {
      ps = getPreparedStatement("SELECT MESSAGE, LINE_NUMBER FROM AD_ERROR_LOG WHERE ERROR_LEVEL='WARN' AND SYSTEM_STATUS LIKE ?");
      ps.setString(1, "%" + state);
      ps.executeQuery();
      ResultSet rs = ps.getResultSet();
      ArrayList<String> warnings = new ArrayList<String>();
      while (rs.next()) {
        warning = true; // there is at least an warning in this state
        int linenumber = rs.getInt(2);
        if (linenumber > newlinenumber) {
          newlinenumber = linenumber;
        }
        if (linenumber > lastlinenumber) {
          warnings.add(rs.getString(1));
        }
      }
      resp.setWarnings(warnings.toArray(new String[0]));

      ps2 = getPreparedStatement("SELECT MESSAGE, LINE_NUMBER FROM AD_ERROR_LOG WHERE ERROR_LEVEL='ERROR' AND SYSTEM_STATUS LIKE ?");
      ps2.setString(1, "%" + state);
      ps2.executeQuery();
      ResultSet rs2 = ps2.getResultSet();
      ArrayList<String> errors = new ArrayList<String>();
      while (rs2.next()) {
        error = true; // there is at least an error in this state
        int linenumber = rs2.getInt(2);
        if (linenumber > newlinenumber) {
          newlinenumber = linenumber;
        }
        if (linenumber > lastlinenumber) {
          errors.add(rs2.getString(1));
        }
      }
      resp.setErrors(errors.toArray(new String[0]));

      ps3 = getPreparedStatement("SELECT MESSAGE FROM AD_ERROR_LOG ORDER BY CREATED DESC");
      ps3.executeQuery();
      ResultSet rs3 = ps3.getResultSet();
      if (rs3.next()) {
        resp.setLastmessage(rs3.getString(1));
      } else {
        resp.setLastmessage("");
      }

      vars.setSessionValue("ApplyModules|Last_Line_Number_Log", new Integer(newlinenumber)
          .toString());
      if (error)
        resp.setStatusofstate("Error");
      else if (warning)
        resp.setStatusofstate("Warning");
      else
        resp.setStatusofstate(defaultState);
    } catch (Exception e) {
    } finally {
      try {
        releasePreparedStatement(ps3);
        releasePreparedStatement(ps2);
        releasePreparedStatement(ps);
      } catch (SQLException e2) {
      }
    }
    return resp;
  }

  /**
   * This method is called via AJAX through a timer in the rebuild window. It returns the current
   * status of the system (and warnings/errors that happened in the current state)
   */
  private void update(HttpServletResponse response, VariablesSecureApp vars) {
    PreparedStatement ps = null;
    try {
      ps = getPreparedStatement("SELECT SYSTEM_STATUS FROM AD_SYSTEM_INFO");
      ps.executeQuery();
      ResultSet rs = ps.getResultSet();
      rs.next();
      String state = rs.getString(1);
      ApplyModulesResponse resp = fillResponse(vars, state, "Processing");
      response.setContentType("text/plain; charset=UTF-8");
      final PrintWriter out = response.getWriter();
      String strResult;
      XStream xs = new XStream(new JettisonMappedXmlDriver());
      xs.alias("Response", ApplyModulesResponse.class);
      strResult = xs.toXML(resp);
      out.print(strResult);
      out.close();
    } catch (Exception e) {
    } finally {
      if (ps != null)
        try {
          releasePreparedStatement(ps);
        } catch (SQLException e) {
        }
    }
  }

  /**
   * This method is called via AJAX, and returns the status and warnings/errors for a particular
   * state. This method will be called when the Rebuild Window notices that one or more steps were
   * not updated and the build process already finished them
   */
  private void requesterrorstate(HttpServletResponse response, VariablesSecureApp vars) {
    String state = vars.getStringParameter("reqStatus");
    ApplyModulesResponse resp = fillResponse(vars, state, "Success");
    response.setContentType("text/plain; charset=UTF-8");
    try {
      final PrintWriter out = response.getWriter();
      String strResult;
      XStream xs = new XStream(new JettisonMappedXmlDriver());
      xs.alias("Response", ApplyModulesResponse.class);
      strResult = xs.toXML(resp);
      out.print(strResult);
      out.close();
    } catch (IOException e) {
    }
  }

  /**
   * Method to be called via AJAX. It returns a XML structure with the error messages (if any) or a
   * Success one
   */
  private void getError(HttpServletResponse response, VariablesSecureApp vars) throws IOException,
      ServletException {
    OBError error = new OBError();
    PreparedStatement ps;
    PreparedStatement ps2;
    try {
      ps = getPreparedStatement("SELECT MESSAGE FROM AD_ERROR_LOG WHERE ERROR_LEVEL='ERROR'");
      ps.executeQuery();
      ResultSet rs = ps.getResultSet();
      if (rs.next()) {
        error.setType("Error");
        error.setTitle(Utility.messageBD(myPool, "Error", vars.getLanguage()));
        error.setMessage(Utility.messageBD(myPool, "BuildError", vars.getLanguage())
            + "<a href=\"http://wiki.openbravo.com/wiki/ERP/2.50/Update_Tips\" target=\"_blank\">"
            + Utility.messageBD(myPool, "ThisLink", vars.getLanguage()) + "</a>.");

      } else {
        ps2 = getPreparedStatement("SELECT MESSAGE FROM AD_ERROR_LOG WHERE ERROR_LEVEL='WARN'");
        ps2.executeQuery();
        ResultSet rs2 = ps2.getResultSet();
        if (rs2.next()) {
          error.setType("Warning");
          error.setTitle(Utility.messageBD(myPool, "Warning", vars.getLanguage()));
          error
              .setMessage(Utility.messageBD(myPool, "BuildWarning", vars.getLanguage())
                  + "<a href=\"http://wiki.openbravo.com/wiki/ERP/2.50/Update_Tips\" target=\"_blank\">"
                  + Utility.messageBD(myPool, "ThisLink", vars.getLanguage()) + "</a>."
                  + Utility.messageBD(myPool, "BuildWarning2", vars.getLanguage()));

        } else {
          error.setType("Success");
          error.setTitle(Utility.messageBD(myPool, "Success", vars.getLanguage()));
          error.setMessage(Utility.messageBD(myPool, "BuildSuccessful", vars.getLanguage()));
        }
      }
    } catch (Exception e) {
    }

    XStream xs = new XStream(new JettisonMappedXmlDriver());
    xs.alias("OBError", OBError.class);
    String strResult = xs.toXML(error);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.print(strResult);
    out.close();
  }
}
