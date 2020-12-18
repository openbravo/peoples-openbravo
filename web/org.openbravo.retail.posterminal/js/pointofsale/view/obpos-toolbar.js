/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.ToolbarImpl',
  kind: 'OB.UI.MultiColumn.Toolbar',
  classes: 'obObposPointOfSaleUiToolbarImpl',
  menuEntries: [
    {
      kind: 'OB.UI.MenuDisableEnableRFIDReader',
      classes: 'obObposPointOfSaleUiToolbarImpl-obUiMenuDisableEnableRFIDReader'
    },
    {
      kind: 'OB.UI.MenuSeparator',
      name: 'sep0',
      classes: 'obObposPointOfSaleUiToolbarImpl-sep0',
      init: function(model) {
        if (!OB.UTIL.RfidController.isRfidConfigured()) {
          this.hide();
        }
      }
    }
  ],
  initComponents: function() {
    // set up the POS menu
    //Menu entries is used for modularity. cannot be initialized
    //this.menuEntries = [];
    this.menuEntries.push({
      kind: 'OB.UI.MenuReceiptSelector',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuReceiptSelector'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuReturn',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuReturn'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuVoidLayaway',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuVoidLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuReceiptLayaway',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuReceiptLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuCancelLayaway',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuCancelLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuProperties',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuProperties'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuInvoice',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuInvoice'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuPrint',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuPrint'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuLayaway',
      classes: 'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuLayaway'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuCancelAndReplace',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuCancelAndReplace'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuCustomers',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuCustomers'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuOpenDrawer',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuOpenDrawer'
    });
    // TODO: what is this for?!!
    // this.menuEntries = this.menuEntries.concat(this.externalEntries);
    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator',
      name: 'sep1',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuDiscounts',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuDiscounts'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator',
      name: 'sep2',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuSeparator'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuReactivateQuotation',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuReactivateQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuRejectQuotation',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuRejectQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuCreateOrderFromQuotation',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuCreateOrderFromQuotation'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuCreateQuotationFromOrder',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuCreateQuotationFromOrder'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuQuotation',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuQuotation'
    });
    this.menuEntries.push({
      kind: 'OB.UI.MenuMultiOrders',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuMultiOrders'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSeparator',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuSeparator',
      name: 'sep3'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuBackOffice',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuBackOffice'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSelectPrinter',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuSelectPrinter'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuSelectPDFPrinter',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuSelectPDFPrinter'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuForceIncrementalRefresh',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuForceIncrementalRefresh'
    });

    this.menuEntries.push({
      kind: 'OB.UI.MenuTestPrinter',
      classes:
        'obObposPointOfSaleUiLeftToolbarImpl-menuEntries-obUiMenuTestPrinter'
    });

    //remove duplicates
    this.menuEntries = _.uniq(this.menuEntries, false, function(p) {
      return p.kind + p.name;
    });
    this.inherited(arguments);
  }
});
