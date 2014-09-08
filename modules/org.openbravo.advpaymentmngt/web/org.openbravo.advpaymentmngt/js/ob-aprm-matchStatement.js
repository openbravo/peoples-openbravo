/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM.MatchStatement = {};


OB.APRM.MatchStatement.onLoad = function (view) {
  var execute, grid = view.theForm.getItem('match_statement').canvas.viewGrid;

  grid.dataSourceOrig = grid.dataSource;
  grid.dataSource = null;
  execute = function (ok) {
    var onLoadCallback, newCriteria = {},
        params = {};
    if (grid.view.sourceView) {
      params.context = grid.view.sourceView.getContextInfo();
    }
    params.executeMatching = ok;
    onLoadCallback = function (response, data, request) {
      if (data.responseActions) {
        OB.Utilities.Action.executeJSON(data.responseActions, null, null, view);
      }
      grid.dataSource = grid.dataSourceOrig;
      view.onRefreshFunction(view);
    };
    OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.MatchStatementOnLoadActionHandler', {}, params, onLoadCallback);
    if (grid && grid.parentElement && grid.parentElement.messageBar && grid.parentElement.messageBar.text && grid.parentElement.messageBar.text.contents) {
      grid.parentElement.messageBar.text.setContents(grid.parentElement.messageBar.text.contents.replace(OB.I18N.getLabel('OBUIAPP_ClearFilters'), OB.I18N.getLabel('OBUIAPP_ClearFilters') + '<br/>' + OB.I18N.getLabel('APRM_GRID_PERSIST_MESSAGE')));
    }
  };
  isc.confirm(OB.I18N.getLabel('APRM_AlgorithmConfirm'), execute);
};

OB.APRM.MatchStatement.onRefresh = function (view) {
  var grid = view.theForm.getItem('match_statement').canvas.viewGrid;
  grid.filterByEditor();
};

OB.APRM.MatchStatement.onProcess = function (view, actionHandlerCall) {
  var execute;
  execute = function (ok) {
    if (ok) {
      actionHandlerCall(view);
    } else {
      view.parentElement.parentElement.closeClick();
    }
  };
  isc.confirm(OB.I18N.getLabel('APRM_ProcessReconciliation'), execute);
};


isc.ClassFactory.defineClass('APRMMatchStatGridButtonsComponent', isc.HLayout);

isc.APRMMatchStatGridButtonsComponent.addProperties({
  canExpandRecord: true,

  click: function () {
    this.grid.selectSingleRecord(this.record);
    return this.Super('click', arguments);
  },

  initWidget: function () {
    this.view = this.grid.view;
    var me = this,
        searchButton, addButton, clearButton, buttonSeparator1, buttonSeparator2;

    searchButton = isc.OBGridToolStripIcon.create({
      buttonType: 'search',
      originalPrompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_SEARCH_BUTTON'),
      prompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_SEARCH_BUTTON'),
      action: function () {
        var processId = '154CB4F9274A479CB38A285E16984539',
            grid = me.grid,
            record = me.record,
            standardWindow = grid.view.parentWindow.view.standardWindow;

        var process = standardWindow.openProcess({
          callerField: me,
          paramWindow: true,
          processId: processId,
          windowId: grid.view.windowId,
          externalParams: {
            bankStatementLineId: record.id,
            transactionDate: record.transactionDate
          },
          windowTitle: OB.I18N.getLabel('APRM_MATCHTRANSACTION_SEARCH_BUTTON', [this.title])
        });
      }
    });

    addButton = isc.OBGridToolStripIcon.create({
      buttonType: 'add',
      originalPrompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_ADD_BUTTON'),
      prompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_ADD_BUTTON'),
      action: function () {
        var processId = 'E68790A7B65F4D45AB35E2BAE34C1F39',
            grid = me.grid,
            record = me.record,
            standardWindow = grid.view.parentWindow.view.standardWindow;

        var process = standardWindow.openProcess({
          callerField: me,
          paramWindow: true,
          processId: processId,
          windowId: grid.view.windowId,
          externalParams: {
            bankStatementLineId: me.record.id
          },
          windowTitle: OB.I18N.getLabel('APRM_MATCHTRANSACTION_ADD_BUTTON', [this.title])
        });
      }
    });

    clearButton = isc.OBGridToolStripIcon.create({
      buttonType: 'clearRight',
      showDisabled: true,
      originalPrompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_DELETE_BUTTON'),
      prompt: OB.I18N.getLabel('APRM_MATCHTRANSACTION_DELETE_BUTTON'),
      action: function () {
        var callback, bankStatementLineId = me.record.id,
            view = me.grid.view;
        callback = function (response, data, request) {
          view.onRefreshFunction(view);
          if (data && data.message && data.message.severity === 'error') {
            view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, data.message.title, data.message.text);
          }
        };
        OB.RemoteCallManager.call('org.openbravo.advpaymentmngt.actionHandler.UnMatchTransactionActionHandler', {
          bankStatementLineId: bankStatementLineId
        }, {}, callback);
      }
    });

    buttonSeparator1 = isc.OBGridToolStripSeparator.create({});
    buttonSeparator2 = isc.OBGridToolStripSeparator.create({});

    // Disable clear button if record is not linked to a transaction
    clearButton.setDisabled(!me.record.cleared);

    this.addMembers([searchButton, buttonSeparator1, addButton, buttonSeparator2, clearButton]);
    this.Super('initWidget', arguments);
  }
});


isc.APRMMatchStatGridButtonsComponent.addProperties({
  cellAlign: 'center',

  height: 21,
  width: '100%',
  overflow: 'hidden',
  align: 'center',
  defaultLayoutAlign: 'center',
  styleName: 'OBGridToolStrip',
  layoutLeftMargin: -2,
  layoutRightMargin: 0,
  membersMargin: 4
});