/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.retail.posterminal.term;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.model.HQLPropertyList;
import org.openbravo.mobile.core.model.ModelExtension;
import org.openbravo.mobile.core.model.ModelExtensionUtils;

public class ReturnReason extends QueryTerminalProperty {
  public static final String RETURNREASONPROPERTYEXTENSION = "OBPOS_ReturnReasonExtension";

  @Inject
  @Any
  @Qualifier(RETURNREASONPROPERTYEXTENSION)
  private Instance<ModelExtension> extensions;

  @Override
  protected List<String> getQuery(JSONObject jsonsent) throws JSONException {

    List<String> hqlQueries = new ArrayList<>();
    HQLPropertyList regularReturnReasonHQLProperties = ModelExtensionUtils
        .getPropertyExtensions(extensions);

    hqlQueries.add("select" + regularReturnReasonHQLProperties.getHqlSelect() //
        + "from ReturnReason reason " + "where " + " reason.isrfc = true "
        + "and (reason.$incrementalUpdateCriteria) AND ($naturalOrgCriteria) and $readableSimpleClientCriteria order by reason.name asc, reason.id");

    return hqlQueries;
  }

  @Override
  protected boolean bypassPreferenceCheck() {
    return true;
  }

  @Override
  public String getProperty() {
    return "returnreasons";
  }

  @Override
  public boolean returnList() {
    return false;
  }
}
