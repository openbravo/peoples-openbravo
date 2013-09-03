package org.openbravo.materialmgmt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.QueryTimeoutException;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.exception.GenericJDBCException;
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
  private static final Logger log4j = Logger.getLogger(InventoryCountProcess.class);

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(OBMessageUtils.messageBD("Success"));

    try {
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

      OBContext.setAdminMode(false);
      try {
        msg = processInventory(inventory);
      } finally {
        OBContext.restorePreviousMode();
      }

      inventory.setProcessNow(false);

      OBDal.getInstance().save(inventory);
      OBDal.getInstance().flush();

      bundle.setResult(msg);

      // Postgres wraps the exception into a GenericJDBCException
    } catch (GenericJDBCException ge) {
      log4j.error("Exception processing physical inventory", ge);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((GenericJDBCException) ge).getSQLException().getMessage());
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
      final String recordID = (String) bundle.getParams().get("M_Inventory_ID");
      final InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, recordID);
      inventory.setProcessNow(false);
      OBDal.getInstance().save(inventory);
      // Oracle wraps the exception into a QueryTimeoutException
    } catch (QueryTimeoutException qte) {
      log4j.error("Exception processing physical inventory", qte);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
          .getLanguage()));
      msg.setMessage(((QueryTimeoutException) qte).getSQLException().getMessage().split("\n")[0]);
      bundle.setResult(msg);
      OBDal.getInstance().rollbackAndClose();
      final String recordID = (String) bundle.getParams().get("M_Inventory_ID");
      final InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, recordID);
      inventory.setProcessNow(false);
      OBDal.getInstance().save(inventory);
    } catch (final Exception e) {
      log4j.error("Exception processing physical inventory", e);
      msg.setType("Error");
      msg.setTitle(OBMessageUtils.messageBD(bundle.getConnection(), "Error", bundle.getContext()
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
    Map<String, SQLFunction> function = dialect.getFunctions();
    if (!function.containsKey("get_uuid")) {
      dialect.getFunctions().put("get_uuid", new StandardSQLFunction("get_uuid", new StringType()));
    }
    if (!function.containsKey("now")) {
      dialect.getFunctions().put("now", new StandardSQLFunction("now", new DateType()));
    }
    if (!function.containsKey("to_date")) {
      dialect.getFunctions().put("to_date", new StandardSQLFunction("to_date", new DateType()));
    }
    if (!function.containsKey("to_timestamp")) {
      dialect.getFunctions().put("to_timestamp",
          new StandardSQLFunction("to_timestamp", new DateType()));
    }
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
    insert.append(", " + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
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
    insert.append(", to_timestamp(to_char(:currentDate), to_char('DD-MM-YYYY HH24:MI:SS'))");
    insert.append(" \nfrom " + InventoryCountLine.ENTITY_NAME + " as e");
    insert.append(" , " + User.ENTITY_NAME + " as u");
    insert.append(" , " + AttributeSetInstance.ENTITY_NAME + " as asi");
    insert.append(" , " + Product.ENTITY_NAME + " as p");
    insert.append(" \nwhere e." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inv");
    insert.append(" and u.id = :user");
    insert.append(" and asi.id = COALESCE(e." + InventoryCountLine.PROPERTY_ATTRIBUTESETVALUE
        + ".id , '0')");
    // Non Stockable Products should not generate warehouse transactions
    insert.append(" and e.product.id = p.id and p.stocked = 'Y' and p.productType = 'I'");

    Query queryInsert = OBDal.getInstance().getSession().createQuery(insert.toString());
    queryInsert.setString("inv", inventory.getId());
    queryInsert.setString("user", (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    queryInsert.setString("currentDate", dateFormatter.format(new Date()));
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
    iclQry.setMaxResult(1);
    Object icl = iclQry.uniqueResult();
    if (icl != null) {
      throw new OBException(OBMessageUtils.parseTranslation("@Inline@ "
          + ((InventoryCountLine) icl).getLineNo() + " @productWithoutAttributeSet@"));
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
    where.append("         and coalesce(icl." + InventoryCountLine.PROPERTY_ORDERUOM
        + ", '0') = coalesce(icl2." + InventoryCountLine.PROPERTY_ORDERUOM + ", '0')");
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
    List<InventoryCountLine> iclList = iclQry.list();
    if (!iclList.isEmpty()) {
      String lines = "";
      for (InventoryCountLine icl2 : iclList) {
        lines += icl2.getLineNo().toString() + ", ";
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
    iclList = iclQry.list();
    if (!iclList.isEmpty()) {
      for (InventoryCountLine icl2 : iclList) {
        if (!headerLEorBU.getId().equals(
            DalUtil.getId(osp.getLegalEntityOrBusinessUnit(icl2.getOrganization())))) {
          throw new OBException(OBMessageUtils.parseTranslation("@LinesAndHeaderDifferentLEorBU@"));
        }
      }
    }
    if (headerLEorBU.getOrganizationType().isLegalEntityWithAccounting()) {
      where = new StringBuffer();
      where.append(" as pc ");
      where.append("   join pc." + PeriodControl.PROPERTY_PERIOD + " as p");
      where.append(" where p." + Period.PROPERTY_STARTINGDATE + " <= :dateStarting");
      where.append("   and p." + Period.PROPERTY_ENDINGDATE + " >= :dateEnding");
      where.append("   and pc." + PeriodControl.PROPERTY_DOCUMENTCATEGORY + " = 'MMI' ");
      where.append("   and pc." + PeriodControl.PROPERTY_ORGANIZATION + ".id = :org");
      where.append("   and pc." + PeriodControl.PROPERTY_PERIODSTATUS + " = 'O'");
      OBQuery<PeriodControl> pQry = OBDal.getInstance().createQuery(PeriodControl.class,
          where.toString());
      pQry.setFilterOnReadableClients(false);
      pQry.setFilterOnReadableOrganization(false);
      pQry.setNamedParameter("dateStarting", inventory.getMovementDate());
      pQry.setNamedParameter("dateEnding",
          DateUtils.truncate(inventory.getMovementDate(), Calendar.DATE));
      pQry.setNamedParameter("org", osp.getPeriodControlAllowedOrganization(org).getId());
      pQry.setMaxResult(1);
      if (pQry.uniqueResult() == null) {
        throw new OBException(OBMessageUtils.parseTranslation("@PeriodNotAvailable@"));
      }
    }
  }

  private void checkStock(InventoryCount inventory) {
    StringBuffer where = new StringBuffer();
    where.append(" as icl");
    where.append(" , " + StorageDetail.ENTITY_NAME + " as sd");
    where.append(" where icl." + InventoryCountLine.PROPERTY_PHYSINVENTORY + ".id = :inv");
    where.append("   and sd." + StorageDetail.PROPERTY_PRODUCT + " = icl."
        + InventoryCountLine.PROPERTY_PRODUCT);
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
