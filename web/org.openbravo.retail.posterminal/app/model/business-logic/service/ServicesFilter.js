/*
 ************************************************************************************
 * Copyright (C) 2020-2021 Openbravo S.L.U.
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
  const relatedServicesProduct = async (productId, productList) => {
    const criteriaServiceProduct = new OB.App.Class.Criteria();
    if (productList && productList.length > 0) {
      criteriaServiceProduct.criterion('relatedProduct', productList, 'in');
    } else if (productId) {
      criteriaServiceProduct.criterion('relatedProduct', productId);
    }

    const serviceProduct = await OB.App.MasterdataModels.ServiceProduct.find(
      criteriaServiceProduct.build()
    );
    return serviceProduct.map(c => c.service);
  };

  const relatedServicesProductCategory = async (
    productCategory,
    productCategoryList
  ) => {
    const criteriaServiceProductCategory = new OB.App.Class.Criteria();
    if (productCategoryList && productCategoryList.length > 0) {
      criteriaServiceProductCategory.criterion(
        'relatedCategory',
        productCategoryList,
        'in'
      );
    } else if (productCategory) {
      criteriaServiceProductCategory.criterion(
        'relatedCategory',
        productCategory
      );
    }
    const serviceProductCategory = await OB.App.MasterdataModels.ServiceProductCategory.find(
      criteriaServiceProductCategory.build()
    );
    return serviceProductCategory.map(c => c.service);
  };

  const addServicesFilter = async (
    criteria,
    productId,
    productCategory,
    productList,
    productCategoryList
  ) => {
    const relatedServicesProductIds = await relatedServicesProduct(
      productId,
      productList
    );
    const relatedServicesProductCategoryIds = await relatedServicesProductCategory(
      productCategory,
      productCategoryList
    );

    criteria.criterion('productType', 'S');
    criteria.criterion('isLinkedToProduct', true);

    const relatedServicesProductCriteria = new OB.App.Class.Criteria()
      .multiCriterion(
        [
          new OB.App.Class.Criterion('includeProducts', false),
          new OB.App.Class.Criterion('id', relatedServicesProductIds, 'in')
        ],
        'and'
      )
      .multiCriterion(
        [
          new OB.App.Class.Criterion('includeProducts', true),
          new OB.App.Class.Criterion('id', relatedServicesProductIds, 'notin')
        ],
        'and'
      )
      .criterion('includeProducts', null)
      .operator('or');
    criteria.innerCriteria(relatedServicesProductCriteria);

    const relatedServicesProductCategoryCriteria = new OB.App.Class.Criteria()
      .multiCriterion(
        [
          new OB.App.Class.Criterion('includeProductCategories', false),
          new OB.App.Class.Criterion(
            'id',
            relatedServicesProductCategoryIds,
            'in'
          )
        ],
        'and'
      )
      .multiCriterion(
        [
          new OB.App.Class.Criterion('includeProductCategories', true),
          new OB.App.Class.Criterion(
            'id',
            relatedServicesProductCategoryIds,
            'notin'
          )
        ],
        'and'
      )
      .criterion('includeProductCategories', null)
      .operator('or');
    criteria.innerCriteria(relatedServicesProductCategoryCriteria);

    return criteria;
  };

  /**
   * Defines the standard services filter.
   */
  class ServicesFilter extends OB.App.Class.StandardFilter {
    async addFilter(properties, criteria) {
      const criteriaWithServicesFilter = await addServicesFilter(
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
