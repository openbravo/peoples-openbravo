/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.OBRETCO = window.OB.OBRETCO || {};
OB.OBRETCO.SpecialSchedule = window.OB.OBRETCO.SpecialSchedule || {};
OB.OBRETCO.SpecialSchedule.OnChangeFunctions =
  window.OB.OBRETCO.SpecialSchedule || {};

OB.OBRETCO.SpecialSchedule.validation = function(
  view,
  actionHandlerCall,
  failureCallback,
  additionalInfo
) {
  var error = false,
    dateGrid = view.theForm.getItem('Date'),
    storeGrid = view.theForm.getItem('Store'),
    row = null,
    i = 0;

  for (; i < dateGrid.getValue()._allRows.length; i++) {
    row = dateGrid.getValue()._allRows[i];
    if (
      row.open &&
      (row.startingTime === undefined ||
        row.startingTime === '' ||
        row.endingTime === undefined ||
        row.endingTime === '')
    ) {
      view.messageBar.setMessage(
        isc.OBMessageBar.TYPE_ERROR,
        null,
        OB.I18N.getLabel('OBRETCO_OPEN_SPECIAL_SCHEDULE', [(i + 1) * 10])
      );
      failureCallback();
      error = true;
    } else if (
      !row.open &&
      ((row.startingTime !== undefined && row.startingTime !== '') ||
        (row.endingTime !== undefined && row.endingTime !== ''))
    ) {
      view.messageBar.setMessage(
        isc.OBMessageBar.TYPE_ERROR,
        null,
        OB.I18N.getLabel('OBRETCO_CLOSE_SPECIAL_SCHEDULE', [(i + 1) * 10])
      );
      failureCallback();
      error = true;
    }
  }

  if (!error && storeGrid.getValue()._selection.length === 0) {
    view.messageBar.setMessage(
      isc.OBMessageBar.TYPE_ERROR,
      null,
      OB.I18N.getLabel('OBRETCO_SELECT_STORE_RECORD_ERROR')
    );
    failureCallback();
    error = true;
  }

  if (!error) {
    actionHandlerCall();
  }
};

OB.OBRETCO.SpecialSchedule.OnChangeFunctions.open = function(
  item,
  view,
  form,
  grid
) {
  if (!item.getValue()) {
    grid.setEditValue(item.rowNum, 'startingTime', '');
    grid.setEditValue(item.rowNum, 'endingTime', '');
  }
};
