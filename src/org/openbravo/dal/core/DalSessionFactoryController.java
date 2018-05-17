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
 * All portions are Copyright (C) 2008-2018 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.dal.core;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.function.SQLFunction;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.session.SessionFactoryController;

/**
 * Initializes and provides the session factory for the runtime dal layer. This
 * SessionFactoryController is initialized after the model has been read in-memory. The
 * {@link DalMappingGenerator DalMappingGenerator} is used to generated the Hibernate mapping for
 * the runtime model (see {@link ModelProvider ModelProvider}.
 * 
 * @author mtaal
 */

public class DalSessionFactoryController extends SessionFactoryController {
  private static final Logger log = Logger.getLogger(DalSessionFactoryController.class);

  @Inject
  @Any
  private Instance<SQLFunctionRegister> sqlFunctionRegisters;

  private Map<String, SQLFunction> sqlFunctions;

  @Override
  protected void mapModel(Configuration configuration) {
    final String mapping = DalMappingGenerator.getInstance().generateMapping();
    log.debug("Generated mapping: ");
    log.debug(mapping);
    configuration.addXML(mapping);
  }

  @Override
  protected void setInterceptor(Configuration configuration) {
    configuration.setInterceptor(new OBInterceptor());
  }

  @Override
  protected Map<String, SQLFunction> getSQLFunctions() {
    if (sqlFunctions != null) {
      return sqlFunctions;
    }
    sqlFunctions = new HashMap<>();
    if (sqlFunctionRegisters == null) {
      return sqlFunctions;
    }
    for (SQLFunctionRegister register : sqlFunctionRegisters) {
      Map<String, SQLFunction> registeredSqlFunctions = register.getSQLFunctions();
      if (registeredSqlFunctions == null) {
        continue;
      }
      sqlFunctions.putAll(registeredSqlFunctions);
    }
    return sqlFunctions;
  }

  void setSQLFunctions(Map<String, SQLFunction> sqlFunctions) {
    this.sqlFunctions = sqlFunctions;
  }
}
