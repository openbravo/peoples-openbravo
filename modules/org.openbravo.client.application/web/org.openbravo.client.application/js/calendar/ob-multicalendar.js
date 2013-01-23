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

isc.ClassFactory.defineClass('OBMultiCalendar', isc.HLayout);

isc.ClassFactory.defineClass('OBMultiCalendarLeftControls', isc.VLayout);

isc.ClassFactory.defineClass('OBMultiCalendarCalendar', isc.OBCalendar);

isc.ClassFactory.defineClass('OBMultiCalendarLegend', isc.VLayout);

isc.ClassFactory.defineClass('OBMultiCalendarLegendElement', isc.HLayout);

isc.OBMultiCalendarLegendElement.addProperties({
  height: 20,
  width: 162,
  color: null,
  name: null,
  id: null,
  checked: true,
  overflow: 'hidden',
  initWidget: function () {
    var checkbox, color, name;
    this.Super('initWidget', arguments);
    if (this.checked === 'true') {
      this.checked = true;
    }
    if (this.checked === 'false') {
      this.checked = false;
    }
    OB.Utilities.Style.addRule('.bgColor_' + this.color, 'background-color: ' + OB.Utilities.getRGBAStringFromOBColor(this.color) + ';' + 'color: ' + (OB.Utilities.getBrightFromOBColor(this.color) > 125 ? 'black' : 'white'));
    checkbox = isc.DynamicForm.create({
      width: 20,
      checked: this.checked,
      fields: [{
        height: 16,
        width: 20,
        showTitle: false,
        value: this.checked,
        changed: function (form, item, value) {
          var calendarData = form.parentElement.multiCalendar.calendarData,
              i;
          this.Super('changed', arguments);
          for (i = 0; i < calendarData.calendars.length; i++) {
            if (calendarData.calendars[i].id === form.parentElement.id) {
              calendarData.calendars[i].checked = value;
            }
          }
          form.parentElement.multiCalendar.refreshCalendar();
        },
        type: 'checkbox'
      }]
    });
    color = isc.Layout.create({
      width: 15,
      height: 18,
      styleName: 'OBMultiCalendarLegendElementColor',
      backgroundColor: OB.Utilities.getRGBAStringFromOBColor(this.color)
    });
    name = isc.Label.create({
      height: 10,
      width: 118,
      styleName: 'OBMultiCalendarLegendElementName',
      contents: this.name
    });
    this.addMembers([checkbox, color, name]);
  }
});

isc.OBMultiCalendarLegend.addProperties({
  // height: '*',
  overflow: 'auto',
  membersMargin: 5,

  initWidget: function () {
    this.multiCalendar.OBMultiCalendarLegend = this;
    this.Super('initWidget', arguments);
  },

  updateMembers: function (newMembers) {
    var i;
    if (this.members) {
      for (i = this.members.length - 1; i > -1; i--) {
        this.members[i].destroy();
      }
    }
    this.multiCalendar.eventStyles = {};

    for (i = 0; i < newMembers.length; i++) {
      this.addMember(isc.OBMultiCalendarLegendElement.create({
        multiCalendar: this.multiCalendar,
        color: newMembers[i].color,
        name: newMembers[i].name,
        id: newMembers[i].id,
        checked: newMembers[i].checked
      }));
      this.multiCalendar.eventStyles[newMembers[i].id] = 'bgColor_' + newMembers[i].color;
    }

    if (this.multiCalendar.leftControls) {
      // initialized so refresh
      this.multiCalendar.refreshCalendar();
    }
  }
});

isc.OBMultiCalendarLeftControls.addProperties({
  width: '200',
  height: '100%',
  layoutLeftMargin: 10,
  layoutRightMargin: 10,
  layoutTopMargin: 10,
  membersMargin: 5,
  defaultLayoutAlign: 'center',
  filter: null,
  dateChooser: null,
  legend: null,
  getFilterValueMap: function () {
    var filterObj = {},
        calendarData = this.multiCalendar.calendarData,
        i;
    for (i = 0; i < calendarData.filters.length; i++) {
      filterObj[calendarData.filters[i].id] = calendarData.filters[i].name;
    }
    return filterObj;
  },
  getLegendValueMap: function () {
    var calendarData = this.multiCalendar.calendarData,
        legendArray = [],
        i;
    for (i = 0; i < calendarData.calendars.length; i++) {
      if (calendarData.hasFilter === false || calendarData.calendars[i].filterId === this.filter.getValue('filter')) {
        legendArray.push(calendarData.calendars[i]);
      }
    }
    return legendArray;
  },
  initWidget: function () {
    var button, label, legend, leftControls = this;
    this.Super('initWidget', arguments);
    if (this.multiCalendar.calendarData.hasFilter) {
      this.filter = isc.DynamicForm.create({
        fields: [{
          name: 'filter',
          title: leftControls.multiCalendar.filterName,
          type: 'comboBox',
          valueMap: leftControls.getFilterValueMap(),
          value: leftControls.multiCalendar.calendarData.currentFilter,
          width: 180,
          titleOrientation: 'top',
          required: true,
          changed: function (form, item, value) {
            this.Super('changed', arguments);
            leftControls.multiCalendar.calendarData.currentFilter = value;
            leftControls.legend.updateMembers(leftControls.getLegendValueMap());
          },

          cellStyle: OB.Styles.OBFormField.DefaultComboBox.cellStyle,
          titleStyle: OB.Styles.OBFormField.DefaultComboBox.titleStyle,
          textBoxStyle: OB.Styles.OBFormField.DefaultComboBox.textBoxStyle,
          pendingTextBoxStyle: OB.Styles.OBFormField.DefaultComboBox.pendingTextBoxStyle,
          controlStyle: OB.Styles.OBFormField.DefaultComboBox.controlStyle,
          pickListBaseStyle: OB.Styles.OBFormField.DefaultComboBox.pickListBaseStyle,
          pickListTallBaseStyle: OB.Styles.OBFormField.DefaultComboBox.pickListTallBaseStyle,
          pickerIconStyle: OB.Styles.OBFormField.DefaultComboBox.pickerIconStyle,
          pickerIconSrc: OB.Styles.OBFormField.DefaultComboBox.pickerIconSrc,
          height: OB.Styles.OBFormField.DefaultComboBox.height,
          pickerIconWidth: OB.Styles.OBFormField.DefaultComboBox.pickerIconWidth,
          // fixes issue https://issues.openbravo.com/view.php?id=15105
          pickListCellHeight: OB.Styles.OBFormField.DefaultComboBox.quickRunPickListCellHeight,
          recentPropertyName: this.recentPropertyName,
          pickListProperties: {
            textMatchStyle: 'substring',
            selectionType: 'single',
            bodyStyleName: OB.Styles.OBFormField.DefaultComboBox.pickListProperties.bodyStyleName
          },
          pickListHeaderHeight: 0
        }]
      });
    } else {
      this.filter = isc.VLayout.create({
        height: 8
      });
    }
    button = isc.OBLinkButtonItem.create({
      width: 180,
      title: '[ Create Event ]',
      click: function () {
        leftControls.multiCalendar.calendar.addEventButton.click();
      }
    });
    this.dateChooser = isc.OBDateChooser.create({
      autoHide: false,
      showCancelButton: false,
      firstDayOfWeek: this.multiCalendar.firstDayOfWeek,
      dataChanged: function (param) {
        this.parentElement.multiCalendar.calendar.setChosenDate(this.getData());
        this.parentElement.multiCalendar.calendar.setCurrentViewName('day');
      }
    });
    label = isc.Label.create({
      height: 10,
      contents: this.multiCalendar.legendName + ' :'
    });
    this.legend = isc.OBMultiCalendarLegend.create({
      multiCalendar: this.multiCalendar
    });
    this.legend.updateMembers(leftControls.getLegendValueMap());
    this.addMembers([this.filter]);
    if (this.multiCalendar.canCreateEvents) {
      this.addMembers([button]);
    }
    this.addMembers([this.dateChooser, label, this.legend]);
  }
});

isc.OBMultiCalendar.addProperties({
  width: '100%',
  height: '100%',
  filterName: 'Filter',
  legendName: 'Legend',
  defaultViewName: null,
  calendarData: null,
  showLeftControls: true,
  showCustomEventsBgColor: true,

  parseCalendarData: function (calendarData) {
    var canCreateEvents, i;
    if (calendarData.filters) {
      calendarData.hasFilter = true;
    } else {
      calendarData.hasFilter = false;
    }
    for (i = 0; i < calendarData.calendars.length; i++) {
      if (typeof calendarData.calendars[i].checked === 'undefined') {
        calendarData.calendars[i].checked = true;
      }
      if (typeof calendarData.calendars[i].color === 'undefined') {
        calendarData.calendars[i].color = OB.Utilities.generateOBColor(
        null, null, null, 100, calendarData.calendars[i].id);
      }
      if (i === 0 && typeof calendarData.calendars[i].canCreateEvents !== 'undefined') {
        canCreateEvents = false;
      }
      if (typeof calendarData.calendars[i].canCreateEvents !== 'undefined' && canCreateEvents === false && calendarData.calendars[i].canCreateEvents === true) {
        canCreateEvents = true;
      }
      if (canCreateEvents === false && i === calendarData.calendars.length - 1) {
        this.canCreateEvents = false;
        this.calendarProps.canCreateEvents = false;
      }
    }
    return calendarData;
  },
  setLoading: function (value) {
    if (value !== false) {
      if (this.members[1]) {
        this.members[1].hide();
      }
      if (this.members[2]) {
        this.members[2].hide();
      }
      if (this.members[0]) {
        this.members[0].show();
      }
    } else {
      if (this.members[0]) {
        this.members[0].hide();
      }
      if (this.members[1]) {
        this.members[1].show();
      }
      if (this.members[2]) {
        this.members[2].show();
      }
    }
  },
  initComponents: function () {
    var callback, i, me = this;
    for (i = this.members.length - 1; i > -1; i--) {
      this.members[i].destroy();
    }
    if (this.calendarProps.firstDayOfWeek) {
      this.firstDayOfWeek = this.calendarProps.firstDayOfWeek;
    } else {
      this.firstDayOfWeek = 1;
    }
    if (this.calendarProps.filterName) {
      this.filterName = this.calendarProps.filterName;
    }
    if (this.calendarProps.legendName) {
      this.legendName = this.calendarProps.legendName;
    }
    if (typeof this.calendarProps.showLeftControls !== 'undefined') {
      this.showLeftControls = this.calendarProps.showLeftControls;
    }
    if (typeof this.calendarProps.showCustomEventsBgColor !== 'undefined') {
      this.showCustomEventsBgColor = this.calendarProps.showCustomEventsBgColor;
    }
    if (typeof this.calendarProps.canCreateEvents !== 'undefined') {
      this.canCreateEvents = this.calendarProps.canCreateEvents;
    }
    this.addMembers([OB.Utilities.createLoadingLayout()]);
    callback = function (rpcResponse, data, rpcRequest) {
      me.calendarData = me.parseCalendarData(data);
      me.drawComponents();
    };
    OB.RemoteCallManager.call(this.calendarProps.calendarDataActionHandler, {
      action: 'calendarData'
    }, {}, callback);
  },

  initWidget: function () {
    this.initComponents();
    this.Super('initWidget', arguments);
  },
  drawComponents: function () {
    if (this.canCreateEvents) {
      this.showCustomEventsBgColor = true;
    }

    this.leftControls = isc.OBMultiCalendarLeftControls.create({
      multiCalendar: this
    });
    this.calendar = isc.OBMultiCalendarCalendar.create(isc.addProperties(this.calendarProps, {
      multiCalendar: this
    }));
    this.setLoading(false);
    if (this.showLeftControls) {
      this.addMembers([this.leftControls]);
    }
    this.addMembers([isc.VLayout.create({
      members: [this.calendar]
    })]);
    this.refreshCalendar();
  },

  refreshCalendar: function () {
    if (this.calendar) {
      this.calendar.filterData();
    }
  }
});