/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use. this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2024 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

isc.ClassFactory.defineClass('MultiVariantPurchaseGridItem', isc.CanvasItem);

isc.MultiVariantPurchaseGridItem.addProperties({
  completeValue: null,
  showTitle: false,
  productData: {}, // Stores data for different products
  init: function() {
    this.colSpan = 4;
    this.disabled = false;
    this.grid = this.createGrid();
    this.Super('init', arguments);
    OB.MultiVariantPurchaseGridItem = this;
  },
  createGrid: function(columns = [], rows = [], initialValues = []) {
    // Create or recreate ListGrid based on new or existing configuration
    if (this.grid) {
      this.grid.destroy(); // If grid exists, destroy before recreating
    }
    const cols = columns.map(col => ({
      name: col.name,
      title: col.title,
      type: 'integer',
      defaultValue: 0
    }));

    let rowsData = rows.map(row => ({
      // TODO: Remove mentions to color
      color: row.title,
      ...columns.reduce((acc, col) => ({ ...acc, [col.name]: 0 }), {})
    }));

    initialValues.forEach(item => {
      let row = rowsData.find(
        r => r.color.toLowerCase() === item.color.toLowerCase()
      );
      if (row && row.hasOwnProperty(item.size)) {
        row[item.size] = item.quantity;
      }
    });

    this.grid = isc.ListGrid.create({
      width: 300,
      height: 100,
      canEdit: true,
      editEvent: 'click',
      editByCell: true,
      showHeaderContextMenu: false, // Disables the default context menu for headers
      headerContextMenu: null, // Ensure no custom menus are applied
      autoFitData: 'vertical', // Fit grid vertically based on number of rows
      autoFitMaxRecords: 10, // Maximum number of records to fit without scrolling
      canReorderFields: false, // Disables dragging to reorder columns
      canResizeFields: false, // Optionally disable resizing columns
      leaveScrollbarGap: false, // If you expect to never exceed 10, setting to false can remove unnecessary scrollbar space
      bodyOverflow: 'visible',
      overflow: 'hidden', // Use hidden to cut off any excess but consider "auto" if exceeding
      redraw: function() {
        this.Super('redraw', arguments);
        this.adjustHeight();
      },
      adjustHeight: function() {
        const rowHeight = this.getRowHeight();
        const numRows = this.getTotalRows();
        const newHeight = rowHeight * Math.min(numRows, 10); // Calculate new height but limit to 10 rows
        this.setHeight(newHeight);
      },
      fields: [
        {
          name: 'color',
          title: '',
          canEdit: false,
          width: 100,
          canSort: false
        },
        ...cols
      ],
      data: rowsData,
      autoDraw: false
    });

    this.setCanvas(this.grid); // Assign the grid as the canvas of the CanvasItem
    this.grid.draw(); // Manually draw the grid
    return this.grid;
  },

  selectProduct: function(productId, columns, rows, initialValues) {
    // Save current data if productId exists
    if (this.currentProductId && this.grid) {
      // Get the current data from the grid and convert it to a JSON string
      var currentData = this.grid.getData();
      this.productData[this.currentProductId] = currentData.flatMap(row =>
        this.transformObjectToArray(row)
      );
    }

    // Check if product data already exists
    if (this.productData[productId]) {
      var savedData = this.productData[productId];
      // Create grid with saved data
      this.createGrid(columns, rows, savedData);
    } else {
      // Create new grid with initial values
      this.createGrid(columns, rows, initialValues);
    }

    this.currentProductId = productId; // Update current product id
  },

  transformObjectToArray: function(obj) {
    // Extract the color value first
    const color = obj.color;

    // Use Object.keys() to get all keys, filter out 'color', and map to new array format
    return Object.keys(obj)
      .filter(key => key !== 'color') // Ignore the 'color' key
      .map(size => ({
        color: color,
        size: size,
        quantity: obj[size]
      }));
  },

  destroy: function() {
    if (this.canvas && typeof this.canvas.destroy === 'function') {
      this.canvas.destroy();
      this.canvas = null;
    }
    return this.Super('destroy', arguments);
  }
});
