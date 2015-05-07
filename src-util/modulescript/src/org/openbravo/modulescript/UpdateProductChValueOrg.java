package org.openbravo.modulescript;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class UpdateProductChValueOrg extends ModuleScript {

  private static final Logger log4j = Logger.getLogger(UpdateProductChValueOrg.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean executed = UpdateProductChValueOrgData.isModuleScriptExecuted(cp);
      if (!executed) {
        int count = UpdateProductChValueOrgData.updateProductChValueOrg(cp);
        if (count > 0)
          log4j.info("Updated " + count + " invoices.");
        UpdateProductChValueOrgData.createPreference(cp);
      }
    } catch (Exception e) {
      handleError(e);
    }
  }

}
