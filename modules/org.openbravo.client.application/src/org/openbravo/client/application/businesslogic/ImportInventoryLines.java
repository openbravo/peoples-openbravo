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
 * All portions are Copyright (C) 2023 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):
 ************************************************************************
 */

package org.openbravo.client.application.businesslogic;

import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.persistence.Tuple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.client.application.attachment.AttachImplementationManager;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;

public class ImportInventoryLines extends ProcessUploadedFile {
  private static final long serialVersionUID = 1L;
  private static final Logger log = LogManager.getLogger();

  @Override
  protected void clearBeforeImport(String ownerId, JSONObject paramValues) {
    @SuppressWarnings("unchecked")
    NativeQuery<String> qry = OBDal.getInstance()
        .getSession()
        .createNativeQuery(
            "update m_inventoryline set updated=now(), updatedby=:userId, isactive='N' where m_inventory_id = :m_inventory_id");
    qry.setParameter("userId", OBContext.getOBContext().getUser().getId());
    qry.setParameter("m_inventory_id", ownerId);
    qry.executeUpdate();

  }

  @Override
  protected UploadResult doProcessFile(JSONObject paramValues, File file) throws Exception {
    final UploadResult uploadResult = new UploadResult();
    final String inventoryId = paramValues.getString("inpOwnerId");
    final String tabId = paramValues.getString("inpTabId");
    final String noStock = paramValues.getString("noStock");

    try (BufferedReader br = Files.newBufferedReader(Paths.get(file.getAbsolutePath()))) {
      String line;
      while ((line = br.readLine()) != null) {

        String separador = getFieldSeparator();
        String[] fields = line.split(separador, -1);
        String upc = fields[0];
        String productSearchkey = fields[1];
        String attributes = fields[2];
        String bookQty = fields[3];
        String qtyCount = fields[4];
        String bin = fields[6];
        String description = fields[7];

        // check if the line already exists
        Product product = getProduct(upc, productSearchkey);
        if (product == null) {
          continue;
        }
        final OBQuery<InventoryCountLine> qry = OBDal.getInstance()
            .createQuery(InventoryCountLine.class,
                "m_inventory_id=:m_inventory_id and m_product_id=:m_product_id and m_locator_id=:m_locator_id "
                    + "and m_attributesetinstance_id=:m_attributesetinstance_id");
        qry.setNamedParameter("m_inventory_id", inventoryId);
        qry.setNamedParameter("m_product_id", product.getId());
        qry.setNamedParameter("m_locator_id", getLocator(bin).getId());
        qry.setNamedParameter("m_attributesetinstance_id",
            getAttributeSetInstance(attributes) != null
                ? getAttributeSetInstance(attributes).getId()
                : "0");
        qry.setFilterOnActive(false);
        List<InventoryCountLine> lines = qry.list();
        InventoryCountLine inventoryLine = null;

        if (lines.size() == 0) {
          // create a new one
          InventoryCount inventoryCount = OBDal.getInstance()
              .get(InventoryCount.class, inventoryId);

          inventoryLine = OBProvider.getInstance().get(InventoryCountLine.class);
          inventoryLine.setClient(inventoryCount.getClient());
          inventoryLine.setOrganization(inventoryCount.getOrganization());
          inventoryLine.setPhysInventory(inventoryCount);
          inventoryLine.setLineNo(getMaxLineNo(inventoryId) + 10);
          inventoryLine.setProduct(product);
          inventoryLine.setAttributeSetValue(
              getAttributeSetInstance(attributes) != null ? getAttributeSetInstance(attributes)
                  : OBDal.getInstance().get(AttributeSetInstance.class, "0"));
          inventoryLine.setBookQuantity(BigDecimal.ZERO);
          inventoryLine.setGapqty(BigDecimal.ZERO);
          inventoryLine.setUOM(product.getUOM());
          inventoryLine.setStorageBin(getLocator(bin));
          inventoryLine.setActive(true);
        } else {
          // get the line from the result
          inventoryLine = lines.get(0);
          inventoryLine
              .setGapqty(new BigDecimal(bookQty).subtract(inventoryLine.getBookQuantity()));
        }
        inventoryLine.setActive(true);
        inventoryLine.setQuantityCount(new BigDecimal(qtyCount));
        inventoryLine.setDescription(description);
        inventoryLine.setCsvimported(true);
        OBDal.getInstance().save(inventoryLine);
        OBDal.getInstance().flush();
        uploadResult.incTotalCount();
      }
    }

    try (ScrollableResults physicalInventorylines = getPhysicalInventoryLines(inventoryId)) {
      inCaseNoStock(noStock, physicalInventorylines);
    }
    InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, inventoryId);

    AttachImplementationManager aim = WeldUtils
        .getInstanceFromStaticBeanManager(AttachImplementationManager.class);
    aim.upload(Collections.emptyMap(), tabId, inventoryId, inventory.getOrganization().getId(),
        file);
    return uploadResult;
  }

  private void inCaseNoStock(String noStock, ScrollableResults physicalInventorylines) {
    while (physicalInventorylines.next()) {
      Tuple physicalInventoryline = (Tuple) physicalInventorylines.get()[0];
      InventoryCountLine inventoryCountLine = OBDal.getInstance()
          .get(InventoryCountLine.class, physicalInventoryline.get("inventoryLineId"));

      if (noStock.equals("deleteLines")) {
        OBDal.getInstance().remove(inventoryCountLine);
      } else if (noStock.equals("quantityCountZero")) {
        inventoryCountLine.setQuantityCount(BigDecimal.ZERO);
      } else if (noStock.equals("quantityCountOriginal")) {
        inventoryCountLine.setQuantityCount(inventoryCountLine.getBookQuantity());
      } else if (noStock.equals("doNotModify")) {
        // Do nothing
      }
    }
  }

  private Long getMaxLineNo(String inventoryId) {
    String hql = "select coalesce(max(il.lineNo), 0) from MaterialMgmtInventoryCountLine as il where il.physInventory.id = :inventoryId";
    Query<Long> query = OBDal.getInstance().getSession().createQuery(hql, Long.class);
    query.setParameter("inventoryId", inventoryId);
    Long maxLineNo = query.uniqueResult();
    if (maxLineNo != null) {
      return maxLineNo;
    }
    return 0L;
  }

  private Product getProduct(String upc, String productSearchkey) {
    final OBQuery<Product> qry = OBDal.getInstance()
        .createQuery(Product.class, "value=:value or upc=:upc");
    qry.setNamedParameter("value", productSearchkey);
    qry.setNamedParameter("upc", upc);
    qry.setMaxResult(1);
    return qry.uniqueResult();
  }

  private Locator getLocator(String bin) {
    final OBQuery<Locator> qry = OBDal.getInstance().createQuery(Locator.class, "value=:value");
    qry.setNamedParameter("value", bin);
    qry.setMaxResult(1);
    return qry.uniqueResult();
  }

  private AttributeSetInstance getAttributeSetInstance(String attributeSetInstance) {
    final OBQuery<AttributeSetInstance> qry = OBDal.getInstance()
        .createQuery(AttributeSetInstance.class, "description=:description");
    qry.setNamedParameter("description", attributeSetInstance);
    qry.setMaxResult(1);
    return qry.uniqueResult();
  }

  private String getFieldSeparator() {
    String fieldSeparator = "";
    try {
      fieldSeparator = Preferences.getPreferenceValue("OBSERDS_CSVFieldSeparator", true,
          OBContext.getOBContext().getCurrentClient(),
          OBContext.getOBContext().getCurrentOrganization(), OBContext.getOBContext().getUser(),
          OBContext.getOBContext().getRole(), null);
    } catch (PropertyNotFoundException e) {
      // There is no preference for the field separator. Using the default one.
      fieldSeparator = ",";
    } catch (PropertyException e1) {
      log.error("Error getting Preference: " + e1.getMessage(), e1);
    }
    return fieldSeparator;
  }

  private static ScrollableResults getPhysicalInventoryLines(String inventoryId) {
    //@formatter:off
    String hql =  " select pil.id as inventoryLineId " +
                  " from MaterialMgmtInventoryCountLine as pil" +
                  " where pil.physInventory.id = :inventoryId " +
                  " and iscsvimported = 'N'" +
                  " order by pil.lineNo, pil.id ";
    //@formatter:on  

    final Query<Tuple> query = OBDal.getInstance().getSession().createQuery(hql, Tuple.class);
    query.setParameter("inventoryId", inventoryId);
    return query.scroll(ScrollMode.FORWARD_ONLY);
  }

}
