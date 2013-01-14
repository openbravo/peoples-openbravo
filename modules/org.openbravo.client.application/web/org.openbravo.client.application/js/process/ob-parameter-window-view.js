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
 * All portions are Copyright (C) 2012-2013 Openbravo SLU
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
        okButton, newButton, cancelButton, view = this;

    // Buttons

    function actionClick() {
      view.messageBar.hide();
      if (view.validate()) {
        view.doProcess(this._buttonValue);
      } else {
        // If the messageBar is visible, it means that it has been set due to a custom validation inside view.validate()
        // so we don't want to overwrite it with the generic OBUIAPP_ErrorInFields message
        if (!view.messageBar.isVisible()) {
          view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('OBUIAPP_ErrorInFields'));
        }
      }
    }

    okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUIAPP_Done'),
      realTitle: '',
      _buttonValue: 'DONE',
      click: actionClick
    });

    cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      realTitle: '',
      click: function () {
        view.closeClick();
      }
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
      buttonLayout.push(cancelButton);
      buttonLayout.push(isc.LayoutSpacer.create({}));
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

    // Parameters
    if (this.viewProperties.fields) {
      for (i = 0; i < this.viewProperties.fields.length; i++) {
        field = this.viewProperties.fields[i];
        field = isc.addProperties({
          view: this
        }, field);

        if (field.isGrid) {
          this.grid = isc.OBPickAndExecuteView.create(field);
        } else {
          items.push(field);
        }
      }

      this.theForm = isc.DynamicForm.create({
        width: '99%',
        //height: '100%',
        titleSuffix: '',
        requiredTitleSuffix: '',
        autoFocus: true,
        titleOrientation: 'top',
        numCols: 4,
        colWidths: ['*', '*', '*', '*']
      });

      this.theForm.setItems(items);
      this.members.push(this.theForm);
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

    OB.RemoteCallManager.call('org.openbravo.client.application.process.DefaultsProcessActionHandler', {}, {
      processId: this.processId
    }, function (rpcResponse, data, rpcRequest) {
      view.handleDefaults(data);
    });
  },

  handleResponse: function (refresh, message, responseActions) {
    var window = this.parentWindow,
        tab = OB.MainView.TabSet.getTab(this.viewTabId), i;

    // change title to done
    if (tab) {
      tab.setTitle(OB.I18N.getLabel('OBUIAPP_ProcessTitle_Done', [this.tabTitle]));
    }

    this.showProcessing(false);
    if (message) {
      if (this.popup) {
        this.buttonOwnerView.messageBar.setMessage(message.severity, message.text);
      } else {
        this.messageBar.setMessage(message.severity, message.text);
      }
    }

    if (!message || message.validationError !== 'Y') {
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
    }

    if (responseActions) {
      OB.Utilities.Action.executeJSON(responseActions, null, null, this);
    }

    if (this.popup) {
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
    var viewGrid;

    if (this.theForm) {
      return this.theForm.validate();
    }

    if (this.grid) {
      viewGrid = this.grid.viewGrid;

      viewGrid.endEditing();
      return !viewGrid.hasErrors();
    }
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
      allRows = grid.data.allRows || grid.data;
      allProperties._selection = [];
      allProperties._allRows = [];

      for (i = 0; i < len; i++) {
        tmp = isc.addProperties({}, selection[i], grid.getEditedRecord(selection[i]));
        allProperties._selection.push(tmp);
      }

      len = (allRows && allRows.length) || 0;

      for (i = 0; i < len; i++) {
        tmp = isc.addProperties({}, allRows[i], grid.getEditedRecord(allRows[i]));
        allProperties._allRows.push(tmp);
      }
    }

    allProperties._buttonValue = btnValue || 'DONE';

    allProperties._params = {};
    if (this.theForm && this.theForm.getItems) {
      params = this.theForm.getItems();
      for (i = 0; i < params.length; i++) {
        allProperties._params[params[i].name] = params[i].getValue();
      }
    }

    OB.RemoteCallManager.call(this.actionHandler, allProperties, {
      processId: this.processId,
      windowId: this.windowId
    }, function (rpcResponse, data, rpcRequest) {
      view.handleResponse(true, (data && data.message), (data && data.responseActions));
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
  }
});