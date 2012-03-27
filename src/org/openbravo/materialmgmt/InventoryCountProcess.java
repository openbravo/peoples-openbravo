package org.openbravo.materialmgmt;

import java.util.Calendar;

import org.apache.commons.lang.time.DateUtils;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.AttributeSet;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.calendar.PeriodControl;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.InventoryCountLine;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

public class InventoryCountProcess implements Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
      // retrieve custom params
      final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("M_Inventory_ID");
      final InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, recordID);

      // lock inventory
      inventory.setProcessNow(true);
      OBDal.getInstance().save(inventory);
      OBDal.getInstance().commitAndClose();

      if ("CO".equals(strAction)) {
        msg = processInventory(inventory);
      }
      inventory.setProcessNow(false);

      OBDal.getInstance().save(inventory);
      OBDal.getInstance().flush();

      bundle.setResult(msg);

    } catch (final Exception e) {
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(FIN_Utility.getExceptionMessage(e));
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
    }

  }

  public OBError processInventory(InventoryCount inventory) throws OBException {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    runChecks(inventory);
    for (InventoryCountLine icl : inventory.getMaterialMgmtInventoryCountLineList()) {
      MaterialTransaction trx = OBProvider.getInstance().get(MaterialTransaction.class);
      trx.setOrganization(icl.getOrganization());
      trx.setMovementType("I+");
      trx.setStorageBin(icl.getStorageBin());
      trx.setProduct(icl.getProduct());
      trx.setAttributeSetValue(icl.getAttributeSetValue() == null ? OBDal.getInstance().get(
          AttributeSetInstance.class, "0") : icl.getAttributeSetValue());
      trx.setMovementDate(inventory.getMovementDate());
      trx.setMovementQuantity(icl.getBookQuantity() == null ? icl.getQuantityCount() : icl
          .getQuantityCount().subtract(icl.getBookQuantity()));
      trx.setPhysicalInventoryLine(icl);
      trx.setOrderUOM(icl.getOrderUOM());
      trx.setOrderQuantity(icl.getQuantityOrderBook() == null ? icl.getOrderQuantity() : icl
          .getOrderQuantity().subtract(icl.getQuantityOrderBook()));
      trx.setUOM(icl.getUOM());
      OBDal.getInstance().save(trx);
      if (!checkStock(icl.getProduct(), icl.getOrganization())) {
        throw new OBException("@NotEnoughStocked@ @line@ " + icl.getLineNo());
      }
      inventory.setProcessed(true);
    }
    return msg;
  }

  private void runChecks(InventoryCount inventory) throws OBException {
    if (inventory.isProcessNow()) {
      throw new OBException(OBMessageUtils.parseTranslation("@OtherProcessActive@"));
    }
    if (inventory.isProcessed()) {
      throw new OBException(OBMessageUtils.parseTranslation("@AlreadyPosted@"));
    }
    // Products without attribute set.
    StringBuffer where = new StringBuffer();
    where.append(" as icl");
    where.append("   join icl." + InventoryCountLine.PROPERTY_PRODUCT + " as p");
    where.append("   join p." + Product.PROPERTY_ATTRIBUTESET + " as as");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " = :inventory");
    where.append("   and as." + AttributeSet.PROPERTY_REQUIREATLEASTONEVALUE + " = true");
    where.append("   and coalesce(p." + Product.PROPERTY_ATTRIBUTESETVALUE + ", '-') <> 'F'");
    where.append("   and coalesce(icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ", '0') = '0'");
    where.append("  order by icl." + InventoryCountLine.PROPERTY_LINENO);
    OBQuery<InventoryCountLine> iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class,
        where.toString());
    iclQry.setNamedParameter("inventory", inventory);
    if (!iclQry.list().isEmpty()) {
      throw new OBException(OBMessageUtils.parseTranslation("@Inline@ "
          + iclQry.list().get(0).getLineNo() + " @productWithoutAttributeSet@"));
    }

    // duplicated product
    where = new StringBuffer();
    where.append(" as icl");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " = :inventory");
    where.append("   and exists (select 1 from " + InventoryCountLine.ENTITY_NAME + " as icl2");
    where.append("       where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " = icl2."
        + InventoryCountLine.PROPERTY_PHYSINVENTORY);
    where.append("       where icl." + InventoryCountLine.PROPERTY_PRODUCT + " = icl2."
        + InventoryCountLine.PROPERTY_PRODUCT);
    where.append("       where coalesce(icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ", '0') = coalesce(icl2." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE + ", '0')");
    where.append("       where icl." + InventoryCountLine.PROPERTY_ORDERUOM + " = icl2."
        + InventoryCountLine.PROPERTY_ORDERUOM);
    where.append("       where icl." + InventoryCountLine.PROPERTY_STORAGEBIN + " = icl2."
        + InventoryCountLine.PROPERTY_STORAGEBIN);
    where.append("       where icl." + InventoryCountLine.PROPERTY_LINENO + " <> icl2."
        + InventoryCountLine.PROPERTY_LINENO);
    where.append(" order by icl." + InventoryCountLine.PROPERTY_PRODUCT);
    where.append(", icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE);
    where.append(", icl." + InventoryCountLine.PROPERTY_STORAGEBIN);
    where.append(", icl." + InventoryCountLine.PROPERTY_ORDERUOM);
    where.append(", icl." + InventoryCountLine.PROPERTY_LINENO);
    iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class, where.toString());
    iclQry.setNamedParameter("inventory", inventory);
    if (!iclQry.list().isEmpty()) {
      String lines = "";
      for (InventoryCountLine icl : iclQry.list()) {
        lines += icl.getLineNo().toString() + ", ";
      }
      throw new OBException(OBMessageUtils.parseTranslation("@Thelines@ " + lines
          + "@sameInventorylines@"));
    }

    Organization org = inventory.getOrganization();
    if (!org.isReady()) {
      throw new OBException(OBMessageUtils.parseTranslation("@OrgHeaderNotReady@"));
    }
    if (!org.getOrganizationType().isTransactionsAllowed()) {
      throw new OBException(OBMessageUtils.parseTranslation("@OrgHeaderNotTransAllowed@"));
    }
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(
        inventory.getClient().getId());
    Organization headerLEorBU = osp.getLegalEntityOrBusinessUnit(org);
    iclQry = OBDal.getInstance().createQuery(
        InventoryCountLine.class,
        InventoryCountLine.PROPERTY_PHYSINVENTORY + " = :inventory and "
            + InventoryCountLine.PROPERTY_ORGANIZATION + " <> :organization");
    iclQry.setNamedParameter("inventory", inventory);
    iclQry.setNamedParameter("organization", org);
    if (!iclQry.list().isEmpty()) {
      for (InventoryCountLine icl : iclQry.list()) {
        if (!headerLEorBU.getId().equals(
            DalUtil.getId(osp.getLegalEntityOrBusinessUnit(icl.getOrganization())))) {
          throw new OBException(OBMessageUtils.parseTranslation("@LinesAndHeaderDifferentLEorBU@"));
        }
      }
    }
    if (headerLEorBU.getOrganizationType().isLegalEntityWithAccounting()) {
      where = new StringBuffer();
      where.append(" as p ");
      where.append(" where p." + Period.PROPERTY_STARTINGDATE + " >= :dateStarting");
      where.append(" and p." + Period.PROPERTY_ENDINGDATE + " <= :dateEnding");
      where.append(" and exists (select 1 from " + PeriodControl.ENTITY_NAME + " as pc ");
      where.append("    where p." + Period.PROPERTY_ID + " = pc." + PeriodControl.PROPERTY_PERIOD);
      where.append("      and pc." + PeriodControl.PROPERTY_DOCUMENTCATEGORY + " = 'MMI' ");
      where.append("      and pc." + PeriodControl.PROPERTY_ORGANIZATION + " = :org");
      where.append("      and pc." + PeriodControl.PROPERTY_PERIODSTATUS + " = 'O'");
      where.append("            )");
      OBQuery<Period> pQry = OBDal.getInstance().createQuery(Period.class, where.toString());
      pQry.setNamedParameter("dateStarting", inventory.getMovementDate());
      pQry.setNamedParameter("dateEnding",
          DateUtils.truncate(inventory.getMovementDate(), Calendar.DATE));
      pQry.setNamedParameter("org", osp.getPeriodControlAllowedOrganization(org));
      if (pQry.list().isEmpty()) {
        throw new OBException(OBMessageUtils.parseTranslation("@PeriodNotAvailable@"));
      }
    }
  }

  private boolean checkStock(Product product, Organization org) {
    if (!product.getClient().getClientInformationList().get(0).isAllowNegativeStock()) {
      return true;
    }
    StringBuffer where = new StringBuffer();
    where.append(StorageDetail.PROPERTY_PRODUCT + " :product ");
    where.append(" and " + StorageDetail.PROPERTY_ORGANIZATION + " :org ");
    where.append(" and (" + StorageDetail.PROPERTY_QUANTITYONHAND + " < 0");
    where.append("   or " + StorageDetail.PROPERTY_ONHANDORDERQUANITY + " < 0");
    where.append("   )");
    OBQuery<StorageDetail> sdQry = OBDal.getInstance().createQuery(StorageDetail.class,
        where.toString());
    sdQry.setNamedParameter("product", product);
    sdQry.setNamedParameter("org", org);
    if (sdQry.count() > 0) {
      return false;
    }
    return true;
  }
}
