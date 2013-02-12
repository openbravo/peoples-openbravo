package org.openbravo.retail.posterminal.stock;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.process.JSONProcessSimple;

public class StoreDetailedStock extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws JSONException, ServletException {
    String orgId, prodId;
    OBContext.setAdminMode(true);
    JSONArray responseArray = new JSONArray();
    BigDecimal totalQtyCounter = BigDecimal.ZERO;
    try {

      orgId = jsonData.getString("organization");
      prodId = jsonData.getString("product");

      String hqlQuery = "select ms.storageBin.warehouse.id, ms.storageBin.warehouse.name, ms.storageBin.id, ms.storageBin.searchKey, sum(ms.quantityOnHand) as qtyonhand "
          + "from MaterialMgmtStorageDetail ms "
          + "where ms.storageBin.warehouse.id in ( "
          + "SELECT ow.warehouse.id "
          + "FROM OrganizationWarehouse as ow "
          + "WHERE "
          + "ow.organization.id = '"
          + orgId
          + "') "
          + "and ms.product.id = '"
          + prodId
          + "'"
          + "group by ms.storageBin.warehouse.id, ms.storageBin.warehouse.name, ms.storageBin.warehouse.id, ms.storageBin.id, ms.storageBin.searchKey "
          + "order by ms.storageBin.warehouse.name";

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlQuery);

      ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
      boolean resultsAvailable = false;

      String curWHId = null;
      BigDecimal qtyCounterPerWH = BigDecimal.ZERO;
      String curWHName = "";
      JSONObject warehouseInfo = new JSONObject();
      JSONArray arrBinInfo = new JSONArray();

      while (results.next()) {
        if (curWHId == null) {
          resultsAvailable = true;
          curWHName = (String) results.get(1);
          curWHId = (String) results.get(0);
          qtyCounterPerWH = BigDecimal.ZERO;
        }
        if (!curWHId.equals((String) results.get(0))) {
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
        binInfo.put("binid", (String) results.get(2));
        binInfo.put("binname", (String) results.get(3));
        binInfo.put("binqty", ((BigDecimal) results.get(4)).toString());

        arrBinInfo.put(binInfo);

        qtyCounterPerWH = qtyCounterPerWH.add((BigDecimal) results.get(4));
        totalQtyCounter = totalQtyCounter.add((BigDecimal) results.get(4));
      }

      if (resultsAvailable) {
        warehouseInfo.put("warehouseid", curWHId);
        warehouseInfo.put("warehousename", curWHName);
        warehouseInfo.put("warehouseqty", qtyCounterPerWH.toString());
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
}