/*global define */

define(['builder', 'utilities', 'i18n', 'model/order', 'model/terminal'], function (B) {
  
  OB = window.OB || {};
  OB.COMP = window.OB.COMP || {};

  OB.COMP.Terminal = function (yourterminal, yourcompany, yourcompanyproperties) {
    this.yourterminal = yourterminal;
    this.yourcompany = yourcompany;
    this.yourcompanyproperties = yourcompanyproperties;
  };
  
  OB.COMP.Terminal.prototype.setModel = function (terminal) {
    this.terminal = terminal;
    
    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function () {
      
      var name = '';     
      var clientname = '';
      var orgname = '';
      var pricelistname = '';
      var currencyname = '';
      var locationname = '';
      
      if (this.terminal.get('terminal')) {
        name = this.terminal.get('terminal')._identifier;
        clientname = this.terminal.get('terminal')['client._identifier'];
        orgname = this.terminal.get('terminal')['organization._identifier'];
      }      
      if (this.terminal.get('pricelist')) {
        pricelistname = this.terminal.get('pricelist')._identifier;
        currencyname = this.terminal.get('pricelist')['currency._identifier'];
      }
      if (this.terminal.get('location')) {
        locationname = this.terminal.get('location')._identifier;
      }

      this.yourterminal.text(name);
      this.yourcompany.text(orgname);
      this.yourcompanyproperties.empty().append(B(
        {kind: B.KindJQuery('div'), content: [
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyClient')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold;'}, content: [
              clientname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyOrg')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold;'}, content: [
              orgname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyPriceList')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold;'}, content: [
              pricelistname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyCurrency')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold;'}, content: [
              currencyname
            ]}
          ]},
          {kind: B.KindJQuery('div'), content: [
            {kind: B.KindJQuery('span'), content: [
              OB.I18N.getLabel('OBPOS_CompanyLocation')
            ]},
            {kind: B.KindJQuery('span'), attr:{'style': 'font-weight: bold;'}, content: [
              locationname
            ]}
          ]}
        ]}          
      ).$);
    }, this);
        
  };

});  