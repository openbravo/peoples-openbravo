/*
 ************************************************************************************
 * Copyright (C) 2019-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/* global enyo */

enyo.kind({
  name: 'OBPOS.UI.StoreInformation',
  kind: 'OB.UI.Modal',
  classes: 'obposUiStoreInformation',
  body: {
    kind: 'OBPOS.UI.StoreInformationLine'
  },
  footer: {
    kind: 'OBPOS.UI.StoreInformationFooter'
  },
  handlers: {
    onHideInfoPopup: 'hidePopup',
    onShowInfoPopup: 'showPopup'
  },
  storeId: null,
  executeOnShow: function() {
    this.setHeader(this.args.orgName);
    this.storeId = this.args.orgId;
    this.$.body.$.storeInformationLine.loadInfo(this.storeId);
  },
  initComponents: function() {
    this.inherited(arguments);
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationLine',
  classes: 'obposUiStoreInformationLine',
  components: [
    {
      classes: 'obposUiStoreInformationLine-container1',
      components: [
        {
          classes: 'obposUiStoreInformationLine-container1-iconAddress',
          name: 'iconAddress'
        },
        {
          classes: 'obposUiStoreInformationLine-container1-addressValue',
          name: 'addressValue'
        }
      ]
    },
    {
      classes: 'obposUiStoreInformationLine-element1'
    },
    {
      classes: 'obposUiStoreInformationLine-container2',
      components: [
        {
          classes: 'obposUiStoreInformationLine-container2-container1',
          components: [
            {
              classes:
                'obposUiStoreInformationLine-container2-container1-iconPhone',
              name: 'iconPhone'
            },
            {
              classes:
                'obposUiStoreInformationLine-container2-container1-phoneNumber',
              name: 'phoneNumber'
            }
          ]
        },
        {
          classes: 'obposUiStoreInformationLine-container2-container2',
          components: [
            {
              classes:
                'obposUiStoreInformationLine-container2-container1-iconFax',
              name: 'iconFax'
            },
            {
              classes:
                'obposUiStoreInformationLine-container2-container1-faxNumber',
              name: 'faxNumber'
            }
          ]
        }
      ]
    },
    {
      classes: 'obposUiStoreInformationLine-container3',
      components: [
        {
          classes: 'obposUiStoreInformationLine-container3-iconEmail',
          name: 'iconEmail'
        },
        {
          classes: 'obposUiStoreInformationLine-container3-email',
          name: 'email'
        }
      ]
    },
    {
      classes: 'obposUiStoreInformationLine-container4',
      components: [
        {
          classes: 'obposUiStoreInformationLine-container4-iconCif',
          name: 'iconCIF',
          initComponents: function() {
            this.setContent('CIF');
          }
        },
        {
          classes: 'obposUiStoreInformationLine-container4-cif',
          name: 'cif'
        }
      ]
    },
    {
      classes: 'obposUiStoreInformationLine-container5',
      components: [
        {
          name: 'storeCallCenterSchedule',
          kind: 'OB.UI.ScrollableTable',
          classes:
            'obposUiStoreInformationLine-container5-storeCallCenterSchedule',
          renderHeader: 'OBPOS.UI.StoreInformationScheduleScrollableHeader',
          renderLine: 'OBPOS.UI.StoreInformationScheduleLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }
      ]
    },
    {
      classes: 'obposUiStoreInformationLine-container6',
      components: [
        {
          name: 'storeSpecialSchedule',
          kind: 'OB.UI.ScrollableTable',
          classes:
            'obposUiStoreInformationLine-container6-storeSpecialSchedule',
          renderHeader:
            'OBPOS.UI.StoreInformationSpecialScheduleScrollableHeader',
          renderLine: 'OBPOS.UI.StoreInformationSpecialScheduleLine',
          renderEmpty: 'OB.UI.RenderEmpty'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);

    this.$.storeCallCenterSchedule.setCollection(new Backbone.Collection());
    this.$.storeSpecialSchedule.setCollection(new Backbone.Collection());
  },
  clearInfo: function() {
    this.$.storeCallCenterSchedule.collection.reset();
    this.$.storeSpecialSchedule.collection.reset();

    this.$.addressValue.setContent('');
    this.$.phoneNumber.setContent('');
    this.$.faxNumber.setContent('');
    this.$.email.setContent('');
    this.$.cif.setContent('');
  },
  loadInfo: function(storeId) {
    var me = this,
      params = {},
      currentDate = new Date(),
      process = new OB.DS.Process(
        'org.openbravo.retail.posterminal.master.CrossStoreInfo'
      ),
      i;

    params.terminalTime = currentDate;
    params.terminalTimeOffset = {
      value: currentDate.getTimezoneOffset(),
      type: 'long'
    };

    this.clearInfo();

    process.exec(
      {
        org: storeId,
        parameters: params
      },
      enyo.bind(me, function(data) {
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
            if (
              data[i].scheduletype &&
              data[i].scheduletype === 'Store Schedule'
            ) {
              orgSchedule.push(data[i]);
            } else if (
              data[i].scheduletype &&
              data[i].scheduletype === 'Call Center Schedule'
            ) {
              callCenter.push(data[i]);
            } else if (data[i].specialdate) {
              var date = {};
              if (data[i].startingTime) {
                if (specialdate[specialHourIndex]) {
                  specialdate[specialHourIndex].specialdate =
                    OB.Utilities.Date.JSToOB(
                      new Date(data[i].specialdate),
                      dateFormat
                    ) +
                    ' ' +
                    this.getTime(data[i]);
                } else {
                  date.specialdate =
                    OB.Utilities.Date.JSToOB(
                      new Date(data[i].specialdate),
                      dateFormat
                    ) +
                    ' ' +
                    this.getTime(data[i]);
                  date.closingHoliday = null;
                  specialdate.push(date);
                }
                specialHourIndex++;
              } else {
                if (specialdate[specialHolidayIndex]) {
                  specialdate[
                    specialHolidayIndex
                  ].closingHoliday = OB.Utilities.Date.JSToOB(
                    new Date(data[i].specialdate),
                    dateFormat
                  );
                } else {
                  date.specialdate = null;
                  date.closingHoliday = OB.Utilities.Date.JSToOB(
                    new Date(data[i].specialdate),
                    dateFormat
                  );
                  specialdate.push(date);
                }
                specialHolidayIndex++;
              }
            }
          }

          this.showScheduleInfo(orgSchedule, callCenter, specialdate);
        }
      }),
      function(error) {
        OB.UTIL.showError(error);
      }
    );
  },
  showScheduleInfo: function(orgSchedule, callCenter, specialdate) {
    var scheduleIndex = 0,
      callCenterIndex = 0,
      weekday,
      scheduleCallCenterLine = {},
      scheduleCallCenter = [];
    while (
      scheduleIndex < orgSchedule.length ||
      callCenterIndex < callCenter.length
    ) {
      scheduleCallCenterLine = {};
      weekday =
        scheduleIndex < orgSchedule.length
          ? orgSchedule[scheduleIndex].weekday
          : '7';
      if (
        callCenterIndex < callCenter.length &&
        weekday > callCenter[callCenterIndex].weekday
      ) {
        weekday = callCenter[callCenterIndex].weekday;
      }

      scheduleCallCenterLine.weekday = this.getWeekDay(weekday);

      if (
        scheduleIndex < orgSchedule.length &&
        weekday === orgSchedule[scheduleIndex].weekday
      ) {
        scheduleCallCenterLine.schedule = this.getTime(
          orgSchedule[scheduleIndex]
        );
        scheduleIndex++;
      }

      if (
        callCenterIndex < callCenter.length &&
        weekday === callCenter[callCenterIndex].weekday
      ) {
        scheduleCallCenterLine.callCenter = this.getTime(
          callCenter[callCenterIndex]
        );
        callCenterIndex++;
      }
      scheduleCallCenter.push(scheduleCallCenterLine);
    }
    this.$.storeCallCenterSchedule.collection.reset(scheduleCallCenter);
    this.$.storeSpecialSchedule.collection.reset(specialdate);
  },
  getWeekDay: function(weekday) {
    if (weekday === '7') {
      weekday = '0';
    }
    return OB.I18N.getWeekday(weekday);
  },
  getTime: function(schedule) {
    var startTime = OB.I18N.formatHour(new Date(schedule.startingTime));
    var endTime = OB.I18N.formatHour(new Date(schedule.endingTime));
    return startTime + ' - ' + endTime;
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationFooter',
  classes: 'obUiModal-footer-mainButtons obposUiStoreInformationFooter',
  components: [
    {
      classes: 'obposUiStoreInformationFooter-btnClose',
      name: 'btnClose',
      kind: 'OB.UI.ModalDialogButton',
      i18nLabel: 'OBPOS_LblSlaveClose',
      tap: function() {
        if (this.disabled) {
          return true;
        }
        this.owner.owner.owner.hide();
      }
    }
  ]
});

enyo.kind({
  kind: 'OB.UI.ScrollableTableHeader',
  name: 'OBPOS.UI.StoreInformationScheduleScrollableHeader',
  classes: 'obposUiStoreInformationScheduleScrollableHeader',
  components: [
    {
      classes: 'obposUiStoreInformationScheduleScrollableHeader-container1',
      components: [
        {
          classes:
            'obposUiStoreInformationScheduleScrollableHeader-container1-container1',
          components: [
            {
              classes:
                'obposUiStoreInformationScheduleScrollableHeader-container1-container1-iconSchedule',
              name: 'iconSchedule'
            }
          ]
        },
        {
          classes:
            'obposUiStoreInformationScheduleScrollableHeader-container1-openHour',
          name: 'openHour'
        },
        {
          classes:
            'obposUiStoreInformationScheduleScrollableHeader-container1-element1'
        },
        {
          classes:
            'obposUiStoreInformationScheduleScrollableHeader-container1-callCenter',
          name: 'callCenter'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.openHour.setContent(OB.I18N.getLabel('OBPOS_LblOpenHour'));
    this.$.callCenter.setContent(OB.I18N.getLabel('OBPOS_LblCallCenter'));
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationScheduleLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obposUiStoreInformationScheduleLine',
  components: [
    {
      classes: 'obposUiStoreInformationScheduleLine-element1'
    },
    {
      classes: 'obposUiStoreInformationScheduleLine-weekday',
      name: 'weekday'
    },
    {
      classes: 'obposUiStoreInformationScheduleLine-openHour',
      name: 'openHour'
    },
    {
      classes: 'obposUiStoreInformationScheduleLine-callCenter',
      name: 'callCenter'
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.weekday.setContent(this.model.get('weekday'));
    this.$.openHour.setContent(this.model.get('schedule'));
    this.$.callCenter.setContent(this.model.get('callCenter'));
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationSpecialScheduleScrollableHeader',
  kind: 'OB.UI.ScrollableTableHeader',
  classes: 'obposUiStoreInformationSpecialScheduleScrollableHeader',
  components: [
    {
      components: [
        {
          classes: 'obpos-row-store-space'
        },
        {
          classes:
            'obposUiStoreInformationSpecialScheduleScrollableHeader-specialOpenHour',
          name: 'specialOpenHour'
        },
        {
          classes:
            'obposUiStoreInformationSpecialScheduleScrollableHeader-element2'
        },
        {
          classes:
            'obposUiStoreInformationSpecialScheduleScrollableHeader-specialCloseHour',
          name: 'specialCloseHour'
        }
      ]
    }
  ],
  initComponents: function() {
    this.inherited(arguments);
    this.$.specialOpenHour.setContent(
      OB.I18N.getLabel('OBPOS_LblSpecialOpenHour')
    );
    this.$.specialCloseHour.setContent(
      OB.I18N.getLabel('OBPOS_LblCloseHolidays')
    );
  }
});

enyo.kind({
  name: 'OBPOS.UI.StoreInformationSpecialScheduleLine',
  kind: 'OB.UI.listItemButton',
  classes: 'obposUiStoreInformationSpecialScheduleLine',
  components: [
    {
      classes: 'obposUiStoreInformationSpecialScheduleLine-element1'
    },
    {
      classes: 'obposUiStoreInformationSpecialScheduleLine-openHour',
      name: 'openHour'
    },
    {
      classes: 'obposUiStoreInformationSpecialScheduleLine-closeHour',
      name: 'closeHour'
    }
  ],
  create: function() {
    this.inherited(arguments);
    this.$.openHour.setContent(this.model.get('specialdate'));
    this.$.closeHour.setContent(this.model.get('closingHoliday'));
  }
});
