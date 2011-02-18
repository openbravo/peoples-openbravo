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

// = ViewManager =
//
// The ViewManager handles the interaction between the Toolbar Components
// and the {{{Main View}}}
//
(function(OB, isc) {

  if (!OB || !isc) {
    throw {
      name : "ReferenceError",
      message : "openbravo and isc objects are required"
    };
  }

  // cache object references locally
  var L = OB.Layout, M = OB.MainView, ISC = isc, vmgr; // Local reference to
                                                        // ViewManager instance

  function ViewManager() {
  }

  ViewManager.prototype = {

    // if true then certain functions are disab
    inStateHandling: false,
      
    // ** {{ ViewManager.views }} **
    // Collection of opened views
    views : {
      cache : [],

      getViewTabID : function(/* String */vName, /* Object */params) {
        var len = this.cache.length, i, item;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.instance.isSameTab && item.instance.isSameTab(vName, params)) {
            return item.tabID;
          }
        }
        return null;
      },

      getTabNumberFromViewParam : function(/* String */param, value) {
        var tabSet = M.TabSet, numberOfTabs = tabSet.tabs.length, viewParam = "", result = null;
        for ( var i = 0; i < numberOfTabs; i++) {
          viewParam = tabSet.getTabPane(i)[param];
          if (viewParam === value) {
            result = i;
          }
        }
        return result;
      },

      push : function(/* Object */instanceDetails) {
        this.cache.push(instanceDetails);
      },

      removeTab : function(/* String */tabID) {
        var len = this.cache.length, i, item, removed;
        for (i = len; i > 0; i--) {
          item = this.cache[i - 1];
          if (item.tabID === tabID) {
            removed = this.cache.splice(i - 1, 1);
            return;
          }
        }
      }
    },

    // ** {{{ ViewManager.openView(viewName, params) }}} **
    //
    // Shows a new tab in the {{{ Main Layout }}}
    //
    // Parameters:
    // * {{{viewName}}} is a String of the view implementation, e.g. {{{
    // ClassicOBWindow }}}
    // * {{{params}}} is an Object with the parameters passed to the
    // implementation to initialize an instance, e.g. {{{ {tabId: "100"} }}}
    // * {{{state}}} is an Object which can contain more complex state
    // information
    // to initialize an instance.
    //
    openView : function(/* String */viewName, /* Object */params, /* Object */ state) {

      //
      // Returns the function implementation of a View
      //
      function getView(/* String */viewName, /* Object */params, /* Object */ state) {

        if (!viewName) {
          throw {
            name : "ParameterRequired",
            message : "View implementation name is required"
          };
        }
        
        //
        // Shows a view in a tab in the {{{ TabSet }}} or external
        //
        function showTab(/* String */viewName, /* Object */params, /* Object */ state) {

          var viewTabID = vmgr.views.getViewTabID(viewName, params), tabset = M.TabSet, tabTitle;

          // always create a new instance anyway as parameters
          // may have changed
          var viewInstance = ISC[viewName].create(params);
          
          if (state && viewInstance.setViewState) {
            viewInstance.setViewState(state);
          }
          
          // eventhough there is already an open tab
          // still refresh it
          if (viewTabID !== null) {
            // refresh the view
            tabset.updateTab(viewTabID, viewInstance);
            // and show it
            tabset.selectTab(viewTabID);

            // update the cache
            vmgr.views.removeTab(viewTabID, false);
            vmgr.views.push( {
              viewName : viewName,
              params : params,
              instance : viewInstance,
              tabID : viewTabID
            });
            // note after this the viewTabID is not anymore viewInstance.ID +
            // "_tab"
            // but this is not a problem, it should be unique that's the most
            // important part
            return;
          }

          // is not shown in a tab, let it show itself in a different way
          if (viewInstance && viewInstance.show && viewInstance.showsItself) {
            viewInstance.show();
            return;
          }

          // Creating an instance of the view implementation
          viewTabID = viewInstance.ID + "_tab";

          if (viewInstance) {

            tabTitle = params.tabTitle || viewInstance.tabTitle || params.tabId
                || viewName;

            var tabDef = {
                ID : viewTabID,
                title : tabTitle,
                canClose : true,
                viewName: viewName,
                params: params,
                pane : viewInstance
              };
      
            // let the params override tab properties like canClose
            tabDef = isc.addProperties(tabDef, params);
            
            // Adding to the MainView tabSet
            tabset.addTab(tabDef);

            // Adding a reference to opened views collection
            vmgr.views.push( {
              viewName : viewName,
              params : params,
              instance : viewInstance,
              tabID : viewTabID
            });

            // the select tab event will update the history
            tabset.selectTab(viewTabID);
          }
        }

        //
        // Function used by the {{ ISC.RPCManager }} after receiving the view
        // implementation from the back-end
        //          
        function fetchViewCallback(/* Object */response) {
          if (!ISC[viewName]) {
            throw {
              name : "ReferenceError",
              message : "The view " + viewName + " not defined"
            };
          }
          showTab(viewName, params);
        }

        //
        // Fetch the view implementation from the server. After fetching
        // the implementation, stores a reference in the cache
        //
        function fetchView(/* String */viewName) {

          var rpcMgr = ISC.RPCManager;
          var reqObj = {
            params : {
              name : viewName
            },
            callback : fetchViewCallback,
            evalResult : true,
            httpMethod : "GET",
            useSimpleHttp : true,
            actionURL : '../../../org.openbravo.client.kernel/OBUIAPP_MainLayout/View'
          };
          var request = rpcMgr.sendRequest(reqObj);
        }

        if (isc[viewName]) {
          showTab(viewName, params);
        } else {
          fetchView(viewName);
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

    restoreState: function (/* Object */ newState, /* Object */ data) {
      
      var tabSet = M.TabSet, tabsLength, i, tabObject, tabParams, hasChanged = false, stateData;

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
      
      isc.Log.logDebug("Changed " + hasChanged, "OB");
      
      // changes occured, start from scratch again, recreating each view
      if (hasChanged) {
        tabSet.removeTabs(tabSet.tabs);

        vmgr.views.cache = [];
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
      }
      
      tabSet.selectTab(newState.st);
      
      vmgr.inStateHandling = false;
    },
    
    createAddStartTab: function() {
      var startTabUrl = OB.PropertyStore.get('OBUIAPP_UIStartTabURL');
      if (!startTabUrl) {
          return;
      }
            
      // the myOB field is used to keep track that the params are for the singleton my ob tab
      OB.Layout.ViewManager.openView("OBMyOpenbravo", {myOB: true, canClose: false}, null);
    }
  };

  // Initialize ViewManager object
  vmgr = L.ViewManager = new ViewManager();
})(OB, isc);