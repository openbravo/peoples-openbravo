/*
 ************************************************************************************
 * Copyright (C) 2015-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.stock;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.retail.posterminal.JSONProcessSimple;

public class StoreDetailedStock extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws JSONException, ServletException {
    String prodId;
    OBContext.setAdminMode(true);
    JSONArray responseArray = new JSONArray();
    BigDecimal totalQtyCounter = BigDecimal.ZERO;
    try {

      final boolean isCrossStore = jsonData.has("crossOrganization")
          && !jsonData.isNull("crossOrganization") && !StringUtils
              .equals(jsonData.optString("crossOrganization"), jsonData.getString("organization"));
      final String orgId = isCrossStore ? jsonData.getString("crossOrganization")
          : jsonData.getString("organization");
      prodId = jsonData.getString("product");

      String hqlQuery = " select wh.id, wh.name, sb.id, sb.searchKey, sum(ms.quantityOnHand - ms.reservedQty) as qtyonhand "
          + " from MaterialMgmtStorageDetail ms join ms.storageBin sb "
          + " join sb.inventoryStatus ls join sb.warehouse wh "
          + " join wh.organizationWarehouseList ow where ow.organization.id = :orgId "
          + " and ms.product.id = :prodId and ms.quantityOnHand - ms.reservedQty <> 0";
      if (isCrossStore) {
        hqlQuery += " and ls.oBRETCOAvailableCrossStore = true";
      } else {
        hqlQuery += " and ls.available = true";
      }
      hqlQuery += " and wh.active = true";
      hqlQuery += " group by wh.id, wh.name, sb.id, sb.searchKey";
      hqlQuery += " order by wh.name";

      final Session session = OBDal.getInstance().getSession();
      final Query<Object[]> query = session.createQuery(hqlQuery, Object[].class);
      query.setParameter("orgId", orgId);
      query.setParameter("prodId", prodId);

      ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
      boolean resultsAvailable = false;

      String curWHId = null;
      BigDecimal qtyCounterPerWH = BigDecimal.ZERO;
      String curWHName = "";
      JSONObject warehouseInfo = new JSONObject();
      JSONArray arrBinInfo = new JSONArray();
      try {
        while (results.next()) {
          if (curWHId == null) {
            resultsAvailable = true;
            curWHName = (String) results.get(1);
            curWHId = (String) results.get(0);
            qtyCounterPerWH = BigDecimal.ZERO;
          }
          if (!curWHId.equals(results.get(0))) {
            warehouseInfo.put("warehouseid", curWHId);
            warehouseInfo.put("warehousename", curWHName);
            warehouseInfo.put("warehouseqty", qtyCounterPerWH);
            warehouseInfo.put("bins", arrBinInfo);
            responseArray.put(warehouseInfo);

            warehouseInfo = new JSONObject();
            arrBinInfo = new JSONArray();

            curWHName = (String) results.get(1);
            curWHId = (String) results.get(0);
            qtyCounterPerWH = BigDecimal.ZERO;
          }
          JSONObject binInfo = new JSONObject();
          binInfo.put("binid", results.get(2));
          binInfo.put("binname", results.get(3));
          binInfo.put("binqty", ((BigDecimal) results.get(4)).toString());

          arrBinInfo.put(binInfo);

          qtyCounterPerWH = qtyCounterPerWH.add((BigDecimal) results.get(4));
          totalQtyCounter = totalQtyCounter.add((BigDecimal) results.get(4));
        }
      } finally {
        results.close();
      }

      if (resultsAvailable) {
        warehouseInfo.put("warehouseid", curWHId);
        warehouseInfo.put("warehousename", curWHName);
        warehouseInfo.put("warehouseqty", qtyCounterPerWH);
        warehouseInfo.put("bins", arrBinInfo);
        responseArray.put(warehouseInfo);
      }

    } catch (Exception e) {
      throw new OBException();
    } finally {
      OBContext.restorePreviousMode();
    }

    JSONObject preFinalResult = new JSONObject();
    preFinalResult.put("product", prodId);
    preFinalResult.put("qty", totalQtyCounter);
    preFinalResult.put("warehouses", responseArray);

    JSONObject finalResult = new JSONObject();
    finalResult.put("data", preFinalResult);
    finalResult.put("status", 0);
    return finalResult;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }
}
