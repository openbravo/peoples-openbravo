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

/*global enyo, Backbone, _ */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.BPLocationShip',
  classes: 'btnlink-gray',
  showing: false,
  style: 'display: table; float: right;',
  components: [{
    name: 'bottomAddrIcon',
    style: 'display: table-cell; background-image: url(img/ShippingAddress.png); background-repeat: no-repeat; background-size: 100%; width: 25px;'
  }, {
    name: 'identifier',
    style: 'display: table-cell; text-overflow:ellipsis; white-space: nowrap; overflow: hidden; max-width: 70px;'
  }, {
    style: 'clear: both;'
  }],
  published: {
    order: null
  },
  events: {
    onShowPopup: ''
  },
  handlers: {
    onBPLocSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function (inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
    if (!this.isEnabled) {
      this.removeClass('btnlink');
      this.addClass('btnbp');
    } else {
      this.removeClass('btnbp');
      this.addClass('btnlink');
    }
  },
  chagedStyle: function (status) {
    var me = this;
    if (!status) {
      me.setShowing(status);
      me.parent.$.bplocbutton.$.bottomAddrIcon.applyStyle('display', 'none');
      me.parent.$.bplocbutton.$.identifier.applyStyle('max-width', '200px');
    } else {
      me.setShowing(status);
      me.parent.$.bplocbutton.$.bottomAddrIcon.applyStyle('display', 'table-cell');
      me.parent.$.bplocbutton.$.identifier.applyStyle('max-width', '70px');
    }
  },
  buttonShowing: function (bp) {
    var criteria = {},
        me = this;
    if (!bp.get('locShipId') && !bp.get('locId')) {
      me.chagedStyle(false);
    } else if (bp.get('locShipId') === bp.get('locId')) {
      criteria.bpartner = bp.get('id');
      OB.Dal.find(OB.Model.BPLocation, criteria, function (dataBps) {
        if (dataBps && dataBps.length > 1) {
          me.chagedStyle(true);
        } else {
          me.chagedStyle(false);
        }
      }, function (tx, error) {
        OB.UTIL.showError("OBDAL error: " + error);
      });
    } else {
      me.chagedStyle(true);
    }
  },
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomershipaddress'
      });
    }
  },
  initComponents: function () {
    this.inherited(arguments);
  },
  renderBPLocation: function (newLocation) {
    this.$.identifier.setContent(newLocation);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderBPLocation(_.isNull(this.order.get('bp').get('locShipName')) ? OB.I18N.getLabel('OBPOS_LblEmptyAddress') : this.order.get('bp').get('locShipName'));
    } else {
      this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.buttonShowing(model.get('bp'));
        this.renderBPLocation(_.isNull(model.get('bp').get('locShipName')) ? OB.I18N.getLabel('OBPOS_LblEmptyAddress') : model.get('bp').get('locShipName'));
      } else {
        this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
      }
    }, this);
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.SearchCustomerShipAddressWindowButton',
  style: 'width: 170px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblEditAddress',
  disabled: false,
  handlers: {
    onSetModel: 'setModel',
    onNewBPLocDisabled: 'doDisableNewBPLoc'
  },
  doDisableNewBPLoc: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  setModel: function (inSender, inEvent) {
    this.model = inEvent.model;
  },
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    this.model.get('subWindowManager').set('currentWindow', {
      name: 'customerShipAddressSearch',
      params: {
        caller: 'mainSubWindow',
        bPartner: this.model.get('order').get('bp').get('id')
      }
    });
  },
  putDisabled: function (status) {
    if (status === false) {
      this.disabled = false;
      this.setDisabled(false);
      this.removeClass('disabled');
      return;
    } else {
      this.disabled = true;
      this.setDisabled(true);
      this.addClass('disabled');
    }
  },
  initComponents: function () {
    this.inherited(arguments);
    this.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_receipt.customers'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpShipLocScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [{
    style: 'padding: 10px;',
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell; width: 100%;',
        components: [{
          kind: 'OB.UI.SearchInputAutoFilter',
          name: 'filterText',
          style: 'width: 100%'
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
  }, {
    style: 'padding: 10px;',
    showing: true,
    handlers: {
      onSetShow: 'setShow'
    },
    setShow: function (inSender, inEvent) {
      this.setShowing(inEvent.visibility);
    },
    components: [{
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.NewCustomerAddressWindowButton',
          name: 'newAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SearchCustomerShipAddressWindowButton'
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.$.filterText.setValue('');
    this.doSearchAction({
      locName: this.$.filterText.getValue()
    });
    return true;
  },
  searchAction: function () {
    this.doSearchAction({
      locName: this.$.filterText.getValue()
    });
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsShipLocLine',
  kind: 'OB.UI.SelectButton',
  components: [{
    name: 'line',
    style: 'line-height: 30px;',
    components: [{
      style: 'display: table;',
      components: [{
        name: 'identifier',
        style: 'display: table-cell;'
      }, {
        name: 'bottomShipIcon'
      }, {
        name: 'bottomBillIcon'
      }, {
        style: 'clear: both;'
      }]
    }]
  }],
  events: {
    onHideThisPopup: ''
  },
  tap: function () {
    this.inherited(arguments);
    this.doHideThisPopup();
  },
  create: function () {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('name'));
    var locId = this.owner.owner.owner.owner.bPartner.get('locShipId');
    if (locId === this.model.get('id')) {
      this.applyStyle('background-color', '#fbf6d1');
    }
    if (this.model.get('isBillTo') && this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('addresshipitems');
      this.$.bottomBillIcon.addClass('addressbillitems');
    } else if (this.model.get('isBillTo')) {
      this.$.bottomBillIcon.addClass('addressbillitems');
    } else if (this.model.get('isShipTo')) {
      this.$.bottomShipIcon.addClass('addresshipitems');
    }
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBpsShipLoc',
  classes: 'row-fluid',
  published: {
    bPartner: null
  },
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeBusinessPartner: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      style: 'border-bottom: 1px solid #cccccc;',
      classes: 'row-fluid',
      components: [{
        classes: 'span12',
        components: [{
          name: 'bpsloclistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '400px',
          renderHeader: 'OB.UI.ModalBpShipLocScrollableHeader',
          renderLine: 'OB.UI.ListBpsShipLocLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }]
      }]
    }]
  }],
  clearAction: function (inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function (inSender, inEvent) {
    var me = this,
        criteria = {},
        filter = inEvent.locName;

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }

    function successCallbackBPsLoc(dataBps) {
      if (dataBps && dataBps.length > 0) {
        me.bpsList.reset(dataBps.models);
      } else {
        me.bpsList.reset();
      }
    }
    criteria.name = {
      operator: OB.Dal.CONTAINS,
      value: filter
    };
    criteria.bpartner = this.bPartner.get('id');
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
          value: this.bPartner.get('id'),
          isId: true
          };
      var remoteCriteria = [filterIdentifier, bPartnerId];
      criteria.remoteFilters = remoteCriteria;
    }
    OB.Dal.find(OB.Model.BPLocation, criteria, successCallbackBPsLoc, errorCallback);
    return true;
  },
  bpsList: null,
  init: function (model) {
    this.bpsList = new Backbone.Collection();
    this.$.bpsloclistitemprinter.setCollection(this.bpsList);
    this.bpsList.on('click', function (model) {
      var me = this;

      function errorCallback(tx, error) {
        OB.error(tx);
        OB.error(error);
      }

      function successCallbackBPs(dataBps) {
        dataBps.set('locShipId', model.get('id'));
        dataBps.set('locShipName', model.get('name'));
        dataBps.set('locationModel', model);
        dataBps.set('postalCode', model.get('postalCode'));
        dataBps.set('cityName', model.get('cityName'));
        dataBps.set('countryName', model.get('countryName'));
        me.doChangeBusinessPartner({
          businessPartner: dataBps
        });
      }
      OB.Dal.get(OB.Model.BusinessPartner, this.bPartner.get('id'), successCallbackBPs, errorCallback);
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBPLocationShip',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnShow: function () {
    if (_.isUndefined(this.args.visibilityButtons)) {
      this.args.visibilityButtons = true;
    }
    this.waterfall('onSetShow', {
      visibility: this.args.visibilityButtons
    });
    this.$.body.$.listBpsShipLoc.setBPartner(this.model.get('order').get('bp'));
    this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpShipLocScrollableHeader.searchAction();
    this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpShipLocScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    return true;
  },
  executeOnHide: function () {
    this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpShipLocScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignCustomerShipAddress',
  body: {
    kind: 'OB.UI.ListBpsShipLoc'
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });

  }
});