/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.RenderDropDepDestinations =  OB.COMP.SelectButton.extend({
	 
    attributes: {'style':'background-color:#dddddd;  border: 1px solid #ffffff;'},
    render: function() {
      this.$el.append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), attr: {style: 'padding: 1px 0px 1px 5px;'}, content: [
             this.model.get('name')
          ]}
        ]}
      ).$el);
      return this;
    }
  });
}());