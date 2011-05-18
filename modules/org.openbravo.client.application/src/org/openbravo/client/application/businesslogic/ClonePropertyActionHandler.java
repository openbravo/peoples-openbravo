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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):   Sreedhar Sirigiri (TDS), Mallikarjun M (TDS)
 ************************************************************************
 */
package org.openbravo.client.application.businesslogic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.json.DataResolvingMode;
import org.openbravo.service.json.DataToJsonConverter;

/**
 * Clone an existing sales order.
 * 
 * @author Mallikarjun M
 * 
 */
public class ClonePropertyActionHandler extends BaseActionHandler {

  protected JSONObject execute(Map<String, Object> parameters, String data) {
    OBContext.setAdminMode();
    final DataToJsonConverter jsonConverter = new DataToJsonConverter();
    JSONObject json = null;
    try {
      String orderId = (String) parameters.get("orderId");
      Order objOrder = OBDal.getInstance().get(Order.class, orderId);
      Order objCloneOrder = OBProvider.getInstance().get(Order.class);
      objCloneOrder.setNewOBObject(true);
      objCloneOrder.setOrganization(objOrder.getOrganization());
      objCloneOrder.setOrderDate(objOrder.getOrderDate());
      objCloneOrder.setOrderReference(objOrder.getOrderReference());
      objCloneOrder.setDescription(objOrder.getDescription());
      objCloneOrder.setScheduledDeliveryDate(objOrder.getScheduledDeliveryDate());
      objCloneOrder.setBusinessPartner(objOrder.getBusinessPartner());
      objCloneOrder.setPartnerAddress(objOrder.getPartnerAddress());
      objCloneOrder.setUserContact(objOrder.getUserContact());
      objCloneOrder.setSalesRepresentative(objOrder.getSalesRepresentative());
      objCloneOrder.setDeliveryLocation(objOrder.getDeliveryLocation());
      objCloneOrder.setInvoiceTerms(objOrder.getInvoiceTerms());
      objCloneOrder.setWarehouse(objOrder.getWarehouse());
      objCloneOrder.setPriceList(objOrder.getPriceList());
      objCloneOrder.setCurrency(objOrder.getCurrency());
      objCloneOrder.setDeliveryMethod(objOrder.getDeliveryMethod());
      objCloneOrder.setPriority(objOrder.getPriority());
      objCloneOrder.setShippingCompany(objOrder.getShippingCompany());
      objCloneOrder.setFreightAmount(objOrder.getFreightAmount());
      objCloneOrder.setFreightCostRule(objOrder.getFreightCostRule());
      objCloneOrder.setCharge(objOrder.getCharge());
      objCloneOrder.setChargeAmount(objOrder.getChargeAmount());
      objCloneOrder.setPaymentMethod(objOrder.getPaymentMethod());
      objCloneOrder.setPaymentTerms(objOrder.getPaymentTerms());
      objCloneOrder.setActivity(objOrder.getActivity());
      objCloneOrder.setProject(objOrder.getProject());
      objCloneOrder.setSalesCampaign(objOrder.getSalesCampaign());
      objCloneOrder.setFormOfPayment(objOrder.getFormOfPayment());
      objCloneOrder.setTrxOrganization(objOrder.getTrxOrganization());
      objCloneOrder.setUpdatedBy(objOrder.getUpdatedBy());
      objCloneOrder.setCreatedBy(objOrder.getCreatedBy());
      objCloneOrder.setSummedLineAmount(new BigDecimal("0"));//
      objCloneOrder.setGrandTotalAmount(new BigDecimal("0"));//
      objCloneOrder.setDocumentType(objOrder.getDocumentType());
      objCloneOrder.setCopyFrom(objOrder.isCopyFrom());
      objCloneOrder.setCopyFromPO(objOrder.isCopyFromPO());
      objCloneOrder.setDeliveryNotes(objOrder.getDeliveryNotes());
      objCloneOrder.setActive(objOrder.isActive());
      objCloneOrder.setClient(objOrder.getClient());
      objCloneOrder.setDelivered(objOrder.isDelivered());
      objCloneOrder.setPrint(objOrder.isPrint());
      objCloneOrder.setPrintDiscount(objOrder.isPrintDiscount());
      objCloneOrder.setAccountingDate(objOrder.getAccountingDate());
      objCloneOrder.setDatePrinted(objOrder.getDatePrinted());
      objCloneOrder.setSelected(objOrder.isSelected());
      objCloneOrder.setDropShipContact(objOrder.getDropShipContact());
      objCloneOrder.setDropShipLocation(objOrder.getDropShipLocation());
      objCloneOrder.setDropShipPartner(objOrder.getDropShipPartner());
      objCloneOrder.setSelfService(objOrder.isSelfService());
      objCloneOrder.setGenerateTemplate(objOrder.isGenerateTemplate());
      objCloneOrder.setIncoterms(objOrder.getIncoterms());
      objCloneOrder.setINCOTERMSDescription(objOrder.getINCOTERMSDescription());
      objCloneOrder.setCreationDate(new java.util.Date());
      objCloneOrder.setUpdated(new java.util.Date());
      objCloneOrder.setDelivered(objOrder.isDelivered());
      objCloneOrder.setDeliveryLocation(objOrder.getDeliveryLocation());
      objCloneOrder.setInvoiceAddress(objOrder.getInvoiceAddress());
      objCloneOrder.setDocumentAction("CO");
      objCloneOrder.setDocumentStatus("DR");
      objCloneOrder.setPosted("N");
      objCloneOrder.setProcessed(false);
      objCloneOrder.setSalesTransaction(true);
      objCloneOrder.setTransactionDocument(objOrder.getTransactionDocument());
      // save the cloned order object
      OBDal.getInstance().save(objCloneOrder);
      // get the lines associated with the order and clone them to the new
      // order line.
      List<OrderLine> lsOrderLines = getOrderLines(objOrder);

      for (OrderLine ol : lsOrderLines) {
        String strPriceVersionId = getPriceListVersion(objOrder.getPriceList().getId(), objOrder
            .getClient().getId());
        BigDecimal bdPriceList = getPriceList(ol.getProduct().getId(), strPriceVersionId);
        OrderLine objOrdLine = OBProvider.getInstance().get(OrderLine.class);
        objOrdLine.setNewOBObject(true);
        objOrdLine.setLineNo(ol.getLineNo());
        objOrdLine.setClient(ol.getClient());
        objOrdLine.setOrganization(ol.getOrganization());
        objOrdLine.setActive(ol.isActive());
        objOrdLine.setCreationDate(new java.util.Date());
        objOrdLine.setCreatedBy(ol.getCreatedBy());
        objOrdLine.setUpdated(new java.util.Date());
        objOrdLine.setUpdatedBy(ol.getUpdatedBy());
        objOrdLine.setSalesOrder(objCloneOrder);
        objOrdLine.setBusinessPartner(ol.getBusinessPartner());
        objOrdLine.setPartnerAddress(ol.getPartnerAddress());
        objOrdLine.setOrderDate(ol.getOrderDate());
        objOrdLine.setScheduledDeliveryDate(ol.getScheduledDeliveryDate());
        objOrdLine.setDateDelivered(ol.getDateDelivered());
        objOrdLine.setInvoiceDate(ol.getInvoiceDate());
        objOrdLine.setDescription(ol.getDescription());
        objOrdLine.setProduct(ol.getProduct());
        objOrdLine.setWarehouse(ol.getWarehouse());
        objOrdLine.setDirectShipment(ol.isDirectShipment());
        objOrdLine.setUOM(ol.getUOM());
        objOrdLine.setOrderedQuantity(ol.getOrderedQuantity());
        objOrdLine.setReservedQuantity(new BigDecimal("0"));//
        objOrdLine.setDeliveredQuantity(new BigDecimal("0"));//
        objOrdLine.setInvoicedQuantity(new BigDecimal("0"));//
        objOrdLine.setShippingCompany(ol.getShippingCompany());
        objOrdLine.setCurrency(ol.getCurrency());
        objOrdLine.setListPrice(bdPriceList);//
        objOrdLine.setUnitPrice(ol.getUnitPrice());
        objOrdLine.setPriceLimit(ol.getPriceLimit());
        objOrdLine.setLineNetAmount(ol.getLineNetAmount());
        objOrdLine.setDiscount(ol.getDiscount());
        objOrdLine.setFreightAmount(ol.getFreightAmount());
        objOrdLine.setCharge(ol.getCharge());
        objOrdLine.setChargeAmount(ol.getChargeAmount());
        objOrdLine.setTax(ol.getTax());
        objOrdLine.setResourceAssignment(ol.getResourceAssignment());
        objOrdLine.setSOPOReference(ol.getSOPOReference());
        objOrdLine.setAttributeSetValue(ol.getAttributeSetValue());
        objOrdLine.setDescriptionOnly(ol.isDescriptionOnly());
        objOrdLine.setOrderQuantity(ol.getOrderQuantity());
        objOrdLine.setOrderUOM(ol.getOrderUOM());
        objOrdLine.setPriceAdjustment(ol.getPriceAdjustment());
        objOrdLine.setStandardPrice(ol.getStandardPrice());
        objOrdLine.setCancelPriceAdjustment(ol.isCancelPriceAdjustment());
        objOrdLine.setOrderDiscount(ol.getOrderDiscount());
        objOrdLine.setTaxableAmount(ol.getTaxableAmount());
        objOrdLine.setEditLineAmount(ol.isEditLineAmount());
        OBDal.getInstance().save(objOrdLine);
      }
      OBDal.getInstance().flush();
      json = jsonConverter.toJsonObject(objCloneOrder, DataResolvingMode.FULL);
      OBDal.getInstance().commitAndClose();
      return json;
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return json;
  }

  /**
   * @param objOrder
   * @return
   * @throws ServletException
   */
  private List<OrderLine> getOrderLines(Order objOrder) throws ServletException {
    String whereClause = "salesOrder = :objOrder";
    OBQuery<OrderLine> qOrderLines = OBDal.getInstance().createQuery(OrderLine.class, whereClause);
    qOrderLines.setNamedParameter("objOrder", objOrder);
    return qOrderLines.list();
  }

  private String getPriceListVersion(String priceList, String clientId) {
    OBContext.setAdminMode();
    try {
      String whereClause = " as plv , PricingPriceList pl where pl.id=plv.id and plv.active='Y' and "
          + " pl.id = :priceList and plv.client.id = :clientId order by plv.validFromDate desc";

      OBQuery<PriceListVersion> ppriceListVersion = OBDal.getInstance().createQuery(
          PriceListVersion.class, whereClause);
      ppriceListVersion.setNamedParameter("priceList", priceList);
      ppriceListVersion.setNamedParameter("clientId", clientId);

      if (!ppriceListVersion.list().isEmpty()) {
        return ppriceListVersion.list().get(0).getId();
      } else {
        return "0";
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private BigDecimal getPriceList(String strProductID, String strPriceVersionId) {
    OBContext.setAdminMode();
    BigDecimal bdPriceList = null;
    try {
      final List<Object> parameters = new ArrayList<Object>();
      parameters.add(strProductID);
      parameters.add(strPriceVersionId);
      final String procedureName = "M_BOM_PriceList";
      bdPriceList = (BigDecimal) CallStoredProcedure.getInstance().call(procedureName, parameters,
          null);
    } finally {
      OBContext.restorePreviousMode();
    }

    return (bdPriceList);
  }
}
