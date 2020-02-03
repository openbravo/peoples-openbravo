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
 * All portions are Copyright (C) 2014-2020 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.costing;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.InventoryCountProcess;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.cost.InvAmtUpdLnInventories;
import org.openbravo.model.materialmgmt.cost.InventoryAmountUpdate;
import org.openbravo.model.materialmgmt.cost.InventoryAmountUpdateLine;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.service.db.DbUtility;

public class InventoryAmountUpdateProcess extends BaseActionHandler {
  private static final Logger log = LogManager.getLogger();

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject result = new JSONObject();
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode(true);

    try {
      final JSONObject jsonData = new JSONObject(data);

      String orgId = jsonData.getString("inpadOrgId");
      String invAmtUpdId = jsonData.getString("M_Ca_Inventoryamt_ID");
      InventoryAmountUpdate invAmtUpd = OBDal.getInstance()
          .get(InventoryAmountUpdate.class, invAmtUpdId);
      final OBCriteria<InventoryAmountUpdateLine> qLines = OBDal.getInstance()
          .createCriteria(InventoryAmountUpdateLine.class);
      qLines.add(Restrictions.eq(InventoryAmountUpdateLine.PROPERTY_CAINVENTORYAMT, invAmtUpd));

      ScrollableResults scrollLines = qLines.scroll(ScrollMode.FORWARD_ONLY);
      try {
        int cnt = 0;
        while (scrollLines.next()) {
          final InventoryAmountUpdateLine line = (InventoryAmountUpdateLine) scrollLines.get()[0];
          String lineId = line.getId();
          CostingRule rule = CostingUtils.getCostDimensionRule(
              OBDal.getInstance().get(Organization.class, orgId), line.getReferenceDate());
          String ruleId = rule.getId();
          OrganizationStructureProvider osp = OBContext.getOBContext()
              .getOrganizationStructureProvider(rule.getClient().getId());
          final Set<String> childOrgs = osp.getChildTree(rule.getOrganization().getId(), true);
          if (!rule.isWarehouseDimension()) {
            createInventories(lineId, null, ruleId, childOrgs, line.getReferenceDate());
          } else {
            createInventories(lineId, line.getWarehouse(), ruleId, childOrgs,
                line.getReferenceDate());
          }

          if ((cnt++ % 10) == 0) {
            OBDal.getInstance().flush();
            // clear session after each line iteration because the number of objects read in memory
            // is big
            OBDal.getInstance().getSession().clear();
          }
        }
        invAmtUpd = OBDal.getInstance().get(InventoryAmountUpdate.class, invAmtUpdId);
        invAmtUpd.setProcessed(true);
        OBDal.getInstance().save(invAmtUpd);
        OBDal.getInstance().flush();

        try {
          // to ensure that the closed inventory is created before opening inventory
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          log.error("Error waiting between processing close an open inventories", e);
        }

        //@formatter:off
        final String hql =
                " as inv" +
                " where exists (" +
                "      select 1 from InventoryAmountUpdateLineInventories invAmtUpd" +
                "       where invAmtUpd.caInventoryamtline.caInventoryamt.id =:invAmtUpdId" +
                "         and invAmtUpd.initInventory = inv" +
                "      )";
        //@formatter:on

        ScrollableResults invLines = OBDal.getInstance()
            .createQuery(InventoryCount.class, hql)
            .setNamedParameter("invAmtUpdId", invAmtUpdId)
            .scroll(ScrollMode.FORWARD_ONLY);
        try {
          while (invLines.next()) {
            final InventoryCount inventory = (InventoryCount) invLines.get()[0];
            new InventoryCountProcess().processInventory(inventory, false, true);
          }
        } finally {
          invLines.close();
        }

      } finally {
        scrollLines.close();
      }

      errorMessage.put("severity", "success");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      result.put("message", errorMessage);
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      log.error(e.getMessage(), e);
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("title", OBMessageUtils.messageBD("Error"));
        errorMessage.put("text", message);
        result.put("message", errorMessage);
      } catch (Exception ignore) {
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return result;
  }

  protected void createInventories(String lineId, Warehouse warehouse, String ruleId,
      Set<String> childOrgs, Date date) {

    CostingRule costRule = OBDal.getInstance().get(CostingRule.class, ruleId);
    InventoryAmountUpdateLine line = OBDal.getInstance()
        .get(InventoryAmountUpdateLine.class, lineId);
    ScrollableResults stockLines = getStockLines(childOrgs, date, line.getProduct(), warehouse,
        costRule.isBackdatedTransactionsFixed());
    // The key of the Map is the concatenation of orgId and warehouseId
    Map<String, String> inventories = new HashMap<String, String>();
    Map<String, Long> maxLineNumbers = new HashMap<String, Long>();
    InventoryCountLine closingInventoryLine = null;
    InventoryCountLine openInventoryLine = null;
    int i = 1;
    try {
      while (stockLines.next()) {
        Object[] stockLine = stockLines.get();
        String attrSetInsId = (String) stockLine[0];
        String uomId = (String) stockLine[1];
        String orderUOMId = (String) stockLine[2];
        String locatorId = (String) stockLine[3];
        String warehouseId = (String) stockLine[4];
        BigDecimal qty = (BigDecimal) stockLine[5];
        BigDecimal orderQty = (BigDecimal) stockLine[6];
        //
        String invId = inventories.get(warehouseId);
        InvAmtUpdLnInventories inv = null;
        if (invId == null) {
          inv = createInventorieLine(line, warehouseId, date);

          inventories.put(warehouseId, inv.getId());
        } else {
          inv = OBDal.getInstance().get(InvAmtUpdLnInventories.class, invId);
        }
        Long lineNo = (maxLineNumbers.get(inv.getId()) == null ? 0L
            : maxLineNumbers.get(inv.getId())) + 10L;
        maxLineNumbers.put(inv.getId(), lineNo);

        if (BigDecimal.ZERO.compareTo(qty) < 0) {
          // Do not insert negative values in Inventory lines, instead reverse the Quantity Count
          // and the Book Quantity. For example:
          // Instead of CountQty=0 and BookQty=-5 insert CountQty=5 and BookQty=0
          // By doing so the difference between both quantities remains the same and no negative
          // values have been inserted.

          openInventoryLine = insertInventoryLine(inv.getInitInventory(), line.getProduct().getId(),
              attrSetInsId, uomId, orderUOMId, locatorId, qty, BigDecimal.ZERO, orderQty,
              BigDecimal.ZERO, lineNo, null, line.getUnitCost());
          insertInventoryLine(inv.getCloseInventory(), line.getProduct().getId(), attrSetInsId,
              uomId, orderUOMId, locatorId, BigDecimal.ZERO, qty, BigDecimal.ZERO, orderQty, lineNo,
              openInventoryLine, null);

        } else {
          openInventoryLine = insertInventoryLine(inv.getInitInventory(), line.getProduct().getId(),
              attrSetInsId, uomId, orderUOMId, locatorId, BigDecimal.ZERO, qty.negate(),
              BigDecimal.ZERO, orderQty == null ? null : orderQty, lineNo, closingInventoryLine,
              line.getUnitCost());
          insertInventoryLine(inv.getCloseInventory(), line.getProduct().getId(), attrSetInsId,
              uomId, orderUOMId, locatorId, qty == null ? null : qty.negate(), BigDecimal.ZERO,
              orderQty == null ? null : orderQty, BigDecimal.ZERO, lineNo, openInventoryLine, null);

        }

        if ((i % 100) == 0) {
          OBDal.getInstance().flush();
          OBDal.getInstance().getSession().clear();
          // Reload line after clear session.
          line = OBDal.getInstance().get(InventoryAmountUpdateLine.class, lineId);
        }
        i++;
      }
    } finally {
      stockLines.close();
    }
    // Process closing physical inventories.
    for (InvAmtUpdLnInventories inv : line.getInventoryAmountUpdateLineInventoriesList()) {
      new InventoryCountProcess().processInventory(inv.getCloseInventory(), false);
    }
  }

  private ScrollableResults getStockLines(Set<String> childOrgs, Date date, Product product,
      Warehouse warehouse, boolean backdatedTransactionsFixed) {
    Date localDate = date;
    //@formatter:off
    String hqlSelect =
            "select trx.attributeSetValue.id" +
            "  , trx.uOM.id" +
            "  , trx.orderUOM.id" +
            "  , trx.storageBin.id" +
            "  , loc.warehouse.id" +
            "  , sum(trx.movementQuantity)" +
            "  , sum(trx.orderQuantity)" +
            " from MaterialMgmtMaterialTransaction as trx" +
            "    join trx.storageBin as loc" +
            " where trx.organization.id in (:orgs)";
    //@formatter:on
    if (localDate != null) {
      if (backdatedTransactionsFixed) {
        //@formatter:off
        hqlSelect +=
            "   and trx.movementDate <= :date";
        //@formatter:on
      } else {
        //@formatter:off
        String hqlSubSelect =
                "select min(trx.transactionProcessDate)" +
                " from MaterialMgmtMaterialTransaction as trx";
        //@formatter:on
        if (warehouse != null) {
          //@formatter:off
          hqlSubSelect +=
                "   join trx.storageBin as locator";
          //@formatter:on
        }
        //@formatter:off
        hqlSubSelect +=
                " where trx.product.id = :product" +
                "   and trx.movementDate > :date" +
        // Include only transactions that have its cost calculated
                "   and trx.isCostCalculated = true";
        //@formatter:on
        if (warehouse != null) {
          //@formatter:off
          hqlSubSelect +=
                "   and locator.warehouse.id = :warehouse";
          //@formatter:on
        }
        //@formatter:off
        hqlSubSelect +=
                "   and trx.organization.id in (:orgs)";
        //@formatter:on

        Query<Date> trxsubQry = OBDal.getInstance()
            .getSession()
            .createQuery(hqlSubSelect, Date.class)
            .setParameter("date", localDate)
            .setParameter("product", product.getId());
        if (warehouse != null) {
          trxsubQry.setParameter("warehouse", warehouse.getId());
        }
        Date trxprocessDate = trxsubQry.setParameterList("orgs", childOrgs).uniqueResult();
        if (trxprocessDate != null) {
          localDate = trxprocessDate;
          //@formatter:off
          hqlSelect +=
                "   and trx.transactionProcessDate < :date";
          //@formatter:on
        } else {
          //@formatter:off
          hqlSelect +=
                "   and trx.movementDate <= :date";
          //@formatter:on
        }
      }
    }
    if (warehouse != null) {
      //@formatter:off
      hqlSelect +=
                "   and loc.warehouse = :warehouse";
      //@formatter:on
    }
    //@formatter:off
    hqlSelect +=
            "   and trx.product = :product" +
            " group by trx.attributeSetValue.id" +
            "   , trx.uOM.id" +
            "   , trx.orderUOM.id" +
            "   , trx.storageBin.id" +
            "   , loc.warehouse.id" +
            "   having sum(trx.movementQuantity) <> 0" +
            " order by loc.warehouse.id" +
            "   , trx.storageBin.id" +
            "   , trx.attributeSetValue.id" +
            "   , trx.uOM.id" +
            "   , trx.orderUOM.id";
    //@formatter:on

    Query<Object[]> stockLinesQry = OBDal.getInstance()
        .getSession()
        .createQuery(hqlSelect, Object[].class)
        .setParameterList("orgs", childOrgs);
    if (localDate != null) {
      stockLinesQry.setParameter("date", localDate);
    }
    if (warehouse != null) {
      stockLinesQry.setParameter("warehouse", warehouse);
    }
    return stockLinesQry.setParameter("product", product)
        .setFetchSize(1000)
        .scroll(ScrollMode.FORWARD_ONLY);
  }

  private InvAmtUpdLnInventories createInventorieLine(InventoryAmountUpdateLine invLine,
      String warehouseId, Date date) {
    Date localDate = date;
    if (localDate == null) {
      localDate = new Date();
    }
    Client client = (Client) OBDal.getInstance()
        .getProxy(Client.ENTITY_NAME, invLine.getClient().getId());
    String orgId = invLine.getOrganization().getId();
    Warehouse warehouse = (Warehouse) OBDal.getInstance()
        .getProxy(Warehouse.ENTITY_NAME, warehouseId);
    InvAmtUpdLnInventories inv = OBProvider.getInstance().get(InvAmtUpdLnInventories.class);
    inv.setClient(client);
    inv.setOrganization(
        (Organization) OBDal.getInstance().getProxy(Organization.ENTITY_NAME, orgId));
    inv.setWarehouse(warehouse);

    inv.setCaInventoryamtline(invLine);
    List<InvAmtUpdLnInventories> invList = invLine.getInventoryAmountUpdateLineInventoriesList();
    invList.add(inv);
    invLine.setInventoryAmountUpdateLineInventoriesList(invList);

    final InventoryCount closeInv = OBProvider.getInstance().get(InventoryCount.class);
    final Organization invOrg = CostingUtils.getOrganizationForCloseAndOpenInventories(orgId,
        warehouse);
    closeInv.setClient(client);
    closeInv.setName(OBMessageUtils.messageBD("InvAmtUpdCloseInventory"));
    closeInv.setWarehouse(warehouse);
    closeInv.setOrganization(invOrg);
    closeInv.setMovementDate(localDate);
    closeInv.setInventoryType("C");
    inv.setCloseInventory(closeInv);

    final InventoryCount initInv = OBProvider.getInstance().get(InventoryCount.class);
    initInv.setClient(client);
    initInv.setName(OBMessageUtils.messageBD("InvAmtUpdInitInventory"));
    initInv.setWarehouse(warehouse);
    initInv.setOrganization(invOrg);
    initInv.setMovementDate(localDate);
    initInv.setInventoryType("O");
    inv.setInitInventory(initInv);
    OBDal.getInstance().save(invLine);
    OBDal.getInstance().save(closeInv);
    OBDal.getInstance().save(initInv);

    OBDal.getInstance().flush();

    return inv;
  }

  private InventoryCountLine insertInventoryLine(InventoryCount inventory, String productId,
      String attrSetInsId, String uomId, String orderUOMId, String locatorId, BigDecimal qtyCount,
      BigDecimal qtyBook, BigDecimal orderQtyCount, BigDecimal orderQtyBook, Long lineNo,
      InventoryCountLine relatedInventoryLine, BigDecimal cost) {
    InventoryCountLine icl = OBProvider.getInstance().get(InventoryCountLine.class);
    icl.setClient(inventory.getClient());
    icl.setOrganization(inventory.getOrganization());
    icl.setPhysInventory(inventory);
    icl.setLineNo(lineNo);
    icl.setStorageBin((Locator) OBDal.getInstance().getProxy(Locator.ENTITY_NAME, locatorId));
    icl.setProduct((Product) OBDal.getInstance().getProxy(Product.ENTITY_NAME, productId));
    icl.setAttributeSetValue((AttributeSetInstance) OBDal.getInstance()
        .getProxy(AttributeSetInstance.ENTITY_NAME, attrSetInsId));
    icl.setQuantityCount(qtyCount);
    icl.setBookQuantity(qtyBook);
    icl.setUOM((UOM) OBDal.getInstance().getProxy(UOM.ENTITY_NAME, uomId));
    if (orderUOMId != null) {
      icl.setOrderQuantity(orderQtyCount);
      icl.setQuantityOrderBook(orderQtyBook);
      icl.setOrderUOM(
          (ProductUOM) OBDal.getInstance().getProxy(ProductUOM.ENTITY_NAME, orderUOMId));
    }
    icl.setRelatedInventory(relatedInventoryLine);
    if (cost != null) {
      icl.setCost(cost);
    }
    List<InventoryCountLine> invLines = inventory.getMaterialMgmtInventoryCountLineList();
    invLines.add(icl);
    inventory.setMaterialMgmtInventoryCountLineList(invLines);
    OBDal.getInstance().save(inventory);
    OBDal.getInstance().flush();
    return icl;
  }
}
