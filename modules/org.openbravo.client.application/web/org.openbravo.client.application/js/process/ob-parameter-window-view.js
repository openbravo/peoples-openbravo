/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012-2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBParameterWindowView', isc.OBBaseParameterWindowView);

// == OBParameterWindowView ==
//   OBParameterWindowView is the implementation of OBBaseParameterWindowView 
//   for parameter windows, this is, Process Definition with Standard UIPattern.
//   It contains a series of parameters (fields) and, optionally, a grid.
isc.OBParameterWindowView.addProperties({
  // Set later inside initWidget
  firstFocusedItem: null,

  viewGrid: null,

  addNewButton: null,

  gridFields: [],

  initWidget: function () {
    var params, view = this;
    this.baseParams.processId = this.processId;

    this.Super('initWidget', arguments);

    params = isc.shallowClone(this.baseParams);
    if (this.sourceView) {
      params.context = this.sourceView.getContextInfo(false, true, true, true);
    }

    OB.RemoteCallManager.call('org.openbravo.client.application.process.DefaultsProcessActionHandler', {}, params, function (rpcResponse, data, rpcRequest) {
      view.handleDefaults(data);
    });

  },

  buildButtonLayout: function () {
    var view = this,
        buttonLayout = [],
        okButton, newButton, cancelButton, i;
    // Buttons

    function actionClick() {
      var hasErrors = false,
          grid, fields, selection, len, allRows, lineNumbers, i, j, record, undef;
      view.messageBar.hide();
      if (view.grid && view.grid.viewGrid) {
        grid = view.grid.viewGrid;
        fields = grid.getFields();
        selection = grid.getSelectedRecords() || [];
        len = selection.length;
        allRows = grid.data.allRows || grid.data.localData || grid.data;
        for (i = 0; i < len; i++) {
          record = grid.getEditedRecord(grid.getRecordIndex(selection[i]));
          for (j = 0; j < fields.length; j++) {
            if (fields[j].required) {
              if (record[fields[j].name] === null || record[fields[j].name] === '' || record[fields[j] === undef]) {
                hasErrors = true;
                if (lineNumbers === undef) {
                  lineNumbers = grid.getRecordIndex(selection[i]).toString();
                } else {
                  lineNumbers = lineNumbers + "," + grid.getRecordIndex(selection[i]).toString();
                }
              }
            }
          }
        }
      }
      if (!hasErrors) {
        if (view.validate()) {
          view.doProcess(this._buttonValue);
        } else {
          // If the messageBar is visible, it means that it has been set due to a custom validation inside view.validate()
          // so we don't want to overwrite it with the generic OBUIAPP_ErrorInFields message
          if (!view.messageBar.isVisible()) {
            view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
          }
        }
      } else {
        view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_FillMandatoryFields') + " " + lineNumbers);
      }
    }

    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Done'),
      realTitle: '',
      _buttonValue: 'DONE',
      click: actionClick
    });

    if (this.popup) {
      view.firstFocusedItem = okButton;
      buttonLayout.push(isc.LayoutSpacer.create({}));
    }

    if (this.buttons && !isc.isA.emptyObject(this.buttons)) {
      for (i in this.buttons) {
        if (this.buttons.hasOwnProperty(i)) {

          newButton = isc.OBFormButton.create({
            title: this.buttons[i],
            realTitle: '',
            _buttonValue: i,
            click: actionClick
          });
          buttonLayout.push(newButton);
          OB.TestRegistry.register('org.openbravo.client.application.process.pickandexecute.button.' + i, newButton);

          // pushing a spacer
          if (this.popup) {
            buttonLayout.push(isc.LayoutSpacer.create({
              width: 32
            }));
          }
        }
      }
    } else {
      buttonLayout.push(okButton);
      OB.TestRegistry.register('org.openbravo.client.application.process.pickandexecute.button.ok', okButton);
      if (this.popup) {
        buttonLayout.push(isc.LayoutSpacer.create({
          width: 32
        }));
      }
    }

    if (this.popup) {
      cancelButton = isc.OBFormButton.create({
        title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
        realTitle: '',
        click: function () {
          view.closeClick();
        }
      });
      buttonLayout.push(cancelButton);
      buttonLayout.push(isc.LayoutSpacer.create({}));
      OB.TestRegistry.register('org.openbravo.client.application.process.pickandexecute.button.cancel', cancelButton);
    }
    return buttonLayout;
  },
  handleResponse: function (refresh, message, responseActions, retryExecution, data) {
    var window = this.parentWindow,
        tab = OB.MainView.TabSet.getTab(this.viewTabId),
        i;

    // change title to done
    if (tab) {
      tab.setTitle(OB.I18N.getLabel('OBUIAPP_ProcessTitle_Done', [this.tabTitle]));
    }

    if (data.showResultsInProcessView) {
      if (!this.resultLayout) {
        this.resultLayout = isc.HLayout.create({
          width: '100%',
          height: '*'
        });
        this.addMember(this.resultLayout);
      } else {
        // clear the resultLayout
        this.resultLayout.setMembers([]);
      }
    }

    this.showProcessing(false);
    if (message) {
      if (this.popup) {
        if (!retryExecution) {
          if (message.title) {
            this.buttonOwnerView.messageBar.setMessage(message.severity, message.title, message.text);
          } else {
            this.buttonOwnerView.messageBar.setMessage(message.severity, message.text);
          }
        } else {
          // Popup has no message bar, showing the message in a warn popup
          isc.warn(message.text);
        }
      } else {
        if (message.title) {
          this.messageBar.setMessage(message.severity, message.title, message.text);
        } else {
          this.messageBar.setMessage(message.severity, message.text);
        }
      }
    }

    if (!retryExecution) {
      this.disableFormItems();
    } else {
      // Show again all toolbar buttons so the process
      // can be called again
      if (this.toolBarLayout) {
        for (i = 0; i < this.toolBarLayout.children.length; i++) {
          if (this.toolBarLayout.children[i].show) {
            this.toolBarLayout.children[i].show();
          }
        }
      }
      if (this.popupButtons) {
        this.popupButtons.show();
      }
    }

    if (responseActions) {
      responseActions._processView = this;
      OB.Utilities.Action.executeJSON(responseActions, null, null, this);
    }

    if (this.popup && !retryExecution) {
      this.buttonOwnerView.setAsActiveView();

      if (refresh) {
        window.refresh();
      }

      this.closeClick = function () {
        return true;
      }; // To avoid loop when "Super call"
      this.parentElement.parentElement.closeClick(); // Super call
    }
  },

  doProcess: function (btnValue) {
    var i, tmp, view = this,
        grid, allProperties = (this.sourceView && this.sourceView.getContextInfo(false, true, false, true)) || {},
        selection, len, allRows, params, tab;
    // activeView = view.parentWindow && view.parentWindow.activeView,  ???.
    if (this.resultLayout && this.resultLayout.destroy) {
      this.resultLayout.destroy();
      delete this.resultLayout;
    }
    this.showProcessing(true);

    // change tab title to show executing...
    tab = OB.MainView.TabSet.getTab(this.viewTabId);
    if (tab) {
      tab.setTitle(OB.I18N.getLabel('OBUIAPP_ProcessTitle_Executing', [this.tabTitle]));
    }

    if (this.grid) {
      // TODO: Support for multiple grids
      grid = this.grid.viewGrid;
      selection = grid.getSelectedRecords() || [];
      len = selection.length;
      allRows = grid.data.allRows || grid.data.localData || grid.data;
      allProperties._selection = [];
      allProperties._allRows = [];

      for (i = 0; i < len; i++) {
        tmp = isc.addProperties({}, selection[i], grid.getEditedRecord(grid.getRecordIndex(selection[i])));
        allProperties._selection.push(tmp);
      }

      len = (allRows && allRows.length) || 0;
      // Only send _allRows if all rows are cached
      if (!(grid.data.resultSize) || (len < grid.data.resultSize)) {
        for (i = 0; i < len; i++) {
          tmp = isc.addProperties({}, allRows[i], grid.getEditedRecord(grid.getRecordIndex(allRows[i])));
          allProperties._allRows.push(tmp);
        }
      }
    }

    allProperties._buttonValue = btnValue || 'DONE';

    allProperties._params = this.getContextInfo();

    OB.RemoteCallManager.call(this.actionHandler, allProperties, {
      processId: this.processId,
      windowId: this.windowId
    }, function (rpcResponse, data, rpcRequest) {
      view.handleResponse(true, (data && data.message), (data && data.responseActions), (data && data.retryExecution), data);
    });
  }
});