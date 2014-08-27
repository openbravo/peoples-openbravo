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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.cost.CostAdjustment;
import org.openbravo.model.materialmgmt.cost.CostAdjustmentLine;
import org.openbravo.model.materialmgmt.cost.TransactionCost;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.procurement.ReceiptInvoiceMatch;

public class PriceDifferenceProcess {

  public static void processPriceDifferenceTransaction(MaterialTransaction materialTransaction)
      throws OBException, JSONException {
    BigDecimal newAmountCost = BigDecimal.ZERO;
    BigDecimal orderAmt = BigDecimal.ZERO;
    BigDecimal oldAmountCost = BigDecimal.ZERO;
    BigDecimal qty = BigDecimal.ZERO;
    Date costAdjDateAcct = null;

    CostAdjustment costAdjustmentHeader = CostAdjustmentUtils.insertCostAdjustmentHeader(
        materialTransaction.getOrganization(), "PDC"); // PDC= Price Difference Correction
    // No invoice
    if (materialTransaction.getGoodsShipmentLine().getProcurementReceiptInvoiceMatchList().size() == 0) {// getProcurementReceiptInvoiceMatchList()????

      for (org.openbravo.model.procurement.POInvoiceMatch matchPO : materialTransaction
          .getGoodsShipmentLine().getProcurementPOInvoiceMatchList()) {
        orderAmt = matchPO.getQuantity().multiply(matchPO.getSalesOrderLine().getUnitPrice());
        newAmountCost = newAmountCost.add(FinancialUtils.getConvertedAmount(orderAmt, matchPO
            .getSalesOrderLine().getCurrency(), materialTransaction.getCurrency(),
            materialTransaction.getMovementDate(), materialTransaction.getOrganization(),
            FinancialUtils.PRECISION_STANDARD));
        if ((costAdjDateAcct == null)
            || (costAdjDateAcct.before(matchPO.getInvoiceLine().getInvoice().getInvoiceDate()))) {
          costAdjDateAcct = matchPO.getInvoiceLine().getInvoice().getInvoiceDate();
        }
      }
    } else {
      for (ReceiptInvoiceMatch matchReceipt : materialTransaction.getGoodsShipmentLine()
          .getProcurementReceiptInvoiceMatchList()) {
        orderAmt = matchReceipt.getQuantity()
            .multiply(matchReceipt.getInvoiceLine().getUnitPrice());
        newAmountCost = newAmountCost.add(FinancialUtils.getConvertedAmount(orderAmt, matchReceipt
            .getInvoiceLine().getInvoice().getCurrency(), materialTransaction.getCurrency(),
            materialTransaction.getMovementDate(), materialTransaction // Currency From??????
                .getOrganization(), FinancialUtils.PRECISION_STANDARD));
        qty = qty.add(matchReceipt.getQuantity());
        if ((costAdjDateAcct == null)
            || (costAdjDateAcct.before(matchReceipt.getInvoiceLine().getInvoice().getInvoiceDate()))) {
          costAdjDateAcct = matchReceipt.getInvoiceLine().getInvoice().getInvoiceDate();
        }
      }
      if (qty != materialTransaction.getMovementQuantity()) {
        for (org.openbravo.model.procurement.POInvoiceMatch matchPO : materialTransaction
            .getGoodsShipmentLine().getProcurementPOInvoiceMatchList()) {

          if (matchPO.getGoodsShipmentLine() != null) {

            orderAmt = (matchPO.getQuantity().subtract(qty)).multiply(matchPO.getSalesOrderLine()
                .getUnitPrice());
            newAmountCost = newAmountCost.add(FinancialUtils.getConvertedAmount(orderAmt, matchPO
                .getSalesOrderLine().getCurrency(), materialTransaction.getCurrency(),
                materialTransaction.getMovementDate(), materialTransaction.getOrganization(),
                FinancialUtils.PRECISION_STANDARD));
            break;
          }
        }
      }
    }
    oldAmountCost = materialTransaction.getTransactionCost();
    for (TransactionCost trxCost : materialTransaction.getTransactionCostList()) {
      if (trxCost.getCostAdjustmentLine() != null
          && trxCost.getCostAdjustmentLine().getCostAdjustment().getSourceProcess().equals("PDC")) {
        oldAmountCost = oldAmountCost.add(trxCost.getCost());
      }
    }

    if (costAdjDateAcct == null) {
      costAdjDateAcct = new Date();
    }
    CostAdjustmentLine costAdjLine = CostAdjustmentUtils.insertCostAdjustmentLine(
        materialTransaction, costAdjustmentHeader, newAmountCost.subtract(oldAmountCost),
        Boolean.TRUE, null, costAdjDateAcct);

    costAdjLine.setNeedsPosting(false);
    OBDal.getInstance().save(costAdjLine);

    materialTransaction.setCheckpricedifference(false);
    OBDal.getInstance().save(materialTransaction);

  }

  /**
   * Method to process a cost adjustment.
   * 
   * @param costAdj
   *          the cost adjustment to be processed.
   * @return the message to be shown to the user properly formatted and translated to the user
   *         language.
   * @throws OBException
   *           when there is an error that prevents the cost adjustment to be processed.
   * @throws JSONException
   */
  public static JSONObject processPriceDifference(Date date, Product product) throws OBException,
      JSONException {

    JSONObject message = new JSONObject();
    message.put("severity", "success");
    message.put("title", "");
    message.put("text", OBMessageUtils.messageBD("Success"));

    OBCriteria<MaterialTransaction> mTrxs = OBDal.getInstance().createCriteria(
        MaterialTransaction.class);
    if (date != null) {
      mTrxs.add(Restrictions.le(MaterialTransaction.PROPERTY_MOVEMENTDATE, date));
    }
    if (product != null) {
      mTrxs.add(Restrictions.eq(MaterialTransaction.PROPERTY_PRODUCT, product));
    }
    mTrxs.add(Restrictions.eq(MaterialTransaction.PROPERTY_CHECKPRICEDIFFERENCE, true));
    mTrxs.addOrderBy(MaterialTransaction.PROPERTY_MOVEMENTDATE, true);
    mTrxs.addOrderBy(MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE, true);
    ScrollableResults lines = mTrxs.scroll(ScrollMode.FORWARD_ONLY);

    while (lines.next()) {
      MaterialTransaction line = (MaterialTransaction) lines.get(0);
      processPriceDifferenceTransaction(line);
    }
    return message;
  }
}
