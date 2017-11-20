/*
 ************************************************************************************
 * Copyright (C) 2017 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {

  OB.Model.OrderFilter = OB.Data.ExtensibleModel.extend({
    source: 'org.openbravo.retail.posterminal.PaidReceiptsFilter',
    dataLimit: OB.Dal.DATALIMIT,
    remote: 'OBPOS_remote.order',
    _modelName: 'OrderFilter'
  });

  OB.Model.OrderFilter.addProperties([{
    name: 'id',
    column: 'id',
    primaryKey: true,
    filter: false,
    type: 'TEXT'
  }, {
    name: 'documentTypeId',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'documentStatus',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'orderDate',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'creationDate',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'totalamount',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'businessPartnerName',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'documentNo',
    column: 'documentNo',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_DocumentNo'
  }, {
    name: 'businessPartner',
    column: 'businessPartner',
    serverColumn: 'businessPartnerName',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblCustomer',
    isSelector: true,
    selectorPopup: 'modalcustomer',
    operator: OB.Dal.EQ,
    preset: {
      id: '',
      name: ''
    }
  }, {
    name: 'orderDateFrom',
    column: 'orderDate',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_DateFrom',
    isDate: true,
    hqlFilter: 'OrderDateFrom_Filter'
  }, {
    name: 'orderDateTo',
    column: 'orderDateTo',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_DateTo',
    isDate: true,
    hqlFilter: 'OrderDateTo_Filter'
  }, {
    name: 'totalamountFrom',
    column: 'totalamount',
    type: 'TEXT',
    filter: true,
    caption: 'OBPOS_AmountFrom',
    isNumeric: true,
    hqlFilter: 'OrderAmountFrom_Filter'
  }, {
    name: 'totalamountTo',
    column: 'totalamountTo',
    type: 'TEXT',
    filter: true,
    caption: 'OBPOS_AmountTo',
    isNumeric: true,
    hqlFilter: 'OrderAmountTo_Filter'
  }, {
    name: 'orderType',
    column: 'orderType',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_OrderType',
    isList: true,
    termProperty: 'orderType',
    propertyId: 'id',
    propertyName: 'name',
    operator: '='
  }]);

}());