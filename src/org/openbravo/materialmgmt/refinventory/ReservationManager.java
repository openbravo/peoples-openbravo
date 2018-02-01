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

package org.openbravo.materialmgmt.refinventory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;

/**
 * Manages reservations in referenced inventory.
 * 
 * For each storage detail that is going to be box/unboxed to a referenced inventory, it searches
 * for any possible reservation already linked. If the quantity on hand not reserved yet is greater
 * or equal than the qty to box/unbox, then there is no neeed to modify any existing reservation. If
 * the process must box/unbox something already linked to a reservation, then it releases the
 * affected reserved quantity before running the goods movement and just after that recreates a new
 * reservation pointing to the new attribute set instance and (optionally) to the new storage bin.
 * 
 * The process is exactly the same for boxing and unboxing.
 * 
 * Note that everything is done within the same transaction so no possibility to create collateral
 * issues.
 *
 */
class ReservationManager {
  private final List<ReferencedInventoryReservation> refInvReservations = new ArrayList<>();

  void releaseReservationsIfNecessary(final StorageDetail storageDetail,
      final BigDecimal qtyMovement, final AttributeSetInstance newAttributeSetInstance,
      final Locator newStorageBin) {
    final BigDecimal qtyOnHand = storageDetail.getQuantityOnHand();
    final BigDecimal qtyReserved = storageDetail.getReservedQty();
    final BigDecimal qtyOnHandNotReserved = qtyOnHand.subtract(qtyReserved);

    if (hasQtyReserved(qtyReserved)
        && !hasEnoughQtyOnHandNotReservedToFulfillQtyMovement(qtyOnHandNotReserved, qtyMovement)) {
      BigDecimal remainingQtyToReleaseInReservations = qtyMovement.subtract(qtyOnHandNotReserved);
      final ScrollableResults reservationStockScroll = getAvailableStockReservations(storageDetail,
          newStorageBin);
      try {
        while (reservationStockScroll.next()
            && hasPendingReservationsToModify(remainingQtyToReleaseInReservations)) {
          final ReservationStock reservationStock = (ReservationStock) reservationStockScroll.get()[0];
          final BigDecimal currentReservedQty = (BigDecimal) reservationStockScroll.get()[1];
          final BigDecimal qtyToModifyInThisReservation = remainingQtyToReleaseInReservations
              .min(currentReservedQty);

          final Reservation reservation = releaseQtyInReservationAndCloseItIfPossible(
              qtyToModifyInThisReservation, reservationStock);
          refInvReservations.add(new ReferencedInventoryReservation(qtyToModifyInThisReservation,
              reservation, newStorageBin, newAttributeSetInstance.getId(), reservationStock
                  .isAllocated(), reservation.getStorageBin() != null, reservation
                  .getAttributeSetValue() != null));

          remainingQtyToReleaseInReservations = remainingQtyToReleaseInReservations
              .subtract(qtyToModifyInThisReservation);
          // OBDal.getInstance().getSession().evict(reservationStock); // Problems in JUnit
        }

        if (hasPendingReservationsToModify(remainingQtyToReleaseInReservations)) {
          throw new OBException(String.format(
              OBMessageUtils.messageBD("RefInventoryCannotReallocateAllQuantity"),
              remainingQtyToReleaseInReservations));
        }
      } finally {
        if (reservationStockScroll != null) {
          reservationStockScroll.close();
        }
      }
    }
  }

  private Reservation releaseQtyInReservationAndCloseItIfPossible(
      BigDecimal reservationQtyToModify, final ReservationStock reservationStock) {
    reservationStock.setReleased(reservationStock.getReleased().add(reservationQtyToModify));
    OBDal.getInstance().save(reservationStock);
    OBDal.getInstance().flush(); // Necessary to run trigger that updates reservation header
    final Reservation reservation = reservationStock.getReservation();
    OBDal.getInstance().refresh(reservation); // Necessary to get updated released qty

    if (reservation.getQuantity().compareTo(reservation.getReleased()) == 0) {
      ReservationUtils.processReserve(reservation, "CL");
    }
    return reservation;
  }

  private boolean hasQtyReserved(final BigDecimal qtyReserved) {
    return qtyReserved.compareTo(BigDecimal.ZERO) > 0;
  }

  private boolean hasEnoughQtyOnHandNotReservedToFulfillQtyMovement(
      final BigDecimal qtyOnHandNotReserved, final BigDecimal qtyMovement) {
    return qtyOnHandNotReserved.compareTo(qtyMovement) >= 0;
  }

  private boolean hasPendingReservationsToModify(final BigDecimal reservationQtyToModify) {
    return reservationQtyToModify.compareTo(BigDecimal.ZERO) > 0;
  }

  private ScrollableResults getAvailableStockReservations(final StorageDetail storageDetail,
      final Locator newStorageBin) {
    final String olHql = "select sr, sr.quantity - sr.released " + //
        "from MaterialMgmtReservationStock sr " + //
        "join sr.reservation res " + //
        "where coalesce(sr.storageBin.id, res.storageBin.id) = :sdBinId " + //
        // Skip reservations forced to a bin different from the destination bin
        "and (res.storageBin.id is null or res.storageBin.id = :toBindId) " + //
        "and coalesce(sr.attributeSetValue.id, res.attributeSetValue.id) = :sdAttributeSetId " + //
        "and sr.quantity - sr.released > 0 " + //
        "and res.product.id = :productId " + //
        "and res.uOM.id = :uomId " + //
        "and res.rESStatus = 'CO' " + //
        "order by case when sr.allocated = 'Y' then 1 else 0 end, " + //
        "      case when res.attributeSetValue.id is not null then 1 else 0 end, " + //
        "      sr.quantity - sr.released asc  ";
    final Session session = OBDal.getInstance().getSession();
    final Query sdQuery = session.createQuery(olHql.toString());
    sdQuery.setParameter("sdBinId", storageDetail.getStorageBin().getId());
    sdQuery.setParameter("toBindId", newStorageBin.getId());
    sdQuery.setParameter("sdAttributeSetId", storageDetail.getAttributeSetValue().getId());
    sdQuery.setParameter("productId", storageDetail.getProduct().getId());
    sdQuery.setParameter("uomId", storageDetail.getUOM().getId());
    sdQuery.setFetchSize(1000);
    return sdQuery.scroll(ScrollMode.FORWARD_ONLY);
  }

  private class ReferencedInventoryReservation {
    final BigDecimal reservationQty;
    final String orgId;
    final String productId;
    final String uomId;
    final String orderLineId;
    final String warehouseId;
    final String storageBinId;
    final String attributeSetId;
    final boolean isAllocated;
    final boolean isForceBin;
    final boolean isForceAttributeSet;

    ReferencedInventoryReservation(BigDecimal reservationQty, Reservation reservation,
        Locator newStorageBin, String attributeSetId, boolean isAllocated, boolean isForceBin,
        boolean isForceAttributeSet) {
      this.reservationQty = reservationQty;
      this.orgId = reservation.getOrganization().getId();
      this.productId = reservation.getProduct().getId();
      this.uomId = reservation.getUOM().getId();
      this.orderLineId = reservation.getSalesOrderLine() == null ? null : reservation
          .getSalesOrderLine().getId();
      this.warehouseId = newStorageBin.getWarehouse().getId();
      this.storageBinId = newStorageBin.getId();
      this.attributeSetId = attributeSetId;
      this.isAllocated = isAllocated;
      this.isForceBin = isForceBin;
      this.isForceAttributeSet = isForceAttributeSet;
    }

  }

  void createRefInventoryReservationsIfNecessary() {
    for (final ReferencedInventoryReservation refInvReservation : refInvReservations) {
      createAndProcessReservation(refInvReservation);
    }
  }

  private void createAndProcessReservation(final ReferencedInventoryReservation refInvReservation) {
    final Reservation reservation = OBProvider.getInstance().get(Reservation.class);
    reservation.setOrganization(OBDal.getInstance().getProxy(Organization.class,
        refInvReservation.orgId));
    reservation.setQuantity(refInvReservation.reservationQty);
    reservation
        .setProduct(OBDal.getInstance().getProxy(Product.class, refInvReservation.productId));
    reservation.setUOM(OBDal.getInstance().getProxy(UOM.class, refInvReservation.uomId));
    if (refInvReservation.orderLineId != null) {
      reservation.setSalesOrderLine(OBDal.getInstance().getProxy(OrderLine.class,
          refInvReservation.orderLineId));
    }
    reservation.setWarehouse(OBDal.getInstance().getProxy(Warehouse.class,
        refInvReservation.warehouseId));
    reservation.setStorageBin(OBDal.getInstance().getProxy(Locator.class,
        refInvReservation.storageBinId));
    reservation.setAttributeSetValue(OBDal.getInstance().getProxy(AttributeSetInstance.class,
        refInvReservation.attributeSetId));
    OBDal.getInstance().save(reservation);
    ReservationUtils.processReserve(reservation, "PR");
    OBDal.getInstance().refresh(reservation); // Refresh to update status

    if (refInvReservation.isAllocated) {
      transformToAllocated(reservation);
    }

    cleanHeaderBinAndAttributeSetIfNecessary(reservation, refInvReservation.isForceBin,
        refInvReservation.isForceAttributeSet);
  }

  private void transformToAllocated(final Reservation reservation) {
    for (final ReservationStock reservationStock : reservation
        .getMaterialMgmtReservationStockList()) {
      reservationStock.setAllocated(true);
      OBDal.getInstance().save(reservationStock);
    }
  }

  private void cleanHeaderBinAndAttributeSetIfNecessary(final Reservation reservation,
      boolean isForceBin, boolean isForceAttributeSet) {
    if (!isForceBin) {
      reservation.setStorageBin(null);
    }
    if (!isForceAttributeSet) {
      reservation.setAttributeSetValue(null);
    }
    if (!isForceBin || !isForceAttributeSet) {
      OBDal.getInstance().save(reservation);
      OBDal.getInstance().flush(); // Necessary to flush here
    }
  }
}
