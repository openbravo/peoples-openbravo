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
  style: 'float: right;width: 120px;text-overflow:ellipsis;white-space: nowrap;overflow: hidden;padding-left: -;padding-left: 0px;margin-left: 0px;margin-bottom: 0px;padding-bottom: 0px;margin-top: 10px; padding-right: 0px;',
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
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomershipaddress'
      });
    }
  },
  initComponents: function () {},
  renderBPLocation: function (newLocation) {
    this.setContent(newLocation);
  },
  orderChanged: function (oldValue) {
    if (this.order.get('bp')) {
      this.renderBPLocation(_.isNull(this.order.get('bp').get('locShipName')) ? OB.I18N.getLabel('OBPOS_LblEmptyAddress') : this.order.get('bp').get('locShipName'));
    } else {
      this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderBPLocation(_.isNull(model.get('bp').get('locShipName')) ? this.drawingImg(OB.I18N.getLabel('OBPOS_LblEmptyAddress')) : this.drawingImg(model.get('bp').get('locShipName')));
      } else {
        this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
      }
    }, this);
  },
  drawingImg: function (name) {
    var components = this.getComponents();
    if (components.length > 0) {
      components[0].destroy();
    }
    this.createComponent({
      classes: 'addressitems',
      tag: 'div',
      allowHtml: true,
      content: '<img class="addressimage" src="img/ShippingAddress.png"/><div class="addressitemstext">' + name + '</div>'
    });
    this.render();
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

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBpsShipLoc',
  classes: 'row-fluid',
  published: {
    bPartnerId: null
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
          renderLine: 'OB.UI.ListBpsLocLine',
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
        if (model.get('isBillTo') && model.get('isShipTo')) {
          dataBps.set('locId', model.get('id'));
          dataBps.set('locName', model.get('name'));
          dataBps.set('locShipId', model.get('id'));
          dataBps.set('locShipName', model.get('name'));
        } else if (model.get('isBillTo')) {
          dataBps.set('locId', model.get('id'));
          dataBps.set('locName', model.get('name'));
        } else if (model.get('isShipTo')) {
          dataBps.set('locShipId', model.get('id'));
          dataBps.set('locShipName', model.get('name'));
        }
        dataBps.set('locationModel', model);
        dataBps.set('postalCode', model.get('postalCode'));
        dataBps.set('cityName', model.get('cityName'));
        dataBps.set('countryName', model.get('countryName'));
        me.doChangeBusinessPartner({
          businessPartner: dataBps
        });
      }
      OB.Dal.get(OB.Model.BusinessPartner, this.bPartnerId, successCallbackBPs, errorCallback);
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBPLocationShip',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnShow: function () {
    this.$.body.$.listBpsShipLoc.setBPartnerId(this.model.get('order').get('bp').get('id'));
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