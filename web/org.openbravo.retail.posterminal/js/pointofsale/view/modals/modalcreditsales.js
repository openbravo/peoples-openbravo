/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

enyo.kind({
  kind: 'OB.UI.ModalAction',
  name: 'OBPOS.UI.modalEnoughCredit',
  myId: 'modalEnoughCredit',
  header: OB.I18N.getLabel('OBPOS_enoughCreditHeader'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_enoughCreditBody')
  },
  bodyButtons: {
    components: [{
      kind: 'OB.UI.ModalDialogButton',
      name: 'OBPOS.UI.useCreditButton',
      content: OB.I18N.getLabel('OBPOS_LblUseCredit'),
      isApplyButton: true,
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      init: function (model) {
    	  this.model = model;
      },
      tap: function () {
    	$('#modalEnoughCredit').modal('hide');
    	this.model.get('order').trigger('paymentDone');
    	this.model.get('order').trigger('openDrawer');
    	if (!OB.POS.modelterminal.get('connectedToERP')) {
    		var bp = this.model.get('order').get('bp');
    		var bpCreditUsed = this.model.get('order').get('bp').get('creditUsed');
    		var totalPending = this.model.get('order').getPending();
    		bp.set('creditUsed', bpCreditUsed - totalPending);
    		function success(tx) {
			  window.console.log(tx);
			}
			 
			function error(tx) {
			  window.console.error(tx);
			}
    		OB.Dal.save(bp, success, error);
    	}
      }
    }, {
      kind: 'OB.UI.ModalDialogButton',
      name: 'OBPOS.UI.cancelUseCreditButton',
      content: OB.I18N.getLabel('OBPOS_LblCancel'),
      classes: 'btnlink btnlink-gray modal-dialog-content-button',
      attributes: {
  	    'data-dismiss': 'modal'
  	  }
    }]
  },
  executeOnShow: function (e) {
	  var pendingQty = e.data.dialog.container.model.get('order').getPending();
	  var bpName = e.data.dialog.container.model.get('order').get('bp').get('_identifier');
	  e.data.dialog.$.bodyContent.children[0].setContent(OB.I18N.getLabel('OBPOS_enoughCreditBody', [pendingQty, bpName]));
  }
});

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OBPOS.UI.modalNotEnoughCredit',
  myId: 'modalNotEnoughCredit',
  style: 'background-color: #EBA001;',
  header: OB.I18N.getLabel('OBPOS_notEnoughCreditHeader'),
  isApplyButton: true,
  bodyContent: {
	  tag: 'div',
	  content: OB.I18N.getLabel('OBPOS_notEnoughCreditBody')
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale',{
	  kind: 'OBPOS.UI.modalEnoughCredit'
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale',{
	  kind: 'OBPOS.UI.modalNotEnoughCredit'
});