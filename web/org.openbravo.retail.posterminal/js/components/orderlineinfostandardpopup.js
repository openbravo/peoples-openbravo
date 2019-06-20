/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, OBRDM, enyo, _ */

enyo.kind({
  name: 'OBRDM.UI.OrderLineInfoPopupStandard',
  kind: 'OB.UI.Modal',
  topPosition: '44px',
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
  components: [
    {
      name: 'table',
      kind: 'OBRDM.UI.OrderLineInfoTableStandard'
    },
    {
      name: 'buttonFooter',
      classes: 'obrdm-oederlineinfostandard-buttonfooter',
      components: [
        {
          name: 'infoButtons',
          classes: 'obrdm-oederlineinfostandard-infobuttons'
        }
      ]
    }
  ],
  buttons: [
    {
      kind: 'OBRDM.UI.OrderLineInfoCloseBtn',
      name: 'buttoncancel'
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
  classes: 'obrdm-orderineinfotablestandard',
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
  handlers: {
    onShowInfo: 'showInfo'
  },
  components: [
    {
      classes: 'obrdm-orderlineinfopopupstandard',
      components: [
        {
          name: 'searchKey',
          classes: 'obrdm-orderlineinfopopupstandard-searchkey',
          components: [
            {
              name: 'searchKeyLbl',
              classes: 'obrdm-orderlineinfopopupstandard-searchkey-lbl'
            },
            {
              name: 'searchKeyValue',
              classes: 'obrdm-orderlineinfopopupstandard-searchkey-value'
            }
          ]
        },
        {
          name: 'name',
          classes: 'obrdm-orderlineinfopopupstandard-name',
          components: [
            {
              name: 'nameLbl',
              classes: 'obrdm-orderlineinfopopupstandard-name-lbl'
            },
            {
              name: 'nameValue',
              classes: 'obrdm-orderlineinfopopupstandard-name-value'
            }
          ]
        },
        {
          name: 'ean',
          classes: 'obrdm-orderlineinfopopupstandard-ean',
          components: [
            {
              name: 'eanLbl',
              classes: 'obrdm-orderlineinfopopupstandard-ean-lbl'
            },
            {
              name: 'eanValue',
              classes: 'obrdm-orderlineinfopopupstandard-ean-value'
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
