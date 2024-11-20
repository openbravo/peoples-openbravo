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
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.service.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openbravo.base.exception.OBSecurityException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.module.Application;

/**
 * A data source that retrieves applications and "Any"
 */
public class ApplicationDataSource extends ReadOnlyDataSourceService {
  private static final Logger log = LogManager.getLogger();
  private static final String AD_APPLICATIONS_TABLE_ID = "EBC9A4A7BC9E48D7BBC9F70530627FB9";

  @Override
  protected int getCount(Map<String, String> parameters) {
    return getData(parameters, 0, Integer.MAX_VALUE).size();
  }

  @Override
  protected List<Map<String, Object>> getData(Map<String, String> parameters, int startRow,
      int endRow) {
    try {
      OBContext.setAdminMode();
      OBCriteria<Application> criteria = OBDal.getInstance().createCriteria(Application.class);

      // TODO: filtering
      List<Application> applications = criteria.list();

      List<Map<String, Object>> result = new ArrayList<>();
      // Add Any element
      result.add(Map.of("searchKey", "ANY", "name", "Any"));
      for (Application application : applications) {
        Map<String, Object> applicationResult = Map.of("searchKey", application.getValue(), //
            "name", application.getName() //
        );
        result.add(applicationResult);
      }

      return result;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Override
  public void checkFetchDatasourceAccess(Map<String, String> parameters) {
    final OBContext obContext = OBContext.getOBContext();
    try {
      Entity entityToCheck = ModelProvider.getInstance()
          .getEntityByTableId(AD_APPLICATIONS_TABLE_ID);
      if (entityToCheck != null) {
        obContext.getEntityAccessChecker().checkReadableAccess(entityToCheck);
      }
    } catch (OBSecurityException e) {
      handleExceptionUnsecuredDSAccess(e);
    }
  }
}
