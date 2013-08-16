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

enyo.kind({
    kind: 'OB.UI.SmallButton',
    name: 'OB.UI.BPLocation',
    classes: 'btnlink btnlink-small btnlink-gray',
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
        this.setDisabled(inEvent.status);
    },
    tap: function () {
        if (!this.disabled) {
            this.doShowPopup({
                popup: 'modalcustomeraddress'
            });
        }
    },
    initComponents: function () {},
    renderBPLocation: function (newLocation) {
        this.setContent(newLocation);
    },
    orderChanged: function (oldValue) {
        if (this.order.get('bp')) {
            this.renderBPLocation(this.order.get('bp').get('locName'));
        } else {
            this.renderBPLocation('');
        }

        this.order.on('change:bp', function (model) {
            if (model.get('bp')) {
                this.renderBPLocation(model.get('bp').get('locName'));
            } else {
                this.renderBPLocation('');
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
        onSetModel: 'setModel'
    },
    setModel: function (inSender, inEvent) {
        this.model = inEvent.model;
    },
    tap: function (model) {
        if (this.disabled) {
            return true;
        }
        this.doHideThisPopup();
        var me = this;

        function errorCallback(tx, error) {
            window.console.error(tx);
            window.console.error(error);
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
        OB.Dal.get(OB.Model.BusinessPartner, this.model.get('order').get('bp').get('id'), successCallbackBPs, errorCallback);
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
        this.putDisabled(!OB.MobileApp.model.hasPermission('OBPOS_retail.editCustomers'));
    }
});

enyo.kind({
    kind: 'OB.UI.Button',
    name: 'OB.UI.SearchCustomerAddressWindowButton',
    style: 'width: 170px; margin: 0px 0px 8px 5px;',
    classes: 'btnlink-yellow btnlink btnlink-small',
    i18nLabel: 'OBPOS_LblEditAddress',
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
            name: 'customerAddressSearch',
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
                    kind: 'OB.UI.NewCustomerAddressWindowButton'
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
                window.console.error(tx);
                window.console.error(error);
            }

            function successCallbackBPs(dataBps) {
                function success(tx) {
                    me.doChangeBusinessPartner({
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
        this.$.body.$.listBpsLoc.setBPartnerId(this.model.get('order').get('bp').get('id'));
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