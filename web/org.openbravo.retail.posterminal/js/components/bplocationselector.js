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

/*global enyo, Backbone */

/*header of scrollable table*/
enyo.kind({
    kind: 'OB.UI.Button',
    name: 'OB.UI.AdvancedSearchCustomerWindowButton',
    style: 'margin: 0px 0px 8px 5px;',
    classes: 'btnlink-yellow btnlink btnlink-small',
    i18nLabel: 'OBPOS_LblAdvancedSearch',
    disabled: false,
    handlers: {
        onSetModel: 'setModel'
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
            name: 'customerAdvancedSearch',
            params: {
                caller: 'mainSubWindow'
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
            this.setDisabled();
            this.addClass('disabled');
        }
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
                    kind: 'OB.UI.AdvancedSearchCustomerWindowButton'
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
    name: 'OB.UI.ListBpsLocLine',
    kind: 'OB.UI.SelectButton',
    components: [{
        name: 'line',
        style: 'line-height: 30px;',
        components: [{
            name: 'identifier'
        }, {
            style: 'clear: both;'
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
    }
});

/*scrollable table (body of modal)*/
enyo.kind({
    name: 'OB.UI.ListBpsLoc',
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
        OB.Dal.find(OB.Model.BPLocation, {
            bpartner: this.bPartnerId,
            name: {
                operator: OB.Dal.CONTAINS,
                value: filter
            }
        }, successCallbackBPsLoc, errorCallback);
        return true;
    },
    bpsList: null,
    init: function (model) {
        this.bpsList = new Backbone.Collection();
        this.$.bpsloclistitemprinter.setCollection(this.bpsList);
        this.bpsList.on('click', function (model) {
            var parent = this;

            function errorCallback(tx, error) {
                window.console.error(tx);
                window.console.error(error);
            }

            function successCallbackBPs(dataBps) {
                function success(tx) {
                    parent.doChangeBusinessPartner({
                        businessPartner: dataBps
                    });
                }

                function error(tx) {
                    window.console.error(tx);
                }
                dataBps.set('locId', model.get('id'));
                dataBps.set('locName', model.get('name'));
                OB.Dal.save(dataBps, success, error);
            }
            OB.Dal.get(OB.Model.BusinessPartner, this.bPartnerId, successCallbackBPs, errorCallback);
        }, this);
    }
});

/*Modal definiton*/
enyo.kind({
    name: 'OB.UI.ModalBPLocation',
    topPosition: '125px',
    kind: 'OB.UI.Modal',
    executeOnShow: function () {
        var bId = this.model.get('order').get('bp').get('id');
        this.$.body.$.listBpsLoc.setBPartnerId(bId);
        this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.searchAction();
        return true;
    },
    executeOnHide: function () {
        this.$.body.$.listBpsLoc.$.bpsloclistitemprinter.$.theader.$.modalBpLocScrollableHeader.clearAction();
    },
    i18nHeader: 'OBPOS_LblAssignCustomerAddress',
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

// Register Popup
OB.UI.WindowView.registerPopup('OB.OBPOSPointOfSale.UI.PointOfSale', {
    kind: 'OB.UI.ModalBPLocation',
    name: 'OB_UI_ModalBPLocation'
});