/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonPrev = OB.COMP.ToolbarButton.extend({
	_id: 'closeprevbutton',
//    icon: 'icon-chevron-left icon-white  btn-icon-left',
	disabled: 'disabled',
    label: OB.I18N.getLabel('OBPOS_LblPrevStep'),
    clickEvent: function (e) {
    if(this.options.modeldaycash.defaults.step===2){
        this.options.closenextbutton.$el.text(OB.I18N.getLabel('OBPOS_LblNextStep'));
        this.options.postprintclose.$el.hide();
        this.options.countcash.$el.show();
        this.options.modeldaycash.defaults.step=1;
        this.options.closenextbutton.$el.removeAttr('disabled');
    }else if(this.options.modeldaycash.defaults.step===1){
        this.options.countcash.$el.hide();
        this.options.pendingreceipts.$el.show();
        this.options.modeldaycash.defaults.step=0;
        this.$el.attr('disabled','disabled');
    }
    //this.options.modeldaycash.prevStep();
    }
  });

  OB.COMP.ButtonNext = OB.COMP.ToolbarButton.extend({
	_id: 'closenextbutton',
//    iconright: 'icon-chevron-right icon-white  btn-icon-right',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),

    clickEvent: function (e) {
    if(this.options.modeldaycash.defaults.step===0){
    this.options.pendingreceipts.$el.hide();
        this.options.countcash.$el.show();
        this.options.modeldaycash.defaults.step=1;
        this.options.closeprevbutton.$el.removeAttr('disabled');
    }else if(this.options.modeldaycash.defaults.step===1){
    this.options.countcash.$el.hide();
        this.options.postprintclose.$el.show();
        this.options.modeldaycash.defaults.step=2;
        this.$el.text(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
        this.options.renderpaymentlines.$el.empty();
        this.options.renderpaymentlines.render();
        //this.$el.attr('disabled','disabled');
    }
    //this.options.modeldaycash.nextStep();
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
    this.options['counted_'+this.rowid].$el.show();
    //this.options.countcash.;
    //this.options.modeldaycash.ok();
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
    //this.$el.hide();
    //this.options.countcash.;
    //this.options.modeldaycash.ok();
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