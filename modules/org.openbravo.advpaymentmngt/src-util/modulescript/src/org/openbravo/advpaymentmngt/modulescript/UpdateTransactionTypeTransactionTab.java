package org.openbravo.advpaymentmngt.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

// This modulescript initializes the colum TRXTYPE (Transaction Type) in FIN_FINACC_TRANSACTION TABLE
public class UpdateTransactionTypeTransactionTab extends ModuleScript {
 
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      boolean isUpdated= UpdateTransactionTypeTransactionTabData.isExecuted(cp);
      if (!isUpdated){
        UpdateTransactionTypeTransactionTabData.updateToBankFee(cp);
        UpdateTransactionTypeTransactionTabData.updateToBPDeposit(cp);
        UpdateTransactionTypeTransactionTabData.updateToBPWithdrawal(cp);
        UpdateTransactionTypeTransactionTabData.createPreference(cp);
      }
    } catch (Exception e) {
      handleError(e);
    }   
  }
}