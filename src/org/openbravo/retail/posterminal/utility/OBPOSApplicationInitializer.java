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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.client.kernel.ApplicationInitializer;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.db.DalConnectionProvider;

public class OBPOSApplicationInitializer implements ApplicationInitializer {

  @Override
  public void initialize() {
    OBDal.getInstance().registerSQLFunction("c_currency_rate",
        new StandardSQLFunction("c_currency_rate", StandardBasicTypes.STRING));
    OBDal.getInstance().registerSQLFunction("get_pricelist_version",
        new SQLFunctionTemplate(StandardBasicTypes.STRING, getPriceFunction()));
  }

  private String getPriceFunction() {

    final String RDBMS = new DalConnectionProvider(false).getRDBMS();

    String func = " (select m_pricelist_version_id"
        + " from m_pricelist_version plv "
        + " where plv.m_pricelist_id = ?1"
        + " and plv.isactive = 'Y' and validfrom in (select max(pplv.validfrom)"
        + "     from m_pricelist_version pplv" //
        + "     where pplv.isactive = 'Y'" + "     and pplv.m_pricelist_id = ?1"
        + "     and to_char(pplv.validfrom,'yyyy-mm-dd') <= ?2)";
    if ("ORACLE".equals(RDBMS)) {
      func = func + " and rownum <= 1)";
    } else {
      func = func + " limit 1)";
    }
    return func;
  }
}
