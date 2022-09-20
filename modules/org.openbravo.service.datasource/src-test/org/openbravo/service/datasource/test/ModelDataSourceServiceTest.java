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
package org.openbravo.service.datasource.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.model.Entity;
import org.openbravo.base.model.ModelProvider;
import org.openbravo.base.model.Property;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.domain.Reference;
import org.openbravo.model.ad.module.Module;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.service.datasource.ModelDataSourceService;
import org.openbravo.service.json.AdditionalPropertyResolver;
import org.openbravo.test.base.TestConstants;

/**
 * Test cases for the {@link ModelDataSourceService} class
 */
public class ModelDataSourceServiceTest extends WeldBaseTest {
  private static final String RELEVANT_CHARACTERISTICS_REFERENCE = "247C9B7EEFE1475EA322003B96E8B7AE";

  @Before
  public void initialize() {
    addRelevantCharacteristic();
    setSystemAdministratorContext();
  }

  @After
  public void cleanUp() {
    OBDal.getInstance().rollbackAndClose();
  }

  private void addRelevantCharacteristic() {
    try {
      OBContext.setAdminMode(false);
      Module module = OBDal.getInstance().get(Module.class, TestConstants.Modules.ID_CORE);
      module.setInDevelopment(true);
      OBDal.getInstance().flush();

      org.openbravo.model.ad.domain.List listReference = OBProvider.getInstance()
          .get(org.openbravo.model.ad.domain.List.class);
      listReference
          .setClient(OBDal.getInstance().getProxy(Client.class, TestConstants.Clients.SYSTEM));
      listReference.setOrganization(
          OBDal.getInstance().getProxy(Organization.class, TestConstants.Orgs.MAIN));
      listReference.setModule(module);
      listReference.setReference(
          OBDal.getInstance().getProxy(Reference.class, RELEVANT_CHARACTERISTICS_REFERENCE));
      listReference.setSearchKey("M_Test");
      listReference.setName("Test");
      OBDal.getInstance().save(listReference);
      OBDal.getInstance().flush();
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  @Test
  public void fetchAllProperties() throws JSONException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("inpadTableId", "208");
    parameters.put("_constructor", "AdvancedCriteria");
    //@formatter:off
    parameters.put("criteria",
        "{\"fieldName\":\"_dummy\",\"operator\":\"equals\",\"value\":1663665066321}");
    //@formatter:on

    ModelDataSourceService datasource = WeldUtils
        .getInstanceFromStaticBeanManager(ModelDataSourceService.class);
    String result = datasource.fetch(parameters);

    List<String> expectedProperties = new ArrayList<>();
    expectedProperties.add("_identifier");
    expectedProperties.addAll(getProductProperties(p -> true));
    assertThat(getPropertiesFromResponse(result), contains(expectedProperties.toArray()));
  }

  @Test
  public void fetchMatchingProperties() throws JSONException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("inpadTableId", "208");
    parameters.put("_constructor", "AdvancedCriteria");
    parameters.put("_OrExpression", "true");
    //@formatter:off
    parameters.put("criteria",
        "{\"fieldName\":\"_dummy\",\"operator\":\"equals\",\"value\":1663665066321}" +
        "__;__" +
        "{\"fieldName\":\"property\",\"operator\":\"iContains\",\"value\":\"a\"}");
    //@formatter:on

    ModelDataSourceService datasource = WeldUtils
        .getInstanceFromStaticBeanManager(ModelDataSourceService.class);
    String result = datasource.fetch(parameters);

    List<String> expectedProperties = getProductProperties(p -> p.startsWith("a"));
    assertThat(getPropertiesFromResponse(result), contains(expectedProperties.toArray()));
  }

  @Test
  public void fetchAdditionalProperty() throws JSONException {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("inpadTableId", "208");
    parameters.put("_constructor", "AdvancedCriteria");
    parameters.put("_OrExpression", "true");
    //@formatter:off
    parameters.put("criteria",
        "{\"fieldName\":\"_dummy\",\"operator\":\"equals\",\"value\":1663665066321}" +
        "__;__" +
        "{\"fieldName\":\"property\",\"operator\":\"iContains\",\"value\":\"mtes\"}");
    //@formatter:on

    ModelDataSourceService datasource = WeldUtils
        .getInstanceFromStaticBeanManager(ModelDataSourceService.class);
    String result = datasource.fetch(parameters);

    assertThat(getPropertiesFromResponse(result), contains("mTest"));
  }

  private List<String> getPropertiesFromResponse(String response) throws JSONException {
    JSONArray data = new JSONObject(response).getJSONObject("response").getJSONArray("data");
    return IntStream.range(0, data.length()).mapToObj(i -> {
      try {
        return data.getJSONObject(i).getString("property");
      } catch (JSONException ex) {
        throw new OBException("Unexpected data received from datasource", ex);
      }
    }).collect(Collectors.toList());
  }

  private List<String> getProductProperties(Predicate<String> filter) {
    Entity product = ModelProvider.getInstance().getEntity(Product.class);
    List<String> properties = product.getProperties()
        .stream()
        .map(Property::getName)
        .collect(Collectors.toList());
    Set<String> additionalProperties = WeldUtils
        .getInstancesSortedByPriority(AdditionalPropertyResolver.class)
        .stream()
        .map(resolver -> resolver.getPropertyNames(product))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
    properties.addAll(additionalProperties);
    return properties.stream().filter(filter).sorted().collect(Collectors.toList());
  }
}
