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
      this.$el.addClass('hidden');
      return this;
    },
    clickEvent: function (e) {
        this.destinations.trigger('depositdrop');
    }
  });

  OB.COMP.ButtonNextCashMgmt = OB.COMP.RegularButton.extend({
    _id: 'cashmgmtnextbutton',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),
    attributes: {'style': 'min-width: 115px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-white').addClass('btnlink-fontgrey');
      this.$el.attr('disabled','disabled');
      return this;
    },
    clickEvent: function (e) {
      if(this.$el.text()===OB.I18N.getLabel('OBPOS_LblDone')){
        this.options.ListDepositsDrops.listdepositsdrops.trigger('depositdrop');
      }else{
        this.$el.text(OB.I18N.getLabel('OBPOS_LblDone'));
        this.options.msginfo.$el.text(OB.I18N.getLabel('OBPOS_MsgTapDone'));
        this.options.ListDepositsDrops.$el.hide();
        this.options.depositsdropsTicket.$el.show();
      }
   }
  });
}());