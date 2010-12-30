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
 * All portions are Copyright (C) 2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
*/
// jslint

// is placed here because the smartclient labels are loaded just after the smartclient
// core
isc.setAutoDraw(false);

/*
 * The code below sets all standard Smartclient system labels.
 * For more information see the 'Internationalization and Localization (i18n,l10n)'
 * section of the Smartclient reference.
 * 
 * Note: Smartclient label properties can be class or instance properties. For instance 
 * properties a call to addProperties needs to be done.
*/

// note different locales can have different starting day of the week
// new Date(2000, 0, 2).getDay() is a sunday, so start from there
isc.Date.shortDayNames = [];
isc.Date.shortDayNames[new Date(2000, 0, 2).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Sun');
isc.Date.shortDayNames[new Date(2000, 0, 3).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Mon');
isc.Date.shortDayNames[new Date(2000, 0, 4).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Tue');
isc.Date.shortDayNames[new Date(2000, 0, 5).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Wed');
isc.Date.shortDayNames[new Date(2000, 0, 6).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Thu');
isc.Date.shortDayNames[new Date(2000, 0, 7).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Fri');
isc.Date.shortDayNames[new Date(2000, 0, 8).getDay()] = OB.I18N.getLabel('OBUISC_Date.shortDayNames.Sat');

isc.Date.shortMonthNames = [
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Jan'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Feb'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Mar'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Apr'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.May'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Jun'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Jul'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Aug'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Sep'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Oct'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Nov'),
    OB.I18N.getLabel('OBUISC_Date.shortMonthNames.Dec')
];
  
isc.Dialog.OK_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'); 
isc.Dialog.APPLY_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.APPLY_BUTTON_TITLE'); 
isc.Dialog.YES_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.YES_BUTTON_TITLE');
isc.Dialog.NO_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.NO_BUTTON_TITLE');
isc.Dialog.CANCEL_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE');
isc.Dialog.DONE_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.DONE_BUTTON_TITLE');
isc.Dialog.CONFIRM_TITLE = OB.I18N.getLabel('OBUISC_Dialog.CONFIRM_BUTTON_TITLE');
isc.Dialog.SAY_TITLE = OB.I18N.getLabel('OBUISC_Dialog.SAY_BUTTON_TITLE');
isc.Dialog.WARN_TITLE = OB.I18N.getLabel('OBUISC_Dialog.WARN_BUTTON_TITLE');
isc.Dialog.ASK_TITLE = OB.I18N.getLabel('OBUISC_Dialog.ASK_TITLE');
isc.Dialog.ASK_FOR_VALUE_TITLE = OB.I18N.getLabel('OBUISC_Dialog.ASK_FOR_VALUE_TITLE');
isc.Dialog.LOGIN_TITLE = OB.I18N.getLabel('OBUISC_Dialog.LOGIN_TITLE');
isc.Dialog.USERNAME_TITLE = OB.I18N.getLabel('OBUISC_Dialog.USERNAME_TITLE');
isc.Dialog.PASSWORD_TITLE = OB.I18N.getLabel('OBUISC_Dialog.PASSWORD_TITLE');
isc.Dialog.LOGIN_BUTTON_TITLE = OB.I18N.getLabel('OBUISC_Dialog.LOGIN_BUTTON_TITLE');
isc.Dialog.LOGIN_ERROR_MESSAGE = OB.I18N.getLabel('OBUISC_Dialog.LOGIN_ERROR_MESSAGE');
isc.RPCManager.defaultPrompt = OB.I18N.getLabel('OBUISC_RPCManager.defaultPrompt');
isc.RPCManager.timeoutErrorMessage = OB.I18N.getLabel('OBUISC_RPCManager.timeoutErrorMessage');
isc.RPCManager.removeDataPrompt = OB.I18N.getLabel('OBUISC_RPCManager.removeDataPrompt');
isc.RPCManager.saveDataPrompt = OB.I18N.getLabel('OBUISC_RPCManager.saveDataPrompt');
isc.RPCManager.fetchDataPrompt = OB.I18N.getLabel('OBUISC_RPCManager.fetchDataPrompt');
isc.Operators.equalsTitle = OB.I18N.getLabel('OBUISC_Operators.equalsTitle');
isc.Operators.notEqualTitle = OB.I18N.getLabel('OBUISC_Operators.notEqualTitle');
isc.Operators.greaterThanTitle = OB.I18N.getLabel('OBUISC_Operators.greaterThanTitle');
isc.Operators.lessThanTitle = OB.I18N.getLabel('OBUISC_Operators.lessThanTitle');
isc.Operators.greaterOrEqualTitle = OB.I18N.getLabel('OBUISC_Operators.greaterOrEqualTitle');
isc.Operators.lessOrEqualTitle = OB.I18N.getLabel('OBUISC_Operators.lessOrEqualTitle');
isc.Operators.betweenTitle = OB.I18N.getLabel('OBUISC_Operators.betweenTitle');
isc.Operators.betweenInclusiveTitle = OB.I18N.getLabel('OBUISC_Operators.betweenInclusiveTitle');
isc.Operators.iContainsTitle = OB.I18N.getLabel('OBUISC_Operators.iContainsTitle');
isc.Operators.iStartsWithTitle = OB.I18N.getLabel('OBUISC_Operators.iStartsWithTitle');
isc.Operators.iEndsWithTitle = OB.I18N.getLabel('OBUISC_Operators.iEndsWithTitle');
isc.Operators.containsTitle = OB.I18N.getLabel('OBUISC_Operators.containsTitle');
isc.Operators.startsWithTitle = OB.I18N.getLabel('OBUISC_Operators.startsWithTitle');
isc.Operators.endsWithTitle = OB.I18N.getLabel('OBUISC_Operators.endsWithTitle');
isc.Operators.iNotContainsTitle = OB.I18N.getLabel('OBUISC_Operators.iNotContainsTitle');
isc.Operators.iNotStartsWithTitle = OB.I18N.getLabel('OBUISC_Operators.iNotStartsWithTitle');
isc.Operators.iNotEndsWithTitle = OB.I18N.getLabel('OBUISC_Operators.iNotEndsWithTitle');
isc.Operators.notContainsTitle = OB.I18N.getLabel('OBUISC_Operators.notContainsTitle');
isc.Operators.notStartsWithTitle = OB.I18N.getLabel('OBUISC_Operators.notStartsWithTitle');
isc.Operators.notEndsWithTitle = OB.I18N.getLabel('OBUISC_Operators.notEndsWithTitle');
isc.Operators.isNullTitle = OB.I18N.getLabel('OBUISC_Operators.isNullTitle');
isc.Operators.notNullTitle = OB.I18N.getLabel('OBUISC_Operators.notNullTitle');
isc.Operators.regexpTitle = OB.I18N.getLabel('OBUISC_Operators.regexpTitle');
isc.Operators.iregexpTitle = OB.I18N.getLabel('OBUISC_Operators.iregexpTitle');
isc.Operators.inSetTitle = OB.I18N.getLabel('OBUISC_Operators.inSetTitle');
isc.Operators.notInSetTitle = OB.I18N.getLabel('OBUISC_Operators.notInSetTitle');
isc.Operators.equalsFieldTitle = OB.I18N.getLabel('OBUISC_Operators.equalsFieldTitle');
isc.Operators.notEqualFieldTitle = OB.I18N.getLabel('OBUISC_Operators.notEqualFieldTitle');
isc.Operators.andTitle = OB.I18N.getLabel('OBUISC_Operators.andTitle');
isc.Operators.notTitle = OB.I18N.getLabel('OBUISC_Operators.notTitle');
isc.Operators.orTitle = OB.I18N.getLabel('OBUISC_Operators.orTitle');
isc.GroupingMessages.upcomingTodayTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingTodayTitle');
isc.GroupingMessages.upcomingTomorrowTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingTomorrowTitle');
isc.GroupingMessages.upcomingThisWeekTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingThisWeekTitle');
isc.GroupingMessages.upcomingNextWeekTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingNextWeekTitle');
isc.GroupingMessages.upcomingNextMonthTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingNextMonthTitle');
isc.GroupingMessages.upcomingBeforeTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingBeforeTitle');
isc.GroupingMessages.upcomingLaterTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.upcomingLaterTitle');
isc.GroupingMessages.byDayTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byDayTitle');
isc.GroupingMessages.byWeekTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byWeekTitle');
isc.GroupingMessages.byMonthTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byMonthTitle');
isc.GroupingMessages.byQuarterTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byQuarterTitle');
isc.GroupingMessages.byYearTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byYearTitle');
isc.GroupingMessages.byDayOfMonthTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byDayOfMonthTitle');
isc.GroupingMessages.byUpcomingTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byUpcomingTitle');
isc.GroupingMessages.byHoursTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byHoursTitle');
isc.GroupingMessages.byMinutesTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byMinutesTitle');
isc.GroupingMessages.bySecondsTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.bySecondsTitle');
isc.GroupingMessages.byMilisecondsTitle = OB.I18N.getLabel('OBUISC_GroupingMessages.byMilisecondsTitle');
isc.Validator.notABoolean = OB.I18N.getLabel('OBUISC_Validator.notABoolean');
isc.Validator.notAString = OB.I18N.getLabel('OBUISC_Validator.notAString');
isc.Validator.notAnInteger = OB.I18N.getLabel('OBUISC_Validator.notAnInteger');
isc.Validator.notADecimal = OB.I18N.getLabel('OBUISC_Validator.notADecimal');
isc.Validator.notADate = OB.I18N.getLabel('OBUISC_Validator.notADate');
isc.Validator.mustBeLessThan = OB.I18N.getLabel('OBUISC_Validator.mustBeLessThan');
isc.Validator.mustBeGreaterThan = OB.I18N.getLabel('OBUISC_Validator.mustBeGreaterThan');
isc.Validator.mustBeLaterThan = OB.I18N.getLabel('OBUISC_Validator.mustBeLaterThan');
isc.Validator.mustBeEarlierThan = OB.I18N.getLabel('OBUISC_Validator.mustBeEarlierThan');
isc.Validator.mustBeShorterThan = OB.I18N.getLabel('OBUISC_Validator.mustBeShorterThan');
isc.Validator.mustBeLongerThan = OB.I18N.getLabel('OBUISC_Validator.mustBeLongerThan');
isc.Validator.mustBeExactLength = OB.I18N.getLabel('OBUISC_Validator.mustBeExactLength');
isc.Validator.requiredField = OB.I18N.getLabel('OBUISC_Validator.requiredField');
isc.Validator.notOneOf = OB.I18N.getLabel('OBUISC_Validator.notOneOf');
isc.Time.AMIndicator = OB.I18N.getLabel('OBUISC_Time.AMIndicator');
isc.Time.PMIndicator = OB.I18N.getLabel('OBUISC_Time.PMIndicator');
isc.Window.title = OB.I18N.getLabel('OBUISC_Window.title');
isc.FilterBuilder.removeButtonPrompt = OB.I18N.getLabel('OBUISC_FilterBuilder.removeButtonPrompt');
isc.FilterBuilder.addButtonPrompt = OB.I18N.getLabel('OBUISC_FilterBuilder.addButtonPrompt');
isc.FilterBuilder.rangeSeparator = OB.I18N.getLabel('OBUISC_FilterBuilder.rangeSeparator');
isc.FilterBuilder.subClauseButtonTitle = OB.I18N.getLabel('OBUISC_FilterBuilder.subClauseButtonTitle');
isc.FilterBuilder.subClauseButtonPrompt = OB.I18N.getLabel('OBUISC_FilterBuilder.subClauseButtonPrompt');
isc.Button.title = OB.I18N.getLabel('OBUISC_Button.title');
isc.DateChooser.todayButtonTitle = OB.I18N.getLabel('OBUISC_DateChooser.todayButtonTitle');
isc.DateChooser.cancelButtonTitle = OB.I18N.getLabel('OBUISC_DateChooser.cancelButtonTitle');
isc.DynamicForm.errorsPreamble = OB.I18N.getLabel('OBUISC_DynamicForm.errorsPreamble');
isc.DynamicForm.unknownErrorMessage = OB.I18N.getLabel('OBUISC_DynamicForm.unknownErrorMessage');
// the following two do not seem to exist
// isc.SelectOtherItem.otherTitle = OB.I18N.getLabel('OBUISC_SelectOtherItem.otherTitle');
// isc.SelectOtherItem.selectOtherPrompt = OB.I18N.getLabel('OBUISC_SelectOtherItem.selectOtherPrompt');
isc.DateItem.invalidDateStringMessage = OB.I18N.getLabel('OBUISC_DateItem.invalidDateStringMessage');
isc.DateItem.pickerIconPrompt = OB.I18N.getLabel('OBUISC_');
isc.ValuesManager.unknownErrorMessage = OB.I18N.getLabel('OBUISC_ValuesManager.unknownErrorMessage');
isc.DataBoundComponent.addFormulaFieldText = OB.I18N.getLabel('OBUISC_DataBoundComponent.addFormulaFieldText');
isc.DataBoundComponent.editFormulaFieldText = OB.I18N.getLabel('OBUISC_DataBoundComponent.editFormulaFieldText');
isc.DataBoundComponent.addSummaryFieldText = OB.I18N.getLabel('OBUISC_DataBoundComponent.addSummaryFieldText');
isc.DataBoundComponent.editSummaryFieldText = OB.I18N.getLabel('OBUISC_DataBoundComponent.editSummaryFieldText');
isc.Selection.selectionRangeNotLoadedMessage = OB.I18N.getLabel('OBUISC_Selection.selectionRangeNotLoadedMessage');
isc.GridRenderer.emptyMessage = OB.I18N.getLabel('OBUISC_GridRenderer.emptyMessage');

isc.ListGrid.addProperties({emptyMessage: OB.I18N.getLabel('OBUISC_ListGrid.emptyMessage')});
isc.ListGrid.addProperties({loadingDataMessage: OB.I18N.getLabel('OBUISC_ListGrid.loadingDataMessage')});
isc.ListGrid.addProperties({loadingMessage: ''}); // empty string is fine see description in smartclient reference OB.I18N.getLabel('OBUISC_ListGrid.loadingMessage')
isc.ListGrid.addProperties({removeFieldTitle: OB.I18N.getLabel('OBUISC_ListGrid.removeFieldTitle')});
isc.ListGrid.addProperties({cancelEditingConfirmationMessage: OB.I18N.getLabel('OBUISC_ListGrid.cancelEditingConfirmationMessage')});
isc.ListGrid.addProperties({confirmDiscardEditsMessage: OB.I18N.getLabel('OBUISC_ListGrid.confirmDiscardEditsMessage')});
isc.ListGrid.addProperties({discardEditsSaveButtonTitle: OB.I18N.getLabel('OBUISC_ListGrid.discardEditsSaveButtonTitle')});
isc.ListGrid.addProperties({freezeOnRightText: OB.I18N.getLabel('OBUISC_ListGrid.freezeOnRightText')});
isc.ListGrid.addProperties({freezeOnLeftText: OB.I18N.getLabel('OBUISC_ListGrid.freezeOnLeftText')});
isc.ListGrid.addProperties({sortFieldAscendingText: OB.I18N.getLabel('OBUISC_ListGrid.sortFieldAscendingText')});
isc.ListGrid.addProperties({sortFieldDescendingText: OB.I18N.getLabel('OBUISC_ListGrid.sortFieldDescendingText')});
isc.ListGrid.addProperties({fieldVisibilitySubmenuTitle: OB.I18N.getLabel('OBUISC_ListGrid.fieldVisibilitySubmenuTitle')});
isc.ListGrid.addProperties({freezeFieldText: OB.I18N.getLabel('OBUISC_ListGrid.freezeFieldText')});
isc.ListGrid.addProperties({unfreezeFieldText: OB.I18N.getLabel('OBUISC_ListGrid.unfreezeFieldText')});
isc.ListGrid.addProperties({groupByText: OB.I18N.getLabel('OBUISC_ListGrid.groupByText')});
isc.ListGrid.addProperties({ungroupText: OB.I18N.getLabel('OBUISC_ListGrid.ungroupText')});
isc.ListGrid.addProperties({fieldVisibilitySubmenuTitle: OB.I18N.getLabel('OBUISC_ListGrid.fieldVisibilitySubmenuTitle')});
isc.ListGrid.addProperties({clearSortFieldText: OB.I18N.getLabel('OBUISC_ListGrid.clearSortFieldText')});

isc.TreeGrid.parentAlreadyContainsChildMessage = OB.I18N.getLabel('OBUISC_TreeGrid.parentAlreadyContainsChildMessage');
isc.TreeGrid.cantDragIntoSelfMessage = OB.I18N.getLabel('OBUISC_TreeGrid.cantDragIntoSelfMessage');
isc.TreeGrid.cantDragIntoChildMessage = OB.I18N.getLabel('OBUISC_TreeGrid.cantDragIntoChildMessage');
isc.MenuButton.title = OB.I18N.getLabel('OBUISC_MenuButton.title');
isc.FormulaBuilder.autoHideCheckBoxLabel = OB.I18N.getLabel('OBUISC_FormulaBuilder.autoHideCheckBoxLabel');
isc.FormulaBuilder.helpTextIntro = OB.I18N.getLabel('OBUISC_FormulaBuilder.helpTextIntro');
isc.FormulaBuilder.instructionsTextStart = OB.I18N.getLabel('OBUISC_FormulaBuilder.instructionsTextStart');
isc.FormulaBuilder.samplePrompt = OB.I18N.getLabel('OBUISC_FormulaBuilder.samplePrompt');
isc.SummaryBuilder.autoHideCheckBoxLabel = OB.I18N.getLabel('OBUISC_SummaryBuilder.autoHideCheckBoxLabel');
isc.SummaryBuilder.helpTextIntro = OB.I18N.getLabel('OBUISC_SummaryBuilder.helpTextIntro');

//isc.Calendar is not loaded as a default
//isc.Calendar.invalidDateMessage = OB.I18N.getLabel('OBUISC_Calendar.invalidDateMessage');
//isc.Calendar.dayViewTitle = OB.I18N.getLabel('OBUISC_Calendar.dayViewTitle');
//isc.Calendar.weekViewTitle = OB.I18N.getLabel('OBUISC_Calendar.weekViewTitle');
//isc.Calendar.monthViewTitle = OB.I18N.getLabel('OBUISC_Calendar.monthViewTitle');
//isc.Calendar.timelineViewTitle = OB.I18N.getLabel('OBUISC_Calendar.timelineViewTitle');
//isc.Calendar.eventNameFieldTitle = OB.I18N.getLabel('OBUISC_Calendar.eventNameFieldTitle');
//isc.Calendar.saveButtonTitle = OB.I18N.getLabel('OBUISC_Calendar.saveButtonTitle');
//isc.Calendar.detailsButtonTitle = OB.I18N.getLabel('OBUISC_Calendar.detailsButtonTitle');
//isc.Calendar.cancelButtonTitle = OB.I18N.getLabel('OBUISC_Calendar.cancelButtonTitle');
//isc.Calendar.previousButtonHoverText = OB.I18N.getLabel('OBUISC_Calendar.previousButtonHoverText');
//isc.Calendar.nextButtonHoverText = OB.I18N.getLabel('OBUISC_Calendar.nextButtonHoverText');
//isc.Calendar.addEventButtonHoverText = OB.I18N.getLabel('OBUISC_Calendar.addEventButtonHoverText');
//isc.Calendar.datePickerHoverText = OB.I18N.getLabel('OBUISC_Calendar.datePickerHoverText');