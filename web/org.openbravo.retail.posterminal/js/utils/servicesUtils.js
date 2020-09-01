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
  OB.UTIL.checkServicesToDelete();
  order.trigger('updateServicePrices');
};

OB.UTIL.checkServicesToDelete = function() {
  var removedServices = [],
    servicesToBeDeleted = [];
  removedServices.push(OB.I18N.getLabel('OBPOS_ServiceRemoved'));

  OB.MobileApp.model.receipt.get('lines').models.forEach(line => {
    var trancheValues = [],
      totalAmountSelected = 0,
      minimumSelected = Infinity,
      maximumSelected = 0,
      uniqueQuantityServiceToBeDeleted,
      asPerProductServiceToBeDeleted;

    if (line.get('obposIsDeleted')) {
      return;
    }

    if (line.has('relatedLines') && line.get('relatedLines').length > 0) {
      line.get('relatedLines').forEach(line2 => {
        if (
          !line2.deferred &&
          !line.get('originalOrderLineId') &&
          OB.MobileApp.model.receipt.get('lines').get(line2.orderlineId)
        ) {
          line2 = OB.MobileApp.model.receipt.get('lines').get(line2.orderlineId)
            .attributes;
        }
        trancheValues = OB.UI.SearchServicesFilter.prototype.calculateTranche(
          line2,
          trancheValues
        );
      });

      totalAmountSelected = trancheValues[0];
      minimumSelected = trancheValues[1];
      maximumSelected = trancheValues[2];
      uniqueQuantityServiceToBeDeleted =
        line.get('product').get('quantityRule') === 'UQ' &&
        ((line.has('serviceTrancheMaximum') &&
          totalAmountSelected > line.get('serviceTrancheMaximum')) ||
          (line.has('serviceTrancheMinimum') &&
            totalAmountSelected < line.get('serviceTrancheMinimum')));
      asPerProductServiceToBeDeleted =
        line.get('product').get('quantityRule') === 'PP' &&
        ((line.has('serviceTrancheMaximum') &&
          maximumSelected > line.get('serviceTrancheMaximum')) ||
          (line.has('serviceTrancheMinimum') &&
            minimumSelected < line.get('serviceTrancheMinimum')));
      if (
        (!line.has('deliveredQuantity') ||
          line.get('deliveredQuantity') <= 0) &&
        (uniqueQuantityServiceToBeDeleted || asPerProductServiceToBeDeleted)
      ) {
        servicesToBeDeleted.push(line);
        removedServices.push(line.get('product').get('_identifier'));
      }
    }
  });
  if (servicesToBeDeleted.length > 0) {
    OB.MobileApp.model.receipt.deleteLinesFromOrder(servicesToBeDeleted);
    OB.MobileApp.model.receipt.set('undo', null);
    OB.UTIL.showConfirmation.display(
      OB.I18N.getLabel('OBPOS_ServiceRemovedHeader'),
      removedServices
    );
  }
};
