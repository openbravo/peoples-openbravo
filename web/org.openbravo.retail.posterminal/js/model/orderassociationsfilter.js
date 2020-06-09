/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Model.OrderAssociationsFilter = OB.Data.ExtensibleModel.extend({
    source: 'org.openbravo.retail.posterminal.AssociateOrderLines',
    dataLimit: OB.Dal.DATALIMIT,
    remoteDataLimit: OB.Dal.DATALIMIT,
    remote: 'OBPOS_remote.order',
    _modelName: 'OrderAssociationsFilter',
    forceRemoteEntity: true
  });

  OB.Model.OrderAssociationsFilter.addProperties([
    {
      name: 'orderId',
      column: 'orderId',
      filter: false,
      type: 'TEXT',
      operator: OB.Dal.EQ,
      isDate: false
    },
    {
      name: 'documentNo',
      column: 'documentNo',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_DocumentNo',
      operator: OB.Dal.CONTAINS
    },
    {
      name: 'orderDate',
      column: 'orderDate',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_DateOrdered',
      operator: OB.Dal.EQ,
      isDate: true
    },
    {
      name: 'bpName',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'bpId',
      column: 'bpId',
      sortName: 'bpName',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblCustomer',
      isSelector: true,
      selectorPopup: 'modalcustomer',
      selectorPopupFunction: OB.UTIL.modalCustomer,
      operator: OB.Dal.EQ,
      preset: {
        id: '',
        name: ''
      }
    },
    {
      name: 'lineNo',
      column: 'lineNo',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'qty',
      column: 'qty',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'net',
      column: 'net',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'gross',
      column: 'gross',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'orderlineId',
      column: 'orderlineId',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'productId',
      column: 'productId',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'productCategory',
      column: 'productCategory',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'productName',
      column: 'productName',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'orderDate',
      column: 'orderDate',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discount_ruleId',
      column: 'discount_ruleId',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discountType_id',
      column: 'discountType_id',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discountType_name',
      column: 'discountType_name',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discount_userAmt',
      column: 'discount_userAmt',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discount_totalAmt',
      column: 'discount_totalAmt',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discount_displayedTotalAmount',
      column: 'discount_displayedTotalAmount',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'discount_actualAmt',
      column: 'discount_actualAmt',
      filter: false,
      type: 'TEXT'
    }
  ]);
})();
