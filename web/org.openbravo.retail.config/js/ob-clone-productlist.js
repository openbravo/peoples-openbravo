/*
 ************************************************************************************
 * Copyright (C) 2012-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

// Registers a button to clone the product list
// put within a function to hide local vars etc.
(function() {
  var cloneButtonProps = isc.addProperties(
    {},
    isc.OBToolbar.CLONE_BUTTON_PROPERTIES
  );

  cloneButtonProps.action = function() {
    var view = this.view,
      callback;

    callback = function(ok) {
      var requestParams;

      if (ok) {
        requestParams = {
          ProductListId: view.viewGrid.getSelectedRecord().id
        };
        OB.RemoteCallManager.call(
          'org.openbravo.retail.config.businesslogic.CloneProductListActionHandler',
          {},
          requestParams,
          function(rpcResponse, data, rpcRequest) {
            var recordIndex =
                view.viewGrid.getRecordIndex(
                  view.viewGrid.getSelectedRecord()
                ) + 1,
              status = rpcResponse.status;
            if (data && Object.prototype.hasOwnProperty.call(data, 'status')) {
              status = data.status;
            }
            if (status === isc.RPCResponse.STATUS_SUCCESS) {
              // takes care of transforming dates etc.
              data = view.viewGrid.getDataSource().recordsFromObjects(data)[0];
              view.viewGrid.data.insertCacheData(data, recordIndex);
              view.viewGrid.scrollToRow(recordIndex);
              view.viewGrid.markForRedraw();
              view.editRecord(view.viewGrid.getRecord(recordIndex), false);
            } else {
              if (view.isShowingForm) {
                view.switchFormGridVisibility();
              }
              view.messageBar.setMessage(
                isc.OBMessageBar.TYPE_ERROR,
                null,
                data.error
              );
              view.refresh();
            }
          }
        );
      }
    };
    isc.ask(OB.I18N.getLabel('OBRETCO_WantToCloneProductList'), callback);
  };
  cloneButtonProps.buttonType = 'obretco_clone';
  // register the button for product list tab
  OB.ToolbarRegistry.registerButton(
    cloneButtonProps.buttonType,
    isc.OBToolbarIconButton,
    cloneButtonProps,
    100,
    ['09EF738DE00C4500819EC13BB48DEAE2']
  );
})();
