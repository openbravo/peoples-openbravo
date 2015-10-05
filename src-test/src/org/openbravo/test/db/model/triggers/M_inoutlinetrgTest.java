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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.db.model.triggers;

import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Query;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.utility.Sequence;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.plm.ProductUOM;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.test.base.OBBaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M_inoutlinetrgTest extends OBBaseTest {

  private static Logger log = LoggerFactory.getLogger(M_inoutlinetrgTest.class);
  // User Openbravo
  private static String USER_ID = "100";

  // Role QA Testing Admin
  private static String ROLE_ID = "4028E6C72959682B01295A071429011E";

  // QA Testing Client
  private static final String ClientId = "4028E6C72959682B01295A070852010D";

  // Spain Organization
  private static final String OrganizationId = "357947E87C284935AD1D783CF6F099A1";

  // MM Shipment Document Type, Document Sequence
  private static final String Shipment_DocumentTypeId = "FF8080812C2ABFC6012C2B3BDF4A004E";
  private static final String Shipment_DocSequenceId = "FF8080812C2ABFC6012C2B3BDF4A004D";

  // Business Partner: Customer A
  private static final String CustomerId = "4028E6C72959682B01295F40C3CB02EC";
  private static final String CustomerAddressId = "4028E6C72959682B01295F40C43802EE";

  // Warehouse: Spain Warehouse
  private static String WarehouseId = "4028E6C72959682B01295ECFEF4502A0";

  // Locator: spain111
  private static String LocatorId = "4028E6C72959682B01295ECFEF6502A3";

  // Movement Quantity: 10
  private static BigDecimal MovementQty = new BigDecimal(10);

  // Product: Distribution good A
  private static String ProductId = "4028E6C72959682B01295ADC211E0237";
  private BigDecimal afterValue = BigDecimal.ZERO;
  private BigDecimal beforeValue = BigDecimal.ZERO;
  private Date afterUpdatedDateTime = new Date();
  private Date beforeUpdatedDateTime = new Date();

  @Test
  public void testM_InOutLineTrg() throws SQLException {
    StorageDetail beforeUpdate = null;
    StorageDetail afterUpdate = null;
    Date afterUpdatedDate = new Date();
    Date beforeUpdatedDate = new Date();

    ShipmentInOut inOut = insertMInOut();
    assertTrue("M_Inout header not inserted successfully ", inOut != null);

    // Case 1: Check whether update inventory called on M_Inoutline insertion with
    // 1. Product is not null
    // 2. Locator is not null
    // 3. MovementQty is not zero
    // 4. AttributeSetInstance is null
    // 5. ProductUom is null

    Product product = OBDal.getInstance().get(Product.class, ProductId);
    Locator locator = OBDal.getInstance().get(Locator.class, LocatorId);

    // Get Storage Detail for Product before inserting line in M_InOutLine

    beforeUpdate = getStorageDetail(product, product.getUOM(), null, locator, null);
    OBDal.getInstance().refresh(beforeUpdate);
    beforeValue = beforeUpdate.getQuantityInDraftTransactions();
    beforeUpdatedDate = beforeUpdate.getUpdated();

    log.info("*************** Case I *****************");
    log.info("Qty in Draft transaction before insertion: " + beforeValue);

    ShipmentInOutLine inOutLine = insertMInOutLine(inOut);
    assertTrue(inOutLine != null);

    afterUpdate = getStorageDetail(product, inOutLine.getUOM(), inOutLine.getAttributeSetValue(),
        inOutLine.getStorageBin(), inOutLine.getOrderUOM());
    OBDal.getInstance().refresh(afterUpdate);

    afterValue = afterUpdate.getQuantityInDraftTransactions();
    afterUpdatedDate = afterUpdate.getUpdated();
    log.info("Qty in Draft transaction after insertion " + afterValue);
    log.info("Updated DateTime before Case I " + beforeUpdatedDate);
    log.info("Updated DateTime after Case I " + afterUpdatedDate);

    assertTrue("Update inventory is not called on Case I", afterValue.compareTo(beforeValue) != 0);
    assertTrue("Update inventory is not called on Case I",
        !beforeUpdatedDate.equals(afterUpdatedDate));

    beforeUpdate = afterUpdate;
    OBDal.getInstance().refresh(beforeUpdate);
    beforeValue = beforeUpdate.getQuantityInDraftTransactions();
    beforeUpdatedDate = beforeUpdate.getUpdated();
    log.info("*************** Case II *****************");
    log.info("Qty in Draft transaction before setting product as null in m_inoutline "
        + beforeValue);

    // Case 2 : Set Product null
    inOutLine = updateMInOutLine(inOutLine, 1, null, null, null);
    assertTrue(inOutLine.getProduct() == null);

    afterUpdate = getStorageDetail(product, inOutLine.getUOM(), inOutLine.getAttributeSetValue(),
        inOutLine.getStorageBin(), inOutLine.getOrderUOM());
    OBDal.getInstance().refresh(afterUpdate);

    afterValue = afterUpdate.getQuantityInDraftTransactions();
    afterUpdatedDate = afterUpdate.getUpdated();
    log.info("Qty in Draft transaction after setting product as null in m_inoutline " + afterValue);
    log.info("Updated DateTime before Case II " + beforeUpdatedDate);
    log.info("Updated DateTime after Case II " + afterUpdatedDate);

    assertTrue("Update inventory is not called on Case II", afterValue.compareTo(beforeValue) != 0);
    assertTrue("Update inventory is not called on Case II",
        !beforeUpdatedDate.equals(afterUpdatedDate));

    beforeUpdate = afterUpdate;
    OBDal.getInstance().refresh(beforeUpdate);

    // Case 3 : Set blank product in InOutLine with Product: Distribution good A

    beforeValue = beforeUpdate.getQuantityInDraftTransactions();
    beforeUpdatedDate = beforeUpdate.getUpdated();
    log.info("*************** Case III *****************");
    log.info("Qty in Draft transaction before setting null product with value in m_inoutline "
        + beforeValue);

    inOutLine = updateMInOutLine(inOutLine, 1, product, null, null);

    afterUpdate = getStorageDetail(product, inOutLine.getUOM(), inOutLine.getAttributeSetValue(),
        inOutLine.getStorageBin(), inOutLine.getOrderUOM());
    OBDal.getInstance().refresh(afterUpdate);

    afterValue = afterUpdate.getQuantityInDraftTransactions();
    afterUpdatedDate = afterUpdate.getUpdated();

    log.info("Qty in Draft transaction after setting null product with value in m_inoutline "
        + afterValue);

    log.info("Updated DateTime before Case III " + beforeUpdatedDate);
    log.info("Updated DateTime after Case III " + afterUpdatedDate);

    assertTrue("Update inventory is not called on Case III", afterValue.compareTo(beforeValue) != 0);
    assertTrue("Update inventory is not called on Case III",
        !beforeUpdatedDate.equals(afterUpdatedDate));

    beforeUpdate = afterUpdate;
    OBDal.getInstance().refresh(beforeUpdate);

    // Case 4 : Update description for the m_inoutline and check that storage details is not updated

    beforeValue = beforeUpdate.getQuantityInDraftTransactions();
    beforeUpdatedDate = beforeUpdate.getUpdated();
    log.info("*************** Case IV *****************");

    inOutLine = updateMInOutLine(inOutLine, 4, product, null, "description updated for this line");

    afterUpdate = getStorageDetail(product, inOutLine.getUOM(), inOutLine.getAttributeSetValue(),
        inOutLine.getStorageBin(), inOutLine.getOrderUOM());
    OBDal.getInstance().refresh(afterUpdate);

    afterValue = afterUpdate.getQuantityInDraftTransactions();
    afterUpdatedDate = afterUpdate.getUpdated();

    log.info("Qty in Draft transaction before updating description in m_inoutline " + beforeValue);
    log.info("Qty in Draft transaction after updating description in m_inoutline " + afterValue);
    log.info("Updated DateTime before updating description in m_inoutline " + beforeUpdatedDate);
    log.info("Updated DateTime after updating description in m_inoutline " + afterUpdatedDate);

    assertTrue("Update inventory is called on Case IV, should not be",
        afterValue.compareTo(beforeValue) == 0);
    assertTrue("Update inventory is called on Case IV, should not be",
        afterUpdatedDate.equals(beforeUpdatedDate));

    // Case V: Delete a M_InoutLine

    beforeUpdate = afterUpdate;
    OBDal.getInstance().refresh(beforeUpdate);
    beforeValue = beforeUpdate.getQuantityInDraftTransactions();
    beforeUpdatedDate = beforeUpdate.getUpdated();

    log.info("*************** Case V *****************");
    log.info("Qty in Draft transaction before deletion: " + beforeValue);
    log.info("Updated DateTime before deletion: " + beforeUpdatedDate);

    inOut = deleteMInOutLine(inOutLine);
    assertTrue(inOut.getMaterialMgmtShipmentInOutLineList().isEmpty());

    afterUpdate = getStorageDetail(product, product.getUOM(), null, locator, null);
    OBDal.getInstance().refresh(beforeUpdate);
    afterValue = afterUpdate.getQuantityInDraftTransactions();
    afterUpdatedDate = afterUpdate.getUpdated();

    log.info("Qty in Draft transaction after deletion: " + afterValue);
    log.info("Updated DateTime after deletion: " + afterUpdatedDate);

    assertTrue("Update inventory is not called on Case V", afterValue.compareTo(beforeValue) != 0);
    assertTrue("Update inventory is not called on Case V",
        !beforeUpdatedDate.equals(afterUpdatedDate));

  }

  private ShipmentInOut insertMInOut() throws SQLException {
    try {
      OBContext.setAdminMode(true);
      // Set QA context
      OBContext.setOBContext(USER_ID, ROLE_ID, ClientId, OrganizationId);
      ShipmentInOut shipmentInOut = OBProvider.getInstance().get(ShipmentInOut.class);
      BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, CustomerId);
      Location bpLocation = OBDal.getInstance().get(Location.class, CustomerAddressId);
      DocumentType doctype = OBDal.getInstance().get(DocumentType.class, Shipment_DocumentTypeId);
      Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, WarehouseId);
      shipmentInOut.setBusinessPartner(bpartner);
      shipmentInOut.setDocumentType(doctype);
      shipmentInOut.setPartnerAddress(bpLocation);
      shipmentInOut.setDocumentNo(getDocumentNo(Shipment_DocSequenceId));
      shipmentInOut.setMovementDate(new Date());
      shipmentInOut.setAccountingDate(new Date());
      shipmentInOut.setWarehouse(warehouse);

      OBDal.getInstance().save(shipmentInOut);
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().refresh(shipmentInOut);

      return shipmentInOut;
    } finally {
      OBContext.restorePreviousMode();
    }

  }

  private ShipmentInOutLine insertMInOutLine(ShipmentInOut shipmentInOut) throws SQLException {
    try {
      OBContext.setAdminMode(true);
      // Set QA context
      OBContext.setOBContext(USER_ID, ROLE_ID, ClientId, OrganizationId);
      ShipmentInOutLine shipmentInOutLine = OBProvider.getInstance().get(ShipmentInOutLine.class);
      Product product = OBDal.getInstance().get(Product.class, ProductId);
      Locator locator = OBDal.getInstance().get(Locator.class, LocatorId);
      shipmentInOutLine.setShipmentReceipt(shipmentInOut);
      shipmentInOutLine.setLineNo(10L);
      shipmentInOutLine.setProduct(product);
      shipmentInOutLine.setMovementQuantity(MovementQty);
      shipmentInOutLine.setStorageBin(locator);
      shipmentInOutLine.setUOM(product.getUOM());
      OBDal.getInstance().save(shipmentInOutLine);
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().refresh(shipmentInOutLine);

      return shipmentInOutLine;

    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ShipmentInOutLine updateMInOutLine(ShipmentInOutLine shipmentInOutLine, int i,
      Product product, BigDecimal movementQty, String strDescription) throws SQLException {
    ShipmentInOutLine InOutLine = shipmentInOutLine;
    switch (i) {

    case 1:
      InOutLine.setProduct(product);
      OBDal.getInstance().save(InOutLine);
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().refresh(InOutLine);
      break;

    case 2:
      InOutLine.setMovementQuantity(movementQty);
      OBDal.getInstance().save(InOutLine);
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().refresh(InOutLine);
      break;

    case 4:
      InOutLine.setDescription(strDescription);
      OBDal.getInstance().save(InOutLine);
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().refresh(InOutLine);
      break;

    default:
    }

    return InOutLine;
  }

  private ShipmentInOut deleteMInOutLine(ShipmentInOutLine shipmentInOutLine) throws SQLException {
    ShipmentInOut shipmentInOut = shipmentInOutLine.getShipmentReceipt();
    OBDal.getInstance().remove(shipmentInOutLine);
    OBDal.getInstance().save(shipmentInOut);
    OBDal.getInstance().getConnection().commit();
    OBDal.getInstance().refresh(shipmentInOut);
    return shipmentInOut;

  }

  // Calculates the next document number for this sequence
  private String getDocumentNo(String sequenceId) {
    try {
      Sequence sequence = OBDal.getInstance().get(Sequence.class, sequenceId);
      String prefix = sequence.getPrefix() == null ? "" : sequence.getPrefix();
      String suffix = sequence.getSuffix() == null ? "" : sequence.getSuffix();
      String documentNo = prefix + sequence.getNextAssignedNumber().toString() + suffix;
      sequence.setNextAssignedNumber(sequence.getNextAssignedNumber() + sequence.getIncrementBy());
      return documentNo;
    } catch (Exception e) {
      throw new OBException(e);
    }
  }

  private StorageDetail getStorageDetail(Product product, UOM uom,
      AttributeSetInstance attributeSetInstance, Locator locator, ProductUOM productUom) {
    StorageDetail storageDetail = null;
    String hqlString = " select sd from  MaterialMgmtStorageDetail sd " + " where sd.product.id ='"
        + product.getId() + "' and sd.storageBin.id = '" + locator.getId() + "'"
        + " and sd.uOM.id = '" + uom.getId() + "'";
    if (attributeSetInstance != null) {
      hqlString = hqlString + " and sd.attributeSetValue.id = '" + attributeSetInstance.getId()
          + "'";
    } else {
      hqlString = hqlString + " and sd.attributeSetValue.id = '0'";
    }
    if (productUom != null) {
      hqlString = hqlString + " and sd.orderUOM.id = '" + productUom.getId() + "'";
    } else {
      hqlString = hqlString + " and sd.orderUOM.id is null ";
    }
    Query query = OBDal.getInstance().getSession().createQuery(hqlString);
    query.uniqueResult();
    if (!query.list().isEmpty()) {
      storageDetail = (StorageDetail) query.list().get(0);
    }
    return storageDetail;
  }
}
