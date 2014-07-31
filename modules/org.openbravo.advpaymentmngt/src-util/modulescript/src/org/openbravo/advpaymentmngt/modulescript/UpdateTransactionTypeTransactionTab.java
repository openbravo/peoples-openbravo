package org.openbravo.advpaymentmngt.modulescript;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import java.sql.PreparedStatement;

public class UpdateTransactionTypeTransactionTab extends ModuleScript {
 
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      UpdateTransactionTypeTransactionTabData.updateToBankFee(cp);
      UpdateTransactionTypeTransactionTabData.updateToBPDeposit(cp);
      UpdateTransactionTypeTransactionTabData.updateToBPWithdrawal(cp);
    } catch (Exception e) {
      handleError(e);
    }   
  }
}