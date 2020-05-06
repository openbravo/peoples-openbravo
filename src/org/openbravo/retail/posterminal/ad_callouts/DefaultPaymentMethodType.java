/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.ad_callouts;

import javax.servlet.ServletException;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;

public class DefaultPaymentMethodType extends SimpleCallout {

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    String paymentgroup = info.getStringParameter("inpobposPaymentgroupId");
    String paymentmethodtype = info.getStringParameter("inpobposPaymentmethodTypeId");

    String calculatedpaymentmethodtype = calculatePaymentMethodType(paymentgroup,
        paymentmethodtype);

    info.addResult("inpobposPaymentmethodTypeId", calculatedpaymentmethodtype);
  }

  private String calculatePaymentMethodType(String paymentgroup, String paymentmethodtype) {

    Session session = OBDal.getInstance().getSession();

    if (paymentgroup == null || paymentgroup.isEmpty()) {
      return null;
    }

    if (paymentmethodtype != null && !paymentmethodtype.isEmpty()) {
      Query<String> paymentmethodtypes = session
          .createQuery("SELECT id FROM OBPOS_PAYMENTMETHOD_TYPE\n"
              + "WHERE provider = (SELECT pg.provider FROM OBPOS_PAYMENTGROUP as pg WHERE pg.id = :paymentgroup)\n"
              + "AND id = :paymentmethodtype", String.class);
      paymentmethodtypes.setParameter("paymentgroup", paymentgroup);
      paymentmethodtypes.setParameter("paymentmethodtype", paymentmethodtype);

      if (paymentmethodtypes.uniqueResult() != null) {
        // PaymentMethodType is valid for the selected PaymentGroup
        return paymentmethodtype;
      }
    }

    Query<String> undefinedpaymentmethodtype = session
        .createQuery("SELECT id FROM OBPOS_PAYMENTMETHOD_TYPE\n"
            + "WHERE provider = (SELECT pg.provider FROM OBPOS_PAYMENTGROUP as pg WHERE pg.id = :paymentgroup)\n"
            + "AND searchKey = 'UNDEFINED'", String.class);
    undefinedpaymentmethodtype.setParameter("paymentgroup", paymentgroup);
    String undefinedid = undefinedpaymentmethodtype.uniqueResult();
    if (undefinedid != null) {
      return undefinedid;
    }

    Query<String> firstpaymentmethodtype = session.createQuery(
        "SELECT id FROM OBPOS_PAYMENTMETHOD_TYPE\n"
            + "WHERE provider = (SELECT pg.provider FROM OBPOS_PAYMENTGROUP as pg WHERE pg.id = :paymentgroup)",
        String.class).setMaxResults(1);
    firstpaymentmethodtype.setParameter("paymentgroup", paymentgroup);
    return firstpaymentmethodtype.uniqueResult();
  }
}
