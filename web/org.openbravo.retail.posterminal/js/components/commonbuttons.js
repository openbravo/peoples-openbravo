/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global window, B, $, Backbone, _, MBP */
enyo.kind({
  name: 'OB.UI.Button',
  kind: 'enyo.Button',
  handlers: {
    onmouseover: 'mouseOverOut',
    onmouseout: 'mouseOverOut'
  },
  //TODO: support windows 7  setTimeout(function() { me.$el.removeClass('btn-down'); }, 125);
  mouseOverOut: function(sender, event) {

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
  name: 'OB.UI.Modal',
  tag: 'div',
  classes: 'modal hide fade',
  style: 'display: none;',
  components: [{
    tag: 'div',
    classes: 'modal-header',
    components: [{
      tag: 'a',
      classes: 'close',
      attributes: {
        'data-dismiss': 'modal'
      },
      components: [{
        tag: 'span',
        style: 'font-size: 150%',
        content: 'x' //TODO: '&times;'
      }]
    }, {
      tag: 'h3',
      name: 'divheader'
    }],
  }, {
    tag: 'div',
    name: 'body',
    classes: 'modal-header'
  }],
  //TODO: maxheight: null,
  initComponents: function() {
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

  render: function() {
    this.inherited(arguments);
    OB.UTIL.adjustModalPosition($(this.node));
    OB.UTIL.focusInModal($(this.node));
  },

  makeId: function() {
    return this.myId || this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OB.UI.RenderEmpty',
  style: 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc',
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBPOS_SearchNoResults'));
  }
});


enyo.kind({
  name: 'OB.UI.SelectButton',
  kind: 'OB.UI.Button',
  classes: 'btnselect',

  tap: function() {
    this.model.trigger('selected', this.model);
    this.model.trigger('click', this.model);
    $('#' + this.id).parents('.modal').filter(':first').modal('hide')
  }
});

enyo.kind({
  name: 'OB.UI.CancelButton',
  kind: 'OB.UI.SmallButton',
  classes: 'btnlink-white btnlink-fontgray',
  attributes: {
    href: '#modalCancel',
    'data-toggle': 'modal'
  },
  initComponents: function() {
    this.inherited(arguments);
    this.setContent(this.label || OB.I18N.getLabel('OBPOS_LblCancel'));
  }
});

enyo.kind({
  name: 'OB.UI.RadioButton',
  tag: 'button',
  classes: 'btn btn-radio',
  style: 'padding: 0px 0px 0px 40px; margin: 10px;',
  initComponents: function() {
    this.inherited(arguments);
  }
});



// Toolbar Button
enyo.kind({
  name: 'OB.UI.ToolbarButton',
  kind: 'OB.UI.RegularButton',
  classes: 'btnlink-toolbar',
  initComponents: function() {
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
  tap: function() {
    this.checked = !this.checked;
    this.addRemoveClass('active', this.checked);
  }

});


// Order list
enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.ButtonTab',
  attributes: {
    'data-toggle': 'tab'
  },
  tabPanel: '#',
  initComponents: function() {
    this.inherited(arguments);
    this.addClass('btnlink btnlink-gray');
    this.setAttribute('href', this.tabPanel);
    if (this.label) {
      this.createComponent({
        name: 'lbl',
        tag: 'span',
        content: this.label
      })
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
  attributes: {
    'data-toggle': 'tab'
  },
  events: {
    onTabChange: ''
  },
  initComponents: function() {
    this.inherited(arguments);
    this.addClass('btnlink-toolbar');
  },
});




// Menu Button
// Toolbar Button
enyo.kind({
  name: 'OB.UI.ToolbarMenuButton',
  kind: 'OB.UI.ToolbarButton',
  attributes: {
    'data-toggle': 'dropdown'
  }
});


enyo.kind({
  name: 'OB.UI.ToolbarMenu',
  classes: 'dropdown',
  style: 'display: inline-block; width: 100%;',
  components: [{
    kind: 'OB.UI.ToolbarMenuButton',
    components: [{
      name: 'leftIcon'
    }, {
      tag: 'span'
    }, {
      name: 'rightIcon'
    }],
  }, {
    tag: 'ul',
    classes: 'dropdown-menu',
    name: 'menu'
  }],
  initComponents: function() {
    this.inherited(arguments);
    if (this.icon) {
      this.$.leftIcon.addClass(this.icon);
    }
    if (this.iconright) {
      this.$.rightIcon.addClass(this.iconright);
    }

    enyo.forEach(this.menuEntries, function(entry) {
      this.$.menu.createComponent(entry);
    }, this)
  }
});

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.StandardMenu',
  kind: 'OB.UI.ToolbarMenu',
  icon: 'btn-icon btn-icon-menu',
  initComponents: function() {
    // dynamically generating the menu
    this.menuEntries = [];
    this.menuEntries.push({
      kind: 'OB.UI.MenuReturn'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuInvoice'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuItem',
      label: OB.I18N.getLabel('OBPOS_LblOpenbravoWorkspace'),
      url: '../..'
    });

    enyo.forEach(OB.POS.windows.filter(function(window) {
      // show in menu only the ones with menuPosition
      return window.get('menuPosition');
    }), function(window) {
      this.menuEntries.push({
        kind: 'OB.UI.MenuItem',
        label: window.get('menuLabel'),
        route: window.get('route')
      });
    }, this);
    this.inherited(arguments);
  }
});





enyo.kind({
  name: 'OB.UI.MenuSeparator',
  tag: 'li',
  classes: 'divider'
});



enyo.kind({
  name: 'OB.UI.MenuItem',
  tag: 'li',
  components: [{
    tag: 'a',
    name: 'item',
    style: 'padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;',
    attributes: {
      href: '#'
    }
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.$.item.setContent(this.label);
    if (!OB.POS.modelterminal.hasPermission(this.permission)) {
      this.$.item.setStyle('color: #cccccc; padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;');
    }
  },
  tap: function() {
    // TODO: check online for required windows
    if (!OB.POS.modelterminal.hasPermission(this.permission)) {
      return;
    }
    if (this.route) {
      OB.POS.navigate(this.route);
    }
    if (this.url) {
      window.open(this.url, '_blank');
    }
  }
});




enyo.kind({
  tag: 'li',
  name: 'OB.UI.MenuAction',
  components: [{
    name: 'lbl',
    tag: 'a',
    style: 'padding: 12px 15px 12px 15px;',
    attributes: {
      href: '#'
    },
  }],
  initComponents: function() {
    this.inherited(arguments);
    this.$.lbl.setContent(this.label);
  }
});





enyo.kind({
  //TODO: maxheight, 
  // x -> &times;
  name: 'OB.UI.ModalAction',
  tag: 'div',
  classes: 'modal hide fade modal-dialog',
  style: 'display:none',
  bodyContentClass: 'modal-dialog-content-text',
  bodyButtonsClass: 'modal-dialog-content-buttons-container',
  components: [{
    tag: 'div',
    classes: 'modal-header modal-dialog-header',
    components: [{
      tag: 'a',
      classes: 'close',
      attributes: {
        'data-dismiss': 'modal'
      },
      components: [{
        tag: 'span',
        style: 'font-size: 150%',
        content: 'x'
      }]
    }, {
      name: 'header',
      tag: 'h3',
      classes: 'modal-dialog-header-text',
    }, {
      tag: 'div',
      classes: 'modal-body modal-dialog-body',
      components: [{
        tag: 'div',
        name: 'bodyContent'
      }, {
        tag: 'div',
        name: 'bodyButtons'
      }]
    }]
  }],

  initComponents: function() {
    console.log('initComponents modalAction')
    this.inherited(arguments);
    this.$.header.setContent(this.header);

    this.$.bodyContent.setClasses(this.bodyContentClass);
    this.$.bodyContent.createComponent(this.bodyContent);

    this.$.bodyButtons.setClasses(this.bodyButtonsClass);
    this.$.bodyButtons.createComponent(this.bodyButtons);
  },

  render: function() {
    this.inherited(arguments);
    OB.UTIL.adjustModalPosition($(this.node));
    OB.UTIL.focusInModal($(this.node));
  },
  makeId: function() {
    return this.myId || this.inherited(arguments);
  }
});


enyo.kind({
  name: 'OB.UI.SearchInput',
  kind: 'enyo.Input',
});