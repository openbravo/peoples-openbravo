/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.materialmgmt;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.DalConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductPriceUtils {

  private static final Logger log = LoggerFactory.getLogger(ProductPriceUtils.class);

  /**
   * Method that returns a warning message if a service of a Return From Customer is not Returnable
   * of the return period is expired.
   */

  public static JSONObject productReturnAllowedRFC(ShipmentInOutLine shipmentLine,
      Product serviceProduct, Date rfcOrderDate) {
    JSONObject result = null;
    OBContext.setAdminMode(true);
    try {
      if (!serviceProduct.isReturnable()) {
        throw new OBException("@Product@ '" + serviceProduct.getIdentifier()
            + "' @ServiceIsNotReturnable@");
      } else {
        try {
          final Date orderDate = shipmentLine != null && shipmentLine.getSalesOrderLine() != null ? OBDateUtils
              .getDate(OBDateUtils.formatDate(shipmentLine.getSalesOrderLine().getOrderDate()))
              : null;
          Date returnDate = null;
          String message = null;
          if (orderDate != null && serviceProduct.getOverdueReturnDays() != null) {
            returnDate = DateUtils.addDays(orderDate, serviceProduct.getOverdueReturnDays()
                .intValue());
          }
          if (serviceProduct.getOverdueReturnDays() != null && returnDate != null
              && rfcOrderDate.after(returnDate)) {
            message = "@Product@ '" + serviceProduct.getIdentifier() + "' @ServiceReturnExpired@: "
                + OBDateUtils.formatDate(returnDate);
          }
          if (serviceProduct.getOverdueReturnDays() != null && returnDate == null) {
            message = "@Product@ '" + serviceProduct.getIdentifier()
                + "' @ServiceMissingReturnDate@";
          }
          if (message != null) {
            message = OBMessageUtils.parseTranslation(new DalConnectionProvider(false),
                RequestContext.get().getVariablesSecureApp(), OBContext.getOBContext()
                    .getLanguage().getLanguage(), message);
            result = new JSONObject();
            result.put("severity", "warning");
            result.put("title", "Warning");
            result.put("text", message);
          }
        } catch (ParseException e) {
          log.error(e.getMessage(), e);
        } catch (JSONException e) {
          log.error(e.getMessage(), e);
        }
      }
      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
