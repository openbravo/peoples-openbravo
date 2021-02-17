/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class CurrencyPanel extends QueryTerminalProperty {

  public static final String currencyPanelPropertyExtension = "OBPOS_CurrencyPanel";

  @Inject
  @Any
  @Qualifier(currencyPanelPropertyExtension)
  private Instance<ModelExtension> extensions;

  @Override
  protected boolean isAdminMode() {
    return true;
  }

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    //@formatter:off
    String hqlQuery = " select " + ModelExtensionUtils.getPropertyExtensions(extensions).getHqlSelect()
                    + " from OBPOS_CurrencyPanel e"
                    + " where (e.$incrementalUpdateCriteria) and $readableSimpleClientCriteria"
                    + " and $activeCriteria order by lineNo asc";
    //@formatter:on

    return Arrays.asList(new String[] { hqlQuery });
  }

  @Override
  public String getProperty() {
    return "currencyPanel";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
