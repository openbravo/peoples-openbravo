/*global window, B, $, Backbone, _ */

(function () {

  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Base button
  OB.COMP.Button = Backbone.View.extend({
    tagName: 'button',
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

  OB.COMP.PaymentButton = OB.COMP.Button.extend({
    className: 'btnlink btnlink-small',
    attributes: {'style': 'width:70px; text-align:right;'},
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

  // Toolbar Button
  OB.COMP.ToolbarButton = OB.COMP.Button.extend({
    render: function () {
      this.$el.addClass('btnlink');
      if (this.icon) {
        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
      }
      this.$el.append($('<span>' + this.label + '</span>'));
      if (this.iconright) {
        this.$el.append($('<i class=\"' + this.iconright + '\"></i>'));
      }
      return this;
    },
    icon: '',
    iconright: '',
    label: ''
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

  // Menu Button
  OB.COMP.MenuButton = Backbone.View.extend({
    tagName: 'div',
    className: 'dropdown',
    attributes: {'style': 'display:inline-block;'},
    initialize: function () {
      this.button = $('<a href=\"#\" class=\"btnlink\" data-toggle=\"dropdown\"></a>');

      // The button
      this.$el.append(this.button);
      if (this.icon) {
        this.button.append($('<i class=\"' + this.icon + '\"></i>'));
      }
      this.button.append($('<span>' + this.label + ' </span>'));
      if (this.iconright) {
        this.button.append($('<i class=\"' + this.iconright + '\"></i>'));
      }
      this.button.append($('<span class=\"caret\"></span>'));

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

  OB.COMP.MenuSeparator = Backbone.View.extend({
    tagName: 'li',
    className: 'divider'
  });

  OB.COMP.MenuItem = Backbone.View.extend({
    tagName: 'li',
    initialize: function () {
      this.$el.append($('<a/>').attr('style', 'padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;').attr('href', this.href).attr('onclick', 'OB.UTIL.showLoading(true); return true;').append($('<span/>').text(this.label)));
    },
    href: '',
    label: ''
  });

  OB.COMP.MenuAction = Backbone.View.extend({
    tagName: 'li',
    initialize: function () {
      this.$anchor = $('<a/>').attr('style', 'padding-bottom: 10px;padding-left: 15px; padding-right: 15px;padding-top: 10px;').attr('href', '#').append($('<span/>').text(this.label));
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

  // Generic Tab Button
  OB.COMP.ButtonTab = Backbone.View.extend({
    tagName: 'a',
    className: 'btnlink btnlink-gray',
    attributes: {'data-toggle': 'tab'},
    initialize: function () {
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

  OB.COMP.CustomView = Backbone.View.extend({
    initialize: function () {
      this.component = B(this.createView(), this.options);
      this.setElement(this.component.$el);
    },
    createView: function () {
      return ({kind: B.KindJQuery('div')});
    }
  });

}());