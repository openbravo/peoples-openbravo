/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonPrev = OB.COMP.RegularButton.extend({
    _id: 'closeprevbutton',
	  disabled: 'disabled',
    label: OB.I18N.getLabel('OBPOS_LblPrevStep'),
    attributes: {'style': 'min-width: 115px; margin: 5px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-fontgrey');
      return this;
    },
    clickEvent: function (e) {
      if (this.options.modeldaycash.defaults.step === 2) {
        //Count Cash back from Post, print & Close.
        this.options.countcash.$el.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.show();
        this.options.postprintclose.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.hide();
        this.options.closenextbutton.$el.text(OB.I18N.getLabel('OBPOS_LblNextStep'));
        this.options.modeldaycash.defaults.step=1;
      } else if (this.options.modeldaycash.defaults.step === 1) {
        //Pending receipts back from Count Cash.
        this.options.pendingreceipts.$el.show();
        this.options.countcash.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.hide();
        this.options.modeldaycash.defaults.step=0;
        this.$el.attr('disabled','disabled');
        this.options.closenextbutton.$el.removeAttr('disabled');
    }
    }
  });

  OB.COMP.ButtonNext = OB.COMP.RegularButton.extend({
    _id: 'closenextbutton',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),
    attributes: {'style': 'min-width: 115px; margin: 5px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-fontgrey');
      return this;
    },
    clickEvent: function (e) {
      if(this.options.modeldaycash.defaults.step === 0){
        //Pending receipts to Count Cash
        this.options.countcash.$el.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.show();
        this.options.pendingreceipts.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.hide();
        this.options.modeldaycash.defaults.step=1;
        this.options.closeprevbutton.$el.removeAttr('disabled');
        if($('button[button="okbutton"][style!="display: none; "]').length!==0){
          this.$el.attr('disabled','disabled');
        }
      } else if (this.options.modeldaycash.defaults.step === 1) {
        //Count Cash to Post, print & Close
        this.options.postprintclose.$el.show();
        this.options.closekeyboard.toolbars.toolbarempty.show();
        this.options.countcash.$el.hide();
        this.options.closekeyboard.toolbars.toolbarcountcash.hide();
        this.options.renderpaymentlines.$el.empty();
        this.options.renderpaymentlines.render();
        this.options.modeldaycash.defaults.step=2;
        this.$el.text(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
      } else if (this.options.modeldaycash.defaults.step === 2) {
        this.options.modeldaycash.paymentmethods.trigger('closed');
      }
   }
  });

  OB.COMP.ButtonOk =OB.COMP.SmallButton.extend({
	_id: 'okbutton',
    icon: 'btn-icon btn-icon-check',
    className: 'btnlink-green btnlink-cashup-ok',
    label: '',
   clickEvent: function (e) {
	this.$el.hide();
	$('button[button*="allokbutton"]').css('visibility','hidden');
    var elem = this.me.options.modeldaycash.paymentmethods.get(this.options[this._id].rowid);
    this.options['counted_'+this.options[this._id].rowid].$el.text(elem.get('expected').toString());
    elem.set('counted',OB.DEC.add(0,elem.get('expected')));
    this.me.options.modeldaycash.set('totalCounted',OB.DEC.add(this.me.options.modeldaycash.get('totalCounted'),elem.get('counted')));
    this.options['counted_'+this.rowid].$el.show();
    if($('button[button="okbutton"][style!="display: none; "]').length===0){
      this.me.options.closenextbutton.$el.removeAttr('disabled');
    }
   }
  });

  OB.COMP.ButtonEdit =OB.COMP.SmallButton.extend({
    _id: 'editbutton',
    icon: 'btn-icon btn-icon-edit',
    className: 'btnlink-orange btnlink-cashup-edit',
    label: '',
   clickEvent: function (e) {
	   var that = this;
       $($(this.me.options.closekeyboard.toolbars.toolbarcountcash).find('.btnkeyboard')).each(function(){
       if($(this).text()===that.commercialName){
       that.me.options.closekeyboard.trigger('command', that.searchKey);
   }
   });
   }
  });

  OB.COMP.ButtonVoid = OB.COMP.RegularButton.extend({
    _id: 'closevoidbutton',
    label: 'VOID',
    order: null,
    me: null,
    attributes: {'style': 'min-width: 115px; margin: 5px;'},
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-fontgrey');
      return this;
    },
    clickEvent: function (e) {
       this.me.receiptlist.remove(this.order);
       OB.Dal.remove(this.order, function(){
         return true;
       }, function(){
         OB.UTIL.showError('Error removing');
       });
    }
  });
}());