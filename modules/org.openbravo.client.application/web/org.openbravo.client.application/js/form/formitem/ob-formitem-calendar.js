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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// == OBClientClassCanvasItem ==
// Extends Calendar, with some customizations (most of them styling related)
isc.ClassFactory.defineClass('OBCalendar', isc.Calendar);

isc.ClassFactory.defineClass('OBCalendarTabSet', isc.TabSet);


isc.OBCalendar.addProperties({
  initWidget: function () {
    this.firstDayOfWeek = 1;
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
    this.Super('initWidget', arguments);
    this.controlsBar.reorderMember(4, 1); // Moves the 'next' button to the second position
    this.controlsBar.reorderMember(2, 4); // Moves the 'displayed date' to last position
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
  }
});