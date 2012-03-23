/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingServer.TrxType;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.Costing;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;

public class CostingUtils {
  protected static Logger log4j = Logger.getLogger(CostingUtils.class);

  /**
   * Calls {@link #getTransactionCost(MaterialTransaction, Date, boolean)} setting the calculateTrx
   * flag to false.
   */
  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date) {
    return getTransactionCost(transaction, date, false);
  }

  /**
   * Calculates the total cost amount of a transaction including the cost adjustments done until the
   * given date.
   * 
   * @param transaction
   *          MaterialTransaction to get its cost.
   * @param date
   *          The Date it is desired to know the cost.
   * @param calculateTrx
   *          boolean flag to force the calculation of the transaction cost if it is not calculated.
   * @return The total cost amount.
   */
  public static BigDecimal getTransactionCost(MaterialTransaction transaction, Date date,
      boolean calculateTrx) {
    OBCriteria<TransactionCost> obcTrxCost = OBDal.getInstance().createCriteria(
        TransactionCost.class);
    obcTrxCost.add(Restrictions.eq(TransactionCost.PROPERTY_INVENTORYTRANSACTION, transaction));
    obcTrxCost.add(Restrictions.le(TransactionCost.PROPERTY_COSTDATE, date));
    // obcTrxCost.setProjection(Projections.sum(TransactionCost.PROPERTY_COST));

    if (obcTrxCost.count() == 0) {
      // Transaction hasn't been calculated yet.
      if (calculateTrx) {
        log4j.debug("Cost for transaction will be calculated." + transaction.getIdentifier());
        CostingServer transactionCost = new CostingServer(transaction);
        transactionCost.process();
        return transactionCost.getTransactionCost();
      }
      log4j.error("No cost found for transaction " + transaction.getIdentifier() + " with id "
          + transaction.getId() + " on date " + OBDateUtils.formatDate(date));
      throw new OBException("@NoCostFoundForTrxOnDate@ @Transaction@: "
          + transaction.getIdentifier() + " @Date@ " + OBDateUtils.formatDate(date));
    }
    BigDecimal cost = BigDecimal.ZERO;
    for (TransactionCost trxCost : obcTrxCost.list()) {
      cost = cost.add(trxCost.getCost());
    }
    return cost;
  }

  /**
   * Calls {@link #getStandardCost(Product, Date, HashMap, boolean)} setting the
   * recheckWithoutDimensions flag to true.
   */
  public static BigDecimal getStandardCost(Product product, Organization org, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) throws OBException {
    return getStandardCost(product, org, date, costDimensions, true);
  }

  /**
   * Calculates the standard cost of a product on the given date and cost dimensions.
   * 
   * @param product
   *          The Product to get its Standard Cost
   * @param date
   *          The Date to get the Standard Cost
   * @param costDimensions
   *          The cost dimensions to get the Standard Cost if it is defined by some of them.
   * @param recheckWithoutDimensions
   *          boolean flag to force a recall the method to get the Standard Cost at client level if
   *          no cost is found in the given cost dimensions.
   * @return the Standard Cost.
   * @throws OBException
   *           when no standard cost is found.
   */
  public static BigDecimal getStandardCost(Product product, Organization org, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean recheckWithoutDimensions)
      throws OBException {
    Costing stdCost = getStandardCostDefinition(product, org, date, costDimensions,
        recheckWithoutDimensions);
    if (stdCost == null) {
      // If no standard cost is found throw an exception.
      throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName()
          + ", @Date@: " + OBDateUtils.formatDate(date));
    }
    return stdCost.getCost();
  }

  /**
   * Calls {@link #getStandardCostDefinition(Product, Date, HashMap, boolean)} setting the
   * recheckWithoutDimensions flag to true.
   */
  public static Costing getStandardCostDefinition(Product product, Organization org, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) {
    return getStandardCostDefinition(product, org, date, costDimensions, true);
  }

  /**
   * Calculates the standard cost definition of a product on the given date and cost dimensions.
   * 
   * @param product
   *          The Product to get its Standard Cost
   * @param date
   *          The Date to get the Standard Cost
   * @param costDimensions
   *          The cost dimensions to get the Standard Cost if it is defined by some of them.
   * @param recheckWithoutDimensions
   *          boolean flag to force a recall the method to get the Standard Cost at client level if
   *          no cost is found in the given cost dimensions.
   * @return the Standard Cost. Null when no definition is found.
   */
  public static Costing getStandardCostDefinition(Product product, Organization org, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions, boolean recheckWithoutDimensions) {
    // Get cost from M_Costing for given date.
    OBCriteria<Costing> obcCosting = OBDal.getInstance().createCriteria(Costing.class);
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_PRODUCT, product));
    obcCosting.add(Restrictions.le(Costing.PROPERTY_STARTINGDATE, date));
    obcCosting.add(Restrictions.gt(Costing.PROPERTY_ENDINGDATE, date));
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_COSTTYPE, "STA"));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      obcCosting.add(Restrictions.eq(Costing.PROPERTY_WAREHOUSE,
          costDimensions.get(CostDimension.Warehouse)));
    }
    obcCosting.add(Restrictions.eq(Costing.PROPERTY_ORGANIZATION, org));
    obcCosting.setFilterOnReadableOrganization(false);
    if (obcCosting.count() > 0) {
      if (obcCosting.count() > 1) {
        log4j.warn("More than one cost found for same date: " + OBDateUtils.formatDate(date)
            + " for product: " + product.getName() + " (" + product.getId() + ")");
      }
      if (obcCosting.list().get(0).getCost() == null) {
        throw new OBException("@NoStandardCostDefined@ @Product@: " + product.getName()
            + ", @Date@: " + OBDateUtils.formatDate(date));
      }
      return obcCosting.list().get(0);
    } else if (recheckWithoutDimensions) {
      return getStandardCostDefinition(product, org, date, getEmptyDimensions(), false);
    }
    return null;
  }

  /**
   * @return The costDimensions HashMap with null values for the dimensions.
   */
  public static HashMap<CostDimension, BaseOBObject> getEmptyDimensions() {
    HashMap<CostDimension, BaseOBObject> costDimensions = new HashMap<CostDimension, BaseOBObject>();
    costDimensions.put(CostDimension.Warehouse, null);
    return costDimensions;
  }

  /**
   * Calculates the stock of the product on the given date and for the given cost dimensions. It
   * only takes transactions that have its cost calculated.
   */
  public static BigDecimal getCurrentStock(Product product, Organization org, Date date,
      HashMap<CostDimension, BaseOBObject> costDimensions) {
    OBCriteria<MaterialTransaction> obcTrx = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    obcTrx.createAlias(MaterialTransaction.PROPERTY_STORAGEBIN, "locator");
    obcTrx.setFilterOnReadableOrganization(false);
    obcTrx.add(Restrictions.eq(MaterialTransaction.PROPERTY_PRODUCT, product));
    obcTrx.add(Restrictions.le(MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE, date));
    // Include only transactions that have its cost calculated
    obcTrx.add(Restrictions.isNotNull(MaterialTransaction.PROPERTY_TRANSACTIONCOST));
    if (costDimensions.get(CostDimension.Warehouse) != null) {
      obcTrx.add(Restrictions.eq("locator." + Locator.PROPERTY_WAREHOUSE,
          costDimensions.get(CostDimension.Warehouse)));
    }
    // Get child tree of organizations.
    Set<String> orgs = OBContext.getOBContext().getOrganizationStructureProvider()
        .getChildTree(org.getId(), true);
    obcTrx.add(Restrictions.in(MaterialTransaction.PROPERTY_ORGANIZATION + ".id", orgs));
    obcTrx.setProjection(Projections.sum(MaterialTransaction.PROPERTY_MOVEMENTQUANTITY));

    if (obcTrx.list() != null && obcTrx.list().size() > 0) {
      @SuppressWarnings("rawtypes")
      List o = obcTrx.list();
      Object resultSet = (Object) o.get(0);
      return (resultSet != null) ? (BigDecimal) resultSet : BigDecimal.ZERO;
    }

    return BigDecimal.ZERO;
  }

  public static BusinessPartner getTrxBusinessPartner(MaterialTransaction transaction,
      TrxType trxType) {
    switch (trxType) {
    case Receipt:
    case ReceiptNegative:
    case ReceiptReturn:
    case ReceiptVoid:
    case Shipment:
    case ShipmentNegative:
    case ShipmentReturn:
    case ShipmentVoid:
      return transaction.getGoodsShipmentLine().getShipmentReceipt().getBusinessPartner();
    default:
      return null;
    }
  }

  public static OrderLine getOrderLine(Product product, BusinessPartner bp, Organization org) {
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider();

    StringBuffer where = new StringBuffer();
    where.append(" as ol");
    where.append("   join ol." + OrderLine.PROPERTY_SALESORDER + " as o");
    where.append("   join o." + Order.PROPERTY_DOCUMENTTYPE + " as dt");
    where.append(" where o." + Order.PROPERTY_BUSINESSPARTNER + " = :bp");
    where.append("   and ol." + OrderLine.PROPERTY_PRODUCT + " = :product");
    where.append("   and o." + Order.PROPERTY_ORGANIZATION + ".id in :org");
    where.append("   and o." + Order.PROPERTY_DOCUMENTSTATUS + " in ('CO', 'CL')");
    where.append("   and o." + Order.PROPERTY_SALESTRANSACTION + " = false");
    where.append("   and dt." + DocumentType.PROPERTY_RETURN + " = false");
    where.append(" order by o." + Order.PROPERTY_ORDERDATE + " desc");
    OBQuery<OrderLine> olQry = OBDal.getInstance().createQuery(OrderLine.class, where.toString());
    olQry.setNamedParameter("bp", bp);
    olQry.setNamedParameter("product", product);
    olQry.setNamedParameter("org", osp.getChildTree(org.getId(), true));
    if (!olQry.list().isEmpty()) {
      olQry.setMaxResult(1);
      return olQry.list().get(0);
    }
    return null;
  }
}
