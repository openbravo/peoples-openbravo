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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.financial;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.model.ad.datamodel.Table;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.calendar.Period;

public class ResetAccounting {
  final static int FETCH_SIZE = 1000;
  static Logger log4j = Logger.getLogger(ResetAccounting.class);

  @SuppressWarnings("unchecked")
  public static HashMap<String, Integer> delete(String adClientId, String adOrgId,
      String adTableId, String recordId, String strdatefrom, String strdateto) throws OBException {
    if (recordId == null) {
      recordId = "";
    }
    Long totalProcess = System.currentTimeMillis();
    Long inicio = 0l;
    Long fin = 0l;
    Long totalselect = 0l;
    int deleted = 0;
    int updated = 0;
    HashMap<String, Integer> results = new HashMap<String, Integer>();
    results.put("deleted", new Integer(0));
    results.put("updated", new Integer(0));
    results.put("totaldeleted", new Integer(0));
    results.put("totalupdated", new Integer(0));
    List<String> clients = getClients(adClientId);
    List<String> tables = getTables(adTableId);
    try {
      for (String client : clients) {
        Set<String> orgIds = new OrganizationStructureProvider().getNaturalTree(adOrgId);
        for (String table : tables) {
          List<String> docbasetypes = getDocbasetypes(client, table, recordId);
          String myQuery = "select distinct e.recordID from FinancialMgmtAccountingFact e where e.organization.id in (:orgIds) and e.client.id = :clientId and e.table.id = :tableId";
          if (recordId != null && !"".equals(recordId)) {
            myQuery = myQuery + " and e.recordID = :recordId ";
          }
          for (String dbt : docbasetypes) {
            List<Date[]> periods = new ArrayList<Date[]>();
            String calendarId = getCalendarId(adOrgId);
            periods = getPeriodsDates(getOpenPeriods(client, dbt, orgIds, calendarId, table,
                recordId, strdatefrom, strdateto));
            int docUpdated = 0;
            int docDeleted = 0;
            for (Date[] p : periods) {
              StringBuffer consDate = new StringBuffer();
              consDate.append(" and e.documentCategory = '" + dbt + "'");
              consDate.append(" and e.accountingDate >= :dateFrom and e.accountingDate <= :dateTo");
              String exceptionsSql = myQuery + consDate.toString();
              consDate
                  .append(" and not exists (select a from FinancialMgmtAccountingFact a where a.recordID = e.recordID and a.table.id = e.table.id and (a.accountingDate < :dateFrom or a.accountingDate > :dateTo))");
              final Query query = OBDal.getInstance().getSession()
                  .createQuery(myQuery + consDate.toString());
              if (recordId != null && !"".equals(recordId)) {
                query.setString("recordId", recordId);
              }
              query.setParameterList("orgIds", orgIds);
              query.setString("clientId", client);
              query.setString("tableId", table);
              query.setDate("dateFrom", p[0]);
              query.setDate("dateTo", p[1]);
              if (recordId != null && !"".equals(recordId)) {
                query.setFetchSize(1);
              } else {
                query.setFetchSize(FETCH_SIZE);
              }
              inicio = System.currentTimeMillis();
              List<String> transactions = query.list();
              fin = System.currentTimeMillis();
              totalselect = totalselect + fin - inicio;
              while (transactions.size() > 0) {
                HashMap<String, Integer> partial = delete(transactions, table, client);
                deleted = deleted + partial.get("deleted");
                updated = updated + partial.get("updated");
                docUpdated = docUpdated + partial.get("updated");
                docDeleted = docDeleted + partial.get("deleted");
                inicio = System.currentTimeMillis();
                transactions = query.list();
                fin = System.currentTimeMillis();
                totalselect = totalselect + fin - inicio;
              }
              // Documents with postings in different periods are treated separately to validate all
              // dates are within an open period
              HashMap<String, Integer> partial = treatExceptions(exceptionsSql, recordId, table,
                  orgIds, client, p[0], p[1], calendarId, strdatefrom, strdateto);
              deleted = deleted + partial.get("deleted");
              updated = updated + partial.get("updated");
              docUpdated = docUpdated + partial.get("updated");
              docDeleted = docDeleted + partial.get("deleted");
            }
            log4j.debug("docBaseType: " + dbt);
            log4j.debug("updated: " + docUpdated);
            log4j.debug("deleted: " + docDeleted);
          }
        }
      }
    } catch (OBException e) {
      throw e;
    } catch (Exception e) {
      throw new OBException("Delete failed", e);
    }
    results.put("deleted", deleted);
    results.put("updated", updated);
    log4j.debug("total totalProcess (milies): " + (System.currentTimeMillis() - totalProcess));
    if (recordId != null && !"".equals(recordId) && deleted == 0 && updated == 0) {
      throw new OBException("@PeriodClosedForUnPosting@");
    }
    return results;
  }

  private static HashMap<String, Integer> delete(List<String> transactions, String tableId,
      String client) throws OBException {
    HashMap<String, Integer> result = new HashMap<String, Integer>();
    if (transactions.size() == 0) {
      result.put("deleted", new Integer(0));
      result.put("updated", new Integer(0));
      return result;
    }
    String tableName = "";
    OBContext.setAdminMode(false);
    try {
      Table table = OBDal.getInstance().get(Table.class, tableId);
      tableName = table.getName();
      String strUpdate = "update "
          + tableName
          + " set posted='N', processNow=false where (posted<>'N' or posted is null or processNow = false) and id in (:transactions) ";
      String strDelete = "delete from FinancialMgmtAccountingFact where table.id = :tableId and recordID in (:transactions) and client.id=:clientId";
      final Query update = OBDal.getInstance().getSession().createQuery(strUpdate);
      update.setParameterList("transactions", transactions);
      int updated = update.executeUpdate();
      final Query delete = OBDal.getInstance().getSession().createQuery(strDelete);
      delete.setString("tableId", tableId);
      delete.setParameterList("transactions", transactions);
      delete.setString("clientId", client);
      int deleted = delete.executeUpdate();
      result.put("deleted", new Integer(deleted));
      result.put("updated", new Integer(updated));
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().getSession().clear();
      return result;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException("Error Deleting Accounting", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  public static HashMap<String, Integer> restore(String clientId, String adOrgId, String datefrom,
      String dateto) throws OBException {
    HashMap<String, Integer> results = new HashMap<String, Integer>();
    results.put("deleted", new Integer(0));
    results.put("updated", new Integer(0));
    List<String> tableIds = getActiveTables(clientId, adOrgId);
    for (String tableId : tableIds) {
      HashMap<String, Integer> partial = restore(clientId, adOrgId, tableId, datefrom, dateto);
      results.put("deleted", results.get("deleted") + partial.get("deleted"));
      results.put("updated", results.get("updated") + partial.get("updated"));

    }
    log4j.debug("deleted: " + results.get("deleted"));
    log4j.debug("updated: " + results.get("updated"));
    return results;
  }

  public static HashMap<String, Integer> restore(String clientId, String adOrgId, String tableId,
      String datefrom, String dateto) throws OBException {
    HashMap<String, Integer> results = new HashMap<String, Integer>();
    results.put("deleted", new Integer(0));
    results.put("updated", new Integer(0));
    String tableName = "";
    String tableDate = "";
    OBContext.setAdminMode(false);
    try {
      Table table = OBDal.getInstance().get(Table.class, tableId);
      tableName = table.getName();
      tableDate = lowerCaseFirst(camelCaseIt(table.getAcctdateColumn().getName()));
      String strUpdate = "update "
          + tableName
          + " set posted='N', processNow=false where posted not in ('N','Y') and processed = 'Y' and organization.id in (:orgIds)  ";
      if (!("".equals(datefrom))) {
        strUpdate = strUpdate + " and " + tableDate + " >= :dateFrom ";
      }
      if (!("".equals(dateto))) {
        strUpdate = strUpdate + " and " + tableDate + " <= :dateTo ";
      }

      Query update = OBDal.getInstance().getSession().createQuery(strUpdate);
      update
          .setParameterList("orgIds", new OrganizationStructureProvider().getNaturalTree(adOrgId));
      try {
        if (!("".equals(datefrom))) {
          update.setDate("dateFrom", OBDateUtils.getDate(datefrom));
        }
        if (!("".equals(dateto))) {
          update.setDate("dateTo", OBDateUtils.getDate(dateto));
        }
      } catch (ParseException e) {
        // TODO Auto-generated catch block
        log4j.debug(e);
      }

      int updated = update.executeUpdate();
      results.put("updated", new Integer(updated));
      OBDal.getInstance().getConnection().commit();
      OBDal.getInstance().getSession().clear();
      return results;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      throw new OBException("Error Reseting Accounting", e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unchecked")
  private static List<String> getClients(String clientId) {
    String myQuery = "select e.id from ADClient e where e.id = :clientId";
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    query.setString("clientId", clientId);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getTables(String adTableId) {
    OBContext.setAdminMode(false);
    try {
      List<String> accountingTables = new ArrayList<String>();
      if (!"".equals(adTableId)) {
        Table myTable = OBDal.getInstance().get(Table.class, adTableId);
        accountingTables.add(myTable.getId());
        return accountingTables;
      }
      String myQuery = "select distinct e.table.id from FinancialMgmtAccountingFact e where e.table.id <> '145'";
      accountingTables = OBDal.getInstance().getSession().createQuery(myQuery).list();
      return accountingTables;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @SuppressWarnings("unused")
  private static List<Organization> getOrganizations(Client client, Set<String> orgIds) {
    final String CLIENT_SYSTEM = "0";
    OBCriteria<Organization> obc = OBDal.getInstance().createCriteria(Organization.class);
    if (!CLIENT_SYSTEM.equals(client.getId())) {
      obc.add(Restrictions.eq(Organization.PROPERTY_CLIENT, client));
    }
    obc.add(Restrictions.in(Organization.PROPERTY_ID, orgIds));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    return obc.list();
  }

  private static List<String> getDocbasetypes(String clientId, String tableId, String recordId) {
    String myQuery = "select distinct e.documentCategory from FinancialMgmtAccountingFact e where e.client.id = :clientId and e.table.id = :tableId ";
    if (!"".equals(recordId)) {
      myQuery = myQuery + "and e.recordID=:recordId";
    }
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    query.setString("clientId", clientId);
    query.setString("tableId", tableId);
    if (!"".equals(recordId)) {
      query.setString("recordId", recordId);
      query.setFetchSize(1);
    }
    @SuppressWarnings("unchecked")
    List<String> docbasetypes = query.list();
    return docbasetypes;
  }

  @SuppressWarnings("unchecked")
  private static List<Period> getOpenPeriods(String clientId, String docBaseType,
      Set<String> orgIds, String calendarId, String tableId, String recordId, String datefrom,
      String dateto) {
    if (!"".equals(recordId)) {
      List<Period> periods = new ArrayList<Period>();
      periods.add(getDocumentPeriod(clientId, tableId, recordId, docBaseType));
      return periods;
    }
    String myQuery = "select distinct p from FinancialMgmtPeriodControl e left join e.period p left join p.year y left join y.calendar c where c.id = :calendarId and e.client.id = :clientId and e.documentCategory = :docbasetype and e.periodStatus = 'O'";

    if (!("".equals(datefrom))) {
      myQuery = myQuery + " and p.startingDate >= :dateFrom";
    }
    if (!("".equals(dateto))) {
      myQuery = myQuery + " and p.endingDate <=  :dateTo";
    }
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    // TODO: Review orgIds
    // query.setParameterList("orgIds", orgIds);
    query.setString("calendarId", calendarId);
    query.setString("clientId", clientId);
    query.setString("docbasetype", docBaseType);
    try {
      if (!("".equals(datefrom))) {
        query.setDate("dateFrom", OBDateUtils.getDate(datefrom));
      }
      if (!("".equals(dateto))) {
        query.setDate("dateTo", OBDateUtils.getDate(dateto));
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      log4j.debug(e);
    }
    return query.list();
  }

  private static Period getDocumentPeriod(String clientId, String tableId, String recordId,
      String docBaseType) {
    String myQuery = "select distinct e.period from FinancialMgmtAccountingFact e , FinancialMgmtPeriodControl p where p.period=e.period and p.periodStatus = 'O' and e.client.id = :clientId and e.table.id = :tableId and e.recordID=:recordId and p.documentCategory = :docbasetype";
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    query.setString("clientId", clientId);
    query.setString("tableId", tableId);
    query.setString("recordId", recordId);
    query.setString("docbasetype", docBaseType);
    query.setFetchSize(1);
    @SuppressWarnings("unchecked")
    List<Period> periods = query.list();
    if (periods.isEmpty()) {
      throw new OBException("@PeriodClosedForUnPosting@");
    }
    return periods.get(0);
  }

  private static List<Date[]> getPeriodsDates(List<Period> periods) {
    List<Date[]> result = new ArrayList<Date[]>();
    for (Period period : periods) {
      Date[] dates = new Date[2];
      dates[0] = period.getStartingDate();
      dates[1] = period.getEndingDate();
      result.add(dates);
    }
    return result;
  }

  private static String getCalendarId(String adOrgId) {
    Organization organization = OBDal.getInstance().get(Organization.class, adOrgId);
    if (organization.getCalendar() != null) {
      return organization.getCalendar().getId();
    } else {
      return getCalendarId(new OrganizationStructureProvider().getParentOrg(adOrgId));
    }
  }

  @SuppressWarnings("unchecked")
  private static List<String> getActiveTables(String clientId, String adOrgId) {
    String myQuery = "select distinct table.id from FinancialMgmtAcctSchemaTable where accountingSchema.id in (:accountingSchemaIds) and client.id = :clientId and active= true";
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    List<String> accountingSchemaIds = getAccountingSchemaIds(clientId, adOrgId);
    query.setParameterList("accountingSchemaIds", accountingSchemaIds);
    query.setString("clientId", clientId);
    return query.list();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getAccountingSchemaIds(String clientId, String orgIg) {
    String myQuery = "select distinct accountingSchema.id from OrganizationAcctSchema where client.id = :clientId and active= true and organization.id in (:orgIds)";
    Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    query.setString("clientId", clientId);
    query.setParameterList("orgIds", new OrganizationStructureProvider().getNaturalTree(orgIg));
    return query.list();

  }

  /**
   * 
   * @param myQuery
   * @param recordId
   * @param table
   * @param orgIds
   * @param client
   * @param dateFrom
   * @param dateTo
   */
  private static HashMap<String, Integer> treatExceptions(String myQuery, String recordId,
      String table, Set<String> orgIds, String client, Date dateFrom, Date dateTo,
      String calendarId, String datefrom, String dateto) {
    HashMap<String, Integer> results = new HashMap<String, Integer>();
    results.put("deleted", 0);
    results.put("updated", 0);
    final Query query = OBDal.getInstance().getSession().createQuery(myQuery);
    if (recordId != null && !"".equals(recordId)) {
      query.setString("recordId", recordId);
    }
    query.setParameterList("orgIds", orgIds);
    query.setString("clientId", client);
    query.setString("tableId", table);
    query.setDate("dateFrom", dateFrom);
    query.setDate("dateTo", dateTo);
    if (recordId != null && !"".equals(recordId)) {
      query.setFetchSize(1);
    }
    @SuppressWarnings("unchecked")
    List<String> transactions = query.list();
    for (String transaction : transactions) {
      OBCriteria<AccountingFact> factCrit = OBDal.getInstance()
          .createCriteria(AccountingFact.class);
      factCrit.add(Restrictions.eq(AccountingFact.PROPERTY_RECORDID, transaction));
      factCrit.add(Restrictions.eq(AccountingFact.PROPERTY_TABLE,
          OBDal.getInstance().get(Table.class, table)));
      factCrit.add(Restrictions.eq(AccountingFact.PROPERTY_CLIENT,
          OBDal.getInstance().get(Client.class, client)));
      List<AccountingFact> facts = factCrit.list();
      Set<Date> exceptionDates = new HashSet<Date>();
      for (AccountingFact fact : facts) {
        if (dateFrom.compareTo(fact.getAccountingDate()) != 0
            || dateTo.compareTo(fact.getAccountingDate()) != 0) {
          exceptionDates.add(fact.getAccountingDate());
        }
      }
      if (checkDates(exceptionDates, client, orgIds, facts.get(0).getDocumentCategory(),
          calendarId, datefrom, dateto)) {
        List<String> toDelete = new ArrayList<String>();
        toDelete.add(transaction);
        results = delete(toDelete, table, client);
      } else {
        if (recordId != null && !"".equals(recordId)) {
          throw new OBException("@PeriodClosedForUnPosting@");
        }
      }
    }
    return results;
  }

  private static boolean checkDates(Set<Date> exceptionDates, String clientId, Set<String> orgIds,
      String documentCategory, String calendarId, String datefrom, String dateto) {
    List<Period> openPeriods = getOpenPeriods(clientId, documentCategory, orgIds, calendarId, "",
        "", datefrom, dateto);
    int validDates = 0;
    for (Period period : openPeriods) {
      for (Date date : exceptionDates) {
        if (date.compareTo(period.getStartingDate()) >= 0
            && date.compareTo(period.getEndingDate()) <= 0) {
          validDates++;
        }
      }
    }
    if (exceptionDates.size() != validDates) {
      return false;
    }
    return true;
  }

  private static String camelCaseIt(String mappingName) {
    String localMappingName = mappingName.replaceAll("_", " ");
    String separator = " ";
    // strip _ at the end
    while (localMappingName.endsWith(separator)) {
      localMappingName = localMappingName.substring(0, localMappingName.length() - 1);
    }
    // strip _ at the beginning
    while (localMappingName.startsWith(separator)) {
      localMappingName = localMappingName.substring(1);
    }

    // "CamelCasing"
    int pos = localMappingName.indexOf(separator);
    while (pos != -1) {
      final String leftPart = localMappingName.substring(0, pos);
      final String camelLetter = String.valueOf(localMappingName.charAt(pos + 1)).toUpperCase();
      final String rightPart = localMappingName.substring(pos + 2);
      localMappingName = leftPart + camelLetter + rightPart;
      pos = localMappingName.indexOf(separator);
    }
    return localMappingName;
  }

  private static String lowerCaseFirst(String value) {
    if (value.length() > 1) {
      return value.substring(0, 1).toLowerCase() + value.substring(1);
    }
    return value;
  }

}
