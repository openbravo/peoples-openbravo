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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.common.actionhandler.createlinesfromprocess;

import java.math.BigDecimal;

/**
 * Class to store Shipment/Receipt Lines Information data
 *
 */
public class ShipmentInOutLineData {
  private String shipmentInOutLineId;
  private BigDecimal movementQuantity;
  private BigDecimal orderQuantity;
  private BigDecimal operativeQuantity;
  private String operativeUOMId;
  private String uomId;

  public ShipmentInOutLineData(Object[] data) {
    this.shipmentInOutLineId = (String) data[0];
    this.movementQuantity = (BigDecimal) data[1];
    this.orderQuantity = (BigDecimal) data[2];
    this.operativeQuantity = (BigDecimal) data[3];
    this.operativeUOMId = (String) data[4];
    this.uomId = (String) data[5];
  }

  public String getShipmentInOutLineId() {
    return shipmentInOutLineId;
  }

  public void setShipmentInOutLineId(String shipmentInOutLineId) {
    this.shipmentInOutLineId = shipmentInOutLineId;
  }

  public BigDecimal getMovementQuantity() {
    return movementQuantity;
  }

  public void setMovementQuantity(BigDecimal movementQuantity) {
    this.movementQuantity = movementQuantity;
  }

  public BigDecimal getOrderQuantity() {
    return orderQuantity;
  }

  public void setOrderQuantity(BigDecimal orderQuantity) {
    this.orderQuantity = orderQuantity;
  }

  public BigDecimal getOperativeQuantity() {
    return operativeQuantity;
  }

  public void setOperativeQuantity(BigDecimal operativeQuantity) {
    this.operativeQuantity = operativeQuantity;
  }

  public String getOperativeUOMId() {
    return operativeUOMId;
  }

  public void setOperativeUOMId(String operativeUOMId) {
    this.operativeUOMId = operativeUOMId;
  }

  public String getUOMId() {
    return uomId;
  }

  public void setUOMId(String uOMId) {
    this.uomId = uOMId;
  }

}
