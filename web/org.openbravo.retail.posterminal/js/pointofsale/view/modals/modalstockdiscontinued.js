/*
 ************************************************************************************
 * Copyright (C) 2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

(function() {
  enyo.kind({
    kind: 'OB.UI.ModalAction',
    name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockDiscontinued',
    classes: 'obObposPointOfSaleUiModalsModalStockDiscontinued',
    header: '',
    bodyContent: {
      classes: 'obObposPointOfSaleUiModalsModalStockDiscontinued-bodyContent',
      components: [
        {
          name: 'bodymessage',
          classes:
            'obObposPointOfSaleUiModalsModalStockDiscontinued-bodyContent-bodymessage',
          content: ''
        }
      ]
    },
    buttonPushed: false,
    buttons: [],
    bodyButtons: {},
    executeOnHide: function() {
      if (
        !this.buttonPushed &&
        this.args.options &&
        this.args.options.onHideFunction
      ) {
        this.args.options.onHideFunction(this);
      }
      return true;
    },
    executeOnShow: function() {
      var me = this;
      this.buttonPushed = false;
      this.acceptLine = this.args.acceptLine;
      this.order = this.args.order;
      this.line = this.args.line;
      this.product = this.args.product;
      this.actionName = this.args.actionName;
      this.autoDismiss =
        !OB.UTIL.isNullOrUndefined(this.args.options) &&
        !OB.UTIL.isNullOrUndefined(this.args.options.autoDismiss)
          ? this.args.options.autoDismiss
          : true;
      if (this.args.options && this.args.options.hideCloseButton) {
        this.$.headerCloseButton.hide();
      } else {
        this.$.headerCloseButton.show();
      }
      this.$.header.setContent(this.args.header);
      this.$.bodyContent.$.bodymessage.setContent(this.args.message);
      // Destroy previous buttons
      _.each(this.$.bodyButtons.getComponents(), function(cmp) {
        if (cmp.kind === 'OB.UI.ModalDialogButton') {
          cmp.destroy();
        }
      });
      var buttons = this.args.buttons.concat(this.buttons);
      // Create buttons
      _.each(
        buttons,
        function(btn, indx) {
          this.$.bodyButtons.createComponent({
            kind: 'OB.UI.ModalDialogButton',
            name: 'btnAction' + indx,
            classes:
              'obObposPointOfSaleUiModalsModalStockDiscontinued-bodyButtons-obUiModalDialogButton',
            content: btn.label,
            isDefaultAction: btn.isDefaultAction,
            action: btn.action,
            displayLogic: btn.displayLogic,
            tap: function() {
              me.buttonPushed = true;
              if (this.action) {
                this.action(true);
              }
              this.doHideThisPopup();
            }
          });
        },
        this
      );
      this.$.bodyButtons.render();
      // Apply display logic
      _.each(this.$.bodyButtons.getComponents(), function(cmp) {
        if (cmp.displayLogic) {
          cmp.displayLogic();
        }
      });
      return true;
    }
  });

  OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
    kind: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockDiscontinued',
    name: 'OBPOSPointOfSale_UI_Modals_ModalStockDiscontinued'
  });
})();
