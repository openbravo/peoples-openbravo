package org.openbravo.advpaymentmngt.hqlinjections;

import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.datasource.hql.HqlQueryTransformer;

@ComponentProvider.Qualifier("58AF4D3E594B421A9A7307480736F03E")
public class AddPaymentOrderInvoicesTransformer extends HqlQueryTransformer {

  @Override
  public String transformHqlQuery(String hqlQuery, Map<String, String> requestParameters,
      Map<String, Object> queryNamedParameters) {
    // Retrieve Parameters
    String transactionType = requestParameters.get("transaction_type");
    final String strInvoiceId = requestParameters.get("@Invoice.id@");
    // Initialize Variables
    boolean isSalesTransaction = "true".equals(requestParameters.get("@Invoice.salesTransaction@")) ? true : false;
    String transformedHql = null;
    StringBuffer selectClause = new StringBuffer();
    StringBuffer whereClause = new StringBuffer();
    StringBuffer groupByClause = new StringBuffer();

    if (strInvoiceId != null) {

      final Invoice invoice = OBDal.getInstance().get(Invoice.class, strInvoiceId);
      isSalesTransaction = invoice.isSalesTransaction();

      if ("I".equals(transactionType)) {

        // Create Select Clause
        selectClause.append(" array_to_string(array_agg(psd.id), ',') as paymentScheduleDetail, ");
        selectClause.append(" array_to_string(array_agg(ord.documentNo), ',') as salesOrderNo, ");
        selectClause.append(" inv.documentNo as invoiceNo, ");
        selectClause
            .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
        selectClause
            .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
        selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
        selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
        selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
        selectClause
            .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
        selectClause.append(" SUM(psd.amount) as outstandingAmount, ");
        selectClause.append(" 0 as amount ");

        // Create WhereClause
        whereClause.append(" psd.paymentDetails is null ");
        whereClause.append(" and (oinfo is null or oinfo.active = true) ");
        whereClause.append(" and ((inv is not null ");
        if (invoice.getBusinessPartner() != null) {
          whereClause.append(" and inv.businessPartner.id = '"
              + invoice.getBusinessPartner().getId() + "'");
        }
        if (invoice.getPaymentMethod() != null) {
          whereClause.append(" and inv.paymentMethod.id = '" + invoice.getPaymentMethod().getId()
              + "'");
        }
        whereClause.append(" and inv.salesTransaction = " + isSalesTransaction);
        whereClause.append(" and inv.currency.id = '" + invoice.getCurrency().getId() + "' )) ");

        // Create GroupBy Clause
        groupByClause.append(" inv.documentNo, ");
        groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
        groupByClause.append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id), ");
        groupByClause.append(" COALESCE(inv.invoiceDate, ord.orderDate), ");
        groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
        groupByClause.append(" COALESCE(ips.amount, ops.amount), ");
        groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) ");

        // Replace where filters and having count clause
        if (requestParameters.containsKey("criteria")) {
          String criteria = requestParameters.get("criteria");
          hqlQuery = replaceFiltersAndHavingClause(hqlQuery, criteria, transactionType);
        } else {
          hqlQuery = hqlQuery.replace("@havingClause@", "");
        }

      } else if ("O".equals(transactionType)) {

        // Create Select Clause
        selectClause.append(" array_to_string(array_agg(psd.id), ',') as paymentScheduleDetail, ");
        selectClause.append(" ord.documentNo as salesOrderNo, ");
        selectClause.append(" array_to_string(array_agg(inv.documentNo), ',') as invoiceNo, ");
        selectClause
            .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
        selectClause
            .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
        selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
        selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
        selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
        selectClause
            .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
        selectClause.append(" SUM(psd.amount) as outstandingAmount, ");
        selectClause.append(" 0 as amount ");

        // Create WhereClause
        whereClause.append(" psd.paymentDetails is null ");
        whereClause.append(" and (oinfo is null or oinfo.active = true) ");
        whereClause.append(" and ((ord is not null ");
        if (invoice.getBusinessPartner() != null) {
          whereClause.append(" and ord.businessPartner.id = '"
              + invoice.getBusinessPartner().getId() + "'");
        }
        if (invoice.getPaymentMethod() != null) {
          whereClause.append(" and ord.paymentMethod.id = '" + invoice.getPaymentMethod().getId()
              + "'");
        }
        whereClause.append(" and ord.salesTransaction = " + isSalesTransaction);
        whereClause.append(" and ord.currency.id = '" + invoice.getCurrency().getId() + "' )) ");

        // Create GroupBy Clause
        groupByClause.append(" ord.documentNo, ");
        groupByClause.append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id), ");
        groupByClause.append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id), ");
        groupByClause.append(" COALESCE(inv.invoiceDate, ord.orderDate), ");
        groupByClause.append(" COALESCE(ips.expectedDate, ops.expectedDate), ");
        groupByClause.append(" COALESCE(ips.amount, ops.amount), ");
        groupByClause.append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) ");

        // Replace where filters and having count clause
        if (requestParameters.containsKey("criteria")) {
          String criteria = requestParameters.get("criteria");
          hqlQuery = replaceFiltersAndHavingClause(hqlQuery, criteria, transactionType);
        } else {
          hqlQuery = hqlQuery.replace("@havingClause@", "");
        }

      } else {
        // Create Select Clause
        selectClause.append(" psd.id as paymentScheduleDetail, ");
        selectClause.append(" ord.documentNo as salesOrderNo, ");
        selectClause.append(" inv.documentNo as invoiceNo, ");
        selectClause
            .append(" COALESCE(ips.finPaymentmethod.id, ops.finPaymentmethod.id) as paymentMethod, ");
        selectClause
            .append(" COALESCE(inv.businessPartner.id, ord.businessPartner.id) as businessPartner, ");
        selectClause.append(" COALESCE(inv.invoiceDate, ord.orderDate) as transactionDate, ");
        selectClause.append(" COALESCE(ips.expectedDate, ops.expectedDate) as expectedDate, ");
        selectClause.append(" COALESCE(ips.amount, ops.amount) as expectedAmount, ");
        selectClause
            .append(" COALESCE(inv.grandTotalAmount, ord.grandTotalAmount) as invoicedAmount, ");
        selectClause.append(" psd.amount as outstandingAmount, ");
        selectClause.append(" 0 as amount ");

        // Create WhereClause
        whereClause.append(" psd.paymentDetails is null ");
        whereClause.append(" and (oinfo is null or oinfo.active = true) ");
        whereClause.append(" and ((inv is not null ");
        if (invoice.getBusinessPartner() != null) {
          whereClause.append(" and inv.businessPartner.id = '"
              + invoice.getBusinessPartner().getId() + "'");
        }
        if (invoice.getPaymentMethod() != null) {
          whereClause.append(" and inv.paymentMethod.id = '" + invoice.getPaymentMethod().getId()
              + "'");
        }
        whereClause.append(" and inv.salesTransaction = " + isSalesTransaction);
        whereClause.append(" and inv.currency.id = '" + invoice.getCurrency().getId() + "' ) ");
        whereClause.append(" or (ord is not null ");
        if (invoice.getBusinessPartner() != null) {
          whereClause.append(" and ord.businessPartner.id = '"
              + invoice.getBusinessPartner().getId() + "'");
        }
        if (invoice.getPaymentMethod() != null) {
          whereClause.append(" and ord.paymentMethod.id = '" + invoice.getPaymentMethod().getId()
              + "'");
        }
        whereClause.append(" and ord.salesTransaction = " + isSalesTransaction);
        whereClause.append(" and ord.currency.id = '" + invoice.getCurrency().getId() + "' )) ");

        // There is no Group By Clause
        hqlQuery = hqlQuery.replace("group by", "");

        // Replace where filters and having count clause
        if (requestParameters.containsKey("criteria")) {
          String criteria = requestParameters.get("criteria");
          hqlQuery = replaceFiltersAndHavingClause(hqlQuery, criteria, transactionType);
        } else {
          hqlQuery = hqlQuery.replace("@havingClause@", "");
        }
      }

      // Remove alias @@ from Order By clause
      if (requestParameters.containsKey("_sortBy")) {
        String sortBy = requestParameters.get("_sortBy");
        if (sortBy.startsWith("-")) {
          sortBy = sortBy.substring(1);
        }
        hqlQuery = hqlQuery.replace("@" + sortBy + "@", sortBy);
      }

      transformedHql = hqlQuery.replace("@selectClause@ ", selectClause.toString());
      transformedHql = transformedHql.replace("@whereClause@ ", whereClause.toString());
      transformedHql = transformedHql.replace("@groupByClause@", groupByClause.toString());
    }

    return transformedHql;
  }

  /**
   * @param hqlQuery
   *          : original hql query
   * @param criteria
   *          : filters of the query
   * @param transactionType
   *          : Invoices, Orders or Both
   * @return
   */
  private String replaceFiltersAndHavingClause(String hqlQuery, String criteria,
      String transactionType) {
    String[] fieldsInCriteria = criteria.split("__;__");
    StringBuffer havingClause = new StringBuffer();

    try {
      if ("I".equals(transactionType)) {
        for (int i = 0; i < fieldsInCriteria.length; i++) {
          JSONObject jsonCriteria = new JSONObject(fieldsInCriteria[i]);
          String fieldName = jsonCriteria.getString("fieldName");
          if ("salesOrderNo".equals(fieldName)) {
            if (havingClause.length() <= 0) {
              havingClause.append(" having (");
            }
            String strToReplace = "@" + fieldName + "@";
            String strToReplaceBeginning = "upper(@salesOrderNo@)";
            String strToReplaceWith = "array_to_string(array_agg(ord.documentNo), ',')";
            String strToRemoveFromQuery = replaceHavingClause(hqlQuery, strToReplace,
                strToReplaceBeginning, strToReplaceWith, havingClause);
            if (strToRemoveFromQuery.toLowerCase().contains(" and")) {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 1=1 and ");
            } else {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 1=1 ");
            }
            String strReplaced = strToRemoveFromQuery.replace(strToReplace, strToReplaceWith);
            // Remove and from having count
            if (strReplaced.toLowerCase().contains(" and")) {
              strReplaced = strReplaced.substring(0, strReplaced.indexOf(" and"));
            }
            havingClause.append(strReplaced);

          } else if ("invoiceNo".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@invoiceNo@", "inv.documentNo");
          } else if ("paymentMethod".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@paymentMethod@", "ips.finPaymentmethod.id");
          } else if ("businessPartner".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@businessPartner@", "inv.businessPartner.id");
          } else if ("transactionDate".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@transactionDate@", "inv.invoiceDate");
          } else if ("expectedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@expectedAmount@", "ips.amount");
          } else if ("invoicedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@invoicedAmount@", "inv.grandTotalAmount");
          } else if ("outstandingAmount".equals(fieldName)) {
            // Remove the aggregate function from where clause and put in having clause
            if (havingClause.length() <= 0) {
              havingClause.append(" having (");
            } else {
              havingClause.append(" and ");
            }
            String strToReplace = "@" + fieldName + "@";
            String strToReplaceBeginning = "@outstandingAmount@";
            String strToReplaceWith = "SUM(psd.amount)";
            String strToRemoveFromQuery = replaceHavingClause(hqlQuery, strToReplace,
                strToReplaceBeginning, strToReplaceWith, havingClause);
            if (strToRemoveFromQuery.toLowerCase().contains(" and")) {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 2=2 and ");
            } else {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 2=2 ");
            }
            hqlQuery = hqlQuery.replace(strToRemoveFromQuery, "");
            String strReplaced = strToRemoveFromQuery.replace(strToReplace, strToReplaceWith);
            // Remove and
            if (strReplaced.toLowerCase().contains(" and")) {
              strReplaced = strReplaced.substring(0, strReplaced.indexOf(" and"));
            }
            havingClause.append(strReplaced);
          }
        }
      } else if ("O".equals(transactionType)) {
        for (int i = 0; i < fieldsInCriteria.length; i++) {
          JSONObject jsonCriteria = new JSONObject(fieldsInCriteria[i]);
          String fieldName = jsonCriteria.getString("fieldName");
          if ("salesOrderNo".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@salesOrderNo@", "ord.documentNo");
          } else if ("invoiceNo".equals(fieldName)) {
            if (havingClause.length() <= 0) {
              havingClause.append(" having (");
            }
            String strToReplace = "@" + fieldName + "@";
            String strToReplaceBeginning = "upper(@invoiceNo@)";
            String strToReplaceWith = "array_to_string(array_agg(inv.documentNo), ',')";
            String strToRemoveFromQuery = replaceHavingClause(hqlQuery, strToReplace,
                strToReplaceBeginning, strToReplaceWith, havingClause);
            if (strToRemoveFromQuery.toLowerCase().contains(" and")) {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 1=1 and ");
            } else {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 1=1 ");
            }
            String strReplaced = strToRemoveFromQuery.replace(strToReplace, strToReplaceWith);
            // Remove and from having count
            if (strReplaced.toLowerCase().contains(" and")) {
              strReplaced = strReplaced.substring(0, strReplaced.indexOf(" and"));
            }
            havingClause.append(strReplaced);
          } else if ("paymentMethod".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@paymentMethod@", "ops.finPaymentmethod.id");
          } else if ("businessPartner".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@businessPartner@", "ord.businessPartner.id");
          } else if ("transactionDate".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@transactionDate@", "ord.orderDate");
          } else if ("expectedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@expectedAmount@", "ops.amount");
          } else if ("invoicedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@invoicedAmount@", "ord.grandTotalAmount");
          } else if ("outstandingAmount".equals(fieldName)) {
            // Remove the aggregate function from where clause and put in having clause
            if (havingClause.length() <= 0) {
              havingClause.append(" having (");
            } else {
              havingClause.append(" and ");
            }
            String strToReplace = "@" + fieldName + "@";
            String strToReplaceBeginning = "@outstandingAmount@";
            String strToReplaceWith = "SUM(psd.amount)";
            String strToRemoveFromQuery = replaceHavingClause(hqlQuery, strToReplace,
                strToReplaceBeginning, strToReplaceWith, havingClause);
            if (strToRemoveFromQuery.toLowerCase().contains(" and")) {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 2=2 and ");
            } else {
              hqlQuery = hqlQuery.replace(strToRemoveFromQuery, " 2=2 ");
            }
            hqlQuery = hqlQuery.replace(strToRemoveFromQuery, "");
            String strReplaced = strToRemoveFromQuery.replace(strToReplace, strToReplaceWith);
            // Remove and
            if (strReplaced.toLowerCase().contains(" and")) {
              strReplaced = strReplaced.substring(0, strReplaced.indexOf(" and"));
            }
            havingClause.append(strReplaced);
          }
        }
      } else {
        for (int i = 0; i < fieldsInCriteria.length; i++) {
          JSONObject jsonCriteria = new JSONObject(fieldsInCriteria[i]);
          String fieldName = jsonCriteria.getString("fieldName");
          if ("salesOrderNo".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@salesOrderNo@", "ord.documentNo");
          } else if ("invoiceNo".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@invoiceNo@", "inv.documentNo");
          } else if ("paymentMethod".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@paymentMethod@", "ops.finPaymentmethod.id");
          } else if ("businessPartner".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@businessPartner@", "ord.businessPartner.id");
          } else if ("transactionDate".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@transactionDate@", "ord.orderDate");
          } else if ("expectedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@expectedAmount@", "ops.amount");
          } else if ("invoicedAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@invoicedAmount@", "ord.grandTotalAmount");
          } else if ("outstandingAmount".equals(fieldName)) {
            hqlQuery = hqlQuery.replace("@outstandingAmount@", "psd.amount");
          }
        }
      }
      havingClause.append(" )");
      hqlQuery = hqlQuery.replace("@havingClause@", havingClause.toString());
      return hqlQuery;
    } catch (JSONException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @param hqlQuery
   * @param strToReplace
   * @param strToReplaceBeginning
   * @param strToReplaceWith
   * @param havingClause
   * @return
   */
  private String replaceHavingClause(String hqlQuery, String strToReplace,
      String strToReplaceBeginning, String strToReplaceWith, StringBuffer havingClause) {
    String strReplaced = null;
    int beginIndex = hqlQuery.indexOf(strToReplaceBeginning);
    int endIndexParenthesis = hqlQuery.indexOf(" ) ", beginIndex);
    int endIndexAnd = hqlQuery.indexOf(" and ", beginIndex);
    int endIndex = 0;
    if (endIndexParenthesis < endIndexAnd || endIndexAnd == -1) {
      endIndex = endIndexParenthesis;
    } else {
      // + 4 to remove and sentence
      endIndex = endIndexAnd + 4;
    }
    strReplaced = hqlQuery.substring(beginIndex, endIndex);
    return strReplaced;
  }
}