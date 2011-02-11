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
// = ViewManager =
//
// The ViewManager manages the views displayed in the tabs of the main layout.
// It is called to restore a previous state which is maintained by the History 
// manager. View types which are not yet defined on the client are loaded
// from the server. 
//
(function(OB, isc){

  if (!OB || !isc) {
    throw {
      name: 'ReferenceError',
      message: 'openbravo and isc objects are required'
    };
  }
  
  // cache object references locally
  var L = OB.Layout, M = OB.MainView, ISC = isc, vmgr; // Local reference to
  // ViewManager instance
  var tabSet = M.TabSet;
  
  function ViewManager(){
  }
  
  ViewManager.prototype = {
  
    // if true then certain functions are disab
    inStateHandling: false,
    
    // ** {{ ViewManager.views }} **
    // Collection of opened views
    views: {
      cache: [],
      
      getViewTabID: function(/* String */vName, /* Object */ params){
        var len = this.cache.length, i, item;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.instance.isSameTab && item.instance.isSameTab(vName, params)) {
            return item.viewTabId;
          }
        }
        return null;
      },
      
      getTabNumberFromViewParam: function(/* String */param, value){
        var numberOfTabs = tabSet.tabs.length, viewParam = '', result = null;
        for (var i = 0; i < numberOfTabs; i++) {
          viewParam = tabSet.getTabPane(i)[param];
          if (viewParam === value) {
            result = i;
          }
        }
        return result;
      },
      
      push: function(/* Object */instanceDetails){
        this.cache.push(instanceDetails);
      },
      
      removeTab: function(/* String */viewTabId){
        var len = this.cache.length, i, item, removed;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.viewTabId === viewTabId) {
            removed = this.cache.splice(i - 1, 1);
            return;
          }
        }
      }
    },

    findLoadingTab: function(params) {
      if (!params.loadingTabId) {
        return null;
      }
      for (var i = 0; i < tabSet.tabs.length; i++) {
        var pane = tabSet.tabs[i].pane;
        if (pane.viewTabId && pane.viewTabId === params.loadingTabId) {
          return tabSet.tabs[i];
        }
      }
      return null;
    },
    
    fetchView: function(/* String */viewId, /*function*/ callback, /*Object*/ clientContext, /*Object*/params, useLoadingTab){
      if (useLoadingTab) {
        // open a loading tab
        params = params || {};
        var layout = OB.Utilities.createLoadingLayout();
        // is used to prevent history updating
        layout.isLoadingTab = true;
        var viewTabId = '_' + new Date().getTime();
        params.loadingTabId = viewTabId;
        this.createTab(viewId, viewTabId, layout, params);
      }
      
      var rpcMgr = ISC.RPCManager;
      var reqObj = {
        params: {
          viewId: viewId
        },
        callback: callback,
        clientContext: clientContext,
        evalResult: true,
        httpMethod: 'GET',
        useSimpleHttp: true,
        actionURL: OB.Application.contextUrl + 'org.openbravo.client.kernel/OBUIAPP_MainLayout/View'
      };
      var request = rpcMgr.sendRequest(reqObj);
    },
    
    createTab: function(viewName, viewTabId, viewInstance, params) {
      if (params.i18nTabTitle) {
        // note call to I18N is done below after the tab
        // has been created
        tabTitle = '';
      } else {
        tabTitle = params.tabTitle || viewInstance.tabTitle || params.tabId ||
        viewName;
      }
      
      var tabDef = {
        ID: viewTabId,
        title: tabTitle,
        canClose: true,
        viewName: viewName,
        params: params,
        pane: viewInstance
      };
      
      // let the params override tab properties like canClose
      tabDef = isc.addProperties(tabDef, params);
      
      // Adding to the MainView tabSet
      tabSet.addTab(tabDef);
      
      if (params.i18nTabTitle) {
        tabTitle = '';
        // note the callback calls the tabSet
        // with the tabid to set the label
        OB.I18N.getLabel(params.i18nTabTitle, null, {
          setTitle: function(label){
            tabSet.setTabTitle(viewTabId, label);
          }
        }, 'setTitle');
      }
      
      // tell the viewinstance what tab it is on
      // note do not use tabId on the viewInstance
      // as tabId is used by the classic ob window
      // local variable is: viewTabId
      if (viewInstance.setViewTabId) {
        viewInstance.setViewTabId(viewTabId);
      } else {
        viewInstance.viewTabId = viewTabId;
      }
      
      // Adding a reference to opened views collection
      vmgr.views.push({
        viewName: viewName,
        params: params,
        instance: viewInstance,
        viewTabId: viewTabId
      });
      
      // the select tab event will update the history
      tabSet.selectTab(viewTabId);
    },
  
    // ** {{{ ViewManager.openView(viewName, params) }}} **
    //
    // Shows a new tab in the {{{ Main Layout }}}
    //
    // Parameters:
    // * {{{viewName}}} is a String of the view implementation, e.g. {{{
    // OBClassicWindow }}}
    // * {{{params}}} is an Object with the parameters passed to the
    // implementation to initialize an instance, e.g. {{{ {tabId: '100'} }}}
    // * {{{state}}} is an Object which can contain more complex state
    // information
    // to initialize an instance.
    //
    openView: function(/* String */viewName, /* Object */ params, /* Object */ state){
    
      params = params || {};
      
      //
      // Returns the function implementation of a View
      //
      function getView(/* String */viewName, /* Object */ params, /* Object */ state){
      
        if (!viewName) {
          throw {
            name: 'ParameterRequired',
            message: 'View implementation name is required'
          };
        }
        
        //
        // Shows a view in a tab in the {{{ TabSet }}} or external
        //
        function showTab(/* String */viewName, /* Object */ params, /* Object */ state){
        
          var viewTabId, tabTitle, loadingTab = vmgr.findLoadingTab(params);
          
          if (loadingTab) {
            viewTabId = loadingTab.pane.viewTabId;
          } else {
            viewTabId = vmgr.views.getViewTabID(viewName, params);
          }
          
          // always create a new instance anyway as parameters
          // may have changed
          var viewInstance = ISC[viewName].create(params);
          
          if (state && viewInstance.setViewState) {
            viewInstance.setViewState(state);
          }
          
          // is not shown in a tab, let it show itself in a different way
          // but first get rid of the loading tab
          if (viewInstance && viewInstance.show && viewInstance.showsItself) {
            if (loadingTab) {
              delete params.loadingTabId;
              tabSet.removeTab(loadingTab.viewTabId);
            }
            viewInstance.show();
            return;
          }

          // eventhough there is already an open tab
          // still refresh it
          if (viewTabId !== null) {
            // refresh the view
            tabSet.updateTab(viewTabId, viewInstance);
            // and show it
            tabSet.selectTab(viewTabId);
            
            // tell the viewinstance what tab it is on
            // note do not use tabId on the viewInstance
            // as tabId is used by the classic ob window
            // local variable is: viewTabId (with uppercase ID)
            // function call and other variable uses camelcase Id
            if (viewInstance.setViewTabId) {
              viewInstance.setViewTabId(viewTabId);
            } else {
              viewInstance.viewTabId = viewTabId;
            }

            // update the cache
            vmgr.views.removeTab(viewTabId, false);
            vmgr.views.push({
              viewName: viewName,
              params: params,
              instance: viewInstance,
              viewTabId: viewTabId
            });
            // note after this the viewTabId is not anymore viewInstance.ID +
            // '_tab'
            // but this is not a problem, it should be unique that's the most
            // important part
            
            // the select tab event will update the history
            if (tabSet.getSelectedTab() && tabSet.getSelectedTab().pane.viewTabId === viewTabId) {
              OB.Layout.HistoryManager.updateHistory();
            } else {              
              tabSet.selectTab(viewTabId);
            }

            return;
          }
          
          // Creating an instance of the view implementation
          viewTabId = viewInstance.ID + '_tab';
          
          if (viewInstance) {
            vmgr.createTab(viewName, viewTabId, viewInstance, params);
          }
        }
        
        //
        // Function used by the {{ ISC.RPCManager }} after receiving the view
        // implementation from the back-end
        //          
        function fetchViewCallback(/* Object */response){
          // if the window is in development it's name is always unique
          // and has changed
          if (vmgr.loadedWindowClassName) {
            viewName = vmgr.loadedWindowClassName;
          }
          if (!ISC[viewName]) {
            throw {
              name: 'ReferenceError',
              message: 'The view ' + viewName + ' not defined'
            };
          }
          showTab(viewName, params);
        }
                
        if (isc[viewName]) {
          showTab(viewName, params);
        } else {         
           vmgr.fetchView(viewName, fetchViewCallback, null, params, true);
        }
      }
      getView(viewName, params, state);
    },
    
    // ** {{{ ViewManager.restoreState(state, data) }}} **
    //
    // Restores the state of the main layout using the passed in state object.
    // This state object contains view id's and book marked parameters.
    // The data object contains extra (more complex) state information which 
    // can not be bookmarked.
    //
    
    restoreState: function(/* Object */newState, /* Object */ data){
    
      var tabSet = M.TabSet, tabsLength, i, tabObject, hasChanged = false, stateData;
      
      if (vmgr.inStateHandling) {
        return;
      }
      
      vmgr.inStateHandling = true;
      
      // create an empty layout
      if (!newState) {
        tabSet.removeTabs(tabSet.tabs);
        vmgr.views.cache = [];
        vmgr.inStateHandling = false;
        return;
      }
      
      // do some comparison
      tabsLength = newState.bm.length;
      hasChanged = tabSet.tabs.length !== tabsLength;
      // same length, compare individual tabs
      if (!hasChanged) {
        for (i = 0; i < tabsLength; i++) {
          tabObject = tabSet.getTabObject(i);
          
          // changed if the view id is different
          if (newState.bm[i].viewId !== tabObject.viewName) {
            hasChanged = true;
          } else if (tabObject.pane.isEqualParams) {
            // or if the bookmark params are not the same
            hasChanged = hasChanged || !tabObject.pane.isEqualParams(newState.bm[i].params);
          }
        }
      }
      
      isc.Log.logDebug('Changed ' + hasChanged, 'OB');
      
      // changes occured, start from scratch again, recreating each view
      if (hasChanged) {
        // stop if tabSet removed failed because a tab has incorrect data
        if (!tabSet.removeTabs(tabSet.tabs)) {
          vmgr.inStateHandling = false;
          return;
        }
        
        vmgr.views.cache = [];
        
        // handles the case that not all views are there
        // view implementations are requested async resulting
        // in a wrong tab order, therefore only get the views
        // in the correct order, continuing when a new view 
        // arrives
        // see here:
        // https://issues.openbravo.com/view.php?id=15146
        requestViewsRestoreState = function (rpcResponse){
          var clientContext = rpcResponse.clientContext;
          var currentIndex = clientContext.currentIndex;
          var data = clientContext.data;
          var newState = clientContext.newState;
          var tabsLength = clientContext.tabsLength;
          var i, viewId;
          
          if (currentIndex < tabsLength) {
            for (i = currentIndex; i < tabsLength; i++) {
            
              // ignore the first tab, or the tabs opened without view id
              if (!newState.bm[i].viewId) {
                continue;
              }
              // not defined get the view!
              if (!isc[newState.bm[i].viewId]) {
                viewId = newState.bm[i].viewId;
                clientContext.currentIndex = i + 1;
                vmgr.fetchView(viewId, requestViewsRestoreState, clientContext, newState.bm[i].params);
                return;
              }
            }
          }
          // everything is here, open the views
          for (i = 0; i < tabsLength; i++) {
          
            if (data && data[i]) {
              stateData = data[i];
            }
            
            // ignore the first tab, or the tabs opened without view id
            if (!newState.bm[i].viewId) {
              continue;
            }
            
            vmgr.openView(newState.bm[i].viewId, newState.bm[i].params, stateData);
          }
          tabSet.selectTab(newState.st);
          vmgr.inStateHandling = false;
        };
                
        for (i = 0; i < tabsLength; i++) {
        
          if (data && data[i]) {
            stateData = data[i];
          }
          
          // ignore the first tab, or the tabs opened without view id
          if (!newState.bm[i].viewId) {
            continue;
          }

          if (!isc[newState.bm[i].viewId]) {
            var clientContext = {};
            
            viewId = newState.bm[i].viewId;
            clientContext.currentIndex = i + 1;
            clientContext.data = data;
            clientContext.newState = newState;
            clientContext.tabsLength = tabsLength;
            vmgr.fetchView(viewId, requestViewsRestoreState, clientContext, newState.bm[i].params);
            return;
          }
          
          vmgr.openView(newState.bm[i].viewId, newState.bm[i].params, stateData);
        }
      }
      
      tabSet.selectTab(newState.st);
      
      vmgr.inStateHandling = false;
    },
    
    createAddStartTab: function(){
      var error, historyId =  isc.History.getCurrentHistoryId();
      if (historyId) {
        try {
          OB.Layout.HistoryManager.restoreHistory(historyId, isc.History.getHistoryData(historyId));
          return;
        } catch (exception) {
          // ignore all errors
        }
      }

      // todo: this call in a way assumes that there is a myob module installed
      // it is nicer to somehow set the page to load in a different way
      // this can be done if an smartclient problem has been solved
      // see this forum post:
      // http://forums.smartclient.com/showthread.php?p=53077
      var viewId = 'OBMyOpenbravoImplementation';
      var viewParams = {
        tabTitle: OB.I18N.getLabel('OBUIAPP_MyOpenbravo'),
        myOB: true,
        canClose: false
      };
      // check if there is already a start page, only open it if not
      var viewTabId = this.views.getViewTabID(viewId, viewParams);
      if (!viewTabId) {
        this.openView(viewId, viewParams, null);
      }
    }
  };
  
  // Initialize ViewManager object
  vmgr = L.ViewManager = new ViewManager();
})(OB, isc);
