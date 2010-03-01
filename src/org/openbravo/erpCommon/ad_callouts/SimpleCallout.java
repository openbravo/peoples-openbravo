/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SL
 * All portions are Copyright (C) 2010 Openbravo SL
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public abstract class SimpleCallout extends HttpSecureAppServlet {

    private static final long serialVersionUID = 1L;

    private StringBuffer result;
    private int rescounter;
    private int selectcounter;

    protected VariablesSecureApp vars;


    protected abstract void execute() throws ServletException;

    protected String getLastFieldChanged() {
      return vars.getStringParameter("inpLastFieldChanged");
    }

    protected String getTabId() {
        return vars.getStringParameter("inpTabId");
    }

    protected String getStringParameter(String param) {
        return vars.getStringParameter(param);
    }

    protected BigDecimal getBigDecimalParameter(String param) throws ServletException {
        return new BigDecimal(vars.getNumericParameter(param, "0"));
    }

    protected void addSelect(String param) {

        if (rescounter > 0) {
            result.append(',');
        }
        rescounter++;
        result.append("\nnew Array(\"");
        result.append(param);
        result.append("\", ");

        selectcounter = 0;
    }

    protected void addSelectResult(String name, String value) {
        addSelectResult(name, value, false);
    }

    protected void addSelectResult(String name, String value, boolean selected) {

        if (selectcounter > 0) {
            result.append(',');
        }
        selectcounter++;
        result.append("new Array(\"");
        result.append(name);
        result.append("\", \"");
        result.append(FormatUtilities.replaceJS(value));
        result.append("\",");
        result.append(selected ? "true" : "false");
        result.append(")");
    }

    protected void endSelect() {
        if (selectcounter == 0) {
            result.append("null");
        }
        result.append(")");
    }

    protected void addResult(String param, Object value) {

        if (rescounter > 0) {
            result.append(',');
        }
        rescounter++;

        result.append("\nnew Array(\"");
        result.append(param);
        result.append("\", ");
        result.append(value == null ? "null" : value.toString());
        result.append(")");
    }

    protected void addResult(String param, String value) {
        addResult(param, (Object) value == null ? null : "\"" + FormatUtilities.replaceJS(value) + "\"");
    }

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        vars = new VariablesSecureApp(request);
        result = new StringBuffer();

        if (vars.commandIn("DEFAULT")) {
            try {
                printPage(response);
            } catch (ServletException ex) {
                pageErrorCallOut(response);
            }
        } else {
            pageError(response);
        }

        result = null;
        vars = null;
    }

    private void printPage(HttpServletResponse response) throws IOException, ServletException {

        if (log4j.isDebugEnabled()) {
            log4j.debug("Output: dataSheet");
        }

        XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
                "org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

        result.append("var calloutName='");
        result.append(getSimpleClassName());
        result.append("';\nvar respuesta = new Array(");

        rescounter = 0;
        execute();

        result.append(");");
        xmlDocument.setParameter("array", result.toString());
        xmlDocument.setParameter("frameName", "appFrame");
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println(xmlDocument.print());
        out.close();
    }

    protected String getSimpleClassName() {
        String classname = getClass().getName();
        int i = classname.lastIndexOf(".");
        if (i < 0) {
            return classname;
        } else {
            return classname.substring(i + 1);
        }
    }
}
