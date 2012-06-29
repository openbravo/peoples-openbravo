/*global B, window, define, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Done = OB.COMP.RegularButton.extend({
    _id: 'donebutton',
    label: OB.I18N.getLabel('OBPOS_LblDone'),
    attributes: {'style': 'min-width: 115px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgrey');
      return this;
    },
    clickEvent: function (e) {
        window.location=OB.POS.hrefWindow('retail.pointofsale');
    }
  });
}());