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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('OBCharacteristicsItem', isc.CanvasItem);

isc.OBCharacteristicsItem.addProperties({
  completeValue: null,

  init: function () {
    this.canvas = isc.OBCharacteristicsLayout.create({

    });

    this.title = null;
    this.colSpan = 4;

    this.Super('init', arguments);
  },

  setValue: function (value) {
    var field, formFields = [];
    this.completeValue = value;
    if (!value) {
      this.hide();
      this.Super('setValue', arguments);
      return;
    }
    this.show();

    //Remove all members the widget might have
    //this.canvas.removeMembers(this.canvas.getMembers());
    if (value.characteristics) {
      for (field in value.characteristics) {
        if (value.characteristics.hasOwnProperty(field)) {
          formFields.push(isc.OBTextItem.create({
            width: '*',
            title: field,
            value: value.characteristics[field]
          }));
        }
      }
    }

    this.canvas.setFields(formFields);

    this.Super('setValue', arguments);
  },

  getValue: function () {
    if (this.completeValue && this.completeValue.dbValue) {
      return this.completeValue.dbValue;
    }
    return this.completeValue;
  }
});

isc.ClassFactory.defineClass('OBCharacteristicsLayout', isc.DynamicForm);

isc.OBCharacteristicsLayout.addProperties({
  titleOrientation: 'left',
  width: '*',
  numCols: 3,
  colWidths: ['10%', '40%', '50%']
});