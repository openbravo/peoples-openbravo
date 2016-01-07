/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
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
  name: 'OB.UI.BPLocation',
  classes: 'btnlink-gray',
  style: 'display: table; float: right;',
  components: [{
    name: 'bottomAddrIcon',
    classes: 'addressbillbutton',
    showing: false
  }, {
    name: 'identifier',
    classes: 'addressbilltext'
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
  tap: function () {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomeraddress',
        args: {
          target: 'order'
        }
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
      this.renderBPLocation(_.isNull(this.order.get('bp').get('locName')) ? OB.I18N.getLabel('OBPOS_LblEmptyAddress') : this.order.get('bp').get('locName'));
    } else {
      this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
    }

    this.order.on('change:bp', function (model) {
      if (model.get('bp')) {
        this.renderBPLocation(_.isNull(model.get('bp').get('locName')) ? OB.I18N.getLabel('OBPOS_LblEmptyAddress') : model.get('bp').get('locName'));
      } else {
        this.renderBPLocation(OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
      }
    }, this);
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.NewCustomerAddressWindowButton',
  events: {
    onChangeSubWindow: '',
    onHideThisPopup: ''
  },
  disabled: false,
  style: 'width: 170px; margin: 0px 5px 8px 19px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblNewCustomerAddress',
  handlers: {
    onSetModel: 'setModel',
    onNewBPLocDisabled: 'doDisableNewBPLoc',
    onSetBusinessPartner: 'setBusinessPartner'
  },
  setModel: function (inSender, inEvent) {
    this.model = inEvent.model;
  },
  doDisableNewBPLoc: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  setBusinessPartner: function (inSender, inEvent) {
    this.bPartner = inEvent.bPartner;
  },
  tap: function (model) {
    if (this.disabled) {
      return true;
    }
    this.doHideThisPopup();
    var me = this;

    function errorCallback(tx, error) {
      OB.error(tx);
      OB.error(error);
    }

    function successCallbackBPs(dataBps) {
      me.doChangeSubWindow({
        newWindow: {
          name: 'customerAddrCreateAndEdit',
          params: {
            navigateOnClose: 'mainSubWindow',
            businessPartner: dataBps
          }
        }
      });
    }

    if (this.bPartner) {
      successCallbackBPs(this.bPartner);
    } else {
      OB.Dal.get(OB.Model.BusinessPartner, this.model.get('order').get('bp').get('id'), successCallbackBPs, errorCallback);
    }
  },
  putDisabled: function (status) {
    if (status === false) {
      this.disabled = false;
      this.setDisabled(false);
      this.removeClass('disabled');
      return;
    }
    this.disabled = true;
    this.setDisabled(true);
    this.addClass('disabled');
  }
});

enyo.kind({
  kind: 'OB.UI.Button',
  name: 'OB.UI.SearchCustomerAddressWindowButton',
  style: 'width: 170px; margin: 0px 0px 8px 5px;',
  classes: 'btnlink-yellow btnlink btnlink-small',
  i18nLabel: 'OBPOS_LblAdvancedSearch',
  disabled: false,
  handlers: {
    onSetModel: 'setModel',
    onNewBPLocDisabled: 'doDisableNewBPLoc',
    onSetBusinessPartner: 'setBusinessPartner'
  },
  doDisableNewBPLoc: function (inSender, inEvent) {
    this.putDisabled(inEvent.status);
  },
  setModel: function (inSender, inEvent) {
    this.model = inEvent.model;
  },
  setBusinessPartner: function (inSender, inEvent) {
    this.bPartner = inEvent.bPartner;
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
      name: 'customerAddressSearch',
      params: {
        caller: 'mainSubWindow',
        bPartner: this.bPartner ? this.bPartner.get('id') : this.model.get('order').get('bp').get('id')
      }
    });
  },
  putDisabled: function (status) {
    if (status === false) {
      this.disabled = false;
      this.setDisabled(false);
      this.removeClass('disabled');
      return;
    }
    this.disabled = true;
    this.setDisabled(true);
    this.addClass('disabled');
  },
  initComponents: function () {
    this.inherited(arguments);
    this.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_receipt.customers'));
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpLocScrollableHeader',
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
          name: 'bpsLocationSearchfilterText',
          style: 'width: 100%'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          name: 'bpsLocationSearchClearButton',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          name: 'bpsLocationSearchButton',
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
          kind: 'OB.UI.SearchCustomerAddressWindowButton'
        }]
      }]
    }]
  }],
  clearAction: function () {
    this.$.bpsLocationSearchfilterText.setValue('');
    this.doSearchAction({
      locName: this.$.bpsLocationSearchfilterText.getValue()
    });
    return true;
  },
  searchAction: function () {
    this.doSearchAction({
      locName: this.$.bpsLocationSearchfilterText.getValue()
    });
    return true;
  }
});


enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocDetailsContextMenuItem',
  i18NLabel: 'OBPOS_BPViewDetails',
  selectItem: function (bploc) {
    var me = this;
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });
    OB.MobileApp.view.$.containerWindow.getRoot().model.attributes.subWindowManager.set('currentWindow', {
      name: 'customerAddressView',
      params: {
        businessPartner: me.owner.owner.bPartner,
        bPLocation: bploc,
        navigateOnClose: 'mainSubWindow'
      }
    });
    return true;
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocEditContextMenuItem',
  i18NLabel: 'OBPOS_BPEdit',
  selectItem: function (bploc) {
    var me = this;
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });
    OB.MobileApp.view.$.containerWindow.getRoot().model.attributes.subWindowManager.set('currentWindow', {
      name: 'customerAddrCreateAndEdit',
      params: {
        businessPartner: me.owner.owner.bPartner,
        bPLocation: bploc,
        navigateOnClose: 'mainSubWindow'
      }
    });
    return true;
  },
  create: function () {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenu',
  name: 'OB.UI.BPLocationContextMenu',
  initComponents: function () {
    this.inherited(arguments);
    var menuOptions = [],
        extraOptions = OB.MobileApp.model.get('extraBPLocContextMenuOptions') || [];

    menuOptions.push({
      kind: 'OB.UI.BPLocDetailsContextMenuItem',
      permission: 'OBPOS_receipt.customers'
    }, {
      kind: 'OB.UI.BPLocEditContextMenuItem',
      permission: 'OBPOS_retail.editCustomers'
    });

    menuOptions = menuOptions.concat(extraOptions);
    this.$.menu.setItems(menuOptions);
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLocLine',
  kind: 'OB.UI.SelectButton',
  components: [{
    name: 'line',
    style: 'line-height: 30px; width: 100%',
    components: [{
      name: 'textInfo',
      style: 'float: left; ',
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
    }, {
      kind: 'OB.UI.BPLocationContextMenu',
      name: 'btnContextMenu',
      style: 'float: right'
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
    var locId = this.owner.owner.owner.owner.bPartner.get('locId');
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
    // Context menu
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.setModel(this.model);
      this.$.btnContextMenu.bPartner = this.owner.owner.owner.owner.bPartner;
    }
  }
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBpsLoc',
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
          renderHeader: 'OB.UI.ModalBpLocScrollableHeader',
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
    criteria.bpartner = this.bPartner.get('id');
    criteria.isBillTo = true;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var filterIdentifier = {
        columns: ['_filter'],
        operator: 'startsWith',
        value: filter
      },
          bPartnerId = {
          columns: ['bpartner'],
          operator: 'equals',
          value: this.bPartner.get('id'),
          isId: true
          },
          isBillTo = {
          columns: ['isBillTo'],
          operator: 'equals',
          value: true,
          boolean: true
          };
      var remoteCriteria = [filterIdentifier, bPartnerId, isBillTo];
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
        dataBps.set('locId', model.get('id'));
        dataBps.set('locName', model.get('name'));
        me.doChangeBusinessPartner({
          businessPartner: dataBps,
          target: me.owner.owner.args.target
        });
      }
      if (!model.get('ignoreSetBPLoc')) {
        OB.Dal.get(OB.Model.BusinessPartner, this.bPartner.get('id'), successCallbackBPs, errorCallback);
      }
    }, this);
  }
});

/*Modal definiton*/
enyo.kind({
  name: 'OB.UI.ModalBPLocation',
  topPosition: '125px',
  kind: 'OB.UI.Modal',
  executeOnShow: function () {
    if (_.isUndefined(this.args.visibilityButtons)) {
      this.args.visibilityButtons = true;
    }
    this.waterfall('onSetShow', {
      visibility: this.args.visibilityButtons
    });
    this.waterfall('onSetBusinessPartner', {
      bPartner: this.args.businessPartner ? this.args.businessPartner : this.model.get('order').get('bp')
    });
    this.bubble('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    this.changedTitle(this.args.businessPartner ? this.args.businessPartner : this.model.get('order').get('bp'));
    this.$.body.$.listBpsLoc.setBPartner(this.args.businessPartner ? this.args.businessPartner : this.model.get('order').get('bp'));
    this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
    this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.$.newAction.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    return true;
  },
  executeOnHide: function () {
    this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.$.bpsLocationSearchfilterText.setValue('');
  },
  changedTitle: function (bp) {
    var me = this,
        criteria = {};

    function successCallbackBPsLoc(dataBps) {
      if (dataBps && dataBps.length > 1) {
        me.$.header.setContent(OB.I18N.getLabel('OBPOS_LblAssignCustomerBillAddress'));
      } else if (dataBps.models[0].get('isBillTo') && dataBps.models[0].get('isShipTo')) {
        me.$.header.setContent(OB.I18N.getLabel('OBPOS_LblAssignCustomerAddress'));
      } else {
        me.$.header.setContent(OB.I18N.getLabel('OBPOS_LblAssignCustomerBillAddress'));
      }
    }

    function errorCallback(tx, error) {
      OB.UTIL.showError("OBDAL error: " + error);
    }
    criteria.bpartner = bp.get('id');
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      var bPartnerId = {
        columns: ['bpartner'],
        operator: 'equals',
        value: bp.get('id'),
        isId: true
      };
      var remoteCriteria = [bPartnerId];
      criteria.remoteFilters = remoteCriteria;
    }
    OB.Dal.find(OB.Model.BPLocation, criteria, successCallbackBPsLoc, errorCallback);
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListBpsLoc'
  },
  init: function (model) {
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });

  }
});