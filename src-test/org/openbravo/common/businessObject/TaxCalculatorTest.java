package org.openbravo.common.businessObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.junit.Assert;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.test.base.BaseTest;

public class TaxCalculatorTest extends BaseTest {

  public TaxCalculatorTest() {

  }

  /**
   * Test without Inclusive of Tax.
   */
  public void testSimpleTaxCalculatorAgainstNetAmount() {
    TaxRate rate = getTaxRate("IOT-Ten Percent Central Tax");
    TaxCalculator calculator = new TaxCalculator(rate);
    BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(10000.00),
        false, false);
    assertEquals(new BigDecimal(1000.00), taxAmt);

  }

  /**
   * Test without Inclusive of Tax.
   */
  public void testSimpleTaxCalculatorAgainstGrossAmount() {
    TaxRate rate = getTaxRate("IOT-Ten Percent Central Tax");
    TaxCalculator calculator = new TaxCalculator(rate);
    BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(11000.00), true,
        false);
    assertEquals(new BigDecimal(1000.00), taxAmt);

  }

  /**
   * Test without Inclusive of Tax.
   */
  public void testSummaryBasedTaxCalculatorAgainstNetAmount() {
    TaxRate rate = getTaxRate("IOT-Basic Excise Duty");
    TaxCalculator calculator = new TaxCalculator(rate);
    BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(1000.00), false,
        false);
    assertEquals(new BigDecimal(123.60).round(new MathContext(2)), taxAmt.round(new MathContext(2)));

  }

  /**
   * Test Inclusive of Tax.
   */
  public void testSummaryBasedTaxCalculatorAgainstIOTAmount() {
    TaxRate rate = getTaxRate("IOT-Basic Excise Duty");
    TaxCalculator calculator = new TaxCalculator(rate);
    BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(1123.60), true,
        false);
    assertEquals(new BigDecimal(123.60).round(new MathContext(2)), taxAmt.round(new MathContext(2)));

  }

  /**
   * Test Invalid Tnx type.
   */

  public void testSimpleTaxCalculatorInvalidTxnType() {
    TaxRate rate = getTaxRate("IOT- Ten Percent Sales Tax");

    TaxCalculator calculator = new TaxCalculator(rate);
    try {
      BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(10000.00),
          true, false);
      Assert.assertTrue(false);

    } catch (OBException exc) {
      Assert.assertTrue(true);
    }

  }

  /**
   * Test Valid Tnx type.
   */
  public void testSimpleTaxCalculatorValidTxnType() {
    TaxRate rate = getTaxRate("IOT-Ten Percent Central Tax");
    TaxCalculator calculator = new TaxCalculator(rate);
    BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(10000.00),
        false, true);
    assertEquals(new BigDecimal(1000.00), taxAmt);
  }

  /**
   * Test Valid Date.
   */
  public void testSimpleTaxCalculatorValidDate() {
    TaxRate rate = getTaxRate("IOT-Basic Excise Duty");
    TaxCalculator calculator = new TaxCalculator(rate);
    Date validDateFrom = rate.getValidFromDate();
    Date currentDate = new Date();
    if (validDateFrom.before(currentDate) || validDateFrom.equals(currentDate)) {
      BigDecimal taxAmt = calculator.calculateTax(getCurrency("USD"), new BigDecimal(1000.00),
          false, false);
      assertEquals(new BigDecimal(123.60).round(new MathContext(2)), taxAmt
          .round(new MathContext(2)));
    } else
      throw new OBException("Tax Rate Date is not Valid ");
  }

  private Currency getCurrency(String iSOCode) {
    OBCriteria<Currency> ccyCriteria = OBDal.getInstance().createCriteria(Currency.class);
    ccyCriteria.add(Restrictions.eq(Currency.PROPERTY_ISOCODE, iSOCode));
    ccyCriteria.add(Restrictions.eq(TaxRate.PROPERTY_ACTIVE, Boolean.TRUE));
    List<Currency> currencyList = ccyCriteria.list();
    if (currencyList.size() == 0) {
      throw new OBException("Currency " + iSOCode + " does not exist.");
    }
    return currencyList.get(0);

  }

  private TaxRate getTaxRate(String taxName) {
    OBCriteria<TaxRate> taxRateCriteria = OBDal.getInstance().createCriteria(TaxRate.class);
    taxRateCriteria.add(Restrictions.eq(TaxRate.PROPERTY_ORGANIZATION, OBDal.getInstance().get(
        Organization.class, this.TEST_US_ORG_ID)));
    taxRateCriteria.add(Restrictions.eq(TaxRate.PROPERTY_NAME, taxName));
    taxRateCriteria.add(Restrictions.eq(TaxRate.PROPERTY_ACTIVE, Boolean.TRUE));
    List<TaxRate> taxRateList = taxRateCriteria.list();
    if (taxRateList.size() == 0) {
      throw new OBException("Tax Rate " + taxName + " does not exist.");
    }
    return taxRateList.get(0);

  }

}
