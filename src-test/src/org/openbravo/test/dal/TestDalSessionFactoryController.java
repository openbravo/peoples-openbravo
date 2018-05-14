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
 * All portions are Copyright (C) 2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.dal;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;
import org.openbravo.dal.core.DalSessionFactoryController;

/**
 * Extends the standard {@link DalSessionFactoryController} in order to provide the ability of
 * registering SQL functions in Hibernate for the test infrastructure. This is because is not
 * possible to use weld on the initialization done in the
 * {@link org.openbravo.test.base.OBBaseTest#setDalUp}. Note that the standard
 * {@link DalSessionFactoryController} uses weld to retrieve the list of SQL functions to be
 * registered.
 */
public class TestDalSessionFactoryController extends DalSessionFactoryController {

  @Override
  protected Map<String, SQLFunction> getSQLFunctions() {
    Map<String, SQLFunction> sqlFunctions = new HashMap<>();
    sqlFunctions.put("ad_column_identifier_std", new StandardSQLFunction(
        "ad_column_identifier_std", StandardBasicTypes.STRING));
    return sqlFunctions;
  }
}
