/*
 ************************************************************************************
 * Copyright (C) 2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone, _ */

(function () {
  OB.Model.OrderAssociationsFilter = OB.Data.ExtensibleModel.extend({});

  OB.Model.OrderAssociationsFilter.addProperties([{
    name: 'orderId',
    column: 'orderId',
    filter: false,
    type: 'TEXT',
    operator: OB.Dal.EQ,
    isDate: false
  }, {
    name: 'documentNo',
    column: 'documentNo',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_DocumentNo',
    operator: OB.Dal.CONTAINS
  }, {
    name: 'orderDate',
    column: 'orderDate',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_DateOrdered',
    operator: OB.Dal.EQ,
    isDate: true
  }, {
    name: 'businessPartner',
    column: 'businessPartner',
    filter: true,
    type: 'TEXT',
    caption: 'OBPOS_LblCustomer',
    operator: OB.Dal.EQ,
    selectorPopup: 'modalcustomer',
    isSelector: true
  }]);

}());