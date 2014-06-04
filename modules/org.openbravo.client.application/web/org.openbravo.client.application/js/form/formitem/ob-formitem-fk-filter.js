/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

//== OBFKFilterTextItem ==
//Input used for filtering on FK fields.
isc.ClassFactory.defineClass('OBFKFilterTextItem', isc.OBListFilterItem);

isc.OBFKFilterTextItem.addProperties({
  operator: 'iContains',
  overrideTextMatchStyle: 'substring',
  allowExpressions: false,
  showOptionsFromDataSource: true,
  selectOnFocus: false,
  validateOnExit: true,

  multiple: true,
  multipleAppearance: 'picklist',
  multipleValueSeparator: ' or ',

  // only show the drop down on demand
  // this because we want to support partial values
  // for filtering, also getting trouble because values get
  // completely selected
  showPickListOnKeypress: true,
  filterOnKeypress: false,
  changeOnKeypress: true,
  addUnknownValues: true,
  defaultToFirstOption: false,

  emptyPickListMessage: OB.I18N.getLabel('OBUISC_ListGrid.emptyMessage'),

  init: function () {
    var me = this,
        grid = this.form.grid.sourceWidget,
        gridField = grid.getField(this.name),
        dataSource;

    // the textMatchStyle is sometimes overridden from the underlying
    // grid, this happens when used within the selector editor.
    // for foreign key fields we only support like/contains/substring
    // so force that
    this.textMatchStyle = this.overrideTextMatchStyle;

    // the data from the datasource will contain the id and the identifier
    // the value for the filter and the display are the same: the identifier
    this.displayField = this.criteriaDisplayField || OB.Constants.IDENTIFIER;
    this.valueField = this.criteriaDisplayField || OB.Constants.IDENTIFIER;

    this.pickListProperties = {

      // make sure that we send the same parameters as the grid
      onFetchData: function (criteria, requestProperties) {
        requestProperties = requestProperties || {};
        requestProperties.params = grid.getFetchRequestParams(requestProperties.params);
        delete me.forceReload;
      },

      fetchDelay: 400,
      // prevent aggressive local filtering by smartclient
      filterLocally: false,
      multipleValueSeparator: ' or ',
      dataProperties: {
        useClientFiltering: false
      },

      isSelected: function (record) {
        var i, values = this.formItem.getValue();
        if (values.length) {
          for (i = 0; i < values.length; i++) {
            if (record[me.displayField] === values[i]) {
              return true;
            }
          }
        }
        return record[me.displayField] === values;
      },

      // override data arrived to prevent the first entry from being
      // selected
      // this to handle the picklist in foreign key filter item. When a user
      // types a partial value maybe he/she wants to filter by this partial
      // value
      // auto-selecting the first value makes this impossible.
      // Therefore this option to prevent this.
      // There are maybe nicer points to do this overriding but this was the
      // place after the first item was selected.
      // This first selection happens in ScrollingMenu.dataChanged
      dataArrived: function (startRow, endRow) {
        var record, rowNum, i, values = this.formItem.getValue();
        this.Super('dataArrived', arguments);
        if (values) {
          if (!isc.isA.Array(values)) {
            values = [values];
          }
          for (rowNum = startRow; rowNum < (endRow + 1); rowNum++) {
            record = this.getRecord(rowNum);
            if (record && values.contains(record[me.displayField])) {
              this.selectRecord(record, true);
            }
          }
        }
      }
    };
    dataSource = OB.Datasource.create({
      dataURL: grid.getDataSource().dataURL,
      requestProperties: {
        params: {
          // distinct forces the distinct query on the server side
          _distinct: gridField.valueField || gridField.name
        }
      },
      fields: this.pickListFields
    });
    if (grid.Class === 'OBTreeGrid') {
      dataSource.requestProperties.params.tabId = grid.view.tabId;
    }
    this.setOptionDataSource(dataSource);

    this.Super('init', arguments);

    // don't validate for FK filtering, any value is allowed
    this.validators = [];

    // listen to data arrival in the grid
    // if data arrived we have to reload also
    this.observe(grid, "dataArrived", "observer.setForceReload()");

    this.multipleValueSeparator = ' or ';
  },

  destroy: function () {
    var grid = this.form && this.form.grid && this.form.grid.sourceWidget;
    if (grid) {
      this.ignore(grid, 'dataArrived');
    }
    return this.Super('destroy', arguments);
  },

  // When the selected value is part of the pickList the grid is already filtered,
  // so no additional request is required. But when there is a keyword entered,
  // the grid has to be filtered. Refer issue, https://issues.openbravo.com/view.php?id=26700.
  handleEditorExit: function () {
    var value = this.getValue(),
        performFetch = false,
        rows, i;
    if (this.pickList && this.pickList.data && (this.pickList.data.allRows || this.pickList.data.localData)) {
      rows = this.pickList.data.allRows || this.pickList.data.localData;
    }
    if (value && isc.isA.Array(value) && value.length > 0 && rows) {
      for (i = 0; i < value.length; i++) {
        if (value[i].indexOf('==') === 0) {
          value[i] = value[i].substring(2, value[i].length);
          if (rows.find('name', value[i]) === undefined) {
            performFetch = true;
          }
        }
      }
    } else {
      if (rows && rows.find('name', value)) {
        performFetch = true;
      }
    }
    if (performFetch) {
      this.Super('handleEditorExit', arguments);
    } else {
      return value;
    }
  },

  // note: can't override changed as it is used by the filter editor 
  // itself, see the RecordEditor source code and the changed event
  change: function (form, item, value, oldValue) {
    this._hasChanged = true;
    this.Super('change', arguments);
  },

  blur: function () {
    if (this._hasChanged) {
      this.form.grid.performAction();
    }
    delete this._hasChanged;
    this.Super('blur', arguments);
  },

  // overridden otherwise the picklist fields from the grid field
  // are being used
  getPickListFields: function () {
    return [{
      name: this.displayField,
      escapeHTML: true
    }];
  },

  itemHoverHTML: function () {
    return this.getElementValue();
  },

  mapValueToDisplay: function (value) {
    var i, result = '';
    if (isc.isAn.Array(value) && value.length === 1) {
      // '_nativeElementBlur' calls 'refreshDisplayValue' and this one calls to this 'mapValueToDisplay' passing as argument "this.getValue()".
      // EXCEPT in the 'or' case, in Smartclient 8.3d this value was a string containing the typed value but in Smartclient 9.1d this value
      // is an array, being the typed value in the first element, so a conversion is needed to preserve the old logic.
      value = value[0];
    }
    if (!isc.isAn.Array(value)) {
      return this.Super('mapValueToDisplay', arguments);
    }
    for (i = 0; i < value.length; i++) {
      if (i > 0) {
        result += this.multipleValueSeparator;
      }
      // encode 'or' and 'and'
      result += OB.Utilities.encodeSearchOperator(this.Super('mapValueToDisplay', value[i]));
    }
    return result;
  },

  // combine the value of the field with the overall grid
  // filter values
  getPickListFilterCriteria: function () {
    var pickListCriteria = this.getCriterion(),
        gridCriteria, i, criteriaFieldName = this.getCriteriaFieldName();

    if (this.form.grid.sourceWidget.lazyFiltering) {
      // Fetch the criteria from the current values of the filter editor
      gridCriteria = this.form.grid.getValues();
    } else {
      gridCriteria = this.form.grid.sourceWidget.getCriteria();
    }

    gridCriteria = gridCriteria || {
      _constructor: 'AdvandedCriteria',
      operator: 'and'
    };
    gridCriteria.criteria = gridCriteria.criteria || [];

    for (i = 0; i < gridCriteria.criteria.length; i++) {
      if (criteriaFieldName === gridCriteria.criteria[i].fieldName) {
        gridCriteria.criteria.removeAt(i);
        break;
      }
    }

    // when in refresh picklist the user is typing
    // a value, filter using that
    if (this.keyPressed && pickListCriteria) {
      gridCriteria.criteria.add(pickListCriteria);
      delete this.keyPressed;
    }

    // add a dummy criteria to force a fetch
    // smartclient will try to do smart and prevent fetches if
    // criteria have not changed
    // note the system can be made smarter by checking if something
    // got reloaded in the underlying grid
    if (this.forceReload) {
      gridCriteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());
    }
    return gridCriteria;
  },

  setForceReload: function () {
    this.forceReload = true;
    if (this.form) {
      this.invalidateDisplayValueCache();
    }
  },

  canEditCriterion: function (criterion) {
    var firstCriteria;
    if (criterion.operator === 'and') {
      // and operator does not include the fieldName as a root property
      if (!criterion.criteria || criterion.criteria.length === 0) {
        return true;
      } else {
        // all criteria of the criterion are associated with the same name, pick the first
        firstCriteria = criterion.criteria[0];
        return (firstCriteria.fieldName === this.name || (this.criteriaField && (firstCriteria.fieldName === this.criteriaField)));
      }

    } else {
      return criterion && (criterion.fieldName === this.name || (this.criteriaField && (criterion.fieldName === this.criteriaField)));
    }
  },

  getCriterion: function (textMatchStyle) {
    var value = this.getCriteriaValue(),
        operator, fieldName, crit;

    if (value === null || isc.is.emptyString(value)) {
      return;
    }

    // the criteria parser expects an or expression
    if (isc.isAn.Array(value)) {
      value = this.mapValueToDisplay(value);
    }

    operator = this.getOperator(textMatchStyle, isc.isAn.Array(value));
    fieldName = this.getCriteriaFieldName();

    crit = this.parseValueExpressions(value, fieldName, operator);

    if (crit === null) {
      crit = {
        fieldName: fieldName,
        operator: operator,
        value: value
      };
    }

    if (this.operator && this.operator !== 'iContains') {
      // In this case we need to overwrite the operator assigned by the parseValueExpressions/parseOBValueExpressions logic
      crit = this.replaceCriterionOperator(crit, value, this.operator);
    }

    return crit;
  },

  getOperator: function (textMatchStyle) {
    if (this.operator && this.operator !== 'iContains') {
      return this.operator;
    } else {
      return this.Super('getOperator', arguments);
    }
  },

  replaceCriterionOperator: function (criterion, value, newOperator) {
    var newCriterion = criterion,
        i;
    if (newCriterion.criteria && newCriterion.criteria.length > 0) {
      // If there is a sub-criteria, go inside to process the childs
      for (i = 0; i < newCriterion.criteria.length; i++) {
        newCriterion.criteria[i] = this.replaceCriterionOperator(newCriterion.criteria[i], value, newOperator);
      }
    } else if ((criterion.operator === 'iContains' || criterion.operator === 'contains') && value.indexOf('~') !== 0 && value.indexOf('!~') !== 0) {
      // In case the criteria is 'iContains'/'contains', replace it by the desired one,
      // but only in the case there are no explicit 'iContains'/'contains' prefixes
      newCriterion.operator = newOperator;
    }
    // TODO: If there is a complex criteria with a 'iContains'/'contains' prefix, like "Cust or ~mplo", it won't work ok, since it will be
    //       translated to "^Cust or ^mplo" or "==Cust or ==mplo" (depending of the newOperator) instead of "^Cust or ~mplo" or "==Cust or ~mplo"
    return newCriterion;
  },

  setCriterion: function (criterion) {
    var i, value, values = [],
        operators, valueSet = false,
        criteria = criterion ? criterion.criteria : null;
    if (criteria && criteria.length && criterion.operator === 'or') {
      operators = isc.DataSource.getSearchOperators();
      for (i = 0; i < criteria.length; i++) {
        //handles case where column filter symbols are removed. Refer Issue https://issues.openbravo.com/view.php?id=23925
        if (criteria[i].operator !== "iContains" && criteria[i].operator !== "contains" && criteria[i].operator !== "regexp") {
          if (operators[criteria[i].operator] && (operators[criteria[i].operator].ID === criteria[i].operator) && operators[criteria[i].operator].symbol && criteria[i].value && (criteria[i].value.indexOf(operators[criteria[i].operator].symbol) === -1)) {
            values.push(operators[criteria[i].operator].symbol + criteria[i].value);
            valueSet = true;
          }
        }
        if (valueSet === false) {
          values.push(criteria[i].value);
        }
        valueSet = false;
      }
      this.setValue(values);
    } else {
      value = this.buildValueExpressions(criterion);
      this.setValue(value);
    }
  },

  // make sure that the correct field name is used to filter the main grid
  // if this is not here then the value will be removed by smartclient as it
  // sets the criterion back into the item
  // see also the setValuesAsCriteria in ob-grid-js which again translates
  // back
  getCriteriaFieldName: function () {
    return this.criteriaField || this.name + OB.Constants.FIELDSEPARATOR + OB.Constants.IDENTIFIER;
  },

  // solve a small bug in the value expressions
  buildValueExpressions: function () {
    var ret = this.Super('buildValueExpressions', arguments);
    if (isc.isA.String(ret) && ret.contains('undefined')) {
      return ret.replace('undefined', '');
    }
    return ret;
  },

  refreshPickList: function () {
    if (this.valueIsExpression()) {
      return;
    }

    // is called when the user enters values
    // filter using those values
    if (!this._pickedValue) {
      this.keyPressed = true;
    }

    return this.Super('refreshPickList', arguments);
  },

  valueIsExpression: function () {
    var prop, opDefs, val = this.getElementValue();
    // if someone starts typing and and or then do not filter
    // onkeypress either
    if (val.contains(' and')) {
      return true;
    }

    if (val.startsWith('=')) {
      return true;
    }

    // now check if the item element value is only
    // an operator, if so, go away
    opDefs = isc.DataSource.getSearchOperators();
    for (prop in opDefs) {
      if (opDefs.hasOwnProperty(prop)) {
        // let null and not null fall through
        // as they should be filtered
        if (prop === 'isNull' || prop === 'notNull') {
          continue;
        }

        same = opDefs[prop].symbol && val.startsWith(opDefs[prop].symbol);
        if (same) {
          return true;
        }
      }
    }
    return false;
  }
});