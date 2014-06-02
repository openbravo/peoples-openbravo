/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBParameterWindowView', isc.VLayout);

// == OBParameterWindowView ==
//   OBParameterWindowView is the view that represents parameter windows, this
//   is, Process Definition with Standard UIPattern. It contains a series of 
//   parameters (fields) and, optionally, a grid.
isc.OBParameterWindowView.addProperties({
  // Set default properties for the OBPopup container
  showMinimizeButton: true,
  showMaximizeButton: true,
  popupWidth: '90%',
  popupHeight: '90%',
  // Set later inside initWidget
  firstFocusedItem: null,

  // Set now pure P&E layout properties
  width: '100%',
  height: '100%',
  overflow: 'auto',
  autoSize: false,

  dataSource: null,

  viewGrid: null,

  addNewButton: null,

  gridFields: [],
  members: [],

  initWidget: function () {
    var i, field, items = [],
        buttonLayout = [],
        okButton, newButton, cancelButton, view = this,
        newShowIf, params;

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

    if (!this.popup) {
      this.toolBarLayout = isc.OBToolbar.create({
        view: this,
        leftMembers: [{}],
        rightMembers: buttonLayout
      });
      // this.toolBarLayout.addMems(buttonLayout);
      this.members.push(this.toolBarLayout);
    }

    // Message bar
    this.messageBar = isc.OBMessageBar.create({
      visibility: 'hidden',
      view: this
    });
    this.members.push(this.messageBar);

    newShowIf = function (item, value, form, values) {
      var currentValues = isc.shallowClone(values || form.view.getCurrentValues()),
          context = {},
          originalShowIfValue = false;

      OB.Utilities.fixNull250(currentValues);

      try {
        if (isc.isA.Function(this.originalShowIf)) {
          originalShowIfValue = this.originalShowIf(item, value, form, currentValues, context);
        } else {
          originalShowIfValue = isc.JSON.decode(this.originalShowIf);
        }
      } catch (_exception) {
        isc.warn(_exception + ' ' + _exception.message + ' ' + _exception.stack);
      }
      return originalShowIfValue;
    };

    // Parameters
    if (this.viewProperties.fields) {
      for (i = 0; i < this.viewProperties.fields.length; i++) {
        field = this.viewProperties.fields[i];
        field = isc.addProperties({
          view: this
        }, field);

        if (field.showIf) {
          field.originalShowIf = field.showIf;
          field.showIf = newShowIf;
        }
        if (field.isGrid) {
          this.grid = isc.OBPickAndExecuteView.create(field);
        } else {
          items.push(field);
        }
      }

      if (items.length !== 0) {
        // create form if there items to include
        this.theForm = isc.DynamicForm.create({
          paramWindow: this,
          width: '99%',
          titleSuffix: '',
          requiredTitleSuffix: '',
          autoFocus: true,
          titleOrientation: 'top',
          numCols: 4,
          showErrorIcons: false,
          colWidths: ['*', '*', '*', '*'],
          itemChanged: function (item, newValue) {
            var affectedParams, i, field;

            this.paramWindow.handleReadOnlyLogic();

            // Check validation rules (subordinated fields), when value of a
            // parent field is changed, all its subordinated are reset
            affectedParams = this.paramWindow.dynamicColumns[item.name];
            if (!affectedParams) {
              return;
            }
            for (i = 0; i < affectedParams.length; i++) {
              field = this.getField(affectedParams[i]);
              if (field && field.setValue) {
                field.setValue(null);
              }
            }
          }
        });

        this.theForm.setItems(items);
        this.members.push(this.theForm);
      }
    }
    if (this.grid) {
      this.members.push(this.grid);
    }


    if (this.popup) {
      this.firstFocusedItem = okButton;
      this.popupButtons = isc.HLayout.create({
        align: 'center',
        width: '100%',
        height: OB.Styles.Process.PickAndExecute.buttonLayoutHeight,
        members: [isc.HLayout.create({
          width: 1,
          overflow: 'visible',
          styleName: this.buttonBarStyleName,
          height: this.buttonBarHeight,
          defaultLayoutAlign: 'center',
          members: buttonLayout
        })]
      });
      this.members.push(this.popupButtons);
      this.closeClick = function () {
        this.closeClick = function () {
          return true;
        }; // To avoid loop when "Super call"
        this.parentElement.parentElement.closeClick(); // Super call
      };
    }
    this.loading = OB.Utilities.createLoadingLayout(OB.I18N.getLabel('OBUIAPP_PROCESSING'));
    this.loading.hide();
    this.members.push(this.loading);
    this.Super('initWidget', arguments);

    params = {
      processId: this.processId
    };

    if (this.sourceView) {
      params.context = this.sourceView.getContextInfo(false, true, true, true);
    }

    OB.RemoteCallManager.call('org.openbravo.client.application.process.DefaultsProcessActionHandler', {}, params, function (rpcResponse, data, rpcRequest) {
      view.handleDefaults(data);
    });
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

  disableFormItems: function () {
    var i, params;
    if (this.theForm && this.theForm.getItems) {
      params = this.theForm.getItems();
      for (i = 0; i < params.length; i++) {
        if (params[i].disable) {
          params[i].disable();
        }
      }
    }
  },

  // dummy required by OBStandardView.prepareGridFields
  setFieldFormProperties: function () {},

  validate: function () {
    var viewGrid, validForm;
    if (this.theForm) {
      validForm = this.theForm.validate();
      if (!validForm) {
        return validForm;
      }
    }

    if (this.grid) {
      viewGrid = this.grid.viewGrid;

      viewGrid.endEditing();
      return !viewGrid.hasErrors();
    }
    return true;
  },

  showProcessing: function (processing) {
    var i;
    if (processing) {
      if (this.theForm) {
        this.theForm.hide();
      }
      if (this.grid) {
        this.grid.hide();
      }
      if (this.popupButtons) {
        this.popupButtons.hide();
      }

      if (this.toolBarLayout) {
        for (i = 0; i < this.toolBarLayout.children.length; i++) {
          if (this.toolBarLayout.children[i].hide) {
            this.toolBarLayout.children[i].hide();
          }
        }
      }

      this.loading.show();
    } else {
      if (this.theForm) {
        this.theForm.show();
      }
      if (this.grid) {
        this.grid.show();
      }

      this.loading.hide();
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
  },

  handleDefaults: function (defaults) {
    var i, field, def;
    if (!this.theForm) {
      return;
    }

    for (i in defaults) {
      if (defaults.hasOwnProperty(i)) {
        def = defaults[i];
        field = this.theForm.getItem(i);
        if (field) {
          if (isc.isA.Object(def)) {
            if (def.identifier && def.value) {
              field.valueMap = field.valueMap || {};
              field.valueMap[def.value] = def.identifier;
              field.setValue(def.value);
            }
          } else {
            field.setValue(def);
          }
        }
      }
    }

    this.handleReadOnlyLogic();

    // redraw to execute display logic
    this.theForm.markForRedraw();
  },

  // Checks params with readonly logic enabling or disabling them based on it
  handleReadOnlyLogic: function () {
    var form, fields, i, field;

    form = this.theForm;
    if (!form) {
      return;
    }

    fields = form.getFields();
    for (i = 0; i < fields.length; i++) {
      field = form.getField(i);
      if (field.readOnlyIf && field.setDisabled) {
        field.setDisabled(field.readOnlyIf(form.getValues()));
      }
    }
  },

  getContextInfo: function () {
    var result = {},
        params, i;
    if (!this.theForm) {
      return result;
    }

    if (this.theForm && this.theForm.getItems) {
      params = this.theForm.getItems();
      for (i = 0; i < params.length; i++) {
        result[params[i].name] = params[i].getValue();
      }
    }

    return result;
  }
});