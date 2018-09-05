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
  OB.Model.OrderAssociationsFilter = OB.Data.ExtensibleModel.extend({
    source: 'org.openbravo.retail.posterminal.AssociateOrderLines',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.DATALIMIT,
    remote: 'OBPOS_remote.order',
    _modelName: 'OrderAssociationsFilter',
    forceRemoteEntity: true
  });

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
    name: 'businessPartnerName',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'businessPartner',
    column: 'businessPartner',
    sortName: 'businessPartnerName',
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
    name: 'lineNo',
    column: 'lineNo',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'description',
    column: 'description',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'productName',
    column: 'productName',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'qtyOrdered',
    column: 'qtyOrdered',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'qtyDelivered',
    column: 'qtyDelivered',
    filter: false,
    type: 'TEXT'
  }, {
    name: 'orderDate',
    column: 'orderDate',
    filter: false,
    type: 'TEXT'
  }]);

}());