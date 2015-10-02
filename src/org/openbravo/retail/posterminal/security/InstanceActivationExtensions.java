/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_forms.InstanceManagement;
import org.openbravo.erpCommon.obps.ActivationKey;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.xmlEngine.XmlDocument;

/**
 * Extends Activation Window by adding a "Deactivate all POS Terminals" process that is shown in
 * case of license violation due to exceeded number of Terminals
 * 
 * @author alostale
 *
 */
@SuppressWarnings("serial")
public class InstanceActivationExtensions extends InstanceManagement {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("SHOW_DEACTIVATE_TERMINALS")) {
      printPageDeactivateTerminals(response, vars);
    } else if (vars.commandIn("DEACTIVATE_TERMINALS")) {
      deactivateProcess(vars);
      printPageClosePopUp(response, vars, "../ad_forms/InstanceManagement.html");
    } else {
      super.doPost(request, response);
    }
  }

  private void deactivateProcess(VariablesSecureApp vars) {
    OBError msg = new OBError();
    try {
      ActivationKey ak = ActivationKey.getInstance();
      int deactivatedTerminals = OBDal.getInstance().getSession()
          .createQuery("update OBPOS_Applications set active = false where active = true")
          .executeUpdate();
      log4j.info("Deactivated " + deactivatedTerminals + " POS Terminals");
      msg.setType("Success");
      msg.setTitle(Utility.messageBD(this, "OBPOS_AllTermianlsDeactivatedTitle", vars.getLanguage()));
      msg.setMessage(OBMessageUtils.getI18NMessage("OBPOS_AllTermianlsDeactivatedMsg",
          new String[] { Integer.toString(deactivatedTerminals),
              ak.getAllowedPosTerminals().toString() }));

    } catch (Exception e) {
      log4j.error("Error deactivating POS Terminals", e);
      msg.setType("Error");
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(), e.getMessage()));
    }

    vars.setMessage("InstanceManagement", msg);
  }

  private void printPageDeactivateTerminals(HttpServletResponse response, VariablesSecureApp vars)
      throws IOException {
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/retail/posterminal/security/DeactivateTerminals").createXmlDocument();
    response.setContentType("text/html; charset=UTF-8");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());

    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

}
