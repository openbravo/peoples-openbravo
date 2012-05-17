/*global window, define, $, Backbone */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  // Generic Button
  OB.COMP.Button = Backbone.View.extend({
    tagName: 'a',
    className: 'btnlink',
    attributes: {'href': '#'},
    initialize: function () {
      if (this.icon) {
        this.$el.append($('<i class=\"' + this.icon + '\"></i>'));
      }
      this.$el.append($('<span>' + this.label + '</span>'));     
      if (this.iconright) {
        this.$el.append($('<i class=\"' + this.iconright + '\"></i>'));
      }      
    },
    icon: '',
    iconright: '',
    label: '',
    events: {
      'click': 'clickEvent' // attach the click event as part of the element
    },
    clickEvent: function (e) {
      e.preventDefault();
    }       
  });
  
  OB.COMP.ButtonNew = OB.COMP.Button.extend({
    icon: 'icon-asterisk icon-white',
    label: OB.I18N.getLabel('OBPOS_LblNew'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modelorderlist.addNewOrder();
    }       
  });  
  
  OB.COMP.ButtonDelete = OB.COMP.Button.extend({
    icon: 'icon-trash  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblDelete'),
    clickEvent: function (e) {
      e.preventDefault();
      if (window.confirm(OB.I18N.getLabel('OBPOS_MsgConfirmDelete'))) {
        this.options.modelorderlist.deleteCurrent();
      }
    }       
  });
  
  OB.COMP.ButtonPrint = OB.COMP.Button.extend({
    icon: 'icon-print  icon-white',
    label: OB.I18N.getLabel('OBPOS_LblPrint'),
    clickEvent: function (e) {
      e.preventDefault();
      this.options.modelorder.trigger('print');
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
    initialize: function () {
      this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-header'}, content: [
            {kind: B.KindJQuery('a'), attr: {'class': 'close', 'data-dismiss': 'modal'}, content: [ 
              {kind: B.KindHTML('<span>&times;</span>')}
            ]},
            {kind: B.KindJQuery('h3'), content: [this.header]}
          ]}      
      , this.options).$el);
      this.$el.append(B(
          {kind: B.KindJQuery('div'), attr: {'class': 'modal-body'}, content: [
            this.getContentView()
          ]}   
      , this.options).$el);                  
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

});    