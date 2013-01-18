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
 * All portions are Copyright (C) 2011-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBTimeItem ==
// For entering times.
isc.ClassFactory.defineClass('OBTimeItem', isc.TimeItem);

isc.OBTimeItem.addProperties({
  operator: 'equals',
  validateOnExit: true,
  showHint: false,
  timeFormatter: 'to24HourTime',
  displayFormat: 'to24HourTime',
  short24TimeFormat: 'HH:MM:SS',
  shortTimeFormat: 'HH:MM:SS',
  long24TimeFormat: 'HH:MM:SS',
  longTimeFormat: 'HH:MM:SS',

  // make sure that the undo/save buttons get enabled, needs to be done like
  // this because changeOnKeypress is false. Activating changeOnKeypress makes the
  // item not editable as it is reformatted on keyStroke, the same happens calling
  // from this method form.itemChangeActions
  keyPress: function (item, form, keyName, characterValue) {
    var i, f = this.form,
        toolBarButtons;

    if ((f && f.view && f.view.toolBar && f.view.messageBar && f.setHasChanged) && (characterValue || keyName === 'Backspace' || keyName === 'Delete')) {
      toolBarButtons = f.view.toolBar.leftMembers;
      f.setHasChanged(true);
      f.view.messageBar.hide();
      for (i = 0; i < toolBarButtons.length; i++) {
        if (toolBarButtons[i].updateState) {
          toolBarButtons[i].updateState();
        }
      }
    }
    this.Super('keyPress', arguments);
  },

  // SmartClient's TimeItem doesn't keep time zone. Preserve it in case the
  // string contains time zone. So time in this format is kept: 12:00+01:00
  setValue: function (value) {
    if (isc.isA.String(value) && (value.contains('+') || value.contains('-'))) {
      value = isc.Time.parseInput(value, null, null, true);
    }
    if (value && isc.isA.String(value)) {
      value = isc.Time.parseInput(value);
    }
    if (value && isc.isA.Date(value)) {
      this.setTodaysDate(value);
    }
    return this.Super('setValue', arguments);
  },

  getValue: function () {
    var value = this.Super('getValue', arguments);
    if (value && isc.isA.Date(value)) {
      this.setTodaysDate(value);
    }
    return value;
  },

  setTodaysDate: function (date) {
    var today = new Date();
    date.setYear(today.getFullYear());
    date.setMonth(today.getMonth());
    date.setDate(today.getDate());
  },

  /* The following functions allow proper timeGrid operation */

  doShowTimeGrid: function (timeValue) {
    if (this.timeGrid && !this.timeGrid.isVisible()) {
      this.timeGrid.show();
      if (this.getValue()) {
        this.timeGrid.selectTimeInList(timeValue);
      }
    }
  },
  doHideTimeGrid: function (timeValue) {
    var me = this;
    if (this.timeGrid) {
      setTimeout(function () {
        me.timeGrid.hide();
      }, 100);
    }
  },

  init: function () {
    var oldShowHint, hint;
    this.Super('init', arguments);
    if (this.showTimeGrid && this.form && !this.timeGrid) {
      oldShowHint = this.showHint;
      this.showHint = true;
      hint = this.getHint();
      this.showHint = oldShowHint;
      this.timeGridProps = this.timeGridProps || {};
      this.timeGrid = isc.OBTimeItemGrid.create(isc.addProperties({
        formItem: this,
        timeFormat: hint
      }, this.timeGridProps));
      this.form.addChild(this.timeGrid); // Added grid in the form to avoid position problems
    }
  },
  keyDown: function () {
    if (this.timeGrid) {
      if (isc.EH.getKey() === 'Arrow_Up' && (!isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) && this.timeGrid.isVisible()) {
        this.timeGrid.selectPreviousRecord();
      } else if (isc.EH.getKey() === 'Arrow_Down' && (!isc.EH.ctrlKeyDown() && !isc.EH.altKeyDown() && !isc.EH.shiftKeyDown()) && this.timeGrid.isVisible()) {
        this.timeGrid.selectNextRecord();
      } else {
        this.timeGrid.hide();
      }
    }
    return this.Super('keyDown', arguments);
  },
  click: function () {
    this.doShowTimeGrid(isc.Time.parseInput(this.getEnteredValue()));
    return this.Super('click', arguments);
  },
  focus: function () {
    this.doShowTimeGrid(this.getValue());
    return this.Super('focus', arguments);
  },
  blur: function () {
    this.doHideTimeGrid();
    return this.Super('blur', arguments);
  },
  moved: function () {
    if (this.timeGrid) {
      this.timeGrid.updatePosition();
    }
    return this.Super('moved', arguments);
  }
});


isc.ClassFactory.defineClass("OBTimeItemGrid", isc.ListGrid);

isc.OBTimeItemGrid.addProperties({
  formItem: null,
  timeFormat: null,
  data: null,
  showHeader: false,
  selectionType: 'single',
  visibility: 'hidden',
  precission: 'minute',
  // Possible values are 'hour', 'minute' and 'second'
  is24hTime: true,
  minTime: '00:00:00',
  maxTime: '23:59:59',
  // Be careful with setting it as '24:00:00' since it is considered as '00:00:00' of the following day
  timeGranularity: 1800,
  // In seconds
  timeReference: '00:00:00',
  showDiffText: null,
  timeLabels: null,
  maxTimeStringLength: 0,

  dateObjToTimeString: function (dateObj) {
    var lengthThreshold, tmpString, isPM = false,
        dateString = '';
    if (this.precission === 'hour' || this.precission === 'minute' || this.precission === 'second') {
      tmpString = dateObj.getHours();
      if (!this.is24hTime && tmpString - 12 >= 0) {
        tmpString = tmpString - 12;
        isPM = true;
      }
      if (!this.is24hTime && tmpString === 0) {
        tmpString = 12;
      }
      tmpString = tmpString.toString();
      if (tmpString.length < 2) {
        tmpString = '0' + tmpString;
      }
      dateString += tmpString;
    }
    if (this.precission === 'minute' || this.precission === 'second') {
      tmpString = dateObj.getMinutes();
      tmpString = tmpString.toString();
      if (tmpString.length < 2) {
        tmpString = '0' + tmpString;
      }
      dateString += ':' + tmpString;
    }
    if (this.precission === 'second') {
      tmpString = dateObj.getSeconds();
      tmpString = tmpString.toString();
      if (tmpString.length < 2) {
        tmpString = '0' + tmpString;
      }
      dateString += ':' + tmpString;
    }
    if (!this.is24hTime && isPM) {
      dateString += ' pm';
    } else if (!this.is24hTime && !isPM) {
      dateString += ' am';
    }

    return dateString;
  },
  timeStringToDateObj: function (stringTime) {
    var lengthThreshold;
    if (stringTime.length < 3) {
      stringTime = stringTime + ':00:00';
    } else if (stringTime.length < 6) {
      stringTime = stringTime + ':00';
    }

    if (typeof stringTime === 'string') {
      if (parseInt(stringTime.substring(0, stringTime.length - 6), 10) < 24) {
        stringTime = new Date(new Date(0).toDateString() + ' ' + stringTime);
      } else {
        stringTime = new Date(new Date(new Date(0).setDate(2)).setHours(0));
      }
    }
    return stringTime;
  },
  normalizeDateObj: function (dateObj) {
    var timeRefHrs, timeRefMins, timeRefSecs, newTimeRef;
    if (this.precission === 'hour' || this.precission === 'minute' || this.precission === 'second') {
      timeRefHrs = dateObj.getHours();
    } else {
      timeRefHrs = 0;
    }
    if (this.precission === 'minute' || this.precission === 'second') {
      timeRefMins = dateObj.getMinutes();
    } else {
      timeRefMins = 0;
    }
    if (this.precission === 'second') {
      timeRefSecs = dateObj.getSeconds();
    } else {
      timeRefSecs = 0;
    }
    newTimeRef = new Date(0);
    newTimeRef = new Date(newTimeRef.setHours(timeRefHrs));
    newTimeRef = new Date(newTimeRef.setMinutes(timeRefMins));
    newTimeRef = new Date(newTimeRef.setSeconds(timeRefSecs));
    newTimeRef = new Date(newTimeRef.setMilliseconds(0));
    return newTimeRef;
  },
  getDiffText: function (date, reference) {
    var diffMs = (date - reference),
        diffDays = (diffMs / 86400000),
        diffHrs = ((diffMs % 86400000) / 3600000),
        diffMins = (((diffMs % 86400000) % 3600000) / 60000),
        diffSecs = ((((diffMs % 86400000) % 3600000) % 60000) / 1000),
        diffText = '';

    if (diffDays >= 0) {
      diffDays = Math.floor(diffDays);
    } else {
      diffDays = Math.ceil(diffDays);
    }
    if (diffHrs >= 0) {
      diffHrs = Math.floor(diffHrs);
    } else {
      diffHrs = Math.ceil(diffHrs);
    }
    if (diffMins >= 0) {
      diffMins = Math.floor(diffMins);
    } else {
      diffMins = Math.ceil(diffMins);
    }
    if (diffSecs >= 0) {
      diffSecs = Math.floor(diffSecs);
    } else {
      diffSecs = Math.ceil(diffSecs);
    }

    if (diffHrs === 1 || diffHrs === -1) {
      diffText += diffHrs + ' ' + this.timeLabels[21];
    } else if (diffHrs || this.precission === 'hour') {
      diffText += diffHrs + ' ' + this.timeLabels[22];
    }

    if (diffText.length > 0 && diffMins) {
      diffText += ' ';
    }

    if (diffMins === 1 || diffMins === -1) {
      diffText += diffMins + ' ' + this.timeLabels[31];
    } else if (diffMins || (!diffHrs && this.precission === 'minute')) {
      diffText += diffMins + ' ' + this.timeLabels[32];
    }

    if (diffText.length > 0 && diffSecs) {
      diffText += ' ';
    }

    if (diffSecs === 1 || diffSecs === -1) {
      diffText += diffSecs + ' ' + this.timeLabels[41];
    } else if (diffSecs || (!diffHrs && !diffMins && this.precission === 'second')) {
      diffText += diffSecs + ' ' + this.timeLabels[42];
    }

    diffText = '(' + diffText + ')';

    if (this.maxTimeStringLength < diffText.length) {
      this.maxTimeStringLength = diffText.length;
    }

    return diffText;
  },
  convertTimes: function () {
    this.minTime = this.timeStringToDateObj(this.minTime);
    this.maxTime = this.timeStringToDateObj(this.maxTime);
    this.timeReference = this.timeStringToDateObj(this.timeReference);
  },
  selectTimeInList: function (time) {
    var rowNum, i;

    time = this.timeStringToDateObj(time);
    time = this.normalizeDateObj(time);

    for (i = 0; i < this.data.length; i++) {
      if (this.normalizeDateObj(this.data[i].jsTime) <= time) {
        rowNum = i;
      } else {
        break;
      }
    }
    this.scrollCellIntoView(rowNum, null, true, true);
    this.doSelectionUpdated = false;
    this.selectSingleRecord(rowNum);
    this.doSelectionUpdated = true;
  },
  doSelectionUpdated: true,
  selectionUpdated: function (record) {
    if (this.formItem && record && this.doSelectionUpdated) {
      this.formItem.setValue(record.jsTime);
    }
    return this.Super('selectionUpdated ', arguments);
  },

  show: function () {
    var timeRef, formItemWidth;
    if (this.isVisible()) {
      return;
    }
    if (this.formItem && this.formItem.relativeField) {
      this.formItem.eventParent.getValue(this.formItem.relativeField);
      timeRef = this.formItem.eventParent.getValue(this.formItem.relativeField);
      if (timeRef) {
        timeRef = this.normalizeDateObj(timeRef);
        this.timeReference = timeRef;
        if (this.formItem && !this.formItem.showNegativeTimes) {
          this.minTime = timeRef;
        }
        this.setData(this.generateData());
      }
    }

    if (this.precission === 'hour') {
      this.setWidth(3 * this.characterWidth + this.maxTimeStringLength * this.characterWidth + 18);
    } else if (this.precission === 'minute') {
      this.setWidth(6 * this.characterWidth + this.maxTimeStringLength * this.characterWidth + 18);
    } else if (this.precission === 'second') {
      this.setWidth(9 * this.characterWidth + this.maxTimeStringLength * this.characterWidth + 18);
    }
    if (this.formItem) {
      formItemWidth = this.formItem.getVisibleWidth();
      if (formItemWidth && formItemWidth - 2 > this.getWidth()) {
        this.setWidth(formItemWidth - 2);
      }
    }

    this.updatePosition();
    return this.Super('show', arguments);
  },
  generateData: function () {
    var dateObj, timeGranularityInMilliSeconds, timeRef, dateArray = [];
    this.convertTimes();
    this.maxTimeStringLength = 0;
    timeRef = this.timeReference;

    if (this.precission === 'second') {
      timeGranularityInMilliSeconds = this.timeGranularity * 1000;
    } else if (this.precission === 'minute') {
      timeGranularityInMilliSeconds = Math.ceil(this.timeGranularity / 60) * 1000 * 60;
    } else if (this.precission === 'hour') {
      timeGranularityInMilliSeconds = Math.ceil(this.timeGranularity / (60 * 60)) * 1000 * 60 * 60;
    }

    while (this.minTime <= timeRef) {
      dateObj = {
        time: this.dateObjToTimeString(timeRef) + (this.showDiffText ? ' ' + this.getDiffText(timeRef, this.timeReference) : ''),
        jsTime: timeRef
      };
      dateArray.unshift(dateObj);
      timeRef = new Date(timeRef.getTime() - timeGranularityInMilliSeconds);
    }
    timeRef = this.timeReference;
    while (timeRef <= this.maxTime) {
      dateObj = {
        time: this.dateObjToTimeString(timeRef) + (this.showDiffText ? ' ' + this.getDiffText(timeRef, this.timeReference) : ''),
        jsTime: timeRef
      };
      if (timeRef !== this.timeReference) {
        dateArray.push(dateObj);
      }
      timeRef = new Date(timeRef.getTime() + timeGranularityInMilliSeconds);
    }
    return dateArray;
  },
  selectPreviousRecord: function () {
    var selectedRecord = this.getSelectedRecord(),
        i;
    if (selectedRecord) {
      for (i = 0; i < this.data.length; i++) {
        if (this.data[i] === selectedRecord && i !== 0) {
          this.scrollCellIntoView(i - 1, null, true, true);
          this.selectSingleRecord(i - 1);
          break;
        }
      }
    } else {
      this.scrollCellIntoView(0, null, true, true);
      this.selectSingleRecord(0);
    }
  },
  selectNextRecord: function () {
    var selectedRecord = this.getSelectedRecord(),
        i;
    if (selectedRecord) {
      for (i = 0; i < this.data.length; i++) {
        if (this.data[i] === selectedRecord && i !== this.data.length - 1) {
          this.scrollCellIntoView(i + 1, null, true, true);
          this.selectSingleRecord(i + 1);
          break;
        }
      }
    } else {
      this.scrollCellIntoView(0, null, true, true);
      this.selectSingleRecord(0);
    }
  },
  updatePosition: function () {
    var me = this,
        interval;
    if (this.formItem) {
      this.placeNear(this.formItem.getPageLeft() + 2, this.formItem.getPageTop() + 26);
    }
  },
  initWidget: function () {
    var labels;
    if (this.timeFormat.indexOf('SS') !== -1) {
      this.precission = 'second';
    } else if (this.timeFormat.indexOf('MM') !== -1) {
      this.precission = 'minute';
    } else if (this.timeFormat.indexOf('HH') !== -1) {
      this.precission = 'hour';
    }

    if (this.timeFormat.toUpperCase().indexOf('AM') !== -1 || this.timeFormat.toUpperCase().indexOf('PM') !== -1) {
      this.is24hTime = false;
    }

    if (this.formItem && this.formItem.timeGranularity) {
      this.timeGranularity = this.formItem.timeGranularity;
    }

    if (this.formItem && this.formItem.relativeField && this.showDiffText !== false) {
      this.showDiffText = true;
    }

    labels = OB.I18N.getLabel('OBUIAPP_TimeUnits');
    if (labels) {
      this.timeLabels = labels.split(',');
    }

    this.setData(this.generateData());

    return this.Super('initWidget', arguments);
  },
  fields: [{
    name: 'time',
    title: 'Time'
  }, {
    name: 'jsTime',
    title: 'JS Time',
    showIf: 'false'
  }]
});