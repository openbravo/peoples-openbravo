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

  OB.COMP.Scan = function (context) {
    var me = this;

    var undoclick;

    this.component = B(
      {kind: B.KindJQuery('div'), content: [
        {kind: B.KindJQuery('div'), attr: {'style': 'position:relative; background-color: #7da7d9; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px'}, content: [
          {kind: OB.COMP.Clock, attr: {'className': 'pos-clock'}},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('div'), id: 'msgwelcome', attr: {'style': 'padding: 10px; display: none;'}, content: [
              {kind: B.KindJQuery('div'), attr: {'style': 'float:right;'}, content: [
                OB.I18N.getLabel('OBPOS_WelcomeMessage')
              ]}
            ]},
            {kind: B.KindJQuery('div'), id: 'msgaction', attr: {'style': 'display: none;'}, content: [
              {kind: B.KindJQuery('div'), id: 'txtaction', attr: {'style': 'padding: 10px; float: left; width: 320px; line-height: 23px;'}},
              {kind: B.KindJQuery('div'), attr: {'style': 'float: right;'}, content: [
                {kind: OB.COMP.SmallButton, attr: { 'label': OB.I18N.getLabel('OBPOS_LblUndo'), 'className': 'btnlink-white btnlink-fontblue',
                  'clickEvent': function() {
                    if (undoclick) {
                      undoclick();
                    }
                  }
                }}
              ]}
            ]}
          ]}
        ]}
      ]}
    );
    this.$el = this.component.$el;
    var msgwelcome = this.component.context.msgwelcome.$el;
    var txtaction = this.component.context.txtaction.$el;
    var msgaction = this.component.context.msgaction.$el;

    this.receipt = context.modelorder;

    this.receipt.on('clear change:undo', function() {
      var undoaction = this.receipt.get('undo');
      if (undoaction) {
        msgwelcome.hide();
        msgaction.show();
        txtaction.text(undoaction.text);
        undoclick = undoaction.undo;
      } else {
        msgaction.hide();
        msgwelcome.show();
      }
    }, this);

  };

}());