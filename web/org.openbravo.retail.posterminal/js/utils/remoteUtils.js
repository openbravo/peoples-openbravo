/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */
OB.UTIL.remoteSearch = function(model) {
  let isRemoteSearch = false;
  if (
    OB.MobileApp.model.hasPermission(model.prototype.remote, true) ||
    OB.App.MasterdataController.isLoadingCacheForModel(
      model.prototype.modelName
    )
  ) {
    isRemoteSearch = true;
  }

  return isRemoteSearch;
};
