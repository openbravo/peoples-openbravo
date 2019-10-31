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
 * All portions are Copyright (C) 2016-2019 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.cancelandreplace.data;

public abstract class CancelAndReplaceTestData {

  /*
   * CONSTANTS:
   */
  protected static final String BP_CUSTOMER_A = "4028E6C72959682B01295F40C3CB02EC";

  private String testNumber;
  private String testDescription;
  private String bpartnerId;
  private boolean activateNettingGoodsShipmentPref;
  private boolean activateAssociateNettingGoodsShipmentPref;
  private boolean orderPaid;
  private CancelAndReplaceOrderTestData oldOrder;
  private CancelAndReplaceOrderTestData inverseOrder;
  private CancelAndReplaceOrderTestData newOrder;

  private String errorMessage;

  public String getBpartnerId() {
    return bpartnerId;
  }

  public void setBpartnerId(String bpartnerId) {
    this.bpartnerId = bpartnerId;
  }

  public String getTestDescription() {
    return testDescription;
  }

  public void setTestDescription(String testDescription) {
    this.testDescription = testDescription;
  }

  public String getTestNumber() {
    return testNumber;
  }

  public void setTestNumber(String testNumber) {
    this.testNumber = testNumber;
  }

  public CancelAndReplaceOrderTestData getOldOrder() {
    return oldOrder;
  }

  public void setOldOrder(CancelAndReplaceOrderTestData oldOrder) {
    this.oldOrder = oldOrder;
  }

  public CancelAndReplaceOrderTestData getInverseOrder() {
    return inverseOrder;
  }

  public void setInverseOrder(CancelAndReplaceOrderTestData inverseOrder) {
    this.inverseOrder = inverseOrder;
  }

  public CancelAndReplaceOrderTestData getNewOrder() {
    return newOrder;
  }

  public void setNewOrder(CancelAndReplaceOrderTestData newOrder) {
    this.newOrder = newOrder;
  }

  public boolean isActivateNettingGoodsShipmentPref() {
    return activateNettingGoodsShipmentPref;
  }

  public void setActivateNettingGoodsShipmentPref(boolean activateNettingGoodsShipmentPref) {
    this.activateNettingGoodsShipmentPref = activateNettingGoodsShipmentPref;
  }

  public boolean isActivateAssociateNettingGoodsShipmentPref() {
    return activateAssociateNettingGoodsShipmentPref;
  }

  public void setActivateAssociateNettingGoodsShipmentPref(
      boolean activateAssociateNettingGoodsShipmentPref) {
    this.activateAssociateNettingGoodsShipmentPref = activateAssociateNettingGoodsShipmentPref;
  }

  public CancelAndReplaceTestData() {
    initialize();
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public abstract void initialize();

  public boolean isOrderPaid() {
    return orderPaid;
  }

  public void setOrderPaid(boolean orderPaid) {
    this.orderPaid = orderPaid;
  }

}
