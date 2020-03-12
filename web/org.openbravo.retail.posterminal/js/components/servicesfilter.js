/*
 ************************************************************************************
 * Copyright (C) 2015-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, _*/

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.SearchServicesFilter',
  classes: 'obUiSearchServicesFilter',
  filterName: 'ServicesFilter',
  published: {
    type: 'PANEL',
    productId: null,
    productList: null,
    orderline: null,
    orderlineList: null
  },
  handlers: {
    onAddProduct: 'addProduct'
  },

  addCriteria: async function(criteria, value, condition) {
    if (value.productList && value.productList.length > 0) {
      let productCategoryList = [],
        productList = [];
      for (const productId of value.productList) {
        productList.push(productId);
        productCategoryList.push(
          await value.orderlineList
            .find(function(ol) {
              return ol.get('product').get('id') === productId;
            }, this)
            .get('product')
            .get('productCategory')
        );
      }
      criteria = await OB.UTIL.servicesFilter(
        criteria,
        undefined,
        undefined,
        productList,
        productCategoryList
      );
    } else if (this.productId) {
      criteria = await OB.UTIL.servicesFilter(
        criteria,
        value.productId,
        value.orderline.get('product').get('productCategory')
      );
    }
    if (value.productList && value.productList.length > 0) {
      if (value.productList.length > 1) {
        criteria.criterion('availableForMultiline', true);
      }
      for (const orderLine of value.orderlineList) {
        const discountedPrice =
          orderLine.get('discountedPrice') ||
          orderLine.get('discountedLinePrice');
        criteria.multiCriterion(
          [
            new OB.App.Class.Criterion('obposMinpriceassocprod', null),
            new OB.App.Class.Criterion(
              'obposMinpriceassocprod',
              discountedPrice,
              'lowerOrEqualThan'
            )
          ],
          'or'
        );
        criteria.multiCriterion(
          [
            new OB.App.Class.Criterion('obposMaxpriceassocprod', null),
            new OB.App.Class.Criterion(
              'obposMaxpriceassocprod',
              discountedPrice,
              'greaterOrEqualThan'
            )
          ],
          'or'
        );
      }
    } else if (value.productId) {
      const discountedPrice =
        value.orderline.get('discountedPrice') ||
        value.orderline.get('discountedLinePrice');
      criteria.multiCriterion(
        [
          new OB.App.Class.Criterion('obposMinpriceassocprod', null),
          new OB.App.Class.Criterion(
            'obposMinpriceassocprod',
            discountedPrice,
            'lowerOrEqualThan'
          )
        ],
        'or'
      );
      criteria.multiCriterion(
        [
          new OB.App.Class.Criterion('obposMaxpriceassocprod', null),
          new OB.App.Class.Criterion(
            'obposMaxpriceassocprod',
            discountedPrice,
            'greaterOrEqualThan'
          )
        ],
        'or'
      );
    }
  },
  hqlCriteria: function() {
    var me = this,
      prodList,
      catList,
      lineIdList,
      existingServices,
      trancheValues = [],
      totalAmountSelected = 0,
      minimumSelected = 999999999999,
      maximumSelected = 0,
      filters;
    OB.MobileApp.model.set('serviceSearchLaunched', true);
    if (this.orderlineList && this.orderlineList.length > 0) {
      prodList = this.orderlineList.map(function(line) {
        var product = line.get('product');
        return product.get('forceFilterId') || product.get('id');
      });
      catList = this.orderlineList.map(function(line) {
        return line.get('product').get('productCategory');
      });
      catList = catList.sort().filter(function(item, pos, ary) {
        return !pos || item !== ary[pos - 1];
      });
      lineIdList = this.orderlineList.map(function(line) {
        return line.get('id');
      });
      existingServices = OB.MobileApp.model.receipt
        .get('lines')
        .filter(function(l) {
          if (
            l.get('relatedLines') &&
            _.intersection(
              lineIdList,
              _.pluck(l.get('relatedLines'), 'orderlineId')
            ).length > 0
          ) {
            return true;
          }
          return false;
        })
        .map(function(line) {
          var product = line.get('product');
          return product.get('forceFilterId') || product.get('id');
        });
      this.orderlineList.forEach(function(line) {
        trancheValues = me.calculateTranche(line.attributes, trancheValues);
      });
      totalAmountSelected = trancheValues[0];
      minimumSelected = trancheValues[1];
      maximumSelected = trancheValues[2];
      filters = [
        {
          columns: [],
          operator: OB.Dal.FILTER,
          value:
            this.orderlineList.length > 1
              ? 'Services_Filter_Multi'
              : 'Services_Filter',
          params: [
            prodList,
            catList,
            prodList.length,
            catList.length,
            existingServices.length > 0 ? existingServices : '-',
            totalAmountSelected,
            minimumSelected,
            maximumSelected
          ],
          fieldType: 'Long'
        },
        {
          columns: ['ispack'],
          operator: 'equals',
          value: false,
          fieldType: 'forceString'
        }
      ];
    } else {
      existingServices = OB.MobileApp.model.receipt
        .get('lines')
        .filter(function(l) {
          if (
            l.get('relatedLines') &&
            _.indexOf(
              _.pluck(l.get('relatedLines'), 'orderlineId'),
              me.orderline.get('id')
            ) !== -1
          ) {
            return true;
          }
          return false;
        })
        .map(function(line) {
          var product = line.get('product');
          return product.get('forceFilterId') || product.get('id');
        });
      var product = this.orderline.get('product');
      if (this.orderline.get('qty') > 0) {
        var discountAmount = _.reduce(
            this.orderline.get('promotions'),
            function(memo, promo) {
              return memo + promo.amt;
            },
            0
          ),
          currentLinePrice = OB.DEC.div(
            OB.DEC.sub(this.orderline.get('gross'), discountAmount),
            this.orderline.get('qty')
          );
        totalAmountSelected = OB.DEC.add(
          totalAmountSelected,
          OB.DEC.sub(this.orderline.get('gross'), discountAmount)
        );
        if (currentLinePrice < minimumSelected) {
          minimumSelected = currentLinePrice;
        }
        if (currentLinePrice > maximumSelected) {
          maximumSelected = currentLinePrice;
        }
      }
      filters = [
        {
          columns: [],
          operator: OB.Dal.FILTER,
          value: 'Services_Filter',
          params: [
            product.get('isNew')
              ? null
              : product.get('forceFilterId')
              ? product.get('forceFilterId')
              : product.get('id'),
            product.get('productCategory'),
            '',
            '',
            existingServices.length > 0 ? existingServices : "'-'",
            totalAmountSelected,
            minimumSelected,
            maximumSelected
          ]
        },
        {
          columns: ['ispack'],
          operator: 'equals',
          value: false,
          fieldType: 'forceString'
        }
      ];
    }
    this.filterExtensions.forEach(function(extension) {
      if (extension.hqlExtension) {
        filters = extension.hqlExtension(
          this.productId,
          this.productList,
          this.orderline,
          this.orderlineList,
          filters,
          this.extraParams
        );
      }
    }, this);

    return filters;
  },
  lineAttributes: function() {
    var productList = [];

    if (this.orderlineList) {
      this.orderlineList.forEach(function(ol) {
        ol.set('preserveId', true);
        productList.push({
          orderlineId: ol.get('id'),
          productName: ol.get('product').get('_identifier'),
          productId: ol.get('product').get('id'),
          productCategory: ol.get('product').get('productCategory')
        });
      });
    } else if (this.orderline) {
      this.orderline.set('preserveId', true);
      productList.push({
        orderlineId: this.orderline.get('id'),
        productName: this.orderline.get('product').get('_identifier'),
        productId: this.orderline.get('product').get('id'),
        productCategory: this.orderline.get('product').get('productCategory')
      });
    }

    return {
      relatedLines: productList
    };
  },
  initComponents: function() {
    this.inherited(arguments);
    this.caption = OB.I18N.getLabel('OBPOS_ServicesFor');
  },
  calculateTranche: function(line, trancheValues) {
    var totalAmountSelected = 0,
      minimumSelected = 999999999999,
      maximumSelected = 0;
    if (trancheValues && trancheValues.length === 3) {
      totalAmountSelected = trancheValues[0];
      minimumSelected = trancheValues[1];
      maximumSelected = trancheValues[2];
    }
    if (line.qty > 0) {
      var discountAmount = _.reduce(
          line.promotions,
          function(memo, promo) {
            return memo + promo.amt;
          },
          0
        ),
        currentLinePrice = OB.DEC.div(
          OB.DEC.sub(line.gross, discountAmount),
          line.qty
        );
      totalAmountSelected = OB.DEC.add(
        totalAmountSelected,
        OB.DEC.sub(line.gross, discountAmount)
      );
      if (currentLinePrice < minimumSelected) {
        minimumSelected = currentLinePrice;
      }
      if (currentLinePrice > maximumSelected) {
        maximumSelected = currentLinePrice;
      }
    }
    return [totalAmountSelected, minimumSelected, maximumSelected];
  },
  filterExtensions: []
});

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.MandatoryServicesFilter',
  classes: 'obUiMandatoryServicesFilter',
  filterName: 'MandatoryServicesFilter',
  published: {
    type: 'HIDDEN'
  },

  addCriteria: async function(criteria, value, condition) {
    criteria.criterion('proposalType', 'MP');
  },
  hqlCriteria: function() {
    return [
      {
        columns: [],
        operator: OB.Dal.FILTER,
        value: 'Mandatory_Services',
        params: []
      }
    ];
  }
});

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.FinalMandatoryServicesFilter',
  classes: 'obUiFinalMandatoryServicesFilter',
  filterName: 'FinalMandatoryServicesFilter',
  published: {
    type: 'PANEL'
  },

  addCriteria: async function(criteria, value, condition) {
    criteria.criterion('productType', 'S');
    criteria.criterion('proposalType', 'FMA');
  },

  hqlCriteria: function() {
    return [
      {
        columns: [],
        operator: OB.Dal.FILTER,
        value: 'Final_Services',
        params: []
      },
      {
        columns: ['ispack'],
        operator: 'equals',
        value: false,
        fieldType: 'forceString'
      }
    ];
  },
  renderInfo: function() {
    var content = {
      content: OB.I18N.getLabel('OBPOS_FinalServices')
    };
    return content;
  }
});
