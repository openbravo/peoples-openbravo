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
 * All portions are Copyright (C) 2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.utility;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;

/**
 * Utilities to get AcctSchema
 */
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
    try {
      OBContext.setAdminMode(true);

      if (StringUtils.isBlank(orgId)) {
        // No organization
        return null;
      }
      final Organization org = OBDal.getInstance().get(Organization.class, orgId);
      if (org == null) {
        // No organization
        return null;
      }
      String acctSchemaId = getOrgLedgerRecursive(orgId);
      if (!StringUtils.isEmpty(acctSchemaId)) {
        // Get ledger of organization tree
        return acctSchemaId;
      }
      String clientId = StringUtils.equals(orgId, "0") ? OBContext.getOBContext()
          .getCurrentClient().getId() : org.getClient().getId();
      // Get client base Ledger
      return getClientLedger(clientId);

    } catch (Exception e) {
      log4j.error("Impossible to get ledger for organization id " + orgId, e);
    } finally {
      OBContext.restorePreviousMode();
    }

    return null;
  }

  private static String getOrgLedgerRecursive(String orgId) {
    try {
      OBContext.setAdminMode(true);
      StringBuffer where = new StringBuffer();
      where.append(" select " + Organization.PROPERTY_GENERALLEDGER + ".id");
      where.append(" from " + Organization.ENTITY_NAME);
      where.append(" where ad_isorgincluded(:orgId, " + Organization.PROPERTY_ID + ", "
          + Organization.PROPERTY_CLIENT + ".id) <> -1");
      where.append(" and " + Organization.PROPERTY_GENERALLEDGER + " is not null");
      where.append(" order by ad_isorgincluded(:orgId, " + Organization.PROPERTY_ID + ", "
          + Organization.PROPERTY_CLIENT + ".id)");
      Query qry = OBDal.getInstance().getSession().createQuery(where.toString());
      qry.setParameter("orgId", orgId);
      qry.setMaxResults(1);
      return (String) qry.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private static String getClientLedger(String clientId) {
    try {
      OBContext.setAdminMode(true);
      StringBuffer where = new StringBuffer();
      where.append(" select " + AcctSchema.PROPERTY_ID);
      where.append(" from " + AcctSchema.ENTITY_NAME);
      where.append(" where " + AcctSchema.PROPERTY_CLIENT + ".id = :clientId");
      where.append(" order by " + AcctSchema.PROPERTY_NAME);
      Query qry = OBDal.getInstance().getSession().createQuery(where.toString());
      qry.setParameter("clientId", clientId);
      qry.setMaxResults(1);
      return (String) qry.uniqueResult();
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
