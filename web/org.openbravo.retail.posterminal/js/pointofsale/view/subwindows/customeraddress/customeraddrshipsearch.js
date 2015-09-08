/*
 ************************************************************************************
 * Copyright (C) 2012-2015 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerShipAddress',
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  published: {
    bPartnerId: null,
    bPartnerModel: null
  },
  events: {
    onChangeBusinessPartner: '',
    onChangeSubWindow: ''
  },
  components: [{
    style: 'text-align: center; padding-top: 10px; float: left; width: 19%;',
    components: [{
      kind: 'OB.OBPOSPointOfSale.UI.customeraddr.CustomerAddrLeftBar'
    }]
  }, {
    style: 'float: left; width: 80%;',
    components: [{
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'bpsloclistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '301px',
          renderHeader: 'OB.OBPOSPointOfSale.UI.customeraddr.ModalCustomerAddrScrollableHeader',
          renderLine: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerAddrLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }, {
    style: 'clear: both'
  }],
  clearAction: function (inSender, inEvent) {
    this.bpsLocList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        filter = inEvent.locName,
        criteria = {};

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPsLoc(dataBps, args) {
      me.bpsLocList.reset();
      if (dataBps && dataBps.length > 0) {
        me.bpsLocList.add(dataBps.models);
      }
    }
    if (filter && _.isString(filter) && filter.length > 0) {
      criteria.name = {
        operator: OB.Dal.CONTAINS,
        value: filter
      };
    }
    criteria.bpartner = this.bPartnerId;
    criteria.isShipTo = true;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var filterIdentifier = {
        columns: ['_identifier'],
        operator: 'startsWith',
        value: filter
      },
          bPartnerId = {
          columns: ['bpartner'],
          operator: 'equals',
          value: this.bPartnerId,
          isId: true
          };
      var remoteCriteria = [filterIdentifier, bPartnerId];
      criteria.remoteFilters = remoteCriteria;
    }
    OB.Dal.find(OB.Model.BPLocation, criteria, successCallbackBPsLoc, errorCallback);
    return true;
  },
  bpsLocList: null,
  init: function () {
    this.bpsLocList = new Backbone.Collection();
    this.$.bpsloclistitemprinter.setCollection(this.bpsLocList);
    this.bpsLocList.on('click', function (model) {
      var sw = this.subWindow;
      this.doChangeSubWindow({
        newWindow: {
          name: 'customerAddressView',
          params: {
            navigateOnClose: sw.getName(),
            businessPartner: this.bPartnerModel,
            bPLocation: model
          }
        }
      });
    }, this);
  }
});

/*body*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customershipaddr.casbody',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerShipAddress',
    style: 'padding: 15px; padding-top: 0px;'
  }]
});

//Modal pop up
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customershipaddr.cas',
  kind: 'OB.UI.Subwindow',
  events: {
    onSearchAction: ''
  },
  beforeSetShowing: function (params) {
    if (params.bPartner) {
      var listCustAddr = this.$.subWindowBody.$.casbody.$.listCustomerShipAddress;
      listCustAddr.setBPartnerId(params.bPartner);
      OB.Dal.get(OB.Model.BusinessPartner, params.bPartner, function successCallbackBPs(dataBps) {
        listCustAddr.setBPartnerModel(dataBps);
        listCustAddr.$.customerAddrLeftBar.$.newCustomerAddrButton.setBusinessPartner(dataBps);
      }, function errorCallback(tx, error) {});
    }
    if (this.caller === 'mainSubWindow') {
      this.waterfall('onSearchAction', {
        cleanResults: true
      });
    }
    this.$.subWindowBody.$.casbody.$.listCustomerShipAddress.$.bpsloclistitemprinter.$.theader.$.modalCustomerAddrScrollableHeader.searchAction();
    return true;
  },
  beforeClose: function (dest) {
    if (dest === 'mainSubWindow') {
      this.waterfall('onSearchAction', {
        cleanResults: true
      });
    }
    return true;
  },
  defaultNavigateOnClose: 'mainSubWindow',
  header: {
    kind: 'OB.UI.SubwindowHeader',
    i18nHeaderMessage: 'OBPOS_TitleCustomerAddressSearch',
    onTapCloseButton: function () {
      var subWindow = this.subWindow;
      subWindow.doChangeSubWindow({
        newWindow: {
          name: 'mainSubWindow'
        }
      });
    }
  },
  body: {
    kind: 'OB.OBPOSPointOfSale.UI.customershipaddr.casbody'
  }
});