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

// should be moved to client.kernel component
// placed here to prevent dependencies of client.kernel on Preferences
OB.Application.startPage = '${data.startPage}';
OB.Application.loginPage = '${data.loginPage}';

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
    showSelectedIcon: true
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
                src: "[SKINIMG]../../org.openbravo.client.application/images/logo-openbravo.gif"
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
  
  closeTabIcon: {align: 'right', src: '[SKINIMG]../../org.openbravo.client.application/images/close-tab.gif'},
  closeTabIconSize: 18,
    
  // note see the smartclient autochild concept for why tabBarProperties is valid
  tabBarProperties: {height: 30, styleName: "mainTabBar", baseStyle: 'mainTab', buttonConstructor: isc.OBTabHeaderButton},
  tabProperties: {styleName: "mainTabBar", baseStyle: 'mainTab', margin: 0, padding: 0},
  
  tabSelected: function(tabNum, tabPane, ID, tab) {
    OB.Layout.HistoryManager.updateHistory();
  },
  
  removeTabs: function(tabs, destroyPanes) {
    if (!isc.isAn.Array(tabs)) { 
        tabs = [tabs];
    }
    
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