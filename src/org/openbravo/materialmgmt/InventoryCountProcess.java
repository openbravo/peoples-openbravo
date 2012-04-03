package org.openbravo.materialmgmt;

import java.util.Calendar;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.type.DateType;
import org.hibernate.type.StringType;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.SessionHandler;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
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
      // final String strAction = (String) bundle.getParams().get("action");

      // retrieve standard params
      final String recordID = (String) bundle.getParams().get("M_Inventory_ID");
      final InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, recordID);

      // lock inventory
      if (inventory.isProcessNow()) {
        throw new OBException(OBMessageUtils.parseTranslation("@OtherProcessActive@"));
      }
      inventory.setProcessNow(true);
      OBDal.getInstance().save(inventory);
      if (SessionHandler.isSessionHandlerPresent()) {
        SessionHandler.getInstance().commitAndStart();
      }

      // if ("CO".equals(strAction)) {
      OBContext.setAdminMode(false);
      try {
        msg = processInventory(inventory);
      } finally {
        OBContext.restorePreviousMode();
      }

      // }
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
      final String recordID = (String) bundle.getParams().get("M_Inventory_ID");
      final InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, recordID);
      inventory.setProcessNow(false);
      OBDal.getInstance().save(inventory);
    }

  }

  public OBError processInventory(InventoryCount inventory) throws OBException {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));
    runChecks(inventory);

    // In case get_uuid is not already registered, it's registered now.
    final Dialect dialect = ((SessionFactoryImpl) ((SessionImpl) OBDal.getInstance().getSession())
        .getSessionFactory()).getDialect();
    dialect.getFunctions().put("get_uuid", new StandardSQLFunction("get_uuid", new StringType()));
    dialect.getFunctions().put("now", new StandardSQLFunction("now", new DateType()));

    StringBuffer insert = new StringBuffer();
    insert.append("insert into " + MaterialTransaction.ENTITY_NAME + "(");
    insert.append(" id ");
    insert.append(", " + MaterialTransaction.PROPERTY_ACTIVE);
    insert.append(", " + MaterialTransaction.PROPERTY_CLIENT);
    insert.append(", " + MaterialTransaction.PROPERTY_ORGANIZATION);
    insert.append(", " + MaterialTransaction.PROPERTY_CREATIONDATE);
    insert.append(", " + MaterialTransaction.PROPERTY_CREATEDBY);
    insert.append(", " + MaterialTransaction.PROPERTY_UPDATED);
    insert.append(", " + MaterialTransaction.PROPERTY_UPDATEDBY);
    insert.append(", " + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
    insert.append(", " + MaterialTransaction.PROPERTY_MOVEMENTDATE);
    insert.append(", " + MaterialTransaction.PROPERTY_STORAGEBIN);
    insert.append(", " + MaterialTransaction.PROPERTY_PRODUCT);
    insert.append(", " + MaterialTransaction.PROPERTY_ATTRIBUTESETVALUE);
    insert.append(", " + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY);
    insert.append(", " + MaterialTransaction.PROPERTY_UOM);
    insert.append(", " + MaterialTransaction.PROPERTY_ORDERQUANTITY);
    insert.append(", " + MaterialTransaction.PROPERTY_ORDERUOM);
    insert.append(", " + MaterialTransaction.PROPERTY_PHYSICALINVENTORYLINE);
    // select from inventory line
    insert.append(" ) \n select get_uuid() ");
    insert.append(", e." + InventoryCountLine.PROPERTY_ACTIVE);
    insert.append(", e." + InventoryCountLine.PROPERTY_CLIENT);
    insert.append(", e." + InventoryCountLine.PROPERTY_ORGANIZATION);
    insert.append(", now()");
    insert.append(", u");
    insert.append(", now()");
    insert.append(", u");
    insert.append(", 'I+'");
    insert.append(", e." + InventoryCountLine.PROPERTY_PHYSINVENTORY + "."
        + InventoryCount.PROPERTY_MOVEMENTDATE);
    insert.append(", e." + InventoryCountLine.PROPERTY_STORAGEBIN);
    insert.append(", e." + InventoryCountLine.PROPERTY_PRODUCT);
    insert.append(", asi");
    insert.append(", e." + InventoryCountLine.PROPERTY_QUANTITYCOUNT + " - COALESCE(" + "e."
        + InventoryCountLine.PROPERTY_BOOKQUANTITY + ", 0)");
    insert.append(", e." + InventoryCountLine.PROPERTY_UOM);
    insert.append(", e." + InventoryCountLine.PROPERTY_ORDERQUANTITY + " - COALESCE(" + "e."
        + InventoryCountLine.PROPERTY_QUANTITYORDERBOOK + ", 0)");
    insert.append(", e." + InventoryCountLine.PROPERTY_ORDERUOM);
    insert.append(", e");
    insert.append(" \nfrom " + InventoryCountLine.ENTITY_NAME + " as e");
    insert.append(" , " + User.ENTITY_NAME + " as u");
    insert.append(" , " + AttributeSetInstance.ENTITY_NAME + " as asi");
    insert.append(" \nwhere e." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inv");
    insert.append(" and u.id = :user");
    insert.append(" and asi.id = COALESCE(e." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ", '0')");

    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    queryInsert.setString("inv", inventory.getId());
    queryInsert.setString("user", (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    queryInsert.executeUpdate();

    if (!inventory.getClient().getClientInformationList().get(0).isAllowNegativeStock()) {
      checkStock(inventory);
    }

    inventory.setProcessed(true);
    return msg;
  }

  private void runChecks(InventoryCount inventory) throws OBException {
    if (inventory.isProcessed()) {
      throw new OBException(OBMessageUtils.parseTranslation("@AlreadyPosted@"));
    }
    // Products without attribute set.
    StringBuffer where = new StringBuffer();
    where.append(" as icl");
    where.append("   join icl." + InventoryCountLine.PROPERTY_PRODUCT + " as p");
    where.append("   join p." + Product.PROPERTY_ATTRIBUTESET + " as aset");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inventory");
    where.append("   and aset." + AttributeSet.PROPERTY_REQUIREATLEASTONEVALUE + " = true");
    where.append("   and coalesce(p." + Product.PROPERTY_ATTRIBUTESETVALUE + ", '-') <> 'F'");
    where.append("   and coalesce(icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ", '0') = '0'");
    where.append("  order by icl." + InventoryCountLine.PROPERTY_LINENO);
    OBQuery<InventoryCountLine> iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class,
        where.toString());
    iclQry.setNamedParameter("inventory", inventory.getId());
    if (!iclQry.list().isEmpty()) {
      throw new OBException(OBMessageUtils.parseTranslation("@Inline@ "
          + iclQry.list().get(0).getLineNo() + " @productWithoutAttributeSet@"));
    }

    // duplicated product
    where = new StringBuffer();
    where.append(" as icl");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inventory");
    where.append("   and exists (select 1 from " + InventoryCountLine.ENTITY_NAME + " as icl2");
    where.append("       where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + " = icl2."
        + InventoryCountLine.PROPERTY_PHYSINVENTORY);
    where.append("         and icl." + InventoryCountLine.PROPERTY_PRODUCT + " = icl2."
        + InventoryCountLine.PROPERTY_PRODUCT);
    where.append("         and coalesce(icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ", '0') = coalesce(icl2." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE + ", '0')");
    where.append("         and icl." + InventoryCountLine.PROPERTY_ORDERUOM + " = icl2."
        + InventoryCountLine.PROPERTY_ORDERUOM);
    where.append("         and icl." + InventoryCountLine.PROPERTY_STORAGEBIN + " = icl2."
        + InventoryCountLine.PROPERTY_STORAGEBIN);
    where.append("         and icl." + InventoryCountLine.PROPERTY_LINENO + " <> icl2."
        + InventoryCountLine.PROPERTY_LINENO + ")");
    where.append(" order by icl." + InventoryCountLine.PROPERTY_PRODUCT);
    where.append(", icl." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE);
    where.append(", icl." + InventoryCountLine.PROPERTY_STORAGEBIN);
    where.append(", icl." + InventoryCountLine.PROPERTY_ORDERUOM);
    where.append(", icl." + InventoryCountLine.PROPERTY_LINENO);
    iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class, where.toString());
    iclQry.setNamedParameter("inventory", inventory.getId());
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
        InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inventory and "
            + InventoryCountLine.PROPERTY_ORGANIZATION + ".id <> :organization");
    iclQry.setNamedParameter("inventory", inventory.getId());
    iclQry.setNamedParameter("organization", org.getId());
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
      where.append(" where p." + Period.PROPERTY_STARTINGDATE + " <= :dateStarting");
      where.append(" and p." + Period.PROPERTY_ENDINGDATE + " >= :dateEnding");
      where.append(" and exists (select 1 from " + PeriodControl.ENTITY_NAME + " as pc ");
      where.append("    where p." + Period.PROPERTY_ID + " = pc." + PeriodControl.PROPERTY_PERIOD);
      where.append("      and pc." + PeriodControl.PROPERTY_DOCUMENTCATEGORY + " = 'MMI' ");
      where.append("      and pc." + PeriodControl.PROPERTY_ORGANIZATION + ".id = :org");
      where.append("      and pc." + PeriodControl.PROPERTY_PERIODSTATUS + " = 'O'");
      where.append("            )");
      OBQuery<Period> pQry = OBDal.getInstance().createQuery(Period.class, where.toString());
      pQry.setNamedParameter("dateStarting", inventory.getMovementDate());
      pQry.setNamedParameter("dateEnding",
          DateUtils.truncate(inventory.getMovementDate(), Calendar.DATE));
      pQry.setNamedParameter("org", osp.getPeriodControlAllowedOrganization(org).getId());
      if (pQry.list().isEmpty()) {
        throw new OBException(OBMessageUtils.parseTranslation("@PeriodNotAvailable@"));
      }
    }
  }

  private void checkStock(InventoryCount inventory) {
    StringBuffer where = new StringBuffer();
    where.append(" as icl");
    where.append(" join icl." + InventoryCountLine.PROPERTY_PRODUCT + " as sd");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inv");
    where.append("   and sd." + StorageDetail.PROPERTY_ORGANIZATION + " = icl."
        + InventoryCountLine.PROPERTY_ORGANIZATION);
    where.append("   and (sd." + StorageDetail.PROPERTY_QUANTITYONHAND + " < 0");
    where.append("     or sd." + StorageDetail.PROPERTY_ONHANDORDERQUANITY + " < 0");
    where.append("     )");
    where.append(" order by icl." + InventoryCountLine.PROPERTY_LINENO);
    OBQuery<InventoryCountLine> iclQry = OBDal.getInstance().createQuery(InventoryCountLine.class,
        where.toString());
    iclQry.setNamedParameter("inv", inventory.getId());
    if (iclQry.count() > 0) {
      throw new OBException("@NotEnoughStocked@ @line@ " + iclQry.list().get(0).getLineNo());
    }
  }
}
