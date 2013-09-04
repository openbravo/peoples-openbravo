/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

//Header of the body of cas (customer address search)
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.ModalCustomerAddrScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  style: 'border-bottom: 10px solid #ff0000;',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px; 10px; 0px; 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'filterText',
          style: 'width: 100%',
          isFirstFocus: true
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.$.filterText.setValue('');
    this.doClearAction();
  },
  searchAction: function () {
    this.doSearchAction({
      locName: this.$.filterText.getValue(),
      operator: OB.Dal.CONTAINS
    });
    return true;
  }
});

/*items of collection Customer*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerAddrLine',
  kind: 'OB.UI.SelectButton',
  classes: 'btnselect-customer',
  components: [{
    name: 'line',
    style: 'line-height: 23px;',
    components: [{
      style: 'float: left; font-weight: bold;',
      name: 'identifier'
    }, {
      style: 'clear: both;'
    }]
  }],
  create: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('name'));
  }
});

/*Search Customer Button*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.SearchCustomerButton',
  kind: 'OB.UI.Button',
  events: {
    onSearchAction: ''
  },
  classes: 'btnlink-left-toolbar',
  searchAction: function (params) {
    this.doSearchAction({
      locName: params.initial,
      operator: params.operator
    });
  },
  initComponents: function () {
    this.inherited(arguments);
    this.setContent(this.i18nLabel ? OB.I18N.getLabel(this.i18nLabel) : this.label);
  }
});

/*scrollable table (body of customer)*/
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerAddress',
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
    style: 'float: left; width: 100%;',
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
    criteria.name = {
      operator: OB.Dal.CONTAINS,
      value: filter
    };
    criteria.bpartner = this.bPartnerId;
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
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.casbody',
  components: [{
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.ListCustomerAddress',
    style: 'padding: 15px; padding-top: 0px;'
  }]
});

//Modal pop up
enyo.kind({
  name: 'OB.OBPOSPointOfSale.UI.customeraddr.cas',
  kind: 'OB.UI.Subwindow',
  events: {
    onSearchAction: ''
  },
  beforeSetShowing: function (params) {
    if (params.bPartner) {
      var listCustAddr = this.$.subWindowBody.$.casbody.$.listCustomerAddress;
      listCustAddr.setBPartnerId(params.bPartner);

      OB.Dal.get(OB.Model.BusinessPartner, params.bPartner, function successCallbackBPs(dataBps) {
        listCustAddr.setBPartnerModel(dataBps);
      }, function errorCallback(tx, error) {

      });
    }
    if (this.caller === 'mainSubWindow') {
      this.waterfall('onSearchAction', {
        cleanResults: true
      });
    }
    this.$.subWindowBody.$.casbody.$.listCustomerAddress.$.bpsloclistitemprinter.$.theader.$.modalCustomerAddrScrollableHeader.searchAction();
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
    kind: 'OB.OBPOSPointOfSale.UI.customeraddr.casbody'
  }
});