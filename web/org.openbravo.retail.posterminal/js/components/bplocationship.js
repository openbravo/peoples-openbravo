/*
 ************************************************************************************
 * Copyright (C) 2012-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 *
 * Contributed by Qualian Technologies Pvt. Ltd.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

/*items of collection*/
enyo.kind({
  name: 'OB.UI.ListBpsShipLocLine',
  kind: 'OB.UI.ListBpsLocLine',
  classes: 'obUiListBpsShipLocLine',
  locId: 'shipLocId'
});

/*scrollable table (body of modal)*/
enyo.kind({
  name: 'OB.UI.ListBpsShipLoc',
  classes: 'obUiListBpsShipLoc',
  published: {
    bPartner: null,
    manageAddress: false,
    target: 'order'
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
      classes: 'obUiListBpsShipLoc-container1',
      components: [
        {
          name: 'bpsloclistitemprinter',
          kind: 'OB.UI.ScrollableTable',
          calsses: 'obUiListBpsShipLoc-container1-bpsloclistitemprinter',
          renderHeader: 'OB.UI.ModalBpLocScrollableHeader',
          renderLine: 'OB.UI.ListBpsShipLocLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }
      ]
    }
  ],
  clearAction: function(inSender, inEvent) {
    this.bpsList.reset();
    return true;
  },
  searchAction: async function(inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('searchCustomerAddress');
    var me = this,
      criteria = {},
      filter = inEvent.locName;

    function errorCallback(tx, error) {
      OB.UTIL.showError(error);
    }

    function successCallbackBPsLoc(dataBps) {
      if (dataBps && dataBps.length > 0) {
        if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
          me.bpsList.reset(dataBps.models);
        } else {
          me.bpsList.reset(dataBps);
        }
      } else {
        me.bpsList.reset();
      }
    }

    var operator = OB.Dal.CONTAINS;
    if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
      operator = OB.MobileApp.model.hasPermission(
        'OBPOS_remote.customer_usesContains',
        true
      )
        ? OB.Dal.CONTAINS
        : OB.Dal.STARTSWITH;
    }
    criteria.name = {
      operator: operator,
      value: filter
    };
    criteria.bpartner = this.bPartner.get('id');
    criteria.isShipTo = true;

    if (OB.UTIL.remoteSearch(OB.Model.BusinessPartner)) {
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
        isShipTo = {
          columns: ['isShipTo'],
          operator: 'equals',
          value: true,
          boolean: true
        };
      var remoteCriteria = [filterIdentifier, bPartnerId, isShipTo];
      criteria.remoteFilters = remoteCriteria;

      OB.Dal.find(
        OB.Model.BPLocation,
        criteria,
        successCallbackBPsLoc,
        errorCallback
      );
    } else {
      try {
        const criteria = new OB.App.Class.Criteria();
        criteria.criterion('bpartner', this.bPartner.get('id'));
        criteria.criterion('isShipTo', true);
        criteria.criterion('name', filter, 'includes');
        let bPLocations = await OB.App.MasterdataModels.BusinessPartnerLocation.find(
          criteria.build()
        );
        let transformedBPLocations = [];
        for (let i = 0; i < bPLocations.length; i++) {
          transformedBPLocations.push(
            OB.Dal.transform(OB.Model.BPLocation, bPLocations[i])
          );
        }
        successCallbackBPsLoc(transformedBPLocations);
      } catch (error) {
        errorCallback(error);
      }
    }
    OB.UTIL.ProcessController.finish('searchCustomerAddress', execution);
    return true;
  },
  bpsList: null,
  init: function(model) {
    this.bpsList = new Backbone.Collection();
    this.$.bpsloclistitemprinter.setCollection(this.bpsList);
    this.bpsList.on(
      'click',
      async function(model) {
        var me = this;

        function errorCallback(tx, error) {
          OB.error(tx);
          OB.error(error);
        }

        function successCallbackBPs(dataBps) {
          dataBps.set('locationModel', model);
          dataBps.set('shipLocId', model.get('id'));
          dataBps.set('shipLocName', model.get('name'));
          dataBps.set('shipPostalCode', model.get('postalCode'));
          dataBps.set('shipCityName', model.get('cityName'));
          dataBps.set('shipCountryName', model.get('countryName'));
          dataBps.set('shipRegionId', model.get('regionId'));
          dataBps.set('shipCountryId', model.get('countryId'));

          //Keep the other address:
          dataBps.set('locId', me.bPartner.get('locId'));
          dataBps.set('locName', me.bPartner.get('locName'));
          dataBps.set('postalCode', me.bPartner.get('postalCode'));
          dataBps.set('cityName', me.bPartner.get('cityName'));
          dataBps.set('countryName', me.bPartner.get('countryName'));

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
          if (OB.MobileApp.model.hasPermission('OBPOS_remote.customer', true)) {
            OB.Dal.get(
              OB.Model.BusinessPartner,
              this.bPartner.get('id'),
              successCallbackBPs,
              errorCallback
            );
          } else {
            try {
              let businessPartner = await OB.App.MasterdataModels.BusinessPartner.withId(
                this.bPartner.get('id')
              );
              successCallbackBPs(
                OB.Dal.transform(OB.Model.BusinessPartner, businessPartner)
              );
            } catch (error) {
              errorCallback(error);
            }
          }
        }
      },
      this
    );
  }
});

/*Modal definiton*/
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OB.UI.ModalBPLocationShip',
  classes: 'obUiModalSelector',
  executeOnShow: function() {
    if (!this.isInitialized()) {
      this.inherited(arguments);
      if (_.isUndefined(this.args.visibilityButtons)) {
        this.args.visibilityButtons = true;
      }
      this.waterfall('onSetShow', {
        visibility: this.args.visibilityButtons
      });
      this.bubble('onSetBusinessPartnerTarget', {
        target: this.args.target
      });
      this.$.body.$.listBpsShipLoc.setBPartner(
        this.model.get('order').get('bp')
      );
      this.$.body.$.listBpsShipLoc.setTarget(this.args.target);
      this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
      this.$.footer.$.modalBpLocFooter.$.newAction.setDisabled(
        !OB.MobileApp.model.hasPermission(
          'OBPOS_retail.createCustomerLocationButton',
          true
        )
      );
    } else {
      if (this.args.makeSearch) {
        this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
      } else {
        this.$.body.$.listBpsShipLoc.bpsList.reset(
          this.$.body.$.listBpsShipLoc.bpsList.models.map(bp => {
            bp.unset('ignoreSetBPLoc');
            return bp;
          })
        );
      }
    }
    return true;
  },
  executeOnHide: function() {
    this.inherited(arguments);
    this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.clearAction();
  },
  i18nHeader: 'OBPOS_LblAssignCustomerShipAddress',
  body: {
    kind: 'OB.UI.ListBpsShipLoc',
    classes: 'obUiModalSelector-body-obUiListBpsShipLoc'
  },
  footer: {
    kind: 'OB.UI.ModalBpLocFooter'
  },
  getScrollableTable: function() {
    return this.$.body.$.listBpsShipLoc.$.bpsloclistitemprinter;
  },
  init: function(model) {
    this.inherited(arguments);
    this.model = model;
    this.waterfall('onSetModel', {
      model: this.model
    });
  }
});
