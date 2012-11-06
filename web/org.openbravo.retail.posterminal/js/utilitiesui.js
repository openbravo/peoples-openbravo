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
    var url = (this.img) ? 'data:' + this.contentType + ';base64,' + this.img : this['default'];
    this.$.image.applyStyle('background', '#ffffff url(' + url + ') center center no-repeat');
    this.$.image.applyStyle('background-size', 'contain');
  }
});

enyo.kind({
  name: 'OB.UTIL.showAlert',
  style: 'position:absolute; right:35px; top: 5px',
  components: [{
    tag: 'button',
    classes: 'close',
    attributes: {
      'data-dismiss': 'alert'
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
      var alert = new(enyo.kind({
        kind: 'OB.UTIL.showAlert',
        title: title,
        txt: txt,
        type: type
      }))();
      alert.renderInto(enyo.dom.byId('alertContainer'));
      return alert;
    }
  },

  initComponents: function () {
    this.inherited(arguments);
    this.$.title.setContent(this.title);
    this.$.txt.setContent(this.txt);
    this.addClass('alert fade in ' + this.type);

    setTimeout(function () {
      $('.alert').alert('close');
    }, 5000);
  }
});

OB.UTIL.isSupportedBrowser = function () {
  if ($.browser.webkit && window.openDatabase) { //If the browser is not supported, show message and finish.
    return true;
  } else {
    return false;
  }
};

OB.UTIL.showLoading = function (value) {
  if (value) {
    $('#containerLoading').css('display', '');
    $('#containerWindow').css('display', 'none');
  } else {
    $('#containerLoading').css('display', 'none');
    $('#containerWindow').css('display', '');
  }
};

OB.UTIL.showSuccess = function (s) {
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblSuccess'), 'alert-success');
};

OB.UTIL.showWarning = function (s) {
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblWarning'), '');
};

OB.UTIL.showStatus = function (s) {
  return OB.UTIL.showAlert.display(s, 'Wait', '');
};

OB.UTIL.showError = function (s) {
  OB.UTIL.showLoading(false);
  OB.UTIL.showAlert.display(s, OB.I18N.getLabel('OBPOS_LblError'), 'alert-error');
};

/* This will automatically set the focus in the first focusable item in the modal popup */
OB.UTIL.focusInModal = function (modalObj) {
  modalObj.on('shown', function (e) {
    var firstFocusableItem = $(this).find('input,select,button').filter(':visible:enabled:first');
    if (firstFocusableItem) {
      firstFocusableItem.focus();
    }
    return true;
  });
};