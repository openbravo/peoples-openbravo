/*
 ************************************************************************************
 * Copyright (C) 2018-2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, enyo, _*/

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.OpenRelatedReceipts_btnApply',
  classes: 'obUiOpenRelatedReceiptsBtnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  events: {
    onApplyChanges: '',
    onExecuteCallback: ''
  },
  tap: function() {
    this.inherited(arguments);
    this.doExecuteCallback({
      executeCallback: false
    });
    this.doHideThisPopup();
    this.doApplyChanges();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.OpenRelatedReceipts_btnCancel',
  classes: 'obUiOpenRelatedReceiptsBtnCancel',
  isDefaultAction: false,
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButtonOpenRelatedReceipts',
  kind: 'OB.UI.CheckboxButton',
  classes: 'obUiCheckboxButtonOpenRelatedReceipts span1',
  events: {
    onLineSelected: ''
  },
  handlers: {
    onCheckAll: 'checkAll'
  },
  checkAll: function(inSender, inEvent) {
    if (inEvent.checked) {
      this.check();
    } else {
      this.unCheck();
    }
  },
  tap: function() {
    this.inherited(arguments);
    this.doLineSelected({
      selected: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.RelatedReceipt',
  classes: 'obUiRelatedReceipt',
  handlers: {
    onLineSelected: 'lineSelected'
  },
  components: [
    {
      kind: 'OB.UI.CheckboxButtonOpenRelatedReceipts',
      name: 'checkboxButtonOpenRelatedReceipts',
      classes: 'obUiRelatedReceipt-checkboxButtonOpenRelatedReceipts'
    },
    {
      name: 'documentNo',
      classes: 'obUiRelatedReceipt-documentNo span4'
    },
    {
      name: 'orderedDate',
      classes: 'obUiRelatedReceipt-orderedDate span2'
    },
    {
      name: 'amount',
      classes: 'obUiRelatedReceipt-amount span2'
    },
    {
      name: 'pending',
      classes: 'obUiRelatedReceipt-pending span2'
    },
    {
      classes: 'obUiRelatedReceipt-element1'
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    var symbol = OB.MobileApp.model.get('terminal').symbol,
      symbolAtRight = OB.MobileApp.model.get('terminal')
        .currencySymbolAtTheRight;
    this.$.documentNo.setContent(this.order.get('documentNo'));
    this.$.orderedDate.setContent(
      OB.I18N.formatDate(new Date(this.order.get('orderDate')))
    );
    this.$.amount.setContent(
      OB.I18N.formatCurrencyWithSymbol(
        this.order.get('amount'),
        symbol,
        symbolAtRight
      )
    );
    this.$.pending.setContent(
      OB.I18N.formatCurrencyWithSymbol(
        this.order.get('pending'),
        symbol,
        symbolAtRight
      )
    );
  },
  lineSelected: function(inSender, inEvent) {
    inEvent.selectedLine = this.order.id;
  }
});

enyo.kind({
  name: 'OB.UI.ModalOpenRelatedReceipts',
  kind: 'OB.UI.ModalAction',
  classes: 'obUiModalOpenRelatedReceipts',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCheckedAll: 'checkedAll',
    onLineSelected: 'lineSelected',
    onExecuteCallback: 'executeCallback'
  },
  bodyContent: {
    kind: 'Scroller',
    classes: 'obUiModalOpenRelatedReceipts-bodyContent',
    thumb: true,
    components: [
      {
        name: 'attributes',
        classes: 'obUiModalOpenRelatedReceipts-bodyContent-attributes'
      }
    ]
  },
  bodyButtons: {
    classes: 'obUiModalOpenRelatedReceipts-bodyButtons',
    components: [
      {
        kind: 'OB.UI.OpenRelatedReceipts_btnApply',
        classes:
          'obUiModalOpenRelatedReceipts-bodyButtons-obUiOpenRelatedReceiptsBtnApply'
      },
      {
        kind: 'OB.UI.OpenRelatedReceipts_btnCancel',
        classes:
          'obUiModalOpenRelatedReceipts-bodyButtons-obUiOpenRelatedReceiptsBtnCancel'
      }
    ]
  },
  lineSelected: function(inSender, inEvent) {
    var selectedOrder = _.find(this.args.models, function(model) {
      return model.id === inEvent.selectedLine;
    });
    if (inEvent.selected) {
      this.selectedOrders.push(selectedOrder.attributes);
    } else {
      var index;
      for (index = 0; index < this.selectedOrders.length; index++) {
        var order = this.selectedOrders[index];
        if (order.id === selectedOrder.id) {
          break;
        }
      }
      this.selectedOrders.splice(index, 1);
    }
    this.$.bodyButtons.$.openRelatedReceipts_btnApply.setDisabled(
      _.reduce(
        this.$.bodyContent.$.attributes.$,
        function(count, line) {
          return line.$.checkboxButtonOpenRelatedReceipts.checked
            ? count + 1
            : count;
        },
        0
      ) === 0
    );
  },
  applyChanges: function(inSender, inEvent) {
    this.args.callback(this.selectedOrders);
  },
  executeCallback: function(inSender, inEvent) {
    this.execCallback = inEvent.executeCallback;
  },
  executeOnShow: function() {
    var lineNum = 0,
      model,
      i;
    this.selectedOrders = JSON.parse(JSON.stringify(this.args.models));
    this.execCallback = true;
    this.$.bodyContent.$.attributes.destroyComponents();
    this.$.header.destroyComponents();
    this.$.header.createComponent({
      name: 'CheckAllHeaderDocNum',
      classes: 'obUiModalOpenRelatedReceipts-header-checkAllHeaderDocNum',
      components: [
        {
          content: OB.I18N.getLabel('OBPOS_OpenRelatedReceiptsTitle'),
          name: 'headerLbl',
          classes:
            'obUiModalOpenRelatedReceipts-checkAllHeaderDocNum-headerLbl span12'
        },
        {
          classes: 'obUiModalOpenRelatedReceipts-checkAllHeaderDocNum-element1'
        }
      ]
    });
    this.$.header.addClass('obUiModalOpenRelatedReceipts-header');
    this.$.header.createComponent({
      name: 'CheckAllHeader',
      classes: 'obUiModalOpenRelatedReceipts-header-checkAllHeader',
      components: [
        {
          name: 'documentNoLbl',
          content: OB.I18N.getLabel('OBPOS_DocumentNo'),
          classes:
            'obUiModalOpenRelatedReceipts-checkAllHeader-documentNoLbl span4'
        },
        {
          name: 'orderedDateLbl',
          content: OB.I18N.getLabel('OBPOS_DateOrdered'),
          classes:
            'obUiModalOpenRelatedReceipts-checkAllHeader-orderedDateLbl span2'
        },
        {
          name: 'amountLbl',
          content: OB.I18N.getLabel('OBPOS_AmountOfCash'),
          classes: 'obUiModalOpenRelatedReceipts-checkAllHeader-amountLbl span2'
        },
        {
          name: 'pendingLbl',
          content: OB.I18N.getLabel('OBPOS_Pending'),
          classes:
            'obUiModalOpenRelatedReceipts-checkAllHeader-pendingLbl span2'
        },
        {
          classes: 'obUiModalOpenRelatedReceipts-checkAllHeader-element1'
        }
      ]
    });

    for (i = 1; i < this.args.models.length; i++) {
      model = this.args.models[i];
      this.$.bodyContent.$.attributes.createComponent({
        kind: 'OB.UI.RelatedReceipt',
        name: 'line' + lineNum,
        order: model
      });
      lineNum++;
    }
    this.waterfall('onCheckAll', {
      checked: true
    });
    this.$.bodyButtons.$.openRelatedReceipts_btnApply.setDisabled(false);
    this.$.header.render();
    this.$.bodyContent.$.attributes.render();
  },
  executeOnHide: function() {
    if (this.execCallback) {
      this.args.callback([this.args.models[0]]);
    }
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.bodyContent.$.attributes;
  }
});
