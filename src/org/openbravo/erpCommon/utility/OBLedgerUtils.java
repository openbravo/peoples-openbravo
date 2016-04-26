package org.openbravo.erpCommon.utility;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;

public class OBLedgerUtils {
  private static Logger log4j = Logger.getLogger(OBLedgerUtils.class);

  /**
   * Returns the ledger id for the given organization id.
   * 
   * If the org id is empty, it returns null. If the given organization has no ledger, it tries to
   * get its legal entity's ledger. If not found, it returns the organization client's ledger
   * 
   * @param orgId
   *          Organization Id whose ledger is needed
   * 
   * @return String ledgerId ledger id for the given organization. Null if not found
   */
  public static String getOrgLedger(String orgId) {
    if (StringUtils.isBlank(orgId)) {
      return null;
    }

    OBContext.setAdminMode(true);
    try {
      final Organization org = OBDal.getInstance().get(Organization.class, orgId);
      if (org == null) {
        // No organization
        return null;
      } else {
        AcctSchema acctSchema = getLedgerRecursive(org);
        if (acctSchema != null) {
          return acctSchema.getId();
        } else {
          // Get client base Ledger
          String clientId = StringUtils.equals(orgId, "0")
              ? OBContext.getOBContext().getCurrentClient().getId() : org.getClient().getId();
          return getClientBaseLedger(clientId);
        }
      }
    } catch (Exception e) {
      log4j.error("Impossible to get ledger for organization id " + orgId, e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  private static String getClientBaseLedger(String clientId) {
    String qryString = " select id  from FinancialMgmtAcctSchema where client.id='" + clientId
        + "'";
    Query qry = OBDal.getInstance().getSession().createQuery(qryString);
    @SuppressWarnings("unchecked")
    List<String> qryList = qry.list();
    if (qryList != null && qryList.size() > 0) {
      return qryList.get(0);
    } else {
      return null;

    }
  }

  private static AcctSchema getLedgerRecursive(Organization adOrg) {
    if (adOrg != null) {
      AcctSchema acctSchema = adOrg.getGeneralLedger();
      if (acctSchema != null) {
        return acctSchema;
      } else {
        if (!adOrg.getId().equals("0")) {
          return getLedgerRecursive(
              OBContext.getOBContext().getOrganizationStructureProvider().getParentOrg(adOrg));
        }

      }
    }
    return null;
  }
}
