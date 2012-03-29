define(['utilities', 'i18n', 'model/order', 'model/terminal'], function () {
  
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
        name = this.terminal.get('terminal')['_identifier'];
        clientname = this.terminal.get('terminal')['client._identifier'];
        orgname = this.terminal.get('terminal')['organization._identifier'];
      }      
      if (this.terminal.get('pricelist')) {
        pricelistname = this.terminal.get('pricelist')['_identifier'];
        currencyname = this.terminal.get('pricelist')['currency._identifier'];
      }
      if (this.terminal.get('location')) {
        locationname = this.terminal.get('location')['_identifier'];
      }

      this.yourterminal.text(name);
      this.yourcompany.text(orgname);
      this.yourcompanyproperties.empty().append(
          OB.UTIL.EL(
            {tag: 'div', content: [
              {tag: 'div', content: [
                {tag: 'span', content: [
                  OB.I18N.getLabel('OBPOS_CompanyClient')
                ]},
                {tag: 'span', attr:{'style': 'font-weight: bold;'}, content: [
                  clientname
                ]}
              ]},
              {tag: 'div', content: [
                {tag: 'span', content: [
                  OB.I18N.getLabel('OBPOS_CompanyOrg')
                ]},
                {tag: 'span', attr:{'style': 'font-weight: bold;'}, content: [
                  orgname
                ]}
              ]},
              {tag: 'div', content: [
                {tag: 'span', content: [
                  OB.I18N.getLabel('OBPOS_CompanyPriceList')
                ]},
                {tag: 'span', attr:{'style': 'font-weight: bold;'}, content: [
                  pricelistname
                ]}
              ]},
              {tag: 'div', content: [
                {tag: 'span', content: [
                  OB.I18N.getLabel('OBPOS_CompanyCurrency')
                ]},
                {tag: 'span', attr:{'style': 'font-weight: bold;'}, content: [
                  currencyname
                ]}
              ]},
              {tag: 'div', content: [
                {tag: 'span', content: [
                  OB.I18N.getLabel('OBPOS_CompanyLocation')
                ]},
                {tag: 'span', attr:{'style': 'font-weight: bold;'}, content: [
                  locationname
                ]}
              ]}
            ]}
          )
      );
    }, this);
        
  }

});  