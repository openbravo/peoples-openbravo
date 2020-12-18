/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global OBRDM, enyo */

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoPopupStandard',
  kind: 'OB.UI.Modal',
  classes: 'obrdmUiOrderLineInfoPopupStandard',
  i18nHeader: 'OBRDM_LblInformation',
  body: {
    kind: 'OBRDM.UI.OrderLineInfoStandard_body'
  },
  handlers: {
    onHideInfoPopup: 'hidePopup',
    onShowInfoPopup: 'showPopup'
  },
  executeOnShow: function() {
    this.line = this.args.line;
    this.waterfall('onShowInfo', {
      line: this.line
    });
  },
  executeOnHide: function() {
    if (this.args.callback) {
      this.args.callback(this.args.context);
    }
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  hidePopup: function(inSender, inEvent) {
    this.hide();
    return true;
  },
  showPopup: function(inSender, inEvent) {
    this.show();
    return true;
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoStandard_body',
  classes: 'obrdmUiOrderLineInfoStandardBody',
  components: [
    {
      name: 'table',
      kind: 'OBRDM.UI.OrderLineInfoTableStandard',
      classes: 'obrdmUiOrderLineInfoStandardBody-table'
    },
    {
      name: 'buttonFooter',
      classes: 'obrdmUiOrderLineInfoStandardBody-buttonFooter',
      components: [
        {
          name: 'infoButtons',
          classes: 'obrdmUiOrderLineInfoStandardBody-buttonFooter-infoButtons'
        }
      ]
    }
  ],
  buttons: [
    {
      name: 'buttoncancel',
      kind: 'OBRDM.UI.OrderLineInfoCloseBtn',
      classes: 'obrdmUiOrderLineInfoStandardBody-buttoncancel'
    }
  ],
  initComponents: function() {
    var me = this;

    this.inherited(arguments);
    _.each(this.buttons, function(button) {
      me.$.infoButtons.createComponent(button, {
        owner: me
      });
    });
  }
});

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoTableStandard',
  classes: 'obrdmUiOrderLineInfoTableStandard',
  infoTableComponents: [],
  components: [],
  initComponents: function() {
    this.inherited(arguments);
    var me = this;
    _.each(this.infoTableComponents, function(comp) {
      me.createComponent(comp, {
        owner: me
      });
    });
  }
});

OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
  kind: 'OBRDM.UI.OrderLineInfoPopupStandard',
  name: 'OBRDM_UI_OrderLineInfoPopupStandard'
});

enyo.kind({
  name: 'OBRDM.UI.IssueSalesOrderInformationLine',
  classes: 'obrdmUiIssueSalesOrderInformationLine',
  handlers: {
    onShowInfo: 'showInfo'
  },
  components: [
    {
      classes: 'obrdmUiIssueSalesOrderInformationLine-container1',
      components: [
        {
          name: 'searchKey',
          classes: 'obrdmUiIssueSalesOrderInformationLine-container1-searchKey',
          components: [
            {
              name: 'searchKeyLbl',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-searchKey-searchKeyLbl'
            },
            {
              name: 'searchKeyValue',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-searchKey-searchKeyValue'
            }
          ]
        },
        {
          name: 'name',
          classes: 'obrdmUiIssueSalesOrderInformationLine-container1-name',
          components: [
            {
              name: 'nameLbl',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-name-nameLbl'
            },
            {
              name: 'nameValue',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-name-nameValue'
            }
          ]
        },
        {
          name: 'ean',
          classes: 'obrdmUiIssueSalesOrderInformationLine-container1-ean',
          components: [
            {
              name: 'eanLbl',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-ean-eanLbl'
            },
            {
              name: 'eanValue',
              classes:
                'obrdmUiIssueSalesOrderInformationLine-container1-ean-eanValue'
            }
          ]
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.searchKeyLbl.setContent(
      OB.I18N.getLabel('OBRDM_LblProductSearchKey')
    );
    this.$.nameLbl.setContent(OB.I18N.getLabel('OBRDM_LblProductName'));
    this.$.eanLbl.setContent(OB.I18N.getLabel('OBRDM_LblProductEAN'));
  },
  showInfo: function(inSender, inEvent) {
    this.$.searchKeyValue.setContent(inEvent.line.get('productSearchKey'));
    this.$.nameValue.setContent(inEvent.line.get('productName'));
    this.$.eanValue.setContent(inEvent.line.get('uPCEAN'));
    return true;
  }
});

OBRDM.UI.OrderLineInfoTableStandard.prototype.infoTableComponents.push({
  kind: 'OBRDM.UI.IssueSalesOrderInformationLine',
  name: 'IssueSalesOrderInformationLine'
});

OBRDM.UI.OrderLineInfoTable.prototype.infoTableComponents.push({
  kind: 'OBRDM.UI.IssueSalesOrderInformationLine',
  name: 'IssueSalesOrderInformationLine'
});
