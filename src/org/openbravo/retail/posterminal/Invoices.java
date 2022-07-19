/*
 ************************************************************************************
 * Copyright (C) 2018-2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;
import org.openbravo.model.ad.access.InvoiceLineTax;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.InvoiceLineOffer;
import org.openbravo.service.json.JsonConstants;

public class Invoices extends JSONProcessSimple {
  public static final Logger log = LogManager.getLogger();

  public static final String invoicesPropertyExtension = "InvoiceExtension";
  public static final String invoicesLinesPropertyExtension = "InvoiceExtensionLines";
  public static final String invoicesShipLinesPropertyExtension = "InvoiceExtensionShipLines";
  public static final String invoicesRelatedLinesPropertyExtension = "InvoiceExtensionRelatedLines";
  public static final String invoicesPaymentsPropertyExtension = "InvoiceExtensionPayments";

  @Inject
  @Any
  @Qualifier(invoicesPropertyExtension)
  private Instance<ModelExtension> extensions;
  @Inject
  @Any
  @Qualifier(invoicesLinesPropertyExtension)
  private Instance<ModelExtension> extensionsLines;
  @Inject
  @Any
  @Qualifier(invoicesShipLinesPropertyExtension)
  private Instance<ModelExtension> extensionsShipLines;
  @Inject
  @Any
  @Qualifier(invoicesRelatedLinesPropertyExtension)
  private Instance<ModelExtension> extensionsRelatedLines;
  @Inject
  @Any
  @Qualifier(invoicesPaymentsPropertyExtension)
  private Instance<ModelExtension> extensionsPayments;

  @Override
  public JSONObject exec(JSONObject jsonsent) throws JSONException, ServletException {
    JSONObject result = new JSONObject();
    OBContext.setAdminMode(true);
    try {
      final DateFormat parseDateFormat = (DateFormat) POSUtils.dateFormatUTC.clone();
      parseDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      final DateFormat orderDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      orderDateFormat
          .setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID()));
      final DateFormat paymentDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
      paymentDateFormat
          .setTimeZone(TimeZone.getTimeZone(Calendar.getInstance().getTimeZone().getID()));

      JSONArray respArray = new JSONArray();

      String invoiceid = jsonsent.getString("invoiceid");

      // get the invoice
      HQLPropertyList hqlPropertiesReceipts = ModelExtensionUtils.getPropertyExtensions(extensions);
      String hqlInvoices = "select " + hqlPropertiesReceipts.getHqlSelect()
          + " from Invoice as inv LEFT JOIN inv.salesRepresentative as salesRepresentative "
          + " LEFT JOIN inv.salesOrder ord  LEFT JOIN ord.obposApplications pos"
          + " where inv.id = :invoiceId";
      @SuppressWarnings("rawtypes")
      Query invoicesQuery = OBDal.getInstance().getSession().createQuery(hqlInvoices);
      invoicesQuery.setParameter("invoiceId", invoiceid);

      JSONArray invoices = hqlPropertiesReceipts.getJSONArray(invoicesQuery);

      for (int receipt = 0; receipt < invoices.length(); receipt++) {
        JSONObject invoice = invoices.getJSONObject(receipt);
        Organization org = OBDal.getInstance().get(Organization.class, invoice.get("organization"));
        invoice.put("orderid", invoiceid);
        invoice.put("generateInvoice", true);

        try {
          Date date = parseDateFormat.parse(invoice.getString("orderDate"));
          invoice.put("orderDate", orderDateFormat.format(date));
        } catch (ParseException e) {
          log.error(e.getMessage(), e);
        }

        JSONArray listinvoicesLines = new JSONArray();

        // get the details of each line
        HQLPropertyList hqlPropertiesLines = ModelExtensionUtils
            .getPropertyExtensions(extensionsLines);
        String hqlInvoicesLines = "select " + hqlPropertiesLines.getHqlSelect() //
            + " from InvoiceLine as invLine" //
            + " where invLine.invoice.id=:invoiceId" //
            + " order by invLine.lineNo";
        @SuppressWarnings("rawtypes")
        Query invoicesLinesQuery = OBDal.getInstance().getSession().createQuery(hqlInvoicesLines);
        invoicesLinesQuery.setParameter("invoiceId", invoiceid);

        JSONArray invoicesLines = hqlPropertiesLines.getJSONArray(invoicesLinesQuery);

        for (int receiptLine = 0; receiptLine < invoicesLines.length(); receiptLine++) {
          JSONObject invoiceLine = invoicesLines.getJSONObject(receiptLine);

          invoiceLine.put("priceIncludesTax", invoice.getBoolean("priceIncludesTax"));

          // promotions per line
          OBCriteria<InvoiceLineOffer> qPromotions = OBDal.getInstance()
              .createCriteria(InvoiceLineOffer.class);
          qPromotions.add(Restrictions.eq(InvoiceLineOffer.PROPERTY_INVOICELINE + ".id",
              (String) invoiceLine.getString("lineId")));
          if (org.getOBRETCOCrossStoreOrganization() != null) {
            qPromotions.setFilterOnReadableOrganization(false);
          }
          qPromotions.addOrder(Order.asc(InvoiceLineOffer.PROPERTY_LINENO));
          JSONArray promotions = new JSONArray();
          boolean hasPromotions = false;
          for (InvoiceLineOffer promotion : qPromotions.list()) {
            BigDecimal displayedAmount = promotion.getDisplayedTotalAmount();
            if (displayedAmount == null) {
              displayedAmount = promotion.getTotalAmount();
            }

            JSONObject jsonPromo = new JSONObject();
            String name = promotion.getPriceAdjustment().getPrintName() != null
                ? promotion.getPriceAdjustment().getPrintName()
                : promotion.getPriceAdjustment().getName();
            jsonPromo.put("ruleId", promotion.getPriceAdjustment().getId());
            jsonPromo.put("name", name);
            jsonPromo.put("amt", displayedAmount);
            jsonPromo.put("actualAmt", promotion.getTotalAmount());
            jsonPromo.put("hidden", BigDecimal.ZERO.equals(displayedAmount));
            promotions.put(jsonPromo);
            hasPromotions = true;
          }

          BigDecimal lineAmount;
          if (hasPromotions) {
            // When it has promotions, show line amount without them as they are shown after it
            lineAmount = (new BigDecimal(invoiceLine.optString("quantity"))
                .multiply(new BigDecimal(invoiceLine.optString("unitPrice"))));
          } else {
            lineAmount = new BigDecimal(invoiceLine.optString("lineGrossAmount"));
          }
          invoiceLine.put("lineGrossAmount", lineAmount);

          invoiceLine.put("promotions", promotions);

          // taxes per line
          OBCriteria<InvoiceLineTax> qTaxes = OBDal.getInstance()
              .createCriteria(InvoiceLineTax.class);
          qTaxes.add(Restrictions.eq(InvoiceLineTax.PROPERTY_INVOICELINE + ".id",
              (String) invoiceLine.getString("lineId")));
          if (org.getOBRETCOCrossStoreOrganization() != null) {
            qTaxes.setFilterOnReadableOrganization(false);
          }
          qTaxes.addOrder(Order.asc(InvoiceLineTax.PROPERTY_LINENO));
          JSONArray taxes = new JSONArray();
          for (InvoiceLineTax tax : qTaxes.list()) {
            JSONObject jsonTax = new JSONObject();
            jsonTax.put("taxId", tax.getTax().getId());
            jsonTax.put("identifier", tax.getTax().getName());
            jsonTax.put("taxAmount", tax.getTaxAmount());
            jsonTax.put("taxableAmount", tax.getTaxableAmount());
            jsonTax.put("taxRate", tax.getTax().getRate());
            jsonTax.put("docTaxAmount", tax.getTax().getDocTaxAmount());
            jsonTax.put("lineNo", tax.getTax().getLineNo());
            jsonTax.put("cascade", tax.getTax().isCascade());
            taxes.put(jsonTax);
          }

          invoiceLine.put("taxes", taxes);

          listinvoicesLines.put(invoiceLine);
        }
        invoice.put("receiptLines", listinvoicesLines);

        HQLPropertyList hqlPropertiesPayments = ModelExtensionUtils
            .getPropertyExtensions(extensionsPayments);
        String hqlPaymentsIn = "select " + hqlPropertiesPayments.getHqlSelect()
            + "from FIN_Payment_ScheduleDetail as scheduleDetail "
            + "join scheduleDetail.paymentDetails as paymentDetail "
            + "join paymentDetail.finPayment as finPayment "
            + "join scheduleDetail.invoicePaymentSchedule.invoice as invoice "
            + "where invoice.id=:invoiceId " //
            + "order by finPayment.documentNo";

        @SuppressWarnings("rawtypes")
        Query paymentsInQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentsIn);
        paymentsInQuery.setParameter("invoiceId", invoiceid);
        JSONArray listPaymentsIn = hqlPropertiesPayments.getJSONArray(paymentsInQuery);

        JSONArray listinvoicesPayments = new JSONArray();
        JSONArray listPaymentsType = new JSONArray();

        String hqlPaymentsType = "select p.commercialName as name, p.financialAccount.id as account, p.searchKey as searchKey, "
            + "obpos_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as rate, "
            + "obpos_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id) as mulrate, "
            + "p.financialAccount.currency.iSOCode as isocode, "
            + "p.paymentMethod.openDrawer as openDrawer " + "from FIN_Payment_Schedule as ps "
            + "join ps.fINPaymentScheduleDetailInvoicePaymentScheduleList psd "
            + "join psd.paymentDetails pd " + "join pd.finPayment fp "
            + "join OBPOS_App_Payment p with fp.account = p.financialAccount "
            + "where ps.invoice.id=:invoiceId "
            + "group by  p.financialAccount.id, p.commercialName ,p.searchKey, "
            + "obpos_currency_rate(p.financialAccount.currency, p.obposApplications.organization.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id), "
            + "obpos_currency_rate(p.obposApplications.organization.currency, p.financialAccount.currency, null, null, p.obposApplications.client.id, p.obposApplications.organization.id), "
            + "p.financialAccount.currency.iSOCode, p.paymentMethod.openDrawer, p.active "
            + "order by p.active desc";
        Query<Object[]> paymentsTypeQuery = OBDal.getInstance()
            .getSession()
            .createQuery(hqlPaymentsType, Object[].class);
        paymentsTypeQuery.setParameter("invoiceId", invoiceid);

        List<Object[]> paymentsTypeQueryList = paymentsTypeQuery.list()
            .stream()
            .reduce(new ArrayList<>(), (List<Object[]> pml, Object[] pm) -> {
              if (pml.stream().noneMatch(p -> p[1].equals(pm[1]))) {
                pml.add(pm);
              }
              return pml;
            }, (pm1, pm2) -> {
              pm1.addAll(pm2);
              return pm1;
            });

        for (Object[] objPaymentsType : paymentsTypeQueryList) {
          JSONObject paymentsType = new JSONObject();
          paymentsType.put("name", objPaymentsType[0]);
          paymentsType.put("account", objPaymentsType[1]);
          paymentsType.put("kind", objPaymentsType[2]);
          paymentsType.put("rate", objPaymentsType[3]);
          paymentsType.put("mulrate", objPaymentsType[4]);
          paymentsType.put("isocode", objPaymentsType[5]);
          paymentsType.put("openDrawer", objPaymentsType[6]);
          listPaymentsType.put(paymentsType);
        }
        for (int i = 0; i < listPaymentsIn.length(); i++) {
          JSONObject objectIn = (JSONObject) listPaymentsIn.get(i);
          boolean added = false;
          for (int j = 0; j < listPaymentsType.length(); j++) {
            JSONObject objectType = (JSONObject) listPaymentsType.get(j);
            if (objectIn.get("account").equals(objectType.get("account"))) {
              JSONObject invoicePayment = new JSONObject();
              invoicePayment.put("amount",
                  new BigDecimal((String) objectIn.get("amount").toString())
                      .multiply(new BigDecimal((String) objectType.get("mulrate").toString())));
              try {
                Date date = parseDateFormat.parse((String) objectIn.get("paymentDate"));
                invoicePayment.put("paymentDate", paymentDateFormat.format(date));
              } catch (ParseException e) {
                log.error(e.getMessage(), e);
              }
              if (objectIn.has("paymentData")) {
                invoicePayment.put("paymentData",
                    new JSONObject((String) objectIn.get("paymentData")));
              }
              invoicePayment.put("name", objectType.get("name"));
              invoicePayment.put("kind", objectType.get("kind"));
              invoicePayment.put("rate", objectType.get("rate"));
              invoicePayment.put("mulrate", objectType.get("mulrate"));
              invoicePayment.put("isocode", objectType.get("isocode"));
              invoicePayment.put("openDrawer", objectType.get("openDrawer"));
              invoicePayment.put("isPrePayment", true);
              added = true;
              listinvoicesPayments.put(invoicePayment);
            }
          }
          if (!added) {
            // The payment type of the current payment is not configured for the webpos

            String hqlPaymentType = "select p.paymentMethod.name as name, p.account.id as account, "
                + "c_currency_rate(p.account.currency, p.organization.currency, null, null, p.client.id, p.organization.id) as rate, "
                + "c_currency_rate(p.organization.currency, p.account.currency, null, null, p.client.id, p.organization.id) as mulrate, "
                + "p.account.currency.iSOCode as isocode "
                + " from FIN_Payment as p where p.id=:paymentId";
            @SuppressWarnings("rawtypes")
            Query paymentTypeQuery = OBDal.getInstance().getSession().createQuery(hqlPaymentType);
            paymentTypeQuery.setParameter("paymentId", objectIn.getString("paymentId"));

            if (paymentTypeQuery.list().size() > 0) {

              Object objPaymentType = paymentTypeQuery.list().get(0);
              Object[] objPaymentsType = (Object[]) objPaymentType;
              JSONObject paymentsType = new JSONObject();
              paymentsType.put("name", objPaymentsType[0]);
              paymentsType.put("account", objPaymentsType[1]);
              paymentsType.put("kind", "");
              paymentsType.put("rate", objPaymentsType[2]);
              paymentsType.put("mulrate", objPaymentsType[3]);
              paymentsType.put("isocode", objPaymentsType[4]);
              paymentsType.put("openDrawer", "N");

              JSONObject invoicePayment = new JSONObject();
              invoicePayment.put("amount",
                  new BigDecimal((String) objectIn.get("amount").toString())
                      .multiply(new BigDecimal((String) paymentsType.get("mulrate").toString())));
              try {
                Date date = parseDateFormat.parse((String) objectIn.get("paymentDate"));
                invoicePayment.put("paymentDate", paymentDateFormat.format(date));
              } catch (ParseException e) {
                log.error(e.getMessage(), e);
              }
              if (objectIn.has("paymentData")) {
                invoicePayment.put("paymentData",
                    new JSONObject((String) objectIn.get("paymentData")));
              }
              invoicePayment.put("name", paymentsType.get("name"));
              invoicePayment.put("kind", paymentsType.get("kind"));
              invoicePayment.put("rate", paymentsType.get("rate"));
              invoicePayment.put("mulrate", paymentsType.get("mulrate"));
              invoicePayment.put("isocode", paymentsType.get("isocode"));
              invoicePayment.put("openDrawer", paymentsType.get("openDrawer"));
              invoicePayment.put("isPrePayment", true);
              added = true;
              listinvoicesPayments.put(invoicePayment);
            }
          }
        }

        invoice.put("receiptPayments", listinvoicesPayments);

        // TODO: make this extensible
        String hqlReceiptTaxes = "select invoiceTax.tax.id as taxId, invoiceTax.tax.rate as rate, "
            + "invoiceTax.taxableAmount as taxableamount, invoiceTax.taxAmount as taxamount, invoiceTax.tax.name as name, "
            + "invoiceTax.tax.cascade as cascade, invoiceTax.tax.docTaxAmount as docTaxAmount, invoiceTax.lineNo as lineNo, invoiceTax.tax.taxBase.id as taxBase "
            + "from InvoiceTax as invoiceTax where invoiceTax.invoice.id=:invoiceId "
            + "order by invoiceTax.lineNo ";
        @SuppressWarnings("rawtypes")
        Query ReceiptTaxesQuery = OBDal.getInstance().getSession().createQuery(hqlReceiptTaxes);
        ReceiptTaxesQuery.setParameter("invoiceId", invoiceid);
        JSONArray jsonListTaxes = new JSONArray();
        for (Object objTax : ReceiptTaxesQuery.list()) {
          Object[] objTaxInfo = (Object[]) objTax;
          JSONObject jsonObjTaxes = new JSONObject();
          jsonObjTaxes.put("taxid", objTaxInfo[0]);
          jsonObjTaxes.put("rate", objTaxInfo[1]);
          jsonObjTaxes.put("net", objTaxInfo[2]);
          jsonObjTaxes.put("amount", objTaxInfo[3]);
          jsonObjTaxes.put("name", objTaxInfo[4]);
          jsonObjTaxes.put("gross", new BigDecimal((String) objTaxInfo[2].toString())
              .add(new BigDecimal((String) objTaxInfo[3].toString())));
          jsonObjTaxes.put("cascade", objTaxInfo[5]);
          jsonObjTaxes.put("docTaxAmount", objTaxInfo[6]);
          jsonObjTaxes.put("lineNo", objTaxInfo[7]);
          jsonObjTaxes.put("taxBase", objTaxInfo[8]);
          jsonListTaxes.put(jsonObjTaxes);
        }

        invoice.put("receiptTaxes", jsonListTaxes);

        respArray.put(invoice);

        result.put(JsonConstants.RESPONSE_DATA, respArray);
        result.put(JsonConstants.RESPONSE_STATUS, JsonConstants.RPCREQUEST_STATUS_SUCCESS);
      }
    } finally {

      OBContext.restorePreviousMode();
    }
    return result;
  }
}
