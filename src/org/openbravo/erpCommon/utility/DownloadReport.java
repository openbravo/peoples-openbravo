package org.openbravo.erpCommon.utility;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FileUtility;

public class DownloadReport extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String report = vars.getStringParameter("report");
    downloadReport(vars, response, report);
  }

  private void downloadReport(VariablesSecureApp vars, HttpServletResponse response, String report)
      throws IOException, ServletException {
    FileUtility f = new FileUtility(globalParameters.strFTPDirectory, report, false, true);
    if (!f.exists())
      return;
    int pos = report.indexOf("-");
    String filename = report.substring(0, pos);
    pos = report.lastIndexOf(".");
    String extension = report.substring(pos);
    response.setContentType("application/x-download");
    response.setHeader("Content-Disposition", "attachment; filename=" + filename + extension);
    f.dumpFile(response.getOutputStream());
    response.getOutputStream().flush();
    response.getOutputStream().close();
    if (!f.deleteFile())
      log4j.error("Download report could not delete the file :" + report);
  }
}
