(function (OBPOS) {

  OBPOS.Sales.Terminal = function (elemt, elems) {
    this.elemt = elemt;
    this.elems = elems;
  };
  
  OBPOS.Sales.Terminal.prototype.setModel = function (terminal) {
    this.terminal = terminal;
    
    this.terminal.on('change:terminal change:bplocation change:location change:pricelist change:pricelistversion', function () {
      
      var t1 = '';
      var line1 = '';      
      if (this.terminal.get('terminal')) {
        line1 += this.terminal.get('terminal')['client._identifier'] + " | " + this.terminal.get('terminal')['organization._identifier'];
        t1 = this.terminal.get('terminal')['_identifier'];
      }      
      if (this.terminal.get('pricelist')) {
        line1 += ' | ' + this.terminal.get('pricelist')['_identifier'] + " | " + this.terminal.get('pricelist')['currency._identifier'];
      }

      var line2 = '';
      if (this.terminal.get('location')) {
        line2 += this.terminal.get('location')['_identifier'];
      }

      this.elemt.text(t1);
      this.elems.html(line1 + "<br/>" + line2);      
    }, this);
        
  }

}(window.OBPOS));   