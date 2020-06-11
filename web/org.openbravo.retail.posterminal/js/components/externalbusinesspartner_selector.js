/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, OB */

enyo.kind({
  name: 'OB.UI.NewExternalBusinessPartner',
  kind: 'OB.UI.ModalDialogButton',
  events: { onViewEditExternalBp: '' },
  disabled: false,
  classes: 'obUiNewExternalBusinessPartenr',
  i18nLabel: 'OBPOS_LblNewCustomer',
  tap: function() {
    if (this.disabled) {
      return true;
    }
    this.doViewEditExternalBp({ bp: null, mode: 'insert' });
    return true;
  }
});

enyo.kind({
  name: 'OB.UI.AdvancedFilterButtonExternalBp',
  kind: 'OB.UI.ButtonAdvancedFilter',
  classes: 'obUiAdvancedFilterButtonExternalBp',
  dialog: 'modalAdvancedFiltersExternalBp',
  i18nLabel: 'OBPOS_LblAdvancedFilter',
  disabled: false,
  handlers: {
    onNewBPDisabled: 'doDisableNewBP'
  },
  doDisableNewBP: function(inSender, inEvent) {
    this.setDisabled(inEvent.status);
  }
});

enyo.kind({
  name: 'OB.UI.ModalAdvancedFiltersExternalBp',
  kind: 'OB.UI.ModalAdvancedFilters',
  classes: 'modalAdvancedFiltersExternalBp',
  initComponents: function() {
    this.externalBPListViewData = new OB.App.Class.ExternalBusinessPartnerListViewData();
    this.inherited(arguments);
    this.setFilters(this.externalBPListViewData.getAdvancedFilters());
  }
});

enyo.kind({
  name: 'OB.UI.ModalExternalBpSelectorScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obUiModalExternalBpSelectorScrollableHeader',
  components: [
    {
      name: 'filterExternalBpSelector',
      kind: 'OB.UI.FilterSelectorTableHeader',
      classes:
        'obUiModalExternalBpSelectorScrollableHeader-filterExternalBpSelector',
      filters: OB.Model.ExternalBpIntegration.getProperties()
    }
  ],
  initComponents: function() {
    this.externalBPListViewData = new OB.App.Class.ExternalBusinessPartnerListViewData();
    this.filters = this.externalBPListViewData.getMainFilters();
    this.inherited(arguments);
    // disable auto search when key is pressed
    this.$.filterExternalBpSelector.$.formElementEntityFilterText.coreElement.skipAutoFilterPref = true;
  }
});

enyo.kind({
  name: 'OB.UI.ModalSelectorExternalBusinessPartners',
  kind: 'OB.UI.ModalSelector',
  classes: 'obUiModalSelectorExternalBusinessPartners',
  i18nHeader: 'OBPOS_LblAssignCustomer',
  events: {
    onShowPopup: ''
  },
  handlers: {
    onExternalBpSelected: 'externalBpSelected',
    onViewEditExternalBp: 'viewEditExternalBp'
  },
  body: {
    kind: 'OB.UI.ListExternalBpsSelector',
    name: 'listExternalBpsSelector'
  },
  footer: { kind: 'OB.UI.ModalExternalBpSelectorFooter' },
  setSearchPerformed: function(executed) {
    this.oneSearchWasPerformed = executed;
    this.allowCreate = true;
    if (
      !this.oneSearchWasPerformed &&
      OB.MobileApp.model.hasPermission('OBPOS_retail.disableNewBPButton', true)
    ) {
      this.allowCreate = false;
    }
    this.$.footer.$.modalExternalBpSelectorFooter.setAllowCreate(
      this.allowCreate
    );
  },
  executeOnShow: function() {
    this.detailViewActive = false;
    this.allowCreate = true;
    if (!this.isInitialized()) {
      this.inherited(arguments);
      this.oneSearchWasPerformed = false;
      if (
        OB.MobileApp.model.hasPermission(
          'OBPOS_retail.disableNewBPButton',
          true
        )
      ) {
        this.allowCreate = false;
      }
      this.$.footer.$.modalExternalBpSelectorFooter.setAllowCreate(
        this.allowCreate
      );
      if (OB.UTIL.isNullOrUndefined(this.args.target)) {
        this.args.target = 'order';
      }
      this.$.header.setContent(
        OB.I18N.getLabel(
          this.args.target.indexOf('filterSelectorButton_') === 0
            ? 'OBPOS_LblSelectCustomer'
            : 'OBPOS_LblAssignCustomer'
        )
      );
      this.getFilterSelectorTableHeader().clearFilter();
      this.$.body.$.listExternalBpsSelector.dialog = this;
    }

    if (this.args.lastModifiedExtBp || this.args.presetExternalBpId) {
      if (this.args.presetExternalBpId) {
        this.externalBPListViewData.reset();
        this.externalBPListViewData.addPlainBpToList(
          this.args.presetExternalBp
        );
      } else {
        if (this.args.lastModifiedExtBp) {
          if (this.args.lastModifiedExtBpIsNew) {
            this.externalBPListViewData.addBpToList(
              this.args.lastModifiedExtBp
            );
          } else {
            this.externalBPListViewData.replaceBusinessPartnerFromList(
              this.args.lastModifiedExtBp
            );
          }
        }
      }
      this.$.body.$.listExternalBpsSelector.drawResultsInList();
    }
    this.args.target = this.args.target || 'order';
    this.bubble('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    this.waterfall('onSetBusinessPartnerTarget', {
      target: this.args.target
    });
    if (this.keepFiltersOnClose) {
      this.doSetSelectorAdvancedSearch({
        isAdvanced: this.advancedFilterShowing
      });
    }
    return true;
  },
  executeOnHide: function() {
    if (this.keepFiltersOnClose) {
      this.advancedFilterShowing = this.$.body.$.listExternalBpsSelector.$.stBPAssignToReceipt.$.theader.$.modalExternalBpSelectorScrollableHeader.$.filterExternalBpSelector.$.advancedFilterInfo.showing;
    }
    this.inherited(arguments);
    if (this.selectorHide === false && !this.detailViewActive) {
      this.externalBPListViewData.reset();
    }
    if (
      !this.selectorHide &&
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
  getScrollableTable: function() {
    return this.$.body.$.listExternalBpsSelector.$.stBPAssignToReceipt;
  },
  getFilterSelectorTableHeader: function() {
    return this.$.body.$.listExternalBpsSelector.$.stBPAssignToReceipt.$.theader
      .$.modalExternalBpSelectorScrollableHeader.$.filterExternalBpSelector;
  },
  getAdvancedFilterBtn: function() {
    return this.$.footer.$.modalExternalBpSelectorFooter.$.advancedFilterButton;
  },
  getAdvancedFilterDialog: function() {
    return 'modalAdvancedFiltersExternalBp';
  },
  initComponents: function() {
    this.externalBPListViewData = new OB.App.Class.ExternalBusinessPartnerListViewData();
    this.inherited(arguments);
  },
  viewEditExternalBp: function(inSender, inEvent) {
    this.doShowPopup({
      popup: 'modalExternalBusinessPartnerViewEdit',
      args: {
        businessPartnerIdentifier: inEvent.bp
          ? inEvent.bp.getIdentifier()
          : null,
        businessPartnerId: inEvent.bp ? inEvent.bp.getKey() : null,
        businessPartner: inEvent.bp || null,
        mode: inEvent.mode,
        target: this.args.target,
        navigationPath: OB.UTIL.BusinessPartnerSelector.cloneAndPush(
          this.args.navigationPath,
          'modalExternalBusinessPartner'
        )
      }
    });
    this.detailViewActive = true;
    return true;
  },
  externalBpSelected: function(inSender, inEvent) {
    const order = OB.MobileApp.model.orderList.modelorder;
    order
      .setAndSaveExternalBP(inEvent.bp)
      .then(() => {
        if (this.target.startsWith('filterSelectorButton_')) {
          this.$.body.$.listExternalBpsSelector.doChangeFilterSelector({
            selector: {
              name: this.target.substring('filterSelectorButton_'.length),
              value: inEvent.bp.getKey(),
              text: inEvent.bp.getIdentifier(),
              businessPartner: inEvent.bp
            }
          });
        }
      })
      .catch(objError => {
        let reasonTitle = objError.reason
          ? objError.reason
          : OB.I18N.getLabel('OBPOS_BusinessPartnerAssignCanceledTitle');
        let reasonDetail = objError.reasonDetail
          ? objError.reasonDetail
          : OB.I18N.getLabel('OBPOS_BusinessPartnerAssignCanceledDetail');
        OB.UTIL.showConfirmation.display(reasonTitle, reasonDetail);
      });
  }
});

enyo.kind({
  name: 'OB.UI.ExternalBPDetailsContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiExternalBpDetailsContextMenuItem',
  i18NLabel: 'OBPOS_BPViewDetails',
  events: { onViewEditExternalBp: '' },
  selectItem: function(bpartner) {
    this.doViewEditExternalBp({ bp: bpartner, mode: 'viewDetails' });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.ExternalBPEditContextMenuItem',
  kind: 'OB.UI.ListContextMenuItem',
  classes: 'obUiBpExternalEditContextMenuItem',
  i18NLabel: 'OBPOS_BPEdit',
  events: { onViewEditExternalBp: '' },
  selectItem: function(bpartner) {
    this.doViewEditExternalBp({ bp: bpartner, mode: 'edit' });
    return true;
  },
  create: function() {
    this.inherited(arguments);
    this.setContent(OB.I18N.getLabel(this.i18NLabel));
  }
});

enyo.kind({
  name: 'OB.UI.ExternalBusinessPartnerContextMenu',
  kind: 'OB.UI.ListContextMenu',
  classes: 'obUiExternalBusinessPartnerContextMenu',
  initComponents: function() {
    this.inherited(arguments);
    let menuOptions = [];

    menuOptions.push({
      kind: 'OB.UI.ExternalBPDetailsContextMenuItem',
      permission: 'OBPOS_receipt.customers'
    });
    if (
      OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomerButton', true)
    ) {
      menuOptions.push({
        kind: 'OB.UI.ExternalBPEditContextMenuItem',
        classes: 'obUiBusinessPartnerContextMenu-obUiBpEditContextMenuItem',
        permission: 'OBPOS_retail.editCustomerButton'
      });
    }
    this.$.menu.setItems(menuOptions);
  },
  itemSelected: function(sender, event) {
    this.owner.contextMenuItemSelected = true;
    event.originator.selectItem(this.model);
    return true;
  }
});

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListExternalBpsSelectorLine',
  kind: 'OB.UI.ListSelectorLine',
  classes: 'obUiListExternalBpsSelectorLine',
  events: { onExternalBpSelected: '' },
  components: [
    {
      name: 'line',
      classes: 'obUiListExternalBpsSelectorLine-line',
      components: [
        {
          name: 'textInfo',
          classes: 'obUiListExternalBpsSelectorLine-line-textInfo'
        },
        {
          name: 'btnContextMenu',
          kind: 'OB.UI.ExternalBusinessPartnerContextMenu',
          classes: 'obUiListExternalBpsSelectorLine-line-btnContextMenu'
        }
      ]
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.bpObj = this.params.dialog.externalBPListViewData.getBusinessPartnerFromList(
      this.model.get('mixedBp')
    );
    let components = this.bpObj.getPropertiesForList().map(bpProperty => {
      let content = bpProperty.value;
      if (bpProperty.reference === 'C') {
        if (bpProperty.valueLabel) {
          content = bpProperty.valueLabel;
        } else if (bpProperty.options && bpProperty.options.length > 0) {
          let selectedOption = bpProperty.options.find(opt => {
            opt.searchKey === bpProperty.value;
          });
          if (selectedOption) {
            if (selectedOption.istranslatable && selectedOption.message) {
              content = OB.I18N.getLabel(selectedOption.message$_identifier);
            } else {
              content = selectedOption.text;
            }
          }
        }
      }
      return {
        tag: 'span',
        name: bpProperty.apiKey,
        classes:
          'obUiListExternalBpsSelectorLine-line-textInfo-' + bpProperty.apiKey,
        content: content
      };
    });
    components = OB.App.ExternalBusinessPartnerAPI.onBpListItemViewLoad(
      this.bpObj,
      components
    );
    this.$.textInfo.createComponents(components);
    // Context menu
    if (this.$.btnContextMenu.$.menu.itemsCount === 0) {
      this.$.btnContextMenu.hide();
    } else {
      this.$.btnContextMenu.setModel(this.bpObj);
    }
  },
  tap: function() {
    this.inherited(arguments);
    this.doExternalBpSelected({ bp: this.bpObj });
  }
});

enyo.kind({
  name: 'OB.UI.ListExternalBpsSelector',
  classes: 'obUiListExternalBpsSelector',
  handlers: {
    onSearchAction: 'searchAction',
    onClearFilterSelector: 'clearAction',
    onSetBusinessPartnerTarget: 'setBusinessPartnerTarget'
  },
  events: {
    onChangeBusinessPartner: '',
    onChangeFilterSelector: '',
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [
    {
      classes: 'obUiListExternalBpsSelector-container1',
      components: [
        {
          name: 'stBPAssignToReceipt',
          kind: 'OB.UI.ScrollableTable',
          classes: 'obUiListExternalBpsSelector-container1-stBpAssignToReceipt',
          renderHeader: 'OB.UI.ModalExternalBpSelectorScrollableHeader',
          renderLine: 'OB.UI.ListExternalBpsSelectorLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        },
        {
          name: 'renderLoading',
          classes:
            'obUiListBpsSobUiListExternalBpsSelectorelector-container1-renderLoading',
          showing: false,
          initComponents: function() {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }
      ]
    }
  ],
  setBusinessPartnerTarget: function(inSender, inEvent) {
    this.target = inEvent.target;
  },
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: function(inSender, inEvent) {
    this.$.stBPAssignToReceipt.renderLineParams = { dialog: this.dialog };
    this.dialog.externalBPListViewData
      .fetchBusinessPartnersFromAPI(inEvent.filters)
      .then(bps => {
        this.dialog.setSearchPerformed(true);
        this.$.renderLoading.hide();
        if (bps && bps.length > 0) {
          this.bpsList.reset(bps);
          this.$.stBPAssignToReceipt.$.tbody.show();
        } else {
          this.bpsList.reset();
          this.$.stBPAssignToReceipt.$.tempty.show();
        }
      })
      .catch(error => {
        this.dialog.setSearchPerformed(true);
        OB.UTIL.showConfirmation.display(
          OB.I18N.getLabel('OBMOBC_Error'),
          error.message,
          null,
          {
            onHideFunction: function() {
              this.doShowSelector();
            }
          }
        );
      });
  },
  drawResultsInList: function() {
    this.$.stBPAssignToReceipt.renderLineParams = { dialog: this.dialog };
    this.bpsList.reset(this.dialog.externalBPListViewData.bplist);
    this.$.stBPAssignToReceipt.$.tbody.show();
  },

  bpsList: null,
  initComponents: function() {
    this.popup = OB.UTIL.getPopupFromComponent(this);
    this.inherited(arguments);
    this.$.stBPAssignToReceipt.renderLineParams = this.popup;
  },
  init: function(model) {
    this.bpsList = new Backbone.Collection();
    this.$.stBPAssignToReceipt.setCollection(this.bpsList);
  }
});
enyo.kind({
  name: 'OB.UI.ModalExternalBpSelectorFooter',
  classes: 'obUiModalExternalBpSelectorFooter',
  components: [
    {
      classes: 'obUiModalExternalBpSelectorFooter-container1',
      showing: true,
      components: [
        {
          classes:
            'obUiModal-footer-secondaryButtons obUiModalExternalBpSelectorFooter-container1-container1',
          components: [
            {
              kind: 'OB.UI.AdvancedFilterButtonExternalBp',
              classes:
                'obUiModalExternalBpSelectorFooter-container1-container1-element1'
            }
          ]
        },
        {
          classes:
            'obUiModal-footer-mainButtons obUiModalExternalBpSelectorFooter-container1-container2',
          components: [
            {
              kind: 'OB.UI.NewExternalBusinessPartner',
              name: 'newAction',
              classes:
                'obUiModalExternalBpSelectorFooter-container1-container2-newAction'
            },
            {
              kind: 'OB.UI.ModalDialogButton',
              classes:
                'obUiModalExternalBpSelectorFooter-container1-container2-close',
              i18nLabel: 'OBRDM_LblClose',
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
  ],
  setAllowCreate: function(allowCreate) {
    this.$.newAction.setDisabled(!allowCreate);
  }
});
