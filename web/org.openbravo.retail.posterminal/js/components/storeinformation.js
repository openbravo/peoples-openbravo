/*
 ************************************************************************************
 * Copyright (C) 2019 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global enyo, Backbone, _ */

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
    var me = this;
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
      classes: 'obpos-icon-cif obpos-row-store-info',
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
      scrollAreaMaxHeight: '180px',
      renderHeader: 'OBPOS.UI.StoreInformationScheduleScrollableHeader',
      renderLine: 'OBPOS.UI.StoreInformationScheduleLine',
      renderEmpty: 'OB.UI.RenderEmpty'
    }]
  }, {
    classes: 'obpos-store-line span12 obpos-store-line-header2',
    components: [{
      classes: 'span5',
      name: 'openHolidays',
      components: [{
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblOpenHolidays'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleHolidays'
      }, {
        classes: 'obpos-clear-both obpos-store-line-header'
      }, {
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblSpecialOpenHour'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleSpecialOpenHour'
      }]
    }, {
      classes: 'span6',
      name: 'closeHolidays',
      components: [{
        classes: 'obpos-store-line-header obpos-center-text',
        initComponents: function () {
          this.setContent(OB.I18N.getLabel('OBPOS_LblCloseHolidays'));
        }
      }, {
        classes: 'obpos-clear-both obpos-center-text',
        name: 'exampleCloseHolidays'
      }]
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

  storeCallCenterList: null,

  initComponents: function () {
    this.inherited(arguments);

    this.storeCallCenterList = new Backbone.Collection();
    this.$.storeCallCenterSchedule.setCollection(this.storeCallCenterList);

    this.$.exampleHolidays.setContent('07/07/18 10:00 - 22:00');
    this.$.exampleCloseHolidays.setContent('18/11/18');
    this.$.exampleSpecialOpenHour.setContent('24/12/18 10:00 - 20:00');
  },
  clearInfo: function () {
    this.$.storeCallCenterSchedule.collection.reset();

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
      if (data && !data.exception && data.length > 0) {
        this.$.addressValue.setContent(data[0].address);
        this.$.phoneNumber.setContent(data[0].phone);
        this.$.faxNumber.setContent(data[0].alternativePhone);
        this.$.email.setContent(data[0].email);
        this.$.cif.setContent(data[0].taxID);

        var orgSchedule = [],
            callCenter = [],
            specialdate = [];

        for (i = 1; i < data.length; i++) {
          if (data[i].scheduletype && data[i].scheduletype === 'Store Schedule') {
            orgSchedule.push(data[i]);
          } else if (data[i].scheduletype && data[i].scheduletype === 'Call Center Schedule') {
            callCenter.push(data[i]);
          } else if (data[i].specialdate) {
            specialdate.push(data[i]);
          }
        }

        this.showScheduleInfo(orgSchedule, callCenter, specialdate);
      }
    }));
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
  },
  getWeekDay: function (weekday) {
    if (weekday === '7') {
      weekday = '0';
    }
    return OB.I18N.getWeekday(weekday);
  },
  getTime: function (schedule) {
    var startTime = OB.Utilities.Date.JSToOB(new Date(schedule.startingTime), '%H:%M');
    var endTime = OB.Utilities.Date.JSToOB(new Date(schedule.endingTime), '%H:%M');
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
      classes: 'obpos-store-information-empty'
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