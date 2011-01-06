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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */


isc.ClassFactory.defineClass('OBStatusBarTextLabelBar', isc.HLayout);

isc.ClassFactory.defineClass('OBStatusBarTextLabel', isc.Label);


isc.ClassFactory.defineClass('OBStatusBarIconButtonBar', isc.HLayout);

isc.ClassFactory.defineClass('OBStatusBarIconButton', isc.ImgButton);

isc.OBStatusBarIconButton.addProperties({
  buttonType: null,
  view: null,

  action: function(){
    var rowNum, newRowNum, newRecord;
    if (this.buttonType === 'previous') {
      rowNum = this.view.viewGrid.data.indexOf(this.view.viewGrid.getSelectedRecord());
      newRowNum = rowNum - 1;
      if (newRowNum > -1) {
        newRecord = this.view.viewGrid.getRecord(newRowNum);
        this.view.viewGrid.scrollRecordToTop(newRowNum);
        this.view.editRecord(newRecord);
        this.view.updateTabTitle();
      }
    } else if (this.buttonType === 'next') {
      rowNum = this.view.viewGrid.data.indexOf(this.view.viewGrid.getSelectedRecord());
      newRowNum = rowNum + 1;
      // if there is data move to it
      if (this.view.viewGrid.data.get(newRowNum)) {
        newRecord = this.view.viewGrid.getRecord(newRowNum);
        this.view.viewGrid.scrollRecordToTop(newRowNum);
        this.view.editRecord(newRecord);
        this.view.updateTabTitle();
      }
    } else if (this.buttonType === 'close') {
      this.view.switchFormGridVisibility();
    }
  },

  initWidget: function() {
    if (this.initWidgetStyle) {
      this.initWidgetStyle();
    }
    this.Super('initWidget', arguments);
  }
});


isc.ClassFactory.defineClass('OBStatusBar', isc.HLayout);

isc.OBStatusBar.addProperties({
  view: null,
  iconButtonGroupSpacerWidth: 0, //Set in the skin

  initWidget: function() {
    var messageBar = isc.OBStatusBarTextLabelBar.create({});
    messageBar.addMember(isc.OBStatusBarTextLabel.create({
      contents: '' //'Status:'
    }));

    var previousButton = isc.OBStatusBarIconButton.create({view: this.view, buttonType: 'previous'});
    var nextButton = isc.OBStatusBarIconButton.create({view: this.view, buttonType: 'next'});
    var closeButton = isc.OBStatusBarIconButton.create({view: this.view, buttonType: 'close'});
    var buttonSpacer = isc.HLayout.create({width: this.iconButtonGroupSpacerWidth});
    var buttonBar = isc.OBStatusBarIconButtonBar.create({});

    buttonBar.addMembers([previousButton, nextButton, buttonSpacer, closeButton]);
    this.addMembers([messageBar, buttonBar]);
  }
});