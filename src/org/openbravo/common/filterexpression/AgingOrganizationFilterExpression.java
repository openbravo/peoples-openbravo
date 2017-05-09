/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.common.filterexpression;

import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.openbravo.client.application.FilterExpression;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.enterprise.Organization;

public class AgingOrganizationFilterExpression implements FilterExpression {

  @SuppressWarnings("unchecked")
  @Override
  public String getExpression(Map<String, String> requestMap) {

    Organization org = OBContext.getOBContext().getCurrentOrganization();

    StringBuilder hqlQuery = new StringBuilder("select o.id from Organization o ");
    hqlQuery
        .append("where exists (select 1 from OrganizationType ot where o.organizationType = ot and ot.transactionsAllowed = true) and o.ready = true ");
    hqlQuery.append("order by o.name asc");

    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(hqlQuery.toString());

    List<String> orgList = (List<String>) query.list();

    if (orgList.contains(org.getId())) {
      return org.getId();
    } else {
      if (!orgList.isEmpty()) {
        return orgList.get(0);
      }
    }

    return null;
  }

}
