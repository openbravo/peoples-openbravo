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
isc.MenuButton.create({
    title: OB.I18N.getLabel('${data.label}'),
    baseStyle: 'navBarButton',
    showMenuButtonImage: false,
    align: "center",
    height: 26,
    iconHeight: 6,
    iconWidth: 10,
    iconSpacing: 10,
    iconAlign: "left",
    iconOrientation: 'right',
    icon: {src: "[SKINIMG]../../org.openbravo.client.navigationbarcomponents/images/ico-green-arrow-down.gif"},

    // put something in the array, otherwise there 
    // are small styling issues
    baseData: [        
    <#list data.rootMenuOptions as menuOption>
        <@createMenuItem menuOption=menuOption /><#if menuOption_has_next>,</#if>        
    </#list>
    ],
    
    initWidget: function() {
        // tell the menu who we are
        this.menu.menuButton = this;
        this.Super("initWidget", arguments);
    },
    
    showMenu: function() {
        var recent = OB.RecentUtilities.getRecentValue('UINAVBA_MenuRecentList');
        var recentEntries = [];
        if (recent && recent.length > 0) {
          for (var recentIndex = 0; recentIndex < recent.length; recentIndex++) {
            var recentEntry = recent[recentIndex];
            if (recentEntry) {            
                recentEntries[recentIndex] = {title: recentEntry.tabTitle, recentObject: recentEntry, _baseStyle: 'navBarComponentMenuItemCell'};
            }
          }
          recentEntries[recent.length] = {isSeparator: true};
        }
        this.menu.setData(recentEntries.concat(this.baseData));
        this.menu.markForRedraw();
        
        this.parentElement.setStyleName('navBarComponentSelected');
        
        this.Super("showMenu", arguments);
    },

    menu: isc.Menu.create({
    
    // move the menu a few pixels down and a bit to the left
    placeNear: function(left, top) {
        var parentLeft = this.menuButton.parentElement.getPageLeft();        
        return this.Super("placeNear", [parentLeft, top - 1]);
    },
    
    initWidget: function() {
        var theMenu = this;
        this.Super("initWidget", arguments);
        isc.Page.registerKey({keyName: "m", ctrlKey: true, shiftKey: true}, function(key, target) {theMenu.menuButton.showMenu();});
    },
    
    baseStyle: "navBarComponentMenuItemCell",
    styleName: "navBarComponentMenu",
    bodyStyleName: "navBarComponentMenuBody",
    
    autoDraw: false,
    autoFitData: "both",
    canHover: false,
    showIcons: false,    
    submenuOffset: -6,
    selectedHideLayout: null,
    
    show: function() {
        this.Super("show", arguments);
        this.menuButton.parentElement.setStyleName('navBarComponentSelected');        

        // this code hides the horizontal line between the menu button and the menu
        var layoutContainer = this.menuButton.parentElement;
        this.selectedHideLayout = isc.Layout.create({styleName: 'navBarComponentHideLine', 
          height: 4, width: layoutContainer.getVisibleWidth() - 2, 
          top: layoutContainer.getPageTop() + layoutContainer.getVisibleHeight() - 2, 
          left: layoutContainer.getPageLeft() + 1});
        this.selectedHideLayout.show();
        this.selectedHideLayout.moveAbove(this);
    },
    
    hide: function() {
        this.Super("hide", arguments);                
        if (this.selectedHideLayout) {
          this.selectedHideLayout.hide();
          this.selectedHideLayout.destroy();
          this.selectedHideLayout = null;
        }
        this.menuButton.parentElement.setStyleName('navBarComponent');        
    },
    
    itemClick: function(item, colNum) {
        var goto = null;
        if (item.viewId) {
            goto = item;
        } else if (item.recentObject) {
            goto = item.recentObject;
            if (!goto.viewId) {
                goto.viewId = "ClassicOBWindow"; 
            }
        } else if (item.tabId) {
            goto = {viewId: "ClassicOBWindow", windowId: item.windowId, tabId: item.tabId, id: item.tabId, command: "DEFAULT", tabTitle: item.title};
        } else if (item.manualUrl) {
            if (item.manualProcessId) {
                goto = {viewId: "ClassicOBWindow", obManualURL: item.manualUrl, processId: item.manualProcessId, id: item.manualProcessId, command: "DEFAULT", tabTitle: item.title};
            } else if (item.processId) {
                goto = {viewId: "PopupClassicOBWindow", obManualURL: item.manualUrl, processId: item.processId, id: item.processId, command: "BUTTON" + item.processId, tabTitle: item.title};
            } else if (item.formId) { 
                goto = {viewId: "ClassicOBWindow", obManualURL: item.manualUrl, id: item.manualUrl, formId: item.formId, command: "DEFAULT", tabTitle: item.title};
            } else {
                goto = {viewId: "ClassicOBWindow", obManualURL: item.manualUrl, id: item.manualUrl, command: "DEFAULT", tabTitle: item.title};
            }
        } else if (item.externalUrl) {
            goto = {viewId: "ExternalPage", contentsURL: item.externalUrl, id: item.externalUrl, command: "DEFAULT", tabTitle: item.title};
        }
        OB.RecentUtilities.addRecent('UINAVBA_MenuRecentList', goto);        
        OB.Layout.ViewManager.openView(goto.viewId, goto);
    }
    })
})

<#macro createMenuItem menuOption>
    {title: '${menuOption.label?js_string}'
    <#if menuOption.window>
        , tabId: '${menuOption.id?js_string}'
        , windowId: '${menuOption.menu.window.id?js_string}'
    <#elseif menuOption.process>
        , manualUrl: '${menuOption.id?js_string}', processId: '${menuOption.menu.process.id}'
    <#elseif menuOption.processManual>
        , manualUrl: '${menuOption.id?js_string}', manualProcessId: '${menuOption.menu.process.id}'
    <#elseif menuOption.task>
        , manualUrl: '${menuOption.id?js_string}'
    <#elseif menuOption.form>
        , manualUrl: '${menuOption.id?js_string}'
        , formId: '${menuOption.formId?js_string}'
    <#elseif menuOption.external>
        , externalUrl: '${menuOption.id?js_string}'
    <#elseif menuOption.view>
        , viewId: '${menuOption.id?js_string}'
        , tabTitle: '${menuOption.label?js_string}'
    </#if>
    <#list menuOption.parameters as parameter>
        , '${parameter.name?js_string}': '${parameter.parameterValue?js_string}'        
    </#list>
    , '_baseStyle': 'navBarComponentMenuItemCell'
    <#if menuOption.children?size &gt; 0>
    , submenu: [
    <#list menuOption.children as childMenuOption>
        <@createMenuItem menuOption=childMenuOption /><#if childMenuOption_has_next>,</#if>        
    </#list>]
    </#if>
    }
</#macro>