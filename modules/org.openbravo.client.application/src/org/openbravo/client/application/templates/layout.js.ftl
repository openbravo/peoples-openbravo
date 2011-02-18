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

/* jslint */
// make sure that the layout is loaded in the parent window if we accidentally end up
// in a child frame
try {
  if (window.parent && window.parent.OB && window.parent.OB.Layout) {
    isc.Log.logDebug("Reloading in parent frame", "OB");
    window.parent.location.href=window.location.href;
  } else if (window.parent.parent && window.parent.parent.OB && window.parent.parent.OB.Layout) {
    isc.Log.logDebug("Reloading in parent.parent frame", "OB");
    window.parent.parent.location.href=window.location.href;
  } else {
    isc.Log.logDebug("loading in own frame", "OB");
  }
} catch(e) {
    // ignoring on purpose
    isc.Log.logDebug("Error when checking parent frame: " + e.message, "OB");
}

isc.Canvas.addClassProperties({neverUsePNGWorkaround:true});

OB.KeyboardManager.KS.readRegisteredKSList('OBUIAPP_KeyboardShortcuts');

// should be moved to client.kernel component
// placed here to prevent dependencies of client.kernel on Preferences
OB.Application.startPage = '${data.startPage}';

// load the stylesheet used here
isc.Page.loadStyleSheet('[SKIN]../org.openbravo.client.application/navigation_bar_styles.css?' + OB.Application.moduleVersionParameters['9BA0836A3CD74EE4AB48753A47211BCC']);
isc.Page.loadStyleSheet('[SKIN]../org.openbravo.client.application/main_content_styles.css?' + OB.Application.moduleVersionParameters['9BA0836A3CD74EE4AB48753A47211BCC']);

isc.defineClass("OBNavBarSeparator", "Img").addProperties({
    src: '[SKINIMG]../../org.openbravo.client.application/images/navbar-separator.gif',
    height: 11,
    layoutAlign: 'center'
});

isc.defineClass("OBTabHeaderButton", "StretchImgButton").addProperties({
    width: 1,
    overflow: "visible",
    capSize: 8,
    src: '',
    baseStyle: 'mainTabBarButton',
    showSelectedIcon: true,
    showRollOverIcon: true,
    align: 'right' 
});

// the OB.Layout contains everything
OB.Layout = isc.VLayout.create({
  width: "100%",
  height: "100%",
  overflow: "auto"
});

// create the bar with navigation components
OB.Toolbar = isc.ToolStrip.create({
  width: 1,
  overflow: "visible",
  layoutLeftMargin: 2,
  separatorSize: 0,
  height: 28,
  defaultLayoutAlign: "center",
  styleName: "navBarToolStrip",
  
  addMembers: function(members) {
    // encapsulate the members
    var newMembers = [];
    for (var i = 0; i < members.length; i++) {
        // encapsulate in 2 hlayouts to handle correct mouse over/hover and show of box
        var newMember = isc.HLayout.create({layoutLeftMargin: 10, layoutRightMargin: 10, width: "100%", height: "100%", styleName: 'navBarComponent', members:[members[i]]}); 
        newMembers[i] = newMember;
    }    
    // note the array has to be placed in an array otherwise the newMembers
    // is considered to the argument list
    this.Super("addMembers", [newMembers]);
  }
});

// the TopLayout has the navigation bar on the left and the logo on the right
OB.TopLayout = isc.HLayout.create({
      width: "100%",
      height: "1",
      overflow: "visible",
      layoutTopMargin: 4
    });
    
// create the navbar on the left and the logo on the right    
OB.TopLayout.addMember(OB.Toolbar);
OB.TopLayout.addMember(
        isc.HLayout.create({
            width: "100%",
            align: "right",
            layoutRightMargin: 10,
            membersMargin: 10,
            defaultLayoutAlign: "center",
            members: [isc.Img.create({
                imageType: "normal",
                width: 122,
                height: 34,
                src: OB.Application.contextUrl + "utility/ShowImageLogo?logo=yourcompanymenu"
            }), isc.Img.create({
                imageType: "normal",
                src: OB.Application.contextUrl + "utility/GetOpenbravoLogo.png",
                getInnerHTML: function() {
                    var html = this.Super("getInnerHTML", arguments);
                    <#if data.addProfessionalLink>
                    return '<a href="http://www.openbravo.com/product/erp/professional/" target="_new">' + html + '</a>';
                    <#else>
                    return html;
                    </#if>
                }
            })]
        })      
);

// add the top part to the main layout
OB.Layout.addMember(OB.TopLayout);

// create some vertical space
OB.Layout.addMember(isc.LayoutSpacer.create({height: 10}));

OB.MainView = isc.VLayout.create({
  width: "100%",
  height: "100%"
});
OB.Layout.addMember(OB.MainView);

OB.MainView.TabSet = isc.TabSet.create({
  tabBarPosition: "top",
  width: "100%",
  height: "100%",
  
  destroyPanes: false,
  
  paneContainerClassName: "mainTabPaneContainer",
  
  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,
  
  useSimpleTabs: true,
  styleName: "mainTab",
  simpleTabBaseStyle: "mainTab",
  
  stateAsString: null,
  
  closeTabIcon: '[SKINIMG]../../org.openbravo.client.application/images/ico-close-tab.png',
  closeTabIconSize: 18,
    
  // note see the smartclient autochild concept for why tabBarProperties is valid
  tabBarProperties: {height: 30, styleName: "mainTabBar", baseStyle: 'mainTab', buttonConstructor: isc.OBTabHeaderButton},
  tabProperties: {styleName: "mainTabBar", baseStyle: 'mainTab', margin: 0, padding: 0},
  
  tabSelected: function(tabNum, tabPane, ID, tab) {
    OB.Layout.HistoryManager.updateHistory();
  },

  draw : function() {
    var me = this;
    var ksAction_ActivateTab = function(tab) {
      me.selectTab(tab-1);
      if(typeof me.getSelectedTab().pane.focusTab === "function") {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ActivateTab1', ksAction_ActivateTab, 1);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab2', ksAction_ActivateTab, 2);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab3', ksAction_ActivateTab, 3);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab4', ksAction_ActivateTab, 4);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab5', ksAction_ActivateTab, 5);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab6', ksAction_ActivateTab, 6);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab7', ksAction_ActivateTab, 7);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab8', ksAction_ActivateTab, 8);
    OB.KeyboardManager.KS.add('TabSet_ActivateTab9', ksAction_ActivateTab, 9);
    var ksAction_closeAllTabs = function() {
      var tabCount, tabArray = [], i;
      for (i = 1; i > 0; i++) {
        if (typeof me.getTab(i) === "undefined") {
          break;
        }
      }
      tabCount = i-1;
      me.selectTab(0);
      for (i = 1; i <= tabCount; i++) {
        tabArray.push(i);
      }
      me.removeTabs(tabArray);
    };
    OB.KeyboardManager.KS.add('TabSet_closeAllTabs', ksAction_closeAllTabs);
    var ksAction_ActivateRightTab = function() {
      me.selectTab((me.getTabNumber(me.getSelectedTab()))+1);
      if(typeof me.getSelectedTab().pane.focusTab === "function") {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ActivateRightTab', ksAction_ActivateRightTab);
    var ksAction_ActivateLeftTab = function() {
      me.selectTab((me.getTabNumber(me.getSelectedTab()))-1);
      if(typeof me.getSelectedTab().pane.focusTab === "function") {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ActivateLeftTab', ksAction_ActivateLeftTab);
    var ksAction_ReloadActive = function() {
      if(typeof me.getSelectedTab().pane.refreshTab === "function") {
        me.getSelectedTab().pane.refreshTab();
      } else {
        me.getSelectedTab().pane.markForRedraw();
      }
      if(typeof me.getSelectedTab().pane.focusTab === "function") {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ReloadActive', ksAction_ReloadActive);
    this.Super("draw", arguments);
  },
  
  removeTabs: function(tabs, destroyPanes) {
    if (!isc.isAn.Array(tabs)) { 
        tabs = [tabs];
    }

    // get the actual tab button object from whatever was passed in.
    // We can pass this to tabBar.removeTabs()
    tabs = this.map("getTab", tabs);

    var tabsLength = tabs.length, i;
    
    for (i = 0; i < tabsLength; i++) {
        OB.Layout.ViewManager.views.removeTab(tabs[i].ID);        
    }
  
    this.Super("removeTabs", arguments);
    
    OB.Layout.HistoryManager.updateHistory();
  }
});
OB.MainView.addMember(OB.MainView.TabSet);

OB.Toolbar.addMembers([
<#list data.navigationBarComponents as nbc>
${nbc.jscode}<#if nbc_has_next>,</#if>
</#list>]);