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
package org.openbravo.test.selector;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.openbravo.base.weld.test.WeldBaseTest;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.service.json.JsonConstants;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.test.base.mock.HttpServletRequestMock;
import org.openbravo.userinterface.selector.CustomQuerySelectorDatasource;
import org.openbravo.userinterface.selector.Selector;
import org.openbravo.userinterface.selector.SelectorConstants;

/**
 * Test cases covering selector HQL query transformers.
 */
public class TestSelectorHqlQueryTransformer extends WeldBaseTest {

  @Inject
  private CustomQuerySelectorDatasource customQuerySelectorDatasource;

  @Test
  public void customQuerySelectorWithHqlTranformer() throws Exception {
    HttpServletRequestMock.setRequestMockInRequestContext();
    CustomQuerySelectorDatasource selectorDatasorce = customQuerySelectorDatasource;

    String selectorId = "EB3C41F0973A4EDA91E475833792A6D4";
    String documentDate = "2022-07-11";
    Map<String, String> parameters = new HashMap<>();
    parameters.put(JsonConstants.STARTROW_PARAMETER, "0");
    parameters.put(JsonConstants.ENDROW_PARAMETER, "75");
    parameters.put(JsonConstants.NOCOUNT_PARAMETER, "true");
    parameters.put(SelectorConstants.DS_REQUEST_SELECTOR_ID_PARAMETER, selectorId);
    parameters.put("inpadOrgId", TEST_ORG_ID);
    parameters.put("documentDate", documentDate);
    final SimpleDateFormat xmlDateFormat = JsonUtils.createDateFormat();
    final List<Object> typedParameters = new ArrayList<>();
    final Map<String, Object> namedParameters = new HashMap<>();

    OBContext.setAdminMode();
    try {
      Selector sel = OBDal.getInstance().get(Selector.class, selectorId);
      String hql = selectorDatasorce.parseOptionalFilters(parameters, sel, xmlDateFormat,
          typedParameters, namedParameters);

      assertThat(hql, containsString(documentDate));
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}
