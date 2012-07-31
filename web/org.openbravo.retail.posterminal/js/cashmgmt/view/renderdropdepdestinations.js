/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.COMP.RenderDropDepDestinations = OB.COMP.SelectButton.extend({
  attributes: {
    'style': 'background-color:#dddddd;  border: 1px solid #ffffff;'
  },
  contentView: [{
    tag: 'div',
    id: 'divcontent',
    attributes: {
      style: 'padding: 1px 0px 1px 5px;'
    }
  }],

  render: function() {
    this.divcontent.text(this.model.get('name'));
    return this;
  }
});