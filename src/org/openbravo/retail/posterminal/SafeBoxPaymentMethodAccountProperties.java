package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(SafeBoxes.safeBoxesPaymentMethodsAccountPropertyExtension)
public class SafeBoxPaymentMethodAccountProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty(
            "coalesce((SELECT fr.endingBalance FROM FIN_Reconciliation fr WHERE fr.creationDate = (SELECT MAX(fr2.creationDate) FROM FIN_Reconciliation fr2 WHERE fr2.account.id = :financialAccountId) AND fr.account.id = :financialAccountId), ffa.initialBalance)",
            "initialBalance"));
      }
    };

    return list;
  }

}
