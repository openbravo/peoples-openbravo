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
 * All portions are Copyright (C) 2020 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.client.kernel;

import java.util.List;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.Type;

/**
 * Creates SQL parsing for fullTextSearchRank hbm function
 */
public class PgFullTextRankFunction implements SQLFunction {

  @Override
  public Type getReturnType(Type arg0, Mapping arg1) throws QueryException {
    return new BigDecimalType();
  }

  @Override
  public boolean hasArguments() {
    return true;
  }

  @Override
  public boolean hasParenthesesIfNoArguments() {
    return false;
  }

  /**
   * Function that parses the hbm function to SQL language:
   * 
   * @param args:
   *          list of arguments passed to fullTextSearchFilter hbm function
   *          <li>table
   *          <li>field: tsvector column of table
   *          <li>ftsconfiguration [optional]: language to pass to to_tsquery function
   *          <li>value: string to be searched/compared
   */
  @SuppressWarnings("rawtypes")
  @Override
  public String render(Type arg0, List args, SessionFactoryImplementor factory)
      throws QueryException {
    if (args == null || args.size() < 3) {
      throw new IllegalArgumentException("The function must be passed at least 3 arguments");
    }

    int pointPosition = (int) args.get(0).toString().indexOf(".");
    String table = (String) args.get(0).toString().substring(0, pointPosition);
    String field = (String) args.get(1);
    String ftsConfiguration;
    String value;
    String fragment;

    if (args.size() == 4) {
      ftsConfiguration = (String) args.get(2);
      value = (String) args.get(3);
      fragment = "ts_rank_cd(" + table + "." + field + ", to_tsquery(" + ftsConfiguration
          + "::regconfig, " + value + "), 4)";
    } else {
      value = (String) args.get(2);
      fragment = "ts_rank_cd(" + table + "." + field + ", to_tsquery(" + value + "), 4)";
    }

    return fragment;
  }
}
