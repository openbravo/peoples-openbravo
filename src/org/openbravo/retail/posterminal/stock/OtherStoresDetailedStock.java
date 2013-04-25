package org.openbravo.retail.posterminal.stock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import org.openbravo.retail.posterminal.JSONProcessSimple;

public class OtherStoresDetailedStock extends JSONProcessSimple {
  @Override
  public JSONObject exec(JSONObject jsonData) throws JSONException, ServletException {
    String orgId, prodId;
    OBContext.setAdminMode(true);
    JSONArray responseArray = new JSONArray();
    BigDecimal totalQtyCounter = BigDecimal.ZERO;
    List<String> countedWarehouses = new ArrayList<String>();
    try {

      orgId = jsonData.getString("organization");
      prodId = jsonData.getString("product");

      String hqlQuery = "select ow.organization.id, ow.organization.name, ow.warehouse.id, ow.warehouse.name, sum(ms.quantityOnHand) as qtyonhand "
          + "from OrganizationWarehouse as ow, MaterialMgmtStorageDetail as ms "
          + "where ow.organization.id <> ' "
          + orgId
          + "' and "
          + "ow.warehouse.id = ms.storageBin.warehouse.id and "
          + "ms.storageBin.warehouse in ( "
          + "select oww.warehouse.id from OrganizationWarehouse as oww where oww.organization.id <> '"
          + orgId
          + "' "
          + ") and "
          + "ms.product.id = '"
          + prodId
          + "' "
          + "group by ow.organization.id, ow.organization.name, ow.warehouse.id, ow.warehouse.name "
          + "order by ow.organization.id ";

      final Session session = OBDal.getInstance().getSession();
      final Query query = session.createQuery(hqlQuery);

      ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
      boolean resultsAvailable = false;

      String curOrgId = null;
      BigDecimal qtyCounterPerOrg = BigDecimal.ZERO;
      String curOrgName = "";
      JSONObject orgInfo = new JSONObject();
      JSONArray arrWarehousesInfo = new JSONArray();

      while (results.next()) {
        if (curOrgId == null) {
          resultsAvailable = true;
          curOrgName = (String) results.get(1);
          curOrgId = (String) results.get(0);
          qtyCounterPerOrg = BigDecimal.ZERO;
        }
        if (!curOrgId.equals((String) results.get(0))) {
          orgInfo.put("organizationid", curOrgId);
          orgInfo.put("organizationname", curOrgName);
          orgInfo.put("organizationqty", qtyCounterPerOrg);
          orgInfo.put("warehouses", arrWarehousesInfo);
          responseArray.put(orgInfo);

          orgInfo = new JSONObject();
          arrWarehousesInfo = new JSONArray();

          curOrgName = (String) results.get(1);
          curOrgId = (String) results.get(0);
          qtyCounterPerOrg = BigDecimal.ZERO;
        }
        JSONObject warehouseInfo = new JSONObject();
        warehouseInfo.put("warehouseid", (String) results.get(2));
        if (!countedWarehouses.contains((String) results.get(2))) {
          countedWarehouses.add((String) results.get(2));
          totalQtyCounter = totalQtyCounter.add((BigDecimal) results.get(4));
        }
        warehouseInfo.put("warehousename", (String) results.get(3));
        warehouseInfo.put("warehouseqty", ((BigDecimal) results.get(4)).toString());

        arrWarehousesInfo.put(warehouseInfo);

        qtyCounterPerOrg = qtyCounterPerOrg.add((BigDecimal) results.get(4));
      }

      if (resultsAvailable) {
        orgInfo.put("organizationid", curOrgId);
        orgInfo.put("organizationname", curOrgName);
        orgInfo.put("organizationqty", qtyCounterPerOrg.toString());
        orgInfo.put("warehouses", arrWarehousesInfo);
        responseArray.put(orgInfo);
      }

    } catch (Exception e) {
      throw new OBException();
    } finally {
      OBContext.restorePreviousMode();
    }

    JSONObject preFinalResult = new JSONObject();
    preFinalResult.put("product", prodId);
    preFinalResult.put("qty", totalQtyCounter);
    preFinalResult.put("organizations", responseArray);

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