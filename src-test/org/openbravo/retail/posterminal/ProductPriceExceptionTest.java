/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletException;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBDal;
import org.openbravo.mobile.core.master.MasterDataProcessHQLQuery.MasterDataModel;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.model.pricing.pricelist.PriceListSchema;
import org.openbravo.model.pricing.pricelist.PriceListVersion;
import org.openbravo.model.pricing.pricelist.ProductPrice;
import org.openbravo.model.pricing.pricelist.ProductPriceException;
import org.openbravo.retail.posterminal.master.Product;

public class ProductPriceExceptionTest extends PriceExceptionBaseTest {
  private static final String POS_VBS1 = "9104513C2D0741D4850AE8493998A7C8";
  private static final String AVALANCHE_TRANSRECEIVER_ID = "934E7D7587EC4C7A9E9FF58F0382D450";
  private static final String PRODUCT_PRICE_ID = "562921761FFE41869F2ADFDD2F7CCD36";

  @Inject
  @MasterDataModel("Product")
  private Product productMasterDataModel;

  public void createPriceException(String exceptionOrg, String price) {
    createPriceException(exceptionOrg, price, true);
  }

  public void createPriceException(String exceptionOrg, String price, boolean activeFlag) {
    Organization organization = OBDal.getInstance().getProxy(Organization.class, exceptionOrg);
    ProductPrice productPrice = OBDal.getInstance().getProxy(ProductPrice.class, PRODUCT_PRICE_ID);

    OBContext.setAdminMode();
    OBInterceptor.setPreventUpdateInfoChange(true);
    try {
      ProductPriceException ppe = OBProvider.getInstance().get(ProductPriceException.class);
      ppe.setClient(OBDal.getInstance().getProxy(Client.class, WHITE_VALLEY));
      ppe.setOrganization(organization);
      ppe.setValidFromDate(minusDaysFromCurrentDate(1));
      ppe.setValidToDate(plusDaysToCurrentDate(30));
      ppe.setStandardPrice(new BigDecimal(price));
      ppe.setActive(activeFlag);
      ppe.setOrgdepth(calculateOrgDepth(0, organization));
      ppe.setProductPrice(productPrice);
      OBDal.getInstance().save(ppe);

      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  @After
  public void rollbackSetup() {
    rollback();
  }

  @Test
  public void definedPriceIsPresent() throws JSONException, IOException, ServletException {

    createPriceException(WHITE_VALLEY_GROUP, "5");

    assertThat(getPriceException(), is(5));

  }

  @Test
  public void definedPriceIsPresentInChildOrg()
      throws JSONException, IOException, ServletException {

    createPriceException(VALL_BLANCA, "2");

    assertThat(getPriceException(), is(2));
  }

  @Test
  public void definedPriceIsNOTPresentInOtherOrg()
      throws JSONException, IOException, ServletException {

    createPriceException(SOUTH_WEST_ZONE, "3");

    // Vall Blanca not under South West Zone so the Product Price is loaded.
    assertThat(getPriceException(), is(150.5));
  }

  @Test
  public void definedPriceIsPresentInSpecificOrg()
      throws JSONException, IOException, ServletException {

    createPriceException(NORTH_EAST_ZONE, "7");

    assertThat(getPriceException(), is(7));

  }

  @Test
  public void definedPriceIsPresentInSpecificOrgAfterDeactivationOtherPriceUsed()
      throws JSONException, IOException, ServletException {

    createPriceException(WHITE_VALLEY_GROUP, "2.2");
    createPriceException(VALL_BLANCA, "3.2", false);

    assertThat(getPriceException(), is(2.2));
  }

  @Test
  public void definedPriceIsPresentInOtherPricelist()
      throws JSONException, IOException, ServletException {

    createProductPrice(createPricelistVersion());

    createPriceException(VALL_BLANCA, "6.8");

    assertThat(getPriceException(), is(6.8));
  }

  @Test
  public void definedPriceChangedWhenIncrementatlRefreshDone()
      throws JSONException, IOException, ServletException {

    productIncrementalRefresh(hoursAgo(1));

    createPriceException(VALL_BLANCA, "880");

    assertThat(getPriceException(), is(880));

  }

  private void createProductPrice(PriceListVersion priceListVersion) {
    OBContext.setAdminMode();
    OBInterceptor.setPreventUpdateInfoChange(true);
    try {
      ProductPrice pp = OBProvider.getInstance().get(ProductPrice.class);
      pp.setProduct(OBDal.getInstance()
          .get(org.openbravo.model.common.plm.Product.class, AVALANCHE_TRANSRECEIVER_ID));
      pp.setPriceListVersion(priceListVersion);
      pp.setStandardPrice(new BigDecimal(110));
      pp.setListPrice(new BigDecimal(110));
      pp.setAlgorithm("S");
      OBDal.getInstance().save(pp);

      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  private PriceListVersion createPricelistVersion() {
    OBContext.setAdminMode();
    OBInterceptor.setPreventUpdateInfoChange(true);
    try {
      PriceListVersion ppv = OBProvider.getInstance().get(PriceListVersion.class);
      ppv.setName("Price Exception Test");
      ppv.setValidFromDate(Date.from(new Timestamp(System.currentTimeMillis()).toInstant()));
      ppv.setPriceListSchema(OBDal.getInstance().get(PriceListSchema.class, PRICE_LIST_SCHEMA));
      ppv.setPriceList(OBDal.getInstance().get(PriceList.class, PRICE_LIST));
      OBDal.getInstance().save(ppv);

      OBDal.getInstance().flush();

      return ppv;
    } finally {
      OBContext.restorePreviousMode();
      OBInterceptor.setPreventUpdateInfoChange(false);
    }
  }

  private JSONArray productsFullRefresh() throws JSONException, IOException, ServletException {
    return productIncrementalRefresh(null);
  }

  private JSONArray productIncrementalRefresh(Instant lastUpdate)
      throws JSONException, IOException, ServletException {
    StringWriter w = new StringWriter();

    JSONObject productParams = new JSONObject();
    productParams.put("pos", POS_VBS1);
    JSONObject params = new JSONObject();
    params.put("terminalTime", new Timestamp(System.currentTimeMillis()).toInstant());
    params.put("terminalTimeOffset", new JSONObject().put("value", "-120"));
    productParams.put("parameters", params);

    if (lastUpdate != null) {
      productParams.put("lastUpdated", lastUpdate.getEpochSecond() * 1_000);
    }

    productMasterDataModel.exec(w, productParams);
    String r = "{" + w.toString() + "}";

    JSONObject resp = new JSONObject(r);
    return resp.getJSONArray("data");
  }

  protected Object getPriceException() throws JSONException, IOException, ServletException {
    JSONArray products = productsFullRefresh();

    for (int i = 0; i < products.length(); i++) {
      JSONObject jsonObj = (JSONObject) products.get(i);

      if (jsonObj.getString("id").equals(AVALANCHE_TRANSRECEIVER_ID)) {

        return jsonObj.get("standardPrice");
      }
    }
    return null;
  }
}
