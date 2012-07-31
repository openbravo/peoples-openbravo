/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.COMP.ButtonNextCashMgmt = OB.COMP.RegularButton.extend({
  _id: 'cashmgmtnextbutton',
  label: OB.I18N.getLabel('OBPOS_LblDone'),
  attributes: {
    'style': 'min-width: 115px;'
  },
  className: 'btnlink-white btnlink-fontgray',
  clickEvent: function(e) {
    //TODO: make some util to do this cleaner
    this.options.parent.options.parent.model.depsdropstosend.trigger('makeDeposits');
  }
});
