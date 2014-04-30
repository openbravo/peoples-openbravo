/*
 ************************************************************************************
 * Copyright (C) 2014 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.advpaymentmngt.filterexpression;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.service.db.DalConnectionProvider;

public class DocumentNoFilterExpression implements FilterExpression {

  private static final Logger log = Logger.getLogger(DocumentNoFilterExpression.class);

  @Override
  public String getExpression(Map<String, String> requestMap) {
    ConnectionProvider conn = new DalConnectionProvider();
    return null;

    // String strPaymentDocumentNo = Utility.getDocumentNo(conn, vars, "AddPaymentFromTransaction",
    // "FIN_Payment", strDocTypeId, strDocTypeId, false, true);

  }

}
