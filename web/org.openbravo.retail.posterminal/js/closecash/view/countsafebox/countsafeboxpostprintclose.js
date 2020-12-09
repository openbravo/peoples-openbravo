/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  kind: 'OB.OBPOSCloseCash.UI.PostPrintClose',
  name: 'OB.OBPOSCountSafeBox.UI.PostPrintClose',
  headerComponents: [
    {
      tag: 'img',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-img',
      initComponents: function() {
        if (OB.MobileApp.model.get('terminal').organizationImage) {
          this.setAttribute(
            'src',
            'data:' +
              OB.MobileApp.model.get('terminal').organizationImageMime +
              ';base64,' +
              OB.MobileApp.model.get('terminal').organizationImage
          );
        }
      }
    },
    {
      name: 'store',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-store'
    },
    {
      name: 'terminal',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-terminal'
    },
    {
      name: 'safebox',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-safebox'
    },
    {
      name: 'user',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-user'
    },
    {
      name: 'openingtime',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-openingtime'
    },
    {
      name: 'time',
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-time'
    },
    {
      classes: 'obObPosCloseCashUiPostPrintClose-headerContainer-element1'
    }
  ],
  componentInitialization: function() {
    this.model.get('closeCashReport').on('add', closeCashReport => {
      const filtered = this.filterMovements(closeCashReport, false);
      this.$.startingsTable.setCollection(filtered.startings);
      this.$.startingsTable.setValue(
        'totalstartings',
        closeCashReport.get('totalStartings')
      );

      this.$.dropsTable.setCollection(filtered.drops);
      this.$.dropsTable.setValue(
        'totaldrops',
        closeCashReport.get('totalDrops')
      );

      this.$.depositsTable.setCollection(filtered.deposits);
      this.$.depositsTable.setValue(
        'totaldeposits',
        closeCashReport.get('totalDeposits')
      );
    });
  },
  stepDisplayer: function() {
    const currentSafeBox = JSON.parse(
      OB.UTIL.localStorage.getItem('currentSafeBox')
    );
    this.$.headerContainer.$.safebox.setContent(
      OB.I18N.getLabel('OBPOS_LblSafeBox') + ': ' + currentSafeBox.name
    );
  }
});
