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
 * All portions are Copyright (C) 2009-2010 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

// = Openbravo Datasource Utilities =
// Defines a number of utility methods related to datasources.
OB.Datasource = {};

// ** {{{ OB.Datasource.get(dataSourceId, target, dsFieldName) }}} **
//
// Retrieves a datasource from the server. The return from the server is a
// javascript
// string which is evaluated. This string should create a datasource. The
// datasource
// object is set in a field of the target (if the target parameter is set). This
// is
// done asynchronously.
//
// The method returns the datasourceid.
//
// Parameters:
// * {{{dataSourceId}}}: the id or name of the datasource
// * {{{target}}}: the target object which needs the datasource
// * {{{dsFieldName}}}: the field name to set in the target object.
// * {{{doNew}}}: if set to true then a new datasource is created
// If not set then setDataSource or optionDataSource are used.
//
OB.Datasource.get = function(/* String */dataSourceId, /* Object */
target, /* String */dsFieldName, /*Boolean*/ doNew) {
  var ds;
  if (!doNew) {  
    ds = isc.DataSource.getDataSource(dataSourceId);
    if (ds) {
      // only set if target is defined
      if (target) {
        if (dsFieldName) {
          target[dsFieldName] = ds;
        } else if (target.setDataSource) {
          target.setDataSource(ds);
        } else {
          target.optionDataSource = ds;
        }
      }
      return ds;
    }
  }

  // create the callback
  var callback = function(rpcResponse, data, rpcRequest) {
    // prevent registering it again
    var ds = isc.DataSource.getDataSource(data.ID);
    if (ds) {
      data = ds;
    } else if (!isc.DataSource.get(data.ID)) {
      isc.DataSource.registerDataSource(data);
    }

    // only set if target is defined
    if (target) {
      if (dsFieldName) {
        target[dsFieldName] = data;
      } else if (target.setDataSource) {
        target.setDataSource(data);
      } else {
        target.optionDataSource = data;
      }
    }
  };

  var rpcRequest = {};
  rpcRequest.params = {
    'create' : true
  };
  if (doNew) {
    rpcRequest.params._new = true;
  }
  rpcRequest.httpMethod = 'GET';
  rpcRequest.actionURL = OB.Application.contextUrl
      + 'org.openbravo.client.kernel/OBSERDS_Datasource/' + dataSourceId;
  rpcRequest.callback = callback;
  rpcRequest.useSimpleHttp = true;
  rpcRequest.evalResult = true;
  isc.RPCManager.sendRequest(rpcRequest);

  // return null
  return dataSourceId;
};

// ** {{{ OB.Datasource.create}}} **
// Performs a last check if the datasource was already registered before
// actually creating it, prevents re-creating datasources when multiple
// async requests are done for the same datasource.
// Parameters:
// * {{{dsProperties}}}: the properties of the datasource which needs to be
// created.
OB.Datasource.create = function(/* Object */dsProperties) {
  if (dsProperties.ID) {
    var ds = isc.DataSource.getDataSource(dsProperties.ID);
    if (ds) {
      return ds;
    }
  }
  return isc.RestDataSource.create(dsProperties);
};
