/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2015 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package org.openbravo.test.accounting;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletException;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.financial.ResetAccounting;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.test.base.OBBaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests cases to check taxes computation
 * 
 * 
 */
@RunWith(Parameterized.class)
public class PostDocumentTest extends OBBaseTest {
  final static private Logger log = LoggerFactory.getLogger(PostDocumentTest.class);
  // User Openbravo
  private final String USER_ID = "100";
  // Client QA Testing
  private static final String CLIENT_ID = "4028E6C72959682B01295A070852010D";
  // Organization Spain
  private static final String ORGANIZATION_SPAIN_ID = "357947E87C284935AD1D783CF6F099A1";
  // Role QA Testing Admin
  private final String ROLE_ID = "4028E6C72959682B01295A071429011E";
  // Table INVOICE
  private static final String TABLE_INVOICE = "318";
  // Accounting Schemas
  private static final String MAIN_EURO_LEGDER = "9A68A0F8D72D4580B3EC3CAA00A5E1F0";
  private static final String USA_DOLLAR_LEGDER = "432EAC71E1B8451E97C7F54718C4A06B";
  // Sales Invoice with documentNo: I/29
  private static final String INVOICE_TEST0 = "66AE9202889D47DE910DECD72A427DAC";
  // Purchase Invoice with documentNo: 10000018
  private static final String INVOICE_TEST1 = "F5D55960B2704B86903740BF35779DB6";
  // ACCOUNTS USED FOR TEST RESULTS
  private static String IVA_REPERCUTIDO = "3544375BB8414739B93813F54246B2E1";
  private static String IVA_SOPORTADO = "D04185C47CCA43B1A59DF318A6921E2B";
  private static String CLIENTES = "44D112E5F0CF4D1B8D4652EE282076F2";
  private static String VENTA_MERCADERIAS = "861F04DD57084B9882064312E7AC1EEF";

  private String testNumber;
  private String testDescription;
  private String keyId;
  private String tableId;
  private String orgId;
  private String[][] resultTest;

  public PostDocumentTest(String testNumber, String testDescription, String keyId, String tableId,
      String orgId, String[][] resultTest) {
    this.testDescription = testDescription;
    this.testNumber = testNumber;
    this.keyId = keyId;
    this.tableId = tableId;
    this.orgId = orgId;
    this.resultTest = resultTest;
  }

  /** parameterized: Documents to be posted together with results expected */
  @Parameters(name = "idx:{0} name:{1}")
  public static Collection<Object[]> params() {
    // { "Ledger", "Account", "date", "currency", "amtsourcedr", "amtsourcecr", "amtacctdr",
    // "amtacctcr"
    // },
    String[][] resultTest1 = {
        { MAIN_EURO_LEGDER, VENTA_MERCADERIAS, "18-07-2014", "102", "0.00", "200.00", "0.00",
            "200.00" },
        { MAIN_EURO_LEGDER, IVA_REPERCUTIDO, "18-07-2014", "102", "0.00", "1.00", "0.00", "1.00" },
        { MAIN_EURO_LEGDER, IVA_REPERCUTIDO, "18-07-2014", "102", "0.00", "6.00", "0.00", "6.00" },
        { MAIN_EURO_LEGDER, CLIENTES, "18-07-2014", "102", "207.00", "0.00", "207.00", "0.00" } };
    String[][] resultTest2 = {
        { MAIN_EURO_LEGDER, IVA_SOPORTADO, "04-05-2015", "102", "10.00", "0.00", "10.00", "0.00" },
        { MAIN_EURO_LEGDER, IVA_SOPORTADO, "04-05-2015", "102", "-10.00", "0.00", "-10.00", "0.00" } };

    return Arrays.asList(new Object[][] {
        { "1", "Sales invoice I/29", INVOICE_TEST0, TABLE_INVOICE, ORGANIZATION_SPAIN_ID,
            resultTest1 },
        // Purchase invoice of zero amount but with taxes: Issue 29266
        { "2", "Purchase Invoice ", INVOICE_TEST1, TABLE_INVOICE, ORGANIZATION_SPAIN_ID,
            resultTest2 } });
  }

  /*
   * Posts a document and verifies entries are correct
   */
  @Test
  public void testPostDocument() {
    try {
      ResetAccounting.delete(CLIENT_ID, orgId, tableId, keyId, "", "");
      postDocument();
      checkResults();
    } catch (ServletException e) {
      log.error("Error posting document", e);
    }
  }

  private void checkResults() {
    OBCriteria<AccountingFact> obc = OBDal.getInstance().createCriteria(AccountingFact.class);
    obc.add(Restrictions.eq(AccountingFact.PROPERTY_RECORDID, keyId));
    obc.add(Restrictions.eq(AccountingFact.PROPERTY_TABLE + ".id", tableId));
    obc.setFilterOnReadableClients(false);
    obc.setFilterOnReadableOrganization(false);
    obc.addOrder(Order.asc(AccountingFact.PROPERTY_CREATIONDATE));
    int counter = 0;
    for (AccountingFact fact : obc.list()) {
      String[] result = resultTest[counter];
      counter++;
      String ledger = result[0];
      String accountId = result[1];
      String date = result[2];
      String currencyId = result[3];
      String amtSourceDr = result[4];
      String amtSourceCr = result[5];
      String amtAcctDr = result[6];
      String amtAcctCr = result[7];
      BigDecimal amtAcctCrObtained = fact.getCredit();
      BigDecimal amtAcctDrObtained = fact.getDebit();
      BigDecimal amtSourceCrObtained = fact.getForeignCurrencyCredit();
      BigDecimal amtSourceDrObtained = fact.getForeignCurrencyDebit();
      // log.info(fact.getAccount().getIdentifier());
      // log.info(OBDateUtils.formatDate(fact.getAccountingDate()));
      // log.info(fact.getCurrency().getId());
      // log.info(fact.getCredit().toString());
      // log.info(fact.getDebit().toString());
      // log.info(fact.getForeignCurrencyCredit().toString());
      // log.info(fact.getForeignCurrencyDebit().toString());
      assertThat("Wrong amtSourceCr", new BigDecimal(amtSourceCr),
          closeTo(amtSourceCrObtained, BigDecimal.ZERO));
      assertThat("Wrong amtSourceDr", new BigDecimal(amtSourceDr),
          closeTo(amtSourceDrObtained, BigDecimal.ZERO));
      assertThat("Wrong amtAcctCr", new BigDecimal(amtAcctCr),
          closeTo(amtAcctCrObtained, BigDecimal.ZERO));
      assertThat("Wrong amtAcctDr", new BigDecimal(amtAcctDr),
          closeTo(amtAcctDrObtained, BigDecimal.ZERO));
      assertThat("Wrong Date", date, equalTo(OBDateUtils.formatDate(fact.getAccountingDate())));
      assertThat("Wrong Leger", ledger, equalTo(fact.getAccountingSchema().getId()));
      assertThat("Wrong Account", accountId, equalTo(fact.getAccount().getId()));
      assertThat("Wrong Currency", currencyId, equalTo(fact.getCurrency().getId()));
    }
    assertTrue("Wrong number of entries. Expected: " + resultTest.length + " obtained: " + counter,
        resultTest.length == counter);
  }

  private void postDocument() throws ServletException {
    ConnectionProvider conn = getConnectionProvider();
    Connection con = null;
    try {
      con = conn.getTransactionConnection();
      AcctServer acct = AcctServer.get(tableId, CLIENT_ID, orgId, conn);
      if (acct == null) {
        conn.releaseRollbackConnection(con);
        return;
      } else if (!acct.post(keyId, false, new VariablesSecureApp(USER_ID, CLIENT_ID, orgId), conn,
          con) || acct.errors != 0) {
        conn.releaseRollbackConnection(con);
        return;
      }
      conn.releaseCommitConnection(con);
      OBDal.getInstance().commitAndClose();
    } catch (Exception e) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
    }
    return;
  }
}