/*global B,_,Backbone */

(function () {

  OB = window.OB || {};
  OB.DATA = window.OB.DATA || {};

  OB.DATA.DropDepSave = function (context) {
    var i;
    var me = this;
    this._id = 'dropdepsave';
    this.context = context;
    me.context.depsdropstosend = [];
    this.context.ListDepositsDrops.listdepositsdrops.on('depositdrop', function (model, index) {
      if(me.context.depsdropstosend.length===0){
        window.location=OB.POS.hrefWindow('retail.pointofsale');
        return true;
      }
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
      for(i=0; i< this.context.ListDepositsDrops.listdepositsdrops.models.length; i++) {
        if(this.context.ListDepositsDrops.listdepositsdrops.models[i].get('paySearchKey')===me.context.destinationKey){
          if((OB.DEC.sub(this.context.ListDepositsDrops.listdepositsdrops.models[i].get('total'),me.context.amountToDrop) < 0)){
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
            return true;
          }
            me.context.ListDepositsDrops.listdepositsdrops.models[i].get('listdepositsdrops').push({deposit: 0, drop: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), name: me.context.destinationKey, user: OB.POS.modelterminal.get('context').user._identifier, time: new Date()});
            me.context.depsdropstosend.push({amount: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), key: me.context.destinationKey, type: me.context.type, reasonId:model.get('id'), user: OB.POS.modelterminal.get('context').user._identifier, time: new Date().toString().substring(16,21)});
            me.context.ListDepositsDrops.listdepositsdrops.trigger('reset');
        }
      }
     }, this);
    this.context.SearchDepositEvents.destinations.on('click', function (model, index) {
      for(i=0; i< this.context.ListDepositsDrops.listdepositsdrops.models.length; i++) {
        if(this.context.ListDepositsDrops.listdepositsdrops.models[i].get('paySearchKey')===me.context.destinationKey){
          if((OB.DEC.add(this.context.ListDepositsDrops.listdepositsdrops.models[i].get('total'),me.context.amountToDrop) < 0)){
            OB.UTIL.showError(OB.I18N.getLabel('OBPOS_MsgMoreThanAvailable'));
            return true;
          }
          me.context.ListDepositsDrops.listdepositsdrops.models[i].get('listdepositsdrops').push({deposit: me.context.amountToDrop, drop: 0, description: me.context.identifier+' - '+model.get('name'), name: me.context.destinationKey, user: OB.POS.modelterminal.get('context').user._identifier, time: new Date()});
          me.context.depsdropstosend.push({amount: me.context.amountToDrop, description: me.context.identifier+' - '+model.get('name'), key: me.context.destinationKey, type: me.context.type, reasonId:model.get('id'), user: OB.POS.modelterminal.get('context').user._identifier, time: new Date().toString().substring(16,21)});
          me.context.ListDepositsDrops.listdepositsdrops.trigger('reset');
        }
     }
    }, this);
  };

 }());