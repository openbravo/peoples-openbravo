/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, setInterval */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.CloseInfo = function (context) {
    var me = this;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'position: relative; background: #363636; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: OB.COMP.Clock, attr: {'className': 'pos-clock'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'padding: 5px 10px 10px 10px; line-height: 23px;'}, content: [
              {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'float: right; padding: 0px;'}, content: [
                {kind: OB.COMP.SmallButton.extend({attributes: {'href': '#modalCancel', 'data-toggle': 'modal'}, className: 'btnlink-white btnlink-fontgrey'}), attr: {'label': OB.I18N.getLabel('OBPOS_LblCancel')}}
              ]},
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblCashUpProcess')]} ,
              {kind: B.KindJQuery('div'), attr: {'style': 'padding: 5px;'}, content: [{kind: OB.COMP.ButtonPrev},{kind: OB.COMP.ButtonNext}]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep1')]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep2')]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep3')]} ,
              {kind: B.KindJQuery('div'), content: [OB.I18N.getLabel('OBPOS_LblStep4')]}
            ]}
          ]}
        ]}
      ]}
    , context);
    this.$el = this.component.$el;
    context.closeprevbutton.$el.attr('disabled','disabled');
    context.countcash.$el.hide();
    context.cashtokeep.$el.hide();
    context.postprintclose.$el.hide();
  };

}());