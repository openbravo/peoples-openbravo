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
import org.hibernate.type.BooleanType;
import org.hibernate.type.Type;

public class PgFullTextFilterFunction implements SQLFunction {

  @Override
  public Type getReturnType(Type arg0, Mapping arg1) throws QueryException {
    return new BooleanType();
  }

  @Override
  public boolean hasArguments() {
    return true;
  }

  @Override
  public boolean hasParenthesesIfNoArguments() {
    return false;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public String render(Type arg0, List args, SessionFactoryImplementor factory)
      throws QueryException {
    if (args == null || args.size() < 2) {
      throw new IllegalArgumentException("The function must be passed at least 1 argument");
    }

    String fragment = null;
    String field = null;
    String ftsConfiguration = null;
    String value = null;

    if (args.size() == 3) {
      field = (String) args.get(0);
      ftsConfiguration = (String) args.get(1);
      value = (String) args.get(2);
      fragment = field + " @@ " + "to_tsquery(" + ftsConfiguration + "::regconfig, " + value + ")";
    } else {
      field = (String) args.get(0);
      value = (String) args.get(1);
      fragment = field + " @@ " + "to_tsquery(" + value + ")";
    }

    return fragment;
  }
}
