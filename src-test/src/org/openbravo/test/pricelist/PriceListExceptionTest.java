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
 * All portions are Copyright (C) 2022 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.test.pricelist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.pricing.pricelist.ProductPriceException;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.test.base.OBBaseTest;

/**
 * Tests cases to check Price Lists Exceptions
 */
@RunWith(Parameterized.class)
public class PriceListExceptionTest extends OBBaseTest {

  final static private Logger log = LogManager.getLogger();

  private final String USER_ID = "100";
  private final String ROLE_ID = "42D0EEB1C66F497A90DD526DC597E6F0";
  private final String PRODUCT_PRICE_WATER = "41732EFCA6374148BFD8B08C8B12DB73";
  private final String CLIENT_ID = "23C59575B9CF467C9620760EB255B389";
  private static final SimpleDateFormat dateFormat = JsonUtils.createDateFormat();
  private final static String ORGANIZATION_S_ID = "DC206C91AA6A4897B44DA897936E0EC3";
  private final static String ORGANIZATION_N_ID = "E443A31992CB4635AFCAEABE7183CE85";
  private final static String ORGANIZATION_FB_ID = "B843C30461EA4501935CB1D125C9C25A";

  private String org;
  private String date;
  private String priceExpected;

  public PriceListExceptionTest(String org, String date, String priceExpected) {
    this.org = org;
    this.date = date;
    this.priceExpected = priceExpected;
  }

  /** Parameterized possible combinations */
  @Parameters(name = "Organization id:{0} date:{1} price:{2}")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] { { ORGANIZATION_N_ID, "2022-07-30", "3" },
        { ORGANIZATION_S_ID, "2022-07-15", "2" }, { ORGANIZATION_FB_ID, "2022-07-10", "1" },
        { ORGANIZATION_N_ID, "2022-08-10", "1.53" } });
  }

  @Test
  public void testPriceListProductPricesException() {

    // Set context
    OBContext.setOBContext(USER_ID, ROLE_ID, CLIENT_ID, ORGANIZATION_FB_ID);

    try {
      ProductPrice productPrice = OBDal.getInstance().get(ProductPrice.class, PRODUCT_PRICE_WATER);
      // Create data test
      ProductPriceException productPriceExceptionFB = OBProvider.getInstance()
          .get(ProductPriceException.class);
      productPriceExceptionFB.setProductPrice(productPrice);
      productPriceExceptionFB
          .setOrganization(OBDal.getInstance().get(Organization.class, ORGANIZATION_FB_ID));
      productPriceExceptionFB.setStandardPrice(new BigDecimal(1));
      productPriceExceptionFB.setValidFromDate(dateFormat.parse("2022-07-01"));
      productPriceExceptionFB.setValidToDate(dateFormat.parse("2022-07-10"));
      OBDal.getInstance().save(productPriceExceptionFB);

      ProductPriceException productPriceExceptionS = OBProvider.getInstance()
          .get(ProductPriceException.class);
      productPriceExceptionS.setProductPrice(productPrice);
      productPriceExceptionS
          .setOrganization(OBDal.getInstance().get(Organization.class, ORGANIZATION_S_ID));
      productPriceExceptionS.setStandardPrice(new BigDecimal(2));
      productPriceExceptionS.setValidFromDate(dateFormat.parse("2022-07-11"));
      productPriceExceptionS.setValidToDate(dateFormat.parse("2022-07-24"));
      OBDal.getInstance().save(productPriceExceptionS);

      ProductPriceException productPriceExceptionN = OBProvider.getInstance()
          .get(ProductPriceException.class);
      productPriceExceptionN.setProductPrice(productPrice);
      productPriceExceptionN
          .setOrganization(OBDal.getInstance().get(Organization.class, ORGANIZATION_N_ID));
      productPriceExceptionN.setStandardPrice(new BigDecimal(3));
      productPriceExceptionN.setValidFromDate(dateFormat.parse("2022-07-25"));
      productPriceExceptionN.setValidToDate(dateFormat.parse("2022-07-31"));
      OBDal.getInstance().save(productPriceExceptionN);

      OBDal.getInstance().flush();

      BigDecimal price = FinancialUtils.getProductStdPrice(productPrice,
          OBDal.getInstance().get(Organization.class, org), dateFormat.parse(date));

      // Check if price is correct.
      assertTrue(price.equals(new BigDecimal(priceExpected)));

      // Delete data test.
      OBDal.getInstance().remove(productPriceExceptionFB);
      OBDal.getInstance().remove(productPriceExceptionS);
      OBDal.getInstance().remove(productPriceExceptionN);

      log.info("Test Completed successfully");
    }

    catch (Exception e) {
      log.error("Error when executing testPriceListProductPricesException", e);
      assertFalse(true);
    }
  }
}
