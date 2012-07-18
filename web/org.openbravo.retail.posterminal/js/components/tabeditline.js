/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonTabEditLine = OB.COMP.ToolbarButtonTab.extend({
    tabpanel: '#edition',
    label: OB.I18N.getLabel('OBPOS_LblEdit'),
    render: function () {
      OB.COMP.ToolbarButtonTab.prototype.render.call(this); // super.initialize();
      this.options.modelorder.get('lines').on('click', function () {
        OB.UTIL.setOrderLineInEditMode(true);
        this.$el.tab('show');
        this.$el.parent().parent().addClass('active'); // Due to the complex construction of the toolbar buttons, forced active tab icon is needed
      }, this);
      return this;
    },
    clickEvent: function () {
      OB.COMP.ToolbarButtonTab.prototype.clickEvent.call(this); // super.initialize();
      OB.UTIL.setOrderLineInEditMode(true);
    },
    shownEvent: function (e) {
      this.options.keyboard.show('toolbarscan');
    }
  });

  OB.COMP.TabEditLine = Backbone.View.extend({
    tagName: 'div',
    attributes: {'id': 'edition', 'class': 'tab-pane'},
    initialize: function () {
      var editLine = new OB.COMP.EditLine(this.options);
      this.$el.append(editLine.$el);
    }
  });

}());