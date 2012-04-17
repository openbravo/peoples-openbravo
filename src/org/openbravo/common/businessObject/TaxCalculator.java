package org.openbravo.common.businessObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.geography.Region;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.financialmgmt.tax.TaxRate;

public class TaxCalculator {

  private static final int PERCENT_TO_VALUE = 100;
  private TaxRate taxRate;
  protected Logger log4j = Logger.getLogger(this.getClass());
  Map<TaxRate, BigDecimal> taxAmtsMap = new HashMap<TaxRate, BigDecimal>();

  public TaxCalculator(TaxRate aTaxRate) {
    setTaxRate(aTaxRate);
  }

  public TaxCalculator(String aTaxRateId) {
    this(OBDal.getInstance().get(TaxRate.class, aTaxRateId));
  }

  public TaxRate getTaxRate() {
    return taxRate;
  }

  public void setTaxRate(TaxRate taxRate) {
    this.taxRate = taxRate;
  }

  // Get price inclusive tax

  public static boolean isPriceTaxInclusive(String orderId) {
    if (OBDal.getInstance().get(Order.class, orderId).getPriceList().isPriceIncludesTax())
      return true;
    else
      return false;

  }

  public BigDecimal taxCalculationFromOrder(String orderId, BigDecimal amount) {
    Order ord = OBDal.getInstance().get(Order.class, orderId);
    BigDecimal taxAmt = new BigDecimal(0);
    taxAmt = calculateTax(ord.getCurrency(), amount, ord.getPriceList().isPriceIncludesTax(), ord
        .isSalesTransaction());
    return taxAmt;
  }

  public BigDecimal calculateTax(Currency ccy, BigDecimal amount, boolean isInclusiveOfTax,
      boolean isSale) {
    return calculateTax(ccy, amount, isInclusiveOfTax, isSale, null, null, null, null);

  }

  public BigDecimal calculateTax(Currency ccy, BigDecimal amount, boolean isInclusiveOfTax,
      boolean isSale, Country sourceCountry, Region sourceRegion, Country destinationCountry,
      Region destinationRegion) {
    BigDecimal taxAmt = new BigDecimal(0);
    BigDecimal taxDivider = new BigDecimal(0);
    taxAmtsMap.clear();
    if (isInclusiveOfTax) {
      taxDivider = getTaxAmount(new BigDecimal(1), taxRate, isSale, ccy);
      taxDivider = taxDivider.add(new BigDecimal(1));
      taxAmt = amount.subtract(amount.divide(taxDivider, ccy.getStandardPrecision().intValue()));
    } else
      taxAmt = getTaxAmount(amount, taxRate, isSale, ccy);
    log4j.debug("Tax Amt: " + taxAmt);
    return taxAmt.divide(new BigDecimal(1));

  }

  private BigDecimal getTaxAmount(BigDecimal amount, TaxRate rate, boolean isSale, Currency ccy) {
    BigDecimal amt = new BigDecimal(0);
    if ((isSale && rate.getSalesPurchaseType().equals("P"))
        || (!isSale && rate.getSalesPurchaseType().equals("S"))) {
      throw new OBException("Tax Rate is not valid for this document type");
    }
    // if summary level
    if (rate.isSummaryLevel()) {
      List<TaxRate> rateList = getChildTaxRates(rate);
      for (TaxRate childRate : rateList) {
        amt = amt.add(getTaxAmount(amount, childRate, isSale, ccy));
      }
    } else {
      if (rate.getBaseAmount().equals(""))
        throw new OBException("Tax BaseAmount is null");
      if (rate.getBaseAmount().equals("LNA")) {
        amt = amount.multiply(rate.getRate()).divide(new BigDecimal(PERCENT_TO_VALUE));
        log4j.debug("Tax Amt: " + amt);
      } else if (rate.getBaseAmount().equals("LNATAX")) {
        amt = taxAmtsMap.get(rate.getTaxBase()).add(amount).multiply(rate.getRate()).divide(
            new BigDecimal(PERCENT_TO_VALUE));
        log4j.debug("Tax Amt: " + amt);
      } else if (rate.getBaseAmount().equals("TAX")) {
        if (rate.getTaxBase().equals(""))
          throw new OBException("TaxBase is null");
        amt = taxAmtsMap.get(rate.getTaxBase()).multiply(rate.getRate()).divide(
            new BigDecimal(PERCENT_TO_VALUE));
        log4j.debug("Tax Amt: " + amt);
      }
      amt = amt.round(new MathContext(ccy.getStandardPrecision().intValue()));
    }
    taxAmtsMap.put(rate, amt);
    return amt;
  }

  // Get tax rate list
  private List<TaxRate> getChildTaxRates(TaxRate rate) {
    OBCriteria<TaxRate> taxRateCriteria = OBDal.getInstance().createCriteria(TaxRate.class);
    taxRateCriteria.add(Restrictions.eq(TaxRate.PROPERTY_PARENTTAXRATE, rate));
    taxRateCriteria.addOrderBy(TaxRate.PROPERTY_LINENO, true);
    return taxRateCriteria.list();

  }

}
