package org.openbravo.retail.posterminal;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(SafeBoxes.safeBoxesPaymentMethodsTransactionPropertyExtension)
public class SafeBoxPaymentMethodTransactionProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("coalesce(sum(fft.depositAmount), 0)", "depositBalance"));
        add(new HQLProperty("coalesce(sum(fft.paymentAmount), 0)", "paymentBalance"));
      }
    };

    return list;
  }

}
