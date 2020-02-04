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
  //force remote mode until services are migrated to indexedDB
  forceRemote: true,
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
  sqlFilter: function() {
    var me = this,
      result = {},
      where = '',
      filters = [],
      auxProdFilters = [],
      auxCatFilters = [],
      auxProdStr = '(',
      auxCatStr = '(',
      appendProdComma = false,
      appendCatComma = false,
      existingServices,
      lineIdList,
      trancheValues = [],
      totalAmountSelected = 0,
      minimumSelected = 999999999999,
      maximumSelected = 0;

    if (this.productList && this.productList.length > 0) {
      //product multiselection
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

      //build auxiliar string for the products filter and for the categories filter:
      this.orderlineList.forEach(function(l) {
        if (appendProdComma) {
          auxProdStr += ', ';
        } else {
          appendProdComma = true;
        }
        auxProdStr += '?';

        var product = l.get('product');
        auxProdFilters.push(product.get('forceFilterId') || product.get('id'));

        if (
          auxCatFilters.indexOf(l.get('product').get('productCategory')) < 0
        ) {
          if (appendCatComma) {
            auxCatStr += ', ';
          } else {
            appendCatComma = true;
          }
          auxCatStr += '?';

          auxCatFilters.push(l.get('product').get('productCategory'));
        }
        trancheValues = me.calculateTranche(l.attributes, trancheValues);
      });
      totalAmountSelected = trancheValues[0];
      minimumSelected = trancheValues[1];
      maximumSelected = trancheValues[2];

      auxProdStr += ')';
      auxCatStr += ')';

      where =
        " and product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

      if (this.productList.length > 1) {
        where += " product.availableForMultiline = 'true' and ";
      }

      var addAnd;
      //including/excluding products
      where +=
        "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id in " +
        auxProdStr +
        ' )) ';
      where +=
        "or (product.includeProducts = 'N' and " +
        auxProdFilters.length +
        ' = (select count(*) from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id in ' +
        auxProdStr +
        ' )) ';
      where += 'or product.includeProducts is null) ';
      where +=
        " AND (product.isPriceRuleBased = 'false' OR (product.quantityRule = 'UQ' AND "; //
      addAnd = false;

      this.productList.forEach(function(p) {
        if (addAnd) {
          where += ' AND ';
        } else {
          addAnd = true;
        }
        where +=
          ' EXISTS (SELECT 1' + //
          ' FROM m_servicepricerule_version sprv' + //
          ' WHERE sprv.product = product.m_product_id' + //
          ' AND   sprv.validFromDate = (SELECT MAX(sprv2.validFromDate)' + //
          '                             FROM m_servicepricerule_version sprv2' + //
          '                             WHERE sprv2.product = product.m_product_id' + //
          "                             AND   sprv2.validFromDate <= strftime ('%Y-%m-%d','now')" + //
          "                             AND   sprv2.active = 'true'" + //
          "                             AND   ((product.includeProducts = 'Y' AND product.includeProductCategories = 'Y')" + //
          "                                    OR (product.includeProducts = 'Y' AND product.includeProductCategories is null)" + //
          "                                    OR (product.includeProducts is null AND product.includeProductCategories = 'Y')" + //
          "                                    OR (product.includeProducts = 'N' AND (sprv2.relatedProduct IS NULL OR sprv2.relatedProduct = ?))" + //
          "                                    OR (product.includeProductCategories = 'N' AND (sprv2.relatedProductCategory IS NULL OR sprv2.relatedProductCategory = ?))))" + //
          ' AND   (sprv.minimum IS NULL OR sprv.minimum <= ?)' + //
          ' AND   (sprv.maximum IS NULL OR sprv.maximum >= ?)' + //
          " AND   sprv.active = 'true')"; //
      });
      where += ") OR (product.quantityRule = 'PP' AND ";
      addAnd = false;
      this.productList.forEach(function(p) {
        if (addAnd) {
          where += ' AND ';
        } else {
          addAnd = true;
        }
        where +=
          ' EXISTS (SELECT 1' + //
          ' FROM m_servicepricerule_version sprv' + //
          ' WHERE sprv.product = product.m_product_id' + //
          ' AND   sprv.validFromDate = (SELECT MAX(sprv2.validFromDate)' + //
          '                             FROM m_servicepricerule_version sprv2' + //
          '                             WHERE sprv2.product = product.m_product_id' + //
          "                             AND   sprv2.validFromDate <= strftime ('%Y-%m-%d','now')" + //
          "                             AND   sprv2.active = 'true'" + //
          "                             AND   ((product.includeProducts = 'Y' AND product.includeProductCategories = 'Y')" + //
          "                                    OR (product.includeProducts = 'Y' AND product.includeProductCategories is null)" + //
          "                                    OR (product.includeProducts is null AND product.includeProductCategories = 'Y')" + //
          "                                    OR (product.includeProducts = 'N' AND (sprv2.relatedProduct IS NULL OR sprv2.relatedProduct = ?))" + //
          "                                    OR (product.includeProductCategories = 'N' AND (sprv2.relatedProductCategory IS NULL OR sprv2.relatedProductCategory = ?))))" + //
          ' AND   (sprv.minimum IS NULL OR sprv.minimum <= ?)' + //
          ' AND   (sprv.maximum IS NULL OR sprv.maximum >= ?)' + //
          " AND   sprv.active = 'true')"; //
      });

      where += ')) ';

      //including/excluding product categories
      where +=
        "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id in " +
        auxCatStr +
        ' )) ';
      where +=
        "or (product.includeProductCategories = 'N' and " +
        auxCatFilters.length +
        ' = (select count(*) from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id in ' +
        auxCatStr +
        ' )) ';
      where += 'or product.includeProductCategories is null)) ';
      where +=
        "and product.m_product_id not in ('" +
        existingServices.join("','") +
        "')";

      filters = filters.concat(auxProdFilters);
      filters = filters.concat(auxProdFilters);

      this.productList.forEach(function(productId) {
        filters.push(productId);
        filters.push(
          this.orderlineList
            .find(function(ol) {
              return ol.get('product').get('id') === productId;
            }, this)
            .get('product')
            .get('productCategory')
        );
        filters.push(totalAmountSelected);
        filters.push(totalAmountSelected);
      }, this);

      this.productList.forEach(function(productId) {
        filters.push(productId);
        filters.push(
          this.orderlineList
            .find(function(ol) {
              return ol.get('product').get('id') === productId;
            }, this)
            .get('product')
            .get('productCategory')
        );
        filters.push(minimumSelected);
        filters.push(maximumSelected);
      }, this);

      filters = filters.concat(auxCatFilters);
      filters = filters.concat(auxCatFilters);
    } else if (this.productId) {
      // Only one product
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
      trancheValues = this.calculateTranche(this.orderline.attributes);
      totalAmountSelected = trancheValues[0];
      minimumSelected = trancheValues[1];
      maximumSelected = trancheValues[2];

      where =
        " and product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

      //including/excluding products
      where +=
        "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? )) ";
      where +=
        "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? )) ";
      where += 'or product.includeProducts is null) ';
      where +=
        " AND (product.isPriceRuleBased = 'false' " + //
        " OR (product.quantityRule = 'UQ' AND EXISTS (SELECT 1" + //
        ' FROM m_servicepricerule_version sprv' + //
        ' WHERE sprv.product = product.m_product_id' + //
        ' AND   sprv.validFromDate = (SELECT MAX(sprv2.validFromDate)' + //
        '                             FROM m_servicepricerule_version sprv2' + //
        '                             WHERE sprv2.product = product.m_product_id' + //
        "                             AND   sprv2.validFromDate <= strftime ('%Y-%m-%d','now')" + //
        "                             AND   sprv2.active = 'true'" + //
        "                             AND   ((product.includeProducts = 'Y' AND product.includeProductCategories = 'Y') " + //
        "                                    OR (product.includeProducts = 'Y' AND product.includeProductCategories is null)" + //
        "                                    OR (product.includeProducts is null AND product.includeProductCategories = 'Y')" + //
        "                                    OR (product.includeProducts = 'N' AND (sprv2.relatedProduct IS NULL OR sprv2.relatedProduct = ?)) " + //
        "                                    OR (product.includeProductCategories = 'N' AND (sprv2.relatedProductCategory IS NULL OR sprv2.relatedProductCategory = ?))))" + //
        ' AND   (sprv.minimum IS NULL OR sprv.minimum <= ?)' + //
        ' AND   (sprv.maximum IS NULL OR sprv.maximum >= ?)' + //
        " AND   sprv.active = 'true')) " + //
        " OR (product.quantityRule = 'PP' AND EXISTS (SELECT 1" + //
        ' FROM m_servicepricerule_version sprv' + //
        ' WHERE sprv.product = product.m_product_id' + //
        ' AND   sprv.validFromDate = (SELECT MAX(sprv2.validFromDate)' + //
        '                             FROM m_servicepricerule_version sprv2' + //
        '                             WHERE sprv2.product = product.m_product_id' + //
        "                             AND   sprv2.validFromDate <= strftime ('%Y-%m-%d','now')" + //
        "                             AND   sprv2.active = 'true'" + //
        "                             AND   ((product.includeProducts = 'Y' AND product.includeProductCategories = 'Y') " + //
        "                                    OR (product.includeProducts = 'Y' AND product.includeProductCategories is null)" + //
        "                                    OR (product.includeProducts is null AND product.includeProductCategories = 'Y')" + //
        "                                    OR (product.includeProducts = 'N' AND (sprv2.relatedProduct IS NULL OR sprv2.relatedProduct = ?)) " + //
        "                                    OR (product.includeProductCategories = 'N' AND (sprv2.relatedProductCategory IS NULL OR sprv2.relatedProductCategory = ?))))" + //
        ' AND   (sprv.minimum IS NULL OR sprv.minimum <= ?)' + //
        ' AND   (sprv.maximum IS NULL OR sprv.maximum >= ?)' + //
        " AND   sprv.active = 'true'))) ";

      //including/excluding product categories
      where +=
        "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
      where +=
        "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
      where += 'or product.includeProductCategories is null)) ';
      where +=
        "and product.m_product_id not in ('" +
        (existingServices.length > 0 ? existingServices.join("','") : '-') +
        "')";

      var product = this.orderline.get('product'),
        productId = product.get('forceFilterId') || product.get('id');
      filters.push(productId);
      filters.push(productId);
      filters.push(productId);
      filters.push(this.orderline.get('product').get('productCategory'));
      filters.push(totalAmountSelected);
      filters.push(totalAmountSelected);
      filters.push(productId);
      filters.push(this.orderline.get('product').get('productCategory'));
      filters.push(minimumSelected);
      filters.push(maximumSelected);
      filters.push(this.orderline.get('product').get('productCategory'));
      filters.push(this.orderline.get('product').get('productCategory'));
    }

    var extResult;
    this.filterExtensions.forEach(function(extension) {
      if (extension.sqlExtension) {
        extResult = extension.sqlExtension(
          this.productId,
          this.productList,
          this.orderline,
          this.orderlineList,
          where,
          filters,
          this.extraParams
        );
        where = extResult.where;
        filters = extResult.filters;
      }
    }, this);

    result.where = where;
    result.filters = filters;

    return result;
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
  //force remote mode until services are migrated to indexedDB
  forceRemote: true,
  published: {
    type: 'HIDDEN'
  },
  sqlFilter: function() {
    var result = {};

    result.where = " and product.proposalType = 'MP'";
    result.filters = [];

    return result;
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
  //force remote mode until services are migrated to indexedDB
  forceRemote: true,
  published: {
    type: 'PANEL'
  },
  sqlFilter: function() {
    var result = {};

    result.where =
      " and product.productType = 'S' and product.proposalType = 'FMA'";
    result.filters = [];

    return result;
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
