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
 * All portions are Copyright (C) 2015 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBBaseParameterWindowView', isc.VLayout);

// == OBBaseParameterWindowView ==
//   OBBaseParameterWindowView is the base view that it can be extended by
// any process that use parameters defined in OBUIAPP_Parameter
isc.OBBaseParameterWindowView.addProperties({
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

  toolBarLayout: null,
  members: [],
  baseParams: {},
  formProps: {
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
  },

  initWidget: function () {
    var i, field, items = [],
        buttonLayout = [],
        view = this,
        newShowIf;

    buttonLayout = view.buildButtonLayout();
    
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
        this.theForm = isc.DynamicForm.create(this.formProps);

        this.theForm.setItems(items);
        this.members.push(this.theForm);
      }
    }
    if (this.grid) {
      this.members.push(this.grid);
    }


    if (this.popup) {
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

  },

  /*
   * Function that creates the layout with the buttons. Classes implementing OBBaseParameterWindowView
   * have to override this function to add the needed buttons. 
   */
  buildButtonLayout: function () {
    return [];
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