package org.openbravo.retail.posterminal.master;

import java.util.ArrayList;
import java.util.List;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLProperty;
import org.openbravo.mobile.core.model.ModelExtension;

@Qualifier(TaxZone.taxZonePropertyExtension)
public class TaxZoneProperties extends ModelExtension {

  @Override
  public List<HQLProperty> getHQLProperties(Object params) {
    ArrayList<HQLProperty> list = new ArrayList<HQLProperty>() {
      private static final long serialVersionUID = 1L;
      {
        add(new HQLProperty("financialMgmtTaxZone.id", "id"));
        add(new HQLProperty("financialMgmtTaxZone.tax.id", "tax"));
        add(new HQLProperty("financialMgmtTaxZone.fromCountry.id", "fromCountry"));
        add(new HQLProperty("financialMgmtTaxZone.destinationCountry.id", "destinationCountry"));
        add(new HQLProperty("financialMgmtTaxZone.fromRegion.id", "fromRegion"));
        add(new HQLProperty("financialMgmtTaxZone.destinationRegion.id", "destinationRegion"));
        add(new HQLProperty("financialMgmtTaxZone.active", "active"));
      }
    };
    return list;
  }

}