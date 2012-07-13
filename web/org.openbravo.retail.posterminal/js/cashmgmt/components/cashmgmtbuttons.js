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
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgray');
      this.$el.addClass('hidden');
      return this;
    },
    clickEvent: function (e) {
        this.destinations.trigger('depositdrop');
    }
  });

  OB.COMP.ButtonNextCashMgmt = OB.COMP.RegularButton.extend({
    _id: 'cashmgmtnextbutton',
    label: OB.I18N.getLabel('OBPOS_LblDone'),
    attributes: {'style': 'min-width: 115px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgray');
      return this;
    },
    clickEvent: function (e) {
        if(this.options.ListDepositsDrops.totalTendered + this.options.ListDepositsDrops.totalToDrop >= 0 ){
          this.options.ListDepositsDrops.listdepositsdrops.trigger('depositdrop');
        }else{
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
        }
   }
  });
}());