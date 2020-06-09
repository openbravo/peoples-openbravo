/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Model.OBRDM_OrderFilter = OB.Data.ExtensibleModel.extend({});

  OB.Model.OBRDM_OrderFilter.addProperties([
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
      caption: 'OBRDM_LblDocumentNo',
      operator: OB.Dal.CONTAINS
    },
    {
      name: 'orderDate',
      column: 'orderDate',
      filter: true,
      type: 'TEXT',
      caption: 'OBRDM_LblOrderDate',
      operator: OB.Dal.EQ,
      isDate: true
    },
    {
      name: 'businessPartner',
      column: 'businessPartner',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_LblCustomer',
      operator: OB.Dal.EQ,
      selectorPopup: 'modalcustomer',
      selectorPopupFunction: OB.UTIL.modalCustomer,
      isSelector: true
    },
    {
      name: 'deliveryMode',
      column: 'deliveryMode',
      filter: true,
      type: 'TEXT',
      caption: 'OBRDM_DeliveryMode',
      operator: OB.Dal.EQ,
      isList: true,
      termProperty: 'deliveryModes',
      propertyId: 'id',
      propertyName: 'name',
      excludeValues: ['PickAndCarry']
    },
    {
      name: 'deliveryDate',
      column: 'deliveryDate',
      filter: true,
      type: 'DATE',
      caption: 'OBRDM_DeliveryDate',
      operator: OB.Dal.EQ,
      isDate: true
    }
  ]);
})();
