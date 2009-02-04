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
 * The Initial Developer of the Original Code is Openbravo SL 
 * All portions are Copyright (C) 2009 Openbravo SL 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.system;

import java.util.Date;
import java.util.List;

import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;
import org.hibernate.criterion.Expression;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.provider.OBSingleton;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.utility.DataSet;
import org.openbravo.model.ad.utility.DataSetTable;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.system.SystemValidationResult.SystemValidationType;

/**
 * Provides utility like services.
 * 
 * @author Martin Taal
 */
public class SystemService implements OBSingleton {
  private static SystemService instance;

  public static SystemService getInstance() {
    if (instance == null) {
      instance = OBProvider.getInstance().get(SystemService.class);
    }
    return instance;
  }

  public static void setInstance(SystemService instance) {
    SystemService.instance = instance;
  }

  /**
   * Returns true if for a certain class there are objects which have changed.
   * 
   * @param clzs
   *          the type of objects which are checked
   * @param afterDate
   *          the timestamp to check
   * @return true if there is an object in the database which changed since afterDate, false
   *         otherwise
   */
  public boolean hasChanged(Class<?>[] clzs, Date afterDate) {
    for (Class<?> clz : clzs) {
      final OBCriteria<?> obc = OBDal.getInstance().createCriteria((Class<BaseOBObject>) clz);
      obc.add(Expression.gt(Organization.PROPERTY_UPDATED, afterDate));
      // todo: count is slower than exists, is exists possible?
      if (obc.count() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if objects of a {@link DataSetTable} of the {@link DataSet} have changed since a
   * specific date. Note that this method does not use whereclauses or other filters defined in the
   * dataSetTable. It checks all instances of the table of the DataSetTable.
   * 
   * @param dataSet
   *          the DataSetTables of this dataSet are checked.
   * @param afterDate
   *          the time limit
   * @return true if there is at least one object which has changed since afterDate, false
   *         afterwards
   */
  public <T extends BaseOBObject> boolean hasChanged(DataSet dataSet, Date afterDate) {
    for (DataSetTable dataSetTable : dataSet.getDataSetTableList()) {
      final Entity entity = ModelProvider.getInstance().getEntityByTableName(
          dataSetTable.getTable().getTableName());
      final OBCriteria<T> obc = OBDal.getInstance().createCriteria(entity.getName());
      obc.add(Expression.gt(Organization.PROPERTY_UPDATED, afterDate));
      // todo: count is slower than exists, is exists possible?
      if (obc.count() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Validates a specific module, both the database model as the module data itself
   * 
   * @param module
   *          the module to validate
   * @param database
   *          the database to read the dbschema from
   * @return the validation result
   */
  public SystemValidationResult validateModule(Module module, Database database) {
    final DatabaseValidator databaseValidator = new DatabaseValidator();
    databaseValidator.setValidateModule(module);
    databaseValidator.setDatabase(database);
    final SystemValidationResult result = databaseValidator.validate();

    final ModuleValidator moduleValidator = new ModuleValidator();
    moduleValidator.setValidateModule(module);
    result.addAll(moduleValidator.validate());
    return result;
  }

  /**
   * Prints the validation result grouped by validation type to the log.
   * 
   * @param log
   *          the log to which the validation result is printed
   * @param result
   *          the validation result containing both errors and warning
   * @return the errors are returned as a string
   */
  public String logValidationResult(Logger log, SystemValidationResult result) {
    for (SystemValidationType validationType : result.getWarnings().keySet()) {
      log.warn("\n");
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.warn("Warnings for Validation type: " + validationType);
      log.warn("+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> warnings = result.getWarnings().get(validationType);
      for (String warning : warnings) {
        log.warn(warning);
      }
    }

    final StringBuilder sb = new StringBuilder();
    for (SystemValidationType validationType : result.getErrors().keySet()) {
      sb.append("\n");
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      sb.append("\nErrors for Validation type: " + validationType);
      sb.append("\n+++++++++++++++++++++++++++++++++++++++++++++++++++");
      final List<String> errors = result.getErrors().get(validationType);
      for (String err : errors) {
        sb.append("\n");
        sb.append(err);
      }
    }
    log.error(sb.toString());
    return sb.toString();
  }
}
