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
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.event;

import javax.enterprise.event.Observes;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.client.kernel.event.EntityNewEvent;
import org.openbravo.client.kernel.event.EntityPersistenceEventObserver;
import org.openbravo.client.kernel.event.EntityUpdateEvent;
import org.openbravo.model.pricing.priceadjustment.PriceAdjustment;
import org.openbravo.model.pricing.priceadjustment.Product;

/**
 * Validates the Price Adjustment Products on events
 */
public class PriceAdjustmentProductEventHandler extends EntityPersistenceEventObserver {
  private static final Entity[] ENTITIES = {
      ModelProvider.getInstance().getEntity(Product.ENTITY_NAME) };
  private static final String PRICE_ADJUSTMENT_ID = "5D4BAF6BB86D4D2C9ED3D5A6FC051579";

  @Override
  protected Entity[] getObservedEntities() {
    return ENTITIES;
  }

  public void onSave(@Observes EntityNewEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Product discountProduct = (Product) event.getTargetInstance();
    validatePriceAdjustmentType(discountProduct);
    validateDates(discountProduct);
  }

  public void onUpdate(@Observes EntityUpdateEvent event) {
    if (!isValidEvent(event)) {
      return;
    }
    final Product discountProduct = (Product) event.getTargetInstance();
    validatePriceAdjustmentType(discountProduct);
    validateDates(discountProduct);
  }

  /**
   * When a Price Adjustment is set for each product, the Price Adjustment Type field will decide
   * which kind of discount to apply, so the corresponding field needs to be filled.
   * 
   * @param discountProduct
   *          The Product where a discount is applied that we need to check
   */
  private void validatePriceAdjustmentType(Product discountProduct) {
    final PriceAdjustment discount = discountProduct.getPriceAdjustment();
    if (!discount.getDiscountType().getId().equals(PRICE_ADJUSTMENT_ID)
        || !discount.getPriceAdjustmentScope().equals("E")) {
      return;
    }
    if (discountProduct.getPriceAdjustmentType().equals("A")
        && discountProduct.getDiscountAmount() == null) {
      throw new OBException("@PriceAdjustmentEmptyField@");
    }
    if (discountProduct.getPriceAdjustmentType().equals("P")
        && discountProduct.getDiscount() == null) {
      throw new OBException("@PriceAdjustmentEmptyField@");
    }
    if (discountProduct.getPriceAdjustmentType().equals("F")
        && discountProduct.getFixedPrice() == null) {
      throw new OBException("@PriceAdjustmentEmptyField@");
    }
  }

  /**
   * For price adjustments that are set for each product, checks that the dates in the Product tab
   * are between the ones in the discount header
   * 
   * @param discountProduct
   *          The Product where a discount is applied that we need to check
   */
  private void validateDates(Product discountProduct) {
    final PriceAdjustment discount = discountProduct.getPriceAdjustment();

    // Only check price adjustments that are set for each product
    if (!discount.getDiscountType().getId().equals(PRICE_ADJUSTMENT_ID)
        || !discount.getPriceAdjustmentScope().equals("E")) {
      return;
    }
    if (discountProduct.getStartingDate() != null
        && discountProduct.getStartingDate().before(discount.getStartingDate())) {
      throw new OBException("@PriceAdjustmentProductDateError@");
    }
    if (discountProduct.getEndingDate() != null
        && discountProduct.getEndingDate().after(discount.getEndingDate())) {
      throw new OBException("@PriceAdjustmentProductDateError@");
    }
  }
}
