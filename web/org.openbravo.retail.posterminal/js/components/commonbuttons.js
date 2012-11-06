/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, $ */
enyo.kind({
  name: 'OB.UI.Button',
  kind: 'enyo.Button',
  handlers: {
    onmouseover: 'mouseOverOut',
    onmouseout: 'mouseOverOut'
  },
  //TODO: support windows 7  setTimeout(function() { me.$el.removeClass('btn-down'); }, 125);
  mouseOverOut: function (sender, event) {
    this.addRemoveClass('btn-over', event.type === 'mouseover');
  }
});

enyo.kind({
  name: 'OB.UI.RegularButton',
  kind: 'OB.UI.Button',
  icon: '',
  iconright: '',
  label: '',
  classes: 'btnlink'
});

enyo.kind({
  name: 'OB.UI.SmallButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-small'
});

enyo.kind({
  name: 'OB.UI.ModalDialogButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-gray modal-dialog-content-button'
});

enyo.kind({
  name: 'OB.UI.Popup',
  kind: "onyx.Popup",
  centered: true,
  floating: true,
  scrim: true,
  handlers: {
    onHideThisPopup: 'hide'
  },
  show: function () {
    this.inherited(arguments);
    if (this.executeOnShow) {
      this.executeOnShow();
    }
  },
  hide: function () {
    this.inherited(arguments);
    if (this.executeOnHide) {
      this.executeOnHide();
    }
  },
  updatePosition: function () {
    // Improve of enyo "updatePosition" function to proper manage of % and absolute top and left positions
    var top, left, t = this.getBounds();;
    if (this.topPosition) {
      top = this.topPosition.toString();
    } else if (this.centered) {
      top = '50%';
    } else {
      top = '0';
    }
    if (top.indexOf('px') !== -1) {
      top = top.replace('px', '');
    }

    if (this.leftPosition) {
      left = this.leftPosition.toString();
    } else if (this.centered) {
      left = '50%';
    } else {
      left = '0';
    }
    if (left.indexOf('px') !== -1) {
      left = left.replace('px', '');
    }

    if (top.indexOf('%') !== -1) {
      this.addStyles('top: ' + top);
      this.addStyles('margin-top: -' + Math.max(t.height / 2, 0).toString() + 'px;');
    } else {
      this.addStyles('top: ' + top + 'px');
    }

    if (left.indexOf('%') !== -1) {
      this.addStyles('left: ' + left);
      this.addStyles('margin-left: -' + Math.max(t.width / 2, 0).toString() + 'px;');
    } else {
      this.addStyles('left: ' + left + 'px');
    }

    return true;
  }
});

enyo.kind({
  name: 'OB.UI.Modal',
  kind: "OB.UI.Popup",
  classes: 'modal',
  components: [{
    tag: 'div',
    classes: 'modal-header',
    components: [{
      tag: 'div',
      classes: 'modal-closebutton',
      components: [{
        tag: 'span',
        ontap: 'hide',
        style: 'font-size: 150%',
        allowHtml: true,
        content: '&times;'
      }]
    }, {
      style: 'line-height: 27px; font-size: 18px; font-weight: bold;',
      name: 'divheader'
    }]
  }, {
    tag: 'div',
    name: 'body',
    classes: 'modal-body'
  }],
  //TODO: maxheight: null,
  initComponents: function () {
    this.inherited(arguments);
    if (this.modalClass) {
      this.addClass(this.modalClass);
    }

    this.$.divheader.setContent(this.header);

    if (this.bodyClass) {
      this.$.body.addClass(this.bodyClass);
    }
    this.$.body.createComponent(this.body);
  },

  render: function () {
    this.inherited(arguments);
    OB.UTIL.focusInModal($(this.node));
  }
});

enyo.kind({
  name: 'OB.UI.RenderEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBPOS_SearchNoResults'));
  }
});


enyo.kind({
  name: 'OB.UI.SelectButton',
  kind: 'OB.UI.Button',
  classes: 'btnselect',
  tap: function () {
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
  }
});

enyo.kind({
  name: 'OB.UI.CancelButton',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-white btnlink-fontgray',
  events: {
    onShowPopup: ''
  },
  tap: function () {
    this.doShowPopup({
      popup: 'modalCancel'
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBPOS_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.RadioButton',
  tag: 'button',
  classes: 'btn btn-radio',
  style: 'padding: 0px 0px 0px 40px; margin: 10px;',
  initComponents: function () {
    this.inherited(arguments);
  }
});



// Toolbar Button
enyo.kind({
  name: 'OB.UI.ToolbarButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-toolbar',
  initComponents: function () {
    this.inherited(arguments);
    if (this.icon) {
      this.addClass(this.icon);
    }
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButton',
  tag: 'button',
  classes: 'btn-check',
  checked: false,
  tap: function () {
    this.checked = !this.checked;
    this.addRemoveClass('active', this.checked);
  }

});

// Order list
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.ButtonTab',
  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnlink btnlink-gray');
    if (this.label) {
      this.createComponent({
        name: 'lbl',
        tag: 'span',
        content: this.label
      });
    }
    //TODO
    //this.receipt.on('change:gross', function() {
    //  this.render();
    //}, this)
  }
});



// Order list
enyo.kind({
  name: 'OB.UI.ToolbarButtonTab',
  kind: 'OB.UI.ButtonTab',
  events: {
    onTabChange: ''
  },
  initComponents: function () {
    this.inherited(arguments);
    this.addClass('btnlink-toolbar');
  }
});


enyo.kind({
  name: 'OB.UI.TabPane',
  classes: 'postab-pane'
});


enyo.kind({
  //TODO: maxheight, 
  name: 'OB.UI.ModalAction',
  kind: "OB.UI.Popup",
  classes: 'modal modal-dialog',
  bodyContentClass: 'modal-dialog-content-text',
  bodyButtonsClass: 'modal-dialog-content-buttons-container',
  components: [{
    tag: 'div',
    classes: 'modal-header modal-dialog-header',
    components: [{
      name: 'headerCloseButton',
      tag: 'div',
      classes: 'modal-closebutton',
      components: [{
        tag: 'span',
        ontap: 'hide',
        style: 'font-size: 150%',
        allowHtml: true,
        content: '&times'
      }]
    }, {
      name: 'header',
      classes: 'modal-dialog-header-text'
    }, {
      tag: 'div',
      classes: 'modal-body modal-dialog-body',
      name: 'bodyParent',
      components: [{
        tag: 'div',
        name: 'bodyContent'
      }, {
        tag: 'div',
        name: 'bodyButtons'
      }]
    }]
  }],

  initComponents: function () {
    this.inherited(arguments);
    this.$.header.setContent(this.header);

    this.$.bodyParent.setStyle('max-height: ' + this.maxheight + ';');

    this.$.bodyContent.setClasses(this.bodyContentClass);
    this.$.bodyContent.createComponent(this.bodyContent);

    this.$.bodyButtons.setClasses(this.bodyButtonsClass);
    this.$.bodyButtons.createComponent(this.bodyButtons);
  }
});

enyo.kind({
  name: 'OB.UI.SearchInput',
  kind: 'enyo.Input'
});

enyo.kind({
  tag: 'li',
  name: 'OB.UI.MenuAction',
  permission: null,
  components: [{
    name: 'lbl',
    tag: 'a',
    allowHtml: true,
    style: 'padding: 12px 15px 12px 15px;',
    attributes: {
      href: '#'
    }
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.lbl.setContent(this.label);
    if (this.permission && !OB.POS.modelterminal.hasPermission(this.permission)) {
      this.$.lbl.setStyle('color: #cccccc; padding: 12px 15px 12px 15px;');
    }
  }
});


enyo.kind({
  name: 'OB.UI.ModalInfo',
  kind: 'OB.UI.ModalAction',
  bodyButtons: {
    components: [{
      kind: 'OB.UI.AcceptDialogButton'
    }]
  },
  events: {
    onShowPopup: ''
  },
  closeOnAcceptButton: true,
  initComponents: function () {
    this.inherited(arguments);
    this.$.bodyButtons.$.acceptDialogButton.dialogContainer = this;

  }
});

enyo.kind({
  name: 'OB.UI.AcceptDialogButton',
  kind: 'OB.UI.ModalDialogButton',
  content: OB.I18N.getLabel('OBPOS_LblOk'),
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    if (this.dialogContainer.acceptCallback) {
      this.dialogContainer.acceptCallback();
    }
    if (this.dialogContainer.closeOnAcceptButton) {
      this.doHideThisPopup();
    }
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.CancelDialogButton',
  classes: 'btnlink btnlink-gray modal-dialog-content-button',
  content: OB.I18N.getLabel('OBPOS_LblCancel'),
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.doHideThisPopup();
  }
});