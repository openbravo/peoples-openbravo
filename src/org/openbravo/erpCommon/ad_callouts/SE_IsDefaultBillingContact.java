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
 * All portions are Copyright (C) 2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_callouts;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;

public class SE_IsDefaultBillingContact extends SimpleCallout {
  private static final long serialVersionUID = 1L;
  private static final RequestFilter filterYesNo = new ValueListFilter("Y", "N");

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String lastFieldChanged = info.getStringParameter("inpLastFieldChanged", null);
    final String strValue = info.getStringParameter(lastFieldChanged, filterYesNo);
    if ("inpisbillingcontact".equals(lastFieldChanged)) {
      if ("N".equals(strValue)) {
        info.addResult("inpisdefaultbillingcontact", "N");
      }
    } else if ("inpisdefaultbillingcontact".equals(lastFieldChanged)) {
      if ("Y".equals(strValue)) {
        final String cbpartnerId = info.getStringParameter("inpcBpartnerId", IsIDFilter.instance);
        final String adUserId = info.getStringParameter("inpadUserId", IsIDFilter.instance);

        OBContext.setAdminMode(true);
        try {
          final User defaultBillingContact = getDefaultBillingContacts(cbpartnerId, adUserId);
          if (defaultBillingContact != null) {
            final String msg = String.format(
                OBMessageUtils.messageBD("DuplicatedBillingContactDefaults"),
                defaultBillingContact.getIdentifier());
            info.addResult("ERROR", msg);
            info.addResult(info.getLastFieldChanged(), "N");
          }
        } finally {
          OBContext.restorePreviousMode();
        }
      }
    } else {
      log4j.error("SE_IsDefaultBillingContact doesn't work for column: " + lastFieldChanged);
    }
  }

  protected User getDefaultBillingContacts(final String cbpartnerId, final String currentUserId) {
    final StringBuilder hql = new StringBuilder();
    hql.append(" as u ");
    hql.append("  where u.businessPartner.id = :c_bpartner_id ");
    hql.append("  and u.id != :current_user_id");
    hql.append("  and u.isBillingContact = true ");
    hql.append("  and u.isDefaultBillingContact = true ");

    final OBQuery<User> query = OBDal.getInstance().createQuery(User.class, hql.toString());
    query.setNamedParameter("c_bpartner_id", cbpartnerId);
    query.setNamedParameter("current_user_id", currentUserId);
    query.setMaxResult(1);
    return query.uniqueResult();
  }
}
