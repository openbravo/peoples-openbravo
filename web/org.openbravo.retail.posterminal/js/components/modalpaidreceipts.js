/*
 ************************************************************************************
 * Copyright (C) 2014-2018 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB, Backbone, _, moment, enyo */


/*header of scrollable table*/
enyo.kind({
  name: 'OB.UI.ModalPRScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  events: {
    onSearchAction: '',
    onClearAction: ''
  },
  handlers: {
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
          style: 'width: 100%',
          skipAutoFilterPref: 'OBPOS_remote.order'

        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'clearButton',
          classes: 'btnlink-gray btn-icon-small btn-icon-clear',
          style: 'width: 100px; margin: 0px 5px 8px 19px;',
          ontap: 'clearAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          kind: 'OB.UI.SmallButton',
          name: 'searchButton',
          classes: 'btnlink-yellow btn-icon-small btn-icon-search',
          style: 'width: 100px; margin: 0px 0px 8px 5px;',
          ontap: 'searchAction'
        }]
      }]
    }, {
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblStartDate'));
          },
          style: 'width: 200px;  margin: 0px 0px 2px 5px;'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getLabel('OBPOS_LblEndDate'));
          },
          style: 'width 200px; margin: 0px 0px 2px 65px;'
        }]
      }]
    }, {
      style: 'display: table;',
      components: [{
        style: 'display: table-cell;',
        components: [{
          kind: 'enyo.Input',
          name: 'startDate',
          size: '10',
          type: 'text',
          style: 'width: 100px;  margin: 0px 0px 8px 5px;',
          onchange: 'searchAction'
        }]
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getDateFormatLabel());
          },
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }, {
        kind: 'enyo.Input',
        name: 'endDate',
        size: '10',
        type: 'text',
        style: 'width: 100px;  margin: 0px 0px 8px 50px;',
        onchange: 'searchAction'
      }, {
        style: 'display: table-cell;',
        components: [{
          tag: 'h4',
          initComponents: function () {
            this.setContent(OB.I18N.getDateFormatLabel());
          },
          style: 'width: 100px; color:gray;  margin: 0px 0px 8px 5px;'
        }]
      }]
    }]
  }],
  showValidationErrors: function (stDate, endDate) {
    var me = this;
    if (stDate === false) {
      this.$.startDate.addClass('error');
      setTimeout(function () {
        me.$.startDate.removeClass('error');
      }, 5000);
    }
    if (endDate === false) {
      this.$.endDate.addClass('error');
      setTimeout(function () {
        me.$.endDate.removeClass('error');
      }, 5000);
    }
  },
  disableFilterText: function (value) {
    this.$.filterText.setDisabled(value);
  },
  disableFilterButtons: function (value) {
    this.$.searchButton.setDisabled(value);
    this.$.clearButton.setDisabled(value);
  },
  clearAction: function () {
    if (!this.$.filterText.disabled) {
      this.$.filterText.setValue('');
    }
    this.$.startDate.setValue('');
    this.$.endDate.setValue('');
    this.doClearAction();
  },

  getDateFilters: function () {
    var startDate, endDate, startDateValidated = true,
        endDateValidated = true,
        formattedStartDate = '',
        formattedEndDate = '';
    startDate = this.$.startDate.getValue();
    endDate = this.$.endDate.getValue();

    if (startDate !== '') {
      startDateValidated = OB.Utilities.Date.OBToJS(startDate, OB.Format.date);
      if (startDateValidated) {
        formattedStartDate = OB.Utilities.Date.JSToOB(startDateValidated, 'yyyy-MM-dd');
      }
    }

    if (endDate !== '') {
      endDateValidated = OB.Utilities.Date.OBToJS(endDate, OB.Format.date);
      if (endDateValidated) {
        formattedEndDate = OB.Utilities.Date.JSToOB(endDateValidated, 'yyyy-MM-dd');
      }
    }

    if (startDate !== '' && startDateValidated && endDate !== '' && endDateValidated) {
      if (moment(endDateValidated).diff(moment(startDateValidated)) < 0) {
        endDateValidated = null;
        startDateValidated = null;
      }
    }

    if (startDateValidated === null || endDateValidated === null) {
      this.showValidationErrors(startDateValidated !== null, endDateValidated !== null);
      return false;
    }
    this.$.startDate.removeClass('error');
    this.$.endDate.removeClass('error');

    this.filters = _.extend(this.filters, {
      startDate: formattedStartDate,
      endDate: formattedEndDate
    });

    return true;
  },

  searchAction: function () {
    var params = this.parent.parent.parent.parent.parent.parent.parent.parent.params;

    this.filters = {
      documentType: params.isQuotation ? ([OB.MobileApp.model.get('terminal').terminalType.documentTypeForQuotations]) : ([OB.MobileApp.model.get('terminal').terminalType.documentType, OB.MobileApp.model.get('terminal').terminalType.documentTypeForReturns]),
      docstatus: params.isQuotation ? 'UE' : null,
      isQuotation: params.isQuotation ? true : false,
      isLayaway: params.isLayaway ? true : false,
      isReturn: params.isReturn ? true : false,
      filterText: this.$.filterText.getValue(),
      pos: OB.MobileApp.model.get('terminal').id,
      client: OB.MobileApp.model.get('terminal').client,
      organization: OB.MobileApp.model.get('terminal').organization
    };

    if (!this.getDateFilters()) {
      return true;
    }

    this.doSearchAction({
      filters: this.filters
    });
    return true;
  }
});

enyo.kind({
  name: 'OB.UI.ModalVerifiedReturns',
  kind: 'OB.UI.ModalSelector',
  topPosition: '70px',
  i18nHeader: 'OBPOS_LblPaidReceipts',
  published: {
    params: null
  },
  body: {
    kind: 'OB.UI.ReceiptsForVerifiedReturnsList'
  },
  getFilterSelectorTableHeader: function () {
    return this.$.body.$.receiptsForVerifiedReturnsList.$.openreceiptslistitemprinter.$.theader.$.modalVerifiedReturnsScrollableHeader.$.filterSelector;
  },
  getAdvancedFilterBtn: function () {
    return this.$.body.$.receiptsForVerifiedReturnsList.$.openreceiptslistitemprinter.$.theader.$.modalVerifiedReturnsScrollableHeader.$.advancedFilterWindowButtonVerifiedReturns;
  },
  getAdvancedFilterDialog: function () {
    return 'modalAdvancedFilterVerifiedReturns';
  },
  executeOnShow: function () {
    if (!this.initialized) {
      this.inherited(arguments);
      this.getFilterSelectorTableHeader().clearFilter();
    }
  },
  init: function (model) {
    this.model = model;
  }
});