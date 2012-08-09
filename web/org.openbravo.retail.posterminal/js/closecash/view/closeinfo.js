/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, setInterval */

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ButtonPrev',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-fontgray',
  style: 'min-width: 115px;',
  disabled: true,
  events: {
    onPrev:''
  },
  content: OB.I18N.getLabel('OBPOS_LblPrevStep'),
  tap: function() {
    this.doPrev();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.ButtonNext',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-fontgray',
  style: 'min-width: 115px;',
  events: {
    onNext:''
  },
  content: OB.I18N.getLabel('OBPOS_LblNextStep'),
  tap: function() {
    this.doNext();
  }
});

enyo.kind({
  name: 'OB.OBPOSCashUp.UI.CashUpInfo',
  components: [{
    style: 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{ //clock here
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    },
    {
      style: 'padding: 5px; float: right;',
      components: [{
        kind: 'OB.UI.CancelButton'
      }]
    },
    {
      // process info
      style: 'padding: 5px',
      content: OB.I18N.getLabel('OBPOS_LblCashUpProcess')
    },
    {
      // Prev and Next buttons
      style: 'padding: 5px;',
      components: [{
        kind: 'OB.OBPOSCashUp.UI.ButtonPrev'
      },
      {
        kind: 'OB.OBPOSCashUp.UI.ButtonNext'
      }]
    },  {
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep1')
    },{
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep2')
    },{
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep3')
    },{
      style: 'padding: 3px',
      content: OB.I18N.getLabel('OBPOS_LblStep4')
    }]
  }]
});

//(function () {
//
//  OB = window.OB || {};
//  OB.COMP = window.OB.COMP || {};
//
//  OB.COMP.CloseInfo = function (context) {
//    var me = this;
//
//    this.component = B(
//      {kind: B.KindJQuery('div'), content: [
//        {kind: B.KindJQuery('div'), attr: {'style': 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
//          {kind: OB.COMP.Clock, attr: {'className': 'pos-clock'}},
//          {kind: B.KindJQuery('div'), content: [
//            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 5px 10px 10px 10px; line-height: 23px;'}, content: [
//              {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'float: right; padding: 0px;'}, content: [
//                {kind: OB.COMP.SmallButton.extend({attributes: {'href': '#modalCancel', 'data-toggle': 'modal'}, className: 'btnlink-white btnlink-fontgrey'}), attr: {'label': OB.I18N.getLabel('OBPOS_LblCancel')}}
//              ]},
//              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblCashUpProcess')]} ,
//              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px;'}, content: [{kind: OB.COMP.ButtonPrev},{kind: OB.COMP.ButtonNext}]} ,
//              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep1')]} ,
//              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep2')]} ,
//              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep3')]} ,
//              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep4')]}
//            ]}
//          ]}
//        ]}
//      ]}
//    , context);
//    this.$el = this.component.$el;
//    context.closeprevbutton.$el.attr('disabled','disabled');
//    context.countcash.$el.hide();
//    context.cashtokeep.$el.hide();
//    context.postprintclose.$el.hide();
//  };
//
//}());