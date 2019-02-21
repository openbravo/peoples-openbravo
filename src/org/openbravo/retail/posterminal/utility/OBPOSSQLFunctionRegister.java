/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.utility;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.SQLFunctionRegister;
import org.openbravo.database.ConnectionProviderImpl;
import org.openbravo.erpCommon.utility.SystemInfo;
import org.openbravo.service.db.DalConnectionProvider;

/**
 * A class in charge of registering the SQL functions required by the Web POS module.
 */
@ApplicationScoped
public class OBPOSSQLFunctionRegister implements SQLFunctionRegister {

  final static String RDBMS = new DalConnectionProvider(false).getRDBMS();

  @Override
  public Map<String, SQLFunction> getSQLFunctions() {
    Map<String, SQLFunction> sqlFunctions = new HashMap<>();
    sqlFunctions.put("c_currency_rate",
        new StandardSQLFunction("c_currency_rate", StandardBasicTypes.STRING));
    sqlFunctions.put("obpos_currency_rate",
        new StandardSQLFunction("obpos_currency_rate", StandardBasicTypes.STRING));
    sqlFunctions.put("get_pricelist_version",
        new SQLFunctionTemplate(StandardBasicTypes.STRING, getPriceFunction()));
    sqlFunctions.put("hqlaggdist",
        new SQLFunctionTemplate(StandardBasicTypes.STRING, getAggregationSQL()));
    return sqlFunctions;
  }

  private String getPriceFunction() {

    String func = " (select m_pricelist_version_id" + " from m_pricelist_version plv "
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

  private String getAggregationSQL() {
    if ("ORACLE".equals(RDBMS)) {
      if (is11R2orNewer() && true) {
        return "RTRIM(REGEXP_REPLACE(listagg(to_char(?1), ',') WITHIN GROUP (ORDER BY ?1),'([^,]*)(,\\1)+($|,)','\\1\\3'),',')";
      } else {
        return "wm_concat(distinct to_char(?1))";
      }
    } else {
      return "array_to_string(array_agg(distinct ?1), ',')";
    }
  }

  private boolean is11R2orNewer() {
    String dbVersion = null;
    try {
      dbVersion = SystemInfo.getDatabaseVersion(
          new ConnectionProviderImpl(OBPropertiesProvider.getInstance().getOpenbravoProperties()));
    } catch (Exception ignore) {
    }
    if (dbVersion == null) {
      return false;
    }
    int version = Integer.parseInt(dbVersion.replaceAll("\\.", "").substring(0, 3));
    return version >= 112;
  }

}
