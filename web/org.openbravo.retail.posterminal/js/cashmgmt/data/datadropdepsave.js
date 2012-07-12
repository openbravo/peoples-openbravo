/*global B,_,Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DropDepSave = function (context) {
    this._id = 'dropdepsave';
    this.context = context;
    var me = this;
    me.context.depsdropstosend = [];
    this.context.ListDepositsDrops.listdepositsdrops.on('depositdrop', function (model, index) {
        this.proc.exec({
          depsdropstosend:me.context.depsdropstosend
        }, function (data, message) {
        if (data && data.exception) {
          OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgErrorDropDep'));
        } else {
          me.context.trigger('print');
          window.location=OB.POS.hrefWindow('retail.pointofsale');
        }
       });
     }, this);
    this.proc = new OB.DS.Process('org.openbravo.retail.posterminal.ProcessCashMgmt');
    this.context.SearchDropEvents.destinations.on('click', function (model, index) {
        me.context.ListDepositsDrops.dropsdeps.add({deposit: 0, drop: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), name: me.context.destinationKey});
        me.context.depsdropstosend.push({amount: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), key: me.context.destinationKey, type: me.context.type, reasonId:model.get('id')});
      }, this);
    this.context.SearchDepositEvents.destinations.on('click', function (model, index) {
      me.context.ListDepositsDrops.dropsdeps.add({deposit: me.context.amountToDrop, drop: 0, description: me.context.identifier+' - '+model.get('name'), name: me.context.destinationKey});
      me.context.depsdropstosend.push({amount: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), key: me.context.destinationKey, type: me.context.type, reasonId:model.get('id')});
    }, this);
  };

 }());