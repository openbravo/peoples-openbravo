package org.openbravo.advpaymentmngt.test.draft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentProposal;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.test.base.BaseTest;

/**
 * The PaymentTest_03 class used to test the payment document generation and reactivation with
 * write-off option.
 */
public class PaymentTest_05 extends BaseTest {

  private static final Logger log = Logger.getLogger(PaymentTest_05.class);

  private static final String MANUAL_EXECUTION = "M";
  private static final String CLEARED_ACCOUNT = "CLE";
  private static final String IN_TRANSIT_ACCOUNT = "INT";
  private static final String WITHDRAWN_ACCOUNT = "WIT";
  private static final String DEPOSIT_ACCOUNT = "DEP";
  private static final String CASH = "C";
  private static final String STANDARD_DESCRIPTION = "JUnit Test Payment_05";

  private String financialAccountId;
  private String paymentMethodId;

  /**
   * Initial Set up.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TestUtility.setTestContext();
  }

  /**
   * Payment Proposal with two invoices of different business partner partially paid.
   */
  public void testRunPayment_05() {
    String mcGiver = "1000008";
    String turtle = "1000013";
    String currencyId = "102"; // EUR
    Invoice inv1;
    Invoice inv2;
    AdvPaymentMngtDao dao = new AdvPaymentMngtDao();
    boolean exception = false;

    try {
      inv1 = createPurchaseInvoice(mcGiver);
      inv2 = createPurchaseInvoice(turtle);

      inv1 = OBDal.getInstance().get(Invoice.class, inv1.getId());
      inv2 = OBDal.getInstance().get(Invoice.class, inv2.getId());

      FIN_PaymentProposal paymentProposal = TestUtility.createNewPaymentProposal(OBContext
          .getOBContext().getCurrentClient(), OBContext.getOBContext().getCurrentOrganization(),
          OBDal.getInstance().get(FIN_FinancialAccount.class, financialAccountId), OBDal
              .getInstance().get(Currency.class, currencyId), OBDal.getInstance().get(
              FIN_PaymentMethod.class, paymentMethodId));

      List<FIN_PaymentScheduleDetail> scheduleDetails1 = dao
          .getInvoicePendingScheduledPaymentDetails(inv1);
      List<FIN_PaymentScheduleDetail> scheduleDetails2 = dao
          .getInvoicePendingScheduledPaymentDetails(inv2);

      List<FIN_PaymentScheduleDetail> mergeScheduleDetails = new ArrayList<FIN_PaymentScheduleDetail>();
      mergeScheduleDetails.add(scheduleDetails1.get(0));
      mergeScheduleDetails.add(scheduleDetails2.get(0));

      HashMap<String, BigDecimal> amounts = new HashMap<String, BigDecimal>();
      amounts.put(scheduleDetails1.get(0).getId(), new BigDecimal("100"));
      amounts.put(scheduleDetails2.get(0).getId(), new BigDecimal("100"));

      OBContext.setAdminMode(true);
      try {
        // ORA-01779: cannot modify a column which maps to a non key-preserved table
        FIN_AddPayment.savePaymentProposal(paymentProposal, new BigDecimal("200"),
            mergeScheduleDetails, amounts, null);
      } finally {
        OBContext.restorePreviousMode();
      }

      TestUtility.processPaymentProposal(paymentProposal, "GSP");

    } catch (Exception e) {
      e.printStackTrace();
      log.error(FIN_Utility.getExceptionMessage(e));
      exception = true;
    }

    assertFalse(exception);

  }

  private Invoice createPurchaseInvoice(String businessPartnerId) throws Exception {

    // DATA SETUP
    String priceListId = "1000003"; // General Sales
    String paymentTermId = "1000000"; // Immediate"
    String currencyId = "102"; // EUR
    String productId = "1000006"; // Hat
    String taxId = "1000002"; // VAT 4%
    String docTypeId = "1000008"; // AR Invoice
    BigDecimal invoicedQuantity = new BigDecimal("5");
    BigDecimal netUnitPrice = new BigDecimal("35");
    BigDecimal netListPrice = new BigDecimal("37");
    BigDecimal lineNetAmount = new BigDecimal("175");
    BigDecimal priceLimit = new BigDecimal("33.50");

    PriceList testPriceList = OBDal.getInstance().get(PriceList.class, priceListId);
    BusinessPartner testBusinessPartner = OBDal.getInstance().get(BusinessPartner.class,
        businessPartnerId);
    Location location = TestUtility.getOneInstance(Location.class, new Value(
        Location.PROPERTY_BUSINESSPARTNER, testBusinessPartner));
    PaymentTerm testPaymentTerm = OBDal.getInstance().get(PaymentTerm.class, paymentTermId);
    Currency testCurrency = OBDal.getInstance().get(Currency.class, currencyId);
    Product testProduct = OBDal.getInstance().get(Product.class, productId);
    UOM uom = TestUtility.getOneInstance(UOM.class, new Value(UOM.PROPERTY_NAME, testProduct
        .getUOM().getName()));
    TaxRate testTaxRate = OBDal.getInstance().get(TaxRate.class, taxId);
    DocumentType testDocumentType = OBDal.getInstance().get(DocumentType.class, docTypeId);

    FIN_FinancialAccount testAccount = TestUtility.insertFinancialAccount("APRM_FINACC_PAYMENT_01",
        STANDARD_DESCRIPTION, testCurrency, CASH, false,
        getOneInstance(org.openbravo.model.common.geography.Location.class), testBusinessPartner,
        null, null, null, null, null, null, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO,
        null, true, true);

    FIN_PaymentMethod testPaymentMethod = TestUtility.insertPaymentMethod("APRM_PM_PAYMENT_01",
        STANDARD_DESCRIPTION, true, false, false, MANUAL_EXECUTION, null, false,
        IN_TRANSIT_ACCOUNT, DEPOSIT_ACCOUNT, CLEARED_ACCOUNT, true, false, false, MANUAL_EXECUTION,
        null, false, IN_TRANSIT_ACCOUNT, WITHDRAWN_ACCOUNT, CLEARED_ACCOUNT, true, true);

    FinAccPaymentMethod existAssociation = TestUtility.getOneInstance(FinAccPaymentMethod.class,
        new Value(FinAccPaymentMethod.PROPERTY_ACCOUNT, testAccount), new Value(
            FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, testPaymentMethod));

    if (existAssociation == null)
      TestUtility.associatePaymentMethod(testAccount, testPaymentMethod);

    this.financialAccountId = testAccount.getId();
    this.paymentMethodId = testPaymentMethod.getId();

    Invoice invoice = TestUtility.createNewInvoice(OBContext.getOBContext().getCurrentClient(),
        OBContext.getOBContext().getCurrentOrganization(), new Date(), new Date(), new Date(),
        testDocumentType, testBusinessPartner, location, testPriceList, testCurrency,
        testPaymentMethod, testPaymentTerm, testProduct, uom, invoicedQuantity, netUnitPrice,
        netListPrice, priceLimit, testTaxRate, lineNetAmount, false);

    TestUtility.processInvoice(invoice);

    return invoice;
  }
}
