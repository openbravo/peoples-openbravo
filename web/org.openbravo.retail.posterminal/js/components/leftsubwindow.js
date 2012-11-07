/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone */


enyo.kind({
  name: 'OB.UI.LeftSubWindow',
  classes: 'span6',
  showing: false,
  events: {
    onShowLeftSubWindow: '',
    onCloseLeftSubWindow: ''
  },
  components: [{
    style: 'margin: 5px; height: 612px; background-color: white;',
    components: [{
      name: 'leftSubWindowHeader'
    }, {
      name: 'leftSubWindowBody'
    }]
  }],
  mainBeforeSetShowing: function (params) {
    //TODO
    if (this.beforeSetShowing) {
      return this.beforeSetShowing(params);
    }
    return true;
  },
  mainBeforeSetHidden: function (params) {
    //TODO
    if (this.beforeSetHidden) {
      return this.beforeSetHidden(params);
    }
    return true;
  },
  relComponentsWithLeftSubWindow: function (comp, leftSubWin) {
    if (!comp || !comp.getComponents) {
      return;
    }
    enyo.forEach(comp.getComponents(), function (child) {
      leftSubWin.relComponentsWithLeftSubWindow(child, leftSubWin);
      child.leftSubWindow = leftSubWin;
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    if (this.header) {
      this.$.leftSubWindowHeader.createComponent(this.header);
      this.headerComponent = this.$.leftSubWindowHeader.getComponents()[0];
    }
    if (this.body) {
      this.$.leftSubWindowBody.createComponent(this.body);
      this.bodyComponent = this.$.leftSubWindowBody.getComponents()[0];
    }

    this.relComponentsWithLeftSubWindow(this, this);
  }
});