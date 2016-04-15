/*
 ************************************************************************************
 * Copyright (C) 2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.SearchServicesFilter',
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
  sqlFilter: function () {
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
        existingServices, lineIdList;

    if (this.productList && this.productList.length > 0) {
      //product multiselection
      lineIdList = this.orderlineList.map(function (line) {
        return line.get('id');
      });
      existingServices = OB.MobileApp.model.receipt.get('lines').filter(function (l) {
        if (l.get('relatedLines') && _.intersection(lineIdList, _.pluck(l.get('relatedLines'), 'orderlineId')).length > 0) {
          return true;
        }
        return false;
      }).map(function (line) {
        var product = line.get('product');
        return product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      });

      //build auxiliar string for the products filter and for the categories filter:
      this.orderlineList.forEach(function (l) {
        if (appendProdComma) {
          auxProdStr += ', ';
        } else {
          appendProdComma = true;
        }
        auxProdStr += '?';

        var product = l.get('product');
        auxProdFilters.push(product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id'));

        if (auxCatFilters.indexOf(l.get('product').get('productCategory')) < 0) {
          if (appendCatComma) {
            auxCatStr += ', ';
          } else {
            appendCatComma = true;
          }
          auxCatStr += '?';

          auxCatFilters.push(l.get('product').get('productCategory'));
        }
      });

      auxProdStr += ')';
      auxCatStr += ')';

      where = " and product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

      if (this.productList.length > 1) {
        where += " product.availableForMultiline = 'true' and ";
      }

      //including/excluding products
      where += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id in " + auxProdStr + " )) ";
      where += "or (product.includeProducts = 'N' and " + auxProdFilters.length + " = (select count(*) from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id in " + auxProdStr + " )) ";
      where += "or product.includeProducts is null) ";

      //including/excluding product categories
      where += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id in " + auxCatStr + " )) ";
      where += "or (product.includeProductCategories = 'N' and " + auxCatFilters.length + " = (select count(*) from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id in " + auxCatStr + " )) ";
      where += "or product.includeProductCategories is null)) ";
      where += "and product.m_product_id not in ('" + existingServices.join("','") + "')";

      filters = filters.concat(auxProdFilters);
      filters = filters.concat(auxProdFilters);
      filters = filters.concat(auxCatFilters);
      filters = filters.concat(auxCatFilters);

    } else if (this.productId) {
      // Only one product
      existingServices = OB.MobileApp.model.receipt.get('lines').filter(function (l) {
        if (l.get('relatedLines') && _.indexOf(_.pluck(l.get('relatedLines'), 'orderlineId'), me.orderline.get('id')) !== -1) {
          return true;
        }
        return false;
      }).map(function (line) {
        var product = line.get('product');
        return product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      });

      where = " and product.productType = 'S' and (product.isLinkedToProduct = 'true' and ";

      //including/excluding products
      where += "((product.includeProducts = 'Y' and not exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? )) ";
      where += "or (product.includeProducts = 'N' and exists (select 1 from m_product_service sp where product.m_product_id = sp.m_product_id and sp.m_related_product_id = ? )) ";
      where += "or product.includeProducts is null) ";

      //including/excluding product categories
      where += "and ((product.includeProductCategories = 'Y' and not exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id =  ? )) ";
      where += "or (product.includeProductCategories = 'N' and exists (select 1 from m_product_category_service spc where product.m_product_id = spc.m_product_id and spc.m_product_category_id  = ? )) ";
      where += "or product.includeProductCategories is null)) ";
      where += "and product.m_product_id not in ('" + existingServices.join("','") + "')";

      var product = this.orderline.get('product'),
          productId = product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      filters.push(productId);
      filters.push(productId);
      filters.push(this.orderline.get('product').get('productCategory'));
      filters.push(this.orderline.get('product').get('productCategory'));
    }

    result.where = where;
    result.filters = filters;

    return result;
  },
  hqlCriteria: function () {
    var me = this,
        prodList, catList, lineIdList, existingServices;
    if (this.orderlineList && this.orderlineList.length > 0) {
      prodList = this.orderlineList.map(function (line) {
        var product = line.get('product');
        return product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      });
      catList = this.orderlineList.map(function (line) {
        return line.get('product').get('productCategory');
      });
      catList = catList.sort().filter(function (item, pos, ary) {
        return !pos || item !== ary[pos - 1];
      });
      lineIdList = this.orderlineList.map(function (line) {
        return line.get('id');
      });
      existingServices = OB.MobileApp.model.receipt.get('lines').filter(function (l) {
        if (l.get('relatedLines') && _.intersection(lineIdList, _.pluck(l.get('relatedLines'), 'orderlineId')).length > 0) {
          return true;
        }
        return false;
      }).map(function (line) {
        var product = line.get('product');
        return product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      });
      return [{
        columns: [],
        operator: OB.Dal.FILTER,
        value: (this.orderlineList.length > 1 ? 'Services_Filter_Multi' : 'Services_Filter'),
        params: [prodList.join("','"), catList.join("','"), prodList.length, catList.length, (existingServices.length > 0 ? existingServices.join("','") : '-')]
      }, {
        columns: ['ispack'],
        operator: 'equals',
        value: false,
        fieldType: 'forceString'
      }];
    } else {
      existingServices = OB.MobileApp.model.receipt.get('lines').filter(function (l) {
        if (l.get('relatedLines') && _.indexOf(_.pluck(l.get('relatedLines'), 'orderlineId'), me.orderline.get('id')) !== -1) {
          return true;
        }
        return false;
      }).map(function (line) {
        var product = line.get('product');
        return product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id');
      });
      var product = this.orderline.get('product');
      return [{
        columns: [],
        operator: OB.Dal.FILTER,
        value: 'Services_Filter',
        params: [product.get('forceFilterId') ? product.get('forceFilterId') : product.get('id'), product.get('productCategory'), '', '', (existingServices.length > 0 ? existingServices.join("','") : '-')]
      }, {
        columns: ['ispack'],
        operator: 'equals',
        value: false,
        fieldType: 'forceString'
      }];
    }
  },
  lineAttributes: function () {

    var productList = [];

    if (this.orderlineList) {
      this.orderlineList.forEach(function (ol) {
        ol.set('preserveId', true);
        productList.push({
          orderlineId: ol.get('id'),
          productName: ol.get('product').get('_identifier')
        });
      });
    } else if (this.orderline) {
      this.orderline.set('preserveId', true);
      productList.push({
        orderlineId: this.orderline.get('id'),
        productName: this.orderline.get('product').get('_identifier')
      });
    }

    return {
      relatedLines: productList
    };
  },
  initComponents: function () {
    this.inherited(arguments);
    this.caption = OB.I18N.getLabel('OBPOS_ServicesFor');
  }
});

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.MandatoryServicesFilter',
  filterName: 'MandatoryServicesFilter',
  published: {
    type: 'HIDDEN'
  },
  sqlFilter: function () {
    var result = {};

    result.where = " and product.proposalType = 'MP'";
    result.filters = [];

    return result;
  },
  hqlCriteria: function () {
    return [{
      columns: [],
      operator: OB.Dal.FILTER,
      value: 'Mandatory_Services',
      params: []
    }];
  }
});

enyo.kind({
  kind: 'OB.UI.SearchProductCharacteristicFilter',
  name: 'OB.UI.FinalMandatoryServicesFilter',
  filterName: 'FinalMandatoryServicesFilter',
  published: {
    type: 'PANEL'
  },
  sqlFilter: function () {
    var result = {};

    result.where = " and product.productType = 'S' and product.proposalType = 'FMA'";
    result.filters = [];

    return result;
  },
  hqlCriteria: function () {
    return [{
      columns: [],
      operator: OB.Dal.FILTER,
      value: 'Final_Services',
      params: []
    }, {
      columns: ['ispack'],
      operator: 'equals',
      value: false,
      fieldType: 'forceString'
    }];
  },
  renderInfo: function () {
    var content = {
      content: OB.I18N.getLabel('OBPOS_FinalServices')
    };
    return content;
  }
});