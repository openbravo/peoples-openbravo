/*
 ************************************************************************************
 * Copyright (C) 2016-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, Backbone, moment, _ */

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.ButtonSelectAll',
  classes: 'btnlink-yellow btnlink btnlink-small btnlink-headerButton',
  i18nLabel: 'OBPOS_lblSelectAll',
  events: {
    onSelectAll: ''
  },
  selectAll: true,
  changeTo: function (select) {
    if (!select) {
      this.setContent(OB.I18N.getLabel('OBPOS_LblUnSelectAll'));
    } else {
      this.setContent(OB.I18N.getLabel('OBPOS_lblSelectAll'));
    }
    this.selectAll = select;
  },
  tap: function () {
    this.doSelectAll({
      selectAll: this.selectAll
    });
    this.changeTo(!this.selectAll);
  }
});

enyo.kind({
  kind: 'OB.UI.ButtonAdvancedFilter',
  name: 'OB.UI.SelectorButtonAdvancedFilter',
  classes: 'btnlink-headerButton',
  dialog: 'OBPOS_modalAdvancedFilterOrders'
});

enyo.kind({
  kind: 'OB.UI.SmallButton',
  name: 'OB.UI.ButtonAssociateSelected',
  classes: 'btnlink-green btnlink btnlink-small btnlink-headerButton ',
  i18nLabel: 'OBPOS_AssociateSelected',
  processesToListen: ['associateLines'],
  events: {
    onAssociateSelected: ''
  },
  initComponents: function () {
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function () {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  tap: function () {
    this.setDisabled(true);
    this.doAssociateSelected();
  }
});

/* Scrollable table header (header of modal) */
enyo.kind({
  name: 'OB.UI.ModalOrderScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  filterModel: OB.Model.OrderAssociationsFilter,
  components: [{
    classes: 'filterSelectorTableHeader',
    kind: 'OB.UI.FilterSelectorTableHeader',
    name: 'filterSelector'
  }, {
    showing: true,
    classes: 'filterSelectorTableHeader_buttons',
    components: [{
      classes: 'filterSelectorTableHeader_Firstbutton',
      components: [{
        classes: 'selectorHeader_btnSelectAll',
        components: [{
          kind: 'OB.UI.ButtonSelectAll'
        }]
      }, {
        classes: 'selectorHeader_btnAdvancedFilter',
        components: [{
          kind: 'OB.UI.SelectorButtonAdvancedFilter'
        }]
      }, {
        classes: 'selectorHeader_btnAssociateSelected',
        components: [{
          kind: 'OB.UI.ButtonAssociateSelected'
        }]
      }]
    }]
  }],
  initComponents: function () {
    this.filters = this.filterModel.getFilterPropertiesWithSelectorPreference();
    this.inherited(arguments);
  }
});

/* items of collection */
enyo.kind({
  name: 'OB.UI.ListOrdersLine',
  kind: 'OB.UI.listItemButton',
  classes: 'selector_orderLines',
  components: [{
    name: 'order',
    classes: 'ob-row-order',
    components: [{
      classes: 'ob-checkbox-half-on',
      name: 'iconOrder',
      tap: function () {
        this.bubble('onTapOrderIcon');
      }
    }, {
      classes: 'ob-row-order-text-bold',
      name: 'documentNo'
    }, {
      classes: 'ob-row-order-text',
      name: 'bpName'
    }, {
      classes: 'ob-row-order-text',
      name: 'orderedDate'
    }]
  }, {
    name: 'orderline',
    classes: 'ob-row-orderline',
    components: [{
      classes: 'ob-cell-orderline-left',
      components: [{
        classes: 'ob-checkbox-off',
        name: 'iconOrderLine',
        tap: function () {
          this.bubble('onTapLineIcon');
        }
      }, {
        classes: 'ob-cell-orderline-left-info',
        components: [{
          classes: 'ob-text-line',
          name: 'orderlineInfo'
        }]
      }]
    }]
  }],
  events: {
    onHideThisPopup: '',
    onChangeLine: '',
    onChangeAllLines: ''
  },
  handlers: {
    onTapOrderIcon: 'tapOrderIcon',
    onTapLineIcon: 'tapLineIcon'
  },

  changeIconClass: function (icon, mode) {
    icon.removeClass('ob-checkbox-half-on');
    icon.removeClass('ob-checkbox-on');
    icon.removeClass('ob-checkbox-off');
    if (mode === 'OFF') {
      icon.addClass('ob-checkbox-off');
    } else if (mode === 'ON') {
      icon.addClass('ob-checkbox-on');
    } else {
      icon.addClass('ob-checkbox-half-on');
    }
  },

  associateOrder: function (inSender, inEvent) {
    if (inEvent.value === 0) {
      this.changeIconClass(this.$.iconOrderLine, 'OFF');
    } else if (inEvent.value === 1) {
      this.changeIconClass(this.$.iconOrderLine, 'ON');
    } else {
      this.changeIconClass(this.$.iconOrderLine, 'HALF');
    }
    this.model.set('toAssociate', inEvent.value, {
      silent: true
    });
    this.doChangeLine({
      orderId: this.model.get('orderId')
    });
    this.model.trigger('verifyAssociateTicketsButton', this.model);
  },

  tapOrderIcon: function () {
    var iconClasses = this.$.iconOrder.getClassAttribute(),
        mode = iconClasses === 'ob-checkbox-on' ? 'OFF' : 'ON';

    this.changeIconClass(this.$.iconOrder, mode);
    this.doChangeAllLines({
      orderId: this.model.get('id'),
      mode: mode
    });
    this.model.trigger('verifyAssociateTicketsButton', this.model);
    return true;
  },

  tapLineIcon: function () {
    var qty, iconClasses = this.$.iconOrderLine.getClassAttribute().split(' ');
    if (iconClasses[1] === 'ob-checkbox-on') {
      qty = 0;
    } else {
      qty = 1;
    }
    this.associateOrder(null, {
      value: qty
    });
    return true;
  },
  create: function () {
    this.inherited(arguments);
    this.model.set('renderCmp', this, {
      silent: true
    });
    if (this.model.get('ltype') === 'ORDER') {
      this.owner.addClass('ob-order');
      this.$.orderline.hide();
      this.$.order.show();
      this.$.documentNo.setContent(this.model.get('documentNo'));
      this.$.bpName.setContent(' / ' + this.model.get('bpName'));
      this.$.orderedDate.setContent(' / ' + this.model.get('orderedDate'));
    } else {
      this.owner.addClass('ob-orderline');
      this.$.order.hide();
      this.$.orderline.show();
      this.$.orderlineInfo.setContent(OB.I18N.getLabel('OBPOS_LblLineInfo', [this.model.get('lineNo'), this.model.get('qty'), this.model.get('productName')]));
      var qty = this.model.get('toAssociate') ? this.model.get('toAssociate') : 0;
      this.associateOrder(null, {
        value: qty
      });
    }
  }
});

/* Scrollable table (body of modal) */
enyo.kind({
  name: 'OB.UI.ListOrders',
  classes: 'row-fluid',
  handlers: {
    onClearFilterSelector: 'clearAction',
    onSearchAction: 'searchAction',
    onSelectAll: 'selectAll',
    onAssociateSelected: 'associateSelected',
    onChangeLine: 'changeLine',
    onChangeAllLines: 'changeAllLines'
  },
  events: {
    onHideSelector: '',
    onShowSelector: ''
  },
  components: [{
    classes: 'span12',
    components: [{
      classes: 'row-fluid selector_orderList',
      components: [{
        classes: 'span12',
        components: [{
          name: 'orderSelector',
          kind: 'OB.UI.ScrollableTable',
          scrollAreaMaxHeight: '420px',
          renderHeader: 'OB.UI.ModalOrderScrollableHeader',
          renderLine: 'OB.UI.ListOrdersLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }, {
          name: 'renderLoading',
          classes: 'selectorLoadingMsg',
          showing: false,
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblLoading'));
          }
        }]
      }]
    }]
  }],
  changeAllLines: function (inSender, inEvent) {
    var order = _.find(this.$.orderSelector.collection.models, function (ord) {
      return ord.get('ltype') === 'ORDER' && ord.get('id') === inEvent.orderId;
    });
    if (order) {
      _.each(order.get('lines'), function (line) {
        var qty = 0,
            cmp = line.get('renderCmp');
        if (inEvent.mode === 'ON') {
          qty = 1;
        }
        cmp.changeIconClass(cmp.$.iconOrderLine, inEvent.mode);
        line.set('toAssociate', qty, {
          silent: true
        });
      });
    }
    return true;
  },

  changeLine: function (inSender, inEvent) {
    var order = _.find(this.$.orderSelector.collection.models, function (ord) {
      return ord.get('ltype') === 'ORDER' && ord.get('id') === inEvent.orderId;
    });
    if (order) {
      var none = true,
          completed = true;
      _.each(order.get('lines'), function (line) {
        if (line.get('toAssociate') > 0) {
          none = false;
        }
        if (line.get('toAssociate') !== 1) {
          completed = false;
        }
      });
      var status = none ? 'OFF' : completed ? 'ON' : 'HALF',
          cmp = order.get('renderCmp');
      cmp.changeIconClass(cmp.$.iconOrder, status);
    }
    return true;
  },

  selectAll: function (inSender, inEvent) {
    var mode = inEvent.selectAll ? 'ON' : 'OFF';
    _.each(this.$.orderSelector.collection.models, function (order) {
      var cmp = order.get('renderCmp');
      cmp.changeIconClass(cmp.$.iconOrder, mode);
      this.changeAllLines(null, {
        orderId: order.get('id'),
        mode: mode
      });
    }, this);
    this.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(mode === 'OFF');
  },

  associateSelected: function (inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('associateLines'),
        associatedOrderLineIds = [],
        modalSelector = this.parent.parent,
        selectedLine = modalSelector.selectedLine,
        relatedLines = selectedLine.get('relatedLines'),
        receipt = modalSelector.receipt ? modalSelector.receipt : OB.MobileApp.model.receipt,
        relatedLinePaid;

    if (OB.UTIL.isNullOrUndefined(relatedLines[0].bpName) || OB.UTIL.isNullOrUndefined(relatedLines[0].qty)) {
      relatedLines[0].bpName = receipt.get('bp').get('name');
      relatedLines[0].qty = selectedLine.getQty();
    }

    relatedLinePaid = _.find(relatedLines, function (line) {
      return line.obposIspaid === true;
    });

    _.each(
    this.ordersList.models, function (line) {
      if (line.get('ltype') === 'ORDERLINE' && line.get('toAssociate') === 1 && !associatedOrderLineIds.includes(line.get('orderlineId')) && selectedLine.get('id') !== line.get('orderlineId')) {
        var newRelatedLine = {};
        newRelatedLine.orderlineId = line.get('orderlineId');
        newRelatedLine.deferred = line.get('deferred');
        newRelatedLine.gross = line.get('gross');
        newRelatedLine.net = line.get('net');
        newRelatedLine.productName = line.get('productName');
        newRelatedLine.orderDocumentNo = line.get('documentNo');
        newRelatedLine.otherTicket = line.get('documentNo') === receipt.get('documentNo') ? false : true;
        newRelatedLine.qty = line.get('qty');
        newRelatedLine.deliveredQuantity = line.get('deliveredQuantity');
        newRelatedLine.promotions = line.get('promotions');
        newRelatedLine.bpName = line.get('bpName');
        newRelatedLine.obposIspaid = (OB.UTIL.isNullOrUndefined(relatedLinePaid)) ? false : relatedLinePaid.obposIspaid;
        relatedLines.push(newRelatedLine);
        associatedOrderLineIds.push(line.get('orderlineId'));
      }
    });
    if (selectedLine.get('product').get('quantityRule') === 'PP') {
      selectedLine.set('qty', selectedLine.get('relatedLines').length);
    }
    receipt.save(function () {
      selectedLine.trigger('change');
      modalSelector.hideSelector();
    });
    modalSelector.initialized = false;

    OB.UTIL.ProcessController.finish('associateLines', execution);
    return true;
  },

  clearAction: function () {
    this.ordersList.reset();
    this.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.changeTo(true);
    this.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(true);
    this.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(true);
    return true;
  },

  searchAction: function (inSender, inEvent) {
    var me = this,
        orderLinesToExclude = [],
        selectedLine = this.parent.parent.selectedLine,
        bp = this.parent.parent.receipt.get('bp').get('id'),
        filterModel = OB.Model.OrderAssociationsFilter;
    this.ordersList.reset();
    me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(true);
    _.each(selectedLine.get('relatedLines'), function (relatedLine) {
      orderLinesToExclude.push(relatedLine.orderlineId);
    });
    if (!inEvent.advanced) {
      this.waterfall('onDisableSearch');
    }

    function errorCallback(error) {
      me.$.renderLoading.hide();
      me.$.orderSelector.collection.reset();
      me.$.orderSelector.$.tempty.show();
      if (!inEvent.advanced) {
        me.waterfall('onEnableSearch');
      }
      if (error) {
        OB.UTIL.showError(OB.I18N.getLabel('OBPOS_OfflineWindowRequiresOnline'));
      }
    }

    function successCallbackOrders(data) {
      if (!inEvent.advanced) {
        me.waterfall('onEnableSearch');
      }
      if (data && !data.exception && data.length > 0) {
        me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(data.length === 0);
        var ordersLoaded = new Backbone.Collection();
        _.each(data.models, function (iter) {
          var order = _.find(
          ordersLoaded.models, function (ord) {
            return ord.id === iter.get('orderId');
          });
          if (!order) {
            order = new OB.Model.OrderAssociationsFilter({
              id: iter.get('orderId'),
              ltype: 'ORDER',
              documentNo: iter.get('documentNo'),
              orderedDate: moment(
              iter.get('orderDate'), "YYYY-MM-DD").format(
              OB.Format.date.toUpperCase()),
              bpName: iter.get('bpName'),
              bpId: iter.get('bpId'),
              orderTotal: iter.get('orderTotal'),
              lines: []
            });
            ordersLoaded.add(order);
          }

          var newline = {},
              attributes = iter.attributes,
              propt;
          for (propt in attributes) {
            if (attributes.hasOwnProperty(propt)) {
              newline[propt] = attributes[propt];
            }
          }
          delete newline.renderCmp; // remove
          // cycles
          newline.orderlineId = iter.get('orderlineId');
          newline.ltype = 'ORDERLINE';
          newline.deferred = true;
          newline.gross = iter.get('gross');
          newline.net = iter.get('net');
          newline.qty = iter.get('qty');
          newline.dateDelivered = iter.get('orderDate') ? moment(
          iter.get('orderDate'), "YYYY-MM-DD").format(
          OB.Format.date.toUpperCase()) : '';
          newline.lineNo = iter.get('lineNo');
          newline.productName = iter.get('productName');
          newline.documentNo = iter.get('documentNo');
          newline.bpName = iter.get('bpName');
          newline.orderId = iter.get('orderId');
          var orderLine = _.find(
          order.get('lines'), function (line) {
            return line.get('orderlineId') === newline.orderlineId;
          });

          var promotion = {};

          if (!OB.UTIL.isNullOrUndefined(newline.discount_ruleId) && !OB.UTIL.isNullOrUndefined(newline.discountType_id) && !OB.UTIL.isNullOrUndefined(newline.discount_displayedTotalAmount) && !OB.UTIL.isNullOrUndefined(newline.discount_userAmt)) {
            promotion.actualAmt = newline.discount_actualAmt;
            promotion.amt = newline.discount_totalAmt;
            promotion.discountType = newline.discountType_id;
            promotion.displayedTotalAmount = newline.discount_displayedTotalAmount;
            promotion.name = newline.discountType_name;
            promotion.ruleId = newline.discount_ruleId;
            promotion.userAmt = newline.discount_userAmt;
            if (orderLine && orderLine.get('promotions')) {
              orderLine.get('promotions').push(promotion);
            } else {
              newline.promotions = [];
              newline.promotions.push(promotion);
            }
          } else {
            newline.promotions = [];
          }
          delete newline.discount_actualAmt;
          delete newline.discount_totalAmt;
          delete newline.discountType_id;
          delete newline.discount_displayedTotalAmount;
          delete newline.discountType_name;
          delete newline.discount_ruleId;
          delete newline.discount_userAmt;
          if (!orderLine) {
            order.get('lines').push(
            new Backbone.Model(
            newline));
          }

        });
        var lines = [];
        _.each(ordersLoaded.models, function (order) {
          lines.push(order);
          _.each(order.get('lines'), function (line) {
            lines.push(line);
          });
        });
        me.$.renderLoading.hide();
        me.$.orderSelector.collection.reset(lines);
        me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.changeTo(true);
      } else if (data.exception && data.exception.message) {
        OB.UTIL.showError(OB.I18N.getLabel(data.exception.message));
        me.$.renderLoading.hide();
        me.$.orderSelector.collection.reset();
        me.$.orderSelector.$.tempty.show();
      } else {
        me.$.renderLoading.hide();
        me.$.orderSelector.collection.reset();
        me.$.orderSelector.$.tempty.show();
      }
    }

    this.$.orderSelector.$.tempty.hide();
    this.$.orderSelector.$.tbody.hide();
    this.$.orderSelector.$.tlimit.hide();
    this.$.renderLoading.show();

    var criteria = [];

    if (inEvent.orderby) {
      criteria._orderByClause = inEvent.orderby.name + ' ' + inEvent.orderby.direction + ', lineNo asc';
    } else {
      criteria._orderByClause = 'documentNo desc, lineNo asc';
    }

    criteria.forceRemote = true;
    criteria.remoteFilters = [];
    criteria.remoteFilters.push({
      columns: 'productId',
      value: selectedLine.get('product').get('id'),
      operator: '=',
      isId: true
    }, {
      columns: 'includeProductCategories',
      value: selectedLine.get('product').get('includeProductCategories'),
      operator: '='
    }, {
      columns: 'includeProducts',
      value: selectedLine.get('product').get('includeProducts'),
      operator: '='
    }, {
      columns: 'excluded',
      value: orderLinesToExclude.toString(),
      operator: '='
    });

    if (!inEvent.filters) {
      criteria.remoteFilters.push({
        columns: 'bpId',
        value: bp,
        operator: '=',
        isId: true
      });
    }
    _.each(inEvent.filters, function (flt) {
      criteria.remoteFilters.push({
        columns: flt.column,
        operator: flt.operator,
        value: flt.value
      });
    });

    OB.Dal.find(filterModel, criteria, function (data) {
      if (data) {
        successCallbackOrders(data);
      } else {
        errorCallback();
      }
    }, errorCallback);


    return true;
  },
  ordersList: null,

  init: function (model) {
    var me = this;
    this.ordersList = new Backbone.Collection();
    this.$.orderSelector.setCollection(this.ordersList);
    this.ordersList.on('verifyAssociateTicketsButton', function (item) {
      if (item.get('toAssociate') > 0) {
        me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(false);
      } else {
        me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(true);
        _.each(
        me.ordersList.models, function (e) {
          if (e.get('toAssociate') > 0) {
            me.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(false);
            return;
          }
        });
      }
    });
  }
});

/* Modal definition */
enyo.kind({
  kind: 'OB.UI.ModalSelector',
  name: 'OB.UI.ModalAssociateTickets',
  topPosition: '45px',
  classes: 'modal_associateTickets',
  i18nHeader: 'OBPOS_SelectLinesToAssociate',
  body: {
    kind: 'OB.UI.ListOrders'
  },
  executeOnShow: function () {
    if (!this.initialized || (!OB.UTIL.isNullOrUndefined(this.args) && !OB.UTIL.isNullOrUndefined(this.args.receipt))) {
      this.inherited(arguments);
      this.selectedLine = this.args.selectedLines[0];
      this.receipt = this.args.receipt;
      this.$.body.$.listOrders.clearAction();
      this.getFilterSelectorTableHeader().clearFilter();
      var businessPartner = _.find(OB.Model.OrderAssociationsFilter.getProperties(), function (prop) {
        return prop.name === 'bpId';
      }, this);
      businessPartner.preset.id = this.model.get('order').get('bp').get('id');
      businessPartner.preset.name = this.model.get('order').get('bp').get('_identifier');
      this.$.body.$.listOrders.searchAction(null, {
        originServer: this.args.originServer
      });
      if (!this.notClear) {
        this.$.body.$.listOrders.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAssociateSelected.setDisabled(true);
        this.$.body.$.listOrders.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonSelectAll.setDisabled(true);
      }
      OB.MobileApp.view.scanningFocus(false);
    }
  },
  executeOnHide: function () {
    this.inherited(arguments);
    OB.MobileApp.view.scanningFocus(true);
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.listOrders.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function () {
    return this.$.body.$.listOrders.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.buttonAdvancedFilter;
  },
  getAdvancedFilterDialog: function () {
    return 'OBPOS_modalAdvancedFilterOrders';
  },
  init: function (model) {
    this.inherited(arguments);
    this.initialized = false;
    this.$.body.$.listOrders.$.orderSelector.$.theader.$.modalOrderScrollableHeader.$.filterSelector.$.entityFilterText.skipAutoFilterPref = true;
  }
});

/* Advanced filter definition */
enyo.kind({
  kind: 'OB.UI.ModalAdvancedFilters',
  name: 'OB.UI.ModalAdvancedFilterOrders',
  initComponents: function () {
    this.inherited(arguments);
    this.setFilters(OB.Model.OrderAssociationsFilter.getFilterPropertiesWithSelectorPreference());
  }
});