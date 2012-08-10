/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global setInterval, Backbone */


enyo.kind({
  name: 'OB.UI.Scan',
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
            tap: function() {
              if (this.undoclick) {
                this.undoclick();
              }
            }
          }]
        }]
      }]
    }]
  }],

  init: function() {
    var receipt;
    this.inherited(arguments);

    receipt = this.owner.owner.owner.model.get('order');

    receipt.on('clear change:undo', function() {
      this.manageUndo(receipt);
    }, this);

    this.manageUndo(receipt);
  },

  manageUndo: function(receipt) {
    var undoaction = receipt.get('undo');

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

(function() {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Scan = Backbone.View.extend({
    tagName: 'div',
    contentView: [{
      tag: 'div',
      attributes: {
        'style': 'position:relative; background-color: #7da7d9; background-size: cover; color: white; height: 200px; margin: 5px; padding: 5px'
      },
      content: [{
        view: OB.COMP.Clock.extend({
          'className': 'pos-clock'
        })
      }, {
        tag: 'div',
        content: [{
          tag: 'div',
          id: 'msgwelcome',
          attributes: {
            'style': 'padding: 10px; display: none;'
          },
          content: [{
            tag: 'div',
            attributes: {
              'style': 'float:right;'
            },
            content: [
            OB.I18N.getLabel('OBPOS_WelcomeMessage')]
          }]
        }, {
          tag: 'div',
          id: 'msgaction',
          attributes: {
            'style': 'display: none;'
          },
          content: [{
            tag: 'div',
            id: 'txtaction',
            attributes: {
              'style': 'padding: 10px; float: left; width: 320px; line-height: 23px;'
            }
          }, {
            tag: 'div',
            attributes: {
              'style': 'float: right;'
            },
            content: [{
              view: OB.COMP.SmallButton.extend({
                'label': OB.I18N.getLabel('OBPOS_LblUndo'),
                'className': 'btnlink-white btnlink-fontblue'
              }),
              id: 'btnundo'
            }]
          }]
        }]
      }]
    }],
    initialize: function() {

      OB.UTIL.initContentView(this);
      var me = this;
      this.undoclick = null;
      this.receipt = this.options.root.modelorder;

      this.btnundo.clickEvent = function() {
        if (me.undoclick) {
          me.undoclick();
        }
      };

      this.receipt.on('clear change:undo', function() {
        this.render();
      }, this);
    },
    render: function() {
      var undoaction = this.receipt.get('undo');
      if (undoaction) {
        this.msgwelcome.hide();
        this.msgaction.show();
        this.txtaction.text(undoaction.text);
        this.undoclick = undoaction.undo;
      } else {
        this.msgaction.hide();
        this.msgwelcome.show();
        this.undoclick = null;
      }
      return this;
    }
  });

}());