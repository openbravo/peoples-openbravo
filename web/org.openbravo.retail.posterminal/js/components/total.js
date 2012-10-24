/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

// Order list
enyo.kind({
  name: 'OB.UI.Total',
  tag: 'span',
  attributes: {
    style: 'font-weight:bold;'
  },
  renderTotal: function (total) {
    this.setContent(OB.I18N.formatCurrency(total));
  }
});