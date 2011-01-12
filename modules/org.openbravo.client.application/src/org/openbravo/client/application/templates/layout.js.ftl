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

// needed for backward compatibility... to open the registration form
function openRegistration() {
  OB.Utilities.openProcessPopup(OB.Application.contextUrl + '/ad_forms/Registration.html', true);
}

isc.Canvas.addClassProperties({neverUsePNGWorkaround:true});
isc.Canvas.loadingImageSrc = '[SKINIMG]../../org.openbravo.client.application/images/system/windowLoading.gif';
isc.Canvas.loadingImageSize = 70;

OB.KeyboardManager.KS.readRegisteredKSList('OBUIAPP_KeyboardShortcuts');
OB.KeyboardManager.KS.readRegisteredKSList('UINAVBA_KeyboardShortcuts');

// should be moved to client.kernel component
// placed here to prevent dependencies of client.kernel on Preferences
OB.Application.startPage = '${data.startPage}';

isc.defineClass("OBTabHeaderButton", "StretchImgButton").addProperties({
    src: '',
    showSelectedIcon: true,
    showRollOverIcon: true
}, OB.MainLayoutStylingProperties.OBTabHeaderButton);

// the OB.Layout contains everything
OB.Layout = isc.VLayout.create({
  width: '100%',
  height: '100%',
  overflow: 'auto'
});

// create the bar with navigation components
OB.Toolbar = isc.ToolStrip.create({  
  addMembers: function(members) {
    // encapsulate the members
    var newMembers = [];
    for (var i = 0; i < members.length; i++) {
        // encapsulate in 2 hlayouts to handle correct mouse over/hover and show of box
        var newMember = isc.HLayout.create({layoutLeftMargin: 10, layoutRightMargin: 10, width: '100%', height: '100%', styleName: 'OBNavBarComponent', members:[members[i]]}); 
        newMembers[i] = newMember;
    }    
    // note the array has to be placed in an array otherwise the newMembers
    // is considered to the argument list
    this.Super('addMembers', [newMembers]);
  }
}, OB.MainLayoutStylingProperties.Toolbar);

// the TopLayout has the navigation bar on the left and the logo on the right
OB.TopLayout = isc.HLayout.create({}, OB.MainLayoutStylingProperties.TopLayout);
    
// create the navbar on the left and the logo on the right
OB.TopLayout.CompanyImageLogo = isc.Img.create({
  imageType: 'normal'
}, OB.MainLayoutStylingProperties.CompanyImageLogo);
    
OB.TopLayout.addMember(OB.Toolbar);
OB.TopLayout.addMember(
        isc.HLayout.create({
            width: '100%',
            align: 'right',
            layoutRightMargin: 10,
            membersMargin: 10,
            defaultLayoutAlign: 'center',
            members: [OB.TopLayout.CompanyImageLogo, isc.Img.create({
                imageType: 'normal',
                src: OB.Application.contextUrl + 'utility/GetOpenbravoLogo.png',
                getInnerHTML: function() {
                    var html = this.Super('getInnerHTML', arguments);
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
  width: '100%',
  height: '100%'
});
OB.Layout.addMember(OB.MainView);

OB.MainView.TabSet = isc.TabSet.create({
  width: '100%',
  height: '100%',
  
  destroyPanes: true,
  
  stateAsString: null,
    
  // note see the smartclient autochild concept for why tabBarProperties is valid
  tabBarProperties: isc.addProperties({
    buttonConstructor: isc.OBTabHeaderButton,
    
    dblClickWaiting: false,
    
    itemClick: function(item, itemNum){
      var me = this;
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function(){
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          if (me.tabSet.selectedTab === itemNum && item.pane.doHandleClick) {
            item.pane.doHandleClick();
          }
        }
      }, OB.Constants.DBL_CLICK_DELAY);
      
    },
    itemDoubleClick: function(item, itemNum){
      this.dblClickWaiting = false;
      if (this.tabSet.selectedTab === itemNum && item.pane.doHandleDoubleClick) {
        item.pane.doHandleDoubleClick();
      }
    }
  }, OB.MainLayoutStylingProperties.TabSet_tabBarProperties),
  tabProperties: OB.MainLayoutStylingProperties.TabSet_tabProperties,
  
  tabSelected: function(tabNum, tabPane, ID, tab) {
    OB.Layout.HistoryManager.updateHistory();
    if (tabPane.tabSelected) {
      tabPane.tabSelected(tabNum, tabPane, ID, tab);
    }
  },
  
  tabDeselected: function (tabNum, tabPane, ID, tab, newTab) {
    if (tabPane.tabDeselected) {
      tabPane.tabDeselected(tabNum, tabPane, ID, tab, newTab);
    }
  },
  
  closeClick: function(tab) {
    if (tab.pane && tab.pane.closeClick) {
      tab.pane.closeClick(tab, this);
    } else {
      this.doCloseClick(tab);
    }   
  },
  
  doCloseClick: function(tab) {
    return this.Super('closeClick', arguments);
  },

  initWidget: function() {
    this.tabBarProperties.tabSet = this;
    return this.Super('initWidget', arguments);
  },

  draw : function() {
    var me = this;
    var ksAction_ActivateTab = function(tab) {
      me.selectTab(tab-1);
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
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
        if (typeof me.getTab(i) === 'undefined') {
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
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ActivateRightTab', ksAction_ActivateRightTab);
    var ksAction_ActivateLeftTab = function() {
      me.selectTab((me.getTabNumber(me.getSelectedTab()))-1);
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ActivateLeftTab', ksAction_ActivateLeftTab);
    var ksAction_ReloadActive = function() {
      if(typeof me.getSelectedTab().pane.refreshTab === 'function') {
        me.getSelectedTab().pane.refreshTab();
      } else {
        me.getSelectedTab().pane.markForRedraw();
      }
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.add('TabSet_ReloadActive', ksAction_ReloadActive);
    this.Super('draw', arguments);
  },

  // is used by selenium
  getTabFromTitle : function(title) {
      var index = 0, tab = null;
      for (; index < OB.MainView.TabSet.tabs.getLength();index++) {
        tab = OB.MainView.TabSet.getTabObject(index);
        if (tab.title === title) {
          return tab;
        }
      }
      return null;
    },
    
  removeTabs: function(tabs, destroyPanes) {
    var i, tab, appFrame, tabsLength, toRemove = [],
        tabSet = OB.MainView.TabSet;

    if(!tabs) {
      return;
    }

    if (!isc.isAn.Array(tabs)) { 
        tabs = [tabs];
    }

    // get the actual tab button object from whatever was passed in.
    // We can pass this to tabBar.removeTabs()
    tabs = this.map('getTab', tabs);

    tabsLength = tabs.length;

    for (i = 0; i < tabsLength; i++) {
      tab = tabSet.getTab(tabs[i].ID);
      if(tab.pane.Class === 'OBClassicWindow') {

        appFrame = tab.pane.appFrameWindow ||
                   tab.pane.getAppFrameWindow();

        if(appFrame && appFrame.isUserChanges) {
          if(appFrame.validate && !appFrame.validate()) {
            return false;
          }
          tab.pane.saveRecord(tabs[i].ID);
        } else {
          OB.Layout.ViewManager.views.removeTab(tabs[i].ID);
          toRemove.push(tabs[i].ID);
        }
      } else {
        OB.Layout.ViewManager.views.removeTab(tabs[i].ID);
        toRemove.push(tabs[i].ID);
      }
    }
    this.Super('removeTabs', [toRemove]);
    OB.Layout.HistoryManager.updateHistory();
    return true;
  }
}, OB.MainLayoutStylingProperties.TabSet);

OB.MainView.addMember(OB.MainView.TabSet);

OB.TestRegistry.register('org.openbravo.client.application.mainview', OB.MainView);
OB.TestRegistry.register('org.openbravo.client.application.mainview.tabset', OB.MainView.TabSet);
OB.TestRegistry.register('org.openbravo.client.application.layout', OB.Layout);

OB.Toolbar.addMembers([
<#list data.navigationBarComponents as nbc>
${nbc.jscode}<#if nbc_has_next>,</#if>
</#list>]);

// test to see if we can show the heartbeat or registration popups (or not)
(function _OB_checkHeartBeatRegistration() {
 var handleReturn = function(response, data, request) {
     if (data.showInstancePurpose) {
       OB.Layout.ClassicOBCompatibility.Popup.openInstancePurpose();
     } else if (data.showHeartbeat) {
       OB.Layout.ClassicOBCompatibility.Popup.openHeartbeat();
     } else if (data.showRegistration) {
       OB.Layout.ClassicOBCompatibility.Popup.openRegistration();
     }
 };

 OB.RemoteCallManager.call('org.openbravo.client.application.HeartBeatPopupActionHandler', {}, {}, handleReturn);

})();
