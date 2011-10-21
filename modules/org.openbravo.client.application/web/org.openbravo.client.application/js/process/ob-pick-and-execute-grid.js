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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBPickAndExecuteGrid', isc.OBGrid);

isc.OBPickAndExecuteGrid.addProperties({
  dataProperties: {
    useClientFiltering: false,
    useClientSorting: false
  },
  view: null,
  dataSource: null,
  showFilterEditor: true,

  // Editing
  canEdit: true,
  editEvent: 'click',
  autoSaveEdits: false,

  selectionAppearance: 'checkbox',
  autoFitFieldWidths: true,
  autoFitWidthApproach: 'title',
  canAutoFitFields: false,
  minFieldWidth: 75,
  width: '100%',
  height: '100%',

  // default selection
  selectionProperty: 'selected',

  selectedIds: [],

  selectionUpdated: function (record, recordList) {
    var i, len = recordList.length;

    this.selectedIds = [];

    for (i = 0; i < len; i++) {
      this.selectedIds.push(recordList[i].id);
    }

    this.Super('selectionUpdated', arguments);
  },

  handleFilterEditorSubmit: function (criteria, context) {
    var ids = [],
        crit = {},
        len = this.selectedIds.length;

    for (i = 0; i < len; i++) {
      ids.push({
        fieldName: 'id',
        operator: 'equals',
        value: this.selectedIds[i]
      });
    }

    if (len > 0) {
      crit._constructor = 'AdvancedCriteria';
      crit._OrExpression = true; // trick to get a really _or_ in the backend
      crit.operator = 'or';
      crit.criteria = ids;
      crit.criteria.push(criteria); // original filter
    } else {
      crit = criteria;
    }

    this.Super('handleFilterEditorSubmit', [crit, context]);
  }
});