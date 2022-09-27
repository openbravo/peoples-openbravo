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
 * All portions are Copyright (C) 2022 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.materialmgmt;

import java.util.List;

import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_process.ADProcessID;
import org.openbravo.model.ad.ui.Field;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessRunnerHook;

/**
 * Synchronizes the terminology of the fields based in relevant characteristic properties. This kind
 * of fields are not backed by a DB physical column and therefore their terminology cannot be
 * synchronized through the AD_Synchronize DB procedure.
 */
@ADProcessID("172")
public class RelevantCharacteristicFieldTerminologySynchronizer implements ProcessRunnerHook {

  @Override
  public void onExecutionFinish(ProcessBundle bundle) {
    try {
      OBContext.setAdminMode(false);
      for (Field field : getNonSynchronizableFieldsByDB()) {
        RelevantCharacteristicProperty.from(field).ifPresent(p -> {
          field.setName(p.getFieldName());
          field.setDescription(p.getDescription());
          field.setHelpComment(p.getDescription());
        });
      }
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private List<Field> getNonSynchronizableFieldsByDB() {
    //@formatter:off
    final String hql = " as f" +
                       " where f.column is null" +
                       "  and f.clientclass is null" +
                       "  and f.property is not null" +
                       "  and f.centralMaintenance = true" +
                       "  and f.module.inDevelopment = true";
    //@formatter:on
    return OBDal.getInstance().createQuery(Field.class, hql).list();
  }
}
