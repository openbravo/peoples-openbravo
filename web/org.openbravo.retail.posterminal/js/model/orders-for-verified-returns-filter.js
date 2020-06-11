/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

(function() {
  OB.Model.VReturnsFilter = OB.Data.ExtensibleModel.extend({
    source: 'org.openbravo.retail.posterminal.PaidReceiptsFilter',
    dataLimit: OB.Dal.DATALIMIT,
    remote: 'OBPOS_remote.order',
    _modelName: 'VReturnsFilter',
    forceRemoteEntity: true
  });

  OB.Model.VReturnsFilter.addProperties([
    {
      name: 'id',
      column: 'id',
      primaryKey: true,
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'documentTypeId',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'documentStatus',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'orderDate',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'creationDate',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'totalamount',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'businessPartnerName',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'organization',
      filter: false,
      type: 'TEXT'
    },
    {
      name: 'documentNo',
      column: 'documentNo',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_DocumentNo'
    },
    {
      name: 'businessPartner',
      column: 'businessPartner',
      sortName: 'businessPartnerName',
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
      name: 'externalBusinessPartnerReference',
      column: 'externalBusinessPartnerReference',
      filter: false,
      type: 'TEXT',
      caption: 'OBPOS_LblCustomer'
    },
    {
      name: 'orderDateFrom',
      column: 'orderDate',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_DateFrom',
      isDate: true,
      hqlFilter: 'OrderDateFrom_Filter'
    },
    {
      name: 'orderDateTo',
      column: 'orderDateTo',
      filter: true,
      type: 'TEXT',
      caption: 'OBPOS_DateTo',
      isDate: true,
      hqlFilter: 'OrderDateTo_Filter'
    },
    {
      name: 'totalamountFrom',
      column: 'totalamount',
      type: 'TEXT',
      filter: true,
      caption: 'OBPOS_AmountFrom',
      isNumeric: true,
      hqlFilter: 'OrderAmountFrom_Filter'
    },
    {
      name: 'totalamountTo',
      column: 'totalamountTo',
      type: 'TEXT',
      filter: true,
      caption: 'OBPOS_AmountTo',
      isNumeric: true,
      hqlFilter: 'OrderAmountTo_Filter'
    },
    {
      name: 'orderType',
      column: 'orderType',
      filter: false,
      type: 'TEXT',
      caption: 'OBPOS_OrderType',
      isList: true,
      termProperty: 'orderType',
      propertyId: 'id',
      propertyName: 'name',
      operator: '='
    },
    {
      name: 'iscancelled',
      filter: false,
      type: 'BOOL'
    },
    {
      name: 'store',
      column: 'organizationName',
      type: 'TEXT',
      filter: true,
      isMandatoryFilter: true,
      caption: 'OBPOS_Store',
      isList: true,
      termProperty: 'store',
      propertyId: 'id',
      propertyName: 'name',
      hqlFilter: 'Store',
      remoteEntity: true,
      applyUIRestrictions: true,
      disableSorting: true
    }
  ]);
})();
