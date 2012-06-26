/*global B,_,Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DropDepSave = function (context) {
    this._id = 'dropdepsave';
    this.context = context;
    var me = this;
    this.context.destinations = new Backbone.Collection();
    this.context.destinations.on('click', function (model, index) {
        this.proc.exec({
//      terminalId: OB.POS.modelterminal.get('terminal').id,
          name: model.get('name'),
          amount: me.context.amountToDrop,
          key: me.context.destinationKey,
          type: me.context.type
        }, function (data, message) {
        if (data && data.exception) {
          OB.UTIL.showError("ERROR");
        } else {
          OB.UTIL.showSuccess("OK");
        }
       });
     }, this);
     this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt');

  };

 }());