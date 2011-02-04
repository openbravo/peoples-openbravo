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
// = OBViewDataSource =
//
// The datasource which is used within a view. It adds specific behavior
// by adding extra request parameters.
//
isc.ClassFactory.defineClass('OBViewDataSource', isc.OBRestDataSource);

isc.OBViewDataSource.addProperties( {

  showProgress : function(editedRecord) {

    // don't show it, done to quickly
    if (!editedRecord._showProgressAfterDelay) {
      return;
    }

    if (editedRecord && editedRecord.editColumnLayout) {
      if (!this.view.isShowingForm) {
        editedRecord.editColumnLayout.toggleProgressIcon(true);
      }
    }

    if (this.view.isShowingForm) {
      var btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
      btn.customState = 'Progress';
      btn.resetBaseStyle();
      btn.markForRedraw();
    }
  },

  hideProgress : function(editedRecord) {
    editedRecord._showProgressAfterDelay = false;
    if (editedRecord && editedRecord.editColumnLayout) {
      editedRecord.editColumnLayout.toggleProgressIcon(false);
    }

    // always remove the progress style here anyway
    var btn = this.view.toolBar.getLeftMember(isc.OBToolbar.TYPE_SAVE);
    btn.customState = '';
    btn.resetBaseStyle();
    btn.markForRedraw();
  },

  performDSOperation : function(operationType, data, callback,
      requestProperties) {
    // requestProperties.showPrompt = false;
    // set the current selected record before the delay
    var currentRecord = this.view.viewGrid.getSelectedRecord();
    if (currentRecord) {
      // only show progress after 200ms delay
      currentRecord._showProgressAfterDelay = true;
      // keep the edited record in the client context
      if (!requestProperties.clientContext) {
        requestProperties.clientContext = {};
      }
      requestProperties.clientContext.progressIndicatorSelectedRecord = currentRecord;
      this
          .delayCall(
              'showProgress',
              [ requestProperties.clientContext.progressIndicatorSelectedRecord ],
              200);
    }
    
    // doing row editing
    if (this.view.viewGrid.getEditRow()) {
      if (!requestProperties.clientContext) {
        requestProperties.clientContext = {};
      }
      requestProperties.clientContext.editRow = this.view.viewGrid.getEditRow(); 
    }

    var newRequestProperties = this.getTabInfoRequestProperties(this.view,
        requestProperties);
    // standard update is not sent with operationType
    var additionalPara = {
      _operationType : 'update',
      _noActiveFilter : true
    };
    isc.addProperties(newRequestProperties.params, additionalPara);
    if (!newRequestProperties.dataSource) {
      newRequestProperties.dataSource = this;
    }
    this.Super('performDSOperation', [ operationType, data, callback,
        newRequestProperties ]);
  },

  transformResponse : function(dsResponse, dsRequest, jsonData) {
    if (dsRequest.clientContext && dsRequest.clientContext.progressIndicatorSelectedRecord) {
      this.hideProgress(dsRequest.clientContext.progressIndicatorSelectedRecord);
    }
    if (jsonData) {
      var errorStatus = !jsonData.response || jsonData.response.status === 'undefined' || jsonData.response.status !== isc.RPCResponse.STATUS_SUCCESS;
      if (errorStatus) {
        var handled = this.view.setErrorMessageFromResponse(dsResponse,
            jsonData, dsRequest);

        if (!handled && !dsRequest.willHandleError) {
          OB.KernelUtilities.handleSystemException(error.message);
        }
      } else {
        // there are some cases where the jsonData is not passed, in case of
        // errors
        // make it available through the response object
        dsResponse.dataObject = jsonData;
      }
    }
    return this.Super('transformResponse', arguments);
  },

  // ** {{{ getTabInfoRequestProperties }}} **
  //
  // Adds tab and module information to the requestProperties.
  //
  // Parameters:
  // * {{{theView}}}: view to obtain tab and module info from.
  // * {{{requestProperties}}}: original requestProperties.
  // Return:
  // * Original requestProperties including the new module and tab
  // properties.
  getTabInfoRequestProperties : function(theView, requestProperties) {
    if (theView && theView.tabId) {
      var tabParam = {
        params : {
          windowId : theView.standardWindow.windowId,
          tabId : theView.tabId,
          moduleId : theView.moduleId
        }
      };
      if (requestProperties) {
        isc.addProperties(requestProperties, tabParam);
      } else {
        requestProperties = tabParam;
      }
    }
    return requestProperties;
  }
});
