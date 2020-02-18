/*
 ************************************************************************************
 * Copyright (C) 2018-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _ */

(function() {
  enyo.kind({
    kind: 'OB.UI.Modal',
    name: 'OB.OBPOSPointOfSale.UI.Modals.ModalStockDiscontinued',
    classes: 'obObposPointOfSaleUiModalsModalStockDiscontinued',
    header: '',
    body: {
      classes: 'obObposPointOfSaleUiModalsModalStockDiscontinued-body',
      components: [
        {
          name: 'bodymessage',
          classes:
            'obObposPointOfSaleUiModalsModalStockDiscontinued-body-bodymessage',
          content: ''
        }
      ]
    },
    buttonPushed: false,
    buttons: [],
    footer: {},
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
      this.attrs = this.args.attrs;
      if (this.args.options && this.args.options.hideCloseButton) {
        this.$.headerCloseButton.hide();
      } else {
        this.$.headerCloseButton.show();
      }
      this.setHeader(this.args.header);
      this.$.body.$.bodymessage.setContent(this.args.message);
      // Destroy previous buttons
      _.each(this.$.footer.getComponents(), function(cmp) {
        if (cmp.kind === 'OB.UI.ModalDialogButton') {
          cmp.destroy();
        }
      });
      var buttons = this.args.buttons.concat(this.buttons);
      // Create buttons
      _.each(
        buttons,
        function(btn, indx) {
          this.$.footer.createComponent({
            kind: 'OB.UI.ModalDialogButton',
            name: 'btnAction' + indx,
            classes:
              'obObposPointOfSaleUiModalsModalStockDiscontinued-footer-obUiModalDialogButton',
            label: btn.label,
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
      this.$.footer.render();
      // Apply display logic
      _.each(this.$.footer.getComponents(), function(cmp) {
        if (cmp.displayLogic) {
          cmp.displayLogic(me.attrs);
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
