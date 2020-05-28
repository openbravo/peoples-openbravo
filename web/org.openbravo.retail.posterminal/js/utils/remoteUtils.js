/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global OB */
OB.UTIL.remoteSearch = function(currentModel) {
  return (
    OB.MobileApp.model.hasPermission(currentModel.prototype.remote, true) ||
    Object.keys(OB.Model)
      .filter(
        model =>
          OB.Model[model] &&
          OB.Model[model].prototype &&
          OB.Model[model].prototype.remote === currentModel.prototype.remote
      )
      .map(model => OB.Model[model])
      .map(model =>
        OB.App.MasterdataController.isLoadingCacheForModel(
          model.prototype.equivalentModel
            ? model.prototype.equivalentModel
            : model.prototype.indexDBModel
            ? model.prototype.indexDBModel
            : model.prototype.modelName
        )
      )
      .reduce((accum, current) => accum || current)
  );
};
