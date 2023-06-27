package org.openbravo.client.application.businesslogic;

import org.openbravo.model.materialmgmt.transaction.InventoryCount;

public abstract class InventoryCountUpdateHook {

  public abstract void exec(InventoryCount inventory) throws Exception;
}
