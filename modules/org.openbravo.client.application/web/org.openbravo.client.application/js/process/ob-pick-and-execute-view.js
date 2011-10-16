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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.defineClass('OBPickAndExecuteView', isc.OBPopup);


isc.OBPickAndExecuteView.addProperties({

  // Override default properties of OBPopup
  canDragReposition: false,
  canDragResize: false,
  isModal: false,
  showModalMask: false,
  dismissOnEscape: false,
  showMinimizeButton: false,
  showMaximizeButton: false,
  showFooter: false,

  width: '100%',
  overflow: 'auto',

  dataSource: null,

  viewGrid: null,

  gridFields: null,

  initWidget: function () {

    var view = this,
        okButton = isc.OBFormButton.create({
        title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
        click: function () {
          console.log(view.viewGrid.getSelectedRecords());
        }
      }),
        cancelButton = isc.OBFormButton.create({
        title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
        click: function () {
          view.viewGrid.discardAllEdits();
        }
      });

    this.prepareGridFields(this.viewProperties.fields);

    this.dataSource = this.viewProperties.dataSource;
    this.dataSource.view = this;

    this.viewGrid = isc.OBPickAndExecuteGrid.create({
      view: this,
      sortField: 'lineNo',
      fields: this.gridFields,
      // FIXME: using fixed size
      height: 300,
      dataSource: this.dataSource
    });

    this.items = [this.viewGrid, isc.HLayout.create({
      styleName: this.buttonBarStyleName,
      height: this.buttonBarHeight,
      defaultLayoutAlign: 'center',
      members: [isc.LayoutSpacer.create({}), okButton, isc.LayoutSpacer.create({
        width: this.buttonBarSpace
      }), cancelButton, isc.LayoutSpacer.create({})]
    })];

    this.Super('initWidget', arguments);
    this.viewGrid.fetchData();
  },

  prepareGridFields: function (fields) {
    //FIXME: using fixed $b4 reference
    var result = isc.OBStandardView.$b4.prepareGridFields.apply(this, arguments),
        i, f, len = result.length;
    for (i = 0; i < len; i++) {
      if (result[i].disabled) {
        result[i].canEdit = false;
      }
    }
    this.gridFields = result;
  },

  // dummy required by OBStandardView.prepareGridFields
  setFieldFormProperties: function () {}
});