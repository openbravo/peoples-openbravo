/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.Scan',
  published: {
    receipt: null
  },
  components: [{
    style: 'position:relative; background-color: #7da7d9; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px',
    components: [{
      kind: 'OB.UI.Clock',
      classes: 'pos-clock'
    }, {
      components: [{
        name: 'msgwelcome',
        showing: false,
        style: 'padding: 10px;',
        components: [{
          style: 'float:right;',
          content: OB.I18N.getLabel('OBPOS_WelcomeMessage')
        }]
      }, {
        name: 'msgaction',
        showing: false,
        components: [{
          name: 'txtaction',
          style: 'padding: 10px; float: left; width: 320px; line-height: 23px;'
        }, {
          style: 'float: right;',
          components: [{
            name: 'undobutton',
            kind: 'OB.UI.SmallButton',
            content: OB.I18N.getLabel('OBPOS_LblUndo'),
            classes: 'btnlink-white btnlink-fontblue',
            tap: function () {
              if (this.undoclick) {
                this.undoclick();
              }
            }
          }]
        }]
      }]
    }]
  }],

  receiptChanged: function () {
    this.receipt.on('clear change:undo', function () {
      this.manageUndo();
    }, this);

    this.manageUndo();
  },

  manageUndo: function () {
    var undoaction = this.receipt.get('undo');

    if (undoaction) {
      this.$.msgwelcome.hide();
      this.$.msgaction.show();
      this.$.txtaction.setContent(undoaction.text);
      this.$.undobutton.undoclick = undoaction.undo;
    } else {
      this.$.msgaction.hide();
      this.$.msgwelcome.show();
      delete this.$.undobutton.undoclick;
    }
  }
});