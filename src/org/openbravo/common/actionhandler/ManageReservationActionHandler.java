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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.common.actionhandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationManualPickEdit;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.service.db.DbUtility;

/**
 * 
 * @author gorkaion
 * 
 */
public class ManageReservationActionHandler extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(ManageReservationActionHandler.class);
  final String strOrderLineTableId = "260";
  final String strReservationsTableId = "77264B07BB0E4FA483A07FB40C2E0FE0";
  final String strResStockTableId = "D6079A4A6C2542678D9A50114367B967";

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      final String strTableId = jsonRequest.getString("inpTableId");
      boolean processReservation = false;

      Reservation reservation = null;
      if (strTableId.equals(strReservationsTableId) || strTableId.equals(strResStockTableId)) {
        final String strReservationId = jsonRequest.getString("M_Reservation_ID");
        reservation = OBDal.getInstance().get(Reservation.class, strReservationId);
      } else if (strTableId.equals(strOrderLineTableId)) {
        final String strOrderLineId = jsonRequest.getString("C_OrderLine_ID");
        final OrderLine sol = OBDal.getInstance().get(OrderLine.class, strOrderLineId);
        reservation = ReservationUtils.getReservationFromOrder(sol);

        processReservation = reservation.getRESStatus().equals("DR");
      }
      if (reservation != null) {
        // FIXME: Replace with OBDao method when handler is merged with latest pi.
        // List<String> idList = OBDao.getIDListFromOBObject(reservation
        // .getMaterialMgmtReservationStockList());
        List<String> idList = new ArrayList<String>();
        for (ReservationStock resStock : reservation.getMaterialMgmtReservationStockList()) {
          idList.add(resStock.getId());
        }
        manageReservedStockLines(jsonRequest, reservation, idList);
      }
      if (processReservation) {
        ReservationUtils.processReserve(reservation, "PR");
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void manageReservedStockLines(JSONObject jsonRequest, Reservation reservation,
      List<String> idList) throws JSONException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      removeNonSelectedLines(idList, reservation);
      return;
    }
    for (long i = 0; i < selectedLines.length(); i++) {
      JSONObject selectedLine = selectedLines.getJSONObject((int) i);
      log.debug(selectedLine);
      ReservationStock resStock = null;
      String strReservationStockId = selectedLine.get(
          ReservationManualPickEdit.PROPERTY_RESERVATIONSTOCK).equals(null) ? "" : selectedLine
          .getString(ReservationManualPickEdit.PROPERTY_RESERVATIONSTOCK);
      boolean existsReservationStock = StringUtils.isNotBlank(strReservationStockId);
      if (existsReservationStock) {
        resStock = OBDal.getInstance().get(ReservationStock.class, strReservationStockId);
        idList.remove(strReservationStockId);
      } else {
        resStock = OBProvider.getInstance().get(ReservationStock.class);
      }
      resStock.setReservation(reservation);
      resStock.setOrganization(reservation.getOrganization());

      final String strLocator = selectedLine.get(ReservationManualPickEdit.PROPERTY_STORAGEBIN)
          .equals(null) ? "" : selectedLine
          .getString(ReservationManualPickEdit.PROPERTY_STORAGEBIN);
      if (StringUtils.isNotBlank(strLocator)) {
        resStock.setStorageBin((Locator) OBDal.getInstance().getProxy(Locator.ENTITY_NAME,
            strLocator));
      }
      final String strASIId = selectedLine
          .get(ReservationManualPickEdit.PROPERTY_ATTRIBUTESETVALUE).equals(null) ? ""
          : selectedLine.getString(ReservationManualPickEdit.PROPERTY_ATTRIBUTESETVALUE);
      if (StringUtils.isNotBlank(strASIId)) {
        resStock.setAttributeSetValue((AttributeSetInstance) OBDal.getInstance().getProxy(
            AttributeSetInstance.ENTITY_NAME, strASIId));
      }
      final String strOrderLineId = selectedLine.get(
          ReservationManualPickEdit.PROPERTY_PURCHASEORDERLINE).equals(null) ? "" : selectedLine
          .getString(ReservationManualPickEdit.PROPERTY_PURCHASEORDERLINE);
      if (StringUtils.isNotBlank(strOrderLineId)) {
        resStock.setSalesOrderLine((OrderLine) OBDal.getInstance().getProxy(OrderLine.ENTITY_NAME,
            strOrderLineId));
      }

      final Boolean isAllocated = selectedLine.getString(
          ReservationManualPickEdit.PROPERTY_ALLOCATED).equals(null) ? false : selectedLine
          .getBoolean(ReservationManualPickEdit.PROPERTY_ALLOCATED);
      resStock.setAllocated(isAllocated == true);

      final BigDecimal qty = new BigDecimal(
          selectedLine.getString(ReservationManualPickEdit.PROPERTY_QUANTITY));
      resStock.setQuantity(qty);

      if (!existsReservationStock) {
        List<ReservationStock> resStocks = reservation.getMaterialMgmtReservationStockList();
        resStocks.add(resStock);
        reservation.setMaterialMgmtReservationStockList(resStocks);
      }

      OBDal.getInstance().save(resStock);
      OBDal.getInstance().save(reservation);
      OBDal.getInstance().flush();
    }

    removeNonSelectedLines(idList, reservation);
  }

  private void removeNonSelectedLines(List<String> idList, Reservation reservation) {
    if (idList.size() > 0) {
      for (String id : idList) {
        ReservationStock resStock = OBDal.getInstance().get(ReservationStock.class, id);
        reservation.getMaterialMgmtReservationStockList().remove(resStock);
        OBDal.getInstance().remove(resStock);
      }
      OBDal.getInstance().save(reservation);
      OBDal.getInstance().flush();
    }
  }
}
