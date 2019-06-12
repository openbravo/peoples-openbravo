/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone*/

enyo.kind({
  name: 'OBPOS.UI.StoreInformation',
  kind: 'OB.UI.Modal',
  topPosition: '40px',
  body: {
    kind: 'OBPOS.UI.StoreInformationLine'
  },
  handlers: {
    onHideInfoPopup: 'hidePopup',
    onShowInfoPopup: 'showPopup'
  },
  storeId: null,
  executeOnShow: function () {
    this.$.header.setContent(this.args.orgName);
    this.storeId = this.args.orgId;
    this.$.body.$.storeInformationLine.loadInfo(this.storeId);
  },
  initComponents: function () {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationLine',
  components: [{
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-address',
      name: 'iconAddress'
    }, {
      classes: 'obpos-row-store-info',
      name: 'addressValue'
    }]
  }, {
    classes: 'obpos-clear-both'
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'span6',
      components: [{
        classes: 'obpos-icon-phone',
        name: 'iconPhone'
      }, {
        classes: 'obpos-row-store-info',
        name: 'phoneNumber'
      }]
    }, {
      classes: 'span6',
      components: [{
        classes: 'obpos-icon-fax',
        name: 'iconFax'
      }, {
        classes: 'obpos-row-store-info',
        name: 'faxNumber'
      }]
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-email',
      name: 'iconEmail'
    }, {
      classes: 'obpos-row-store-info',
      name: 'email'
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      classes: 'obpos-icon-cif',
      name: 'iconCIF',
      initComponents: function () {
        this.setContent('CIF');
      }
    }, {
      classes: 'obpos-row-store-info',
      name: 'cif'
    }]
  }, {
    classes: 'span12',
    components: [{
      name: 'storeCallCenterSchedule',
      kind: 'OB.UI.ScrollableTable',
      scrollAreaMaxHeight: '140px',
      renderHeader: 'OBPOS.UI.StoreInformationScheduleScrollableHeader',
      renderLine: 'OBPOS.UI.StoreInformationScheduleLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    }]
  }, {
    classes: 'obpos-store-line span12',
    components: [{
      name: 'storeSpecialSchedule',
      kind: 'OB.UI.ScrollableTable',
      scrollAreaMaxHeight: '140px',
      renderHeader: 'OBPOS.UI.StoreInformationSpecialScheduleScrollableHeader',
      renderLine: 'OBPOS.UI.StoreInformationSpecialScheduleLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    }]
  }, {
    classes: 'span12 obpos-center-text',
    components: [{
      classes: 'obpos-clear-both obpos-btnlink btnlink btnlink-small btnlink-gray',
      name: 'btnClose',
      kind: 'OB.UI.Button',
      i18nLabel: 'OBPOS_LblSlaveClose',
      tap: function () {
        if (this.disabled) {
          return true;
        }
        this.owner.owner.owner.hide();
      }
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);

    this.$.storeCallCenterSchedule.setCollection(new Backbone.Collection());
    this.$.storeSpecialSchedule.setCollection(new Backbone.Collection());
  },
  clearInfo: function () {
    this.$.storeCallCenterSchedule.collection.reset();
    this.$.storeSpecialSchedule.collection.reset();

    this.$.addressValue.setContent('');
    this.$.phoneNumber.setContent('');
    this.$.faxNumber.setContent('');
    this.$.email.setContent('');
    this.$.cif.setContent('');
  },
  loadInfo: function (storeId) {
    var me = this,
        params = {},
        currentDate = new Date(),
        process = new OB.DS.Process('org.openbravo.retail.posterminal.master.CrossStoreInfo'),
        i;

    params.terminalTime = currentDate;
    params.terminalTimeOffset = {
      value: currentDate.getTimezoneOffset(),
      type: 'long'
    };

    this.clearInfo();

    process.exec({
      org: storeId,
      parameters: params
    }, enyo.bind(me, function (data) {
      if (data && data.exception) {
        OB.UTIL.showError(data.exception.message);
      } else if (data && !data.exception && data.length > 0) {
        this.$.addressValue.setContent(data[0].address);
        this.$.phoneNumber.setContent(data[0].phone);
        this.$.faxNumber.setContent(data[0].alternativePhone);
        this.$.email.setContent(data[0].email);
        this.$.cif.setContent(data[0].taxID);

        var orgSchedule = [],
            callCenter = [],
            specialdate = [],
            specialHourIndex = 0,
            specialHolidayIndex = 0,
            dateFormat = OB.Format.date;

        for (i = 1; i < data.length; i++) {
          if (data[i].scheduletype && data[i].scheduletype === 'Store Schedule') {
            orgSchedule.push(data[i]);
          } else if (data[i].scheduletype && data[i].scheduletype === 'Call Center Schedule') {
            callCenter.push(data[i]);
          } else if (data[i].specialdate) {
            var date = {};
            if (data[i].startingTime) {
              if (specialdate[specialHourIndex]) {
                specialdate[specialHourIndex].specialdate = OB.Utilities.Date.JSToOB(new Date(data[i].specialdate), dateFormat) + ' ' + this.getTime(data[i]);
              } else {
                date.specialdate = OB.Utilities.Date.JSToOB(new Date(data[i].specialdate), dateFormat) + ' ' + this.getTime(data[i]);
                date.closingHoliday = null;
                specialdate.push(date);
              }
              specialHourIndex++;
            } else {
              if (specialdate[specialHolidayIndex]) {
                specialdate[specialHolidayIndex].closingHoliday = OB.Utilities.Date.JSToOB(new Date(data[i].specialdate), dateFormat);
              } else {
                date.specialdate = null;
                date.closingHoliday = OB.Utilities.Date.JSToOB(new Date(data[i].specialdate), dateFormat);
                specialdate.push(date);
              }
              specialHolidayIndex++;
            }
          }
        }

        this.showScheduleInfo(orgSchedule, callCenter, specialdate);
      }
    }), function (error) {
      OB.UTIL.showError(error);
    });
  },
  showScheduleInfo: function (orgSchedule, callCenter, specialdate) {
    var scheduleIndex = 0,
        callCenterIndex = 0,
        weekday, scheduleCallCenterLine = {},
        scheduleCallCenter = [];
    while (scheduleIndex < orgSchedule.length || callCenterIndex < callCenter.length) {
      scheduleCallCenterLine = {};
      weekday = scheduleIndex < orgSchedule.length ? orgSchedule[scheduleIndex].weekday : '7';
      if (callCenterIndex < callCenter.length && weekday > callCenter[callCenterIndex].weekday) {
        weekday = callCenter[callCenterIndex].weekday;
      }

      scheduleCallCenterLine.weekday = this.getWeekDay(weekday);

      if (scheduleIndex < orgSchedule.length && weekday === orgSchedule[scheduleIndex].weekday) {
        scheduleCallCenterLine.schedule = this.getTime(orgSchedule[scheduleIndex]);
        scheduleIndex++;
      }

      if (callCenterIndex < callCenter.length && weekday === callCenter[callCenterIndex].weekday) {
        scheduleCallCenterLine.callCenter = this.getTime(callCenter[callCenterIndex]);
        callCenterIndex++;
      }
      scheduleCallCenter.push(scheduleCallCenterLine);
    }
    this.$.storeCallCenterSchedule.collection.reset(scheduleCallCenter);
    this.$.storeSpecialSchedule.collection.reset(specialdate);
  },
  getWeekDay: function (weekday) {
    if (weekday === '7') {
      weekday = '0';
    }
    return OB.I18N.getWeekday(weekday);
  },
  getTime: function (schedule) {
    var startTime = OB.I18N.formatHour(new Date(schedule.startingTime));
    var endTime = OB.I18N.formatHour(new Date(schedule.endingTime));
    return startTime + ' - ' + endTime;
  }
});

enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OBPOS.UI.StoreInformationScheduleScrollableHeader',
  classes: 'obpos-store-information-header',
  components: [{
    components: [{
      classes: 'obpos-store-information-header-icon',
      components: [{
        classes: 'obpos-icon-schedule',
        name: 'iconSchedule'
      }]
    }, {
      classes: 'obpos-store-information-openhour',
      name: 'openHour'
    }, {
      classes: 'obpos-store-information-empty9'
    }, {
      classes: 'obpos-store-information-callcenter',
      name: 'callCenter'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.openHour.setContent(OB.I18N.getLabel('OBPOS_LblOpenHour'));
    this.$.callCenter.setContent(OB.I18N.getLabel('OBPOS_LblCallCenter'));
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationScheduleLine',
  kind: 'OB.UI.listItemButton',
  components: [{
    classes: 'obpos-row-store-space'
  }, {
    classes: 'obpos-row-store-info span3',
    name: 'weekday'
  }, {
    classes: 'obpos-row-store-info span4 obpos-center-text',
    name: 'openHour'
  }, {
    classes: 'obpos-row-store-info',
    name: 'callCenter'
  }],
  create: function () {
    this.inherited(arguments);
    this.$.weekday.setContent(this.model.get('weekday'));
    this.$.openHour.setContent(this.model.get('schedule'));
    this.$.callCenter.setContent(this.model.get('callCenter'));
  }
});

enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OBPOS.UI.StoreInformationSpecialScheduleScrollableHeader',
  classes: 'obpos-store-information-header',
  components: [{
    components: [{
      classes: 'obpos-row-store-space'
    }, {
      classes: 'span5 obpos-store-information-specialOpenHour',
      name: 'specialOpenHour'
    }, {
      classes: 'obpos-store-information-empty1'
    }, {
      classes: 'obpos-store-information-callcenter',
      name: 'specialCloseHour'
    }]
  }],
  initComponents: function () {
    this.inherited(arguments);
    this.$.specialOpenHour.setContent(OB.I18N.getLabel('OBPOS_LblSpecialOpenHour'));
    this.$.specialCloseHour.setContent(OB.I18N.getLabel('OBPOS_LblCloseHolidays'));
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationSpecialScheduleLine',
  kind: 'OB.UI.listItemButton',
  components: [{
    classes: 'obpos-row-store-space'
  }, {
    classes: 'obpos-row-store-info span6',
    name: 'openHour'
  }, {
    classes: 'obpos-row-store-info',
    name: 'closeHour'
  }],
  create: function () {
    this.inherited(arguments);
    this.$.openHour.setContent(this.model.get('specialdate'));
    this.$.closeHour.setContent(this.model.get('closingHoliday'));
  }
});