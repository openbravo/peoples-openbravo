/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal.modulescript;

import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class ConvertBrandIntoCharacteristic extends ModuleScript {

  private static final Logger log4j = LogManager.getLogger();
  private static final String RETAIL_PACK_MODULE_ID = "03FAB282A7BF47D3B1B242AC67F7845B";

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      if (!ConvertBrandIntoCharacteristicData.existBrand(cp)) {
        if (!ConvertBrandIntoCharacteristicData.hasPreference(cp)) {
          // create characteristic
          ConvertBrandIntoCharacteristicData[] data = ConvertBrandIntoCharacteristicData
              .selectBrandByClient(cp);
          for (ConvertBrandIntoCharacteristicData brand : data) {
            String id = UUID.randomUUID().toString().replace("-", "").toUpperCase();
            ConvertBrandIntoCharacteristicData.insertCharacteristic(cp.getConnection(), cp, id,
                brand.adClientId);
            // create characteristic value
            ConvertBrandIntoCharacteristicData[] data2 = ConvertBrandIntoCharacteristicData
                .selectBrands(cp, brand.adClientId);
            for (ConvertBrandIntoCharacteristicData brand2 : data2) {
              ConvertBrandIntoCharacteristicData.insertCharacteristicValues(cp.getConnection(), cp,
                  brand2.mBrandId, brand2.adClientId, brand2.adOrgId, id, brand2.name);
              ConvertBrandIntoCharacteristicData.insertTreeNode(cp.getConnection(), cp,
                  brand2.mBrandId, brand2.adClientId, brand2.adOrgId);
            }
            // create product characteristic value
            ConvertBrandIntoCharacteristicData[] data3 = ConvertBrandIntoCharacteristicData
                .selectProductBrands(cp, brand.adClientId);
            for (ConvertBrandIntoCharacteristicData brand3 : data3) {
              ConvertBrandIntoCharacteristicData.insertProductCharacteristic(cp.getConnection(), cp,
                  brand3.adClientId, brand3.adOrgId, brand3.mProductId, id);
              ConvertBrandIntoCharacteristicData.insertProductCharacteristicValue(
                  cp.getConnection(), cp, brand3.adClientId, brand3.adOrgId, brand3.mProductId, id,
                  brand3.mBrandId);
              ConvertBrandIntoCharacteristicData.updateCharacteristicDesc(cp.getConnection(), cp,
                  brand3.name, brand3.mProductId);
            }
          }
          ConvertBrandIntoCharacteristicData.createPreference(cp);
        }
      }
    } catch (

    Exception e) {
      log4j.error("Errors converting brand into characteristic");
      handleError(e);
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits(RETAIL_PACK_MODULE_ID, null,
        new OpenbravoVersion(1, 8, 5000));
  }

  @Override
  protected boolean executeOnInstall() {
    return false;
  }

}
