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
 * All portions are Copyright (C) 2010-2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */


isc.ClassFactory.defineClass('OBTabBarButton', isc.StretchImgButton);

isc.ClassFactory.defineClass('OBTabSet', isc.TabSet);

isc.ClassFactory.defineClass('OBTabBar', isc.TabBar);


isc.ClassFactory.defineClass('OBTabBarButtonMain', isc.OBTabBarButton);

isc.ClassFactory.defineClass('OBTabSetMain', isc.OBTabSet);

isc.OBTabSetMain.addProperties({
  destroyPanes: true,

  stateAsString: null,

  // note see the smartclient autochild concept for why tabBarProperties is valid
  tabBarProperties: isc.addProperties({
    buttonConstructor: isc.OBTabBarButtonMain,

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
  }),

  tabSelected: function(tabNum, tabPane, ID, tab) {
    if (!tabPane.isLoadingTab) {
      OB.Layout.HistoryManager.updateHistory();
    }
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
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  },

  draw : function() {
    var me = this;
    var ksAction_ActivateTab = function(tab) {
      me.selectTab(tab-1);
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.set('TabSet_ActivateTab1', ksAction_ActivateTab, 1);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab2', ksAction_ActivateTab, 2);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab3', ksAction_ActivateTab, 3);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab4', ksAction_ActivateTab, 4);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab5', ksAction_ActivateTab, 5);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab6', ksAction_ActivateTab, 6);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab7', ksAction_ActivateTab, 7);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab8', ksAction_ActivateTab, 8);
    OB.KeyboardManager.KS.set('TabSet_ActivateTab9', ksAction_ActivateTab, 9);
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
    OB.KeyboardManager.KS.set('TabSet_closeAllTabs', ksAction_closeAllTabs);
    var ksAction_ActivateRightTab = function() {
      me.selectTab((me.getTabNumber(me.getSelectedTab()))+1);
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.set('TabSet_ActivateRightTab', ksAction_ActivateRightTab);
    var ksAction_ActivateLeftTab = function() {
      me.selectTab((me.getTabNumber(me.getSelectedTab()))-1);
      if(typeof me.getSelectedTab().pane.focusTab === 'function') {
        me.getSelectedTab().pane.focusTab();
      }
    };
    OB.KeyboardManager.KS.set('TabSet_ActivateLeftTab', ksAction_ActivateLeftTab);
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
});

isc.ClassFactory.defineClass('OBTabBarMain', isc.OBTabBar);

isc.OBTabBarMain.addProperties({
  initWidget: function() {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});


isc.ClassFactory.defineClass('OBTabBarButtonChild', isc.OBTabBarButton);

isc.ClassFactory.defineClass('OBTabSetChild', isc.OBTabSet);

isc.OBTabSetChild.addProperties({
  destroyPanes: true,

  stateAsString: null,

  tabBarProperties: isc.addProperties({
    buttonConstructor: isc.OBTabBarButtonChild,

    dblClickWaiting: false,

    canDrag: false,
    dragAppearance: 'none',
    dragStartDistance: 1,
    overflow: 'hidden',

    itemClick: function(item, itemNum){
      var me = this, tab = item;
      me.dblClickWaiting = true;
      isc.Timer.setTimeout(function(){
        // if no double click happened then do the single click
        if (me.dblClickWaiting) {
          me.dblClickWaiting = false;
          me.tabSet.doHandleClick();
        }
      }, OB.Constants.DBL_CLICK_DELAY);

    },

    itemDoubleClick: function(item, itemNum){
      var tab = item;
      this.dblClickWaiting = false;
      this.tabSet.doHandleDoubleClick();
    },

    dragStop: function(){
      // change the height to percentage based to handle resizing of browser:
      this.tabSet.parentContainer.convertToPercentageHeights();
      this.setCursor(isc.Canvas.ROW_RESIZE);
      return true;
    },

    mouseDown: function() {
      if (this.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.MOVE);
      }
    },

    mouseUp: function() {
      if (this.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      }
    },

    mouseOut: function() {
      if (this.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      }
    },

    mouseOver: function() {
      if (this.state === isc.OBStandardView.STATE_IN_MID) {
        this.setCursor(isc.Canvas.ROW_RESIZE);
      }
    },

    getCurrentCursor: function() {
      if (this.state === isc.OBStandardView.STATE_IN_MID) {
        if (isc.EventHandler.leftButtonDown()) {
          return isc.Canvas.MOVE;
        }
        return isc.Canvas.ROW_RESIZE;
      }
      return this.Super('getCurrentCursor', arguments);
    },

    dragStart: function(){
      // -2 to prevent scrollbar
      this.tabSet.maxHeight = this.tabSet.parentContainer.getHeight() - 2;
      this.tabSet.minHeight = (this.getHeight() * 2) + 15;
      return true;
    },

    dragMove: function(){
      var offset = (0 - isc.EH.dragOffsetY);
      this.resizeTarget(this.tabSet, true, true, offset, -1 * this.getHeight(), null, true);
      this.tabSet.draggedHeight = this.tabSet.getHeight();
      // if (this.tabSet.getHeight() === this.getHeight()) {
      // // set the parent to top-max
      // this.tabSet.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
      // this.tabSet.draggedHeight = null;
      // }
      return true;
    }
  }),

  state: null,
  previousState: null,

  // keeps track of the previous dragged height, to restore it
  draggedHeight: null,

  setDraggable: function(draggable){
    if (draggable) {
      this.tabBar.canDrag = true;
      this.tabBar.cursor = isc.Canvas.ROW_RESIZE;
    } else {
      this.tabBar.canDrag = false;
      this.tabBar.cursor = isc.Canvas.DEFAULT;
    }
  },

  doHandleClick: function(){
    if (this.state === isc.OBStandardView.STATE_MIN) {
      // we are minimized, there must be a parent then
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_MID);
      } else {
        this.parentContainer.setHalfSplit();
      }
    } else if (this.state === isc.OBStandardView.STATE_BOTTOM_MAX) {
      this.setState(isc.OBStandardView.STATE_MID);
    }
  },

  doHandleDoubleClick: function(){
    if (this.state === isc.OBStandardView.STATE_TOP_MAX) {
      // we are maximized go back to the previous state
      if (this.previousState && this.previousState !== this.state) {
        if (this.previousState === isc.OBStandardView.STATE_IN_MID) {
          this.parentContainer.setHalfSplit();
        } else if (this.previousState === isc.OBStandardView.STATE_MIN) {
          if (this.parentTabSet) {
            this.parentTabSet.setState(isc.OBStandardView.STATE_TOP_MAX);
          } else {
            this.parentContainer.setTopMaximum();
          }
        } else {
          this.setState(this.previousState);
        }
      } else {
        this.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      }
    } else {
      // first set to IN_MID, to prevent empty tab displays
      this.setState(isc.OBStandardView.STATE_IN_MID);
      this.setState(isc.OBStandardView.STATE_TOP_MAX);
    }
  },

  getState: function(){
    return this.state;
  },

  setState: function(newState){

    // disabled this as sometimes states have
    // to be reset to recompute heights changed automatically
    // if (this.state === newState) {
    // return;
    // }

    var tab, i, pane;
    var tmpPreviousState = this.state;

    // is corrected below for one state
    this.setDraggable(false);

    if (newState === isc.OBStandardView.STATE_TOP_MAX) {
      this.state = newState;

      // minimize the ancestors
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }

      // note this for loop must be done before the parent's are
      // done otherwise the content is not drawn
      // the top member in each tab is maximized
      // the bottom member in each tab is set to the tabbar height
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setTopMaximum();
      }

    } else if (newState === isc.OBStandardView.STATE_MIN) {
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.getTabPane(tab).hide();
      }

      // the height is set to the height of the tabbar
      this.setHeight(this.tabBar.getHeight());

      this.state = newState;
    } else if (newState === isc.OBStandardView.STATE_BOTTOM_MAX) {
      // the top part in each layout is set to 0%, and the bottom to max
      this.state = newState;
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.makeTabVisible(tab);
        pane = this.getTabPane(tab);
        pane.setBottomMaximum();
      }
    } else if (newState === isc.OBStandardView.STATE_IN_MID) {
      this.state = newState;
      this.setDraggable(true);
      // minimize the third level
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        pane.setHeight('100%');
        this.makeTabVisible(tab);
        if (pane.members[1]) {
          pane.members[1].setState(isc.OBStandardView.STATE_MIN);
        } else {
          pane.members[0].setHeight('100%');
        }
      }
    } else if (newState === isc.OBStandardView.STATE_MID) {
      if (this.parentTabSet) {
        this.parentTabSet.setState(isc.OBStandardView.STATE_BOTTOM_MAX);
      } else if (this.parentContainer) {
        this.parentContainer.setBottomMaximum();
      }
      // the content of the tabs is split in 2
      this.state = newState;
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        pane = this.getTabPane(tab);
        this.makeTabVisible(tab);
        pane.setHalfSplit();
      }
    }

    this.previousState = tmpPreviousState;

    for (i = 0; i < this.tabs.length; i++) {
      tab = this.tabs[i];
      tab.pane.setMaximizeRestoreButtonState();
    }
  },

  makeTabVisible: function(tab){
    if (tab === this.getSelectedTab()) {
      pane = this.getTabPane(tab);
      if (pane.refreshContents) {
        pane.doRefreshContents();
      }
      pane.show();
      if (pane.members[0]) {
        pane.members[0].show();
      }
      if (pane.members[1]) {
        pane.members[1].show();
      }
//      this.selectTab(tab);
    }
  },

  tabSelected: function(tabNum, tabPane, ID, tab){
    if (tabPane.refreshContents) {
      tabPane.doRefreshContents(true);
    }
  },

  initWidget: function(){
    this.tabBarProperties.tabSet = this;
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});

isc.ClassFactory.defineClass('OBTabBarChild', isc.OBTabBar);

isc.OBTabBarChild.addProperties({
  initWidget: function() {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});