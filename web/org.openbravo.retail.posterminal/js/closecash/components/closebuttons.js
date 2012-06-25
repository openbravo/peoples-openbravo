/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonPrev = OB.COMP.ToolbarButton.extend({
	_id: 'closeprevbutton',
	disabled: 'disabled',
    label: OB.I18N.getLabel('OBPOS_LblPrevStep'),
    clickEvent: function (e) {
    if(this.options.modeldaycash.defaults.step===2){
        //Count Cash back from Post, print & Close.
        this.options.countcash.$el.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.show();
        this.options.postprintclose.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.hide();
        this.options.closenextbutton.$el.text(OB.I18N.getLabel('OBPOS_LblNextStep'));
        this.options.modeldaycash.defaults.step=1;
    }else if(this.options.modeldaycash.defaults.step===1){
        //Pending receipts back from Count Cash.
        this.options.pendingreceipts.$el.show();
        this.options.countcash.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.hide();
        this.options.modeldaycash.defaults.step=0;
        this.$el.attr('disabled','disabled');
    }
    }
  });

  OB.COMP.ButtonNext = OB.COMP.ToolbarButton.extend({
	_id: 'closenextbutton',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),
    clickEvent: function (e) {
    if(this.options.modeldaycash.defaults.step===0){
        //Pending receipts to Count Cash
        this.options.countcash.$el.show();
        this.options.closekeyboard.toolbars.toolbarcountcash.show();
        this.options.pendingreceipts.$el.hide();
        this.options.closekeyboard.toolbars.toolbarempty.hide();
        this.options.modeldaycash.defaults.step=1;
        this.options.closeprevbutton.$el.removeAttr('disabled');
    }else if(this.options.modeldaycash.defaults.step===1){
        //Count Cash to Post, print & Close
        this.options.postprintclose.$el.show();
        this.options.closekeyboard.toolbars.toolbarempty.show();
        this.options.countcash.$el.hide();
        this.options.closekeyboard.toolbars.toolbarcountcash.hide();
        this.options.renderpaymentlines.$el.empty();
        this.options.renderpaymentlines.render();
        this.options.modeldaycash.defaults.step=2;
        this.$el.text(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
    }else if(this.options.modeldaycash.defaults.step===2){
    this.options.modeldaycash.paymentmethods.trigger('closed');
    }

   }
  });

  OB.COMP.ButtonOk =OB.COMP.Button.extend({
	_id: 'okbutton',
    iconright: 'icon-ok icon-black',
    tagName: 'a',
    className: 'btnlink btnlink-green',
    label: '',
   clickEvent: function (e) {
	this.$el.hide();
	$('a[button*="allokbutton"]').css('visibility','hidden');
    var elem = this.me.options.modeldaycash.paymentmethods.get(this.options[this._id].rowid);
    this.options['counted_'+this.options[this._id].rowid].$el.text(elem.get('expected').toString());
    elem.set('counted',OB.DEC.add(0,elem.get('expected')));
    this.me.options.modeldaycash.set('totalCounted',OB.DEC.add(this.me.options.modeldaycash.get('totalCounted'),elem.get('counted')));
    this.options['counted_'+this.rowid].$el.show();
   },
   render: function () {
      this.$el.addClass('btnlink');
      if (this.icon) {
        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
      }
      this.$el.append($('<span>' + this.label + '</span>'));
      if (this.iconright) {
        this.$el.append($('<i class=\"' + this.iconright + '\"></i>'));
      }
      return this;
    }
  });

  OB.COMP.ButtonEdit =OB.COMP.Button.extend({
	_id: 'editbutton',
    iconright: 'icon-pencil icon-black',
    tagName: 'a',
    className: 'btnlink btnlink-orange',
    label: '',
   clickEvent: function (e) {
	   var that = this;
       $($(this.me.options.closekeyboard.toolbars.toolbarcountcash).find('.btnkeyboard')).each(function(){
       if($(this).text()===that.commercialName){
       that.me.options.closekeyboard.trigger('command', that.searchKey);
   }
   });
   },
   render: function () {
      this.$el.addClass('btnlink');
      if (this.icon) {
        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
      }
      this.$el.append($('<span>' + this.label + '</span>'));
      if (this.iconright) {
        this.$el.append($('<i class=\"' + this.iconright + '\"></i>'));
      }
      return this;
    }
  });
}());