/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.dal.core.OBContext;

public class InvoiceTerms extends QueryTerminalProperty {

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {
    String hqlInvoiceTerms = "SELECT list.searchKey AS id, ";
    hqlInvoiceTerms += "COALESCE((SELECT trl.name FROM list.aDListTrlList AS trl ";
    hqlInvoiceTerms += "WHERE trl.language = '";
    hqlInvoiceTerms += OBContext.getOBContext().getLanguage().getLanguage();
    hqlInvoiceTerms += "'), list.name) AS name FROM ADList AS list ";
    hqlInvoiceTerms += "WHERE list.reference.id = '150' AND list.$readableSimpleCriteria AND list.$activeCriteria ";
    hqlInvoiceTerms += "ORDER BY list.sequenceNumber, name";
    return Arrays.asList(hqlInvoiceTerms);
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public String getProperty() {
    return "invoiceTerms";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
