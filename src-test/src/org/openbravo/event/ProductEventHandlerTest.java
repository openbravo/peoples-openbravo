/*
 ************************************************************************************
 * Copyright (C) 2023-2024 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Arrays;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openbravo.base.weld.test.ParameterCdiTest;
import org.openbravo.base.weld.test.ParameterCdiTestRule;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.common.plm.Characteristic;
import org.openbravo.model.common.plm.Product;

public class ProductEventHandlerTest extends WeldBaseTest {

  private static final String WHITEVALLEY_CLIENT = "39363B0921BB4293B48383844325E84C";
  private static final String WHITEVALLEYADMIN_ROLE = "E717F902C44C455793463450495FF36B";
  private static final String WHITEVALLEYADMIN_USER = "CFCF5FA26B344930A36C6DD7E5C76BE1";
  private static final String VALLBLANCA_ORG = "D270A5AC50874F8BA67A88EE977F8E3B";
  private static final String AVALANCHE_PRODUCT = "934E7D7587EC4C7A9E9FF58F0382D450";
  private static final String DRYFITRUN_PRODUCT = "78D42266B5594BDB90C61FA18395CEF4";
  private static final String COLOR_CHARACTERISTIC = "015D6C6072AC4A13B7573A261B2011BC";
  private static final String SIZE_CHARACTERISTIC = "33E950C3E6274C20A56CE301358203AE";

  @Rule
  public ParameterCdiTestRule<TestData> parameterValuesRule = new ParameterCdiTestRule<>(
      Arrays.asList(//
          new TestData("No generic product with characteristic dimensions disabled", //
              false, false, false, null, null, null), //
          new TestData("No generic product with characteristic dimensions enabled", //
              false, false, true, null, null, "@CharacteristicDimensionValidation_GenericProduct@"), //

          new TestData("Generic product with characteristic dimensions disabled", //
              true, false, false, null, null, null), //

          new TestData(
              "Generic product with characteristic dimensions disabled and row characteristic defined", //
              true, false, false, COLOR_CHARACTERISTIC, null,
              "@CharacteristicDimensionValidation_Disabled@"), //
          new TestData(
              "Generic product with characteristic dimensions disabled and column characteristic defined", //
              true, false, false, null, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Disabled@"), //
          new TestData(
              "Generic product with characteristic dimensions disabled and row and column characteristic defined", //
              true, false, false, COLOR_CHARACTERISTIC, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Disabled@"), //

          new TestData("Variant product with characteristic dimensions disabled", //
              false, true, false, null, null, null), //

          new TestData(
              "Variant product with characteristic dimensions disabled and row characteristic defined", //
              false, true, false, COLOR_CHARACTERISTIC, null,
              "@CharacteristicDimensionValidation_Disabled@"), //
          new TestData(
              "Variant product with characteristic dimensions disabled and column characteristic defined", //
              false, true, false, null, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Disabled@"), //
          new TestData(
              "Variant product with characteristic dimensions disabled and row and column characteristic defined", //
              false, true, false, COLOR_CHARACTERISTIC, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Disabled@"), //

          new TestData(
              "Generic product with characteristic dimensions enabled and row and column characteristics undefined", //
              true, false, true, null, null, "@CharacteristicDimensionValidation_Enabled@"), //
          new TestData("Generic product with characteristic dimensions enabled and row characteristic defined", //
              true, false, true, COLOR_CHARACTERISTIC, null, null), //
          new TestData(
              "Generic product with characteristic dimensions enabled and column characteristic defined", //
              true, false, true, null, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Enabled@"), //
          new TestData(
              "Generic product with characteristic dimensions enabled and row and column characteristics defined", //
              true, false, true, COLOR_CHARACTERISTIC, SIZE_CHARACTERISTIC, null), //

          new TestData(
              "Variant product with characteristic dimensions enabled and row and column characteristics undefined", //
              false, true, true, null, null, "@CharacteristicDimensionValidation_Enabled@"), //
          new TestData("Variant product with characteristic dimensions enabled and row characteristic defined", //
              false, true, true, COLOR_CHARACTERISTIC, null, null), //
          new TestData(
              "Variant product with characteristic dimensions enabled and column characteristic defined", //
              false, true, true, null, SIZE_CHARACTERISTIC,
              "@CharacteristicDimensionValidation_Enabled@"), //
          new TestData(
              "Variant product with characteristic dimensions enabled and row and column characteristics defined", //
              false, true, true, COLOR_CHARACTERISTIC, SIZE_CHARACTERISTIC, null) //
      ));

  private @ParameterCdiTest TestData testData;

  @Before
  public void beforeTest() {
    OBContext.setOBContext(WHITEVALLEYADMIN_USER, WHITEVALLEYADMIN_ROLE, WHITEVALLEY_CLIENT,
        VALLBLANCA_ORG);
  }

  @After
  public void afterTest() {
    rollback();
  }

  @Test
  public void productEventHandlerTest() {
    final Product product = OBDal.getInstance()
        .get(Product.class, testData.isGenericProduct() ? DRYFITRUN_PRODUCT : AVALANCHE_PRODUCT);
    assertThat(
        "Product event handler should throw the correct exception: " + testData.getDescription(),
        updateProduct(product), equalTo(testData.getError()));

    if (testData.getError() == null) {
      if (testData.isGenericProduct()) {
        product.getProductGenericProductList()
            .stream()
            .forEach(variantProduct -> assertProduct(product, variantProduct));
      } else if (testData.isVariantProduct()) {
        assertProduct(product.getGenericProduct(), product);
      }
    }
  }

  private void assertProduct(final Product genericProduct, Product variantProduct) {
    assertThat(
        "Variant product should have the same characteristic dimensions configuration than generic product: "
            + testData.getDescription(),
        variantProduct.isEnableCharacteristicDimensions(),
        equalTo(genericProduct.isEnableCharacteristicDimensions()));
    assertThat(
        "Variant product should have the same row characteristic than generic product: "
            + testData.getDescription(),
        variantProduct.isEnableCharacteristicDimensions(),
        equalTo(genericProduct.isEnableCharacteristicDimensions()));
    assertThat(
        "Variant product should have the same column characteristic than generic product: "
            + testData.getDescription(),
        variantProduct.isEnableCharacteristicDimensions(),
        equalTo(genericProduct.isEnableCharacteristicDimensions()));
  }

  private String updateProduct(Product product) {
    try {
      product.setGeneric(testData.isGenericProduct());
      product.setGenericProduct(
          testData.isVariantProduct() ? OBDal.getInstance().get(Product.class, DRYFITRUN_PRODUCT)
              : null);
      product.setEnableCharacteristicDimensions(testData.hasCharacteristicDimensions());
      product.setRowCharacteristic(Optional.ofNullable(testData.getRowCharacteristic())
          .map(id -> OBDal.getInstance().get(Characteristic.class, id))
          .orElse(null));
      product.setColumnCharacteristic(Optional.ofNullable(testData.getColumnCharacteristic())
          .map(id -> OBDal.getInstance().get(Characteristic.class, id))
          .orElse(null));
      OBDal.getInstance().save(product);
      OBDal.getInstance().flush();
      return null;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  class TestData {
    private String description;
    private boolean isGenericProduct;
    private boolean isVariantProduct;
    private boolean hasCharacteristicDimensions;
    private String rowCharacteristic;
    private String columnCharacteristic;
    private String error;

    public TestData(String description, boolean isGenericProduct, boolean isVariantProduct,
        boolean hasCharacteristicDimensions, String rowCharacteristic, String columnCharacteristic,
        String error) {
      this.description = description;
      this.isGenericProduct = isGenericProduct;
      this.isVariantProduct = isVariantProduct;
      this.hasCharacteristicDimensions = hasCharacteristicDimensions;
      this.rowCharacteristic = rowCharacteristic;
      this.columnCharacteristic = columnCharacteristic;
      this.error = error;
    }

    String getDescription() {
      return description;
    }

    boolean isGenericProduct() {
      return isGenericProduct;
    }

    boolean isVariantProduct() {
      return isVariantProduct;
    }

    boolean hasCharacteristicDimensions() {
      return hasCharacteristicDimensions;
    }

    String getRowCharacteristic() {
      return rowCharacteristic;
    }

    String getColumnCharacteristic() {
      return columnCharacteristic;
    }

    String getError() {
      return error;
    }
  }
}
