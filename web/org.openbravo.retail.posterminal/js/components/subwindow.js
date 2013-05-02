/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo $ */

enyo.kind({
  name: 'OB.UI.Subwindow',
  kind: 'enyo.Popup',

  modal: false,
  autoDismiss: false,
  floating: false,

  events: {
    onChangeSubWindow: ''
  },
  classes: 'subwindow',
  showing: false,
  handlers: {
    onkeydown: 'keydownHandler'
  },
  keydownHandler: function (inSender, inEvent) {
    var keyCode = inEvent.keyCode;
    if (keyCode === 27 && this.showing) { //Handle ESC key to hide the popup
      //TODO: Improve the way the "close" action is defined
      this.$.subWindowHeader.getComponents()[0].onTapCloseButton();
      return true;
    } else if (keyCode === 13 && this.defaultActionButton) { //Handle ENTER key to execute the default action (if exists)
      this.defaultActionButton.executeTapAction();
      return true;
    } else {
      return false;
    }
  },
  showingChanged: function () {
    this.inherited(arguments);
    if (this.showing) {
      this.originalScanMode = OB.MobileApp.view.scanMode;
      OB.MobileApp.view.scanningFocus(false);
      OB.POS.terminal.openedSubwindow = this;
      this.setDefaultActionButton();
    } else {
      OB.MobileApp.view.scanningFocus(this.originalScanMode);
      OB.POS.terminal.openedSubwindow = null;
    }
  },
  focusInPopup: function () {
    var allChildsArray = OB.UTIL.getAllChildsSorted(this),
        isFirstFocusableElementObtained = false,
        tagName, element, i;
    for (i = 0; i < allChildsArray.length; i++) {
      if (allChildsArray[i].hasNode() && allChildsArray[i].hasNode().tagName) {
        tagName = allChildsArray[i].hasNode().tagName.toUpperCase();
      } else {
        tagName = '';
      }
      if ((tagName === 'INPUT' || tagName === 'SELECT' || tagName === 'TEXTAREA' || tagName === 'BUTTON') && allChildsArray[i].showing && !isFirstFocusableElementObtained) {
        element = allChildsArray[i];
        isFirstFocusableElementObtained = true;
      } else if (allChildsArray[i].isFirstFocus) {
        element = allChildsArray[i];
        break;
      }
    }
    if (element) {
      element.focus();
    }
    return true;
  },
  setDefaultActionButton: function (element) {
    var allChildsArray, tagName, i;
    if (element) {
      allChildsArray = [element];
    } else {
      allChildsArray = OB.UTIL.getAllChildsSorted(this);
    }

    for (i = 0; i < allChildsArray.length; i++) {
      if (allChildsArray[i].hasNode() && allChildsArray[i].hasNode().tagName) {
        tagName = allChildsArray[i].hasNode().tagName.toUpperCase();
      } else {
        tagName = '';
      }
      if (tagName === 'BUTTON' && allChildsArray[i].isDefaultAction) {
        element = allChildsArray[i];
        break;
      }
    }
    if (element) {
      this.defaultActionButton = element;
    }
    return true;
  },
  mainBeforeSetShowing: function (args) {
    var valueToReturn = true;
    if (args.caller) {
      this.caller = args.caller;
    }
    if (args.navigateOnClose) {
      this.navigateOnClose = args.navigateOnClose;
    } else {
      this.navigateOnClose = this.defaultNavigateOnClose;
    }
    if (this.beforeSetShowing) {
      valueToReturn = this.beforeSetShowing(args);
    }

    return valueToReturn;
  },
  mainAfterShow: function (args) {
    this.focusInPopup();
    if (this.afterShow) {
      this.afterShow(args);
    }
  },
  mainBeforeClose: function (dest) {
    var valueToReturn = true;
    if (dest) {
      this.lastLeaveTo = dest;
    }
    if (this.beforeClose) {
      valueToReturn = this.beforeClose(dest);
    }

    return valueToReturn;
  },
  header: {},
  body: {},
  goBack: function () {
    //navigate to this.caller
  },
  components: [{
    name: 'subWindowHeader'
  }, {
    name: 'subWindowBody'
  }],
  relComponentsWithSubWindow: function (comp, subWin) {
    if (!comp || !comp.getComponents) {
      return;
    }
    enyo.forEach(comp.getComponents(), function (child) {
      subWin.relComponentsWithSubWindow(child, subWin);
      child.subWindow = subWin;
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    //set header
    this.$.subWindowHeader.createComponent(this.header);
    //set body
    this.$.subWindowBody.createComponent(this.body);

    this.relComponentsWithSubWindow(this, this);
  }
});

enyo.kind({
  name: 'OB.UI.SubwindowHeader',
  classes: 'subwindowheader',
  components: [{
    name: "closebutton",
    tag: 'div',
    classes: 'subwindow-closebutton',
    components: [{
      tag: 'span',
      allowHtml: true,
      content: '&times;'
    }]
  }, {
    classes: 'subwindowheadertext',
    name: 'headermessage'
  }],
  initComponents: function () {
    this.inherited(arguments);
    if (this.i18nHeaderMessage) {
      this.$.headermessage.setContent(OB.I18N.getLabel(this.i18nHeaderMessage));
    } else if (this.headerMessage) {
      this.$.headermessage.setContent(this.headermessage);
    } else {
      this.$.headermessage.setContent(OB.I18N.getLabel('OBPOS_TitleCustomerAdvancedSearch'));
    }
    this.$.closebutton.headerContainer = this.$.closebutton.parent;
    this.$.closebutton.tap = this.onTapCloseButton;
  }
});