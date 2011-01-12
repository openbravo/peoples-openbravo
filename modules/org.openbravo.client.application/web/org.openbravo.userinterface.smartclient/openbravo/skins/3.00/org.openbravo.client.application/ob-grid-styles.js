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

// Styling properties for a generic grid (ob-grid.js)
isc.OBGrid.addProperties({
  baseStyle: 'OBGridCell',
  baseStyleView: 'OBGridCell', // for use in ob-view-grid.js while no editing a cell
  baseStyleEdit: 'OBGridCellEdit', // for use in ob-view-grid.js while editing a cell
  headerBaseStyle: 'OBGridHeaderCell',
  headerBarStyle: 'OBGridHeaderBar',
  headerTitleStyle: 'OBGridHeaderCellTitle',
  cellPadding: 0, /* Set in the CSS */
  cellHeight: 22,
  sortAscendingImage:{src:'[SKIN]/../../../org.openbravo.client.application/images/grid/gridHeader_sortAscending.gif', width:7, height:11},
  sortDescendingImage:{src:'[SKIN]/../../../org.openbravo.client.application/images/grid/gridHeader_sortDescending.gif', width:7, height:11},
  headerMenuButtonConstructor: 'OBGridHeaderImgButton',
  headerMenuButtonWidth: 17,
  headerMenuButtonSrc: '[SKIN]/../../org.openbravo.client.application/images/grid/gridHeaderMenuButton.png',
  hoverWidth: 200,
  editLinkColumnWidth: 58,

  summaryRowConstructor: 'OBGridSummary',
  summaryRowDefaults:{
    showRollOver:false
  },
  summaryRowHeight: 22,
  summaryRowStyle: 'OBGridSummaryCell',
  summaryRowStyle_sum: 'OBGridSummaryCell_sum',
  summaryRowStyle_avg: 'OBGridSummaryCell_avg',
  
  progressIconDefaults: {
      width: 16,
      height: 16,
      visibility: 'hidden',
      src: '[SKIN]/../../org.openbravo.client.application/images/system/progress-indicator-row.gif'
  }
});

isc.OBGrid.changeDefaults('filterEditorDefaults', {
  height: 22,
  styleName: 'OBGridFilterBase',
  baseStyle: 'OBGridFilterCell'
});

isc.OBGrid.changeDefaults('sorterDefaults', {
  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
  showFocused: false,
//  src: '[SKIN]ListGrid/header.png',
  src: '[SKIN]/../../org.openbravo.client.application/images/grid/gridHeader_bg.png',
  baseStyle: 'OBGridSorterButton'
});

isc.OBGrid.changeDefaults('headerButtonDefaults', {
  showTitle: true,
  showDown: true,
  showFocused: false,
  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
  src: '[SKIN]/../../org.openbravo.client.application/images/grid/gridHeader_bg.png'
});

isc.OBGrid.changeDefaults('headerMenuButtonDefaults', {
  showDown: false,
  showTitle: true,
  baseStyle: 'pepe'
  //src: '[SKIN]/../../org.openbravo.client.application/images/grid/gridHeader_bg.png'
});


// Styling properties for the header button of a generic grid (ob-grid.js)
isc.OBGridHeaderImgButton.addProperties({
  showFocused: false,
  showRollOver: false,
  showFocusedAsOver: false,
  showDown: false
});


// Styling properties for the buttons of the grid in 'grid mode' (ob-view-grid.js)
isc.OBGridToolStripIcon.addProperties({
  width: 21,
  height: 19,
  showRollOver: true,
  showDown: true,
  showDisabled: false,
  showFocused: false,
  showFocusedAsOver: true,
  baseStyle: 'OBGridToolStripIcon',
  initWidgetStyle: function() {
    this.setSrc('[SKIN]/../../org.openbravo.client.application/images/grid/gridButton-' + this.buttonType + '.png'); /* this.buttonType could be: edit - form - cancel - save */
  }
});

isc.OBGridToolStripSeparator.addProperties({
  width: 1,
  height: 11,
  imageType: 'normal',
  src: '[SKIN]/../../org.openbravo.client.application/images/grid/gridButton-separator.png'
});

isc.OBGridToolStrip.addProperties({
  height: '100%',
  width: '100%',
  styleName: 'OBGridToolStrip',
  membersMargin: 4
});

isc.OBGridButtonsComponent.addProperties({
  height: 1,
  width: '100%',
  visible: 'overflow',
  align: 'center'
});

isc.OBGridLinkButton.addProperties({
  baseStyle: 'OBGridLinkButton',
  showDown: true,
  showFocused: true,
  showFocusedAsOver: true,
  showRollOver: true,
  autoFit: true,
  height: 1,
  overflow: 'visible'
});