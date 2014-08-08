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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.APRM.MatchStatement = {};


OB.APRM.MatchStatement.onLoad = function (view) {

};


OB.APRM.MatchStatement.onProcess = function (view, actionHandlerCall) {

};


isc.ClassFactory.defineClass('APRMMatchStatGridButtonsComponent', isc.HLayout);

isc.APRMMatchStatGridButtonsComponent.addProperties({
  canExpandRecord: true,

  initWidget: function () {
    var me = this,
        searchButton, addButton, clearButton, buttonSeparator1, buttonSeparator2;

    searchButton = isc.OBGridToolStripIcon.create({
      buttonType: 'search',
      originalPrompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      prompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      action: function () {
        var processId = '9BED7889E1034FE68BD85D5D16857320',
            grid = me.grid,
            record = me.record,
            standardWindow = grid.view.parentWindow.view.standardWindow;

        //TODO: Apply the proper created process
        var process = standardWindow.buildProcess({
          callerField: me,
          paramWindow: true,
          processId: processId,
          windowId: grid.view.windowId,
          windowTitle: 'Search'
        });

        grid.openExpansionProcess(process, record);
      }
    });

    addButton = isc.OBGridToolStripIcon.create({
      buttonType: 'add',
      originalPrompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      prompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      action: function () {
        alert('Add Button');
      }
    });

    clearButton = isc.OBGridToolStripIcon.create({
      buttonType: 'clearRight',
      originalPrompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      prompt: OB.I18N.getLabel('OBUIAPP_GridEditButtonPrompt'),
      action: function () {
        alert('Clear Button');
      }
    });

    buttonSeparator1 = isc.OBGridToolStripSeparator.create({});
    buttonSeparator2 = isc.OBGridToolStripSeparator.create({});

    this.addMembers([searchButton, buttonSeparator1, addButton, buttonSeparator2, clearButton]);
    this.Super('initWidget', arguments);
  }
});


isc.APRMMatchStatGridButtonsComponent.addProperties({
  cellAlign: 'center',

  height: 21,
  width: '100%',
  overflow: 'hidden',
  align: 'center',
  defaultLayoutAlign: 'center',
  styleName: 'OBGridToolStrip',
  layoutLeftMargin: -2,
  layoutRightMargin: 0,
  membersMargin: 4
});