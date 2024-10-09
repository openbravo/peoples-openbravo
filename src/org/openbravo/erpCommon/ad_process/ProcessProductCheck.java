/*
 ************************************************************************************
 * Copyright (C) 2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.erpCommon.ad_process;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.UOMUtil;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.ProductCheck;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.warehouse.advancedwarehouseoperations.utils.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessProductCheck extends DalBaseProcess {
  private static final Logger log4j = LoggerFactory.getLogger(ProcessProductCheck.class);

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    final OBError msg = new OBError();
    try {
      OBContext.setAdminMode(false);
      final String prodCheckId = (String) bundle.getParams().get("M_Product_Check_ID");
      final ProductCheck retProdCheck = OBDal.getInstance().get(ProductCheck.class, prodCheckId);

      // Goods movement header
      final InternalMovement header = OBProvider.getInstance().get(InternalMovement.class);
      header.setClient(retProdCheck.getClient());
      header.setOrganization(retProdCheck.getOrganization());
      header.setMovementDate(DateUtils.truncate(new Date(), Calendar.DATE));
      header.setName(generateInternalMovementName(retProdCheck));
      OBDal.getInstance().save(header);

      // movement line
      final InternalMovementLine line = OBProvider.getInstance().get(InternalMovementLine.class);
      line.setClient(retProdCheck.getClient());
      line.setOrganization(retProdCheck.getOrganization());
      line.setLineNo(10L);
      line.setProduct(retProdCheck.getProduct());
      line.setMovementQuantity(retProdCheck.getQtyreturned().negate());
      line.setUOM(retProdCheck.getProduct().getUOM());
      if (UOMUtil.isUomManagementEnabled()) {
        line.setOperativeQuantity(retProdCheck.getQtyreturned().negate());
        line.setAlternativeUOM(retProdCheck.getProduct().getUOM());
      }
      line.setAttributeSetValue(retProdCheck.getProduct().getAttributeSetValue());
      line.setStorageBin(retProdCheck.getStorageBin());
      line.setNewStorageBin(retProdCheck.getNewStorageBin());
      line.setMovement(header);
      OBDal.getInstance().save(line);

      header.getMaterialMgmtInternalMovementLineList().add(line);
      OBDal.getInstance().save(header);
      retProdCheck.setProcessed(true);
      OBDal.getInstance().save(retProdCheck);

      Utilities.processGoodsMovement(header.getId());

      msg.setType("Success");
      msg.setTitle(OBMessageUtils.messageBD("Success"));

    } catch (Exception e) {
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD("Error"));
      msg.setMessage(e.getMessage());
      log4j.error("Error Processing Returned Product Check", e);
    }
    bundle.setResult(msg);
  }

  private String generateInternalMovementName(final ProductCheck retProdCheck) {
    final Date today = new Date();
    final Product product = retProdCheck.getProduct();
    final Locator confirmedLocatorFrom = retProdCheck.getStorageBin();
    final Locator confirmedLocatorTo = retProdCheck.getNewStorageBin();

    return StringUtils.left(OBDateUtils.formatDate(today) + "_" + product.getSearchKey() + "_"
        + (confirmedLocatorFrom != null ? confirmedLocatorFrom.getIdentifier() : "")
        + (confirmedLocatorTo != null ? "_" + confirmedLocatorTo.getIdentifier() : ""), 60);
  }
}
