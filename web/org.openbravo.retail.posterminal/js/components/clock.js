/*global window, B, $, Backbone */

(function () {

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
      },
    }],
    initialize: function () {
      OB.UTIL.initContentView(this);

      var me = this;
      var updateclock = function () {
          var d = new Date();
          me.divclock.text(OB.I18N.formatHour(d));
          me.divdate.text(OB.I18N.formatDate(d));
          };
      updateclock();
      setInterval(updateclock, 1000);
    },
    attr: function (attributes) {
      if (attributes.className) {
        this.$el.attr('class', attributes.className);
      }
    }
  });
}());