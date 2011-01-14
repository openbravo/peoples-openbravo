/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distribfuted  on  an "AS IS"
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
// = OBStandardViewTabSet =
//
// An OBStandardViewTabSet contains the child tabs of an OBStandardView. 
// Each tab inside an OBStandardViewTabSet contains an OBStandardView instance.
isc.ClassFactory.defineClass('OBStandardViewTabSet', isc.OBTabSetChild);

isc.OBStandardViewTabSet.addClassProperties({

  TABBARPROPERTIES: {
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
          
          // set the active tab
          if (tab.pane && tab.pane.setAsActiveView) {
            tab.pane.setAsActiveView();
          }
          
          me.tabSet.doHandleClick();
        }
      }, OB.Constants.DBL_CLICK_DELAY);
      
    },
    
    itemDoubleClick: function(item, itemNum){
      var tab = item;
      this.dblClickWaiting = false;      
      // set the active tab
      if (tab.pane && tab.pane.setAsActiveView) {
        tab.pane.setAsActiveView();
      }
      this.tabSet.doHandleDoubleClick();
    },
    
    dragStop: function(){
      // change the height to percentage based to handle resizing of browser:
      this.tabSet.parentContainer.convertToPercentageHeights();
      return true;
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
  }
});

isc.OBStandardViewTabSet.addProperties({
  tabBarPosition: 'top',
  width: '100%',
  height: '*',
  overflow: 'hidden',
  
  // get rid of the margin around the content of a pane
  paneMargin: 0,
  paneContainerMargin: 0,
  paneContainerPadding: 0,
  showPaneContainerEdges: false,
  
  state: null,
  previousState: null,
  
  // keeps track of the previous dragged height, to restore it
  draggedHeight: null,
  
  setDraggable: function(draggable){
    if (draggable) {
      this.tabBar.canDrag = true;
      this.tabBar.cursor = isc.Canvas.HAND;
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
    if (this.getSelectedTab()) {
      this.getSelectedTab().pane.setAsActiveView();
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
      // the height is set to the height of the tabbar
      this.setHeight(this.tabBar.getHeight());
      for (i = 0; i < this.tabs.length; i++) {
        tab = this.tabs[i];
        this.getTabPane(tab).hide();
      }
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
        this.makeTabVisible(tab);
        pane.members[0].setHeight('*');
        if (pane.members[1]) {
          pane.members[1].setState(isc.OBStandardView.STATE_MIN);
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
        pane.setHalfSplit();
        this.makeTabVisible(tab);
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
      pane.show();
      if (pane.doRefreshContents) {
        pane.doRefreshContents();
      }
      if (pane.members[0]) {
        pane.members[0].show();
      }
      if (pane.members[1]) {
        pane.members[1].show();
      }
      this.selectTab(tab);
    }
  },
  
  tabSelected: function(tabNum, tabPane, ID, tab){
    if (tabPane.refreshContents) {
      tabPane.doRefreshContents();
    }
  },
  
  initWidget: function(){
    this.tabBarProperties = isc.addProperties({}, isc.OBStandardViewTabSet.TABBARPROPERTIES);
    this.tabBarProperties.tabSet = this;
    this.Super('initWidget', arguments);
  }
});
