/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */
OB.UTIL.servicesFilter = async function(
  criteria,
  productId,
  productCategory,
  productList,
  productCategoryList
) {
  async function relatedServicesProduct(productId, productList) {
    let criteriaServiceProduct = new OB.App.Class.Criteria();
    if (productList && productList.length > 0) {
      criteriaServiceProduct.criterion('relatedProduct', productList, 'in');
    } else if (productId) {
      criteriaServiceProduct.criterion('relatedProduct', productId);
    }
    try {
      let serviceProduct = await OB.App.MasterdataModels.ServiceProduct.find(
        criteriaServiceProduct.build()
      );
      return serviceProduct.map(c => c.service);
    } catch (error) {
      OB.UTIL.showError(error);
    }
  }
  async function relatedServicesProductCategory(
    productCategory,
    productCategoryList
  ) {
    let criteriaServiceProductCategory = new OB.App.Class.Criteria();
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
    try {
      let ServiceProductCategory = await OB.App.MasterdataModels.ServiceProductCategory.find(
        criteriaServiceProductCategory.build()
      );
      return ServiceProductCategory.map(c => c.service);
    } catch (error) {
      OB.UTIL.showError(error);
    }
  }

  let relatedServicesProductIds = await relatedServicesProduct(
    productId,
    productList
  );
  let relatedServicesProductCategoryIds = await relatedServicesProductCategory(
    productCategory,
    productCategoryList
  );

  criteria.criterion('productType', 'S');
  criteria.criterion('isLinkedToProduct', true);

  let relatedServicesProductCriteria = new OB.App.Class.Criteria();

  relatedServicesProductCriteria
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

  let relatedServicesProductCategoryCriteria = new OB.App.Class.Criteria();

  relatedServicesProductCategoryCriteria
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

OB.UTIL.handlePriceRuleBasedServices = function(order) {
  OB.MobileApp.view.$.containerWindow
    .getRoot()
    .$.multiColumn.$.leftPanel.$.receiptview.$.orderview.checkServicesToDelete();
  order.trigger('updateServicePrices');
};
