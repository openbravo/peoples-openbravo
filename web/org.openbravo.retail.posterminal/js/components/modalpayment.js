/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

(function() {

  OB = window.OB || {};
  OB.UI = window.OB.UI || {};

  OB.UI.ModalPayment = OB.COMP.Modal.extend({

    header: OB.I18N.getLabel('OBPOS_LblModalPayment'),
    maxheight: '600px',
    getContentView: function() {
      return Backbone.View.extend({
        tagName: 'div'
      });
    },
    show: function(receipt, key, name, paymentMethod, amount) {

      this.paymentcomponent = new paymentMethod.view({paymentMethod: paymentMethod}).render();
      this.contentview.$el.empty().append(this.paymentcomponent.$el);
      this.paymentcomponent.show(receipt, key, name, amount);
      this.$el.modal();
    }
  });

}());