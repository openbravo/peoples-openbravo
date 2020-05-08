/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global */

OB.UI.SearchServicesFilter.prototype.filterExtensions.push({
  sqlExtension: function(
    productId,
    productList,
    orderline,
    orderlineList,
    where,
    filters,
    extraParams
  ) {
    var newWhere, result;

    newWhere =
      where +
      " and product.obrdmIsdeliveryservice = '" +
      Boolean(extraParams && extraParams.isDeliveryService) +
      "'";
    result = {
      where: newWhere,
      filters: filters
    };

    return result;
  },
  hqlExtension: function(
    productId,
    productList,
    orderline,
    orderlineList,
    filters,
    extraParams
  ) {
    filters.push({
      columns: ['obrdmIsdeliveryservice'],
      operator: 'equals',
      value: Boolean(extraParams && extraParams.isDeliveryService),
      boolean: true
    });
    return filters;
  }
});

OB.UTIL.HookManager.registerHook(
  'OBPOS_ServicePriceRules_PreSetPriceToLine',
  function(args, callback) {
    if (
      OB.MobileApp.model.get('deliveryPaymentMode') === 'PD' &&
      args.line.get('product').get('obrdmIsdeliveryservice')
    ) {
      var newAmountToPay =
          args.priceChanged || !args.line.has('baseAmountToPayInDeliver')
            ? OB.DEC.mul(args.newprice, args.line.get('qty'))
            : OB.DEC.mul(
                args.line.get('product').get('listPrice'),
                args.line.get('qty')
              ),
        baseAmountToPay = args.line.get('baseAmountToPayInDeliver');
      if (
        OB.UTIL.isNullOrUndefined(baseAmountToPay) ||
        OB.DEC.sub(newAmountToPay, baseAmountToPay) !== 0
      ) {
        args.line.set('obrdmAmttopayindelivery', newAmountToPay);
        args.line.set('baseAmountToPayInDeliver', newAmountToPay);
      }
      args.newprice = 0;
    } else {
      args.line.unset('obrdmAmttopayindelivery');
      args.line.unset('baseAmountToPayInDeliver');
    }
    OB.UTIL.HookManager.callbackExecutor(args, callback);
  }
);
