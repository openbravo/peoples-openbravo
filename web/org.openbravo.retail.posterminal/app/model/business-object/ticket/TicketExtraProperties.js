/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/**
 * @fileoverview Defines a mechanism to provide extra properties when creating a new Ticket
 * @see TicketUtils.newTicket
 */

(function TicketExtraPropertiesDefinition() {
  // This implementation is based in the "old" Receipt Properties dialog
  // It should be replaced with a new mechanism to register extra properties for the ticket
  const getTicketExtraProperties = () => {
    const window = OB.MobileApp.view.$.containerWindow;
    if (
      window &&
      window.getRoot() &&
      window.getRoot().$.receiptPropertiesDialog
    ) {
      const properties = window.getRoot().$.receiptPropertiesDialog
        .newAttributes;
      return properties.reduce((o, p) => {
        if (p.modelProperty) {
          return { ...o, [p.modelProperty]: p.defaultValue || '' };
        }
        if (p.extraProperties) {
          const extraProperties = p.extraProperties.reduce((oep, ep) => {
            return { ...oep, [ep]: '' };
          }, {});
          return { ...o, ...extraProperties };
        }
        return o;
      }, {});
    }
    return {};
  };

  OB.App.TicketExtraProperties = {};

  OB.App.TicketExtraProperties.getAll = () => {
    return getTicketExtraProperties();
  };
})();
