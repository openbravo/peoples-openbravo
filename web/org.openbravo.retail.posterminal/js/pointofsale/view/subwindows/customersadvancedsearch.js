/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  style: 'background-color: #FFFFFF;',
  name: 'OB.OBPOSPointOfSale.UI.sw_advancedSearch',
  showing: false,  
  components: [{
    tag: 'div',
    style: 'padding: 9px 15px;',
    components: [{
      tag: 'a',
      classes: 'close',
      components: [{
        tag: 'span',
        style: 'font-size: 150%',
        allowHtml: true,
        content: '&times;'
      }],
      tap: function() {
        this.model.get('subWindowManager').set('currentWindow', {
          name: 'mainSubWindow',
          params: []
        });
      },
      init: function(model) {
        this.model = model;
      }
    }, {
      tag: 'h3',
      name: 'divheaderCustomerAdvancedSearch',
      content: OB.I18N.getLabel('OBPOS_TitleCustomerAdvancedSearch')
    }]
  }, {
    kind: 'OB.UI.ListCustomers'
  }]
});