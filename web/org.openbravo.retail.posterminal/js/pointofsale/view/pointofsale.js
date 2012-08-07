/*global OB, enyo */

/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
  tag: 'li',
  classes: 'span3',
  components: [{
    name: 'theButton',
    attributes: {
      style: 'margin: 0px 5px 0px 5px;'
    }
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.$.theButton.createComponent(this.button);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  classes: 'span4',
  components: [{
    tag: 'ul',
    classes: 'unstyled nav-pos row-fluid',
    name: 'toolbar'
  }],
  initComponents: function() {
    this.inherited(arguments);
    enyo.forEach(this.buttons, function(btn) {
      this.$.toolbar.createComponent({
        kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarButton',
        button: btn
      });
    }, this);
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl',
  kind: 'OB.OBPOSPointOfSale.UI.LeftToolbar',
  buttons: [{
    kind: 'OB.UI.ButtonNew'
  }, {
    kind: 'OB.UI.ButtonDelete'
  }, {
    kind: 'OB.UI.ButtonPrint'
  }, {
    kind: 'OB.UI.ToolbarMenu'
  }]
});

// Point of sale main window view
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.PointOfSale',
  kind: 'OB.UI.WindowView',
  windowmodel: OB.OBPOSPointOfSale.Model.PointOfSale,
  tag: 'section',
  components: [{
    classes: 'row',
    attributes: {
      style: 'margin-bottom: 5px;'
    },
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.LeftToolbarImpl'

    }]
  }
  //  , 
  //  {
  //    classes: 'span8',
  //    components: [{
  //      tag: 'ul',
  //      classes: 'unstyled nav-pos row-fluid',
  //      components: [{
  //        tag: 'li',
  //        classes: 'span3',
  //        components: [{
  //          attributes: {
  //            'data-toggle': 'tab',
  //            style: 'margin: 0px 5px 0px 5px;'
  //          },
  //          components: [{
  //            kind: 'OB.UI.ButtonTabPayment'
  //          }]
  //        }]
  //      }, 
  //      {
  //        tag: 'li',
  //        classes: 'span2',
  //        components: [{
  //          attributes: {
  //            'data-toggle': 'tab',
  //            style: 'margin: 0px 5px 0px 5px;'
  //          },
  //          components: [{
  //            kind: 'OB.UI.ButtonTabBrowse'
  //          }]
  //        }]
  //      }, {
  //        tag: 'li',
  //        classes: 'span2',
  //        components: [{
  //          attributes: {
  //            'data-toggle': 'tab',
  //            style: 'margin: 0px 5px 0px 5px;'
  //          },
  //          components: [{
  //            kind: 'OB.UI.ButtonTabSearch'
  //          }]
  //        }]
  //      }, {
  //        tag: 'li',
  //        classes: 'span2',
  //        components: [{
  //          attributes: {
  //            'data-toggle': 'tab',
  //            style: 'margin: 0px 5px 0px 5px;'
  //          },
  //          components: [{
  //            kind: 'OB.UI.ButtonTabEditLine'
  //          }]
  //        }]
  //      }]
  //    }]
  //  }
  ]
});
OB.POS.registerWindow('retail.pointofsale', OB.OBPOSPointOfSale.UI.PointOfSale, 10);