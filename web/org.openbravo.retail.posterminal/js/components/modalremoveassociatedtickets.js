/*
 ************************************************************************************
 * Copyright (C) 2016-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */
enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnApply',
  classes: 'obUiModalRemoveAssociationsBtnApply',
  isDefaultAction: true,
  i18nContent: 'OBPOS_LblApplyButton',
  processesToListen: ['removeAssociations'],
  events: {
    onApplyChanges: '',
    onLineSelected: ''
  },
  initComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.subscribe(this.processesToListen, this);
  },
  destroyComponents: function() {
    this.inherited(arguments);
    OB.UTIL.ProcessController.unSubscribe(this.processesToListen, this);
  },
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
    this.doApplyChanges();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnCancel',
  classes: 'obUiModalRemoveAssociationsBtnCancel',
  isDefaultAction: false,
  i18nContent: 'OBMOBC_LblCancel',
  tap: function() {
    this.inherited(arguments);
    this.doHideThisPopup();
  }
});

enyo.kind({
  kind: 'OB.UI.ModalDialogButton',
  name: 'OB.UI.ModalRemoveAssociations_btnSelectAll',
  classes: 'obUiModalRemoveAssociationsBtnSelectAll',
  i18nContent: 'OBPOS_lblSelectAll',
  events: {
    onCheckedAll: ''
  },
  checked: false,
  handlers: {
    onAllSelected: 'allSelected'
  },
  allSelected: function(inSender, inEvent) {
    if (inEvent.allSelected) {
      this.checked = true;
      this.setLabel(OB.I18N.getLabel('OBPOS_lblSelectNone'));
    } else {
      this.checked = false;
      this.setLabel(OB.I18N.getLabel('OBPOS_lblSelectAll'));
    }
    return true;
  },
  tap: function() {
    this.checked = !this.checked;
    if (this.checked) {
      this.setLabel(OB.I18N.getLabel('OBPOS_lblSelectNone'));
    } else {
      this.setLabel(OB.I18N.getLabel('OBPOS_lblSelectAll'));
    }
    this.inherited(arguments);
    this.doCheckedAll({
      checked: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButtonRemoveAllAssociations',
  kind: 'OB.UI.CheckboxButton',
  classes: 'obUiCheckboxButtonRemoveAllAssociations',
  events: {
    onCheckedAll: ''
  },
  handlers: {
    onAllSelected: 'allSelected'
  },
  allSelected: function(inSender, inEvent) {
    if (inEvent.allSelected) {
      this.check();
    } else {
      this.unCheck();
    }
    return true;
  },
  tap: function() {
    this.inherited(arguments);
    this.doCheckedAll({
      checked: this.checked
    });
  }
});

enyo.kind({
  name: 'OB.UI.CheckboxButtonRemoveAssociations',
  kind: 'OB.UI.CheckboxButton',
  classes: 'obUiCheckboxButtonRemoveAssociations',
  handlers: {
    onCheckAll: 'checkAll'
  },
  events: {
    onLineSelected: ''
  },
  checkAll: function(inSender, inEvent) {
    if (inEvent.checked && !this.checked) {
      this.check();
      this.doLineSelected({
        selected: true
      });
    } else if (!inEvent.checked && this.checked) {
      this.unCheck();
      this.doLineSelected({
        selected: false
      });
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
  name: 'OB.UI.AssociatedOrderLine',
  classes: 'obUiAssociatedOrderLine',
  handlers: {
    onLineSelected: 'lineSelected'
  },
  components: [
    {
      kind: 'OB.UI.CheckboxButtonRemoveAssociations',
      name: 'checkboxButtonRemoveAssociations',
      classes: 'obUiAssociatedOrderLine-checkboxButtonRemoveAssociations'
    },
    {
      kind: 'OB.UI.FormElement',
      name: 'formElementProductName',
      classes: 'obUiAssociatedOrderLine-formElementProductName',
      coreElement: {
        kind: 'OB.UI.FormElement.Input',
        name: 'productName',
        disabled: true,
        classes: 'obUiAssociatedOrderLine-productName',
        i18nLabel: 'OBPOS_LblProductName'
      }
    },
    {
      kind: 'OB.UI.FormElement',
      name: 'formElementOrderedQuantity',
      classes: 'obUiAssociatedOrderLine-formElementOrderedQuantity',
      coreElement: {
        kind: 'OB.UI.FormElement.Input',
        name: 'orderedQuantity',
        disabled: true,
        classes: 'obUiAssociatedOrderLine-orderedQuantity',
        i18nLabel: 'OBPOS_LblQty'
      }
    },
    {
      kind: 'OB.UI.FormElement',
      name: 'formElementDocumentNo',
      classes: 'obUiAssociatedOrderLine-formElementDocumentNo',
      coreElement: {
        kind: 'OB.UI.FormElement.Input',
        name: 'documentNo',
        disabled: true,
        classes: 'obUiAssociatedOrderLine-documentNo',
        i18nLabel: 'OBPOS_ticket'
      }
    },
    {
      kind: 'OB.UI.FormElement',
      name: 'formElementCustomer',
      classes: 'obUiAssociatedOrderLine-formElementCustomer',
      coreElement: {
        kind: 'OB.UI.FormElement.Input',
        name: 'customer',
        disabled: true,
        classes: 'obUiAssociatedOrderLine-customer',
        i18nLabel: 'OBPOS_LblCustomer'
      }
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.formElementProductName.coreElement.setValue(
      this.newAttribute.productName
    );
    this.$.formElementOrderedQuantity.coreElement.setValue(
      this.newAttribute.qty
    );
    this.$.formElementDocumentNo.coreElement.setValue(
      this.newAttribute.orderDocumentNo
    );
    this.$.formElementCustomer.coreElement.setValue(this.newAttribute.bpName);
  },
  lineSelected: function(inSender, inEvent) {
    inEvent.selectedLine = this.newAttribute.orderlineId;
    //Return false value to propagate the event until applyChanges function
    return false;
  }
});

enyo.kind({
  name: 'OB.UI.ModalRemoveAssociatedTickets',
  kind: 'OB.UI.Modal',
  classes: 'obUiModalRemoveAssociatedTickets',
  handlers: {
    onApplyChanges: 'applyChanges',
    onCheckedAll: 'checkedAll',
    onLineSelected: 'lineSelected'
  },
  header: {
    classes: 'obUiModalRemoveAssociatedTickets-header',
    components: [
      {
        name: 'serviceName',
        classes: 'obUiModalRemoveAssociatedTickets-header-serviceName'
      },
      {
        name: 'linesLabel',
        classes: 'obUiModalRemoveAssociatedTickets-header-linesLabel',
        init: function() {
          this.setContent(
            OB.I18N.getLabel('OBPOS_SelectAssociationsToRemoved')
          );
        }
      }
    ]
  },
  body: {
    classes: 'obUiModalRemoveAssociatedTickets-body',
    components: [
      {
        name: 'scrollerHeader',
        classes: 'obUiModalRemoveAssociatedTickets-body-scrollerHeader',
        components: [
          {
            kind: 'OB.UI.CheckboxButtonRemoveAllAssociations',
            name: 'checkboxButtonRemoveAllAssociations',
            classes:
              'obUiModalRemoveAssociatedTickets-body-scrollerHeader-checkboxButtonRemoveAllAssociations'
          },
          {
            name: 'productNameLbl',
            classes:
              'obUiModalRemoveAssociatedTickets-body-scrollerHeader-productNameLbl',
            init: function() {
              this.setContent(OB.I18N.getLabel('OBPOS_LblProductName'));
            }
          },
          {
            name: 'totalQtyLbl',
            classes:
              'obUiModalRemoveAssociatedTickets-body-scrollerHeader-totalQtyLbl',
            init: function() {
              this.setContent(OB.I18N.getLabel('OBPOS_LblQty'));
            }
          },
          {
            name: 'receiptLbl',
            classes:
              'obUiModalRemoveAssociatedTickets-body-scrollerHeader-receiptLbl',
            init: function() {
              this.setContent(OB.I18N.getLabel('OBPOS_ticket'));
            }
          },
          {
            name: 'customerLbl',
            classes:
              'obUiModalRemoveAssociatedTickets-body-scrollerHeader-customerLbl',
            init: function() {
              this.setContent(OB.I18N.getLabel('OBPOS_LblCustomer'));
            }
          }
        ]
      },
      {
        kind: 'Scroller',
        classes: 'obUiModalRemoveAssociatedTickets-body-scroller',
        thumb: true,
        horizontal: 'hidden',
        components: [
          {
            name: 'attributes',
            classes: 'obUiModalRemoveAssociatedTickets-scroller-attributes'
          }
        ]
      }
    ]
  },
  footer: {
    classes:
      'obUiModal-footer-mainButtons obUiModalRemoveAssociatedTickets-footer',
    components: [
      {
        kind: 'OB.UI.ModalRemoveAssociations_btnSelectAll',
        classes:
          'obUiModalRemoveAssociatedTickets-footer-obUiModalRemoveAssociationsBtnSelectAll'
      },
      {
        kind: 'OB.UI.ModalRemoveAssociations_btnCancel',
        classes:
          'obUiModalRemoveAssociatedTickets-footer-obUiModalRemoveAssociationsBtnCancel'
      },
      {
        kind: 'OB.UI.ModalRemoveAssociations_btnApply',
        classes:
          'obUiModalRemoveAssociatedTickets-footer-obUiModalRemoveAssociationsBtnApply'
      }
    ]
  },
  initComponents: function() {
    this.inherited(arguments);
    this.attributeContainer = this.$.body.$.attributes;
  },
  checkedAll: function(inSender, inEvent) {
    this.waterfall('onCheckAll', {
      checked: inEvent.checked
    });
    return true;
  },
  lineSelected: function(inSender, inEvent) {
    if (inEvent.selected) {
      this.selectedLines += 1;
    } else {
      this.selectedLines -= 1;
    }
    if (this.selectedLines === this.numberOfLines) {
      this.allSelected = true;
      this.waterfall('onAllSelected', {
        allSelected: this.allSelected
      });
    } else {
      if (this.allSelected) {
        this.allSelected = false;
        this.waterfall('onAllSelected', {
          allSelected: this.allSelected
        });
      }
    }

    var receiptLine = this.args.selectedLine;
    if (OB.UTIL.isNullOrUndefined(this.linesToAssociate)) {
      this.linesToAssociate = JSON.parse(
        JSON.stringify(receiptLine.get('relatedLines'))
      );
    }
    if (inEvent.selected) {
      this.linesToAssociate = this.linesToAssociate.filter(function(line) {
        return line.orderlineId !== inEvent.selectedLine;
      });
    } else {
      this.linesToAssociate.push(
        _.find(receiptLine.get('relatedLines'), function(line) {
          return line.orderlineId === inEvent.selectedLine;
        })
      );
    }
    return true;
  },
  applyChanges: function(inSender, inEvent) {
    var execution = OB.UTIL.ProcessController.start('removeAssociations'),
      receipt = this.args.receipt,
      receiptLine = this.args.selectedLine;
    if (this.linesToAssociate && this.linesToAssociate.length === 0) {
      receipt.deleteLinesFromOrder([receiptLine]);
    } else if (this.linesToAssociate && this.linesToAssociate.length > 0) {
      if (receiptLine.get('quantityRule') === 'PP') {
        receiptLine.set('qty', receiptLine.get('relatedLines').length);
      }
      receiptLine.set('relatedLines', this.linesToAssociate);
      receipt.trigger('updateServicePrices');
      receipt.save(function() {
        OB.UTIL.ProcessController.finish('removeAssociations', execution);
        return true;
      });
    }
    OB.UTIL.ProcessController.finish('removeAssociations', execution);
    return true;
  },
  executeOnShow: function() {
    var me = this,
      lineNum = 0;
    OB.UTIL.showLoading(false);
    this.$.header.$.serviceName.setContent(
      OB.I18N.getLabel('OBPOS_ServiceHeader', [
        me.args.selectedLine.get('product').get('_identifier')
      ])
    );
    this.$.body.$.attributes.destroyComponents();
    this.numberOfLines = 0;
    this.selectedLines = 0;
    this.allSelected = false;
    this.linesToAssociate = null;
    this.waterfall('onAllSelected', {
      allSelected: this.allSelected
    });
    _.each(me.args.selectedLine.get('relatedLines'), function(relatedLine) {
      if (
        (_.isUndefined(relatedLine.orderDocumentNo) ||
          _.isUndefined(relatedLine.bpName) ||
          _.isUndefined(relatedLine.qty)) &&
        me.args.receipt
      ) {
        var line = _.find(me.args.receipt.get('lines').models, function(line) {
          return line.id === relatedLine.orderlineId;
        });
        relatedLine.orderDocumentNo = me.args.receipt.get('documentNo');
        relatedLine.bpName = me.args.receipt.get('bp').get('name');
        if (!OB.UTIL.isNullOrUndefined(line)) {
          relatedLine.qty = line.get('qty');
        }
      }

      me.$.body.$.attributes.createComponent({
        kind: 'OB.UI.AssociatedOrderLine',
        name: 'line' + lineNum,
        classes:
          'obUiModalRemoveAssociatedTickets-attributes-obUiAssociatedOrderLine',
        newAttribute: relatedLine
      });
      lineNum++;
      me.numberOfLines += 1;
    });
    this.$.body.$.attributes.render();
  }
});
