/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

package org.openbravo.retail.posterminal;

import javax.enterprise.context.ApplicationScoped;

import org.openbravo.client.kernel.ComponentProvider.Qualifier;
import org.openbravo.mobile.core.process.HQLCriteriaProcess;

@ApplicationScoped
@Qualifier("ServicePriceRuleVersion_RangeFilter")
public class ServicePriceRuleVersionHQLRangeCriteria extends HQLCriteriaProcess {

  public String getHQLFilter(String params) {
    return " ((sprv.obposMinimum is null or sprv.obposMinimum <= $1) and (sprv.obposMaximum is null or sprv.obposMaximum >= $1))";
  }
}