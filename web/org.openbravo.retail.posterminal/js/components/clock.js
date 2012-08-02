/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, $, Backbone */

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Generic Clock
  OB.COMP.Clock = Backbone.View.extend({
    tagName: 'div',
    contentView: [{
      id: 'divclock',
      tag: 'div',
      attributes: {
        'class': 'clock-time'
      }
    }, {
      id: 'divdate',
      tag: 'div',
      attributes: {
        'class': 'clock-date'
      }
    }],
    initialize: function() {
      OB.UTIL.initContentView(this);

      var me = this;
      var updateclock = function() {
          var d = new Date();
          me.divclock.text(OB.I18N.formatHour(d));
          me.divdate.text(OB.I18N.formatDate(d));
          };
      updateclock();
      setInterval(updateclock, 1000);
    },
    attr: function(attributes) {
      if (attributes.className) {
        this.$el.attr('class', attributes.className);
      }
    }
  });
}());

enyo.kind({
  name: 'OB.UI.Clock',
  tag: 'div',
  components: [{
    tag: 'div',
    classes: 'clock-time',
    name: 'clock'
  }, {
    tag: 'div',
    classes: 'clock-date',
    name: 'date'
  }],

  initComponents: function() {
    var me = this,
        updateClock = function() {
        var d = new Date();
        me.$.clock.setContent(OB.I18N.formatHour(d));
        me.$.date.setContent(OB.I18N.formatDate(d));
        };

    this.inherited(arguments);
    updateClock();
    setInterval(updateClock, 15000);
  }
});