/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, $ */

(function() {

    OB = window.OB || {};

    OB.UI = window.OB.UI || {};

    OB.Model.WindowModel = Backbone.Model.extend({
        models: [],
        data: {},

        initialize: function() {
            var me = this,
                queue = {};
            _.each(this.get('models'), function(item) {
                var ds;

                if (item.prototype.online) {
                    ds = new OB.DS.DataSource(new OB.DS.Request(item, OB.POS.modelterminal.get('terminal').client, OB.POS.modelterminal.get('terminal').organization, OB.POS.modelterminal.get('terminal').id));


                    queue[item.prototype.modelName] = false;
                    ds.on('ready', function() {
                        me.data[item.prototype.modelName] = new Backbone.Collection(ds.cache);
                        console.log('loaded model', item);
                        queue[item.prototype.modelName] = true;
                        if (OB.UTIL.queueStatus(queue)) me.trigger('ready');
                    });
                    ds.load(item.params);
                }
            });
            //TODO: load offline models when regesitering window
        },

        getData: function(dsName) {
        	console.log ('getData',dsName);
            return this.data[dsName];
        }

    });

    OB.Model.CashManagement = OB.Model.WindowModel.extend({
        defaults: {
            models: [OB.Model.DepositsDrops, OB.Model.CashMgmtPaymentMethod, OB.Model.DropEvents, OB.Model.DepositEvents]
        }
    });

    OB.UI.WindowView = Backbone.View.extend({
        windowmodel: null,

        initialize: function() {
            var me = this;
            this.model = new this.windowmodel();
            this.model.on('ready', function() {
            	OB.UTIL.initContentView(me);
                console.log('ready...');
                me.trigger('ready');
            });
           // OB.UTIL.initContentView(this);
            // debugger;
        }

    });

    OB.UI.CashManagement = OB.UI.WindowView.extend({
        windowmodel: OB.Model.CashManagement,
        tagName: 'section',
        contentView: [{
            tag: 'div',
            attributes: {
                'class': 'row'
            },
            content: [
            // 1st column: list of deposits/drops done or in process
            {
                tag: 'div',
                attributes: {
                    'class': 'span6'
                },
                content: [{
                    view: OB.COMP.ListDepositsDrops
                }]
            },
            //2nd column:
            {
                tag: 'div',
                attributes: {
                    'class': 'span6'
                },
                content: [{
                    tag: 'div',
                    attributes: {
                        'class': 'span6'
                    },
                    content: [{
                        view: OB.COMP.CashMgmtInfo
                    }]
                }, {
                    view: OB.COMP.CashMgmtKeyboard
                }]
            },
            //hidden stuff XXX:???
            {
            	tag: 'div',
            	content: [{view: OB.UI.ModalDepositEvents}]
            }
            ]
        }]
        ,

        initialize: function() {
            OB.UI.WindowView.prototype.initialize.call(this, arguments);
            this.modalCancel = new OB.COMP.ModalCancel();
        }
    });

    //            	  OB.COMP.CustomView.extend({
    //                createView: function () {
    //                  return (
    //                    {kind: B.KindJQuery('section'), content: [
    //                      {kind: OB.COMP.ModalCancel},
    //                      {kind: OB.DATA.Container, content: [
    //                        {kind: OB.DATA.DepositEvents},
    //                        {kind: OB.DATA.DropEvents},
    //                        {kind: OB.DATA.DepositsDrops},
    //                        {kind: OB.DATA.CashMgmtPaymentMethod}
    //                      ]},
    //                      {kind: B.KindJQuery('div'), attr: {'class': 'row'}, content: [
    //                        {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
    //                           {kind: OB.COMP.ListDepositsDrops}
    //                          // {kind: OB.COMP.DepositsDropsTicket}
    //                         ]},
    //                         
    //                        {kind: B.KindJQuery('div'), attr: {'class': 'span6'}, content: [
    //                          {kind: B.KindJQuery('div'), content: [
    //                            {kind: OB.COMP.CashMgmtInfo }
    //                          ]},
    //                          {kind: OB.COMP.CashMgmtKeyboard }
    //                        ]}
    //                      ]},
    //                      {kind: OB.UI.ModalDropEvents},
    //                      {kind: OB.UI.ModalDepositEvents},
    //                      {kind: OB.DATA.DropDepSave},
    //                      {kind: OB.DATA.Container, content: [
    //                        {kind: OB.COMP.HWManager, attr: {'templatecashmgmt': 'res/printcashmgmt.xml'}}
    //                      ]}
    //                    ]}
    //                  );
    //                }
    //              });
    //  // register
    OB.POS.registerWindow('retail.cashmanagement', OB.UI.CashManagement);
}());