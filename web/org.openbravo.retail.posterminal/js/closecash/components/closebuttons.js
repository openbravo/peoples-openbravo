/*global window, B, $, Backbone */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.ButtonPrev = OB.COMP.SmallButton.extend({
    _id: 'closeprevbutton',
    disabled: 'disabled',
    label: OB.I18N.getLabel('OBPOS_LblPrevStep'),
    attributes: {'style': 'min-width: 115px; margin: 5px;'},
    render: function () {
      OB.COMP.SmallButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-fontgray');
      return this;
    },
    clickEvent: function (e) {
      var found = false;
      if (this.options.modeldaycash.defaults.step === 3 || this.options.modeldaycash.defaults.step === 2) {
        //Count Cash back from Post, print & Close.
        if(this.options.modeldaycash.defaults.step === 2){
          this.options.modeldaycash.set('allowedStep', this.options.modeldaycash.get('allowedStep')-1);
        }else{
          this.options.modeldaycash.defaults.step=2;
        }
        found = false;
        this.options.closenextbutton.$el.attr('disabled','disabled');
        $(".active").removeClass("active");
        //Count Cash to Cash to keep or Cash to keep to Cash to keep
        if( $(".active").length===0){
          this.options.cashtokeep.$el.show();
          this.options.postprintclose.$el.hide();
          this.options.closenextbutton.$el.text(OB.I18N.getLabel('OBPOS_LblNextStep'));
        }
         while(this.options.modeldaycash.get('allowedStep') >= 0 ){

           if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').automatemovementtoother){
            found = true;
            $('#cashtokeepheader').text(OB.I18N.getLabel('OBPOS_LblStep3of4',[this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('name')]));
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').keepfixedamount){
              $('#keepfixedamountlbl').text(OB.I18N.formatCurrency(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').amount));
              $('#keepfixedamount').val(OB.I18N.formatCurrency(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').amount));
              $('#keepfixedamount').show();
              $('#keepfixedamountlbl').show();
            }else{
              $('#keepfixedamount').hide();
              $('#keepfixedamountlbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowmoveeverything){
              $('#allowmoveeverything').val(OB.I18N.formatCurrency(OB.DEC.Zero));
              $('#allowmoveeverythinglbl').text('Nothing');
              $('#allowmoveeverything').show();
              $('#allowmoveeverythinglbl').show();
            }else{
              $('#allowmoveeverything').hide();
              $('#allowmoveeverythinglbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowdontmove){
              $('#allowdontmove').val(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('counted'));
              $('#allowdontmovelbl').text('Total amount of '+OB.I18N.formatCurrency(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('counted')));
              $('#allowdontmove').show();
              $('#allowdontmovelbl').show();
            }else{
              $('#allowdontmove').hide();
              $('#allowdontmovelbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowvariableamount){
              $('#allowvariableamountlbl').text('Other');
              $('#allowvariableamount').show();
              $('#allowvariableamountlbl').show();
              $('#variableamount').show();
              $('#variableamount').val('');
            }else {
              $('#allowvariableamount').hide();
              $('#allowvariableamountlbl').hide();
              $('#variableamount').hide();
            }
            break;
          }
           this.options.modeldaycash.set('allowedStep', this.options.modeldaycash.get('allowedStep')-1);
        }
        if(found===false){
          this.options.countcash.$el.show();
          this.options.cashtokeep.$el.hide();
          this.options.closekeyboard.show('toolbarcountcash');
          this.options.modeldaycash.defaults.step=1;
          this.options.modeldaycash.set('allowedStep', 0);
          this.options.closenextbutton.$el.removeAttr('disabled');
        }
      } else if (this.options.modeldaycash.defaults.step === 1) {
        //Pending receipts back from Count Cash.
        this.options.pendingreceipts.$el.show();
        this.options.countcash.$el.hide();
        this.options.closekeyboard.show('toolbarempty');
        this.options.modeldaycash.defaults.step=0;
        this.$el.attr('disabled','disabled');
        this.options.closenextbutton.$el.removeAttr('disabled');
    }
    }
  });

  OB.COMP.ButtonNext = OB.COMP.SmallButton.extend({
    _id: 'closenextbutton',
    label: OB.I18N.getLabel('OBPOS_LblNextStep'),
    attributes: {'style': 'min-width: 115px; margin: 5px;'},
    render: function () {
      OB.COMP.SmallButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-fontgray');
      return this;
    },
    clickEvent: function (e) {
      var found = false;
      if(this.options.modeldaycash.defaults.step === 0){
        //Pending receipts to Count Cash
        this.options.countcash.$el.show();
        this.options.pendingreceipts.$el.hide();
        this.options.closekeyboard.show('toolbarcountcash');
        this.options.modeldaycash.defaults.step=1;
        this.options.closeprevbutton.$el.removeAttr('disabled');
        if($('button[button="okbutton"][style!="display: none; "]').length!==0){
          this.$el.attr('disabled','disabled');
        }
      } else if (this.options.modeldaycash.defaults.step === 1 || this.options.modeldaycash.defaults.step === 2){
        found = false;
        if(this.options.modeldaycash.defaults.step === 2){
          this.options.modeldaycash.set('allowedStep', this.options.modeldaycash.get('allowedStep')+1);
        }
        this.$el.attr('disabled','disabled');
        this.options.modeldaycash.defaults.step = 2;
      //Count Cash to Cash to keep or Cash to keep to Cash to keep
        if( $(".active").length>0 && this.options.modeldaycash.get('allowedStep')!==0){
          if($('.active').val()===""){//Variable Amount
            this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')-1).get('paymentMethod').amountToKeep=OB.I18N.formatCurrency(OB.I18N.parseNumber($('#variableamount').val()));
          }else{
            this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')-1).get('paymentMethod').amountToKeep=OB.I18N.formatCurrency(OB.I18N.parseNumber($('.active').val()));
          }
          $(".active").removeClass("active");
        }else{
          this.options.countcash.$el.hide();
          this.options.cashtokeep.$el.show();
          this.options.closekeyboard.show('toolbarempty');
        }
         while(this.options.modeldaycash.get('allowedStep') < this.options.modeldaycash.paymentmethods.length){

           if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').automatemovementtoother){
            found = true;
            $('#cashtokeepheader').text(OB.I18N.getLabel('OBPOS_LblStep3of4', [this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('name')]));
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').keepfixedamount){
              $('#keepfixedamountlbl').text(OB.I18N.formatCurrency(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').amount));
              $('#keepfixedamount').val(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').amount);
              $('#keepfixedamount').show();
              $('#keepfixedamountlbl').show();
            }else{
              $('#keepfixedamount').hide();
              $('#keepfixedamountlbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowmoveeverything){
              $('#allowmoveeverything').val(0);
              $('#allowmoveeverythinglbl').text('Nothing');
              $('#allowmoveeverything').show();
              $('#allowmoveeverythinglbl').show();
            }else{
              $('#allowmoveeverything').hide();
              $('#allowmoveeverythinglbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowdontmove){
              $('#allowdontmove').val(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('counted'));
              $('#allowdontmovelbl').text('Total amount of '+OB.I18N.formatCurrency(OB.DEC.add(0,this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('counted'))));
              $('#allowdontmove').show();
              $('#allowdontmovelbl').show();
            }else{
              $('#allowdontmove').hide();
              $('#allowdontmovelbl').hide();
            }
            if(this.options.modeldaycash.paymentmethods.at(this.options.modeldaycash.get('allowedStep')).get('paymentMethod').allowvariableamount){
              $('#allowvariableamountlbl').text('Other');
              $('#allowvariableamount').show();
              $('#allowvariableamountlbl').show();
              $('#variableamount').show();
              $('#variableamount').val('');
            }else {
              $('#allowvariableamount').hide();
              $('#allowvariableamountlbl').hide();
              $('#variableamount').hide();
            }
            break;
          }
           this.options.modeldaycash.set('allowedStep', this.options.modeldaycash.get('allowedStep')+1);
        }
        if(found===false){
          this.options.postprintclose.$el.show();
          this.options.cashtokeep.$el.hide();
          this.options.renderpaymentlines.$el.empty();
          this.options.renderpaymentlines.render();
          this.$el.text(OB.I18N.getLabel('OBPOS_LblPostPrintClose'));
          this.$el.removeAttr('disabled');
          this.options.modeldaycash.set('allowedStep', this.options.modeldaycash.get('allowedStep')-1);
          this.options.modeldaycash.defaults.step=3;
        }
      } else if (this.options.modeldaycash.defaults.step === 3) {
        this.options.modeldaycash.paymentmethods.trigger('closed');
      }
   }
  });

  OB.COMP.ButtonOk =OB.COMP.SmallButton.extend({
    _id: 'okbutton',
    icon: 'btn-icon-small btn-icon-check',
    className: 'btnlink-green btnlink-cashup-ok',
    label: '',
    clickEvent: function (e) {
      this.$el.hide();
      $('button[button*="allokbutton"]').css('visibility','hidden');
      var elem = this.me.options.modeldaycash.paymentmethods.get(this.options[this._id].rowid);
      this.options['counted_'+this.options[this._id].rowid].$el.text(OB.I18N.formatCurrency(elem.get('expected')));
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
    icon: 'btn-icon-small btn-icon-edit',
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

  OB.COMP.ButtonVoid = OB.COMP.SmallButton.extend({
    _id: 'closevoidbutton',
    label: OB.I18N.getLabel('OBUIAPP_Delete'),
    order: null,
    me: null,
    ctx: null,
    className: 'btnlink-gray',
    attributes: {'style': 'min-width: 70px; margin: 2px 5px 2px 5px;'},
    clickEvent: function (e) {
       this.me.receiptlist.remove(this.order);
       if(this.me.receiptlist.length===0){
         this.ctx.closenextbutton.$el.removeAttr('disabled');
       }
       OB.Dal.remove(this.order, function(){
         return true;
       }, function(){
         OB.UTIL.showError('Error removing');
       });
    }
  });

  // Cash To Keep Radio Button
  OB.COMP.CashToKeepRadioButton = OB.COMP.RadioButton.extend({
    _id: 'radiobutton',
    label: '',
    me: null,
    clickEvent: function (e) {
      this.options.closenextbutton.$el.removeAttr('disabled');
    }
  });

}());