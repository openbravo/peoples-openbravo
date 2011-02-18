/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
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

// = Classic OB Window =
//
// Implements the view which shows a classic OB window in a Smartclient HTMLFlow component. The 
// classic OB window is shown in a tab in the multi-tab interface 
//
isc.defineClass("ClassicOBWindow", isc.HTMLPane).addProperties( {
  showsItself : false,
  contentsType : 'page',
  windowId : '',
  tabId : '',
  recordId : '',
  // tab title is the title in the MDI tab, it is set in the view-manager
  // and updated below
  tabTitle: null,
  processId: null,
  formId: null,
  mappingName: null,
  command : 'DEFAULT',
  showEdges : false,
  styleName : 'classicOBWindow',
  appURL : OB.Application.contextUrl + 'security/Menu.html',
  obManualURL : '',
  padding : 0,
  margin : 0,
  height : "100%",
  width : "100%",
  // ignore the tab info update for one time, to prevent double history entries
  ignoreTabInfoUpdate: true,
  hasBeenDrawnOnce: false,
  loadingMessage : OB.I18N.getLabel('OBUIAPP_Loading')
});

isc.ClassicOBWindow.addMethods( {
  
  updateTabInformation: function (windowId, tabId, recordId, command, obManualURL, title) {
    // ignore the first time
    if (this.ignoreTabInfoUpdate) {
      this.ignoreTabInfoUpdate = false;
      return;
    }
    
    if (windowId) {
      this.windowId = windowId;
    }
    
    if (tabId) {
      this.tabId = tabId;
    }    
    
    if (recordId) {
      this.recordId = recordId;
    }    
    
    if (command) {
      this.command = command.toUpperCase();
    }    
    
    if (obManualURL) {
      this.obManualURL = obManualURL;
    }
    
    if (title) {
      this.tabTitle = title;
    }
    
    OB.Layout.HistoryManager.updateHistory();
  },

  getBookMarkParams: function() {
    var result = {};
    if (this.recordId) {
      result.recordId = this.recordId;
    }
    if (this.windowId) {
      result.windowId = this.windowId;
    }
    if (this.obManualURL) {
      result.obManualURL = this.obManualURL;
    }
    if (this.command) {
      result.command = this.command;
    }
    if (this.tabId) {
      result.tabId = this.tabId;
    }
    if (this.processId) {
      result.processId = this.processId;
    }
    if (this.formId) {
      result.formId = this.formId;
    }
    if (this.keyParameter) {
      result.keyParameter = this.keyParameter;
    }
    if (this.mappingName) {
      result.mappingName = this.mappingName;
    }
    return result;
  },
  
  initWidget : function(args) {
    var urlCharacter = '?';
    if (this.appURL.indexOf('?') !== -1) {
      urlCharacter = '&';
    }
    if (this.keyParameter) {
      this.contentsURL = this.appURL + urlCharacter + 'url=' + this.mappingName
          + '&' + this.keyParameter + "=" + this.recordId
          + '&noprefs=true&Command=DIRECT&hideMenu=true';
    } else if (this.obManualURL && this.obManualURL !== '') {
      this.contentsURL = this.appURL + urlCharacter + 'url=' + this.obManualURL
          + '&noprefs=true&Command=' + this.command + '&hideMenu=true';
    } else {
      this.contentsURL = this.appURL + urlCharacter + 'Command=' + this.command
          + '&noprefs=true';
      if (this.recordId !== '') {
        this.contentsURL = this.contentsURL + '&windowId=' + this.windowId;
      }
      this.contentsURL = this.contentsURL + '&tabId=' + this.tabId;
      if (this.recordId !== '') {
        this.contentsURL = this.contentsURL + '&recordId=' + this.recordId;
      }
      this.contentsURL = this.contentsURL + '&hideMenu=true';
    }
    
    this.Super('initWidget', args);
  },

  isEqualParams: function(params) {
    if (params && (this.recordId || params.recordId) && params.recordId !== this.recordId) {
      return false;
    }

    if (params && (this.command || params.command) && params.command !== this.command) {
      return false;
    }

    if (params && (this.tabId || params.tabId) && params.tabId !== this.tabId) {
      return false;
    }

    if (params && (this.formId || params.formId) && params.formId !== this.formId) {
      return false;
    }

    if (params && (this.windowId || params.windowId) && params.windowId !== this.windowId) {
      return false;
    }

    if (params && (this.processId || params.processId) && params.processId !== this.processId) {
      return false;
    }
    
    return true;
  },
  
  isSameTab : function(viewName, params) {
    if (viewName !== 'ClassicOBWindow') {
      return false;
    }
    if (params && (params.obManualURL || this.obManualURL) && params.obManualURL === this.obManualURL) {
      return true;
    }
    
    if (params && (this.windowId || params.windowId) && params.windowId === this.windowId) {
      return true;
    }
    
    if (params && (this.processId || params.processId) && params.processId === this.processId) {
      return true;
    }
    
    if (params && (this.formId || params.formId) && params.formId === this.formId) {
      return true;
    }

    if ((!params || params.tabId === '') && this.tabId === '') {
      return true;
    }
    
    return params.tabId === this.tabId;
  },
  
  // returns the view information for the help view.
  getHelpView: function() {
    if (this.windowId) {
      // tabTitle is set in the viewManager
      return {viewId: 'ClassicOBHelp', tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'), windowId: this.windowId, windowType: "W", windowName: this.tabTitle}
    }
    if (this.processId) {
      return {viewId: 'ClassicOBHelp', windowId: null, tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'), windowType: "R", windowName: this.processId};
    }
    if (this.formId) {
      return {viewId: 'ClassicOBHelp', windowId: null, tabTitle: this.tabTitle + ' - ' + OB.I18N.getLabel('UINAVBA_Help'), windowType: "X", windowName: this.formId};
    }
    return null;
  }
  
});
