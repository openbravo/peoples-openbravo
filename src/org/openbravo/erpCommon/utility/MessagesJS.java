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
 * All portions are Copyright (C) 2001-2006 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.utility;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.utils.FormatUtilities;

/**
 * @author Fernando Iriazabal
 * 
 *         Servlet that prints a javascript with the confirmation messages for
 *         the check javascript of the window.
 */
public class MessagesJS extends HttpSecureAppServlet {
    private static final long serialVersionUID = 1L;

    public void init(ServletConfig config) {
        super.init(config);
        boolHist = false;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        VariablesSecureApp vars = new VariablesSecureApp(request);
        printPageDataSheet(response, vars);
    }

    /**
     * Prints the javascript with the messages.
     * 
     * @param response
     *            Handler for the response.
     * @param vars
     *            Handler for the session info.
     * @throws IOException
     * @throws ServletException
     */
    private void printPageDataSheet(HttpServletResponse response,
            VariablesSecureApp vars) throws IOException, ServletException {
        if (log4j.isDebugEnabled())
            log4j.debug("Output: print page");
        OBError myError = null;
        MessagesJSData[] data = null;
        try {
            data = MessagesJSData.select(this, vars.getLanguage());
        } catch (ServletException ex) {
            myError = Utility.translateError(this, vars, vars.getLanguage(), ex
                    .getMessage());
            log4j.error("Error in MessagesJS.printPageDataSheet(): "
                    + myError.getTitle() + " - " + myError.getMessage());
            return;
        }
        StringBuffer arrayType = new StringBuffer();
        StringBuffer array = new StringBuffer();
        arrayType.append("function messageType(_messageID, _messageType) {\n");
        arrayType.append("  this.id = _messageID;\n");
        arrayType.append("  this.type = _messageType;\n");
        arrayType.append("}\n");
        arrayType
                .append("function messagesTexts(_language, _message, _text, _defaultText) {\n");
        arrayType.append("  this.language = _language;\n");
        arrayType.append("  this.message = _message;\n");
        arrayType.append("  this.text = _text;\n");
        arrayType.append("  this.defaultText = _defaultText;\n");
        arrayType.append("}\n");
        arrayType.append("var arrTypes = new Array(\n");
        array.append("var arrMessages = new Array(\n");
        if (data != null && data.length != 0) {
            for (int i = 0; i < data.length; i++) {
                String num = data[i].value.replace("JS", "");
                if (i > 0) {
                    arrayType.append(",\n");
                    array.append(",\n");
                }
                arrayType.append("new messageType(\"").append(num)
                        .append("\",").append(
                                data[i].msgtype.equals("C") ? "1" : "0")
                        .append(")");
                array.append("new messagesTexts(\"").append(vars.getLanguage())
                        .append("\", \"");
                array.append(num).append("\", \"").append(
                        FormatUtilities.replaceJS(data[i].msgtext)).append(
                        "\", null)");
            }

        }
        arrayType.append(");\n");
        array.append(");\n");

        response.setContentType("text/javascript; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        PrintWriter out = response.getWriter();
        if (log4j.isDebugEnabled())
            log4j.debug(arrayType.toString() + array.toString());
        out.println(arrayType.toString() + array.toString());
        out.close();
    }
}
