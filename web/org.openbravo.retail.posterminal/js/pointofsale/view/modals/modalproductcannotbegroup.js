/*
 ************************************************************************************
 * Copyright (C) 2012 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */

/*global B, Backbone, $, _, enyo */

enyo.kind({
  kind: 'OB.UI.ModalInfo',
  name: 'OB.OBPOSPointOfSale.UI.Modals.ModalProductCannotBeGroup',
  myId: 'modalProductCannotBeGroup',
  header: OB.I18N.getLabel('OBPOS_productCannotBeGroupHeader'),
  bodyContent: {
    content: OB.I18N.getLabel('OBPOS_productCannotBeGroupMessage'),
  }
});