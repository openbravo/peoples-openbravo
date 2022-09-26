/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
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
