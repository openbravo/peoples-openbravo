/*
 ************************************************************************************
 * Copyright (C) 2020 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

OB.App.StateAPI.registerModel('TicketList', {
  tickets: [], // keeps the information of the tickets currently in the ticket list
  addedIds: [] // ids of the tickets added into the list ordered by when they were added into the list
});
