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

// Note: currently this class covers both the top-level view as well as child views
// in child tabs. If there is a lot of separate code then maybe the 2 types of usages
// have to be factored out in separate classes.

isc.ClassFactory.defineClass("OBStandardView", isc.VLayout);

// = OBStandardView =
// The OBStandardView is the composite canvas which contains a form/grid and
// child tabs.

isc.OBStandardView.addProperties({
  // properties used by the ViewManager, only relevant in case this is the top
  // view shown directly in the main tab
  showsItself : false,
  tabTitle: null,
  
  getBookMarkParams: function() {
    var result = {};
    return result;
  },
  
  isEqualParams: function(params) {
    return false;
  },
  
  isSameTab : function(viewName, params) {
    return false;
  },

  // returns the view information for the help view.
  getHelpView: function() {
    return null;
  },
  
// ** {{{ windowId }}} **
// The id of the window shown here, only set for the top view in the hierarchy
// and if this is a window/tab view.
  windowId: null,
 
// ** {{{ tabId }}} **
// The id of the tab shown here, set in case of a window/tab view.
  tabId: null,
   
// ** {{{ processId }}} **
// The id of the process shown here, set in case of a process view.
  processId: null,
 
// ** {{{ formId }}} **
// The id of the form shown here, set in case of a form view.
  formId: null,
  
// ** {{{ parentView }}} **
// The parentView if this view is a child in a parent child structure.
  parentView: null,
 
// ** {{{ tabSet }}} **
// The tabSet which shows this view. If the parentView is null then this is the
// top tabSet.
  tabSet: null,
  tab: null,
  
// ** {{{ toolbar }}} **
// The toolbar canvas.
  toolbar: null,
  
// ** {{{ formGridLayout }}} **
// The layout which holds the form and grid.
  formGridLayout: null,
  
// ** {{{ childTabSet }}} **
// The tabSet holding the child tabs with the OBView instances.
  childTabSet: null,
  
// ** {{{ hasChildTabs }}} **
// Is set to true if there are child tabs.
  hasChildTabs: false,
    
// ** {{{ dataSource }}} **
// The dataSource used to fill the data in the grid/form.
  dataSource: null,
  
  width: "100%",
  height: "100%",
  margin: 0,
  padding: 0,
  overflow: "auto",

  initWidget: function() {
    this.createMainParts();
    this.createViewStructure();
    this.Super("initWidget", arguments);
  },
  
  setDataSource: function(ds) {
    this.dataSource = ds;
  },
  
// ** {{{ createViewStructure }}} **
// Is to be overridden, is called in initWidget.
  createViewStructure: function() {
  },
  
// ** {{{ createMainParts }}} **
// Creates the main layout components of this view.
  createMainParts: function() {
    if (this.tabId.length > 0) {
      this.formGridLayout = isc.HLayout.create({
        width: "100%",
        height: "50%",
        overflow: "auto",
        showResizeBar: true,
        members: [isc.Label.create({contents: this.tabId})]
      });
//      this.formGridLayout.addMember(isc.Label.create({title: 'test'}));
      this.addMember(this.formGridLayout);
    }
    if (this.hasChildTabs) {
      this.childTabSet = isc.TabSet.create({
        tabBarPosition: "top",
        width: "100%",
        height: "50%",
        overflow: "auto",

        // get rid of the margin around the content of a pane
        paneMargin: 0,
        paneContainerMargin: 0,
        paneContainerPadding: 0,
        showPaneContainerEdges: false,        
        useSimpleTabs: true
      });
      this.addMember(this.childTabSet);
    }    
  },
  
// ** {{{ addChildView }}} **
// The addChildView creates the child tab and sets the pointer back to this
// parent.
  addChildView: function(childView) {
      childView.parentView = this;
      
      var childTabDef = {
          title : childView.tabTitle,
          pane : childView
        };
      
      childView.tab = childTabDef;
      childView.tabSet = this.tabSet;
      this.childTabSet.addTab(childTabDef);
  }
});

