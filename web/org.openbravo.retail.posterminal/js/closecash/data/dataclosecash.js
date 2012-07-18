/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B,_,$*/

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.PaymentCloseCash = function (context) {
    this._id = 'paymentCloseCash';
    var me = context;

//    this.receipt = context.modelorder;
    context.modeldaycash.paymentmethods = new OB.MODEL.PaymentMethodCol();
    context.modeldaycash.paymentmethods.on('closed', function () {
        me.modeldaycash.trigger('print');
        this.proc.exec({
        terminalId: OB.POS.modelterminal.get('terminal').id,
        cashCloseInfo: me.modeldaycash.paymentmethods.serializeToJSON()
          }, function (data, message) {
            if (data && data.exception) {
              OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgFinishCloseError'));
            } else {
              $('#modalFinishClose').modal('show');
            }
          });
        }, this);
        this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashClose');
  };

 }());