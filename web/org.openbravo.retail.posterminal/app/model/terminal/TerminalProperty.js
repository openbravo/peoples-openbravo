/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines the access to the terminal properties
 */

OB.App.TerminalProperty = {};

OB.App.TerminalProperty.get = property => {
  return OB.MobileApp.model.get(property);
};
