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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// ** {{{ custom }}} **
// It executes a custom action. Only for testing purposes.
// Parameters:
// * {{{func}}}: The function to be executed. It can be defined in several ways. Examples:
//    OB.Utilities.Action.execute('custom', { func: "alert('Test')" });
//    OB.Utilities.Action.execute('custom', { func: "(function (){alert('Test')})()" });
//    OB.Utilities.Action.execute('custom', { func: function() { alert('Test'); }});
//    OB.Utilities.Action.execute('custom', { func: function(paramObj) { alert(paramObj.text) }, text: 'Test' });
//    OB.Utilities.Action.executeJSON( { custom: { func: function(paramObj) { alert(paramObj.text) }, text: 'Test' } });
OB.Utilities.Action.set('custom', function (paramObj) {
  if (Object.prototype.toString.apply(paramObj.func) === '[object Function]') {
    paramObj.func(paramObj);
  } else if (Object.prototype.toString.apply(paramObj.func) === '[object String]') {
    var execFunction = new Function(paramObj.func);
    execFunction();
  }
});

// ** {{{ showMsgInView }}} **
// It shows a message in the current active view. In the end, it calls to messageBar.setMessage of the current active view
// Parameters:
// * {{{msgType}}}: The message type. It can be 'success', 'error', 'info' or 'warning'
// * {{{msgTitle}}}: The title of the message.
// * {{{msgText}}}: The text of the message.
OB.Utilities.Action.set('showMsgInView', function (paramObj) {
  var view = OB.MainView.TabSet.getSelectedTab().pane.activeView;
  if (view && view.messageBar) {
    view.messageBar.setMessage(paramObj.msgType, paramObj.msgTitle, paramObj.msgText);
  }
});

//** {{{ showMsgInProcessView }}} **
// It shows a message in the view that invoked the process.
//Parameters:
//* {{{msgType}}}: The message type. It can be 'success', 'error', 'info' or 'warning'
//* {{{msgTitle}}}: The title of the message.
//* {{{msgText}}}: The text of the message.
OB.Utilities.Action.set('showMsgInProcessView', function (paramObj) {
  var processView = paramObj._processView;
  if (processView.popup && processView.buttonOwnerView && processView.buttonOwnerView.messageBar) {
    processView.buttonOwnerView.messageBar.setMessage(paramObj.msgType, paramObj.msgTitle, paramObj.msgText);
  } else if (processView.messageBar) {
    processView.messageBar.setMessage(paramObj.msgType, paramObj.msgTitle, paramObj.msgText);
  }
});

// ** {{{ openDirectTab }}} **
// Open a view using a tab id and record id. The tab can be a child tab. If the record id
// is not set then the tab is opened in grid mode. If command is not set then default is
// used. In the end, it calls to OB.Utilities.openDirectTab
// Parameters:
// * {{{tabId}}}: The tab id of the view to be opened
// * {{{recordId}}}: The record id of the view to be opened
// * {{{command}}}: The command with which the view to be opened
// * {{{wait}}}: If true, the thread in which this action was called (if there is any) will be paused until the view be opened.
OB.Utilities.Action.set('openDirectTab', function (paramObj) {
  var processIndex;
  if (!paramObj.newTabPosition) {
    processIndex = OB.Utilities.getProcessTabBarPosition(paramObj._processView);
    if (processIndex === -1) {
      // If the process is not found in the main tab bar, add the new window in the last position
      paramObj.newTabPosition = OB.MainView.TabSet.paneContainer.members.length;
    } else {
      // If the process is foudn in the main tab bar, add the new window in its next position
      paramObj.newTabPosition = processIndex + 1;
    }
  }
  if (!paramObj.isOpening) {
    OB.Utilities.openDirectTab(paramObj.tabId, paramObj.recordId, paramObj.command, paramObj.newTabPosition, paramObj.criteria);
  }
  if ((paramObj.wait === true || paramObj.wait === 'true') && paramObj.threadId) {
    if (!OB.MainView.TabSet.getTabObject(paramObj.newTabPosition) || OB.MainView.TabSet.getTabObject(paramObj.newTabPosition).pane.isLoadingTab === true) {
      OB.Utilities.Action.pauseThread(paramObj.threadId);
      paramObj.isOpening = true;
      OB.Utilities.Action.execute('openDirectTab', paramObj, 100); //Call this action again with a 100ms delay
    } else {
      OB.Utilities.Action.resumeThread(paramObj.threadId, 1500); //Call this action again with a 1000ms delay
    }
  }
});