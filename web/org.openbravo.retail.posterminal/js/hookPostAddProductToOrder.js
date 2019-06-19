/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global _ */

OB.UTIL.HookManager.registerHook('OBPOS_PostAddProductToOrder', function(
  args,
  callbacks
) {
  if (OB.MobileApp.model.hasPermission('OBRDM_EnableDeliveryModes', true)) {
    if (args.newLine) {
      var order = args.receipt,
        orderLine = args.orderline;
      if (
        orderLine.get('product').get('productType') !== 'S' &&
        !orderLine.get('obrdmDeliveryMode')
      ) {
        var defaultDeliveryModeInProduct,
          defaultDeliveryMode,
          deliveryDateInProduct,
          deliveryTimeInProduct;
        if (order.get('isLayaway') || order.get('orderType') === 2) {
          defaultDeliveryModeInProduct = orderLine
            .get('product')
            .get('obrdmDeliveryModeLyw');
          defaultDeliveryMode = defaultDeliveryModeInProduct
            ? defaultDeliveryModeInProduct
            : order.get('obrdmDeliveryModeProperty')
            ? order.get('obrdmDeliveryModeProperty')
            : 'PickAndCarry';
        } else {
          defaultDeliveryModeInProduct = orderLine
            .get('product')
            .get('obrdmDeliveryMode');
          deliveryDateInProduct = orderLine
            .get('product')
            .get('obrdmDeliveryDate');
          deliveryTimeInProduct = orderLine
            .get('product')
            .get('obrdmDeliveryTime');
          defaultDeliveryMode = defaultDeliveryModeInProduct
            ? defaultDeliveryModeInProduct
            : order.get('obrdmDeliveryModeProperty')
            ? order.get('obrdmDeliveryModeProperty')
            : 'PickAndCarry';
        }
        orderLine.set('obrdmDeliveryMode', defaultDeliveryMode);
        if (
          orderLine.get('obrdmDeliveryMode') === 'PickupInStoreDate' ||
          orderLine.get('obrdmDeliveryMode') === 'HomeDelivery'
        ) {
          var currentDate = new Date();
          currentDate.setHours(0);
          currentDate.setMinutes(0);
          currentDate.setSeconds(0);
          orderLine.set(
            'obrdmDeliveryDate',
            defaultDeliveryModeInProduct
              ? deliveryDateInProduct
                ? deliveryDateInProduct
                : currentDate
              : order.get('obrdmDeliveryDateProperty')
          );
        }
        if (orderLine.get('obrdmDeliveryMode') === 'HomeDelivery') {
          var currentTime = new Date();
          currentTime.setSeconds(0);
          orderLine.set(
            'obrdmDeliveryTime',
            defaultDeliveryModeInProduct
              ? deliveryTimeInProduct
                ? deliveryTimeInProduct
                : currentTime
              : order.get('obrdmDeliveryTimeProperty')
          );
        }
        orderLine.set(
          'nameDelivery',
          _.find(OB.MobileApp.model.get('deliveryModes'), function(dm) {
            return dm.id === orderLine.get('obrdmDeliveryMode');
          }).name
        );
      }
    }
  }
  OB.UTIL.HookManager.callbackExecutor(args, callbacks);
});
