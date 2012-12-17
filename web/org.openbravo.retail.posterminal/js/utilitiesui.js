/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone $ */


OB = window.OB || {};
OB.UTIL = window.OB.UTIL || {};

enyo.kind({
  name: 'OB.UI.Thumbnail',
  published: {
    img: null
  },
  tag: 'div',
  classes: 'image-wrap',
  contentType: 'image/png',
  width: '49px',
  height: '49px',
  'default': 'img/box.png',
  components: [{
    tag: 'div',
    name: 'image',
    style: 'margin: auto; height: 100%; width: 100%; background-size: contain;'
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.applyStyle('height', this.height);
    this.applyStyle('width', this.width);
    this.imgChanged();
  },

  imgChanged: function () {
    var url;
    if (this.img) {
      if (this.img === 'iconBestSellers') {
        url = 'img/iconBestSellers.png';
      } else {
        url = 'data:' + this.contentType + ';base64,' + this.img;
      }
    } else {
      url = this['default'];
    }
    this.$.image.applyStyle('background', '#ffffff url(' + url + ') center center no-repeat');
    this.$.image.applyStyle('background-size', 'contain');
  }
});

enyo.kind({
  name: 'OB.UTIL.showAlert',
  classes: 'alert alert-fade',
  components: [{
    kind: 'OB.UI.Button',
    classes: 'alert-closebutton',
    tap: function () {
      this.owner.hide();
    },
    allowHtml: true,
    content: '&times;'
  }, {
    name: 'title',
    tag: 'strong'
  }, {
    name: 'txt'
  }],
  statics: {
    display: function (txt, title, type) {
      var componentsArray = OB.POS.terminal.$.alertContainer.getComponents(),
          i;
      // To erase first previous shown alert
      for (i = 0; i < componentsArray.length; i++) {
        componentsArray[i].destroy();
      }
      OB.POS.terminal.$.alertContainer.createComponent({
        kind: 'OB.UTIL.showAlert',
        title: title,
        txt: txt,
        type: type
      }).render();
      return OB.POS.terminal.$.alertContainer.getComponents()[0];
    }
  },

  initComponents: function () {
    var me = this;
    this.inherited(arguments);
    this.$.title.setContent(this.title);
    this.$.txt.setContent(this.txt);
    if (!this.type) {
      this.type = 'alert-warning';
    }
    this.addClass(this.type);
    setTimeout(function () {
      me.addClass('alert-fade-in');
    }, 1);

    setTimeout(function () {
      me.hide();
    }, 5000);
  }
});

OB.UTIL.isSupportedBrowser = function () {
  if (navigator.userAgent.toLowerCase().indexOf('webkit') !== -1 && window.openDatabase) { //If the browser is not supported, show message and finish.
    return true;
  } else {
    return false;
  }
};

OB.UTIL.showLoading = function (value) {
  if (value) {
    OB.POS.terminal.$.containerWindow.hide();
    OB.POS.terminal.$.containerLoading.show();
  } else {
    OB.POS.terminal.$.containerLoading.hide();
    OB.POS.terminal.$.containerWindow.show();
  }
};

OB.UTIL.showLoggingOut = function (value) {
  if (value) {
    OB.POS.terminal.$.containerWindow.hide();
    OB.POS.terminal.$.containerLoggingOut.show();
  } else {
    OB.POS.terminal.$.containerLoggingOut.hide();
    OB.POS.terminal.$.containerWindow.show();
  }
};

OB.UTIL.showLoggingOut = function (value) {
  if (value) {
    OB.POS.terminal.$.containerWindow.hide();
    OB.POS.terminal.$.containerLoggingOut.show();
  } else {
    OB.POS.terminal.$.containerLoggingOut.hide();
    OB.POS.terminal.$.containerWindow.show();
  }
};

OB.UTIL.showSuccess = function (s) {
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblSuccess'), 'alert-success');
};

OB.UTIL.showWarning = function (s) {
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblWarning'), 'alert-warning');
};

OB.UTIL.showStatus = function (s) {
  return OB.UTIL.showAlert.display(s, 'Wait', '');
};

OB.UTIL.showError = function (s) {
  OB.UTIL.showLoading(false);
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblError'), 'alert-error');
};

// This funtion returns an array of Enyo components (the object and its children) sorted
// in the same way it they are rendered in the html
OB.UTIL.getAllChildsSorted = function (component, result) {
  var i;
  if (Object.prototype.toString.call(component) !== '[object Array]') {
    component = [component];
  }
  if (!result) {
    result = [];
  }
  for (i = 0; i < component.length; i++) {
    result.push(component[i]);
    if (typeof component[i].children !== 'undefined' && component[i].children.length !== 0) {
      result = OB.UTIL.getAllChildsSorted(component[i].children, result);
    }
  }
  return result;
};