/*
 ************************************************************************************
 * Copyright (C) 2013-2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global Backbone*/

/** Backbone model kept for backward compatibibility. @see OB.App.OfflineSession  */
OB.Data.Registry.registerModel(
  Backbone.Model.extend({
    modelName: 'Supervisor'
  })
);
