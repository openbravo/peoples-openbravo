/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview defines the ServicesFilter class.
 * @author Carlos Aristu <carlos.aristu@openbravo.com>
 */

(function ServicesFilterDefinition() {
  /**
   * Defines the standard services filter.
   */
  class ServicesFilter extends OB.App.Class.StandardFilter {
    // eslint-disable-next-line class-methods-use-this
    async addFilter(properties, criteria) {
      const criteriaWithServicesFilter = await OB.UTIL.servicesFilter(
        criteria,
        properties.productId,
        properties.productCategory,
        properties.productList,
        properties.productCategoryList
      );
      return criteriaWithServicesFilter;
    }
  }
  OB.App.StandardFilters.Services = new ServicesFilter();
})();
