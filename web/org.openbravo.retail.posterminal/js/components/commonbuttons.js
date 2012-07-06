/*global window, B, $, Backbone, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Base button
  OB.COMP.Button = Backbone.View.extend({
    tagName: 'button',
    attr: function (attributes) {
      if (attributes.label) {
        this.label = attributes.label;
      }
      if (attributes.style) {
        this.$el.attr('style', attributes.style);
      }
      if (attributes.clickEvent) {
        this.clickEvent = attributes.clickEvent;
      }
      if (attributes.icon) {
        this.icon = attributes.icon;
      }
      if (attributes.iconright) {
        this.iconright = attributes.iconright;
      }
      if (attributes.href) {
        this.href = attributes.href;
      }
      if (attributes.dataToggle) {
        this.dataToggle = attributes.dataToggle;
      }
      if (attributes.className) {
        this.$el.addClass(attributes.className);
      }
    },
    initialize: function () {
      this.$el.click(_.bind(this._clickEvent, this));
      // new googleuiFastButton(this.el, this._clickEvent);
    },
    _clickEvent: function (e) {
      this.clickEvent(e);
    },
    clickEvent: function (e) {
    }
  });

  // Regular Button
  OB.COMP.RegularButton = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink');
      if (this.href) {
        this.$el.attr('href', this.href);
      }
      if (this.dataToggle) {
        this.$el.attr('data-toggle', this.dataToggle);
      }
      if (this.icon) {
        this.$el.append($('<div class=\"' + this.icon + '\"></div>'));
      }
      this.$el.append($('<span>' + this.label + '</span>'));
      if (this.iconright) {
        this.$el.append($('<div class=\"' + this.iconright + '\"></div>'));
      }
      return this;
    },
    icon: '',
    iconright: '',
    label: ''
  });

  // Regular Button
  OB.COMP.SmallButton = OB.COMP.RegularButton.extend({
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-small');
      return this;
    },
    icon: '',
    iconright: '',
    label: ''
  });

  // Modal Dialog Button
  OB.COMP.ModalDialogButton = OB.COMP.RegularButton.extend({
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-gray modal-dialog-content-button');
      return this;
    }
  });

  // Toolbar Button
  OB.COMP.ToolbarButton = OB.COMP.RegularButton.extend({
    render: function () {
      OB.COMP.RegularButton.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-toolbar');
      return this;
    }
  });

  // Checkbox Button
  OB.COMP.CheckboxButton = Backbone.View.extend({
    tagName: 'button',
    attributes: {'class': 'btn-check'},
    initialize: function () {
      this.$el.click(_.bind(this._clickEvent, this));
    },
    attr: function (attributes) {
      if (attributes.className) {
        this.$el.addClass(attributes.className);
      }
      if (attributes.id) {
        this.$el.attr('id', attributes.id);
      }
    },
    _clickEvent: function (e) {
      this.$el.toggleClass('active');
      this.clickEvent(e);
    },
    clickEvent: function (e) {
    }
  });

  // Radio Button
  OB.COMP.RadioButton = Backbone.View.extend({
    tagName: 'button',
    className: 'btn',
    initialize: function () {
      this.$el.click(_.bind(this._clickEvent, this));
    },
    append: function (child) {
      if (child.render) {
        this.$el.append(child.render().$el); // it is a backbone view.
      } else if (child.$el) {
        this.$el.append(child.$el);
      }
    },
    attr: function (attributes) {
      if (attributes.className) {
        this.$el.addClass(attributes.className);
      }
      if (attributes.id) {
        this.$el.attr('id', attributes.id);
      }
    },
    render: function () {
      this.$el.addClass('btn-radio');
      this.$el.attr('style', 'padding: 0px 0px 0px 40px; margin: 10px;');
      return this;
    },
    _clickEvent: function (e) {
      this.clickEvent(e);
    },
    clickEvent: function (e) {
    }
  });

  // Generic Tab Button
  OB.COMP.ButtonTab = OB.COMP.Button.extend({
    className: 'btnlink btnlink-gray',
    attributes: {'data-toggle': 'tab'},
    initialize: function () {
      OB.COMP.Button.prototype.initialize.call(this); // super.initialize();
      this.$el.attr('href', this.tabpanel);
      this.$el.append($('<span>' + this.label + '</span>'));
    },
    tabpanel: '#',
    label: '',
    events: {
      'shown': 'shownEvent' // attach the click event as part of the element
    },
    shownEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });

  // Toolbar Tab Button
  OB.COMP.ToolbarButtonTab = OB.COMP.ButtonTab.extend({
    render: function () {
      OB.COMP.ButtonTab.prototype.render.call(this); // super.initialize();
      this.$el.addClass('btnlink-toolbar');
      return this;
    }
  });

  // Menu Button
  OB.COMP.ToolbarMenuButton = Backbone.View.extend({
    tagName: 'div',
    className: 'dropdown',
    attributes: {'style': 'display: inline-block; width: 100%;'},
    initialize: function () {
      this.button = $('<button class=\"btnlink btnlink-toolbar\" data-toggle=\"dropdown\"></button>');

      // The button
      this.$el.append(this.button);
      if (this.icon) {
        this.button.append($('<div class=\"' + this.icon + '\"></i>'));
      }
      this.button.append($('<span>' + this.label + ' </span>'));
      if (this.iconright) {
        this.button.append($('<div class=\"' + this.iconright + '\"></i>'));
      }
      //this.button.append($('<span class=\"caret\"></span>'));

      this.menu = $('<ul class=\"dropdown-menu\"></ul>');
      this.$el.append(this.menu);
    },
    append: function (child) {
      if (child.render) {
        this.menu.append(child.render().$el); // it is a backbone view.
      } else if (child.$el) {
        this.menu.append(child.$el);
      }
    },
    icon: '',
    iconright: '',
    label: ''
  });

  OB.COMP.PaymentButton = OB.COMP.Button.extend({
    className: 'btnlink btnlink-small',
    attributes: {'style': 'width: 75px; height: 35px; text-align:right; margin: 6px 8px 6px 8px; font-size: 16px;'},
    paymenttype: 'OBPOS_payment.cash',
    amount: 10,
    label: null,
    classcolor: 'btnlink-orange',
    render: function() {
      this.$el.addClass(this.classcolor);
      this.$el.text(this.label || OB.I18N.formatCurrency(this.amount));
      return this;
    },
    clickEvent: function (e) {
      this.options.modelorder.addPayment(new OB.MODEL.PaymentLine({'kind': this.paymenttype, 'name': OB.POS.modelterminal.getPaymentName(this.paymenttype), 'amount': OB.DEC.number(this.amount)}));
    }
  });

  // Clears the text of the previous field
  OB.COMP.ClearButton = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnclear');
      this.$el.html('&times;');
      return this;
    },
    clickEvent: function (e) {
      // clears the text of the previous field.
      this.$el.prev().val('');
    }
  });

  // Select Div
  OB.COMP.SelectPanel = Backbone.View.extend({
    tagName: 'div',
    className: 'btnselect',
    initialize: function () {
      this.model = this.options.model;
    }
  });

  // Select Button
  OB.COMP.SelectButton = OB.COMP.Button.extend({
    className: 'btnselect',
    initialize: function () {
      OB.COMP.Button.prototype.initialize.call(this); // super.initialize();
      this.model = this.options.model;
    },
    clickEvent: function (e) {
      this.model.trigger('selected', this.model);
      this.model.trigger('click', this.model);
      this.$el.parents('.modal').filter(':first').modal('hide'); // If in a modal dialog, close it
    }
  });
  
  OB.COMP.RenderEmpty = Backbone.View.extend({
    tagName: 'div',
    attributes: {'style': 'border-bottom: 1px solid #cccccc; padding: 20px; text-align: center; font-weight: bold; font-size: 30px; color: #cccccc'}, 
    render: function () {      
      this.$el.text(this.label || OB.I18N.getLabel('OBPOS_SearchNoResults'));
      return this;
    }
  });

  OB.COMP.MenuSeparator = Backbone.View.extend({
    tagName: 'li',
    className: 'divider'
  });

  OB.COMP.MenuItem = Backbone.View.extend({
    tagName: 'li',
    initialize: function () {
      this.$el.append($('<a/>').attr('style', 'padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;').attr('href', this.href).attr('target', this.target).attr('onclick', this.onclick).append($('<span/>').text(this.label)));
    },
    href: '',
    target: '_self',
    onclick: 'OB.UTIL.showLoading(true); return true;',
    label: ''
  });

  OB.COMP.MenuAction = Backbone.View.extend({
    tagName: 'li',
    initialize: function () {
      this.$anchor = $('<a/>').attr('style', 'padding: 12px 15px 12px 15px;').attr('href', '#').append($('<span/>').text(this.label));
      this.$el.click(_.bind(this._clickEvent, this));
      this.$el.append(this.$anchor);
    },
    _clickEvent: function (e) {
      e.preventDefault();
      this.clickEvent(e);
    },
    label: '',
    clickEvent: function (e) {
    }
  });

  OB.COMP.Modal = Backbone.View.extend({
    tagName: 'div',
    className: 'modal hide fade',
    attributes: {'style': 'display: none;'},
    maxheight: null,
    initialize: function () {
      this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [
              {kind: B.KindHTML('<span style=\"font-size: 150%;\">&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), content: [this.header]}
          ]}
      , this.options).$el);
      var body = $('<div/>').addClass('modal-body');
      if (this.maxheight) {
        body.css('max-height', this.maxheight);
      }
      this.$el.append(body);
      
      var getcv = this.getContentView();
      if (getcv.kind) {
        // it is a builder structure
        this.contentview = B(getcv, this.options);            
      } else {
        // it is a backbone view
        this.contentview = new getcv(this.options).render();
      }
      body.append(this.contentview.$el);    
    },
    events: {
      'show': 'showEvent' // attach the click event as part of the element
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });

  OB.COMP.ModalAction = Backbone.View.extend({
    tagName: 'div',
    className: 'modal hide fade modal-dialog',
    attributes: {'style': 'display: none;'},
    width: null,
    maxheight: null,
    initialize: function () {
      if (this.width) {
        this.$el.css('width', this.width);
      }

      this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header modal-dialog-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [
              {kind: B.KindHTML('<span style=\"font-size: 150%;\">&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), attr: {'class': 'modal-dialog-header-text'}, content: [this.header]}
          ]}
      , this.options).$el);
      var body = $('<div/>').addClass('modal-body').addClass('modal-dialog-body');
      if (this.maxheight) {
        body.css('max-height', this.maxheight);
      }

      var bodyContentContainer = $('<div/>').addClass('modal-dialog-content-text');
      if (this.setBodyContent) {
        var bodyContent = this.setBodyContent();
        var theBodyContent;
        if (bodyContent.kind) {
          // it is a builder structure
          theBodyContent = B(bodyContent, this.options);
        } else {
          // it is a backbone view
          theBodyContent = new bodyContent(this.options).render();
        }
        bodyContentContainer.append(theBodyContent.$el);
      }

      var bodyButtonsContainer = $('<div/>').addClass('modal-dialog-content-buttons-container');
      if (this.setBodyButtons) {
        var bodyButtons = this.setBodyButtons();
        var theBodyButtons;
        if (bodyButtons.kind) {
          // it is a builder structure
          theBodyButtons = B(bodyButtons, this.options);
        } else {
          // it is a backbone view
          theBodyButtons = new bodyButtons(this.options).render();
        }
        bodyButtonsContainer.append(theBodyButtons.$el);
      }
      body.append(bodyContentContainer);
      body.append(bodyButtonsContainer);

      this.$el.append(body);
    },
    events: {
      'show': 'showEvent' // attach the click event as part of the element
    },
    showEvent: function (e) {
      // custom bootstrap event, no need to prevent default
    }
  });

  OB.COMP.CustomView = Backbone.View.extend({
    initialize: function () {
      this.component = B(this.createView(), this.options);
      this.setElement(this.component.$el);
    },
    createView: function () {
      return ({kind: B.KindJQuery('div')});
    }
  });

  OB.COMP.SearchInput = Backbone.View.extend({
    tagName: 'input',
    attr: function (attributes) {
      if (attributes.clickEvent) {
        this.clickEvent = attributes.clickEvent;
      }
      if (attributes.xWebkitSpeech) {
        this.$el.attr('x-webkit-speech', attributes.xWebkitSpeech);
      }
      if (attributes.type) {
        this.$el.attr('type', attributes.type);
      }
      if (attributes.style) {
        this.$el.attr('style', attributes.style);
      }
      if (attributes.className) {
        this.$el.addClass(attributes.className);
      }
    },
    initialize: function () {
      this.$el.keypress(_.bind(this._clickEvent, this));
      // new googleuiFastButton(this.el, this._clickEvent);
    },
    _clickEvent: function (e) {
      this.clickEvent(e);
    },
    clickEvent: function (e) {
    }
  });

}());