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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */


// == OBCalendar_EventDialogBridge ==
// Hack to allow the OBCalendar open its own full customizable EventDialog (OBEventEditor)
isc.ClassFactory.defineClass('OBCalendar_EventDialogBridge', isc.Window);

isc.OBCalendar_EventDialogBridge.addProperties({
  show: function () {
    this.Super('show', arguments);

    var calendar = this.calendar,
        currentStart = this.currentStart,
        currentEnd = this.currentEnd,
        event = this.event;

    if (calendar.OBEventEditor) {
      calendar.OBEventEditor.setProperties({
        calendar: calendar,
        currentStart: currentStart,
        currentEnd: currentEnd,
        event: event
      });
      calendar.OBEventEditor.initComponents();
      calendar.OBEventEditor.show();
      this.closeClick();
    }
  }
});

// == OBCalendar_EventDialogBridge ==
// Hack to allow calendar TabSet style personalization
isc.ClassFactory.defineClass('OBCalendarTabSet', isc.TabSet);

// == OBClientClassCanvasItem ==
// Extends Calendar, with some customizations (most of them styling related)
isc.ClassFactory.defineClass('OBCalendar', isc.Calendar);


isc.OBCalendar.addProperties({
  autoFetchData: true,
  initWidget: function () {
    var calendar = this,
        multiCalendar = this.multiCalendar;

    this.eventWindowStyle = OB.Styles.OBCalendar.eventWindowStyle;
    this.datePickerButtonDefaults.src = OB.Styles.OBCalendar.datePickerButton.src;
    this.datePickerButtonDefaults.width = OB.Styles.OBCalendar.datePickerButton.width;
    this.datePickerButtonDefaults.height = OB.Styles.OBCalendar.datePickerButton.height;
    this.previousButtonDefaults.src = OB.Styles.OBCalendar.previousButton.src;
    this.previousButtonDefaults.width = OB.Styles.OBCalendar.previousButton.width;
    this.previousButtonDefaults.height = OB.Styles.OBCalendar.previousButton.height;
    this.nextButtonDefaults.src = OB.Styles.OBCalendar.nextButton.src;
    this.nextButtonDefaults.width = OB.Styles.OBCalendar.nextButton.width;
    this.nextButtonDefaults.height = OB.Styles.OBCalendar.nextButton.height;
    this.controlsBarDefaults.layoutTopMargin = OB.Styles.OBCalendar.controlsTopMarging;

    this.mainViewDefaults._constructor = isc.OBCalendarTabSet;
    this.eventDialogDefaults._constructor = isc.OBCalendar_EventDialogBridge;
    this.eventDialogDefaults.calendar = this;

    if (!this.eventIdField) {
      this.eventIdField = 'eventId';
    }
    if (typeof this.showAddEventControl === 'undefined') {
      this.showAddEventControl = true;
    }
    if (typeof this.showDatePickerControl === 'undefined') {
      this.showDatePickerControl = true;
    }
    if (this.OBEventEditorClass) {
      this.OBEventEditor = isc[this.OBEventEditorClass].create({});
    }

    this.dataSource = OB.Datasource.create({
      dataURL: this.dataSourceProps.dataURL,
      fields: [{
        name: this.eventIdField,
        primaryKey: true
      }, {
        name: this.nameField
      }, {
        name: this.descriptionField
      }, {
        name: this.startDateField,
        type: "datetime"
      }, {
        name: this.endDateField,
        type: "datetime"
      }],

      // these are read extra from the server with the events
      additionalProperties: this.dataSourceProps.additionalProperties,

      transformRequest: function (dsRequest) {
        dsRequest.params = dsRequest.params || {};
        dsRequest.params._extraProperties = this.additionalProperties;
        dsRequest.willHandleError = true;

        return this.Super('transformRequest', arguments);
      },
      transformResponse: function (dsResponse, dsRequest, data) {
        var showDSAlert, records = data && data.response && data.response.data,
            i;

        showDSAlert = function (text) {
          isc.warn(text, function () {
            return true;
          }, {
            icon: '[SKINIMG]Dialog/error.png',
            title: OB.I18N.getLabel('OBUIAPP_Error')
          });
        };

        // handle error
        if (data && data.response && data.response.error) {
          showDSAlert(data.response.error.message);
          calendar.filterData();
        } else if (data && data.response && data.response.errors) {
          showDSAlert(JSON.stringify(data.response.errors));
          calendar.filterData();
        } else {
          if (records) {
            for (i = 0; i < records.getLength(); i++) {
              if (typeof records[i][calendar.nameField] === 'undefined') {
                //To avoid the event displays 'undefined' when no name has been set
                records[i][calendar.nameField] = '';
              }
              if (typeof records[i][calendar.descriptionField] === 'undefined') {
                records[i][calendar.descriptionField] = '';
              }
              if (multiCalendar && multiCalendar.showCustomEventsBgColor) {
                records[i].eventWindowStyle = multiCalendar.eventStyles[records[i][calendar.legendIdField]] + ' ' + OB.Styles.OBCalendar.eventWindowStyle;
              }
              if (typeof calendar.customTransformResponse === 'function') {
                records[i] = calendar.customTransformResponse(records[i], calendar);
              }
            }
          }
        }
        return this.Super('transformResponse', arguments);
      },

      // override the addData, updateData and removeData to wrap
      // the calendar callback to prevent adding events in cased
      // of errors
      addData: function (newRecord, callback, requestProperties) {
        var newCallBack = function (dsResponse, data, dsRequest) {
            // don't call if there is an error
            if (dsResponse.status < 0) {
              return;
            }
            callback(dsResponse, data, dsRequest);
            };
        return this.Super('addData', [newRecord, newCallBack, requestProperties]);
      },
      updateData: function (updatedRecord, callback, requestProperties) {
        var newCallBack = function (dsResponse, data, dsRequest) {
            // don't call if there is an error
            if (dsResponse.status < 0) {
              return;
            }
            callback(dsResponse, data, dsRequest);
            };
        return this.Super('updateData', [updatedRecord, newCallBack, requestProperties]);
      },
      removeData: function (recordKeys, callback, requestProperties) {
        var newCallBack = function (dsResponse, data, dsRequest) {
            // don't call if there is an error
            if (dsResponse.status < 0) {
              return;
            }
            callback(dsResponse, data, dsRequest);
            };
        return this.Super('removeData', [recordKeys, newCallBack, requestProperties]);
      }
    });
    this.Super('initWidget', arguments);
    this.controlsBar.reorderMember(4, 1); // Moves the 'next' button to the second position
    this.controlsBar.reorderMember(2, 4); // Moves the 'displayed date' to last position
    if (this.defaultViewName && ((this.showDayView !== false && this.showWeekView !== false) || (this.showDayView !== false && this.showMonthView !== false) || (this.showWeekView !== false && this.showMonthView !== false))) {
      this.setCurrentViewName(this.defaultViewName);
    }
    if (!this.showAddEventControl) {
      this.addEventButton.hide();
    }
    if (!this.showDatePickerControl) {
      this.datePickerButton.hide();
    }

    if (this.showDayView !== false) {
      this.dayView.baseStyle = OB.Styles.OBCalendar.dayView_baseStyle;
    }
    if (this.showWeekView !== false) {
      this.weekView.baseStyle = OB.Styles.OBCalendar.weekView_baseStyle;
      this.weekView.headerBaseStyle = OB.Styles.OBCalendar.weekView_headerBaseStyle;
    }
    if (this.showMonthView !== false) {
      this.monthView.baseStyle = OB.Styles.OBCalendar.monthView_baseStyle;
      this.monthView.headerBaseStyle = OB.Styles.OBCalendar.monthView_headerBaseStyle;
    }
  },

  eventResized: function (newDate, event) {
    newDate.setSeconds(0);
    if (this.showEventDialogOnEventResize) {
      this.eventDialog.event = event;
      this.eventDialog.currentStart = event[this.startDateField];
      this.eventDialog.currentEnd = newDate;
      this.eventDialog.calendar = this;
      try {
        //To avoid js error due to conflicts with Smartclient default EventDialog
        this.eventDialog.show();
      } catch (e) {}
    } else {
      return this.Super('eventResized', arguments);
    }
  },
  eventMoved: function (newDate, event) {
    newDate.setSeconds(0);
    if (this.showEventDialogOnEventMove) {
      var dateDiff = event[this.endDateField] - event[this.startDateField],
           //Event duration
          newEndDate = newDate.getTime() + dateDiff; //Add the event duration to the new startDate
      newEndDate = new Date(newEndDate);
      this.eventDialog.event = event;
      this.eventDialog.currentStart = newDate;
      this.eventDialog.currentEnd = newEndDate;
      this.eventDialog.calendar = this;
      try {
        //To avoid js error due to conflicts with Smartclient default EventDialog
        this.eventDialog.show();
      } catch (e) {}
    } else {
      return this.Super('eventMoved', arguments);
    }
  },
  eventRemoveClick: function (event) {
    if (this.showEventDialogOnEventDelete) {
      this.eventDialog.event = event;
      this.eventDialog.currentStart = event[this.startDateField];
      this.eventDialog.currentEnd = event[this.endDateField];
      this.eventDialog.calendar = this;
      try {
        //To avoid js error due to conflicts with Smartclient default EventDialog
        this.eventDialog.show();
      } catch (e) {
      }
      return false;
    } else {
      return this.Super('eventRemoveClick', arguments);
    }
  },
  showOBEventDialog: function () {
    var dialog = isc.OBPopup.create({});
    dialog.show();
  },

  getCriteria: function (criteria) {
    var startTime, endTime, legend, i, orPart = {
      operator: 'or',
      criteria: []
    };

    if (this.month === 0) {
      startTime = new Date(this.year - 1, 11, 23, 0, 0, 0);
    } else {
      startTime = new Date(this.year, this.month - 1, 23, 0, 0, 0);
    }

    if (!criteria || !criteria.operator) {
      criteria = {
        _constructor: "AdvancedCriteria",
        operator: "and"
      };
    }
    criteria.criteria = criteria.criteria || [];

    // add the date criteria
    if (this.month === 11) {
      endTime = new Date(this.year + 1, 0, 7, 0, 0, 0);
    } else {
      endTime = new Date(this.year, this.month + 1, 7, 0, 0, 0);
    }
    criteria.criteria.push({
      fieldName: this.startDateField,
      operator: 'greaterOrEqual',
      value: startTime
    });
    criteria.criteria.push({
      fieldName: this.endDateField,
      operator: 'lessThan',
      value: endTime
    });

    if (this.multiCalendar) {
      legend = this.multiCalendar.leftControls.getLegendValueMap();
      for (i = 0; i < legend.getLength(); i++) {
        if (legend[i].checked) {
          orPart.criteria.push({
            fieldName: this.legendIdField,
            operator: 'equals',
            value: legend[i].id
          });
        }
      }
      // some dummy value to force an empty resultset
      if (orPart.criteria.getLength() === 0) {
        orPart.criteria.push({
          fieldName: this.legendIdField,
          operator: 'equals',
          value: new Date().getTime().toString()
        });
      }
    } else if (this.legendId) {
      orPart.criteria.push({
        fieldName: this.legendIdField,
        operator: 'equals',
        value: this.legendId
      });
    }

    if (orPart.criteria.getLength() > 0) {
      criteria.criteria.push(orPart);
    }

    if (typeof this.getCustomCriteria === 'function') {
      criteria.criteria.push(this.getCustomCriteria(this));
    }

    // always force a reload
    criteria.criteria.push(isc.OBRestDataSource.getDummyCriterion());

    return criteria;
  },

  draw: function () {
    var ret, _originalTabSelected = this.mainView.tabSelected,
        calendar = this;
    ret = this.Super('draw', arguments);

    // If change filter/legend parameters in day/week view and you switch to the other one,
    // data needs to be refreshed in order to show changes
    if (this.multiCalendar && this.mainView && typeof this.mainView.selectTab === 'function') {
      this.mainView.tabSelected = function () {
        var actionObject, mvret;
        actionObject = {
          target: this,
          method: _originalTabSelected,
          parameters: arguments
        };
        mvret = OB.Utilities.callAction(actionObject);
        calendar.refreshSelectedView();
        return mvret;
      };
    }
    return ret;
  },

  fetchData: function (criteria, callback, request) {
    return this.Super('fetchData', [this.getCriteria(criteria), callback, request]);
  },

  filterData: function (criteria) {
    return this.Super('filterData', [this.getCriteria(criteria)]);
  },

  // read the dates for the current month
  dateChanged: function () {
    if (this.multiCalendar) {
      this.multiCalendar.leftControls.dateChooser.setData(this.chosenDate);
    }

    // no change
    if (this.month === this.prevMonth) {
      return;
    }

    this.prevMonth = this.month;
    this.filterData();
  },

  addEvent: function (startDate, endDate, name, description, otherFields, ignoreDataChanged) {
    var i, legend = this.multiCalendar.leftControls.getLegendValueMap();
    otherFields = otherFields || {};

    // solve bug that otherwise time fields are not passed in
    startDate.logicalDate = false;
    endDate.logicalDate = false;

    return this.Super('addEvent', [startDate, endDate, name, description, otherFields, ignoreDataChanged]);
  }

});