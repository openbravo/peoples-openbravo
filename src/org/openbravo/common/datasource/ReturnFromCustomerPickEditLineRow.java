package org.openbravo.common.datasource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openbravo.service.json.JsonUtils;

public class ReturnFromCustomerPickEditLineRow {
  private String c_rm_order_pick_edit_lines_id;
  private String ad_client_id;
  private String ad_client_identifier;
  private String ad_org_id;
  private String ad_org_identifier;
  private Character isActive;
  private String createdBy;
  private String updatedBy;
  private Date created;
  private Date updated;
  private String obSelected;
  private String mInOutLineId;
  private String returnOrderid;
  private String inOutNo;
  private Date movementDate;
  private String mProductId;
  private String mProductIdentifier;
  private String mAttributeInstanceId;
  private String mAttributeInstanceIdentifier;
  private Number movementQty;
  private String cUomId;
  private String cUomIdentifier;
  private Number returned;
  private Number priceActual;
  private String cReturnReasonId;
  private String cReturnReasonIdentifier;
  private String orderNo;
  private Number returnedQty;
  private String cBPartnerId;
  private String cTaxId;
  private Character isTaxIncluded;
  private String cOrderLineId;

  private final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
  private final SimpleDateFormat xmlDateTimeFormat = JsonUtils.createDateTimeFormat();

  public ReturnFromCustomerPickEditLineRow(Object[] values) {
    this.c_rm_order_pick_edit_lines_id = (String) values[0];
    this.ad_client_id = (String) values[1];
    this.ad_org_id = (String) values[2];
    this.ad_client_identifier = (String) values[3];
    this.ad_org_identifier = (String) values[4];
    this.isActive = (Character) values[5];
    this.createdBy = (String) values[6];
    this.updatedBy = (String) values[7];
    this.created = (Date) values[8];
    this.updated = (Date) values[9];
    this.obSelected = (String) values[10];
    this.mInOutLineId = (String) values[11];
    this.returnOrderid = (String) values[12];
    this.inOutNo = (String) values[13];
    this.movementDate = (Date) values[14];
    this.mProductId = (String) values[15];
    this.mProductIdentifier = (String) values[16];
    this.mAttributeInstanceId = (String) values[17];
    this.mAttributeInstanceIdentifier = (String) values[18];
    this.movementQty = (Number) values[19];
    this.cUomId = (String) values[20];
    this.cUomIdentifier = (String) values[21];
    this.returned = (Number) values[22];
    this.priceActual = (Number) values[23];
    this.cReturnReasonId = (String) values[24];
    this.cReturnReasonIdentifier = (String) values[25];
    this.orderNo = (String) values[26];
    this.returnedQty = (Number) values[27];
    if (this.returnedQty == null) {
      this.returnedQty = 0;
    }
    this.cBPartnerId = (String) values[28];
    this.cTaxId = (String) values[29];
    this.isTaxIncluded = (Character) values[30];
    this.cOrderLineId = (String) values[31];
    if (this.cOrderLineId != null && this.cOrderLineId.isEmpty()) {
      this.cOrderLineId = null;
    }
  }

  public Map<String, Object> toMap() {
    Map<String, Object> row = new LinkedHashMap<String, Object>();

    row.put("_identifier", this.c_rm_order_pick_edit_lines_id);
    row.put("entityName", "ReturnMaterialOrderPickEditLines");
    row.put("client", this.ad_client_id);
    row.put("client$_identifier", this.ad_client_identifier);
    row.put("organization", this.ad_org_id);
    row.put("organization$_identifier", this.ad_org_identifier);
    row.put("active", this.isActive);
    row.put("createdBy", this.createdBy);
    row.put("updatedBy", this.updatedBy);
    row.put("creationDate", xmlDateTimeFormat.format(this.created));
    row.put("updated", xmlDateTimeFormat.format(this.updated));
    if ("Y".equals(this.obSelected)) {
      row.put("obSelected", true);
    } else {
      row.put("obSelected", false);
    }
    row.put("goodsShipmentLine", this.mInOutLineId);
    row.put("returnOrder", this.returnOrderid);
    row.put("inOutDocumentNumber", this.inOutNo);
    if (this.movementDate != null) {
      row.put("movementDate", xmlDateFormat.format(this.movementDate));
    }
    row.put("product", this.mProductId);
    row.put("product$_identifier", this.mProductIdentifier);
    row.put("attributeSetValue", this.mAttributeInstanceId);
    row.put("attributeSetValue$_identifier", this.mAttributeInstanceIdentifier);
    row.put("movementQuantity", this.movementQty);
    row.put("uOM", this.cUomId);
    row.put("uOM$_identifier", this.cUomIdentifier);
    row.put("returned", this.returned);
    row.put("unitPrice", this.priceActual);
    row.put("returnReason", this.cReturnReasonId);
    row.put("returnReason$_identifier", this.cReturnReasonIdentifier);
    row.put("orderNo", this.orderNo);
    row.put("salesTransaction", true);
    row.put("returnQtyOtherRM", this.returnedQty);
    row.put("businessPartner", this.cBPartnerId);
    row.put("tax", this.cTaxId);
    row.put("priceIncludesTax", this.isTaxIncluded);
    row.put("salesOrderLine", this.cOrderLineId);
    return row;
  }
}