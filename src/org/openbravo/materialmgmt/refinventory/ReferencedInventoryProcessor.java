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
 * All portions are Copyright (C) 2017 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.materialmgmt.refinventory;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.ReferencedInventory;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.service.db.CallProcess;

/**
 * Generates a goods movement to change the referenced inventory of the selected storage details.
 *
 */

// TODO hooks

abstract class ReferencedInventoryProcessor {
  private static final String M_MOVEMENT_POST_ID = "122";
  private static final String JS_STORAGEDETAIL_ID = "id";
  private static final String QUANTITY = "quantityOnHand";

  private JSONArray selectedStorageDetails;

  /**
   * The created goods movement header will belong to the returned organization
   */
  protected abstract Organization getGoodsMovementHeaderOrganization();

  /**
   * The returned ReferencedInventory will be associated to the given storage detail
   */
  protected abstract ReferencedInventory getReferencedInventory(final StorageDetail storageDetail);

  /**
   * The returned ReferencedInventory will be associated to the given storage detail
   */
  protected abstract AttributeSetInstance getAttributeSetInstanceTo(
      final StorageDetail storageDetail);

  /**
   * Returns a string with the name for the generated goods movement
   */
  protected abstract String generateInternalMovementName();

  /**
   * Returns the expected goods movement line bin to
   */
  protected abstract String getNewStorageBinId(final JSONObject storageDetailJS);

  protected void setSelectedStorageDetailsAndValidateThem(final JSONArray selectedStorageDetails)
      throws JSONException {
    this.selectedStorageDetails = selectedStorageDetails;
    checkThereAreStorageDetailsToProcessOrThrowException();
    checkValidQuantitiesOrThrowException();
  }

  private void checkThereAreStorageDetailsToProcessOrThrowException() {
    if (selectedStorageDetails == null || selectedStorageDetails.length() == 0) {
      throw new OBException(OBMessageUtils.messageBD("NotSelected"));
    }
  }

  private void checkValidQuantitiesOrThrowException() throws JSONException {
    for (int i = 0; i < selectedStorageDetails.length(); i++) {
      final JSONObject storageDetailJS = selectedStorageDetails.getJSONObject(i);
      final StorageDetail storageDetail = getStorageDetail(storageDetailJS);
      final BigDecimal qtySelected = getSelectedQty(storageDetailJS);
      checkIsPositiveQty(storageDetail, qtySelected);
      checkQtyIsLowerOrEqualThanQtyOnHand(storageDetail, qtySelected);
    }
  }

  private void checkIsPositiveQty(final StorageDetail storageDetail, final BigDecimal qtySelected) {
    if (qtySelected.compareTo(BigDecimal.ZERO) <= 0) {
      throw new OBException(String.format(OBMessageUtils.messageBD("RefInv_NegativeQty"),
          storageDetail.getIdentifier()));
    }
  }

  private void checkQtyIsLowerOrEqualThanQtyOnHand(final StorageDetail storageDetail,
      final BigDecimal qtySelected) {
    final BigDecimal qtyOnHand = storageDetail.getQuantityOnHand();
    if (qtySelected.compareTo(qtyOnHand) > 0) {
      throw new OBException(String.format(
          OBMessageUtils.messageBD("RefInv_QtyGreaterThanOnHandQty"),
          FIN_Utility.formatNumber(qtySelected, "qty", "Relation"),
          FIN_Utility.formatNumber(qtyOnHand, "qty", "Relation"), storageDetail.getIdentifier()));
    }
  }

  /**
   * Creates, process and returns a goods movement with the referenced inventory change.
   * 
   * @throws Exception
   *           In case of exception, the transaction is rollback and the exception is thrown.
   * 
   */
  public InternalMovement createAndProcessGoodsMovement() throws Exception {
    try {
      OBContext.setAdminMode(true);
      final InternalMovement goodsMovementHeader = createAndSaveGoodsMovementHeader();
      createAndSaveGoodsMovementLines(goodsMovementHeader);
      processGoodsMovement(goodsMovementHeader.getId());
      OBDal.getInstance().flush(); // Flush in admin mode
      return goodsMovementHeader;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw e;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private InternalMovement createAndSaveGoodsMovementHeader() {
    final InternalMovement header = OBProvider.getInstance().get(InternalMovement.class);
    header.setClient(OBContext.getOBContext().getCurrentClient());
    header.setOrganization(getGoodsMovementHeaderOrganization());
    header.setName(_generateInternalMovementName());
    header.setMovementDate(new Date());
    OBDal.getInstance().save(header);
    return header;
  }

  private String _generateInternalMovementName() {
    return StringUtils.left(generateInternalMovementName(), 60);
  }

  private void createAndSaveGoodsMovementLines(final InternalMovement goodsMovementHeader)
      throws JSONException {
    long lineNo = 10l;
    for (int i = 0; i < selectedStorageDetails.length(); i++) {
      final JSONObject storageDetailJS = selectedStorageDetails.getJSONObject(i);
      createAndSaveGoodsMovementLine(goodsMovementHeader, storageDetailJS, lineNo);
      lineNo = lineNo + 10l;
    }
  }

  private InternalMovementLine createAndSaveGoodsMovementLine(
      final InternalMovement internalMovement, final JSONObject storageDetailJS, final long lineNo)
      throws JSONException {
    final StorageDetail storageDetail = getStorageDetail(storageDetailJS);
    final BigDecimal movementQty = getSelectedQty(storageDetailJS);

    final InternalMovementLine line = OBProvider.getInstance().get(InternalMovementLine.class);
    line.setClient(internalMovement.getClient());
    line.setOrganization(storageDetail.getOrganization());
    line.setLineNo(lineNo);
    line.setProduct(storageDetail.getProduct());
    line.setMovementQuantity(movementQty);
    line.setUOM(storageDetail.getProduct().getUOM());
    line.setAttributeSetValue(storageDetail.getAttributeSetValue());
    line.setStorageBin(storageDetail.getStorageBin());
    line.setNewStorageBin(getProposedNewBinOrStorageDetailCurrentBin(storageDetailJS, storageDetail));
    line.setMovement(internalMovement);
    line.setAttributeSetInstanceTo(getAttributeSetInstanceTo(storageDetail));
    internalMovement.getMaterialMgmtInternalMovementLineList().add(line);
    OBDal.getInstance().save(line);
    return line;
  }

  private Locator getProposedNewBinOrStorageDetailCurrentBin(final JSONObject storageDetailJS,
      final StorageDetail storageDetail) {
    final String locatorId = getNewStorageBinId(storageDetailJS);
    if (StringUtils.isBlank(locatorId) || StringUtils.equals("null", locatorId)) {
      return storageDetail.getStorageBin();
    }

    return OBDal.getInstance().getProxy(Locator.class, locatorId);
  }

  protected StorageDetail getStorageDetail(final JSONObject storageDetailJS) throws JSONException {
    final StorageDetail storageDetail = OBDal.getInstance().get(StorageDetail.class,
        getStorageDetailId(storageDetailJS));
    return storageDetail;
  }

  private String getStorageDetailId(JSONObject jsStorageDetail) throws JSONException {
    return jsStorageDetail.getString(JS_STORAGEDETAIL_ID);
  }

  private BigDecimal getSelectedQty(final JSONObject storageDetailJS) throws JSONException {
    final BigDecimal selectedQty = new BigDecimal(storageDetailJS.getString(QUANTITY));
    return selectedQty;
  }

  /**
   * Process a Goods Movement (Internal Movement).
   * 
   * Throws OBException if not possible to process
   */
  private void processGoodsMovement(final String goodsMovementId) {
    final Process process = OBDal.getInstance().get(Process.class, M_MOVEMENT_POST_ID);
    final ProcessInstance pinstance = CallProcess.getInstance()
        .call(process, goodsMovementId, null);
    final OBError result = OBMessageUtils.getProcessInstanceMessage(pinstance);
    if (StringUtils.equals("Error", result.getType())) {
      throw new OBException(OBMessageUtils.messageBD("ErrorProcessingGoodMovement") + ": "
          + result.getMessage());
    }
  }
}
