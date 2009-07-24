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
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;

/**
 * Provides an api on top of the package private
 * {@link SLOrderProductData#getOffersPriceCurrency(org.openbravo.database.ConnectionProvider, String, String, String, String, String, String, String)}
 * method.
 * 
 * @author mtaal
 */
public class GetPriceOfferInvoice {

  /**
   * @see SLOrderProductData#getOffersPriceCurrency(ConnectionProvider, String, String, String,
   *      String, String, String, String)
   */
  public static String getOffersPriceInvoice(ConnectionProvider connectionProvider,
      String dateordered, String cBpartnerId, String mProductId, String pricestd, String qty,
      String pricelist, String currencyid) throws ServletException {
    return SLOrderProductData.getOffersPriceCurrency(connectionProvider, dateordered, cBpartnerId,
        mProductId, pricestd, qty, pricelist, currencyid);
  }
}
