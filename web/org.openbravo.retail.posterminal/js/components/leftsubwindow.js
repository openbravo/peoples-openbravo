/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo*/

enyo.kind({
  name: 'OB.UI.LeftSubWindow',
  classes: 'obUiLeftSubWindow span6',
  showing: false,
  events: {
    onShowLeftSubWindow: '',
    onCloseLeftSubWindow: ''
  },
  components: [{
    classes: 'obUiLeftSubWindow-container1',
    components: [{
      name: 'leftSubWindowHeader',
      classes: 'obUiLeftSubWindow-container1-leftSubWindowHeader'
    }, {
      name: 'leftSubWindowBody',
      classes: 'obUiLeftSubWindow-container1-leftSubWindowBody'
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