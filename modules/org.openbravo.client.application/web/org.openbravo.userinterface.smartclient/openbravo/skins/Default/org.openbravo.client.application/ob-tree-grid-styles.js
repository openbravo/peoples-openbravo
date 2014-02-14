isc.OBTreeGrid.addProperties({
  bodyStyleName: 'OBGridBody',
  baseStyle: 'OBTreeGridCell',
  recordStyleError: 'OBGridCellError',
  recordStyleSelectedViewInActive: 'OBGridCellSelectedViewInactive',
  headerBaseStyle: 'OBGridHeaderCell',
  headerBarStyle: 'OBGridHeaderBar',
  headerTitleStyle: 'OBGridHeaderCellTitle',
  emptyMessageStyle: 'OBGridNotificationText',
  emptyMessageLinkStyle: 'OBGridNotificationTextLink',
  cellPadding: 0,
  /* Set in the CSS */
  cellAlign: 'center',
  leaveHeaderMenuButtonSpace: false,
  sortAscendingImage: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_sortAscending.png',
    width: 7,
    height: 11
  },
  sortDescendingImage: {
    src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_sortDescending.png',
    width: 7,
    height: 11
  },
  headerMenuButtonConstructor: 'OBGridHeaderImgButton',
  headerButtonConstructor: 'ImgButton',
  headerMenuButtonWidth: 17,
  headerMenuButtonSrc: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeaderMenuButton.png',
  hoverWidth: 200
});

isc.OBTreeGrid.changeDefaults('headerButtonDefaults', {
	  showTitle: true,
	  showDown: true,
	  showFocused: false,
	  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
	  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png'
});

isc.OBTreeGrid.changeDefaults('headerMenuButtonDefaults', {
	  showDown: false,
	  showTitle: true
	  //src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png'
});

isc.OBTreeGrid.changeDefaults('sorterDefaults', {
	  // baseStyle / titleStyle is auto-assigned from headerBaseStyle
	  showFocused: false,
	  //  src: '[SKIN]ListGrid/header.png',
	  src: OB.Styles.skinsPath + 'Default/org.openbravo.client.application/images/grid/gridHeader_bg.png',
	  baseStyle: 'OBGridSorterButton'
	});

isc.OBTreeGrid.addProperties({
  // note should be the same as the height of the OBGridButtonsComponent
  recordComponentHeight: 21,
  cellHeight: 25,
  bodyStyleName: 'OBViewGridBody'
});