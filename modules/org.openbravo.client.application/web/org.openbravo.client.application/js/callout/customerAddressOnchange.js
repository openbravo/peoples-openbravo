/*
 ************************************************************************************
 * Copyright (C) 2022 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.CORE = window.OB.CORE || {};
OB.CORE.OnChangeFunctions = {};

OB.CORE.OnChangeFunctions.Set_Default_IsAddrProperty = function(
  item,
  view,
  form,
  grid
) {
  if (form.isNew) {
    // Ensure that if a property is created from address property tab it is marked as isAddressProperty
    form.setItemValue('isAddressProperty', true);
  }
};

OB.OnChangeRegistry.register(
  'EE99B25298D14C67AC7674E560F24BCB',
  'apiKey',
  OB.CORE.OnChangeFunctions.Set_Default_IsAddrProperty,
  'OBMOBC_Set_Default_IsAddrProperty'
);
