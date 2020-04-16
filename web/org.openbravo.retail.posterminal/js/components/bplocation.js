/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

enyo.kind({
  kind: 'OB.UI.FormElement.Selector',
  name: 'OB.UI.SmallBPButton',
  classes: 'obUiSmallBPButton',
  published: {
    order: null
  },
  handlers: {
    onBPLocSelectionDisabled: 'buttonDisabled'
  },
  buttonDisabled: function(inSender, inEvent) {
    this.isEnabled = !inEvent.status;
    this.setDisabled(inEvent.status);
  },
  initComponents: function() {
    this.inherited(arguments);
  },
  renderBPLocation: function(newLocationId, newLocationName) {
    this.setValue(newLocationId, newLocationName);
  },
  orderChanged: function(oldValue) {
    if (this.order.get('bp')) {
      this.renderBPLocation(
        _.isNull(this.order.get('bp').get(this.locName))
          ? null
          : this.order.get('bp').get(this.locId),
        _.isNull(this.order.get('bp').get(this.locName))
          ? OB.I18N.getLabel('OBPOS_LblEmptyAddress')
          : this.order.get('bp').get(this.locName)
      );
    } else {
      this.renderBPLocation(null, OB.I18N.getLabel('OBPOS_LblEmptyAddress'));
    }

    this.order.on(
      'change:bp',
      function(model) {
        if (model.get('bp')) {
          if (this.buttonShowing) {
            this.buttonShowing(model.get('bp'));
          }
          this.renderBPLocation(
            _.isNull(model.get('bp').get(this.locName))
              ? null
              : model.get('bp').get(this.locId),
            _.isNull(model.get('bp').get(this.locName))
              ? OB.I18N.getLabel('OBPOS_LblEmptyAddress')
              : model.get('bp').get(this.locName)
          );
        } else {
          this.renderBPLocation(
            null,
            OB.I18N.getLabel('OBPOS_LblEmptyAddress')
          );
        }
      },
      this
    );
  }
});

enyo.kind({
  kind: 'OB.UI.SmallBPButton',
  name: 'OB.UI.BPLocation',
  classes: 'obUiBPLocation',
  locName: 'locName',
  locId: 'locId',
  events: {
    onShowPopup: ''
  },
  tap: function() {
    var bp = OB.MobileApp.model.receipt.get('bp');
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomeraddress',
        args: {
          locationButton: true,
          target: 'order',
          clean: true,
          manageAddress: bp.get('locId') === bp.get('shipLocId'),
          navigationPath: []
        }
      });
    }
  }
});

enyo.kind({
  kind: 'OB.UI.SmallBPButton',
  name: 'OB.UI.BPLocationShip',
  classes: 'obUiBPLocationShip',
  locName: 'shipLocName',
  locId: 'shipLocId',
  events: {
    onShowPopup: ''
  },
  changeStyle: function(status) {
    var me = this;
    me.formElement.setShowing(status);
    if (!status) {
      me.formElement.parent.$.formElementBplocbutton.setLabel(
        OB.I18N.getLabel('OBPOS_LblAddress')
      );
      me.formElement.parent.$.formElementBplocbutton.addClass(
        'obUiOrderHeader-buttonComponents-formElementBplocbutton_noicon'
      );
    } else {
      me.formElement.parent.$.formElementBplocbutton.setLabel(
        OB.I18N.getLabel('OBPOS_LblBillAddr')
      );
      me.formElement.parent.$.formElementBplocbutton.removeClass(
        'obUiOrderHeader-buttonComponents-formElementBplocbutton_noicon'
      );
    }
  },
  buttonShowing: function(bp) {
    var criteria = {},
      me = this;

    function successLocations(dataBps) {
      if (bp.get('shipLocId') === bp.get('locId')) {
        me.changeStyle(false);
      } else if (dataBps && dataBps.length > 1) {
        me.changeStyle(true);
      } else if (
        dataBps &&
        _.isArray(dataBps) &&
        dataBps[0] &&
        ((dataBps[0].get('isBillTo') && !dataBps[0].get('isShipTo')) ||
          (!dataBps[0].get('isBillTo') && dataBps[0].get('isShipTo')))
      ) {
        me.changeStyle(true);
      } else {
        me.changeStyle(false);
      }
    }

    if (!bp.get('shipLocId') && !bp.get('locId')) {
      me.changeStyle(false);
    } else {
      if (
        bp.get('locations') &&
        bp.get('locations').length > 0 &&
        bp.get('locations')[0].attributes
      ) {
        successLocations(bp.get('locations'));
      } else {
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
        OB.Dal.find(
          OB.Model.BPLocation,
          criteria,
          function(locations) {
            successLocations(locations.models);
          },
          function(tx, error) {
            OB.UTIL.showError(error);
          }
        );
      }
    }
  },
  tap: function() {
    if (!this.disabled) {
      this.doShowPopup({
        popup: 'modalcustomershipaddress',
        args: {
          target: 'order',
          clean: true,
          navigationPath: []
        }
      });
    }
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.NewCustomerAddressWindowButton',
  events: {
    onChangeSubWindow: '',
    onShowPopup: '',
    onHideSelector: ''
  },
  disabled: false,
  classes: 'obUiNewCustomerAddressWindowButton',
  i18nLabel: 'OBPOS_LblNewCustomerAddress',
  handlers: {
    onSetModel: 'setModel',
    onNewBPLocDisabled: 'doDisableNewBPLoc',
    onSetBusinessPartner: 'setBusinessPartner'
  },
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
  },
  setModel: function(inSender, inEvent) {
    this.model = inEvent.model;
    return true;
  },
  doDisableNewBPLoc: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
    return true;
  },
  setBusinessPartner: function(inSender, inEvent) {
    this.bPartner = inEvent.bPartner;
    return true;
  },
  tap: function(model) {
    if (this.disabled) {
      return true;
    }
    this.doHideSelector({
      selectorHide: true
    });
    var me = this;

    function errorCallback(tx, error) {
      OB.error(tx);
      OB.error(error);
    }

    function successCallbackBPs(dataBps) {
      var navigationPath;
      if (me.popup.kind === 'OB.UI.ModalBPLocation') {
        navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          me.popup.args.navigationPath,
          'modalcustomeraddress'
        );
      } else {
        navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          me.popup.args.navigationPath,
          'modalcustomershipaddress'
        );
      }
      me.doShowPopup({
        popup: 'customerAddrCreateAndEdit',
        args: {
          businessPartner: dataBps,
          target: me.popup.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
            navigationPath,
            'customerAddressView'
          ),
          cancelNavigationPath: navigationPath
        }
      });
    }

    if (this.bPartner) {
      successCallbackBPs(this.bPartner);
    } else {
      OB.Dal.get(
        OB.Model.BusinessPartner,
        this.model
          .get('order')
          .get('bp')
          .get('id'),
        successCallbackBPs,
        errorCallback
      );
    }
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpLocScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalBpLocScrollableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
    onSearchActionByKey: 'searchAction',
    onFiltered: 'searchAction'
  },
  components: [
    {
      classes: 'obUiModalBpLocScrollableHeader-container1',
      components: [
        {
          classes: 'obUiModalBpLocScrollableHeader-container1-container1',
          components: [
            {
              classes:
                'obUiModalBpLocScrollableHeader-container1-container1-container1',
              components: [
                {
                  kind: 'OB.UI.FormElement',
                  name: 'formElementBpsLocationSearchfilterText',
                  classes:
                    'obUiFormElement_dataEntry obUiModalBpLocScrollableHeader-container1-container1-container1-formElementBpsLocationSearchfilterText',
                  coreElement: {
                    kind: 'OB.UI.SearchInputAutoFilter',
                    name: 'bpsLocationSearchfilterText',
                    i18nLabel: 'OBMOBC_LblSearchEllipsis',
                    classes:
                      'obUiModalBpLocScrollableHeader-container1-container1-container1-bpsLocationSearchfilterText'
                  }
                }
              ]
            },
            {
              classes:
                'obUiModalBpLocScrollableHeader-container1-container1-container2',
              components: [
                {
                  kind: 'OB.UI.Button',
                  classes:
                    'obUiSearchClearButton obUiModalBpLocScrollableHeader-container1-container1-container2-bpsLocationSearchClearButton',
                  i18nLabel: 'OBMOBC_LblClear',
                  name: 'bpsLocationSearchClearButton',
                  ontap: 'clearAction'
                }
              ]
            },
            {
              classes:
                'obUiModalBpLocScrollableHeader-container1-container1-container3',
              components: [
                {
                  kind: 'OB.UI.Button',
                  classes:
                    'obUiSearchLaunchButton obUiModalBpLocScrollableHeader-container1-container1-container3-bpsLocationSearchButton',
                  i18nLabel: 'OBMOBC_LblSearch',
                  name: 'bpsLocationSearchButton',
                  ontap: 'searchAction'
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  clearAction: function() {
    this.$.formElementBpsLocationSearchfilterText.coreElement.setValue('');
    this.doSearchAction({
      locName: this.$.formElementBpsLocationSearchfilterText.coreElement.getValue()
    });
    return true;
  },
  searchAction: function() {
    this.doSearchAction({
      locName: this.$.formElementBpsLocationSearchfilterText.coreElement.getValue()
    });
    return true;
  }
});

enyo.kind({
  name: 'OB.UI.ModalBpLocFooter',
  classes: 'obUiModalBpLocFooter',
  components: [
    {
      classes: 'obUiModalBpLocFooter-container1',
      showing: true,
      handlers: {
        onSetShow: 'setShow'
      },
      setShow: function(inSender, inEvent) {
        this.setShowing(inEvent.visibility);
        return true;
      },
      components: [
        {
          classes:
            'obUiModal-footer-mainButtons obUiModalBpLocFooter-container1-container1',
          components: [
            {
              kind: 'OB.UI.NewCustomerAddressWindowButton',
              classes: 'obUiModalBpLocFooter-container1-container1-newAction',
              name: 'newAction'
            },
            {
              kind: 'OB.UI.ModalDialogButton',
              classes: 'obUiModalBpLocFooter-container1-container1-close',
              i18nLabel: 'OBRDM_LblClose',
              isDefaultAction: true,
              tap: function() {
                if (this.disabled === false) {
                  this.doHideThisPopup();
                }
              }
            }
          ]
        }
      ]
    }
  ]
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocDetailsContextMenuItem',
  i18NLabel: 'OBPOS_BPViewDetails',
  classes: 'obUiBPLocDetailsContextMenuItem',
  selectItem: function(bploc) {
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });
    var contextMenu = this.owner.owner;
    contextMenu.dialog.menuSelected = true;
    contextMenu.dialog.owner.owner.selectorHide = true;
    contextMenu.dialog.owner.owner.hide();
    contextMenu.dialog.bubble('onShowPopup', {
      popup: 'customerAddressView',
      args: {
        businessPartner: contextMenu.bPartner,
        bPLocation: bploc,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          contextMenu.dialog.owner.owner.args.navigationPath,
          contextMenu.owner.locId === 'locId'
            ? 'modalcustomeraddress'
            : 'modalcustomershipaddress'
        ),
        target: contextMenu.dialog.target
      }
    });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocEditContextMenuItem',
  i18NLabel: 'OBPOS_BPEdit',
  classes: 'obUiBPLocEditContextMenuItem',
  selectItem: function(bploc) {
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });
    var contextMenu = this.owner.owner,
      returnTo =
        contextMenu.dialog.kind === 'OB.UI.ListBpsShipLoc'
          ? 'modalcustomershipaddress'
          : 'modalcustomeraddress',
      navigationPath = OB.UTIL.BusinessPartnerSelector.cloneAndPush(
        contextMenu.dialog.owner.owner.args.navigationPath,
        returnTo
      );

    contextMenu.dialog.menuSelected = true;
    contextMenu.dialog.owner.owner.selectorHide = true;
    contextMenu.dialog.owner.owner.hide();
    contextMenu.dialog.bubble('onShowPopup', {
      popup: 'customerAddrCreateAndEdit',
      args: {
        businessPartner: contextMenu.bPartner,
        bPLocation: bploc,
        target: contextMenu.dialog.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          navigationPath,
          'customerAddressView'
        ),
        cancelNavigationPath: navigationPath
      }
    });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocAssignToReceiptContextMenuItem',
  i18NLabel: 'OBPOS_BPLocAssignToReceipt',
  classes: 'obUiBPLocAssignToReceiptContextMenuItem',
  selectItem: function(bploc) {
    var contextMenu = this.owner.owner;
    contextMenu.dialog.menuSelected = true;
    if (contextMenu.dialog.owner) {
      contextMenu.dialog.owner.owner.selectorHide = true;
    }
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });

    function errorCallback(tx, error) {
      OB.error(tx);
      OB.error(error);
    }

    function successCallbackBPs(dataBps) {
      dataBps.set('locationModel', bploc);
      dataBps.set('locId', bploc.get('id'));
      dataBps.set('locName', bploc.get('name'));
      dataBps.set('postalCode', bploc.get('postalCode'));
      dataBps.set('cityName', bploc.get('cityName'));
      dataBps.set('countryName', bploc.get('countryName'));

      dataBps.set('shipLocId', bploc.get('id'));
      dataBps.set('shipLocName', bploc.get('name'));
      dataBps.set('shipPostalCode', bploc.get('postalCode'));
      dataBps.set('shipCityName', bploc.get('cityName'));
      dataBps.set('shipCountryName', bploc.get('countryName'));
      dataBps.set('shipRegionId', bploc.get('regionId'));
      dataBps.set('shipCountryId', bploc.get('countryId'));

      contextMenu.dialog.component.doChangeBusinessPartner({
        businessPartner: dataBps,
        target: 'order'
      });
    }
    OB.Dal.get(
      OB.Model.BusinessPartner,
      contextMenu.bPartner.get('id'),
      successCallbackBPs,
      errorCallback
    );
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocAssignToReceiptShippingContextMenuItem',
  i18NLabel: 'OBPOS_BPLocAssignToReceiptShipping',
  classes: 'obUiBPLocAssignToReceiptShippingContextMenuItem',
  selectItem: function(bploc) {
    var contextMenu = this.owner.owner;
    contextMenu.dialog.menuSelected = true;
    if (contextMenu.dialog.owner) {
      contextMenu.dialog.owner.owner.selectorHide = true;
    }
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });

    function errorCallback(tx, error) {
      OB.error(tx);
      OB.error(error);
    }

    function successCallbackBPs(dataBps) {
      dataBps.set('locationModel', bploc);
      dataBps.set('shipLocId', bploc.get('id'));
      dataBps.set('shipLocName', bploc.get('name'));
      dataBps.set('shipPostalCode', bploc.get('postalCode'));
      dataBps.set('shipCityName', bploc.get('cityName'));
      dataBps.set('shipCountryName', bploc.get('countryName'));
      dataBps.set('shipRegionId', bploc.get('regionId'));
      dataBps.set('shipCountryId', bploc.get('countryId'));

      //Keep the other address:
      if (
        !contextMenu.dialog.manageAddress &&
        !contextMenu.bPartner.get('locId')
      ) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_BPartnerNoInvoiceAddress', [
            contextMenu.bPartner.get('_identifier')
          ])
        );
        return;
      }
      dataBps.set('locId', contextMenu.bPartner.get('locId'));
      dataBps.set('locName', contextMenu.bPartner.get('locName'));
      dataBps.set('postalCode', contextMenu.bPartner.get('postalCode'));
      dataBps.set('cityName', contextMenu.bPartner.get('cityName'));
      dataBps.set('countryName', contextMenu.bPartner.get('countryName'));

      contextMenu.dialog.component.doChangeBusinessPartner({
        businessPartner: dataBps,
        target: 'order'
      });
    }

    OB.Dal.get(
      OB.Model.BusinessPartner,
      contextMenu.bPartner.get('id'),
      successCallbackBPs,
      errorCallback
    );
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenuItem',
  name: 'OB.UI.BPLocAssignToReceiptInvoicingContextMenuItem',
  classes: 'obUiBPLocAssignToReceiptInvoicingContextMenuItem',
  i18NLabel: 'OBPOS_BPLocAssignToReceiptInvoicing',
  selectItem: function(bploc) {
    var contextMenu = this.owner.owner;
    contextMenu.dialog.menuSelected = true;
    if (contextMenu.dialog.owner) {
      contextMenu.dialog.owner.owner.selectorHide = true;
    }
    bploc.set('ignoreSetBPLoc', true, {
      silent: true
    });

    function errorCallback(tx, error) {
      OB.error(tx);
      OB.error(error);
    }

    function successCallbackBPs(dataBps) {
      dataBps.set('locId', bploc.get('id'));
      dataBps.set('locName', bploc.get('name'));
      dataBps.set('postalCode', bploc.get('postalCode'));
      dataBps.set('cityName', bploc.get('cityName'));
      dataBps.set('countryName', bploc.get('countryName'));

      // Keep the other address:
      if (
        !contextMenu.dialog.manageAddress &&
        !contextMenu.bPartner.get('shipLocId')
      ) {
        OB.UTIL.showError(
          OB.I18N.getLabel('OBPOS_BPartnerNoShippingAddress', [
            contextMenu.bPartner.get('_identifier')
          ])
        );
        return;
      }
      if (!bploc.get('onlyOneAddress')) {
        dataBps.set('shipLocId', contextMenu.bPartner.get('shipLocId'));
        dataBps.set('shipLocName', contextMenu.bPartner.get('shipLocName'));
        dataBps.set(
          'shipPostalCode',
          contextMenu.bPartner.get('shipPostalCode')
        );
        dataBps.set('shipCityName', contextMenu.bPartner.get('shipPostalCode'));
        dataBps.set(
          'shipCountryName',
          contextMenu.bPartner.get('shipCountryName')
        );
      }

      contextMenu.dialog.component.doChangeBusinessPartner({
        businessPartner: dataBps,
        target: 'order'
      });
    }
    OB.Dal.get(
      OB.Model.BusinessPartner,
      contextMenu.bPartner.get('id'),
      successCallbackBPs,
      errorCallback
    );
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  kind: 'OB.UI.ListContextMenu',
  name: 'OB.UI.BPLocationContextMenu',
  classes: 'obUiBPLocationContextMenu',
  initComponents: function() {
    this.inherited(arguments);
    var menuOptions = [],
      bpLoc = this.owner.model,
      extraOptions =
        OB.MobileApp.model.get('extraBPLocContextMenuOptions') || [];

    menuOptions.push(
      {
        kind: 'OB.UI.BPLocDetailsContextMenuItem',
        permission: 'OBPOS_receipt.customers',
        classes: 'obUiBPLocationContextMenu-BPLocDetailsContextMenuItem'
      },
      {
        kind: 'OB.UI.BPLocEditContextMenuItem',
        permission: 'OBPOS_retail.editCustomerLocationButton',
        classes: 'obUiBPLocationContextMenu-BPLocDetailsContextMenuItem'
      }
    );

    if (bpLoc.get('isBillTo') && bpLoc.get('isShipTo')) {
      menuOptions.push({
        kind: 'OB.UI.BPLocAssignToReceiptContextMenuItem',
        permission: 'OBPOS_retail.assignToReceiptAddress',
        classes: 'obUiBPLocationContextMenu-BPLocAssignToReceiptContextMenuItem'
      });
    }
    if (
      !bpLoc.get('onlyOneAddress') ||
      !(bpLoc.get('isBillTo') && bpLoc.get('isShipTo'))
    ) {
      if (bpLoc.get('isShipTo')) {
        menuOptions.push({
          kind: 'OB.UI.BPLocAssignToReceiptShippingContextMenuItem',
          permission: 'OBPOS_retail.assignToReceiptShippingAddress',
          classes:
            'obUiBPLocationContextMenu-BPLocAssignToReceiptShippingContextMenuItem'
        });
      }
      if (bpLoc.get('isBillTo')) {
        menuOptions.push({
          kind: 'OB.UI.BPLocAssignToReceiptInvoicingContextMenuItem',
          permission: 'OBPOS_retail.assignToReceiptInvoicingAddress',
          classes:
            'obUiBPLocationContextMenu-BPLocAssignToReceiptInvoicingContextMenuItem'
        });
      }
    }

    menuOptions = menuOptions.concat(extraOptions);
    this.$.menu.setItems(menuOptions);
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsLocLine',
  kind: 'OB.UI.ListSelectorLine',
  classes: 'obUiListSelectorLine',
  locId: 'locId',
  components: [
    {
      name: 'line',
      classes: 'obUiListSelectorLine-line',
      components: [
        {
          name: 'textInfo',
          classes: 'obUiListSelectorLine-line-textInfo',
          components: [
            {
              classes: 'obUiListSelectorLine-line-textInfo-element1',
              components: [
                {
                  classes:
                    'obUiListSelectorLine-line-textInfo-element1-identifier',
                  name: 'identifier'
                },
                {
                  classes:
                    'obUiListSelectorLine-line-textInfo-element1-bottomShipIcon',
                  name: 'bottomShipIcon'
                },
                {
                  classes:
                    'obUiListSelectorLine-line-textInfo-element1-bottomBillIcon',
                  name: 'bottomBillIcon'
                },
                {
                  classes:
                    'obUiListSelectorLine-line-textInfo-element1-element1'
                }
              ]
            }
          ]
        },
        {
          kind: 'OB.UI.BPLocationContextMenu',
          name: 'btnContextMenu',
          classes: 'obUiListSelectorLine-line-btnContextMenu'
        }
      ]
    }
  ],
  canHidePopup: function() {
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.$.identifier.setContent(this.model.get('name'));
    if (
      this.model.get('id') ===
      OB.MobileApp.model.receipt.get('bp').get(this.locId)
    ) {
      this.addClass('obUiListSelectorLine_equalLocId');
    }
    if (this.owner.owner.owner.owner.manageAddress) {
      if (this.model.get('isBillTo') && this.model.get('isShipTo')) {
        this.$.bottomShipIcon.show();
        this.$.bottomBillIcon.show();
      } else if (this.model.get('isBillTo')) {
        this.$.bottomBillIcon.show();
        this.$.bottomShipIcon.hide();
      } else if (this.model.get('isShipTo')) {
        this.$.bottomShipIcon.show();
        this.$.bottomBillIcon.hide();
      }
    } else {
      this.$.bottomShipIcon.hide();
      this.$.bottomBillIcon.hide();
    }
    // Context menu
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.setModel(this.model);
      this.$.btnContextMenu.dialog = this.owner.owner.owner.owner;
      this.$.btnContextMenu.dialog.component = this.owner.owner.owner.owner;
      this.$.btnContextMenu.dialog.menuSelected = false;
      this.$.btnContextMenu.bPartner = new OB.Model.BusinessPartner(
        this.owner.owner.owner.owner.bPartner
      );
    }
  }
});

/* scrollable table (body of modal) */
enyo.kind({
  name: 'OB.UI.ListBpsLoc',
  classes: 'ListBpsLoc row-fluid',
  published: {
    bPartner: null,
    manageAddress: false,
    target: 'order',
    initialLoad: true
  },
  handlers: {
    onSearchAction: 'searchAction',
    onClearAction: 'clearAction'
  },
  events: {
    onChangeFilterSelector: '',
    onChangeBusinessPartner: ''
  },
  components: [
    {
      classes: 'obUiListBpsLoc-container1',
      components: [
        {
          name: 'bpsloclistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          classes: 'obUiListBpsLoc-container1-bpsloclistitemprinter',
          scrollAreaClasses:
            'obUiListBpsLoc-container1-bpsloclistitemprinter_scrollArea',
          renderHeader: 'OB.UI.ModalBpLocScrollableHeader',
          renderLine: 'OB.UI.ListBpsLocLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('searchCustomerAddress');
    var me = this,
      criteria = {},
      filter = inEvent.locName;

    function errorCallback(tx, error) {
      if (
        OB.MobileApp.model.get('permissions') &&
        OB.MobileApp.model.get('permissions')['OBPOS_remote.customer'] &&
        !OB.MobileApp.model.get('connectedToERP')
      ) {
        OB.UTIL.showConfirmation.display(
          'Error',
          OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable')
        );
      } else {
        OB.UTIL.showError(error);
      }
    }

    function successCallbackBPsLoc(dataBps) {
      if (dataBps && dataBps.length > 0) {
        if (me.initialLoad) {
          me.initialLoad = false;
          me.onlyOneAddress = dataBps.length === 1;
        }
        _.each(dataBps.models, function(bp) {
          bp.set('onlyOneAddress', me.onlyOneAddress);
        });
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
    if (!this.manageAddress) {
      criteria.isBillTo = true;
    }
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
        };
      var remoteCriteria = [filterIdentifier, bPartnerId];
      if (!this.manageAddress) {
        remoteCriteria.push({
          columns: ['isBillTo'],
          operator: 'equals',
          value: true,
          boolean: true
        });
      }
      criteria.remoteFilters = remoteCriteria;
    }
    OB.Dal.find(
      OB.Model.BPLocation,
      criteria,
      successCallbackBPsLoc,
      errorCallback
    );
    OB.UTIL.ProcessController.finish('searchCustomerAddress', execution);
    return true;
  },
  bpsList: null,
  init: function(model) {
    this.bpsList = new Backbone.Collection();
    this.$.bpsloclistitemprinter.setCollection(this.bpsList);
    this.bpsList.on(
      'click',
      function(model) {
        var me = this;
        me.owner.owner.selectorHide = true;

        function errorCallback(tx, error) {
          OB.error(tx);
          OB.error(error);
        }

        function successCallbackBPs(dataBps) {
          if (
            OB.MobileApp.model.receipt.get('bp').get('id') === dataBps.get('id')
          ) {
            dataBps.set(
              'locId',
              OB.MobileApp.model.receipt.get('bp').get('locId')
            );
            dataBps.set(
              'locName',
              OB.MobileApp.model.receipt.get('bp').get('locName')
            );
            dataBps.set(
              'postalCode',
              OB.MobileApp.model.receipt.get('bp').get('postalCode')
            );
            dataBps.set(
              'cityName',
              OB.MobileApp.model.receipt.get('bp').get('cityName')
            );
            dataBps.set(
              'countryName',
              OB.MobileApp.model.receipt.get('bp').get('countryName')
            );

            dataBps.set(
              'shipLocId',
              OB.MobileApp.model.receipt.get('bp').get('shipLocId')
            );
            dataBps.set(
              'shipLocName',
              OB.MobileApp.model.receipt.get('bp').get('shipLocName')
            );
            dataBps.set(
              'shipPostalCode',
              OB.MobileApp.model.receipt.get('bp').get('shipPostalCode')
            );
            dataBps.set(
              'shipCityName',
              OB.MobileApp.model.receipt.get('bp').get('shipPostalCode')
            );
            dataBps.set(
              'shipCountryName',
              OB.MobileApp.model.receipt.get('bp').get('shipCountryName')
            );
          }

          if (me.manageAddress) {
            if (model.get('isBillTo')) {
              dataBps.set('locId', model.get('id'));
              dataBps.set('locName', model.get('name'));
              dataBps.set('postalCode', model.get('postalCode'));
              dataBps.set('cityName', model.get('cityName'));
              dataBps.set('countryName', model.get('countryName'));
              dataBps.set('locationBillModel', model);
            }
            if (model.get('isShipTo')) {
              dataBps.set('shipLocId', model.get('id'));
              dataBps.set('shipLocName', model.get('name'));
              dataBps.set('shipPostalCode', model.get('postalCode'));
              dataBps.set('shipCityName', model.get('cityName'));
              dataBps.set('shipCountryName', model.get('countryName'));
              dataBps.set('locationModel', model);
            }
          } else {
            dataBps.set('locId', model.get('id'));
            dataBps.set('locName', model.get('name'));
            dataBps.set('postalCode', model.get('postalCode'));
            dataBps.set('cityName', model.get('cityName'));
            dataBps.set('countryName', model.get('countryName'));
            dataBps.set('locationModel', model);
            dataBps.set('locationBillModel', model);
          }

          if (me.target.startsWith('filterSelectorButton_')) {
            me.doChangeFilterSelector({
              selector: {
                name: me.target.substring('filterSelectorButton_'.length),
                value: dataBps.get('id'),
                text: dataBps.get('_identifier'),
                businessPartner: dataBps
              }
            });
          } else {
            me.doChangeBusinessPartner({
              businessPartner: dataBps,
              target: me.owner.owner.args.target
            });
          }
        }
        if (!model.get('ignoreSetBPLoc')) {
          OB.Dal.get(
            OB.Model.BusinessPartner,
            this.bPartner.get('id'),
            successCallbackBPs,
            errorCallback
          );
        }
      },
      this
    );
  }
});

/*Modal definition*/
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OB.UI.ModalBPLocation',
  classes: 'obUiModalBPLocation',
  events: {
    onShowPopup: ''
  },
  executeOnShow: function() {
    if (!this.isInitialized()) {
      this.inherited(arguments);
      if (_.isUndefined(this.args.visibilityButtons)) {
        this.args.visibilityButtons = true;
      }
      this.waterfall('onSetShow', {
        visibility: this.args.visibilityButtons
      });
      this.bPartner = this.args.businessPartner
        ? this.args.businessPartner
        : this.model.get('order').get('bp');
      this.waterfall('onSetBusinessPartner', {
        bPartner: this.bPartner
      });
      this.bubble('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      this.selectorHide = false;
      this.changedTitle(this.bPartner);
      this.$.body.$.listBpsLoc.setManageAddress(this.args.manageAddress);
      this.$.body.$.listBpsLoc.setBPartner(this.bPartner);
      this.$.body.$.listBpsLoc.setTarget(this.args.target);
      this.$.body.$.listBpsLoc.setInitialLoad(true);
      if (this.bPartner) {
        this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
      } else {
        this.$.body.$.listBpsLoc.bpsList.reset([]);
      }
      this.$.footer.$.modalBpLocFooter.$.newAction.setDisabled(
        !OB.MobileApp.model.hasPermission(
          'OBPOS_retail.createCustomerLocationButton',
          true
        )
      );
    } else if (this.args.makeSearch) {
      this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
    }

    return true;
  },
  executeOnHide: function() {
    var selectorHide = this.selectorHide;
    this.inherited(arguments);
    this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.$.formElementBpsLocationSearchfilterText.coreElement.setValue(
      ''
    );
    if (
      !selectorHide &&
      this.args.navigationPath &&
      this.args.navigationPath.length > 0
    ) {
      this.doShowPopup({
        popup: this.args.navigationPath[this.args.navigationPath.length - 1],
        args: {
          businessPartner: this.args.businessPartner,
          target: this.args.target,
          navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPop(
            this.args.navigationPath
          ),
          makeSearch: this.args.makeSearch
        }
      });
    }
  },
  changedTitle: function(bp) {
    if (this.args.manageAddress) {
      if (this.args.locationButton) {
        this.$.header.setContent(
          OB.I18N.getLabel('OBPOS_LblAssignCustomerAddress')
        );
      } else {
        this.$.header.setContent(
          OB.I18N.getLabel('OBPOS_LblManageCustomerAddresses', [
            (this.args.businessPartner
              ? this.args.businessPartner
              : this.model.get('order').get('bp')
            ).get('name')
          ])
        );
      }
      return;
    }

    var me = this,
      criteria = {};

    function successCallbackBPsLoc(dataBps) {
      if (dataBps && dataBps.length > 1) {
        me.$.header.setContent(
          OB.I18N.getLabel('OBPOS_LblAssignCustomerBillAddress')
        );
      } else if (
        dataBps &&
        dataBps[0] &&
        dataBps[0].get('isBillTo') &&
        dataBps[0].get('isShipTo')
      ) {
        me.$.header.setContent(
          OB.I18N.getLabel('OBPOS_LblAssignCustomerAddress')
        );
      } else {
        me.$.header.setContent(
          OB.I18N.getLabel('OBPOS_LblAssignCustomerBillAddress')
        );
      }
    }

    function errorCallback(tx, error) {
      if (
        OB.MobileApp.model.get('permissions') &&
        OB.MobileApp.model.get('permissions')['OBPOS_remote.customer'] &&
        !OB.MobileApp.model.get('connectedToERP')
      ) {
        OB.UTIL.showConfirmation.display(
          'Error',
          OB.I18N.getLabel('OBMOBC_MsgApplicationServerNotAvailable')
        );
      } else {
        OB.UTIL.showError(error);
      }
    }

    if (
      bp.get('locations') &&
      bp.get('locations').length > 0 &&
      bp.get('locations')[0].attributes
    ) {
      successCallbackBPsLoc(bp.get('locations'));
    } else {
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
      OB.Dal.find(
        OB.Model.BPLocation,
        criteria,
        function(locations) {
          successCallbackBPsLoc(locations.models);
        },
        errorCallback
      );
    }
  },
  i18nHeader: '',
  body: {
    kind: 'OB.UI.ListBpsLoc'
  },
  footer: {
    kind: 'OB.UI.ModalBpLocFooter'
  },
  getScrollableTable: function() {
    return this.$.body.$.listBpsLoc.$.bpsloclistitemprinter;
  },
  init: function(model) {
    this.inherited(arguments);
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});
