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

isc.ClassFactory.defineClass('OBHelpAbout', isc.OBQuickRun);

// = OB Help About =
// Provides the help/about widget in the navigation bar. It displays two
// links: about and help. The help link will only be
// displayed if the current selected window has a help view.
isc.OBHelpAbout
    .addProperties( {

      layoutProperties : {},

      title : OB.I18N.getLabel('UINAVBA_Help'),

      // Set to empty to prevent an icon from being displayed on the button.
      src : '',

      showTitle : true,

      beforeShow : function() {
        // determine if the help should be displayed or not
      var tabPane = null, aboutLink = null, helpLink = null, helpView = null;

      aboutLink = {
        editorType : 'link',
        value : null,
        showTitle : false,
        target : 'javascript',
        shouldSaveValue : false,
        linkTitle : OB.I18N.getLabel('UINAVBA_About'),
        handleClick : function() {
          isc.OBQuickRun.hide();

          OB.Layout.ClassicOBCompatibility.Popup.open('About', 620, 500,
              OB.Application.contextUrl + '/ad_forms/about.html', '', window);
        }
      };

      helpLink = {
        editorType : 'link',
        value : null,
        showTitle : false,
        target : 'javascript',
        shouldSaveValue : false,
        linkTitle : OB.I18N.getLabel('UINAVBA_Help'),
        handleClick : function() {
          isc.OBQuickRun.hide();
          OB.Layout.ViewManager.openView(helpView.viewId, helpView);
        }
      };

      // get the selected tab
      var selectedTab = OB.MainView.TabSet.getSelectedTab();
      if (selectedTab && selectedTab.pane && selectedTab.pane.getHelpView) {
        tabPane = selectedTab.pane;
      }
      // determine if a help link should be shown or not
      if (!tabPane) {
        this.members[0].setFields( [ aboutLink ]);
      } else {
        helpView = tabPane.getHelpView();
        if (!helpView) {
          this.members[0].setFields( [ aboutLink ]);
        } else {
          this.members[0].setFields( [ helpLink, aboutLink ]);
        }
      }
    },

    members : [ isc.DynamicForm.create( {
      numCols : 1
    }) ],

    keyboardShortcutId : 'NavBar_OBHelpAbout'
    });
