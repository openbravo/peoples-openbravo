/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;

public class SE_GLItem_Transaction extends SimpleCallout {

  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    try {
      final String strGLItemId = info.getStringParameter("inpcGlitemId", IsIDFilter.instance);
      final String strTransactionId = info.getStringParameter("Fin_Finacc_Transaction_ID",
          IsIDFilter.instance);
      if ("".equals(strGLItemId)) {
        return;
      }
      GLItem glItem = OBDal.getInstance().get(GLItem.class, strGLItemId);
      FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class,
          strTransactionId);
      GLItem oldGLItem = transaction.getGLItem();
      String description = transaction.getDescription();
      String oldGlItemString = Utility.messageBD(this, "APRM_GLItem", info.vars.getLanguage())
          + ": " + oldGLItem.getName();
      String newGlItemString = Utility.messageBD(this, "APRM_GLItem", info.vars.getLanguage())
          + ": " + glItem.getName();
      if (description != null && !description.isEmpty()) {
        description = description.indexOf(oldGlItemString) != -1 ? (description
            .indexOf(oldGlItemString) == 0 ? "" : description.substring(0,
            description.indexOf(oldGlItemString) - 1)
            + "\n")
            + newGlItemString
            + description.substring(
                oldGlItemString.length() + description.indexOf(oldGlItemString),
                description.length()) : description;
      }
      description = (description == null || description.isEmpty()) ? newGlItemString : description;
      info.addResult("inpdescription", description);
    } catch (Exception e) {
      return;
    }
  }
}
